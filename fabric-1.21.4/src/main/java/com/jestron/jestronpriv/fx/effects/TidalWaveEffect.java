package com.jestron.jestronpriv.fx.effects;

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

/** Kurbanin arkasindan dev bir dalga yukselir, ustune kivrilir ve kopuklu bir carpmayla yikilir. */
public final class TidalWaveEffect extends KillEffect {

    public TidalWaveEffect() {
        super("dev_dalga", "&9&lDev Dalga", Material.HEART_OF_THE_SEA, true,
                "Arkasından dev bir dalga yükselir,",
                "üstüne kıvrılır ve yıkılır.");
    }

    private static final int RISE = 13, CRASH = 21, END = 32, COLS = 9, ROWS = 4;
    private static final Color FOAM = Color.fromRGB(230, 248, 255), SEA = Color.fromRGB(40, 120, 220);

    /** Dalga kesiti: satir (0 dip .. ROWS-1 tepe) icin yerel (y, z) ve egim; zamanla yukselir, kivrilir, cokerken yayilir. */
    private static double[] section(int row, int t) {
        double rise = smooth(phase(t, 2, RISE));
        double curl = smooth(phase(t, RISE - 2, CRASH));
        double crash = smooth(phase(t, CRASH, END));
        double u = (row + 0.5) / ROWS;
        double h = 3.4 * rise;
        // dik duvar -> tepe one dogru kivrilir (cember yayi)
        double ang = u * curl * 2.2;
        double y = h * u * (1 - curl * 0.35 * u) * (1 - crash);
        double z = -2.6 + rise * 0.6 + Math.sin(ang) * h * 0.45 * u + crash * 2.6 * (0.5 + u);
        y = Math.max(0.05, y + Math.cos(ang) * 0.0);
        double tilt = -ang - crash * 1.2;
        return new double[]{y, z, tilt, 1 - crash * 0.6};
    }

    @Override
    public void play(FxSession s) {
        final Quaternionf face = s.face();
        final double gy = s.groundDy(0, 0);
        for (int c = 0; c < COLS; c++) {
            final double x = (c - (COLS - 1) / 2.0) * 0.55;
            for (int r = 0; r < ROWS; r++) {
                final int rr = r;
                final Material m = r == ROWS - 1 ? Material.WHITE_STAINED_GLASS : (r % 2 == 0 ? Material.BLUE_STAINED_GLASS : Material.LIGHT_BLUE_STAINED_GLASS);
                final double wob = c * 0.7 + r;
                block(s, m, BRIGHT, 2 + r, END, 1, 3, t -> {
                    double[] sec = section(rr, t);
                    double y = gy + sec[0] + Math.sin(t * 0.5 + wob) * 0.05;
                    Vector3f p = rot(face, v(x, 0, sec[1]));
                    p.y = (float) y;
                    float h = (float) (3.4 / ROWS * 1.15 * sec[3]);
                    return Pose.of(p, mul(face, qX(sec[2])), v(0.6, Math.max(0.02, h), 0.12));
                });
            }
        }
        // kopuk ve serpinti
        s.every(6, CRASH, 2, t -> {
            double[] top = section(ROWS - 1, t);
            for (int i = 0; i < 4; i++) {
                Vector3f p = rot(face, v(s.rd(-2.2, 2.2), 0, top[1]));
                s.particle("splash", s.loc(p.x, gy + top[0] + 0.3, p.z), 4, 0.2, 0.1);
            }
            s.dust(s.lloc(0, gy + top[0], top[1]), FOAM, 1.2f, 6, 1.4);
        });
        s.at(2, () -> {
            s.sound("ambient.underwater.enter", 1.0f, 0.6f);
            s.longSound("block.water.ambient", 1.2f, 0.6f);
        });
        s.at(RISE, () -> s.sound("entity.player.splash.high_speed", 0.8f, 0.5f));
        s.at(CRASH, () -> {
            Location c = s.loc(0, gy + 0.6, 0);
            s.stopSound("block.water.ambient");
            s.particle("splash", c, 120, 2.0, 0.6, 2.0, 0.4);
            s.particle(P.CLOUD, c, 20, 1.5, 0.1);
            s.dust(c, FOAM, 1.6f, 40, 1.8);
            s.dust(c, SEA, 1.4f, 30, 1.6);
            s.sound("entity.player.splash.high_speed", 1.3f, 0.6f);
            s.sound("entity.generic.splash", 1.2f, 0.5f);
            s.sound("entity.dolphin.splash", 1.0f, 0.7f);
            s.sound("item.bucket.empty", 1.0f, 0.6f);
        });
        FxKit.shockRing(s, gy + 0.03, 0.4, 3.8, CRASH, 10, Material.LIGHT_BLUE_STAINED_GLASS, 18, 0.22f, true);
        s.every(CRASH + 2, 46, 2, t -> {
            s.particle("splash", s.loc(s.rd(-2.5, 2.5), gy + 0.1, s.rd(-2.5, 2.5)), 3, 0.3, 0.05);
            if (t % 4 == 0) s.particle("bubble_pop", s.loc(s.rd(-2, 2), gy + 0.1, s.rd(-2, 2)), 2, 0.2, 0);
        });
        s.every(CRASH + 4, 40, 6, t -> s.sound("block.water.ambient", 0.6f, s.rp(1.0f, 1.4f)));
    }
}
