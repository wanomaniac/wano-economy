package com.wanomaniac.economy.auctioning.types;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.UUID;

public record AuctionGuiData(
        UUID auctionId,
        UUID auctioneer,
        boolean isNew
) {
    public static final StreamCodec<RegistryFriendlyByteBuf, AuctionGuiData> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, AuctionGuiData::auctionId,
            UUIDUtil.STREAM_CODEC, AuctionGuiData::auctioneer,
            ByteBufCodecs.BOOL, AuctionGuiData::isNew,
            AuctionGuiData::new
    );
}