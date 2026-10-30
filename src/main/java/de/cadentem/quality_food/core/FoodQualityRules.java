package de.cadentem.quality_food.core;

import de.cadentem.quality_food.QualityFood;
import de.cadentem.quality_food.api.FoodQualities;
import de.cadentem.quality_food.core.codecs.Quality;
import de.cadentem.quality_food.core.codecs.QualityType;
import de.cadentem.quality_food.registry.QFComponents;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Server-side {@link FoodQualities} rules, loaded from {@code data/<namespace>/quality_food/food_qualities} */
@EventBusSubscriber
public class FoodQualityRules {
    public static final String DIRECTORY = QualityFood.MODID + "/food_qualities";

    private static volatile List<Rule> rules = List.of();

    private record Rule(Identifier id, Set<Identifier> items, Set<TagKey<Item>> tags, Set<Identifier> qualities, int priority) {
        boolean matches(final ItemStack stack) {
            if (items.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()))) {
                return true;
            }

            for (TagKey<Item> tag : tags) {
                if (stack.is(tag)) {
                    return true;
                }
            }

            return false;
        }
    }

    @SubscribeEvent
    public static void addListener(final AddServerReloadListenersEvent event) {
        event.addListener(QualityFood.location("food_qualities"), new Listener(event.getRegistryAccess()));
    }

    /** @return The quality types the item can get, {@code null} if no rule limits it */
    public static @Nullable Set<Identifier> getAllowed(final ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        for (Rule rule : rules) {
            if (rule.matches(stack)) {
                return rule.qualities();
            }
        }

        return null;
    }

    /** @param allowed The result of {@link #getAllowed(ItemStack)} */
    public static boolean isAllowed(@Nullable final Set<Identifier> allowed, final Holder<QualityType> type) {
        return allowed == null || type.unwrapKey().map(key -> allowed.contains(key.identifier())).orElse(false);
    }

    public static boolean isAllowed(final ItemStack stack, final Holder<QualityType> type) {
        return isAllowed(getAllowed(stack), type);
    }

    public static boolean isAllowed(final ItemStack stack, final Quality quality) {
        Set<Identifier> allowed = getAllowed(stack);
        return allowed == null || allowed.contains(quality.type());
    }

    private static class Listener extends SimpleJsonResourceReloadListener<FoodQualities> {
        private final RegistryAccess access;

        Listener(final RegistryAccess access) {
            super(FoodQualities.CODEC, FileToIdConverter.json(DIRECTORY));
            this.access = access;
        }

        @Override
        protected void apply(final Map<Identifier, FoodQualities> entries, final ResourceManager manager, final ProfilerFiller profiler) {
            Optional<HolderLookup.RegistryLookup<QualityType>> types = access.lookup(QFComponents.QUALITY_TYPE_REGISTRY).map(registry -> registry);
            List<Rule> loaded = new ArrayList<>();

            entries.forEach((id, entry) -> {
                Set<Identifier> items = new HashSet<>();
                Set<TagKey<Item>> tags = new HashSet<>();

                for (String item : entry.items()) {
                    if (item.startsWith("#")) {
                        tags.add(TagKey.create(Registries.ITEM, Identifier.parse(item.substring(1))));
                    } else {
                        items.add(Identifier.parse(item));
                    }
                }

                types.ifPresent(lookup -> entry.qualities().stream()
                        .filter(quality -> lookup.get(QFComponents.key(quality)).isEmpty())
                        .forEach(quality -> QualityFood.LOG.warn("Food quality rule [{}] references the unknown quality type [{}]", id, quality)));

                loaded.add(new Rule(id, Set.copyOf(items), Set.copyOf(tags), Set.copyOf(entry.qualities()), entry.priority()));
            });

            loaded.sort(Comparator.comparingInt(Rule::priority).reversed().thenComparing(rule -> rule.id().toString()));
            rules = List.copyOf(loaded);
            QualityFood.LOG.info("Loaded {} food quality rules", loaded.size());
        }
    }
}
