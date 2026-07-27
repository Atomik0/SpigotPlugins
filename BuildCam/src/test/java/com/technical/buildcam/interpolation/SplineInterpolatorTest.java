package com.technical.buildcam.interpolation;

import com.technical.buildcam.model.CameraPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SplineInterpolatorTest {

    @Test
    @DisplayName("Should return start point when progress is 0.0")
    void testStartPoint() {
        CameraPoint p1 = new CameraPoint(0, 0, 0, 0, 0, "world");
        CameraPoint p2 = new CameraPoint(10, 10, 10, 90, 45, "world");

        CameraPoint result = SplineInterpolator.interpolate(List.of(p1, p2), 0.0);
        assertEquals(0.0, result.getX(), 0.001);
        assertEquals(0.0, result.getY(), 0.001);
        assertEquals(0.0, result.getZ(), 0.001);
    }

    @Test
    @DisplayName("Should return end point when progress is 1.0")
    void testEndPoint() {
        CameraPoint p1 = new CameraPoint(0, 0, 0, 0, 0, "world");
        CameraPoint p2 = new CameraPoint(10, 10, 10, 90, 45, "world");

        CameraPoint result = SplineInterpolator.interpolate(List.of(p1, p2), 1.0);
        assertEquals(10.0, result.getX(), 0.001);
        assertEquals(10.0, result.getY(), 0.001);
        assertEquals(10.0, result.getZ(), 0.001);
    }

    @Test
    @DisplayName("Should wrap angles smoothly across 360 degree boundary")
    void testAngleWrapping() {
        float start = 350.0f;
        float end = 10.0f;

        float resultMid = SplineInterpolator.interpolateAngle(start, end, 0.5f);
        // Shortest path between 350 and 10 is +20 degrees, mid is 0 (or 360)
        float normalized = (resultMid + 360.0f) % 360.0f;
        assertEquals(0.0f, normalized, 0.001f);
    }
}
