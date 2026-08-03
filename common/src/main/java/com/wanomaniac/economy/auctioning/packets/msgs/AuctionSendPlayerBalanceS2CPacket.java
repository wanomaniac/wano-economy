package com.wanomaniac.economy.auctioning.packets.msgs;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.IdentifierUtils;
import com.wanomaniac.economy.ModIdentifier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record AuctionSendPlayerBalanceS2CPacket(long balance) implements CustomPacketPayload {
    public static final Type<AuctionSendPlayerBalanceS2CPacket> TYPE =
            new Type<>(IdentifierUtils.toNative(ModIdentifier.fromNamespaceAndPath(CommonEconomy.MOD_ID,"auction_send_player_balance")));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Codec for encoding/decoding
    public static final StreamCodec<FriendlyByteBuf, AuctionSendPlayerBalanceS2CPacket> CODEC =
            StreamCodec.of(
                    (buf, payload) -> buf.writeLong(payload.balance),
                    buf -> new AuctionSendPlayerBalanceS2CPacket(buf.readLong())
            );
}
