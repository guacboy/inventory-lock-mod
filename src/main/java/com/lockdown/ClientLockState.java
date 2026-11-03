package com.lockdown;

import net.minecraft.network.PacketByteBuf;

/**
 * The lock state last received from the server. Immutable snapshots behind a volatile
 * reference, since packets arrive on the network thread and are read on the render thread.
 * Kept free of client only classes so common code (Lockdown.lockedCount) can reference it.
 */
public final class ClientLockState {
    private static final ClientLockState EMPTY = new ClientLockState(false, 0, 0);
    private static volatile ClientLockState current = EMPTY;

    public final boolean running;
    public final int lockedCount;
    public final int ticksRemaining;

    private ClientLockState(boolean running, int lockedCount, int ticksRemaining) {
        this.running = running;
        this.lockedCount = lockedCount;
        this.ticksRemaining = ticksRemaining;
    }

    public static ClientLockState get() {
        return current;
    }

    public static void read(PacketByteBuf buf) {
        current = new ClientLockState(buf.readBoolean(), buf.readVarInt(), buf.readVarInt());
    }

    public static void clear() {
        current = EMPTY;
    }
}
