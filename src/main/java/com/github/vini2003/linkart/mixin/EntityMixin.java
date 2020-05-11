package com.github.vini2003.linkart.mixin;

import com.github.vini2003.linkart.accessor.AbstractMinecartEntityAccessor;
import com.github.vini2003.linkart.registry.LinkartDistanceRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Shadow
    public World world;

    @Shadow
    private Vec3d velocity;

    private static void applyVelocity(AbstractMinecartEntity entity) {
        AbstractMinecartEntityAccessor accessor = (AbstractMinecartEntityAccessor) entity;

        if (accessor.getPrevious() != null && !((AbstractMinecartEntityAccessor) accessor.getPrevious()).getVelocities().isEmpty()) {
            Pair<Vec3d, Vec3d> velocity = ((AbstractMinecartEntityAccessor) accessor.getPrevious()).getVelocities().pop();

            double followDistance = Math.max(LinkartDistanceRegistry.INSTANCE.getByKey(entity.getType()), LinkartDistanceRegistry.INSTANCE.getByKey(accessor.getPrevious().getType()));
            double currentDistance = entity.getPos().distanceTo(velocity.getLeft());

            ((AbstractMinecartEntityAccessor) accessor.getPrevious()).getVelocities().clear();

            if (currentDistance > followDistance) {
                Vec3d basePosition = velocity.getLeft();
                Vec3d nextPosition = entity.getPos();

                entity.addVelocity(basePosition.x - nextPosition.x, entity.getVelocity().y, basePosition.z - nextPosition.z);
            } else if (entity.dimension == accessor.getPrevious().dimension) {
                entity.setVelocity(0, 0, 0);
            }
        }

        if (accessor.getNext() != null) {
            accessor.getVelocities().add(new Pair<>(entity.getPos(), entity.getVelocity()));
        }
    }

    @Inject(at = @At("RETURN"), method = "toTag(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;")
    void onToTag(CompoundTag tag, CallbackInfoReturnable<CompoundTag> callbackInformationReturnable) {
        if ((Object) this instanceof AbstractMinecartEntity) {
            AbstractMinecartEntity entity = (AbstractMinecartEntity) (Object) this;
            AbstractMinecartEntityAccessor accessor = (AbstractMinecartEntityAccessor) entity;

            if (accessor.getNext() != null) {
                tag.putUuid("next", accessor.getNext().getUuid());
            }

            if (accessor.getPrevious() != null) {
                tag.putUuid("previous", accessor.getPrevious().getUuid());
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "fromTag(Lnet/minecraft/nbt/CompoundTag;)V")
    void onFromTag(CompoundTag tag, CallbackInfo callbackInformation) {
        if ((Object) this instanceof AbstractMinecartEntity) {
            AbstractMinecartEntity entity = (AbstractMinecartEntity) (Object) this;
            AbstractMinecartEntityAccessor accessor = (AbstractMinecartEntityAccessor) entity;

            if (tag.containsUuid("next")) {
                accessor.setNextUuid(tag.getUuid("next"));
            }

            if (tag.containsUuid("previous")) {
                accessor.setPreviousUuid(tag.getUuid("previous"));
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "Lnet/minecraft/entity/Entity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V")
    void onSetVelocity(Vec3d oldVelocity, CallbackInfo callbackInformation) {
        if ((Object) this instanceof AbstractMinecartEntity) {
            AbstractMinecartEntity entity = (AbstractMinecartEntity) (Object) this;
            applyVelocity(entity);
        }
    }
}
