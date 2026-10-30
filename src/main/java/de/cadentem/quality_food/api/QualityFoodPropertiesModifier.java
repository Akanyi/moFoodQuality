package de.cadentem.quality_food.api;

import de.cadentem.quality_food.core.codecs.Quality;
import de.cadentem.quality_food.core.codecs.QualityType;
import net.minecraft.core.Holder;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface QualityFoodPropertiesModifier {
    FoodProperties modify(ItemStack stack, FoodProperties current, Quality quality, Holder<QualityType> qualityType);
}

