package de.cadentem.quality_food.api;

import de.cadentem.quality_food.QualityFood;
import de.cadentem.quality_food.core.codecs.QualityType;
import de.cadentem.quality_food.registry.QFComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/**
 * Material quality presets (icon, translation and values) in sequence order <br>
 * {@link #IRON}, {@link #GOLD} and {@link #DIAMOND} are registered by default, the others can be enabled through {@link QualityFoodAPI#registerMaterialQualities(MaterialQuality...)} <br>
 * The defined levels are fixed anchors (wood {@code -2} ... iron {@code 1}, gold {@code 2}, diamond {@code 3} ... ender {@code 5}) - the level of an item is the position in the sequence, see {@link de.cadentem.quality_food.util.QualityLevels}
 */
public enum MaterialQuality {
    WOOD("wood"),
    STONE("stone"),
    COPPER("copper"),
    IRON("iron"),
    GOLD("gold"),
    DIAMOND("diamond"),
    NETHERITE("netherite"),
    ENDER("ender");

    private final String path;

    MaterialQuality(final String path) {
        this.path = path;
    }

    public Identifier id() {
        return QualityFood.location(path);
    }

    public ResourceKey<QualityType> key() {
        return QFComponents.key(id());
    }

    public Identifier icon() {
        return QualityFood.location("quality_icon/" + path);
    }

    /** A new builder with the preset values of this material - the values which would otherwise be derived from the level are set explicitly */
    public QualityTypeBuilder builder() {
        QualityTypeBuilder builder = switch (this) {
            case WOOD -> QualityTypeBuilder.create()
                    .chance(0.25).weight(0.1).minWeight(0)
                    .durationMultiplier(1.1).probabilityMultiplier(1.05).amplifierModifier(0)
                    .nutritionMultiplier(1.1).saturationMultiplier(1.05)
                    .craftingBonus(0.05).cookingBonus(0.05)
                    .potentialBonus(0.005)
                    .cookingBonusDivisor(1d).eggHatchBonus(1).eggMultiHatchBonus(1);
            case STONE -> QualityTypeBuilder.create()
                    .chance(0.18).weight(0.2).minWeight(0)
                    .durationMultiplier(1.2).probabilityMultiplier(1.1).amplifierModifier(0)
                    .nutritionMultiplier(1.2).saturationMultiplier(1.1)
                    .craftingBonus(0.08).cookingBonus(0.1)
                    .potentialBonus(0.01)
                    .cookingBonusDivisor(1d).eggHatchBonus(1).eggMultiHatchBonus(2);
            case COPPER -> QualityTypeBuilder.create()
                    .chance(0.14).weight(0.35).minWeight(0)
                    .durationMultiplier(1.3).probabilityMultiplier(1.15).amplifierModifier(0)
                    .nutritionMultiplier(1.3).saturationMultiplier(1.15)
                    .craftingBonus(0.1).cookingBonus(0.2)
                    .potentialBonus(0.015)
                    .cookingBonusDivisor(1d).eggHatchBonus(1).eggMultiHatchBonus(3);
            case IRON -> QualityTypeBuilder.create()
                    .chance(0.1).weight(1).minWeight(0)
                    .durationMultiplier(1.5).probabilityMultiplier(1.25).amplifierModifier(1)
                    .nutritionMultiplier(1.5).saturationMultiplier(1.25)
                    .craftingBonus(0.15).cookingBonus(0.35)
                    .potentialBonus(0.025)
                    .cookingBonusDivisor(1d).eggHatchBonus(2).eggMultiHatchBonus(4);
            case GOLD -> QualityTypeBuilder.create()
                    .chance(0.03).weight(2).minWeight(1)
                    .durationMultiplier(2).probabilityMultiplier(1.5).amplifierModifier(2)
                    .nutritionMultiplier(2).saturationMultiplier(1.5)
                    .craftingBonus(0.4).cookingBonus(1)
                    .cropMultiplier(0.9).seedMultiplier(0.9)
                    .potentialBonus(0.1)
                    .cookingBonusDivisor(4d).eggHatchBonus(4).eggMultiHatchBonus(8);
            case DIAMOND -> QualityTypeBuilder.create()
                    .chance(0.005).weight(3).minWeight(1.75)
                    .durationMultiplier(2.5).probabilityMultiplier(2).amplifierModifier(3)
                    .nutritionMultiplier(2.5).saturationMultiplier(1.75)
                    .craftingBonus(0.7).cookingBonus(2)
                    .cropMultiplier(0.75).seedMultiplier(0.75)
                    .potentialBonus(0.2)
                    .cookingBonusDivisor(9d).eggHatchBonus(6).eggMultiHatchBonus(12);
            case NETHERITE -> QualityTypeBuilder.create()
                    .chance(0.001).weight(4).minWeight(3)
                    .durationMultiplier(3).probabilityMultiplier(2.5).amplifierModifier(4)
                    .nutritionMultiplier(3).saturationMultiplier(2)
                    .craftingBonus(0.85).cookingBonus(3)
                    .cropMultiplier(0.6).seedMultiplier(0.6)
                    .potentialBonus(0.3)
                    .cookingBonusDivisor(16d).eggHatchBonus(7).eggMultiHatchBonus(16);
            case ENDER -> QualityTypeBuilder.create()
                    .chance(0.0002).weight(5).minWeight(4)
                    .durationMultiplier(3.5).probabilityMultiplier(3).amplifierModifier(5)
                    .nutritionMultiplier(3.5).saturationMultiplier(2.25)
                    .craftingBonus(1).cookingBonus(4)
                    .cropMultiplier(0.5).seedMultiplier(0.5)
                    .potentialBonus(0.4)
                    .cookingBonusDivisor(25d).eggHatchBonus(7).eggMultiHatchBonus(20);
        };

        return builder.level(ordinal() - IRON.ordinal() + 1).icon(icon()).name(displayName());
    }

    public Component displayName() {
        MutableComponent name = Component.translatable("quality_type.quality_food." + path);

        return switch (this) {
            case WOOD -> name.withColor(0xB8945F);
            case STONE -> name.withColor(0x8C9399);
            case COPPER -> name.withColor(0xE07A4E);
            case IRON -> name.withStyle(ChatFormatting.GRAY);
            case GOLD -> name.withStyle(ChatFormatting.GOLD);
            case DIAMOND -> name.withStyle(ChatFormatting.AQUA);
            case NETHERITE -> name.withColor(0x9A8789);
            case ENDER -> name.withStyle(ChatFormatting.DARK_PURPLE);
        };
    }
}
