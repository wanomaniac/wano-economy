package com.wanomaniac.economy.trading.packets;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.trading.client.TradeMenuClientScreen;
import com.wanomaniac.economy.trading.packets.msgs.TradeSendPlayerBalanceS2CPacket;
import com.wanomaniac.economy.trading.packets.msgs.SetClientMoneyTextboxStatusS2CPacket;
import com.wanomaniac.economy.trading.packets.msgs.SyncExtraMoneyPayloadS2CPacket;
import net.minecraft.client.Minecraft;

public class TradePacketsClient {
    public static void register() {
        TradePacketsCommon.register();
        // Server -> Client
        CommonEconomy.packets.registerClientReceiver(
                SyncExtraMoneyPayloadS2CPacket.TYPE,
                (payload) -> Minecraft.getInstance().execute(() -> {
                    if (Minecraft.getInstance().screen instanceof TradeMenuClientScreen screen) {
                        screen.syncExtraMoney(payload.player(), payload.amount());
                        screen.hasSynchronizedMoney = true;
                    }
                })
        );

        CommonEconomy.packets.registerClientReceiver(
                TradeSendPlayerBalanceS2CPacket.TYPE,
                (payload) -> Minecraft.getInstance().execute(() -> {
                    if (Minecraft.getInstance().screen instanceof TradeMenuClientScreen screen) {
                        screen.playerCurrentMoney = payload.balance();
                    }
                })
        );

        CommonEconomy.packets.registerClientReceiver(
                SetClientMoneyTextboxStatusS2CPacket.TYPE,
                (payload) -> Minecraft.getInstance().execute(() -> {
                    if (Minecraft.getInstance().screen instanceof TradeMenuClientScreen screen) {
                        screen.setMoneyTextboxStatus(payload.enabled());
                    }
                })
        );

        CommonEconomy.packets.registerPayloads();
    }
}
