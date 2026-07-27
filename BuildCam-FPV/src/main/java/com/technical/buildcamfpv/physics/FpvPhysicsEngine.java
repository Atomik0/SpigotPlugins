package com.technical.buildcamfpv.physics;

public class FpvPhysicsEngine {
    private Vector3D position;
    private Vector3D velocity;
    private Quaternion orientation;

    // Configurable FPV Physics Parameters
    private double maxRollRate = 360.0;  // Degrees per second
    private double maxPitchRate = 360.0; // Degrees per second
    private double maxYawRate = 270.0;   // Degrees per second

    private double maxThrustPower = 25.0; // Acceleration m/s^2
    private double gravity = 9.81;        // m/s^2
    private double airResistance = 0.15;   // Drag coefficient
    private double cameraTilt = 25.0;     // Camera angle tilt in degrees

    public FpvPhysicsEngine(Vector3D initialPosition, float initialYaw, float initialPitch) {
        this.position = initialPosition;
        this.velocity = Vector3D.zero();

        // Convert Bukkit initial yaw and pitch to quaternion orientation
        double yawRad = Math.toRadians(-initialYaw);
        double pitchRad = Math.toRadians(-initialPitch);

        Quaternion qYaw = Quaternion.fromAxisAngle(0, 1, 0, yawRad);
        Quaternion qPitch = Quaternion.fromAxisAngle(1, 0, 0, pitchRad);
        this.orientation = qYaw.multiply(qPitch).normalize();
    }

    public void update(double deltaTimeSeconds, double rollInput, double pitchInput, double yawInput, double throttleInput) {
        if (deltaTimeSeconds <= 0) return;

        // 1. Calculate Angular Velocity Rates (Acro Mode)
        double rollRad = Math.toRadians(rollInput * maxRollRate * deltaTimeSeconds);
        double pitchRad = Math.toRadians(pitchInput * maxPitchRate * deltaTimeSeconds);
        double yawRad = Math.toRadians(-yawInput * maxYawRate * deltaTimeSeconds); // Inverted for Bukkit coordinate convention

        Quaternion deltaRoll = Quaternion.fromAxisAngle(0, 0, 1, rollRad);
        Quaternion deltaPitch = Quaternion.fromAxisAngle(1, 0, 0, pitchRad);
        Quaternion deltaYaw = Quaternion.fromAxisAngle(0, 1, 0, yawRad);

        // Apply rotational changes to current orientation
        orientation = orientation.multiply(deltaYaw).multiply(deltaPitch).multiply(deltaRoll).normalize();

        // 2. Calculate Thrust Direction Vector (pointing forward/up along drone body)
        // Transform local (0, 1, 0) up thrust vector by drone orientation quaternion
        double thrustX = 2 * (orientation.getX() * orientation.getY() - orientation.getW() * orientation.getZ());
        double thrustY = 1 - 2 * (orientation.getX() * orientation.getX() + orientation.getZ() * orientation.getZ());
        double thrustZ = 2 * (orientation.getY() * orientation.getZ() + orientation.getW() * orientation.getX());

        Vector3D thrustDirection = new Vector3D(-thrustX, thrustY, thrustZ).normalize();

        // 3. Compute Accelerations (Thrust + Gravity + Drag)
        double clampedThrottle = Math.max(0.0, Math.min(1.0, throttleInput));
        Vector3D thrustAcc = thrustDirection.multiply(clampedThrottle * maxThrustPower);
        Vector3D gravityAcc = new Vector3D(0.0, -gravity, 0.0);

        Vector3D totalAcc = thrustAcc.add(gravityAcc);

        // 4. Update Velocity and Position with Euler integration & drag
        velocity = velocity.add(totalAcc.multiply(deltaTimeSeconds));
        velocity = velocity.multiply(Math.max(0.0, 1.0 - airResistance * deltaTimeSeconds));

        position = position.add(velocity.multiply(deltaTimeSeconds));
    }

    public Vector3D getPosition() {
        return position;
    }

    public Vector3D getVelocity() {
        return velocity;
    }

    public Quaternion getOrientation() {
        return orientation;
    }

    public double getCameraTilt() {
        return cameraTilt;
    }

    public void setCameraTilt(double cameraTilt) {
        this.cameraTilt = cameraTilt;
    }

    public double getMaxRollRate() { return maxRollRate; }
    public void setMaxRollRate(double maxRollRate) { this.maxRollRate = maxRollRate; }

    public double getMaxPitchRate() { return maxPitchRate; }
    public void setMaxPitchRate(double maxPitchRate) { this.maxPitchRate = maxPitchRate; }

    public double getMaxYawRate() { return maxYawRate; }
    public void setMaxYawRate(double maxYawRate) { this.maxYawRate = maxYawRate; }

    public double getMaxThrustPower() { return maxThrustPower; }
    public void setMaxThrustPower(double maxThrustPower) { this.maxThrustPower = maxThrustPower; }

    public double getGravity() { return gravity; }
    public void setGravity(double gravity) { this.gravity = gravity; }

    public double getAirResistance() { return airResistance; }
    public void setAirResistance(double airResistance) { this.airResistance = airResistance; }
}

