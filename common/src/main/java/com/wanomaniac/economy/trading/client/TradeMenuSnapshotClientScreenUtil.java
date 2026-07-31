package com.wanomaniac.economy.trading.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

public class TradeMenuSnapshotClientScreenUtil {

    // When a player presses ESC for a couple of ms, it should close the screen and not open another one.
    // It might be good UX to do that.
    private static final long HOLD_THRESHOLD_MS = 400;
    public static boolean longEscape = false;
    private static boolean isEscDown = false;
    private static long escPressedTime = 0;

    public static void render(TradeMenuSnapshotClientScreen screen, GuiGraphics graphics, int mouseX, int mouseY, float delta){
        long holdDuration = System.currentTimeMillis() - escPressedTime;
        if(isEscDown && holdDuration > 20){
            float progress = Math.min(1.0f, (float) holdDuration / HOLD_THRESHOLD_MS);
            int centerX = screen.width / 2;
            int barWidth = 160;
            int barHeight = 8;
            int barLeft = centerX - (barWidth / 2);
            // Push it slightly below the center inventory graphic so it doesn't block the actual trade items
            int barTop = 1;

            // Dark gray empty bar track
            graphics.fill(barLeft, barTop, barLeft + barWidth, barTop + barHeight, 0xFF222222);

            // Dynamic green fill bar
            int filledWidth = (int) (barWidth * progress);
            graphics.fill(barLeft, barTop, barLeft + filledWidth, barTop + barHeight, 0xFF55FF55);
        }
    }

    public static void renderBg(TradeMenuSnapshotClientScreen screen, GuiGraphics graphics, float partialTick, int mouseX, int mouseY, int i, int j, int imageWidth) {
        int rows = screen.getMenu().getRowCount(); // 7
        int headerH = 17;
        int rowTileH = 18;
        int footerH = 96;

        int textureW = 256;
        int textureH = 256;
        int rowTileV = 17;
        int footerV = 126;

        // --- SIDE PANEL RENDERING & RESIZE SAFETY ---
        int footerY = j + headerH + (rows * rowTileH);
        int playerModelSize = 30;
        // Dynamically clamp the left side coordinates so they never go off-screen
        int leftPlayerX = Math.max(15, i - 55);
        int leftPlayerY = j + 5;

        // Clamp the right side coordinates so they don't exceed the window width
        int rightPlayerX = Math.min(screen.width - 45, i + imageWidth + 15);
        int leftX1 = leftPlayerX;               // Top-left boundary X
        int leftY1 = leftPlayerY + 40;          // Top-left boundary Y
        int leftY2 = leftY1 + 70;               // Bottom-right boundary Y (height of bounding box)

        int rightX1 = rightPlayerX;             // Top-left boundary X
        int rightY1 = footerY + 40;             // Top-left boundary Y
        int rightY2 = rightY1 + 70;             // Bottom-right boundary Y (height of bounding box)

        // --- FIXED SIDE PANEL ANCHORING (FULLSCREEN COMPATIBLE) ---
        // Mirror the exact distance on both sides of the main asset (e.g., 40 pixels out)
        int panelOffset = 40;

        // Left side center is 'i' minus half the panel's visual width
        int fixedLeftCenterX = i - panelOffset;

        // Right side center is the right edge of the GUI plus half the panel's visual width
        int fixedRightCenterX = i + imageWidth + panelOffset;

        // Ensure vertical offsets stay aligned underneath your layout boxes
        int firstStackY = leftY2 + 6;
        int secondStackY = leftY2 + 6;
        int textSpacing = 11;

        if (screen.getMenu().guiData.getTraders().size() >= 2) {
            UUID firstPlayerUUID = screen.getMenu().guiData.getTraders().get(0);
            UUID secondPlayerUUID = screen.getMenu().guiData.getTraders().get(1);

            long firstIn = screen.getMenu().guiData.getIncomingMoney().getOrDefault(firstPlayerUUID, 0L);
            long firstOut = screen.getMenu().guiData.getOutgoingMoney().getOrDefault(firstPlayerUUID, 0L);
            long firstTotal = firstIn - firstOut;

            long secondIn = screen.getMenu().guiData.getIncomingMoney().getOrDefault(secondPlayerUUID, 0L);
            long secondOut = screen.getMenu().guiData.getOutgoingMoney().getOrDefault(secondPlayerUUID, 0L);
            long secondTotal = secondIn - secondOut;

            // --- FIRST TRADER (Left Panel) ---
            graphics.drawCenteredString(MenuUtil.getFont(), Component.literal("In: " + firstIn), fixedLeftCenterX, firstStackY, 0xFF00FF00);
            firstStackY += textSpacing;
            graphics.drawCenteredString(MenuUtil.getFont(), Component.literal("Out: " + firstOut), fixedLeftCenterX, firstStackY, 0xFFFF5555);
            firstStackY += textSpacing;

            String firstTotalStr = (firstTotal >= 0 ? "+" : "") + firstTotal;
            int firstTotalColor = firstTotal >= 0 ? 0xFF55FF55 : 0xFFFF5555;
            graphics.drawCenteredString(MenuUtil.getFont(), Component.literal("Total: " + firstTotalStr), fixedLeftCenterX, firstStackY, firstTotalColor);

            // --- SECOND TRADER (Right Panel) ---
            graphics.drawCenteredString(MenuUtil.getFont(), Component.literal("In: " + secondIn), fixedRightCenterX, secondStackY, 0xFF00FF00);
            secondStackY += textSpacing;
            graphics.drawCenteredString(MenuUtil.getFont(), Component.literal("Out: " + secondOut), fixedRightCenterX, secondStackY, 0xFFFF5555);
            secondStackY += textSpacing;

            String secondTotalStr = (secondTotal >= 0 ? "+" : "") + secondTotal;
            int secondTotalColor = secondTotal >= 0 ? 0xFF55FF55 : 0xFFFF5555;
            graphics.drawCenteredString(MenuUtil.getFont(), Component.literal("Total: " + secondTotalStr), fixedRightCenterX, secondStackY, secondTotalColor);
        }

        if(screen.getMenu().guiData.cancelled()){
            Component reasonComponent = Component.literal("Cancelled: " + screen.getMenu().guiData.getCancelReason());
            int textX = (screen.width - MenuUtil.getFont().width(reasonComponent)) / 2; // Horizontally centered
            int textY = screen.height - 10;

            // Draw it with a clean red error color (0xFFFF5555) and a shadow enabled
            graphics.drawString(MenuUtil.getFont(), reasonComponent, textX, textY, 0xFFFF5555, true);
        }

    }

    public static boolean startEscUXThingy(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            // Track when the key was first pressed down
            if (!isEscDown) {
                isEscDown = true;
                escPressedTime = System.currentTimeMillis();
            }

            return true;
        } else {
            isEscDown = false;
            return false;
        }
    }

    public static boolean decideEscUXTHingy(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (isEscDown) {
                isEscDown = false;
                long holdDuration = System.currentTimeMillis() - escPressedTime;

                // If they held it long enough, completely close the screen
                return holdDuration >= HOLD_THRESHOLD_MS;
            }
        }
        return false;
    }






}
