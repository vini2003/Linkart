package com.github.vini2003.linkart.registry;

import com.github.vini2003.linkart.accessor.AbstractMinecartEntityAccessor;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.container.PlayerContainer;
import net.minecraft.container.Slot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.PacketByteBuf;

import java.util.Optional;
import java.util.UUID;

public class LinkartNetworks {
    public static final Identifier LINK_PACKET = new Identifier("linkart", "link");
    public static final Identifier UNLINK_PACKET = new Identifier("linkart", "unlink");

    public static PacketByteBuf createPacket(Entity next, Entity previous) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        buffer.writeUuid(next.getUuid());
        buffer.writeUuid(previous.getUuid());
        return buffer;
    }

    public static void initialize() {
        ServerSidePacketRegistry.INSTANCE.register(LINK_PACKET, ((context, buffer) -> {
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

        ServerSidePacketRegistry.INSTANCE.register(UNLINK_PACKET, ((context, buffer) -> {
            UUID next = buffer.readUuid();
            UUID previous = buffer.readUuid();

            ServerWorld serverWorld = (ServerWorld) context.getPlayer().getEntityWorld();

            context.getTaskQueue().execute(() -> {
                AbstractMinecartEntity entityA = (AbstractMinecartEntity) serverWorld.getEntity(next);
                AbstractMinecartEntity entityB = (AbstractMinecartEntity) serverWorld.getEntity(previous);

                AbstractMinecartEntityAccessor accessorA = (AbstractMinecartEntityAccessor) entityA;
                AbstractMinecartEntityAccessor accessorB = (AbstractMinecartEntityAccessor) entityB;

                accessorA.setNext(null);
                accessorB.setPrevious(null);

                PlayerEntity playerEntity = context.getPlayer();

                if (LinkartConfigurations.INSTANCE.getConfig().isChainEnabled()) {
                    ItemScatterer.spawn(playerEntity.world, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), new ItemStack(LinkartItems.CHAIN_ITEM));
                }
            });
        }));
    }
}
