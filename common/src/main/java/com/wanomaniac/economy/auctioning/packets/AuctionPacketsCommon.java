package com.wanomaniac.economy.auctioning.packets;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.auctioning.packets.msgs.*;

public class AuctionPacketsCommon {
    public static void register(){
        // Global (Bi-Directional)
        CommonEconomy.packets.registerGlobalPayload(
                BidderLeavePacket.TYPE,
                BidderLeavePacket.CODEC
        );
        // Client -> Server
        CommonEconomy.packets.registerC2SPayload(
                StartBiddingItemC2SPacket.TYPE,
                StartBiddingItemC2SPacket.CODEC
        );
        CommonEconomy.packets.registerC2SPayload(
                AuctionRequestPlayerBalanceC2SPacket.TYPE,
                AuctionRequestPlayerBalanceC2SPacket.CODEC
        );
        CommonEconomy.packets.registerC2SPayload(
                BidMoneyC2SPacket.TYPE,
                BidMoneyC2SPacket.CODEC
        );
        // Server -> Client
        CommonEconomy.packets.registerS2CPayload(
                ConfirmBiddingSelectionS2CPacket.TYPE,
                ConfirmBiddingSelectionS2CPacket.CODEC
        );
        CommonEconomy.packets.registerS2CPayload(
                InitalizeBidderScreenS2CPacket.TYPE,
                InitalizeBidderScreenS2CPacket.CODEC
        );
        CommonEconomy.packets.registerS2CPayload(
                BidderJoinS2CPacket.TYPE,
                BidderJoinS2CPacket.CODEC
        );
        CommonEconomy.packets.registerS2CPayload(
                CancelAuctionS2CPacket.TYPE,
                CancelAuctionS2CPacket.CODEC
        );
        CommonEconomy.packets.registerS2CPayload(
                SynchronizeItemBiddingS2CPacket.TYPE,
                SynchronizeItemBiddingS2CPacket.CODEC
        );
        CommonEconomy.packets.registerS2CPayload(
                AuctionSendPlayerBalanceS2CPacket.TYPE,
                AuctionSendPlayerBalanceS2CPacket.CODEC
        );
    }
}
