package de.cadentem.quality_food.core.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/** Values of a {@link QualityType} which are derived from its level if they are not set */
public record LevelSettings(Optional<Double> cookingBonusDivisor, Optional<Integer> eggHatchBonus, Optional<Integer> eggMultiHatchBonus) {
    public static final LevelSettings DEFAULT = new LevelSettings(Optional.empty(), Optional.empty(), Optional.empty());

    public static final MapCodec<LevelSettings> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                    Codec.DOUBLE.validate(LevelSettings::validateDivisor).optionalFieldOf("cooking_bonus_divisor").forGetter(LevelSettings::cookingBonusDivisor),
                    Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("egg_hatch_bonus").forGetter(LevelSettings::eggHatchBonus),
                    Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("egg_multi_hatch_bonus").forGetter(LevelSettings::eggMultiHatchBonus))
            .apply(builder, LevelSettings::new));

    private static DataResult<Double> validateDivisor(final Double divisor) {
        return divisor > 0 ? DataResult.success(divisor) : DataResult.error(() -> "cooking_bonus_divisor must be greater than 0: " + divisor);
    }
}
