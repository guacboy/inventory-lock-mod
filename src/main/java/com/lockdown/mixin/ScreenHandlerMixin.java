package com.lockdown.mixin;

import com.lockdown.Lockdown;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Refuses every click on a locked slot (pickup, shift click, throw, clone, drag) and number
 * key or F swaps into one. Runs on both sides, so client prediction and server agree.
 */
@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {
    @Shadow @Final public List<Slot> slots;

    @Inject(method = "onSlotClick", at = @At("HEAD"), cancellable = true)
    private void lockdown$blockLocked(int slotId, int button, SlotActionType action, PlayerEntity player,
                                      CallbackInfoReturnable<ItemStack> cir) {
        Slot slot = slotId >= 0 && slotId < slots.size() ? slots.get(slotId) : null;
        if (Lockdown.blocksClick(player, slot, button, action)) cir.setReturnValue(ItemStack.EMPTY);
    }
}
