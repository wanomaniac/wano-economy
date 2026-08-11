package com.wanomaniac.economy.auctioning.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class AuctionUtils {
public static AtomicReference<String> getPlayerUsername(UUID playerUuid) {
    AtomicReference<String> usernameRef = new AtomicReference<>("UNKNOWN");
    if (Minecraft.getInstance().getConnection() != null) {
        PlayerInfo playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(playerUuid);
        if (playerInfo != null) {
            usernameRef.set(playerInfo.getProfile().getName());
        }
    }

    return usernameRef;
}
}
