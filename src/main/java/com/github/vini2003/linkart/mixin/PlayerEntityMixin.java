package com.github.vini2003.linkart.mixin;

import com.github.vini2003.linkart.Linkart;
import com.github.vini2003.linkart.accessor.AbstractMinecartEntityAccessor;
import com.github.vini2003.linkart.registry.LinkartDistanceRegistry;
import com.github.vini2003.linkart.registry.LinkartItems;
import com.github.vini2003.linkart.registry.LinkartLinkerRegistry;
import com.github.vini2003.linkart.registry.LinkartNetworks;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
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
                    playerEntity.sendMessage(new LiteralText("§rMinecart at §a" + (int) x1 + "§r, §a" + (int) y1 + "§r, §a" + (int) z1 + "§r selected as parent!"));
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
                        playerEntity.sendMessage(new LiteralText("§cFailed to link minecarts; cannot link minecart with itself!"));
                    }

                    callbackInformationReturnable.setReturnValue(ActionResult.FAIL);
                    callbackInformationReturnable.cancel();

                    return;
                }

                if (accessorB.getPrevious() == entityA || accessorA.getNext() == entityB) {
                    Linkart.SELECTED_ENTITIES.put(playerEntity, null);

                    if (playerEntity.world.isClient) {
                        playerEntity.sendMessage(new LiteralText("§cFailed to link minecarts; cannot double-link minecarts!"));
                    }

                    callbackInformationReturnable.setReturnValue(ActionResult.FAIL);
                    callbackInformationReturnable.cancel();

                    return;
                }



                double x2 = entityB.getX();
                double y2 = entityB.getY();
                double z2 = entityB.getZ();

                double mD = Math.max(LinkartDistanceRegistry.INSTANCE.getByKey(entityA.getType()), LinkartDistanceRegistry.INSTANCE.getByKey(entityB.getType()));

                if (entityA.getPos().distanceTo(entityB.getPos()) > mD * 4) {
                    if (playerEntity.world.isClient) {
                        playerEntity.sendMessage(new LiteralText("§cFailed to link minecarts; distance too big: over " + (int) mD * 4 + "!"));

                        Linkart.SELECTED_ENTITIES.put(playerEntity, null);

                        callbackInformationReturnable.setReturnValue(ActionResult.FAIL);
                        callbackInformationReturnable.cancel();

                        return;
                    }

                    Linkart.SELECTED_ENTITIES.put(playerEntity, null);
                }

                accessorB.setNext((AbstractMinecartEntity) entityA);
                ((AbstractMinecartEntityAccessor) accessorB.getNext()).setPrevious(entityB);

                if (playerEntity.world.isClient) {
                    playerEntity.sendMessage(new LiteralText("§rMinecart at §a" + (int) x1 + "§r, §a" + (int) y1 + "§r, §a" + (int) z1 + "§r selected as child of §a" + (int) x2 + "§r, §a" + (int) y2 + "§r, §a" + (int) z2 + "§r!"));
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
