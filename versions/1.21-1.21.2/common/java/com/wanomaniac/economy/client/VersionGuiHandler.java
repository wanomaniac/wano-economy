package com.wanomaniac.economy.client;


import com.wanomaniac.economy.IdentifierUtils;
import com.wanomaniac.economy.ModIdentifier;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class VersionGuiHandler {
    public static void blitGUI(GuiGraphics graphics, ModIdentifier identifier, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight){
        graphics.blit(
                IdentifierUtils.toNative(identifier),
                x,
                y,
                u,
                v,
                width,
                height,
                textureWidth,
                textureHeight
        );
    }

    public static void blitSpriteGUI(GuiGraphics graphics, ModIdentifier identifier, int x, int y, int width, int height){
        graphics.blitSprite(
                IdentifierUtils.toNative(identifier),
                x,
                y,
                width,
                height
        );
    }

    public static void renderItemTooltip(
            GuiGraphics graphics,
            Font font,
            ItemStack stack,
            int x,
            int y,
            List<Component> textTooltip
    ) {
        if (stack.isEmpty()) return;

        Optional<TooltipComponent> imageTooltip = stack.getTooltipImage();

        graphics.renderTooltip(
                font,
                textTooltip,
                imageTooltip,
                x,
                y
        );
    }
}
