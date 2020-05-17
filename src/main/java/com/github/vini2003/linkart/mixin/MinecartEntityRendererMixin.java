package com.github.vini2003.linkart.mixin;

import com.github.vini2003.linkart.accessor.AbstractMinecartEntityAccessor;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.MinecartEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecartEntityRenderer.class)
public class MinecartEntityRendererMixin<T extends AbstractMinecartEntity> {
	@Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/entity/vehicle/AbstractMinecartEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	void onRender(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo callbackInformation) {
		AbstractMinecartEntityAccessor accessor = (AbstractMinecartEntityAccessor) entity;

		// TODO: Make this work.

		if (accessor.hasPrevious()) {
			matrices.push();

			Vec3d pA = entity.getBoundingBox().getCenter();
			Vec3d pB = accessor.getPrevious().getBoundingBox().getCenter();

			VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getLines());

			consumer.vertex(matrices.peek().getModel(), (float) pA.x, (float) pA.y, (float) pA.z).color(255, 255, 255, 255).next();
			consumer.vertex(matrices.peek().getModel(), (float) pB.x, (float) pB.y, (float) pB.z).color(255, 255, 255, 255).next();

			matrices.pop();
		}
	}
}
