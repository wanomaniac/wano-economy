package com.wanomaniac.economy.auctioning.packets.msgs;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.IdentifierUtils;
import com.wanomaniac.economy.ModIdentifier;
import com.wanomaniac.economy.auctioning.types.AuctionGuiData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

// Client confirms the bidding and tells the server what exactly has been confirmed.
public record StartBiddingItemC2SPacket(AuctionGuiData data, int slotIndex, Long startingPrice) implements CustomPacketPayload {
    public static final Type<StartBiddingItemC2SPacket> TYPE =
            new Type<>(IdentifierUtils.toNative(ModIdentifier.fromNamespaceAndPath(CommonEconomy.MOD_ID, "start_bidding_item")));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, StartBiddingItemC2SPacket> CODEC = StreamCodec.composite(
            AuctionGuiData.STREAM_CODEC, StartBiddingItemC2SPacket::data,
            ByteBufCodecs.VAR_INT, StartBiddingItemC2SPacket::slotIndex,
            ByteBufCodecs.VAR_LONG, StartBiddingItemC2SPacket::startingPrice,
            StartBiddingItemC2SPacket::new
    );
}
