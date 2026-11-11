package com.jestron.jestronpriv.client.look;

import com.jestron.jestronpriv.cape.CapeArt;

/**
 * Gokyuzu efektlerinin geometrisi (Minecraft'a bagimsiz, test edilebilir): kutup isigi perdeleri ve kayan yildizlar.
 * Uzay: kamera merkezli, dunya eksenli; kuzey = -Z, yukari = +Y, gok kubbesi ~100 blok yaricapta (vanilla gunes /
 * ay ile ayni). Ciktilar dortgen listesidir: her kose x, y, z ve ARGB.
 */
public final class SkyFx {

    /** Dortgen alicisi. */
    public interface Sink {
        void quad(float[] xyz, int[] argb);
    }

    /** Aktif kayan yildiz. */
    public static final class Meteor {
        public float sx, sy, sz, vx, vy, vz;
        public float age, life, len, width;
        public boolean alive;
    }

    public static final int MAX_METEORS = 8;
    public final Meteor[] meteors = new Meteor[MAX_METEORS];
    private final float[] q = new float[12];
    private final int[] c = new int[4];
    private long seed = 0x9E3779B97F4A7C15L;

    public SkyFx() {
        for (int i = 0; i < MAX_METEORS; i++) meteors[i] = new Meteor();
    }

    private float rnd() {
        seed ^= seed << 13;
        seed ^= seed >>> 7;
        seed ^= seed << 17;
        return ((seed >>> 40) & 0xFFFFFF) / (float) 0x1000000;
    }

    private static int argb(float a, int rgb) {
        int ai = (int) (CapeArt.clamp01(a) * 255f + 0.5f);
        return (ai << 24) | (rgb & 0xFFFFFF);
    }

    /** Gok kubbesi yaricapi: en dusuk gorus mesafesinde (2 parca = 128 blok uzak duzlem) bile kirpilmaz. */
    public static final float DOME = 96f;

    /** Noktayi kubbeye yansitir (gok sonsuz uzakta: sadece yon onemli). */
    private void put(int v, float x, float y, float z) {
        float l = (float) Math.sqrt(x * x + y * y + z * z);
        float k = l < 1e-4f ? 0f : DOME / l;
        q[v * 3] = x * k;
        q[v * 3 + 1] = y * k;
        q[v * 3 + 2] = z * k;
    }

    // ------------------------------------------------------------------ kutup isigi

    /**
     * Kuzey gogunde iki katmanli, dalgalanan isik perdeleri.
     *
     * @param t         saniye
     * @param intensity 0..2 (gece * yagmur * guc)
     * @param height    0.6..1.6
     * @param rainbow   renkler zamanla / boyunca gokkusagi
     */
    public void aurora(Sink out, float t, float intensity, float height, int c1, int c2, boolean rainbow) {
        if (!(intensity > 0.003f) || !Float.isFinite(t)) return;
        intensity = Math.min(2f, intensity);
        height = CapeArt.clamp(height, 0.6f, 1.6f);
        layer(out, t, intensity, height, c1, c2, rainbow, 100f, 0f, 1f);
        layer(out, t * 0.8f + 17f, intensity * 0.55f, height * 1.15f, c1, c2, rainbow, 125f, 0.9f, 0.85f);
    }

    private void layer(Sink out, float t, float k, float h, int c1, int c2, boolean rainbow, float dist, float phase, float span) {
        final int n = 72;
        float arc = (float) Math.toRadians(150.0) * span;
        float px = 0, pz = 0, pBot = 0, pTop = 0, pA = 0;
        int pc1 = 0, pc2 = 0;
        for (int i = 0; i <= n; i++) {
            float s = (float) i / n;
            float az = -arc / 2f + arc * s + 0.25f * CapeArt.sin(t * 0.07f + phase);
            float fold = 9f * CapeArt.sin(s * 7.3f + t * 0.33f + phase) + 4f * CapeArt.sin(s * 17f - t * 0.6f + phase * 2f);
            float d = dist + fold;
            float x = d * CapeArt.sin(az), z = -d * CapeArt.cos(az);
            float wave = CapeArt.noise(s * 6f + t * 0.12f + phase * 3f, t * 0.05f);
            float bot = (22f + 7f * CapeArt.sin(s * 5f + t * 0.21f + phase)) * h;
            float top = bot + (26f + 26f * wave) * h;
            float flick = 0.45f + 0.55f * CapeArt.noise(s * 14f - t * 0.55f, t * 0.25f + phase);
            float edge = CapeArt.smooth(0f, 0.12f, s) * CapeArt.smooth(1f, 0.88f, s);
            float a = k * flick * edge;
            int a1 = c1, a2 = c2;
            if (rainbow) {
                a1 = CapeArt.hsv(s * 0.8f + t * 0.03f, 0.75f, 1f);
                a2 = CapeArt.hsv(s * 0.8f + t * 0.03f + 0.3f, 0.75f, 1f);
            }
            if (i > 0) {
                float m0 = pBot + (pTop - pBot) * 0.28f, m1 = bot + (top - bot) * 0.28f;
                // alt bant: alt kenar saydam, ust tarafta parlak
                put(0, px, pBot, pz);
                put(1, x, bot, z);
                put(2, x, m1, z);
                put(3, px, m0, pz);
                c[0] = argb(0f, pc1);
                c[1] = argb(0f, a1);
                c[2] = argb(a * 0.85f, a1);
                c[3] = argb(pA * 0.85f, pc1);
                out.quad(q, c);
                // ust bant: renk ikinci renge kayar, tepede soner
                put(0, px, m0, pz);
                put(1, x, m1, z);
                put(2, x, top, z);
                put(3, px, pTop, pz);
                c[0] = argb(pA * 0.85f, pc1);
                c[1] = argb(a * 0.85f, a1);
                c[2] = argb(0f, a2);
                c[3] = argb(0f, pc2);
                out.quad(q, c);
            }
            px = x;
            pz = z;
            pBot = bot;
            pTop = top;
            pA = a;
            pc1 = a1;
            pc2 = a2;
        }
    }

    // ------------------------------------------------------------------ kayan yildiz

    /**
     * Kayan yildizlari ilerletir ve gerekirse yenisini dogurur.
     *
     * @param dt        saniye
     * @param perMinute dakikadaki ortalama yildiz
     * @param night     0..1 (gunduz yeni yildiz dogmaz)
     */
    public void tickMeteors(float dt, float perMinute, float night, float lengthMul) {
        if (!(dt > 0f) || !Float.isFinite(dt)) return;
        dt = Math.min(dt, 0.25f);
        for (Meteor m : meteors) {
            if (!m.alive) continue;
            m.age += dt;
            if (m.age >= m.life || headElevation(m) < 0.04f) m.alive = false;
        }
        float p = dt * perMinute / 60f * CapeArt.clamp01(night);
        if (rnd() < p) spawn(lengthMul);
    }

    private void spawn(float lengthMul) {
        Meteor m = null;
        for (Meteor x : meteors) {
            if (!x.alive) {
                m = x;
                break;
            }
        }
        if (m == null) return;
        // baslangic: yukseklik 25-70 derece, rastgele yon
        float el = (float) Math.toRadians(25 + rnd() * 45), az = rnd() * CapeArt.TAU;
        float ce = (float) Math.cos(el);
        m.sx = ce * (float) Math.sin(az);
        m.sy = (float) Math.sin(el);
        m.sz = -ce * (float) Math.cos(az);
        // hiz: kubbeye teget, cogunlukla asagi
        float ex = (float) Math.cos(az), ez = (float) Math.sin(az);
        float side = rnd() * 2f - 1f;
        float dx = ex * side, dy = -0.6f - rnd() * 0.5f, dz = ez * side;
        float dot = dx * m.sx + dy * m.sy + dz * m.sz;
        dx -= dot * m.sx;
        dy -= dot * m.sy;
        dz -= dot * m.sz;
        float l = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (l < 1e-4f) return;
        float speed = 0.55f + rnd() * 0.45f;
        m.vx = dx / l * speed;
        m.vy = dy / l * speed;
        m.vz = dz / l * speed;
        m.age = 0f;
        m.life = 0.55f + rnd() * 0.6f;
        m.len = (0.12f + rnd() * 0.1f) * CapeArt.clamp(lengthMul, 0.5f, 2f);
        m.width = 0.55f + rnd() * 0.5f;
        m.alive = true;
    }

    /** Basin yonunun dikey bileseni (0 = ufuk). Ufka inen yildiz soner. */
    private static float headElevation(Meteor m) {
        float x = m.sx + m.vx * m.age, y = m.sy + m.vy * m.age, z = m.sz + m.vz * m.age;
        float l = (float) Math.sqrt(x * x + y * y + z * z);
        return l < 1e-4f ? -1f : y / l;
    }

    /** Ayar ekranindan deneme: hemen bir yildiz. */
    public void forceMeteor(float lengthMul) {
        spawn(lengthMul);
    }

    /** Kayan yildiz serit dortgenleri (bas parlak, kuyruk soner). */
    public void meteors(Sink out, int rgb, float bright) {
        if (!(bright > 0.003f)) return;
        for (Meteor m : meteors) {
            if (!m.alive || headElevation(m) < 0.04f) continue;
            float lifeK = m.age / m.life;
            float fade = CapeArt.smooth(0f, 0.15f, lifeK) * CapeArt.smooth(1f, 0.7f, lifeK);
            float a = fade * bright;
            if (a < 0.003f) continue;
            final int segs = 6;
            float ppx = 0, ppy = 0, ppz = 0, pwx = 0, pwy = 0, pwz = 0, pa = 0;
            for (int i = 0; i <= segs; i++) {
                float f = (float) i / segs;
                float trav = m.age - f * m.len / Math.max(0.05f, (float) Math.sqrt(m.vx * m.vx + m.vy * m.vy + m.vz * m.vz)) * 0.9f;
                float tt = Math.max(0f, trav);
                float x = m.sx + m.vx * tt, y = m.sy + m.vy * tt, z = m.sz + m.vz * tt;
                float l = (float) Math.sqrt(x * x + y * y + z * z);
                if (l < 1e-4f) return;
                x = x / l * DOME;
                y = y / l * DOME;
                z = z / l * DOME;
                // genislik: bakis (noktanin kendisi) x hiz
                float wx = y * m.vz - z * m.vy, wy = z * m.vx - x * m.vz, wz = x * m.vy - y * m.vx;
                float wl = (float) Math.sqrt(wx * wx + wy * wy + wz * wz);
                if (wl < 1e-5f) return;
                float w = m.width * (1f - f * 0.9f) * 0.5f;
                wx = wx / wl * w;
                wy = wy / wl * w;
                wz = wz / wl * w;
                float sa = a * (1f - f);
                if (i > 0) {
                    q[0] = ppx - pwx; q[1] = ppy - pwy; q[2] = ppz - pwz;
                    q[3] = ppx + pwx; q[4] = ppy + pwy; q[5] = ppz + pwz;
                    q[6] = x + wx; q[7] = y + wy; q[8] = z + wz;
                    q[9] = x - wx; q[10] = y - wy; q[11] = z - wz;
                    int head = CapeArt.mix(rgb, 0xFFFFFF, 0.5f);
                    c[0] = argb(pa, i == 1 ? head : rgb);
                    c[1] = argb(pa, i == 1 ? head : rgb);
                    c[2] = argb(sa, rgb);
                    c[3] = argb(sa, rgb);
                    out.quad(q, c);
                }
                if (i == 0) {
                    // bas: kucuk parlak isik
                    float hw = m.width * 1.4f;
                    float ux = wx / Math.max(1e-5f, w) * hw, uy = wy / Math.max(1e-5f, w) * hw, uz = wz / Math.max(1e-5f, w) * hw;
                    float vx = m.vx, vy = m.vy, vz = m.vz;
                    float vl = (float) Math.sqrt(vx * vx + vy * vy + vz * vz);
                    vx = vx / vl * hw;
                    vy = vy / vl * hw;
                    vz = vz / vl * hw;
                    int hc = CapeArt.mix(rgb, 0xFFFFFF, 0.6f);
                    for (int sq = 0; sq < 4; sq++) {
                        float su = (sq & 1) == 0 ? 1f : -1f, sv = (sq & 2) == 0 ? 1f : -1f;
                        q[0] = x; q[1] = y; q[2] = z;
                        q[3] = x + ux * su; q[4] = y + uy * su; q[5] = z + uz * su;
                        q[6] = x + ux * su + vx * sv; q[7] = y + uy * su + vy * sv; q[8] = z + uz * su + vz * sv;
                        q[9] = x + vx * sv; q[10] = y + vy * sv; q[11] = z + vz * sv;
                        c[0] = argb(a * 0.8f, hc);
                        c[1] = c[2] = c[3] = argb(0f, hc);
                        out.quad(q, c);
                    }
                }
                ppx = x;
                ppy = y;
                ppz = z;
                pwx = wx;
                pwy = wy;
                pwz = wz;
                pa = sa;
            }
        }
    }

    public int aliveCount() {
        int n = 0;
        for (Meteor m : meteors) if (m.alive) n++;
        return n;
    }
}
