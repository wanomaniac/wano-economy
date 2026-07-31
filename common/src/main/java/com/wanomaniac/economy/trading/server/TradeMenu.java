package com.wanomaniac.economy.trading.server;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.TradeMenuSoundUtil;
import com.wanomaniac.economy.trading.PlayerInfoTools;
import com.wanomaniac.economy.trading.packets.msgs.SetClientMoneyTextboxStatusS2CPacket;
import com.wanomaniac.economy.trading.packets.msgs.SyncExtraMoneyPayloadS2CPacket;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
    private static final int TRADE_START      = 0;
    private static final int TRADE_END = 18;
    private static final int PLAYER_INV_START = 62;
    private static final int PLAYER_INV_END   = 88;
    private static final int HOTBAR_START     = 88;
    private static  final int HOTBAR_END       = 95;

    public TradeMenu(MenuType<?> menu, int id, Inventory playerInv, TradeSession session, boolean isA) {
        super(menu, id);
        this.session = session;
        this.isA = isA;

        Player partner = isA ? session.b : session.a;
        String partnerName = (partner != null) ? partner.getName().getString() : "Partner";

        ready.set(DataComponents.CUSTOM_NAME, Component.literal("Click to Unready"));
        notReady.set(DataComponents.CUSTOM_NAME, Component.literal("Click to Ready"));
        redDivider.set(DataComponents.CUSTOM_NAME, Component.literal(String.format("Border between you & %s", partnerName)));

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

        ItemStack yourHead = PlayerInfoTools.createPlayerHead(session.a.level().getServer(), isA ? session.a.getUUID() : session.b.getUUID());
        ItemStack partnerHead = PlayerInfoTools.createPlayerHead(session.a.level().getServer(), isA ? session.b.getUUID() : session.a.getUUID());

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
                    @Override public boolean mayPickup(Player p) { return false; }
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
    // READY BUTTON CLICK
    // -------------------------------
    @Override
    public void clicked(int slot, int drag, ClickType type, Player player) {
        Player expectedPlayer = isA ? session.a : session.b;
        if (player != expectedPlayer) {
            return;
        }

        // If the player clicks their own trade offer slots while they are ready, deny it
        if (slot >= 0 && slot < TRADE_END) {
            boolean amIReady = isA ? session.aReady : session.bReady;
            if (amIReady) {
                // Force full container sync to correct the client's visual ghost items
                this.sendAllDataToRemote();
                return;
            }
        }

        if (slot == readyButtonSlotId) {
            if (type != ClickType.PICKUP) {
                this.sendAllDataToRemote();
                return;
            }

            if (isA) session.aReady = !session.aReady;
            else session.bReady = !session.bReady;

            SetClientMoneyTextboxStatusS2CPacket sync = new SetClientMoneyTextboxStatusS2CPacket(
                    isA ? !session.aReady : !session.bReady
            ); // if ready, disabled, if not enable.
            CommonEconomy.packets.sendToPlayer(isA ? session.a : session.b, sync);
            boolean newReadyState = isA ? session.aReady : session.bReady;

            // Note 12 is F#4 (1.0 pitch), 24 is F#5 (2.0 pitch)
            int note = newReadyState ? 24 : 17;
            float pitch = (float) Math.pow(2.0, (note - 12) / 12.0);
            ServerPlayer clickingPlayer = isA ? session.a : session.b;
            if (clickingPlayer != null) {
                clickingPlayer.connection.send(new ClientboundSoundPacket(
                        SoundEvents.NOTE_BLOCK_HARP,     // Holder<SoundEvent>
                        TradeMenuSoundUtil.getFeedbackSoundSource(),                 // Or SoundSource.MASTER / PLAYERS
                        clickingPlayer.getX(),
                        clickingPlayer.getY(),
                        clickingPlayer.getZ(),
                        1.0F,                           // Volume
                        pitch,                          // Pitch
                        clickingPlayer.getRandom().nextLong() // Seed
                ));
            }

            int partnerNote = newReadyState ? 20 : 13; // Distinct G#5 for ready, G4 for unready
            float partnerPitch = (float) Math.pow(2.0, (partnerNote - 12) / 12.0);
            ServerPlayer partnerPlayer = isA ? session.b : session.a;
            if (partnerPlayer != null) {
                partnerPlayer.connection.send(new ClientboundSoundPacket(
                        SoundEvents.NOTE_BLOCK_BELL,
                        TradeMenuSoundUtil.getFeedbackSoundSource(),
                        partnerPlayer.getX(),
                        partnerPlayer.getY(),
                        partnerPlayer.getZ(),
                        0.8F,
                        partnerPitch,
                        partnerPlayer.getRandom().nextLong()
                ));
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

        SyncExtraMoneyPayloadS2CPacket sync = new SyncExtraMoneyPayloadS2CPacket(
                id,
                amount
        );

        if (playerA != null) {
            CommonEconomy.packets.sendToPlayer(playerA, sync);
        }
        if (playerB != null) {
            CommonEconomy.packets.sendToPlayer(playerB, sync);
        }
    }

    @Override public boolean stillValid(Player player) { return true; }

    @Override
    public void removed(Player player) {
        super.removed(player);
        session.cancel(TradeCancelReason.MANUAL_CLOSE);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {

        boolean amIReady = isA ? session.aReady : session.bReady;
        if (amIReady) {
            return ItemStack.EMPTY; // Stop all shift-click movements while readied up
        }

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
