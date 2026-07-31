package com.wanomaniac.economy.interfaces;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;

public interface IPlatformRegistryInit {
    /**
     * Registers for the platform's registry.
     */
    void register();
}
