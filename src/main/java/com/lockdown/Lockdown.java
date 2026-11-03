package com.lockdown;

import java.util.Arrays;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;

/**
 * Shared constants and lock lookups used by both the server and client mixins.
 */
public final class Lockdown {
    public static final String MOD_ID = "lockdown";
    public static final Identifier CHANNEL = new Identifier(MOD_ID, "state");

    public static final int OFFHAND_INDEX = 40;
    public static final int DEFAULT_INTERVAL_TICKS = 2 * 60 * 20;

    /** PlayerInventory indices in the order they get locked. */
    public static final int[] ORDER = buildOrder();
    public static final int TOTAL = ORDER.length;

    /** RANK[inventoryIndex] = position of that index in ORDER, or -1 if it never locks (armor). */
    private static final int[] RANK = buildRank();

    private Lockdown() {
    }

    private static int[] buildOrder() {
        int[] order = new int[37];
        int n = 0;
        for (int i = 9; i <= 17; i++) order[n++] = i;   // top row, left to right
        for (int i = 26; i >= 18; i--) order[n++] = i;  // middle row, right to left
        for (int i = 27; i <= 35; i++) order[n++] = i;  // bottom row, left to right
        for (int i = 8; i >= 0; i--) order[n++] = i;    // hotbar, right to left
        order[n] = OFFHAND_INDEX;                        // offhand last
        return order;
    }

    private static int[] buildRank() {
        int[] rank = new int[OFFHAND_INDEX + 1];
        Arrays.fill(rank, -1);
        for (int r = 0; r < ORDER.length; r++) rank[ORDER[r]] = r;
        return rank;
    }

    public static boolean isLocked(int inventoryIndex, int lockedCount) {
        if (inventoryIndex < 0 || inventoryIndex >= RANK.length) return false;
        int rank = RANK[inventoryIndex];
        return rank >= 0 && rank < lockedCount;
    }

    /** Server players read their own state, client players read the last synced state. */
    public static int lockedCount(PlayerEntity player) {
        if (player instanceof LockdownPlayer) {
            return ((LockdownPlayer) player).lockdown$getState().getLockedCount();
        }
        if (player != null && player.world.isClient) {
            return ClientLockState.get().lockedCount;
        }
        return 0;
    }

    public static boolean isLocked(PlayerEntity player, int inventoryIndex) {
        return isLocked(inventoryIndex, lockedCount(player));
    }

    /** True if the slot belongs to a player inventory and that inventory index is locked. */
    public static boolean isLocked(Slot slot) {
        if (slot == null || !(slot.inventory instanceof PlayerInventory)) return false;
        PlayerInventory inventory = (PlayerInventory) slot.inventory;
        return isLocked(inventory.player, ((LockdownSlot) slot).lockdown$inventoryIndex());
    }

    /**
     * Whether a container click must be refused. Covers clicks on a locked slot and
     * number key or F swaps (SWAP with button 0..8 or 40) that target a locked slot.
     */
    public static boolean blocksClick(PlayerEntity player, Slot slot, int button, SlotActionType action) {
        if (isLocked(slot)) return true;
        return action == SlotActionType.SWAP && isLocked(player, button);
    }
}
