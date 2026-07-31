package com.wanomaniac.economy.auctioning.packets.msgs;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.IdentifierUtils;
import com.wanomaniac.economy.ModIdentifier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

// Server menu detects a click on item, tells the client screen the details and leaves it to handle it.
public record ConfirmBiddingSelectionS2CPacket(ItemStack item) implements CustomPacketPayload {
    public static final Type<ConfirmBiddingSelectionS2CPacket> TYPE =
            new Type<>(IdentifierUtils.toNative(ModIdentifier.fromNamespaceAndPath(CommonEconomy.MOD_ID,"confirm_bidding_selection")));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Codec for encoding/decoding
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfirmBiddingSelectionS2CPacket> CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, // Codec for serializing/deserializing the ItemStack (handles empty slots safely)
            ConfirmBiddingSelectionS2CPacket::item, // Getter method reference
            ConfirmBiddingSelectionS2CPacket::new   // Constructor reference
    );
}
