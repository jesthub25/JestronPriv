package com.jestron.jestronpriv.fx.effects;

import com.jestron.jestronpriv.fx.FxBuild;
import com.jestron.jestronpriv.fx.FxBuild.Pose;
import com.jestron.jestronpriv.fx.FxKit;
import com.jestron.jestronpriv.fx.FxSession;
import com.jestron.jestronpriv.fx.KillEffect;
import com.jestron.jestronpriv.fx.P;
import com.jestron.jestronpriv.fx.api.Color;
import com.jestron.jestronpriv.fx.api.Location;
import com.jestron.jestronpriv.fx.api.Material;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static com.jestron.jestronpriv.fx.FxBuild.*;
import static com.jestron.jestronpriv.fx.FxMath.*;
/** Karanlik bir dumandan yarasa surusu cikar, kurbanin etrafinda sarmal cizip goge dagilir. */
public final class BatSwarmEffect extends KillEffect {

    public BatSwarmEffect() {
        super("yarasa_surusu", "&8&lYarasa Sürüsü", Material.BLACK_WOOL, true,
                "Karanlık dumandan bir yarasa sürüsü",
                "çıkar, sarmal çizip göğe dağılır.");
    }

    private static final int BATS = 10;

    @Override
    public void play(FxSession s) {
        for (int b = 0; b < BATS; b++) {
            final double a0 = b * Math.PI * 2 / BATS;
            final double rMax = s.rd(1.0, 1.7);
            final double speed = s.rd(0.18, 0.26) * (b % 2 == 0 ? 1 : -1);
            final double climb = s.rd(0.07, 0.11);
            final double flapRate = s.rd(0.9, 1.1);
            final int st = 3 + b / 3;
            final Vector3f flee = v(Math.cos(a0) * 2.5, s.rd(1.5, 2.5), Math.sin(a0) * 2.5);
            // Govde pozu
            java.util.function.IntFunction<Vector3f> pos = t -> {
                double u = t - st;
                double r = Math.min(rMax, 0.2 + u * 0.12);
                double a = a0 + u * speed;
                Vector3f p = v(Math.cos(a) * r, 0.4 + u * climb + Math.sin(u * 0.9) * 0.08, Math.sin(a) * r);
                if (t > 32) p.add(new Vector3f(flee).mul((float) Math.pow(phase(t, 32, 42), 1.5)));
                return p;
            };
            java.util.function.IntFunction<Quaternionf> yaw = t -> {
                double u = t - st;
                double a = a0 + u * speed;
                return qY(-a + (speed > 0 ? -Math.PI / 2 : Math.PI / 2));
            };
            block(s, Material.BLACK_WOOL, LIT, st, 41, 1, 2, t -> Pose.of(pos.apply(t), yaw.apply(t), v(0.16, 0.14, 0.26)));
            block(s, Material.GRAY_WOOL, LIT, st, 41, 1, 2, t -> attach(pos.apply(t), yaw.apply(t), v(0, 0.1, -0.1), q(), v(0.12, 0.08, 0.08)));
            for (int w = -1; w <= 1; w += 2) {
                final int side = w;
                block(s, Material.BLACK_CONCRETE, DARK, st, 41, 1, 2, t -> {
                    double flap = Math.sin(t * flapRate) * 0.6;
                    Quaternionf body = yaw.apply(t);
                    Quaternionf wing = qZ(side * flap);
                    Vector3f off = rot(wing, v(side * 0.2, 0, 0));
                    return attach(pos.apply(t), body, new Vector3f(off).add(0, 0.02f, 0), wing, v(0.36, 0.02, 0.2));
                });
            }
        }
        // Parcaciklar: karanlik duman ve kanat tozu
        s.every(2, 10, 1, t -> {
            s.particle(P.SQUID_INK, s.loc(0, 0.5, 0), 4, 0.35, 0.03);
            s.particle(P.LARGE_SMOKE, s.loc(0, 0.3, 0), 2, 0.3, 0.02);
        });
        s.every(10, 34, 3, t -> s.particle(P.SMOKE, s.loc(s.rd(-1.4, 1.4), s.rd(0.6, 2.8), s.rd(-1.4, 1.4)), 2, 0.1, 0.01));
        s.at(33, () -> s.particle(P.SQUID_INK, s.loc(0, 2.6, 0), 14, 1.0, 0.08));
        // Ses tasarimi
        s.sound("entity.illusioner.cast_spell", 0.6f, 0.6f);
        s.every(3, 12, 3, t -> s.sound("entity.bat.takeoff", 0.8f, s.rp(0.8f, 1.2f)));
        s.at(8, () -> s.longSound("entity.bat.loop", 0.5f, 1.0f));
        s.every(14, 32, 4, t -> s.sound("entity.bat.ambient", 0.6f, s.rp(0.8f, 1.3f)));
        s.at(33, () -> {
            s.sound("entity.bat.takeoff", 1.0f, 0.9f);
            s.sound("entity.bat.takeoff", 0.8f, 1.2f);
        });
    }
}
