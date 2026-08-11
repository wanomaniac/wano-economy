package com.wanomaniac.economy.auctioning;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.EconomyManager;
import com.wanomaniac.economy.auctioning.packets.msgs.BidderJoinS2CPacket;
import com.wanomaniac.economy.auctioning.packets.msgs.BidderLeavePacket;
import com.wanomaniac.economy.auctioning.packets.msgs.CancelAuctionS2CPacket;
import com.wanomaniac.economy.auctioning.packets.msgs.SynchronizeItemBiddingS2CPacket;
import com.wanomaniac.economy.auctioning.server.AuctionSession;
import com.wanomaniac.economy.auctioning.server.ItemBidding;
import com.wanomaniac.economy.auctioning.server.NotificationRecord;
import com.wanomaniac.economy.auctioning.types.AuctionGuiData;
import com.wanomaniac.economy.trading.NbtUtil;
import com.wanomaniac.economy.trading.server.TradeSession;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
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
import java.util.concurrent.atomic.AtomicBoolean;

public class AuctionManager {
    public final List<UUID> sessionsToBeCancelled = new ArrayList<>();
    public final List<AuctionSession> sessions = new ArrayList<>();
    public final List<AuctionSession> snapshots = new CopyOnWriteArrayList<>();
    public final MinecraftServer server;

    public AuctionManager(MinecraftServer server) {
        this.server = server;
        loadHistory();
    }

    public void synchronizeSessionForAuctioneer(ServerPlayer auctioneer){
        AuctionSession playerPrevSession = findSessionByAuctioneerID(auctioneer.getUUID());
        if(playerPrevSession != null) {
            playerPrevSession.auctioneer = auctioneer;
            // synchronize it.
            for (ItemBidding bidding : playerPrevSession.getAllBiddings()) {
                CommonEconomy.packets.sendToPlayer(auctioneer, new SynchronizeItemBiddingS2CPacket(Optional.ofNullable(bidding)));
            }
            for (UUID bidderID : playerPrevSession.bidders) {
                BidderJoinS2CPacket bidderJoinPacket = new BidderJoinS2CPacket(bidderID, new AuctionGuiData(playerPrevSession.id, playerPrevSession.auctioneer.getUUID(), false));
                CommonEconomy.packets.sendToPlayer(auctioneer, bidderJoinPacket);
            }
            sessionsToBeCancelled.remove(playerPrevSession.id);
        }
    }

    public AuctionSession createSession(ServerPlayer auctioneer){
        AuctionSession playerPrevSession = findSessionByAuctioneerID(auctioneer.getUUID());
        if(playerPrevSession != null){
            playerPrevSession.auctioneer = auctioneer;
            // synchronize it.
            if(playerPrevSession.getCurrentBidding() != null && playerPrevSession.getCurrentBidding().isActive()){
                CommonEconomy.packets.sendToPlayer(auctioneer, new SynchronizeItemBiddingS2CPacket(Optional.ofNullable(playerPrevSession.getCurrentBidding())));
            }
            for(UUID bidderID : playerPrevSession.bidders) {
                BidderJoinS2CPacket bidderJoinPacket = new BidderJoinS2CPacket(bidderID, new AuctionGuiData(playerPrevSession.id, playerPrevSession.auctioneer.getUUID(), false));
                CommonEconomy.packets.sendToPlayer(auctioneer, bidderJoinPacket);
            }

            // return it.
            return findSessionByAuctioneerID(auctioneer.getUUID());
        }

        AuctionSession session = new AuctionSession(auctioneer);
        sessions.add(session);
        Component clickableCommand = AuctionUtil.createAsClickableCommandComponent("/auction join " + auctioneer.getName().getString());

        Component message = Component.empty()
                .append(auctioneer.getName().copy().withStyle(ChatFormatting.BOLD))
                .append(Component.literal(" has started an auction!\n").withStyle(ChatFormatting.ITALIC))
                .append(Component.literal("Use ").append(clickableCommand).append(" to participate!"));
        server.getPlayerList().broadcastSystemMessage(message, false);

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

    public AuctionSession findSessionByBidderID(UUID id){
        return sessions.stream()
                .filter(session -> session.bidders != null && session.bidders.contains(id))
                .findFirst()
                .orElse(null);
    }

    public boolean doesSessionExistForAuctionnerID(UUID id){
        return findSessionByAuctioneerID(id) != null;
    }

    private void setBiddingItemInInventories(AuctionSession session){
        setBiddingItemInInventories(session.getCurrentBidding(), session);
    }

    private void setBiddingItemInInventories(ItemBidding bidding, AuctionSession session){
        ItemStack item = bidding.item().copy();

        // Items cannot be given while the bidding is active. Must be done again
        if(bidding.highestBidder() != null && bidding.isExpired()) {
            ServerPlayer player = server.getPlayerList().getPlayer(bidding.highestBidder());
            if (player == null){
                bidding.finalized = false;
                return;
            }
            Component message = Component.literal("[AUCTION] transaction for item ") // todo: translatable
                    .withStyle(ChatFormatting.WHITE)
                    .append(AuctionUtil.getItemComponent(item))
                    .append(Component.literal(" has completed.").withStyle(ChatFormatting.GREEN));

            player.getInventory().placeItemBackInInventory(item);
            bidding.finalized = true;

            if(session.auctioneer != null){
                session.auctioneer.sendSystemMessage(message);
                TradeSession.playCompleteSound(session.auctioneer);
            }
            player.sendSystemMessage(message);
            TradeSession.playCompleteSound(player);
        } else { // If bidding is cancelled, dont save it.
            if (session.auctioneer == null) {
                bidding.finalized = false;
                return;
            }
            session.auctioneer.sendSystemMessage(Component.literal(bidding.isActive() ? "Bidding was cancelled." : "Nobody bidded.").withStyle(ChatFormatting.GRAY));
            session.cancelBidding(server);
            // if this is the only bidding, remove it from snapshots
            if(session.biddings.isEmpty()){
                snapshots.remove(session); // shouldnt be possible to not give back items to a active session (auctioneer is still in)
            }
        }
    }

    public void tickAllSessions() {
        List<AuctionSession> tempSessions;
        synchronized (sessions) {
            tempSessions = new ArrayList<>(sessions);
        }
        List<UUID> toCancelLocal = new ArrayList<>();

        for (AuctionSession session : tempSessions) {
            if(session.getCurrentBidding() == null) continue;

            boolean wasActive = session.getCurrentBidding().isActive();
            session.getCurrentBidding().tick();
            if(wasActive != session.getCurrentBidding().isActive()) {
                session.sendPayloadToSessionMembers(server, new SynchronizeItemBiddingS2CPacket(Optional.empty()));
                if(session.getCurrentBidding().highestBidder() == null){
                    session.cancelBidding(server);
                } else {
                    setBiddingItemInInventories(session);
                }
                AtomicBoolean wasSessionDeleted = new AtomicBoolean(false);
                synchronized (sessionsToBeCancelled) {
                    if (sessionsToBeCancelled.contains(session.id)) {
                        toCancelLocal.add(session.id);
                        wasSessionDeleted.set(true);
                    }
                }

                if(wasSessionDeleted.get()) continue;
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

        for (UUID idToCancel : toCancelLocal) {
            cancelSession(idToCancel);
            synchronized (sessionsToBeCancelled) {
                sessionsToBeCancelled.remove(idToCancel);
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
            BidderJoinS2CPacket bidderJoinPacket = new BidderJoinS2CPacket(bidder.getUUID(), new AuctionGuiData(session.id, session.auctioneer.getUUID(), false));
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

        if(session.getCurrentBidding() != null && session.getCurrentBidding().isActive() && session.getCurrentBidding().highestBidder() == null){
            session.cancelBidding(server);
        }

        // cannot stop bidding, auctioneer will just have to be gone.
        if(session.getCurrentBidding() != null && session.getCurrentBidding().isActive() && session.getCurrentBidding().highestBidder() != null){
            sessionsToBeCancelled.add(session.id);
            return;
        }

        if(session.biddings.isEmpty())  {
            session.cancelled = true;
        }

        server.getPlayerList().broadcastSystemMessage(Component.literal(session.cancelled? "The auction has been cancelled!" : "The auction has finished!"), false);
        session.bidders.forEach((playerID) -> {
           ServerPlayer player = server.getPlayerList().getPlayer(playerID);
           if(player == null) return;

           CommonEconomy.packets.sendToPlayer(player, new CancelAuctionS2CPacket());
        });

        session.auctioneer = null;
        if(!session.cancelled) snapshots.add(session);
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

        Optional<ListTag> listObj = NbtUtil.getListFromCompound(rootTag, "Sessions");
        if(listObj.isEmpty()) return;
        ListTag list = listObj.get();

        for (int i = 0; i < list.size(); i++) {
            snapshots.add(new AuctionSession(server, NbtUtil.GetCompoundTag(list, i)));
        }
    }

    public void saveHistory(){
        if (!sessions.isEmpty()) {
            List<AuctionSession> sessionsToCancel;

            synchronized (sessions) {
                sessionsToCancel = new ArrayList<>(sessions);
            }

            for (AuctionSession session : sessionsToCancel) {
                cancelSession(session.id); // todo: add reasons.
            }
        }

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
            list.add(trade.save(server));
        }
        root.put("Sessions", list);
        Path historyFile = myModDir.resolve("auction_history.dat");
        try (OutputStream out = Files.newOutputStream(
                historyFile,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        )) {
            NbtIo.writeCompressed(root, out);
        } catch (IOException e) {
            CommonEconomy.LOGGER.error(e);
        }
    }

    private void giveBiddingsForUnfinalized(AuctionSession session, ServerPlayer player){
        for (Map.Entry<UUID, ItemBidding> bidding : session.biddings.entrySet()){
            boolean isPlayerWeAreLookingFor = bidding.getValue().highestBidder() != null && bidding.getValue().isExpired() ? bidding.getValue().highestBidder().equals(player.getUUID()) : session.auctioneerID.equals(player.getUUID());
            if(!bidding.getValue().finalized && isPlayerWeAreLookingFor){
                setBiddingItemInInventories(bidding.getValue(), session);
            }
        }
    }

    // Get snapshots/sessions that were unearned and give items.
    public void onPlayerJoin(ServerPlayer player) {
        for (AuctionSession session : sessions) { // for sessions that are still active.
            giveBiddingsForUnfinalized(session, player);
        }

        for (AuctionSession session : snapshots) { // for sessions that were ended
            if(session.auctioneer == null){
                session.auctioneer = server.getPlayerList().getPlayer(session.auctioneerID);
            }
            giveBiddingsForUnfinalized(session, player);
        }
    }

    public AuctionSession findSnapshotByID(UUID id) {
        Optional<AuctionSession> sessionObj = snapshots.stream()
                .filter(auctionSession -> auctionSession.id.equals(id))
                .findFirst();
        return sessionObj.orElse(null);
    }
}
