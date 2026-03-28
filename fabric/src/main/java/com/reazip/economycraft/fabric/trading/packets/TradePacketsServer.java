package com.reazip.economycraft.fabric.trading.packets;

import com.reazip.economycraft.EconomyCraft;
import com.reazip.economycraft.EconomyManager;
import com.reazip.economycraft.fabric.trading.server.TradeMenu;
import com.reazip.economycraft.fabric.trading.packets.msgs.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;


@Environment(EnvType.SERVER)
public class TradePacketsServer {
    public static void register(){
        TradePacketsCommon.register();
        // Server Side recievers
        ServerPlayNetworking.registerGlobalReceiver(
                RequestPlayerBalanceC2SPacket.TYPE,
                (payload, context) -> {
                    Objects.requireNonNull(context.player().getServer()).execute(() -> {
                        ServerPlayNetworking.send(context.player(), new SendPlayerBalanceS2CPacket(EconomyCraft.getManager(context.player().getServer()).getBalance(context.player().getUUID(), false)));
                    });
                });

        ServerPlayNetworking.registerGlobalReceiver(
                UpdateExtraMoneyPayloadC2SP.TYPE,
                (payload, context) -> {
                    AtomicLong amount = new AtomicLong(payload.amount());

                    Objects.requireNonNull(context.player().getServer()).execute(() -> {
                        if (context.player().containerMenu instanceof TradeMenu menu) {
                            if (amount.get() < 0) {
                                return;
                            }

                            EconomyManager manager = EconomyCraft.getManager(context.player().getServer());
                            Long balance = manager.getBalance(context.player().getUUID(), false);
                            if (amount.get() > balance) {
                                amount.set(balance); // clamp
                            }

                            menu.updateExtraMoney(context.player().getUUID(), amount.get());
                        }
                    });
                }
        );
    }
}

