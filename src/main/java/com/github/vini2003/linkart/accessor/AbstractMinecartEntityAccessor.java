package com.github.vini2003.linkart.accessor;

import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.UUID;

public interface AbstractMinecartEntityAccessor {
    AbstractMinecartEntity getPrevious();

    void setPrevious(AbstractMinecartEntity previous);

    AbstractMinecartEntity getNext();

    void setNext(AbstractMinecartEntity next);

    UUID getPreviousUuid();

    void setPreviousUuid(UUID uuid);

    UUID getNextUuid();

    void setNextUuid(UUID uuid);
}
