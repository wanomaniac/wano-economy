package com.wanomaniac.economy.trading.types;

import com.wanomaniac.economy.ClientEconomy;
import com.wanomaniac.economy.interfaces.IPlatformMenuFactory;
import com.wanomaniac.economy.services.ServiceKey;
import com.wanomaniac.economy.services.ServicesManager;
import com.wanomaniac.economy.trading.server.TradeData;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;

public class TradeMenuTypes {
    public static final Supplier<MenuType<TradeInventoryType>> TRADE_MENU = ClientEconomy.menuFactory.createExtended(
                    "trade_menu",
                    (menuType, syncId, inv, buf) -> new TradeInventoryType(syncId, inv, new SimpleContainer(9 * 7), buf),
                    TradeGuiData.PACKET_CODEC);

    public static final Supplier<MenuType<TradeSnapshotType>> TRADE_SNAPSHOT_MENU =
            ClientEconomy.menuFactory.createExtended(
                    "trade_menu_snapshot",
                    (menuType,syncId, inv, buf) -> new TradeSnapshotType(syncId, inv, new SimpleContainer(9 * 7), buf),
                    TradeData.PACKET_CODEC
            );

    public static void init() { // clinit
    }
}
