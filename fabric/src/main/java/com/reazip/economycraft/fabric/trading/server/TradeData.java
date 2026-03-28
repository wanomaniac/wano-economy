package com.reazip.economycraft.fabric.trading.server;

import com.mojang.serialization.DataResult;
import com.reazip.economycraft.fabric.trading.types.TradeGuiData;
import net.minecraft.nbt.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class TradeData {
    public static final StreamCodec<RegistryFriendlyByteBuf, TradeData> PACKET_CODEC =
            new StreamCodec<>() {
                @Override
                public TradeData decode(RegistryFriendlyByteBuf buf) {
                    UUID id = buf.readUUID();

                    // Decode list of traders
                    int tradersSize = buf.readInt();
                    List<UUID> traders = new ArrayList<>(tradersSize);
                    for (int i = 0; i < tradersSize; i++) {
                        traders.add(buf.readUUID());
                    }

                    // Decode outgoingMoney
                    int outSize = buf.readInt();
                    Map<UUID, Long> outgoingMoney = new HashMap<>();
                    for (int i = 0; i < outSize; i++) {
                        outgoingMoney.put(buf.readUUID(), buf.readLong());
                    }

                    // Decode incomingMoney
                    int inSize = buf.readInt();
                    Map<UUID, Long> incomingMoney = new HashMap<>();
                    for (int i = 0; i < inSize; i++) {
                        incomingMoney.put(buf.readUUID(), buf.readLong());
                    }

                    return new TradeData(id, traders, outgoingMoney, incomingMoney, null);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, TradeData data) {
                    buf.writeUUID(data.id);

                    // Encode list of traders
                    buf.writeInt(data.traders.size());
                    for (UUID trader : data.traders) {
                        buf.writeUUID(trader);
                    }

                    // Encode outgoingMoney
                    buf.writeInt(data.outgoingMoney.size());
                    data.outgoingMoney.forEach((uuid, money) -> {
                        buf.writeUUID(uuid);
                        buf.writeLong(money);
                    });

                    // Encode incomingMoney
                    buf.writeInt(data.incomingMoney.size());
                    data.incomingMoney.forEach((uuid, money) -> {
                        buf.writeUUID(uuid);
                        buf.writeLong(money);
                    });
                }
            };

    public UUID getId() {
        return id;
    }

    public List<UUID> getTraders() {
        return traders;
    }

    public Map<UUID, Long> getOutgoingMoney() {
        return outgoingMoney;
    }

    public Map<UUID, Long> getIncomingMoney() {
        return incomingMoney;
    }

    public Map<UUID, SimpleContainer> getTradeOffers() {
        return tradeOffers;
    }

    final UUID id;
    final List<UUID> traders;
    final Map<UUID, Long> outgoingMoney;
    final Map<UUID, Long> incomingMoney;
    final Map<UUID, SimpleContainer> tradeOffers;

    public TradeData(List<UUID> traders, Map<UUID, Long> outgoing, Map<UUID, Long> incoming,  Map<UUID, SimpleContainer> offers){
        id = UUID.randomUUID();
        this.traders = traders;
        this.outgoingMoney = outgoing;
        this.incomingMoney = incoming;
        this.tradeOffers = offers;
    }

    public TradeData(UUID id, List<UUID> traders, Map<UUID, Long> outgoing, Map<UUID, Long> incoming,  Map<UUID, SimpleContainer> offers){
        this.id = id;
        this.traders = traders;
        this.outgoingMoney = outgoing;
        this.incomingMoney = incoming;
        this.tradeOffers = offers;
    }

    public static CompoundTag saveContainer(SimpleContainer container) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Size", container.getContainerSize());

        ListTag items = new ListTag();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                Tag itemStack = ItemStack.CODEC
                        .encodeStart(NbtOps.INSTANCE, stack)
                        .getOrThrow();
                items.add(itemStack);
            }
        }

        tag.put("Items", items);
        return tag;
    }

    public static SimpleContainer loadContainer(CompoundTag tag) {
        Optional<Integer> size = tag.getInt("Size");
        if(size.isEmpty()) return null;
        SimpleContainer container = new SimpleContainer(size.get());

        Optional<ListTag> itemsOpt = tag.getList("Items"); // each entry is a CompoundTag
        if(itemsOpt.isEmpty()) return null;
        ListTag items = itemsOpt.get();

        for (int i = 0; i < items.size(); i++) {
            CompoundTag stackTag = (CompoundTag) items.get(i);
            DataResult<ItemStack> stackResult = ItemStack.CODEC
                    .parse(NbtOps.INSTANCE, stackTag);
            if(stackResult.isError()) return null;

            container.setItem(i, stackResult.getOrThrow());
        }

        return container;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.put("Id", StringTag.valueOf(id.toString()));

        // Save traders
        ListTag traderList = new ListTag();
        for (UUID trader : traders) {
            traderList.add(StringTag.valueOf(trader.toString()));
        }
        tag.put("Traders", traderList);

        // Save money maps
        tag.put("OutgoingMoney", saveMoneyMap(outgoingMoney));
        tag.put("IncomingMoney", saveMoneyMap(incomingMoney));

        // Save trade offers
        CompoundTag offersTag = new CompoundTag();
        for (Map.Entry<UUID, SimpleContainer> entry : tradeOffers.entrySet()) {
            offersTag.put(entry.getKey().toString(), saveContainer(entry.getValue()));
        }
        tag.put("TradeOffers", offersTag);

        return tag;
    }

    // Helper
    private static CompoundTag saveMoneyMap(Map<UUID, Long> map) {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<UUID, Long> entry : map.entrySet()) {
            tag.putLong(entry.getKey().toString(), entry.getValue());
        }
        return tag;
    }


    public static TradeData load(CompoundTag tag){
        String id = tag.getString("Id").get();
        UUID uid = UUID.fromString(id);
        ListTag traderList = tag.getList("Traders").get();
        List<UUID> traders = new ArrayList<>();
        for (Tag t : traderList) {
            traders.add(UUID.fromString(t.asString().get()));
        }
        Map<UUID, Long> outgoingMoney = loadMoneyMap(tag.getCompound("OutgoingMoney").get());
        Map<UUID, Long> incomingMoney = loadMoneyMap(tag.getCompound("IncomingMoney").get());

        // Load trade offers
        Map<UUID, SimpleContainer> tradeOffers = new HashMap<>();
        CompoundTag offersTag = tag.getCompound("TradeOffers").get();
        for (String key : offersTag.keySet()) {
            UUID traderId = UUID.fromString(key);
            SimpleContainer container = loadContainer(offersTag.getCompound(key).get());
            tradeOffers.put(traderId, container);
        }

        return new TradeData(uid, traders, outgoingMoney, incomingMoney, tradeOffers);
    }

    // Helper
    private static Map<UUID, Long> loadMoneyMap(CompoundTag tag) {
        Map<UUID, Long> map = new HashMap<>();
        for (String key : tag.keySet()) {
            map.put(UUID.fromString(key), tag.getLong(key).get());
        }
        return map;
    }
}
