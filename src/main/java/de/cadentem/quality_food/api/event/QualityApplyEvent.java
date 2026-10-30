package de.cadentem.quality_food.api.event;

import de.cadentem.quality_food.core.codecs.Quality;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Fired on {@link net.neoforged.neoforge.common.NeoForge#EVENT_BUS} when a {@link Quality} is applied to an {@link ItemStack}.
 */
public abstract class QualityApplyEvent extends Event {
    private final ItemStack stack;
    protected Quality quality;
    private final boolean canUpgrade;

    protected QualityApplyEvent(final ItemStack stack, final Quality quality, boolean canUpgrade) {
        this.stack = stack;
        this.quality = quality;
        this.canUpgrade = canUpgrade;
    }

    public ItemStack getStack() {
        return stack;
    }

    public Quality getQuality() {
        return quality;
    }

    public boolean canUpgrade() {
        return canUpgrade;
    }

    /**
     * Fired before the quality is written to the stack.
     * Can modify the quality to apply, or cancel the application completely.
     */
    public static class Pre extends QualityApplyEvent implements ICancellableEvent {
        public Pre(final ItemStack stack, final Quality quality, boolean canUpgrade) {
            super(stack, quality, canUpgrade);
        }

        public void setQuality(final Quality quality) {
            this.quality = quality;
        }
    }

    /**
     * Fired after the quality has been successfully applied to the stack.
     */
    public static class Post extends QualityApplyEvent {
        public Post(final ItemStack stack, final Quality quality, boolean canUpgrade) {
            super(stack, quality, canUpgrade);
        }
    }
}

