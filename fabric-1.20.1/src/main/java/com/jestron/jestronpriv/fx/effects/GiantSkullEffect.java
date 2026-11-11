package com.jestron.jestronpriv.fx.effects;

import com.jestron.jestronpriv.fx.FxSession;
import com.jestron.jestronpriv.fx.api.Transformation;
import com.jestron.jestronpriv.fx.api.BlockDisplay;
import com.jestron.jestronpriv.fx.api.Color;
import com.jestron.jestronpriv.fx.FxKit;
import com.jestron.jestronpriv.fx.KillEffect;
import com.jestron.jestronpriv.fx.P;
import com.jestron.jestronpriv.fx.api.Location;
import com.jestron.jestronpriv.fx.api.Material;
import com.jestron.jestronpriv.fx.api.ItemDisplay;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

import static com.jestron.jestronpriv.fx.FxMath.*;

/** Yerden dev bir wither kafatasi yukselir, titrer ve kemik parcalarina patlar. */
public final class GiantSkullEffect extends KillEffect {

    private static final double TOP = 1.9;

    public GiantSkullEffect() {
        super("kafatasi", "&8&lDev Kafatası", Material.WITHER_SKELETON_SKULL, true,
                "Yerden dev bir kafatası yükselir",
                "ve kemiklere ayrılarak patlar.");
    }

    @Override
    public void play(FxSession s) {
        playBase(s);
        extras(s);
    }

    private void playBase(FxSession s) {
        ItemDisplay skull = s.lit(s.item(Material.WITHER_SKELETON_SKULL, item(v(0, -1.2, 0), s.face(), 0.001f)));
        s.anim(skull, 2, 34, 2, t -> {
            double y = t <= 14 ? lerp(-1.0, TOP, easeOut((t - 2) / 12.0)) : TOP + 0.08 * Math.sin(t * 0.4);
            double sc = 2.6 * Math.min(1, (t - 2) / 4.0 + 0.15);
            double jx = 0, jz = 0;
            if (t >= 28) {
                jx = (t % 4 == 0 ? 0.06 : -0.06);
                jz = (t % 4 == 2 ? 0.05 : -0.05);
            }
            return item(v(jx, y, jz), qY(s.faceYaw + 0.1 * (t - 2)), (float) sc);
        });
        s.tfAt(36, skull, item(v(0, TOP, 0), qY(s.faceYaw + 3.4), 0.001f), 1);

        s.every(2, 14, 3, t -> s.groundCrack(s.loc(0, 0.1, 0), 12, 0.6));
        s.every(8, 34, 2, t -> {
            for (int k = 0; k < 3; k++) {
                double a = t * 0.45 + k * Math.PI * 2 / 3;
                double y = 0.3 + ((t - 8) * 0.07 + k * 0.6) % 2.6;
                s.particle(P.SOUL_FIRE_FLAME, s.loc(Math.cos(a) * 1.3, y, Math.sin(a) * 1.3), 1, 0, 0);
            }
            if (t % 4 == 0) s.particle(P.SMOKE, s.loc(0, TOP, 0), 3, 0.6, 0.02);
            if (t % 6 == 0) s.particle(P.SCULK_SOUL, s.loc(0, TOP - 0.6, 0), 1, 0.5, 0.02);
        });

        s.sound("entity.wither_skeleton.ambient", 1.0f, 0.5f);
        s.at(8, () -> s.sound(s.loc(0, TOP, 0), "entity.wither.ambient", 0.6f, 0.6f));
        s.at(20, () -> s.sound(s.loc(0, TOP, 0), "entity.skeleton.ambient", 0.8f, 0.5f));
        s.at(30, () -> s.sound(s.loc(0, TOP, 0), "entity.wither.shoot", 0.6f, 0.5f));

        final Location c = s.loc(0, TOP, 0);
        s.at(36, () -> {
            s.sound(c, "entity.wither.break_block", 0.7f, 0.8f);
            s.sound(c, "block.bone_block.break", 1.0f, 0.6f);
            s.sound(c, "entity.skeleton.death", 0.8f, 0.6f);
            s.sound(c, "entity.generic.explode", 0.5f, 1.2f);
            s.particle(P.SOUL, c, 25, 0.7, 0.05);
            s.particle(P.LARGE_SMOKE, c, 15, 0.6, 0.03);
            s.particle(P.EXPLOSION, c, 2, 0.4, 0);
            s.crack(c, Material.BONE_BLOCK, 40, 0.6);
            for (int i = 0; i < 20; i++) s.dir(P.SOUL_FIRE_FLAME, c, randomUnit(s.rnd), 0.2);
        });

        // Kemikler
        for (int i = 0; i < 10; i++) {
            Vector3f dir = randomUnit(s.rnd);
            double sp = s.rd(0.15, 0.3);
            List<double[]> path = s.ballistic(dir.x * 0.4, TOP + dir.y * 0.4, dir.z * 0.4,
                    dir.x * sp, Math.abs(dir.y) * 0.25 + 0.1, dir.z * sp, 0.055, 0.06, 14);
            final Quaternionf r0 = mul(qY(s.rnd.nextDouble() * 6), qZ(s.rnd.nextDouble() * 6));
            final Vector3f ax = randomUnit(s.rnd);
            double[] p0 = path.get(0);
            ItemDisplay bone = s.lit(s.item(Material.BONE, item(v(p0[0], p0[1], p0[2]), r0, 0.001f)));
            s.tfAt(36, bone, item(v(p0[0], p0[1], p0[2]), r0, 0.8f), 0);
            Vector3f last = v(p0[0], p0[1], p0[2]);
            Quaternionf lastR = r0;
            double angle = 0;
            for (int k = 1; k < path.size() && 36 + k <= 43; k++) {
                double[] p = path.get(k);
                if (p[3] >= 1) {
                    // yere yatik
                    lastR = mul(qY(angle), qX(Math.PI / 2));
                } else {
                    angle += 0.5;
                    lastR = mul(qAxis(angle, ax), r0);
                }
                last = v(p[0], p[1], p[2]);
                s.tfAt(36 + k, bone, item(last, lastR, 0.8f), 1);
            }
            s.tfAt(44, bone, item(last, lastR, 0.001f), 4);
        }
    }

    /** Gercekcilik katmani: ek 3D detaylar, enkaz, sok dalgalari ve parlamalar. */
    private void extras(FxSession s) {

        // Kemik kafes: yerden yukselen kavisli kaburgalar
        for (int i = 0; i < 8; i++) {
            final double a = i * Math.PI * 2 / 8 + 0.2;
            final Vector3f p0 = v(Math.cos(a) * 1.15, 0, Math.sin(a) * 1.15);
            final Vector3f p1 = v(Math.cos(a) * 1.35, 1.1, Math.sin(a) * 1.35);
            final Vector3f p2 = v(Math.cos(a) * 0.85, 2.15, Math.sin(a) * 0.85);
            final Vector3f[][] segs = {{p0, p1}, {p1, p2}};
            for (int j = 0; j < 2; j++) {
                final Vector3f a1 = segs[j][0], b1 = segs[j][1];
                final Vector3f down = v(0, -2.4, 0);
                BlockDisplay d = s.lit(s.block(Material.BONE_BLOCK, beam(new Vector3f(a1).add(down), new Vector3f(b1).add(down), 0.14f)));
                final int st = 6 + (i % 4);
                s.anim(d, st, st + 8, 2, t -> {
                    double k = easeOut((t - st) / 8.0);
                    Vector3f off = v(0, -2.4 * (1 - k), 0);
                    return beam(new Vector3f(a1).add(off), new Vector3f(b1).add(off), 0.14f);
                });
                s.tfAt(36, d, beam(a1, new Vector3f(a1).add(new Vector3f(b1).sub(a1).mul(0.01f)), 0.001f), 2);
            }
        }
        // Ruh atesi halkasi
        for (int i = 0; i < 8; i++) {
            final double a = i * Math.PI * 2 / 8;
            final double fx = Math.cos(a) * 1.7, fz = Math.sin(a) * 1.7;
            final Vector3f c = v(fx, s.groundDy(fx, fz), fz);
            BlockDisplay f = s.bright(s.block(Material.SOUL_FIRE, cube(new Vector3f(c).add(0, 0.0005f, 0), qY(a), 0.001f)));
            final int k = i;
            s.anim(f, 8 + i % 3, 36, 4, t -> {
                float sz = (float) (0.65 * Math.min(1, (t - 8) / 4.0) * (0.9 + 0.25 * Math.abs(Math.sin(t * 0.7 + k))));
                return cube(new Vector3f(c).add(0, sz / 2, 0), qY(a), Math.max(0.001f, sz));
            });
            s.tfAt(37, f, cube(c, qY(a), 0.001f), 3);
        }
        FxKit.shockRing(s, 0.04, 0.4, 3.0, 36, 8, Material.LIGHT_BLUE_STAINED_GLASS, 18, 0.25f, true);
        FxKit.debris(s, v(0, TOP, 0), new Material[]{Material.BONE_BLOCK, Material.BONE_BLOCK, Material.SMOOTH_BASALT}, 8, 0.12, 0.28, 0.1, 0.3, 0.12f, 0.22f, 36, false);
    }
}
