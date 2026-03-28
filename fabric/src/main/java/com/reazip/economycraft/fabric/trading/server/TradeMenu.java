package com.reazip.economycraft.fabric.trading.server;

import com.mojang.authlib.GameProfile;
import com.reazip.economycraft.fabric.trading.packets.msgs.SetClientMoneyTextboxStatusS2CP;
import com.reazip.economycraft.fabric.trading.packets.msgs.SyncExtraMoneyPayloadS2CP;
import com.reazip.economycraft.fabric.trading.types.TradeMenuTypes;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

// Server sided
public class TradeMenu extends AbstractContainerMenu {
    public TradeSession session = null;
    public boolean isA = false;

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


    public TradeMenu(int id, Inventory playerInv, TradeSession session, boolean isA) {
        super(TradeMenuTypes.TRADE_MENU, id);
        this.session = session;
        this.isA = isA;

        ready.set(DataComponents.CUSTOM_NAME, Component.literal("Click to Ready"));
        notReady.set(DataComponents.CUSTOM_NAME, Component.literal("Not Ready"));
        redDivider.set(DataComponents.CUSTOM_NAME, Component.literal("Divider"));

        // -------------------------------
        // 18-SLOT HORIZONTAL PLAYER OFFER
        // -------------------------------
        for (int i = 0; i < 18; i++) {
            this.addSlot(new Slot(isA ? session.aOffer : session.bOffer,
                    i,
                    8 + i * 18,
                    TOP_OFFER_Y));
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
        SimpleContainer otherOffer = isA ? session.bOffer : session.aOffer;

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

        ItemStack yourHead = createPlayerHead(isA ? session.a : session.b);
        ItemStack partnerHead = createPlayerHead(isA ? session.b : session.a);

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
                        statusPlayerReady.set(DataComponents.CUSTOM_NAME, Component.literal("You [Ready]"));
                        statusPlayerNotReady.set(DataComponents.CUSTOM_NAME, Component.literal("You [Not Ready]"));
                        return (isA ? session.aReady : session.bReady) ? statusPlayerReady : statusPlayerNotReady;
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
                    @Override public boolean mayPickup(Player p) { return true; }
                    @Override public @NotNull ItemStack getItem() {
                        return (isA ? session.aReady : session.bReady) ? ready : notReady;
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
                        statusPlayerReady.set(DataComponents.CUSTOM_NAME, Component.literal((isA ? session.b.getName().getString() : session.a.getName().getString()) + "'s status [Ready]"));
                        statusPlayerNotReady.set(DataComponents.CUSTOM_NAME, Component.literal((isA ? session.b.getName().getString() : session.a.getName().getString()) + "'s status [Not Ready]"));
                        return (isA ? session.bReady : session.aReady) ? statusPlayerReady : statusPlayerNotReady;
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

    // -------------------------------
    // READY BUTTON CLICK
    // -------------------------------
    @Override
    public void clicked(int slot, int drag, ClickType type, Player player) {
        if (slot == readyButtonSlotId && type == ClickType.PICKUP) {
            if (isA) session.aReady = !session.aReady;
            else session.bReady = !session.bReady;

            SetClientMoneyTextboxStatusS2CP sync = new SetClientMoneyTextboxStatusS2CP(
                    isA ? !session.aReady : !session.bReady
            ); // if ready, disabled, if not enable.

            ServerPlayNetworking.send(isA ? session.a : session.b, sync);

            int note = 21; // D♯5
            float pitch = (float) Math.pow(2.0, (note - 12) / 12.0);
            if(isA) {
                session.a.playSound(
                        SoundEvents.NOTE_BLOCK_HARP.value(),
                        1.0F,
                        pitch
                );
            } else {
                session.b.playSound(
                        SoundEvents.NOTE_BLOCK_HARP.value(),
                        1.0F,
                        pitch
                );
            }
            session.menuA.broadcastChanges();
            session.menuB.broadcastChanges();
            session.tryComplete();
            return;
        }
        super.clicked(slot, drag, type, player);
    }

    public void updateExtraMoney(UUID id, long amount){
        if (isA) {
            if (id.equals(session.a.getUUID())) {
                session.setMoneyA(amount);
            } else {
                session.setMoneyB(amount);
            }
        } else {
            if (id.equals(session.b.getUUID())) {
                session.setMoneyB(amount);
            } else {
                session.setMoneyA(amount);
            }
        }

        var playerA = session.a;
        var playerB = session.b; // may be null if one side not present

        SyncExtraMoneyPayloadS2CP sync = new SyncExtraMoneyPayloadS2CP(
                id,
                amount
        );

        if (playerA != null) {
            ServerPlayNetworking.send(playerA, sync);
        }
        if (playerB != null) {
            ServerPlayNetworking.send(playerB, sync);
        }
    }

    @Override public boolean stillValid(Player player) { return true; }

    @Override
    public void removed(Player player) {
        super.removed(player);
        session.cancel();
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        // INDEXES
        final int TRADE_START      = 0;
        final int TRADE_END        = 18;

        final int PLAYER_INV_START = 61;
        final int PLAYER_INV_END   = 88;

        final int HOTBAR_START     = 88;
        final int HOTBAR_END       = 95;

        ItemStack empty = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return empty;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        // FROM TRADE -> PLAYER INVENTORY + HOTBAR
        if (index >= 0 && index < TRADE_END) {
            if (!moveItemStackTo(stack, PLAYER_INV_START, HOTBAR_END, false)) {
                return empty;
            }
        }
        // FROM PLAYER INVENTORY -> TRADE
        else if (index >= PLAYER_INV_START && index < HOTBAR_END) {
            if (!moveItemStackTo(stack, TRADE_START, TRADE_END, false)) {
                return empty;
            }
        } else {
            return empty; // everything else is read-only/filler
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return copy;
    }

}
