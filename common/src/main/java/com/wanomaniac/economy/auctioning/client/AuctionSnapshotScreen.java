package com.wanomaniac.economy.auctioning.client;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.auctioning.client.widgets.*;
import com.wanomaniac.economy.auctioning.packets.msgs.OpenHistoryMenuC2SPacket;
import com.wanomaniac.economy.auctioning.server.AuctionSession;
import com.wanomaniac.economy.auctioning.server.HistoryMenuDetails;
import com.wanomaniac.economy.auctioning.server.ItemBidding;
import com.wanomaniac.economy.auctioning.types.AuctionGuiData;
import com.wanomaniac.economy.client.AbstractInputScreen;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AuctionSnapshotScreen extends AbstractInputScreen {
    AuctionSession session;
    AuctionGuiData data;
    ItemBidding currentBidding;
    AuctionBidListWidget auctionHistoryWidget;
    LiveAuctionSidebarWidget auctionSidebarWidget;
    LiveBiddingWidget biddingSidebarWidget;
    final WidgetRedirector redirector;
    final HistoryMenuDetails details;

    public AuctionSnapshotScreen(Component title, AuctionSession Session, HistoryMenuDetails details) {
        super(title);
        this.session = Session;
        data = new AuctionGuiData(session.id, session.auctioneerID, false);
        redirector = new WidgetRedirector(this);
        this.details = details;
    }

    @Override
    public boolean whenKeyPressed(KeyEvent event) {
        if(redirector.whenKeyPressed(event)) return true;

        if (currentBidding != null) {
            if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
                currentBidding = null;
                return  true;
            }
        }

        return false; // dont stop here
    }

    @Override
    public boolean whenKeyReleased(KeyEvent event) {
        return false;
    }

    @Override
    public boolean whenMouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if(redirector.whenMouseClicked(event, isDoubleClick)) return true;

        // Continue normal mouse click flow
        return false;
    }

    @Override
    public boolean whenMouseReleased(MouseButtonEvent event) {
        redirector.whenMouseReleased(event);
        return false;
    }

    @Override
    public boolean whenMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if(redirector.whenMouseScrolled(mouseX, mouseY, scrollY)) return true;
        return false;
    }

    @Override
    public boolean whenMouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if(redirector.whenMouseDragged(event.y())) return true;
        return false;
    }


    @Override
    public boolean whenCharTyped(CharacterEvent event) {
        if(redirector.whenCharTyped(event)) return true;
        return false;
    }

    @Override
    protected void init() {
        super.init();

        if(auctionHistoryWidget == null) {
            this.auctionHistoryWidget = new AuctionBidListWidget(10, 30, AuctionPlayerWidget.CARD_WIDTH, 150, true, this::setActiveBidding);
            redirector.addWidget(auctionHistoryWidget);
        } else {
            this.auctionHistoryWidget.setPositionAndBounds(10, 30, AuctionPlayerWidget.CARD_WIDTH, 150);
        }

        if(biddingSidebarWidget == null) {
            this.biddingSidebarWidget = new LiveBiddingWidget(10, 30, AuctionPlayerWidget.CARD_WIDTH, 150);
            redirector.addWidget(biddingSidebarWidget);
        } else {
            this.biddingSidebarWidget.setPositionAndBounds(10, 30, AuctionPlayerWidget.CARD_WIDTH, 150);
        }
        biddingSidebarWidget.active = false;

        if(auctionSidebarWidget == null) {
            this.auctionSidebarWidget = new LiveAuctionSidebarWidget(width - AuctionPlayerWidget.CARD_WIDTH - 10, 30, AuctionPlayerWidget.CARD_WIDTH, 150);
            redirector.addWidget(auctionSidebarWidget);
        } else {
            this.auctionSidebarWidget.setPositionAndBounds(width - AuctionPlayerWidget.CARD_WIDTH - 10, 30, AuctionPlayerWidget.CARD_WIDTH, 150);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float partialTick) {
        int modalX = (this.width) / 2;
        int modalY = (this.height ) / 2;

        graphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        super.render(graphics, x, y, partialTick);


        auctionSidebarWidget.updateData(data, currentBidding, session.bidders, session.biddersInactive);
        auctionSidebarWidget.render(graphics, x, y, partialTick);
        if(currentBidding == null){
            biddingSidebarWidget.active = false;
            auctionHistoryWidget.active = true;
            auctionHistoryWidget.updateData(Arrays.stream(session.getAllBiddings()).toList());
            auctionHistoryWidget.render(graphics, x, y, partialTick);
            graphics.drawCenteredString(
                    this.font,
                    Component.literal("Select a bidding from the list..."),
                    modalX,
                    modalY,
                    0xFFFFFFFF // White text color
            );
            return;
        } else {
            auctionHistoryWidget.active = false;
        }

        biddingSidebarWidget.active = true;
        biddingSidebarWidget.updateData(currentBidding);
        biddingSidebarWidget.render(graphics, x, y, partialTick);
        if(currentBidding.highestBidder() == null) {
            graphics.drawCenteredString(
                    this.font,
                    Component.literal("No biddings yet."),
                    modalX,
                    45,
                    0xFFFFFFFF // White text color
            );
        } else {
          AuctionPlayerWidget.renderBiddingPlayerText(graphics, currentBidding);
        }

        BiddingInfoWidget.render(graphics,  currentBidding, width, height, x, y);

        // Animated it cuz why not.
        long time = System.currentTimeMillis();
        double speed = 0.0025;
        float pulse = (float) ((Math.sin(time * speed) + 1.0) / 2.0);
        int minAlpha = 0x33;
        int maxAlpha = 0xCC;
        int alpha = minAlpha + (int) (pulse * (maxAlpha - minAlpha));
        int animatedColor = (alpha << 24) | 0x00FFFFFF;
        Component escText = Component.literal("Press ESC to leave this bid.");
        graphics.drawString(font, escText, modalX - (font.width(escText) / 2) , 5, animatedColor);
    }

    public void setActiveBidding(ItemBidding bidding){
        this.currentBidding = bidding;
    }

    @Override
    public void onClose() {
        if(details != null){
            CommonEconomy.packets.sendToServer(new OpenHistoryMenuC2SPacket(details));
        }

        super.onClose();
    }
}
