package com.wanomaniac.economy.trading.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import org.jetbrains.annotations.NotNull;

public class MenuUtil {
    public static @NotNull Font getFont() {
        return Minecraft.getInstance().font;
    }
}
