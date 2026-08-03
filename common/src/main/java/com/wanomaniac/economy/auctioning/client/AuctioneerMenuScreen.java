package com.wanomaniac.economy.auctioning.client;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.EconomyManager;
import com.wanomaniac.economy.ModIdentifier;
import com.wanomaniac.economy.auctioning.client.widgets.AuctionPlayerListWidget;
import com.wanomaniac.economy.auctioning.client.widgets.BiddingInfoWidget;
import com.wanomaniac.economy.auctioning.packets.msgs.StartBiddingItemC2SPacket;
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
    ItemStack biddingItem = null;
    ItemBidding currentBidding = null;
    private Button confirmButton;
    private Button cancelButton;
    private EditBox startingPriceBox;
    public List<UUID> bidders = new ArrayList<>();
    public List<UUID> biddersInactive = new ArrayList<>();

    private static final ModIdentifier CONTAINER_BACKGROUND =
            ModIdentifier.parse("minecraft:textures/gui/container/generic_54.png"); // Standard chest texture frame
    private static final ModIdentifier CONTAINER_SLOT =
            ModIdentifier.withMojangNamespace("container/slot"); // Vanilla 18x18 item slot frame

    public AuctioneerMenuScreen(AuctioneerMenuType menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = 72 - 34;
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
        isConfirmationActive = false;
        isBiddingActive = true;
        Long startingPrice = Long.parseLong(startingPriceBox.getValue().isEmpty() ? "0" : startingPriceBox.getValue());
        CommonEconomy.packets.sendToServer(new StartBiddingItemC2SPacket(menu.guiData, getSlotIndexForItem(biddingItem), startingPrice));
    }

    @Override
    protected void init() {
        super.init();
        int buttonWidth = 80;
        int buttonHeight = 20;
        int modalX = this.width / 2;
        int modalY = this.height / 2;
        int buttonY = modalY + 50; // Below the item display

        this.confirmButton = Button.builder(
                        Component.literal("Confirm"),
                        btn -> this.onConfirmBidding() // Click handler
                )
                .bounds(modalX - buttonWidth - 5, buttonY, buttonWidth, buttonHeight)
                .build();

        this.cancelButton = Button.builder(
                        Component.literal("Cancel"),
                        btn -> {
                            biddingItem = null;
                            isConfirmationActive = false;
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
    }

    @Override
    public boolean whenKeyPressed(KeyEvent event) {
        if (isConfirmationActive) {
            if(event.key() == GLFW.GLFW_KEY_ESCAPE) {
                if (startingPriceBox.isFocused()) {
                    startingPriceBox.setFocused(false);
                    return true; // stop ESC from closing the screen
                }
                biddingItem = null;
                isConfirmationActive = false;
            }
            if(startingPriceBox.isFocused()){
                setFocused(startingPriceBox);
                GUIInputUtil.onEditBoxKeyPressed(startingPriceBox, event);
                return true;
            }
            return true;
        }

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
            VersionGuiHandler.whenButtonWidgetMouseClick(confirmButton, event, doubleClick);
            VersionGuiHandler.whenButtonWidgetMouseClick(cancelButton, event, doubleClick);
            return true;
        }

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
            // Render unique UIS for different cases, menu overlay UI will have to be disabled while these are active.
            g.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
            if(isConfirmationActive) renderConfirmationModal(g, mouseX, mouseY, delta);
            else if(isBiddingActive) renderBiddingAuctioneer(g, mouseX, mouseY, delta);
        } else {
            confirmButton.visible = false;
            cancelButton.visible = false;
            startingPriceBox.visible = false;
            super.render(g, mouseX, mouseY, delta);
        }
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
            // Add dynamic timer line on top of the screen where the width gets smaller from the time
            AuctionScreenUtil.drawTimerLine(g, currentBidding, width);
            AuctionPlayerListWidget.renderBiddingSidebar(g, menu.guiData, currentBidding, mouseX, mouseY);
            AuctionPlayerListWidget.renderAuctionSidebar(g, currentBidding, menu.guiData, bidders, biddersInactive, width, mouseX, mouseY);
            BiddingInfoWidget.render(g,  currentBidding, width, height, mouseX, mouseY);

            if(currentBidding.currentBid() != 0){
                g.drawString(Minecraft.getInstance().font, Component.literal("Up for $"+ currentBidding.currentBid()), modalX, 65, 0xFF55FF55);
            }


        }
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

            confirmButton.visible = true;
            cancelButton.visible = true;
            startingPriceBox.visible = true;

            confirmButton.render(g, mouseX, mouseY, delta);
            cancelButton.render(g, mouseX, mouseY, delta);
            startingPriceBox.render(g, mouseX, mouseY, delta);
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
        }

        currentBidding = bidding;

        if(currentBidding == null) return;
        if(currentBidding.isExpired()){

        }
    }
}
