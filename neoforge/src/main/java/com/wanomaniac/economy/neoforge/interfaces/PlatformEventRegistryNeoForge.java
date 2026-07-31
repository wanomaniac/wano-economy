package com.wanomaniac.economy.neoforge.interfaces;
import com.wanomaniac.economy.interfaces.IPlatformEventRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.util.function.Consumer;

import static net.neoforged.neoforge.common.NeoForge.*;

public class PlatformEventRegistryNeoForge implements IPlatformEventRegistry {
    @Override
    public void whenPlayerJoins(Consumer<ServerPlayer> callback) {
        EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                callback.accept(serverPlayer);
            }
        });
    }

    @Override
    public void onServerStarted(Consumer<MinecraftServer> callback) {
        EVENT_BUS.addListener((ServerStartedEvent event) -> callback.accept(event.getServer()));
    }

    @Override
    public void onServerStarting(Consumer<MinecraftServer> callback) {
        EVENT_BUS.addListener((ServerStartingEvent event) -> callback.accept(event.getServer()));
    }

    @Override
    public void onServerStopping(Consumer<MinecraftServer> callback) {
        EVENT_BUS.addListener((ServerStoppingEvent event) -> callback.accept(event.getServer()));
    }

    @Override
    public void whenLivingEntityAfterDeath(DeathCallback callback) {
        EVENT_BUS.addListener((LivingDeathEvent event) -> callback.accept(event.getEntity(), event.getSource()));
    }

    @Override
    public void onCommandRegistrationCallback(CommandRegisterCallback callback) {
        EVENT_BUS.addListener((RegisterCommandsEvent event) -> callback.accept(
                event.getDispatcher(),
                event.getBuildContext(),
                event.getCommandSelection()
        ));
    }
}
