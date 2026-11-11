package com.jestron.jestronpriv.fx;

import com.jestron.jestronpriv.fx.api.BlockDisplay;
import com.jestron.jestronpriv.fx.api.Color;
import com.jestron.jestronpriv.fx.api.ItemDisplay;
import com.jestron.jestronpriv.fx.api.Location;
import com.jestron.jestronpriv.fx.api.Material;
import com.jestron.jestronpriv.fx.api.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

import static com.jestron.jestronpriv.fx.FxMath.*;

/**
 * Tum efektlerin ortak "gercekcilik" araclari: 3D sok halkalari, balistik enkaz, zemin catlaklari,
 * yanik izleri, parlama kabuklari, hayalet iz kopyalari ve carpma kombinasyonu.
 * Hepsi 2.5 sn sinirina uyar: her nesne en gec 48. tick'te kucultulup gizlenir.
 */
public final class FxKit {

    /** Tum gecici nesnelerin en gec kaybolmaya basladigi tick. */
    public static final int FADE_AT = 43;

    private FxKit() {
    }

    private static int clampStart(int t) {
        return Math.max(0, t);
    }

    // ------------------------------------------------------------------ 3D sok halkasi

    /**
     * Yerde/havada genisleyen 3D halka: n adet ince plaka r0'dan r1'e acilir, yukseklik h'den sifira iner.
     */
    public static void shockRing(FxSession s, double y, double r0, double r1, int from, int dur, Material m, int n, float h, boolean bright) {
        final int f = Math.max(2, from);
        final int end = Math.min(f + dur, 46);
        s.at(f - 2, () -> {
            for (int i = 0; i < n; i++) {
                final double a = i * Math.PI * 2 / n;
                final Quaternionf q = qY(-a + Math.PI / 2);
                BlockDisplay d = s.block(m, cube(v(Math.cos(a) * r0, y, Math.sin(a) * r0), q, 0.001f));
                if (bright) s.bright(d);
                else s.lit(d);
                s.anim(d, f, end, 2, t -> {
                    double k = (t - f) / (double) Math.max(1, end - f);
                    double r = lerp(r0, r1, easeOut(k));
                    double hh = Math.max(0.001, h * Math.pow(1 - k, 0.8));
                    double w = 2 * Math.PI * r / n * 1.1;
                    return cube(v(Math.cos(a) * r, y + hh / 2, Math.sin(a) * r), q, v(w, hh, 0.05));
                });
                s.tfAt(end + 1, d, cube(v(Math.cos(a) * r1, y, Math.sin(a) * r1), q, 0.001f), 1);
                s.removeAt(end + 2, d);
            }
        });
    }

    // ------------------------------------------------------------------ balistik enkaz

    /** Merkezden firlayan, donen, yere dusup duran ve sonunda kuculen 3D blok parcalari. */
    public static void debris(FxSession s, Vector3f c, Material[] mats, int count, double spMin, double spMax,
                              double upMin, double upMax, float sMin, float sMax, int at, boolean brightAll) {
        final int a0 = Math.max(2, at);
        s.at(a0 - 2, () -> {
            for (int i = 0; i < count; i++) {
                Material m = mats[i % mats.length];
                float size = (float) s.rd(sMin, sMax);
                double ang = s.rnd.nextDouble() * Math.PI * 2;
                double sp = s.rd(spMin, spMax);
                double x0 = c.x + Math.cos(ang) * 0.15, z0 = c.z + Math.sin(ang) * 0.15;
                List<double[]> path = s.ballistic(x0, c.y, z0, Math.cos(ang) * sp, s.rd(upMin, upMax), Math.sin(ang) * sp,
                        0.055, size / 2, Math.max(1, FADE_AT - a0));
                Quaternionf r0 = mul(qY(s.rnd.nextDouble() * 6.28), qX(s.rnd.nextDouble() * 6.28));
                Vector3f ax = randomUnit(s.rnd);
                BlockDisplay d = s.block(m, cube(v(x0, c.y, z0), r0, 0.001f));
                if (brightAll || m == Material.MAGMA_BLOCK || m == Material.SHROOMLIGHT || m == Material.GLOWSTONE) s.bright(d);
                else s.lit(d);
                s.tfAt(a0, d, cube(v(x0, c.y, z0), r0, size), 0);
                double angle = 0;
                Vector3f last = v(x0, c.y, z0);
                Quaternionf lastR = r0;
                double spin = s.rd(0.35, 0.7);
                for (int k = 1; k < path.size() && a0 + k <= FADE_AT; k++) {
                    double[] p = path.get(k);
                    boolean landed = p[3] >= 1;
                    if (!landed) angle += spin;
                    last = v(p[0], p[1], p[2]);
                    lastR = mul(qAxis(angle, ax), r0);
                    if (k <= 12 || k % 2 == 0) s.tfAt(a0 + k, d, cube(last, lastR, size), k <= 12 ? 1 : 2);
                }
                s.tfAt(FADE_AT + 1, d, cube(last, lastR, 0.001f), 4);
            }
        });
    }

    // ------------------------------------------------------------------ zemin izleri

    /** Merkezden disari dogru zikzak zemin catlaklari (ince koyu cubuklar). */
    public static void cracks(FxSession s, int count, double len, Material m, int at, int grow) {
        cracks(s, count, len, m, at, grow, 0, 0.09);
    }

    /** lightMode: 0 = simsiyah, 1 = dogal isik, 2 = tam parlak (magma, buz isiltisi vb.). */
    public static void cracks(FxSession s, int count, double len, Material m, int at, int grow, int lightMode, double width) {
        final int a0 = Math.max(2, at);
        s.at(a0 - 2, () -> {
            for (int i = 0; i < count; i++) {
                double ang = i * Math.PI * 2 / count + s.rd(-0.3, 0.3);
                double px = 0, pz = 0;
                int segs = 3;
                for (int g = 0; g < segs; g++) {
                    double segLen = len / segs * s.rd(0.7, 1.2);
                    double a = ang + s.rd(-0.45, 0.45);
                    double qx = px + Math.cos(a) * segLen, qz = pz + Math.sin(a) * segLen;
                    final double mx = (px + qx) / 2, mz = (pz + qz) / 2;
                    final double y = Math.max(s.groundDy(mx, mz), s.groundDy(qx, qz)) + 0.012 + i * 0.0007;
                    final float w = (float) (width * (1 - g * 0.25));
                    final float L = (float) segLen;
                    final Quaternionf q = qY(-Math.atan2(qz - pz, qx - px));
                    final double sx = px, sz = pz;
                    BlockDisplay d = s.block(m, cube(v(sx, y, sz), q, 0.001f));
                    if (lightMode == 0) s.dark(d);
                    else if (lightMode == 2) s.bright(d);
                    else s.lit(d);
                    final int st = a0 + g;
                    s.anim(d, st, st + grow, 1, t -> {
                        double k = Math.min(1, (t - st + 1) / (double) Math.max(1, grow));
                        double cx = sx + (mx - sx) * k, cz = sz + (mz - sz) * k;
                        return cube(v(cx, y, cz), q, v(Math.max(0.001, L * k), 0.02, w));
                    });
                    s.tfAt(FADE_AT, d, cube(v(mx, y, mz), q, 0.001f), 5);
                    px = qx;
                    pz = qz;
                }
            }
        });
    }

    /** Yanik izi: ust uste dondurulmus koyu plakalar. */
    public static void scorch(FxSession s, double radius, int at) {
        final int a0 = Math.max(2, at);
        s.at(a0 - 2, () -> {
            Material[] ms = {Material.COAL_BLOCK, Material.BLACK_CONCRETE, Material.GRAY_CONCRETE};
            for (int i = 0; i < 3; i++) {
                final double y = s.groundDy(0, 0) + 0.006 + i * 0.006;
                final Quaternionf q = qY(i * 0.7 + s.rnd.nextDouble());
                final float r = (float) (radius * (1 - i * 0.22));
                BlockDisplay d = s.block(ms[i], cube(v(0, y, 0), q, 0.001f));
                if (i == 0) s.dark(d);
                else s.lit(d);
                s.tfAt(a0, d, cube(v(0, y, 0), q, v(r, 0.012f, r)), 3);
                s.tfAt(FADE_AT, d, cube(v(0, y, 0), q, v(0.001f, 0.001f, 0.001f)), 5);
            }
        });
    }

    // ------------------------------------------------------------------ parlama / isin

    /** Nabiz gibi atan, tam parlak, renkli cam kabuk ("bloom" hissi). */
    public static BlockDisplay glow(FxSession s, Material glass, Vector3f c, float size, int from, int to, double pulse) {
        final int f = Math.max(2, from);
        final int end = Math.min(to, 46);
        BlockDisplay d = s.bright(s.block(glass, cube(c, mul(qY(f * 0.12), qX(0.62)), 0.001f)));
        s.anim(d, f, end, 2, t -> {
            double in = Math.min(1, (t - f) / 4.0);
            double out = t > end - 4 ? Math.max(0, (end - t) / 4.0) : 1;
            double sc = size * easeOut(in) * out * (1 + pulse * Math.sin(t * 0.9));
            return cube(c, mul(qY(t * 0.12), qX(0.62)), (float) Math.max(0.001, sc));
        });
        s.tfAt(end + 1, d, cube(c, mul(qY(end * 0.12), qX(0.62)), 0.001f), 1);
        return d;
    }

    /** Asagidan yukari (veya yukaridan asagi) uzanan cam isin sutunu. */
    public static void pillar(FxSession s, Material glass, double x, double z, double y0, double height, float width, int from, int to) {
        final int f = Math.max(2, from);
        final int end = Math.min(to, 46);
        final Vector3f c = v(x, y0 + height / 2, z);
        BlockDisplay d = s.bright(s.block(glass, cube(c, qY(f * 0.2), v(0.001, height, 0.001))));
        s.anim(d, f, end, 1, t -> {
            double in = Math.min(1, (t - f + 1) / 3.0);
            double out = t > end - 4 ? Math.max(0.001, (end - t) / 4.0) : 1;
            float w = (float) Math.max(0.001, width * easeOut(in) * out);
            return cube(c, qY(t * 0.2), v(w, height, w));
        });
        s.tfAt(end + 1, d, cube(c, qY(end * 0.2), 0.001f), 1);
    }

    // ------------------------------------------------------------------ hayalet iz

    /** Hareketli bir esyanin arkasinda kalan, hizla kuculen hayalet kopya (hareket bulaniklik hissi). */
    public static void ghostItem(FxSession s, Material m, Transformation t, int at, int life, boolean bright) {
        s.at(at, () -> {
            ItemDisplay d = s.item(m, t);
            if (bright) s.bright(d);
            else s.lit(d);
            Vector3f sc = t.getScale();
            s.tfAt(at + 2, d, item(t.getTranslation(), t.getLeftRotation(), 0.001f), Math.max(1, life - 2));
            s.removeAt(at + life, d);
        });
    }

    public static void ghostBlock(FxSession s, Material m, Transformation t, int at, int life) {
        s.at(at, () -> {
            BlockDisplay d = s.bright(s.block(m, t));
            Vector3f sc = t.getScale();
            Vector3f tr = t.getTranslation();
            Vector3f center = new Vector3f(tr).add(rot(t.getLeftRotation(), new Vector3f(sc).mul(0.5f)));
            s.tfAt(at + 2, d, cube(center, t.getLeftRotation(), 0.001f), Math.max(1, life - 2));
            s.removeAt(at + life, d);
        });
    }

    // ------------------------------------------------------------------ parcacik kombolari

    public static void dustRing(FxSession s, Location c, int n, double r, double speed) {
        for (int i = 0; i < n; i++) {
            double a = i * Math.PI * 2 / n;
            s.dir(P.CLOUD, c.clone().add(Math.cos(a) * r, 0, Math.sin(a) * r), Math.cos(a), 0.03, Math.sin(a), speed);
        }
    }

    public static void embers(FxSession s, Location c, int count, double spread) {
        for (int i = 0; i < count; i++) {
            s.dir(P.FLAME, c.clone().add(s.rd(-spread, spread), s.rd(0, spread), s.rd(-spread, spread)),
                    s.rd(-0.15, 0.15), 1, s.rd(-0.15, 0.15), s.rd(0.05, 0.16));
        }
        s.particle(P.LAVA, c, Math.max(1, count / 4), spread * 0.6, 0);
    }

    public static void sparks(FxSession s, Location c, int count, double speed) {
        for (int i = 0; i < count; i++) s.dir(P.CRIT, c, randomUnit(s.rnd), speed * s.rd(0.6, 1.2));
        s.particle(P.ELECTRIC_SPARK, c, count / 2 + 1, 0.3, 0.3);
    }

    // ------------------------------------------------------------------ carpma

    /**
     * Agir bir carpma: flas, toz halkasi, 3D sok halkasi, zemin catlaklari, yanik izi ve firlayan zemin parcalari.
     * power: 0.5 (hafif) .. 1.5 (cok agir)
     */
    public static void impact(FxSession s, double power, int at, Color tint, boolean scorch) {
        final int a0 = Math.max(2, at);
        final double gy = s.groundDy(0, 0);
        s.at(a0, () -> {
            Location c = s.loc(0, gy + 0.15, 0);
            s.flash(s.loc(0, gy + 0.8, 0), Color.WHITE);
            s.particle(P.EXPLOSION, s.loc(0, gy + 0.5, 0), (int) Math.max(1, 2 * power), 0.4 * power, 0);
            dustRing(s, c, (int) (22 * power), 0.3, 0.3 * power);
            s.groundCrack(c, (int) (60 * power), 0.9 * power);
            s.dust(c, tint, 1.2f, (int) (20 * power), 0.6 * power);
            sparks(s, s.loc(0, gy + 0.4, 0), (int) (14 * power), 0.4);
        });
        shockRing(s, gy + 0.02, 0.3, 2.6 * power, a0, 8, Material.LIGHT_GRAY_STAINED_GLASS, 14, (float) (0.28 * power), false);
        cracks(s, (int) Math.max(4, 7 * power), 1.3 * power, Material.BLACK_CONCRETE, a0, 3);
        if (scorch) scorch(s, 1.3 * power, a0);
        Material g = s.groundMaterial(Material.DIRT);
        debris(s, v(0, gy + 0.25, 0), new Material[]{g, g, Material.GRAVEL, Material.COBBLESTONE}, (int) Math.max(3, 7 * power),
                0.1 * power, 0.26 * power, 0.25, 0.45 * Math.min(1.3, power), 0.12f, (float) (0.26 * Math.min(1.3, power)), a0, false);
    }

    /** Hafif zemin dokunusu (kucuk ok, damla vb.). */
    public static void puff(FxSession s, Location c, int n) {
        dustRing(s, c, n, 0.05, 0.08);
        s.groundCrack(c, n, 0.15);
    }
}
