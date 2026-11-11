package com.jestron.jestronpriv.client.pvp;

/** Ayar ekranlari icin hazir renkler ve gokkusagi yardimcisi. */
public final class Palette {

    public static final int[] COLORS = {0xFFFFFF, 0xFF4040, 0xFF9A2E, 0xFFE047, 0x6BFF5A, 0x3DF0E0, 0x4A9DFF, 0xB36BFF,
            0xFF6FC8, 0xFFC830, 0xA0A0A0, 0x151515};
    public static final String[] NAMES = {"Beyaz", "Kırmızı", "Turuncu", "Sarı", "Yeşil", "Turkuaz", "Mavi", "Mor",
            "Pembe", "Altın", "Gri", "Siyah"};

    private Palette() {
    }

    public static int index(int rgb) {
        rgb &= 0xFFFFFF;
        for (int i = 0; i < COLORS.length; i++) if (COLORS[i] == rgb) return i;
        return -1;
    }

    public static int next(int rgb) {
        int i = index(rgb);
        return COLORS[(i + 1) % COLORS.length];
    }

    public static String name(int rgb) {
        int i = index(rgb);
        return i < 0 ? String.format("#%06X", rgb & 0xFFFFFF) : NAMES[i];
    }

    /** Ekranda renkli isim: "§" kodu yerine yakin renkte kare isareti ile. */
    public static String label(int rgb) {
        return name(rgb);
    }

    public static int rainbow(double timeSec, float offset) {
        float h = (float) ((timeSec * 0.25 + offset) % 1.0);
        if (h < 0) h += 1;
        return hsv(h, 0.85f, 1f);
    }

    public static int hsv(float h, float s, float v) {
        h -= (float) Math.floor(h);
        float r = clamp(Math.abs(h * 6 - 3) - 1), g = clamp(2 - Math.abs(h * 6 - 2)), b = clamp(2 - Math.abs(h * 6 - 4));
        r = v * (1 - s + s * r);
        g = v * (1 - s + s * g);
        b = v * (1 - s + s * b);
        return ((int) (r * 255 + 0.5f) << 16) | ((int) (g * 255 + 0.5f) << 8) | (int) (b * 255 + 0.5f);
    }

    /** Iki renk arasi karisim (k 0..1). */
    public static int mix(int a, int b, float k) {
        k = clamp(k);
        int r = (int) (((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * k);
        int g = (int) (((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * k);
        int bl = (int) ((a & 255) + ((b & 255) - (a & 255)) * k);
        return (r << 16) | (g << 8) | bl;
    }

    public static int argb(int rgb, float alpha) {
        int a = Math.round(clamp(alpha) * 255f);
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    private static float clamp(float v) {
        return v < 0 ? 0 : (v > 1 ? 1 : v);
    }
}
