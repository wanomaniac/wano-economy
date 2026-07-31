package com.wanomaniac.economy.fabric.interfaces;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;

// refactor? this is pretty useless
public class PlatformPacketsFabricUtil {
    public static MinecraftServer getServerFromContext(ServerPlayNetworking.Context context){
        return context.server();
    }
}
