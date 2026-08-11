package com.wanomaniac.economy.auctioning;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.item.ItemStack;

public class AuctionUtil {
    public static Component getItemComponent(ItemStack bidding){
        Component itemDisplayName = bidding.getHoverName();
        return itemDisplayName.copy().withStyle(style ->
                style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(bidding))).withColor(ChatFormatting.DARK_PURPLE).withBold(true)
        );
    }

    public static Component createAsClickableCommandComponent(String command){
        return Component.literal(command)
                .withStyle(style -> style
                        .withColor(ChatFormatting.GREEN)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to join auction.")))
                );
    }
}
