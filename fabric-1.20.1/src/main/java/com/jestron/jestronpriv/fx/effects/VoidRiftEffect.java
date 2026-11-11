package com.jestron.jestronpriv.fx.effects;

import com.jestron.jestronpriv.fx.FxSession;
import com.jestron.jestronpriv.fx.api.Transformation;
import com.jestron.jestronpriv.fx.api.ItemDisplay;
import com.jestron.jestronpriv.fx.FxKit;
import com.jestron.jestronpriv.fx.KillEffect;
import com.jestron.jestronpriv.fx.P;
import com.jestron.jestronpriv.fx.api.Color;
import com.jestron.jestronpriv.fx.api.Location;
import com.jestron.jestronpriv.fx.api.Material;
import com.jestron.jestronpriv.fx.api.BlockDisplay;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static com.jestron.jestronpriv.fx.FxMath.*;

/** Havada dikey bir boyut yirtigi acilir, obsidyen parcalarini icine ceker ve kapanir. */
public final class VoidRiftEffect extends KillEffect {

    private static final int SLICES = 9;
    private static final double H = 2.6, W = 1.0, BOTTOM = 0.25;
    private static final Color RIM = Color.fromRGB(170, 60, 255);

    public VoidRiftEffect() {
        super("boyut_yirtigi", "&5&lBoyut Yırtığı", Material.CRYING_OBSIDIAN, true,
                "Havada bir boyut yırtığı açılır,",
                "obsidyen parçalarını içine çeker.");
    }

    private static double open(int t) {
        if (t < 2) return 0;
        if (t <= 10) return easeOutBack((t - 2) / 8.0);
        if (t <= 32) return 1 + 0.06 * Math.sin(t * 0.7);
        if (t <= 38) return 1 - easeIn((t - 32) / 6.0);
        return 0;
    }

    private static double sliceWidth(int i) {
        return W * Math.pow(Math.sin(Math.PI * (i + 0.5) / SLICES), 0.8);
    }

    @Override
    public void play(FxSession s) {
        playBase(s);
        extras(s);
    }

    private void playBase(FxSession s) {
        final Quaternionf face = s.face();
        final double sh = H / SLICES;
        for (int i = 0; i < SLICES; i++) {
            final double yc = BOTTOM + sh * (i + 0.5);
            final double w = sliceWidth(i);
            BlockDisplay rim = s.bright(s.block(Material.PURPLE_CONCRETE, cube(rot(face, v(0, yc, -0.035)), face, 0.001f)));
            BlockDisplay core = s.dark(s.block(Material.BLACK_CONCRETE, cube(rot(face, v(0, yc, 0)), face, 0.001f)));
            s.anim(rim, 2, 40, 2, t -> {
                double o = open(t);
                return cube(rot(face, v(0, yc, -0.035)), face, v(Math.max(0.001, (w + 0.14) * o), sh * Math.min(1, o * 3) + 0.01, 0.03));
            });
            s.anim(core, 2, 40, 2, t -> {
                double o = open(t);
                return cube(rot(face, v(0, yc, 0)), face, v(Math.max(0.001, w * o), sh * Math.min(1, o * 3) + 0.005, 0.05));
            });
        }

        // Kenar parcaciklari
        final Vector3f right = s.local(1, 0, 0);
        s.every(4, 36, 2, t -> {
            double o = open(t);
            for (int i = 0; i < SLICES; i++) {
                double yc = BOTTOM + sh * (i + 0.5);
                double half = (sliceWidth(i) + 0.14) * o / 2;
                for (int sgn = -1; sgn <= 1; sgn += 2) {
                    Location l = s.loc(right.x * half * sgn, yc, right.z * half * sgn);
                    s.dust(l, RIM, 0.7f, 1, 0.02);
                }
            }
        });
        final Location center = s.loc(0, BOTTOM + H / 2, 0);
        s.every(2, 8, 1, t -> {
            for (int i = 0; i < 5; i++) {
                Vector3f d = randomUnit(s.rnd).mul(2.0f);
                s.particle(P.PORTAL, center, 0, d.x, d.y, d.z, 1.0, null);
            }
        });
        s.every(10, 32, 1, t -> {
            for (int i = 0; i < 3; i++) {
                Vector3f d = randomUnit(s.rnd);
                Location from = center.clone().add(d.x * 2.2, d.y * 1.4, d.z * 2.2);
                s.dir(P.SQUID_INK, from, -d.x, -d.y * 0.6, -d.z, 0.1);
            }
        });

        // Ice cekilen obsidyen parcalari
        for (int i = 0; i < 10; i++) {
            final Material m = i % 2 == 0 ? Material.OBSIDIAN : Material.CRYING_OBSIDIAN;
            double a = s.rnd.nextDouble() * Math.PI * 2;
            double r = s.rd(2.5, 3.5);
            final Vector3f start = v(Math.cos(a) * r, s.rd(0.5, 3.0), Math.sin(a) * r);
            final Vector3f c = v(0, BOTTOM + H / 2, 0);
            final float size = (float) s.rd(0.2, 0.35);
            final Quaternionf r0 = mul(qY(s.rnd.nextDouble() * 6), qX(s.rnd.nextDouble() * 6));
            final Vector3f ax = randomUnit(s.rnd);
            final int st = 8 + i;
            final int dur = 16 + s.rnd.nextInt(6);
            BlockDisplay d = s.lit(s.block(m, cube(start, r0, 0.001f)));
            s.anim(d, st, Math.min(st + dur, 38), 2, t -> {
                double u = Math.min(1, (t - st) / (double) dur);
                double pull = Math.pow(1 - u, 1.5);
                Vector3f rel = new Vector3f(start).sub(c);
                Vector3f p = new Vector3f(c).add(rot(qY(u * 2.5), rel).mul((float) pull));
                float sc = (float) (size * (u < 0.15 ? u / 0.15 : (1 - Math.max(0, (u - 0.7) / 0.3))));
                return cube(p, mul(qAxis(t * 0.5, ax), r0), Math.max(0.001f, sc));
            });
            final int endT = Math.min(st + dur, 38) + 1;
            s.tfAt(endT, d, cube(c, mul(qAxis(endT * 0.5, ax), r0), 0.001f), 1);
        }

        s.sound("block.end_portal_frame.fill", 1.0f, 0.6f);
        s.at(3, () -> s.sound("entity.enderman.teleport", 0.9f, 0.5f));
        s.at(4, () -> s.longSound("block.portal.ambient", 0.7f, 0.6f));
        s.at(20, () -> s.sound(center, "entity.illusioner.mirror_move", 0.8f, 0.6f));
        s.at(38, () -> {
            s.sound(center, "entity.enderman.teleport", 1.0f, 0.7f);
            s.sound(center, "block.respawn_anchor.deplete", 0.8f, 1.2f);
            s.sound(center, "entity.generic.explode", 0.3f, 1.8f);
            s.particle(P.EXPLOSION, center, 1, 0, 0);
            s.dust(center, RIM, 1.1f, 30, 0.5);
            s.flash(center, RIM);
        });
    }

    /** Gercekcilik katmani: ek 3D detaylar, enkaz, sok dalgalari ve parlamalar. */
    private void extras(FxSession s) {

        final Quaternionf face = s.face();
        final double sh = H / SLICES;
        // Yirtigin kenarinda titreyen kristal dikenler
        for (int i = 0; i < SLICES; i++) {
            final double yc = BOTTOM + sh * (i + 0.5);
            final double w = sliceWidth(i);
            for (int sgn = -1; sgn <= 1; sgn += 2) {
                final int sg = sgn;
                final double len = s.rd(0.15, 0.32);
                final double tilt = s.rd(-0.5, 0.5);
                final Material m = (i + sgn + 3) % 3 == 0 ? Material.CRYING_OBSIDIAN : (i % 2 == 0 ? Material.PURPLE_STAINED_GLASS : Material.MAGENTA_STAINED_GLASS);
                BlockDisplay d = s.bright(s.block(m, cube(rot(face, v(0, yc, 0)), face, 0.001f)));
                s.anim(d, 2, 40, 2, t -> {
                    double o = open(t);
                    double edge = (w + 0.14) * o / 2;
                    double l = len * Math.min(1, o) * (1 + 0.15 * Math.sin(t * 0.8 + yc * 5));
                    Quaternionf q = mul(face, qZ(sg * tilt));
                    Vector3f c = rot(face, v(sg * (edge + l / 2), yc, -0.01));
                    return cube(c, q, v(Math.max(0.001, l), 0.06, 0.05));
                });
                s.tfAt(41, d, cube(rot(face, v(0, yc, 0)), face, 0.001f), 1);
            }
        }
        // Bosluktan uzanan kara dokunaclar
        final Vector3f center = v(0, BOTTOM + H / 2, 0);
        for (int k = 0; k < 6; k++) {
            final int kk = k;
            final double base = k * Math.PI * 2 / 6;
            double a10 = base + 0.4 * Math.sin(10 * 0.3 + kk);
            Vector3f d10 = s.local(Math.cos(a10), Math.sin(a10) * 0.9, 0.35 * Math.sin(10 * 0.25 + kk));
            BlockDisplay d = s.dark(s.block(Material.BLACK_CONCRETE, beam(center, new Vector3f(center).add(d10.mul(0.002f)), 0.001f)));
            s.anim(d, 10, 34, 2, t -> {
                double grow = Math.min(1, (t - 10) / 6.0) * (t > 30 ? Math.max(0.001, (34 - t) / 4.0) : 1);
                double a = base + 0.4 * Math.sin(t * 0.3 + kk);
                Vector3f dir = s.local(Math.cos(a), Math.sin(a) * 0.9, 0.35 * Math.sin(t * 0.25 + kk));
                Vector3f end = new Vector3f(center).add(dir.mul((float) (1.6 * Math.max(0.01, grow))));
                return beam(center, end, (float) (0.06 * grow + 0.001));
            });
            double aEnd = base + 0.4 * Math.sin(34 * 0.3 + kk);
            Vector3f dirEnd = s.local(Math.cos(aEnd), Math.sin(aEnd) * 0.9, 0.35 * Math.sin(34 * 0.25 + kk));
            s.tfAt(35, d, beam(center, new Vector3f(center).add(dirEnd.mul(0.002f)), 0.001f), 1);
        }
        FxKit.glow(s, Material.PURPLE_STAINED_GLASS, center, 1.5f, 4, 36, 0.07);
        FxKit.shockRing(s, BOTTOM + H / 2, 0.3, 3.4, 38, 7, Material.MAGENTA_STAINED_GLASS, 22, 0.14f, true);
    }
}
