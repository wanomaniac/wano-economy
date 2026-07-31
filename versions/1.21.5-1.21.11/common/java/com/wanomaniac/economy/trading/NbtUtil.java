package com.wanomaniac.economy.trading;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.Set;

// Keys got a overhaul between 1.21 - 1.21.9 so we need to generalise it!
public class NbtUtil {
    public static ListTag LoadHistoryTag(CompoundTag rootTag){
        return rootTag.getList("Trades").get();
    }

    public static CompoundTag GetCompoundTag(ListTag listTag, int iteration){
        return listTag.getCompound(iteration).get();
    }

    public static Optional<Integer> getIntFromCompound(CompoundTag compoundTag, String key){
        return compoundTag.getInt(key);
    }

    public static Optional<ListTag> getListFromCompound(CompoundTag compoundTag, String key){
        return compoundTag.getList(key);
    }

    public static Optional<ListTag> getStringListFromCompound(CompoundTag compoundTag, String key){
        return compoundTag.getList(key);
    }

    public static Optional<String> getStringFromCompound(CompoundTag compoundTag, String key){
        return compoundTag.getString(key);
    }

    public static Optional<Long> getLongFromCompound(CompoundTag compoundTag, String key){
        return compoundTag.getLong(key);
    }

    public static Optional<CompoundTag> getCompoundFromCompound(CompoundTag compoundTag, String key){
        return compoundTag.getCompound(key);
    }

    public static Set<String> getKeySetFromCompound(CompoundTag compoundTag){
        return compoundTag.keySet();
    }

    public static boolean getCompoundBooleanOr(CompoundTag compoundTag, String key, boolean def){
        return compoundTag.getBoolean(key).get()|| def;
    }

    public static String getCompoundStringOr(CompoundTag compoundTag, String key, String def){
        Optional<String> value = compoundTag.getString(key);
        return value.orElse(def);
    }

    public static Optional<String> getTagAsString(Tag tag){
        return tag.asString();
    }
}
