package com.wanomaniac.economy.trading.types;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.UUID;


public record TradeGuiData(UUID id, UUID playerA, UUID playerB, boolean isA, Long extraMoneyA, Long extraMoneyB) {
    public static final StreamCodec<RegistryFriendlyByteBuf, TradeGuiData> PACKET_CODEC =
            new StreamCodec<>() {

                @Override
                public TradeGuiData decode(RegistryFriendlyByteBuf buf) {
                    UUID id = buf.readUUID();
                    UUID a = buf.readUUID();
                    UUID b = buf.readUUID();
                    boolean isA = buf.readBoolean();
                    Long extraMoneyA = buf.readLong();
                    Long extraMoneyB = buf.readLong();
                    return new TradeGuiData(id, a, b, isA, extraMoneyA, extraMoneyB);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, TradeGuiData data) {
                    buf.writeUUID(data.id);
                    buf.writeUUID(data.playerA);
                    buf.writeUUID(data.playerB);
                    buf.writeBoolean(data.isA);
                    buf.writeLong(data.extraMoneyA);
                    buf.writeLong(data.extraMoneyB);
                }
            };
}
