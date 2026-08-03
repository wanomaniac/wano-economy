package com.wanomaniac.economy.auctioning.client.widgets;

import com.wanomaniac.economy.auctioning.client.AuctionUtils;
import com.wanomaniac.economy.auctioning.server.ItemBidding;
import com.wanomaniac.economy.auctioning.types.AuctionGuiData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AuctionPlayerListWidget {
    public static void renderBiddingSidebar(GuiGraphics graphics, AuctionGuiData data, ItemBidding currentBidding, int mouseX, int mouseY) {
        int startX = 10; // Anchored on the left side
        int startY = 30; // Positioned below top bar / headers
        int spacing = 28; // Padding between player cards

        int currentY = startY;

        graphics.drawString(Minecraft.getInstance().font, Component.literal("Bidding"), startX, currentY + 2, 0xFFAAAAAA, false);
        currentY += 12;
        if (currentBidding.highestBidder() != null) {
            AuctionPlayerWidget.renderPlayerCard(
                    graphics,
                    startX,
                    currentY,
                    currentBidding.highestBidder(),
                    AuctionUtils.getPlayerUsername(currentBidding.highestBidder()),
                    "Current Bidder",
                    0xFF55FF55 // Green
            );
            currentY += spacing;
        } else {
            // No bids yet
            AuctionPlayerWidget.renderPlayerCard(
                    graphics,
                    startX,
                    currentY,
                    null,
                    "No Bids Yet",
                    "Top Bidder",
                    0xFFAAAAAA // Light Grey
            );
            currentY += spacing;
        }

        // ---  RECENT BIDDERS LIST ---
        graphics.drawString(Minecraft.getInstance().font, Component.literal("Bid History"), startX, currentY + 2, 0xFFAAAAAA, false);
        currentY += 12;
        List<Map.Entry<UUID, Long>> recentBids = currentBidding.getTopBids(4); // todo: remove limit
        for (Map.Entry<UUID, Long> bid : recentBids) {
            AuctionPlayerWidget.renderPlayerCard(
                    graphics,
                    startX,
                    currentY,
                    bid.getKey(),
                    AuctionUtils.getPlayerUsername(bid.getKey()) + " ($" + bid.getValue() + ")",
                    "Bidder",
                    0xFF55FFFF // Aqua / Cyan
            );
            currentY += spacing;
        }
    }

        public static void drawRightAlignedString(GuiGraphics graphics, Font font, Component text, int rightX, int y, int color, boolean shadow) {
            int textWidth = font.width(text);
            graphics.drawString(font, text, rightX - textWidth, y, color, shadow);
        }

        public static void renderAuctionSidebar(GuiGraphics graphics, ItemBidding currentBidding, AuctionGuiData data, List<UUID> bidders, List<UUID> biddersInactive, int width, int mouseX, int mouseY) {
            int startX = width - 120 - 10;
            int startY = 30; // Positioned below top bar / headers
            int spacing = 28; // Padding between player cards

            int currentY = startY;

            // ---  RECENT BIDDERS LIST ---
            drawRightAlignedString(graphics, Minecraft.getInstance().font, Component.literal("Auction"), width - 10, currentY + 2, 0xFFAAAAAA, false);
            currentY += 12;
            AuctionPlayerWidget.renderPlayerCard(
                    graphics,
                    startX,
                    currentY,
                    data.auctioneer(),
                    AuctionUtils.getPlayerUsername(data.auctioneer()),
                    "Auctioneer",
                    0xFFFFAA00 // Gold / Orange
            );
            currentY += spacing;
            for (UUID player : bidders) {
                boolean isPlayerInactive = biddersInactive.contains(player);
                if(currentBidding == null){
                    AuctionPlayerWidget.renderPlayerCard(
                            graphics,
                            startX,
                            currentY,
                            player,
                            AuctionUtils.getPlayerUsername(player),
                            "Player",
                            isPlayerInactive ? 0xFFAAAAAA : 0xFF55FFFF
                    );
                }
                else {
                    if (currentBidding.getPlayerBidding(player) != 0) {
                        AuctionPlayerWidget.renderPlayerCard(
                                graphics,
                                startX,
                                currentY,
                                player,
                                AuctionUtils.getPlayerUsername(player) + " ($" + currentBidding.getPlayerBidding(player) + ")",
                                "Bidder",
                                isPlayerInactive ? 0xFFAAAAAA : 0xFF55FFFF
                        );
                    } else {
                        AuctionPlayerWidget.renderPlayerCard(
                                graphics,
                                startX,
                                currentY,
                                player,
                                AuctionUtils.getPlayerUsername(player),
                                "Not bidded",
                                isPlayerInactive ? 0xFFAAAAAA : 0xFF55FFFF // Aqua / Cyan
                        );
                    }
                }
                currentY += spacing;
            }
    }
}
