package com.lockdown.mixin.client;

import com.lockdown.client.LockOverlay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow @Final private MinecraftClient client;
    @Shadow private int scaledWidth;
    @Shadow private int scaledHeight;

    @Inject(method = "render", at = @At("TAIL"))
    private void lockdown$render(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        LockOverlay.drawHud(matrices, client, scaledWidth, scaledHeight);
    }
}
