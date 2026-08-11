package com.wanomaniac.economy.client;

import com.wanomaniac.economy.client.input.CharacterEvent;
import com.wanomaniac.economy.client.input.CursorTypes;
import com.wanomaniac.economy.client.input.KeyEvent;
import com.wanomaniac.economy.client.input.MouseButtonEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import org.lwjgl.glfw.GLFW;

import java.util.EnumMap;
import java.util.Map;

public class GUIInputUtil {
    public static boolean onEditBoxMouseClicked(EditBox editBox, MouseButtonEvent event, boolean isDoubleClick){
        return editBox.mouseClicked(event.x(), event.y(), event.button());
    }

    public static boolean onEditBoxKeyPressed(EditBox editBox, KeyEvent event){
        return editBox.keyPressed(event.key(), event.scancode(), event.modifiers());
    }

    public static boolean onEditBoxCharTyped(EditBox editBox, CharacterEvent event) {
        return editBox.charTyped((char) event.codepoint(), event.modifiers());
    }

    public static boolean onButtonMouseClicked(Button button, MouseButtonEvent event, boolean isDoubleClick){
        return button.mouseClicked(event.x(), event.y(), event.button());
    }

//    private static final Map<CursorTypes, Long> CURSOR_CACHE = new EnumMap<>(CursorTypes.class);
//
//    public static long mapFromCursorTypes(CursorTypes type) {
//        return CURSOR_CACHE.computeIfAbsent(type, t -> {
//            int glfwShape = switch (t) {
//                case ARROW -> GLFW.GLFW_ARROW_CURSOR;
//                case POINTING_HAND -> GLFW.GLFW_POINTING_HAND_CURSOR;
//            };
//            return GLFW.glfwCreateStandardCursor(glfwShape);
//        });
//    }

    // graphics does nothing
    public static void changeCursor(GuiGraphics graphics, CursorTypes cursorTypes) {
// inconsistent UX since minecraft 1.21-1.21.8 never changed their cursors to their specific UI.
//        long windowID = Minecraft.getInstance().getWindow().getWindow();
//        long mappedID = mapFromCursorTypes(cursorTypes);
//        GLFW.glfwSetCursor(windowID, mappedID);
    }

}
