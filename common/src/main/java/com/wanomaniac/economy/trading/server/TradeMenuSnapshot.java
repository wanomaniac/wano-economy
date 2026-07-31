package com.wanomaniac.economy.trading.server;

import com.wanomaniac.economy.ServerTaskSchedulingUtil;
import com.wanomaniac.economy.trading.PlayerInfoTools;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
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
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.wanomaniac.economy.ServerEconomy.TRADE_MANAGER;

public class TradeMenuSnapshot extends AbstractContainerMenu {
    private final ItemStack statusPlayerNotReady = new ItemStack(Items.BLACK_STAINED_GLASS_PANE);
    private final ItemStack statusPlayerReady = new ItemStack(Items.WHITE_STAINED_GLASS_PANE);
    private final ItemStack ready = new ItemStack(Items.LIME_STAINED_GLASS_PANE);
    private final ItemStack notReady = new ItemStack(Items.RED_STAINED_GLASS_PANE);
    private final ItemStack redDivider = new ItemStack(Items.RED_STAINED_GLASS_PANE);

    private int readyButtonSlotId;

    // Horizontal layout Y positions
    // Horizontal layout Y positions
    private static final int TOP_OFFER_Y      = 18;
    private static final int DIVIDER_Y        = 36;
    private static final int OTHER_OFFER_Y    = 54;
    private static final int CONTROL_Y        = 72;
    private static final int PLAYER_INV_Y     = 90;
    private static final int HOTBAR_Y         = 148;
    private static final int TRADE_START      = 0;
    private static final int TRADE_END = 18;
    private static final int PLAYER_INV_START = 62;
    private static final int PLAYER_INV_END   = 88;
    private static final int HOTBAR_START     = 88;
    private static  final int HOTBAR_END       = 95;
    private HistoryMenuDetails historyMenuDetails = null;
//    final boolean isA;
    final TradeData data;

    public void setLongGuestState(boolean state){
        data.setGuestLongEscape(state);
    }

    public TradeMenuSnapshot(MenuType<?> menuType, int id, Inventory playerInv, TradeData data) {
        this(menuType, id, playerInv, data, null);
    }

    public TradeMenuSnapshot(MenuType<?> menuType, int id, Inventory playerInv, TradeData data, HistoryMenuDetails historyMenuDetails) {
        super(menuType, id);
        if(historyMenuDetails != null){
            this.historyMenuDetails = historyMenuDetails;
        }
        this.data = data;
//        this.isA = player.getUUID() == data.getTraders().getFirst();

        ready.set(DataComponents.CUSTOM_NAME, Component.literal("Click to Ready"));
        notReady.set(DataComponents.CUSTOM_NAME, Component.literal("Not Ready"));
        redDivider.set(DataComponents.CUSTOM_NAME, Component.literal("Divider"));

        // -------------------------------
        // 18-SLOT HORIZONTAL PLAYER OFFER
        // -------------------------------
        for (int i = 0; i < 18; i++) {
            this.addSlot(new Slot(data.tradeOffers.get(data.traders.getFirst()),
                    i,
                    8 + i * 18,
                    TOP_OFFER_Y){
                @Override public boolean mayPlace(ItemStack stack) { return false; }
                @Override public boolean mayPickup(Player p) { return false; }
            });
        }

        // -------------------------------
        // RED DIVIDER (9 WIDE)
        // -------------------------------
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(new SimpleContainer(1), 0,
                    8 + i * 18,
                    DIVIDER_Y
            ) {
                @Override public boolean mayPlace(ItemStack stack) { return false; }
                @Override public boolean mayPickup(Player player) { return false; }
                @Override public @NotNull ItemStack getItem() { return redDivider; }
            });
        }

        // -------------------------------
        // OTHER PLAYER READ-ONLY 18-SLOT OFFER
        // -------------------------------
        SimpleContainer otherOffer = data.tradeOffers.get(data.traders.get(1));

        for (int i = 0; i < 18; i++) {
            this.addSlot(new Slot(otherOffer,
                    i,
                    8 + i * 18,
                    OTHER_OFFER_Y
            ) {
                @Override public boolean mayPlace(ItemStack stack) { return false; }
                @Override public boolean mayPickup(Player p) { return false; }
            });
        }

        // -------------------------------
        // CONTROL PANEL (9 WIDE)
        // -------------------------------
        // Fill with gray
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(new SimpleContainer(1), 0,
                    8 + i * 18,
                    CONTROL_Y
            ) {
                @Override public boolean mayPlace(ItemStack stack) { return false; }
                @Override public boolean mayPickup(Player player) { return false; }
                @Override public @NotNull ItemStack getItem() {
                    return Items.GRAY_STAINED_GLASS_PANE.getDefaultInstance();
                }
            });
        }


        int center = 4;

        ItemStack yourHead = PlayerInfoTools.createPlayerHead(TRADE_MANAGER.server, data.traders.get(0));
        ItemStack partnerHead = PlayerInfoTools.createPlayerHead(TRADE_MANAGER.server, data.traders.get(1));

        for (int i = 0; i < 9; i++) {
            int x = 8 + i * 18;

            // ---------------------------
            // 0 → your head (left side)
            // ---------------------------
            if (i == 0) {
                this.addSlot(new Slot(new SimpleContainer(1), 0, x, CONTROL_Y) {
                    @Override public boolean mayPlace(ItemStack s) { return false; }
                    @Override public boolean mayPickup(Player p) { return false; }
                    @Override public @NotNull ItemStack getItem() { return yourHead; }
                });
                continue;
            }

            // ---------------------------
            // 8 → partner head (right side)
            // ---------------------------
            if (i == 8) {
                this.addSlot(new Slot(new SimpleContainer(1), 0, x, CONTROL_Y) {
                    @Override public boolean mayPlace(ItemStack s) { return false; }
                    @Override public boolean mayPickup(Player p) { return false; }
                    @Override public @NotNull ItemStack getItem() { return partnerHead; }
                });
                continue;
            }

            // ---------------------------
            // i == center - 2 → your ready state
            // ---------------------------
            if (i == center - 2) {
                this.addSlot(new Slot(new SimpleContainer(1), 0, x, CONTROL_Y) {
                    @Override public boolean mayPlace(ItemStack s) { return false; }
                    @Override public boolean mayPickup(Player p) { return false; }
                    @Override public @NotNull ItemStack getItem() {
                        if(data.cancelled){
                            statusPlayerNotReady.set(DataComponents.CUSTOM_NAME, Component.literal((TradeUtils.getPlayerUsername(TRADE_MANAGER.server, data.traders.get(0))) + "'s status [Not Ready]"));
                        } else {
                            statusPlayerReady.set(DataComponents.CUSTOM_NAME, Component.literal((TradeUtils.getPlayerUsername(TRADE_MANAGER.server, data.traders.get(0))) + "'s status [Ready]"));
                        }

                         return data.cancelled ? statusPlayerNotReady : statusPlayerReady;
                    }
                });
                continue;
            }

            // ---------------------------
            // i == center → clickable ready button
            // ---------------------------
            if (i == center) {
                readyButtonSlotId = this.slots.size();
                this.addSlot(new Slot(new SimpleContainer(1), 1, x, CONTROL_Y) {
                    @Override public boolean mayPlace(ItemStack s) { return false; }
                    @Override public boolean mayPickup(Player p) { return false; }
                    @Override public @NotNull ItemStack getItem() {
                        return ready;
                    }
                });
                continue;
            }

            // ---------------------------
            // i == center + 2 → partner ready state
            // ---------------------------
            if (i == center + 2) {
                this.addSlot(new Slot(new SimpleContainer(1), 2, x, CONTROL_Y) {
                    @Override public boolean mayPlace(ItemStack s) { return false; }
                    @Override public boolean mayPickup(Player p) { return false; }
                    @Override public @NotNull ItemStack getItem() {
                        if(data.cancelled){
                            statusPlayerNotReady.set(DataComponents.CUSTOM_NAME, Component.literal((TradeUtils.getPlayerUsername(TRADE_MANAGER.server, data.traders.get(1))) + "'s status [Not Ready]"));
                        } else {
                            statusPlayerReady.set(DataComponents.CUSTOM_NAME, Component.literal((TradeUtils.getPlayerUsername(TRADE_MANAGER.server, data.traders.get(1))) + "'s status [Ready]"));
                        }

                        return data.cancelled ? statusPlayerNotReady : statusPlayerReady;
                    }
                });
                continue;
            }

            // ---------------------------
            // EVERYTHING ELSE → filler glass
            // ---------------------------
            this.addSlot(new Slot(new SimpleContainer(1), 0, x, CONTROL_Y) {
                @Override public boolean mayPlace(ItemStack s) { return false; }
                @Override public boolean mayPickup(Player p) { return false; }
                @Override public @NotNull ItemStack getItem() {
                    ItemStack item = Items.GRAY_STAINED_GLASS_PANE.getDefaultInstance();
                    item.set(DataComponents.CUSTOM_NAME, Component.literal(""));
                    return item;
                }
            });

        }


        // -------------------------------
        // PLAYER INVENTORY (3 ROWS)
        // -------------------------------
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                this.addSlot(new Slot(playerInv, x + y * 9 + 9, 8 + x * 18, PLAYER_INV_Y + y * 18));
            }
        }

        // -------------------------------
        // HOTBAR
        // -------------------------------
        for (int x = 0; x < 9; x++) {
            this.addSlot(new Slot(playerInv,
                    x,
                    8 + x * 18,
                    HOTBAR_Y));
        }
    }






    // -------------------------------
    // READY BUTTON CLICK
    // -------------------------------
    @Override
    public void clicked(int slot, int drag, ClickType type, Player player) {
        return;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player instanceof ServerPlayer serverPlayer && historyMenuDetails != null) {
            boolean shouldOpenHistory = this.data.getGuestLongEscape();
            ServerTaskSchedulingUtil.Schedule(Objects.requireNonNull(serverPlayer.level().getServer()), new TickTask( // opening a menu mid closing is a bad idea, do it later!
                    serverPlayer.level().getServer().getTickCount() + 1,
                    () -> {
                        if (serverPlayer.connection.isAcceptingMessages() && serverPlayer.isAlive() && !shouldOpenHistory) {
                            serverPlayer.openMenu(new SimpleMenuProvider(
                                    (syncId, inv, p) -> new TradeHistoryMenu(syncId, inv, historyMenuDetails),
                                    Component.literal(historyMenuDetails.asAdmin() ? "Server Trade History" : "Your Trade History")
                            ));
                        }
                    }
            ));
        }
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return false;
    }

    @Override public boolean stillValid(Player player) { return true; }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

}