package com.wanomaniac.economy.trading.types;

import com.wanomaniac.economy.trading.server.TradeData;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;

public class TradeSnapshotType extends ChestMenu {
    public static final int ROWS = 7;
    public final TradeData guiData;

    public TradeSnapshotType(int syncId, Inventory playerInventory, Container container, TradeData guiData) {
        super(MenuType.GENERIC_9x6, syncId, playerInventory, container, ROWS); // pass 7 as rows
        this.guiData = guiData;
    }
}
