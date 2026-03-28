package com.reazip.economycraft.fabric.trading;
import com.reazip.economycraft.fabric.trading.server.TradeData;
import com.reazip.economycraft.fabric.trading.server.TradeSession;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public class TradeManager {

    // trade requests: key = receiver, value = list of senders
    private final Map<UUID, Set<UUID>> pendingRequests = new HashMap<>();

    // trade history, store last N trades
    public final List<TradeData> history = new ArrayList<>();

    public final MinecraftServer server;

    public TradeManager(MinecraftServer server) {
        this.server = server;
        loadHistory();
    }

    public boolean requestTrade(ServerPlayer from, ServerPlayer to) {
        if (from.getUUID().equals(to.getUUID())) return false;

        pendingRequests.computeIfAbsent(to.getUUID(), k -> new HashSet<>()).add(from.getUUID());
        return true;
    }

    public Set<UUID> getPendingFor(UUID target) {
        return pendingRequests.getOrDefault(target, Collections.emptySet());
    }

    public boolean hasRequestFrom(UUID receiver, UUID sender) {
        return pendingRequests.getOrDefault(receiver, Collections.emptySet()).contains(sender);
    }

    public boolean accept(UUID receiver, UUID sender) {
        Set<UUID> req = pendingRequests.get(receiver);
        if (req == null || !req.remove(sender)) return false;

        if (req.isEmpty()) pendingRequests.remove(receiver);
        return true;
    }

    public boolean reject(UUID receiver, UUID sender) {
        Set<UUID> req = pendingRequests.get(receiver);
        if (req == null || !req.remove(sender)) return false;

        if (req.isEmpty()) pendingRequests.remove(receiver);
        return true;
    }

    public void snapshotSession(TradeData data){
        history.add(data);
    }

    public void saveHistory(){
        if(history.isEmpty()) return;
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path myModDir = configDir.resolve("economycraft");
        try {
            Files.createDirectories(myModDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        CompoundTag root = new CompoundTag();
        root.putInt("SchemaVersion", 1);

        ListTag list = new ListTag();
        for (TradeData trade : history) {
            list.add(trade.save()); // TradeData → CompoundTag
        }
        root.put("Trades", list);
        Path history = myModDir.resolve("trade_history.dat");
        try (OutputStream out = Files.newOutputStream(
                history,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        )) {
            boolean isDev = FabricLoader.getInstance().isDevelopmentEnvironment();
            if(isDev){
                NbtIo.write(root, history);
            } else NbtIo.writeCompressed(root, out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadHistory(){
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path myModDir = configDir.resolve("economycraft");
        Path history = myModDir.resolve("trade_history.dat");
        if(!Files.exists(history)) return;
        CompoundTag rootTag;
        try {
            rootTag = NbtIo.read(history);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ListTag lists = rootTag.getList("Trades").get();

        lists.forEach((trade -> {
            this.history.add(TradeData.load((CompoundTag) trade));
        }));
    }

    public List<UUID> getHistory(UUID player) {
        List<UUID> involvedTrades = new ArrayList<>();
        history.forEach((data) -> {
            if(data.getTraders().contains(player)) involvedTrades.add(data.getId());
        });
        return involvedTrades;
    }

    public List<UUID> getHistory() {
        return history.stream().map(TradeData::getId).collect(Collectors.toList());
    }

    public TradeData getSnapshot(UUID id) {
        if(history.isEmpty()) return null;

        return history.stream().filter(data -> data.getId().equals(id)).findFirst().orElse(null);
    }

    public ServerPlayer getPlayer(UUID id) {
        return server.getPlayerList().getPlayer(id);
    }
}

