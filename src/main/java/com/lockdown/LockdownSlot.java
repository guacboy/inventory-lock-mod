package com.lockdown;

/**
 * Implemented on Slot by SlotMixin. CreativeSlotMixin overrides it, because the creative
 * inventory wraps the real slot and stores a screen slot id where the inventory index should be.
 */
public interface LockdownSlot {
    int lockdown$inventoryIndex();
}
