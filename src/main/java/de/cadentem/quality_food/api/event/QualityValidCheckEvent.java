package de.cadentem.quality_food.api.event;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.Event;

/**
 * Events for dynamically overriding whether an {@link ItemStack} or {@link Block} is valid for quality.
 */
public abstract class QualityValidCheckEvent extends Event {
    public enum Result {
        DEFAULT,
        ALLOW,
        DENY
    }

    private Result result = Result.DEFAULT;

    public Result getResult() {
        return result;
    }

    public void setResult(final Result result) {
        this.result = result;
    }

    public static class Item extends QualityValidCheckEvent {
        private final ItemStack stack;
        private final boolean checkBlock;

        public Item(final ItemStack stack, boolean checkBlock) {
            this.stack = stack;
            this.checkBlock = checkBlock;
        }

        public ItemStack getStack() {
            return stack;
        }

        public boolean isCheckBlock() {
            return checkBlock;
        }
    }

    public static class BlockCheck extends QualityValidCheckEvent {
        private final Block block;
        private final boolean checkItem;

        public BlockCheck(final Block block, boolean checkItem) {
            this.block = block;
            this.checkItem = checkItem;
        }

        public Block getBlock() {
            return block;
        }

        public boolean isCheckItem() {
            return checkItem;
        }
    }
}

