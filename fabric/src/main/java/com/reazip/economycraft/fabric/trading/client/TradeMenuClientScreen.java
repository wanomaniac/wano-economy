package com.reazip.economycraft.fabric.trading.client;

import com.reazip.economycraft.fabric.trading.packets.msgs.RequestPlayerBalanceC2SPacket;
import com.reazip.economycraft.fabric.trading.packets.msgs.UpdateExtraMoneyPayloadC2SP;
import com.reazip.economycraft.fabric.trading.types.TradeInventoryType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.gui.components.EditBox;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

public class TradeMenuClientScreen extends AbstractContainerScreen<TradeInventoryType> {
    private EditBox extraMoneyField;
    public Long playerCurrentMoney = -1L;
    public Long otherPlayerMoney = -1L;
    private long lastMoney = 0L;
    public boolean hasSynchronizedMoney = false;
    private boolean hasTextboxChanged = false;
    private boolean textBoxButtonStatus = true;

    public TradeMenuClientScreen(TradeInventoryType menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 17 + menu.getRowCount() * 18 + 96;


    }

    public void syncExtraMoney(UUID player, long amount) {
        if(menu.guiData.isA()){
            if(player.equals(menu.guiData.playerA())){
                extraMoneyField.setValue(String.valueOf(amount));
            } else {
                otherPlayerMoney = amount;
            }
        } else {
            if(player.equals(menu.guiData.playerB())){
                extraMoneyField.setValue(String.valueOf(amount));
            } else {
                otherPlayerMoney = amount;
            }
        }
    }

    public void setMoneyTextboxStatus(boolean enabled) {
        extraMoneyField.setEditable(enabled);
        textBoxButtonStatus = enabled;
    }

    @Override
    protected void init() {
        super.init();

        if(playerCurrentMoney == -1L) {
            ClientPlayNetworking.send(new RequestPlayerBalanceC2SPacket());
        }
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
        extraMoneyField.setValue("0");
        extraMoneyField.setFilter(s -> s.matches("\\d*"));

        extraMoneyField.setResponder(text -> {
            if (text.isEmpty()){
                hasTextboxChanged = false;
                return;
            }

            hasTextboxChanged = true;

            try {
                long value = Long.parseLong(text);

                if (value > playerCurrentMoney) {
                    extraMoneyField.setValue(String.valueOf(playerCurrentMoney));
                }

//             ClientPlayNetworking.send(new UpdateExtraMoneyPayloadC2SP(value)); too laggy
            } catch (NumberFormatException ignored) {
                extraMoneyField.setValue("0");
            }
        });

        this.addWidget(this.extraMoneyField);
        this.setInitialFocus(this.extraMoneyField);
    }


    private static final ResourceLocation TEXTURE =
            ResourceLocation.parse("minecraft:textures/gui/container/generic_54.png");


    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        if(playerCurrentMoney == -1L) {
            g.drawCenteredString(this.font, Component.literal("Loading..."), this.width / 2, this.height / 2, 0xFFFFFFFF);
            return;
        }
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
        if(playerCurrentMoney == -1L) return;

        long value;
        try {
            value = Long.parseLong(extraMoneyField.getValue());
        } catch (NumberFormatException e) {
            value = lastMoney;
        }
        if(!extraMoneyField.isFocused() && value != lastMoney && textBoxButtonStatus){
            lastMoney = value;
            ClientPlayNetworking.send(new UpdateExtraMoneyPayloadC2SP(lastMoney));
            hasSynchronizedMoney = false;
        }

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

        if(!extraMoneyField.isFocused() && hasTextboxChanged && textBoxButtonStatus) {
            if (!hasSynchronizedMoney) {
                    graphics.drawString(this.font, Component.literal("Saving.... Don't ready up yet."),
                    leftCenterX - extraMoneyField.getWidth() / 2,
                    leftTopY + 45, 0xFFFF0000
                    );
            } else {
                graphics.drawString(this.font, Component.literal("Saved! You can ready up."),
                        leftCenterX - extraMoneyField.getWidth() / 2,
                        leftTopY + 45, 0xFF00FF00
                );
            }
        } else if(!textBoxButtonStatus){
            graphics.drawString(this.font, Component.literal("Unready up to edit your money!"),
                    leftCenterX - extraMoneyField.getWidth() / 2,
                    leftTopY + 45, 0xFFFF0000
            );
        }



        // LEFT PLAYER LABEL
        graphics.drawCenteredString(
                this.font,
                Component.literal("YOU!"),
                leftCenterX + 20,
                leftTopY + 45,
                0xFFFFFFFF
        );

        // MONEY TEXT (read-only)
        graphics.drawCenteredString(
                this.font,
                Component.literal("Your realtime money: " + (otherPlayerMoney > 0 ? playerCurrentMoney - value + otherPlayerMoney : playerCurrentMoney - value)),
                leftCenterX,
                leftTopY + 5,  // below the player label
                0xFF00FF00
        );

        // ADDITIONAL TRADE MONEY OUTGOING
        graphics.drawCenteredString(
                this.font,
                Component.literal("Additional Trade money outgoing: " + value),
                leftCenterX,
                leftTopY + 15,
                0xFFFFFFFF
        );


        // ADDITIONAL TRADE MONEY INCOMING
        if(otherPlayerMoney > 0) {
            graphics.drawCenteredString(
                    this.font,
                    Component.literal("Incoming trade money: " + otherPlayerMoney),
                    rightCenterX,
                    rightTopY - 40,
                    0xFFFFFFFF
            );
        }

        // LEFT PLAYER (You)
        renderPlayerModel(
                graphics,
                leftPlayerX, leftPlayerY,
                playerModelSize,
                menu.guiData.isA() ? getPlayerFromUUID(menu.guiData.playerA()) : getPlayerFromUUID(menu.guiData.playerB()),
                mouseX,
                mouseY
        );

        // RIGHT PLAYER (Partner)
        renderPlayerModel(
                graphics,
                i + this.imageWidth, j + headerH + rows * rowTileH,
                playerModelSize,
                menu.guiData.isA() ? getPlayerFromUUID(menu.guiData.playerB()) : getPlayerFromUUID(menu.guiData.playerA()),
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
