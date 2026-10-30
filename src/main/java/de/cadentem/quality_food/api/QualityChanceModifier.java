package de.cadentem.quality_food.api;

import de.cadentem.quality_food.core.codecs.QualityType;
import de.cadentem.quality_food.util.HarvestContext;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@FunctionalInterface
public interface QualityChanceModifier {
    double modify(Context context, double currentChance);

    enum RollType {
        GENERIC,
        HARVEST,
        CRAFTING,
        COOKING
    }

    record Context(
            RollType rollType,
            ItemStack stack,
            Holder<QualityType> candidateType,
            @Nullable Player player,
            @Nullable HarvestContext harvestContext,
            @Nullable Collection<ItemStack> ingredients,
            double bonusOrPotential
    ) { }
}

