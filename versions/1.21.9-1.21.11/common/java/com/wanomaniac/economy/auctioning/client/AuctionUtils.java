package com.wanomaniac.economy.auctioning.client;

import com.wanomaniac.economy.GeneralUtils;
import net.minecraft.client.Minecraft;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class AuctionUtils {
    public static Map<UUID, String> fetchedUsernamesFromResolver = new HashMap<>();

public static AtomicReference<String> getPlayerUsername(UUID playerUuid) {
    AtomicReference<String> username = new AtomicReference<>("LOADING");

    var nameAndId = Minecraft.getInstance().services().nameToIdCache().get(playerUuid);
    if (nameAndId.isPresent()) {
        username.set(nameAndId.get().name());
        return username;
    }

    if(fetchedUsernamesFromResolver.containsKey(playerUuid)){
        username.set(fetchedUsernamesFromResolver.get(playerUuid));
        return username;
    }

    CompletableFuture.runAsync(() -> {
        try {
            var cachedProfile = Minecraft.getInstance().services().profileResolver().fetchById(playerUuid);
            if (cachedProfile.isPresent()) {
                username.set(cachedProfile.get().name());
            } else {
                username.set("UNKNOWN");
            }
        } catch (Exception e) {
            username.set("UNKNOWN");
        }

        fetchedUsernamesFromResolver.put(playerUuid, username.get());
    }, GeneralUtils.getBackgroundExecutor());

    return username;
}
}
