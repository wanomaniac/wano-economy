package com.reazip.economycraft.fabric.client;
import com.reazip.economycraft.fabric.trading.client.TradeMenuClientScreen;
import com.reazip.economycraft.fabric.trading.client.TradeMenuSnapshootClientScreen;
import com.reazip.economycraft.fabric.trading.types.TradeMenuTypes;
import com.reazip.economycraft.fabric.trading.packets.TradePacketsClient;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public final class EconomyCraftFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(TradeMenuTypes.TRADE_SNAPSHOT_MENU, TradeMenuSnapshootClientScreen::new);
        MenuScreens.register(TradeMenuTypes.TRADE_MENU, TradeMenuClientScreen::new);
        TradePacketsClient.register();
    }
}
