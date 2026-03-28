package com.reazip.economycraft.fabric.server;
import com.reazip.economycraft.fabric.trading.TradeManager;
import com.reazip.economycraft.fabric.trading.commands.TradeCommandRegister;
import com.reazip.economycraft.fabric.trading.packets.TradePacketsServer;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import net.fabricmc.api.DedicatedServerModInitializer;

public class InitalizerFabricServer implements DedicatedServerModInitializer {
    public static TradeManager TRADE_MANAGER;

    @Override
    public void onInitializeServer() {
        TradePacketsServer.register();
        LifecycleEvent.SERVER_STARTING.register(server -> TRADE_MANAGER = new TradeManager(server));
        LifecycleEvent.SERVER_STOPPING.register(server -> TRADE_MANAGER.saveHistory());
        CommandRegistrationEvent.EVENT.register(TradeCommandRegister::registerCommands);
    }
}
