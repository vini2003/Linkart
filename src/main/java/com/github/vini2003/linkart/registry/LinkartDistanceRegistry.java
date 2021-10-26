package com.github.vini2003.linkart.registry;

import net.minecraft.entity.EntityType;

import java.util.Collection;
import java.util.HashMap;

public class LinkartDistanceRegistry {
	public static final LinkartDistanceRegistry INSTANCE = new LinkartDistanceRegistry();
	private static final HashMap<EntityType<?>, Double> DISTANCES = new HashMap<>();

	static {
		INSTANCE.register(EntityType.MINECART, 1.0);
		INSTANCE.register(EntityType.CHEST_MINECART, 1.0);
		INSTANCE.register(EntityType.FURNACE_MINECART, 1.0);
		INSTANCE.register(EntityType.HOPPER_MINECART, 1.0);
		INSTANCE.register(EntityType.TNT_MINECART, 1.0);
		INSTANCE.register(EntityType.SPAWNER_MINECART, 1.0);
		INSTANCE.register(EntityType.COMMAND_BLOCK_MINECART, 1.0);
	}

	private LinkartDistanceRegistry() {
	}

	public static void initialize() {
		// NO-OP
	}

	public Collection<EntityType<?>> getKeys() {
		return DISTANCES.keySet();
	}

	public Collection<Double> getValues() {
		return DISTANCES.values();
	}

	public Double getByKey(EntityType<?> key) {
		return DISTANCES.get(key);
	}

	public EntityType<?> getByValue(Double value) {
		throw new UnsupportedOperationException("Cannot retrieve registry entry by key in registry whose values can be duplicate!");
	}

	public void register(EntityType<?> key, Double value) {
		DISTANCES.put(key, value);
	}
}
