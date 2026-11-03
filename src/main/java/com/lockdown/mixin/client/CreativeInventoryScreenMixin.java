package com.lockdown.mixin.client;

import com.lockdown.Lockdown;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The creative screen overrides onMouseClick and edits the inventory directly in several
 * branches (number key swaps from item tabs, for example), skipping ScreenHandler.onSlotClick.
 */
@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin {
    @Inject(method = "onMouseClick", at = @At("HEAD"), cancellable = true)
    private void lockdown$blockLocked(Slot slot, int slotId, int button, SlotActionType action, CallbackInfo ci) {
        if (Lockdown.blocksClick(MinecraftClient.getInstance().player, slot, button, action)) ci.cancel();
    }
}
