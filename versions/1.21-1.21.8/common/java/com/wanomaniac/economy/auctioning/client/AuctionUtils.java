package com.wanomaniac.economy.auctioning.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class AuctionUtils {
public static String getPlayerUsername(UUID playerUuid) {
    if (Minecraft.getInstance().getConnection() != null) {
        PlayerInfo playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(playerUuid);
        if (playerInfo != null) {
            return playerInfo.getProfile().getName();
        }
    }

    return "UNKNOWN";
}
}
