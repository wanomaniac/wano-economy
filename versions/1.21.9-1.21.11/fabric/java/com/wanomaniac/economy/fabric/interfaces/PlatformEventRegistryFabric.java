package com.wanomaniac.economy.fabric.interfaces;

import com.wanomaniac.economy.interfaces.IPlatformEventRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

public class PlatformEventRegistryFabric implements IPlatformEventRegistry {
    @Override
    public void whenPlayerJoins(Consumer<ServerPlayer> callback) {
        ServerPlayerEvents.JOIN.register(callback::accept);
    }

    @Override
    public void onServerStarted(Consumer<MinecraftServer> callback) {
        ServerLifecycleEvents.SERVER_STARTED.register(callback::accept);
    }

    @Override
    public void onServerStarting(Consumer<MinecraftServer> callback) {
        ServerLifecycleEvents.SERVER_STARTING.register(callback::accept);
    }

    @Override
    public void onServerStopping(Consumer<MinecraftServer> callback) {
        ServerLifecycleEvents.SERVER_STOPPING.register(callback::accept);
    }

    @Override
    public void whenLivingEntityAfterDeath(DeathCallback callback) {
        ServerLivingEntityEvents.AFTER_DEATH.register(callback::accept);
    }

    @Override
    public void onCommandRegistrationCallback(CommandRegisterCallback callback) {
        CommandRegistrationCallback.EVENT.register(callback::accept);
    }
}
