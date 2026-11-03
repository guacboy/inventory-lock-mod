package com.lockdown.client;

import com.lockdown.ClientLockState;
import com.lockdown.Lockdown;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;

/** Draws the locked slot overlay and the countdown text. */
public final class LockOverlay {
    private static final int FILL_COLOR = 0xB4500000;
    private static final int X_COLOR = 0xFFFF4040;
    private static final int TEXT_COLOR = 0xFFFFFF;
    // GUI items render at z 100..250, so draw above them but below tooltips (z 400).
    private static final float Z = 300.0F;

    private LockOverlay() {
    }

    /** Dark red box with a pixel X over the 16x16 slot whose top left corner is (x, y). */
    public static void drawSlot(MatrixStack matrices, int x, int y) {
        matrices.push();
        matrices.translate(0.0, 0.0, Z);
        DrawableHelper.fill(matrices, x, y, x + 16, y + 16, FILL_COLOR);
        for (int i = 2; i < 14; i++) {
            DrawableHelper.fill(matrices, x + i, y + i, x + i + 1, y + i + 1, X_COLOR);
            DrawableHelper.fill(matrices, x + 15 - i, y + i, x + 16 - i, y + i + 1, X_COLOR);
        }
        matrices.pop();
    }

    /** Called at the end of InGameHud.render. Mirrors the slot positions in renderHotbar. */
    public static void drawHud(MatrixStack matrices, MinecraftClient client, int scaledWidth, int scaledHeight) {
        PlayerEntity player = client.player;
        if (player == null || client.options.hudHidden) return;
        ClientLockState state = ClientLockState.get();

        if (!player.isSpectator()) {
            int center = scaledWidth / 2;
            int itemY = scaledHeight - 19;
            for (int hotbar = 0; hotbar < 9; hotbar++) {
                if (Lockdown.isLocked(hotbar, state.lockedCount)) {
                    drawSlot(matrices, center - 90 + hotbar * 20 + 2, itemY);
                }
            }
            if (Lockdown.isLocked(Lockdown.OFFHAND_INDEX, state.lockedCount)) {
                boolean offhandOnLeft = player.getMainArm() == Arm.RIGHT;
                drawSlot(matrices, offhandOnLeft ? center - 91 - 26 : center + 91 + 10, itemY);
            }
        }

        if (client.options.debugEnabled || (!state.running && state.lockedCount == 0)) return;
        String status;
        if (state.lockedCount >= Lockdown.TOTAL) {
            status = "All slots locked";
        } else if (state.running) {
            int seconds = (state.ticksRemaining + 19) / 20;
            status = String.format("Next lock in %d:%02d", seconds / 60, seconds % 60);
        } else {
            status = "Lockdown paused";
        }
        client.textRenderer.drawWithShadow(matrices, status, 4, 4, TEXT_COLOR);
        client.textRenderer.drawWithShadow(matrices, "Locked " + state.lockedCount + "/" + Lockdown.TOTAL, 4, 14, TEXT_COLOR);
    }
}
