package com.github.vini2003.linkart.utility

import com.github.vini2003.linkart.accessor.AbstractMinecartEntityAccessor
import com.github.vini2003.linkart.registry.LinkartConfigurations
import com.github.vini2003.linkart.registry.LinkartDistanceRegistry
import net.minecraft.block.AbstractRailBlock
import net.minecraft.block.enums.RailShape
import net.minecraft.entity.Entity
import net.minecraft.entity.vehicle.AbstractMinecartEntity
import net.minecraft.entity.vehicle.FurnaceMinecartEntity
import net.minecraft.util.Pair
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.apache.commons.lang3.mutable.MutableDouble
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import java.util.*

object RailUtils {

    fun getNextRail(next: AbstractMinecartEntity, previous: AbstractMinecartEntity): Pair<BlockPos, MutableDouble>? {
        Optional.ofNullable(if (next.passengerList.size >= 1) next.passengerList[0] else null)
            .ifPresent { entity: Entity ->
                entity.yaw = next.yaw
                entity.pitch = next.pitch
                entity.prevYaw = next.yaw
                entity.prevPitch = next.pitch
            }
        Optional.ofNullable(if (previous.passengerList.size >= 1) previous.passengerList[0] else null)
            .ifPresent { entity: Entity ->
                entity.yaw = previous.yaw
                entity.pitch = previous.pitch
                entity.prevYaw = previous.yaw
                entity.prevPitch = previous.pitch
            }
        val entityPosition = next.blockPos
        val finalPosition = previous.blockPos
        val state = next.world.getBlockState(next.blockPos)
        if (state.block !is AbstractRailBlock) return null
        val fuck =
            getNeighbors(next.blockPos, state.get((state.block as AbstractRailBlock).shapeProperty)) as List<BlockPos>
        for (initialPosition in fuck) {
            val cache: MutableSet<BlockPos> = HashSet()
            cache.add(entityPosition)
            val distance = MutableDouble(0)
            if (step(next.world, cache, initialPosition, finalPosition, distance)) {
                return Pair(initialPosition, distance)
            }
        }
        return null
    }

    fun getNextVelocity(entityA: AbstractMinecartEntity, entityB: AbstractMinecartEntity): Vec3d {
        val pair = getNextRail(entityA, entityB)
        val maximumDistance = Math.max(
            LinkartDistanceRegistry.INSTANCE.getByKey(entityA.type),
            LinkartDistanceRegistry.INSTANCE.getByKey(entityB.type)
        )
        if (pair == null && entityA.world.registryKey === entityB.world.registryKey) {
            return Vec3d(entityB.x - entityA.x, entityB.y - entityA.y, entityB.z - entityA.z)
        } else if (pair == null) {
            return entityB.velocity
        }
        val position = pair.left
        var distance = pair.right.value
        distance += Math.abs(entityA.x - entityA.blockPos.x - (entityB.x - entityB.blockPos.x))
        distance += Math.abs(entityA.z - entityA.blockPos.z - (entityB.z - entityB.blockPos.z))
        distance += Math.abs(entityA.y - entityA.blockPos.y - (entityB.y - entityB.blockPos.y))
        var velocity = Vec3d.ZERO
        if (distance > maximumDistance) {
            distance = maximumDistance
            if (position.x > entityA.blockPos.x) {
                velocity = Vec3d(
                    velocity.x + LinkartConfigurations.INSTANCE.config.getVelocityMultiplier() * distance,
                    velocity.y,
                    velocity.z
                )
            } else if (position.x < entityA.blockPos.x) {
                velocity = Vec3d(
                    velocity.x - LinkartConfigurations.INSTANCE.config.getVelocityMultiplier() * distance,
                    velocity.y,
                    velocity.z
                )
            }
            if (position.y > entityA.blockPos.y) {
                velocity = Vec3d(
                    velocity.x,
                    velocity.y - LinkartConfigurations.INSTANCE.config.getVelocityMultiplier() * distance,
                    velocity.z
                )
            } else if (position.y < entityA.blockPos.y) {
                velocity = Vec3d(
                    velocity.x,
                    velocity.y + LinkartConfigurations.INSTANCE.config.getVelocityMultiplier() * distance,
                    velocity.z
                )
            }
            if (position.z > entityA.blockPos.z) {
                velocity = Vec3d(
                    velocity.x,
                    velocity.y,
                    velocity.z + LinkartConfigurations.INSTANCE.config.getVelocityMultiplier() * distance
                )
            } else if (position.z < entityA.blockPos.z) {
                velocity = Vec3d(
                    velocity.x,
                    velocity.y,
                    velocity.z - LinkartConfigurations.INSTANCE.config.getVelocityMultiplier() * distance
                )
            }
        }
        return velocity
    }

    fun step(
        world: World,
        cache: MutableSet<BlockPos>,
        currentPosition: BlockPos,
        finalPosition: BlockPos,
        distance: MutableDouble
    ): Boolean {
        val state = world.getBlockState(currentPosition)
        if (state.block !is AbstractRailBlock) return false
        if (currentPosition == finalPosition) return true
        cache.add(currentPosition)
        val neighbors =
            getNeighbors(currentPosition, state.get((state.block as AbstractRailBlock).shapeProperty)) as List<BlockPos>
        for (neighbor in neighbors) {
            if (!cache.contains(neighbor) && step(world, cache, neighbor, finalPosition, distance)) {
                return if (distance.value > LinkartConfigurations.INSTANCE.config.getPathfindingDistance()) {
                    false
                } else {
                    distance.increment()
                    true
                }
            }
        }
        return false
    }

    fun getNeighbors(position: BlockPos, shape: RailShape?): Collection<BlockPos> {
        val neighbors: MutableList<BlockPos> = ArrayList()
        when (shape) {
            RailShape.NORTH_SOUTH -> {
                neighbors.add(position.north())
                neighbors.add(position.south())
                neighbors.add(position.north().down())
                neighbors.add(position.south().down())
            }
            RailShape.EAST_WEST -> {
                neighbors.add(position.west())
                neighbors.add(position.east())
                neighbors.add(position.west().down())
                neighbors.add(position.east().down())
            }
            RailShape.ASCENDING_EAST -> {
                neighbors.add(position.west().down())
                neighbors.add(position.west())
                neighbors.add(position.east().up())
            }
            RailShape.ASCENDING_WEST -> {
                neighbors.add(position.west().up())
                neighbors.add(position.east())
                neighbors.add(position.east().down())
            }
            RailShape.ASCENDING_NORTH -> {
                neighbors.add(position.north().up())
                neighbors.add(position.south())
                neighbors.add(position.south().down())
            }
            RailShape.ASCENDING_SOUTH -> {
                neighbors.add(position.north().down())
                neighbors.add(position.north())
                neighbors.add(position.south().up())
            }
            RailShape.SOUTH_EAST -> {
                neighbors.add(position.east())
                neighbors.add(position.south())
                neighbors.add(position.east().down())
                neighbors.add(position.south().down())
            }
            RailShape.SOUTH_WEST -> {
                neighbors.add(position.west())
                neighbors.add(position.south())
                neighbors.add(position.west().down())
                neighbors.add(position.south().down())
            }
            RailShape.NORTH_WEST -> {
                neighbors.add(position.west())
                neighbors.add(position.north())
                neighbors.add(position.west().down())
                neighbors.add(position.north().down())
            }
            RailShape.NORTH_EAST -> {
                neighbors.add(position.east())
                neighbors.add(position.north())
                neighbors.add(position.east().down())
                neighbors.add(position.north().down())
            }
        }
        return neighbors
    }

    fun adjustVelocities(next: AbstractMinecartEntity, previous: AbstractMinecartEntity) {


        var entityA: AbstractMinecartEntity = next
        var entityB: AbstractMinecartEntity = previous

        if (entityA is FurnaceMinecartEntity) {
            val temp = entityB
            entityB = entityA
            entityA = temp
        }

        val nextVelocity = RailUtils.getNextVelocity(entityA, entityB)
        entityA.velocity = nextVelocity
    }

    fun handleTickCommon(entityMixin: AbstractMinecartEntity?, callbackInformation: CallbackInfo?) {
        val mixedWorld = (entityMixin as? AbstractMinecartEntity)?.world ?: return
        val next = (entityMixin as? AbstractMinecartEntity) ?: return
        val accessor = (next as? AbstractMinecartEntityAccessor) ?: return
        if (!mixedWorld.isClient) {
            if (accessor.previous != null) {
                val previous = accessor.previous
                RailUtils.adjustVelocities(next, previous)
            }
        }
    }
}