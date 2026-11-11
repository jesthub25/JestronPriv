package com.jestron.jestronpriv.hitfx;

/**
 * Vurus partikulu simulasyonu (Minecraft'tan bagimsiz). Sabit kapasiteli dizi yapisi: karede hic nesne
 * olusturmaz, olen partikul sondakiyle yer degistirir. Konumlar dunya koordinatinda (double).
 * Cizim icin {@link #scale}, {@link #alpha}, {@link #flap}, {@link #flipX} egrileri kullanilir.
 */
public final class HitParticles {

    // atlas hucreleri (8 x 4, 64 px) — textures/hitfx/atlas.png
    public static final int SP_ORB = 0, SP_BUBBLE = 1, SP_HEART = 2, SP_SPARK = 3, SP_STAR = 4, SP_FLAME = 5, SP_SNOW = 6,
            SP_GEM = 7, SP_DROP = 8, SP_BOLT = 9, SP_PETAL = 10, SP_LEAF = 11, SP_NOTE = 12, SP_COIN = 13, SP_SKULL = 14,
            SP_RING = 15, SP_WING = 16, SP_STREAK = 17, SP_CONFETTI = 18, SP_SMOKE = 19, SP_CROSS = 20, SP_FEATHER = 21, SP_MOON = 22,
            SP_FLOWER = 23, SP_SHARD = 24, SP_SWIRL = 25, SP_CROWN = 26, SP_RUNE = 27, SP_BAT = 28, SP_PAW = 29, SP_CLOVER = 30, SP_SLASH = 31;
    public static final int ATLAS_COLS = 8, ATLAS_ROWS = 4;

    // davranis bayraklari
    public static final int GLOW = 1, WOBBLE = 2, POP = 4, STREAK = 8, BUTTERFLY = 16, TWINKLE = 32, FLIP = 64, RING = 128,
            FLASH = 256, SPIRAL = 512;

    public static final int CAP = 1024;

    public final double[] x = new double[CAP], y = new double[CAP], z = new double[CAP];
    public final double[] ox = new double[CAP], oz = new double[CAP], floor = new double[CAP];
    public final float[] vx = new float[CAP], vy = new float[CAP], vz = new float[CAP];
    public final float[] age = new float[CAP], life = new float[CAP], size = new float[CAP];
    public final float[] rot = new float[CAP], vrot = new float[CAP], grav = new float[CAP], drag = new float[CAP];
    public final float[] seed = new float[CAP], alpha0 = new float[CAP];
    public final int[] rgb = new int[CAP], sprite = new int[CAP], flags = new int[CAP], light = new int[CAP];
    public int count;

    private long rs = 0x9E3779B97F4A7C15L;
    // burst sirasinda gecici durum
    private double bx, by, bz, bFloor;
    private int bLight;
    private float bSize;

    public void clear() {
        count = 0;
    }

    public void reseed(long s) {
        rs = s == 0 ? 0x9E3779B97F4A7C15L : s;
    }

    private float rnd() {
        rs ^= rs << 13;
        rs ^= rs >>> 7;
        rs ^= rs << 17;
        return (rs >>> 40) / (float) (1L << 24);
    }

    private float rr(float a, float b) {
        return a + (b - a) * rnd();
    }

    private int pick(int... c) {
        return c[Math.min(c.length - 1, (int) (rnd() * c.length))];
    }

    private static int hsv(float h, float s, float v) {
        h -= (float) Math.floor(h);
        float r = clamp01(Math.abs(h * 6 - 3) - 1), g = clamp01(2 - Math.abs(h * 6 - 2)), b = clamp01(2 - Math.abs(h * 6 - 4));
        r = v * (1 - s + s * r);
        g = v * (1 - s + s * g);
        b = v * (1 - s + s * b);
        return ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }

    private static float clamp01(float v) {
        return v < 0 ? 0 : (v > 1 ? 1 : v);
    }

    // ------------------------------------------------------------------ ekleme

    private float dX, dZ;          // vurus yonu (yatay, birim)
    private final float[] tv = new float[3];

    /** Rastgele yon (kure) + vurus yonune egilim + yukari itis, verilen hizda. */
    private float[] burstVel(float speed, float bias, float up) {
        float u = rr(-1, 1), th = rr(0, (float) (Math.PI * 2));
        float s = (float) Math.sqrt(1 - u * u);
        float ex = s * (float) Math.cos(th) + dX * bias, ey = u * 0.7f + up, ez = s * (float) Math.sin(th) + dZ * bias;
        float l = (float) Math.sqrt(ex * ex + ey * ey + ez * ez);
        if (l < 1e-4f) l = 1;
        tv[0] = ex / l * speed;
        tv[1] = ey / l * speed;
        tv[2] = ez / l * speed;
        return tv;
    }

    private int add(int sp, float jitter, float[] v, float lifeS, float sz, int color, float a, float g, float dr, int fl) {
        if (count >= CAP) return -1;
        int i = count++;
        x[i] = bx + rr(-jitter, jitter);
        y[i] = by + rr(-jitter, jitter) * 0.8;
        z[i] = bz + rr(-jitter, jitter);
        ox[i] = bx;
        oz[i] = bz;
        floor[i] = bFloor;
        vx[i] = v[0];
        vy[i] = v[1];
        vz[i] = v[2];
        age[i] = 0;
        life[i] = Math.max(0.05f, lifeS);
        size[i] = sz * bSize;
        rot[i] = rr(0, (float) (Math.PI * 2));
        vrot[i] = 0;
        grav[i] = g;
        drag[i] = dr;
        seed[i] = rnd();
        alpha0[i] = a;
        rgb[i] = color & 0xFFFFFF;
        sprite[i] = sp;
        flags[i] = fl;
        light[i] = bLight;
        return i;
    }

    private void spin(int i, float a, float b) {
        if (i >= 0) vrot[i] = rr(a, b);
    }

    private void upright(int i) {
        if (i >= 0) rot[i] = rr(-0.25f, 0.25f);
    }

    /**
     * Bir vurus patlamasi.
     *
     * @param dirX,dirZ vurus yonu (vurandan hedefe, yatay)
     * @param amount    adet carpani (0..)
     * @param sizeMul   boyut carpani
     * @param lightPacked partikulun cizim isigi (parlayanlar her zaman tam isik)
     * @param floorY    zemin (hedefin ayak hizasi): agir partikuller burada seker ve durur
     */
    public int burst(HitStyle st, double cx, double cy, double cz, float dirX, float dirZ, boolean crit, float amount,
                     float sizeMul, int lightPacked, double floorY) {
        if (st == null || st == HitStyle.VANILLA || !(amount > 0) || !Double.isFinite(cx + cy + cz)) return 0;
        if (!(sizeMul > 0)) sizeMul = 1f;
        if (!Float.isFinite(dirX) || !Float.isFinite(dirZ)) dirX = dirZ = 0;
        bFloor = Double.isFinite(floorY) && floorY < cy ? floorY : cy - 2.0;
        int before = count;
        float l = (float) Math.sqrt(dirX * dirX + dirZ * dirZ);
        dX = l > 1e-4f ? dirX / l : 0;
        dZ = l > 1e-4f ? dirZ / l : 0;
        bx = cx;
        by = cy;
        bz = cz;
        bLight = lightPacked;
        bSize = Math.max(0.2f, Math.min(3f, sizeMul)) * (crit ? 1.15f : 1f);
        float k = (crit ? 1.7f : 1f) * Math.min(4f, amount);
        float sp = crit ? 1.25f : 1f;
        int i;
        switch (st) {
            case KOPUK -> {
                for (int n = cnt(14, k); n > 0; n--) {
                    i = add(SP_BUBBLE, 0.25f, burstVel(rr(1.2f, 2.6f) * sp, 0.5f, 0.8f), rr(0.7f, 1.3f), rr(0.06f, 0.13f),
                            pick(0x7CFF6A, 0x3DDC84, 0xB6FF8A, 0x5CF59A), 0.9f, -1.2f, 2.2f, WOBBLE | POP);
                    upright(i);
                }
                for (int n = cnt(4, k); n > 0; n--)
                    add(SP_ORB, 0.2f, burstVel(rr(0.8f, 1.6f), 0.3f, 0.5f), rr(0.3f, 0.5f), 0.06f, 0x9BFF7A, 0.8f, 0, 3f, GLOW | POP);
            }
            case KELEBEK -> {
                for (int n = cnt(5, k); n > 0; n--) {
                    int c = pick(0xFF5FA8, 0x5FD8FF, 0xFFD54F, 0xB388FF, 0xFF8A50, 0x7CFF6A, 0x40C4FF);
                    add(SP_WING, 0.2f, burstVel(rr(1.0f, 2.0f) * sp, 0.6f, 1.0f), rr(1.4f, 2.2f), rr(0.12f, 0.18f), c, 1f, -0.15f, 1.2f, BUTTERFLY);
                    add(SP_CROSS, 0.25f, burstVel(rr(0.6f, 1.4f), 0.2f, 0.4f), rr(0.35f, 0.6f), 0.05f, c, 0.9f, 0, 3f, GLOW | TWINKLE | POP);
                }
            }
            case KALP -> {
                for (int n = cnt(8, k); n > 0; n--) {
                    i = add(SP_HEART, 0.25f, burstVel(rr(1.0f, 2.2f) * sp, 0.5f, 1.2f), rr(0.8f, 1.3f), rr(0.08f, 0.14f),
                            pick(0xFF4D6D, 0xFF7EB3, 0xFF2E4C, 0xFF9CC2), 1f, -0.5f, 2.5f, POP | WOBBLE);
                    upright(i);
                }
            }
            case ISILTI -> {
                for (int n = cnt(14, k); n > 0; n--) {
                    i = add(SP_SPARK, 0.3f, burstVel(rr(1.5f, 3.5f) * sp, 0.4f, 0.4f), rr(0.4f, 0.9f), rr(0.06f, 0.14f),
                            pick(0xFFE27A, 0xFFF5C2, 0xFFC83D), 1f, 0, 4f, GLOW | TWINKLE | POP);
                    spin(i, -3, 3);
                }
            }
            case YILDIZ -> {
                for (int n = cnt(10, k); n > 0; n--) {
                    i = add(SP_STAR, 0.25f, burstVel(rr(2f, 4f) * sp, 0.5f, 1f), rr(0.6f, 1.1f), rr(0.07f, 0.12f),
                            pick(0xFFE047, 0xFFF176, 0xFFCA28), 1f, 6f, 1f, POP);
                    spin(i, -8, 8);
                }
                for (int n = cnt(3, k); n > 0; n--)
                    add(SP_ORB, 0.15f, burstVel(rr(0.5f, 1f), 0, 0.3f), rr(0.2f, 0.35f), 0.12f, 0xFFF59D, 0.7f, 0, 4f, GLOW | POP);
            }
            case ALEV, RUH -> {
                boolean soul = st == HitStyle.RUH;
                for (int n = cnt(14, k); n > 0; n--) {
                    i = add(SP_FLAME, 0.28f, burstVel(rr(0.6f, 1.6f) * sp, 0.3f, 1.2f), rr(0.4f, 0.8f), rr(0.07f, 0.13f),
                            soul ? pick(0x52E3FF, 0x7DF9FF, 0x3AA0FF) : pick(0xFF9A2E, 0xFFC24A, 0xFF5A1F), 1f, -1.5f, 3f, GLOW | POP);
                    upright(i);
                }
                for (int n = cnt(8, k); n > 0; n--)
                    add(SP_STREAK, 0.2f, burstVel(rr(2.5f, 4.5f) * sp, 0.5f, 0.8f), rr(0.4f, 0.7f), 0.04f,
                            soul ? 0x9FF4FF : 0xFFB040, 1f, 3f, 1.5f, STREAK | GLOW);
                if (soul) add(SP_ORB, 0.05f, burstVel(0.3f, 0, 0.5f), 0.5f, 0.22f, 0x3FC8FF, 0.45f, 0, 2f, GLOW | POP);
            }
            case KAR -> {
                for (int n = cnt(14, k); n > 0; n--) {
                    i = add(SP_SNOW, 0.3f, burstVel(rr(1f, 2.5f) * sp, 0.5f, 0.8f), rr(1f, 1.6f), rr(0.06f, 0.11f),
                            pick(0xFFFFFF, 0xD6F3FF, 0xB3E5FC), 1f, 1.2f, 2.5f, WOBBLE | POP);
                    spin(i, -3, 3);
                }
            }
            case ELMAS -> {
                for (int n = cnt(12, k); n > 0; n--) {
                    i = add(SP_GEM, 0.25f, burstVel(rr(2.5f, 4.5f) * sp, 0.6f, 1.5f), rr(0.7f, 1.1f), rr(0.05f, 0.1f),
                            pick(0x6EF3FF, 0xB3FFFF, 0x30D5E0), 1f, 9f, 0.8f, FLIP);
                    spin(i, -10, 10);
                }
                for (int n = cnt(6, k); n > 0; n--)
                    add(SP_CROSS, 0.3f, burstVel(rr(0.8f, 1.6f), 0.2f, 0.3f), rr(0.3f, 0.6f), 0.06f, 0xE0FFFF, 1f, 0, 3f, GLOW | TWINKLE | POP);
            }
            case KAN -> {
                for (int n = cnt(14, k); n > 0; n--) {
                    i = add(SP_DROP, 0.22f, burstVel(rr(2f, 4f) * sp, 0.7f, 1.2f), rr(0.6f, 0.9f), rr(0.04f, 0.08f),
                            pick(0xC0101A, 0xE0202A, 0x8A0010), 1f, 12f, 0.6f, POP);
                    upright(i);
                }
            }
            case SIMSEK -> {
                for (int n = cnt(3, k); n > 0; n--) {
                    i = add(SP_BOLT, 0.35f, burstVel(0.4f, 0, 0.5f), rr(0.18f, 0.3f), rr(0.18f, 0.28f), pick(0xFFF59D, 0xFFFFFF), 1f, 0, 2f, FLASH | GLOW);
                    upright(i);
                }
                for (int n = cnt(12, k); n > 0; n--)
                    add(SP_STREAK, 0.2f, burstVel(rr(3f, 6f) * sp, 0.4f, 0.4f), rr(0.25f, 0.45f), 0.04f, 0xFFE95C, 1f, 0, 2f, STREAK | GLOW);
            }
            case SAKURA, YAPRAK -> {
                boolean leaf = st == HitStyle.YAPRAK;
                for (int n = cnt(12, k); n > 0; n--) {
                    i = add(leaf ? SP_LEAF : SP_PETAL, 0.3f, burstVel(rr(1f, 2.2f) * sp, 0.6f, 1.2f), rr(1.2f, 1.8f), rr(0.06f, 0.1f),
                            leaf ? pick(0x66BB33, 0x8BC34A, 0x4CAF50, 0xC0D838) : pick(0xFFB7D5, 0xFF9EC4, 0xFFD1E3, 0xFFFFFF),
                            1f, 1.0f, 2.2f, WOBBLE | FLIP);
                    spin(i, -4, 4);
                }
            }
            case NOTA -> {
                for (int n = cnt(7, k); n > 0; n--) {
                    i = add(SP_NOTE, 0.3f, burstVel(rr(1f, 2f) * sp, 0.4f, 1.4f), rr(0.9f, 1.3f), rr(0.08f, 0.12f),
                            hsv(rnd(), 0.75f, 1f), 1f, -0.6f, 2.5f, POP | WOBBLE);
                    upright(i);
                }
            }
            case GOKKUSAGI -> {
                for (int n = cnt(16, k); n > 0; n--)
                    add(SP_ORB, 0.25f, burstVel(rr(2f, 4f) * sp, 0.4f, 0.5f), rr(0.5f, 0.9f), rr(0.06f, 0.12f), hsv(rnd(), 0.8f, 1f), 1f, 0.5f, 3f, POP | GLOW);
                for (int n = cnt(6, k); n > 0; n--)
                    add(SP_CROSS, 0.3f, burstVel(rr(1f, 2f), 0.2f, 0.4f), rr(0.4f, 0.7f), 0.06f, hsv(rnd(), 0.5f, 1f), 1f, 0, 3f, GLOW | TWINKLE | POP);
            }
            case ENDER -> {
                for (int n = cnt(14, k); n > 0; n--) {
                    float a = rr(0, (float) (Math.PI * 2)), sv = rr(1.6f, 2.6f) * sp;
                    tv[0] = -(float) Math.sin(a) * sv;
                    tv[1] = rr(0.4f, 1.2f);
                    tv[2] = (float) Math.cos(a) * sv;
                    i = add(SP_ORB, 0.0f, tv, rr(0.8f, 1.2f), rr(0.05f, 0.1f), pick(0xC86BFF, 0xE0A8FF, 0x8A2BE2), 1f, -0.8f, 0.6f, SPIRAL | GLOW | POP);
                    if (i >= 0) {
                        x[i] = bx + Math.cos(a) * 0.35;
                        z[i] = bz + Math.sin(a) * 0.35;
                    }
                }
            }
            case PARA -> {
                for (int n = cnt(8, k); n > 0; n--) {
                    i = add(SP_COIN, 0.2f, burstVel(rr(2.5f, 4f) * sp, 0.4f, 2.2f), rr(0.9f, 1.3f), rr(0.07f, 0.1f),
                            pick(0xFFD23F, 0xFFC107, 0xFFE082), 1f, 10f, 0.6f, FLIP);
                    upright(i);
                }
                for (int n = cnt(4, k); n > 0; n--)
                    add(SP_SPARK, 0.25f, burstVel(rr(1f, 2f), 0.2f, 0.5f), rr(0.3f, 0.5f), 0.07f, 0xFFF8C4, 1f, 0, 3f, GLOW | TWINKLE | POP);
            }
            case KAFATASI -> {
                for (int n = crit ? 2 : 1; n > 0; n--) {
                    i = add(SP_SKULL, 0.15f, burstVel(rr(0.6f, 1.0f), 0.2f, 1.6f), rr(1.0f, 1.3f), rr(0.14f, 0.18f), 0xEDEDED, 1f, -0.4f, 2f, POP);
                    upright(i);
                }
                for (int n = cnt(8, k); n > 0; n--) {
                    i = add(SP_SMOKE, 0.3f, burstVel(rr(0.5f, 1.2f), 0.3f, 0.8f), rr(0.8f, 1.3f), rr(0.12f, 0.22f), pick(0x2A2A2A, 0x3A2F3F, 0x1E1E1E), 0.7f, -0.6f, 2f, POP);
                    spin(i, -1, 1);
                }
            }
            case SOK -> {
                for (int n = crit ? 2 : 1; n > 0; n--)
                    add(SP_RING, 0f, burstVel(0f, 0, 0), 0.35f + n * 0.08f, 0.25f, 0xCFF6FF, 1f, 0, 0, RING | GLOW);
                for (int n = cnt(10, k); n > 0; n--)
                    add(SP_STREAK, 0.15f, burstVel(rr(3f, 5f) * sp, 0.3f, 0.2f), rr(0.25f, 0.4f), 0.04f, pick(0xFFFFFF, 0xB3F5FF), 1f, 0, 3f, STREAK | GLOW);
            }
            case NEON -> {
                for (int n = cnt(12, k); n > 0; n--)
                    add(SP_STREAK, 0.2f, burstVel(rr(2.5f, 5f) * sp, 0.4f, 0.4f), rr(0.3f, 0.6f), 0.05f, pick(0x00F5FF, 0xFF2BD6), 1f, 0, 2.5f, STREAK | GLOW);
                for (int n = cnt(6, k); n > 0; n--)
                    add(SP_ORB, 0.25f, burstVel(rr(0.8f, 1.6f), 0.2f, 0.3f), rr(0.3f, 0.5f), 0.07f, pick(0x00F5FF, 0xFF2BD6), 1f, 0, 3f, GLOW | POP);
            }
            case KONFETI -> {
                for (int n = cnt(16, k); n > 0; n--) {
                    i = add(SP_CONFETTI, 0.25f, burstVel(rr(2f, 4f) * sp, 0.4f, 2f), rr(1.2f, 1.8f), rr(0.04f, 0.07f),
                            hsv(rnd(), 0.85f, 1f), 1f, 4.5f, 2.2f, FLIP | WOBBLE);
                    spin(i, -8, 8);
                }
            }
            case TUY -> {
                for (int n = cnt(10, k); n > 0; n--) {
                    i = add(SP_FEATHER, 0.3f, burstVel(rr(1.2f, 2.4f) * sp, 0.5f, 1.3f), rr(1.4f, 2.1f), rr(0.08f, 0.13f),
                            pick(0xFFFFFF, 0xF3F0FF, 0xFFF6E0), 1f, 0.7f, 2.6f, WOBBLE | FLIP);
                    spin(i, -2, 2);
                }
                for (int n = cnt(6, k); n > 0; n--)
                    add(SP_SPARK, 0.3f, burstVel(rr(0.6f, 1.4f), 0.2f, 0.6f), rr(0.5f, 0.9f), 0.05f, 0xFFF8D0, 1f, -0.3f, 2.5f, GLOW | TWINKLE | POP);
            }
            case HILAL -> {
                for (int n = cnt(6, k); n > 0; n--) {
                    i = add(SP_MOON, 0.3f, burstVel(rr(0.8f, 1.6f) * sp, 0.3f, 1.6f), rr(1.0f, 1.5f), rr(0.08f, 0.13f),
                            pick(0xE8F0FF, 0xC8D8FF, 0xFFF4C8), 1f, -0.5f, 2.5f, GLOW | POP);
                    spin(i, -1.5f, 1.5f);
                }
                for (int n = cnt(10, k); n > 0; n--)
                    add(SP_CROSS, 0.4f, burstVel(rr(0.5f, 1.2f), 0.1f, 0.5f), rr(0.6f, 1.1f), rr(0.03f, 0.06f), pick(0xFFFFFF, 0xB8CCFF), 1f, -0.2f, 2f,
                            GLOW | TWINKLE | POP);
            }
            case PAPATYA -> {
                for (int n = cnt(10, k); n > 0; n--) {
                    i = add(SP_FLOWER, 0.3f, burstVel(rr(1.4f, 2.8f) * sp, 0.5f, 1.4f), rr(1.1f, 1.6f), rr(0.07f, 0.11f),
                            pick(0xFFFFFF, 0xFFE066, 0xFF8FC8, 0xB59CFF, 0x8FE3FF), 1f, 2.2f, 1.8f, FLIP | WOBBLE | POP);
                    spin(i, -5, 5);
                }
                for (int n = cnt(5, k); n > 0; n--)
                    add(SP_PETAL, 0.3f, burstVel(rr(1f, 2f), 0.4f, 1f), rr(1f, 1.5f), 0.05f, 0xFFFFFF, 1f, 1.2f, 2.2f, WOBBLE | FLIP);
            }
            case BUZ -> {
                for (int n = cnt(12, k); n > 0; n--) {
                    i = add(SP_SHARD, 0.2f, burstVel(rr(3f, 5.5f) * sp, 0.6f, 1.1f), rr(0.6f, 1.0f), rr(0.06f, 0.11f),
                            pick(0xBFF4FF, 0x8FE6FF, 0xE8FBFF), 1f, 11f, 0.7f, POP);
                    spin(i, -12, 12);
                }
                add(SP_RING, 0f, burstVel(0, 0, 0), 0.35f, 0.24f, 0xBFF4FF, 0.9f, 0, 0, RING | GLOW);
                for (int n = cnt(6, k); n > 0; n--)
                    add(SP_SNOW, 0.35f, burstVel(rr(0.6f, 1.4f), 0.2f, 0.6f), rr(0.8f, 1.2f), 0.05f, 0xFFFFFF, 1f, 0.8f, 2.5f, WOBBLE | POP);
            }
            case GIRDAP -> {
                for (int n = cnt(4, k); n > 0; n--) {
                    i = add(SP_SWIRL, 0.25f, burstVel(rr(0.5f, 1.2f), 0.2f, 0.8f), rr(0.6f, 0.9f), rr(0.12f, 0.18f),
                            pick(0x5FD8FF, 0x7B8CFF, 0xB0F0FF), 1f, -0.4f, 2.5f, GLOW | POP);
                    spin(i, -14, -9);
                }
                for (int n = cnt(12, k); n > 0; n--) {
                    float a = rr(0, (float) (Math.PI * 2)), sv = rr(1.8f, 2.8f) * sp;
                    tv[0] = -(float) Math.sin(a) * sv;
                    tv[1] = rr(0.6f, 1.4f);
                    tv[2] = (float) Math.cos(a) * sv;
                    i = add(SP_ORB, 0f, tv, rr(0.7f, 1.1f), rr(0.04f, 0.08f), pick(0x5FD8FF, 0x7B8CFF, 0xE0F8FF), 1f, -0.6f, 0.6f, SPIRAL | GLOW | POP);
                    if (i >= 0) {
                        x[i] = bx + Math.cos(a) * 0.3;
                        z[i] = bz + Math.sin(a) * 0.3;
                    }
                }
            }
            case TAC -> {
                for (int n = crit ? 2 : 1; n > 0; n--) {
                    i = add(SP_CROWN, 0.12f, burstVel(rr(0.6f, 1f), 0.1f, 1.8f), rr(1.0f, 1.3f), rr(0.13f, 0.17f), 0xFFD23F, 1f, -0.3f, 2.2f, POP);
                    upright(i);
                }
                for (int n = cnt(7, k); n > 0; n--) {
                    i = add(SP_COIN, 0.2f, burstVel(rr(2.2f, 3.6f) * sp, 0.4f, 2f), rr(0.9f, 1.2f), rr(0.06f, 0.09f), pick(0xFFD23F, 0xFFE082), 1f, 10f, 0.6f, FLIP);
                    upright(i);
                }
                for (int n = cnt(6, k); n > 0; n--)
                    add(SP_SPARK, 0.3f, burstVel(rr(1f, 2f), 0.2f, 0.6f), rr(0.4f, 0.7f), 0.06f, 0xFFF5C2, 1f, 0, 3f, GLOW | TWINKLE | POP);
            }
            case RUN -> {
                i = add(SP_RUNE, 0f, burstVel(0, 0, 0), 0.6f, 0.2f, pick(0x5FFFE0, 0x7FC8FF), 1f, 0, 0, RING | GLOW);
                spin(i, 3, 5);
                if (crit) {
                    i = add(SP_RUNE, 0f, burstVel(0, 0, 0), 0.75f, 0.14f, 0xC88BFF, 1f, 0, 0, RING | GLOW);
                    spin(i, -6, -4);
                }
                for (int n = cnt(10, k); n > 0; n--)
                    add(SP_ORB, 0.4f, burstVel(rr(0.3f, 0.8f), 0f, 2.2f), rr(0.7f, 1.2f), rr(0.03f, 0.06f), pick(0x5FFFE0, 0x7FC8FF, 0xC88BFF), 1f, -1.2f, 1.5f,
                            GLOW | TWINKLE | POP);
            }
            case YARASA -> {
                for (int n = cnt(5, k); n > 0; n--)
                    add(SP_BAT, 0.2f, burstVel(rr(1.4f, 2.4f) * sp, 0.6f, 1.2f), rr(1.1f, 1.7f), rr(0.11f, 0.16f), pick(0x2A1B38, 0x3A2550, 0x1C1C24), 1f,
                            -0.3f, 1.1f, BUTTERFLY);
                for (int n = cnt(4, k); n > 0; n--)
                    add(SP_ORB, 0.25f, burstVel(rr(0.8f, 1.4f), 0.3f, 0.6f), rr(0.3f, 0.5f), 0.03f, 0xFF3040, 1f, 0, 3f, GLOW | TWINKLE | POP);
            }
            case PATI -> {
                for (int n = cnt(7, k); n > 0; n--) {
                    i = add(SP_PAW, 0.4f, burstVel(rr(0.3f, 0.8f), 0.4f, 0.6f), rr(0.8f, 1.2f), rr(0.06f, 0.1f),
                            pick(0xFF9EC4, 0xFFFFFF, 0xFFD1A8, 0xC8A0FF), 1f, -0.2f, 3f, POP);
                    upright(i);
                }
                for (int n = cnt(4, k); n > 0; n--) {
                    i = add(SP_HEART, 0.3f, burstVel(rr(0.6f, 1.2f), 0.2f, 1f), rr(0.6f, 0.9f), 0.05f, 0xFF7EB3, 1f, -0.4f, 2.5f, POP | WOBBLE);
                    upright(i);
                }
            }
            case YONCA -> {
                for (int n = cnt(9, k); n > 0; n--) {
                    i = add(SP_CLOVER, 0.3f, burstVel(rr(1.4f, 2.8f) * sp, 0.5f, 1.4f), rr(1.1f, 1.6f), rr(0.07f, 0.11f),
                            pick(0x3DDC5A, 0x7CFF6A, 0x2EB84A), 1f, 2f, 1.8f, FLIP | WOBBLE | POP);
                    spin(i, -4, 4);
                }
                for (int n = cnt(6, k); n > 0; n--)
                    add(SP_SPARK, 0.3f, burstVel(rr(1f, 2.2f), 0.3f, 0.6f), rr(0.4f, 0.7f), 0.06f, 0xFFE27A, 1f, 0, 3f, GLOW | TWINKLE | POP);
            }
            case KESIK -> {
                float base = (float) Math.atan2(dZ, dX);
                for (int n = crit ? 2 : 1; n > 0; n--) {
                    i = add(SP_SLASH, 0.05f, burstVel(0.2f, 1f, 0f), 0.28f, 0.32f, n == 1 ? 0xE8FBFF : 0x9FEFFF, 1f, 0, 3f, GLOW | POP);
                    if (i >= 0) rot[i] = base + (n == 1 ? 0.6f : -0.9f) + rr(-0.2f, 0.2f);
                }
                for (int n = cnt(10, k); n > 0; n--)
                    add(SP_STREAK, 0.15f, burstVel(rr(3f, 5.5f) * sp, 0.6f, 0.3f), rr(0.2f, 0.35f), 0.035f, pick(0xFFFFFF, 0xB3F5FF), 1f, 0, 3f, STREAK | GLOW);
            }
            case VOLKAN -> {
                for (int n = cnt(12, k); n > 0; n--) {
                    i = add(SP_DROP, 0.22f, burstVel(rr(2.2f, 4f) * sp, 0.5f, 2.2f), rr(0.7f, 1.1f), rr(0.05f, 0.09f),
                            pick(0xFF6A00, 0xFF9A1F, 0xFFC040), 1f, 11f, 0.5f, GLOW | POP);
                    upright(i);
                }
                for (int n = cnt(6, k); n > 0; n--) {
                    i = add(SP_SMOKE, 0.3f, burstVel(rr(0.5f, 1.1f), 0.2f, 1f), rr(0.9f, 1.4f), rr(0.12f, 0.2f), pick(0x2A2422, 0x3A302A), 0.75f, -0.7f, 2f, POP);
                    spin(i, -1, 1);
                }
                for (int n = cnt(6, k); n > 0; n--) {
                    i = add(SP_FLAME, 0.25f, burstVel(rr(0.6f, 1.4f), 0.3f, 1.2f), rr(0.35f, 0.6f), rr(0.07f, 0.11f), pick(0xFF5A1F, 0xFF9A2E), 1f, -1.5f, 3f,
                            GLOW | POP);
                    upright(i);
                }
            }
            case ZEHIR -> {
                for (int n = cnt(12, k); n > 0; n--) {
                    i = add(SP_BUBBLE, 0.3f, burstVel(rr(0.8f, 1.8f) * sp, 0.4f, 1f), rr(0.8f, 1.3f), rr(0.05f, 0.11f),
                            pick(0x8CFF3A, 0x5CE01A, 0xC0FF5A), 0.9f, -1.0f, 2.4f, WOBBLE | POP | GLOW);
                    upright(i);
                }
                for (int n = cnt(5, k); n > 0; n--) {
                    i = add(SP_SMOKE, 0.35f, burstVel(rr(0.3f, 0.8f), 0.2f, 0.5f), rr(1.0f, 1.5f), rr(0.15f, 0.24f), pick(0x5A8A2A, 0x3E6B1E), 0.55f, -0.3f, 2f, POP);
                    spin(i, -0.8f, 0.8f);
                }
                if (crit) {
                    i = add(SP_SKULL, 0.1f, burstVel(0.6f, 0.2f, 1.6f), 0.9f, 0.11f, 0xA6FF6A, 1f, -0.4f, 2f, POP | GLOW);
                    upright(i);
                }
            }
            case GALAKSI -> {
                for (int n = cnt(12, k); n > 0; n--) {
                    float a = rr(0, (float) (Math.PI * 2)), sv = rr(1.6f, 2.6f) * sp;
                    tv[0] = -(float) Math.sin(a) * sv;
                    tv[1] = rr(-0.3f, 0.6f);
                    tv[2] = (float) Math.cos(a) * sv;
                    i = add(SP_STAR, 0f, tv, rr(0.8f, 1.3f), rr(0.04f, 0.08f), pick(0x9B7BFF, 0x5FB0FF, 0xFF8AE0, 0xFFFFFF), 1f, 0, 0.6f, SPIRAL | GLOW | POP);
                    if (i >= 0) {
                        x[i] = bx + Math.cos(a) * 0.32;
                        z[i] = bz + Math.sin(a) * 0.32;
                        spin(i, -4, 4);
                    }
                }
                for (int n = cnt(10, k); n > 0; n--)
                    add(SP_CROSS, 0.45f, burstVel(rr(0.3f, 0.8f), 0f, 0.3f), rr(0.6f, 1.1f), rr(0.025f, 0.05f), pick(0xFFFFFF, 0xC8B0FF), 1f, 0, 2f, GLOW | TWINKLE | POP);
                add(SP_ORB, 0f, burstVel(0, 0, 0), 0.4f, 0.3f, 0x6A3AFF, 0.5f, 0, 0, FLASH | GLOW);
            }
            case KARADELIK -> {
                for (int n = cnt(16, k); n > 0; n--) {
                    float u = rr(-1, 1), th = rr(0, (float) (Math.PI * 2)), s0 = (float) Math.sqrt(1 - u * u);
                    float ex = s0 * (float) Math.cos(th), ey = u, ez = s0 * (float) Math.sin(th);
                    float r = rr(0.55f, 0.8f);
                    // merkeze dogru + teget
                    tv[0] = -ex * 1.6f - ez * 1.2f;
                    tv[1] = -ey * 1.6f;
                    tv[2] = -ez * 1.6f + ex * 1.2f;
                    i = add(SP_ORB, 0f, tv, rr(0.45f, 0.65f), rr(0.03f, 0.06f), pick(0xB070FF, 0x7A2BFF, 0xFFFFFF), 1f, 0, 0f, SPIRAL | GLOW);
                    if (i >= 0) {
                        x[i] = bx + ex * r;
                        y[i] = by + ey * r * 0.7;
                        z[i] = bz + ez * r;
                    }
                }
                add(SP_ORB, 0f, burstVel(0, 0, 0), 0.55f, 0.16f, 0x140022, 0.95f, 0, 0, POP);
                add(SP_RING, 0f, burstVel(0, 0, 0), 0.5f, 0.2f, 0xB070FF, 0.8f, 0, 0, RING | GLOW);
            }
            case BALON -> {
                for (int n = cnt(9, k); n > 0; n--) {
                    i = add(SP_BUBBLE, 0.3f, burstVel(rr(0.8f, 1.6f) * sp, 0.4f, 1.4f), rr(1.4f, 2.0f), rr(0.09f, 0.15f),
                            pick(0xFF5A7A, 0xFFD23F, 0x5FD8FF, 0x7CFF6A, 0xC88BFF, 0xFF9A2E), 1f, -0.9f, 1.8f, WOBBLE | POP);
                    upright(i);
                }
            }
            case ZUMRUT -> {
                for (int n = cnt(12, k); n > 0; n--) {
                    i = add(SP_GEM, 0.25f, burstVel(rr(2.5f, 4.5f) * sp, 0.6f, 1.5f), rr(0.7f, 1.1f), rr(0.05f, 0.1f),
                            pick(0x2EE07A, 0x7CFFB0, 0x16B85A), 1f, 9f, 0.8f, FLIP);
                    spin(i, -10, 10);
                }
                for (int n = cnt(6, k); n > 0; n--)
                    add(SP_CROSS, 0.3f, burstVel(rr(0.8f, 1.6f), 0.2f, 0.3f), rr(0.3f, 0.6f), 0.06f, 0xD8FFE8, 1f, 0, 3f, GLOW | TWINKLE | POP);
            }
            case EJDERATESI -> {
                for (int n = cnt(14, k); n > 0; n--) {
                    i = add(SP_FLAME, 0.28f, burstVel(rr(0.7f, 1.7f) * sp, 0.4f, 1.2f), rr(0.45f, 0.8f), rr(0.08f, 0.14f),
                            pick(0xC040FF, 0xFF4FD8, 0x8A2BE2), 1f, -1.4f, 3f, GLOW | POP);
                    upright(i);
                }
                for (int n = cnt(8, k); n > 0; n--)
                    add(SP_STREAK, 0.2f, burstVel(rr(2.5f, 4.5f) * sp, 0.5f, 0.8f), rr(0.4f, 0.7f), 0.04f, 0xFF9CF0, 1f, 3f, 1.5f, STREAK | GLOW);
            }
            case BUYU -> {
                for (int n = cnt(18, k); n > 0; n--) {
                    i = add(n % 3 == 0 ? SP_CROSS : SP_SPARK, 0.45f, burstVel(rr(0.4f, 1.3f) * sp, 0.3f, 0.7f), rr(0.7f, 1.2f), rr(0.03f, 0.07f),
                            pick(0xFF5FD8, 0x5FF0FF, 0xFFFFFF, 0xC88BFF), 1f, -0.4f, 1.8f, GLOW | TWINKLE | POP);
                    spin(i, -3, 3);
                }
            }
            case PATLAMA -> {
                add(SP_RING, 0f, burstVel(0, 0, 0), 0.4f, 0.3f, 0xFFB040, 1f, 0, 0, RING | GLOW);
                add(SP_ORB, 0f, burstVel(0, 0, 0), 0.2f, 0.42f, 0xFFE0A0, 0.9f, 0, 0, FLASH | GLOW);
                for (int n = cnt(8, k); n > 0; n--) {
                    i = add(SP_SMOKE, 0.3f, burstVel(rr(0.8f, 1.8f), 0.3f, 0.8f), rr(0.8f, 1.3f), rr(0.14f, 0.24f), pick(0x3A3330, 0x2A2522, 0x5A4A40), 0.8f, -0.5f, 2.4f, POP);
                    spin(i, -1, 1);
                }
                for (int n = cnt(14, k); n > 0; n--)
                    add(SP_STREAK, 0.15f, burstVel(rr(3.5f, 6.5f) * sp, 0.3f, 0.6f), rr(0.3f, 0.55f), 0.04f, pick(0xFFC040, 0xFF7A1F, 0xFFF0A0), 1f, 4f, 1.4f,
                            STREAK | GLOW);
            }
            case MUREKKEP -> {
                for (int n = cnt(14, k); n > 0; n--) {
                    i = add(SP_DROP, 0.2f, burstVel(rr(2.2f, 4.2f) * sp, 0.8f, 1.0f), rr(0.6f, 1.0f), rr(0.05f, 0.1f),
                            pick(0x14141E, 0x1E1A3A, 0x0A0A12, 0x2A1E4A), 1f, 9f, 0.6f, 0);
                    upright(i);
                }
                for (int n = cnt(5, k); n > 0; n--) {
                    i = add(SP_SMOKE, 0.3f, burstVel(rr(0.4f, 0.9f), 0.4f, 0.3f), rr(0.9f, 1.4f), rr(0.14f, 0.22f), pick(0x1A1830, 0x26223F), 0.7f, -0.2f, 2.2f, POP);
                    spin(i, -0.8f, 0.8f);
                }
                if (crit) add(SP_RING, 0f, burstVel(0, 0, 0), 0.45f, 0.24f, 0x3A2E7A, 0.85f, 0, 0, RING);
            }
            case FISEK -> {
                int c0 = hsv(rnd(), 0.75f, 1f), c1 = hsv(rnd(), 0.6f, 1f);
                for (int n = cnt(7, k); n > 0; n--)
                    add(SP_STREAK, 0.15f, burstVel(rr(4f, 6.5f) * sp, 0.2f, 2.6f), rr(0.35f, 0.55f), 0.045f, pick(0xFFF0C0, c0), 1f, 5f, 1.2f, STREAK | GLOW);
                for (int n = cnt(16, k); n > 0; n--) {
                    i = add(n % 2 == 0 ? SP_STAR : SP_CROSS, 0.3f, burstVel(rr(1.6f, 3.4f) * sp, 0.2f, 1.2f), rr(0.7f, 1.2f), rr(0.04f, 0.08f),
                            n % 3 == 0 ? c1 : c0, 1f, 1.6f, 1.6f, GLOW | TWINKLE | POP);
                    spin(i, -5, 5);
                }
                add(SP_RING, 0f, burstVel(0, 0, 0), 0.4f, 0.26f, c0, 0.9f, 0, 0, RING | GLOW);
            }
            case PIKSEL -> {
                for (int n = cnt(16, k); n > 0; n--) {
                    i = add(SP_CONFETTI, 0.25f, burstVel(rr(2f, 3.8f) * sp, 0.5f, 1.6f), rr(0.8f, 1.3f), rr(0.05f, 0.09f),
                            pick(0xFF004D, 0xFFA300, 0xFFEC27, 0x00E436, 0x29ADFF, 0x83769C, 0xFF77A8), 1f, 9f, 0.4f, 0);
                    if (i >= 0) rot[i] = 0;
                }
                if (crit) for (int n = 6; n > 0; n--) {
                    i = add(SP_CONFETTI, 0.1f, burstVel(rr(0.6f, 1.2f), 0, 0.4f), 0.35f, 0.12f, 0xFFFFFF, 1f, 0, 3f, GLOW | POP);
                    if (i >= 0) rot[i] = 0;
                }
            }
            case OKYANUS -> {
                for (int n = cnt(14, k); n > 0; n--) {
                    i = add(SP_DROP, 0.22f, burstVel(rr(2f, 3.8f) * sp, 0.5f, 1.8f), rr(0.6f, 1.0f), rr(0.04f, 0.08f),
                            pick(0x2E9BFF, 0x59C3FF, 0x9EE6FF, 0x1E6FD8), 0.95f, 9f, 0.7f, 0);
                    upright(i);
                }
                for (int n = cnt(7, k); n > 0; n--) {
                    i = add(SP_BUBBLE, 0.3f, burstVel(rr(0.6f, 1.4f), 0.3f, 0.9f), rr(0.6f, 1.1f), rr(0.04f, 0.08f), 0xCFF2FF, 0.85f, -1.0f, 2.4f, WOBBLE | POP);
                    upright(i);
                }
                add(SP_RING, 0f, burstVel(0, 0, 0), 0.45f, 0.26f, 0x59C3FF, 0.8f, 0, 0, RING | GLOW);
            }
            case KUM -> {
                for (int n = cnt(18, k); n > 0; n--) {
                    float a = rr(0, (float) (Math.PI * 2)), sv = rr(1.4f, 2.4f) * sp;
                    tv[0] = -(float) Math.sin(a) * sv;
                    tv[1] = rr(0.1f, 0.9f);
                    tv[2] = (float) Math.cos(a) * sv;
                    i = add(SP_ORB, 0f, tv, rr(0.7f, 1.1f), rr(0.025f, 0.045f), pick(0xE6C88A, 0xD8B070, 0xF2DDAA, 0xC89A5A), 1f, 0.8f, 0.8f, SPIRAL | POP);
                    if (i >= 0) {
                        double r = rr(0.3f, 0.5f);
                        x[i] = bx + Math.cos(a) * r;
                        y[i] = by + rr(-0.5f, 0.4f);
                        z[i] = bz + Math.sin(a) * r;
                    }
                }
                for (int n = cnt(5, k); n > 0; n--) {
                    i = add(SP_SMOKE, 0.35f, burstVel(rr(0.5f, 1f), 0.4f, 0.2f), rr(0.9f, 1.4f), rr(0.16f, 0.26f), pick(0xC8A878, 0xB89868), 0.55f, -0.1f, 2f, POP);
                    spin(i, -1, 1);
                }
            }
            case PLAZMA -> {
                for (int n = cnt(12, k); n > 0; n--) {
                    float a = rr(0, (float) (Math.PI * 2)), sv = rr(1.8f, 2.8f) * sp;
                    tv[0] = -(float) Math.sin(a) * sv;
                    tv[1] = rr(-0.4f, 0.8f);
                    tv[2] = (float) Math.cos(a) * sv;
                    i = add(SP_ORB, 0f, tv, rr(0.6f, 0.95f), rr(0.04f, 0.07f), pick(0x3DF5FF, 0xFF4FE0, 0xB0FFFF), 1f, 0, 0.5f, SPIRAL | GLOW | POP);
                    if (i >= 0) {
                        x[i] = bx + Math.cos(a) * 0.35;
                        z[i] = bz + Math.sin(a) * 0.35;
                    }
                }
                for (int n = cnt(8, k); n > 0; n--)
                    add(SP_STREAK, 0.15f, burstVel(rr(3f, 5f) * sp, 0.4f, 0.4f), rr(0.25f, 0.45f), 0.035f, pick(0x9EFFFF, 0xFF9CF0), 1f, 0, 2f, STREAK | GLOW);
                add(SP_ORB, 0f, burstVel(0, 0, 0), 0.18f, 0.3f, 0xB0FFFF, 0.75f, 0, 0, FLASH | GLOW);
            }
            case GOLGE -> {
                for (int n = cnt(8, k); n > 0; n--) {
                    i = add(SP_SMOKE, 0.3f, burstVel(rr(0.5f, 1.2f), 0.3f, 1.0f), rr(1.0f, 1.6f), rr(0.15f, 0.26f), pick(0x0E0A16, 0x1C1028, 0x241634), 0.85f, -0.5f, 2.2f, POP);
                    spin(i, -0.9f, 0.9f);
                }
                for (int n = cnt(9, k); n > 0; n--)
                    add(SP_ORB, 0.3f, burstVel(rr(0.6f, 1.4f) * sp, 0.3f, 1.1f), rr(0.7f, 1.1f), rr(0.035f, 0.06f), pick(0x7A2BFF, 0xB070FF, 0x4A1A8A), 1f, -0.6f, 2f,
                            GLOW | TWINKLE | POP);
                add(SP_RING, 0f, burstVel(0, 0, 0), 0.5f, 0.24f, 0x2A1048, 0.9f, 0, 0, RING);
            }
            case SEKER -> {
                for (int n = cnt(12, k); n > 0; n--) {
                    int sprite = n % 3 == 0 ? SP_HEART : n % 3 == 1 ? SP_STAR : SP_CONFETTI;
                    i = add(sprite, 0.25f, burstVel(rr(1.6f, 3.2f) * sp, 0.4f, 1.5f), rr(0.8f, 1.3f), rr(0.05f, 0.1f),
                            pick(0xFFB3D9, 0xB3E5FF, 0xFFF3B0, 0xC8FFD4, 0xE0C8FF, 0xFFC8A8), 1f, 3.5f, 1.2f, POP | (sprite == SP_CONFETTI ? FLIP : 0));
                    spin(i, -6, 6);
                }
                for (int n = cnt(6, k); n > 0; n--)
                    add(SP_CROSS, 0.3f, burstVel(rr(0.6f, 1.4f), 0.2f, 0.4f), rr(0.3f, 0.6f), 0.05f, 0xFFFFFF, 1f, 0, 3f, GLOW | TWINKLE | POP);
            }
            case ATES_BOCEGI -> {
                for (int n = cnt(9, k); n > 0; n--)
                    add(SP_ORB, 0.3f, burstVel(rr(0.8f, 1.6f) * sp, 0.4f, 0.9f), rr(1.6f, 2.4f), rr(0.035f, 0.055f), pick(0xE8FF5A, 0xC8FF3A, 0xFFF07A),
                            1f, -0.1f, 1.4f, BUTTERFLY | GLOW | TWINKLE | POP);
                for (int n = cnt(4, k); n > 0; n--)
                    add(SP_CROSS, 0.25f, burstVel(rr(0.5f, 1f), 0.2f, 0.4f), rr(0.3f, 0.5f), 0.045f, 0xF4FFB0, 1f, 0, 3f, GLOW | TWINKLE | POP);
            }
            default -> {
            }
        }
        // kritik: merkezde kisa bir isik patlamasi (vurusu hissettirir)
        if (crit && count > before) add(SP_ORB, 0f, burstVel(0f, 0, 0), 0.16f, 0.32f, mainColor(st), 0.65f, 0, 0, FLASH | GLOW);
        return count - before;
    }

    /**
     * Totem patlamasi: altin + yesil isik halkalari, govde etrafinda yukselen sarmal kureler, goz kirpan isiltilar
     * ve dusen altin yildizlar.
     *
     * @param h  hedefin boyu (blok)
     * @param scale boyut/adet carpani
     */
    public int totem(double px, double py, double pz, float h, float scale, int lightPacked) {
        if (!Double.isFinite(px + py + pz) || !(scale > 0) || !(h > 0)) return 0;
        int before = count;
        dX = 0;
        dZ = 0;
        bx = px;
        by = py + h * 0.55;
        bz = pz;
        bFloor = py + 0.02;
        bLight = lightPacked;
        bSize = Math.max(0.3f, Math.min(2.5f, scale));
        float k = Math.min(2.5f, scale);
        add(SP_RING, 0f, burstVel(0, 0, 0), 0.5f, 0.42f, 0xFFD54F, 1f, 0, 0, RING | GLOW);
        add(SP_RING, 0f, burstVel(0, 0, 0), 0.62f, 0.32f, 0x7CFF6A, 0.9f, 0, 0, RING | GLOW);
        add(SP_ORB, 0f, burstVel(0, 0, 0), 0.22f, 0.5f, 0xFFF3B0, 0.8f, 0, 0, FLASH | GLOW);
        for (int n = cnt(34, k); n > 0; n--) {
            float a = rr(0, (float) (Math.PI * 2)), sv = rr(1.4f, 2.4f);
            tv[0] = -(float) Math.sin(a) * sv;
            tv[1] = rr(1.0f, 2.4f);
            tv[2] = (float) Math.cos(a) * sv;
            int i = add(SP_ORB, 0f, tv, rr(1.1f, 1.7f), rr(0.05f, 0.09f), (n & 1) == 0 ? pick(0xFFE45C, 0xFFD23F) : pick(0x8CFF5A, 0x5CF59A),
                    1f, -0.4f, 0.5f, SPIRAL | GLOW | POP);
            if (i >= 0) {
                float r = rr(0.35f, 0.6f);
                x[i] = bx + Math.cos(a) * r;
                y[i] = py + rr(0f, h);
                z[i] = bz + Math.sin(a) * r;
            }
        }
        for (int n = cnt(14, k); n > 0; n--)
            add(SP_SPARK, 0.5f, burstVel(rr(1f, 2.5f), 0, 0.8f), rr(0.6f, 1.0f), rr(0.06f, 0.12f), pick(0xFFF5C2, 0xFFE27A, 0xC8FF9A), 1f, 0, 2.5f, GLOW | TWINKLE | POP);
        for (int n = cnt(10, k); n > 0; n--) {
            int i = add(SP_STAR, 0.3f, burstVel(rr(2f, 3.5f), 0, 1.6f), rr(0.9f, 1.3f), rr(0.06f, 0.1f), pick(0xFFD54F, 0xFFE082), 1f, 7f, 0.8f, POP);
            spin(i, -6, 6);
        }
        return count - before;
    }

    private int cnt(int base, float k) {
        float f = base * k;
        int n = (int) f;
        if (rnd() < f - n) n++;
        return Math.max(f > 0.01f ? 1 : 0, n);
    }

    public static int mainColor(HitStyle st) {
        return switch (st) {
            case KOPUK -> 0x7CFF6A;
            case KELEBEK -> 0xFF8AD0;
            case KALP -> 0xFF4D6D;
            case ISILTI, YILDIZ, PARA -> 0xFFE27A;
            case ALEV -> 0xFF9A2E;
            case RUH -> 0x52E3FF;
            case KAR -> 0xE6F7FF;
            case ELMAS -> 0x6EF3FF;
            case KAN -> 0xFF3030;
            case SIMSEK -> 0xFFF59D;
            case SAKURA -> 0xFFB7D5;
            case YAPRAK -> 0x8BC34A;
            case NOTA, GOKKUSAGI, KONFETI -> 0xFFFFFF;
            case ENDER -> 0xC86BFF;
            case KAFATASI -> 0xB0B0B0;
            case SOK -> 0xCFF6FF;
            case NEON -> 0x00F5FF;
            case TUY -> 0xFFF8E0;
            case HILAL -> 0xC8D8FF;
            case PAPATYA -> 0xFFE066;
            case BUZ -> 0xBFF4FF;
            case GIRDAP -> 0x5FD8FF;
            case TAC -> 0xFFD23F;
            case RUN -> 0x5FFFE0;
            case YARASA -> 0xFF3040;
            case PATI -> 0xFF9EC4;
            case YONCA -> 0x3DDC5A;
            case KESIK -> 0xE8FBFF;
            case VOLKAN -> 0xFF6A00;
            case ZEHIR -> 0x8CFF3A;
            case GALAKSI -> 0x9B7BFF;
            case KARADELIK -> 0xB070FF;
            case BALON -> 0xFF5A7A;
            case ZUMRUT -> 0x2EE07A;
            case EJDERATESI -> 0xC040FF;
            case BUYU -> 0xFF5FD8;
            case PATLAMA -> 0xFFB040;
            case MUREKKEP -> 0x3A2E7A;
            case FISEK -> 0xFFF0C0;
            case PIKSEL -> 0x29ADFF;
            case OKYANUS -> 0x59C3FF;
            case KUM -> 0xE6C88A;
            case PLAZMA -> 0x3DF5FF;
            case GOLGE -> 0x7A2BFF;
            case SEKER -> 0xFFB3D9;
            case ATES_BOCEGI -> 0xE8FF5A;
            default -> 0xFFFFFF;
        };
    }

    // ------------------------------------------------------------------ simulasyon

    public void update(float dt) {
        if (!(dt > 0f)) return;
        if (dt > 0.1f) dt = 0.1f;
        for (int i = 0; i < count; ) {
            age[i] += dt;
            if (!(age[i] < life[i])) {
                removeAt(i);
                continue;
            }
            int f = flags[i];
            vy[i] -= grav[i] * dt;
            if (drag[i] > 0f) {
                float k = (float) Math.exp(-drag[i] * dt);
                vx[i] *= k;
                vy[i] *= k;
                vz[i] *= k;
            }
            if ((f & SPIRAL) != 0) {
                // merkeze cekim: teget hizla birlikte sarmal yorunge
                vx[i] += (float) (ox[i] - x[i]) * 16f * dt;
                vz[i] += (float) (oz[i] - z[i]) * 16f * dt;
            }
            if ((f & BUTTERFLY) != 0) {
                float a = age[i], s = seed[i] * 6.283f;
                vx[i] += (float) Math.sin(a * 2.3f + s) * 3f * dt;
                vz[i] += (float) Math.cos(a * 1.9f + s * 1.3f) * 3f * dt;
                vy[i] += (float) Math.sin(a * 4.1f + s) * 2f * dt;
                float sp = (float) Math.sqrt(vx[i] * vx[i] + vy[i] * vy[i] + vz[i] * vz[i]);
                if (sp > 2.2f) {
                    float k = 2.2f / sp;
                    vx[i] *= k;
                    vy[i] *= k;
                    vz[i] *= k;
                }
            }
            double wx = 0, wz = 0;
            if ((f & WOBBLE) != 0) {
                float a = age[i], s = seed[i] * 6.283f;
                wx = Math.sin(a * 4f + s) * 0.45;
                wz = Math.cos(a * 3.3f + s) * 0.45;
            }
            x[i] += (vx[i] + wx) * dt;
            y[i] += vy[i] * dt;
            z[i] += (vz[i] + wz) * dt;
            // zemin: dusen partikul yerin icine girmez, hafifce seker ve kayarak durur
            double fy = floor[i] + size[i] * 0.5;
            if (y[i] < fy && vy[i] < 0f && grav[i] > 0f) {
                y[i] = fy;
                vy[i] = -vy[i] * 0.28f;
                if (vy[i] < 0.4f) vy[i] = 0f;
                vx[i] *= 0.55f;
                vz[i] *= 0.55f;
                vrot[i] *= 0.5f;
            }
            rot[i] += vrot[i] * dt;
            if (rot[i] > 1000f || rot[i] < -1000f) rot[i] %= (float) (Math.PI * 2);
            i++;
        }
    }

    private void removeAt(int i) {
        int j = --count;
        if (i == j) return;
        x[i] = x[j];
        y[i] = y[j];
        z[i] = z[j];
        ox[i] = ox[j];
        oz[i] = oz[j];
        floor[i] = floor[j];
        vx[i] = vx[j];
        vy[i] = vy[j];
        vz[i] = vz[j];
        age[i] = age[j];
        life[i] = life[j];
        size[i] = size[j];
        rot[i] = rot[j];
        vrot[i] = vrot[j];
        grav[i] = grav[j];
        drag[i] = drag[j];
        seed[i] = seed[j];
        alpha0[i] = alpha0[j];
        rgb[i] = rgb[j];
        sprite[i] = sprite[j];
        flags[i] = flags[j];
        light[i] = light[j];
    }

    // ------------------------------------------------------------------ cizim egrileri

    /** Boyut (blok, dortgenin yari genisligi). */
    public float scale(int i) {
        float t = age[i] / life[i], s = size[i];
        int f = flags[i];
        if ((f & RING) != 0) return s * (0.3f + 2.4f * (1 - (1 - t) * (1 - t)));
        if ((f & POP) != 0 && t < 0.14f) {
            float u = t / 0.14f;
            // hafif tasan acilis (easeOutBack)
            float c = 1.7f;
            s *= 1 + (c + 1) * (float) Math.pow(u - 1, 3) + c * (float) Math.pow(u - 1, 2);
        }
        if (t > 0.78f) s *= 1 - (t - 0.78f) / 0.22f * 0.65f;
        return Math.max(0f, s);
    }

    public float alpha(int i) {
        float t = age[i] / life[i];
        int f = flags[i];
        float a = alpha0[i];
        if ((f & (FLASH | RING)) != 0) return a * (1 - t) * (1 - t);
        if (t < 0.06f) a *= t / 0.06f;
        if (t > 0.62f) a *= 1 - (t - 0.62f) / 0.38f;
        if ((f & TWINKLE) != 0) a *= 0.55f + 0.45f * (float) Math.sin(age[i] * 26f + seed[i] * 40f);
        return clamp01(a);
    }

    /** Kelebek kanat acisi (radyan, 0 = yatay). */
    public float flap(int i) {
        // V bicimi: kanatlar hic tam yatay olmaz (yandan bakinca da kelebek gorunur)
        return 0.38f + 1.1f * Math.abs((float) Math.sin(age[i] * 15f + seed[i] * 9f));
    }

    /** Para / yaprak / konfeti donmesi: yatay genislik carpani (0.15..1). */
    public float flipX(int i) {
        if ((flags[i] & FLIP) == 0) return 1f;
        return 0.15f + 0.85f * Math.abs((float) Math.cos(age[i] * 9f + seed[i] * 7f));
    }

    public static float u0(int sp) {
        return (sp % ATLAS_COLS) / (float) ATLAS_COLS;
    }

    public static float v0(int sp) {
        return (sp / ATLAS_COLS) / (float) ATLAS_ROWS;
    }

    public static final float DU = 1f / ATLAS_COLS, DV = 1f / ATLAS_ROWS;
}
