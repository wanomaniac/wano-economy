package com.wanomaniac.economy.auctioning.packets.msgs;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.IdentifierUtils;
import com.wanomaniac.economy.ModIdentifier;
import com.wanomaniac.economy.auctioning.server.AuctionSession;
import com.wanomaniac.economy.auctioning.server.HistoryMenuDetails;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

// Sends a snapshot directly to client as it is.
public record OpenHistoryMenuC2SPacket(HistoryMenuDetails details) implements CustomPacketPayload {
    public static final Type<OpenHistoryMenuC2SPacket> TYPE =
            new Type<>(IdentifierUtils.toNative(ModIdentifier.fromNamespaceAndPath(CommonEconomy.MOD_ID, "open_auction_history_menu")));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenHistoryMenuC2SPacket> CODEC = StreamCodec.composite(
            HistoryMenuDetails.STREAM_CODEC,
            OpenHistoryMenuC2SPacket::details,
            OpenHistoryMenuC2SPacket::new
    );
}
