package com.lockdown;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

/** /lockdown start | stop | reset | interval <seconds>, applied to every online player. */
public final class LockdownCommand {
    private LockdownCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("lockdown")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("start")
                        .executes(ctx -> apply(ctx.getSource(), "Lockdown started", LockState::start)))
                .then(CommandManager.literal("stop")
                        .executes(ctx -> apply(ctx.getSource(), "Lockdown stopped", LockState::stop)))
                .then(CommandManager.literal("reset")
                        .executes(ctx -> apply(ctx.getSource(), "Lockdown reset", LockState::reset)))
                .then(CommandManager.literal("interval")
                        .then(CommandManager.argument("seconds", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    int seconds = IntegerArgumentType.getInteger(ctx, "seconds");
                                    return apply(ctx.getSource(), "Lockdown interval set to " + seconds + "s",
                                            state -> state.setIntervalTicks(seconds * 20));
                                }))));
    }

    private static int apply(ServerCommandSource source, String message, Consumer<LockState> action) {
        List<ServerPlayerEntity> players = source.getMinecraftServer().getPlayerManager().getPlayerList();
        for (ServerPlayerEntity player : players) {
            action.accept(((LockdownPlayer) player).lockdown$getState());
        }
        source.sendFeedback(new LiteralText(message), true);
        return players.size();
    }
}
