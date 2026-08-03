package com.wanomaniac.economy.trading.client;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.ModIdentifier;
import com.wanomaniac.economy.client.AbstractInputContainerScreen;
import com.wanomaniac.economy.client.GUIInputUtil;
import com.wanomaniac.economy.client.VersionGuiHandler;
import com.wanomaniac.economy.client.input.CharacterEvent;
import com.wanomaniac.economy.client.input.KeyEvent;
import com.wanomaniac.economy.client.input.MouseButtonEvent;
import com.wanomaniac.economy.trading.packets.msgs.TradeRequestPlayerBalanceC2SPacket;
import com.wanomaniac.economy.trading.packets.msgs.UpdateExtraMoneyPayloadC2SPacket;
import com.wanomaniac.economy.trading.types.TradeInventoryType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.gui.components.EditBox;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

public class TradeMenuClientScreen extends AbstractInputContainerScreen<TradeInventoryType> {
    EditBox extraMoneyField;
    public Long playerCurrentMoney = -1L;
    public Long otherPlayerMoney = -1L;
    long lastMoney = 0L;
    public boolean hasSynchronizedMoney = false;
    boolean hasTextboxChanged = false;
    boolean textBoxButtonStatus = true;
    RemotePlayer dummyThirdPlayer = null;

    public TradeMenuClientScreen(TradeInventoryType menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 17 + menu.getRowCount() * 18 + 96;
    }

    @Override
    public boolean whenKeyPressed(KeyEvent event) {
        // ESC key
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            if (extraMoneyField.isFocused()) {
                extraMoneyField.setFocused(false);
                return true; // stop ESC from closing the screen
            }
        } else if(extraMoneyField.isFocused()){
            GUIInputUtil.onEditBoxKeyPressed(extraMoneyField, event);
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
        if (GUIInputUtil.onEditBoxMouseClicked(extraMoneyField, event, isDoubleClick)) {
            extraMoneyField.setFocused(true);
            return true;
        }
        extraMoneyField.setFocused(false);

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

    // add highlight for inventory holding item for showing items
    @Override
    protected void init() {
        super.init();

        int top  = (this.height - this.imageHeight) / 2;
        this.inventoryLabelY = top + menu.getRowCount() * 18 + 17;

        int i = this.leftPos;
        int j = this.topPos;
        int leftAnchorX = Math.max(10, i - 70);
        int leftCenter = leftAnchorX + 15; // relative left space
        int boxWidth = (int) (this.imageWidth * 0.55f);
        int boxHeight = 18;
        int boxX = leftCenter - (boxWidth / 2);
        int boxY = j + 18;

        // EDIT BOX (money)
        this.extraMoneyField = new EditBox(
                this.font,
                boxX, //leftCenter - 160 / 2,   // X
                boxY,   // Y
                boxWidth,                 // width
                boxHeight,                 // height
                Component.literal("Extra money")
        );
        extraMoneyField.setMaxLength(19);
        extraMoneyField.setValue("0");
        extraMoneyField.setFilter(s -> s.matches("\\d*"));
        extraMoneyField.setResponder(text -> {
            try {
                long val = Long.parseLong(text);
                if (val != lastMoney && textBoxButtonStatus) {
                    hasTextboxChanged = true;
                    lastMoney = val;
                    CommonEconomy.packets.sendToServer(new UpdateExtraMoneyPayloadC2SPacket(lastMoney));
                    hasSynchronizedMoney = false;
                }
            } catch (NumberFormatException ignored) {}
        });

        this.addWidget(this.extraMoneyField);
        this.setInitialFocus(this.extraMoneyField);
        this.extraMoneyField.setFocused(false);

        if(playerCurrentMoney == -1L) {
            CommonEconomy.packets.sendToServer(new TradeRequestPlayerBalanceC2SPacket());
        }
    }


    private static final ModIdentifier TEXTURE =
            ModIdentifier.parse("minecraft:textures/gui/container/generic_54.png");


    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        TradeMenuClientScreenUtil.render(this, g, mouseX, mouseY, delta);
        super.render(g, mouseX, mouseY, delta);
        this.renderTooltip(g, mouseX, mouseY);
    }

    private Player getPlayerFromUUID(UUID id){
        assert Minecraft.getInstance().level != null;
        return Minecraft.getInstance().level.getPlayerByUUID(id);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        if(playerCurrentMoney == -1L || extraMoneyField == null) return;

        int i = this.leftPos;
        int j = this.topPos;

        int rows = menu.getRowCount(); // 7
        int headerH = 17;
        int rowTileH = 18;
        int footerH = 96;

        int textureW = 256;
        int textureH = 256;
        int rowTileV = 17;
        int footerV = 126;

        // 1. HEADER
        VersionGuiHandler.blitGUI(graphics, TEXTURE, i, j, 0, 0, this.imageWidth, headerH, textureW, textureH);

        // 2. ROWS
        for (int r = 0; r < rows; r++) {
            VersionGuiHandler.blitGUI(graphics, TEXTURE, i, j + headerH + (r * rowTileH), 0, rowTileV, this.imageWidth, rowTileH, textureW, textureH);
        }

        // 3. FOOTER
        int footerY = j + headerH + (rows * rowTileH);
        VersionGuiHandler.blitGUI(graphics, TEXTURE, i, footerY, 0, footerV, this.imageWidth, footerH, textureW, textureH);

        // Titles
        graphics.drawString(this.font, this.title, i + 6, j + 6, -12566464, false);
        graphics.drawString(this.font, this.playerInventoryTitle, i + 6, footerY + 3, -12566464, false);

        TradeMenuClientScreenUtil.renderBg(this, graphics, partialTick, mouseX, mouseY, i, j, imageWidth);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int i, int j){

    }


    @Override
    public void onClose() {
        super.onClose();
        dummyThirdPlayer = null;
    }
}
