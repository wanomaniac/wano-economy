package com.wanomaniac.economy.auctioning.client.widgets;

import com.wanomaniac.economy.auctioning.client.AuctionUtils;
import com.wanomaniac.economy.auctioning.server.ItemBidding;
import com.wanomaniac.economy.auctioning.types.AuctionGuiData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class LiveAuctionSidebarWidget extends AbstractScrollableListWidget {
    AuctionGuiData data;
    List<UUID> filteredBidders;
    List<UUID> biddersInactive;
    ItemBidding currentBidding;
    boolean isFiltered;

    public LiveAuctionSidebarWidget(int x, int y, int width, int height) {
        super(x, y, width, height, 14, 28, "Search a player");
    }

    public void updateData(AuctionGuiData data, ItemBidding currentBidding, List<UUID> bidders, List<UUID> biddersInactive){
        this.data = data;
        this.currentBidding = currentBidding;
        String query = searchBox.getValue();
        if(query.isEmpty()) {
            isFiltered = false;
            filteredBidders = bidders;
        }
        else {
            isFiltered = true;
            filteredBidders = bidders.stream().filter((id) -> {
                String username = AuctionUtils.getPlayerUsername(id).get();

                return username.toLowerCase(Locale.ROOT).contains(query);
            }).toList().reversed();
        }

        this.biddersInactive = biddersInactive;
    }

    public static void drawRightAlignedString(GuiGraphics graphics, Font font, Component text, int rightX, int y, int color, boolean shadow) {
        int textWidth = font.width(text);
        graphics.drawString(font, text, rightX - textWidth, y, color, shadow);
    }

    @Override
    protected void renderContent(GuiGraphics graphics, int viewTop, int viewBottom, int currentY, int mouseX, int mouseY) {
        drawRightAlignedString(graphics, Minecraft.getInstance().font, Component.literal("Auction Players"), Minecraft.getInstance().screen.width - 10, y + 2, 0xFFAAAAAA, false);

        graphics.enableScissor(x, viewTop, x + AuctionPlayerWidget.CARD_WIDTH, viewBottom);
        if(!isFiltered) {
            AuctionPlayerWidget.renderPlayerCard(
                    graphics,
                    x,
                    currentY,
                    data.auctioneer(),
                    AuctionUtils.getPlayerUsername(data.auctioneer()).get(),
                    "Auctioneer",
                    0xFFFFAA00 // Gold / Orange
            );
            currentY += itemSpacing;
        }

        if(filteredBidders.isEmpty() && isFiltered){
            AuctionPlayerWidget.renderPlayerCard(
                    graphics,
                    x,
                    currentY,
                    null,
                    "No results.",
                    "Try again",
                    0xFFAAAAAA
            );
            graphics.disableScissor();
            return;
        }

        for (UUID player : filteredBidders) {
            boolean isPlayerInactive = biddersInactive.contains(player);
            if(currentBidding == null){
                AuctionPlayerWidget.renderPlayerCard(
                        graphics,
                        x,
                        currentY,
                        player,
                        AuctionUtils.getPlayerUsername(player).get(),
                        "Player",
                        isPlayerInactive ? 0xFFAAAAAA : 0xFF55FFFF
                );
            }
            else {
                if (currentBidding.getPlayerBidding(player) != 0) {
                    AuctionPlayerWidget.renderPlayerCard(
                            graphics,
                            x,
                            currentY,
                            player,
                            AuctionUtils.getPlayerUsername(player) + " ($" + currentBidding.getPlayerBidding(player) + ")",
                            "Bidder",
                            isPlayerInactive ? 0xFFAAAAAA : 0xFF55FFFF
                    );
                } else {
                    AuctionPlayerWidget.renderPlayerCard(
                            graphics,
                            x,
                            currentY,
                            player,
                            AuctionUtils.getPlayerUsername(player).get(),
                            "Not bidded",
                            isPlayerInactive ? 0xFFAAAAAA : 0xFF55FFFF // Aqua / Cyan
                    );
                }
            }
            currentY += itemSpacing;
        }
        graphics.disableScissor();
    }

    @Override
    protected int getItemCount() {
        if(filteredBidders != null && filteredBidders.isEmpty()) return 2;

        return filteredBidders.size()+1;
    }
}
