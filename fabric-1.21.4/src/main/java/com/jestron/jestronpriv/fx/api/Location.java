package com.jestron.jestronpriv.fx.api;

/** Dunya koordinati (sadece x,y,z). */
public final class Location implements Cloneable {
    private double x, y, z;

    public Location(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public void setY(double y) { this.y = y; }

    public Location add(double dx, double dy, double dz) {
        x += dx;
        y += dy;
        z += dz;
        return this;
    }

    public double distanceSquared(double ox, double oy, double oz) {
        double dx = x - ox, dy = y - oy, dz = z - oz;
        return dx * dx + dy * dy + dz * dz;
    }

    @Override
    public Location clone() {
        return new Location(x, y, z);
    }
}
