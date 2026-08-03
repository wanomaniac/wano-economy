package com.wanomaniac.economy.auctioning.packets.msgs;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.IdentifierUtils;
import com.wanomaniac.economy.ModIdentifier;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import java.util.UUID;

// Notifies a client of a bidder joining
public record BidderLeavePacket(UUID id) implements CustomPacketPayload {
    public static final Type<BidderLeavePacket> TYPE =
            new Type<>(IdentifierUtils.toNative(ModIdentifier.fromNamespaceAndPath(CommonEconomy.MOD_ID,"bidder_leave")));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Codec for encoding/decoding
    public static final StreamCodec<RegistryFriendlyByteBuf, BidderLeavePacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            BidderLeavePacket::id, // Getter method reference
            BidderLeavePacket::new   // Constructor reference
    );
}
