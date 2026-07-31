package com.wanomaniac.economy.fabric.client;
import com.wanomaniac.economy.ClientEconomy;
import com.wanomaniac.economy.trading.client.TradeMenuClientScreen;
import com.wanomaniac.economy.trading.client.TradeMenuSnapshotClientScreen;
import com.wanomaniac.economy.trading.types.TradeMenuTypes;
import com.wanomaniac.economy.trading.packets.TradePacketsClient;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public final class EconomyFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientEconomy.initalize();
    }
}
