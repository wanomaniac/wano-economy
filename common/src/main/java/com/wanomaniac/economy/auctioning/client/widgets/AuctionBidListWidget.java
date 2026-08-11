package com.wanomaniac.economy.auctioning.client.widgets;

import com.wanomaniac.economy.auctioning.client.AuctionUtils;
import com.wanomaniac.economy.auctioning.server.ItemBidding;
import com.wanomaniac.economy.client.GUIInputUtil;
import com.wanomaniac.economy.client.VersionGuiHandler;
import com.wanomaniac.economy.client.input.CursorTypes;
import com.wanomaniac.economy.client.input.MouseButtonEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Consumer;

public class AuctionBidListWidget extends AbstractScrollableListWidget {
    private List<ItemBidding> filteredBids = new ArrayList<>();
    private final Consumer<ItemBidding> onBiddingClick;
    private final boolean clickableAuctions;

    public AuctionBidListWidget(int x, int y, int width, int height, boolean clickableAuctions, Consumer<ItemBidding> onBiddingClick) {
        super(x, y, width, height, 14, 28, "Search a player/item...");
        this.onBiddingClick = onBiddingClick;
        this.clickableAuctions = clickableAuctions;
    }

    public AuctionBidListWidget(int x, int y, int width, int height) {
        this(x, y, width, height, false, null);
    }


    public void updateData(List<ItemBidding> bids) {
        String query = searchBox.getValue().trim().toLowerCase(Locale.ROOT);
        this.filteredBids = bids.stream()
                .filter(Objects::nonNull)
                .filter(bidding -> {
                    if (query.isEmpty()) return true;

                    UUID winner = bidding.highestBidder();
                    String username = (winner != null) ? AuctionUtils.getPlayerUsername(winner).get() : "Unsold";
                    if (username.toLowerCase(Locale.ROOT).contains(query)) return true;

                    if (bidding.item() != null && !bidding.item().isEmpty()) {
                        String itemName = bidding.item().getHoverName().getString();
                        return itemName.toLowerCase(Locale.ROOT).contains(query);
                    }
                    return false;
                })
                .toList()
                .reversed();
    }

    @Override
    protected void renderContent(GuiGraphics graphics, int viewTop, int viewBottom, int currentY, int mouseX, int mouseY) {
        graphics.drawString(Minecraft.getInstance().font, Component.literal("Auction History"), x, y + 2, 0xFFAAAAAA, false);
        if (filteredBids.isEmpty()) {
            AuctionPlayerWidget.renderPlayerCard(graphics, x, viewTop, null, searchBox.getValue().isEmpty() ? "No Past Bids" : "No Results" , searchBox.getValue().isEmpty() ? "Empty History" : "Invalid search", 0xFFAAAAAA);
            return;
        }

        int renderY = currentY;
        if(clickableAuctions) {
            GUIInputUtil.changeCursor(graphics, CursorTypes.ARROW);
            for (ItemBidding bidding : filteredBids) {
                if (bidding.item() != null && !bidding.item().isEmpty()) {
                    if (mouseY >= viewTop && mouseY <= viewBottom) {
                        if (mouseX >= x && mouseX <= x + AuctionPlayerWidget.CARD_WIDTH && mouseY >= renderY && mouseY <= renderY + AuctionPlayerWidget.CARD_HEIGHT) {
                            // add highlight
                            graphics.fill(x, renderY, x + AuctionPlayerWidget.CARD_WIDTH, renderY + AuctionPlayerWidget.CARD_HEIGHT, 0x88AAAAAA);
                            GUIInputUtil.changeCursor(graphics, CursorTypes.POINTING_HAND);
                        }
                    }
                }
                renderY += itemSpacing;
            }
            renderY = currentY;
        }
        for (ItemBidding bidding : filteredBids) {
            if (renderY + itemSpacing >= viewTop && renderY <= viewBottom) {
                UUID winner = bidding.highestBidder();
                boolean hasWinner = winner != null;

                AuctionPlayerWidget.renderPlayerCard(
                        graphics,
                        x,
                        renderY,
                        winner,
                        hasWinner ? AuctionUtils.getPlayerUsername(winner).get() : "Unsold",
                        hasWinner ? "Won ($" + bidding.currentBid() + ")" : "Expired",
                        hasWinner ? 0xFF55FF55 : 0xFFAAAAAA
                );

                if (bidding.item() != null && !bidding.item().isEmpty()) {
                    VersionGuiHandler.renderItemAndDecorations(graphics, Minecraft.getInstance().font, bidding.item(), x + 11   , renderY + 10, 0.5f);
                }
            }
            renderY += itemSpacing;
        }

        isScissorActive = false;
        graphics.disableScissor();
        renderY = currentY;
        for (ItemBidding bidding : filteredBids) {
            if (bidding.item() != null && !bidding.item().isEmpty()) {
                if (mouseY >= viewTop && mouseY <= viewBottom) {
                    if (mouseX >= x + 6 && mouseX <= x + 22 && mouseY >= renderY + 4 && mouseY <= renderY + 20) {
                        List<Component> textTooltip = Screen.getTooltipFromItem(Minecraft.getInstance(), bidding.item());
                        VersionGuiHandler.renderItemTooltip(graphics, Minecraft.getInstance().font, bidding.item(), mouseX, mouseY, textTooltip);
                    }
                }
            }
            renderY += itemSpacing;
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int viewTop = y + headerHeight;
        int viewHeight = height - headerHeight;
        int viewBottom = viewTop + viewHeight;
        int renderY = viewTop - this.scrollOffset;
        for (ItemBidding bidding : filteredBids) {
            if (bidding.item() != null && !bidding.item().isEmpty()) {
                if (event.y() >= viewTop && event.y() <= viewBottom) {
                    if (event.x() >= x && event.x() <= x + AuctionPlayerWidget.CARD_WIDTH && event.y() >= renderY && event.y() <= renderY + AuctionPlayerWidget.CARD_HEIGHT) {
                        GUIInputUtil.changeCursor(null, CursorTypes.ARROW);
                        if(clickableAuctions) onBiddingClick.accept(bidding);
                    }
                }
            }
            renderY += itemSpacing;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    protected int getItemCount() {
        return filteredBids.size();
    }
}