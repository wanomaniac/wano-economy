package com.wanomaniac.economy.fabric.interfaces;

import com.wanomaniac.economy.interfaces.IPlatformMenuFactory;
import com.wanomaniac.economy.trading.client.TradeMenuSnapshotClientScreen;
import com.wanomaniac.economy.trading.types.TradeMenuTypes;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.Supplier;

public class PlatformMenuFactoryFabric implements IPlatformMenuFactory {
    @Override
    public <T extends AbstractContainerMenu, D> Supplier<MenuType<T>> createExtended(String id, ExtendedMenuFactory<T, D> factory, StreamCodec<? super RegistryFriendlyByteBuf, D> codec) {
        MenuType<T> type = new ExtendedScreenHandlerType<>(factory::create, codec);
        return () -> type;
    }

    @Override
    public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void registerScreen(MenuType<M> type, ScreenFactory<M, U> factory) {
        MenuScreens.register(type, factory::create);
    }

    @Override
    public <M extends AbstractContainerMenu, T extends AbstractContainerMenu, D> void openExtendedMenu(ServerPlayer player, Supplier<MenuType<T>> menuType, Component title, ExtendedMenuFactory<M, D> factory, D data) {
        player.openMenu(new ExtendedScreenHandlerFactory<D>() {
            @Override
            public D getScreenOpeningData(ServerPlayer p) {
                return data;
            }

            @Override
            public Component getDisplayName() {
                return title;
            }

            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player p) {
                return factory.create(menuType.get(), syncId, inv, data);
            }
        });
    }

}
