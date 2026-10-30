package de.cadentem.quality_food.core.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cadentem.quality_food.QualityFood;
import de.cadentem.quality_food.core.FoodQualityRules;
import de.cadentem.quality_food.registry.QFComponents;
import de.cadentem.quality_food.util.QualityLevels;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.CommonHooks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record Quality(Identifier type, int level, Optional<List<ConsumeEffect>> effects) {
    public static final Quality NONE = new Quality(QualityFood.location("none"), 0, Optional.empty());
    public static final Quality PLAYER_PLACED = new Quality(QualityFood.location("player_placed"), -1, Optional.empty());

    /** The level is re-calculated when reading, see {@link #normalize(Quality)} */
    public static final Codec<Quality> CODEC = RecordCodecBuilder.<Quality>create(builder -> builder.group(
                    Identifier.CODEC.fieldOf("type").forGetter(Quality::type),
                    Codec.INT.fieldOf("level").forGetter(Quality::level),
                    ConsumeEffect.CODEC.listOf().optionalFieldOf("effects").forGetter(Quality::effects))
            .apply(builder, Quality::new)).xmap(Quality::normalize, quality -> quality);

    public static final StreamCodec<RegistryFriendlyByteBuf, Quality> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, Quality::type,
            ByteBufCodecs.INT, Quality::level,
            ByteBufCodecs.optional(ConsumeEffect.STREAM_CODEC.apply(ByteBufCodecs.list())), Quality::effects,
            Quality::new
    );

    /** May return a direct holder of {@link QualityType#NONE} */
    public Holder<QualityType> getType() {
        if (this == NONE) {
            return Holder.direct(QualityType.NONE);
        }

        HolderLookup.RegistryLookup<QualityType> lookup = CommonHooks.resolveLookup(QFComponents.QUALITY_TYPE_REGISTRY);

        if (lookup == null) {
            return Holder.direct(QualityType.NONE);
        }

        Optional<Holder.Reference<QualityType>> optional = lookup.get(QFComponents.key(type));

        if (optional.isPresent()) {
            return optional.get();
        } else {
            return Holder.direct(QualityType.NONE);
        }
    }

    /**
     * The stored level can be outdated (e.g. a lower quality type was added after the item got its quality) <br>
     * It is set to the current position of the type in the sequence (see {@link QualityLevels}) - the type itself (and therefore its values) stays the same
     */
    public static Quality normalize(final Quality quality) {
        if (quality.type().equals(NONE.type()) && quality.effects().isEmpty()) {
            return NONE;
        }

        if (quality.type().equals(PLAYER_PLACED.type()) && quality.effects().isEmpty()) {
            return PLAYER_PLACED;
        }

        HolderLookup.RegistryLookup<QualityType> lookup = CommonHooks.resolveLookup(QFComponents.QUALITY_TYPE_REGISTRY);

        if (lookup == null) {
            return quality;
        }

        Optional<Holder.Reference<QualityType>> type = lookup.get(QFComponents.key(quality.type()));

        if (type.isEmpty()) {
            // The type was removed - keep the stored data
            return quality;
        }

        int level = QualityLevels.get(type.get());
        return level == quality.level() ? quality : new Quality(quality.type(), level, quality.effects());
    }

    /** @param level The level in the sequence, see {@link QualityLevels} */
    public static Quality getRandom(final ItemStack stack, int level) {
        HolderLookup.RegistryLookup<QualityType> lookup = CommonHooks.resolveLookup(QFComponents.QUALITY_TYPE_REGISTRY);

        if (lookup == null) {
            return Quality.NONE;
        }

        List<Holder<QualityType>> types = new ArrayList<>();

        for (Holder.Reference<QualityType> type : lookup.listElements().toList()) {
            if (QualityLevels.get(type) == level && FoodQualityRules.isAllowed(stack, type)) {
                types.add(type);
            }
        }

        if (types.isEmpty()) {
            return Quality.NONE;
        }

        Collections.shuffle(types);
        return QualityType.createQuality(types.getFirst(), stack);
    }
}
