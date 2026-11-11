package com.jestron.jestronpriv.hats;

/**
 * Sapka geometrisi (MC'den bagimsiz, nesne uretmez). "Sapka uzayi": y yukari, kok = kafanin ust ortasi, on = -z,
 * birim = blok. Ilkel sekiller (kutu, kesik koni, disk, simit, kure, tup, yildiz) bir donusum yiginina gore
 * dortgen olarak {@link Sink}'e yazilir. Cikis koordinati model uzayidir (y asagi): (x, -y, z).
 */
public final class HatMesh {

    /** Isikli (opak / kesik) katman. */
    public static final int BASE = 0;
    /** Parlayan katman (karanlikta isildar). */
    public static final int GLOW = 1;
    /** Yari saydam katman (cam, kristal, hale). */
    public static final int TRANS = 2;

    /** Doku bolgeleri (hat.png 64x64, 4x4 hucre). */
    public static final int R_SOLID = 0, R_FABRIC = 1, R_SOFT = 2, R_STRIPE = 3, R_FUR = 4, R_METAL = 5, R_KNIT = 6, R_STAR = 7,
            R_SCALE = 8, R_STRAW = 9, R_DOTS = 10, R_GEM = 11;

    public static final int MAX_EMIT = 16;
    public static final int MAX_QUADS = 2400;

    public interface Sink {
        void quad(int layer, float[] xyz, float[] uv, int[] argb, float nx, float ny, float nz);
    }

    // 3x4 donusum yigini (satir dizilimi: m00 m01 m02 m03 / m10 .. / m20 ..)
    private final float[] st = new float[12 * 24];
    private int sp;
    private final float[] xyz = new float[12], uv = new float[8], tmp = new float[12];
    private final int[] col = new int[4];
    private Sink sink;
    private int layer = BASE, region = R_SOLID, color = 0xFFFFFFFF;
    private boolean glowAllowed = true;

    /** Uretilen dortgen sayisi ve sapka uzayindaki sinirlar. */
    public int quads;
    public float minX, minY, minZ, maxX, maxY, maxZ;
    /** Parcacik cikis noktalari (sapka uzayi). */
    public final float[] emit = new float[MAX_EMIT * 3];
    public int emitCount;

    public void begin(Sink s, boolean glow) {
        sink = s;
        glowAllowed = glow;
        sp = 0;
        identity(0);
        layer = BASE;
        region = R_SOLID;
        color = 0xFFFFFFFF;
        quads = 0;
        emitCount = 0;
        minX = minY = minZ = Float.MAX_VALUE;
        maxX = maxY = maxZ = -Float.MAX_VALUE;
    }

    /** Sinir bilgisi gecerli mi (en az bir dortgen). */
    public boolean hasBounds() {
        return quads > 0;
    }

    // ------------------------------------------------------------------ durum

    public HatMesh layer(int l) {
        layer = l == GLOW && !glowAllowed ? BASE : l;
        return this;
    }

    public HatMesh color(int argb) {
        color = argb;
        return this;
    }

    public HatMesh rgb(int rgb) {
        color = 0xFF000000 | (rgb & 0xFFFFFF);
        return this;
    }

    public HatMesh region(int r) {
        region = r;
        return this;
    }

    public int currentLayer() {
        return layer;
    }

    // ------------------------------------------------------------------ donusum

    private void identity(int at) {
        int o = at * 12;
        for (int i = 0; i < 12; i++) st[o + i] = 0f;
        st[o] = st[o + 5] = st[o + 10] = 1f;
    }

    public void push() {
        if (sp >= 23) return;
        System.arraycopy(st, sp * 12, st, (sp + 1) * 12, 12);
        sp++;
    }

    public void pop() {
        if (sp > 0) sp--;
    }

    public void translate(float x, float y, float z) {
        int o = sp * 12;
        st[o + 3] += st[o] * x + st[o + 1] * y + st[o + 2] * z;
        st[o + 7] += st[o + 4] * x + st[o + 5] * y + st[o + 6] * z;
        st[o + 11] += st[o + 8] * x + st[o + 9] * y + st[o + 10] * z;
    }

    public void scale(float x, float y, float z) {
        int o = sp * 12;
        for (int r = 0; r < 3; r++) {
            st[o + r * 4] *= x;
            st[o + r * 4 + 1] *= y;
            st[o + r * 4 + 2] *= z;
        }
    }

    public void scale(float s) {
        scale(s, s, s);
    }

    /** Sag carpim: M = M * R (R yerel eksende). */
    private void mulRot(float a00, float a01, float a02, float a10, float a11, float a12, float a20, float a21, float a22) {
        int o = sp * 12;
        for (int r = 0; r < 3; r++) {
            float m0 = st[o + r * 4], m1 = st[o + r * 4 + 1], m2 = st[o + r * 4 + 2];
            st[o + r * 4] = m0 * a00 + m1 * a10 + m2 * a20;
            st[o + r * 4 + 1] = m0 * a01 + m1 * a11 + m2 * a21;
            st[o + r * 4 + 2] = m0 * a02 + m1 * a12 + m2 * a22;
        }
    }

    public void rotX(float a) {
        float c = (float) Math.cos(a), s = (float) Math.sin(a);
        mulRot(1, 0, 0, 0, c, -s, 0, s, c);
    }

    public void rotY(float a) {
        float c = (float) Math.cos(a), s = (float) Math.sin(a);
        mulRot(c, 0, s, 0, 1, 0, -s, 0, c);
    }

    public void rotZ(float a) {
        float c = (float) Math.cos(a), s = (float) Math.sin(a);
        mulRot(c, -s, 0, s, c, 0, 0, 0, 1);
    }

    /** Yerel noktayi sapka uzayina cevirir (tmp[i..i+2]). */
    private void apply(float x, float y, float z, float[] out, int i) {
        int o = sp * 12;
        out[i] = st[o] * x + st[o + 1] * y + st[o + 2] * z + st[o + 3];
        out[i + 1] = st[o + 4] * x + st[o + 5] * y + st[o + 6] * z + st[o + 7];
        out[i + 2] = st[o + 8] * x + st[o + 9] * y + st[o + 10] * z + st[o + 11];
    }

    /** Parcacik cikis noktasi (yerel). */
    public void emitter(float x, float y, float z) {
        if (emitCount >= MAX_EMIT) return;
        apply(x, y, z, emit, emitCount * 3);
        emitCount++;
    }

    // ------------------------------------------------------------------ dortgen

    private float regionU(float s) {
        int rx = region & 3;
        float f = region == R_SOLID ? 0.5f : 0.06f + 0.88f * frac(s);
        return (rx * 16 + f * 16f) / 64f;
    }

    private float regionV(float t) {
        int ry = (region >> 2) & 3;
        float f = region == R_SOLID ? 0.5f : 0.06f + 0.88f * frac(t);
        return (ry * 16 + f * 16f) / 64f;
    }

    private static float frac(float v) {
        if (!(v == v) || v == Float.POSITIVE_INFINITY || v == Float.NEGATIVE_INFINITY) return 0.5f;
        float f = v - (float) Math.floor(v);
        return Math.min(1f, Math.max(0f, f));
    }

    /**
     * Yerel dortgen (saat yonu onemsiz; isik icin normal kenarlardan hesaplanir). s/t: doku koordinati (0..1 tekrar).
     */
    public void quad(float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3,
                     float s0, float t0, float s1, float t1) {
        quadC(x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3, s0, t0, s1, t1, color, color, color, color);
    }

    public void quadC(float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3,
                      float s0, float t0, float s1, float t1, int c0, int c1, int c2, int c3) {
        if (sink == null || quads >= MAX_QUADS) return;
        apply(x0, y0, z0, tmp, 0);
        apply(x1, y1, z1, tmp, 3);
        apply(x2, y2, z2, tmp, 6);
        apply(x3, y3, z3, tmp, 9);
        // normal (sapka uzayinda): kosegenlerin capraz carpimi (dejenere ucgenlerde de kararli)
        float ax = tmp[6] - tmp[0], ay = tmp[7] - tmp[1], az = tmp[8] - tmp[2];
        float bx = tmp[9] - tmp[3], by = tmp[10] - tmp[4], bz = tmp[11] - tmp[5];
        float nx = ay * bz - az * by, ny = az * bx - ax * bz, nz = ax * by - ay * bx;
        float nl = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (!(nl > 1e-9f)) return;   // alansiz dortgen
        nx /= nl;
        ny /= nl;
        nz /= nl;
        for (int i = 0; i < 4; i++) {
            float x = tmp[i * 3], y = tmp[i * 3 + 1], z = tmp[i * 3 + 2];
            if (!(x == x && y == y && z == z)) return;
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
            if (z < minZ) minZ = z;
            if (z > maxZ) maxZ = z;
            // model uzayi: y asagi
            xyz[i * 3] = x;
            xyz[i * 3 + 1] = -y;
            xyz[i * 3 + 2] = z;
        }
        uv[0] = regionU(s0);
        uv[1] = regionV(t0);
        uv[2] = regionU(s1);
        uv[3] = regionV(t0);
        uv[4] = regionU(s1);
        uv[5] = regionV(t1);
        uv[6] = regionU(s0);
        uv[7] = regionV(t1);
        // tekrar eden dokuda (s1 = 1.0 -> frac 0) son sutun bozulmasin
        if (region != R_SOLID) {
            if (s1 > s0 && frac(s1) < frac(s0)) {
                uv[2] = uv[4] = ((region & 3) * 16 + 0.94f * 16f) / 64f;
            }
            if (t1 > t0 && frac(t1) < frac(t0)) {
                uv[5] = uv[7] = (((region >> 2) & 3) * 16 + 0.94f * 16f) / 64f;
            }
        }
        col[0] = c0;
        col[1] = c1;
        col[2] = c2;
        col[3] = c3;
        sink.quad(layer, xyz, uv, col, nx, -ny, nz);
        quads++;
    }

    // ------------------------------------------------------------------ ilkel sekiller

    // ------------------------------------------------------------------ trigonometri tablolari
    // Ilkel sekiller her karede ayni acilari kullanir: sin/cos bir kez hesaplanir (sonuclar eskisiyle bit bit ayni).

    /** TC[n][i] = cos((float) (i * 2pi / n)), n = 3..48. */
    private static final float[][] TC = new float[49][], TS = new float[49][];
    /** Tup halkasi: cos(i * 2pi / n) (double aci), n = 3..16. */
    private static final float[][] DC = new float[17][], DS = new float[17][];
    /** Kure enlemi: LC[lat][i] = cos((float) (pi * i / lat - pi / 2)), lat = 2..16. */
    private static final float[][] LC = new float[17][], LS = new float[17][];

    static {
        for (int n = 3; n <= 48; n++) {
            TC[n] = new float[n + 1];
            TS[n] = new float[n + 1];
            for (int i = 0; i <= n; i++) {
                float a = (float) (i * Math.PI * 2 / n);
                TC[n][i] = (float) Math.cos(a);
                TS[n][i] = (float) Math.sin(a);
            }
        }
        for (int n = 3; n <= 16; n++) {
            DC[n] = new float[n + 1];
            DS[n] = new float[n + 1];
            for (int i = 0; i <= n; i++) {
                DC[n][i] = (float) Math.cos(i * Math.PI * 2 / n);
                DS[n][i] = (float) Math.sin(i * Math.PI * 2 / n);
            }
        }
        for (int n = 2; n <= 16; n++) {
            LC[n] = new float[n + 1];
            LS[n] = new float[n + 1];
            for (int i = 0; i <= n; i++) {
                float p = (float) (Math.PI * i / n - Math.PI / 2);
                LC[n][i] = (float) Math.cos(p);
                LS[n][i] = (float) Math.sin(p);
            }
        }
    }

    /** Eksene hizali kutu: merkez + yari boyutlar. */
    public void box(float cx, float cy, float cz, float hx, float hy, float hz) {
        float x0 = cx - hx, x1 = cx + hx, y0 = cy - hy, y1 = cy + hy, z0 = cz - hz, z1 = cz + hz;
        quad(x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, 0, 0, 1, 1);   // ust
        quad(x0, y0, z1, x1, y0, z1, x1, y0, z0, x0, y0, z0, 0, 0, 1, 1);   // alt
        quad(x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, 0, 0, 1, 1);   // on
        quad(x1, y0, z1, x0, y0, z1, x0, y1, z1, x1, y1, z1, 0, 0, 1, 1);   // arka
        quad(x0, y0, z1, x0, y0, z0, x0, y1, z0, x0, y1, z1, 0, 0, 1, 1);   // sol
        quad(x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0, 0, 0, 1, 1);   // sag
    }

    /** Y ekseni etrafinda kesik koni: y0'da r0, y1'de r1. */
    public void frustum(float y0, float r0, float y1, float r1, int seg, boolean capBottom, boolean capTop) {
        seg = Math.max(3, Math.min(48, seg));
        float h = Math.abs(y1 - y0);
        for (int i = 0; i < seg; i++) {
            float c0 = TC[seg][i], s0 = TS[seg][i], c1 = TC[seg][i + 1], s1 = TS[seg][i + 1];
            float u0 = (float) i / seg * Math.max(1, Math.round((r0 + r1) * 4f)), u1 = (float) (i + 1) / seg * Math.max(1, Math.round((r0 + r1) * 4f));
            quad(c0 * r0, y0, s0 * r0, c1 * r0, y0, s1 * r0, c1 * r1, y1, s1 * r1, c0 * r1, y1, s0 * r1, u0, 0, u1, Math.max(0.2f, h * 2f));
            if (capBottom && r0 > 1e-4f) quad(0, y0, 0, 0, y0, 0, c1 * r0, y0, s1 * r0, c0 * r0, y0, s0 * r0, 0.5f, 0.5f, 0.5f + c0 * 0.5f, 0.5f + s0 * 0.5f);
            if (capTop && r1 > 1e-4f) quad(0, y1, 0, c0 * r1, y1, s0 * r1, c1 * r1, y1, s1 * r1, 0, y1, 0, 0.5f, 0.5f, 0.5f + c0 * 0.5f, 0.5f + s0 * 0.5f);
        }
    }

    /** Duz halka disk (ust ve alt yuz ayni dortgen: cift yuzlu cizilir). */
    public void disc(float y, float rIn, float rOut, int seg) {
        seg = Math.max(3, Math.min(48, seg));
        for (int i = 0; i < seg; i++) {
            float c0 = TC[seg][i], s0 = TS[seg][i], c1 = TC[seg][i + 1], s1 = TS[seg][i + 1];
            quad(c0 * rIn, y, s0 * rIn, c0 * rOut, y, s0 * rOut, c1 * rOut, y, s1 * rOut, c1 * rIn, y, s1 * rIn, (float) i / seg * 3f, 0, (float) (i + 1) / seg * 3f, 1);
        }
    }

    /** Kenari kalinlikli siper (disk + dis kenar seridi). */
    public void brim(float y, float rIn, float rOut, float thick, int seg) {
        disc(y, rIn, rOut, seg);
        disc(y + thick, rIn, rOut, seg);
        frustum(y, rOut, y + thick, rOut, seg, false, false);
    }

    /** Yatay simit: y yuksekliginde R yaricapli, r kalinlikli. */
    public void torus(float y, float R, float r, int segMajor, int segMinor) {
        segMajor = Math.max(3, Math.min(48, segMajor));
        segMinor = Math.max(3, Math.min(16, segMinor));
        for (int i = 0; i < segMajor; i++) {
            float ca0 = TC[segMajor][i], sa0 = TS[segMajor][i], ca1 = TC[segMajor][i + 1], sa1 = TS[segMajor][i + 1];
            for (int j = 0; j < segMinor; j++) {
                float cb0 = TC[segMinor][j], sb0 = TS[segMinor][j], cb1 = TC[segMinor][j + 1], sb1 = TS[segMinor][j + 1];
                float q0 = R + r * cb0, q1 = R + r * cb1;
                quad(ca0 * q0, y + r * sb0, sa0 * q0, ca1 * q0, y + r * sb0, sa1 * q0, ca1 * q1, y + r * sb1, sa1 * q1, ca0 * q1, y + r * sb1, sa0 * q1,
                        (float) i / segMajor * 4f, (float) j / segMinor, (float) (i + 1) / segMajor * 4f, (float) (j + 1) / segMinor);
            }
        }
    }

    /** Kure (dusuk poligon). */
    public void sphere(float cx, float cy, float cz, float r, int lat, int lon) {
        lat = Math.max(2, Math.min(16, lat));
        lon = Math.max(3, Math.min(24, lon));
        for (int i = 0; i < lat; i++) {
            float y0 = LS[lat][i] * r, y1 = LS[lat][i + 1] * r, q0 = LC[lat][i] * r, q1 = LC[lat][i + 1] * r;
            for (int j = 0; j < lon; j++) {
                float c0 = TC[lon][j], s0 = TS[lon][j], c1 = TC[lon][j + 1], s1 = TS[lon][j + 1];
                quad(cx + c0 * q0, cy + y0, cz + s0 * q0, cx + c1 * q0, cy + y0, cz + s1 * q0, cx + c1 * q1, cy + y1, cz + s1 * q1,
                        cx + c0 * q1, cy + y1, cz + s0 * q1, (float) j / lon * 2f, (float) i / lat, (float) (j + 1) / lon * 2f, (float) (i + 1) / lat);
            }
        }
    }

    /** Elipsoid: kureye yerel olcek. */
    public void ellipsoid(float cx, float cy, float cz, float rx, float ry, float rz, int lat, int lon) {
        push();
        translate(cx, cy, cz);
        scale(rx, ry, rz);
        sphere(0, 0, 0, 1f, lat, lon);
        pop();
    }

    private final float[] tubeFrame = new float[9];

    /**
     * Cokgen boyunca tup: pts = [x0,y0,z0, x1,..], n nokta. Yaricap r0'dan r1'e incelir; uc kapatilir.
     */
    public void tube(float[] pts, int n, float r0, float r1, int seg) {
        if (pts == null || n < 2) return;
        seg = Math.max(3, Math.min(16, seg));
        // ilk cerceve
        float px = 0, py = 0, pz = 0;  // onceki halka icin referans dik vektor
        float[] fr = tubeFrame;
        float prevRing0x = 0, prevRing0y = 0, prevRing0z = 0;
        for (int k = 0; k < n; k++) {
            int a = Math.max(0, k - 1), b = Math.min(n - 1, k + 1);
            float tx = pts[b * 3] - pts[a * 3], ty = pts[b * 3 + 1] - pts[a * 3 + 1], tz = pts[b * 3 + 2] - pts[a * 3 + 2];
            float tl = (float) Math.sqrt(tx * tx + ty * ty + tz * tz);
            if (tl < 1e-6f) {
                tx = 0;
                ty = 1;
                tz = 0;
                tl = 1;
            }
            tx /= tl;
            ty /= tl;
            tz /= tl;
            if (k == 0) {
                // sabit bir yonun teget duzlemine izdusumu: tegete gore surekli (esik yok, halka donmez)
                float rx = 0, ry = 0, rz = 1;
                float d = tz;
                if (Math.abs(d) > 0.98f) {
                    rx = 1;
                    rz = 0;
                    d = tx;
                }
                px = rx - d * tx;
                py = ry - d * ty;
                pz = rz - d * tz;
            } else {
                // paralel tasima: onceki dik vektoru yeni tegete izdusur
                float d = px * tx + py * ty + pz * tz;
                px -= d * tx;
                py -= d * ty;
                pz -= d * tz;
            }
            float pl = (float) Math.sqrt(px * px + py * py + pz * pz);
            if (pl < 1e-6f) {
                px = 1;
                py = 0;
                pz = 0;
                pl = 1;
            }
            px /= pl;
            py /= pl;
            pz /= pl;
            float qx = ty * pz - tz * py, qy = tz * px - tx * pz, qz = tx * py - ty * px;
            fr[0] = px;
            fr[1] = py;
            fr[2] = pz;
            fr[3] = qx;
            fr[4] = qy;
            fr[5] = qz;
            if (k > 0) {
                float ra = r0 + (r1 - r0) * (k - 1) / (float) (n - 1), rb = r0 + (r1 - r0) * k / (float) (n - 1);
                float ax0 = pts[(k - 1) * 3], ay0 = pts[(k - 1) * 3 + 1], az0 = pts[(k - 1) * 3 + 2];
                float bx0 = pts[k * 3], by0 = pts[k * 3 + 1], bz0 = pts[k * 3 + 2];
                for (int i = 0; i < seg; i++) {
                    float c0 = DC[seg][i], s0 = DS[seg][i];
                    float c1 = DC[seg][i + 1], s1 = DS[seg][i + 1];
                    // onceki halka yonu (sakli) + bu halka yonu
                    float pa0x = prevRing0x, pa0y = prevRing0y, pa0z = prevRing0z;
                    float qa0x = tubeFrame[6], qa0y = tubeFrame[7], qa0z = tubeFrame[8];
                    // onceki cerceveyi yeniden kur: p_prev ve q_prev = (sakli p) ve (sakli q)
                    float e0x = pa0x * c0 + qa0x * s0, e0y = pa0y * c0 + qa0y * s0, e0z = pa0z * c0 + qa0z * s0;
                    float e1x = pa0x * c1 + qa0x * s1, e1y = pa0y * c1 + qa0y * s1, e1z = pa0z * c1 + qa0z * s1;
                    float f0x = px * c0 + qx * s0, f0y = py * c0 + qy * s0, f0z = pz * c0 + qz * s0;
                    float f1x = px * c1 + qx * s1, f1y = py * c1 + qy * s1, f1z = pz * c1 + qz * s1;
                    quad(ax0 + e0x * ra, ay0 + e0y * ra, az0 + e0z * ra, ax0 + e1x * ra, ay0 + e1y * ra, az0 + e1z * ra,
                            bx0 + f1x * rb, by0 + f1y * rb, bz0 + f1z * rb, bx0 + f0x * rb, by0 + f0y * rb, bz0 + f0z * rb,
                            (float) i / seg, (float) (k - 1) / (n - 1) * 2f, (float) (i + 1) / seg, (float) k / (n - 1) * 2f);
                }
            }
            prevRing0x = px;
            prevRing0y = py;
            prevRing0z = pz;
            tubeFrame[6] = qx;
            tubeFrame[7] = qy;
            tubeFrame[8] = qz;
            // uc kapaklari
            if ((k == 0 && r0 > 1e-4f) || (k == n - 1 && r1 > 1e-4f)) {
                float rr = k == 0 ? r0 : r1;
                float cx = pts[k * 3], cy = pts[k * 3 + 1], cz = pts[k * 3 + 2];
                for (int i = 0; i < seg; i++) {
                    float c0 = DC[seg][i], s0 = DS[seg][i];
                    float c1 = DC[seg][i + 1], s1 = DS[seg][i + 1];
                    quad(cx, cy, cz, cx, cy, cz, cx + (px * c1 + qx * s1) * rr, cy + (py * c1 + qy * s1) * rr, cz + (pz * c1 + qz * s1) * rr,
                            cx + (px * c0 + qx * s0) * rr, cy + (py * c0 + qy * s0) * rr, cz + (pz * c0 + qz * s0) * rr, 0.5f, 0.5f, 0.6f, 0.6f);
                }
            }
        }
    }

    /** Ikinci dereceden bezier egrisini pts dizisine ornekler (n nokta). */
    public static int bezier(float[] out, int n, float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz) {
        n = Math.max(2, Math.min(out.length / 3, n));
        for (int i = 0; i < n; i++) {
            float t = i / (float) (n - 1), u = 1 - t;
            out[i * 3] = u * u * ax + 2 * u * t * bx + t * t * cx;
            out[i * 3 + 1] = u * u * ay + 2 * u * t * by + t * t * cy;
            out[i * 3 + 2] = u * u * az + 2 * u * t * bz + t * t * cz;
        }
        return n;
    }

    /** Kalinlikli yildiz (XY duzleminde, -z'ye bakar): kose sayisi points. */
    public void star(float cx, float cy, float cz, float rOut, float rIn, float depth, int points) {
        points = Math.max(3, Math.min(12, points));
        int n = points * 2;
        float hd = depth / 2f;
        for (int i = 0; i < n; i++) {
            float a0 = (float) (Math.PI / 2 + i * Math.PI / points), a1 = (float) (Math.PI / 2 + (i + 1) * Math.PI / points);
            float r0 = (i & 1) == 0 ? rOut : rIn, r1 = (i & 1) == 0 ? rIn : rOut;
            float x0 = cx + (float) Math.cos(a0) * r0, y0 = cy + (float) Math.sin(a0) * r0;
            float x1 = cx + (float) Math.cos(a1) * r1, y1 = cy + (float) Math.sin(a1) * r1;
            quad(cx, cy, cz - hd, cx, cy, cz - hd, x1, y1, cz - hd, x0, y0, cz - hd, 0.5f, 0.5f, 0.6f, 0.6f);   // on
            quad(cx, cy, cz + hd, x0, y0, cz + hd, x1, y1, cz + hd, cx, cy, cz + hd, 0.5f, 0.5f, 0.6f, 0.6f);   // arka
            quad(x0, y0, cz - hd, x1, y1, cz - hd, x1, y1, cz + hd, x0, y0, cz + hd, 0, 0, 1, 1);                // kenar
        }
    }

    /** Kalp (XY duzlemi, -z'ye bakar), kalinlikli. */
    public void heart(float cx, float cy, float cz, float size, float depth) {
        int n = 20;
        float hd = depth / 2f;
        float pxp = 0, pyp = 0;
        for (int i = 0; i <= n; i++) {
            float t = (float) (i * Math.PI * 2 / n);
            float hx = (float) (16 * Math.pow(Math.sin(t), 3)) / 17f * size;
            float hy = (float) (13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t)) / 17f * size;
            if (i > 0) {
                quad(cx, cy, cz - hd, cx, cy, cz - hd, cx + hx, cy + hy, cz - hd, cx + pxp, cy + pyp, cz - hd, 0.5f, 0.5f, 0.6f, 0.6f);
                quad(cx, cy, cz + hd, cx + pxp, cy + pyp, cz + hd, cx + hx, cy + hy, cz + hd, cx, cy, cz + hd, 0.5f, 0.5f, 0.6f, 0.6f);
                quad(cx + pxp, cy + pyp, cz - hd, cx + hx, cy + hy, cz - hd, cx + hx, cy + hy, cz + hd, cx + pxp, cy + pyp, cz + hd, 0, 0, 1, 1);
            }
            pxp = hx;
            pyp = hy;
        }
    }
}
