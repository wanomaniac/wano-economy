package com.wanomaniac.economy.trading.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;
import java.util.UUID;

public class TradeUtils {
    public static String getPlayerUsername(MinecraftServer server, UUID playerUuid) {
        ServerPlayer onlinePlayer = server.getPlayerList().getPlayer(playerUuid);
        if (onlinePlayer != null) {
            return onlinePlayer.getGameProfile().getName();
        }
        var nameAndId = Objects.requireNonNull(server.getProfileCache()).get(playerUuid);
        if (nameAndId.isPresent()) {
            return nameAndId.get().getName();
        }

        return "UNKNOWN";
    }
}
