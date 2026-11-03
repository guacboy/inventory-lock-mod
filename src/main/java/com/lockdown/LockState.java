package com.lockdown;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;

/** Per player lock progress. Lives on ServerPlayerEntity and is saved in its NBT. */
public final class LockState {
    private boolean running;
    private int lockedCount;
    private int intervalTicks = Lockdown.DEFAULT_INTERVAL_TICKS;
    private int ticksRemaining = intervalTicks;

    public int getLockedCount() {
        return lockedCount;
    }

    public void start() {
        running = true;
    }

    public void stop() {
        running = false;
    }

    public void reset() {
        running = false;
        lockedCount = 0;
        ticksRemaining = intervalTicks;
    }

    public void setIntervalTicks(int ticks) {
        intervalTicks = Math.max(1, ticks);
        ticksRemaining = Math.min(ticksRemaining, intervalTicks);
    }

    /** Advances the countdown by one tick and locks the next slot when it reaches zero. */
    public void tick() {
        if (!running || lockedCount >= Lockdown.TOTAL) return;
        if (--ticksRemaining > 0) return;
        lockedCount++;
        ticksRemaining = intervalTicks;
    }

    public void copyFrom(LockState other) {
        running = other.running;
        lockedCount = other.lockedCount;
        intervalTicks = other.intervalTicks;
        ticksRemaining = other.ticksRemaining;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Running", running);
        tag.putInt("Locked", lockedCount);
        tag.putInt("Interval", intervalTicks);
        tag.putInt("Remaining", ticksRemaining);
        return tag;
    }

    public void fromTag(CompoundTag tag) {
        running = tag.getBoolean("Running");
        lockedCount = Math.max(0, Math.min(Lockdown.TOTAL, tag.getInt("Locked")));
        intervalTicks = Math.max(1, tag.getInt("Interval"));
        ticksRemaining = Math.max(1, Math.min(intervalTicks, tag.getInt("Remaining")));
    }

    /** Sync payload layout, read back by ClientLockState.read. */
    public void write(PacketByteBuf buf) {
        buf.writeBoolean(running);
        buf.writeVarInt(lockedCount);
        buf.writeVarInt(ticksRemaining);
    }
}
