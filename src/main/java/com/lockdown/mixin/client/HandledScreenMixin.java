package com.lockdown.mixin.client;

import com.lockdown.Lockdown;
import com.lockdown.client.LockOverlay;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Overlays locked player slots in every container screen, creative included. */
@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    @Inject(method = "drawSlot", at = @At("TAIL"))
    private void lockdown$drawLock(MatrixStack matrices, Slot slot, CallbackInfo ci) {
        if (Lockdown.isLocked(slot)) LockOverlay.drawSlot(matrices, slot.x, slot.y);
    }
}
