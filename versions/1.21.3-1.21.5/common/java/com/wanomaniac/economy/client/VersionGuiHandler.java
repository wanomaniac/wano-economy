package com.wanomaniac.economy.client;


import com.wanomaniac.economy.IdentifierUtils;
import com.wanomaniac.economy.ModIdentifier;
import com.wanomaniac.economy.client.input.MouseButtonEvent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class VersionGuiHandler {
    public static void blitGUI(GuiGraphics graphics, ModIdentifier identifier, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight){
        graphics.blit(
                RenderType::guiTextured,
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
                RenderType::guiTextured,
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

        graphics.renderTooltip(font, textTooltip, stack.getTooltipImage(), x, y);
    }

    public static void renderItemAndDecorations(GuiGraphics graphics, Font font, ItemStack item, int x, int y, float scale){
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().translate(8.0f, 8.0f, 0);
        graphics.pose().scale(scale, scale, scale);
        graphics.pose().translate(-8.0f, -8.0f, 0);

        graphics.renderItem(item, 0, 0);
        graphics.renderItemDecorations(font, item, 0, 0);

        graphics.pose().popPose();
    }
}
