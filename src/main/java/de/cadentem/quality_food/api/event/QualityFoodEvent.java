package de.cadentem.quality_food.api.event;

import de.cadentem.quality_food.core.codecs.Quality;
import de.cadentem.quality_food.core.codecs.QualityType;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

import java.util.List;

public abstract class QualityFoodEvent extends Event {
    private final ItemStack stack;
    private final Quality quality;
    private final Holder<QualityType> qualityType;

    protected QualityFoodEvent(final ItemStack stack, final Quality quality, final Holder<QualityType> qualityType) {
        this.stack = stack;
        this.quality = quality;
        this.qualityType = qualityType;
    }

    public ItemStack getStack() {
        return stack;
    }

    public Quality getQuality() {
        return quality;
    }

    public Holder<QualityType> getQualityType() {
        return qualityType;
    }

    /**
     * Fired when {@link de.cadentem.quality_food.util.FoodUtils#handleFoodProperties} computes the modified {@link FoodProperties} for an item with quality.
     */
    public static class ModifyFoodProperties extends QualityFoodEvent {
        private final FoodProperties originalProperties;
        private int nutrition;
        private float saturationModifier;
        private boolean canAlwaysEat;

        public ModifyFoodProperties(final ItemStack stack, final Quality quality, final Holder<QualityType> qualityType, final FoodProperties originalProperties, int nutrition, float saturationModifier, boolean canAlwaysEat) {
            super(stack, quality, qualityType);
            this.originalProperties = originalProperties;
            this.nutrition = nutrition;
            this.saturationModifier = saturationModifier;
            this.canAlwaysEat = canAlwaysEat;
        }

        public FoodProperties getOriginalProperties() {
            return originalProperties;
        }

        public int getNutrition() {
            return nutrition;
        }

        public void setNutrition(int nutrition) {
            this.nutrition = nutrition;
        }

        public float getSaturationModifier() {
            return saturationModifier;
        }

        public void setSaturationModifier(float saturationModifier) {
            this.saturationModifier = saturationModifier;
        }

        public boolean canAlwaysEat() {
            return canAlwaysEat;
        }

        public void setCanAlwaysEat(boolean canAlwaysEat) {
            this.canAlwaysEat = canAlwaysEat;
        }
    }

    /**
     * Fired when {@link de.cadentem.quality_food.util.FoodUtils#handleConsumable} computes the modified {@link Consumable} for an item with quality.
     */
    public static class ModifyConsumable extends QualityFoodEvent {
        private final Consumable originalConsumable;
        private float consumeSeconds;
        private final List<ConsumeEffect> effects;

        public ModifyConsumable(final ItemStack stack, final Quality quality, final Holder<QualityType> qualityType, final Consumable originalConsumable, float consumeSeconds, final List<ConsumeEffect> effects) {
            super(stack, quality, qualityType);
            this.originalConsumable = originalConsumable;
            this.consumeSeconds = consumeSeconds;
            this.effects = effects;
        }

        public Consumable getOriginalConsumable() {
            return originalConsumable;
        }

        public float getConsumeSeconds() {
            return consumeSeconds;
        }

        public void setConsumeSeconds(float consumeSeconds) {
            this.consumeSeconds = consumeSeconds;
        }

        public List<ConsumeEffect> getEffects() {
            return effects;
        }
    }

    /**
     * Fired when an entity consumes an item that has quality.
     * Cancelling this event skips executing registered custom {@link de.cadentem.quality_food.api.QualityConsumeEffect} callbacks.
     */
    public static class Consumed extends QualityFoodEvent implements ICancellableEvent {
        private final Level level;
        private final LivingEntity entity;
        private final Consumable consumable;

        public Consumed(final Level level, final LivingEntity entity, final ItemStack stack, final Consumable consumable, final Quality quality, final Holder<QualityType> qualityType) {
            super(stack, quality, qualityType);
            this.level = level;
            this.entity = entity;
            this.consumable = consumable;
        }

        public Level getLevel() {
            return level;
        }

        public LivingEntity getEntity() {
            return entity;
        }

        public Consumable getConsumable() {
            return consumable;
        }
    }
}

