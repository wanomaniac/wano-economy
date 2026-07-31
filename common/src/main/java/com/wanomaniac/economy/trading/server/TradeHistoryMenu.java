package com.wanomaniac.economy.trading.server;

import com.wanomaniac.economy.ServerEconomy;
import com.wanomaniac.economy.trading.PlayerInfoTools;
import com.wanomaniac.economy.trading.client.TradeUi;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TradeHistoryMenu extends AbstractContainerMenu {
    private static final int ROW_COUNT = 6;
    private static final int TOTAL_SLOTS = ROW_COUNT * 9; // 54 slots
    private static final int ITEMS_PER_PAGE = 45; // First 5 rows for player heads

    // Bottom row slot IDs
    private static final int PREV_PAGE_SLOT = 45; // Row 6, Col 1
    private static final int NEXT_PAGE_SLOT = 53; // Row 6, Col 9

    private final List<TradeData> pageTrades = new ArrayList<>();
    private final int currentPage;
    private final int maxPage;
    private final boolean adminView;
    private final UUID adminFilterPlayer;

    public TradeHistoryMenu(int id, Inventory playerInv) {
        this(id, playerInv, new HistoryMenuDetails(0, false, null));
    }

    public TradeHistoryMenu(int id, Inventory playerInv, int page) {
        this(id, playerInv, new HistoryMenuDetails(page, false, null));
    }

    public TradeHistoryMenu(int id, Inventory playerInv, HistoryMenuDetails details) {
        super(MenuType.GENERIC_9x6, id);
        this.adminView = details.asAdmin();
        this.currentPage = details.lastPage();
        this.adminFilterPlayer = details.filterPlayerAdmin();

        Player player = playerInv.player;
        UUID viewerUuid = player.getUUID();

        // 1. Gather filtered list of all trades involving this player
        List<TradeData> allRelevantTrades;

        if(adminView){
            if(details.filterPlayerAdmin() != null){
                allRelevantTrades = ServerEconomy.TRADE_MANAGER.history.stream()
                        .filter(data -> data.getTraders().contains(details.filterPlayerAdmin()))
                        .toList();
            } else allRelevantTrades = ServerEconomy.TRADE_MANAGER.history;
        } else {
            allRelevantTrades = ServerEconomy.TRADE_MANAGER.history.stream()
                    .filter(data -> data.getTraders().contains(viewerUuid))
                    .toList();
        }

        // Calculate max page boundaries
        this.maxPage = Math.max(0, (allRelevantTrades.size() - 1) / ITEMS_PER_PAGE);

        SimpleContainer container = new SimpleContainer(TOTAL_SLOTS);
        int startIndex = currentPage * ITEMS_PER_PAGE;

        // 2. Populate the first 45 slots with historical data
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            int tradeIndex = startIndex + i;
            if (tradeIndex >= allRelevantTrades.size()) break;

            TradeData trade = allRelevantTrades.get(tradeIndex);
            pageTrades.add(trade); // Map local slot tracking directly to this layout page

            UUID partnerUuid = trade.getTraders().get(0).equals(viewerUuid)
                    ? trade.getTraders().get(1)
                    : trade.getTraders().get(0);

            ItemStack head = PlayerInfoTools.createPlayerHead(player.level().getServer(), partnerUuid);
            container.setItem(i, head);
        }


        // 3. Setup Navigation Arrows on Row 6 if pages exist

        if(allRelevantTrades.size() > ITEMS_PER_PAGE) {
            if (currentPage > 0) {
                ItemStack prevArrow = new ItemStack(Items.ARROW);
                prevArrow.set(DataComponents.CUSTOM_NAME, Component.literal("Previous Page (Page " + currentPage + ")"));
                container.setItem(PREV_PAGE_SLOT, prevArrow);
            }

            if (allRelevantTrades.size() > ITEMS_PER_PAGE * (currentPage + 1)) {
                if (currentPage < maxPage) {
                    ItemStack nextArrow = new ItemStack(Items.ARROW);
                    nextArrow.set(DataComponents.CUSTOM_NAME, Component.literal("Next Page (Page " + (currentPage + 2) + ")"));
                    container.setItem(NEXT_PAGE_SLOT, nextArrow);
                }
            }

            // Fill remaining empty cells on row 6 with spacer clean glass
            for (int i = 45; i < TOTAL_SLOTS; i++) {
                if (i != PREV_PAGE_SLOT && i != NEXT_PAGE_SLOT) {
                    ItemStack glass = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
                    glass.set(DataComponents.CUSTOM_NAME, Component.literal("Page " + (currentPage + 1) + " of " + (maxPage + 1)));
                    container.setItem(i, glass);
                }
            }
        }


        // 4. Bind container slots to positions
        for (int row = 0; row < ROW_COUNT; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(container, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        // 5. Append player storage assets down under the screen container canvas
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 198));
        }
    }

    @Override
    public void clicked(int slotId, int drag, ClickType type, Player player) {
        if (slotId < 0 || slotId >= TOTAL_SLOTS) {
            if (player instanceof ServerPlayer sp)
            return;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            // Handle pagination turns
            if (slotId == PREV_PAGE_SLOT && currentPage > 0) {
                Objects.requireNonNull(serverPlayer.level().getServer()).executeIfPossible(() -> {
                    serverPlayer.openMenu(new SimpleMenuProvider(
                            (syncId, inv, p) -> new TradeHistoryMenu(syncId, inv, currentPage - 1),
                            Component.literal("Your Trade History")
                    ));
                });
                return;
            }

            if (slotId == NEXT_PAGE_SLOT && currentPage < maxPage) {
                Objects.requireNonNull(serverPlayer.level().getServer()).executeIfPossible(() -> {
                    serverPlayer.openMenu(new SimpleMenuProvider(
                            (syncId, inv, p) -> new TradeHistoryMenu(syncId, inv, currentPage + 1),
                            Component.literal("Your Trade History")
                    ));
                });
                return;
            }

            // Handle head clicks (only for slots inside the items area)
            if (slotId < pageTrades.size()) {
                TradeData targetSnapshot = pageTrades.get(slotId);
                TradeUi.openSnapshot(serverPlayer, targetSnapshot, new HistoryMenuDetails(currentPage, adminView, adminFilterPlayer));
                return;
            }

        }
    }

    @Override public boolean stillValid(Player player) { return true; }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
}
