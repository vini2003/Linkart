package com.github.vini2003.linkart.registry;

import com.github.vini2003.linkart.accessor.AbstractMinecartEntityAccessor;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class LinkartNetworks {
    public static final Identifier SELECTED_PACKET = new Identifier("linkart", "link");

    public static void initialize() {
        ServerSidePacketRegistry.INSTANCE.register(SELECTED_PACKET, ((context, buffer) -> {
            UUID next = buffer.readUuid();
            UUID previous = buffer.readUuid();

            ServerWorld serverWorld = (ServerWorld) context.getPlayer().getEntityWorld();

            context.getTaskQueue().execute(() -> {
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
