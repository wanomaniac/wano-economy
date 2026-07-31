package com.wanomaniac.economy.client;

import com.wanomaniac.economy.client.input.KeyEvent;
import com.wanomaniac.economy.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonInfo;

public class EditBoxInputUtil {
    public static boolean onMouseClicked(EditBox editBox, MouseButtonEvent event, boolean isDoubleClick){
        return editBox.mouseClicked(new net.minecraft.client.input.MouseButtonEvent(event.x(), event.y(), new MouseButtonInfo(event.buttonInfo().button(), event.buttonInfo().modifiers())), isDoubleClick);
    }

    public static boolean onKeyPressed(EditBox editBox, KeyEvent event){
        return editBox.keyPressed(new net.minecraft.client.input.KeyEvent(event.key(), event.scancode(), event.modifiers()));
    }
}
