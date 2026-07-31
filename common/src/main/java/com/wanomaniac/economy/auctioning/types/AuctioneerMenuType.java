package com.wanomaniac.economy.auctioning.types;

import com.wanomaniac.economy.auctioning.server.AuctioneerMenu;
import com.wanomaniac.economy.trading.server.TradeData;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;

import net.minecraft.world.inventory.MenuType;

public class AuctioneerMenuType extends AuctioneerMenu {
    public final AuctionGuiData guiData;

    public AuctioneerMenuType(int syncId, Inventory playerInventory, AuctionGuiData guiData) {
        super(syncId, null, playerInventory);
        this.guiData = guiData;
    }
}
