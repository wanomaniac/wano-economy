package com.wanomaniac.economy.auctioning.packets.msgs;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.IdentifierUtils;
import com.wanomaniac.economy.ModIdentifier;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record BidMoneyC2SPacket(UUID id, long money) implements CustomPacketPayload {
    public static final Type<BidMoneyC2SPacket> TYPE =
            new Type<>(IdentifierUtils.toNative(ModIdentifier.fromNamespaceAndPath(CommonEconomy.MOD_ID,"bid_money")));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Codec for encoding/decoding
    public static final StreamCodec<FriendlyByteBuf, BidMoneyC2SPacket> CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC, BidMoneyC2SPacket::id,
                    ByteBufCodecs.VAR_LONG, BidMoneyC2SPacket::money,
                    BidMoneyC2SPacket::new
            );
}

