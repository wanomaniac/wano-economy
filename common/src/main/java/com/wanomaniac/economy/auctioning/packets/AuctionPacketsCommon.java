package com.wanomaniac.economy.auctioning.packets;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.auctioning.packets.msgs.ConfirmBiddingSelectionS2CPacket;

public class AuctionPacketsCommon {
    public static void register(){
        // Server -> Client
        CommonEconomy.packets.registerS2CPayload(
                ConfirmBiddingSelectionS2CPacket.TYPE,
                ConfirmBiddingSelectionS2CPacket.CODEC
        );
    }
}
