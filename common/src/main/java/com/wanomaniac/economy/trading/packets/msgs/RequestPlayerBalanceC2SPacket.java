package com.wanomaniac.economy.trading.packets.msgs;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.IdentifierUtils;
import com.wanomaniac.economy.ModIdentifier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RequestPlayerBalanceC2SPacket() implements CustomPacketPayload {
    public static final Type<RequestPlayerBalanceC2SPacket> TYPE =
            new Type<>(IdentifierUtils.toNative(ModIdentifier.fromNamespaceAndPath(CommonEconomy.MOD_ID,"ask_player_balance")));

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

