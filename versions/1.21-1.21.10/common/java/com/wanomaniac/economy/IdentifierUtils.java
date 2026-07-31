package com.wanomaniac.economy;

import net.minecraft.resources.ResourceLocation;


public class IdentifierUtils {
    // Converts your common ID to standard Minecraft Identifier
    public static ResourceLocation toNative(ModIdentifier id) {
        return ResourceLocation.fromNamespaceAndPath(id.namespace(), id.path());
    }

    // Converts standard Minecraft Identifier back to your common ID
    public static ModIdentifier fromNative(ResourceLocation id) {
        return ModIdentifier.of(id.getNamespace(), id.getPath());
    }
}