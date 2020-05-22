package com.github.vini2003.linkart.mixin;

import com.github.vini2003.linkart.Linkart;
import com.github.vini2003.linkart.accessor.AbstractMinecartEntityAccessor;
import com.github.vini2003.linkart.registry.*;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.container.PlayerContainer;
import net.minecraft.container.Slot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
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

			if (LinkartLinkerRegistry.INSTANCE.getByKey(entityA.getType()).stream().noneMatch(item -> item == heldItem)) {
				return;
			}

            double x1 = entityA.getX();
            double y1 = entityA.getY();
            double z1 = entityA.getZ();

            if (Linkart.SELECTED_ENTITIES.get(playerEntity) == null) {
                Linkart.SELECTED_ENTITIES.put(playerEntity, (AbstractMinecartEntity) entityA);

                sendToClient(playerEntity, new TranslatableText(
                        "text.linkart.message.cart.link_initialize",
                        literal((int) x1, Formatting.GREEN),
                        literal((int) y1, Formatting.GREEN),
                        literal((int) z1, Formatting.GREEN)));

                cancelCallback(callbackInformationReturnable);

                return;
            } else {
                AbstractMinecartEntityAccessor accessorA = (AbstractMinecartEntityAccessor) entityA;

                AbstractMinecartEntity entityB = Linkart.SELECTED_ENTITIES.get(playerEntity);
                AbstractMinecartEntityAccessor accessorB = (AbstractMinecartEntityAccessor) entityB;

                double x2 = entityB.getX();
                double y2 = entityB.getY();
                double z2 = entityB.getZ();

                if (LinkartConfigurations.INSTANCE.getConfig().isChainEnabled() && playerEntity.world.isClient) {
                    boolean boolA = accessorA.getNext() == entityB && accessorB.getPrevious() == entityA;
                    boolean boolB = accessorB.getNext() == entityA && accessorA.getPrevious() == entityB;

                    if (boolA) {
                        accessorA.setNext(null);
                        accessorB.setPrevious(null);

                        ClientSidePacketRegistry.INSTANCE.sendToServer(LinkartNetworks.UNLINK_PACKET, LinkartNetworks.createPacket(entityA, entityB));
                    } else if (boolB) {
                        accessorB.setNext(null);
                        accessorA.setPrevious(null);

                        ClientSidePacketRegistry.INSTANCE.sendToServer(LinkartNetworks.UNLINK_PACKET, LinkartNetworks.createPacket(entityB, entityA));
                    }

                    if (boolA || boolB) {
                        sendToClient(playerEntity, new TranslatableText(
                                "text.linkart.message.cart_unlink_success",
                                literal((int) x1, Formatting.YELLOW),
                                literal((int) y1, Formatting.YELLOW),
                                literal((int) z1, Formatting.YELLOW),
                                literal((int) x2, Formatting.YELLOW),
                                literal((int) y2, Formatting.YELLOW),
                                literal((int) z2, Formatting.YELLOW)));

                        cancelCallback(callbackInformationReturnable, playerEntity);

                        return;
                    }
                }

                if (entityA == entityB) {
                    sendToClient(playerEntity, new TranslatableText("text.linkart.message.cart_link_failure_self").formatted(Formatting.RED));
                    cancelCallback(callbackInformationReturnable, playerEntity);

                    return;
                }

                if (accessorB.getPrevious() == entityA || accessorA.getNext() == entityB) {
                    sendToClient(playerEntity, new TranslatableText("text.linkart.message.cart_link_failure_recursion").formatted(Formatting.RED));
                    cancelCallback(callbackInformationReturnable, playerEntity);

                    return;
                }

                int pD = LinkartConfigurations.INSTANCE.getConfig().getPathfindingDistance();

                if (entityA.getPos().distanceTo(entityB.getPos()) > pD) {
                    sendToClient(playerEntity, new TranslatableText("text.linkart.message.cart_link_failure_chain", literal(pD)).formatted(Formatting.RED));
                    cancelCallback(callbackInformationReturnable, playerEntity);

                    return;
                }

                if (LinkartConfigurations.INSTANCE.getConfig().isChainEnabled()) {
                    Optional<Slot> optionalSlot = playerContainer.slots.stream().filter(slot -> slot.getStack().getItem() == LinkartItems.CHAIN_ITEM).findFirst();

                    if (!optionalSlot.isPresent()) {
                        sendToClient(playerEntity, new TranslatableText("text.linkart.message.cart_link_failure_chain").formatted(Formatting.RED));
                        cancelCallback(callbackInformationReturnable, playerEntity);

                        return;
                    }
                }

                accessorB.setNext((AbstractMinecartEntity) entityA);
                ((AbstractMinecartEntityAccessor) accessorB.getNext()).setPrevious(entityB);

                ClientSidePacketRegistry.INSTANCE.sendToServer(LinkartNetworks.LINK_PACKET, LinkartNetworks.createPacket(entityA, entityB));

                sendToClient(playerEntity, new TranslatableText(
                                "text.linkart.message.cart_link_success",
                                literal((int) x1, Formatting.GREEN),
                                literal((int) y1, Formatting.GREEN),
                                literal((int) z1, Formatting.GREEN),
                                literal((int) x2, Formatting.GREEN),
                                literal((int) y2, Formatting.GREEN),
                                literal((int) z2, Formatting.GREEN)));

                cancelCallback(callbackInformationReturnable, playerEntity);

                return;
            }
        }
    }

    private static void cancelCallback(CallbackInfoReturnable<ActionResult> callbackInformationReturnable) {
        callbackInformationReturnable.setReturnValue(ActionResult.FAIL);
        callbackInformationReturnable.cancel();
    }

    private static void cancelCallback(CallbackInfoReturnable<ActionResult> callbackInformationReturnable, PlayerEntity playerEntity) {
        callbackInformationReturnable.setReturnValue(ActionResult.FAIL);
        callbackInformationReturnable.cancel();

        Linkart.SELECTED_ENTITIES.put(playerEntity, null);
    }

    private static void sendToClient(PlayerEntity playerEntity, Text text) {
        if (playerEntity.world.isClient) {
            playerEntity.sendMessage(text);
        }
    }
}
