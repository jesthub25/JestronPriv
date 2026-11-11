package com.jestron.jestronpriv.cape;

import static com.jestron.jestronpriv.cape.CapeArt.*;

/**
 * Pelerin dokusu ureteci (Minecraft'a bagimsiz). Duzen (128x64):
 * <pre>
 *   x 0..39   dis yuz (desen)            40x64
 *   x 40..79  ic yuz (sirta bakan taraf) 40x64
 *   x 80..83  kenar (kalinlik seritleri)  4x64
 * </pre>
 * Iki dizi uretilir: taban (opak ARGB) ve isilti (siyah = isik yok; karanlikta parlayan kisimlar, toplamali cizilir).
 */
public final class CapeTexture {

    public static final int W = 40, H = 64;
    public static final int TEX_W = 128, TEX_H = 64;
    public static final int INNER_X = 40, EDGE_X = 80, EDGE_W = 4;

    /** Uretim secenekleri. */
    public static final class Options {
        /** Ton kaydirma (derece, -180..180). */
        public float hue;
        /** Akan gokkusagi tonu. */
        public boolean rainbow;
        public float rainbowSpeed = 1f;
        /** Dis yuzde ince vurgu kenari. */
        public boolean trim = true;
        /** Ic yuz rengi otomatik (desenin koyu tonu) mu. */
        public boolean innerAuto = true;
        public int innerColor = 0x202020;
        /** Isilti gucu 0..1.5 (dokuya islenir). */
        public float glow = 1f;
        /** Isilti kapali mi. */
        public boolean glowOn = true;

        public Options copy() {
            Options o = new Options();
            o.hue = hue;
            o.rainbow = rainbow;
            o.rainbowSpeed = rainbowSpeed;
            o.trim = trim;
            o.innerAuto = innerAuto;
            o.innerColor = innerColor;
            o.glow = glow;
            o.glowOn = glowOn;
            return o;
        }

        public boolean same(Options o) {
            return o != null && hue == o.hue && rainbow == o.rainbow && rainbowSpeed == o.rainbowSpeed && trim == o.trim && innerAuto == o.innerAuto
                    && innerColor == o.innerColor && glow == o.glow && glowOn == o.glowOn;
        }
    }

    private final Px px = new Px();

    /** Desen dokusu bu zamanda degisir mi (animasyon ya da akan gokkusagi). */
    public static boolean animated(CapeDesign d, Options o) {
        return d.animated() || (o != null && o.rainbow);
    }

    /**
     * Tum dokuyu uretir.
     *
     * @param base ARGB, en az TEX_W*TEX_H
     * @param glow ARGB, en az TEX_W*TEX_H (null olabilir)
     */
    public void paint(CapeDesign d, Options o, float t, int[] base, int[] glow) {
        Px p = px;
        p.w = W;
        p.h = H;
        p.t = t;
        p.c1 = d.c1();
        p.c2 = d.c2();
        p.c3 = d.c3();
        float hue = o.hue;
        float gk = o.glowOn ? clamp(o.glow, 0f, 1.5f) : 0f;
        int trimC = d.c3();
        long sumR = 0, sumG = 0, sumB = 0;
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                p.at(x, y);
                try {
                    d.painter().paint(p);
                } catch (RuntimeException e) {
                    p.rgb = d.c1();
                    p.glow = 0f;
                }
                int c = p.rgb & 0xFFFFFF;
                float gl = clamp01(Float.isNaN(p.glow) ? 0f : p.glow);
                if (o.trim && (x == 0 || x == W - 1 || y == H - 1)) {
                    c = mix(trimC, 0xFFFFFF, 0.15f);
                    gl = Math.max(gl, 0.2f);
                }
                float h = hue;
                if (o.rainbow) h += t * 40f * clamp(o.rainbowSpeed, 0.1f, 4f) + p.v * 120f + p.u * 30f;
                if (h != 0f) c = hueRotate(c, h % 360f);
                sumR += r(c);
                sumG += g(c);
                sumB += b(c);
                base[y * TEX_W + x] = 0xFF000000 | c;
                if (glow != null) {
                    float k = gl * gk;
                    glow[y * TEX_W + x] = 0xFF000000 | (k <= 0.004f ? 0 : mul(c, Math.min(1f, k)));
                }
            }
        }
        int n = W * H;
        int avg = ((int) (sumR / n) << 16) | ((int) (sumG / n) << 8) | (int) (sumB / n);
        int inner = o.innerAuto ? mix(mul(avg, 0.45f), mul(d.c1(), 0.5f), 0.5f) : (o.innerColor & 0xFFFFFF);
        int edge = o.innerAuto ? mix(mul(avg, 0.6f), trimC, 0.35f) : mix(inner, trimC, 0.4f);
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                // kumas dokusu: ince capraz orgu ve hafif dikey golge
                float weave = ((x + y) & 1) == 0 ? 1.05f : 0.95f;
                float shade = 1.08f - y / (float) H * 0.22f;
                if (x == 0 || x == W - 1 || y == H - 1) shade *= 0.8f;
                base[y * TEX_W + INNER_X + x] = 0xFF000000 | mul(inner, weave * shade);
                if (glow != null) glow[y * TEX_W + INNER_X + x] = 0xFF000000;
            }
            for (int x = 0; x < EDGE_W; x++) {
                base[y * TEX_W + EDGE_X + x] = 0xFF000000 | mul(edge, x == 0 || x == EDGE_W - 1 ? 0.85f : 1f);
                if (glow != null) glow[y * TEX_W + EDGE_X + x] = 0xFF000000;
            }
        }
    }

    /**
     * Kucuk resim (2x2 ortalama ile W/2 x H/2) - menudeki izgara icin.
     *
     * @param out ARGB, (W/2)*(H/2)
     */
    public void thumbnail(CapeDesign d, Options o, float t, int[] out) {
        int[] base = new int[TEX_W * TEX_H];
        paint(d, o, t, base, null);
        int tw = W / 2, th = H / 2;
        for (int y = 0; y < th; y++) {
            for (int x = 0; x < tw; x++) {
                int a = base[(y * 2) * TEX_W + x * 2], b2 = base[(y * 2) * TEX_W + x * 2 + 1];
                int c = base[(y * 2 + 1) * TEX_W + x * 2], e = base[(y * 2 + 1) * TEX_W + x * 2 + 1];
                int rr = (r(a) + r(b2) + r(c) + r(e) + 2) >> 2, gg = (g(a) + g(b2) + g(c) + g(e) + 2) >> 2, bb = (b(a) + b(b2) + b(c) + b(e) + 2) >> 2;
                out[y * tw + x] = 0xFF000000 | (rr << 16) | (gg << 8) | bb;
            }
        }
    }
}
