package com.wanomaniac.economy.auctioning.client;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.ModIdentifier;
import com.wanomaniac.economy.auctioning.client.widgets.*;
import com.wanomaniac.economy.auctioning.packets.msgs.StartBiddingItemC2SPacket;
import com.wanomaniac.economy.auctioning.packets.msgs.SynchronizeAuctioneerAuctionC2SPacket;
import com.wanomaniac.economy.auctioning.server.ItemBidding;
import com.wanomaniac.economy.auctioning.types.AuctioneerMenuType;
import com.wanomaniac.economy.client.AbstractInputContainerScreen;
import com.wanomaniac.economy.client.GUIInputUtil;
import com.wanomaniac.economy.client.VersionGuiHandler;
import com.wanomaniac.economy.client.input.CharacterEvent;
import com.wanomaniac.economy.client.input.KeyEvent;
import com.wanomaniac.economy.client.input.MouseButtonEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AuctioneerMenuScreen extends AbstractInputContainerScreen<AuctioneerMenuType> {
    boolean isBiddingActive = false;
    boolean isConfirmationActive = false;
    boolean isCancelMenuActive = false;
    ItemStack biddingItem = null;
    ItemBidding currentBidding = null;
    List<ItemBidding> biddings = new ArrayList<>();
    private Button confirmButton;
    private Button cancelButton;
    private EditBox startingPriceBox;
    public List<UUID> bidders = new ArrayList<>();
    public List<UUID> biddersInactive = new ArrayList<>();
//    private EditBox auctionSearchBox;
    private AuctionBidListWidget auctionHistoryWidget;
    private LiveBiddingWidget biddingLiveWidget;
    private LiveAuctionSidebarWidget auctionSidebarWidget;
    private final WidgetRedirector redirector;

    private static final ModIdentifier CONTAINER_BACKGROUND =
            ModIdentifier.parse("minecraft:textures/gui/container/generic_54.png"); // Standard chest texture frame
    private static final ModIdentifier CONTAINER_SLOT =
            ModIdentifier.withMojangNamespace("container/slot"); // Vanilla 18x18 item slot frame

    public AuctioneerMenuScreen(AuctioneerMenuType menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = 72 - 34;
        redirector = new WidgetRedirector(this);
    }

    private int getSlotIndexForItem(ItemStack targetStack) {
        if (targetStack == null || targetStack.isEmpty()) {
            return -1;
        }

        // Iterate through all slots in the active ContainerMenu
        for (Slot slot : this.menu.slots) {
            // Compare stack instance or content equality
            if (slot.hasItem() && ItemStack.matches(slot.getItem(), targetStack)) {
                return slot.index; // Return the container slot index
            }
        }

        return -1; // Not found
    }

    void onConfirmBidding(){
        // add netcode
        if(isCancelMenuActive) {
            // -1 cancels the bidding.
            biddings.remove(currentBidding);
            this.auctionHistoryWidget.updateData(biddings);
            CommonEconomy.packets.sendToServer(new StartBiddingItemC2SPacket(menu.guiData, -1, 0L));
            isCancelMenuActive = false;
        } else {
        isConfirmationActive = false;
        isBiddingActive = true;
        Long startingPrice = Long.parseLong(startingPriceBox.getValue().isEmpty() ? "0" : startingPriceBox.getValue());
        CommonEconomy.packets.sendToServer(new StartBiddingItemC2SPacket(menu.guiData, getSlotIndexForItem(biddingItem), startingPrice));
        }
    }

    @Override
    protected void init() {
        super.init();
        int buttonWidth = 80;
        int buttonHeight = 20;
        int modalX = this.width / 2;
        int modalY = this.height / 2;
        int buttonY = modalY + 50; // Below the item display

        if(auctionHistoryWidget == null) {
            this.auctionHistoryWidget = new AuctionBidListWidget(10, 30, AuctionPlayerWidget.CARD_WIDTH, 150);
            redirector.addWidget(auctionHistoryWidget);
        } else {
            this.auctionHistoryWidget.setPositionAndBounds(10, 30, AuctionPlayerWidget.CARD_WIDTH, 150);
        }

        if(biddingLiveWidget == null) {
            this.biddingLiveWidget = new LiveBiddingWidget(10, 30, AuctionPlayerWidget.CARD_WIDTH, 150);
            redirector.addWidget(biddingLiveWidget);
        } else {
            this.biddingLiveWidget.setPositionAndBounds(10, 30, AuctionPlayerWidget.CARD_WIDTH, 150);
        }
        biddingLiveWidget.active = false;

        if(auctionSidebarWidget == null) {
            this.auctionSidebarWidget = new LiveAuctionSidebarWidget(width - AuctionPlayerWidget.CARD_WIDTH - 10, 30, AuctionPlayerWidget.CARD_WIDTH, 150);
            redirector.addWidget(auctionSidebarWidget);
        } else {
            this.auctionSidebarWidget.setPositionAndBounds(width - AuctionPlayerWidget.CARD_WIDTH - 10, 30, AuctionPlayerWidget.CARD_WIDTH, 150);
        }

        this.confirmButton = Button.builder(
                        Component.literal("Confirm"),
                        btn -> this.onConfirmBidding() // Click handler
                )
                .bounds(modalX - buttonWidth - 5, buttonY, buttonWidth, buttonHeight)
                .build();

        this.cancelButton = Button.builder(
                        Component.literal("Cancel"),
                        btn -> {
                            if(isCancelMenuActive){
                                isCancelMenuActive = false;
                            } else {
                                biddingItem = null;
                                isConfirmationActive = false;
                            }
                        }
                )
                .bounds(modalX + 5, buttonY, buttonWidth, buttonHeight)
                .build();

        this.confirmButton.visible = false;
        this.cancelButton.visible = false;

        int editBoxWidth = (modalX + 5 + buttonWidth) - (modalX - buttonWidth - 5); // Total width spanning both buttons
        int editBoxX = modalX - buttonWidth - 5; // Align left edge with confirmButton's left edge
        int editBoxY = buttonY + 25;

        this.startingPriceBox = new EditBox(
                this.font,
                editBoxX,
                editBoxY,   // Y
                editBoxWidth,                 // width
                18,
                Component.literal("Extra money")
        );
        startingPriceBox.setEditable(true);
        startingPriceBox.setValue("0");
        startingPriceBox.setFilter(s -> s.matches("\\d*"));

        this.addRenderableWidget(this.confirmButton);
        this.addRenderableWidget(this.cancelButton);
        this.addRenderableWidget(this.startingPriceBox);

        if(!menu.guiData.isNew()) CommonEconomy.packets.sendToServer(new SynchronizeAuctioneerAuctionC2SPacket());
    }

    @Override
    public boolean whenKeyPressed(KeyEvent event) {
        if (isConfirmationActive) {
            if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
                if (startingPriceBox.isFocused()) {
                    startingPriceBox.setFocused(false);
                    return true; // stop ESC from closing the screen
                }
                biddingItem = null;
                isConfirmationActive = false;
                return true;
            }
            if(startingPriceBox.isFocused()){
                setFocused(startingPriceBox);
                GUIInputUtil.onEditBoxKeyPressed(startingPriceBox, event);
                return true;
            }
            return false;
        }  else if(isBiddingActive && !isCancelMenuActive){
            if(event.key() == GLFW.GLFW_KEY_ESCAPE) {
                if(currentBidding.highestBidder() == null){
                    isCancelMenuActive = true;
                    return true;
                } else {
                    return false;
                }
            }

//            if(biddingLiveWidget.whenKeyPressed(event)) return true;
        } else if(isCancelMenuActive){
            if(event.key() == GLFW.GLFW_KEY_ESCAPE) {
                isCancelMenuActive = false;
                return true;
            }
        } else {
//            if(auctionHistoryWidget.whenKeyPressed(event)) return true;
        }

        if(redirector.whenKeyPressed(event)) return true;

//        if(auctionSidebarWidget.whenKeyPressed(event)) return true;

        // If number keys, the player hotbar will be selected
        if (event.key() >= GLFW.GLFW_KEY_1 && event.key() <= GLFW.GLFW_KEY_9) {
            int hotbarIndex = event.key() - GLFW.GLFW_KEY_1;
            Minecraft mc = Minecraft.getInstance();

            if (mc.gameMode != null && mc.player != null) {
                int playerHotbarSlotId = 27 + hotbarIndex;

                mc.gameMode.handleInventoryMouseClick(
                        this.menu.containerId,
                        playerHotbarSlotId,
                        0, // 0 = Left click
                        ClickType.PICKUP,
                        mc.player
                );
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean whenKeyReleased(KeyEvent event) {
        return false;
    }

    @Override
    public boolean whenMouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if(isConfirmationActive){
            if (GUIInputUtil.onEditBoxMouseClicked(startingPriceBox, event, doubleClick)) {
                startingPriceBox.setFocused(true);
                setFocused(startingPriceBox);
                return true;
            }

            startingPriceBox.setFocused(false);
            GUIInputUtil.onButtonMouseClicked(confirmButton, event, doubleClick);
            GUIInputUtil.onButtonMouseClicked(cancelButton, event, doubleClick);
            return true;
        } else if(!isCancelMenuActive) {
            return redirector.whenMouseClicked(event, doubleClick);

//            boolean click1 = auctionHistoryWidget.mouseClicked(event, doubleClick);
//            boolean click2 = biddingLiveWidget.mouseClicked(event, doubleClick);
//            boolean click3 = auctionSidebarWidget.mouseClicked(event, doubleClick);
//
//            return click1 || click2 || click3;
        }

        return false;
    }

    @Override
    public boolean whenMouseReleased(MouseButtonEvent event) {
        redirector.whenMouseReleased(event);

//        auctionHistoryWidget.mouseReleased(event.button());
//        biddingLiveWidget.mouseReleased(event.button());
//        auctionSidebarWidget.mouseReleased(event.button());

        return false;
    }


    @Override
    public boolean whenMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (redirector.whenMouseScrolled(mouseX, mouseY, scrollY)) return true;

//        if (auctionHistoryWidget.mouseScrolled(mouseX, mouseY, scrollY)) return true;
//        if (biddingLiveWidget.mouseScrolled(mouseX, mouseY, scrollY)) return true;
//        if (auctionSidebarWidget.mouseScrolled(mouseX, mouseY, scrollY)) return true;

        return false;
    }

    @Override
    public boolean whenMouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (redirector.whenMouseDragged(event.y())) return true;
//        if (auctionHistoryWidget.mouseDragged(event.y())) return true;
//        if (biddingLiveWidget.mouseDragged(event.y())) return true;
//        if (auctionSidebarWidget.mouseDragged(event.y())) return true;

        return false;
    }

    @Override
    public boolean whenCharTyped(CharacterEvent event) {
        if(redirector.whenCharTyped(event)) return true;

//        if(auctionHistoryWidget.whenCharTyped(event)) return true;
//        if(biddingLiveWidget.whenCharTyped(event)) return true;
//        if(auctionSidebarWidget.whenCharTyped(event)) return true;

        return false;
    }

    @Override
    public void renderBackground(GuiGraphics p_295206_, int p_295457_, int p_294596_, float p_296351_) {
        if(!isBiddingActive && !isConfirmationActive) super.renderBackground(p_295206_, p_295457_, p_294596_, p_296351_);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(
                this.font,
                Component.literal("Select an item to auction."), // todo translatable
                this.inventoryLabelX,
                this.inventoryLabelY,
                0xFF404040, // Standard Minecraft dark-gray GUI text color
                false
        );
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        int i = this.leftPos;
        int j = this.topPos;

        int startY = 50; // Must match startY from AuctioneerMenu!
        int headerH = 17;
        int rowTileH = 18;
        int slotYShift = 34; // Moving up by 34 pixels
        int footerY = j + headerH + (3 * rowTileH) - slotYShift;
        int footerH = 96;
        int textureW = 256;
        int textureH = 256;
        int footerV = 126;

        VersionGuiHandler.blitGUI(
                guiGraphics,
                CONTAINER_BACKGROUND,
                i,
                footerY,
                0,
                footerV,
                this.imageWidth,
                footerH,
                textureW,
                textureH
        );

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                int slotX = x + 7 + col * 18;
                int slotY = y + (startY - 1) + row * 18; // 49 + row * 18

                VersionGuiHandler.blitSpriteGUI(guiGraphics, CONTAINER_SLOT, slotX, slotY, 18, 18);
            }
        }

        int hotbarY = startY + (3 * 18) + 4; // 108

        for (int col = 0; col < 9; ++col) {
            int slotX = x + 7 + col * 18;
            int slotY = y + (hotbarY - 1); // 107

            VersionGuiHandler.blitSpriteGUI(guiGraphics, CONTAINER_SLOT, slotX, slotY, 18, 18);
        }


        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        if(isBiddingActive || isConfirmationActive){
            auctionHistoryWidget.active = false;
            auctionSidebarWidget.active = true;
            // Render unique UIS for different cases, menu overlay UI will have to be disabled while these are active.
            g.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
            if(isCancelMenuActive || isConfirmationActive) {
                auctionSidebarWidget.active = false;
                if (isConfirmationActive) renderConfirmationModal(g, mouseX, mouseY, delta);
                if(isCancelMenuActive) renderCancelModel(g, mouseX, mouseY, delta);

                confirmButton.visible = true;
                cancelButton.visible = true;
                startingPriceBox.visible = true;
                confirmButton.render(g, mouseX, mouseY, delta);
                cancelButton.render(g, mouseX, mouseY, delta);
                if (isConfirmationActive) startingPriceBox.render(g, mouseX, mouseY, delta);
            }
            else if(isBiddingActive) renderBiddingAuctioneer(g, mouseX, mouseY, delta);
        } else {
            super.render(g, mouseX, mouseY, delta);
            auctionSidebarWidget.active = true;
            auctionHistoryWidget.active = true;
            biddingLiveWidget.active = false;

            this.auctionHistoryWidget.updateData(biddings);
            auctionHistoryWidget.render(g, mouseX, mouseY, delta);

            confirmButton.visible = false;
            cancelButton.visible = false;
            startingPriceBox.visible = false;
        }

        auctionSidebarWidget.updateData(menu.guiData, currentBidding, bidders, biddersInactive);
        auctionSidebarWidget.render(g, mouseX, mouseY, delta);
    }

    private void renderBiddingAuctioneer(GuiGraphics g, int mouseX, int mouseY, float delta) {
        int modalX = (this.width) / 2;
        int modalY = (this.height ) / 2;

        if(currentBidding == null){
            Component confirmMessage = Component.literal("Loading...");
            g.drawCenteredString(
                    this.font,
                    confirmMessage,
                    modalX,
                    modalY,
                    0xFFFFFFFF // White text color
            );
        } else {
            biddingLiveWidget.active = true;
            // Add dynamic timer line on top of the screen where the width gets smaller from the time
            AuctionScreenUtil.drawTimerLine(g, currentBidding, width);
            biddingLiveWidget.updateData(currentBidding);
            biddingLiveWidget.render(g, mouseX, mouseY, delta);
//            AuctionPlayerListWidget.renderAuctionSidebar(g, currentBidding, menu.guiData, bidders, biddersInactive, width, mouseX, mouseY);
            BiddingInfoWidget.render(g,  currentBidding, width, height, mouseX, mouseY);

            if(currentBidding.highestBidder() != null){
                int faceSize = 16;
                int spacing = 4;
                Component text = Component.literal(AuctionUtils.getPlayerUsername(currentBidding.highestBidder()) + " has bidded $" + currentBidding.currentBid());
                int textWidth = this.font.width(text);
                int totalWidth = faceSize + spacing + textWidth;
                int startX = modalX - (totalWidth / 2);
                int textY = 45;
                int fontHeight = 9;
                int faceY = textY + (fontHeight - faceSize) / 2;

                AuctionPlayerWidget.renderPlayFace(g, startX-2, faceY-1, currentBidding.highestBidder());
                g.drawString(
                        this.font,
                        text,
                        startX + faceSize + spacing,
                        textY,
                        0xFFFFFFFF,
                        true
                );
            } else {
                Component text = Component.literal("Item is up for sale for a minimum of $"+currentBidding.currentBid());
                int startX = modalX - (this.font.width(text) / 2);
                g.drawString(
                        this.font,
                        text,
                        startX,
                        45,
                        0xFFFFFFFF,
                        true
                );
            }
        }
    }


    private void renderCancelModel(GuiGraphics g, int mouseX, int mouseY, float delta) {
        int modalX = (this.width) / 2;
        int modalY = (this.height) / 2;

        Component confirmMessage = Component.literal("Are you sure you want to cancel this bidding?");
        g.drawCenteredString(
                this.font,
                confirmMessage,
                modalX,
                modalY,
                0xFFFFFFFF // White text color
        );
    }

    private void renderConfirmationModal(GuiGraphics g, int mouseX, int mouseY, float delta) {
        int modalX = (this.width) / 2;
        int modalY = (this.height ) / 2;

        if (this.biddingItem != null && !this.biddingItem.isEmpty()) {
            Component confirmMessage = Component.literal("Are you sure you want to put this item up for bidding?");
            g.drawCenteredString(
                    this.font,
                    confirmMessage,
                    modalX,
                    modalY - 50,
                    0xFFFFFFFF // White text color
            );

            int itemX = modalX;
            int itemY = modalY;

            VersionGuiHandler.renderItemAndDecorations(g, this.font, biddingItem, itemX, itemY, 2.5f);

            int tooltipX = modalX - 195; // 10px spacing from modal right edge
            int tooltipY = modalY - 23;                   // Aligned with modal top

            List<Component> textTooltip = getTooltipFromItem(this.minecraft, this.biddingItem);
            Optional<TooltipComponent> imageTooltip = this.biddingItem.getTooltipImage();

            List<ClientTooltipComponent> components = textTooltip.stream()
                    .map(Component::getVisualOrderText)
                    .map(ClientTooltipComponent::create)
                    .collect(java.util.stream.Collectors.toList());

            imageTooltip.ifPresent(tp -> components.add(1, ClientTooltipComponent.create(tp)));
            VersionGuiHandler.renderItemTooltip(
                    g,
                    this.font,
                    this.biddingItem,
                    tooltipX,
                    tooltipY,
                    textTooltip
            );
        }
    }

    public void ConfirmItem(ItemStack itemStack){
        isConfirmationActive = true;
        biddingItem = itemStack;
    }

    public void BidderJoin(UUID playerID){
        if(biddersInactive.contains(playerID)){
            biddersInactive.remove(playerID);
            bidders.remove(playerID);
        }

        if(bidders.contains(playerID)) return;
        bidders.add(playerID);
    }

    public void BidderLeave(UUID playerID){
        biddersInactive.add(playerID);
    }

    public void setActiveBidding(ItemBidding bidding){
        if(!biddersInactive.isEmpty() && currentBidding == null){
            bidders.removeAll(biddersInactive);
            biddersInactive.clear();
        }
        if(bidding == null){
            isBiddingActive = false;
        } else if(bidding.isActive()){
            isBiddingActive = true;
        }

        currentBidding = bidding;
        if(bidding == null) return;
        biddings.removeIf(b -> b.id().equals(bidding.id()));
        biddings.add(bidding);
        this.auctionHistoryWidget.updateData(biddings);
    }
}
