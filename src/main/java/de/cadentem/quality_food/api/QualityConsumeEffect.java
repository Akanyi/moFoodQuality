package de.cadentem.quality_food.api;

import de.cadentem.quality_food.core.codecs.Quality;
import de.cadentem.quality_food.core.codecs.QualityType;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;

@FunctionalInterface
public interface QualityConsumeEffect {
    void onConsume(Level level, LivingEntity user, ItemStack stack, Consumable consumable, Quality quality, Holder<QualityType> qualityType);
}

