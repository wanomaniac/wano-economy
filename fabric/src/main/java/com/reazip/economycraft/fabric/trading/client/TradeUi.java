package com.reazip.economycraft.fabric.trading.client;

import com.reazip.economycraft.fabric.trading.server.TradeData;
import com.reazip.economycraft.fabric.trading.server.TradeMenu;
import com.reazip.economycraft.fabric.trading.server.TradeMenuSnapshot;
import com.reazip.economycraft.fabric.trading.server.TradeSession;
import com.reazip.economycraft.fabric.trading.types.TradeGuiData;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

public final class TradeUi {
    private TradeUi() {}

    public static void openSnapshot(ServerPlayer player, TradeData data) {
        player.openMenu(new ExtendedScreenHandlerFactory<TradeData>(){
            @Override
            public TradeData getScreenOpeningData(ServerPlayer player) {
                return data;
            }

            @Override
            public @NotNull Component getDisplayName() {
                return Component.literal("Snapshot of trade " + data.getId());
            }

            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                return  new TradeMenuSnapshot(syncId, data);
            }
        });
    }

    public static void open(ServerPlayer a, ServerPlayer b) {
        TradeSession session = new TradeSession(a, b);

        a.openMenu(new ExtendedScreenHandlerFactory<TradeGuiData>() {
            @Override
            public TradeGuiData getScreenOpeningData(ServerPlayer player) {
                return new TradeGuiData(a.getUUID(), b.getUUID(), true, 0L,0L);
            }

            @Override
            public @NotNull Component getDisplayName() {
                return Component.literal("Trading with " + b.getName().getString());
            }

            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                TradeMenu menu = new TradeMenu(syncId, inv, session, true);
                session.menuA = menu;
                return menu;
            }
        });

        b.openMenu(new ExtendedScreenHandlerFactory<TradeGuiData>() {
            @Override
            public TradeGuiData getScreenOpeningData(ServerPlayer player) {
                return new TradeGuiData(a.getUUID(), b.getUUID(), false, 0L, 0L);
            }

            @Override
            public @NotNull Component getDisplayName() {
                return Component.literal("Trading with " + a.getName().getString());
            }

            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                TradeMenu menu = new TradeMenu(syncId, inv, session, false);
                session.menuB = menu;
                return menu;
            }
        });

    }
}
