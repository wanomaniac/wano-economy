package com.wanomaniac.economy.auctioning.client;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.auctioning.client.widgets.AuctionPlayerListWidget;
import com.wanomaniac.economy.auctioning.client.widgets.AuctionPlayerWidget;
import com.wanomaniac.economy.auctioning.client.widgets.BiddingInfoWidget;
import com.wanomaniac.economy.auctioning.packets.msgs.AuctionRequestPlayerBalanceC2SPacket;
import com.wanomaniac.economy.auctioning.packets.msgs.BidMoneyC2SPacket;
import com.wanomaniac.economy.auctioning.packets.msgs.BidderLeavePacket;
import com.wanomaniac.economy.auctioning.server.ItemBidding;
import com.wanomaniac.economy.auctioning.types.AuctionGuiData;
import com.wanomaniac.economy.client.AbstractInputScreen;
import com.wanomaniac.economy.client.GUIInputUtil;
import com.wanomaniac.economy.client.input.CharacterEvent;
import com.wanomaniac.economy.client.input.KeyEvent;
import com.wanomaniac.economy.client.input.MouseButtonEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BidderScreen extends AbstractInputScreen {
    boolean loaded = false;
    AuctionGuiData guiData = null;
    ItemBidding currentBidding = null;
    public List<UUID> bidders = new ArrayList<>();
    public List<UUID> biddersInactive = new ArrayList<>();
    EditBox moneyField;
    Button bidButton;
    long lastMoney;
    long currentBiddedMoney = 0L;
    long playerCurrentMoney = -1L;
    boolean hasTextboxChanged;
    boolean hasSynchronizedMoney;

    public BidderScreen(Component title) {
        super(title);
    }


    @Override
    public boolean whenKeyPressed(KeyEvent event) {
        // ESC key
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            if (moneyField.isFocused()) {
                moneyField.setFocused(false);
                return true; // stop ESC from closing the screen
            }
        } else if(moneyField.isFocused()){
            setFocused(moneyField);
            GUIInputUtil.onEditBoxKeyPressed(moneyField, event);
            return true;
        }

        return false; // dont stop here
    }

    @Override
    public boolean whenKeyReleased(KeyEvent event) {
        return false;
    }

    @Override
    public boolean whenMouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        // First let the textbox handle the click
        if (GUIInputUtil.onEditBoxMouseClicked(moneyField, event, isDoubleClick)) {
            moneyField.setFocused(true);
            setFocused(moneyField);
            return true;
        }

        if(Long.parseLong(moneyField.getValue().isEmpty() ? "0" : moneyField.getValue()) <= currentBiddedMoney){
            moneyField.setValue(String.valueOf(currentBiddedMoney+1));
        }
        moneyField.setFocused(false);
        GUIInputUtil.onButtonMouseClicked(bidButton, event, isDoubleClick);

        // Continue normal mouse click flow
        return false;
    }

    @Override
    public boolean whenMouseReleased(MouseButtonEvent event) {
        return false;
    }

    @Override
    public boolean whenCharTyped(CharacterEvent event) {
        return false;
    }

    @Override
    protected void init() {
        super.init();

        int boxWidth = 112;
        int boxHeight = 18;
        int boxX = (width / 2)-(boxWidth/2);
        int boxY = 15;

        // EDIT BOX (money)
        this.moneyField = new EditBox(
                this.font,
                boxX, //leftCenter - 160 / 2,   // X
                boxY,   // Y
                boxWidth,                 // width
                boxHeight,                 // height
                Component.literal("Extra money")
        );
        moneyField.setEditable(false);
        moneyField.setValue(Long.toString(currentBiddedMoney+1));
        moneyField.setFilter(s -> s.matches("\\d*"));
        moneyField.setResponder(text -> {
            try {
                long val = Long.parseLong(text);

                if (val > playerCurrentMoney) {
                    moneyField.setValue(String.valueOf(playerCurrentMoney));
                    return;
                }
                if (val != lastMoney) {
                    hasTextboxChanged = true;
                    lastMoney = val;
                    hasSynchronizedMoney = false;
                }
            } catch (NumberFormatException ignored) {}
        });

        int buttonX = boxX + boxWidth + 4;

        this.bidButton = Button.builder(Component.literal("Bid"), button -> CommonEconomy.packets.sendToServer(new BidMoneyC2SPacket(guiData.auctionId(), lastMoney)))
                .bounds(buttonX, boxY - 1, 50, boxHeight + 2) // Slight height padding to match EditBox borders nicely
                .build();
        bidButton.active = false;

        this.addRenderableWidget(this.bidButton);
        this.addWidget(this.moneyField);
        this.setInitialFocus(this.moneyField);

        if(playerCurrentMoney == -1L) {
            CommonEconomy.packets.sendToServer(new AuctionRequestPlayerBalanceC2SPacket());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float partialTick) {
        int modalX = (this.width) / 2;
        int modalY = (this.height ) / 2;

        if(currentBidding != null) {
            if (currentBidding.isExpired()) {
                moneyField.setEditable(false);
                bidButton.active = false;
            } else if (isHighestBidder()) {
                bidButton.active = false;
            } else {
                moneyField.setEditable(true);
                bidButton.active = true;
            }
        }
        super.render(graphics, x, y, partialTick);

        graphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);

        if(!loaded || playerCurrentMoney == -1L){
            graphics.drawCenteredString(
                    this.font,
                    Component.literal("Loading..."),
                    modalX,
                    modalY,
                    0xFFFFFFFF // White text color
            );

            return;
        }
        AuctionPlayerListWidget.renderAuctionSidebar(graphics, currentBidding, guiData, bidders, biddersInactive, width, x, y);
        if(currentBidding == null){
            graphics.drawCenteredString(
                    this.font,
                    Component.literal("Waiting for bidding..."),
                    modalX,
                    modalY,
                    0xFFFFFFFF // White text color
            );

            return;
        }

        moneyField.render(graphics, x, y, partialTick);
        AuctionScreenUtil.drawTimerLine(graphics, currentBidding, width);
        AuctionPlayerListWidget.renderBiddingSidebar(graphics, guiData, currentBidding, x, y);

        if(currentBidding.highestBidder() == null) {
            graphics.drawCenteredString(
                    this.font,
                    Component.literal("No biddings yet."),
                    modalX,
                    45,
                    0xFFFFFFFF // White text color
            );
        } else {
            AuctionPlayerWidget.renderPlayFace(graphics, modalX - 16, 45, currentBidding.highestBidder());
            graphics.drawCenteredString(
                    this.font,
                    Component.literal("Bidded $"+currentBidding.currentBid()),
                    modalX,
                    45,
                    0xFFFFFFFF // White text color
            );
        }

        graphics.drawCenteredString(
                this.font,
                Component.literal("You will have $"+(playerCurrentMoney-lastMoney)),
                modalX,
                57,
                0xFFFFFFFF // White text color
        );

        BiddingInfoWidget.render(graphics,  currentBidding, width, height, x, y);
    }

    public void BidderJoin(UUID playerID, AuctionGuiData data){
        if(biddersInactive.contains(playerID)){
            biddersInactive.remove(playerID);
            bidders.remove(playerID);
        }

        if(playerID.equals(Minecraft.getInstance().player.getUUID())){
            guiData = data;
            loaded = true;
        }
        bidders.add(playerID);
    }

    public void BidderLeave(UUID playerID){
        biddersInactive.add(playerID);
    }

    boolean isHighestBidder(){
        if(currentBidding.highestBidder() != null) {
            if (currentBidding.highestBidder().equals(Minecraft.getInstance().player.getUUID())) {
                return true;
            }
        }

        return  false;
    }

    public void setActiveBidding(ItemBidding bidding){
        if(!biddersInactive.isEmpty() && currentBidding == null){
            bidders.removeAll(biddersInactive);
            biddersInactive.clear();
        }
        currentBidding = bidding;
        if(currentBidding == null) return;

        if(isHighestBidder()) hasSynchronizedMoney = true;
        if(currentBidding.currentBid() > 0){
            currentBiddedMoney = currentBidding.currentBid();
        }

    }

    public void syncMoney(long balance){
        playerCurrentMoney = balance;
    }

    @Override
    public void onClose() {
        super.onClose();
        if(guiData != null) CommonEconomy.packets.sendToServer(new BidderLeavePacket(guiData.auctionId()));
    }
}
