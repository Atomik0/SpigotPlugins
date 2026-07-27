package com.technical.buildcamfpv.physics;

public class Quaternion {
    private double w;
    private double x;
    private double y;
    private double z;

    public Quaternion(double w, double x, double y, double z) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Quaternion identity() {
        return new Quaternion(1.0, 0.0, 0.0, 0.0);
    }

    public static Quaternion fromAxisAngle(double axisX, double axisY, double axisZ, double angleRadians) {
        double halfAngle = angleRadians * 0.5;
        double sinHalf = Math.sin(halfAngle);
        double cosHalf = Math.cos(halfAngle);

        double length = Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);
        if (length < 1e-9) {
            return identity();
        }

        double normX = axisX / length;
        double normY = axisY / length;
        double normZ = axisZ / length;

        return new Quaternion(cosHalf, normX * sinHalf, normY * sinHalf, normZ * sinHalf);
    }

    public Quaternion multiply(Quaternion q) {
        double newW = this.w * q.w - this.x * q.x - this.y * q.y - this.z * q.z;
        double newX = this.w * q.x + this.x * q.w + this.y * q.z - this.z * q.y;
        double newY = this.w * q.y - this.x * q.z + this.y * q.w + this.z * q.x;
        double newZ = this.w * q.z + this.x * q.y - this.y * q.x + this.z * q.w;
        return new Quaternion(newW, newX, newY, newZ);
    }

    public Quaternion normalize() {
        double mag = Math.sqrt(w * w + x * x + y * y + z * z);
        if (mag < 1e-9) {
            return identity();
        }
        return new Quaternion(w / mag, x / mag, y / mag, z / mag);
    }

    public double[] toYawPitchRoll() {
        double sqw = w * w;
        double sqx = x * x;
        double sqy = y * y;
        double sqz = z * z;
        double unit = sqx + sqy + sqz + sqw;
        double test = x * y + z * w;

        double yaw;
        double pitch;
        double roll;

        if (test > 0.499 * unit) { // Singularity at north pole
            yaw = 2.0 * Math.atan2(x, w);
            pitch = Math.PI / 2.0;
            roll = 0.0;
        } else if (test < -0.499 * unit) { // Singularity at south pole
            yaw = -2.0 * Math.atan2(x, w);
            pitch = -Math.PI / 2.0;
            roll = 0.0;
        } else {
            yaw = Math.atan2(2.0 * y * w - 2.0 * x * z, sqx - sqy - sqz + sqw);
            pitch = Math.asin(2.0 * test / unit);
            roll = Math.atan2(2.0 * x * w - 2.0 * y * z, -sqx + sqy - sqz + sqw);
        }

        return new double[]{
                Math.toDegrees(yaw),
                Math.toDegrees(pitch),
                Math.toDegrees(roll)
        };
    }

    public double getW() { return w; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
}
