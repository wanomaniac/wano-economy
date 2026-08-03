package com.wanomaniac.economy.auctioning.client.widgets;

import com.wanomaniac.economy.auctioning.server.ItemBidding;
import com.wanomaniac.economy.auctioning.types.AuctionGuiData;
import com.wanomaniac.economy.client.VersionGuiHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;
import java.util.Optional;

public class BiddingInfoWidget {
    public static void render(GuiGraphics graphics, ItemBidding bidding, int width, int height, int mouseX, int mouseY){
        int itemX = (width / 2);
        int itemY = (height / 2);

        VersionGuiHandler.renderItemAndDecorations(graphics, Minecraft.getInstance().font, bidding.item(), itemX, itemY - 28, 2.5f);

        int tooltipY = height - 50;
        Font font = Minecraft.getInstance().font;
        List<Component> textTooltip = Screen.getTooltipFromItem(Minecraft.getInstance(), bidding.item());
        Optional<TooltipComponent> imageTooltip = bidding.item().getTooltipImage();
        List<ClientTooltipComponent> components = textTooltip.stream()
                .map(Component::getVisualOrderText)
                .map(ClientTooltipComponent::create)
                .collect(java.util.stream.Collectors.toList());
        imageTooltip.ifPresent(tp -> components.add(1, ClientTooltipComponent.create(tp)));

        int maxTooltipWidth = 0;
        for (ClientTooltipComponent component : components) {
            int componentWidth = component.getWidth(font);
            if (componentWidth > maxTooltipWidth) {
                maxTooltipWidth = componentWidth;
            }
        }

        int tooltipX = itemX - (maxTooltipWidth / 2) - 5;

        VersionGuiHandler.renderItemTooltip(
                graphics,
                Minecraft.getInstance().font,
                bidding.item(),
                tooltipX,
                tooltipY,
                textTooltip
        );
    }

}
