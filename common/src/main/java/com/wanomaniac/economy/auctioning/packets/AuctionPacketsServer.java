package com.wanomaniac.economy.auctioning.packets;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.EconomyManager;
import com.wanomaniac.economy.ServerEconomy;
import com.wanomaniac.economy.auctioning.packets.msgs.*;
import com.wanomaniac.economy.auctioning.server.AuctionSession;
import com.wanomaniac.economy.auctioning.server.AuctioneerMenu;
import com.wanomaniac.economy.auctioning.server.ItemBidding;
import com.wanomaniac.economy.trading.packets.msgs.TradeSendPlayerBalanceS2CPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.Optional;


public class AuctionPacketsServer {
    public static void register() {
        AuctionPacketsCommon.register();
        CommonEconomy.packets.registerServerReceiver(
                AuctionRequestPlayerBalanceC2SPacket.TYPE,
                (payload, server, player) -> CommonEconomy.packets.sendToPlayer(player, new AuctionSendPlayerBalanceS2CPacket(
                        CommonEconomy.getManager(player.level().getServer()).getBalance(player.getUUID(), false)
                ))
        );

        CommonEconomy.packets.registerServerReceiver(
                StartBiddingItemC2SPacket.TYPE,
                (payload, server, player) -> {
                    AuctionSession session = ServerEconomy.AUCTION_MANAGER.findSessionByID(payload.data().auctionId());
                    if(session == null || session.auctioneer == null) return;

                    if (player.containerMenu instanceof AuctioneerMenu menu) {
                        int slotIndex = payload.slotIndex();
                        if (slotIndex < 0 || slotIndex >= menu.slots.size()) {
                            return; // Out of bounds slot
                        }

                        Slot slot = menu.getSlot(slotIndex);
                        if (!slot.mayPickup(player)) {
                            return; // Player cannot legally take items from this slot
                        }

                        ItemStack biddingOriginal = slot.getItem();
                        if (biddingOriginal.isEmpty()) {
                            return;
                        }
                        ItemStack bidding = biddingOriginal.copy();
                        slot.set(ItemStack.EMPTY);
                        menu.broadcastChanges();

                        ItemBidding newBiding = session.startBidding(bidding, payload.startingPrice());
                        CommonEconomy.packets.sendToPlayer(player, new SynchronizeItemBiddingS2CPacket(Optional.ofNullable(newBiding)));
                        session.bidders.forEach((playerID) -> {
                            ServerPlayer playerBidder = server.getPlayerList().getPlayer(playerID);
                            if(playerBidder == null) return;
                            CommonEconomy.packets.sendToPlayer(playerBidder, new SynchronizeItemBiddingS2CPacket(Optional.ofNullable(newBiding)));
                        });

                    }
                }
        );

        CommonEconomy.packets.registerServerReceiver(
                BidderLeavePacket.TYPE,
                (payload, server, player) -> {
                    AuctionSession session = ServerEconomy.AUCTION_MANAGER.findSessionByID(payload.id());
                    if(session == null) return;

                    session.bidderLeave(player);
                    CommonEconomy.packets.sendToPlayer(session.auctioneer, new BidderLeavePacket(player.getUUID()));
                    session.bidders.forEach((playerID) -> {
                        ServerPlayer playerBidder = server.getPlayerList().getPlayer(playerID);
                        if(playerBidder == null) return;
                        CommonEconomy.packets.sendToPlayer(playerBidder, new BidderLeavePacket(player.getUUID()));
                    });
                }
        );

        CommonEconomy.packets.registerServerReceiver(
                BidMoneyC2SPacket.TYPE,
                (payload, server, player) -> {
                    AuctionSession session = ServerEconomy.AUCTION_MANAGER.findSessionByID(payload.id());
                    if(session == null) return;

                    // Check if the money value is valid, otherwise circumvent it.
                    EconomyManager manager = CommonEconomy.getManager(server);
                    Long balance = manager.getBalance(player.getUUID(), false);

                    if(payload.money() <= 0) return;

                    long finalMoneyValue;
                    if (payload.money() > balance) {
                        finalMoneyValue = balance;
                    } else {
                        finalMoneyValue = payload.money();
                    }

                    if(session.getCurrentBidding().placeBid(player.getUUID(), finalMoneyValue)){
                        CommonEconomy.packets.sendToPlayer(session.auctioneer, new SynchronizeItemBiddingS2CPacket(Optional.ofNullable(session.getCurrentBidding())));
                        session.bidders.forEach((playerID) -> {
                            ServerPlayer playerBidder = server.getPlayerList().getPlayer(playerID);
                            if(playerBidder == null) return;
                            CommonEconomy.packets.sendToPlayer(playerBidder, new SynchronizeItemBiddingS2CPacket(Optional.ofNullable(session.getCurrentBidding())));
                        });
                    }

                }
        );
    }
}
