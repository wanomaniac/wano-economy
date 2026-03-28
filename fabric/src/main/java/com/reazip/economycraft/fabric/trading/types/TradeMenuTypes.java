package com.reazip.economycraft.fabric.trading.types;

import com.reazip.economycraft.fabric.trading.server.TradeData;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;

public class TradeMenuTypes {
    public static final MenuType<TradeInventoryType> TRADE_MENU =
            new ExtendedScreenHandlerType<>(
                    (syncId, inv, buf) -> new TradeInventoryType(syncId, inv, new SimpleContainer(9 * 7), buf), TradeGuiData.PACKET_CODEC
            );

    public static final MenuType<TradeSnapshotType> TRADE_SNAPSHOT_MENU =
            new ExtendedScreenHandlerType<>(
                    (syncId, inv, buf) -> new TradeSnapshotType(syncId, inv, new SimpleContainer(9 * 7), buf), TradeData.PACKET_CODEC
            );
}
