package com.wanomaniac.economy.auctioning.types;

import com.wanomaniac.economy.ClientEconomy;
import com.wanomaniac.economy.auctioning.server.AuctionHistoryMenu;
import com.wanomaniac.economy.auctioning.server.HistoryMenuDetails;
import com.wanomaniac.economy.trading.server.TradeData;
import com.wanomaniac.economy.trading.types.TradeGuiData;
import com.wanomaniac.economy.trading.types.TradeInventoryType;
import com.wanomaniac.economy.trading.types.TradeSnapshotType;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;

public class AuctionMenuTypes {
    public static final Supplier<MenuType<AuctioneerMenuType>> AUCTIONEER_MENU = ClientEconomy.menuFactory.createExtended(
                    "auctioneer_menu",
                    (menuType, syncId, inv, buf) -> new AuctioneerMenuType(syncId, inv, buf),
                    AuctionGuiData.STREAM_CODEC);

    public static void init() { // clinit
    }
}
