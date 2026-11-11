package com.jestron.jestronpriv.fx.effects;

import com.jestron.jestronpriv.fx.FxKit;
import com.jestron.jestronpriv.fx.FxSession;
import com.jestron.jestronpriv.fx.KillEffect;
import com.jestron.jestronpriv.fx.P;
import com.jestron.jestronpriv.fx.api.BlockDisplay;
import com.jestron.jestronpriv.fx.api.Color;
import com.jestron.jestronpriv.fx.api.Location;
import com.jestron.jestronpriv.fx.api.Material;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static com.jestron.jestronpriv.fx.FxMath.*;

/**
 * Oldugu noktanin 1.5 blok ustunde kara delik: simsiyah donen cekirdek, mor olay ufku kabugu,
 * etrafinda donen 3D yigilma diski parcalari, yerden koparilip icine cekilen bloklar ve cokus patlamasi.
 */
public final class BlackHoleEffect extends KillEffect {

    private static final Color ORANGE = Color.fromRGB(255, 140, 30);
    private static final Color PURPLE = Color.fromRGB(120, 0, 200);
    private static final Color RIM = Color.fromRGB(150, 40, 230);
    private static final int COLLAPSE = 40;

    public BlackHoleEffect() {
        super("kara_delik", "&5&lKara Delik", Material.ECHO_SHARD, true,
                "Öldüğü yerin 1.5 blok üstünde",
                "her şeyi yutan bir kara delik açılır.");
    }

    private static double grow(int t) {
        if (t < 2) return 0.0;
        if (t <= 10) return easeOutBack((t - 2) / 8.0);
        if (t <= 34) return 1.0 + 0.05 * Math.sin(t * 0.8);
        if (t <= COLLAPSE) return Math.max(0, 1.0 - easeIn((t - 34) / 6.0));
        return 0.0;
    }

    @Override
    public void play(FxSession s) {
        final Vector3f c = v(0, 1.5, 0);
        final Location cl = s.loc(c);

        // --- Cekirdek: farkli eksenlerde donen 3 siyah kup = puruzsuz kure izlenimi
        final Quaternionf[] base = {qY(0.3), mul(qY(Math.PI / 4), qX(Math.PI / 4)), mul(qZ(Math.PI / 4), qX(Math.PI / 6))};
        final Vector3f[] axes = {v(0, 1, 0), v(1, 0.3, 0), v(0.2, 0.4, 1)};
        final double[] omega = {0.26, -0.22, 0.3};
        for (int i = 0; i < 3; i++) {
            final int k = i;
            BlockDisplay d = s.dark(s.block(Material.BLACK_CONCRETE, cube(c, base[k], 0.001f)));
            s.anim(d, 2, COLLAPSE, 2, t -> {
                double spin = omega[k] * t + (t > 34 ? omega[k] * 1.2 * (t - 34) : 0.0);
                return cube(c, mul(qAxis(spin, axes[k]), base[k]), (float) Math.max(0.001, grow(t)));
            });
        }

        // --- Olay ufku: mor ve siyah yari saydam kabuklar (parlayan halka hissi)
        BlockDisplay shellA = s.bright(s.block(Material.MAGENTA_STAINED_GLASS, cube(c, q(), 0.001f)));
        BlockDisplay shellB = s.bright(s.block(Material.PURPLE_STAINED_GLASS, cube(c, q(), 0.001f)));
        BlockDisplay shellC = s.block(Material.BLACK_STAINED_GLASS, cube(c, q(), 0.001f));
        s.dark(shellC);
        s.anim(shellA, 4, COLLAPSE, 2, t -> cube(c, mul(qY(-t * 0.21), qX(0.7)), (float) Math.max(0.001, 1.42 * grow(t))));
        s.anim(shellB, 4, COLLAPSE, 2, t -> cube(c, mul(qY(t * 0.17), qZ(0.5)), (float) Math.max(0.001, 1.28 * grow(t))));
        s.anim(shellC, 4, COLLAPSE, 2, t -> cube(c, mul(qX(t * 0.19), qY(0.4)), (float) Math.max(0.001, 1.14 * grow(t))));

        // --- 3D yigilma diski: egik duzlemde donen kizgin parcalar
        final Quaternionf disk = mul(s.face(), qX(0.4));
        final Vector3f u = rot(disk, v(1, 0, 0));
        final Vector3f w = rot(disk, v(0, 0, 1));
        Material[] dm = {Material.MAGMA_BLOCK, Material.ORANGE_STAINED_GLASS, Material.PURPLE_STAINED_GLASS, Material.SHROOMLIGHT, Material.OBSIDIAN, Material.MAGENTA_STAINED_GLASS};
        for (int i = 0; i < 16; i++) {
            final double a0 = i * Math.PI * 2 / 16 + s.rd(-0.15, 0.15);
            final double r0 = s.rd(1.25, 1.95);
            final double sp = 0.42 / Math.sqrt(r0);
            final float size = (float) s.rd(0.1, 0.22);
            final double yOff = s.rd(-0.06, 0.06);
            final Quaternionf rq = mul(qY(s.rnd.nextDouble() * 6), qX(s.rnd.nextDouble() * 6));
            final Vector3f ax = randomUnit(s.rnd);
            Material m = dm[i % dm.length];
            BlockDisplay d = s.block(m, cube(c, mul(qAxis(6 * 0.45, ax), rq), 0.001f));
            if (m == Material.OBSIDIAN) s.lit(d);
            else s.bright(d);
            s.anim(d, 6, COLLAPSE, 2, t -> {
                double g = Math.min(1, grow(t) + 0.0001);
                double spiral = t > 30 ? Math.max(0.05, 1 - (t - 30) / 10.0) : 1;
                double r = r0 * spiral * Math.min(1, (t - 4) / 6.0);
                double a = a0 + sp * t * (t > 30 ? 1.6 : 1);
                Vector3f p = new Vector3f(c).add(new Vector3f(u).mul((float) (Math.cos(a) * r)))
                        .add(new Vector3f(w).mul((float) (Math.sin(a) * r))).add(0, (float) yOff, 0);
                return cube(p, mul(qAxis(t * 0.45, ax), rq), (float) Math.max(0.001, size * g));
            });
        }

        // --- Yerden koparilip spiral cizerek iceri cekilen zemin bloklari
        Material gm = s.groundMaterial(Material.DIRT);
        Material[] pulled = {gm, Material.GRAVEL, gm, Material.COBBLESTONE, gm, Material.DIRT};
        for (int i = 0; i < 6; i++) {
            final double a = i * Math.PI * 2 / 6 + 0.3;
            final double r = s.rd(1.6, 2.4);
            final double gx = Math.cos(a) * r, gz = Math.sin(a) * r;
            final double gy = s.groundDy(gx, gz) + 0.12;
            final float size = (float) s.rd(0.22, 0.34);
            final int st = 10 + i * 2;
            final int dur = 18;
            final Quaternionf rq = qY(s.rnd.nextDouble() * 6);
            final Vector3f ax = randomUnit(s.rnd);
            BlockDisplay d = s.lit(s.block(pulled[i], cube(v(gx, gy, gz), rq, 0.001f)));
            s.tfAt(st, d, cube(v(gx, gy, gz), rq, size), 2);
            final int end = Math.min(st + dur, COLLAPSE - 2);
            s.anim(d, st + 2, end, 2, t -> {
                double k = (t - st) / (double) dur;
                double pull = Math.pow(1 - k, 1.4);
                double ang = a + k * 3.2;
                double rr = r * pull;
                double y = lerp(gy, c.y, easeInOut(k));
                float sc = (float) (size * (k > 0.75 ? Math.max(0.001, (1 - k) / 0.25) : 1));
                return cube(v(Math.cos(ang) * rr, y, Math.sin(ang) * rr), mul(qAxis((t - st) * 0.5, ax), rq), Math.max(0.001f, sc));
            });
            s.tfAt(end + 1, d, cube(c, mul(qAxis((end - st) * 0.5, ax), rq), 0.001f), 1);
            final Location gl = s.loc(gx, gy, gz);
            s.at(st, () -> {
                s.groundCrack(gl, 8, 0.15);
                s.sound(gl, "block.gravel.break", 0.6f, 0.6f);
            });
        }

        // --- Yigilma diski parcacik izi + olay ufku halkasi
        final Vector3f view = s.local(0, 0, 1);
        final Vector3f ru = s.local(1, 0, 0);
        s.every(4, 38, 2, t -> {
            double g = Math.min(1.0, grow(t));
            for (int ring = 0; ring < 2; ring++) {
                int n = ring == 0 ? 20 : 26;
                double r = (ring == 0 ? 1.15 : 1.6) * g;
                double phase = t * (ring == 0 ? 0.32 : 0.2);
                for (int i = 0; i < n; i++) {
                    double a = i * Math.PI * 2 / n + phase;
                    Vector3f p = new Vector3f(c).add(new Vector3f(u).mul((float) (Math.cos(a) * r))).add(new Vector3f(w).mul((float) (Math.sin(a) * r)));
                    s.dustT(s.loc(p), ORANGE, PURPLE, 0.9f, 1, 0);
                }
            }
            double rr = 0.78 * grow(t);
            for (int i = 0; i < 22; i++) {
                double a = i * Math.PI * 2 / 22 + t * 0.1;
                Vector3f p = new Vector3f(c).add(new Vector3f(ru).mul((float) (Math.cos(a) * rr))).add(0, (float) (Math.sin(a) * rr), 0);
                s.dust(s.loc(p).add(view.x * 0.05, view.y * 0.05, view.z * 0.05), RIM, 0.7f, 1, 0);
            }
        });

        // --- Iceri akan madde
        s.every(2, 9, 1, t -> {
            for (int i = 0; i < 6; i++) {
                Vector3f d = randomUnit(s.rnd).mul(2.3f);
                s.particle(P.PORTAL, cl, 0, d.x, d.y, d.z, 1.0, null);
            }
        });
        s.every(6, 34, 1, t -> {
            for (int i = 0; i < 4; i++) {
                Vector3f d = randomUnit(s.rnd);
                Location from = cl.clone().add(d.x * 2.4, d.y * 2.4, d.z * 2.4);
                s.dir(i % 2 == 0 ? P.SQUID_INK : P.DRAGON_BREATH, from, -d.x, -d.y, -d.z, 0.11);
            }
        });

        // --- Sesler
        s.sound("entity.warden.sonic_charge", 0.7f, 0.5f);
        s.at(2, () -> s.longSound("block.portal.ambient", 0.8f, 0.5f));
        s.at(14, () -> s.longSound("block.beacon.ambient", 0.9f, 0.5f));
        s.at(24, () -> s.sound("entity.enderman.teleport", 0.5f, 0.5f));
        s.at(34, () -> s.sound("block.beacon.deactivate", 1.0f, 0.5f));

        // --- Cokus patlamasi
        s.at(COLLAPSE, () -> {
            s.sound("entity.generic.explode", 0.7f, 0.5f);
            s.sound("block.respawn_anchor.deplete", 1.0f, 0.7f);
            s.sound("entity.enderman.teleport", 0.8f, 0.5f);
            s.flash(cl, PURPLE);
            s.particle(P.EXPLOSION, cl, 2, 0.3, 0);
            s.dust(cl, RIM, 1.2f, 30, 0.6);
            for (int i = 0; i < 22; i++) s.dir(P.SQUID_INK, cl, randomUnit(s.rnd), 0.35);
            FxKit.sparks(s, cl, 18, 0.45);
        });
        FxKit.shockRing(s, c.y, 0.3, 3.8, COLLAPSE, 6, Material.MAGENTA_STAINED_GLASS, 24, 0.12f, true);
        FxKit.shockRing(s, 0.04, 0.4, 3.0, COLLAPSE, 6, Material.PURPLE_STAINED_GLASS, 20, 0.18f, true);
    }
}
