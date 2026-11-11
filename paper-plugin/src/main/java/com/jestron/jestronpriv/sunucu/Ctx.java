package com.jestron.jestronpriv.sunucu;

import java.util.Random;

/**
 * Aura desenlerine verilen baglam (moddakiyle ayni yardimcilar). Desen dunya konumlarini hesaplar, {@link Sink}
 * parcaciklari gercekten olusturur (sunucuda izleyicilere gonderir, testte sayar).
 */
public final class Ctx {

    /** Parcacik cikisi. Konumlar dunya koordinati; hiz modu {@link #VEL_MOTION} / {@link #VEL_OFFSET} / {@link #VEL_DATA}. */
    public interface Sink {
        void particle(String id, double x, double y, double z, double vx, double vy, double vz, int velMode);

        void dust(int rgb, float size, double x, double y, double z);

        void fade(int from, int to, float size, double x, double y, double z);
    }

    private static final double TAU = Math.PI * 2;
    /** Hiz vektoru: normal hareket (dagilma + hiz ile olceklenir). */
    public static final int VEL_MOTION = 0;
    /** Hiz vektoru aslinda baslangic ofseti (enchant / portal): sadece yatay dagilma ile olceklenir. */
    public static final int VEL_OFFSET = 1;
    /** Hiz vektoru veri tasir (nota rengi): hic dokunulmaz. */
    public static final int VEL_DATA = 2;

    public double x, y, z;
    /** Yatay ileri yon. */
    public double fx, fz;
    /** Animasyon zamani (tick). */
    public double t;
    double prevT = -1;
    public boolean moving;
    public double speed;
    public Random r = new Random();
    float density = 1f;
    double spread = 1, speedMul = 1;
    Sink sink;

    public double rd(double a, double b) {
        return a + r.nextDouble() * (b - a);
    }

    void time(double now, double step) {
        t = now;
        prevT = now - step;
    }

    public boolean every(int n) {
        return crossed(n, 0);
    }

    public boolean crossed(double period, double offset) {
        return Math.floor((t - offset) / period) != Math.floor((prevT - offset) / period);
    }

    /** Yerel ofset: yan (sag +), yukari, ileri (+) */
    public double[] local(double side, double up, double fwd) {
        double sx = -fz, sz = fx;
        return new double[]{side * sx + fwd * fx, up, side * sz + fwd * fz};
    }

    static int velMode(String id) {
        return switch (id) {
            case "note" -> VEL_DATA;
            case "enchant", "portal", "reverse_portal", "nautilus" -> VEL_OFFSET;
            default -> VEL_MOTION;
        };
    }

    /** Yogunluk: 1'den kucukse bazi parcaciklar atlanir, buyukse ikinci kopya. */
    private int count() {
        if (density < 1f) return r.nextFloat() > density ? 0 : 1;
        return density > 1f && r.nextFloat() < density - 1f ? 2 : 1;
    }

    private void emit(String id, double dx, double dy, double dz, double vx, double vy, double vz, int mode) {
        if (sink == null) return;
        dx *= spread;
        dz *= spread;
        if (mode == VEL_MOTION) {
            double h = spread * speedMul;
            vx *= h;
            vz *= h;
            vy *= speedMul;
        } else if (mode == VEL_OFFSET) {
            vx *= spread;
            vz *= spread;
        }
        int n = count();
        for (int i = 0; i < n; i++) {
            double jx = i == 0 ? 0 : rd(-0.08, 0.08) * spread, jz = i == 0 ? 0 : rd(-0.08, 0.08) * spread;
            sink.particle(id, x + dx + jx, y + dy, z + dz + jz, vx, vy, vz, mode);
        }
    }

    public void p(String id, double dx, double dy, double dz) {
        emit(id, dx, dy, dz, 0, 0, 0, VEL_MOTION);
    }

    public void pv(String id, double dx, double dy, double dz, double vx, double vy, double vz) {
        emit(id, dx, dy, dz, vx, vy, vz, velMode(id));
    }

    public void dust(int rgb, float size, double dx, double dy, double dz) {
        if (sink == null) return;
        int n = count();
        for (int i = 0; i < n; i++)
            sink.dust(rgb, Math.max(0.05f, Math.min(4f, size)), x + dx * spread + (i == 0 ? 0 : rd(-0.08, 0.08)), y + dy, z + dz * spread + (i == 0 ? 0 : rd(-0.08, 0.08)));
    }

    /** Sunucu toz parcacigina hiz veremez (istemci rastgeleler); hareketli toz yerinde kalir. */
    public void dustV(int rgb, float size, double dx, double dy, double dz, double vx, double vy, double vz) {
        dust(rgb, size, dx, dy, dz);
    }

    public void fade(int a, int b, float size, double dx, double dy, double dz) {
        if (sink == null || count() == 0) return;
        sink.fade(a, b, Math.max(0.05f, Math.min(4f, size)), x + dx * spread, y + dy, z + dz * spread);
    }

    /** Yururken arkada, ayak/bel hizasinda iz noktasi. */
    public double[] behind(double up) {
        double back = rd(0.35, 0.6);
        return new double[]{-fx * back + rd(-0.2, 0.2), up, -fz * back + rd(-0.2, 0.2)};
    }

    public void ring(String id, double radius, double up, int points, double phase) {
        for (int i = 0; i < points; i++) {
            double a = phase + i * TAU / points;
            p(id, Math.cos(a) * radius, up, Math.sin(a) * radius);
        }
    }

    public void dustRing(int rgb, float size, double radius, double up, int points, double phase) {
        for (int i = 0; i < points; i++) {
            double a = phase + i * TAU / points;
            dust(rgb, size, Math.cos(a) * radius, up, Math.sin(a) * radius);
        }
    }
}
