package com.wanomaniac.economy.auctioning.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class AuctionSession {
    public final UUID id;
    public final ServerPlayer auctioneer;
    public ArrayList<UUID> bidders;
    public ArrayList<ItemBidding> biddings;

    public AuctionSession(UUID id, ServerPlayer auctioneer){
        this.id = id;
        this.auctioneer = auctioneer;
    }

    public AuctionSession(ServerPlayer auctioneer){
        this.id = UUID.randomUUID();
        this.auctioneer = auctioneer;
    }
}
