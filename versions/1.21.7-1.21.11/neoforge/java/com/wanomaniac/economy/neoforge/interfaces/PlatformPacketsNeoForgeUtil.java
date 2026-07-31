package com.wanomaniac.economy.neoforge.interfaces;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class PlatformPacketsNeoForgeUtil {
    public static void sendToServer(CustomPacketPayload payload){
        ClientPacketDistributor.sendToServer(payload);
    }
}
