package com.wanomaniac.economy.neoforge.interfaces;
import com.wanomaniac.economy.interfaces.IPlatformPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.logging.log4j.util.TriConsumer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static com.wanomaniac.economy.neoforge.EconomyNeoForge.registrar;



public class PlatformPacketsNeoForge implements IPlatformPackets {
    // We'll need to put the handlers when the register is playing to the server in the moment.
    // So it needs to be stored somewhere!!!
    @SuppressWarnings("rawtypes")
    private final Map<CustomPacketPayload.Type<?>, TriConsumer> serverHandlers = new ConcurrentHashMap<>();
    @SuppressWarnings("rawtypes")
    private final Map<CustomPacketPayload.Type<?>, Consumer> clientHandlers = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T extends CustomPacketPayload> void registerC2SPayload(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec
    ) {
        registrar.playToServer(type, codec, (payload, context) -> {
            TriConsumer<T, MinecraftServer, ServerPlayer> handler = serverHandlers.get(type);
            if (handler != null && context.player() instanceof ServerPlayer serverPlayer) {
                serverPlayer.level().getServer().execute(() ->
                        handler.accept(payload, serverPlayer.level().getServer(), serverPlayer)
                );
            }
        });
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T extends CustomPacketPayload> void registerS2CPayload(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec
    ) {
        registrar.playToClient(type, codec, (payload, context) -> {
            Consumer<T> handler = clientHandlers.get(type);
            if (handler != null) {
                Minecraft.getInstance().execute(() -> handler.accept(payload));
            }
        });
    }

    @Override
    public <T extends CustomPacketPayload> void registerServerReceiver(
            CustomPacketPayload.Type<T> type,
            TriConsumer<T, MinecraftServer, ServerPlayer> handler
    ) {
        serverHandlers.put(type, handler);
    }

    @Override
    public <T extends CustomPacketPayload> void registerClientReceiver(
            CustomPacketPayload.Type<T> type,
            Consumer<T> handler
    ) {
        clientHandlers.put(type, handler);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        PlatformPacketsNeoForgeUtil.sendToServer(payload);
    }
}
