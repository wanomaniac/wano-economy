package com.wanomaniac.economy.auctioning.packets;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.ServerEconomy;
import com.wanomaniac.economy.auctioning.client.AuctioneerMenuScreen;
import com.wanomaniac.economy.auctioning.client.BidderScreen;
import com.wanomaniac.economy.auctioning.packets.msgs.*;
import com.wanomaniac.economy.auctioning.server.AuctionSession;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class AuctionPacketsClient {
    public static void register() {
        AuctionPacketsCommon.register();
        // Server -> Client
        CommonEconomy.packets.registerClientReceiver(
                AuctionSendPlayerBalanceS2CPacket.TYPE,
                (payload) -> {
                    if (Minecraft.getInstance().screen instanceof BidderScreen screen) {
                        screen.syncMoney(payload.balance());
                    }
                }
        );

        CommonEconomy.packets.registerClientReceiver(
                ConfirmBiddingSelectionS2CPacket.TYPE,
                (payload) -> Minecraft.getInstance().execute(() -> {
                    if (Minecraft.getInstance().screen instanceof AuctioneerMenuScreen screen) {
                        screen.ConfirmItem(payload.item());
                    }
                })
        );

        CommonEconomy.packets.registerClientReceiver(
                BidderJoinS2CPacket.TYPE,
                (payload) -> Minecraft.getInstance().execute(() -> {
                    if (Minecraft.getInstance().screen instanceof AuctioneerMenuScreen screen) {
                        screen.BidderJoin(payload.playerID());
                    } else if(Minecraft.getInstance().screen instanceof BidderScreen screen){
                        screen.BidderJoin(payload.playerID(), payload.auctionData());
                    }
                })
        );

        CommonEconomy.packets.registerClientReceiver(
                SynchronizeItemBiddingS2CPacket.TYPE,
                (payload) -> Minecraft.getInstance().execute(() -> {
                    if (Minecraft.getInstance().screen instanceof AuctioneerMenuScreen screen) {
                        screen.setActiveBidding(payload.bidding().isPresent() ? payload.bidding().get() : null);
                    } else if(Minecraft.getInstance().screen instanceof BidderScreen screen){
                        screen.setActiveBidding(payload.bidding().isPresent() ? payload.bidding().get() : null);
                    }
                })
        );

        CommonEconomy.packets.registerClientReceiver(
                InitalizeBidderScreenS2CPacket.TYPE,
                (payload) -> Minecraft.getInstance().execute(() -> {
                    Minecraft.getInstance().setScreen(new BidderScreen(Component.literal("")));
                })
        );

        CommonEconomy.packets.registerClientReceiver(
                CancelAuctionS2CPacket.TYPE,
                (payload) -> Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(null))
        );

        CommonEconomy.packets.registerClientReceiver(
                BidderLeavePacket.TYPE,
                (payload) -> {
                    if (Minecraft.getInstance().screen instanceof AuctioneerMenuScreen screen) {
                        screen.BidderLeave(payload.id());
                    } else if(Minecraft.getInstance().screen instanceof BidderScreen screen){
                        screen.BidderLeave(payload.id());
                    }
                }
        );
    }
}
