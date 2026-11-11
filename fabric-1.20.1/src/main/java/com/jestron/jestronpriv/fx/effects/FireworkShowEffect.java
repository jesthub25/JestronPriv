package com.jestron.jestronpriv.fx.effects;

import com.jestron.jestronpriv.fx.FxKit;
import com.jestron.jestronpriv.fx.FxSession;
import com.jestron.jestronpriv.fx.api.ItemDisplay;
import org.joml.Quaternionf;
import com.jestron.jestronpriv.fx.KillEffect;
import com.jestron.jestronpriv.fx.P;
import com.jestron.jestronpriv.fx.api.Color;
import com.jestron.jestronpriv.fx.api.Location;
import com.jestron.jestronpriv.fx.api.Material;
import org.joml.Vector3f;

import static com.jestron.jestronpriv.fx.FxMath.*;

/** Parcaciklardan uc havai fisek: kure, halka ve kalp seklinde patlamalar. */
public final class FireworkShowEffect extends KillEffect {

    public FireworkShowEffect() {
        super("havai_fisek", "&a&lHavai Fişek Şov", Material.FIREWORK_ROCKET, true,
                "Üç havai fişek fırlar: küre,",
                "halka ve kalp şeklinde patlar.");
    }

    @Override
    public void play(FxSession s) {
        Vector3f b1 = v(0, 7.0, 0);
        Vector3f b2 = s.local(-1.8, 8.3, 0.6);
        Vector3f b3 = s.local(1.8, 6.5, -0.4);
        rocket(s, b1, 1, 8);
        rocket(s, b2, 7, 14);
        rocket(s, b3, 13, 20);

        s.at(8, () -> sphere(s, b1, new Color[]{Color.fromRGB(255, 60, 60), Color.fromRGB(255, 200, 40)}));
        s.at(14, () -> ring(s, b2, Color.fromRGB(60, 200, 255)));
        s.at(20, () -> heart(s, b3, Color.fromRGB(255, 90, 190)));
    }

    private void rocket(FxSession s, Vector3f burst, int from, int to) {
        final Vector3f start = v(burst.x * 0.3, 0.2, burst.z * 0.3);
        s.at(from, () -> s.sound(s.loc(start), "entity.firework_rocket.launch", 1.0f, s.rp(0.9f, 1.1f)));
        // 3D havai fisek roketi: ucu hareket yonune bakar, arkasinda kivilcim ve alev izi
        final Quaternionf rq = spriteToward(new Vector3f(burst).sub(start), 0.6);
        ItemDisplay rocket = s.bright(s.item(Material.FIREWORK_ROCKET, item(start, rq, 0.001f)));
        s.tfAt(from, rocket, item(start, rq, 0.9f), 0);
        for (int t = from; t < to; t++) {
            double u = (t - from + 1) / (double) (to - from);
            Vector3f p = lerp(start, burst, easeOut(u));
            final Location l = s.loc(p);
            s.tfAt(t + 1, rocket, item(p, mul(qAxis((t - from) * 0.6, new Vector3f(burst).sub(start)), rq), 0.9f), 1);
            s.at(t, () -> {
                s.particle(P.ELECTRIC_SPARK, l, 4, 0.05, 0.02);
                s.particle(P.FLAME, l, 2, 0.04, 0.01);
                s.particle(P.SMOKE, l, 2, 0.04, 0.01);
            });
        }
        s.tfAt(to, rocket, item(burst, mul(qAxis((to - 1 - from) * 0.6, new Vector3f(burst).sub(start)), rq), 0.001f), 1);
        s.removeAt(to + 1, rocket);
        FxKit.puff(s, s.loc(start), 8);
    }

    /** Patlamadan sonra yavasca dusen parlak simler ve citirti. */
    private void glitter(FxSession s, Location c, Color col, double radius) {
        int b = s.tick();
        for (int k = 2; k <= 16; k += 2) {
            final int kk = k;
            s.at(b + k, () -> {
                for (int i = 0; i < 10; i++) {
                    org.joml.Vector3f d = randomUnit(s.rnd);
                    Location l = c.clone().add(d.x * radius, d.y * radius * 0.7 - kk * 0.09, d.z * radius);
                    s.particle(i % 2 == 0 ? P.WAX_OFF : P.ELECTRIC_SPARK, l, 1, 0.05, 0);
                    if (i % 3 == 0) s.dust(l, col, 0.6f, 1, 0.05);
                }
            });
        }
        s.at(b + 5, () -> {
            for (int i = 0; i < 14; i++) {
                org.joml.Vector3f d = randomUnit(s.rnd);
                s.particle(P.CRIT, c.clone().add(d.x * radius * 0.8, d.y * radius * 0.8, d.z * radius * 0.8), 2, 0.1, 0.15);
            }
        });
    }

    private void burstFx(FxSession s, Location c, Color col, boolean large) {
        s.sound(c, large ? "entity.firework_rocket.large_blast" : "entity.firework_rocket.blast", 1.2f, 1.0f);
        s.flash(c, col);
        s.at(s.tick() + 6, () -> s.sound(c, "entity.firework_rocket.twinkle", 0.8f, 1.0f));
        s.particle(P.EXPLOSION, c, 1, 0, 0);
        glitter(s, c, col, large ? 2.2 : 1.8);
    }

    private void sphere(FxSession s, Vector3f c, Color[] cols) {
        Location cl = s.loc(c);
        burstFx(s, cl, cols[0], true);
        int n = 90;
        double golden = Math.PI * (3 - Math.sqrt(5));
        for (int frame = 0; frame < 3; frame++) {
            final double r = 0.8 + frame * 0.75;
            s.at(s.tick() + frame * 2, () -> {
                for (int i = 0; i < n; i++) {
                    double y = 1 - (i / (double) (n - 1)) * 2;
                    double rad = Math.sqrt(1 - y * y);
                    double th = golden * i;
                    Location l = s.loc(c.x + Math.cos(th) * rad * r, c.y + y * r, c.z + Math.sin(th) * rad * r);
                    s.dust(l, cols[i % 2], 1.1f, 1, 0);
                }
            });
        }
        for (int i = 0; i < 20; i++) s.dir(P.CRIT, cl, randomUnit(s.rnd), 0.6);
    }

    private void ring(FxSession s, Vector3f c, Color col) {
        Location cl = s.loc(c);
        burstFx(s, cl, col, false);
        Vector3f u = s.local(1, 0, 0);
        Vector3f w = rot(s.face(), rot(qX(0.5), v(0, 0, 1)));
        final Vector3f uu = u, ww = w;
        for (int frame = 0; frame < 3; frame++) {
            final double r = 1.2 + frame * 0.8;
            s.at(s.tick() + frame * 2, () -> {
                for (int i = 0; i < 36; i++) {
                    double a = i * Math.PI * 2 / 36;
                    Location l = s.loc(c.x + (uu.x * Math.cos(a) + ww.x * Math.sin(a)) * r,
                            c.y + (uu.y * Math.cos(a) + ww.y * Math.sin(a)) * r,
                            c.z + (uu.z * Math.cos(a) + ww.z * Math.sin(a)) * r);
                    s.dust(l, col, 1.1f, 1, 0);
                }
            });
        }
        s.dust(cl, Color.WHITE, 1.0f, 10, 0.2);
    }

    private void heart(FxSession s, Vector3f c, Color col) {
        Location cl = s.loc(c);
        burstFx(s, cl, col, true);
        Vector3f right = s.local(1, 0, 0);
        for (int frame = 0; frame < 3; frame++) {
            final double sc = 0.06 + frame * 0.03;
            s.at(s.tick() + frame * 2, () -> {
                for (int i = 0; i < 48; i++) {
                    double t = i * Math.PI * 2 / 48;
                    double x = 16 * Math.pow(Math.sin(t), 3);
                    double y = 13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t);
                    Location l = s.loc(c.x + right.x * x * sc, c.y + y * sc, c.z + right.z * x * sc);
                    s.dust(l, col, 1.1f, 1, 0);
                }
            });
        }
        s.particle(P.HEART, cl, 4, 0.4, 0);
    }
}
