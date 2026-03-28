package com.reazip.economycraft.fabric.trading.client;

import com.reazip.economycraft.fabric.trading.packets.msgs.RequestPlayerBalanceC2SPacket;
import com.reazip.economycraft.fabric.trading.packets.msgs.UpdateExtraMoneyPayloadC2SP;
import com.reazip.economycraft.fabric.trading.types.TradeInventoryType;
import com.reazip.economycraft.fabric.trading.types.TradeSnapshotType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

public class TradeMenuSnapshootClientScreen extends AbstractContainerScreen<TradeSnapshotType> {
    private EditBox extraMoneyField;

    public TradeMenuSnapshootClientScreen(TradeSnapshotType menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 17 + menu.getRowCount() * 18 + 96;
    }

    @Override
    protected void init() {
        super.init();
        // Center X and Y of GUI
        int left = (this.width - this.imageWidth) / 2;
        int top  = (this.height - this.imageHeight) / 2;

        this.inventoryLabelY = top + menu.getRowCount() * 18 + 17;
        // EDIT BOX (Text Input)
        this.extraMoneyField = new EditBox(
                this.font,
                this.leftPos + 10,   // X
                this.topPos + 80,   // Y
                70,                 // width
                18,                 // height
                Component.literal("Extra money")
        );
        extraMoneyField.setMaxLength(19);
        extraMoneyField.setValue(String.valueOf(menu.guiData.getIncomingMoney().get(menu.guiData.getTraders().getFirst())));
        extraMoneyField.setFilter(s -> s.matches("\\d*"));

        this.addWidget(this.extraMoneyField);
        this.setInitialFocus(this.extraMoneyField);
    }


    private static final ResourceLocation TEXTURE =
            ResourceLocation.parse("minecraft:textures/gui/container/generic_54.png");


    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        extraMoneyField.render(g, mouseX, mouseY, delta);
        super.render(g, mouseX, mouseY, delta);
        this.renderTooltip(g, mouseX, mouseY);
    }

    private Player getPlayerFromUUID(UUID id){
        assert Minecraft.getInstance().level != null;
        return Minecraft.getInstance().level.getPlayerByUUID(id);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {

        int i = (width - imageWidth) / 2;
        int j = ((height - imageHeight) / 2);

        int rows = menu.getRowCount();  // 7
        int headerH = 17;
        int rowTileH = 18;
        int footerH = 96;

        int textureW = 256;
        int textureH = 256;

// The row tile is located at V = 17 in the texture
        int rowTileV = 17;

// Footer start V
        int footerV = 126;

// 1. HEADER
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
                i, j,
                0, 0,
                this.imageWidth, headerH,
                textureW, textureH);

// 2. ROWS (repeat the 18px tile)
        for (int r = 0; r < rows; r++) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
                    i,
                    j + headerH + r * rowTileH,
                    0, rowTileV,
                    this.imageWidth, rowTileH,
                    textureW, textureH);
        }

// 3. FOOTER (below all rows)
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
                i,
                j + headerH + rows * rowTileH,
                0, footerV,
                this.imageWidth, footerH,
                textureW, textureH);

        graphics.drawString(this.font, this.title, i + 6, j + 6, -12566464, false);
        graphics.drawString(this.font, this.playerInventoryTitle, i + 6, j + headerH + rows * rowTileH + 3, -12566464, false);

        // variables for measurements
        int playerModelSize = 30; // same as in renderPlayerModel
        int leftPlayerX = i - 50; // a little padding from the left edge
        int leftPlayerY = j + 5; // vertical alignment near footer
        int leftCenterX = leftPlayerX + playerModelSize / 2;
        int leftTopY = leftPlayerY - 12;
        int rightPlayerX = i + this.imageWidth;
        int rightPlayerY = j + headerH + rows * rowTileH;
        int rightCenterX = rightPlayerX + playerModelSize / 2;
        int rightTopY = rightPlayerY - 12;

        extraMoneyField.setX(leftCenterX - extraMoneyField.getWidth() / 2); // center
        extraMoneyField.setY(leftTopY + 50);
        renderPlayerModel(
                graphics,
                leftPlayerX, leftPlayerY,
                playerModelSize,
             getPlayerFromUUID(menu.guiData.getTraders().getFirst()) ,
                mouseX,
                mouseY
        );

        renderPlayerModel(
                graphics,
                i + this.imageWidth, j + headerH + rows * rowTileH,
                playerModelSize,
                getPlayerFromUUID(menu.guiData.getTraders().get(1)),
                mouseX,
                mouseY
        );
    }

    private void renderPlayerModel(
            GuiGraphics g,
            int x,
            int y,
            int scale,
            Player player,
            int mouseX,
            int mouseY
    ) {
        if (player == null) return;
        g.pose().pushMatrix();

        try {
            // FIX: set proper 3D lighting context
            g.enableScissor(0, 0, this.width, this.height);

            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    g,
                    x,
                    y,
                    x + 75,
                    y + 78,
                    scale,
                    0.0625F, mouseX, mouseY,
                    player
            );

        } finally {
            g.disableScissor();
            g.pose().popMatrix();
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int i, int j){

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // First let the textbox handle the click
        if (extraMoneyField.mouseClicked(mouseX, mouseY, button)) {
            extraMoneyField.setFocused(true);
            return true;
        }

        // Clicked outside → remove focus
        extraMoneyField.setFocused(false);

        // Continue normal mouse click flow
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // ESC key
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            // If the text field is focused → unfocus instead of closing screen
            if (extraMoneyField.isFocused()) {
                extraMoneyField.setFocused(false);
                return true; // stop ESC from closing the screen
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }





}
