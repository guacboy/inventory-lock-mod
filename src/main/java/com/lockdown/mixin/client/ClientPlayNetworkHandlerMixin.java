package com.lockdown.mixin.client;

import com.lockdown.ClientLockState;
import com.lockdown.Lockdown;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    /** Forget the previous world's state, so a server without the mod shows no locks. */
    @Inject(method = "onGameJoin", at = @At("HEAD"))
    private void lockdown$reset(GameJoinS2CPacket packet, CallbackInfo ci) {
        ClientLockState.clear();
    }

    /**
     * Runs on the network thread before vanilla hops to the main thread. Parsing here is
     * fine because ClientLockState publishes an immutable snapshot.
     */
    @Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
    private void lockdown$receive(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        if (!Lockdown.CHANNEL.equals(packet.getChannel())) return;
        PacketByteBuf buf = packet.getData(); // a copy, so it is ours to release
        try {
            ClientLockState.read(buf);
        } finally {
            buf.release();
        }
        ci.cancel();
    }
}
