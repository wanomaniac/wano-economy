package com.wanomaniac.economy.trading;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.UUID;

public class PlayerInfoTools {
    public static ItemStack createPlayerHead(MinecraftServer server, UUID playerUuid) {
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);

        // Resolve profile (may be cached, may hit Mojang services)
        server.getProfileCache()
                .get(playerUuid)
                .ifPresent(profile -> {
                    head.set(DataComponents.PROFILE, new ResolvableProfile(profile));

                    // Optional: set name from profile
                    head.set(
                            DataComponents.CUSTOM_NAME,
                            Component.literal(profile.getName())
                    );
                });

        return head;
    }
}
