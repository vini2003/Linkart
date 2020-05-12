package com.github.vini2003.linkart.utility;

import com.github.vini2003.linkart.registry.LinkartDistanceRegistry;
import net.minecraft.block.*;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableDouble;

import java.util.*;

public class RailUtils {
	public static Pair<BlockPos, MutableDouble> getNextRail(AbstractMinecartEntity next, AbstractMinecartEntity previous) {
		BlockPos entityPosition = next.getBlockPos();
		BlockPos finalPosition = previous.getBlockPos();

		if (!(next.world.getBlockState(next.getBlockPos()).getBlock() instanceof AbstractRailBlock)) return null;

		RailPlacementHelper helper = new RailPlacementHelper(next.world, next.getBlockPos(), next.world.getBlockState(next.getBlockPos()));

		for (BlockPos initialPosition : helper.getNeighbors()) {
			Set<BlockPos> cache = new HashSet<>();

			cache.add(entityPosition);

			MutableDouble distance = new MutableDouble(0);

			if (step(next.world, cache, initialPosition, finalPosition, distance)) {
				return new Pair<>(initialPosition, distance);
			}
		}

		return null;
	}

	public static Vec3d getNextVelocity(AbstractMinecartEntity entityA, AbstractMinecartEntity entityB) {
		Pair<BlockPos, MutableDouble> pair = getNextRail(entityA, entityB);

		double maximumDistance = 0.75; // = Math.max(LinkartDistanceRegistry.INSTANCE.getByKey(entityA.getType()), LinkartDistanceRegistry.INSTANCE.getByKey(entityB.getType()));

		if (pair == null && entityA.getPos().distanceTo(entityB.getPos()) > maximumDistance) {
			return new Vec3d(entityB.getX() - entityA.getX(), entityB.getY() - entityA.getY(), entityB.getZ() - entityA.getZ());
		} else if (pair == null) {
			return Vec3d.ZERO;
		}

		BlockPos position = pair.getLeft();
		double distance = pair.getRight().getValue();

		distance += (entityA.getX() - entityA.getBlockPos().getX()) - (entityB.getX() - entityB.getBlockPos().getX());
		distance += (entityA.getZ() - entityA.getBlockPos().getZ()) - (entityB.getZ() - entityB.getBlockPos().getZ());


		Vec3d velocity = Vec3d.ZERO;

		if (distance > maximumDistance) {
			if (position.getX() > entityA.getBlockPos().getX()) {
				velocity = new Vec3d(velocity.x + .5, velocity.y, velocity.z);
			} else if (position.getX() < entityA.getBlockPos().getX()) {
				velocity = new Vec3d(velocity.x - .5, velocity.y, velocity.z);
			}
			if (position.getY() > entityA.getBlockPos().getY()) {
				velocity = new Vec3d(velocity.x, velocity.y - .5, velocity.z);
			} else if (position.getY() < entityA.getBlockPos().getY()) {
				velocity = new Vec3d(velocity.x, velocity.y + .5, velocity.z);
			}
			if (position.getZ() > entityA.getBlockPos().getZ()) {
				velocity = new Vec3d(velocity.x, velocity.y, velocity.z + .5);
			} else if (position.getZ() < entityA.getBlockPos().getZ()) {
				velocity = new Vec3d(velocity.x, velocity.y, velocity.z - .5);
			}
		}

		return velocity;
	}

	public static boolean step(World world, Set<BlockPos> cache, BlockPos currentPosition, BlockPos finalPosition, MutableDouble distance) {
		BlockState state = world.getBlockState(currentPosition);

		if (!(state.getBlock() instanceof AbstractRailBlock)) return false;

		RailPlacementHelper helper = new RailPlacementHelper(world, currentPosition, state);

		if (distance.getValue() > 8) return false;
		if (currentPosition.equals(finalPosition)) return true;

		cache.add(currentPosition);

		if (!cache.contains(helper.getNeighbors().get(0))) {
			distance.getAndIncrement();
			return step(world, cache, helper.getNeighbors().get(0), finalPosition, distance);
		} else if (!cache.contains(helper.getNeighbors().get(1))) {
			distance.getAndIncrement();
			return step(world, cache, helper.getNeighbors().get(1), finalPosition, distance);
		}

		return false;
	}
}
