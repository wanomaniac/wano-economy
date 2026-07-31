package com.wanomaniac.economy.auctioning.server;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.auctioning.packets.msgs.ConfirmBiddingSelectionS2CPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class AuctioneerMenu extends AbstractContainerMenu {
    AuctionSession session;

    public AuctioneerMenu(int containerId, AuctionSession session, Inventory playerInventory) {
        this(null, session, containerId, playerInventory);
    }

    public AuctioneerMenu(@Nullable MenuType<?> menuType, AuctionSession session, int containerId, Inventory playerInventory) {
        super(menuType, containerId);

//        // 1. Add Main Player Inventory (3 rows x 9 columns = 27 slots)
//        for (int row = 0; row < 3; ++row) {
//            for (int col = 0; col < 9; ++col) {
//                // Index starts at 9 (skip hotbar 0-8), mapped to standard inventory GUI coordinates
//                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
//            }
//        }
//
//        // 2. Add Player Hotbar (9 slots)
//        for (int col = 0; col < 9; ++col) {
//            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
//        }

        int startY = 50;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                // Formula: base_y + (row * 18)
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, startY + row * 18));
            }
        }

        // Original Y was 142 -> Changed to 108 (Shifted up by the same 34 pixels to maintain gap)
        int hotbarY = startY + (3 * 18) + 4; // 50 + 54 + 4 = 108
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, hotbarY));
        }
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        // To-Do
        // Tell the client screen the item details, then let it handle the confirmation
        // Once the client screen does this, the menu won't be rendered for the time being.
        if (slotId < 0 || slotId >= this.slots.size()) {
            super.clicked(slotId, button, clickType, player);
            return;
        }

        Slot slot = this.slots.get(slotId);

        // 2. Only process on the server side & ensure the clicked slot has a valid item
        if (!player.level().isClientSide() && slot.hasItem()) {
            ItemStack selectedItem = slot.getItem();
            CommonEconomy.packets.sendToPlayer((ServerPlayer) player, new ConfirmBiddingSelectionS2CPacket(selectedItem));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isAlive();
    }
}
