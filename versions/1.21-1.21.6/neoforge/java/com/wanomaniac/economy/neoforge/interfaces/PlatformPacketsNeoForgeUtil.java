package com.wanomaniac.economy.neoforge.interfaces;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

public class PlatformPacketsNeoForgeUtil {
    public static void sendToServer(CustomPacketPayload payload){
        PacketDistributor.sendToServer(payload);
    }
}
