package com.wanomaniac.economy.auctioning.packets;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.auctioning.client.AuctioneerMenuScreen;
import com.wanomaniac.economy.auctioning.packets.msgs.ConfirmBiddingSelectionS2CPacket;
import net.minecraft.client.Minecraft;

public class AuctionPacketsClient {
    public static void register() {
        AuctionPacketsCommon.register();
        // Server -> Client
        CommonEconomy.packets.registerClientReceiver(
                ConfirmBiddingSelectionS2CPacket.TYPE,
                (payload) -> Minecraft.getInstance().execute(() -> {
                    if (Minecraft.getInstance().screen instanceof AuctioneerMenuScreen screen) {
                        screen.ConfirmItem(payload.item());
                    }
                })
        );
    }
}
