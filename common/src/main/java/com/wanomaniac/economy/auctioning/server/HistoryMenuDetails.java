package com.wanomaniac.economy.auctioning.server;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record HistoryMenuDetails(int lastPage) {
    public static StreamCodec<RegistryFriendlyByteBuf, HistoryMenuDetails> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            HistoryMenuDetails::lastPage,
            HistoryMenuDetails::new
    );
}
