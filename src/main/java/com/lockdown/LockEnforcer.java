package com.lockdown;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;

/** Server side per tick work: advance the timer, clear locked slots, sync the client. */
public final class LockEnforcer {
    private LockEnforcer() {
    }

    public static void tick(ServerPlayerEntity player, LockState state) {
        state.tick();

        int locked = state.getLockedCount();
        for (int r = 0; r < locked; r++) {
            evict(player, Lockdown.ORDER[r], locked);
        }

        if (player.networkHandler != null) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            state.write(buf);
            player.networkHandler.sendPacket(new CustomPayloadS2CPacket(Lockdown.CHANNEL, buf));
        }
    }

    /**
     * Moves the stack out of a locked slot into the next open slot in lock order. If no open
     * slot is empty it tops up matching stacks, and whatever is left gets dropped.
     */
    private static void evict(ServerPlayerEntity player, int index, int locked) {
        PlayerInventory inventory = player.inventory;
        ItemStack stack = inventory.getStack(index);
        if (stack.isEmpty()) return;
        inventory.setStack(index, ItemStack.EMPTY);

        for (int r = locked; r < Lockdown.TOTAL; r++) {
            int target = Lockdown.ORDER[r];
            if (inventory.getStack(target).isEmpty()) {
                inventory.setStack(target, stack);
                return;
            }
        }

        for (int r = locked; r < Lockdown.TOTAL && !stack.isEmpty(); r++) {
            ItemStack target = inventory.getStack(Lockdown.ORDER[r]);
            if (ItemStack.areItemsEqual(target, stack) && ItemStack.areTagsEqual(target, stack)) {
                int limit = Math.min(target.getMaxCount(), inventory.getMaxCountPerStack());
                int moved = Math.min(stack.getCount(), limit - target.getCount());
                if (moved > 0) {
                    target.increment(moved);
                    stack.decrement(moved);
                }
            }
        }

        if (!stack.isEmpty()) {
            player.dropItem(stack, false, true);
        }
    }
}
