package com.reazip.economycraft.fabric.trading.packets.msgs;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record SyncExtraMoneyPayloadS2CP(UUID player, long amount) implements CustomPacketPayload {

    public static final Type<SyncExtraMoneyPayloadS2CP> TYPE =
            new Type<>(ResourceLocation.parse("economycraft:sync_extra_money"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncExtraMoneyPayloadS2CP> CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        buf.writeUUID(payload.player);
                        buf.writeLong(payload.amount);
                    },
                    buf -> new SyncExtraMoneyPayloadS2CP(buf.readUUID(), buf.readLong())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
