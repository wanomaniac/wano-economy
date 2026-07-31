package com.wanomaniac.economy.client;


import com.wanomaniac.economy.IdentifierUtils;
import com.wanomaniac.economy.ModIdentifier;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VersionGuiHandler {
    public static void blitGUI(GuiGraphics graphics, ModIdentifier identifier, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight){
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
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
                RenderPipelines.GUI_TEXTURED,
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

        List<ClientTooltipComponent> clientComponents = new ArrayList<>(
                textTooltip.stream()
                        .map(Component::getVisualOrderText)
                        .map(ClientTooltipComponent::create)
                        .toList()
        );

        Optional<TooltipComponent> imageTooltip = stack.getTooltipImage();
        imageTooltip.ifPresent(tp -> clientComponents.add(1, ClientTooltipComponent.create(tp)));

        graphics.renderTooltip(
                font,
                clientComponents,
                x,
                y,
                DefaultTooltipPositioner.INSTANCE,
                null // Default background texture
        );
    }

    public static void renderItemAndDecorations(GuiGraphics graphics, Font font, ItemStack item, int x, int y, float scale){
        graphics.pose().pushMatrix();

        // 1. Build a JOML Matrix3x2f with Translation + Pivot Offset + Scaling
        Matrix3x2f transform = new Matrix3x2f()
                .translate(x, y)               // Move to screen position
                .translate(8.0f, 8.0f)                 // Move to 16x16 item center pivot
                .scale(scale, scale)                   // Scale up
                .translate(-8.0f, -8.0f);              // Move pivot back

        // 2. Apply this transform matrix to the pose stack
        graphics.pose().mul(transform);

        // 3. Render item at (0, 0) relative to transformed matrix
        graphics.renderItem(item, 0, 0);
        graphics.renderItemDecorations(font, item, 0, 0);

        graphics.pose().popMatrix();
    }
}
