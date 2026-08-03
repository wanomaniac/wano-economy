package com.wanomaniac.economy.trading.client;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.ModIdentifier;
import com.wanomaniac.economy.client.AbstractInputContainerScreen;
import com.wanomaniac.economy.client.VersionGuiHandler;
import com.wanomaniac.economy.client.input.CharacterEvent;
import com.wanomaniac.economy.client.input.KeyEvent;
import com.wanomaniac.economy.client.input.MouseButtonEvent;
import com.wanomaniac.economy.trading.packets.msgs.NotifySnapshotGuestLongEscapeC2SPacket;
import com.wanomaniac.economy.trading.types.TradeSnapshotType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

public class TradeMenuSnapshotClientScreen extends AbstractInputContainerScreen<TradeSnapshotType> {
    public TradeMenuSnapshotClientScreen(TradeSnapshotType menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 17 + menu.getRowCount() * 18 + 96;
    }

    @Override
    public boolean whenKeyPressed(KeyEvent event) {
        if(TradeMenuSnapshotClientScreenUtil.startEscUXThingy(event.key())){
            return true;
        }
        return true;
    }

    @Override
    public boolean whenKeyReleased(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            CommonEconomy.packets.sendToServer(new NotifySnapshotGuestLongEscapeC2SPacket(TradeMenuSnapshotClientScreenUtil.decideEscUXTHingy(event.key())));
        }
        return true;
    }

    @Override
    public boolean whenMouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return true;
    }

    @Override
    public boolean whenMouseReleased(MouseButtonEvent event) {
        return true;
    }

    @Override
    public boolean whenCharTyped(CharacterEvent event) {
        return true;
    }

    @Override
    protected void init() {
        super.init();
        // Center X and Y of GUI
        int left = (this.width - this.imageWidth) / 2;
        int top  = (this.height - this.imageHeight) / 2;

        this.inventoryLabelY = top + menu.getRowCount() * 18 + 17;
    }


    private static final ModIdentifier TEXTURE =
            ModIdentifier.parse("minecraft:textures/gui/container/generic_54.png");


    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);
        this.renderTooltip(g, mouseX, mouseY);
        TradeMenuSnapshotClientScreenUtil.render(this, g, mouseX, mouseY, delta);
    }

    private Player getPlayerFromUUID(UUID id){
        assert Minecraft.getInstance().level != null;
        return Minecraft.getInstance().level.getPlayerByUUID(id);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
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

        TradeMenuSnapshotClientScreenUtil.renderBg(this, graphics, partialTick, mouseX, mouseY, i, j, imageWidth);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int i, int j){

    }


}
