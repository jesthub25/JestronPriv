package com.jestron.jestronpriv.fx.api;

/** Basit RGB renk. */
public final class Color {
    public static final Color WHITE = new Color(255, 255, 255);

    private final int r, g, b;

    private Color(int r, int g, int b) {
        this.r = Math.max(0, Math.min(255, r));
        this.g = Math.max(0, Math.min(255, g));
        this.b = Math.max(0, Math.min(255, b));
    }

    public static Color fromRGB(int r, int g, int b) {
        return new Color(r, g, b);
    }

    public int getRed() { return r; }
    public int getGreen() { return g; }
    public int getBlue() { return b; }
    public float rf() { return r / 255f; }
    public float gf() { return g / 255f; }
    public float bf() { return b / 255f; }
}
