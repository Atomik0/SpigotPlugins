package com.technical.buildcam.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CameraPath {
    private final String name;
    private final List<CameraPoint> points;

    public CameraPath(String name) {
        this(name, new ArrayList<>());
    }

    public CameraPath(String name, List<CameraPoint> points) {
        this.name = Objects.requireNonNull(name, "Path name cannot be null").toLowerCase();
        this.points = new ArrayList<>(Objects.requireNonNull(points, "Points list cannot be null"));
    }

    public String getName() {
        return name;
    }

    public List<CameraPoint> getPoints() {
        return Collections.unmodifiableList(points);
    }

    public void addPoint(CameraPoint point) {
        points.add(Objects.requireNonNull(point, "CameraPoint cannot be null"));
    }

    public boolean removePoint(int index) {
        if (index >= 0 && index < points.size()) {
            points.remove(index);
            return true;
        }
        return false;
    }

    public void clearPoints() {
        points.clear();
    }

    public int size() {
        return points.size();
    }

    public boolean isEmpty() {
        return points.isEmpty();
    }
}
