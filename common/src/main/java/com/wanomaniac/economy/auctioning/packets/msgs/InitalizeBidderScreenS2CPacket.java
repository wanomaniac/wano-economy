package com.wanomaniac.economy.auctioning.packets.msgs;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.IdentifierUtils;
import com.wanomaniac.economy.ModIdentifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

// Client confirms the bidding and tells the server what exactly has been confirmed.
public record InitalizeBidderScreenS2CPacket() implements CustomPacketPayload {
    public static final Type<InitalizeBidderScreenS2CPacket> TYPE =
            new Type<>(IdentifierUtils.toNative(ModIdentifier.fromNamespaceAndPath(CommonEconomy.MOD_ID, "initalize_bidder_screen")));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, InitalizeBidderScreenS2CPacket> CODEC = StreamCodec.unit(
            new InitalizeBidderScreenS2CPacket()
    );
}
