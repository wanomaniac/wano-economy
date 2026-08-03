package com.wanomaniac.economy.client;


import com.wanomaniac.economy.client.input.CharacterEvent;
import com.wanomaniac.economy.client.input.KeyEvent;
import com.wanomaniac.economy.client.input.MouseButtonEvent;
import com.wanomaniac.economy.client.input.MouseButtonInfo;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;


public abstract class AbstractInputScreen extends Screen implements InputScreenInterface {
    public AbstractInputScreen(Component title) {
        super(title);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(!whenKeyPressed(new KeyEvent(keyCode, scanCode, modifiers))) return super.keyPressed(keyCode, scanCode, modifiers);
        else return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if(!whenKeyReleased(new KeyEvent(keyCode, scanCode, modifiers))) return super.keyReleased(keyCode, scanCode, modifiers);
        else return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(!whenMouseClicked(new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(button, 0)), false)) return super.mouseClicked(mouseX, mouseY, button);
        else return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(!whenMouseReleased(new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(button, 0)))) return super.mouseReleased(mouseX, mouseY, button);
        else return true;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if(!whenCharTyped(new CharacterEvent(codePoint, modifiers))) return super.charTyped(codePoint, modifiers);
        else return true;
    }
}

