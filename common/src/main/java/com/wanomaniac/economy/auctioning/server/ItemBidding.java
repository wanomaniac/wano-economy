package com.wanomaniac.economy.auctioning.server;
import com.wanomaniac.economy.trading.NbtUtil;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class ItemBidding {
    private final UUID id;
    private final Map<UUID, Long> bidHistory;
    private UUID highestBidder;
    private long currentBid;
    private final ItemStack item;
    public boolean finalized = false;
    private boolean active;
    public static final int MAX_REMAINING_TICKS = 30 * 20; // Cap at 30 seconds

    private final int totalTicks = MAX_REMAINING_TICKS; // Fixed baseline for total progress % calculation
    private long endTimestamp = 0;   // Target expiration time in epoch milliseconds
    public static final long MS_PER_TICK = 50L;      // 1 tick = 50ms

    public ItemBidding(UUID id, Map<UUID, Long> bidHistory, UUID highestBidder, long currentBid, ItemStack item, boolean active, long endTimestamp) {
        this.id = id;
        this.bidHistory = new HashMap<>(bidHistory); // Ensure map is mutable!
        this.highestBidder = highestBidder;
        this.currentBid = currentBid;
        this.item = item;
        this.active = active;

        if(endTimestamp == 0) this.endTimestamp = System.currentTimeMillis() + (MAX_REMAINING_TICKS * MS_PER_TICK);
        else this.endTimestamp = endTimestamp;
    }

    // Convenience constructor for starting a new bidding session
    public ItemBidding(ItemStack item, Long startingPrice) {
        this(UUID.randomUUID(), new HashMap<>(), null, startingPrice, item, true, 0);
    }

    public ItemBidding(CompoundTag tag, MinecraftServer server){ // this is insecure as shit but oh well.
        id = UUID.fromString(NbtUtil.getStringFromCompound(tag, "Id").get());
        this.bidHistory = new HashMap<>();

        ListTag bidHistoryTag = NbtUtil.getListFromCompound(tag, "BidHistory").get();
        for (int i = 0; i < bidHistoryTag.size(); i++) {
            CompoundTag biddingTag = NbtUtil.getCompoundFromList(bidHistoryTag, i).get();
            UUID id = UUID.fromString(NbtUtil.getStringFromCompound(biddingTag, "Bidder").get());
            Long amount = NbtUtil.getLongFromCompound(biddingTag, "Amount").get();
            bidHistory.put(id, amount);
        }

        if(!NbtUtil.getStringFromCompound(tag, "HighestBidder").get().isEmpty()) {
            this.highestBidder = UUID.fromString(NbtUtil.getStringFromCompound(tag, "HighestBidder").get());
        }
        this.currentBid = NbtUtil.getLongFromCompound(tag, "CurrentBid").get();
        CompoundTag stackTag = NbtUtil.getCompoundFromCompound(tag, "Item").get();
        this.item = ItemStack.CODEC
                .parse(server.registryAccess().createSerializationContext(NbtOps.INSTANCE), stackTag).getOrThrow();
        active = NbtUtil.getCompoundBooleanOr(tag, "Active", false);
        finalized = NbtUtil.getCompoundBooleanOr(tag, "Finalized", false);
    }

    public void tick() {
        if (!active) return;

        if (System.currentTimeMillis() >= this.endTimestamp) {
            this.active = false;
        }
    }

    public boolean isExpired(){
        return !active;
    }

    public boolean isActive(){
        return active;
    }

    public float getRemainingSeconds() {
        long remainingMs = Math.max(0, this.endTimestamp - System.currentTimeMillis());
        return remainingMs / 1000.0f;
    }

    public int getTotalSeconds() {
        return Math.max(0, totalTicks / 20);
    }

    public int getTotalTicks() {
        return MAX_REMAINING_TICKS;
    }

    public boolean placeBid(UUID player, long price) {
        if (price < this.currentBid || !this.active || price == 0) {
            return false;
        }

        this.bidHistory.put(player, price);
        this.currentBid = price;
        this.highestBidder = player;

        long currentMs = System.currentTimeMillis();
        long remainingMs = this.endTimestamp - currentMs;
        long extensionMs = 5 * 1000L; // 5 seconds in ms
        long maxCapMs = 30 * 1000L;  // 30 seconds max cap

        if (remainingMs < (10 * 1000L)) { // Under 10 seconds left
            long newRemainingMs = Math.min(remainingMs + extensionMs, maxCapMs);
            this.endTimestamp = currentMs + newRemainingMs; // Set new expiration target
        }

        return true;
    }

    public Long getPlayerBidding(UUID id){
        return bidHistory.getOrDefault(id, 0L);
    }

    public CompoundTag save(MinecraftServer server){
        CompoundTag tag = new CompoundTag();

        tag.put("Id", StringTag.valueOf(id.toString()));
        ListTag historyList = new ListTag();
        for (Map.Entry<UUID, Long> entry : bidHistory.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("Bidder", entry.getKey().toString());
            entryTag.putLong("Amount", entry.getValue());
            historyList.add(entryTag);
        }
        tag.put("BidHistory", historyList);
        tag.put("HighestBidder", StringTag.valueOf(highestBidder != null ? highestBidder.toString() : ""));
        tag.put("CurrentBid", LongTag.valueOf(currentBid));
        tag.put("Item", ItemStack.CODEC
                .encodeStart(server.registryAccess().createSerializationContext(NbtOps.INSTANCE), item)
                .getOrThrow());
        tag.put("Active", ByteTag.valueOf(active));
        tag.put("Finalized", ByteTag.valueOf(finalized));

        return tag;
    }

    // --- Getters (Matching record-style naming for clean method references) ---
    public UUID id() { return id; }
    public Map<UUID, Long> bidHistory() { return bidHistory; }
    public UUID highestBidder() { return highestBidder; }
    public long currentBid() { return currentBid; }
    public ItemStack item() { return item; }
    public long endTimestamp() { return endTimestamp; }

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemBidding> STREAM_CODEC = StreamCodec.of(
            // -----------------------------------------------------------------
            // ENCODER (Writing data to ByteBuf)
            // -----------------------------------------------------------------
            (buf, bidding) -> {
                // 1. ID
                UUIDUtil.STREAM_CODEC.encode(buf, bidding.id());

                // 2. Bid History Map
                ByteBufCodecs.map(HashMap::new, UUIDUtil.STREAM_CODEC, ByteBufCodecs.VAR_LONG)
                        .encode(buf, (HashMap<UUID, Long>) bidding.bidHistory());

                // 3. Optional Highest Bidder (Handles null cleanly)
                ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC)
                        .encode(buf, java.util.Optional.ofNullable(bidding.highestBidder()));

                // 4. Current Bid
                ByteBufCodecs.VAR_LONG.encode(buf, bidding.currentBid());

                // 5. Item Stack
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, bidding.item());

                // 6. Active
                ByteBufCodecs.BOOL.encode(buf, bidding.isActive());

                // 7. End Timestamp
                ByteBufCodecs.VAR_LONG.encode(buf, bidding.endTimestamp());
            },

            // -----------------------------------------------------------------
            // DECODER (Reading data from ByteBuf in exact same order)
            // -----------------------------------------------------------------
            buf -> {
                UUID id = UUIDUtil.STREAM_CODEC.decode(buf);

                Map<UUID, Long> bidHistory = ByteBufCodecs.map(
                        HashMap::new,
                        UUIDUtil.STREAM_CODEC,
                        ByteBufCodecs.VAR_LONG
                ).decode(buf);

                UUID highestBidder = ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC)
                        .decode(buf)
                        .orElse(null);

                long currentBid = ByteBufCodecs.VAR_LONG.decode(buf);
                ItemStack item = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);

                boolean active = ByteBufCodecs.BOOL.decode(buf);
                long endTimestamp = ByteBufCodecs.VAR_LONG.decode(buf);

                return new ItemBidding(
                        id,
                        bidHistory,
                        highestBidder,
                        currentBid,
                        item,
                        active,
                        endTimestamp
                );
            }
    );
}