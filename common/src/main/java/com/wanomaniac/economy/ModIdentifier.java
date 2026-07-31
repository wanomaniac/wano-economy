package com.wanomaniac.economy;

import java.util.Locale;

// [1.21.11] this exists cuz some fuckhead changed ResourceLocation to Identifier and now its a disaster.
public record ModIdentifier(String namespace, String path) {
    private static boolean isValidNamespace(String namespace) {
        return namespace.chars().allMatch(c -> c == '_' || c == '-' || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '.');
    }

    private static boolean isValidPath(String path) {
        return path.chars().allMatch(c -> c == '_' || c == '-' || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '/' || c == '.');
    }

    public static ModIdentifier withMojangNamespace(String path){
        return ModIdentifier.fromNamespaceAndPath("minecraft", path);
    }

    public static ModIdentifier fromNamespaceAndPath(String namespace, String path) {
        if (!isValidNamespace(namespace) || !isValidPath(path)) {
            throw new IllegalArgumentException(
                    "Invalid ModIdentifier characters! Namespace: '" + namespace + "', Path: '" + path + "'"
            );
        }
        return new ModIdentifier(namespace, path);
    }

    public static ModIdentifier of(String namespace, String path) {
        return new ModIdentifier(namespace, path);
    }

    public static ModIdentifier parse(String fullId) {
        String[] parts = fullId.split(":");
        return new ModIdentifier(parts[0], parts[1]);
    }

    public static ModIdentifier tryParse(String location) {
        if (location == null || location.isEmpty()) {
            return null;
        }

        String[] parts = location.split(":", 2);
        String namespace = parts.length > 1 ? parts[0] : "minecraft"; // Default to "minecraft" if no colon
        String path = parts.length > 1 ? parts[1] : parts[0];

        // Validate namespace and path characters (standard alphanumeric + '_', '-', '.', '/')
        if (!isValidNamespace(namespace) || !isValidPath(path)) {
            return null;
        }

        return new ModIdentifier(namespace, path);
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }
}