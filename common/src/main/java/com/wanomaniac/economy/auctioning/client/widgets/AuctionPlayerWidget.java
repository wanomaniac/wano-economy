package com.wanomaniac.economy.auctioning.client.widgets;

import com.wanomaniac.economy.auctioning.client.AuctionUtils;
import com.wanomaniac.economy.auctioning.server.ItemBidding;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.Objects;
import java.util.UUID;

public class AuctionPlayerWidget {
    public static final int CARD_HEIGHT = 24;
    public static final int CARD_WIDTH = 120;

    /**
     * Renders a single player entry (Head + Name + Role)
     */
    public static void renderPlayerCard(GuiGraphics graphics, int x, int y, UUID playerUuid, String name, String roleTitle, int roleColor) {
        Minecraft mc = Minecraft.getInstance();

        // 1. Draw Background Card Container
        graphics.fill(x, y, x + CARD_WIDTH, y + CARD_HEIGHT, 0x88000000); // Dark semi-transparent background
        graphics.fill(x, y, x + 2, y + CARD_HEIGHT, roleColor);          // Role indicator stripe on left edge

        if (mc.getConnection() != null) {
            PlayerInfo playerInfo = mc.getConnection().getPlayerInfo(playerUuid);
            if (playerInfo != null) {
                PlayerFaceRenderer.draw(graphics, playerInfo.getSkin(), x + 6, y + 4, 16);
            } else {
                int faceX = x + 6;
                int faceY = y + 4;
                int faceSize = 16;

                graphics.fill(faceX, faceY, faceX + faceSize, faceY + faceSize, 0xFF222222);
                Component questionMark = Component.literal("?");
                int textWidth = mc.font.width(questionMark);

                int textX = faceX + (faceSize - textWidth) / 2;
                int textY = faceY + (faceSize - mc.font.lineHeight) / 2 + 1; // +1 for visual vertical alignment
                graphics.drawString(mc.font, questionMark, textX, textY, 0xFFFF3333, false); // Bright red
            }
        }

        Component roleComponent = Component.literal(roleTitle.toUpperCase());
        graphics.drawString(mc.font, roleComponent, x + 28, y + 3, roleColor, false);

        Component nameComponent;
        if(Objects.equals(name, Minecraft.getInstance().player.getName().toString())){
            nameComponent = Component.literal(name).withStyle(ChatFormatting.ITALIC).append(" (YOU)");
        } else nameComponent = Component.literal(name);

        graphics.drawString(mc.font, nameComponent, x + 28, y + 13, 0xFFFFFFFF, false);
    }

    public static void renderPlayFace(GuiGraphics graphics, int x, int y, UUID playerUuid){
        Minecraft mc = Minecraft.getInstance();

        if (playerUuid != null && mc.getConnection() != null) {
            PlayerInfo playerInfo = mc.getConnection().getPlayerInfo(playerUuid);
            if (playerInfo != null) {
                PlayerFaceRenderer.draw(graphics, playerInfo.getSkin(), x + 6, y + 4, 16);
            } else {
                int faceX = x + 6;
                int faceY = y + 4;
                int faceSize = 16;

                graphics.fill(faceX, faceY, faceX + faceSize, faceY + faceSize, 0xFF222222);
                Component questionMark = Component.literal("?");
                int textWidth = mc.font.width(questionMark);

                int textX = faceX + (faceSize - textWidth) / 2;
                int textY = faceY + (faceSize - mc.font.lineHeight) / 2 + 1; // +1 for visual vertical alignment
                graphics.drawString(mc.font, questionMark, textX, textY, 0xFFFF3333, false); // Bright red
            }
        }
    }

    public static void renderBiddingPlayerText(GuiGraphics graphics, ItemBidding currentBidding){
        int faceSize = 16;
        int spacing = 4;
        Component text = Component.literal(AuctionUtils.getPlayerUsername(currentBidding.highestBidder()) + " won by bidding $" + currentBidding.currentBid());
        int textWidth = Minecraft.getInstance().font.width(text);
        int totalWidth = faceSize + spacing + textWidth;
        int startX = (Minecraft.getInstance().screen.width / 2 ) - (totalWidth / 2);
        int textY = 45;
        int fontHeight = 9;
        int faceY = textY + (fontHeight - faceSize) / 2;

        AuctionPlayerWidget.renderPlayFace(graphics, startX-5, faceY-5, currentBidding.highestBidder());
        graphics.drawString(
                Minecraft.getInstance().font,
                text,
                startX + faceSize + spacing,
                textY,
                0xFFFFFFFF,
                true
        );
    }
}
