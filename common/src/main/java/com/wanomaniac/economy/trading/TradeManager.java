package com.wanomaniac.economy.trading;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.trading.server.TradeCancelReason;
import com.wanomaniac.economy.trading.server.TradeData;
import com.wanomaniac.economy.trading.server.TradeSession;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class TradeManager {

    private final Map<UUID, Set<UUID>> pendingRequests = new ConcurrentHashMap<>();

    // O(1) Index Lookup Maps
    public final List<TradeData> history = new CopyOnWriteArrayList<>();
    public final Map<UUID, TradeData> historyById = new ConcurrentHashMap<>();
    public final Map<UUID, Set<UUID>> playerTradeHistory = new ConcurrentHashMap<>();

    public final List<TradeSession> currentSessions = new CopyOnWriteArrayList<>();
    public final MinecraftServer server;

    public TradeManager(MinecraftServer server) {
        this.server = server;
        loadHistory();
    }

    public TradeSession createSession(ServerPlayer a, ServerPlayer b) {
        TradeSession session = new TradeSession(a, b);
        currentSessions.add(session);
        return session;
    }

    public void finishSession(UUID id) {
        currentSessions.removeIf(session -> session.id.equals(id));
    }

    @Nullable
    public TradeSession findCurrentPlayerSession(ServerPlayer player) {
        UUID pId = player.getUUID();
        for (TradeSession session : currentSessions) {
            if (session.a.getUUID().equals(pId) || session.b.getUUID().equals(pId)) {
                return session;
            }
        }
        return null;
    }

    public boolean requestTrade(ServerPlayer from, ServerPlayer to) {
        if (from.getUUID().equals(to.getUUID())) return false;
        pendingRequests.computeIfAbsent(to.getUUID(), k -> ConcurrentHashMap.newKeySet()).add(from.getUUID());
        return true;
    }

    public Set<UUID> getPendingFor(UUID target) {
        return pendingRequests.getOrDefault(target, Collections.emptySet());
    }

    public boolean hasRequestFrom(UUID receiver, UUID sender) {
        return pendingRequests.getOrDefault(receiver, Collections.emptySet()).contains(sender);
    }

    public boolean accept(UUID receiver, UUID sender) {
        return removePending(receiver, sender);
    }

    public boolean reject(UUID receiver, UUID sender) {
        return removePending(receiver, sender);
    }

    private boolean removePending(UUID receiver, UUID sender) {
        Set<UUID> req = pendingRequests.get(receiver);
        if (req == null || !req.remove(sender)) return false;
        if (req.isEmpty()) pendingRequests.remove(receiver);
        return true;
    }

    /**
     * O(1) Memory Indexing when snapshots are recorded
     */
    public void snapshotSession(TradeData data) {
        history.add(data);
        historyById.put(data.getId(), data);

        for (UUID trader : data.getTraders()) {
            playerTradeHistory.computeIfAbsent(trader, k -> ConcurrentHashMap.newKeySet()).add(data.getId());
        }
    }

    /**
     * Fast O(1) lookups
     */
    public Set<UUID> getHistory(UUID player) {
        return playerTradeHistory.getOrDefault(player, Collections.emptySet());
    }

    public List<UUID> getHistory() {
        return new ArrayList<>(historyById.keySet());
    }

    @Nullable
    public TradeData getSnapshot(UUID id) {
        return historyById.get(id);
    }

    public ServerPlayer getPlayer(UUID id) {
        return server.getPlayerList().getPlayer(id);
    }

    // --- SAVE & LOAD OPTIMIZATIONS ---

    public void saveHistory() {
        if (!currentSessions.isEmpty()) {
            currentSessions.forEach(session -> session.cancel(TradeCancelReason.SERVER_SHUTDOWN));
        }

        if (history.isEmpty()) return;

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
        for (TradeData trade : history) {
            list.add(trade.save());
        }
        root.put("Trades", list);

        Path historyFile = myModDir.resolve("trade_history.dat");

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

    public void loadHistory() {
        Path historyFile = CommonEconomy.platform.getConfigurationDirectory()
                .resolve(CommonEconomy.MOD_ID)
                .resolve("trade_history.dat");

        if (!Files.exists(historyFile)) return;

        CompoundTag rootTag;
        try (InputStream in = Files.newInputStream(historyFile)) {
            // Read compressed NBT stream safely with Vanilla NbtAccounter
            rootTag = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
        } catch (IOException e) {
            CommonEconomy.LOGGER.error(e);
            return;
        }

        if (!rootTag.contains("Trades")) return;

        Optional<ListTag> listObj = NbtUtil.getListFromCompound(rootTag, "Trades");
        if(listObj.isEmpty()) return;
        ListTag list = listObj.get();
        for (int i = 0; i < list.size(); i++) {
            TradeData data = TradeData.load(NbtUtil.GetCompoundTag(list, i));
            snapshotSession(data); // Builds O(1) indices automatically!
        }
    }
}