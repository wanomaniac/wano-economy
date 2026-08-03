package com.wanomaniac.economy.client;

import com.wanomaniac.economy.client.input.CharacterEvent;
import com.wanomaniac.economy.client.input.KeyEvent;
import com.wanomaniac.economy.client.input.MouseButtonEvent;

public interface InputScreenInterface {
    // When true, we continue super
    // When false, dont continue
    boolean whenKeyPressed(KeyEvent event);
    boolean whenKeyReleased(KeyEvent event);
    boolean whenMouseClicked(MouseButtonEvent event, boolean isDoubleClick);
    boolean whenMouseReleased(MouseButtonEvent event);
    boolean whenCharTyped(CharacterEvent event);
}
