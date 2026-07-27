package com.technical.buildcam.interpolation;

import com.technical.buildcam.model.CameraPoint;

import java.util.List;
import java.util.Objects;

public class SplineInterpolator {

    public static CameraPoint interpolate(List<CameraPoint> points, double progress) {
        Objects.requireNonNull(points, "Points list cannot be null");
        if (points.isEmpty()) {
            throw new IllegalArgumentException("Points list cannot be empty");
        }
        if (points.size() == 1) {
            return points.get(0);
        }

        double clampedProgress = Math.min(1.0, Math.max(0.0, progress));
        int totalSegments = points.size() - 1;
        double scaledTime = clampedProgress * totalSegments;

        int segmentIndex = (int) Math.floor(scaledTime);
        if (segmentIndex >= totalSegments) {
            segmentIndex = totalSegments - 1;
        }

        double t = scaledTime - segmentIndex;

        CameraPoint p1 = points.get(segmentIndex);
        CameraPoint p2 = points.get(segmentIndex + 1);
        CameraPoint p0 = (segmentIndex > 0) ? points.get(segmentIndex - 1) : p1;
        CameraPoint p3 = (segmentIndex + 2 < points.size()) ? points.get(segmentIndex + 2) : p2;

        double x = catmullRom(p0.getX(), p1.getX(), p2.getX(), p3.getX(), t);
        double y = catmullRom(p0.getY(), p1.getY(), p2.getY(), p3.getY(), t);
        double z = catmullRom(p0.getZ(), p1.getZ(), p2.getZ(), p3.getZ(), t);

        float yaw = interpolateAngle(p1.getYaw(), p2.getYaw(), (float) t);
        float pitch = interpolateAngle(p1.getPitch(), p2.getPitch(), (float) t);

        return new CameraPoint(x, y, z, yaw, pitch, p1.getWorldName());
    }

    public static double catmullRom(double p0, double p1, double p2, double p3, double t) {
        double t2 = t * t;
        double t3 = t2 * t;

        return 0.5 * ((2.0 * p1) +
                (-p0 + p2) * t +
                (2.0 * p0 - 5.0 * p1 + 4.0 * p2 - p3) * t2 +
                (-p0 + 3.0 * p1 - 3.0 * p2 + p3) * t3);
    }

    public static float interpolateAngle(float start, float end, float t) {
        float diff = (end - start) % 360.0f;
        if (diff > 180.0f) {
            diff -= 360.0f;
        } else if (diff < -180.0f) {
            diff += 360.0f;
        }
        return start + diff * t;
    }
}
