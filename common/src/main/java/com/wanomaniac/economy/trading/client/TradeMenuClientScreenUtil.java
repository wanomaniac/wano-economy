package com.wanomaniac.economy.trading.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.player.RemotePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class TradeMenuClientScreenUtil {
    public static void render(TradeMenuClientScreen screen, GuiGraphics g, int mouseX, int mouseY, float delta) {
        if (screen.playerCurrentMoney == -1L) {
            g.drawCenteredString(Minecraft.getInstance().font, Component.literal("Loading..."), screen.width / 2, screen.height / 2, 0xFFFFFFFF);
            return;
        }
        screen.extraMoneyField.render(g, mouseX, mouseY, delta);
    }

    private static Player getPlayerFromUUID(UUID id) {
        assert Minecraft.getInstance().level != null;
        return Minecraft.getInstance().level.getPlayerByUUID(id);
    }

    public static void renderBg(TradeMenuClientScreen screen, GuiGraphics graphics, float partialTick, int mouseX, int mouseY, int i, int j, int imageWidth) {
        if (screen.playerCurrentMoney == -1L || screen.extraMoneyField == null) return;

        int rows = screen.getMenu().getRowCount(); // 7
        int headerH = 17;
        int rowTileH = 18;
        int footerH = 96;

        int textureW = 256;
        int textureH = 256;
        int rowTileV = 17;
        int footerV = 126;

        // --- SIDE PANEL RENDERING & RESIZE SAFETY ---
        int playerModelSize = 30;
        // Dynamically clamp the left side coordinates so they never go off-screen
        int leftPlayerX = Math.max(15, i - 55);
        int leftPlayerY = j + 5;
        int leftCenterX = leftPlayerX + (playerModelSize / 2);
        int leftTopY = leftPlayerY - 12;
        int footerY = j + headerH + (rows * rowTileH);

        // Clamp the right side coordinates so they don't exceed the window width
        int rightPlayerX = Math.min(screen.width - 45, i + imageWidth + 15);
        int rightCenterX = rightPlayerX + (playerModelSize / 2);
        int rightTopY = footerY - 12;

        long value;
        try {
            value = Long.parseLong(screen.extraMoneyField.getValue());
        } catch (NumberFormatException e) {
            value = screen.lastMoney;
        }

        // --- NEW UNIFIED VERTICAL TEXT STACK (All below EditBox) ---
        int editBoxCenterX = screen.extraMoneyField.getX() + (screen.extraMoneyField.getWidth() / 2);
        int currentY = screen.extraMoneyField.getY() + screen.extraMoneyField.getHeight() + 6; // Start 6px below editbox
        int spacing = 11; // Standard vertical spacing (font height + margin)

        // 1. Left Player "YOU!" label
        graphics.drawCenteredString(getFont(), Component.literal("YOU!"), editBoxCenterX, currentY, 0xFFFFFFFF);
        currentY += spacing;

        // 2. Money Text (Cash)
        String moneyString = "Cash: " + (screen.otherPlayerMoney > 0 ? screen.playerCurrentMoney - value + screen.otherPlayerMoney : screen.playerCurrentMoney - value);
        graphics.drawCenteredString(getFont(), Component.literal(moneyString), editBoxCenterX, currentY, 0xFF00FF00);
        currentY += spacing;

        // 3. Outgoing trade money (Out)
        graphics.drawCenteredString(getFont(), Component.literal("Out: " + value), editBoxCenterX, currentY, 0xFFFFFFFF);
        currentY += spacing;

        // 4. Partner incoming money (In)
        if (screen.otherPlayerMoney > 0) {
            graphics.drawCenteredString(getFont(), Component.literal("In: " + screen.otherPlayerMoney), editBoxCenterX, currentY, 0xFFFFFFFF);
            currentY += spacing;
        }

        // 5. Status / Warning Text (Added with minor padding)
        currentY += 4;
        if (!screen.extraMoneyField.isFocused() && screen.hasTextboxChanged && screen.textBoxButtonStatus) {
            Component statusText = screen.hasSynchronizedMoney ?
                    Component.literal("Saved! Ready up.") :
                    Component.literal("Saving....");
            int color = screen.hasSynchronizedMoney ? 0xFF00FF00 : 0xFFFF0000;
            graphics.drawCenteredString(getFont(), statusText, editBoxCenterX, currentY, color);
        } else if (!screen.textBoxButtonStatus) {
            graphics.drawCenteredString(getFont(), Component.literal("Unready to edit!"), editBoxCenterX, currentY, 0xFFFF0000);
        }

        // --- LEFT PLAYER (YOU) ---
        int leftX1 = leftPlayerX;               // Top-left boundary X
        int leftY1 = leftPlayerY + 40;          // Top-left boundary Y
        int leftX2 = leftX1 + 50;               // Bottom-right boundary X (width of bounding box)
        int leftY2 = leftY1 + 70;               // Bottom-right boundary Y (height of bounding box)

        InventoryScreen.renderEntityInInventoryFollowsMouse(
                graphics,
                leftX1, leftY1,                 // Box top-left
                leftX2, leftY2,                 // Box bottom-right
                playerModelSize,                // Scale (30)
                0.0625F,                        // Pivot offset
                (float) mouseX, (float) mouseY,   // Mouse coordinates
                screen.getMenu().guiData.isA() ? getPlayerFromUUID(screen.getMenu().guiData.playerA()) : getPlayerFromUUID(screen.getMenu().guiData.playerB())
        );

// --- RIGHT PLAYER (PARTNER) ---
        int rightX1 = rightPlayerX;             // Top-left boundary X
        int rightY1 = footerY + 40;             // Top-left boundary Y
        int rightX2 = rightX1 + 50;             // Bottom-right boundary X (width of bounding box)
        int rightY2 = rightY1 + 70;             // Bottom-right boundary Y (height of bounding box)

        LivingEntity thirdPartyPlayer = screen.dummyThirdPlayer == null ? screen.getMenu().guiData.isA() ? getPlayerFromUUID(screen.getMenu().guiData.playerB()) : getPlayerFromUUID(screen.getMenu().guiData.playerA()) : screen.dummyThirdPlayer;
        if (thirdPartyPlayer == null && screen.dummyThirdPlayer == null) {
            assert Minecraft.getInstance().level != null;
            screen.dummyThirdPlayer = new RemotePlayer(Minecraft.getInstance().level, Objects.requireNonNull(Objects.requireNonNull(Minecraft.getInstance().getConnection()).getPlayerInfo(screen.getMenu().guiData.isA() ? screen.getMenu().guiData.playerB() : screen.getMenu().guiData.playerA())).getProfile());
            thirdPartyPlayer = screen.dummyThirdPlayer;
        }

        if (thirdPartyPlayer != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    graphics,
                    rightX1, rightY1,               // Box top-left
                    rightX2, rightY2,               // Box bottom-right
                    playerModelSize,                // Scale (30)
                    0.0625F,                        // Pivot offset
                    (float) mouseX, (float) mouseY,   // Mouse coordinates
                    thirdPartyPlayer
            );
        }
    }

    private static @NotNull Font getFont() {
        return Minecraft.getInstance().font;
    }

}
