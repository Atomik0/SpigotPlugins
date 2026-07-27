package com.technical.buildcam.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

public class CameraPoint {
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private final String worldName;

    public CameraPoint(double x, double y, double z, float yaw, float pitch, String worldName) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.worldName = Objects.requireNonNull(worldName, "World name cannot be null");
    }

    public static CameraPoint fromLocation(Location location) {
        Objects.requireNonNull(location, "Location cannot be null");
        String world = location.getWorld() != null ? location.getWorld().getName() : "world";
        return new CameraPoint(
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch(),
                world
        );
    }

    public Location toLocation() {
        World world = Bukkit.getWorld(worldName);
        return new Location(world, x, y, z, yaw, pitch);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public String getWorldName() {
        return worldName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CameraPoint that = (CameraPoint) o;
        return Double.compare(that.x, x) == 0 &&
                Double.compare(that.y, y) == 0 &&
                Double.compare(that.z, z) == 0 &&
                Float.compare(that.yaw, yaw) == 0 &&
                Float.compare(that.pitch, pitch) == 0 &&
                Objects.equals(worldName, that.worldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, yaw, pitch, worldName);
    }

    @Override
    public String toString() {
        return String.format("CameraPoint[x=%.2f, y=%.2f, z=%.2f, yaw=%.1f, pitch=%.1f, world=%s]",
                x, y, z, yaw, pitch, worldName);
    }
}
