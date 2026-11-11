package com.jestron.jestronpriv.fx;

import com.jestron.jestronpriv.fx.api.BlockDisplay;
import com.jestron.jestronpriv.fx.api.Display;
import com.jestron.jestronpriv.fx.api.ItemDisplay;
import com.jestron.jestronpriv.fx.api.ItemStack;
import com.jestron.jestronpriv.fx.api.Material;
import com.jestron.jestronpriv.fx.api.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.IntFunction;

import static com.jestron.jestronpriv.fx.FxMath.*;

/**
 * Yeni efektler icin guvenli parca kurucusu. Her parca:
 * <ol>
 *   <li>Oturum basinda gorunmez (0.001) olarak, ilk pozunun dondurmesiyle olusur (donus sicramasi olmaz),</li>
 *   <li>{@code from} tick'inde {@code grow} tick'te buyur,</li>
 *   <li>{@code step} tick aralikla pose fonksiyonunu takip eder (son tick her zaman dahil),</li>
 *   <li>{@code to+1}'de son pozunun dondurmesini koruyarak kuculur, en gec 47. tick'te silinir.</li>
 * </ol>
 * Boylece tum yeni efektler 2.5 sn sinirina ve titreme/sicrama kurallarina otomatik uyar.
 */
public final class FxBuild {

    public static final int DARK = 0, LIT = 1, BRIGHT = 2;
    /** Bir parcanin gorunur kalabilecegi son tick (sonrasinda kuculur). */
    public static final int LAST = FxKit.FADE_AT - 1;

    private FxBuild() {
    }

    /** Merkez, donus ve boyut. Blok icin boyut kutu olcusu; esya icin olcek. */
    public record Pose(Vector3f c, Quaternionf q, Vector3f size) {
        public static Pose of(Vector3f c, Quaternionf q, Vector3f size) {
            return new Pose(c, q, size);
        }

        public static Pose of(Vector3f c, Quaternionf q, float size) {
            return new Pose(c, q, new Vector3f(size));
        }
    }

    private static <T extends Display> T light(FxSession s, T d, int mode) {
        if (mode == BRIGHT) return s.bright(d);
        if (mode == DARK) return s.dark(d);
        return s.lit(d);
    }

    private static Vector3f safe(Vector3f v) {
        return new Vector3f(Math.max(0.001f, v.x), Math.max(0.001f, v.y), Math.max(0.001f, v.z));
    }

    private static Transformation boxTf(Pose p) {
        return cube(p.c(), p.q(), safe(p.size()));
    }

    private static Transformation itemTf(Pose p) {
        return FxMath.item(p.c(), p.q(), safe(p.size()));
    }

    /** Hareketli blok parcasi. */
    public static BlockDisplay block(FxSession s, Material m, int light, int from, int to, int step, int grow, IntFunction<Pose> f) {
        final int a = Math.max(2, from);
        final int b = Math.max(a + 1, Math.min(to, LAST));
        final int g = Math.max(1, Math.min(grow, b - a));
        final int st = Math.max(1, step);
        Pose p0 = f.apply(a);
        BlockDisplay d = light(s, s.block(m, cube(p0.c(), p0.q(), 0.001f)), light);
        // Buyurken hareketin icine yumusakca girer: hedef f(a+g), donus g tick boyunca slerp ile gecer
        s.tfAt(a, d, boxTf(f.apply(a + g)), g);
        for (int t = a + g + st; t <= b; t += st) {
            final int tt = t;
            final int dur = Math.min(st, b - t + 1);
            s.at(tt, () -> s.tf(d, boxTf(f.apply(tt)), dur));
        }
        if (b > a + g && (b - a - g) % st != 0) s.at(b, () -> s.tf(d, boxTf(f.apply(b)), 1));
        Pose pe = f.apply(b);
        s.tfAt(b + 1, d, cube(pe.c(), pe.q(), 0.001f), 3);
        s.removeAt(b + 5, d);
        return d;
    }

    /** Hareketli esya parcasi (kafa, yildiz, goz vb.). */
    public static ItemDisplay item(FxSession s, ItemStack stack, int light, int from, int to, int step, int grow, IntFunction<Pose> f) {
        final int a = Math.max(2, from);
        final int b = Math.max(a + 1, Math.min(to, LAST));
        final int g = Math.max(1, Math.min(grow, b - a));
        final int st = Math.max(1, step);
        Pose p0 = f.apply(a);
        ItemDisplay d = light(s, s.item(stack, FxMath.item(p0.c(), p0.q(), 0.001f)), light);
        s.tfAt(a, d, itemTf(f.apply(a + g)), g);
        for (int t = a + g + st; t <= b; t += st) {
            final int tt = t;
            final int dur = Math.min(st, b - t + 1);
            s.at(tt, () -> s.tf(d, itemTf(f.apply(tt)), dur));
        }
        if (b > a + g && (b - a - g) % st != 0) s.at(b, () -> s.tf(d, itemTf(f.apply(b)), 1));
        Pose pe = f.apply(b);
        s.tfAt(b + 1, d, FxMath.item(pe.c(), pe.q(), 0.001f), 3);
        s.removeAt(b + 5, d);
        return d;
    }

    public static ItemDisplay item(FxSession s, Material m, int light, int from, int to, int step, int grow, IntFunction<Pose> f) {
        return item(s, new ItemStack(m), light, from, to, step, grow, f);
    }

    /** Sabit duran parca (sadece belir / kaybol). */
    public static BlockDisplay still(FxSession s, Material m, int light, int from, int to, int grow, Pose p) {
        return block(s, m, light, from, to, Math.max(1, to - from), grow, t -> p);
    }

    // ------------------------------------------------------------------ yardimcilar

    /** Yerel ofseti bir govde pozuna gore dunyaya cevirir: c + R*off, donus R*local. */
    public static Pose attach(Vector3f bodyC, Quaternionf bodyQ, Vector3f off, Quaternionf local, Vector3f size) {
        Vector3f c = new Vector3f(bodyC).add(rot(bodyQ, new Vector3f(off)));
        return Pose.of(c, mul(bodyQ, local), size);
    }

    /** a'dan b'ye uzanan ince cubuk pozu (isin, mizrak, sap). */
    public static Pose beamPose(Vector3f a, Vector3f b, float width) {
        Vector3f d = new Vector3f(b).sub(a);
        float len = d.length();
        Quaternionf r = len < 1e-4f ? new Quaternionf() : new Quaternionf().rotationTo(0f, 1f, 0f, d.x / len, d.y / len, d.z / len);
        return Pose.of(new Vector3f(a).add(b).mul(0.5f), r, new Vector3f(width, Math.max(1e-3f, len), width));
    }

    public static double smooth(double t) {
        t = clamp01(t);
        return t * t * (3 - 2 * t);
    }

    /** a..b araliginda 0..1 ilerleme. */
    public static double phase(int t, int a, int b) {
        return clamp01((t - a) / (double) Math.max(1, b - a));
    }
}
