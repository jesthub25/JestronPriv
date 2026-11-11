package com.jestron.jestronpriv.itemskin;

/**
 * Cizimden 3B esya modeli: vanilla "item/generated" gibi on / arka yuz + her opak pikselin acik kenarlarindan 1/16
 * kalinlikta yan seritler (yan yana kenarlar tek dortgende birlesir). Uzay: 0..1 kup (vanilla esya modeli gibi),
 * on yuz +Z. Her dortgen saat yonunun tersine (disaridan bakinca) dizilir. Minecraft'a bagimsiz.
 */
public final class SkinMesh {

    /** Dortgen basina kayan sayi: 4 kose x (x, y, z, u, v) + normal (3). */
    public static final int STRIDE = 4 * 5 + 3;
    /** Kenar dortgeni ust siniri: asiri karmasik cizimde sadece on / arka yuz (GUI'de yuzlerce esya ayni anda cizilir). */
    public static final int MAX_EDGE_QUADS = 1500;
    public static final float Z0 = 7.5f / 16f, Z1 = 8.5f / 16f;

    public final float[] data;
    public final int quads;

    private SkinMesh(float[] data, int quads) {
        this.data = data;
        this.quads = quads;
    }

    private static final class Builder {
        float[] d = new float[STRIDE * 64];
        int n;

        void quad(float[] p, float nx, float ny, float nz) {
            // sirayi normale gore duzelt: (b-a) x (c-a) normal yonunde olmali
            float ax = p[0], ay = p[1], az = p[2];
            float bx = p[5] - ax, by = p[6] - ay, bz = p[7] - az;
            float cx = p[10] - ax, cy = p[11] - ay, cz = p[12] - az;
            float rx = by * cz - bz * cy, ry = bz * cx - bx * cz, rz = bx * cy - by * cx;
            boolean flip = rx * nx + ry * ny + rz * nz < 0f;
            if ((n + 1) * STRIDE > d.length) d = java.util.Arrays.copyOf(d, d.length * 2);
            int o = n * STRIDE;
            for (int i = 0; i < 4; i++) {
                int src = flip ? (3 - i) : i;
                System.arraycopy(p, src * 5, d, o + i * 5, 5);
            }
            d[o + 20] = nx;
            d[o + 21] = ny;
            d[o + 22] = nz;
            n++;
        }
    }

    private static float[] v(float... f) {
        return f;
    }

    public static SkinMesh build(SkinImage img) {
        Builder b = new Builder();
        int w = img.w, h = img.h;
        // on ve arka yuz: tum doku (saydam pikseller golgelendiricide atilir)
        b.quad(v(0, 0, Z1, 0, 1, 1, 0, Z1, 1, 1, 1, 1, Z1, 1, 0, 0, 1, Z1, 0, 0), 0, 0, 1);
        b.quad(v(0, 0, Z0, 0, 1, 0, 1, Z0, 0, 0, 1, 1, Z0, 1, 0, 1, 0, Z0, 1, 1), 0, 0, -1);
        int edges = 0;
        float fw = 1f / w, fh = 1f / h;
        // ust / alt kenarlar: satir satir, yan yana pikseller birlesir
        for (int py = 0; py < h; py++) {
            for (int dir = 0; dir < 2; dir++) {
                int ny = dir == 0 ? py - 1 : py + 1;
                int px = 0;
                while (px < w) {
                    if (!img.opaque(px, py) || img.opaque(px, ny)) {
                        px++;
                        continue;
                    }
                    int s = px;
                    while (px < w && img.opaque(px, py) && !img.opaque(px, ny)) px++;
                    float x0 = s * fw, x1 = px * fw;
                    float y = dir == 0 ? 1f - py * fh : 1f - (py + 1) * fh;
                    float vv = (py + 0.5f) * fh;
                    b.quad(v(x0, y, Z0, x0 + 0.5f * fw, vv, x1, y, Z0, x1 - 0.5f * fw, vv, x1, y, Z1, x1 - 0.5f * fw, vv, x0, y, Z1, x0 + 0.5f * fw, vv), 0,
                            dir == 0 ? 1 : -1, 0);
                    if (++edges >= MAX_EDGE_QUADS) return frontBackOnly(img);
                }
            }
        }
        // sol / sag kenarlar: sutun sutun
        for (int px = 0; px < w; px++) {
            for (int dir = 0; dir < 2; dir++) {
                int nx = dir == 0 ? px - 1 : px + 1;
                int py = 0;
                while (py < h) {
                    if (!img.opaque(px, py) || img.opaque(nx, py)) {
                        py++;
                        continue;
                    }
                    int s = py;
                    while (py < h && img.opaque(px, py) && !img.opaque(nx, py)) py++;
                    float yTop = 1f - s * fh, yBot = 1f - py * fh;
                    float x = dir == 0 ? px * fw : (px + 1) * fw;
                    float uu = (px + 0.5f) * fw;
                    b.quad(v(x, yBot, Z0, uu, py * fh - 0.5f * fh, x, yTop, Z0, uu, s * fh + 0.5f * fh, x, yTop, Z1, uu, s * fh + 0.5f * fh, x, yBot, Z1, uu,
                            py * fh - 0.5f * fh), dir == 0 ? -1 : 1, 0, 0);
                    if (++edges >= MAX_EDGE_QUADS) return frontBackOnly(img);
                }
            }
        }
        return new SkinMesh(java.util.Arrays.copyOf(b.d, b.n * STRIDE), b.n);
    }

    private static SkinMesh frontBackOnly(SkinImage img) {
        Builder b = new Builder();
        b.quad(v(0, 0, Z1, 0, 1, 1, 0, Z1, 1, 1, 1, 1, Z1, 1, 0, 0, 1, Z1, 0, 0), 0, 0, 1);
        b.quad(v(0, 0, Z0, 0, 1, 0, 1, Z0, 0, 0, 1, 1, Z0, 1, 0, 1, 0, Z0, 1, 1), 0, 0, -1);
        return new SkinMesh(java.util.Arrays.copyOf(b.d, b.n * STRIDE), b.n);
    }
}
