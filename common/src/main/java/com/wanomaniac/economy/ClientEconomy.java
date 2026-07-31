package com.wanomaniac.economy;
import com.wanomaniac.economy.auctioning.client.AuctioneerMenuScreen;
import com.wanomaniac.economy.auctioning.packets.AuctionPacketsClient;
import com.wanomaniac.economy.auctioning.types.AuctionMenuTypes;
import com.wanomaniac.economy.interfaces.IPlatformMenuFactory;
import com.wanomaniac.economy.services.ServiceKey;
import com.wanomaniac.economy.services.ServicesManager;
import com.wanomaniac.economy.trading.client.TradeMenuClientScreen;
import com.wanomaniac.economy.trading.client.TradeMenuSnapshotClientScreen;
import com.wanomaniac.economy.trading.packets.TradePacketsClient;
import com.wanomaniac.economy.trading.types.TradeMenuTypes;

import java.util.Objects;

public class ClientEconomy {
    public static final IPlatformMenuFactory menuFactory = ServicesManager.get(ServiceKey.of(IPlatformMenuFactory.class));

    public static void initalize(){
        // neoforge registers network commands AFTER menu registration, if we call initalize BEFORE network commands, it will crash. hard coded 4 now.
        if(Objects.equals(CommonEconomy.platform.getPlatformName(), "fabric")){
            TradePacketsClient.register();
            AuctionPacketsClient.register();
        }

        menuFactory.registerScreen(TradeMenuTypes.TRADE_SNAPSHOT_MENU.get(), TradeMenuSnapshotClientScreen::new);
        menuFactory.registerScreen(TradeMenuTypes.TRADE_MENU.get(), TradeMenuClientScreen::new);
        menuFactory.registerScreen(AuctionMenuTypes.AUCTIONEER_MENU.get(), AuctioneerMenuScreen::new);
    }
}
