package com.wanomaniac.economy.auctioning.packets.msgs;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.IdentifierUtils;
import com.wanomaniac.economy.ModIdentifier;
import com.wanomaniac.economy.auctioning.types.AuctionGuiData;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

// Notifies a client of a bidder joining
public record BidderJoinS2CPacket(UUID playerID, AuctionGuiData auctionData) implements CustomPacketPayload {
    public static final Type<BidderJoinS2CPacket> TYPE =
            new Type<>(IdentifierUtils.toNative(ModIdentifier.fromNamespaceAndPath(CommonEconomy.MOD_ID,"bidder_join")));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Codec for encoding/decoding
    public static final StreamCodec<RegistryFriendlyByteBuf, BidderJoinS2CPacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            BidderJoinS2CPacket::playerID, // Getter method reference
            AuctionGuiData.STREAM_CODEC,
            BidderJoinS2CPacket::auctionData,
            BidderJoinS2CPacket::new   // Constructor reference
    );
}
