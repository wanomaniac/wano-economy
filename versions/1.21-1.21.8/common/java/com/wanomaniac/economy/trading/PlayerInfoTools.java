package com.wanomaniac.economy.trading;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.Objects;
import java.util.UUID;

public class PlayerInfoTools {
    public static ItemStack createPlayerHead(MinecraftServer server, UUID playerUuid) {
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);

        GameProfile profile = new GameProfile(playerUuid, "");
        head.set(DataComponents.PROFILE, new ResolvableProfile(profile));

        Objects.requireNonNull(server.getProfileCache()).get(playerUuid).ifPresent(
                (nameAndId ->
                        head.set(DataComponents.CUSTOM_NAME, Component.literal(nameAndId.getName())))
        );

        return head;
    }
}
