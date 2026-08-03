package com.wanomaniac.economy.auctioning.packets.msgs;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.IdentifierUtils;
import com.wanomaniac.economy.ModIdentifier;
import com.wanomaniac.economy.auctioning.server.ItemBidding;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import java.util.Optional;

// Server synchronizes the bidding to a client
public record SynchronizeItemBiddingS2CPacket(Optional<ItemBidding> bidding) implements CustomPacketPayload {
    public static final Type<SynchronizeItemBiddingS2CPacket> TYPE =
            new Type<>(IdentifierUtils.toNative(ModIdentifier.fromNamespaceAndPath(CommonEconomy.MOD_ID, "synchronize_item_bidding")));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, SynchronizeItemBiddingS2CPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ItemBidding.STREAM_CODEC), SynchronizeItemBiddingS2CPacket::bidding,
            SynchronizeItemBiddingS2CPacket::new
    );
}
