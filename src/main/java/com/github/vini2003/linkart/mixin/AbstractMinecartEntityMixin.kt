package com.github.vini2003.linkart.mixin

import org.spongepowered.asm.mixin.Mixin
import net.minecraft.entity.vehicle.AbstractMinecartEntity
import com.github.vini2003.linkart.accessor.AbstractMinecartEntityAccessor
import org.spongepowered.asm.mixin.Unique
import java.util.UUID
import net.minecraft.server.world.ServerWorld
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import com.github.vini2003.linkart.utility.RailUtils
import net.fabricmc.api.EnvType
import net.minecraft.entity.player.PlayerEntity
import com.github.vini2003.linkart.utility.CollisionUtils
import net.fabricmc.api.Environment
import net.minecraft.entity.Entity

@Mixin(AbstractMinecartEntity::class)
abstract class AbstractMinecartEntityMixin : AbstractMinecartEntityAccessor {

    @Unique
    var nextUuid2: UUID? = null

    override fun getNextUuid(): UUID? {
        return nextUuid2
    }

    override fun setNextUuid(uuid: UUID?) {
        nextUuid2 = uuid
    }


    @Unique
    private var previous: AbstractMinecartEntity? = null

    @Unique
    private var next: AbstractMinecartEntity? = null

    @Unique
    private var previousUuid: UUID? = null
    override fun getPrevious(): AbstractMinecartEntity? {
        if (previous == null && getPreviousUuid() != null && !(this as AbstractMinecartEntity).world.isClient) {
            previous =
                ((this as AbstractMinecartEntity).world as? ServerWorld?)?.getEntity(getPreviousUuid()) as AbstractMinecartEntity?
        }
        return previous
    }

    override fun setPrevious(previous: AbstractMinecartEntity?) {
        this.previous = previous
        nextUuid = previous?.uuid
    }

    override fun getNext(): AbstractMinecartEntity? {
        if (next == null && getNextUuid() != null && !(this as AbstractMinecartEntity).world.isClient) {
            next =
                ((this as? AbstractMinecartEntity)?.world as? ServerWorld)?.getEntity(getNextUuid()) as AbstractMinecartEntity?
        }
        return next
    }

    override fun setNext(next: AbstractMinecartEntity?) {
        this.next = next
        nextUuid = next?.uuid
    }

    override fun getPreviousUuid(): UUID? {
        return previousUuid
    }

    override fun setPreviousUuid(uuid: UUID?) {
        previousUuid = uuid
    }


    @Inject(at = [At("HEAD")], method = ["tick()V"])
    fun onTickCommon(callbackInformation: CallbackInfo?) {
        RailUtils.handleTickCommon(this as? AbstractMinecartEntity, callbackInformation)
    }



    @Environment(EnvType.CLIENT)
    @Inject(at = [At("HEAD")], method = ["tick()V"])
    fun onTickClient(callbackInformation: CallbackInfo?) {
        val entity = (this as? AbstractMinecartEntity) ?: return
        entity.passengerList.stream().filter { passenger: Entity? -> passenger is PlayerEntity }
            .forEach { player: Entity ->
//                entity.yaw = entity.yaw + 90 * 0.75f
//                player.pitch = entity.pitch
//                player.prevYaw = entity.prevYaw + 90 * 0.75f
//                player.prevPitch = entity.prevPitch
            }
    }

    @Inject(at = [At("HEAD")], method = ["pushAwayFrom(Lnet/minecraft/entity/Entity;)V"], cancellable = true)
    fun onPushAway(entity: Entity?, callbackInformation: CallbackInfo) {
        if (!CollisionUtils.shouldCollide(this as Entity, entity)) {
            callbackInformation.cancel()
        }
    }

}