package com.wanomaniac.economy.interfaces;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

// Heard there was something called "bad packets" just to fix this ez, but avoid dependencies is now my motto.
public interface IPlatformPackets {
    /**
     * Register a payload structure and its codec (C2S or S2C).
     */
    <T extends CustomPacketPayload> void registerC2SPayload(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec
    );

    <T extends CustomPacketPayload> void registerS2CPayload(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec
    );

    default <T extends CustomPacketPayload> void registerGlobalPayload(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec
    ){
        registerC2SPayload(type, codec);
        registerS2CPayload(type, codec);
    }

    /**
     * Register a C2S receiver (runs on the logical server).
     */
    <T extends CustomPacketPayload> void registerServerReceiver(
            CustomPacketPayload.Type<T> type,
            TriConsumer<T, MinecraftServer, ServerPlayer> handler
    );

    /**
     * Register an S2C receiver (runs on the logical client).
     */
    <T extends CustomPacketPayload> void registerClientReceiver(
            CustomPacketPayload.Type<T> type,
            Consumer<T> handler
    );


    default void registerPayloads(){
        return;
    }

    /**
     * Send a packet to a specific player (Server -> Client).
     */
    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

    /**
     * Send a packet to the server (Client -> Server).
     */
    void sendToServer(CustomPacketPayload payload);
}
