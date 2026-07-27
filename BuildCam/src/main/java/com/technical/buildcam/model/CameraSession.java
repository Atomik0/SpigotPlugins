package com.technical.buildcam.model;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;

public class CameraSession {
    private final Player player;
    private final CameraPath path;
    private final Location originalLocation;
    private final GameMode originalGameMode;
    private final boolean originalFlying;
    private final double durationSeconds;
    private final long startTimeMillis;
    private BukkitTask playbackTask;

    public CameraSession(Player player, CameraPath path, double durationSeconds) {
        this.player = Objects.requireNonNull(player, "Player cannot be null");
        this.path = Objects.requireNonNull(path, "CameraPath cannot be null");
        this.originalLocation = player.getLocation().clone();
        this.originalGameMode = player.getGameMode();
        this.originalFlying = player.isFlying();
        this.durationSeconds = Math.max(1.0, durationSeconds);
        this.startTimeMillis = System.currentTimeMillis();
    }

    public Player getPlayer() {
        return player;
    }

    public CameraPath getPath() {
        return path;
    }

    public Location getOriginalLocation() {
        return originalLocation;
    }

    public GameMode getOriginalGameMode() {
        return originalGameMode;
    }

    public boolean isOriginalFlying() {
        return originalFlying;
    }

    public double getDurationSeconds() {
        return durationSeconds;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public BukkitTask getPlaybackTask() {
        return playbackTask;
    }

    public void setPlaybackTask(BukkitTask playbackTask) {
        this.playbackTask = playbackTask;
    }

    public double getProgress() {
        long elapsed = System.currentTimeMillis() - startTimeMillis;
        double total = durationSeconds * 1000.0;
        return Math.min(1.0, Math.max(0.0, elapsed / total));
    }

    public boolean isFinished() {
        return getProgress() >= 1.0;
    }
}
