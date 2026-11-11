package com.jestron.jestronpriv.tag;

/**
 * Isim etiketi renk ve hareket modlari (MC'den bagimsiz). Her harfin rengi ve dikey kaymasi zamana gore hesaplanir.
 */
public final class TagStyle {

    public static final String[] COLOR_MODES = {"Tek renk", "Gradyan (2 renk)", "Gradyan (3 renk)", "Gökkuşağı", "Akan gökkuşağı", "Nabız",
            "Parıltı süpürmesi", "Ateş", "Buz", "Neon titreşim", "Parti", "Altın", "Okyanus", "Zehir", "Gün batımı", "Galaksi"};
    public static final String[] ANIMS = {"Yok", "Dalga", "Zıplama", "Süzülme", "Titreme", "Nefes", "Daktilo", "Salıncak"};
    public static final String[] EMOJI_ANIMS = {"Yok", "Zıplama", "Dönme", "Nabız", "Sallanma"};

    private TagStyle() {
    }

    public static int modeCount() {
        return COLOR_MODES.length;
    }

    /**
     * Harf rengi (rgb).
     *
     * @param i harf sirasi, n toplam harf, t saniye (hiz carpanli)
     */
    public static int color(int mode, int c1, int c2, int c3, int i, int n, float t) {
        float u = n <= 1 ? 0f : i / (float) (n - 1);
        switch (mode) {
            case 0:
                return c1;
            case 1:
                return mix(c1, c2, u);
            case 2:
                return u < 0.5f ? mix(c1, c2, u * 2f) : mix(c2, c3, (u - 0.5f) * 2f);
            case 3:
                return hsv(u * 0.85f, 0.75f, 1f);
            case 4:
                return hsv(u * 0.6f - t * 0.35f, 0.75f, 1f);
            case 5: {
                float k = 0.5f + 0.5f * (float) Math.sin(t * 3f);
                return mix(c1, c2, k);
            }
            case 6: {
                // c1 zemininde soldan saga beyaz parilti kayar
                float pos = (t * 0.6f) % 1.6f - 0.3f;
                float d = Math.abs(u - pos);
                float k = Math.max(0f, 1f - d / 0.18f);
                return mix(c1, 0xFFFFFF, k * 0.85f);
            }
            case 7: {
                float k = 0.5f + 0.5f * (float) Math.sin(t * 6f + i * 1.3f) * (float) Math.sin(t * 2.3f + i * 0.7f);
                return mix(0xFF3A0A, 0xFFE24A, k);
            }
            case 8: {
                float k = 0.5f + 0.5f * (float) Math.sin(t * 2f + u * 4f);
                return mix(0x7FD8FF, 0xF0FCFF, k);
            }
            case 9: {
                // ara sira bir harf soner (neon tabela)
                int h = hash((int) Math.floor(t * 8f) * 131 + i * 17);
                boolean off = (h & 31) == 0;
                return off ? shade(c1, 0.35f) : c1;
            }
            case 10: {
                int h = hash((int) Math.floor(t * 4f) * 71 + i * 13);
                return hsv((h & 255) / 255f, 0.8f, 1f);
            }
            case 11: {
                float pos = (t * 0.5f) % 1.4f - 0.2f;
                float k = Math.max(0f, 1f - Math.abs(u - pos) / 0.15f);
                return mix(mix(0xC9922A, 0xFFD54A, 0.5f + 0.5f * (float) Math.sin(u * 6f)), 0xFFF6D0, k);
            }
            case 12:
                return mix(0x0A5AA8, 0x3AE8E0, 0.5f + 0.5f * (float) Math.sin(t * 1.8f + u * 5f));
            case 13:
                return mix(0x2FAA2A, 0xB8FF3A, 0.5f + 0.5f * (float) Math.sin(t * 2.5f + u * 7f));
            case 14:
                return u < 0.5f ? mix(0xFF5A3A, 0xFFB23A, u * 2f) : mix(0xFFB23A, 0xB03AFF, (u - 0.5f) * 2f);
            case 15:
                return mix(mix(0x3A1A8A, 0xB03AFF, 0.5f + 0.5f * (float) Math.sin(t * 1.3f + u * 6f)), 0xFFFFFF,
                        (hash((int) Math.floor(t * 6f) * 37 + i * 7) & 15) == 0 ? 0.8f : 0f);
            default:
                return c1;
        }
    }

    /** Harfin dikey kaymasi (etiket pikseli; - yukari). */
    public static float offsetY(int anim, int i, int n, float t) {
        switch (anim) {
            case 1:
                return (float) Math.sin(t * 5f - i * 0.6f) * 1.6f;
            case 2:
                return -Math.abs((float) Math.sin(t * 3.2f)) * 2.5f;
            case 3:
                return (float) Math.sin(t * 1.6f) * 1.4f;
            case 4: {
                int h = hash((int) Math.floor(t * 20f) * 97 + i * 31);
                return ((h & 7) - 3.5f) * 0.18f;
            }
            case 7:
                return (float) Math.sin(t * 2.5f + i * 0.35f) * 0.8f;
            default:
                return 0f;
        }
    }

    /** Harfin yatay kaymasi (titreme / salincak). */
    public static float offsetX(int anim, int i, int n, float t) {
        if (anim == 4) {
            int h = hash((int) Math.floor(t * 20f) * 53 + i * 19);
            return (((h >>> 4) & 7) - 3.5f) * 0.18f;
        }
        if (anim == 7) return (float) Math.sin(t * 2.5f) * 1.5f;
        return 0f;
    }

    /** Olcek carpani (nefes). */
    public static float scale(int anim, float t) {
        return anim == 5 ? 1f + 0.06f * (float) Math.sin(t * 2.4f) : 1f;
    }

    /** Daktilo: gorunen harf sayisi (dongulu). */
    public static int visible(int anim, int n, float t) {
        if (anim != 6 || n <= 0) return n;
        float cycle = n * 0.12f + 2.2f;
        float k = t % cycle;
        return Math.min(n, (int) Math.floor(k / 0.12f) + 1);
    }

    /** Emoji hareketi: dikey kayma, yatay olcek (donme), olcek. out = {dy, sx, s, rot}. */
    public static void emoji(int anim, int i, float t, float[] out) {
        out[0] = 0f;
        out[1] = 1f;
        out[2] = 1f;
        out[3] = 0f;
        switch (anim) {
            case 1 -> out[0] = -Math.abs((float) Math.sin(t * 3.5f + i * 0.9f)) * 2.2f;
            case 2 -> out[1] = (float) Math.cos(t * 2.5f + i * 0.7f);
            case 3 -> out[2] = 1f + 0.15f * (float) Math.sin(t * 4f + i);
            case 4 -> out[3] = (float) Math.sin(t * 3f + i * 0.8f) * 0.35f;
            default -> {
            }
        }
    }

    // ------------------------------------------------------------------ renk yardimcilari

    public static int mix(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255, br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        return (Math.round(ar + (br - ar) * t) << 16) | (Math.round(ag + (bg - ag) * t) << 8) | Math.round(ab + (bb - ab) * t);
    }

    public static int shade(int rgb, float k) {
        int r = Math.min(255, Math.round(((rgb >> 16) & 255) * k)), g = Math.min(255, Math.round(((rgb >> 8) & 255) * k)),
                b = Math.min(255, Math.round((rgb & 255) * k));
        return (r << 16) | (g << 8) | b;
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

    private static int hash(int x) {
        x ^= x >>> 16;
        x *= 0x7feb352d;
        x ^= x >>> 15;
        x *= 0x846ca68b;
        x ^= x >>> 16;
        return x;
    }
}
