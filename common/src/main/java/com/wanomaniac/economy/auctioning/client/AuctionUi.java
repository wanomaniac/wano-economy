package com.wanomaniac.economy.auctioning.client;

import com.wanomaniac.economy.ClientEconomy;
import com.wanomaniac.economy.auctioning.server.AuctionSession;
import com.wanomaniac.economy.auctioning.server.AuctioneerMenu;
import com.wanomaniac.economy.auctioning.types.AuctionGuiData;
import com.wanomaniac.economy.auctioning.types.AuctionMenuTypes;
import com.wanomaniac.economy.trading.server.TradeMenu;
import com.wanomaniac.economy.trading.types.TradeGuiData;
import com.wanomaniac.economy.trading.types.TradeMenuTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class AuctionUi {
    public static void openForAuctioneer(ServerPlayer auctioneer) {
       // to-do create auction manager
        AuctionSession session = new AuctionSession(auctioneer);
        ClientEconomy.menuFactory.openExtendedMenu(auctioneer, AuctionMenuTypes.AUCTIONEER_MENU, Component.literal(""), (menuType, syncID, inv, data) -> {
            return new AuctioneerMenu(menuType, session, syncID, inv);
        }, new AuctionGuiData(session.id, auctioneer.getUUID()));
    }
}
