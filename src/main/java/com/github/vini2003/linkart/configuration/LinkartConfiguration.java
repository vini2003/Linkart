package com.github.vini2003.linkart.configuration;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;

@Config(name = "linkart")
public class LinkartConfiguration implements ConfigData {
	@ConfigEntry.Gui.Tooltip
	public boolean enableLinker = true;

	@ConfigEntry.Gui.Tooltip
	public boolean enableChain = true;

	@ConfigEntry.Gui.Tooltip
	public boolean enableChunkLoading = false;

	@ConfigEntry.Gui.Tooltip
	public int pathfindingDistance = 8;

	@ConfigEntry.Gui.Tooltip
	public float velocityMultiplier = 0.5f;

	@ConfigEntry.Gui.Tooltip
	public int collisionDepth = 16;

	public boolean isLinkerEnabled() {
		return enableLinker;
	}

	public boolean isChainEnabled() {
		return enableChain;
	}

	public boolean isChunkLoadingEnabled() {
		return enableChunkLoading;
	}

	public int getPathfindingDistance() {
		return pathfindingDistance;
	}

	public float getVelocityMultiplier() {
		return velocityMultiplier;
	}

	public int getCollisionDepth() {
		return collisionDepth;
	}
}
