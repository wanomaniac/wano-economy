package com.wanomaniac.economy.auctioning.packets.msgs;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.IdentifierUtils;
import com.wanomaniac.economy.ModIdentifier;
import com.wanomaniac.economy.auctioning.server.AuctionSession;
import com.wanomaniac.economy.auctioning.server.HistoryMenuDetails;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Optional;

// Sends a snapshot directly to client as it is.
public record InitalizeSnapshotScreenS2CPacket(AuctionSession session, Optional<HistoryMenuDetails> details) implements CustomPacketPayload {
    public static final Type<InitalizeSnapshotScreenS2CPacket> TYPE =
            new Type<>(IdentifierUtils.toNative(ModIdentifier.fromNamespaceAndPath(CommonEconomy.MOD_ID, "initalize_snapshot_screen")));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, InitalizeSnapshotScreenS2CPacket> CODEC = StreamCodec.composite(
            AuctionSession.STREAM_CODEC,
            InitalizeSnapshotScreenS2CPacket::session,
            ByteBufCodecs.optional(HistoryMenuDetails.STREAM_CODEC),
            InitalizeSnapshotScreenS2CPacket::details,
            InitalizeSnapshotScreenS2CPacket::new
    );
}
