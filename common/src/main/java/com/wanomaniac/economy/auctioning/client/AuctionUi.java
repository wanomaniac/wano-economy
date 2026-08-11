package com.wanomaniac.economy.auctioning.client;

import com.wanomaniac.economy.ClientEconomy;
import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.ServerEconomy;
import com.wanomaniac.economy.auctioning.packets.msgs.InitalizeBidderScreenS2CPacket;
import com.wanomaniac.economy.auctioning.packets.msgs.InitalizeSnapshotScreenS2CPacket;
import com.wanomaniac.economy.auctioning.server.AuctionHistoryMenu;
import com.wanomaniac.economy.auctioning.server.AuctionSession;
import com.wanomaniac.economy.auctioning.server.AuctioneerMenu;
import com.wanomaniac.economy.auctioning.server.HistoryMenuDetails;
import com.wanomaniac.economy.auctioning.types.AuctionGuiData;
import com.wanomaniac.economy.auctioning.types.AuctionMenuTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

import java.util.Optional;
import java.util.UUID;

public class AuctionUi {
    public static void openForAuctioneer(ServerPlayer auctioneer) {
        boolean doesExist = ServerEconomy.AUCTION_MANAGER.doesSessionExistForAuctionnerID(auctioneer.getUUID());
        AuctionSession session = ServerEconomy.AUCTION_MANAGER.createSession(auctioneer);
        ClientEconomy.menuFactory.openExtendedMenu(auctioneer, AuctionMenuTypes.AUCTIONEER_MENU, Component.literal(""), (menuType, syncID, inv, data) -> new AuctioneerMenu(menuType, session, syncID, inv), new AuctionGuiData(session.id, auctioneer.getUUID(), doesExist));
    }

    public static boolean joinAsBidder(ServerPlayer bidder, UUID playerID){
        AuctionSession session = ServerEconomy.AUCTION_MANAGER.findSessionByAuctioneerID(playerID);
        if(session == null) return false;

        CommonEconomy.packets.sendToPlayer(bidder, new InitalizeBidderScreenS2CPacket());
        return ServerEconomy.AUCTION_MANAGER.joinSession(bidder, session.id);
    }

    public static boolean joinAsBidder(ServerPlayer bidder){
        AuctionSession session = ServerEconomy.AUCTION_MANAGER.findSessionByBidderID(bidder.getUUID());
        if(session == null) return false;

        CommonEconomy.packets.sendToPlayer(bidder, new InitalizeBidderScreenS2CPacket());
        return ServerEconomy.AUCTION_MANAGER.joinSession(bidder, session.id);
    }

    // Snapshots will only need 1 packet from the server.
    public static boolean openSnapshot(ServerPlayer viewer, UUID id){
        AuctionSession session = ServerEconomy.AUCTION_MANAGER.findSnapshotByID(id);
        if(session == null) return false;

        CommonEconomy.packets.sendToPlayer(viewer, new InitalizeSnapshotScreenS2CPacket(session, Optional.empty()));
        return true;
    }

    public static boolean openSnapshot(ServerPlayer viewer, UUID id, HistoryMenuDetails details){
        AuctionSession session = ServerEconomy.AUCTION_MANAGER.findSnapshotByID(id);
        if(session == null) return false;

        CommonEconomy.packets.sendToPlayer(viewer, new InitalizeSnapshotScreenS2CPacket(session, Optional.of(details)));
        return true;
    }

    public static boolean openSnapshot(ServerPlayer viewer, AuctionSession session, HistoryMenuDetails details){
        if(session == null) return false;

        CommonEconomy.packets.sendToPlayer(viewer, new InitalizeSnapshotScreenS2CPacket(session, Optional.of(details)));
        return true;
    }


    public static void openHistoryMenu(ServerPlayer viewer, HistoryMenuDetails details) {
        viewer.openMenu(new SimpleMenuProvider((id, inv, p) -> new AuctionHistoryMenu(id, inv, details), Component.literal("Server Auction History")));
    }
}
