package com.lockdown.mixin;

import com.lockdown.Lockdown;
import com.lockdown.LockdownSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Locked slots accept nothing and give nothing. */
@Mixin(Slot.class)
public abstract class SlotMixin implements LockdownSlot {
    @Shadow @Final private int index;

    @Override
    public int lockdown$inventoryIndex() {
        return index;
    }

    @Inject(method = "canInsert", at = @At("HEAD"), cancellable = true)
    private void lockdown$refuseInsert(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (Lockdown.isLocked((Slot) (Object) this)) cir.setReturnValue(false);
    }

    @Inject(method = "canTakeItems", at = @At("HEAD"), cancellable = true)
    private void lockdown$refuseTake(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (Lockdown.isLocked((Slot) (Object) this)) cir.setReturnValue(false);
    }

    // Do not touch doDrawHoveringEffect: in 1.16.1 HandledScreen.render skips drawSlot entirely
    // when it returns false, which would also hide the lock overlay.
}
