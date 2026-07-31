package com.wanomaniac.economy.trading.client;

import com.wanomaniac.economy.ClientEconomy;
import com.wanomaniac.economy.ServerEconomy;
import com.wanomaniac.economy.trading.server.*;
import com.wanomaniac.economy.trading.server.*;
import com.wanomaniac.economy.trading.types.TradeGuiData;
import com.wanomaniac.economy.trading.types.TradeMenuTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import java.util.UUID;

import static com.wanomaniac.economy.ServerEconomy.TRADE_MANAGER;

public final class TradeUi {
    private TradeUi() {}

    public static void openHistoryMenu(ServerPlayer player, int page, boolean asAdmin, UUID filterPlayer) {
        player.openMenu(new SimpleMenuProvider((id, inv, p) -> new com.wanomaniac.economy.trading.server.TradeHistoryMenu(id, inv, new HistoryMenuDetails(page, asAdmin, filterPlayer)), Component.literal(!asAdmin ? "Your Trade History" : "Server Trade History")));
    }

    public static void openSnapshot(ServerPlayer player, TradeData data) {
        ClientEconomy.menuFactory.openExtendedMenu(player, TradeMenuTypes.TRADE_SNAPSHOT_MENU, Component.literal("Snapshot of trade " + data.getId()), TradeMenuSnapshot::new, data);
    }

    public static void openSnapshot(ServerPlayer player, TradeData data, HistoryMenuDetails details) {
        Component name = Component.literal(String.format("Trade with %s & %s", TradeUtils.getPlayerUsername(TRADE_MANAGER.server, data.getTraders().getFirst()), TradeUtils.getPlayerUsername(TRADE_MANAGER.server, data.getTraders().getLast())));
        ClientEconomy.menuFactory.openExtendedMenu(player, TradeMenuTypes.TRADE_SNAPSHOT_MENU, name, (menuType, syncID, inventory, data2) -> new TradeMenuSnapshot(menuType, syncID, inventory, data2, details), data);
    }

    public static void open(ServerPlayer a, ServerPlayer b) {
        TradeSession session = ServerEconomy.TRADE_MANAGER.createSession(a, b);
        ClientEconomy.menuFactory.openExtendedMenu(a, TradeMenuTypes.TRADE_MENU, Component.literal("Trading with " + b.getName().getString()), (menuType, syncID, inv, data) -> {
        TradeMenu menu = new TradeMenu(menuType, syncID, inv, session, true);
        session.menuA = menu;
        return menu;
        }, new TradeGuiData(session.id, a.getUUID(), b.getUUID(), true, 0L,0L));


        ClientEconomy.menuFactory.openExtendedMenu(b, TradeMenuTypes.TRADE_MENU, Component.literal("Trading with " + a.getName().getString()), (menuType, syncID, inv, data) -> {
            TradeMenu menu = new TradeMenu(menuType, syncID, inv, session, false);
            session.menuB = menu;
            return menu;
        }, new TradeGuiData(session.id, a.getUUID(), b.getUUID(), false, 0L,0L));
    }
}
