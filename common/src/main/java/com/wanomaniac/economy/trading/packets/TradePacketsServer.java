package com.wanomaniac.economy.trading.packets;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.EconomyManager;
import com.wanomaniac.economy.trading.packets.msgs.RequestPlayerBalanceC2SPacket;
import com.wanomaniac.economy.trading.packets.msgs.SendPlayerBalanceS2CPacket;
import com.wanomaniac.economy.trading.packets.msgs.UpdateExtraMoneyPayloadC2SPacket;
import com.wanomaniac.economy.trading.server.TradeMenu;
import com.wanomaniac.economy.trading.packets.msgs.*;
import com.wanomaniac.economy.trading.server.TradeMenuSnapshot;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class TradePacketsServer {
    public static void register(){
        TradePacketsCommon.register();
        // Client -> Server
        CommonEconomy.packets.registerServerReceiver(
                RequestPlayerBalanceC2SPacket.TYPE,
                (payload, server, player) -> Objects.requireNonNull(server).execute(() -> CommonEconomy.packets.sendToPlayer(player, new SendPlayerBalanceS2CPacket(CommonEconomy.getManager(player.level().getServer()).getBalance(player.getUUID(), false)))));

        CommonEconomy.packets.registerServerReceiver(
                UpdateExtraMoneyPayloadC2SPacket.TYPE,
                (payload, server, player) -> {
                    AtomicLong amount = new AtomicLong(payload.amount());
                    Objects.requireNonNull(server).execute(() -> {
                        if (player.containerMenu instanceof TradeMenu menu) {
                            if (amount.get() < 0) {
                                return;
                            }

                            EconomyManager manager = CommonEconomy.getManager(server);
                            Long balance = manager.getBalance(player.getUUID(), false);
                            if (amount.get() > balance) {
                                amount.set(balance); // clamp
                            }

                            menu.updateExtraMoney(player.getUUID(), amount.get());
                        }
                    });
                }
        );
        CommonEconomy.packets.registerServerReceiver(
                NotifySnapshotGuestLongEscapeC2SPacket.TYPE,
                (payload, server, player) -> Objects.requireNonNull(server).execute(() -> {
                    if(player.containerMenu instanceof TradeMenuSnapshot menuSnapshot){
                        menuSnapshot.setLongGuestState(payload.state());
                        player.closeContainer();
                    }
                })
        );


        CommonEconomy.packets.registerPayloads();
    }
}

