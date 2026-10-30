package de.cadentem.quality_food.registry;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import de.cadentem.quality_food.QualityFood;
import de.cadentem.quality_food.api.FoodQualities;
import de.cadentem.quality_food.api.QualityFoodAPI;
import de.cadentem.quality_food.core.FoodQualityRules;
import de.cadentem.quality_food.core.codecs.QualityType;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** Provides the quality types and food quality rules registered through {@link QualityFoodAPI} as a required data pack with the lowest priority */
@EventBusSubscriber
public class QFApiPack {
    private static final String ID = QualityFood.MODID + "/api";

    @SubscribeEvent
    public static void addPack(final AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) {
            return;
        }

        PackLocationInfo location = new PackLocationInfo(ID, Component.literal("moFoodQuality: API"), PackSource.BUILT_IN, Optional.empty());
        Pack.Metadata metadata = new Pack.Metadata(Component.literal("Quality types and food quality rules registered through the moFoodQuality API"), PackCompatibility.COMPATIBLE, FeatureFlagSet.of(), List.of());
        Pack pack = new Pack(location, new ResourcesSupplier(), metadata, new PackSelectionConfig(true, Pack.Position.BOTTOM, false));
        event.addRepositorySource(consumer -> consumer.accept(pack));
    }

    private static Map<Identifier, byte[]> encode() {
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        String directory = Registries.elementsDirPath(QFComponents.QUALITY_TYPE_REGISTRY);
        Map<Identifier, byte[]> data = new LinkedHashMap<>();

        QualityFoodAPI.resolveQualityTypes().forEach((id, type) -> QualityType.CODEC.encodeStart(ops, type)
                .resultOrPartial(error -> QualityFood.LOG.error("Failed to encode quality type [{}]: {}", id, error))
                .ifPresent(json -> data.put(id.withPath(directory + "/" + id.getPath() + ".json"), json.toString().getBytes(StandardCharsets.UTF_8))));

        QualityFoodAPI.getFoodQualities().forEach((id, rule) -> FoodQualities.CODEC.encodeStart(ops, rule)
                .resultOrPartial(error -> QualityFood.LOG.error("Failed to encode food qualities [{}]: {}", id, error))
                .ifPresent(json -> data.put(id.withPath(FoodQualityRules.DIRECTORY + "/" + id.getPath() + ".json"), json.toString().getBytes(StandardCharsets.UTF_8))));

        return data;
    }

    private record ResourcesSupplier() implements Pack.ResourcesSupplier {
        @Override
        public PackResources openPrimary(final PackLocationInfo location) {
            return new Resources(location, encode());
        }

        @Override
        public PackResources openFull(final PackLocationInfo location, final Pack.Metadata metadata) {
            return openPrimary(location);
        }
    }

    private record Resources(PackLocationInfo location, Map<Identifier, byte[]> data) implements PackResources {
        @Override
        public @Nullable IoSupplier<InputStream> getRootResource(final String... elements) {
            return null;
        }

        @Override
        public @Nullable IoSupplier<InputStream> getResource(final PackType type, final Identifier id) {
            byte[] bytes = type == PackType.SERVER_DATA ? data.get(id) : null;
            return bytes == null ? null : () -> new ByteArrayInputStream(bytes);
        }

        @Override
        public void listResources(final PackType type, final String namespace, final String path, final ResourceOutput output) {
            if (type != PackType.SERVER_DATA) {
                return;
            }

            String prefix = path.endsWith("/") ? path : path + "/";

            data.forEach((id, bytes) -> {
                if (id.getNamespace().equals(namespace) && id.getPath().startsWith(prefix)) {
                    output.accept(id, () -> new ByteArrayInputStream(bytes));
                }
            });
        }

        @Override
        public Set<String> getNamespaces(final PackType type) {
            return type == PackType.SERVER_DATA ? data.keySet().stream().map(Identifier::getNamespace).collect(Collectors.toSet()) : Set.of();
        }

        @Override
        public <T> @Nullable T getMetadataSection(final MetadataSectionType<T> type) {
            return null;
        }

        @Override
        public void close() { /* Nothing to do */ }
    }
}
