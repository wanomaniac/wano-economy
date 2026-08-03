package com.wanomaniac.economy.interfaces;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import java.util.function.Consumer;

// Global Minecraft event registrations that are NEEDED!
public interface IPlatformEventRegistry {
    void whenPlayerJoins(Consumer<ServerPlayer> callback);
    void whenServerTicks(Consumer<MinecraftServer> callback);
    void onServerStarted(Consumer<MinecraftServer> callback);
    void onServerStarting(Consumer<MinecraftServer> callback);
    void onServerStopping(Consumer<MinecraftServer> callback);

    void whenLivingEntityAfterDeath(DeathCallback callback);
    void onCommandRegistrationCallback(CommandRegisterCallback callback);
    @FunctionalInterface
    interface DeathCallback {
        void accept(LivingEntity entity, DamageSource source);
    }

    @FunctionalInterface
    interface CommandRegisterCallback {
        void accept(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, Commands.CommandSelection selection);
    }
}
