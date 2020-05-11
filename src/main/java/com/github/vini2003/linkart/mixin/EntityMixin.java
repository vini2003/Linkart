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
}
