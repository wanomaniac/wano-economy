package com.wanomaniac.economy.client;

import com.wanomaniac.economy.client.input.KeyEvent;
import com.wanomaniac.economy.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;

public class GUIInputUtil {
    public static boolean onEditBoxMouseClicked(EditBox editBox, MouseButtonEvent event, boolean isDoubleClick){
        return editBox.mouseClicked(event.x(), event.y(), event.button());
    }

    public static boolean onEditBoxKeyPressed(EditBox editBox, KeyEvent event){
        return editBox.keyPressed(event.key(), event.scancode(), event.modifiers());
    }

    public static boolean onButtonMouseClicked(Button button, MouseButtonEvent event, boolean isDoubleClick){
        return button.mouseClicked(event.x(), event.y(), event.button());
    }
}
