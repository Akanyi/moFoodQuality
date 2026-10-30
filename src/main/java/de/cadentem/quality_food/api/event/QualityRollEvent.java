package de.cadentem.quality_food.api.event;

import de.cadentem.quality_food.api.QualityChanceModifier;
import de.cadentem.quality_food.core.codecs.QualityType;
import de.cadentem.quality_food.util.HarvestContext;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Fired on {@link net.neoforged.neoforge.common.NeoForge#EVENT_BUS} when calculating the roll chance
 * for a candidate {@link QualityType}. Cancelling this event skips rolling for this candidate.
 */
public class QualityRollEvent extends Event implements ICancellableEvent {
    private final QualityChanceModifier.Context context;
    private double chance;

    public QualityRollEvent(final QualityChanceModifier.Context context, double chance) {
        this.context = context;
        this.chance = chance;
    }

    public QualityChanceModifier.Context getContext() {
        return context;
    }

    public QualityChanceModifier.RollType getRollType() {
        return context.rollType();
    }

    public ItemStack getStack() {
        return context.stack();
    }

    public Holder<QualityType> getCandidateType() {
        return context.candidateType();
    }

    public @Nullable Player getPlayer() {
        return context.player();
    }

    public @Nullable HarvestContext getHarvestContext() {
        return context.harvestContext();
    }

    public @Nullable Collection<ItemStack> getIngredients() {
        return context.ingredients();
    }

    public double getBonusOrPotential() {
        return context.bonusOrPotential();
    }

    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }
}

