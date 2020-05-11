package com.github.vini2003.linkart.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class LinkartLinkerRegistry {
    public static final LinkartLinkerRegistry INSTANCE = new LinkartLinkerRegistry();
    private static final BiMap<EntityType<?>, Collection<Item>> ENTRIES = HashBiMap.create();

    static {
        INSTANCE.register(EntityType.MINECART, LinkartItems.LINKER_ITEM);
        INSTANCE.register(EntityType.CHEST_MINECART, LinkartItems.LINKER_ITEM);
        INSTANCE.register(EntityType.FURNACE_MINECART, LinkartItems.LINKER_ITEM);
        INSTANCE.register(EntityType.HOPPER_MINECART, LinkartItems.LINKER_ITEM);
        INSTANCE.register(EntityType.TNT_MINECART, LinkartItems.LINKER_ITEM);
        INSTANCE.register(EntityType.SPAWNER_MINECART, LinkartItems.LINKER_ITEM);
        INSTANCE.register(EntityType.COMMAND_BLOCK_MINECART, LinkartItems.LINKER_ITEM);
    }

    private LinkartLinkerRegistry() {
    }

    public static void initialize() {
        // NO-OP
    }

    public Collection<EntityType<?>> getKeys() {
        return ENTRIES.keySet();
    }

    public Collection<Item> getValues() {
        Set<Item> values = new HashSet<>();
        ENTRIES.forEach((entry, items) -> values.addAll(items));
        return values;
    }

    public Collection<Item> getByKey(EntityType<?> key) {
        return ENTRIES.get(key);
    }

    public EntityType<?> getByValue(Collection<Item> value) {
        return ENTRIES.inverse().get(value);
    }

    public void register(EntityType<?> key, Item value) {
        if (ENTRIES.get(key) == null) {
            ENTRIES.put(key, new HashSet<>());
        }
        ENTRIES.get(key).add(value);
    }
}
