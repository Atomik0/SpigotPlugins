package com.technical.buildcam.storage;

import com.technical.buildcam.model.CameraPath;
import com.technical.buildcam.model.CameraPoint;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class PathStorage {
    private final JavaPlugin plugin;
    private final File storageFile;
    private YamlConfiguration config;

    public PathStorage(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");
        this.storageFile = new File(plugin.getDataFolder(), "paths.yml");
        loadStorageFile();
    }

    private void loadStorageFile() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        if (!storageFile.exists()) {
            try {
                storageFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create paths.yml storage file", e);
            }
        }
        this.config = YamlConfiguration.loadConfiguration(storageFile);
    }

    public synchronized Map<String, CameraPath> loadAllPaths() {
        Map<String, CameraPath> paths = new HashMap<>();
        ConfigurationSection pathsSection = config.getConfigurationSection("paths");
        if (pathsSection == null) {
            return paths;
        }

        for (String pathName : pathsSection.getKeys(false)) {
            List<Map<?, ?>> pointsList = config.getMapList("paths." + pathName + ".points");
            List<CameraPoint> points = new ArrayList<>();

            for (Map<?, ?> map : pointsList) {
                try {
                    double x = ((Number) map.get("x")).doubleValue();
                    double y = ((Number) map.get("y")).doubleValue();
                    double z = ((Number) map.get("z")).doubleValue();
                    float yaw = ((Number) map.get("yaw")).floatValue();
                    float pitch = ((Number) map.get("pitch")).floatValue();
                    String world = (String) map.get("world");
                    points.add(new CameraPoint(x, y, z, yaw, pitch, world));
                } catch (Exception e) {
                    plugin.getLogger().warning("Skipping invalid point in path: " + pathName);
                }
            }

            paths.put(pathName.toLowerCase(), new CameraPath(pathName, points));
        }

        return paths;
    }

    public synchronized void savePath(CameraPath path) {
        Objects.requireNonNull(path, "CameraPath cannot be null");
        List<Map<String, Object>> serializedPoints = new ArrayList<>();

        for (CameraPoint point : path.getPoints()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("x", point.getX());
            map.put("y", point.getY());
            map.put("z", point.getZ());
            map.put("yaw", point.getYaw());
            map.put("pitch", point.getPitch());
            map.put("world", point.getWorldName());
            serializedPoints.add(map);
        }

        config.set("paths." + path.getName().toLowerCase() + ".points", serializedPoints);
        save();
    }

    public synchronized boolean deletePath(String pathName) {
        String name = pathName.toLowerCase();
        if (config.contains("paths." + name)) {
            config.set("paths." + name, null);
            save();
            return true;
        }
        return false;
    }

    private void save() {
        try {
            config.save(storageFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save paths.yml", e);
        }
    }
}
