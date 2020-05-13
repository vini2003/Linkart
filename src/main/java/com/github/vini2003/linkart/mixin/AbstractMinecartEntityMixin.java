package com.github.vini2003.linkart.mixin;

import com.github.vini2003.linkart.accessor.AbstractMinecartEntityAccessor;
import com.github.vini2003.linkart.registry.LinkartDistanceRegistry;
import com.github.vini2003.linkart.utility.RailUtils;
import net.fabricmc.loader.util.sat4j.core.Vec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.UUID;

@Mixin(AbstractMinecartEntity.class)
public class AbstractMinecartEntityMixin implements AbstractMinecartEntityAccessor {
    @Unique
    UUID nextUuid;

    @Unique
    private AbstractMinecartEntity previous;

    @Unique
    private AbstractMinecartEntity next;

    @Unique
    private UUID previosUuid;

    @Override
    public AbstractMinecartEntity getPrevious() {
        return previous;
    }

    @Override
    public void setPrevious(AbstractMinecartEntity previous) {
        this.previous = previous;
    }

    @Override
    public AbstractMinecartEntity getNext() {
        return next;
    }

    @Override
    public void setNext(AbstractMinecartEntity next) {
        this.next = next;
    }

    @Override
    public UUID getPreviousUuid() {
        return previosUuid;
    }

    @Override
    public void setPreviousUuid(UUID uuid) {
        this.previosUuid = uuid;
    }

    @Override
    public UUID getNextUuid() {
        return nextUuid;
    }

    @Override
    public void setNextUuid(UUID uuid) {
        this.nextUuid = uuid;
    }

    @Inject(at = @At("HEAD"), method = "tick()V")
    void onTick(CallbackInfo callbackInformation) {
        World mixedWorld = ((AbstractMinecartEntity) (Object) this).world;

        if (!mixedWorld.isClient) {
            ServerWorld serverWorld = (ServerWorld) mixedWorld;

            AbstractMinecartEntity next = (AbstractMinecartEntity) (Object) this;
            AbstractMinecartEntityAccessor accessor = (AbstractMinecartEntityAccessor) next;

            if (accessor.getPrevious() == null) {
                accessor.setPrevious((AbstractMinecartEntity) serverWorld.getEntity(accessor.getPreviousUuid()));
            }

            if (accessor.getNext() == null) {
                accessor.setNext((AbstractMinecartEntity) serverWorld.getEntity(accessor.getNextUuid()));
            }

            if (accessor.getPrevious() != null) {
                AbstractMinecartEntity previous = accessor.getPrevious();

                Vec3d nextVelocity = RailUtils.getNextVelocity(next, previous);

                if (nextVelocity != null) {
                    next.setVelocity(nextVelocity);
                }
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "pushAwayFrom(Lnet/minecraft/entity/Entity;)V", cancellable = true)
    void onPushAway(Entity entity, CallbackInfo callbackInformation) {
        if (entity instanceof AbstractMinecartEntityAccessor) {
            AbstractMinecartEntityAccessor accessor;

            AbstractMinecartEntity check = (AbstractMinecartEntity) entity;
            do {
                accessor = (AbstractMinecartEntityAccessor) check;
                if (check == (Object) this) {
                    callbackInformation.cancel();
                    return;
                }

                check = accessor.getNext();
            } while (check != null);

            check = (AbstractMinecartEntity) entity;
            do {
                accessor = (AbstractMinecartEntityAccessor) check;
                if (check == (Object) this) {
                    callbackInformation.cancel();
                    return;
                }

                check = accessor.getPrevious();
            } while (check != null);
        }
    }
}
