package com.wanomaniac.economy.auctioning;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.EconomyManager;
import com.wanomaniac.economy.auctioning.packets.msgs.BidderJoinS2CPacket;
import com.wanomaniac.economy.auctioning.packets.msgs.BidderLeavePacket;
import com.wanomaniac.economy.auctioning.packets.msgs.CancelAuctionS2CPacket;
import com.wanomaniac.economy.auctioning.packets.msgs.SynchronizeItemBiddingS2CPacket;
import com.wanomaniac.economy.auctioning.server.AuctionSession;
import com.wanomaniac.economy.auctioning.server.NotificationRecord;
import com.wanomaniac.economy.auctioning.types.AuctionGuiData;
import com.wanomaniac.economy.trading.NbtUtil;
import com.wanomaniac.economy.trading.server.TradeSession;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuctionManager {
    public final List<AuctionSession> sessions = new ArrayList<>();
    public final List<AuctionSession> snapshots = new CopyOnWriteArrayList<>();
    public final MinecraftServer server;

    public AuctionManager(MinecraftServer server) {
        this.server = server;
        loadHistory();
    }

    public AuctionSession createSession(ServerPlayer auctioneer){
        AuctionSession session = new AuctionSession(auctioneer);
        sessions.add(session);
        return session;
    }

    public AuctionSession findSessionByID(UUID id){
        Optional<AuctionSession> sessionObj = sessions.stream()
                .filter(auctionSession -> auctionSession.id.equals(id))
                .findFirst();
        return sessionObj.orElse(null);
    }

    public AuctionSession findSessionByAuctioneerID(UUID id){
        Optional<AuctionSession> sessionObj = sessions.stream()
                .filter(auctionSession -> auctionSession.auctioneer != null && auctionSession.auctioneer.getUUID().equals(id))
                .findFirst();
        return sessionObj.orElse(null);
    }

    private void setBiddingItemInInventories(AuctionSession session){
        ItemStack item = session.getCurrentBidding().item().copy();
        if(session.getCurrentBidding().highestBidder() != null) {
            ServerPlayer player = server.getPlayerList().getPlayer(session.getCurrentBidding().highestBidder());
            if (player == null) return;
            player.getInventory().placeItemBackInInventory(item);

            EconomyManager manager = CommonEconomy.getManager(server);
            manager.addMoney(session.auctioneer.getUUID(), session.getCurrentBidding().currentBid());
            manager.removeMoney(session.getCurrentBidding().highestBidder(), session.getCurrentBidding().currentBid());

            TradeSession.playCompleteSound(session.auctioneer);
            TradeSession.playCompleteSound(player);
        } else {
            session.auctioneer.getInventory().placeItemBackInInventory(item);
        }
    }

    public void tickAllSessions() {
        for (AuctionSession session : sessions) {
            if(session.getCurrentBidding() == null) continue;

            boolean wasActive = session.getCurrentBidding().isActive();
            session.getCurrentBidding().tick();
            if(wasActive != session.getCurrentBidding().isActive()) {
                session.sendPayloadToSessionMembers(server, new SynchronizeItemBiddingS2CPacket(Optional.empty()));
                setBiddingItemInInventories(session);
            }

            for (UUID inactivePlayer : session.bidders){
                ServerPlayer player = server.getPlayerList().getPlayer(inactivePlayer);
                if(player == null){
                    session.biddersInactive.add(inactivePlayer);
                    session.sendPayloadToSessionMembers(server, new BidderLeavePacket(inactivePlayer));
                    continue;
                }

                if(player.hasDisconnected()) session.biddersInactive.add(inactivePlayer);
            }

            for (UUID inactivePlayer : session.biddersInactive){
                ServerPlayer player = server.getPlayerList().getPlayer(inactivePlayer);
                if(player == null) continue;


                if((int)session.getCurrentBidding().getRemainingSeconds() == 20 && !session.hasInactivePlayerBeenNotified(inactivePlayer, 0)) {
                    player.sendSystemMessage(Component.literal("[AUCTION] 20 SECONDS! Don't Dawdle!").withColor(0xFFFFFF55));
                    session.inactiveBidderNotifications.add(new NotificationRecord(inactivePlayer, 0));
                }

                if((int)session.getCurrentBidding().getRemainingSeconds() == 5 && !session.hasInactivePlayerBeenNotified(inactivePlayer, 1)) {
                    player.sendSystemMessage(Component.literal("[AUCTION] 5 SECONDS!").withColor(0xFFFF5555));
                    session.inactiveBidderNotifications.add(new NotificationRecord(inactivePlayer, 1));
                }

                if(session.getCurrentBidding().isExpired() && !session.hasInactivePlayerBeenNotified(inactivePlayer, 2)) {
                    player.sendSystemMessage(Component.literal("[AUCTION] Bidding has finished!!!").withColor(0xFFFF5555));
                    session.inactiveBidderNotifications.add(new NotificationRecord(inactivePlayer, 2));
                }
            }
        }
    }

    public boolean joinSession(ServerPlayer bidder, UUID auctionID){
        AuctionSession session = findSessionByID(auctionID);
        if(session == null) return false;

        if(session.biddersInactive.contains(bidder.getUUID())){
            session.biddersInactive.remove(bidder.getUUID());
            session.bidders.remove(bidder.getUUID());
        }

        if(!session.bidders.contains(bidder.getUUID())) { // bidder already exists on backend.
            session.bidderJoin(bidder);
            BidderJoinS2CPacket bidderJoinPacket = new BidderJoinS2CPacket(bidder.getUUID(), new AuctionGuiData(session.id, session.auctioneer.getUUID()));
            CommonEconomy.packets.sendToPlayer(session.auctioneer, bidderJoinPacket);
            for (UUID player : session.bidders) {
                ServerPlayer playerObject = server.getPlayerList().getPlayer(player);
                if (playerObject == null) {
                    // disconnect
                    continue;
                }
                CommonEconomy.packets.sendToPlayer(playerObject, bidderJoinPacket);
            }
        }

        if(session.getCurrentBidding() != null && session.getCurrentBidding().isActive()){
            CommonEconomy.packets.sendToPlayer(bidder, new SynchronizeItemBiddingS2CPacket(Optional.ofNullable(session.getCurrentBidding())));
        }

        return true;
    }

    public void cancelSession(UUID auctionID){
        AuctionSession session = findSessionByID(auctionID);
        if(session == null) return;

        if(session.getCurrentBidding() != null && session.getCurrentBidding().isActive()){
            if(session.auctioneer != null){ // todo: save session in history so whenever a player joins, it will store items back if cancelled.
                setBiddingItemInInventories(session);
            }
        }

        if(session.biddings.isEmpty()) session.cancelled = true;

        if(!Objects.requireNonNull(session.auctioneer).hasDisconnected()){
            session.auctioneer.sendSystemMessage(Component.literal(session.cancelled? "Auction has been cancelled!" : "Auction has been completed"));
        }
        session.bidders.forEach((playerID) -> {
           ServerPlayer player = server.getPlayerList().getPlayer(playerID);
           if(player == null) return;

           CommonEconomy.packets.sendToPlayer(player, new CancelAuctionS2CPacket());
            player.sendSystemMessage(Component.literal(session.cancelled? "Auction has been cancelled!" : "Auction has been completed"));
        });

        snapshots.add(session);
        sessions.remove(session);
    }

    public void loadHistory(){
        Path historyFile = CommonEconomy.platform.getConfigurationDirectory()
                .resolve(CommonEconomy.MOD_ID)
                .resolve("auction_history.dat");

        if (!Files.exists(historyFile)) return;

        CompoundTag rootTag;
        try (InputStream in = Files.newInputStream(historyFile)) {
            // Read compressed NBT stream safely with Vanilla NbtAccounter
            rootTag = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
        } catch (IOException e) {
            CommonEconomy.LOGGER.error(e);
            return;
        }

        if (!rootTag.contains("Sessions")) return;

        ListTag list = NbtUtil.LoadHistoryTag(rootTag);
        for (int i = 0; i < list.size(); i++) {
            snapshots.add(new AuctionSession(server, NbtUtil.GetCompoundTag(list, i)));
        }
    }

    public void saveHistory(){
        if (!sessions.isEmpty()) {
            sessions.forEach(session -> cancelSession(session.id)); // todo
        }
        if (snapshots.isEmpty()) return;

        Path myModDir = CommonEconomy.platform.getConfigurationDirectory().resolve(CommonEconomy.MOD_ID);
        try {
            Files.createDirectories(myModDir);
        } catch (IOException e) {
            CommonEconomy.LOGGER.error(e);
            return;
        }

        CompoundTag root = new CompoundTag();
        root.putInt("SchemaVersion", 1);

        ListTag list = new ListTag();
        for (AuctionSession trade : snapshots) {
            list.add(trade.save());
        }
        root.put("Sessions", list);
        Path historyFile = myModDir.resolve("auction_history.dat");
        try (OutputStream out = Files.newOutputStream(
                historyFile,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        )) {
            // Always save compressed consistently
            NbtIo.writeCompressed(root, out);
        } catch (IOException e) {
            CommonEconomy.LOGGER.error(e);
        }
    }
}
