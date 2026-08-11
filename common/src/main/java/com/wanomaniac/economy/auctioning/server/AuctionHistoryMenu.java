package com.wanomaniac.economy.auctioning.server;

import com.wanomaniac.economy.ServerEconomy;
import com.wanomaniac.economy.auctioning.client.AuctionUi;
import com.wanomaniac.economy.trading.PlayerInfoTools;
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

public class AuctionHistoryMenu extends AbstractContainerMenu {
    private static final int ROW_COUNT = 6;
    private static final int TOTAL_SLOTS = ROW_COUNT * 9; // 54 slots
    private static final int ITEMS_PER_PAGE = 45; // First 5 rows for player heads

    // Bottom row slot IDs
    private static final int PREV_PAGE_SLOT = 45; // Row 6, Col 1
    private static final int NEXT_PAGE_SLOT = 53; // Row 6, Col 9

    private final List<AuctionSession> pageTrades = new ArrayList<>();
    private int currentPage;
    private final int maxPage;

    public AuctionHistoryMenu(int id, Inventory playerInv, int page) {
        this(id, playerInv, new HistoryMenuDetails(page));
    }

    public AuctionHistoryMenu(int id, Inventory playerInv, HistoryMenuDetails details) {
        super(MenuType.GENERIC_9x6, id);
        this.currentPage = details.lastPage();

        Player player = playerInv.player;

        List<AuctionSession> allRelevantAuctions = ServerEconomy.AUCTION_MANAGER.snapshots.stream()
                    .toList();

        this.maxPage = Math.max(0, (allRelevantAuctions.size() - 1) / ITEMS_PER_PAGE);

        SimpleContainer container = new SimpleContainer(TOTAL_SLOTS);
        int startIndex = currentPage * ITEMS_PER_PAGE;

        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            int tradeIndex = startIndex + i;
            if (tradeIndex >= allRelevantAuctions.size()) break;

            AuctionSession session = allRelevantAuctions.get(tradeIndex);
            pageTrades.add(session);

            UUID partnerUuid = session.auctioneerID;
            ItemStack head = PlayerInfoTools.createPlayerHead(player.level().getServer(), partnerUuid);
            container.setItem(i, head);
        }

        if(allRelevantAuctions.size() > ITEMS_PER_PAGE) {
            if (currentPage > 0) {
                ItemStack prevArrow = new ItemStack(Items.ARROW);
                prevArrow.set(DataComponents.CUSTOM_NAME, Component.literal("Previous Page (Page " + currentPage + ")"));
                container.setItem(PREV_PAGE_SLOT, prevArrow);
            }

            if (allRelevantAuctions.size() > ITEMS_PER_PAGE * (currentPage + 1)) {
                if (currentPage < maxPage) {
                    ItemStack nextArrow = new ItemStack(Items.ARROW);
                    nextArrow.set(DataComponents.CUSTOM_NAME, Component.literal("Next Page (Page " + (currentPage + 2) + ")"));
                    container.setItem(NEXT_PAGE_SLOT, nextArrow);
                }
            }
            for (int i = 45; i < TOTAL_SLOTS; i++) {
                if (i != PREV_PAGE_SLOT && i != NEXT_PAGE_SLOT) {
                    ItemStack glass = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
                    glass.set(DataComponents.CUSTOM_NAME, Component.literal("Page " + (currentPage + 1) + " of " + (maxPage + 1)));
                    container.setItem(i, glass);
                }
            }
        } else {
            if(currentPage > 0) currentPage = 0;
        }

        for (int row = 0; row < ROW_COUNT; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(container, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }
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
                            (syncId, inv, p) -> new AuctionHistoryMenu(syncId, inv, currentPage - 1),
                            Component.literal("Your Trade History")
                    ));
                });
                return;
            }

            if (slotId == NEXT_PAGE_SLOT && currentPage < maxPage) {
                Objects.requireNonNull(serverPlayer.level().getServer()).executeIfPossible(() -> {
                    serverPlayer.openMenu(new SimpleMenuProvider(
                            (syncId, inv, p) -> new AuctionHistoryMenu(syncId, inv, currentPage + 1),
                            Component.literal("Your Trade History")
                    ));
                });
                return;
            }

            // Handle head clicks (only for slots inside the items area)
            if (slotId < pageTrades.size()) {
                AuctionSession targetSnapshot = pageTrades.get(slotId);
                AuctionUi.openSnapshot(serverPlayer, targetSnapshot, new HistoryMenuDetails(currentPage));
                return;
            }

        }
    }

    @Override public boolean stillValid(Player player) { return true; }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
}
