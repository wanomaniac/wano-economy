package com.wanomaniac.economy.auctioning.packets;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.EconomyManager;
import com.wanomaniac.economy.ServerEconomy;
import com.wanomaniac.economy.auctioning.AuctionUtil;
import com.wanomaniac.economy.auctioning.client.AuctionUi;
import com.wanomaniac.economy.auctioning.packets.msgs.*;
import com.wanomaniac.economy.auctioning.server.AuctionSession;
import com.wanomaniac.economy.auctioning.server.AuctioneerMenu;
import com.wanomaniac.economy.auctioning.server.ItemBidding;
import com.wanomaniac.economy.auctioning.server.NotificationRecord;
import com.wanomaniac.economy.trading.packets.msgs.TradeSendPlayerBalanceS2CPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;


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
                SynchronizeAuctioneerAuctionC2SPacket.TYPE,
                (payload, server, player) -> ServerEconomy.AUCTION_MANAGER.synchronizeSessionForAuctioneer(player)
        );
        CommonEconomy.packets.registerServerReceiver(
                OpenHistoryMenuC2SPacket.TYPE,
                (payload, server, player) -> AuctionUi.openHistoryMenu(player, payload.details())
        );
        CommonEconomy.packets.registerServerReceiver(
                StartBiddingItemC2SPacket.TYPE,
                (payload, server, player) -> {
                    AuctionSession session = ServerEconomy.AUCTION_MANAGER.findSessionByID(payload.data().auctionId());
                    if(session == null || session.auctioneer == null) return;

                    if (player.containerMenu instanceof AuctioneerMenu menu) {
                        int slotIndex = payload.slotIndex();
                        if(slotIndex == -1 && session.getCurrentBidding() != null){ // cancel the bidding.
                            session.cancelBidding(server);
                            CommonEconomy.packets.sendToPlayer(player, new SynchronizeItemBiddingS2CPacket(Optional.empty()));
                            session.bidders.forEach((playerID) -> {
                                ServerPlayer playerBidder = server.getPlayerList().getPlayer(playerID);
                                if(playerBidder == null) return;
                                CommonEconomy.packets.sendToPlayer(playerBidder, new SynchronizeItemBiddingS2CPacket(Optional.empty()));
                            });
                        }

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
                        session.inactiveBidderNotifications.clear();
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
                        for (UUID inactivePlayer : session.biddersInactive) {
                            ServerPlayer playerInactive = server.getPlayerList().getPlayer(inactivePlayer);
                            if (playerInactive == null) continue;

                            if (!session.hasInactivePlayerBeenNotified(inactivePlayer, 3)) {
                                Component message = Component.literal("[AUCTION] A bidding for ") // todo: translatable
                                        .withStyle(ChatFormatting.GRAY)
                                        .append(AuctionUtil.getItemComponent(bidding))
                                        .append(Component.literal(" has started").withStyle(ChatFormatting.GRAY));

                                playerInactive.sendSystemMessage(message);
                                session.inactiveBidderNotifications.add(new NotificationRecord(inactivePlayer, 3));
                            }
                        }

                    }
                }
        );

        CommonEconomy.packets.registerServerReceiver(
                BidderLeavePacket.TYPE,
                (payload, server, player) -> {
                    AuctionSession session = ServerEconomy.AUCTION_MANAGER.findSessionByID(payload.id());
                    if(session == null) return;
                    if(session.didBiddingExpire()){
                        session.inactiveBidderNotifications.add(new NotificationRecord(player.getUUID(), 2));
                    }

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
                    if(session == null || session.getCurrentBidding() == null) return;

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

                    if (session.cancelled || finalMoneyValue <= session.getCurrentBidding().currentBid() || !session.getCurrentBidding().isActive()) return;
                    if(session.getCurrentBidding().highestBidder() != null){
                        manager.removeMoney(session.auctioneerID, session.getCurrentBidding().currentBid());
                        manager.addMoney(session.getCurrentBidding().highestBidder(), session.getCurrentBidding().currentBid());
                    }
                    if(session.getCurrentBidding().placeBid(player.getUUID(), finalMoneyValue)){
                        manager.addMoney(session.auctioneerID, finalMoneyValue);
                        manager.removeMoney(session.getCurrentBidding().highestBidder(), finalMoneyValue);
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
