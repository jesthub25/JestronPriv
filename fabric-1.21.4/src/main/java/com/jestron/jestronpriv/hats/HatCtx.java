package com.jestron.jestronpriv.hats;

/** Sapka cizim baglami: zaman, hareket ve renkler (MC'den bagimsiz). */
public final class HatCtx {

    /** Animasyon zamani (saniye, hiz ayariyla olcekli). */
    public float t;
    /** Hareket enerjisi 0..1 (dururken 0, kosarken ~1). */
    public float energy;
    /** Birincil / ikincil / vurgu renkleri (rgb). */
    public int c1 = 0xFFFFFF, c2 = 0xCCCCCC, c3 = 0xFFD54A;
    /** Gokkusagi modu: renkler zamanla doner. */
    public boolean rainbow;

    public HatCtx set(float t, float energy, int c1, int c2, int c3, boolean rainbow) {
        this.t = Float.isFinite(t) ? t : 0f;
        this.energy = Float.isFinite(energy) ? Math.max(0f, Math.min(1f, energy)) : 0f;
        this.c1 = c1 & 0xFFFFFF;
        this.c2 = c2 & 0xFFFFFF;
        this.c3 = c3 & 0xFFFFFF;
        this.rainbow = rainbow;
        return this;
    }

    public int a() {
        return rainbow ? hue(0f) : c1;
    }

    public int b() {
        return rainbow ? hue(0.33f) : c2;
    }

    public int c() {
        return rainbow ? hue(0.66f) : c3;
    }

    /** Akan gokkusagi rengi (rgb). */
    public int hue(float offset) {
        return hsv(t * 0.25f + offset, 0.85f, 1f);
    }

    public static int hsv(float h, float s, float v) {
        h -= (float) Math.floor(h);
        float r = Math.max(0, Math.min(1, Math.abs(h * 6 - 3) - 1));
        float g = Math.max(0, Math.min(1, 2 - Math.abs(h * 6 - 2)));
        float b = Math.max(0, Math.min(1, 2 - Math.abs(h * 6 - 4)));
        r = v * (1 - s + s * r);
        g = v * (1 - s + s * g);
        b = v * (1 - s + s * b);
        return ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }

    public static int mix(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255, br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        return (Math.round(ar + (br - ar) * t) << 16) | (Math.round(ag + (bg - ag) * t) << 8) | Math.round(ab + (bb - ab) * t);
    }

    /** Parlakligi carpanla degistirir (0..2). */
    public static int shade(int rgb, float k) {
        int r = Math.min(255, Math.round(((rgb >> 16) & 255) * k)), g = Math.min(255, Math.round(((rgb >> 8) & 255) * k)),
                b = Math.min(255, Math.round((rgb & 255) * k));
        return (Math.max(0, r) << 16) | (Math.max(0, g) << 8) | Math.max(0, b);
    }
}
