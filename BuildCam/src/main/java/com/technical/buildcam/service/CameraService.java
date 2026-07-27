package com.technical.buildcam.service;

import com.technical.buildcam.interpolation.SplineInterpolator;
import com.technical.buildcam.model.CameraPath;
import com.technical.buildcam.model.CameraPoint;
import com.technical.buildcam.model.CameraSession;
import com.technical.buildcam.storage.PathStorage;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CameraService {
    private final JavaPlugin plugin;
    private final PathStorage storage;
    private final Map<String, CameraPath> paths;
    private final Map<UUID, CameraSession> activeSessions;

    public CameraService(JavaPlugin plugin, PathStorage storage) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");
        this.storage = Objects.requireNonNull(storage, "PathStorage cannot be null");
        this.paths = new ConcurrentHashMap<>(storage.loadAllPaths());
        this.activeSessions = new ConcurrentHashMap<>();
    }

    public CameraPath getPath(String name) {
        if (name == null) return null;
        return paths.get(name.toLowerCase());
    }

    public Set<String> getPathNames() {
        return Collections.unmodifiableSet(paths.keySet());
    }

    public void savePath(CameraPath path) {
        Objects.requireNonNull(path, "CameraPath cannot be null");
        paths.put(path.getName().toLowerCase(), path);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> storage.savePath(path));
    }

    public boolean deletePath(String name) {
        if (name == null) return false;
        String key = name.toLowerCase();
        if (paths.remove(key) != null) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> storage.deletePath(key));
            return true;
        }
        return false;
    }

    public boolean isPlaying(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }

    public CameraSession getActiveSession(Player player) {
        return activeSessions.get(player.getUniqueId());
    }

    public boolean startPlayback(Player player, CameraPath path, double durationSeconds) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(path, "CameraPath cannot be null");

        if (path.getPoints().size() < 2) {
            return false;
        }

        stopPlayback(player, false);

        CameraSession session = new CameraSession(player, path, durationSeconds);
        activeSessions.put(player.getUniqueId(), session);

        player.setGameMode(GameMode.SPECTATOR);
        player.setFlying(true);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    stopPlayback(player, false);
                    cancel();
                    return;
                }

                double progress = session.getProgress();
                CameraPoint point = SplineInterpolator.interpolate(path.getPoints(), progress);
                Location loc = point.toLocation();

                if (loc.getWorld() != null) {
                    player.teleport(loc);
                }

                if (session.isFinished()) {
                    stopPlayback(player, true);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        session.setPlaybackTask(task);
        return true;
    }

    public void stopPlayback(Player player, boolean restoreLocation) {
        if (player == null) return;
        CameraSession session = activeSessions.remove(player.getUniqueId());
        if (session == null) return;

        if (session.getPlaybackTask() != null) {
            session.getPlaybackTask().cancel();
        }

        if (player.isOnline()) {
            player.setGameMode(session.getOriginalGameMode());
            player.setFlying(session.isOriginalFlying());

            if (restoreLocation && session.getOriginalLocation() != null) {
                player.teleport(session.getOriginalLocation());
            }
        }
    }

    public void stopAllSessions() {
        for (UUID uuid : new HashSet<>(activeSessions.keySet())) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                stopPlayback(player, true);
            }
        }
        activeSessions.clear();
    }
}
