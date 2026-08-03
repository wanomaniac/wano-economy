package com.wanomaniac.economy.auctioning.client;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class AuctionUtils {
public static String getPlayerUsername(UUID playerUuid) {
    var nameAndId = Minecraft.getInstance().services().nameToIdCache().get(playerUuid);
    if (nameAndId.isPresent()) {
        return nameAndId.get().name();
    }

    var cachedProfile = Minecraft.getInstance().services().profileResolver().fetchById(playerUuid);
    if (cachedProfile.isPresent()) {
        return cachedProfile.get().name();
    }

    return "UNKNOWN";
}
}
