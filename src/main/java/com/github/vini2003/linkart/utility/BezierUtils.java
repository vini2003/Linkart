package com.github.vini2003.linkart.utility;

import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BezierUtils {
    public static Collection<Vector3f> getSegments(Vec3d p1, Vec3d p2, Vec3d p3, float segments) {
        List<Vector3f> positions = new ArrayList<>();

        double x1 = p1.getX();
        double y1 = p1.getY();
        double z1 = p1.getZ();

        double x3 = p3.getX();
        double y3 = p3.getY();
        double z3 = p3.getZ();

        double x2 = p2.getX();
        double y2 = p2.getY();

        double dZ = (z3 - z1) / segments;
        double cZ = 0;

        for (double t = 0; t < 1; t += (segments / 100)) {
            double p0M = Math.pow(1 - t, 2);
            double p0X = x1 * p0M;
            double p0Y = y1 * p0M;

            double p1M = 2 * t * (1 - t);
            double p1X = p1M * x2;
            double p1Y = p1M * y2;

            double p2M = Math.pow(t, 2);
            double p2X = p2M * x3;
            double p2Y = p2M * y3;

            double pX = p0X + p1X + p2X;
            double pY = p0Y + p1Y + p2Y;

            positions.add(new Vector3f((float) pX, (float) pY, (float) ((float) z1 + cZ)));

            cZ += dZ;
        }

        return positions;
    }
}
