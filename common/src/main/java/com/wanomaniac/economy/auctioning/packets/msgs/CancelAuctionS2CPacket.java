package com.wanomaniac.economy.auctioning.packets.msgs;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.IdentifierUtils;
import com.wanomaniac.economy.ModIdentifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

// Notifies a client of a bidder joining
public record CancelAuctionS2CPacket() implements CustomPacketPayload {
    public static final Type<CancelAuctionS2CPacket> TYPE =
            new Type<>(IdentifierUtils.toNative(ModIdentifier.fromNamespaceAndPath(CommonEconomy.MOD_ID,"cancel_auction")));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Codec for encoding/decoding
    public static final StreamCodec<RegistryFriendlyByteBuf, CancelAuctionS2CPacket> CODEC = StreamCodec.unit(
            new CancelAuctionS2CPacket()
    );
}
