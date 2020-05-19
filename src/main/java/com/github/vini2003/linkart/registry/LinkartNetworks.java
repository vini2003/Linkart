package com.github.vini2003.linkart.registry;

import com.github.vini2003.linkart.Linkart;
import com.github.vini2003.linkart.accessor.AbstractMinecartEntityAccessor;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.container.PlayerContainer;
import net.minecraft.container.Slot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.UUID;

public class LinkartNetworks {
    public static final Identifier SELECTED_PACKET = new Identifier("linkart", "link");

    public static void initialize() {
        ServerSidePacketRegistry.INSTANCE.register(SELECTED_PACKET, ((context, buffer) -> {
            UUID next = buffer.readUuid();
            UUID previous = buffer.readUuid();

            ServerWorld serverWorld = (ServerWorld) context.getPlayer().getEntityWorld();

            context.getTaskQueue().execute(() -> {
                PlayerEntity playerEntity = context.getPlayer();
                PlayerContainer playerContainer = playerEntity.playerContainer;

                if (LinkartConfigurations.INSTANCE.getConfig().isChainEnabled()) {
                    Optional<Slot> optionalSlot = playerContainer.slots.stream().filter(slot -> slot.getStack().getItem() == LinkartItems.CHAIN_ITEM).findFirst();

                    if (!optionalSlot.isPresent()) {
                        playerEntity.sendMessage(new TranslatableText("text.linkart.message.cart_link_failure_desynchronization").formatted(Formatting.RED));
                        return;
                    } else {
                        optionalSlot.get().getStack().decrement(1);
                    }
                }

                AbstractMinecartEntity entityA = (AbstractMinecartEntity) serverWorld.getEntity(next);
                AbstractMinecartEntity entityB = (AbstractMinecartEntity) serverWorld.getEntity(previous);

                AbstractMinecartEntityAccessor accessorA = (AbstractMinecartEntityAccessor) entityA;
                AbstractMinecartEntityAccessor accessorB = (AbstractMinecartEntityAccessor) entityB;

                accessorB.setNext(entityA);
                accessorA.setPrevious(entityB);
            });
        }));
    }
}
