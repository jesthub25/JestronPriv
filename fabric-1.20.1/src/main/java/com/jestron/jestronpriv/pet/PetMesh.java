package com.jestron.jestronpriv.pet;

/**
 * Yoldas modelleri: tamamen kodla uretilen geometri (MC'den bagimsiz, test edilebilir). Yerel uzay: +Y yukari,
 * +Z ileri (yoldasin baktigi yon), birim = blok, model ~0.3 blok. Iki katman: LIT (isikli, saydamlik destekli) ve
 * GLOW (parlak, isiktan bagimsiz). Normaller her dortgende geometriden hesaplanir ve bir ic noktaya gore disari
 * cevrilir; donuk yonu (winding) normale uyacak sekilde duzeltilir (arka yuz atan cizimlerde dogru yuz gorunur).
 */
public final class PetMesh {

    public static final int LIT = 0, GLOW = 1;

    /** Dortgen alicisi. xyz: 4 kose (12 float). twoSided: ince yuzey, iki yonden de yaz. */
    public interface Sink {
        void quad(int layer, float[] xyz, int argb, float nx, float ny, float nz, boolean twoSided);

        /** Kameraya donen yumusak isik halesi (yerel konum, yaricap). */
        void halo(float x, float y, float z, float size, int argb);
    }

    /** Animasyon durumu (PetBrain uretir). */
    public static final class Anim {
        public float time, flap, blink, beat, spin, speed, lookYaw, lookPitch;
    }

    // ------------------------------------------------------------------ afin matris yigini (3x4)

    private final float[] stack = new float[12 * 16];
    private int sp;
    private final float[] m = new float[12];
    private final float[] q = new float[12];
    private final float[] tmp = new float[3];
    private Sink sink;
    public int quads, halos;

    private void identity() {
        m[0] = 1; m[1] = 0; m[2] = 0; m[3] = 0;
        m[4] = 0; m[5] = 1; m[6] = 0; m[7] = 0;
        m[8] = 0; m[9] = 0; m[10] = 1; m[11] = 0;
    }

    private void push() {
        if (sp >= 16) throw new IllegalStateException("yigin tasti");
        System.arraycopy(m, 0, stack, sp * 12, 12);
        sp++;
    }

    private void pop() {
        sp--;
        System.arraycopy(stack, sp * 12, m, 0, 12);
    }

    private void translate(float x, float y, float z) {
        m[3] += m[0] * x + m[1] * y + m[2] * z;
        m[7] += m[4] * x + m[5] * y + m[6] * z;
        m[11] += m[8] * x + m[9] * y + m[10] * z;
    }

    private void scale(float x, float y, float z) {
        m[0] *= x; m[4] *= x; m[8] *= x;
        m[1] *= y; m[5] *= y; m[9] *= y;
        m[2] *= z; m[6] *= z; m[10] *= z;
    }

    /** Eksen etrafinda donus (radyan): 0 = X, 1 = Y, 2 = Z. */
    private void rot(int axis, float a) {
        if (a == 0f) return;
        float c = (float) Math.cos(a), s = (float) Math.sin(a);
        for (int r = 0; r < 3; r++) {
            int o = r * 4;
            float c0 = m[o], c1 = m[o + 1], c2 = m[o + 2];
            switch (axis) {
                case 0 -> {
                    m[o + 1] = c1 * c + c2 * s;
                    m[o + 2] = -c1 * s + c2 * c;
                }
                case 1 -> {
                    m[o] = c0 * c - c2 * s;
                    m[o + 2] = c0 * s + c2 * c;
                }
                default -> {
                    m[o] = c0 * c + c1 * s;
                    m[o + 1] = -c0 * s + c1 * c;
                }
            }
        }
    }

    private void apply(float x, float y, float z, float[] out, int o) {
        out[o] = m[0] * x + m[1] * y + m[2] * z + m[3];
        out[o + 1] = m[4] * x + m[5] * y + m[6] * z + m[7];
        out[o + 2] = m[8] * x + m[9] * y + m[10] * z + m[11];
    }

    // ------------------------------------------------------------------ cikti

    /**
     * Yerel kose noktalarindan dortgen. (hx,hy,hz) yerel "ic" nokta: normal ondan disari bakar. Ucgen icin son
     * kose ucuncuyle ayni verilir.
     */
    private void emit(int layer, float[] local, int argb, float hx, float hy, float hz, boolean twoSided) {
        for (int i = 0; i < 4; i++) apply(local[i * 3], local[i * 3 + 1], local[i * 3 + 2], q, i * 3);
        // normal: kosegenlerin capraz carpimi (dortgen ve ucgende saglam)
        float ax = q[6] - q[0], ay = q[7] - q[1], az = q[8] - q[2];
        float bx = q[9] - q[3], by = q[10] - q[4], bz = q[11] - q[5];
        if (local[9] == local[6] && local[10] == local[7] && local[11] == local[8]) {
            // ucgen: kenarlar
            bx = q[3] - q[0];
            by = q[4] - q[1];
            bz = q[5] - q[2];
            float tx = ax, ty = ay, tz = az;
            ax = bx;
            ay = by;
            az = bz;
            bx = tx;
            by = ty;
            bz = tz;
        }
        float nx = ay * bz - az * by, ny = az * bx - ax * bz, nz = ax * by - ay * bx;
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (!(len > 1e-9f)) return;   // dejenere
        nx /= len;
        ny /= len;
        nz /= len;
        apply(hx, hy, hz, tmp, 0);
        float cx = (q[0] + q[3] + q[6] + q[9]) * 0.25f - tmp[0];
        float cy = (q[1] + q[4] + q[7] + q[10]) * 0.25f - tmp[1];
        float cz = (q[2] + q[5] + q[8] + q[11]) * 0.25f - tmp[2];
        if (nx * cx + ny * cy + nz * cz < 0) {
            nx = -nx;
            ny = -ny;
            nz = -nz;
            // donuk yonunu da cevir (0,3,2,1)
            swap(1, 3);
        }
        quads++;
        sink.quad(layer, q, argb, nx, ny, nz, twoSided);
    }

    private void swap(int a, int b) {
        for (int k = 0; k < 3; k++) {
            float t = q[a * 3 + k];
            q[a * 3 + k] = q[b * 3 + k];
            q[b * 3 + k] = t;
        }
    }

    private final float[] lq = new float[12];

    private void quadL(int layer, int argb, float hx, float hy, float hz, boolean two,
                       float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3) {
        lq[0] = x0; lq[1] = y0; lq[2] = z0;
        lq[3] = x1; lq[4] = y1; lq[5] = z1;
        lq[6] = x2; lq[7] = y2; lq[8] = z2;
        lq[9] = x3; lq[10] = y3; lq[11] = z3;
        emit(layer, lq, argb, hx, hy, hz, two);
    }

    private void halo(float x, float y, float z, float size, int argb) {
        if (!(size > 0)) return;
        apply(x, y, z, tmp, 0);
        // olcek: matrisin ortalama eksen boyu
        float sx = (float) Math.sqrt(m[0] * m[0] + m[4] * m[4] + m[8] * m[8]);
        halos++;
        sink.halo(tmp[0], tmp[1], tmp[2], size * sx, argb);
    }

    // ------------------------------------------------------------------ temel sekiller

    /** Merkez (0,0,0), yari boyutlar. */
    private void box(int layer, int argb, float hx, float hy, float hz) {
        quadL(layer, argb, 0, 0, 0, false, -hx, -hy, hz, hx, -hy, hz, hx, hy, hz, -hx, hy, hz);     // on
        quadL(layer, argb, 0, 0, 0, false, hx, -hy, -hz, -hx, -hy, -hz, -hx, hy, -hz, hx, hy, -hz); // arka
        quadL(layer, argb, 0, 0, 0, false, -hx, -hy, -hz, -hx, -hy, hz, -hx, hy, hz, -hx, hy, -hz); // sol
        quadL(layer, argb, 0, 0, 0, false, hx, -hy, hz, hx, -hy, -hz, hx, hy, -hz, hx, hy, hz);     // sag
        quadL(layer, argb, 0, 0, 0, false, -hx, hy, hz, hx, hy, hz, hx, hy, -hz, -hx, hy, -hz);     // ust
        quadL(layer, argb, 0, 0, 0, false, -hx, -hy, -hz, hx, -hy, -hz, hx, -hy, hz, -hx, -hy, hz); // alt
    }

    /** UV kure (lat x lon). */
    /** sin/cos tablolari: n = 1..32 dilim (sonuclar eskisiyle bit bit ayni, her karede yeniden hesaplanmaz). */
    private static final float[][] AS = new float[33][], AC = new float[33][], LS = new float[33][], LC = new float[33][];

    static {
        for (int n = 1; n <= 32; n++) {
            AS[n] = new float[n + 1];
            AC[n] = new float[n + 1];
            LS[n] = new float[n + 1];
            LC[n] = new float[n + 1];
            for (int i = 0; i <= n; i++) {
                double p = 2 * Math.PI * i / n, t = Math.PI * i / n - Math.PI / 2;
                AS[n][i] = (float) Math.sin(p);
                AC[n][i] = (float) Math.cos(p);
                LS[n][i] = (float) Math.sin(t);
                LC[n][i] = (float) Math.cos(t);
            }
        }
    }

    private void sphere(int layer, int argb, float r, int lat, int lon) {
        if (lat < 1 || lon < 1) return;
        if (lat > 32 || lon > 32) {
            lat = Math.min(32, lat);
            lon = Math.min(32, lon);
        }
        float[] ls = LS[lat], lc = LC[lat], as = AS[lon], ac = AC[lon];
        for (int i = 0; i < lat; i++) {
            float y0 = ls[i] * r, y1 = ls[i + 1] * r;
            float c0 = lc[i] * r, c1 = lc[i + 1] * r;
            for (int j = 0; j < lon; j++) {
                float s0 = as[j], k0 = ac[j], s1 = as[j + 1], k1 = ac[j + 1];
                quadL(layer, argb, 0, 0, 0, false,
                        c0 * s0, y0, c0 * k0, c0 * s1, y0, c0 * k1, c1 * s1, y1, c1 * k1, c1 * s0, y1, c1 * k0);
            }
        }
    }

    /** Cift piramit (kristal): ekvator yaricapi r, ust ve alt uc yuksekligi. */
    private void bipyramid(int layer, int argb, float r, float top, float bottom, int sides) {
        for (int j = 0; j < sides; j++) {
            double p0 = 2 * Math.PI * j / sides, p1 = 2 * Math.PI * (j + 1) / sides;
            float x0 = (float) Math.sin(p0) * r, z0 = (float) Math.cos(p0) * r, x1 = (float) Math.sin(p1) * r, z1 = (float) Math.cos(p1) * r;
            quadL(layer, argb, 0, 0, 0, false, x0, 0, z0, x1, 0, z1, 0, top, 0, 0, top, 0);
            quadL(layer, argb, 0, 0, 0, false, x1, 0, z1, x0, 0, z0, 0, -bottom, 0, 0, -bottom, 0);
        }
    }

    /** XY duzleminde cokgen, Z'de kalinlik (on/arka ucgen yelpaze + yanlar). pts: x,y ciftleri, konveks olmasi gerekmez (merkezden yelpaze). */
    private void extrude(int layer, int argb, float[] pts, float depth, float cxp, float cyp) {
        int n = pts.length / 2;
        float d = depth / 2;
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            float ax = pts[i * 2], ay = pts[i * 2 + 1], bx = pts[j * 2], by = pts[j * 2 + 1];
            // on ve arka: merkezden ucgen (ic nokta: yuzun arkasinda)
            quadL(layer, argb, cxp, cyp, -1f, false, cxp, cyp, d, ax, ay, d, bx, by, d, bx, by, d);
            quadL(layer, argb, cxp, cyp, 1f, false, cxp, cyp, -d, bx, by, -d, ax, ay, -d, ax, ay, -d);
            // yan
            quadL(layer, argb, cxp, cyp, 0, false, ax, ay, d, bx, by, d, bx, by, -d, ax, ay, -d);
        }
    }

    /** Ince yuzey (kanat zari): ic nokta yuzeyin bir tarafinda, iki yuzlu. */
    private void sheet(int layer, int argb, float[] p4) {
        System.arraycopy(p4, 0, lq, 0, 12);
        // ic nokta: normalin tersi yonde; iki yuzlu oldugu icin onemsiz
        emit(layer, lq, argb, (p4[0] + p4[6]) / 2, (p4[1] + p4[7]) / 2 - 0.01f, (p4[2] + p4[8]) / 2, true);
    }

    // ------------------------------------------------------------------ renk

    public static int argb(int rgb, float a) {
        int al = Math.round(Math.max(0f, Math.min(1f, a)) * 255f);
        return (al << 24) | (rgb & 0xFFFFFF);
    }

    public static int mul(int rgb, float k) {
        int r = Math.min(255, Math.max(0, Math.round(((rgb >> 16) & 255) * k)));
        int g = Math.min(255, Math.max(0, Math.round(((rgb >> 8) & 255) * k)));
        int b = Math.min(255, Math.max(0, Math.round((rgb & 255) * k)));
        return (r << 16) | (g << 8) | b;
    }

    public static int mix(int a, int b, float k) {
        k = Math.max(0f, Math.min(1f, k));
        int r = Math.round(((a >> 16) & 255) * (1 - k) + ((b >> 16) & 255) * k);
        int g = Math.round(((a >> 8) & 255) * (1 - k) + ((b >> 8) & 255) * k);
        int bl = Math.round((a & 255) * (1 - k) + (b & 255) * k);
        return (r << 16) | (g << 8) | bl;
    }

    // ------------------------------------------------------------------ uretim

    /**
     * @param p    ana renk, s ikincil renk (RGB)
     * @param glow parlaklik carpani (0.2 - 1.5)
     */
    public void build(PetKind kind, Anim a, int p, int s, float glow, Sink out) {
        this.sink = out;
        quads = halos = 0;
        sp = 0;
        identity();
        if (kind == null || a == null || out == null) return;
        float g = Math.max(0.2f, Math.min(1.5f, glow));
        // sevinc donusu tum modele uygulanir
        rot(1, a.spin);
        switch (kind) {
            case RUH -> ruh(a, p, s, g);
            case KRISTAL -> kristal(a, p, s, g);
            case EJDERHA -> ejderha(a, p, s, g);
            case PERI -> peri(a, p, s, g);
            case GOZ -> goz(a, p, s, g);
            case DRONE -> drone(a, p, s, g);
            case YILDIZ -> yildiz(a, p, s, g);
            case KALP -> kalp(a, p, s, g);
            case KEDI -> kedi(a, p, s, g);
            case KOPEK -> kopek(a, p, s, g);
            case TAVSAN -> tavsan(a, p, s, g);
            case PENGUEN -> penguen(a, p, s, g);
            case BAYKUS -> baykus(a, p, s, g);
            case ARI -> ari(a, p, s, g);
            case KELEBEK -> kelebek(a, p, s, g);
            case BALIK -> balik(a, p, s, g);
            case AHTAPOT -> ahtapot(a, p, s, g);
            case DENIZANASI -> denizanasi(a, p, s, g);
            case YARASA -> yarasa(a, p, s, g);
            case KUS -> kus(a, p, s, g);
            case TILKI -> tilki(a, p, s, g);
            case PANDA -> panda(a, p, s, g);
            case BALCIK -> balcik(a, p, s, g);
            case HAYALET -> hayalet(a, p, s, g);
            case KAFATASI -> kafatasi(a, p, s, g);
            case BALKABAGI -> balkabagi(a, p, s, g);
            case KARDAN_ADAM -> kardanAdam(a, p, s, g);
            case ROBOT -> robot(a, p, s, g);
            case UFO -> ufo(a, p, s, g);
            case ROKET -> roket(a, p, s, g);
            case GEZEGEN -> gezegen(a, p, s, g);
            case GUNES -> gunes(a, p, s, g);
            case AY -> ay(a, p, s, g);
            case ATES_TOPU -> atesTopu(a, p, s, g);
            case KAR_TANESI -> karTanesi(a, p, s, g);
            case ELMAS -> elmas(a, p, s, g);
            case PARA -> para(a, p, s, g);
            case ENERJI_KUPU -> enerjiKupu(a, p, s, g);
            case PRIZMA -> prizma(a, p, s, g);
            case KUM_SAATI -> kumSaati(a, p, s, g);
            case KITAP -> kitap(a, p, s, g);
            case KILIC -> kilic(a, p, s, g);
            case FENER -> fener(a, p, s, g);
            case MANTAR -> mantar(a, p, s, g);
            case CICEK -> cicek(a, p, s, g);
            case ORUMCEK -> orumcek(a, p, s, g);
            case EJDER_YUMURTASI -> ejderYumurtasi(a, p, s, g);
            case KUZU -> kuzu(a, p, s, g);
            case BULUT -> bulut(a, p, s, g, false);
            case FIRTINA -> bulut(a, p, s, g, true);
        }
        this.sink = null;
    }

    private void ruh(Anim a, int p, int s, float g) {
        float t = a.time;
        float pulse = 1f + 0.08f * (float) Math.sin(t * 4.0);
        halo(0, 0, 0, 0.32f * pulse, argb(p, 0.55f * Math.min(1f, g)));
        halo(0, 0, 0, 0.16f, argb(s, 0.8f * Math.min(1f, g)));
        push();
        scale(pulse, pulse, pulse);
        sphere(GLOW, argb(mix(s, 0xFFFFFF, 0.4f), Math.min(1f, 0.95f * g)), 0.055f, 5, 8);
        sphere(LIT, argb(p, 0.38f), 0.095f, 6, 10);
        pop();
        // kuyruk: arkaya dogru kuculen, kivrilan kureler
        for (int i = 1; i <= 5; i++) {
            float k = i / 5f;
            push();
            translate((float) Math.sin(t * 3.0 - i * 0.9) * 0.03f * k, -0.02f * k + (float) Math.cos(t * 2.4 - i) * 0.015f, -0.07f - i * 0.055f);
            sphere(GLOW, argb(mix(p, s, 0.3f), (0.75f - 0.12f * i) * Math.min(1f, g)), 0.05f * (1f - 0.15f * i), 4, 6);
            pop();
        }
        // donen kivilcimlar
        for (int i = 0; i < 3; i++) {
            double ang = t * 2.6 + i * Math.PI * 2 / 3;
            push();
            translate((float) Math.cos(ang) * 0.17f, (float) Math.sin(t * 1.7 + i) * 0.06f, (float) Math.sin(ang) * 0.17f);
            rot(1, t * 4f + i);
            rot(0, t * 3f);
            box(GLOW, argb(s, Math.min(1f, g)), 0.012f, 0.012f, 0.012f);
            halo(0, 0, 0, 0.05f, argb(p, 0.6f * Math.min(1f, g)));
            pop();
        }
    }

    private void kristal(Anim a, int p, int s, float g) {
        float t = a.time;
        halo(0, 0, 0, 0.36f, argb(p, 0.4f * Math.min(1f, g)));
        push();
        rot(1, t * 1.4f);
        rot(2, (float) Math.sin(t * 0.9) * 0.12f);
        bipyramid(LIT, argb(mix(p, 0xFFFFFF, 0.15f), 0.78f), 0.085f, 0.17f, 0.13f, 6);
        push();
        rot(1, -t * 3f);
        bipyramid(GLOW, argb(mix(s, 0xFFFFFF, 0.3f), Math.min(1f, 0.9f * g)), 0.04f, 0.1f, 0.075f, 4);
        pop();
        pop();
        for (int i = 0; i < 3; i++) {
            double ang = -t * 1.8 + i * Math.PI * 2 / 3;
            push();
            translate((float) Math.cos(ang) * 0.2f, (float) Math.sin(t * 2.2 + i * 2) * 0.05f, (float) Math.sin(ang) * 0.2f);
            rot(1, t * 3f + i);
            rot(2, 0.4f);
            bipyramid(LIT, argb(s, 0.85f), 0.022f, 0.045f, 0.035f, 4);
            pop();
        }
    }

    private void ejderha(Anim a, int p, int s, float g) {
        float t = a.time;
        int dark = mul(p, 0.7f), belly = s;
        // govde zinciri (onden arkaya), hafif S salinimi
        float sway = (float) Math.sin(t * 2.2) * 0.06f;
        push();
        translate(0, 0, 0.02f);
        box(LIT, argb(p, 1f), 0.055f, 0.05f, 0.08f);
        push();
        translate(0, -0.03f, 0);
        box(LIT, argb(belly, 1f), 0.04f, 0.022f, 0.07f);
        pop();
        // boyun + bas
        push();
        translate(0, 0.045f, 0.08f);
        rot(0, -0.5f + a.lookPitch * 0.4f);
        rot(1, a.lookYaw * 0.5f);
        box(LIT, argb(p, 1f), 0.032f, 0.03f, 0.05f);
        translate(0, 0.02f, 0.07f);
        rot(0, 0.5f);
        box(LIT, argb(p, 1f), 0.05f, 0.042f, 0.05f);          // kafa
        push();
        translate(0, -0.012f, 0.06f);
        box(LIT, argb(dark, 1f), 0.034f, 0.024f, 0.035f);    // burun
        pop();
        // gozler (kirpma: dikeyde basilir)
        float eh = 0.011f * (1f - 0.85f * a.blink);
        for (int side = -1; side <= 1; side += 2) {
            push();
            translate(side * 0.046f, 0.012f, 0.03f);
            box(GLOW, argb(mix(s, 0xFFFFFF, 0.2f), Math.min(1f, g)), 0.006f, Math.max(0.0015f, eh), 0.012f);
            pop();
            // boynuzlar
            push();
            translate(side * 0.03f, 0.045f, -0.02f);
            rot(0, -0.8f);
            rot(2, side * 0.25f);
            translate(0, 0.03f, 0);
            box(LIT, argb(belly, 1f), 0.008f, 0.032f, 0.008f);
            pop();
        }
        pop();
        // kuyruk
        push();
        translate(0, 0, -0.08f);
        for (int i = 0; i < 5; i++) {
            rot(1, sway * (1f + i * 0.4f) + (float) Math.sin(t * 2.2 - i * 0.7) * 0.18f);
            rot(0, 0.08f);
            float w = 0.04f * (1f - i * 0.16f);
            translate(0, 0, -0.045f);
            box(LIT, argb(i % 2 == 0 ? p : dark, 1f), w, w * 0.85f, 0.045f);
        }
        translate(0, 0, -0.05f);
        box(LIT, argb(belly, 1f), 0.035f, 0.004f, 0.025f);   // kuyruk ucu yelpaze
        pop();
        // bacaklar (katlanmis)
        for (int side = -1; side <= 1; side += 2) {
            for (int fz = -1; fz <= 1; fz += 2) {
                push();
                translate(side * 0.045f, -0.06f, fz * 0.05f);
                rot(0, 0.6f);
                box(LIT, argb(dark, 1f), 0.012f, 0.022f, 0.012f);
                pop();
            }
        }
        // kanatlar: kemik + zar, cirpma
        float flap = (float) Math.sin(a.flap) * 0.85f + 0.15f;
        for (int side = -1; side <= 1; side += 2) {
            push();
            translate(side * 0.045f, 0.05f, 0.02f);
            rot(2, -side * flap);
            rot(1, side * 0.25f);
            // kemik
            push();
            translate(side * 0.09f, 0, 0);
            box(LIT, argb(dark, 1f), 0.09f, 0.008f, 0.008f);
            pop();
            float tip = side * 0.2f, mid = side * 0.11f;
            float[] w1 = {0, 0, 0, tip, 0.02f, 0.01f, mid, -0.005f, -0.13f, 0, 0, -0.1f};
            sheet(LIT, argb(mix(belly, p, 0.25f), 0.9f), w1);
            float[] w2 = {0, 0, 0, mid, -0.005f, -0.13f, side * 0.04f, -0.01f, -0.15f, 0, 0, -0.1f};
            sheet(LIT, argb(mix(belly, p, 0.4f), 0.9f), w2);
            pop();
        }
        pop();
    }

    private void peri(Anim a, int p, int s, float g) {
        float t = a.time;
        halo(0, 0.02f, 0, 0.26f, argb(s, 0.45f * Math.min(1f, g)));
        // govde: kucuk parlak beden + bas
        push();
        rot(0, 0.25f);
        push();
        scale(0.8f, 1.5f, 0.8f);
        sphere(GLOW, argb(mix(s, 0xFFFFFF, 0.5f), Math.min(1f, g)), 0.026f, 4, 6);
        pop();
        translate(0, 0.06f, 0);
        sphere(GLOW, argb(mix(s, 0xFFFFFF, 0.6f), Math.min(1f, g)), 0.022f, 4, 6);
        pop();
        // dort kelebek kanadi: hizli cirpma
        float flap = 0.35f + 0.75f * (0.5f + 0.5f * (float) Math.sin(a.flap * 2.2f));
        for (int side = -1; side <= 1; side += 2) {
            for (int up = 0; up < 2; up++) {
                push();
                translate(0, up == 0 ? 0.015f : -0.005f, -0.01f);
                // acik (0.15 rad) ile yariya kapali (0.95 rad) arasi cirpar; kanatlar hafif arkaya yatik
                rot(1, -side * (0.15f + 0.8f * (1.1f - flap) / 0.75f * 0.75f));
                rot(0, -0.25f);
                float L = up == 0 ? 0.17f : 0.11f, H = up == 0 ? 0.13f : 0.09f;
                float y0 = up == 0 ? 0f : -0.01f;
                float[] w = {0, y0, 0, side * L * 0.55f, y0 + (up == 0 ? H : -H * 0.9f), 0, side * L, y0 + (up == 0 ? H * 0.55f : -H * 0.5f), 0,
                        side * L * 0.35f, y0 + (up == 0 ? 0.01f : -0.02f), 0};
                sheet(LIT, argb(up == 0 ? p : mix(p, s, 0.4f), 0.62f), w);
                pop();
            }
        }
    }

    private void goz(Anim a, int p, int s, float g) {
        float t = a.time;
        halo(0, 0, -0.03f, 0.3f, argb(p, 0.28f * Math.min(1f, g)));
        push();
        rot(1, a.lookYaw);
        rot(0, a.lookPitch);
        push();
        scale(1f, 1f - 0.8f * a.blink, 1f);
        sphere(LIT, argb(s, 1f), 0.11f, 7, 12);
        // iris (one bakan parlak halka) ve gozbebegi
        int n = 14;
        for (int i = 0; i < n; i++) {
            double a0 = 2 * Math.PI * i / n, a1 = 2 * Math.PI * (i + 1) / n;
            float r0 = 0.03f, r1 = 0.058f, z = 0.103f;
            float c0 = (float) Math.cos(a0), s0 = (float) Math.sin(a0), c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);
            quadL(GLOW, argb(p, Math.min(1f, g)), 0, 0, 0, false,
                    c0 * r0, s0 * r0, z + 0.006f, c0 * r1, s0 * r1, z - 0.01f, c1 * r1, s1 * r1, z - 0.01f, c1 * r0, s1 * r0, z + 0.006f);
            quadL(LIT, argb(0x0A0A0A, 1f), 0, 0, 0, false, 0, 0, z + 0.012f, c0 * r0, s0 * r0, z + 0.008f, c1 * r0, s1 * r0, z + 0.008f,
                    c1 * r0, s1 * r0, z + 0.008f);
        }
        // isik parlamasi
        push();
        translate(0.018f, 0.02f, 0.118f);
        box(GLOW, argb(0xFFFFFF, 0.9f), 0.006f, 0.006f, 0.002f);
        pop();
        pop();
        pop();
        // alt dokunaclar (salinan)
        for (int i = 0; i < 4; i++) {
            push();
            rot(1, i * (float) Math.PI / 2 + 0.4f);
            translate(0.05f, -0.1f, 0);
            rot(2, 0.3f + (float) Math.sin(t * 3 + i) * 0.25f);
            translate(0, -0.03f, 0);
            box(LIT, argb(mul(p, 0.6f), 1f), 0.008f, 0.032f, 0.008f);
            pop();
        }
    }

    private void drone(Anim a, int p, int s, float g) {
        float t = a.time;
        int body = 0x2A2E36, arm = 0x40454F;
        box(LIT, argb(body, 1f), 0.07f, 0.022f, 0.07f);
        push();
        translate(0, 0.03f, 0);
        box(LIT, argb(mix(body, p, 0.35f), 1f), 0.045f, 0.012f, 0.045f);
        pop();
        // on kamera (parlak) ve ledler
        push();
        translate(0, -0.012f, 0.07f);
        box(GLOW, argb(p, Math.min(1f, g)), 0.016f, 0.01f, 0.006f);
        pop();
        boolean on = ((int) (t * 2.5f)) % 2 == 0;
        push();
        translate(0, 0, -0.072f);
        box(GLOW, argb(on ? s : mul(s, 0.25f), Math.min(1f, g)), 0.01f, 0.006f, 0.004f);
        pop();
        for (int i = 0; i < 4; i++) {
            push();
            rot(1, (float) Math.PI / 4 + i * (float) Math.PI / 2);
            translate(0, 0.005f, 0.1f);
            box(LIT, argb(arm, 1f), 0.008f, 0.006f, 0.05f);
            translate(0, 0.012f, 0.05f);
            box(LIT, argb(body, 1f), 0.012f, 0.01f, 0.012f);
            // pervane: hizla donen iki kanat + bulanik disk
            translate(0, 0.014f, 0);
            push();
            rot(1, t * 40f * (i % 2 == 0 ? 1 : -1));
            box(LIT, argb(0xD0D0D0, 0.9f), 0.05f, 0.002f, 0.007f);
            pop();
            int n = 10;
            for (int k = 0; k < n; k++) {
                double a0 = 2 * Math.PI * k / n, a1 = 2 * Math.PI * (k + 1) / n;
                float r = 0.052f;
                quadL(LIT, argb(0xFFFFFF, 0.13f), 0, -0.05f, 0, true, 0, 0.001f, 0,
                        (float) Math.cos(a0) * r, 0.001f, (float) Math.sin(a0) * r, (float) Math.cos(a1) * r, 0.001f, (float) Math.sin(a1) * r,
                        (float) Math.cos(a1) * r, 0.001f, (float) Math.sin(a1) * r);
            }
            pop();
        }
        halo(0, -0.02f, 0.075f, 0.06f, argb(p, 0.7f * Math.min(1f, g)));
    }

    private static float[] starPts(int points, float outer, float inner) {
        float[] pts = new float[points * 4];
        for (int i = 0; i < points * 2; i++) {
            double ang = Math.PI / 2 + Math.PI * i / points;
            float r = i % 2 == 0 ? outer : inner;
            pts[i * 2] = (float) Math.cos(ang) * r;
            pts[i * 2 + 1] = (float) Math.sin(ang) * r;
        }
        return pts;
    }

    private static final float[] STAR = starPts(5, 0.15f, 0.065f);
    private static final float[] STAR_IN = starPts(5, 0.085f, 0.037f);
    private static final float[] HEART = heartPts(28, 0.0105f);
    private static final float[] HEART_IN = heartPts(20, 0.0062f);

    private void yildiz(Anim a, int p, int s, float g) {
        float t = a.time;
        halo(0, 0, 0, 0.38f, argb(p, 0.45f * Math.min(1f, g)));
        push();
        rot(1, t * 1.6f);
        rot(2, (float) Math.sin(t * 1.1) * 0.15f);
        extrude(LIT, argb(p, 1f), STAR, 0.045f, 0, 0);
        push();
        scale(1f, 1f, 1.6f);
        extrude(GLOW, argb(mix(s, p, 0.2f), Math.min(1f, g)), STAR_IN, 0.04f, 0, 0);
        pop();
        pop();
    }

    /** Kalp egrisi (x = 16 sin^3 t, y = 13 cos t - 5 cos 2t - 2 cos 3t - cos 4t), merkezlenmis. */
    private static float[] heartPts(int n, float k) {
        float[] pts = new float[n * 2];
        for (int i = 0; i < n; i++) {
            double tt = 2 * Math.PI * i / n;
            double x = 16 * Math.pow(Math.sin(tt), 3);
            double y = 13 * Math.cos(tt) - 5 * Math.cos(2 * tt) - 2 * Math.cos(3 * tt) - Math.cos(4 * tt);
            pts[i * 2] = (float) (x * k);
            pts[i * 2 + 1] = (float) ((y + 2.5) * k);
        }
        return pts;
    }

    private void kalp(Anim a, int p, int s, float g) {
        float beat = a.beat;
        float sc = 1f + 0.16f * beat;
        halo(0, 0, 0, 0.3f * (1f + 0.25f * beat), argb(s, (0.35f + 0.3f * beat) * Math.min(1f, g)));
        push();
        rot(1, (float) Math.sin(a.time * 1.3) * 0.5f);
        scale(sc, sc, sc);
        extrude(LIT, argb(p, 1f), HEART, 0.07f, 0, 0.02f);
        push();
        translate(0, 0, 0f);
        scale(1f, 1f, 1.25f);
        extrude(GLOW, argb(mix(p, s, 0.5f), Math.min(1f, (0.5f + 0.5f * beat) * g)), HEART_IN, 0.07f, 0, 0.012f);
        pop();
        pop();
    }

    // ------------------------------------------------------------------ ek sekiller (yeni yoldaslar)

    private final float[] wb = new float[12];

    /** Iki yuzlu ince yuzey (4 kose; ucgen icin son kose = ucuncu). Dizi ayirmadan. */
    private void sheet4(int layer, int argb, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2,
                        float x3, float y3, float z3) {
        wb[0] = x0; wb[1] = y0; wb[2] = z0;
        wb[3] = x1; wb[4] = y1; wb[5] = z1;
        wb[6] = x2; wb[7] = y2; wb[8] = z2;
        wb[9] = x3; wb[10] = y3; wb[11] = z3;
        sheet(layer, argb, wb);
    }

    /** Kesik koni (Y ekseni, merkez 0): alt yaricap r0, ust r1 (0 = sivri uc), kapakli. */
    private void cyl2(int layer, int argb, float r0, float r1, float h, int n) {
        float y0 = -h / 2, y1 = h / 2;
        for (int j = 0; j < n; j++) {
            double a0 = 2 * Math.PI * j / n, a1 = 2 * Math.PI * (j + 1) / n;
            float s0 = (float) Math.sin(a0), c0 = (float) Math.cos(a0), s1 = (float) Math.sin(a1), c1 = (float) Math.cos(a1);
            quadL(layer, argb, 0, 0, 0, false, s0 * r0, y0, c0 * r0, s1 * r0, y0, c1 * r0, s1 * r1, y1, c1 * r1, s0 * r1, y1, c0 * r1);
            if (r1 > 0) quadL(layer, argb, 0, 0, 0, false, 0, y1, 0, s0 * r1, y1, c0 * r1, s1 * r1, y1, c1 * r1, s1 * r1, y1, c1 * r1);
            if (r0 > 0) quadL(layer, argb, 0, 0, 0, false, 0, y0, 0, s1 * r0, y0, c1 * r0, s0 * r0, y0, c0 * r0, s0 * r0, y0, c0 * r0);
        }
    }

    private void cyl(int layer, int argb, float r, float h, int n) {
        cyl2(layer, argb, r, r, h, n);
    }

    /** XZ duzleminde simit (R ana yaricap, r boru). */
    private void torus(int layer, int argb, float R, float r, int nu, int nv) {
        for (int i = 0; i < nu; i++) {
            double u0 = 2 * Math.PI * i / nu, u1 = 2 * Math.PI * (i + 1) / nu, um = (u0 + u1) / 2;
            float cu0 = (float) Math.cos(u0), su0 = (float) Math.sin(u0), cu1 = (float) Math.cos(u1), su1 = (float) Math.sin(u1);
            float hx = (float) Math.cos(um) * R, hz = (float) Math.sin(um) * R;
            for (int j = 0; j < nv; j++) {
                double v0 = 2 * Math.PI * j / nv, v1 = 2 * Math.PI * (j + 1) / nv;
                float cv0 = (float) Math.cos(v0), sv0 = (float) Math.sin(v0), cv1 = (float) Math.cos(v1), sv1 = (float) Math.sin(v1);
                float a0 = R + r * cv0, a1 = R + r * cv1;
                quadL(layer, argb, hx, 0, hz, false, a0 * cu0, r * sv0, a0 * su0, a0 * cu1, r * sv0, a0 * su1, a1 * cu1, r * sv1, a1 * su1,
                        a1 * cu0, r * sv1, a1 * su0);
            }
        }
    }

    /** XZ duzleminde ince disk (iki yuzlu). */
    private void disc(int layer, int argb, float r, int n) {
        for (int j = 0; j < n; j++) {
            double a0 = 2 * Math.PI * j / n, a1 = 2 * Math.PI * (j + 1) / n;
            sheet4(layer, argb, 0, 0, 0, (float) Math.cos(a0) * r, 0, (float) Math.sin(a0) * r, (float) Math.cos(a1) * r, 0, (float) Math.sin(a1) * r,
                    (float) Math.cos(a1) * r, 0, (float) Math.sin(a1) * r);
        }
    }

    /** Iki goz: koyu parlak kure + isilti (glow = isiktan bagimsiz parlayan goz). Kirpmada basilir. */
    private void eyes(float x, float y, float z, float r, float blink, int col, boolean glow, float g) {
        for (int side = -1; side <= 1; side += 2) {
            push();
            translate(side * x, y, z);
            scale(1f, Math.max(0.12f, 1f - 0.88f * blink), 0.7f);
            sphere(glow ? GLOW : LIT, argb(col, glow ? Math.min(1f, g) : 1f), r, 3, 6);
            if (!glow) {
                translate(r * 0.35f, r * 0.35f, r * 0.85f);
                box(GLOW, argb(0xFFFFFF, 0.95f), r * 0.26f, r * 0.26f, r * 0.08f);
            }
            pop();
        }
    }

    /** Kanat cifti: omuz (x, y, z), uzunluk L, genislik W; cirpma acisi ve geriye supurme. */
    private void wingPair(int layer, int argb, float x, float y, float z, float L, float W, float flapAng, float sweep) {
        for (int side = -1; side <= 1; side += 2) {
            push();
            translate(side * x, y, z);
            rot(2, -side * flapAng);
            rot(1, side * sweep);
            sheet4(layer, argb, 0, 0, W * 0.5f, side * L, 0.01f, W * 0.1f, side * L * 0.75f, 0, -W * 0.5f, 0, 0, -W * 0.35f);
            pop();
        }
    }

    /** Kafa uzerinde konik kulak cifti. */
    private void ears(int argb, float x, float y, float z, float r, float h, float tiltOut, float wiggle) {
        for (int side = -1; side <= 1; side += 2) {
            push();
            translate(side * x, y, z);
            rot(2, -side * (tiltOut + wiggle * side));
            translate(0, h / 2, 0);
            cyl2(LIT, argb, r, 0f, h, 4);
            pop();
        }
    }

    /** Dort kisa bacak. */
    private void legs(int argb, float x, float y, float zf, float zb, float r, float h, float walk) {
        for (int side = -1; side <= 1; side += 2) {
            for (int fz = 0; fz < 2; fz++) {
                push();
                translate(side * x, y, fz == 0 ? zf : zb);
                rot(0, (float) Math.sin(walk + (side * (fz == 0 ? 1 : -1)) * 1.5) * 0.35f);
                translate(0, -h / 2, 0);
                box(LIT, argb, r, h / 2, r);
                pop();
            }
        }
    }

    private static float sin(double v) {
        return (float) Math.sin(v);
    }

    // ------------------------------------------------------------------ hayvanlar

    private void kedi(Anim a, int p, int s, float g) {
        float t = a.time, walk = t * 6f * Math.min(1f, a.speed);
        push();
        translate(0, -0.03f, -0.03f);
        box(LIT, argb(p, 1f), 0.052f, 0.042f, 0.08f);
        translate(0, -0.018f, 0.01f);
        box(LIT, argb(s, 1f), 0.04f, 0.028f, 0.06f);
        pop();
        legs(argb(p, 1f), 0.034f, -0.06f, 0.025f, -0.085f, 0.013f, 0.04f, walk);
        push();
        translate(0, 0.045f, 0.07f);
        rot(1, a.lookYaw * 0.6f);
        rot(0, a.lookPitch * 0.5f);
        box(LIT, argb(p, 1f), 0.062f, 0.052f, 0.052f);
        push();
        translate(0, -0.022f, 0.05f);
        box(LIT, argb(s, 1f), 0.03f, 0.018f, 0.01f);
        translate(0, 0.014f, 0.008f);
        box(LIT, argb(0xFF8AA0, 1f), 0.008f, 0.006f, 0.004f);
        pop();
        eyes(0.028f, 0.012f, 0.052f, 0.011f, a.blink, 0x1E3018, false, g);
        ears(argb(p, 1f), 0.036f, 0.05f, -0.005f, 0.022f, 0.045f, 0.22f, 0.1f * sin(t * 1.7));
        pop();
        push();
        translate(0, -0.005f, -0.11f);
        for (int i = 0; i < 6; i++) {
            rot(0, 0.3f);
            rot(1, sin(t * 2.4 - i * 0.6) * 0.22f);
            translate(0, 0, -0.022f);
            box(LIT, argb(i >= 4 ? s : p, 1f), 0.011f, 0.011f, 0.022f);
        }
        pop();
    }

    private void kopek(Anim a, int p, int s, float g) {
        float t = a.time, walk = t * 6f * Math.min(1f, a.speed);
        int dark = mul(p, 0.7f);
        push();
        translate(0, -0.03f, -0.03f);
        box(LIT, argb(p, 1f), 0.056f, 0.046f, 0.085f);
        translate(0, -0.02f, 0.02f);
        box(LIT, argb(s, 1f), 0.042f, 0.03f, 0.06f);
        pop();
        legs(argb(p, 1f), 0.036f, -0.062f, 0.03f, -0.09f, 0.014f, 0.04f, walk);
        push();
        translate(0, 0.045f, 0.075f);
        rot(1, a.lookYaw * 0.6f);
        rot(0, a.lookPitch * 0.5f);
        box(LIT, argb(p, 1f), 0.06f, 0.054f, 0.055f);
        push();
        translate(0, -0.02f, 0.065f);
        box(LIT, argb(s, 1f), 0.034f, 0.026f, 0.03f);
        translate(0, 0.018f, 0.03f);
        box(LIT, argb(0x1A1414, 1f), 0.012f, 0.009f, 0.006f);
        pop();
        eyes(0.028f, 0.016f, 0.055f, 0.011f, a.blink, 0x1A1410, false, g);
        // sarkik kulaklar
        for (int side = -1; side <= 1; side += 2) {
            push();
            translate(side * 0.062f, 0.03f, 0f);
            rot(2, side * (0.15f + 0.12f * sin(t * 3 + side)));
            translate(0, -0.035f, 0);
            box(LIT, argb(dark, 1f), 0.01f, 0.038f, 0.022f);
            pop();
        }
        pop();
        // sallanan kuyruk
        push();
        translate(0, 0.0f, -0.115f);
        rot(0, 0.7f);
        rot(1, sin(t * 14) * 0.6f);
        translate(0, 0, -0.035f);
        box(LIT, argb(p, 1f), 0.011f, 0.011f, 0.035f);
        pop();
    }

    private void tavsan(Anim a, int p, int s, float g) {
        float t = a.time;
        float hop = Math.abs(sin(t * 4)) * 0.02f * Math.min(1f, a.speed + 0.3f);
        translate(0, hop, 0);
        push();
        translate(0, -0.04f, -0.02f);
        scale(1f, 0.9f, 1.15f);
        sphere(LIT, argb(p, 1f), 0.07f, 5, 8);
        pop();
        push();
        translate(0, -0.03f, -0.1f);
        sphere(LIT, argb(0xFFFFFF, 1f), 0.025f, 3, 6);
        pop();
        push();
        translate(0, 0.04f, 0.055f);
        rot(1, a.lookYaw * 0.6f);
        sphere(LIT, argb(p, 1f), 0.052f, 5, 8);
        eyes(0.03f, 0.012f, 0.04f, 0.011f, a.blink, 0x281418, false, g);
        push();
        translate(0, -0.01f, 0.05f + 0.002f * sin(t * 20));
        box(LIT, argb(s, 1f), 0.008f, 0.006f, 0.004f);
        pop();
        for (int side = -1; side <= 1; side += 2) {
            push();
            translate(side * 0.022f, 0.04f, -0.01f);
            rot(2, -side * (0.12f + 0.1f * sin(t * 2.2 + side)));
            rot(0, -0.15f + 0.1f * sin(t * 1.6));
            translate(0, 0.06f, 0);
            box(LIT, argb(p, 1f), 0.014f, 0.06f, 0.007f);
            translate(0, 0, 0.005f);
            box(LIT, argb(s, 1f), 0.008f, 0.05f, 0.004f);
            pop();
        }
        pop();
    }

    private void penguen(Anim a, int p, int s, float g) {
        float t = a.time;
        rot(2, sin(t * 5) * 0.12f * Math.min(1f, a.speed + 0.2f));
        push();
        scale(1f, 1.35f, 0.95f);
        sphere(LIT, argb(p, 1f), 0.07f, 6, 10);
        translate(0, -0.005f, 0.018f);
        scale(0.85f, 0.88f, 0.85f);
        sphere(LIT, argb(s, 1f), 0.066f, 6, 10);
        pop();
        eyes(0.024f, 0.05f, 0.058f, 0.009f, a.blink, 0x101014, false, g);
        push();
        translate(0, 0.035f, 0.075f);
        rot(0, (float) (Math.PI / 2));
        cyl2(LIT, argb(0xFF9A2E, 1f), 0.012f, 0f, 0.03f, 4);
        pop();
        float fl = 0.3f + 0.35f * sin(a.flap * 1.2f);
        for (int side = -1; side <= 1; side += 2) {
            push();
            translate(side * 0.068f, 0.01f, 0);
            rot(2, side * fl);
            translate(side * 0.008f, -0.035f, 0);
            box(LIT, argb(p, 1f), 0.007f, 0.04f, 0.022f);
            pop();
            push();
            translate(side * 0.025f, -0.098f, 0.02f);
            box(LIT, argb(0xFF9A2E, 1f), 0.016f, 0.005f, 0.022f);
            pop();
        }
    }

    private void baykus(Anim a, int p, int s, float g) {
        push();
        scale(1f, 1.2f, 0.9f);
        sphere(LIT, argb(p, 1f), 0.075f, 6, 10);
        translate(0, -0.012f, 0.02f);
        scale(0.75f, 0.75f, 0.8f);
        sphere(LIT, argb(s, 1f), 0.07f, 5, 8);
        pop();
        push();
        rot(1, a.lookYaw);
        // yuz diskleri + kocaman parlak gozler
        for (int side = -1; side <= 1; side += 2) {
            push();
            translate(side * 0.03f, 0.03f, 0.06f);
            rot(0, (float) (Math.PI / 2));
            cyl(LIT, argb(mix(s, 0xFFFFFF, 0.4f), 1f), 0.027f, 0.006f, 10);
            pop();
        }
        eyes(0.03f, 0.03f, 0.066f, 0.016f, a.blink, 0xFFC020, true, g);
        eyes(0.03f, 0.03f, 0.074f, 0.007f, a.blink, 0x101010, false, g);
        push();
        translate(0, 0.012f, 0.072f);
        rot(0, 2.4f);
        cyl2(LIT, argb(0xE0A030, 1f), 0.008f, 0f, 0.02f, 4);
        pop();
        ears(argb(mul(p, 0.8f), 1f), 0.04f, 0.075f, 0.01f, 0.012f, 0.03f, 0.35f, 0f);
        pop();
        float fl = 0.2f + 0.7f * (0.5f + 0.5f * sin(a.flap));
        wingPair(LIT, argb(mul(p, 0.85f), 1f), 0.06f, 0.01f, -0.01f, 0.09f, 0.08f, fl, 0.5f);
    }

    private void ari(Anim a, int p, int s, float g) {
        float t = a.time;
        push();
        scale(0.85f, 0.85f, 1.25f);
        sphere(LIT, argb(p, 1f), 0.06f, 6, 10);
        pop();
        // seritler
        for (int i = -1; i <= 1; i++) {
            push();
            translate(0, 0, i * 0.03f - 0.01f);
            torus(LIT, argb(s, 1f), 0.049f - Math.abs(i) * 0.008f, 0.008f, 12, 4);
            pop();
        }
        push();
        translate(0, 0, -0.085f);
        rot(0, -(float) (Math.PI / 2));
        cyl2(LIT, argb(s, 1f), 0.012f, 0f, 0.025f, 4);
        pop();
        push();
        translate(0, 0.01f, 0.075f);
        sphere(LIT, argb(s, 1f), 0.035f, 4, 8);
        eyes(0.02f, 0.008f, 0.027f, 0.009f, a.blink, 0x101010, false, g);
        for (int side = -1; side <= 1; side += 2) {
            push();
            translate(side * 0.012f, 0.03f, 0.01f);
            rot(2, -side * 0.35f);
            rot(0, -0.4f);
            translate(0, 0.02f, 0);
            box(LIT, argb(s, 1f), 0.003f, 0.02f, 0.003f);
            translate(0, 0.02f, 0);
            sphere(LIT, argb(s, 1f), 0.006f, 2, 4);
            pop();
        }
        pop();
        float fl = 0.2f + 0.6f * (0.5f + 0.5f * sin(t * 60));
        wingPair(LIT, argb(0xE8F6FF, 0.45f), 0.02f, 0.05f, 0.01f, 0.09f, 0.06f, fl, 0.4f);
        halo(0, 0, 0, 0.12f, argb(p, 0.15f * Math.min(1f, g)));
    }

    private void kelebek(Anim a, int p, int s, float g) {
        float t = a.time;
        push();
        scale(0.6f, 0.6f, 2f);
        sphere(LIT, argb(0x1E1A24, 1f), 0.02f, 3, 6);
        pop();
        float open = 0.15f + 1.1f * (0.5f + 0.5f * sin(t * 6));
        for (int side = -1; side <= 1; side += 2) {
            push();
            rot(2, -side * open);
            sheet4(LIT, argb(p, 0.85f), 0, 0, 0.02f, side * 0.13f, 0, 0.08f, side * 0.15f, 0, -0.01f, 0, 0, -0.02f);
            sheet4(LIT, argb(mix(p, s, 0.5f), 0.85f), 0, 0, -0.01f, side * 0.1f, 0, -0.02f, side * 0.08f, 0, -0.1f, 0, 0, -0.04f);
            push();
            translate(side * 0.09f, 0.002f, 0.035f);
            disc(GLOW, argb(s, 0.9f * Math.min(1f, g)), 0.022f, 8);
            pop();
            pop();
        }
        for (int side = -1; side <= 1; side += 2) {
            push();
            translate(side * 0.006f, 0.005f, 0.04f);
            rot(1, side * 0.4f);
            rot(0, -0.6f);
            translate(0, 0, 0.03f);
            box(LIT, argb(0x1E1A24, 1f), 0.002f, 0.002f, 0.03f);
            pop();
        }
    }

    private void balik(Anim a, int p, int s, float g) {
        float t = a.time;
        rot(1, sin(t * 3) * 0.15f);
        push();
        scale(0.7f, 1f, 1.4f);
        sphere(LIT, argb(p, 1f), 0.06f, 6, 10);
        pop();
        eyes(0.035f, 0.015f, 0.05f, 0.012f, a.blink, 0x101010, false, g);
        // kuyruk yuzgeci
        push();
        translate(0, 0, -0.08f);
        rot(1, sin(t * 8) * 0.5f);
        sheet4(LIT, argb(s, 0.8f), 0, 0, 0, 0, 0.06f, -0.07f, 0, 0, -0.04f, 0, -0.06f, -0.07f);
        pop();
        // sirt ve yan yuzgecler
        sheet4(LIT, argb(s, 0.8f), 0, 0.055f, 0.03f, 0, 0.09f, -0.01f, 0, 0.055f, -0.04f, 0, 0.055f, -0.04f);
        for (int side = -1; side <= 1; side += 2) {
            push();
            translate(side * 0.04f, -0.02f, 0.01f);
            rot(1, side * (0.6f + 0.3f * sin(t * 6 + side)));
            sheet4(LIT, argb(s, 0.8f), 0, 0, 0, side * 0.04f, -0.01f, -0.02f, side * 0.035f, -0.02f, -0.04f, side * 0.035f, -0.02f, -0.04f);
            pop();
        }
        halo(0, 0, 0, 0.12f, argb(p, 0.12f * Math.min(1f, g)));
    }

    private void ahtapot(Anim a, int p, int s, float g) {
        float t = a.time;
        push();
        translate(0, 0.03f, 0);
        scale(1f, 1.15f, 1f);
        sphere(LIT, argb(p, 1f), 0.07f, 6, 10);
        pop();
        eyes(0.03f, 0.03f, 0.06f, 0.013f, a.blink, 0x14081A, false, g);
        for (int i = 0; i < 8; i++) {
            push();
            rot(1, i * (float) (Math.PI / 4));
            translate(0, -0.02f, 0.045f);
            for (int k = 0; k < 4; k++) {
                rot(0, 0.35f + sin(t * 3 + i * 0.8 + k * 0.7) * 0.25f);
                translate(0, 0, 0.018f);
                box(LIT, argb(k == 3 ? s : p, 1f), 0.011f - k * 0.002f, 0.009f - k * 0.0015f, 0.018f);
                translate(0, 0, 0.018f);
            }
            pop();
        }
    }

    private void denizanasi(Anim a, int p, int s, float g) {
        float t = a.time;
        float pulse = 1f + 0.12f * sin(t * 3.2);
        halo(0, 0.02f, 0, 0.26f, argb(p, 0.35f * Math.min(1f, g)));
        push();
        translate(0, 0.03f, 0);
        scale(pulse, 1f / pulse, pulse);
        push();
        scale(1f, 0.7f, 1f);
        sphere(LIT, argb(p, 0.45f), 0.08f, 5, 12);
        pop();
        sphere(GLOW, argb(mix(s, 0xFFFFFF, 0.3f), 0.8f * Math.min(1f, g)), 0.03f, 3, 6);
        pop();
        for (int i = 0; i < 6; i++) {
            push();
            rot(1, i * (float) (Math.PI / 3) + 0.3f);
            translate(0.04f, 0.0f, 0);
            for (int k = 0; k < 5; k++) {
                rot(2, sin(t * 2.5 + i + k * 0.9) * 0.18f);
                translate(0, -0.018f, 0);
                box(GLOW, argb(s, (0.85f - k * 0.13f) * Math.min(1f, g)), 0.003f, 0.018f, 0.003f);
                translate(0, -0.018f, 0);
            }
            pop();
        }
    }

    private void yarasa(Anim a, int p, int s, float g) {
        push();
        scale(1f, 1.1f, 1f);
        sphere(LIT, argb(p, 1f), 0.045f, 5, 8);
        pop();
        eyes(0.017f, 0.012f, 0.038f, 0.008f, a.blink, s, true, g);
        ears(argb(p, 1f), 0.022f, 0.035f, 0f, 0.013f, 0.035f, 0.25f, 0f);
        float fl = sin(a.flap * 1.6f) * 0.9f;
        for (int side = -1; side <= 1; side += 2) {
            push();
            translate(side * 0.035f, 0.01f, 0);
            rot(2, -side * fl);
            // tirtikli zar kanat: uc parca
            sheet4(LIT, argb(mul(p, 1.2f), 0.95f), 0, 0, 0.02f, side * 0.07f, 0.03f, 0.01f, side * 0.06f, -0.01f, -0.03f, 0, -0.01f, -0.025f);
            sheet4(LIT, argb(mul(p, 1.2f), 0.95f), side * 0.07f, 0.03f, 0.01f, side * 0.14f, 0.02f, -0.005f, side * 0.11f, -0.015f, -0.04f,
                    side * 0.06f, -0.01f, -0.03f);
            pop();
        }
    }

    private void kus(Anim a, int p, int s, float g) {
        float t = a.time;
        push();
        scale(0.9f, 0.9f, 1.25f);
        sphere(LIT, argb(p, 1f), 0.052f, 5, 8);
        translate(0, -0.012f, 0.012f);
        sphere(LIT, argb(mix(p, 0xFFFFFF, 0.6f), 1f), 0.042f, 4, 8);
        pop();
        push();
        translate(0, 0.04f, 0.05f);
        rot(1, a.lookYaw * 0.8f);
        rot(0, sin(t * 2.5) * 0.15f);
        sphere(LIT, argb(p, 1f), 0.036f, 5, 8);
        eyes(0.02f, 0.008f, 0.027f, 0.007f, a.blink, 0x101010, false, g);
        push();
        translate(0, -0.004f, 0.04f);
        rot(0, (float) (Math.PI / 2));
        cyl2(LIT, argb(s, 1f), 0.009f, 0f, 0.022f, 4);
        pop();
        pop();
        push();
        translate(0, 0.01f, -0.07f);
        rot(0, 0.25f);
        sheet4(LIT, argb(mul(p, 0.85f), 1f), -0.02f, 0, 0, 0.02f, 0, 0, 0.03f, 0, -0.06f, -0.03f, 0, -0.06f);
        pop();
        float fl = 0.1f + 0.9f * (0.5f + 0.5f * sin(a.flap * 1.4f));
        wingPair(LIT, argb(mul(p, 0.8f), 1f), 0.04f, 0.02f, 0f, 0.1f, 0.06f, fl, 0.3f);
    }

    private void tilki(Anim a, int p, int s, float g) {
        float t = a.time, walk = t * 6f * Math.min(1f, a.speed);
        push();
        translate(0, -0.03f, -0.03f);
        box(LIT, argb(p, 1f), 0.048f, 0.04f, 0.085f);
        translate(0, -0.018f, 0.03f);
        box(LIT, argb(s, 1f), 0.035f, 0.025f, 0.05f);
        pop();
        legs(argb(0x2A2020, 1f), 0.032f, -0.06f, 0.03f, -0.085f, 0.012f, 0.04f, walk);
        push();
        translate(0, 0.04f, 0.07f);
        rot(1, a.lookYaw * 0.6f);
        box(LIT, argb(p, 1f), 0.055f, 0.045f, 0.045f);
        push();
        translate(0, -0.018f, 0.06f);
        box(LIT, argb(s, 1f), 0.022f, 0.016f, 0.025f);
        translate(0, 0.012f, 0.025f);
        box(LIT, argb(0x1A1414, 1f), 0.007f, 0.006f, 0.004f);
        pop();
        eyes(0.026f, 0.01f, 0.045f, 0.009f, a.blink, 0x1A1008, false, g);
        ears(argb(p, 1f), 0.034f, 0.045f, -0.005f, 0.018f, 0.05f, 0.15f, 0.06f * sin(t * 2));
        pop();
        // kabarik kuyruk
        push();
        translate(0, -0.01f, -0.12f);
        rot(0, 0.5f);
        rot(1, sin(t * 2) * 0.35f);
        translate(0, 0, -0.04f);
        push();
        scale(0.8f, 0.8f, 1.6f);
        sphere(LIT, argb(p, 1f), 0.035f, 4, 8);
        pop();
        translate(0, 0, -0.045f);
        sphere(LIT, argb(s, 1f), 0.025f, 4, 6);
        pop();
    }

    private void panda(Anim a, int p, int s, float g) {
        float t = a.time;
        push();
        translate(0, -0.04f, -0.01f);
        scale(1f, 0.95f, 1.1f);
        sphere(LIT, argb(p, 1f), 0.07f, 6, 10);
        pop();
        for (int side = -1; side <= 1; side += 2) {
            push();
            translate(side * 0.05f, -0.06f, 0.04f);
            sphere(LIT, argb(s, 1f), 0.022f, 3, 6);
            pop();
        }
        push();
        translate(0, 0.045f, 0.04f);
        rot(1, a.lookYaw * 0.5f);
        sphere(LIT, argb(p, 1f), 0.058f, 6, 10);
        for (int side = -1; side <= 1; side += 2) {
            push();
            translate(side * 0.024f, 0.006f, 0.046f);
            rot(2, side * 0.5f);
            scale(1f, 1.4f, 0.5f);
            sphere(LIT, argb(s, 1f), 0.016f, 3, 6);
            pop();
            push();
            translate(side * 0.042f, 0.05f, 0f);
            rot(2, -side * 0.1f * sin(t * 2 + side));
            sphere(LIT, argb(s, 1f), 0.018f, 3, 6);
            pop();
        }
        eyes(0.024f, 0.008f, 0.056f, 0.006f, a.blink, 0xFFFFFF, true, 1f);
        push();
        translate(0, -0.015f, 0.056f);
        box(LIT, argb(s, 1f), 0.009f, 0.006f, 0.004f);
        pop();
        pop();
    }

    // ------------------------------------------------------------------ yaratiklar

    private void balcik(Anim a, int p, int s, float g) {
        float t = a.time;
        float bounce = Math.abs(sin(t * 3.2));
        float sq = 1f - 0.18f * (1f - bounce);
        translate(0, bounce * 0.04f - 0.02f, 0);
        scale(1f / (float) Math.sqrt(sq), sq, 1f / (float) Math.sqrt(sq));
        box(LIT, argb(p, 0.55f), 0.07f, 0.07f, 0.07f);
        box(GLOW, argb(s, 0.9f * Math.min(1f, g)), 0.03f, 0.03f, 0.03f);
        for (int side = -1; side <= 1; side += 2) {
            push();
            translate(side * 0.028f, 0.02f, 0.071f);
            box(LIT, argb(0x143A10, 1f), 0.01f, 0.012f * Math.max(0.15f, 1f - a.blink), 0.002f);
            pop();
        }
        push();
        translate(0, -0.02f, 0.071f);
        box(LIT, argb(0x143A10, 1f), 0.015f, 0.004f, 0.002f);
        pop();
    }

    private void hayalet(Anim a, int p, int s, float g) {
        float t = a.time;
        halo(0, 0, 0, 0.28f, argb(s, 0.22f * Math.min(1f, g)));
        push();
        translate(0, 0.03f, 0);
        sphere(LIT, argb(p, 0.85f), 0.065f, 6, 10);
        pop();
        // dalgali etek
        int n = 12;
        for (int i = 0; i < n; i++) {
            double a0 = 2 * Math.PI * i / n, a1 = 2 * Math.PI * (i + 1) / n;
            float r = 0.065f;
            float w0 = -0.09f + 0.015f * sin(t * 4 + i * 1.7), w1 = -0.09f + 0.015f * sin(t * 4 + (i + 1) * 1.7);
            quadL(LIT, argb(p, 0.85f), 0, -0.02f, 0, false, (float) Math.sin(a0) * r, 0.03f, (float) Math.cos(a0) * r, (float) Math.sin(a1) * r, 0.03f,
                    (float) Math.cos(a1) * r, (float) Math.sin(a1) * r * 1.05f, w1, (float) Math.cos(a1) * r * 1.05f, (float) Math.sin(a0) * r * 1.05f, w0,
                    (float) Math.cos(a0) * r * 1.05f);
        }
        eyes(0.024f, 0.04f, 0.058f, 0.012f, a.blink, 0x101830, false, g);
        push();
        translate(0, 0.012f, 0.062f);
        scale(1f, 1f + 0.3f * sin(t * 2), 0.4f);
        sphere(LIT, argb(0x101830, 1f), 0.009f, 3, 6);
        pop();
        for (int side = -1; side <= 1; side += 2) {
            push();
            translate(side * 0.065f, 0.0f, 0.01f);
            rot(2, side * (0.4f + 0.2f * sin(t * 3 + side)));
            translate(side * 0.015f, 0, 0);
            box(LIT, argb(p, 0.85f), 0.018f, 0.01f, 0.012f);
            pop();
        }
    }

    private void kafatasi(Anim a, int p, int s, float g) {
        float t = a.time;
        halo(0, 0, 0, 0.24f, argb(s, 0.3f * Math.min(1f, g)));
        push();
        rot(1, a.lookYaw * 0.6f);
        push();
        translate(0, 0.02f, 0);
        scale(1f, 0.95f, 1.05f);
        sphere(LIT, argb(p, 1f), 0.065f, 6, 10);
        pop();
        eyes(0.026f, 0.025f, 0.05f, 0.016f, 0f, 0x0A0A0A, false, g);
        eyes(0.026f, 0.025f, 0.058f, 0.007f, a.blink, s, true, g);
        push();
        translate(0, 0.002f, 0.064f);
        rot(0, 2.6f);
        cyl2(LIT, argb(0x1A1A1A, 1f), 0.008f, 0f, 0.016f, 3);
        pop();
        // takirdayan cene
        push();
        translate(0, -0.035f, 0.01f);
        rot(0, 0.12f + 0.12f * Math.abs(sin(t * 9)));
        translate(0, -0.012f, 0.025f);
        box(LIT, argb(mul(p, 0.92f), 1f), 0.04f, 0.012f, 0.03f);
        for (int i = -2; i <= 2; i++) {
            push();
            translate(i * 0.013f, 0.014f, 0.028f);
            box(LIT, argb(0xFFFFFF, 1f), 0.004f, 0.005f, 0.003f);
            pop();
        }
        pop();
        pop();
    }

    private void balkabagi(Anim a, int p, int s, float g) {
        float t = a.time;
        float flick = 0.75f + 0.25f * sin(t * 13) * sin(t * 7.3);
        halo(0, 0, 0.04f, 0.25f, argb(s, 0.35f * flick * Math.min(1f, g)));
        for (int i = 0; i < 8; i++) {
            push();
            rot(1, i * (float) (Math.PI / 4));
            translate(0, 0, 0.032f);
            scale(0.55f, 0.85f, 0.75f);
            sphere(LIT, argb(i % 2 == 0 ? p : mul(p, 0.88f), 1f), 0.075f, 5, 8);
            pop();
        }
        push();
        translate(0, 0.07f, 0);
        rot(2, 0.2f);
        cyl(LIT, argb(0x4A6A20, 1f), 0.01f, 0.03f, 5);
        pop();
        // oyma yuz (parlak)
        float z = 0.083f;
        for (int side = -1; side <= 1; side += 2)
            sheet4(GLOW, argb(s, flick * Math.min(1f, g)), side * 0.012f, 0.02f, z, side * 0.04f, 0.02f, z, side * 0.026f, 0.042f, z, side * 0.026f, 0.042f, z);
        sheet4(GLOW, argb(s, flick * Math.min(1f, g)), -0.008f, 0.0f, z + 0.003f, 0.008f, 0.0f, z + 0.003f, 0, 0.014f, z + 0.003f, 0, 0.014f, z + 0.003f);
        sheet4(GLOW, argb(s, flick * Math.min(1f, g)), -0.045f, -0.02f, z - 0.004f, 0.045f, -0.02f, z - 0.004f, 0.03f, -0.04f, z - 0.006f, -0.03f, -0.04f,
                z - 0.006f);
    }

    private void kardanAdam(Anim a, int p, int s, float g) {
        float t = a.time;
        rot(2, sin(t * 2) * 0.06f);
        push();
        translate(0, -0.05f, 0);
        sphere(LIT, argb(p, 1f), 0.06f, 5, 10);
        pop();
        push();
        translate(0, 0.035f, 0);
        sphere(LIT, argb(p, 1f), 0.045f, 5, 10);
        eyes(0.017f, 0.012f, 0.04f, 0.006f, a.blink, 0x101010, false, g);
        push();
        translate(0, 0f, 0.05f);
        rot(0, (float) (Math.PI / 2));
        cyl2(LIT, argb(s, 1f), 0.008f, 0f, 0.035f, 5);
        pop();
        push();
        translate(0, 0.045f, 0);
        cyl(LIT, argb(0x1A1A20, 1f), 0.038f, 0.006f, 10);
        translate(0, 0.022f, 0);
        cyl(LIT, argb(0x1A1A20, 1f), 0.025f, 0.04f, 10);
        translate(0, -0.012f, 0);
        cyl(LIT, argb(0xC02020, 1f), 0.0255f, 0.008f, 10);
        pop();
        pop();
        for (int i = 0; i < 2; i++) {
            push();
            translate(0, -0.03f - i * 0.03f, 0.057f - i * 0.004f);
            box(LIT, argb(0x1A1A20, 1f), 0.006f, 0.006f, 0.004f);
            pop();
        }
        push();
        translate(0, -0.005f, 0);
        torus(LIT, argb(0xD03030, 1f), 0.035f, 0.009f, 12, 4);
        pop();
        for (int side = -1; side <= 1; side += 2) {
            push();
            translate(side * 0.05f, -0.01f, 0);
            rot(2, side * (-0.7f + 0.15f * sin(t * 3 + side)));
            translate(side * 0.03f, 0, 0);
            box(LIT, argb(0x5A3A20, 1f), 0.03f, 0.003f, 0.003f);
            pop();
        }
    }

    private void mantar(Anim a, int p, int s, float g) {
        float t = a.time;
        float hop = Math.abs(sin(t * 3)) * 0.015f;
        translate(0, hop, 0);
        push();
        translate(0, -0.04f, 0);
        cyl2(LIT, argb(s, 1f), 0.04f, 0.032f, 0.07f, 10);
        pop();
        eyes(0.017f, -0.03f, 0.036f, 0.008f, a.blink, 0x101010, false, g);
        push();
        translate(0, 0.0f, 0);
        rot(2, sin(t * 2.2) * 0.08f);
        push();
        scale(1f, 0.6f, 1f);
        sphere(LIT, argb(p, 1f), 0.085f, 5, 12);
        pop();
        for (int i = 0; i < 6; i++) {
            push();
            rot(1, i * 1.05f + 0.3f);
            rot(0, -0.6f - (i % 2) * 0.35f);
            translate(0, 0, 0.083f);
            box(LIT, argb(s, 1f), 0.012f, 0.012f, 0.004f);
            pop();
        }
        pop();
    }

    private void orumcek(Anim a, int p, int s, float g) {
        float t = a.time;
        push();
        translate(0, 0, -0.04f);
        scale(1f, 0.85f, 1.2f);
        sphere(LIT, argb(p, 1f), 0.05f, 5, 8);
        pop();
        push();
        translate(0, 0, 0.025f);
        sphere(LIT, argb(mul(p, 1.2f), 1f), 0.03f, 4, 8);
        for (int i = 0; i < 4; i++) {
            push();
            translate((i % 2 == 0 ? -1 : 1) * (0.008f + (i / 2) * 0.012f), 0.012f - (i / 2) * 0.006f, 0.027f);
            sphere(GLOW, argb(s, Math.min(1f, g)), 0.005f, 2, 4);
            pop();
        }
        pop();
        for (int side = -1; side <= 1; side += 2) {
            for (int i = 0; i < 4; i++) {
                push();
                translate(side * 0.025f, 0, 0.03f - i * 0.022f);
                rot(1, side * (0.9f + (i - 1.5f) * 0.35f) * 1f + sin(t * 8 + i * 1.3 + side) * 0.15f);
                rot(2, -side * 0.5f);
                translate(side * 0.025f, 0, 0);
                box(LIT, argb(p, 1f), 0.025f, 0.004f, 0.004f);
                translate(side * 0.025f, 0, 0);
                rot(2, side * 1.3f);
                translate(side * 0.025f, 0, 0);
                box(LIT, argb(p, 1f), 0.025f, 0.0035f, 0.0035f);
                pop();
            }
        }
    }

    private void kuzu(Anim a, int p, int s, float g) {
        float t = a.time, walk = t * 6f * Math.min(1f, a.speed);
        // pamuk yun: kucuk kurelerden bulut
        for (int i = 0; i < 9; i++) {
            push();
            translate(((i % 3) - 1) * 0.035f, -0.02f + ((i / 3) % 2) * 0.025f, ((i / 3) - 1) * 0.04f - 0.01f);
            sphere(LIT, argb(p, 1f), 0.04f, 4, 6);
            pop();
        }
        legs(argb(s, 1f), 0.03f, -0.05f, 0.02f, -0.05f, 0.01f, 0.05f, walk);
        push();
        translate(0, 0.02f, 0.07f);
        rot(1, a.lookYaw * 0.5f);
        push();
        scale(0.85f, 1f, 1.1f);
        sphere(LIT, argb(s, 1f), 0.036f, 4, 8);
        pop();
        eyes(0.02f, 0.008f, 0.03f, 0.007f, a.blink, 0xFFFFFF, true, 1f);
        for (int side = -1; side <= 1; side += 2) {
            push();
            translate(side * 0.035f, 0.012f, -0.005f);
            rot(2, side * (1.2f + 0.2f * sin(t * 2.5 + side)));
            translate(0, 0.02f, 0);
            box(LIT, argb(s, 1f), 0.006f, 0.02f, 0.01f);
            pop();
        }
        push();
        translate(0, 0.035f, -0.01f);
        sphere(LIT, argb(p, 1f), 0.02f, 3, 6);
        pop();
        pop();
    }

    // ------------------------------------------------------------------ nesneler / dogal olaylar

    private void robot(Anim a, int p, int s, float g) {
        float t = a.time;
        boolean on = ((int) (t * 2f)) % 2 == 0;
        box(LIT, argb(p, 1f), 0.05f, 0.04f, 0.04f);
        push();
        translate(0, 0.075f, 0);
        rot(1, a.lookYaw * 0.7f);
        box(LIT, argb(mix(p, 0xFFFFFF, 0.15f), 1f), 0.055f, 0.04f, 0.045f);
        push();
        translate(0, 0, 0.046f);
        box(LIT, argb(0x10141C, 1f), 0.045f, 0.03f, 0.002f);
        pop();
        for (int side = -1; side <= 1; side += 2) {
            push();
            translate(side * 0.02f, 0.006f, 0.049f);
            box(GLOW, argb(s, Math.min(1f, g)), 0.008f, Math.max(0.0015f, 0.008f * (1f - 0.85f * a.blink)), 0.002f);
            pop();
        }
        push();
        translate(0, -0.013f, 0.049f);
        box(GLOW, argb(s, 0.8f * Math.min(1f, g)), 0.016f, 0.002f, 0.002f);
        pop();
        push();
        translate(0, 0.055f, 0);
        cyl(LIT, argb(0x606A78, 1f), 0.003f, 0.03f, 4);
        translate(0, 0.02f, 0);
        sphere(GLOW, argb(on ? 0xFF4040 : 0x501010, Math.min(1f, g)), 0.008f, 3, 6);
        pop();
        pop();
        for (int side = -1; side <= 1; side += 2) {
            push();
            translate(side * 0.058f, 0.02f, 0);
            rot(0, sin(t * 3 + side) * 0.4f);
            translate(0, -0.035f, 0);
            box(LIT, argb(mul(p, 0.8f), 1f), 0.008f, 0.035f, 0.008f);
            pop();
        }
        push();
        translate(0, -0.06f, 0);
        cyl2(GLOW, argb(s, 0.6f * Math.min(1f, g)), 0.03f, 0.012f, 0.03f, 8);
        pop();
        halo(0, -0.08f, 0, 0.1f, argb(s, 0.4f * Math.min(1f, g)));
    }

    private void ufo(Anim a, int p, int s, float g) {
        float t = a.time;
        rot(2, sin(t * 1.3) * 0.12f);
        push();
        rot(1, t * 2f);
        push();
        scale(1f, 0.28f, 1f);
        sphere(LIT, argb(p, 1f), 0.11f, 4, 14);
        pop();
        int n = 8;
        for (int i = 0; i < n; i++) {
            push();
            rot(1, i * (float) (2 * Math.PI / n));
            translate(0, -0.005f, 0.1f);
            boolean lit = ((int) (t * 6) + i) % 3 == 0;
            sphere(GLOW, argb(lit ? s : mul(s, 0.3f), Math.min(1f, g)), 0.008f, 2, 4);
            pop();
        }
        pop();
        push();
        translate(0, 0.03f, 0);
        sphere(LIT, argb(0xA0E0FF, 0.55f), 0.05f, 4, 8);
        pop();
        // isin: asagi dogru genisleyen saydam koni
        push();
        translate(0, -0.1f, 0);
        cyl2(GLOW, argb(s, 0.18f * Math.min(1f, g)), 0.06f, 0.025f, 0.14f, 10);
        pop();
    }

    private void roket(Anim a, int p, int s, float g) {
        float t = a.time;
        scale(1.35f, 1.35f, 1.35f);
        rot(0, 1.1f);
        rot(1, t * 1.5f);
        cyl(LIT, argb(p, 1f), 0.03f, 0.11f, 10);
        push();
        translate(0, 0.08f, 0);
        cyl2(LIT, argb(s, 1f), 0.03f, 0f, 0.05f, 10);
        pop();
        push();
        translate(0, 0.02f, 0.029f);
        rot(0, (float) (Math.PI / 2));
        cyl(GLOW, argb(0x7AD8FF, 0.9f * Math.min(1f, g)), 0.012f, 0.004f, 8);
        pop();
        for (int i = 0; i < 3; i++) {
            push();
            rot(1, i * (float) (2 * Math.PI / 3));
            sheet4(LIT, argb(s, 1f), 0, -0.035f, 0.028f, 0, -0.06f, 0.06f, 0, -0.07f, 0.06f, 0, -0.06f, 0.028f);
            pop();
        }
        float fl = 0.8f + 0.3f * sin(t * 30);
        push();
        translate(0, -0.075f, 0);
        scale(1f, fl, 1f);
        cyl2(GLOW, argb(0xFFE070, Math.min(1f, g)), 0f, 0.022f, 0.05f, 8);
        translate(0, -0.02f, 0);
        cyl2(GLOW, argb(0xFF6A1E, 0.8f * Math.min(1f, g)), 0f, 0.03f, 0.08f, 8);
        pop();
        halo(0, -0.1f, 0, 0.12f, argb(0xFF8A2A, 0.5f * Math.min(1f, g)));
    }

    private void gezegen(Anim a, int p, int s, float g) {
        float t = a.time;
        halo(0, 0, 0, 0.3f, argb(s, 0.2f * Math.min(1f, g)));
        push();
        rot(2, 0.35f);
        push();
        rot(1, t * 0.8f);
        sphere(LIT, argb(p, 1f), 0.07f, 8, 14);
        torus(LIT, argb(mul(p, 0.8f), 1f), 0.071f, 0.006f, 16, 3);
        pop();
        sphere(GLOW, argb(s, 0.16f * Math.min(1f, g)), 0.076f, 6, 12); // atmosfer
        push();
        scale(1f, 0.08f, 1f);
        torus(LIT, argb(s, 0.75f), 0.115f, 0.025f, 24, 4);
        pop();
        pop();
        push();
        rot(1, t * 2.2f);
        translate(0.16f, 0.02f, 0);
        sphere(LIT, argb(0xC8C8D0, 1f), 0.018f, 4, 6);
        pop();
    }

    private void gunes(Anim a, int p, int s, float g) {
        float t = a.time;
        float pulse = 1f + 0.05f * sin(t * 3);
        halo(0, 0, 0, 0.36f * pulse, argb(p, 0.5f * Math.min(1f, g)));
        sphere(GLOW, argb(mix(p, s, 0.4f), Math.min(1f, g)), 0.06f * pulse, 6, 12);
        push();
        rot(2, t * 0.6f);
        for (int i = 0; i < 10; i++) {
            push();
            rot(2, i * (float) (Math.PI / 5));
            translate(0, 0.085f + 0.008f * sin(t * 5 + i), 0);
            cyl2(GLOW, argb(p, 0.9f * Math.min(1f, g)), 0.014f, 0f, 0.04f, 4);
            pop();
        }
        pop();
    }

    private void ay(Anim a, int p, int s, float g) {
        float t = a.time;
        halo(0, 0, 0, 0.3f, argb(p, 0.3f * Math.min(1f, g)));
        push();
        rot(2, sin(t * 1.4) * 0.25f);
        // hilal: iki yay arasi seritten ekstrude
        int n = 14;
        for (int i = 0; i < n; i++) {
            double a0 = -Math.PI * 0.8 + 1.6 * Math.PI * i / n, a1 = -Math.PI * 0.8 + 1.6 * Math.PI * (i + 1) / n;
            float ro = 0.1f, ri = 0.075f, off = 0.035f;
            float ox0 = (float) Math.cos(a0) * ro, oy0 = (float) Math.sin(a0) * ro, ox1 = (float) Math.cos(a1) * ro, oy1 = (float) Math.sin(a1) * ro;
            float ix0 = (float) Math.cos(a0) * ri + off * (1f - Math.abs((float) Math.sin(a0))), iy0 = (float) Math.sin(a0) * ri;
            float ix1 = (float) Math.cos(a1) * ri + off * (1f - Math.abs((float) Math.sin(a1))), iy1 = (float) Math.sin(a1) * ri;
            for (int f = -1; f <= 1; f += 2)
                quadL(GLOW, argb(p, Math.min(1f, g)), (ox0 + ix0) / 2, (oy0 + iy0) / 2, -f * 0.05f, false, ix0, iy0, f * 0.015f, ox0, oy0, f * 0.015f,
                        ox1, oy1, f * 0.015f, ix1, iy1, f * 0.015f);
            quadL(GLOW, argb(mul(p, 0.85f), Math.min(1f, g)), 0, 0, 0, false, ox0, oy0, 0.015f, ox0, oy0, -0.015f, ox1, oy1, -0.015f, ox1, oy1, 0.015f);
        }
        pop();
        push();
        translate(0.1f, 0.08f, 0);
        rot(2, t * 1.5f);
        float tw = 0.7f + 0.3f * sin(t * 4);
        scale(tw, tw, tw);
        extrude(GLOW, argb(s, Math.min(1f, g)), STAR_IN, 0.01f, 0, 0);
        pop();
    }

    private void bulut(Anim a, int p, int s, float g, boolean storm) {
        float t = a.time;
        float[][] puff = {{0, 0.01f, 0, 0.055f}, {-0.055f, -0.005f, 0.01f, 0.042f}, {0.055f, -0.005f, -0.005f, 0.045f}, {-0.02f, 0.035f, -0.01f, 0.04f},
                {0.025f, 0.03f, 0.015f, 0.038f}, {0, -0.01f, -0.04f, 0.04f}};
        for (float[] pf : puff) {
            push();
            translate(pf[0], pf[1] + 0.004f * sin(t * 2 + pf[0] * 40), pf[2]);
            sphere(LIT, argb(p, 0.95f), pf[3], 4, 8);
            pop();
        }
        if (!storm) {
            for (int i = 0; i < 5; i++) {
                float u = (float) ((t * 1.6 + i * 0.37) % 1.0);
                push();
                translate((i - 2) * 0.025f, -0.05f - u * 0.12f, (i % 2) * 0.02f - 0.01f);
                box(GLOW, argb(s, 0.9f * (1f - u) * Math.min(1f, g)), 0.003f, 0.01f, 0.003f);
                pop();
            }
        } else {
            // bulutun icinde surekli titreyen sonuk elektrik
            push();
            translate(0, 0.005f, 0.02f);
            sphere(GLOW, argb(s, (0.18f + 0.1f * sin(t * 17)) * Math.min(1f, g)), 0.03f, 3, 6);
            pop();
            float flash = Math.max(0f, sin(t * 3.1) * sin(t * 7.7) * 2f - 1f);
            if (flash > 0f) {
                halo(0, -0.02f, 0, 0.3f, argb(s, 0.6f * flash * Math.min(1f, g)));
                push();
                translate(0, -0.06f, 0.01f);
                sheet4(GLOW, argb(s, flash * Math.min(1f, g)), -0.01f, 0, 0, 0.015f, 0, 0, 0.0f, -0.04f, 0, -0.005f, -0.035f, 0);
                sheet4(GLOW, argb(s, flash * Math.min(1f, g)), 0.0f, -0.035f, 0, 0.012f, -0.035f, 0, -0.006f, -0.085f, 0, -0.006f, -0.085f, 0);
                pop();
            }
        }
    }

    private void atesTopu(Anim a, int p, int s, float g) {
        float t = a.time;
        halo(0, 0, 0, 0.3f, argb(p, 0.5f * Math.min(1f, g)));
        sphere(GLOW, argb(s, Math.min(1f, g)), 0.045f, 5, 8);
        sphere(GLOW, argb(p, 0.55f * Math.min(1f, g)), 0.065f, 5, 10);
        for (int i = 0; i < 9; i++) {
            push();
            rot(1, i * 0.7f + t * 2f);
            rot(0, -0.6f - (i % 3) * 0.3f);
            float len = 0.05f + 0.025f * sin(t * 9 + i * 2.1);
            translate(0, 0.055f + len / 2, 0);
            cyl2(GLOW, argb(i % 2 == 0 ? p : mix(p, s, 0.5f), 0.85f * Math.min(1f, g)), 0.02f, 0f, len, 4);
            pop();
        }
    }

    private void karTanesi(Anim a, int p, int s, float g) {
        float t = a.time;
        halo(0, 0, 0, 0.26f, argb(p, 0.3f * Math.min(1f, g)));
        rot(1, t * 0.9f);
        rot(0, sin(t * 0.7) * 0.2f);
        for (int i = 0; i < 6; i++) {
            push();
            rot(2, i * (float) (Math.PI / 3));
            translate(0, 0.06f, 0);
            box(GLOW, argb(p, Math.min(1f, g)), 0.006f, 0.06f, 0.004f);
            for (int k = 0; k < 2; k++) {
                push();
                translate(0, -0.01f + k * 0.035f, 0);
                for (int side = -1; side <= 1; side += 2) {
                    push();
                    rot(2, side * 0.8f);
                    translate(0, 0.014f - k * 0.003f, 0);
                    box(GLOW, argb(s, Math.min(1f, g)), 0.004f, 0.016f - k * 0.004f, 0.003f);
                    pop();
                }
                pop();
            }
            pop();
        }
        bipyramid(GLOW, argb(s, Math.min(1f, g)), 0.016f, 0.012f, 0.012f, 6);
    }

    private void elmas(Anim a, int p, int s, float g) {
        float t = a.time;
        halo(0, 0, 0, 0.3f, argb(p, 0.35f * Math.min(1f, g)));
        rot(1, t * 1.2f);
        push();
        translate(0, 0.02f, 0);
        cyl2(LIT, argb(mix(p, 0xFFFFFF, 0.25f), 0.8f), 0.08f, 0.05f, 0.03f, 8);
        translate(0, -0.015f, 0);
        rot(0, (float) Math.PI);
        translate(0, 0.045f, 0);
        cyl2(LIT, argb(p, 0.8f), 0.08f, 0f, 0.09f, 8);
        pop();
        push();
        rot(1, -t * 2.5f);
        bipyramid(GLOW, argb(s, 0.7f * Math.min(1f, g)), 0.025f, 0.03f, 0.05f, 4);
        pop();
    }

    private void para(Anim a, int p, int s, float g) {
        float t = a.time;
        halo(0, 0, 0, 0.26f, argb(p, 0.35f * Math.min(1f, g)));
        rot(1, t * 3f);
        push();
        rot(0, (float) (Math.PI / 2));
        cyl(LIT, argb(p, 1f), 0.075f, 0.012f, 16);
        torus(LIT, argb(mul(p, 1.12f), 1f), 0.068f, 0.006f, 16, 3);
        pop();
        for (int f = -1; f <= 1; f += 2) {
            push();
            translate(0, 0, f * 0.0065f);
            if (f < 0) rot(1, (float) Math.PI);
            push();
            scale(0.55f, 0.55f, 1f);
            extrude(GLOW, argb(s, 0.9f * Math.min(1f, g)), STAR_IN, 0.003f, 0, 0);
            pop();
            pop();
        }
    }

    private void enerjiKupu(Anim a, int p, int s, float g) {
        float t = a.time;
        halo(0, 0, 0, 0.3f, argb(p, 0.35f * Math.min(1f, g)));
        push();
        rot(1, t * 1.1f);
        rot(0, t * 0.7f);
        box(LIT, argb(p, 0.45f), 0.06f, 0.06f, 0.06f);
        pop();
        push();
        rot(1, -t * 1.7f);
        rot(2, t * 0.9f);
        box(LIT, argb(mix(p, s, 0.5f), 0.55f), 0.038f, 0.038f, 0.038f);
        pop();
        float pulse = 0.85f + 0.15f * sin(t * 5);
        sphere(GLOW, argb(s, Math.min(1f, g)), 0.018f * pulse, 3, 6);
    }

    private void prizma(Anim a, int p, int s, float g) {
        float t = a.time;
        rot(1, t * 1.3f);
        rot(2, 0.2f);
        cyl(LIT, argb(p, 0.6f), 0.06f, 0.13f, 3);
        push();
        rot(1, t * -2f);
        for (int i = 0; i < 6; i++) {
            push();
            rot(1, i * (float) (Math.PI / 3));
            translate(0, -0.02f + i * 0.008f, 0.075f);
            int c = PetMesh.hsvCol((t * 0.2f + i / 6f) % 1f);
            box(GLOW, argb(c, 0.8f * Math.min(1f, g)), 0.012f, 0.003f, 0.025f);
            pop();
        }
        pop();
        halo(0, 0, 0, 0.22f, argb(s, 0.25f * Math.min(1f, g)));
    }

    static int hsvCol(float h) {
        h -= (float) Math.floor(h);
        float r = Math.max(0, Math.min(1, Math.abs(h * 6 - 3) - 1)), gg = Math.max(0, Math.min(1, 2 - Math.abs(h * 6 - 2))),
                b = Math.max(0, Math.min(1, 2 - Math.abs(h * 6 - 4)));
        return ((int) (r * 255) << 16) | ((int) (gg * 255) << 8) | (int) (b * 255);
    }

    private void kumSaati(Anim a, int p, int s, float g) {
        float t = a.time;
        // her 4 sn'de bir yumusakca ters doner
        float cyc = (float) (t % 4.0);
        float flip = cyc > 3.4f ? (cyc - 3.4f) / 0.6f : 0f;
        flip = flip * flip * (3 - 2 * flip);
        float fill = Math.min(1f, cyc / 3.4f);
        rot(2, (float) Math.PI * flip);
        for (int f = -1; f <= 1; f += 2) {
            push();
            translate(0, f * 0.075f, 0);
            cyl(LIT, argb(mul(p, 0.75f), 1f), 0.055f, 0.012f, 8);
            translate(0, f * 0.009f, 0);
            bipyramid(GLOW, argb(s, 0.9f * Math.min(1f, g)), 0.01f, 0.008f, 0.008f, 4);
            pop();
        }
        for (int i = 0; i < 4; i++) {
            push();
            rot(1, i * (float) (Math.PI / 2) + 0.785f);
            translate(0.05f, 0, 0);
            cyl(LIT, argb(p, 1f), 0.005f, 0.14f, 4);
            pop();
        }
        push();
        translate(0, 0.035f, 0);
        cyl2(LIT, argb(0xD8F0FF, 0.35f), 0.006f, 0.04f, 0.07f, 8);
        translate(0, -0.07f, 0);
        cyl2(LIT, argb(0xD8F0FF, 0.35f), 0.04f, 0.006f, 0.07f, 8);
        pop();
        float top = 1f - fill;
        if (top > 0.02f) {
            push();
            translate(0, 0.012f + 0.02f * top, 0);
            cyl2(LIT, argb(s, 1f), 0.004f, 0.035f * top, 0.045f * top, 8);
            pop();
        }
        if (fill > 0.02f) {
            push();
            translate(0, -0.067f + 0.022f * fill, 0);
            cyl2(LIT, argb(s, 1f), 0.036f, 0.036f * (1f - fill * 0.7f), 0.044f * fill, 8);
            pop();
            if (fill < 1f && flip == 0f) {
                push();
                translate(0, -0.02f, 0);
                box(LIT, argb(s, 1f), 0.0018f, 0.03f, 0.0018f);
                pop();
            }
        }
    }

    private void kitap(Anim a, int p, int s, float g) {
        float t = a.time;
        halo(0, 0.02f, 0, 0.24f, argb(s, 0.3f * Math.min(1f, g)));
        rot(0, -0.5f);
        float open = 1.1f + 0.15f * sin(t * 1.5);
        for (int side = -1; side <= 1; side += 2) {
            push();
            rot(2, side * (float) (Math.PI / 2 - open / 2));
            translate(side * 0.05f, 0, 0);
            box(LIT, argb(p, 1f), 0.05f, 0.004f, 0.07f);
            translate(0, 0.008f, 0);
            box(LIT, argb(0xF4ECD8, 1f), 0.046f, 0.004f, 0.065f);
            push();
            translate(0, 0.005f, 0);
            for (int k = 0; k < 3; k++) {
                push();
                translate(side * 0.005f, 0, 0.035f - k * 0.025f);
                box(GLOW, argb(s, (0.6f + 0.4f * sin(t * 4 + k + side)) * Math.min(1f, g)), 0.025f, 0.001f, 0.004f);
                pop();
            }
            pop();
            pop();
        }
        // dalgalanan sayfa
        float u = (float) ((t * 0.5) % 1.0);
        push();
        rot(2, (float) (Math.PI / 2 - open / 2) - u * (float) (Math.PI - open));
        translate(0.046f, 0.012f, 0);
        box(LIT, argb(0xFFF8E8, 0.95f), 0.044f, 0.0015f, 0.064f);
        pop();
    }

    private void kilic(Anim a, int p, int s, float g) {
        float t = a.time;
        rot(1, t * 1.4f);
        rot(2, 0.35f + 0.1f * sin(t * 1.8));
        push();
        translate(0, 0.04f, 0);
        box(LIT, argb(mix(p, 0xFFFFFF, 0.3f), 1f), 0.012f, 0.085f, 0.004f);
        translate(0, 0.09f, 0);
        rot(2, (float) (Math.PI / 4));
        box(LIT, argb(mix(p, 0xFFFFFF, 0.3f), 1f), 0.0085f, 0.0085f, 0.004f);
        pop();
        push();
        translate(0, 0.04f, 0.003f);
        box(GLOW, argb(p, 0.75f * Math.min(1f, g)), 0.004f, 0.08f, 0.002f);
        pop();
        push();
        translate(0, -0.05f, 0);
        box(LIT, argb(0xFFC830, 1f), 0.04f, 0.007f, 0.008f);
        translate(0, -0.03f, 0);
        box(LIT, argb(s, 1f), 0.007f, 0.025f, 0.007f);
        translate(0, -0.03f, 0);
        sphere(LIT, argb(0xFFC830, 1f), 0.01f, 3, 6);
        pop();
        halo(0, 0.04f, 0, 0.18f, argb(p, 0.25f * Math.min(1f, g)));
    }

    private void fener(Anim a, int p, int s, float g) {
        float t = a.time;
        float flick = 0.8f + 0.2f * sin(t * 11) * sin(t * 6.1);
        halo(0, 0, 0, 0.3f, argb(s, 0.45f * flick * Math.min(1f, g)));
        rot(2, sin(t * 1.8) * 0.18f);
        push();
        translate(0, 0.1f, 0);
        cyl(LIT, argb(p, 1f), 0.003f, 0.05f, 4);
        pop();
        push();
        translate(0, 0.06f, 0);
        cyl2(LIT, argb(p, 1f), 0.05f, 0.012f, 0.03f, 8);
        pop();
        push();
        translate(0, -0.06f, 0);
        cyl(LIT, argb(p, 1f), 0.05f, 0.015f, 8);
        pop();
        for (int i = 0; i < 4; i++) {
            push();
            rot(1, i * (float) (Math.PI / 2) + 0.785f);
            translate(0.045f, 0, 0);
            cyl(LIT, argb(p, 1f), 0.004f, 0.1f, 4);
            pop();
        }
        cyl(LIT, argb(mix(s, 0xFFFFFF, 0.5f), 0.3f), 0.043f, 0.1f, 8);
        push();
        translate(0, -0.01f, 0);
        scale(flick, 1f + 0.2f * sin(t * 9), flick);
        cyl2(GLOW, argb(s, Math.min(1f, g)), 0.015f, 0f, 0.05f, 6);
        pop();
    }

    private void cicek(Anim a, int p, int s, float g) {
        float t = a.time;
        rot(0, -0.6f);
        push();
        rot(1, t * 0.8f);
        for (int i = 0; i < 8; i++) {
            push();
            rot(1, i * (float) (Math.PI / 4));
            rot(0, 0.25f + 0.08f * sin(t * 2 + i));
            translate(0, 0, 0.05f);
            push();
            scale(0.55f, 0.18f, 1f);
            sphere(LIT, argb(i % 2 == 0 ? p : mix(p, 0xFFFFFF, 0.25f), 1f), 0.045f, 3, 6);
            pop();
            pop();
        }
        pop();
        push();
        scale(1f, 0.5f, 1f);
        sphere(GLOW, argb(s, Math.min(1f, g)), 0.03f, 4, 8);
        pop();
        push();
        translate(0, -0.06f, 0);
        cyl(LIT, argb(0x3A9A3A, 1f), 0.005f, 0.11f, 4);
        translate(0.02f, -0.02f, 0);
        rot(2, 0.6f + 0.15f * sin(t * 2));
        sheet4(LIT, argb(0x4AB04A, 1f), 0, 0, 0, 0.03f, 0.02f, 0.01f, 0.05f, 0, 0, 0.03f, -0.01f, -0.01f);
        pop();
    }

    private void ejderYumurtasi(Anim a, int p, int s, float g) {
        float t = a.time;
        float beat = 0.5f + 0.5f * sin(t * 2.4);
        halo(0, 0, 0, 0.26f + 0.06f * beat, argb(s, (0.25f + 0.25f * beat) * Math.min(1f, g)));
        rot(1, t * 0.5f);
        rot(2, sin(t * 3) * 0.05f);
        push();
        scale(0.82f, 1.1f, 0.82f);
        sphere(LIT, argb(p, 1f), 0.085f, 7, 12);
        pop();
        // isik sizan catlaklar
        for (int i = 0; i < 4; i++) {
            push();
            rot(1, i * 1.57f + 0.4f);
            rot(0, -0.3f + i * 0.2f);
            translate(0, 0.01f, 0.071f);
            rot(2, 0.5f * (i % 2 == 0 ? 1 : -1));
            box(GLOW, argb(s, (0.5f + 0.5f * beat) * Math.min(1f, g)), 0.002f, 0.03f, 0.002f);
            translate(0.008f, 0.03f, 0);
            rot(2, -1.0f);
            box(GLOW, argb(s, (0.5f + 0.5f * beat) * Math.min(1f, g)), 0.002f, 0.015f, 0.002f);
            pop();
        }
        for (int i = 0; i < 3; i++) {
            push();
            rot(1, t * 1.6f + i * 2.09f);
            translate(0.13f, sin(t * 2 + i) * 0.03f, 0);
            sphere(GLOW, argb(s, 0.85f * Math.min(1f, g)), 0.008f, 2, 4);
            pop();
        }
    }
}
