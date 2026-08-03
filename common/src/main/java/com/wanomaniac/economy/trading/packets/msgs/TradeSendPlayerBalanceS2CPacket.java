package com.wanomaniac.economy.trading.packets.msgs;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.IdentifierUtils;
import com.wanomaniac.economy.ModIdentifier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record TradeSendPlayerBalanceS2CPacket(long balance) implements CustomPacketPayload {
    public static final Type<TradeSendPlayerBalanceS2CPacket> TYPE =
            new Type<>(IdentifierUtils.toNative(ModIdentifier.fromNamespaceAndPath(CommonEconomy.MOD_ID,"trade_send_player_balance")));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Codec for encoding/decoding
    public static final StreamCodec<FriendlyByteBuf, TradeSendPlayerBalanceS2CPacket> CODEC =
            StreamCodec.of(
                    (buf, payload) -> buf.writeLong(payload.balance),
                    buf -> new TradeSendPlayerBalanceS2CPacket(buf.readLong())
            );
}
