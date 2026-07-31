package com.wanomaniac.economy;

import net.minecraft.resources.Identifier;

import static net.minecraft.resources.Identifier.isValidNamespace;
import static net.minecraft.resources.Identifier.isValidPath;

public class IdentifierUtils {
    // Converts your common ID to standard Minecraft Identifier
    public static Identifier toNative(ModIdentifier id) {
        return Identifier.fromNamespaceAndPath(id.namespace(), id.path());
    }

    // Converts standard Minecraft Identifier back to your common ID
    public static ModIdentifier fromNative(Identifier id) {
        return ModIdentifier.of(id.getNamespace(), id.getPath());
    }
}