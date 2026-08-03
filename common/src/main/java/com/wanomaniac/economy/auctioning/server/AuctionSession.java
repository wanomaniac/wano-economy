package com.wanomaniac.economy.auctioning.server;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.auctioning.packets.msgs.BidderLeavePacket;
import com.wanomaniac.economy.trading.NbtUtil;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class AuctionSession {
    public final UUID id;
    public final ServerPlayer auctioneer;
    public ArrayList<UUID> bidders = new ArrayList<>();
    public ArrayList<UUID> biddersInactive = new ArrayList<>();
    public final ArrayList<NotificationRecord> inactiveBidderNotifications = new ArrayList<>();
    public final Map<UUID, ItemBidding> biddings = new HashMap<>();
    public UUID lastBidding;
    public boolean cancelled;
    public boolean active;

    public AuctionSession(MinecraftServer server, CompoundTag tag){
        this.id = UUID.fromString(NbtUtil.getStringFromCompound(tag,"Id").get());
        Optional<ListTag> bidHistoryTag = NbtUtil.getListFromCompound(tag, "BidHistory");

        UUID auctioneerID = UUID.fromString(NbtUtil.getStringFromCompound(tag, "Auctioneer").get());
        this.auctioneer = server.getPlayerList().getPlayer(auctioneerID);

        ListTag bidderList = NbtUtil.getStringListFromCompound(tag, "Bidders").get();
        for (Tag t : bidderList) {
            bidders.add(UUID.fromString(NbtUtil.getTagAsString(t).get()));
        }

        ListTag biddings = NbtUtil.getListFromCompound(tag, "Biddings").get();
        for (int i = 0; i < biddings.size(); i++) {
            CompoundTag biddingTag = NbtUtil.getCompoundFromList(biddings, i).get();
            UUID id = UUID.fromString(NbtUtil.getStringFromCompound(biddingTag, "Id").get());
            ItemBidding bidding = new ItemBidding(biddingTag);
            this.biddings.put(id, bidding);
        }
        lastBidding = UUID.fromString(NbtUtil.getStringFromCompound(tag, "LastBidding").get());
        cancelled = NbtUtil.getCompoundBooleanOr(tag, "Cancelled", false);
        active = NbtUtil.getCompoundBooleanOr(tag, "Active", false);

    }

    public AuctionSession(UUID id, ServerPlayer auctioneer){
        active = true;
        this.id = id;
        this.auctioneer = auctioneer;
    }

    public AuctionSession(ServerPlayer auctioneer){
        active = true;
        this.id = UUID.randomUUID();
        this.auctioneer = auctioneer;
    }

    public ItemBidding startBidding(ItemStack item, Long startingPrice){
        if(!biddersInactive.isEmpty()){
            bidders.removeAll(biddersInactive);
            biddersInactive.clear();
        }

        ItemBidding bidding = new ItemBidding(item, startingPrice);
        biddings.put(bidding.id(), bidding);
        lastBidding = bidding.id();

        return biddings.get(bidding.id());
    }

    public ItemBidding getCurrentBidding(){
        if (biddings.isEmpty()) {
            return null;
        }

        return biddings.get(lastBidding);
    }

    public boolean hasInactivePlayerBeenNotified(UUID player, int type){
        AtomicBoolean confirmed = new AtomicBoolean(false);
        inactiveBidderNotifications.forEach((record) -> confirmed.set(player.equals(record.id()) && type == record.notificationType()));

        return confirmed.get();
    }

    public void sendPayloadToSessionMembers(MinecraftServer server, CustomPacketPayload payload){
        CommonEconomy.packets.sendToPlayer(auctioneer, payload);
        bidders.forEach((playerID) -> {
            ServerPlayer playerBidder = server.getPlayerList().getPlayer(playerID);
            if(playerBidder == null) return;
            CommonEconomy.packets.sendToPlayer(playerBidder, payload);
        });
    }

    public ItemBidding getBiddingFromID(UUID id){
        return biddings.get(id);
    }

    public void bidderJoin(ServerPlayer player){
        if(bidders.contains(player.getUUID())) return;
        bidders.add(player.getUUID());
    }

    public void bidderLeave(ServerPlayer player){
        if(!bidders.contains(player.getUUID())) return;
        biddersInactive.add(player.getUUID());
    }

    public CompoundTag save(){
        CompoundTag tag = new CompoundTag();
        tag.put("Id", StringTag.valueOf(id.toString()));
        tag.put("Auctioneer", StringTag.valueOf(auctioneer.getUUID().toString()));

        // Save traders
        ListTag bidderList = new ListTag();
        for (UUID bidder : bidders) {
            bidderList.add(StringTag.valueOf(bidder.toString()));
        }
        tag.put("Bidders", bidderList);

        ListTag itemBiddingTag = new ListTag();
        for (Map.Entry<UUID, ItemBidding> bidding : biddings.entrySet()) {
            itemBiddingTag.add(bidding.getValue().save());
        }
        tag.put("Biddings", itemBiddingTag);
        tag.put("LastBidding", StringTag.valueOf(lastBidding.toString()));
        tag.put("Cancelled", ByteTag.valueOf(cancelled));
        tag.put("Active", ByteTag.valueOf(active));

        return tag;
    }
}
