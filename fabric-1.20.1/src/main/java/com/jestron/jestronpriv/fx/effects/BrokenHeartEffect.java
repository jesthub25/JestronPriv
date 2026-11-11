package com.jestron.jestronpriv.fx.effects;

import com.jestron.jestronpriv.fx.FxSession;
import com.jestron.jestronpriv.fx.api.ItemDisplay;
import com.jestron.jestronpriv.fx.FxKit;
import com.jestron.jestronpriv.fx.KillEffect;
import com.jestron.jestronpriv.fx.P;
import com.jestron.jestronpriv.fx.api.Color;
import com.jestron.jestronpriv.fx.api.Location;
import com.jestron.jestronpriv.fx.api.Material;
import com.jestron.jestronpriv.fx.api.BlockDisplay;
import com.jestron.jestronpriv.fx.api.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

import static com.jestron.jestronpriv.fx.FxMath.*;

/** Voksel (3D piksel) bir kalp atar, catlar, ikiye ayrilir ve parcalara dagilir. */
public final class BrokenHeartEffect extends KillEffect {

    private static final String[] MAP = {
            "....X....",
            "...XXX...",
            "..XXXXX..",
            ".XXXXXXX.",
            "XXXXXXXXX",
            "XXXXXXXXX",
            "XXXX.XXXX",
            ".XX...XX."
    };
    private static final float VOX = 0.17f;
    private static final float DEPTH = 0.3f;

    public BrokenHeartEffect() {
        super("kirik_kalp", "&c&lKırık Kalp", Material.RED_DYE, true,
                "3D voksel bir kalp atar,",
                "çatlar ve paramparça olur.");
    }

    private static double beat(int t) {
        if (t < 2) return 0.001;
        if (t <= 8) return easeOutBack((t - 2) / 6.0);
        if (t >= 10 && t <= 14) return 1 + 0.15 * Math.sin((t - 10) / 4.0 * Math.PI);
        if (t >= 18 && t <= 22) return 1 + 0.15 * Math.sin((t - 18) / 4.0 * Math.PI);
        return 1.0;
    }

    @Override
    public void play(FxSession s) {
        playBase(s);
        extras(s);
    }

    private void playBase(FxSession s) {
        final Quaternionf face = s.face();
        final Vector3f C = v(0, 2.0, 0);
        final Vector3f pivot = v(0, (0 - 3.5) * VOX - VOX / 2, 0);

        for (int row = 0; row < MAP.length; row++) {
            for (int gx = 0; gx < 9; gx++) {
                if (MAP[row].charAt(gx) != 'X') continue;
                final int gy = row;
                final Vector3f L = v((gx - 4) * VOX, (gy - 3.5) * VOX, 0);
                Material m = Material.RED_CONCRETE;
                if ((gx == 1 && gy == 5) || (gx == 2 && gy == 6) || (gx == 1 && gy == 4)) m = Material.PINK_CONCRETE;
                if (gx == 2 && gy == 5) m = Material.WHITE_CONCRETE;
                if (gy == 0 || (gy == 1 && (gx == 3 || gx == 5))) m = Material.RED_TERRACOTTA;
                final boolean left = gx + (gy % 2) <= 4;
                BlockDisplay d = s.bright(s.block(m, voxel(face, C, L, 0.001)));

                // Belirme + kalp atislari
                for (int t = 2; t <= 22; t += 2) {
                    final double sc = beat(t);
                    s.tfAt(t, d, voxel(face, C, new Vector3f(L).mul((float) sc), sc), 2);
                }
                s.tfAt(24, d, voxel(face, C, L, 1.0), 2);

                // Ikiye ayrilma (alt uc etrafinda donerek)
                final float sign = left ? 1f : -1f;
                for (int t = 26; t <= 32; t += 2) {
                    double k = easeOut((t - 26) / 6.0);
                    Quaternionf r = qZ(sign * Math.toRadians(14) * k);
                    Vector3f local = new Vector3f(pivot).add(rot(r, new Vector3f(L).sub(pivot))).add((float) (-sign * 0.22 * k), 0, 0);
                    s.tfAt(t, d, voxelRot(face, C, local, r, 1.0), 2);
                }

                // Dagilma
                Quaternionf rEnd = qZ(sign * Math.toRadians(14));
                Vector3f localEnd = new Vector3f(pivot).add(rot(rEnd, new Vector3f(L).sub(pivot))).add(-sign * 0.22f, 0, 0);
                Vector3f world0 = new Vector3f(C).add(rot(face, localEnd));
                Vector3f outLocal = v(-sign * s.rd(0.05, 0.16), s.rd(0.05, 0.22), s.rd(-0.08, 0.12));
                Vector3f vel = rot(face, outLocal);
                List<double[]> path = s.ballistic(world0.x, world0.y, world0.z, vel.x, vel.y, vel.z, 0.05, VOX / 2, 16);
                Vector3f ax = randomUnit(s.rnd);
                double angle = 0;
                Vector3f lastP = world0;
                Quaternionf lastR = mul(face, rEnd);
                for (int k = 2; k < path.size() && 32 + k <= 42; k += 2) {
                    double[] p = path.get(k);
                    if (p[3] < 1) angle += 0.6;
                    lastP = v(p[0], p[1], p[2]);
                    lastR = mul(qAxis(angle, ax), face, rEnd);
                    s.tfAt(32 + k, d, cube(lastP, lastR, v(VOX, VOX, DEPTH)), 2);
                }
                s.tfAt(43, d, cube(lastP, lastR, 0.001f), 5);
            }
        }

        s.sound("entity.experience_orb.pickup", 0.8f, 1.2f);
        s.at(2, () -> s.particle(P.HEART, s.loc(0, 2.0, 0), 6, 0.6, 0));
        s.at(10, () -> {
            s.sound(s.loc(C), "entity.warden.heartbeat", 1.2f, 1.0f);
            s.particle(P.HEART, s.loc(0, 2.6, 0), 3, 0.5, 0);
        });
        s.at(18, () -> {
            s.sound(s.loc(C), "entity.warden.heartbeat", 1.2f, 1.0f);
            s.particle(P.HEART, s.loc(0, 2.6, 0), 3, 0.5, 0);
        });
        s.at(26, () -> {
            Location c = s.loc(C);
            s.sound(c, "block.glass.break", 1.0f, 0.7f);
            s.sound(c, "entity.item.break", 1.0f, 0.8f);
            for (int i = 0; i < 8; i++) {
                s.dust(s.loc(new Vector3f(C).add(local(face, (i % 2 == 0 ? 0 : -0.05), (i - 3.5) * VOX, 0.18))), Color.fromRGB(90, 0, 0), 1.0f, 1, 0);
            }
            s.particle(P.CRIT, c, 10, 0.3, 0.2);
        });
        s.at(32, () -> {
            s.sound(s.loc(C), "block.glass.break", 0.8f, 1.1f);
            s.particle(P.DAMAGE_INDICATOR, s.loc(C), 6, 0.4, 0.1);
        });
    }

    private static Vector3f local(Quaternionf face, double x, double y, double z) {
        return rot(face, v(x, y, z));
    }

    private static Transformation voxel(Quaternionf face, Vector3f C, Vector3f L, double sc) {
        Vector3f c = new Vector3f(C).add(rot(face, L));
        float sv = (float) (VOX * sc), sd = (float) (DEPTH * sc);
        return cube(c, face, v(sv, sv, sd));
    }

    private static Transformation voxelRot(Quaternionf face, Vector3f C, Vector3f L, Quaternionf r, double sc) {
        Vector3f c = new Vector3f(C).add(rot(face, L));
        float sv = (float) (VOX * sc), sd = (float) (DEPTH * sc);
        return cube(c, mul(face, r), v(sv, sv, sd));
    }

    /** Gercekcilik katmani: ek 3D detaylar, enkaz, sok dalgalari ve parlamalar. */
    private void extras(FxSession s) {

        final Quaternionf face = s.face();
        final Vector3f C = v(0, 2.0, 0);
        // Her kalp atisinda genisleyen pembe/kirmizi halkalar
        FxKit.shockRing(s, 1.25, 0.4, 2.6, 10, 6, Material.PINK_STAINED_GLASS, 18, 0.12f, true);
        FxKit.shockRing(s, 1.25, 0.4, 2.8, 18, 6, Material.RED_STAINED_GLASS, 18, 0.12f, true);
        // Kalbin kenarindan yayilan parlak dis hat
        java.util.List<Vector3f> edge = new java.util.ArrayList<>();
        for (int row = 0; row < MAP.length; row++) {
            for (int gx = 0; gx < 9; gx++) {
                if (MAP[row].charAt(gx) != 'X') continue;
                boolean border = gx == 0 || gx == 8 || row == 0 || row == MAP.length - 1
                        || MAP[row].charAt(gx - 1) != 'X' || MAP[row].charAt(gx + 1) != 'X'
                        || MAP[row - 1].charAt(gx) != 'X' || MAP[row + 1].charAt(gx) != 'X';
                if (border) edge.add(v((gx - 4) * VOX, (row - 3.5) * VOX, 0.17));
            }
        }
        s.every(8, 25, 2, t -> {
            double pulse = (t >= 10 && t <= 14) || (t >= 18 && t <= 22) ? 1.18 : 1.05;
            for (Vector3f e : edge) {
                Vector3f p = new Vector3f(C).add(rot(face, v(e.x * pulse, e.y * pulse, e.z)));
                s.dust(s.loc(p), Color.fromRGB(255, 120, 170), 0.6f, 1, 0.02);
            }
        });
        s.at(26, () -> FxKit.sparks(s, s.loc(C), 18, 0.35));
        s.at(32, () -> FxKit.dustRing(s, s.loc(0, 0.15, 0), 18, 0.3, 0.12));
    }
}
