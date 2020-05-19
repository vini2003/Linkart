package com.github.vini2003.linkart.mixin;

import com.github.vini2003.linkart.Linkart;
import com.github.vini2003.linkart.accessor.AbstractMinecartEntityAccessor;
import com.github.vini2003.linkart.registry.*;
import com.github.vini2003.linkart.utility.TextUtils;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.container.PlayerContainer;
import net.minecraft.container.Slot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.PacketByteBuf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

import static com.github.vini2003.linkart.utility.TextUtils.literal;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Shadow @Final public PlayerContainer playerContainer;

    @Inject(at = @At("HEAD"), method = "interact(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;", cancellable = true)
    void onInteract(Entity entityA, Hand hand, CallbackInfoReturnable<ActionResult> callbackInformationReturnable) {
        if (entityA instanceof AbstractMinecartEntity && hand == Hand.MAIN_HAND && entityA.world.isClient) {
            PlayerEntity playerEntity = (PlayerEntity) (Object) this;
            Item heldItem = playerEntity.getStackInHand(hand).getItem();

			if (playerEntity.getStackInHand(hand).getItem() != LinkartItems.LINKER_ITEM) {
				return;
			}
			if (LinkartLinkerRegistry.INSTANCE.getByKey(entityA.getType()).stream().noneMatch(item -> item == heldItem)) {
				return;
			}

            double x1 = entityA.getX();
            double y1 = entityA.getY();
            double z1 = entityA.getZ();

            if (Linkart.SELECTED_ENTITIES.get(playerEntity) == null) {
                Linkart.SELECTED_ENTITIES.put(playerEntity, (AbstractMinecartEntity) entityA);

                if (playerEntity.world.isClient) {
                    playerEntity.sendMessage(new TranslatableText(
                            "text.linkart.message.cart.link_initialize",
                            literal(x1, Formatting.GREEN),
                            literal(y1, Formatting.GREEN),
                            literal(z1, Formatting.GREEN)));
                }

                callbackInformationReturnable.setReturnValue(ActionResult.FAIL);
                callbackInformationReturnable.cancel();

                return;
            } else {
                AbstractMinecartEntityAccessor accessorA = (AbstractMinecartEntityAccessor) entityA;

                AbstractMinecartEntity entityB = Linkart.SELECTED_ENTITIES.get(playerEntity);
                AbstractMinecartEntityAccessor accessorB = (AbstractMinecartEntityAccessor) entityB;

                if (entityA == entityB) {
                    Linkart.SELECTED_ENTITIES.put(playerEntity, null);

                    if (playerEntity.world.isClient) {
                        playerEntity.sendMessage(new TranslatableText("text.linkart.message.cart_link_failure_self").formatted(Formatting.RED));
                    }

                    callbackInformationReturnable.setReturnValue(ActionResult.FAIL);
                    callbackInformationReturnable.cancel();

                    return;
                }

                if (accessorB.getPrevious() == entityA || accessorA.getNext() == entityB) {
                    Linkart.SELECTED_ENTITIES.put(playerEntity, null);

                    if (playerEntity.world.isClient) {
                        playerEntity.sendMessage(new TranslatableText("text.linkart.message.cart_link_failure_recursion").formatted(Formatting.RED));
                    }

                    callbackInformationReturnable.setReturnValue(ActionResult.FAIL);
                    callbackInformationReturnable.cancel();

                    return;
                }

                double x2 = entityB.getX();
                double y2 = entityB.getY();
                double z2 = entityB.getZ();

                int pD = LinkartConfigurations.INSTANCE.getConfig().getPathfindingDistance();

                if (entityA.getPos().distanceTo(entityB.getPos()) > pD) {
                    if (playerEntity.world.isClient) {
                        playerEntity.sendMessage(new TranslatableText("text.linkart.message.cart_link_failure_chain", literal(pD)).formatted(Formatting.RED));

                        Linkart.SELECTED_ENTITIES.put(playerEntity, null);

                        callbackInformationReturnable.setReturnValue(ActionResult.FAIL);
                        callbackInformationReturnable.cancel();

                        return;
                    }

                    Linkart.SELECTED_ENTITIES.put(playerEntity, null);
                }

                if (LinkartConfigurations.INSTANCE.getConfig().isChainEnabled()) {
                    Optional<Slot> optionalSlot = playerContainer.slots.stream().filter(slot -> slot.getStack().getItem() == LinkartItems.CHAIN_ITEM).findFirst();

                    if (!optionalSlot.isPresent()) {
                        if (playerEntity.world.isClient) {
                            playerEntity.sendMessage(new TranslatableText("text.linkart.message.cart_link_failure_chain").formatted(Formatting.RED));

                            Linkart.SELECTED_ENTITIES.put(playerEntity, null);

                            callbackInformationReturnable.setReturnValue(ActionResult.FAIL);
                            callbackInformationReturnable.cancel();

                            return;
                        }
                    }
                }

                accessorB.setNext((AbstractMinecartEntity) entityA);
                ((AbstractMinecartEntityAccessor) accessorB.getNext()).setPrevious(entityB);

                if (playerEntity.world.isClient) {
                    playerEntity.sendMessage(new TranslatableText(
                            "text.linkart.message.cart_link_success",
                            literal(x1, Formatting.GREEN),
                            literal(y1, Formatting.GREEN),
                            literal(z1, Formatting.GREEN),
                            literal(x2, Formatting.GREEN),
                            literal(y2, Formatting.GREEN),
                            literal(z2, Formatting.GREEN)));
                }

                Linkart.SELECTED_ENTITIES.put(playerEntity, null);

                PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

                buffer.writeUuid(entityA.getUuid());
                buffer.writeUuid(entityB.getUuid());

                ClientSidePacketRegistry.INSTANCE.sendToServer(LinkartNetworks.SELECTED_PACKET, buffer);

                callbackInformationReturnable.setReturnValue(ActionResult.FAIL);
                callbackInformationReturnable.cancel();
            }
        }
    }
}
