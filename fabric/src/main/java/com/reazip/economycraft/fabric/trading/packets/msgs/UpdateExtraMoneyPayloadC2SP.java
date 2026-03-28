package com.reazip.economycraft.fabric.trading.packets.msgs;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UpdateExtraMoneyPayloadC2SP(long amount) implements CustomPacketPayload {
    public static final Type<UpdateExtraMoneyPayloadC2SP> TYPE =
            new Type<>(ResourceLocation.parse("economycraft:update_extra_money"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Codec for encoding/decoding
    public static final StreamCodec<FriendlyByteBuf, UpdateExtraMoneyPayloadC2SP> CODEC =
            StreamCodec.of(
                    (buf, payload) -> buf.writeLong(payload.amount),
                    buf -> new UpdateExtraMoneyPayloadC2SP(buf.readLong())
            );
}


