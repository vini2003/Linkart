package com.github.vini2003.linkart.utility;

import com.github.vini2003.linkart.accessor.AbstractMinecartEntityAccessor;
import net.minecraft.entity.Entity;

public class CollisionUtils {
    public static boolean shouldCollide(Entity source, Entity target) {
        if (source instanceof AbstractMinecartEntityAccessor) {
            AbstractMinecartEntityAccessor check;

            check = (AbstractMinecartEntityAccessor) source;

            do {
                if (check == target) {
                    return false;
                }
                check = (AbstractMinecartEntityAccessor) check.getNext();
            } while (check != null);

            check = (AbstractMinecartEntityAccessor) source;

            do {
                if (check == target) {
                    return false;
                }
                check = (AbstractMinecartEntityAccessor) check.getPrevious();
            } while (check != null);
        }

        return true;
    }
}
