package com.technical.buildcamfpv.model;

import com.technical.buildcamfpv.physics.FpvPhysicsEngine;
import com.technical.buildcamfpv.physics.Vector3D;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FpvSession {
    private final Player pilot;
    private final String sessionToken;
    private final FpvPhysicsEngine physicsEngine;
    private final Location originalLocation;
    private final GameMode originalGameMode;
    private final boolean originalFlying;
    private final Set<UUID> spectators;

    private volatile double currentRollInput = 0.0;
    private volatile double currentPitchInput = 0.0;
    private volatile double currentYawInput = 0.0;
    private volatile double currentThrottleInput = 0.0;
    private volatile long lastInputTimestamp;

    public FpvSession(Player pilot, String sessionToken) {
        this.pilot = pilot;
        this.sessionToken = sessionToken;
        this.originalLocation = pilot.getLocation().clone();
        this.originalGameMode = pilot.getGameMode();
        this.originalFlying = pilot.isFlying();
        this.spectators = ConcurrentHashMap.newKeySet();

        Location loc = pilot.getLocation();
        Vector3D initPos = new Vector3D(loc.getX(), loc.getY(), loc.getZ());
        this.physicsEngine = new FpvPhysicsEngine(initPos, loc.getYaw(), loc.getPitch());
        this.lastInputTimestamp = System.currentTimeMillis();
    }

    public void updateInputs(double roll, double pitch, double yaw, double throttle) {
        this.currentRollInput = Math.max(-1.0, Math.min(1.0, roll));
        this.currentPitchInput = Math.max(-1.0, Math.min(1.0, pitch));
        this.currentYawInput = Math.max(-1.0, Math.min(1.0, yaw));
        this.currentThrottleInput = Math.max(0.0, Math.min(1.0, throttle));
        this.lastInputTimestamp = System.currentTimeMillis();
    }

    public Player getPilot() { return pilot; }
    public String getSessionToken() { return sessionToken; }
    public FpvPhysicsEngine getPhysicsEngine() { return physicsEngine; }
    public Location getOriginalLocation() { return originalLocation; }
    public GameMode getOriginalGameMode() { return originalGameMode; }
    public boolean isOriginalFlying() { return originalFlying; }
    public Set<UUID> getSpectators() { return spectators; }

    public double getCurrentRollInput() { return currentRollInput; }
    public double getCurrentPitchInput() { return currentPitchInput; }
    public double getCurrentYawInput() { return currentYawInput; }
    public double getCurrentThrottleInput() { return currentThrottleInput; }
    public long getLastInputTimestamp() { return lastInputTimestamp; }
}
