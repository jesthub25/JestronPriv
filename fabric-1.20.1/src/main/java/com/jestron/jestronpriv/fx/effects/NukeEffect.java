package com.jestron.jestronpriv.fx.effects;

import com.jestron.jestronpriv.fx.FxSession;
import org.joml.Vector3f;
import org.joml.Quaternionf;
import com.jestron.jestronpriv.fx.api.Transformation;
import com.jestron.jestronpriv.fx.api.ItemDisplay;
import com.jestron.jestronpriv.fx.FxKit;
import com.jestron.jestronpriv.fx.KillEffect;
import com.jestron.jestronpriv.fx.P;
import com.jestron.jestronpriv.fx.api.Color;
import com.jestron.jestronpriv.fx.api.Location;
import com.jestron.jestronpriv.fx.api.Material;
import com.jestron.jestronpriv.fx.api.BlockDisplay;

import static com.jestron.jestronpriv.fx.FxMath.*;

/** Kucuk bir TNT duser; parlama, ates topu, mantar bulutu ve yerde sok dalgasi. */
public final class NukeEffect extends KillEffect {

    private static final Color FIRE = Color.fromRGB(255, 140, 40);
    private static final Color YELLOW = Color.fromRGB(255, 220, 90);
    private static final Color SMOKE = Color.fromRGB(110, 100, 95);
    private static final Color DUSTC = Color.fromRGB(150, 120, 90);

    public NukeEffect() {
        super("atom", "&6&lAtom Bombası", Material.TNT, true,
                "Minik bir TNT düşer ve dev bir",
                "mantar bulutuyla patlar.");
    }

    private static double stemTop(int t) {
        return Math.min(5.0, Math.max(0, (t - 7) * 0.35));
    }

    @Override
    public void play(FxSession s) {
        playBase(s);
        extras(s);
    }

    private void playBase(FxSession s) {
        BlockDisplay tnt = s.bright(s.block(Material.TNT, cube(v(0, 8.35, 0), qY(0.3), 0.7f)));
        for (int t = 3; t <= 6; t++) {
            double u = (t - 2) / 4.0;
            s.tfAt(t, tnt, cube(v(0, 0.35 + 8 * (1 - u * u), 0), qY(0.3 + t * 0.4), 0.7f), 1);
        }
        s.removeAt(7, tnt);
        s.sound(s.loc(0, 8, 0), "entity.tnt.primed", 1.0f, 1.0f);

        final Location c = s.loc(0, 0.6, 0);
        s.at(7, () -> {
            s.flash(c, Color.WHITE);
            s.flash(s.loc(0, 2, 0), YELLOW);
            s.sound("entity.generic.explode", 1.6f, 0.5f);
            s.sound("item.mace.smash_ground_heavy", 0.8f, 0.5f);
            s.longSound("entity.lightning_bolt.thunder", 0.6f, 0.5f);
            s.particle(P.EXPLOSION_EMITTER, c, 1, 0, 0);
            for (int i = 0; i < 30; i++) {
                org.joml.Vector3f d = randomUnit(s.rnd);
                d.y = Math.abs(d.y);
                s.dir(P.FLAME, c, d, 0.3);
            }
        });
        // Ates topu
        s.every(7, 14, 1, t -> {
            double r = lerp(0.5, 1.8, (t - 7) / 7.0);
            for (int i = 0; i < 22; i++) {
                org.joml.Vector3f d = randomUnit(s.rnd);
                d.y = Math.abs(d.y);
                s.dust(s.loc(d.x * r, 0.4 + d.y * r, d.z * r), i % 2 == 0 ? FIRE : YELLOW, 1.3f, 1, 0.05);
            }
            s.particle(P.EXPLOSION, s.loc(0, 1, 0), 2, 0.8, 0);
        });
        // Yer sok dalgasi
        s.every(7, 16, 1, t -> {
            double r = 0.5 + (t - 7) * 0.5;
            for (int i = 0; i < 28; i++) {
                double a = i * Math.PI * 2 / 28;
                s.dust(s.loc(Math.cos(a) * r, 0.12, Math.sin(a) * r), DUSTC, 1.2f, 1, 0.05);
            }
            if (t == 7) {
                for (int i = 0; i < 28; i++) {
                    double a = i * Math.PI * 2 / 28;
                    s.dir(P.CLOUD, s.loc(Math.cos(a) * 0.5, 0.15, Math.sin(a) * 0.5), Math.cos(a), 0.01, Math.sin(a), 0.45);
                }
                s.groundCrack(s.loc(0, 0.1, 0), 80, 2.0);
            }
        });
        // Mantar govdesi
        s.every(8, 34, 2, t -> {
            double top = stemTop(t);
            for (double y = 0.3; y <= top; y += 0.45) {
                double r = s.rd(0.3, 0.55);
                double a = s.rnd.nextDouble() * Math.PI * 2;
                s.dust(s.loc(Math.cos(a) * r, y, Math.sin(a) * r), y < top - 1 ? SMOKE : FIRE, 1.3f, 1, 0.05);
                s.dust(s.loc(-Math.cos(a) * r, y, -Math.sin(a) * r), SMOKE, 1.2f, 1, 0.05);
            }
        });
        // Mantar basi (donen torus)
        s.every(14, 36, 2, t -> {
            double cyy = stemTop(t) + 0.6;
            double R = 1.4, rr = 0.55;
            double roll = t * 0.25;
            for (int ui = 0; ui < 16; ui++) {
                double u = ui * Math.PI * 2 / 16;
                for (int vi = 0; vi < 5; vi++) {
                    double vv = vi * Math.PI * 2 / 5 + roll;
                    double x = (R + rr * Math.cos(vv)) * Math.cos(u);
                    double z = (R + rr * Math.cos(vv)) * Math.sin(u);
                    double y = cyy + rr * Math.sin(vv);
                    Color col = Math.cos(vv) < 0 ? FIRE : SMOKE;
                    s.dust(s.loc(x, y, z), col, 1.3f, 1, 0.04);
                }
            }
        });
        s.at(14, () -> s.sound(s.loc(0, 3, 0), "entity.dragon_fireball.explode", 0.6f, 0.5f));
        s.at(20, () -> s.sound(s.loc(0, 2, 0), "block.fire.ambient", 0.8f, 0.6f));
    }

    /** Gercekcilik katmani: ek 3D detaylar, enkaz, sok dalgalari ve parlamalar. */
    private void extras(FxSession s) {

        // Ates topu: genisleyip sonen parlak cam katmanlar
        for (int i = 0; i < 7; i++) {
            final Vector3f off = randomUnit(s.rnd).mul((float) s.rd(0.1, 0.5));
            off.y = Math.abs(off.y);
            final Material m = i % 3 == 0 ? Material.YELLOW_STAINED_GLASS : (i % 3 == 1 ? Material.ORANGE_STAINED_GLASS : Material.SHROOMLIGHT);
            final float max = (float) s.rd(1.2, 2.0);
            final Quaternionf rq = mul(qY(s.rnd.nextDouble() * 6), qX(s.rnd.nextDouble() * 6));
            BlockDisplay d = s.bright(s.block(m, cube(v(0, 0.6, 0), mul(qY(7 * 0.2), rq), 0.001f)));
            s.anim(d, 7, 21, 2, t -> {
                double k = (t - 7) / 14.0;
                double sz = max * easeOut(Math.min(1, k * 1.8)) * (k > 0.55 ? Math.max(0.001, (1 - k) / 0.45) : 1);
                return cube(v(off.x * (1 + k), 0.6 + off.y * (1 + k * 2), off.z * (1 + k)), mul(qY(t * 0.2), rq), (float) Math.max(0.001, sz));
            });
            s.tfAt(22, d, cube(v(0, 1, 0), mul(qY(21 * 0.2), rq), 0.001f), 1);
        }
        // Mantar govdesi: yukselen duman yumaklari
        for (int k = 0; k < 7; k++) {
            final double y = 0.5 + k * 0.72;
            final float base = (float) (1.15 - k * 0.06);
            final Material m = k % 2 == 0 ? Material.LIGHT_GRAY_WOOL : Material.GRAY_WOOL;
            final Quaternionf rq = mul(qY(k * 1.3), qX(0.4));
            final int kk = k;
            BlockDisplay d = s.lit(s.block(m, cube(v(0, y, 0), mul(qY(8 * 0.08 + kk), rq), 0.001f)));
            s.anim(d, 8, 44, 2, t -> {
                double vis = clamp01((stemTop(t) - y + 0.6) / 0.8);
                double fade = t > 36 ? Math.max(0.001, 1 - (t - 36) / 8.0) : 1;
                double wob = 0.08 * Math.sin(t * 0.4 + kk);
                return cube(v(wob, y, -wob), mul(qY(t * 0.08 + kk), rq), (float) Math.max(0.001, base * vis * fade * (1 + 0.05 * Math.sin(t * 0.5 + kk))));
            });
            s.tfAt(45, d, cube(v(0, y, 0), mul(qY(44 * 0.08 + kk), rq), 0.001f), 1);
        }
        // Mantar basi: donen yun yumaklari (icte kizgin turuncu)
        for (int i = 0; i < 14; i++) {
            final double u0 = i * Math.PI * 2 / 14;
            final double ph = i * 0.7;
            final boolean inner = i % 3 == 0;
            final Material m = inner ? Material.ORANGE_WOOL : (i % 2 == 0 ? Material.WHITE_WOOL : Material.LIGHT_GRAY_WOOL);
            final float max = (float) (inner ? s.rd(0.75, 0.95) : s.rd(0.95, 1.25));
            final Quaternionf rq = mul(qY(s.rnd.nextDouble() * 6), qX(s.rnd.nextDouble() * 6));
            BlockDisplay d = s.block(m, cube(v(0, 3, 0), mul(qY(14 * 0.1), rq), 0.001f));
            if (inner) s.bright(d);
            else s.lit(d);
            s.anim(d, 14, 44, 2, t -> {
                double cy = stemTop(t) + 0.6;
                double grow = Math.min(1, (t - 14) / 6.0);
                double fade = t > 36 ? Math.max(0.001, 1 - (t - 36) / 8.0) : 1;
                double R = (inner ? 0.95 : 1.45) * (0.6 + 0.4 * grow);
                double roll = t * 0.12;
                double vv = roll + ph;
                double x = (R + 0.35 * Math.cos(vv)) * Math.cos(u0 + t * 0.03);
                double z = (R + 0.35 * Math.cos(vv)) * Math.sin(u0 + t * 0.03);
                double y = cy + 0.35 * Math.sin(vv);
                return cube(v(x, y, z), mul(qY(t * 0.1), rq), (float) Math.max(0.001, max * grow * fade));
            });
            s.tfAt(45, d, cube(v(0, 4, 0), mul(qY(44 * 0.1), rq), 0.001f), 1);
        }
        FxKit.impact(s, 1.4, 7, Color.fromRGB(255, 160, 60), true);
        FxKit.shockRing(s, 0.05, 0.5, 5.5, 7, 10, Material.LIGHT_GRAY_STAINED_GLASS, 22, 0.55f, false);
        FxKit.shockRing(s, 0.05, 0.4, 3.6, 7, 7, Material.ORANGE_STAINED_GLASS, 16, 0.3f, true);
    }
}
