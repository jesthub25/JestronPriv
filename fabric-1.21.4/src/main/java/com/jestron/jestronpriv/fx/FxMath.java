package com.jestron.jestronpriv.fx;

import com.jestron.jestronpriv.fx.api.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Display entity donusum yardimcilari. Tum vektorler entity capasina (anchor) goredir. */
public final class FxMath {

    public static final float PI = (float) Math.PI;
    /** Bir "item" sprite'inin (kilic, ok) ucunun model uzayindaki yonu: sag-ust kose. */
    private static final Vector3f SPRITE_TIP = new Vector3f(1f, 1f, 0f).normalize();
    /** Sprite ucu ile model merkezi arasindaki mesafe (scale 1 icin). */
    public static final float SPRITE_TIP_DIST = 0.62f;

    private FxMath() {
    }

    public static Vector3f v(double x, double y, double z) {
        return new Vector3f((float) x, (float) y, (float) z);
    }

    public static Quaternionf q() {
        return new Quaternionf();
    }

    public static Quaternionf qY(double a) {
        return new Quaternionf().rotateY((float) a);
    }

    public static Quaternionf qX(double a) {
        return new Quaternionf().rotateX((float) a);
    }

    public static Quaternionf qZ(double a) {
        return new Quaternionf().rotateZ((float) a);
    }

    public static Quaternionf qAxis(double angle, Vector3f axis) {
        Vector3f n = new Vector3f(axis);
        if (n.lengthSquared() < 1e-8f) return new Quaternionf();
        n.normalize();
        return new Quaternionf().rotateAxis((float) angle, n.x, n.y, n.z);
    }

    /** a * b (once b, sonra a uygulanir). */
    public static Quaternionf mul(Quaternionf a, Quaternionf b) {
        return new Quaternionf(a).mul(b);
    }

    public static Quaternionf mul(Quaternionf a, Quaternionf b, Quaternionf c) {
        return new Quaternionf(a).mul(b).mul(c);
    }

    public static Vector3f rot(Quaternionf q, Vector3f v) {
        return new Quaternionf(q).transform(new Vector3f(v));
    }

    public static Quaternionf fromTo(Vector3f from, Vector3f to) {
        Vector3f a = new Vector3f(from).normalize();
        Vector3f b = new Vector3f(to);
        if (b.lengthSquared() < 1e-8f) return new Quaternionf();
        b.normalize();
        return new Quaternionf().rotationTo(a, b);
    }

    /** BlockDisplay: [0,1]^3 kupunu verilen merkez/rotasyon/boyutla yerlestirir. */
    public static Transformation cube(Vector3f center, Quaternionf rot, Vector3f size) {
        Vector3f s = new Vector3f(Math.max(size.x, 0.0005f), Math.max(size.y, 0.0005f), Math.max(size.z, 0.0005f));
        Vector3f half = new Vector3f(s).mul(0.5f);
        new Quaternionf(rot).transform(half);
        Vector3f t = new Vector3f(center).sub(half);
        return new Transformation(t, new Quaternionf(rot), s, new Quaternionf());
    }

    public static Transformation cube(Vector3f center, Quaternionf rot, float size) {
        return cube(center, rot, new Vector3f(size, size, size));
    }

    /** ItemDisplay / TextDisplay: model zaten merkezlidir. */
    public static Transformation item(Vector3f center, Quaternionf rot, float scale) {
        float s = Math.max(scale, 0.0005f);
        return new Transformation(new Vector3f(center), new Quaternionf(rot), new Vector3f(s, s, s), new Quaternionf());
    }

    public static Transformation item(Vector3f center, Quaternionf rot, Vector3f scale) {
        Vector3f s = new Vector3f(Math.max(scale.x, 0.0005f), Math.max(scale.y, 0.0005f), Math.max(scale.z, 0.0005f));
        return new Transformation(new Vector3f(center), new Quaternionf(rot), s, new Quaternionf());
    }

    /** a noktasindan b noktasina uzanan ince kup (isin/cubuk). */
    public static Transformation beam(Vector3f a, Vector3f b, float width) {
        Vector3f d = new Vector3f(b).sub(a);
        float len = d.length();
        Quaternionf r;
        if (len < 1e-4f) {
            len = 1e-4f;
            r = new Quaternionf();
        } else {
            r = new Quaternionf().rotationTo(0f, 1f, 0f, d.x / len, d.y / len, d.z / len);
        }
        Vector3f c = new Vector3f(a).add(b).mul(0.5f);
        return cube(c, r, new Vector3f(width, len, width));
    }

    /**
     * Kilic/ok gibi sprite'lar icin: ucu "dir" yonune bakar, "spin" ile kendi ekseninde doner.
     */
    public static Quaternionf spriteToward(Vector3f dir, double spin) {
        // Once ucu asagi (-Y) cevir: Z ekseninde -135 derece. Sprite XY duzleminde kalir.
        Quaternionf base = qZ(-3.0 * Math.PI / 4.0);
        Quaternionf s = qY(spin);
        Quaternionf tilt = fromTo(new Vector3f(0f, -1f, 0f), dir);
        return mul(tilt, s, base);
    }

    /** Yatay hedefe bakan, dikey duzlemli sprite (kilic cemberi icin). pitch>0 => ucu asagi. */
    public static Quaternionf spriteHorizontal(double yawToDir, double pitchDown) {
        return mul(qY(yawToDir), qZ(-Math.PI / 4.0 - pitchDown));
    }

    /** +X eksenini (dx,dz) yonune ceviren Y acisi. */
    public static double yawForX(double dx, double dz) {
        return Math.atan2(-dz, dx);
    }

    public static double clamp01(double x) {
        return x < 0 ? 0 : (x > 1 ? 1 : x);
    }

    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    public static double easeOut(double t) {
        t = clamp01(t);
        return 1 - (1 - t) * (1 - t) * (1 - t);
    }

    public static double easeIn(double t) {
        t = clamp01(t);
        return t * t;
    }

    public static double easeInOut(double t) {
        t = clamp01(t);
        return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
    }

    public static double easeOutBack(double t) {
        t = clamp01(t);
        double c1 = 1.70158, c3 = c1 + 1;
        return 1 + c3 * Math.pow(t - 1, 3) + c1 * Math.pow(t - 1, 2);
    }

    public static Vector3f lerp(Vector3f a, Vector3f b, double t) {
        return new Vector3f(a).lerp(b, (float) t);
    }

    public static Vector3f randomUnit(java.util.Random r) {
        double u = r.nextDouble() * 2 - 1;
        double th = r.nextDouble() * Math.PI * 2;
        double s = Math.sqrt(1 - u * u);
        return v(s * Math.cos(th), u, s * Math.sin(th));
    }
}
