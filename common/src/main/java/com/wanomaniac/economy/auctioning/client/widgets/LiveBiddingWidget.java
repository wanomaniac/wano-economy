package com.wanomaniac.economy.auctioning.client.widgets;

import com.wanomaniac.economy.auctioning.client.AuctionUtils;
import com.wanomaniac.economy.auctioning.server.ItemBidding;
import com.wanomaniac.economy.auctioning.types.AuctionGuiData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.*;

public class LiveBiddingWidget extends AbstractScrollableListWidget {
    private ItemBidding currentBidding = null;
    private List<Map.Entry<UUID, Long>> filteredBidHistory = null;

    public LiveBiddingWidget(int x, int y, int width, int height) {
        super(x, y, width, height, 14, 28, "Search a player/price.");
    }

    public void updateData(ItemBidding bid) {
        currentBidding = bid;
        String query = searchBox.getValue();
        if(query.isEmpty()) filteredBidHistory = null;
        else {
            filteredBidHistory = bid.bidHistory().entrySet().stream()
                    .filter(Objects::nonNull)
                    .filter(bidding -> {
                        UUID winner = bidding.getKey();
                        String username = (winner != null) ? AuctionUtils.getPlayerUsername(winner).get() : "Unsold";
                        if (username.toLowerCase(Locale.ROOT).contains(query)) return true;

                        return bidding.getValue().toString().contains(query);
                    })
                    .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                    .toList();
        }
    }

    @Override
    protected int getTopStaticHeight() {
        return 12 + itemSpacing;
    }

    @Override
    protected void renderContent(GuiGraphics graphics, int viewTop, int viewBottom, int currentY, int mouseX, int mouseY) {
        int staticHeaderHeight = 12 + itemSpacing; // 12px label + card height
        int scrollAreaTop = viewTop + staticHeaderHeight;
        int scrollY = scrollAreaTop - this.scrollOffset + 2;

        graphics.enableScissor(x, scrollAreaTop, x + AuctionPlayerWidget.CARD_WIDTH, viewBottom);
        List<Map.Entry<UUID, Long>> recentBids;
        if(filteredBidHistory == null) recentBids = currentBidding.bidHistory().entrySet().stream().sorted((a, b) -> Long.compare(b.getValue(), a.getValue())).toList();
        else recentBids = filteredBidHistory;

        if(recentBids.isEmpty()){
            AuctionPlayerWidget.renderPlayerCard(
                    graphics,
                    x,
                    scrollY,
                    null,
                    filteredBidHistory == null ? "No bids Yet" : "Invalid search",
                    filteredBidHistory == null ? "Bids here" : "No Results",
                    0xFFAAAAAA // Gray
            );
        }
        for (Map.Entry<UUID, Long> bid : recentBids) {
            if (scrollY + itemSpacing >= scrollAreaTop && scrollY <= viewBottom) {
                AuctionPlayerWidget.renderPlayerCard(
                        graphics,
                        x,
                        scrollY,
                        bid.getKey(),
                        AuctionUtils.getPlayerUsername(bid.getKey()).get() + " ($" + bid.getValue() + ")",
                        "Bidder",
                        0xFF55FFFF // Aqua / Cyan
                );
            }
            scrollY += itemSpacing;
        }

        graphics.disableScissor();
        graphics.drawString(Minecraft.getInstance().font, Component.literal("Bid History"), x, viewTop + itemSpacing, 0xFFAAAAAA, false);
        graphics.drawString(Minecraft.getInstance().font, Component.literal("Bidding"), x, y + 2, 0xFFAAAAAA, false);
        if (currentBidding.highestBidder() != null) {
            AuctionPlayerWidget.renderPlayerCard(
                    graphics,
                    x,
                    viewTop,
                    currentBidding.highestBidder(),
                    AuctionUtils.getPlayerUsername(currentBidding.highestBidder()).get(),
                    "Current Bidder",
                    0xFF55FF55 // Green
            );
        } else {
            AuctionPlayerWidget.renderPlayerCard(
                    graphics,
                    x,
                    viewTop,
                    null,
                    "No Bids Yet",
                    "Top Bidder",
                    0xFFAAAAAA // Light Grey
            );
        }
    }

    @Override
    protected int getItemCount() {
        if(currentBidding == null) return 2; // only 2 widgets added.

        if(filteredBidHistory == null) return currentBidding.bidHistory().size();
        else return filteredBidHistory.size();
    }
}
