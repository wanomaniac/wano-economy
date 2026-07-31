package com.wanomaniac.economy.registry;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.IdentifierUtils;
import com.wanomaniac.economy.ModIdentifier;
import com.wanomaniac.economy.auctioning.types.AuctionMenuTypes;
import com.wanomaniac.economy.trading.types.TradeMenuTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class Menu {
    public static void register(){
        Registry.register(BuiltInRegistries.MENU, IdentifierUtils.toNative(ModIdentifier.fromNamespaceAndPath(CommonEconomy.MOD_ID, "trade_menu")), TradeMenuTypes.TRADE_MENU.get());
        Registry.register(BuiltInRegistries.MENU, IdentifierUtils.toNative(ModIdentifier.fromNamespaceAndPath(CommonEconomy.MOD_ID, "trade_menu_snapshot")), TradeMenuTypes.TRADE_SNAPSHOT_MENU.get());

        // AUCTION
        Registry.register(BuiltInRegistries.MENU, IdentifierUtils.toNative(ModIdentifier.fromNamespaceAndPath(CommonEconomy.MOD_ID, "auctioneer_menu")), AuctionMenuTypes.AUCTIONEER_MENU.get());
    }
}
