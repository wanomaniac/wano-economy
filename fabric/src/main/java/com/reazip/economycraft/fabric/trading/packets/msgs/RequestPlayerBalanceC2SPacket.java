package com.reazip.economycraft.fabric.trading.packets.msgs;


import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RequestPlayerBalanceC2SPacket() implements CustomPacketPayload {
    public static final Type<RequestPlayerBalanceC2SPacket> TYPE =
            new Type<>(ResourceLocation.parse("economycraft:ask_player_balance"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Codec for encoding/decoding
    public static final StreamCodec<FriendlyByteBuf, RequestPlayerBalanceC2SPacket> CODEC =
            StreamCodec.of(
                    (buf, payload) -> {},
                    buf -> new RequestPlayerBalanceC2SPacket()
            );
}

