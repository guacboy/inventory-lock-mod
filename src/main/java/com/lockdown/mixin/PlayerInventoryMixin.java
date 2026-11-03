package com.lockdown.mixin;

import com.lockdown.Lockdown;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Item pickups and other inserts find room through getEmptySlot, so skipping locked slots
 * here keeps them out. Locked slots are always empty, so the "stack onto an existing stack"
 * path can never pick one.
 */
@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
    @Shadow @Final public DefaultedList<ItemStack> main;
    @Shadow @Final public PlayerEntity player;

    @Inject(method = "getEmptySlot", at = @At("HEAD"), cancellable = true)
    private void lockdown$skipLocked(CallbackInfoReturnable<Integer> cir) {
        int locked = Lockdown.lockedCount(player);
        if (locked == 0) return;
        for (int i = 0; i < main.size(); i++) {
            if (main.get(i).isEmpty() && !Lockdown.isLocked(i, locked)) {
                cir.setReturnValue(i);
                return;
            }
        }
        cir.setReturnValue(-1);
    }
}
