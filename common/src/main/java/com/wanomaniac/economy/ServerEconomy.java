package com.wanomaniac.economy;

import com.wanomaniac.economy.auctioning.AuctionManager;
import com.wanomaniac.economy.auctioning.commands.AuctionCommandRegister;
import com.wanomaniac.economy.auctioning.packets.AuctionPacketsServer;
import com.wanomaniac.economy.trading.TradeManager;
import com.wanomaniac.economy.trading.commands.TradeCommandRegister;
import com.wanomaniac.economy.trading.packets.TradePacketsServer;
import com.wanomaniac.economy.trading.server.TradeCancelReason;
import com.wanomaniac.economy.trading.server.TradeSession;
import net.minecraft.server.level.ServerPlayer;

import static com.wanomaniac.economy.CommonEconomy.eventRegistry;

public class ServerEconomy {
    public static TradeManager TRADE_MANAGER;
    public static AuctionManager AUCTION_MANAGER;

    public static void initalize(){
        AuctionPacketsServer.register();
        TradePacketsServer.register();
        eventRegistry.whenPlayerJoins(CommonEconomy::onPlayerJoin);
        eventRegistry.whenLivingEntityAfterDeath((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayer victim)) return;

            var src = damageSource.getEntity();
            if (src instanceof ServerPlayer killer) {
                CommonEconomy.getManager(victim.level().getServer()).handlePvpKill(victim, killer);
            }
        });
        eventRegistry.onCommandRegistrationCallback((dispatcher, registry, selection) -> {
            EconomyCommands.register(dispatcher);
        });
        eventRegistry.onServerStarted(CommonEconomy::getManager);
        eventRegistry.onServerStopping(server -> {
            CommonEconomy.getManager(server).save();
        });
        eventRegistry.onServerStarting(server -> TRADE_MANAGER = new TradeManager(server));
        eventRegistry.onServerStarting(server -> AUCTION_MANAGER = new AuctionManager(server));
        eventRegistry.onServerStopping(server -> TRADE_MANAGER.saveHistory());
        eventRegistry.onServerStopping(server -> AUCTION_MANAGER.saveHistory());
        eventRegistry.onCommandRegistrationCallback(TradeCommandRegister::registerCommands);
        eventRegistry.onCommandRegistrationCallback(AuctionCommandRegister::registerCommands);

        eventRegistry.whenServerTicks((server) -> AUCTION_MANAGER.tickAllSessions());


//        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, originWorld, destinationWorld) -> {
//            // Check if this player was actively trading when they hopped portals
//            TradeSession session = TRADE_MANAGER.findCurrentPlayerSession(player);
//            if (session != null) {
//                // Terminate the trade explicitly for dimension crossing
//                session.cancel(TradeCancelReason.WORLD_ACTION);
//            }
//        });

    }
}
