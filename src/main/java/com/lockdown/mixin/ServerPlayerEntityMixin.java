package com.lockdown.mixin;

import com.lockdown.LockEnforcer;
import com.lockdown.LockState;
import com.lockdown.Lockdown;
import com.lockdown.LockdownPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Attaches LockState to each server player, ticks it, saves it to player NBT (relog), and
 * copies it to the new player entity on respawn (death, leaving the End).
 */
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements LockdownPlayer {
    @Unique private final LockState lockdown$state = new LockState();

    @Override
    public LockState lockdown$getState() {
        return lockdown$state;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void lockdown$tick(CallbackInfo ci) {
        LockEnforcer.tick((ServerPlayerEntity) (Object) this, lockdown$state);
    }

    @Inject(method = "writeCustomDataToTag", at = @At("TAIL"))
    private void lockdown$save(CompoundTag tag, CallbackInfo ci) {
        tag.put(Lockdown.MOD_ID, lockdown$state.toTag());
    }

    @Inject(method = "readCustomDataFromTag", at = @At("TAIL"))
    private void lockdown$load(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains(Lockdown.MOD_ID, 10)) lockdown$state.fromTag(tag.getCompound(Lockdown.MOD_ID));
    }

    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void lockdown$copy(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        lockdown$state.copyFrom(((LockdownPlayer) oldPlayer).lockdown$getState());
    }
}
