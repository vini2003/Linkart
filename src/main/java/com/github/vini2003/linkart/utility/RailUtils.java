package com.github.vini2003.linkart.utility;

import com.github.vini2003.linkart.registry.LinkartConfigurations;
import com.github.vini2003.linkart.registry.LinkartDistanceRegistry;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableDouble;

import java.util.*;

public class RailUtils {
	public static Pair<BlockPos, MutableDouble> getNextRail(AbstractMinecartEntity next, AbstractMinecartEntity previous) {
		Optional.ofNullable(next.getPassengerList().size() >= 1 ? next.getPassengerList().get(0) : null).ifPresent(entity -> {
			entity.yaw = next.yaw;
			entity.pitch = next.pitch;

			entity.prevYaw = next.yaw;
			entity.prevPitch = next.pitch;
		});

		Optional.ofNullable(previous.getPassengerList().size() >= 1 ? previous.getPassengerList().get(0) : null).ifPresent(entity -> {
			entity.yaw = previous.yaw;
			entity.pitch = previous.pitch;

			entity.prevYaw = previous.yaw;
			entity.prevPitch = previous.pitch;
		});


		BlockPos entityPosition = next.getBlockPos();
		BlockPos finalPosition = previous.getBlockPos();

		BlockState state = next.world.getBlockState(next.getBlockPos());

		if (!(state.getBlock() instanceof AbstractRailBlock)) return null;

		List<BlockPos> fuck = (List<BlockPos>) getNeighbors(next.getBlockPos(), state.get(((AbstractRailBlock) state.getBlock()).getShapeProperty()));

		for (BlockPos initialPosition : fuck) {
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

		double maximumDistance = Math.max(LinkartDistanceRegistry.INSTANCE.getByKey(entityA.getType()), LinkartDistanceRegistry.INSTANCE.getByKey(entityB.getType()));

		if (pair == null && entityA.world.getDimensionRegistryKey() == entityB.world.getDimensionRegistryKey()) {
			return new Vec3d(entityB.getX() - entityA.getX(), entityB.getY() - entityA.getY(), entityB.getZ() - entityA.getZ());
		} else if (pair == null) {
			return entityB.getVelocity();
		}

		BlockPos position = pair.getLeft();
		double distance = pair.getRight().getValue();

		distance += Math.abs((entityA.getX() - (entityA.getBlockPos().getX()) - (entityB.getX() - entityB.getBlockPos().getX())));
		distance += Math.abs((entityA.getZ() - (entityA.getBlockPos().getZ()) - (entityB.getZ() - entityB.getBlockPos().getZ())));
		distance += Math.abs((entityA.getY() - (entityA.getBlockPos().getY()) - (entityB.getY() - entityB.getBlockPos().getY())));

		Vec3d velocity = Vec3d.ZERO;

		if (distance > maximumDistance) {
			distance = maximumDistance;

			if (position.getX() > entityA.getBlockPos().getX()) {
				velocity = new Vec3d(velocity.x + LinkartConfigurations.INSTANCE.getConfig().getVelocityMultiplier() * distance, velocity.y, velocity.z);
			} else if (position.getX() < entityA.getBlockPos().getX()) {
				velocity = new Vec3d(velocity.x - LinkartConfigurations.INSTANCE.getConfig().getVelocityMultiplier() * distance, velocity.y, velocity.z);
			}
			if (position.getY() > entityA.getBlockPos().getY()) {
				velocity = new Vec3d(velocity.x, velocity.y - LinkartConfigurations.INSTANCE.getConfig().getVelocityMultiplier() * distance, velocity.z);
			} else if (position.getY() < entityA.getBlockPos().getY()) {
				velocity = new Vec3d(velocity.x, velocity.y + LinkartConfigurations.INSTANCE.getConfig().getVelocityMultiplier() * distance, velocity.z);
			}
			if (position.getZ() > entityA.getBlockPos().getZ()) {
				velocity = new Vec3d(velocity.x, velocity.y, velocity.z + LinkartConfigurations.INSTANCE.getConfig().getVelocityMultiplier() * distance);
			} else if (position.getZ() < entityA.getBlockPos().getZ()) {
				velocity = new Vec3d(velocity.x, velocity.y, velocity.z - LinkartConfigurations.INSTANCE.getConfig().getVelocityMultiplier() * distance);
			}
		}

		return velocity;
	}

	public static boolean step(World world, Set<BlockPos> cache, BlockPos currentPosition, BlockPos finalPosition, MutableDouble distance) {
		BlockState state = world.getBlockState(currentPosition);

		if (!(state.getBlock() instanceof AbstractRailBlock)) return false;

		if (currentPosition.equals(finalPosition)) return true;

		cache.add(currentPosition);

		List<BlockPos> neighbors = (List<BlockPos>) getNeighbors(currentPosition, state.get(((AbstractRailBlock) state.getBlock()).getShapeProperty()));

		for (BlockPos neighbor : neighbors) {
			if (!cache.contains(neighbor) && step(world, cache, neighbor, finalPosition, distance)) {
				if (distance.getValue() > LinkartConfigurations.INSTANCE.getConfig().getPathfindingDistance()) {
					return false;
				} else {
					distance.increment();
					return true;
				}
			}
		}

		return false;
	}

	public static Collection<BlockPos> getNeighbors(BlockPos position, RailShape shape) {
		List<BlockPos> neighbors = new ArrayList<>();
		switch (shape) {
			case NORTH_SOUTH:
				neighbors.add(position.north());
				neighbors.add(position.south());
				neighbors.add(position.north().down());
				neighbors.add(position.south().down());
				break;
			case EAST_WEST:
				neighbors.add(position.west());
				neighbors.add(position.east());
				neighbors.add(position.west().down());
				neighbors.add(position.east().down());
				break;
			case ASCENDING_EAST:
				neighbors.add(position.west().down());
				neighbors.add(position.west());
				neighbors.add(position.east().up());
				break;
			case ASCENDING_WEST:
				neighbors.add(position.west().up());
				neighbors.add(position.east());
				neighbors.add(position.east().down());
				break;
			case ASCENDING_NORTH:
				neighbors.add(position.north().up());
				neighbors.add(position.south());
				neighbors.add(position.south().down());
				break;
			case ASCENDING_SOUTH:
				neighbors.add(position.north().down());
				neighbors.add(position.north());
				neighbors.add(position.south().up());
				break;
			case SOUTH_EAST:
				neighbors.add(position.east());
				neighbors.add(position.south());
				neighbors.add(position.east().down());
				neighbors.add(position.south().down());
				break;
			case SOUTH_WEST:
				neighbors.add(position.west());
				neighbors.add(position.south());
				neighbors.add(position.west().down());
				neighbors.add(position.south().down());
				break;
			case NORTH_WEST:
				neighbors.add(position.west());
				neighbors.add(position.north());
				neighbors.add(position.west().down());
				neighbors.add(position.north().down());
				break;
			case NORTH_EAST:
				neighbors.add(position.east());
				neighbors.add(position.north());
				neighbors.add(position.east().down());
				neighbors.add(position.north().down());
		}
		return neighbors;
	}
}
