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
    protected AbstractInputScreen(Component title) {
        super(title);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        if(!whenKeyPressed(new KeyEvent(event.key(), event.input(), event.modifiers()))) return super.keyPressed(event);
        else return true;
    }
    @Override
    public boolean keyReleased(net.minecraft.client.input.KeyEvent event) {
        if(!whenKeyReleased(new KeyEvent(event.key(), event.input(), event.modifiers()))) return super.keyReleased(event);
        else return true;
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean isDoubleClick) {
        if(!whenMouseClicked(new MouseButtonEvent(event.x(), event.y(), new MouseButtonInfo(event.buttonInfo().button(), event.buttonInfo().modifiers())), isDoubleClick)) return super.mouseClicked(event, isDoubleClick);
        else return true;
    }


    @Override
    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
        if(!whenMouseReleased(new MouseButtonEvent(event.x(), event.y(), new MouseButtonInfo(event.buttonInfo().button(), event.buttonInfo().modifiers())))) return super.mouseReleased(event);
        else return true;
    }

    @Override
    public boolean charTyped(net.minecraft.client.input.CharacterEvent event) {
        if(!whenCharTyped(new CharacterEvent(event.codepoint(), event.modifiers()))) return super.charTyped(event);
        else return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if(!whenMouseScrolled(mouseX, mouseY, scrollX, scrollY)) return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        else return true;
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double dragX, double dragY) {
        if(!whenMouseDragged(new MouseButtonEvent(event.x(), event.y(), new MouseButtonInfo(event.buttonInfo().button(), event.buttonInfo().modifiers())), dragX, dragY)) return super.mouseDragged(event, dragX, dragY);
        else return true;
    }
}

