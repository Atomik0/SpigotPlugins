package com.technical.buildcam.template;

import com.technical.buildcam.model.CameraPath;
import com.technical.buildcam.model.CameraPoint;
import org.bukkit.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TemplateGeneratorTest {

    @Test
    @DisplayName("Should generate orbit path with correct radius and step count")
    void testOrbitGeneration() {
        Location mockLocation = new Location(null, 100.0, 64.0, 100.0);
        CameraPath path = TemplateGenerator.generateOrbit("orbit_test", mockLocation, 10.0, 5.0, 16);

        assertNotNull(path);
        assertEquals("orbit_test", path.getName());
        assertEquals(17, path.getPoints().size()); // 16 steps + 1 closing point

        CameraPoint firstPoint = path.getPoints().get(0);
        assertEquals(110.0, firstPoint.getX(), 0.001); // 100 + 10 * cos(0)
        assertEquals(69.0, firstPoint.getY(), 0.001);  // 64 + 5
    }

    @Test
    @DisplayName("Should calculate lookAt rotation correctly")
    void testCalculateLookAt() {
        // Looking from (0, 0, 0) towards (0, 0, 10) [+Z direction, South]
        float[] rotation = TemplateGenerator.calculateLookAt(0, 0, 0, 0, 0, 10);
        assertEquals(0.0f, rotation[0], 0.001f);
        assertEquals(0.0f, rotation[1], 0.001f);
    }
}
