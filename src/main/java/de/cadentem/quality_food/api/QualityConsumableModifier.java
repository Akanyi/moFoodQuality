package de.cadentem.quality_food.api;

import de.cadentem.quality_food.core.codecs.Quality;
import de.cadentem.quality_food.core.codecs.QualityType;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;

@FunctionalInterface
public interface QualityConsumableModifier {
    Consumable modify(ItemStack stack, Consumable current, Quality quality, Holder<QualityType> qualityType);
}

