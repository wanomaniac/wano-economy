package com.wanomaniac.economy.fabric.server;
import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.ServerEconomy;
import com.wanomaniac.economy.trading.TradeManager;
import com.wanomaniac.economy.trading.commands.TradeCommandRegister;
import com.wanomaniac.economy.trading.packets.TradePacketsServer;
import com.wanomaniac.economy.trading.server.TradeCancelReason;
import com.wanomaniac.economy.trading.server.TradeSession;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class EconomyServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        ServerEconomy.initalize();

    }
}
