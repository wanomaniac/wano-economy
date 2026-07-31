package com.wanomaniac.economy.auctioning.client;

import com.wanomaniac.economy.ModIdentifier;
import com.wanomaniac.economy.auctioning.types.AuctioneerMenuType;
import com.wanomaniac.economy.client.AbstractInputScreen;
import com.wanomaniac.economy.client.VersionGuiHandler;
import com.wanomaniac.economy.client.input.CharacterEvent;
import com.wanomaniac.economy.client.input.KeyEvent;
import com.wanomaniac.economy.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2f;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Optional;

public class AuctioneerMenuScreen extends AbstractInputScreen<AuctioneerMenuType> {
    boolean isBiddingActive = false;
    boolean isConfirmationActive = false;
    ItemStack biddingItem = null;

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

    @Override
    public boolean whenKeyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_ESCAPE && isConfirmationActive) {
            biddingItem = null;
            isConfirmationActive = false;
            return true;
        }

        return false;
    }

    @Override
    public boolean whenKeyReleased(KeyEvent event) {
        return false;
    }

    @Override
    public boolean whenMouseClicked(MouseButtonEvent event, boolean doubleClick) {
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
                0x404040, // Standard Minecraft dark-gray GUI text color
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

//        guiGraphics.blit(
//                RenderPipelines.GUI_TEXTURED,
//                IdentifierUtils.toNative(CONTAINER_BACKGROUND),
//                i,
//                footerY,
//                0,
//                footerV,
//                this.imageWidth,
//                footerH,
//                textureW,
//                textureH
//        );

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
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        if(isBiddingActive || isConfirmationActive){
            // Render unique UIS for different cases, menu overlay UI will have to be disabled while these are active.
            if(isConfirmationActive) renderConfirmationModal(g, mouseX, mouseY, delta);
        } else {
            super.render(g, mouseX, mouseY, delta);
        }
    }

    private void renderConfirmationModal(GuiGraphics g, int mouseX, int mouseY, float delta) {
        // 1. Darken the background inventory
        g.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);

        int modalX = (this.width) / 2;
        int modalY = (this.height ) / 2;

        // 4. Draw the selected item being confirmed
        if (this.biddingItem != null && !this.biddingItem.isEmpty()) {
            int itemX = modalX;
            int itemY = modalY;

            VersionGuiHandler.renderItemAndDecorations(g, this.font, biddingItem, itemX, itemY, 2.5f);

            // ----------------------------------------------------
            // 5. Render Tooltip Fixed to the Right Side of Modal
            // ----------------------------------------------------
            int tooltipX = modalX - 195; // 10px spacing from modal right edge
            int tooltipY = modalY;                   // Aligned with modal top

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
}
