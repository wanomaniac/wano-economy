package com.wanomaniac.economy.auctioning.client;

import com.wanomaniac.economy.auctioning.server.ItemBidding;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class AuctionScreenUtil {
    public static void drawTimerLine(GuiGraphics graphics, ItemBidding currentBidding, int width){
        int modalX = width / 2;

        int barWidth = width; // Full screen width
        int barHeight = 2;    // Slightly sleeker height for full-screen top/bottom bars
        int barY = 0;         // Positioned right at the top edge of the screen (or adjust as needed)

        // Calculate progress ratio (0.0f to 1.0f)
        float totalSeconds = currentBidding.getTotalSeconds();
        float remainingSeconds = currentBidding.getRemainingSeconds();

        // Clamp progress between 0.0 and 1.0 to prevent rendering bugs
        float progress = Math.clamp(remainingSeconds / totalSeconds, 0.0f, 1.0f);

        // Calculate dynamic width based on progress
        int currentBarWidth = (int) (barWidth * progress);

        graphics.fill(0, barY, width, barY + barHeight, 0x44000000); // Semi-transparent black background

        int timerColor;
        if (progress > 0.5f) {
            timerColor = 0xFF55FF55; // Green (>50% time left)
        } else if (progress > 0.2f) {
            timerColor = 0xFFFFFF55; // Yellow (20%-50% time left)
        } else {
            timerColor = 0xFFFF5555; // Red (<20% time left)
        }

        // 3. Draw Shrinking Active Timer Bar (Shrinks from both sides toward the center)
        if (currentBarWidth > 0) {
            int leftX = modalX - (currentBarWidth / 2);
            int rightX = modalX + (currentBarWidth / 2);

            graphics.fill(leftX, barY, rightX, barY + barHeight, timerColor);
        }

        int roundedSeconds = (int) Math.ceil(currentBidding.getRemainingSeconds());
        Component timerText = Component.literal(roundedSeconds + "s remaining");
        graphics.drawCenteredString(Minecraft.getInstance().font, timerText, modalX, barY + barHeight + 4, timerColor);
    }
}
