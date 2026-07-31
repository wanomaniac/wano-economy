package com.wanomaniac.economy;

import com.wanomaniac.economy.auctioning.types.AuctionMenuTypes;
import com.wanomaniac.economy.interfaces.IPlatformEventRegistry;
import com.wanomaniac.economy.interfaces.IPlatformHelper;
import com.wanomaniac.economy.interfaces.IPlatformPackets;
import com.wanomaniac.economy.interfaces.IPlatformRegistryInit;
import com.wanomaniac.economy.services.ServiceKey;
import com.wanomaniac.economy.services.ServicesManager;
import com.wanomaniac.economy.trading.types.TradeMenuTypes;
import com.wanomaniac.economy.util.ChatCompat;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.NumberFormat;
import java.util.Locale;

public final class CommonEconomy {
    public static final String MOD_ID = "wanoeconomy";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    private static EconomyManager manager;
    private static MinecraftServer lastServer;
    private static final NumberFormat FORMAT = NumberFormat.getInstance(Locale.ENGLISH);
    public static final IPlatformEventRegistry eventRegistry = ServicesManager.get(ServiceKey.of(IPlatformEventRegistry.class));
    public static final IPlatformHelper platform = ServicesManager.get(ServiceKey.of(IPlatformHelper.class));
    public static final IPlatformPackets packets = ServicesManager.get(ServiceKey.of(IPlatformPackets.class));

    public static void onInitalize(){
        TradeMenuTypes.init();
        AuctionMenuTypes.init();
        IPlatformRegistryInit initalizeRegister = ServicesManager.get(ServiceKey.of(IPlatformRegistryInit.class));
        initalizeRegister.register();
    }

    public static void onPlayerJoin(ServerPlayer player) {
        EconomyManager eco = getManager(player.level().getServer());
        eco.getBalance(player.getUUID(), true);

        if (eco.getOrders().hasDeliveries(player.getUUID()) || eco.getShop().hasDeliveries(player.getUUID())) {
            ClickEvent ev = ChatCompat.runCommandEvent("/eco orders claim");

            if (ev != null) {
                Component msg = Component.literal("You have unclaimed items: ")
                        .withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal("[Claim]")
                                .withStyle(s -> s.withUnderlined(true).withColor(ChatFormatting.GREEN).withClickEvent(ev)));
                player.sendSystemMessage(msg);
            } else {
                ChatCompat.sendRunCommandTellraw(
                        player,
                        "You have unclaimed items: ",
                        "[Claim]",
                        "/eco orders claim"
                );
            }
        }
    }

    public static EconomyManager getManager(MinecraftServer server) {
        if (manager == null || lastServer != server) {
            manager = new EconomyManager(server);
            lastServer = server;
        }
        return manager;
    }

    public static String formatMoney(long amount) {
        return "$" + FORMAT.format(amount);
    }
}
