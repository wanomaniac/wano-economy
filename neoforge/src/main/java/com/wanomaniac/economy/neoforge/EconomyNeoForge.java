package com.wanomaniac.economy.neoforge;

import com.wanomaniac.economy.ClientEconomy;
import com.wanomaniac.economy.ServerEconomy;
import com.wanomaniac.economy.auctioning.packets.AuctionPacketsClient;
import com.wanomaniac.economy.neoforge.interfaces.PlatformMenuFactoryNeoForge;
import com.wanomaniac.economy.trading.packets.TradePacketsClient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import com.wanomaniac.economy.CommonEconomy;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(CommonEconomy.MOD_ID)
public final class EconomyNeoForge {
    IEventBus modEventBus;
    public static PayloadRegistrar registrar;
    public void register(RegisterPayloadHandlersEvent event) {
        registrar = event.registrar("1.0.0"); // todo: add version
        if (NeoForgeEnvironmentInfo.isClient()) {
            TradePacketsClient.register();
            AuctionPacketsClient.register();
        } else {
            ServerEconomy.initalize();
        }
    }

    public void registerScreens(RegisterMenuScreensEvent event) {
        ClientEconomy.initalize();
        PlatformMenuFactoryNeoForge.registerScreens(event);
    }

    public EconomyNeoForge(IEventBus modEventBus) {
        this.modEventBus = modEventBus;
        CommonEconomy.onInitalize();
        PlatformMenuFactoryNeoForge.MENUS.register(modEventBus);
        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::register);
        modEventBus.addListener(this::registerScreens);
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer victim)) return;
    
        var src = event.getSource().getEntity();
        if (src instanceof ServerPlayer killer) {
            CommonEconomy.getManager(victim.level().getServer()).handlePvpKill(victim, killer);
        }
    }
}
