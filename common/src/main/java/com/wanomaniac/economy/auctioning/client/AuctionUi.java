package com.wanomaniac.economy.auctioning.client;

import com.wanomaniac.economy.ClientEconomy;
import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.ServerEconomy;
import com.wanomaniac.economy.auctioning.packets.msgs.InitalizeBidderScreenS2CPacket;
import com.wanomaniac.economy.auctioning.server.AuctionSession;
import com.wanomaniac.economy.auctioning.server.AuctioneerMenu;
import com.wanomaniac.economy.auctioning.types.AuctionGuiData;
import com.wanomaniac.economy.auctioning.types.AuctionMenuTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class AuctionUi {
    public static void openForAuctioneer(ServerPlayer auctioneer) {
       // to-do create auction manager
        AuctionSession session = ServerEconomy.AUCTION_MANAGER.createSession(auctioneer);
        ClientEconomy.menuFactory.openExtendedMenu(auctioneer, AuctionMenuTypes.AUCTIONEER_MENU, Component.literal(""), (menuType, syncID, inv, data) -> {
            return new AuctioneerMenu(menuType, session, syncID, inv);
        }, new AuctionGuiData(session.id, auctioneer.getUUID()));
    }

    public static boolean joinAsBidder(ServerPlayer bidder, UUID playerID){
        AuctionSession session = ServerEconomy.AUCTION_MANAGER.findSessionByAuctioneerID(playerID);
        if(session == null) return false;

        CommonEconomy.packets.sendToPlayer(bidder, new InitalizeBidderScreenS2CPacket());
        return ServerEconomy.AUCTION_MANAGER.joinSession(bidder, session.id);
    }
}
