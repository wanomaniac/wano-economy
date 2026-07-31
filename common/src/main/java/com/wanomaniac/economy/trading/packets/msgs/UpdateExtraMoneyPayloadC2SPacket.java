package com.wanomaniac.economy.trading.packets.msgs;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.IdentifierUtils;
import com.wanomaniac.economy.ModIdentifier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateExtraMoneyPayloadC2SPacket(long amount) implements CustomPacketPayload {
    public static final Type<UpdateExtraMoneyPayloadC2SPacket> TYPE =
            new Type<>(IdentifierUtils.toNative(ModIdentifier.fromNamespaceAndPath(CommonEconomy.MOD_ID, "update_extra_money")));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Codec for encoding/decoding
    public static final StreamCodec<FriendlyByteBuf, UpdateExtraMoneyPayloadC2SPacket> CODEC =
            StreamCodec.of(
                    (buf, payload) -> buf.writeLong(payload.amount),
                    buf -> new UpdateExtraMoneyPayloadC2SPacket(buf.readLong())
            );
}


