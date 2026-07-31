package com.wanomaniac.economy.auctioning.server;

import net.minecraft.world.item.ItemStack;
import java.util.Map;
import java.util.UUID;

public record ItemBidding(
        Map<UUID, Integer> bidHistory,  // Tracks player UUID -> total/last bid amount
        UUID highestBidder,             // UUID of current winning bidder (null if no bids)
        int currentBid,                 // Current highest bid price
        ItemStack item,                  // The actual item being auctioned
        boolean cancelled // if the auctioneer cancelled the bidding!
) {
    public ItemBidding(ItemStack item, int startingPrice) {
        this(Map.of(), null, startingPrice, item, false);
    }
}