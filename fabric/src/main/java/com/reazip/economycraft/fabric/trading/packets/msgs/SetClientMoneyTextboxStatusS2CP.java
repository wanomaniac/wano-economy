package com.reazip.economycraft.fabric.trading.packets.msgs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SetClientMoneyTextboxStatusS2CP(boolean enabled) implements CustomPacketPayload {
    public static final Type<SetClientMoneyTextboxStatusS2CP> TYPE =
            new Type<>(ResourceLocation.parse("economycraft:set_client_money_textbox_status"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Codec for encoding/decoding
    public static final StreamCodec<FriendlyByteBuf, SetClientMoneyTextboxStatusS2CP> CODEC =
            StreamCodec.of(
                    (buf, payload) -> buf.writeBoolean(payload.enabled),
                    buf -> new SetClientMoneyTextboxStatusS2CP(buf.readBoolean())
            );
}
