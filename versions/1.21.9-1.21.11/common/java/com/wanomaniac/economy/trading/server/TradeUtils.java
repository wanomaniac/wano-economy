package com.wanomaniac.economy.trading.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class TradeUtils {
    public static String getPlayerUsername(MinecraftServer server, UUID playerUuid) {
        ServerPlayer onlinePlayer = server.getPlayerList().getPlayer(playerUuid);
        if (onlinePlayer != null) {
            return onlinePlayer.getGameProfile().name();
        }
        var nameAndId = server.services().nameToIdCache().get(playerUuid);
        if (nameAndId.isPresent()) {
            return nameAndId.get().name();
        }

        var cachedProfile = server.services().profileResolver().fetchById(playerUuid);
        if (cachedProfile.isPresent()) {
            return cachedProfile.get().name();
        }

        return "UNKNOWN";
    }
}
