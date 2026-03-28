package com.reazip.economycraft.fabric.trading.packets;

import com.reazip.economycraft.fabric.trading.client.TradeMenuClientScreen;
import com.reazip.economycraft.fabric.trading.packets.msgs.SendPlayerBalanceS2CPacket;
import com.reazip.economycraft.fabric.trading.packets.msgs.SetClientMoneyTextboxStatusS2CP;
import com.reazip.economycraft.fabric.trading.packets.msgs.SyncExtraMoneyPayloadS2CP;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public class TradePacketsClient {
    public static void register() {
        TradePacketsCommon.register();
        // Client Side
        ClientPlayNetworking.registerGlobalReceiver(
                SyncExtraMoneyPayloadS2CP.TYPE,
                (payload, context) -> {
                    Minecraft.getInstance().execute(() -> {
                        if (Minecraft.getInstance().screen instanceof TradeMenuClientScreen screen) {
                            screen.syncExtraMoney(payload.player(), payload.amount());
                            screen.hasSynchronizedMoney = true;
                        }
                    });
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(
                SendPlayerBalanceS2CPacket.TYPE,
                (payload, context) -> {
                    Minecraft.getInstance().execute(() -> {
                        if (Minecraft.getInstance().screen instanceof TradeMenuClientScreen screen) {
                            screen.playerCurrentMoney = payload.balance();
                        }
                    });
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(
                SetClientMoneyTextboxStatusS2CP.TYPE,
                (payload, context) -> {
                    Minecraft.getInstance().execute(() -> {
                        if (Minecraft.getInstance().screen instanceof TradeMenuClientScreen screen) {
                            screen.setMoneyTextboxStatus(payload.enabled());
                        }
                    });
                }
        );
      
    }
}
