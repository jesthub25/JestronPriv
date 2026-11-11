package com.jestron.jestronpriv.cape;

/**
 * Pelerin desenleri icin kucuk, hizli ve deterministik yardimcilar (renk, gurultu, hucre, yildiz). Minecraft'a
 * bagimli degildir; test edilebilir. Tum gurultuler tamsayi karma ile uretilir: ayni giris her zaman ayni ciktiyi verir.
 */
public final class CapeArt {

    private CapeArt() {
    }

    // ------------------------------------------------------------------ sayi

    public static float clamp(float v, float a, float b) {
        return v < a ? a : (v > b ? b : v);
    }

    public static float clamp01(float v) {
        return v < 0f ? 0f : (v > 1f ? 1f : v);
    }

    public static float smooth(float e0, float e1, float x) {
        float t = clamp01((x - e0) / (e1 - e0));
        return t * t * (3f - 2f * t);
    }

    public static float fract(float v) {
        return v - (float) Math.floor(v);
    }

    /** 0..1 arasi ucgen dalga (1 periyot). */
    public static float tri(float v) {
        float f = fract(v);
        return f < 0.5f ? f * 2f : 2f - f * 2f;
    }

    public static float sin(float v) {
        return (float) Math.sin(v);
    }

    public static float cos(float v) {
        return (float) Math.cos(v);
    }

    public static final float TAU = (float) (Math.PI * 2);

    // ------------------------------------------------------------------ renk

    public static int r(int c) {
        return (c >> 16) & 255;
    }

    public static int g(int c) {
        return (c >> 8) & 255;
    }

    public static int b(int c) {
        return c & 255;
    }

    public static int rgb(float r, float g, float b) {
        int ri = (int) (clamp01(r) * 255f + 0.5f), gi = (int) (clamp01(g) * 255f + 0.5f), bi = (int) (clamp01(b) * 255f + 0.5f);
        return (ri << 16) | (gi << 8) | bi;
    }

    public static int mix(int a, int b, float t) {
        t = clamp01(t);
        int ri = (int) (r(a) + (r(b) - r(a)) * t + 0.5f);
        int gi = (int) (g(a) + (g(b) - g(a)) * t + 0.5f);
        int bi = (int) (b(a) + (b(b) - b(a)) * t + 0.5f);
        return (ri << 16) | (gi << 8) | bi;
    }

    /** Uc renk arasinda 0..1 gecis. */
    public static int ramp(int a, int b, int c, float t) {
        t = clamp01(t);
        return t < 0.5f ? mix(a, b, t * 2f) : mix(b, c, t * 2f - 1f);
    }

    public static int mul(int c, float f) {
        return rgb(r(c) / 255f * f, g(c) / 255f * f, b(c) / 255f * f);
    }

    public static int add(int a, int b) {
        return (Math.min(255, r(a) + r(b)) << 16) | (Math.min(255, g(a) + g(b)) << 8) | Math.min(255, b(a) + b(b));
    }

    /** Ekran karisimi (parlatir, asla karartmaz). */
    public static int screen(int a, int b, float t) {
        t = clamp01(t);
        float ar = r(a) / 255f, ag = g(a) / 255f, ab = b(a) / 255f;
        float br = r(b) / 255f * t, bg = g(b) / 255f * t, bb = b(b) / 255f * t;
        return rgb(1f - (1f - ar) * (1f - br), 1f - (1f - ag) * (1f - bg), 1f - (1f - ab) * (1f - bb));
    }

    public static int hsv(float h, float s, float v) {
        h = fract(h) * 6f;
        int i = (int) h;
        float f = h - i, p = v * (1f - s), q = v * (1f - s * f), u = v * (1f - s * (1f - f));
        return switch (i) {
            case 0 -> rgb(v, u, p);
            case 1 -> rgb(q, v, p);
            case 2 -> rgb(p, v, u);
            case 3 -> rgb(p, q, v);
            case 4 -> rgb(u, p, v);
            default -> rgb(v, p, q);
        };
    }

    public static float luma(int c) {
        return (r(c) * 0.299f + g(c) * 0.587f + b(c) * 0.114f) / 255f;
    }

    /**
     * Ton dondurme (derece). Parlaklik korunur (YIQ uzayinda donme), gri tonlar degismez. Per-piksel ucuzdur.
     */
    public static int hueRotate(int c, float deg) {
        if (deg == 0f) return c;
        float a = deg * (float) (Math.PI / 180.0);
        float cs = (float) Math.cos(a), sn = (float) Math.sin(a);
        float rr = r(c) / 255f, gg = g(c) / 255f, bb = b(c) / 255f;
        float y = 0.299f * rr + 0.587f * gg + 0.114f * bb;
        float i = 0.596f * rr - 0.274f * gg - 0.322f * bb;
        float q = 0.211f * rr - 0.523f * gg + 0.312f * bb;
        float i2 = i * cs - q * sn, q2 = i * sn + q * cs;
        return rgb(y + 0.956f * i2 + 0.621f * q2, y - 0.272f * i2 - 0.647f * q2, y - 1.106f * i2 + 1.703f * q2);
    }

    // ------------------------------------------------------------------ gurultu

    public static int hash(int x, int y) {
        int h = x * 374761393 + y * 668265263;
        h = (h ^ (h >>> 13)) * 1274126177;
        return h ^ (h >>> 16);
    }

    public static int hash(int x, int y, int z) {
        return hash(hash(x, y), z * 1442695041);
    }

    /** 0..1 */
    public static float rand(int x, int y) {
        return (hash(x, y) & 0xFFFFFF) / (float) 0x1000000;
    }

    public static float rand(int x, int y, int z) {
        return (hash(x, y, z) & 0xFFFFFF) / (float) 0x1000000;
    }

    /** Deger gurultusu 0..1 (yumusak). */
    public static float noise(float x, float y) {
        int xi = (int) Math.floor(x), yi = (int) Math.floor(y);
        float xf = x - xi, yf = y - yi;
        float u = xf * xf * (3f - 2f * xf), v = yf * yf * (3f - 2f * yf);
        float a = rand(xi, yi), b = rand(xi + 1, yi), c = rand(xi, yi + 1), d = rand(xi + 1, yi + 1);
        return a + (b - a) * u + (c - a) * v + (a - b - c + d) * u * v;
    }

    /** 3 boyutlu deger gurultusu 0..1 (zamanla akan desenler icin). */
    public static float noise(float x, float y, float z) {
        int zi = (int) Math.floor(z);
        float zf = z - zi;
        float w = zf * zf * (3f - 2f * zf);
        float a = noise(x + zi * 17.13f, y + zi * 31.71f), b = noise(x + (zi + 1) * 17.13f, y + (zi + 1) * 31.71f);
        return a + (b - a) * w;
    }

    /** Katmanli gurultu 0..1. */
    public static float fbm(float x, float y, int oct) {
        float sum = 0f, amp = 0.5f, norm = 0f;
        for (int i = 0; i < oct; i++) {
            sum += noise(x, y) * amp;
            norm += amp;
            x = x * 2.03f + 7.1f;
            y = y * 2.03f + 3.7f;
            amp *= 0.5f;
        }
        return sum / norm;
    }

    public static float fbm(float x, float y, float z, int oct) {
        float sum = 0f, amp = 0.5f, norm = 0f;
        for (int i = 0; i < oct; i++) {
            sum += noise(x, y, z) * amp;
            norm += amp;
            x = x * 2.03f + 7.1f;
            y = y * 2.03f + 3.7f;
            z = z * 1.7f;
            amp *= 0.5f;
        }
        return sum / norm;
    }

    /** Hucre (voronoi) sonucu: en yakin uzaklik, ikinci uzaklik, hucre kimligi. */
    public static final class Cell {
        public float d1, d2;
        public int id;
        public float cx, cy;
    }

    public static Cell cell(float x, float y, Cell out) {
        int xi = (int) Math.floor(x), yi = (int) Math.floor(y);
        float d1 = 9f, d2 = 9f;
        int id = 0;
        float bx = 0, by = 0;
        for (int j = -1; j <= 1; j++) {
            for (int i = -1; i <= 1; i++) {
                int cx = xi + i, cy = yi + j;
                float px = cx + rand(cx, cy, 1), py = cy + rand(cx, cy, 2);
                float dx = px - x, dy = py - y;
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                if (d < d1) {
                    d2 = d1;
                    d1 = d;
                    id = hash(cx, cy);
                    bx = px;
                    by = py;
                } else if (d < d2) d2 = d;
            }
        }
        out.d1 = d1;
        out.d2 = d2;
        out.id = id;
        out.cx = bx;
        out.cy = by;
        return out;
    }

    /**
     * Yildiz alani: piksel izgarasinda seyrek yildizlar. Parlaklik 0..1 (pikselin kendisi), istege bagli titresim.
     *
     * @param density 0..1 yildiz olasiligi
     */
    public static float star(int x, int y, int seed, float density, float t, float twinkle) {
        float r = rand(x, y, seed);
        if (r > density) return 0f;
        float base = 0.45f + 0.55f * rand(x, y, seed + 7);
        if (twinkle <= 0f) return base;
        float ph = rand(x, y, seed + 13) * TAU;
        float sp = 1.5f + rand(x, y, seed + 19) * 3f;
        float tw = 0.5f + 0.5f * (float) Math.sin(t * sp + ph);
        return base * (1f - twinkle + twinkle * tw);
    }

    /** Merkezi (cx, cy) olan 4 kollu parilti (arti bicimi), yaricap rad piksel. */
    public static float sparkle(int x, int y, float cx, float cy, float rad) {
        float dx = Math.abs(x - cx), dy = Math.abs(y - cy);
        if (dx < 0.6f && dy < rad) return 1f - dy / rad;
        if (dy < 0.6f && dx < rad) return 1f - dx / rad;
        return 0f;
    }

    /** Kalp sekli icinde mi (merkez 0,0; yaricap ~1). */
    public static boolean heart(float x, float y) {
        y = -y + 0.25f;
        float a = x * x + y * y - 0.6f;
        return a * a * a - x * x * y * y * y < 0f;
    }

    /** Alti kollu kar tanesi yogunlugu (merkez 0,0, yaricap 1). */
    public static float snowflake(float x, float y) {
        float r = (float) Math.sqrt(x * x + y * y);
        if (r > 1f) return 0f;
        float a = (float) Math.atan2(y, x);
        float seg = (float) (Math.PI / 3);
        a = Math.abs(((a % seg) + seg) % seg - seg / 2f);
        float px = r * (float) Math.cos(a), py = r * (float) Math.sin(a);
        float arm = py < 0.09f ? 1f : 0f;
        float branch = (Math.abs(px - 0.55f) < 0.25f && Math.abs(py - (px - 0.55f + 0.25f) * 0.55f) < 0.07f) ? 1f : 0f;
        float core = r < 0.18f ? 1f : 0f;
        return Math.max(core, Math.max(arm, branch));
    }
}
