package com.wanomaniac.economy.auctioning.server;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.trading.NbtUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class AuctionSession {
    public final UUID id;
    public transient ServerPlayer auctioneer;
    public UUID auctioneerID;
    public ArrayList<UUID> bidders = new ArrayList<>();
    public ArrayList<UUID> biddersInactive = new ArrayList<>();
    public final ArrayList<NotificationRecord> inactiveBidderNotifications = new ArrayList<>();
    public Map<UUID, ItemBidding> biddings = new HashMap<>();
    public UUID lastBidding;
    public boolean cancelled;
    public boolean active;

    public AuctionSession(UUID id, UUID auctioneerID, ArrayList<UUID> bidders, ArrayList<UUID> biddersInactive, Map<UUID, ItemBidding>  biddings, UUID lastBidding, boolean active){
        this.id = id;
        this.auctioneerID = auctioneerID;
        this.bidders = bidders;
        this.biddersInactive = biddersInactive;
        this.biddings = biddings;
        this.lastBidding = lastBidding;
        this.active = active;
    }

    public AuctionSession(MinecraftServer server, CompoundTag tag){
        this.id = UUID.fromString(NbtUtil.getStringFromCompound(tag,"Id").get());

        this.auctioneerID = UUID.fromString(NbtUtil.getStringFromCompound(tag, "Auctioneer").get());
        this.auctioneer = null; // note: will always be null when server starts up for snapshots. So ill use this as a safety check to prevent any live session functions from working
        ListTag bidderList = NbtUtil.getStringListFromCompound(tag, "Bidders").get();
        for (Tag t : bidderList) {
            bidders.add(UUID.fromString(NbtUtil.getTagAsString(t).get()));
        }

        ListTag biddings = NbtUtil.getListFromCompound(tag, "Biddings").get();
        for (int i = 0; i < biddings.size(); i++) {
            CompoundTag biddingTag = NbtUtil.getCompoundFromList(biddings, i).get();
            UUID id = UUID.fromString(NbtUtil.getStringFromCompound(biddingTag, "Id").get());
            ItemBidding bidding = new ItemBidding(biddingTag, server);
            this.biddings.put(id, bidding);
        }
        if(!NbtUtil.getStringFromCompound(tag, "LastBidding").get().isEmpty()) {
            lastBidding = UUID.fromString(NbtUtil.getStringFromCompound(tag, "LastBidding").get());
        }
        active = NbtUtil.getCompoundBooleanOr(tag, "Active", false);
    }

    public AuctionSession(UUID id, ServerPlayer auctioneer){
        active = true;
        this.id = id;
        this.auctioneerID = auctioneer.getUUID();
        this.auctioneer = auctioneer;
    }

    public AuctionSession(ServerPlayer auctioneer){
        active = true;
        this.id = UUID.randomUUID();
        this.auctioneerID = auctioneer.getUUID();
        this.auctioneer = auctioneer;
    }

    public ItemBidding startBidding(ItemStack item, Long startingPrice){
        ItemBidding bidding = new ItemBidding(item, startingPrice);
        biddings.put(bidding.id(), bidding);
        lastBidding = bidding.id();

        return biddings.get(bidding.id());
    }

    public void cancelBidding(MinecraftServer server){
        ItemBidding bidding = getCurrentBidding();
        if(bidding.highestBidder() != null) return;

        // the player will permanantly lose their item if they are disconnected. We'll snapshot it.
        if(auctioneer != null && !auctioneer.hasDisconnected()) auctioneer.getInventory().placeItemBackInInventory(bidding.item().copy());
        else {
            getCurrentBidding().finalized = false;
            return; // cannot delete bidding, snapshot MUST be stored.
        }

        for (UUID inactivePlayer : biddersInactive) {
            ServerPlayer playerInactive = server.getPlayerList().getPlayer(inactivePlayer);
            if (playerInactive == null) continue;

            if (!hasInactivePlayerBeenNotified(inactivePlayer, 4)) {
                Component message = Component.literal("[AUCTION] The bidding was cancelled!") // todo: translatable
                        .withStyle(ChatFormatting.RED);

                playerInactive.sendSystemMessage(message);
                inactiveBidderNotifications.add(new NotificationRecord(inactivePlayer, 4));
            }
        }

        lastBidding = null;
        biddings.remove(bidding.id());
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

    public CompoundTag save(MinecraftServer server){
        CompoundTag tag = new CompoundTag();
        tag.put("Id", StringTag.valueOf(id.toString()));
        tag.put("Auctioneer", StringTag.valueOf(auctioneerID.toString()));

        // Save traders
        ListTag bidderList = new ListTag();
        for (UUID bidder : bidders) {
            bidderList.add(StringTag.valueOf(bidder.toString()));
        }
        tag.put("Bidders", bidderList);

        ListTag itemBiddingTag = new ListTag();
        for (Map.Entry<UUID, ItemBidding> bidding : biddings.entrySet()) {
            itemBiddingTag.add(bidding.getValue().save(server));
        }
        tag.put("Biddings", itemBiddingTag);
        tag.put("LastBidding", StringTag.valueOf(lastBidding != null ? lastBidding.toString() : ""));
        tag.put("Active", ByteTag.valueOf(active));

        return tag;
    }

    public boolean didBiddingExpire() {
        return getCurrentBidding() != null && getCurrentBidding().isExpired();
    }

    public ItemBidding[] getAllBiddings() {
        return biddings.values().toArray(new ItemBidding[0]);
    }

    public UUID id() { return id; }
    public UUID auctioneerId() { return auctioneerID; }
    public ArrayList<UUID> bidders() { return bidders; }
    public ArrayList<UUID> biddersInactive() { return biddersInactive; }
    public Map<UUID, ItemBidding>  biddings() { return biddings; }
    public UUID lastBidding() { return lastBidding; }
    public boolean active() { return active; }

    public static final StreamCodec<RegistryFriendlyByteBuf, AuctionSession> STREAM_CODEC = StreamCodec.of(
            // -----------------------------------------------------------------
            // ENCODER (Writing data to ByteBuf)
            // -----------------------------------------------------------------
            (buf, session) -> {
                // 1. ID
                UUIDUtil.STREAM_CODEC.encode(buf, session.id());

                // 2. Auctioneer ID
                UUIDUtil.STREAM_CODEC.encode(buf, session.auctioneerId());

                // 3. Bidders List
                ByteBufCodecs.collection(ArrayList::new, UUIDUtil.STREAM_CODEC).encode(buf, session.bidders());

                // 4. Inactive Bidders List
                ByteBufCodecs.collection(ArrayList::new, UUIDUtil.STREAM_CODEC).encode(buf, session.biddersInactive());

                // 5. Biddings
                ByteBufCodecs.map(HashMap::new, UUIDUtil.STREAM_CODEC, ItemBidding.STREAM_CODEC)
                        .encode(buf, (HashMap<UUID, ItemBidding>) session.biddings());

                // 6. Optional Last Bidding
                ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC)
                        .encode(buf, java.util.Optional.ofNullable(session.lastBidding()));

                // 7. Active
                ByteBufCodecs.BOOL.encode(buf, session.active());
            },

            // -----------------------------------------------------------------
            // DECODER (Reading data from ByteBuf in exact same order)
            // -----------------------------------------------------------------
            buf -> {
                UUID id = UUIDUtil.STREAM_CODEC.decode(buf);
                UUID auctioneerId = UUIDUtil.STREAM_CODEC.decode(buf);

                ArrayList<UUID> bidders = ByteBufCodecs.collection(
                        ArrayList::new,
                        UUIDUtil.STREAM_CODEC
                ).decode(buf);

                ArrayList<UUID> biddersInactive = ByteBufCodecs.collection(
                        ArrayList::new,
                        UUIDUtil.STREAM_CODEC
                ).decode(buf);

                Map<UUID, ItemBidding> biddings = ByteBufCodecs.map(
                        HashMap::new,
                        UUIDUtil.STREAM_CODEC,
                        ItemBidding.STREAM_CODEC
                ).decode(buf);

                UUID lastBidding = ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC)
                        .decode(buf)
                        .orElse(null);

                boolean active = ByteBufCodecs.BOOL
                        .decode(buf);

                return new AuctionSession(
                        id,
                        auctioneerId,
                        bidders,
                        biddersInactive,
                        biddings,
                        lastBidding,
                        active
                );
            }
    );
}
