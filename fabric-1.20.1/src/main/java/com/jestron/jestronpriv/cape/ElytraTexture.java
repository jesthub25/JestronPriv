package com.jestron.jestronpriv.cape;

import static com.jestron.jestronpriv.cape.CapeArt.*;

/**
 * Elytra dokusu ureteci (Minecraft'a bagimsiz). Vanilla elytra modeli 64x32 dokunun kanat bolgesini kullanir; burada
 * 4 kat cozunurlukte (256x128) uretilir: model UV'leri oransal oldugu icin ayni yerlere 4 kat detay duser.
 * <pre>
 *   kanat kutusu (10x20x2, doku konumu 22,0):
 *   dis yuz (sirttan disari bakan, +z)  x 36..46  y 2..22   desen
 *   ic yuz  (sirta bakan, -z)           x 24..34  y 2..22   desenin aynasi (koyu ya da ayni)
 *   kenarlar                            x 22..24 / 34..36, y 0..2 seritleri
 * </pre>
 * Dis yuzde u = 0 omuz (kanadin mentesesi), u = 1 diger kenar; v = 0 ust, v = 1 alt uc. Iki dizi uretilir: taban
 * (ARGB, kesik pikseller seffaf) ve isilti (siyah = isik yok).
 */
public final class ElytraTexture {

    public static final int K = 4;
    public static final int TEX_W = 64 * K, TEX_H = 32 * K;
    /** Dis yuz boyutu. */
    public static final int W = 10 * K, H = 20 * K;
    static final int OUT_X = 36 * K, IN_X = 24 * K, FACE_Y = 2 * K;
    /** Kucuk resim: iki kanat yan yana (2x kucultulmus). */
    public static final int THUMB_W = W, THUMB_H = H / 2;

    private final Px px = new Px();
    private final int[] faceRgb = new int[W * H], faceA = new int[W * H];
    private final float[] faceGlow = new float[W * H];

    public static boolean animated(CapeDesign d, CapeTexture.Options o) {
        return d.animated() || (o != null && o.rainbow);
    }

    /** Dis yuz (W x H) piksellerini hesaplar. */
    private void face(CapeDesign d, CapeTexture.Options o, float t) {
        Px p = px;
        p.w = W;
        p.h = H;
        p.t = t;
        p.c1 = d.c1();
        p.c2 = d.c2();
        p.c3 = d.c3();
        float gk = o.glowOn ? clamp(o.glow, 0f, 1.5f) : 0f;
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                p.at(x, y);
                try {
                    d.painter().paint(p);
                } catch (RuntimeException e) {
                    p.rgb = d.c1();
                    p.glow = 0f;
                    p.alpha = 1f;
                }
                int c = p.rgb & 0xFFFFFF;
                float a = Float.isNaN(p.alpha) ? 1f : p.alpha;
                float gl = clamp01(Float.isNaN(p.glow) ? 0f : p.glow);
                int i = y * W + x;
                // kesik kenarin hemen yanindaki pikseller de kenar seridi sayilir
                boolean edge = x == 0 || x == W - 1 || y == 0;
                if (o.trim && edge && a >= 0.5f) {
                    c = mix(d.c3(), 0xFFFFFF, 0.15f);
                    gl = Math.max(gl, 0.25f);
                }
                if (o.rainbow) c = hueRotate(c, (o.hue + t * 40f * clamp(o.rainbowSpeed, 0.1f, 4f) + p.v * 140f + p.u * 40f) % 360f);
                else if (o.hue != 0f) c = hueRotate(c, o.hue % 360f);
                faceRgb[i] = c;
                faceA[i] = a >= 0.5f ? 255 : 0;
                faceGlow[i] = a >= 0.5f ? gl * gk : 0f;
            }
        }
    }

    /**
     * Tum dokuyu uretir.
     *
     * @param base ARGB, en az TEX_W*TEX_H (kullanilmayan bolgeler seffaf)
     * @param glow ARGB, en az TEX_W*TEX_H (null olabilir)
     */
    public void paint(CapeDesign d, CapeTexture.Options o, float t, int[] base, int[] glow) {
        face(d, o, t);
        java.util.Arrays.fill(base, 0, TEX_W * TEX_H, 0);
        if (glow != null) java.util.Arrays.fill(glow, 0, TEX_W * TEX_H, 0xFF000000);
        long sr = 0, sg = 0, sb = 0, n = 0;
        for (int y = 0; y < H; y++) {
            int row = (FACE_Y + y) * TEX_W;
            for (int x = 0; x < W; x++) {
                int i = y * W + x, c = faceRgb[i];
                if (faceA[i] == 0) continue;
                sr += r(c);
                sg += g(c);
                sb += b(c);
                n++;
                base[row + OUT_X + x] = 0xFF000000 | c;
                if (glow != null) {
                    float k = faceGlow[i];
                    glow[row + OUT_X + x] = 0xFF000000 | (k <= 0.004f ? 0 : mul(c, Math.min(1f, k)));
                }
                // ic yuz: ayni yer aynalanmis (u = 0 omuz her iki yuzde de ayni kenar)
                // innerAuto = ic yuz de desenli (biraz golgeli); kapali = koyu astar
                int ic = o.innerAuto ? mul(c, 0.82f) : mix(mul(c, 0.4f), o.innerColor & 0xFFFFFF, 0.5f);
                base[row + IN_X + (W - 1 - x)] = 0xFF000000 | ic;
            }
        }
        // kenar seritleri (kalinlik): ortalama rengin koyusu, vurgu rengiyle karisik
        int avg = n == 0 ? d.c1() : ((int) (sr / n) << 16) | ((int) (sg / n) << 8) | (int) (sb / n);
        int edge = mix(mul(avg, 0.7f), d.c3(), 0.3f);
        for (int y = 0; y < H; y++) {
            int row = (FACE_Y + y) * TEX_W;
            // omuz kenari (x 34..36) ve uc kenari (x 22..24): dis yuzun o kenari kesikse seffaf
            boolean shoulder = faceA[y * W] != 0, far = faceA[y * W + W - 1] != 0;
            for (int x = 0; x < 2 * K; x++) {
                int sh = mul(edge, x == 0 || x == 2 * K - 1 ? 0.85f : 1f);
                if (shoulder) base[row + 34 * K + x] = 0xFF000000 | sh;
                if (far) base[row + 22 * K + x] = 0xFF000000 | sh;
            }
        }
        for (int y = 0; y < 2 * K; y++) {
            int row = y * TEX_W;
            for (int x = 0; x < W; x++) {
                // ust kenar (x 24..34) her zaman; alt kenar (x 34..44) sadece alt satiri dolu sutunlarda
                base[row + 24 * K + x] = 0xFF000000 | mul(edge, 1.05f);
                if (faceA[(H - 1) * W + (W - 1 - x)] != 0) base[row + 34 * K + x] = 0xFF000000 | mul(edge, 0.9f);
            }
        }
    }

    /**
     * Kucuk resim: iki kanat (dis yuz) yan yana, sol kanat aynali; 2x2 ortalama. Seffaf pikseller 0.
     *
     * @param out ARGB, THUMB_W * THUMB_H
     */
    public void thumbnail(CapeDesign d, CapeTexture.Options o, float t, int[] out) {
        face(d, o, t);
        int hw = W / 2;
        for (int y = 0; y < THUMB_H; y++) {
            for (int x = 0; x < hw; x++) {
                int rr = 0, gg = 0, bb = 0, k = 0;
                for (int dy = 0; dy < 2; dy++)
                    for (int dx = 0; dx < 2; dx++) {
                        int i = (y * 2 + dy) * W + x * 2 + dx;
                        if (faceA[i] == 0) continue;
                        int c = faceRgb[i];
                        rr += r(c);
                        gg += g(c);
                        bb += b(c);
                        k++;
                    }
                int c = k < 2 ? 0 : 0xFF000000 | ((rr / k) << 16) | ((gg / k) << 8) | (bb / k);
                // sag kanat: omuz ortada (u = 0 ortaya), sol kanat aynasi
                out[y * THUMB_W + hw + x] = c;
                out[y * THUMB_W + hw - 1 - x] = c;
            }
        }
    }

    /** Dis yuzun son uretilen hali (onizleme / test): ARGB, W x H. */
    public void faceArgb(CapeDesign d, CapeTexture.Options o, float t, int[] out) {
        face(d, o, t);
        for (int i = 0; i < W * H; i++) out[i] = faceA[i] == 0 ? 0 : 0xFF000000 | faceRgb[i];
    }
}
