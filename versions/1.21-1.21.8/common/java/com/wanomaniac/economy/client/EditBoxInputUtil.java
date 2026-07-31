package com.wanomaniac.economy.client;

import com.wanomaniac.economy.client.input.KeyEvent;
import com.wanomaniac.economy.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.EditBox;

public class EditBoxInputUtil {
    public static boolean onMouseClicked(EditBox editBox, MouseButtonEvent event, boolean isDoubleClick){
        return editBox.mouseClicked(event.x(), event.y(), event.button());
    }

    public static boolean onKeyPressed(EditBox editBox, KeyEvent event){
        return editBox.keyPressed(event.key(), event.scancode(), event.modifiers());
    }
}
