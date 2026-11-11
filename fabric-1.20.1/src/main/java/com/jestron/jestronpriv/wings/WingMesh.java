package com.jestron.jestronpriv.wings;

/**
 * Kanat geometrisi (Minecraft'tan bagimsiz). Govde uzayinda, blok biriminde dortgenler uretir:
 * x = yanlar, y = asagi, z = sirt (arka). Her kanat bir "kanat duzleminde" kurulur
 * (s = disari, t = asagi), sonra cirpma acisiyla 3B'ye yerlestirilir ve uclar cirpma yonunun
 * tersine esner (gecikmeli uc hareketi). Ayni kare icinde hic nesne olusturmaz.
 */
public final class WingMesh {

    /** Dortgen alicisi. layer: {@link #BASE} veya {@link #GLOW}. */
    public interface Sink {
        void quad(int layer, float[] xyz, float[] uv, int[] argb, float nx, float ny, float nz);
    }

    public static final int BASE = 0, GLOW = 1;
    /** Parcacik cikis noktasi sayisi (her kanat icin 2). */
    public static final int MAX_TIPS = 4;

    private static final float D2R = (float) (Math.PI / 180);
    private static final float TAU = (float) (Math.PI * 2);

    // atlas sutunlari
    private static final float COL_W = 0.25f;
    private static final float GLOW_U0 = 0.75f, GLOW_U1 = 0.875f;
    private static final float SCROLL_U0 = 0.875f, SCROLL_U1 = 1f;

    /** Parcacik uclari (govde uzayi): x,y,z * MAX_TIPS. */
    public final float[] tips = new float[MAX_TIPS * 3];

    /** HOLY: tuy esyasi basina 8 deger {merkez x,y,z, donus x,y,z,w, olcek}. "Kutsal uzay": x yan, y yukari, z ileri;
     *  cizerken govde uzayina X ekseninde 180 derece donulur. */
    public static final int MAX_ITEMS = 20;
    public final float[] items = new float[MAX_ITEMS * 8];
    public int itemCount;
    public int tipCount;
    /** Son build'de uretilen dortgen sayisi. */
    public int quadCount;

    private final float[] q = new float[12], uv = new float[8];
    private final int[] col = new int[4];
    private Sink sink;
    private WingStyle st;
    private WingPose pose;
    private float S, side;
    // kanat duzlemi tabani
    private float ax, ay, az, ox, oy, oz, dx, dy, dz, nx, ny, nz;
    private float bendTip, rRef;
    /**
     * Gercek kanat kivrimi (melek kanadi gibi kemerli): kanat tek duz duzlem degil.
     * curlK: disa dogru geriye kivrim (ustten bakinca C). camberK: alt / arka kenar kubbesi.
     * arcK: dis kisim yukari dogru kemerlenir (dihedral) -> melek kanadinin gorkemli yukari kavisi.
     * Katlandikca hepsi azalir (sirta yaslanir).
     */
    private float curlK, camberK, arcK, curFold;
    // kemik zinciri (duzlem icinde)
    private final float[] cs = new float[4], ct = new float[4];
    // zar icin referans (acik) zincir
    private final float[] rs = new float[4], rt = new float[4];
    // gecici
    private final float[] pS = new float[6], pT = new float[6], pW = new float[6];

    public void build(WingStyle style, WingPose p, float scale, Sink out) {
        this.st = style;
        this.pose = p;
        this.sink = out;
        this.S = Math.max(0.3f, Math.min(2f, scale)) * style.size() * 1.1f;
        quadCount = 0;
        tipCount = 0;
        itemCount = 0;
        switch (style.kind()) {
            case CRYSTAL -> {
                curlK = 0.18f;
                camberK = 0.07f;
                arcK = 0.26f;
            }
            case MEMBRANE -> {
                curlK = 0.20f;
                camberK = 0.11f;
                arcK = 0.22f;
            }
            case BUTTERFLY -> {
                curlK = 0.10f;
                camberK = 0.04f;
                arcK = 0.12f;
            }
            default -> {
                curlK = 0.22f;
                camberK = 0.11f;
                arcK = 0.34f;
            }
        }
        curFold = Math.max(0f, Math.min(1f, p.fold));
        for (int sd = -1; sd <= 1; sd += 2) {
            side = sd;
            switch (style.kind()) {
                case MEMBRANE -> membrane();
                case BUTTERFLY -> butterfly();
                case HOLY -> holy();
                default -> feathers();
            }
        }
    }

    // ------------------------------------------------------------------ ortak

    /** Ek kanat ciftleri icin faz / acilar (ana cift icin 0). */
    private float phaseOff, raiseOff, attachZ = 0.135f;

    private float flapSin() {
        float ph = pose.phase + phaseOff;
        return (float) Math.sin(ph + 0.35f * Math.sin(ph)); // asagi vurus hizli, yukari yavas
    }

    private float flapCos() {
        return (float) Math.cos(pose.phase + phaseOff);
    }

    /** Kanat duzlemini cirpma acisina gore kurar. */
    private void setupPlane(float yawDeg, float attachX, float attachY) {
        float y = yawDeg * D2R;
        float c = (float) Math.cos(y), s = (float) Math.sin(y);
        ox = side * c;
        oy = 0;
        oz = s;
        // asagi ekseni hafif geriye yatik: alt kisimlar sirttan uzaklasir (bacaklara girmez)
        float l = (float) Math.sqrt(1 + 0.14f * 0.14f);
        dx = 0;
        dy = 1 / l;
        dz = 0.14f / l;
        // normal = side * (o x d) -> her iki kanatta da geriye bakar
        float cx = oy * dz - oz * dy, cy = oz * dx - ox * dz, cz = ox * dy - oy * dx;
        float cl = (float) Math.sqrt(cx * cx + cy * cy + cz * cz);
        nx = side * cx / cl;
        ny = side * cy / cl;
        nz = side * cz / cl;
        ax = side * attachX * S;
        ay = attachY * S;
        az = attachZ;
    }

    /** Duzlem koordinatini (s, t, w) 3B'ye cevirir. Uc esnemesi ve kanat kivrimi burada eklenir. */
    private void map(float s, float t, float w, float[] o, int i) {
        float r = (float) Math.sqrt(s * s + t * t) / rRef;
        float ww = w + bendTip * r * r;
        float open = 1f - 0.65f * curFold;
        float lift = 0f;
        if (open > 0f && (curlK != 0f || camberK != 0f || arcK != 0f)) {
            // disa dogru geriye kivrim (r^2: kokte duz, ucta en cok) + alt kenar kubbesi (t > 0 yumusak kare)
            float tn = t / S;
            float cam = tn > 0f ? tn * tn : 0f;
            float so = s > 0f ? s / S : 0f;
            // kivrim miktari buyuk kanatlarda sinirlanir (cok buyuyunce oyuncuyu asmasin)
            float cb = Math.min(S, 1.3f);
            ww += cb * open * (curlK * r * r + camberK * cam);
            // dis kisim yukari dogru kemerlenir: melek kanadinin yukari kavisi (y asagi oldugundan eksi)
            lift = cb * open * arcK * so * so;
        }
        o[i] = ax + ox * s + dx * t + nx * ww;
        o[i + 1] = ay + oy * s + dy * t + ny * ww - lift;
        o[i + 2] = az + oz * s + dz * t + nz * ww;
    }

    private void emit(int layer) {
        // normal: kosegenlerin capraz carpimi (ucgenlerde de calisir)
        float ex = q[6] - q[0], ey = q[7] - q[1], ez = q[8] - q[2];
        float fx = q[9] - q[3], fy = q[10] - q[4], fz = q[11] - q[5];
        float cx = ey * fz - ez * fy, cy = ez * fx - ex * fz, cz = ex * fy - ey * fx;
        float l = (float) Math.sqrt(cx * cx + cy * cy + cz * cz);
        if (l < 1e-9f) {
            cx = nx;
            cy = ny;
            cz = nz;
        } else {
            // sol kanat sagin aynasidir (x = -x): ayna cizim yonunu tersine cevirir, capraz carpim
            // da ters cikar. Duzeltilmezse bir kanat isigi tersten alir (kararir).
            float k = side < 0 ? -1f / l : 1f / l;
            cx *= k;
            cy *= k;
            cz *= k;
        }
        for (int i = 0; i < 12; i++) if (!Float.isFinite(q[i])) return;
        sink.quad(layer, q, uv, col, cx, cy, cz);
        quadCount++;
    }

    private void addTip(float s, float t) {
        if (tipCount >= MAX_TIPS) return;
        map(s, t, 0, tips, tipCount * 3);
        tipCount++;
    }

    private static int rgb(float r, float g, float b, float a) {
        int R = (int) (Math.max(0, Math.min(1, r)) * 255f + 0.5f);
        int G = (int) (Math.max(0, Math.min(1, g)) * 255f + 0.5f);
        int B = (int) (Math.max(0, Math.min(1, b)) * 255f + 0.5f);
        int A = (int) (Math.max(0, Math.min(1, a)) * 255f + 0.5f);
        return (A << 24) | (R << 16) | (G << 8) | B;
    }

    private static float ch(int c, int sh) {
        return ((c >> sh) & 255) / 255f;
    }

    /** Kanat konumuna (0 ic .. 1 dis) gore renk carpani; b = parlaklik. */
    private int tintColor(float wp, float b, float a) {
        float r, g, bl;
        if (st.rainbow()) {
            float h = wp * 0.85f + pose.time * 0.12f;
            h -= (float) Math.floor(h);
            float[] c = hsv(h);
            r = 0.35f + 0.65f * c[0];
            g = 0.35f + 0.65f * c[1];
            bl = 0.35f + 0.65f * c[2];
        } else {
            int[] t = st.tint();
            int c0, c1;
            float k;
            if (wp < 0.5f) {
                c0 = t[0];
                c1 = t[1];
                k = wp * 2;
            } else {
                c0 = t[1];
                c1 = t[2];
                k = (wp - 0.5f) * 2;
            }
            r = ch(c0, 16) + (ch(c1, 16) - ch(c0, 16)) * k;
            g = ch(c0, 8) + (ch(c1, 8) - ch(c0, 8)) * k;
            bl = ch(c0, 0) + (ch(c1, 0) - ch(c0, 0)) * k;
        }
        return rgb(r * b, g * b, bl * b, a);
    }

    private final float[] hsvOut = new float[3];

    private float[] hsv(float h) {
        float r = Math.abs(h * 6 - 3) - 1, g = 2 - Math.abs(h * 6 - 2), b = 2 - Math.abs(h * 6 - 4);
        hsvOut[0] = Math.max(0, Math.min(1, r));
        hsvOut[1] = Math.max(0, Math.min(1, g));
        hsvOut[2] = Math.max(0, Math.min(1, b));
        return hsvOut;
    }

    /** Isilti siddeti: nabiz, titreme, hareket enerjisi. */
    private float glowLevel(float seed) {
        float g = st.glow() * (0.75f + 0.4f * pose.energy);
        if (st.pulse() > 0) g *= 0.8f + 0.3f * (float) Math.sin(pose.time * st.pulse() * TAU + seed);
        if (st.flicker() > 0) {
            float h = hash((int) Math.floor(pose.time * 13f) * 31 + (int) seed * 7);
            float fl = st.flicker();
            if (fl > 0.6f) g *= h > 0.82f ? 1.5f : 0.35f + 0.25f * h;   // simsek: arada bir cakar
            else g *= 1f - fl * 0.6f + fl * 0.6f * h;
        }
        return g;
    }

    private static float hash(int x) {
        x = (x ^ 61) ^ (x >>> 16);
        x *= 9;
        x ^= x >>> 4;
        x *= 0x27d4eb2d;
        x ^= x >>> 15;
        return (x & 0xFFFFFF) / (float) 0x1000000;
    }

    private static float lerp(float a, float b, float k) {
        return a + (b - a) * k;
    }

    private static float ease(float k) {
        k = Math.max(0, Math.min(1, k));
        return k * k * (3 - 2 * k);
    }

    /** Zincir parametresi c (0..3) icin noktayi pS/pT[slot]'a yazar. */
    private void chainPoint(float c, float[] s, float[] t, int slot) {
        int i = Math.max(0, Math.min(2, (int) Math.floor(c)));
        float k = Math.max(0, Math.min(1, c - i));
        s[slot] = cs[i] + (cs[i + 1] - cs[i]) * k;
        t[slot] = ct[i] + (ct[i + 1] - ct[i]) * k;
    }

    // ------------------------------------------------------------------ tuy dizilimi (FEATHER / CRYSTAL / ENERGY)

    /** grup: sayi, c0, c1, boy0, boy1, en, acikAci0, acikAci1, katliAci0, katliAci1, katman w, sutun, egrilik, isilti(1/0) */
    private static final float[][] FEATHER_GROUPS = {
            {10, 2.05f, 3.00f, 0.80f, 0.92f, 0.155f, 76, 6, 93, 86, 0.000f, 0, -10, 1},   // birincil
            {10, 1.00f, 2.00f, 0.62f, 0.70f, 0.160f, 100, 80, 95, 90, 0.004f, 1, -6, 1},  // ikincil
            {4, 0.25f, 0.95f, 0.50f, 0.58f, 0.160f, 112, 102, 100, 97, 0.008f, 1, -4, 1}, // ucuncul
            {14, 0.90f, 2.95f, 0.31f, 0.26f, 0.140f, 102, 40, 96, 88, 0.014f, 2, -4, 0}, // buyuk ortu
            {12, 0.40f, 2.60f, 0.19f, 0.17f, 0.120f, 105, 55, 98, 90, 0.021f, 2, -3, 0},  // orta ortu
            {9, 0.05f, 2.90f, 0.15f, 0.13f, 0.085f, 120, 60, 100, 95, 0.028f, 2, -2, 0},  // kenar
    };
    private static final float[][] CRYSTAL_GROUPS = {
            {7, 2.05f, 3.00f, 0.86f, 0.98f, 0.17f, 74, 8, 94, 85, 0.000f, 0, -4, 1},
            {6, 1.00f, 2.00f, 0.66f, 0.74f, 0.17f, 100, 80, 97, 92, 0.010f, 1, -3, 1},
            {2, 0.40f, 0.95f, 0.54f, 0.58f, 0.17f, 110, 102, 100, 97, 0.020f, 1, -2, 1},
            {7, 0.60f, 2.90f, 0.36f, 0.30f, 0.15f, 104, 45, 96, 88, 0.032f, 2, -2, 1},
    };

    /** Ek ciftler: olcek, baglanti x/y, kalkis acisi, faz, supurme farki. Ilk satir ana cift. */
    private static final float[][] PAIRS = {
            {1f, 0.10f, 0.10f, 0f, 0f, 0f},
            {0.62f, 0.07f, -0.03f, 38f, 0.7f, -4f},   // ust cift (yukari bakar)
            {0.55f, 0.08f, 0.33f, -34f, -0.7f, 6f},   // alt cift (asagi bakar)
    };

    private void feathers() {
        int pairs = Math.max(1, Math.min(PAIRS.length, st.pairs()));
        float baseS = S;
        try {
            // arkadaki (ek) ciftler once, ana cift en son (en onde) cizilir
            for (int pi = pairs - 1; pi >= 0; pi--) {
                float[] pc = PAIRS[pi];
                S = baseS * pc[0];
                raiseOff = pc[3];
                phaseOff = pc[4];
                attachZ = pi == 0 ? 0.135f : 0.128f;
                featherPair(pc[1], pc[2] * (baseS / S), pc[5], pi == 0);
            }
        } finally {
            S = baseS;
            raiseOff = 0;
            phaseOff = 0;
            attachZ = 0.135f;
        }
    }

    private void featherPair(float attachX, float attachY, float yawOff, boolean main) {
        boolean crystal = st.kind() == WingKind.CRYSTAL, energy = st.kind() == WingKind.ENERGY;
        float fs = flapSin(), fc = flapCos();
        float ampN = pose.amp / 45f;
        float f = ease(Math.max(0, Math.min(1, pose.fold + 0.28f * ampN * Math.max(0, fc))));
        setupPlane(pose.yaw + yawOff + pose.amp * fs, attachX, attachY);
        rRef = 1.45f * S;
        bendTip = -(crystal ? 0.15f : 0.38f) * (pose.amp * D2R) * fc * S;

        // katli: dirsek asagida, bilek omuz ustunde (kanadin tepesi), el asagi -> tuyler dizlere iner
        float a1 = lerp(-30, 46, f) - pose.raise - raiseOff, a2 = lerp(-14, -106, f) - raiseOff * 0.5f, a3 = lerp(-4, 80, f) - raiseOff * 0.3f;
        float L1 = 0.30f * S, L2 = 0.40f * S, L3 = 0.38f * S;
        cs[0] = 0;
        ct[0] = 0;
        cs[1] = cs[0] + L1 * (float) Math.cos(a1 * D2R);
        ct[1] = ct[0] + L1 * (float) Math.sin(a1 * D2R);
        cs[2] = cs[1] + L2 * (float) Math.cos(a2 * D2R);
        ct[2] = ct[1] + L2 * (float) Math.sin(a2 * D2R);
        cs[3] = cs[2] + L3 * (float) Math.cos(a3 * D2R);
        ct[3] = ct[2] + L3 * (float) Math.sin(a3 * D2R);

        float[][] groups = crystal ? CRYSTAL_GROUPS : FEATHER_GROUPS;
        // buyuk boyutta katli tuyler yere girmesin: katlandikca boy kisalir (acikken tam boy)
        float kLen = lerp(1f, Math.min(1f, (float) Math.pow(S / 1.1f, -1.5)), f);
        int idx = 0;
        // arkadaki (alttaki) gruplar once cizilir
        for (float[] g : groups) {
            int n = (int) g[0];
            for (int i = 0; i < n; i++, idx++) {
                float k = n == 1 ? 0 : i / (float) (n - 1);
                float c = lerp(g[1], g[2], k);
                chainPoint(c, pS, pT, 0);
                float bulge = (float) Math.sin(k * Math.PI * 0.8);
                float len = lerp(g[3], g[4], k) * (g == groups[0] ? 0.92f + 0.12f * bulge : 1f) * S * kLen;
                float w = g[5] * S;
                float ang = lerp(lerp(g[6], g[7], k), lerp(g[8], g[9], k), f) * D2R;
                float curve = g[12] * D2R;
                float wOff = g[10] * S + (crystal ? 0.012f * S * (float) Math.sin(pose.time * 1.7f + idx) : 0f);
                // kristaller kemikten ayri suzulur
                float gap = crystal ? 0.035f * S : 0f;
                float rs0 = pS[0] + gap * (float) Math.cos(ang), rt0 = pT[0] + gap * (float) Math.sin(ang);
                // ince ruzgar dalgalanmasi
                float ripple = (crystal ? 0.002f : 0.0045f) * S * (float) Math.sin(pose.time * 2.6f + idx * 1.37f + side * 0.8f);
                float wp = Math.min(1f, c / 3f);
                int column = (int) g[11];
                feather(rs0, rt0, ang, curve, len, w, wOff, ripple, wp, column, g[13] > 0, energy, idx);
                if (main && g == groups[0] && (i == n - 1 || i == n - 4)) addTip(tipS, tipT);
            }
        }
    }

    private float tipS, tipT;

    /** Iki parcali (kok-orta-uc) tuy. Uc noktasi tipS/tipT'ye yazilir. */
    private void feather(float r0s, float r0t, float ang, float curve, float len, float w, float wOff, float ripple,
                         float wp, int column, boolean glowable, boolean energy, int idx) {
        float a0 = ang, am = ang + curve * 0.5f, a1 = ang + curve;
        float ms = r0s + (float) Math.cos(a0) * len * 0.5f, mt = r0t + (float) Math.sin(a0) * len * 0.5f;
        float es = ms + (float) Math.cos(a1) * len * 0.5f, et = mt + (float) Math.sin(a1) * len * 0.5f;
        float hw = w * 0.5f;
        // dik yonler (-p = dis kenar, sprite'in dar tarafi)
        float p0s = -(float) Math.sin(a0) * hw, p0t = (float) Math.cos(a0) * hw;
        float pms = -(float) Math.sin(am) * hw, pmt = (float) Math.cos(am) * hw;
        float p1s = -(float) Math.sin(a1) * hw, p1t = (float) Math.cos(a1) * hw;
        float wm = wOff + ripple * 0.5f, we = wOff + ripple;
        tipS = es;
        tipT = et;

        float u0 = column * COL_W, u1 = u0 + COL_W;
        float alpha = st.translucent() ? st.alpha() : 1f;
        float br0 = 0.70f, brm = 0.88f, br1 = 1.0f;
        if (column == 2) {
            br0 = 0.80f;
            brm = 0.92f;
        }
        int cr = tintColor(wp, br0, alpha), cm = tintColor(Math.min(1, wp + 0.08f), brm, alpha),
                ce = tintColor(Math.min(1, wp + 0.16f), br1, alpha);

        // kok -> orta
        map(r0s - p0s, r0t - p0t, wOff, q, 0);
        map(r0s + p0s, r0t + p0t, wOff, q, 3);
        map(ms + pms, mt + pmt, wm, q, 6);
        map(ms - pms, mt - pmt, wm, q, 9);
        setUv(u0, 0, u1, 0, u1, 0.5f, u0, 0.5f);
        setCol(cr, cr, cm, cm);
        emit(BASE);
        if (glowable && st.glow() > 0) glowQuad(GLOW_U0, GLOW_U1, 0, 0.5f, 0.55f, 0.85f, idx);
        // orta -> uc
        map(ms - pms, mt - pmt, wm, q, 0);
        map(ms + pms, mt + pmt, wm, q, 3);
        map(es + p1s, et + p1t, we, q, 6);
        map(es - p1s, et - p1t, we, q, 9);
        setUv(u0, 0.5f, u1, 0.5f, u1, 1f, u0, 1f);
        setCol(cm, cm, ce, ce);
        emit(BASE);
        if (glowable && st.glow() > 0) glowQuad(GLOW_U0, GLOW_U1, 0.5f, 1f, 0.85f, 1f, idx);

        if (energy && glowable && st.scroll() != 0) {
            // tuyun ortasinda akan desen: dar seritler, kok ve uc siyaha soner
            float nw = 0.55f;
            scrollStrip(r0s, r0t, ms, mt, es, et, p0s * nw, p0t * nw, pms * nw, pmt * nw, p1s * nw, p1t * nw,
                    wOff + 0.003f * S, wm + 0.003f * S, we + 0.003f * S, len, w, idx);
        }
        if (st.id().equals("su") && glowable) {
            float nw = 0.6f;
            scrollStrip(r0s, r0t, ms, mt, es, et, p0s * nw, p0t * nw, pms * nw, pmt * nw, p1s * nw, p1t * nw,
                    wOff + 0.002f * S, wm + 0.002f * S, we + 0.002f * S, len, w, idx);
        }
    }

    /** Son yazilan konumlar uzerine isilti dortgeni (ayni geometri, isilti sutunu). */
    private void glowQuad(float u0, float u1, float v0, float v1, float b0, float b1, int idx) {
        float g = glowLevel(idx * 0.9f + side);
        float gr = ch(st.glowColor(), 16) * g, gg = ch(st.glowColor(), 8) * g, gb = ch(st.glowColor(), 0) * g;
        int c0 = rgb(gr * b0, gg * b0, gb * b0, 1), c1 = rgb(gr * b1, gg * b1, gb * b1, 1);
        setUv(u0, v0, u1, v0, u1, v1, u0, v1);
        setCol(c0, c0, c1, c1);
        emit(GLOW);
    }

    private void scrollStrip(float r0s, float r0t, float ms, float mt, float es, float et,
                             float p0s, float p0t, float pms, float pmt, float p1s, float p1t,
                             float w0, float wm, float we, float len, float w, int idx) {
        // v: tuy boyunca dokuyu (en/boy oranina gore) tekrar eder, zamanla kayar
        float span = len / (w * 4f) * 0.5f;
        float off = pose.time * st.scroll() + idx * 0.173f;
        off -= (float) Math.floor(off);
        float g = 0.8f + 0.4f * pose.energy;
        // 4 parca: kok(0) -> orta(1) -> uc(2); parlaklik 0 -> 1 -> 1 -> 0
        float[] fs = {0f, 0.35f, 0.7f, 1f};
        float[] br = {0f, 1f, 1f, 0f};
        for (int k = 0; k < 3; k++) {
            float fa = fs[k], fb = fs[k + 1];
            stripPoint(fa, r0s, r0t, ms, mt, es, et, p0s, p0t, pms, pmt, p1s, p1t, w0, wm, we, 0);
            stripPoint(fb, r0s, r0t, ms, mt, es, et, p0s, p0t, pms, pmt, p1s, p1t, w0, wm, we, 1);
            float va = off + fa * span, vb = off + fb * span;
            float ba = br[k] * g, bb = br[k + 1] * g;
            // dokuyu sarmadan: tam sayi sinirinda dortgeni bol
            float cut = (float) Math.floor(va) + 1f;
            if (vb > cut) {
                float m = (cut - va) / (vb - va);
                float bm = ba + (bb - ba) * m;
                // [fa..fm]
                stripPoint(fa + (fb - fa) * m, r0s, r0t, ms, mt, es, et, p0s, p0t, pms, pmt, p1s, p1t, w0, wm, we, 2);
                stripQuad(0, 2, va - (float) Math.floor(va), 1f, ba, bm);
                stripQuad(2, 1, 0f, vb - cut, bm, bb);
            } else {
                float base = (float) Math.floor(va);
                stripQuad(0, 1, va - base, vb - base, ba, bb);
            }
        }
    }

    private void stripPoint(float f, float r0s, float r0t, float ms, float mt, float es, float et,
                            float p0s, float p0t, float pms, float pmt, float p1s, float p1t,
                            float w0, float wm, float we, int slot) {
        float s, t, ps, pt, w;
        if (f <= 0.5f) {
            float k = f * 2;
            s = lerp(r0s, ms, k);
            t = lerp(r0t, mt, k);
            ps = lerp(p0s, pms, k);
            pt = lerp(p0t, pmt, k);
            w = lerp(w0, wm, k);
        } else {
            float k = (f - 0.5f) * 2;
            s = lerp(ms, es, k);
            t = lerp(mt, et, k);
            ps = lerp(pms, p1s, k);
            pt = lerp(pmt, p1t, k);
            w = lerp(wm, we, k);
        }
        pS[slot * 2] = s - ps;
        pT[slot * 2] = t - pt;
        pS[slot * 2 + 1] = s + ps;
        pT[slot * 2 + 1] = t + pt;
        pW[slot] = w;
    }

    private void stripQuad(int a, int b, float va, float vb, float ba, float bb) {
        map(pS[a * 2], pT[a * 2], pW[a], q, 0);
        map(pS[a * 2 + 1], pT[a * 2 + 1], pW[a], q, 3);
        map(pS[b * 2 + 1], pT[b * 2 + 1], pW[b], q, 6);
        map(pS[b * 2], pT[b * 2], pW[b], q, 9);
        setUv(SCROLL_U0, va, SCROLL_U1, va, SCROLL_U1, vb, SCROLL_U0, vb);
        int ca = rgb(ba, ba, ba, 1), cb = rgb(bb, bb, bb, 1);
        setCol(ca, ca, cb, cb);
        emit(GLOW);
    }

    private void setUv(float a, float b, float c, float d, float e, float f, float g, float h) {
        uv[0] = a;
        uv[1] = b;
        uv[2] = c;
        uv[3] = d;
        uv[4] = e;
        uv[5] = f;
        uv[6] = g;
        uv[7] = h;
    }

    private void setCol(int a, int b, int c, int d) {
        col[0] = a;
        col[1] = b;
        col[2] = c;
        col[3] = d;
    }

    // ------------------------------------------------------------------ zar (MEMBRANE)

    private static final float[] FINGER_LEN = {0.62f, 0.82f, 0.74f, 0.58f};
    private static final float[] FINGER_OPEN = {-14, 14, 40, 68};
    private static final float[] FINGER_FOLD = {76, 86, 96, 106};
    private final float[] fS = new float[4], fT = new float[4], fRS = new float[4], fRT = new float[4];
    private float bS, bT, bRS, bRT, refS0, refS1, refT0, refT1;

    private void membraneChain(float f, float raise, float[] s, float[] t, float[] fs, float[] ft, boolean ref) {
        float a1 = lerp(-35, 46, f) - raise, a2 = lerp(-10, -106, f);
        float L1 = 0.30f * S, L2 = 0.42f * S;
        s[0] = 0;
        t[0] = 0;
        s[1] = L1 * (float) Math.cos(a1 * D2R);
        t[1] = L1 * (float) Math.sin(a1 * D2R);
        s[2] = s[1] + L2 * (float) Math.cos(a2 * D2R);
        t[2] = t[1] + L2 * (float) Math.sin(a2 * D2R);
        float shrink = 1f - 0.25f * f;
        for (int i = 0; i < 4; i++) {
            float a = lerp(FINGER_OPEN[i], FINGER_FOLD[i], f) * D2R;
            float L = FINGER_LEN[i] * S * shrink;
            fs[i] = s[2] + L * (float) Math.cos(a);
            ft[i] = t[2] + L * (float) Math.sin(a);
        }
        if (ref) {
            bRS = 0.03f * S;
            bRT = 0.62f * S;
        } else {
            bS = 0.03f * S;
            bT = 0.62f * S;
        }
    }

    private void membrane() {
        float fs = flapSin(), fc = flapCos();
        float ampN = pose.amp / 45f;
        float f = ease(Math.max(0, Math.min(1, pose.fold + 0.28f * ampN * Math.max(0, fc))));
        setupPlane(pose.yaw + pose.amp * fs, 0.09f, 0.10f);
        rRef = 1.5f * S;
        bendTip = -0.30f * (pose.amp * D2R) * fc * S;
        membraneChain(f, pose.raise, cs, ct, fS, fT, false);
        membraneChain(0, 0, rs, rt, fRS, fRT, true);
        // referans sinirlar (UV icin)
        refS0 = -0.05f * S;
        refS1 = 0;
        refT0 = 0;
        refT1 = bRT;
        for (int i = 0; i < 4; i++) {
            refS1 = Math.max(refS1, fRS[i]);
            refT0 = Math.min(refT0, fRT[i]);
            refT1 = Math.max(refT1, fRT[i]);
        }
        refT0 = Math.min(refT0, rt[1]);
        refS1 += 0.02f * S;
        refT0 -= 0.02f * S;
        refT1 += 0.02f * S;

        float alpha = st.translucent() ? st.alpha() : 1f;
        // parmaklar arasi zar paneller (bilekten yelpaze)
        for (int i = 0; i < 3; i++) membranePanel(cs[2], ct[2], rs[2], rt[2], fS[i], fT[i], fRS[i], fRT[i], fS[i + 1], fT[i + 1], fRS[i + 1], fRT[i + 1], 0.20f, alpha, i);
        // son parmaktan bele: dirsekten yelpaze
        membraneTri(cs[1], ct[1], rs[1], rt[1], cs[2], ct[2], rs[2], rt[2], fS[3], fT[3], fRS[3], fRT[3], alpha, 3);
        membranePanel(cs[1], ct[1], rs[1], rt[1], fS[3], fT[3], fRS[3], fRT[3], bS, bT, bRS, bRT, 0.16f, alpha, 4);
        membraneTri(cs[0], ct[0], rs[0], rt[0], cs[1], ct[1], rs[1], rt[1], bS, bT, bRS, bRT, alpha, 5);
        // on zar (omuz-bilek arasi ince)
        membraneTri(cs[0], ct[0], rs[0], rt[0], cs[1], ct[1], rs[1], rt[1], cs[2], ct[2], rs[2], rt[2], alpha, 6);

        // kemikler
        bone(cs[0], ct[0], cs[1], ct[1], 0.055f, 0.045f);
        bone(cs[1], ct[1], cs[2], ct[2], 0.045f, 0.035f);
        for (int i = 0; i < 4; i++) bone(cs[2], ct[2], fS[i], fT[i], 0.030f, 0.010f);
        // bilek pencesi (basparmak): on kola gore yukari-disari
        float da = (float) Math.atan2(ct[2] - ct[1], cs[2] - cs[1]) - 0.9f;
        float cl = 0.11f * S;
        claw(cs[2], ct[2], cs[2] + (float) Math.cos(da) * cl, ct[2] + (float) Math.sin(da) * cl, 0.035f);
        addTip(fS[1], fT[1]);
        addTip(fS[3], fT[3]);
    }

    private float uOf(float s) {
        return (s - refS0) / (refS1 - refS0);
    }

    private float vOf(float t) {
        return (t - refT0) / (refT1 - refT0);
    }

    /** Merkez z'den X-Y kenarina sarkan zar: 5 ucgenli yelpaze. */
    private void membranePanel(float zs, float zt, float zrs, float zrt, float xs, float xt, float xrs, float xrt,
                               float ys, float yt, float yrs, float yrt, float sag, float alpha, int seed) {
        int seg = 5;
        float ps = xs, pt = xt, prs = xrs, prt = xrt;
        for (int k = 1; k <= seg; k++) {
            float u = k / (float) seg;
            float bow = sag * 4f * u * (1 - u);
            float es = lerp(xs, ys, u), et = lerp(xt, yt, u);
            es += (zs - es) * bow;
            et += (zt - et) * bow;
            float ers = lerp(xrs, yrs, u), ert = lerp(xrt, yrt, u);
            ers += (zrs - ers) * bow;
            ert += (zrt - ert) * bow;
            membraneTri(zs, zt, zrs, zrt, ps, pt, prs, prt, es, et, ers, ert, alpha, seed * 10 + k);
            ps = es;
            pt = et;
            prs = ers;
            prt = ert;
        }
    }

    private void membraneTri(float as, float at, float ars, float art, float bs, float bt, float brs, float brt,
                             float cs_, float ct_, float crs, float crt, float alpha, int seed) {
        map(as, at, 0, q, 0);
        map(bs, bt, 0, q, 3);
        map(cs_, ct_, 0, q, 6);
        map(cs_, ct_, 0, q, 9);
        float ua = uOf(ars) * 0.5f, va = vOf(art) * 0.75f, ub = uOf(brs) * 0.5f, vb = vOf(brt) * 0.75f,
                uc = uOf(crs) * 0.5f, vc = vOf(crt) * 0.75f;
        setUv(ua, va, ub, vb, uc, vc, uc, vc);
        int c0 = tintColor(0.2f, 0.82f, alpha), c1 = tintColor(0.8f, 1f, alpha);
        setCol(c0, c1, c1, c1);
        emit(BASE);
        if (st.glow() > 0) {
            float g = glowLevel(seed * 0.37f + side);
            int gc = rgb(ch(st.glowColor(), 16) * g, ch(st.glowColor(), 8) * g, ch(st.glowColor(), 0) * g, 1);
            setUv(ua + 0.5f, va, ub + 0.5f, vb, uc + 0.5f, vc, uc + 0.5f, vc);
            setCol(gc, gc, gc, gc);
            emit(GLOW);
        }
    }

    /** Kemik: duzlemde bir serit + ona dik bir serit (her acidan hacimli gorunur). */
    private void bone(float s0, float t0, float s1, float t1, float w0, float w1) {
        float ls = s1 - s0, lt = t1 - t0;
        float l = (float) Math.sqrt(ls * ls + lt * lt);
        if (l < 1e-5f) return;
        float ps = -lt / l, pt = ls / l;
        float h0 = w0 * 0.5f * S, h1 = w1 * 0.5f * S;
        int c = tintColor(0.5f, 1f, 1f);
        // duzlem serit (zarin hafif onunde)
        float wf = 0.006f * S;
        map(s0 - ps * h0, t0 - pt * h0, wf, q, 0);
        map(s0 + ps * h0, t0 + pt * h0, wf, q, 3);
        map(s1 + ps * h1, t1 + pt * h1, wf, q, 6);
        map(s1 - ps * h1, t1 - pt * h1, wf, q, 9);
        setUv(0, 0.75f, 0, 0.875f, 1, 0.875f, 1, 0.75f);
        setCol(c, c, c, c);
        emit(BASE);
        map(s0, t0, -h0, q, 0);
        map(s0, t0, h0, q, 3);
        map(s1, t1, h1, q, 6);
        map(s1, t1, -h1, q, 9);
        emit(BASE);
    }

    private void claw(float s0, float t0, float s1, float t1, float w) {
        float ls = s1 - s0, lt = t1 - t0;
        float l = (float) Math.sqrt(ls * ls + lt * lt);
        if (l < 1e-5f) return;
        float ps = -lt / l * w * 0.5f * S, pt = ls / l * w * 0.5f * S;
        int c = tintColor(0.5f, 1f, 1f);
        map(s0 - ps, t0 - pt, 0.008f * S, q, 0);
        map(s0 + ps, t0 + pt, 0.008f * S, q, 3);
        map(s1 + ps, t1 + pt, 0.008f * S, q, 6);
        map(s1 - ps, t1 - pt, 0.008f * S, q, 9);
        // pence sprite'i: taban solda (u=0), uc sagda; v 224..256
        setUv(0, 0.875f, 0, 1f, 0.25f, 1f, 0.25f, 0.875f);
        setCol(c, c, c, c);
        emit(BASE);
    }

    // ------------------------------------------------------------------ kelebek (BUTTERFLY)

    private void butterfly() {
        float fs = flapSin(), fc = flapCos();
        float f = ease(pose.fold);
        float amp = pose.amp * 1.6f + 5f;
        float yaw = soft(lerp(14, 66, f) + amp * fs, -6f, 88f);
        float yawH = soft(lerp(14, 66, f) + amp * flapSinAt(pose.phase - 0.35f), -6f, 88f);
        float alpha = st.translucent() ? st.alpha() : 1f;
        float bend = -0.22f * (amp * D2R) * fc * S;
        // on kanat: kutu 0.95 x 0.85, mentese (0.02, 0.03) sol alt
        setupPlane(yaw, 0.07f, 0.16f);
        rRef = 1.0f * S;
        bendTip = bend;
        wingSheet(0.95f * S, 0.85f * S, 0.02f * S, 0.03f * S, true, alpha);
        addTip(0.62f * S, -0.62f * S);
        // arka kanat: kutu 0.72 x 0.66, mentese (0.02, 0.12) sol ust
        setupPlane(yawH, 0.06f, 0.20f);
        rRef = 0.8f * S;
        bendTip = bend * 0.8f;
        wingSheet(0.72f * S, 0.66f * S, 0.02f * S, 0.12f * S, false, alpha);
        addTip(0.45f * S, 0.40f * S);
    }

    // ------------------------------------------------------------------ kutsal melek (oldurme efekti kanadi)

    /** {aci (derece, yataydan yukari), uzunluk, olcek} — AngelEffect ile ayni tuy dizilimi. */
    private static final float[][] HOLY_FEATHERS = {
            {78, 0.55f, 0.55f}, {62, 0.8f, 0.62f}, {46, 1.05f, 0.7f}, {30, 1.25f, 0.78f}, {15, 1.4f, 0.82f},
            {0, 1.45f, 0.82f}, {-14, 1.35f, 0.76f}, {-28, 1.15f, 0.68f}, {-42, 0.9f, 0.6f}, {-56, 0.65f, 0.52f}};

    private void holy() {
        float f = ease(pose.fold);
        // oldurme efektindeki kanadin ~0.8 olcegi (oyuncu sirtina gore), kok kurek kemiklerinde
        float K = 0.80f * S;
        float rx = side * 0.07f * S, ry = -0.16f * S, rz = -0.17f;
        for (int i = 0; i < HOLY_FEATHERS.length && itemCount < MAX_ITEMS; i++) {
            float[] ft = HOLY_FEATHERS[i];
            float k = i / (float) (HOLY_FEATHERS.length - 1);
            float angDeg = ft[0] + pose.raise * (1f - k);
            angDeg -= f * (angDeg + 70f) * 0.6f;                   // katlaninca tuyler asagi-geriye toplanir
            float ang = angDeg * D2R;
            float len = ft[1] * (1f - 0.3f * f);
            // cirpma: kok etrafinda geriye supurme; alttaki tuyler biraz daha geride (kivrimli yelpaze),
            // dalga tuyden tuye gecikir
            float phi = (pose.yaw + pose.amp * (float) Math.sin(pose.phase + phaseOff - k * 0.9f) + f * 55f) * D2R + k * 0.16f;
            phi = Math.min(phi, 95f * D2R);
            float cphi = (float) Math.cos(side * phi), sphi = (float) Math.sin(side * phi);
            float dx = side * (float) Math.cos(ang), dy = (float) Math.sin(ang);
            float half = len * 0.5f * K;
            // Ry(side*phi) * (dx, dy, 0)
            float cx = rx + dx * half * cphi, cy = ry + dy * half, cz = rz - dx * half * sphi;
            float spriteAng = side > 0 ? ang - (float) (Math.PI / 4) : (float) Math.PI - ang - (float) (Math.PI / 4);
            float hy = side * phi * 0.5f, hz = spriteAng * 0.5f;
            float sy = (float) Math.sin(hy), cy2 = (float) Math.cos(hy), sz = (float) Math.sin(hz), cz2 = (float) Math.cos(hz);
            int o = itemCount * 8;
            items[o] = cx;
            items[o + 1] = cy;
            items[o + 2] = cz;
            items[o + 3] = sy * sz;      // Ry * Rz
            items[o + 4] = sy * cz2;
            items[o + 5] = cy2 * sz;
            items[o + 6] = cy2 * cz2;
            items[o + 7] = ft[2] * 1.25f * K;
            itemCount++;
            if ((i == 0 || i == 5) && tipCount < MAX_TIPS) {
                float full = len * K;
                // kutsal uzaydan govde uzayina: (x, -y, -z)
                tips[tipCount * 3] = rx + dx * full * cphi;
                tips[tipCount * 3 + 1] = -(ry + dy * full);
                tips[tipCount * 3 + 2] = -(rz - dx * full * sphi);
                tipCount++;
            }
        }
    }

    /** Yumusak sinir: aralik disina tasmaz, sinirda ani durma (kirilma) olmaz. */
    private static float soft(float x, float lo, float hi) {
        float m = (lo + hi) * 0.5f, h = (hi - lo) * 0.5f;
        return m + h * (float) Math.tanh((x - m) / h);
    }

    private float flapSinAt(float ph) {
        return (float) Math.sin(ph + 0.35f * Math.sin(ph));
    }

    /** 3x3 izgarali kanat yapragi. fore: y yukari (v ters), degilse y asagi. */
    private void wingSheet(float W, float H, float hx, float hy, boolean fore, float alpha) {
        int n = 3;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                float xa = W * i / n, xb = W * (i + 1) / n, ya = H * j / n, yb = H * (j + 1) / n;
                // duzlem: s = x - hx, t = fore ? -(y - hy) : (y - hy)
                corner(xa, ya, W, H, hx, hy, fore, 0);
                corner(xb, ya, W, H, hx, hy, fore, 1);
                corner(xb, yb, W, H, hx, hy, fore, 2);
                corner(xa, yb, W, H, hx, hy, fore, 3);
                int cA = tintColor(0.3f, 0.8f + 0.2f * (i / (float) n), alpha);
                int cB = tintColor(0.7f, 0.8f + 0.2f * ((i + 1) / (float) n), alpha);
                setCol(cA, cB, cB, cA);
                emit(BASE);
                if (st.glow() > 0) {
                    float g = glowLevel(i + j * 3 + side) * (0.6f + 0.4f * (float) Math.sin(pose.time * 2.2f + i * 0.7f + j));
                    int gc = rgb(ch(st.glowColor(), 16) * g, ch(st.glowColor(), 8) * g, ch(st.glowColor(), 0) * g, 1);
                    setCol(gc, gc, gc, gc);
                    emit(GLOW);
                }
            }
        }
    }

    private void corner(float x, float y, float W, float H, float hx, float hy, boolean fore, int k) {
        float s = x - hx, t = fore ? -(y - hy) : (y - hy);
        float flex = 0.010f * S * (float) Math.sin(pose.time * 3f + x * 6f + side);
        map(s, t, flex, q, k * 3);
        float u = (fore ? 0f : 0.5f) + (x / W) * 0.5f;
        float v = fore ? 1f - y / H : y / H;
        uv[k * 2] = u;
        uv[k * 2 + 1] = v;
    }
}
