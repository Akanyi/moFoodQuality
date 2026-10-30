package de.cadentem.quality_food.api;

import de.cadentem.quality_food.core.codecs.Effect;
import de.cadentem.quality_food.core.codecs.LevelSettings;
import de.cadentem.quality_food.core.codecs.QualityType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Builds a {@link QualityType} - unset optional fields use the same defaults as the datapack format <br>
 * The ranges are the same as the ones enforced by {@link QualityType#CODEC}
 */
public final class QualityTypeBuilder {
    private int level;
    private double chance;
    private double weight;
    private double minWeight;
    private double durationMultiplier = 1;
    private double probabilityMultiplier = 1;
    private int amplifierModifier;
    private double nutritionMultiplier = 1;
    private double saturationMultiplier = 1;
    private double craftingBonus;
    private double cookingBonus;
    private double cropMultiplier = 1;
    private double seedMultiplier = 1;
    private double potentialBonus;
    private @Nullable Double cookingBonusDivisor;
    private @Nullable Integer eggHatchBonus;
    private @Nullable Integer eggMultiHatchBonus;
    private @Nullable List<Effect> effects;
    private @Nullable Identifier icon;
    private @Nullable Component name;

    private QualityTypeBuilder() { }

    public static QualityTypeBuilder create() {
        return new QualityTypeBuilder();
    }

    public static QualityTypeBuilder copyOf(final QualityType type) {
        return create()
                .level(type.level())
                .chance(type.chance())
                .weight(type.weight())
                .minWeight(type.minWeight())
                .durationMultiplier(type.durationMultiplier())
                .probabilityMultiplier(type.probabilityMultiplier())
                .amplifierModifier(type.amplifierModifier())
                .nutritionMultiplier(type.nutritionMultiplier())
                .saturationMultiplier(type.saturationMultiplier())
                .craftingBonus(type.craftingBonus())
                .cookingBonus(type.cookingBonus())
                .cropMultiplier(type.cropMultiplier())
                .seedMultiplier(type.seedMultiplier())
                .potentialBonus(type.potentialBonus())
                .cookingBonusDivisor(type.levelSettings().cookingBonusDivisor().orElse(null))
                .eggHatchBonus(type.levelSettings().eggHatchBonus().orElse(null))
                .eggMultiHatchBonus(type.levelSettings().eggMultiHatchBonus().orElse(null))
                .effects(type.effects().orElse(null))
                .icon(type.icon())
                .name(type.name());
    }

    /**
     * Anchor which orders this quality type in the sequence (higher ones are rolled for first) - can be below 1 to be placed below iron <br>
     * The level stored on items is the position in the sequence, see {@link de.cadentem.quality_food.util.QualityLevels}
     */
    public QualityTypeBuilder level(int level) {
        this.level = level;
        return this;
    }

    /** Base chance (0 - 1) of this quality being applied */
    public QualityTypeBuilder chance(double chance) {
        this.chance = chance;
        return this;
    }

    /** Used to calculate the chance when crafting with or harvesting quality ingredients */
    public QualityTypeBuilder weight(double weight) {
        this.weight = weight;
        return this;
    }

    /** The weight at which the calculated chance starts to increase */
    public QualityTypeBuilder minWeight(double minWeight) {
        this.minWeight = minWeight;
        return this;
    }

    /** Multiplies the duration of beneficial effects (and divides the duration of harmful ones) */
    public QualityTypeBuilder durationMultiplier(double durationMultiplier) {
        this.durationMultiplier = durationMultiplier;
        return this;
    }

    /** Multiplies the probability of beneficial effects (and divides the probability of harmful ones) */
    public QualityTypeBuilder probabilityMultiplier(double probabilityMultiplier) {
        this.probabilityMultiplier = probabilityMultiplier;
        return this;
    }

    /** Added to the amplifier of beneficial effects (and subtracted from harmful ones) */
    public QualityTypeBuilder amplifierModifier(int amplifierModifier) {
        this.amplifierModifier = amplifierModifier;
        return this;
    }

    public QualityTypeBuilder nutritionMultiplier(double nutritionMultiplier) {
        this.nutritionMultiplier = nutritionMultiplier;
        return this;
    }

    public QualityTypeBuilder saturationMultiplier(double saturationMultiplier) {
        this.saturationMultiplier = saturationMultiplier;
        return this;
    }

    /** Additive bonus (0 - 1) to the crafting roll, split across the quality ingredients */
    public QualityTypeBuilder craftingBonus(double craftingBonus) {
        this.craftingBonus = craftingBonus;
        return this;
    }

    /** Bonus (0 - 100) each cooked item of this quality stores in the cooking block */
    public QualityTypeBuilder cookingBonus(double cookingBonus) {
        this.cookingBonus = cookingBonus;
        return this;
    }

    /** Chance multiplier when this quality is rolled for harvested crops */
    public QualityTypeBuilder cropMultiplier(double cropMultiplier) {
        this.cropMultiplier = cropMultiplier;
        return this;
    }

    /** Chance multiplier when this quality is rolled for harvested seeds */
    public QualityTypeBuilder seedMultiplier(double seedMultiplier) {
        this.seedMultiplier = seedMultiplier;
        return this;
    }

    /** Potential (0 - 1) an animal gains when it is fed food of this quality */
    public QualityTypeBuilder potentialBonus(double potentialBonus) {
        this.potentialBonus = potentialBonus;
        return this;
    }

    /** The stored cooking bonus is divided by this value when rolling for this quality - {@code null} derives it from the level ({@code level * level}) */
    public QualityTypeBuilder cookingBonusDivisor(@Nullable final Double cookingBonusDivisor) {
        this.cookingBonusDivisor = cookingBonusDivisor;
        return this;
    }

    /** Subtracted from the vanilla {@code 1 in 8} egg hatch chance - {@code null} derives it from the level ({@code level * 2}) */
    public QualityTypeBuilder eggHatchBonus(@Nullable final Integer eggHatchBonus) {
        this.eggHatchBonus = eggHatchBonus;
        return this;
    }

    /** Subtracted from the vanilla {@code 1 in 32} chance to hatch 4 chicken - {@code null} derives it from the level ({@code level * 4}) */
    public QualityTypeBuilder eggMultiHatchBonus(@Nullable final Integer eggMultiHatchBonus) {
        this.eggMultiHatchBonus = eggMultiHatchBonus;
        return this;
    }

    public QualityTypeBuilder effects(@Nullable final List<Effect> effects) {
        this.effects = effects == null || effects.isEmpty() ? null : List.copyOf(effects);
        return this;
    }

    /** Sprite location (within {@code textures/gui/sprites}) */
    public QualityTypeBuilder icon(final Identifier icon) {
        this.icon = icon;
        return this;
    }

    public QualityTypeBuilder name(final Component name) {
        this.name = name;
        return this;
    }

    public int level() {
        return level;
    }

    public double chance() {
        return chance;
    }

    public double weight() {
        return weight;
    }

    public double minWeight() {
        return minWeight;
    }

    /** @throws IllegalStateException If a value is outside the range of the datapack format or the icon / name is missing */
    public QualityType build() {
        check(chance >= 0 && chance <= 1, "chance must be between 0 and 1");
        check(weight >= 0, "weight must be >= 0");
        check(minWeight >= 0, "min_weight must be >= 0");
        check(durationMultiplier >= 0, "duration_multiplier must be >= 0");
        check(probabilityMultiplier >= 0, "probability_multiplier must be >= 0");
        check(amplifierModifier >= -255 && amplifierModifier <= 255, "amplifier_modifier must be between -255 and 255");
        check(nutritionMultiplier >= 0, "nutrition_multiplier must be >= 0");
        check(saturationMultiplier >= 0, "saturation_multiplier must be >= 0");
        check(craftingBonus >= 0 && craftingBonus <= 1, "crafting_bonus must be between 0 and 1");
        check(cookingBonus >= 0 && cookingBonus <= 100, "cooking_bonus must be between 0 and 100");
        check(cropMultiplier >= 0, "crop_multiplier must be >= 0");
        check(seedMultiplier >= 0, "seed_multiplier must be >= 0");
        check(potentialBonus >= 0 && potentialBonus <= 1, "potential_bonus must be between 0 and 1");
        check(cookingBonusDivisor == null || cookingBonusDivisor > 0, "cooking_bonus_divisor must be > 0");
        check(eggHatchBonus == null || eggHatchBonus >= 0, "egg_hatch_bonus must be >= 0");
        check(eggMultiHatchBonus == null || eggMultiHatchBonus >= 0, "egg_multi_hatch_bonus must be >= 0");
        check(icon != null, "icon is required");
        check(name != null, "name is required");

        LevelSettings levelSettings = new LevelSettings(Optional.ofNullable(cookingBonusDivisor), Optional.ofNullable(eggHatchBonus), Optional.ofNullable(eggMultiHatchBonus));
        return new QualityType(level, chance, weight, minWeight, durationMultiplier, probabilityMultiplier, amplifierModifier, nutritionMultiplier, saturationMultiplier, craftingBonus, cookingBonus, cropMultiplier, seedMultiplier, potentialBonus, levelSettings, Optional.ofNullable(effects), icon, name);
    }

    private static void check(boolean condition, final String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
