package com.technical.buildcam.template;

import com.technical.buildcam.model.CameraPath;
import com.technical.buildcam.model.CameraPoint;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TemplateGenerator {

    public static CameraPath generateOrbit(String pathName, Location target, double radius, double heightOffset, int stepCount) {
        Objects.requireNonNull(target, "Target location cannot be null");
        int steps = Math.max(8, stepCount);
        List<CameraPoint> points = new ArrayList<>();
        String worldName = target.getWorld() != null ? target.getWorld().getName() : "world";

        double centerX = target.getX();
        double centerY = target.getY();
        double centerZ = target.getZ();

        for (int i = 0; i <= steps; i++) {
            double angle = (2.0 * Math.PI * i) / steps;
            double x = centerX + radius * Math.cos(angle);
            double z = centerZ + radius * Math.sin(angle);
            double y = centerY + heightOffset;

            float[] rotation = calculateLookAt(x, y, z, centerX, centerY, centerZ);
            points.add(new CameraPoint(x, y, z, rotation[0], rotation[1], worldName));
        }

        return new CameraPath(pathName, points);
    }

    public static CameraPath generateSpiral(String pathName, Location target, double radius, double startHeight, double endHeight, double turns, int stepCount) {
        Objects.requireNonNull(target, "Target location cannot be null");
        int steps = Math.max(12, stepCount);
        List<CameraPoint> points = new ArrayList<>();
        String worldName = target.getWorld() != null ? target.getWorld().getName() : "world";

        double centerX = target.getX();
        double centerY = target.getY();
        double centerZ = target.getZ();

        for (int i = 0; i <= steps; i++) {
            double progress = (double) i / steps;
            double angle = 2.0 * Math.PI * turns * progress;
            double x = centerX + radius * Math.cos(angle);
            double z = centerZ + radius * Math.sin(angle);
            double y = centerY + startHeight + (endHeight - startHeight) * progress;

            float[] rotation = calculateLookAt(x, y, z, centerX, centerY, centerZ);
            points.add(new CameraPoint(x, y, z, rotation[0], rotation[1], worldName));
        }

        return new CameraPath(pathName, points);
    }

    public static CameraPath generateFlyby(String pathName, Location target, double length, double sideOffset, double heightOffset, int stepCount) {
        Objects.requireNonNull(target, "Target location cannot be null");
        int steps = Math.max(6, stepCount);
        List<CameraPoint> points = new ArrayList<>();
        String worldName = target.getWorld() != null ? target.getWorld().getName() : "world";

        double centerX = target.getX();
        double centerY = target.getY();
        double centerZ = target.getZ();

        double startX = centerX - length / 2.0;
        double endX = centerX + length / 2.0;
        double z = centerZ + sideOffset;
        double y = centerY + heightOffset;

        for (int i = 0; i <= steps; i++) {
            double progress = (double) i / steps;
            double x = startX + (endX - startX) * progress;

            float[] rotation = calculateLookAt(x, y, z, centerX, centerY, centerZ);
            points.add(new CameraPoint(x, y, z, rotation[0], rotation[1], worldName));
        }

        return new CameraPath(pathName, points);
    }

    public static CameraPath generateTopdown(String pathName, Location target, double scanSize, double height, int stepCount) {
        Objects.requireNonNull(target, "Target location cannot be null");
        int steps = Math.max(6, stepCount);
        List<CameraPoint> points = new ArrayList<>();
        String worldName = target.getWorld() != null ? target.getWorld().getName() : "world";

        double centerX = target.getX();
        double centerY = target.getY();
        double centerZ = target.getZ();

        double startZ = centerZ - scanSize / 2.0;
        double endZ = centerZ + scanSize / 2.0;
        double y = centerY + height;

        for (int i = 0; i <= steps; i++) {
            double progress = (double) i / steps;
            double z = startZ + (endZ - startZ) * progress;
            double x = centerX;

            // Pitch 89.0 to look almost straight down while avoiding gimble lock yaw flip
            points.add(new CameraPoint(x, y, z, 0.0f, 89.0f, worldName));
        }

        return new CameraPath(pathName, points);
    }

    public static float[] calculateLookAt(double fromX, double fromY, double fromZ, double toX, double toY, double toZ) {
        double dx = toX - fromX;
        double dy = toY - fromY;
        double dz = toZ - fromZ;
        double distance2d = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) Math.toDegrees(-Math.atan2(dy, distance2d));

        return new float[]{yaw, pitch};
    }
}
