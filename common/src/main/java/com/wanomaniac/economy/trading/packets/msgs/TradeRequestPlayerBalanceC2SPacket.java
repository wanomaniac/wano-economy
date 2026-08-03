package com.wanomaniac.economy.trading.packets.msgs;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.IdentifierUtils;
import com.wanomaniac.economy.ModIdentifier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record TradeRequestPlayerBalanceC2SPacket() implements CustomPacketPayload {
    public static final Type<TradeRequestPlayerBalanceC2SPacket> TYPE =
            new Type<>(IdentifierUtils.toNative(ModIdentifier.fromNamespaceAndPath(CommonEconomy.MOD_ID,"trade_ask_player_balance")));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Codec for encoding/decoding
    public static final StreamCodec<FriendlyByteBuf, TradeRequestPlayerBalanceC2SPacket> CODEC =
            StreamCodec.of(
                    (buf, payload) -> {},
                    buf -> new TradeRequestPlayerBalanceC2SPacket()
            );
}

