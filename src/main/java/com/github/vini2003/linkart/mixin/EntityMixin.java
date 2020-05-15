package com.github.vini2003.linkart.mixin;

import com.github.vini2003.linkart.accessor.AbstractMinecartEntityAccessor;
import com.github.vini2003.linkart.registry.LinkartDistanceRegistry;
import com.github.vini2003.linkart.utility.CollisionUtils;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Pair;
import net.minecraft.util.ReusableStream;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Stream;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public World world;

    @Shadow
    private Vec3d velocity;

    @Shadow public abstract Box getBoundingBox();

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

    @Inject(at = @At("HEAD"), method = "remove")
    void removeLink(CallbackInfo ci) {
        if ((Object)this instanceof AbstractMinecartEntity) {
            AbstractMinecartEntityAccessor accessor = (AbstractMinecartEntityAccessor) this;
            AbstractMinecartEntityAccessor next = (AbstractMinecartEntityAccessor)accessor.getNext();
            AbstractMinecartEntityAccessor previous = (AbstractMinecartEntityAccessor)accessor.getPrevious();

            if (next != null) {
                next.setPrevious(null);
            }

            if (previous != null) {
                previous.setNext(null);
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;", cancellable = true)
    void onRecalculateVelocity(Vec3d movement, CallbackInfoReturnable<Vec3d> callbackInformationReturnable) {
        List<Entity> collisions = this.world.getEntities((Entity) (Object) this, getBoundingBox().stretch(movement));

        for (Entity entity : collisions) {
            if (!CollisionUtils.shouldCollide((Entity) (Object) this, entity)) {
                callbackInformationReturnable.setReturnValue(movement);
                callbackInformationReturnable.cancel();
            }
        }
    }
}
