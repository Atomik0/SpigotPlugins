package com.technical.buildcamfpv.service;

import com.technical.buildcamfpv.model.FpvSession;
import com.technical.buildcamfpv.physics.Quaternion;
import com.technical.buildcamfpv.physics.Vector3D;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FpvService {
    private final JavaPlugin plugin;
    private final Map<UUID, FpvSession> activeSessions;
    private final Map<String, FpvSession> sessionsByToken;
    private final SecureRandom random;
    private BukkitTask physicsTask;

    public FpvService(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");
        this.activeSessions = new ConcurrentHashMap<>();
        this.sessionsByToken = new ConcurrentHashMap<>();
        this.random = new SecureRandom();
        startPhysicsLoop();
    }

    private void startPhysicsLoop() {
        this.physicsTask = new BukkitRunnable() {
            private long lastTime = System.nanoTime();

            @Override
            public void run() {
                long now = System.nanoTime();
                double deltaSeconds = (now - lastTime) / 1e9;
                lastTime = now;

                // Clamp delta time to avoid physics explosion on lag spikes
                deltaSeconds = Math.min(0.1, Math.max(0.01, deltaSeconds));

                for (FpvSession session : activeSessions.values()) {
                    updateSessionPhysics(session, deltaSeconds);
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private void updateSessionPhysics(FpvSession session, double deltaSeconds) {
        Player pilot = session.getPilot();
        if (!pilot.isOnline()) {
            stopSession(pilot, false);
            return;
        }

        // Run physics update
        session.getPhysicsEngine().update(
                deltaSeconds,
                session.getCurrentRollInput(),
                session.getCurrentPitchInput(),
                session.getCurrentYawInput(),
                session.getCurrentThrottleInput()
        );

        Vector3D pos = session.getPhysicsEngine().getPosition();
        Quaternion q = session.getPhysicsEngine().getOrientation();

        // Convert orientation quaternion to Yaw, Pitch, Roll
        double[] ypr = q.toYawPitchRoll();
        double yaw = ypr[0];
        double pitch = ypr[1];

        // Apply camera tilt offset
        pitch += session.getPhysicsEngine().getCameraTilt();

        World world = pilot.getWorld();
        Location targetLoc = new Location(world, pos.getX(), pos.getY(), pos.getZ(), (float) yaw, (float) pitch);

        // Teleport Pilot
        pilot.teleport(targetLoc);

        // Teleport Spectators
        for (UUID spectatorId : session.getSpectators()) {
            Player spectator = plugin.getServer().getPlayer(spectatorId);
            if (spectator != null && spectator.isOnline()) {
                spectator.teleport(targetLoc);
            }
        }
    }

    public FpvSession startSession(Player player) {
        stopSession(player, false);

        String token = generateUniqueToken();
        FpvSession session = new FpvSession(player, token);

        double defaultTilt = plugin.getConfig().getDouble("physics.default-camera-tilt", 25.0);
        double rollRate = plugin.getConfig().getDouble("physics.max-roll-rate", 360.0);
        double pitchRate = plugin.getConfig().getDouble("physics.max-pitch-rate", 360.0);
        double yawRate = plugin.getConfig().getDouble("physics.max-yaw-rate", 270.0);
        double maxThrust = plugin.getConfig().getDouble("physics.max-thrust-power", 25.0);
        double gravity = plugin.getConfig().getDouble("physics.gravity", 9.81);
        double drag = plugin.getConfig().getDouble("physics.air-resistance", 0.15);

        session.getPhysicsEngine().setCameraTilt(defaultTilt);
        session.getPhysicsEngine().setMaxRollRate(rollRate);
        session.getPhysicsEngine().setMaxPitchRate(pitchRate);
        session.getPhysicsEngine().setMaxYawRate(yawRate);
        session.getPhysicsEngine().setMaxThrustPower(maxThrust);
        session.getPhysicsEngine().setGravity(gravity);
        session.getPhysicsEngine().setAirResistance(drag);

        activeSessions.put(player.getUniqueId(), session);
        sessionsByToken.put(token, session);

        player.setGameMode(GameMode.SPECTATOR);
        player.setFlying(true);

        return session;
    }


    public void stopSession(Player player, boolean restoreLocation) {
        if (player == null) return;
        FpvSession session = activeSessions.remove(player.getUniqueId());
        if (session == null) return;

        sessionsByToken.remove(session.getSessionToken());

        if (player.isOnline()) {
            player.setGameMode(session.getOriginalGameMode());
            player.setFlying(session.isOriginalFlying());
            if (restoreLocation && session.getOriginalLocation() != null) {
                player.teleport(session.getOriginalLocation());
            }
        }

        // Release Spectators
        for (UUID spectatorId : session.getSpectators()) {
            Player spectator = plugin.getServer().getPlayer(spectatorId);
            if (spectator != null && spectator.isOnline()) {
                spectator.setGameMode(GameMode.SURVIVAL);
            }
        }
    }

    public void updateInputs(String token, double roll, double pitch, double yaw, double throttle) {
        FpvSession session = sessionsByToken.get(token);
        if (session != null) {
            session.updateInputs(roll, pitch, yaw, throttle);
        }
    }

    public FpvSession getSession(Player player) {
        return activeSessions.get(player.getUniqueId());
    }

    public boolean isSessionActive(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }

    public void stopAllSessions() {
        if (physicsTask != null) {
            physicsTask.cancel();
        }
        for (UUID uuid : new HashSet<>(activeSessions.keySet())) {
            Player p = plugin.getServer().getPlayer(uuid);
            if (p != null) {
                stopSession(p, true);
            }
        }
        activeSessions.clear();
        sessionsByToken.clear();
    }

    private String generateUniqueToken() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
