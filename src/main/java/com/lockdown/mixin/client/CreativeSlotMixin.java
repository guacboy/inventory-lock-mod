package com.lockdown.mixin.client;

import com.lockdown.LockdownSlot;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * CreativeSlot wraps a real player slot but passes its screen slot id to the Slot
 * constructor as the index. Report the wrapped slot's inventory index instead.
 */
@Mixin(targets = "net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen$CreativeSlot")
public abstract class CreativeSlotMixin implements LockdownSlot {
    @Shadow @Final private Slot slot;

    @Override
    public int lockdown$inventoryIndex() {
        return ((LockdownSlot) slot).lockdown$inventoryIndex();
    }
}
