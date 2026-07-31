package com.wanomaniac.economy.interfaces;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.Supplier;

// NeoForge and Fabric and whatever junk use completely different menutype creation.
public interface IPlatformMenuFactory {
    <T extends AbstractContainerMenu, D> Supplier<MenuType<T>> createExtended(
            String id,
            ExtendedMenuFactory<T, D> factory,
            StreamCodec<? super RegistryFriendlyByteBuf, D> codec
    );

    @FunctionalInterface
    public interface ScreenFactory<M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> {
        U create(M menu, Inventory inventory, Component title);
    }
    <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void registerScreen(MenuType<M> type, ScreenFactory<M, U> factory);

    @FunctionalInterface
    public interface ExtendedMenuFactory<T extends AbstractContainerMenu, D> {
        T create(MenuType<?> menuType, int syncId, Inventory inventory, D data);
        default T create(int syncId, Inventory inventory, D data) {
            return create(null, syncId, inventory, data);
        }
    }

    <M extends AbstractContainerMenu, T extends AbstractContainerMenu, D> void openExtendedMenu(
            ServerPlayer player,
            Supplier<MenuType<T>> menuType,
            Component title,
            ExtendedMenuFactory<M, D> factory,
            D data
    );

}
