package com.reazip.economycraft.fabric.trading.server;

import com.mojang.authlib.GameProfile;
import com.reazip.economycraft.fabric.trading.types.TradeMenuTypes;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static com.reazip.economycraft.fabric.server.InitalizerFabricServer.TRADE_MANAGER;

public class TradeMenuSnapshot extends AbstractContainerMenu {
    private final ItemStack statusPlayerNotReady = new ItemStack(Items.BLACK_STAINED_GLASS_PANE);
    private final ItemStack statusPlayerReady = new ItemStack(Items.WHITE_STAINED_GLASS_PANE);
    private final ItemStack ready = new ItemStack(Items.LIME_STAINED_GLASS_PANE);
    private final ItemStack notReady = new ItemStack(Items.RED_STAINED_GLASS_PANE);
    private final ItemStack redDivider = new ItemStack(Items.RED_STAINED_GLASS_PANE);

    private int readyButtonSlotId;

    // Horizontal layout Y positions
    private static final int TOP_OFFER_Y      = 18;
    private static final int DIVIDER_Y        = 36;
    private static final int OTHER_OFFER_Y    = 54;
    private static final int CONTROL_Y        = 72;
    private static final int PLAYER_INV_Y     = 90;
    private static final int HOTBAR_Y         = 148;

//    final boolean isA;
    final TradeData data;

    public TradeMenuSnapshot(int id, TradeData data) {
        super(TradeMenuTypes.TRADE_SNAPSHOT_MENU, id);
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

        ItemStack yourHead = createPlayerHead(TRADE_MANAGER.server, data.traders.get(0));
        ItemStack partnerHead = createPlayerHead(TRADE_MANAGER.server, data.traders.get(1));

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
                        statusPlayerReady.set(DataComponents.CUSTOM_NAME, Component.literal((getPlayerUsername(TRADE_MANAGER.server, data.traders.get(0))) + "'s status [Ready]"));
//                        statusPlayerNotReady.set(DataComponents.CUSTOM_NAME, Component.literal("You [Not Ready]"));
                         return  statusPlayerReady;
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
                        statusPlayerReady.set(DataComponents.CUSTOM_NAME, Component.literal((getPlayerUsername(TRADE_MANAGER.server, data.traders.get(1))) + "'s status [Ready]"));
                        return statusPlayerReady;
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
                    return Items.GRAY_STAINED_GLASS_PANE.getDefaultInstance();
                }
            });
        }
    }

    // -------------------------------
    // PLAYER HEAD CREATION
    // -------------------------------
    private ItemStack createPlayerHead(Player trader) {
        GameProfile gameProfile = trader.getGameProfile();
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);

        head.set(DataComponents.PROFILE, new ResolvableProfile(gameProfile));
        head.set(DataComponents.CUSTOM_NAME,
                Component.literal( trader.getName().getString()));

        return head;
    }

    private ItemStack createPlayerHead(MinecraftServer server, UUID playerUuid) {
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);

        // Resolve profile (may be cached, may hit Mojang services)
        server.getProfileCache()
                .get(playerUuid)
                .ifPresent(profile -> {
                    head.set(DataComponents.PROFILE, new ResolvableProfile(profile));

                    // Optional: set name from profile
                    head.set(
                            DataComponents.CUSTOM_NAME,
                            Component.literal(profile.getName())
                    );
                });

        return head;
    }

    private String getPlayerUsername(MinecraftServer server, UUID playerUuid) {
        AtomicReference<String> username = new AtomicReference<>("UNKNOWN");
        // Resolve profile (may be cached, may hit Mojang services)
        server.getProfileCache()
                .get(playerUuid)
                .ifPresent(profile -> {
                    username.set(profile.getName());
                });

        return username.get();
    }


    // -------------------------------
    // READY BUTTON CLICK
    // -------------------------------
    @Override
    public void clicked(int slot, int drag, ClickType type, Player player) {
        super.clicked(slot, drag, type, player);
    }

    @Override public boolean stillValid(Player player) { return true; }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

}