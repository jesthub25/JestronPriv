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
/** Yerden bazalt bir volkan konisi yukselir, patlar, lav akar ve kizgin kayalar firlatir. */
public final class VolcanoEffect extends KillEffect {

    public VolcanoEffect() {
        super("volkan", "&6&lVolkan", Material.MAGMA_BLOCK, true,
                "Yerden bir volkan yükselir, patlar,",
                "lav akar ve kızgın kayalar fırlatır.");
    }

    private static final int ERUPT = 13;
    private static final double[] RING_R = {1.15, 0.85, 0.55};
    private static final double RING_H = 0.32;

    @Override
    public void play(FxSession s) {
        // Koni: uc kat, her katta donmus bloklar
        for (int ring = 0; ring < RING_R.length; ring++) {
            final int rr = ring;
            int n = 10 - ring * 2;
            for (int i = 0; i < n; i++) {
                final double a = i * Math.PI * 2 / n + ring * 0.3;
                final Material m = (i + ring) % 3 == 0 ? Material.BLACKSTONE : (i % 2 == 0 ? Material.BASALT : Material.SMOOTH_BASALT);
                final float sz = (float) (0.55 - ring * 0.08);
                block(s, m, LIT, 2 + ring * 2, 40, 1, 1, t -> {
                    double rise = smooth(phase(t, 2 + rr * 2, 8 + rr * 2));
                    double sink = t > 35 ? smooth(phase(t, 35, 41)) : 0;
                    double quake = t > ERUPT && t < ERUPT + 6 ? Math.sin(t * 3.1 + a) * 0.03 : 0;
                    double y = (rr + 0.5) * RING_H - (1 - rise + sink) * (rr + 1) * RING_H;
                    return Pose.of(v(Math.cos(a) * RING_R[rr] + quake, y, Math.sin(a) * RING_R[rr]), mul(qY(-a), qZ(0.35)), v(sz, RING_H + 0.08, sz));
                });
            }
        }
        // Krater: kizgin magma
        block(s, Material.MAGMA_BLOCK, BRIGHT, 8, 40, 1, 2, t -> {
            double sink = t > 35 ? smooth(phase(t, 35, 41)) : 0;
            double pulse = 1 + 0.06 * Math.sin(t * 1.3);
            return Pose.of(v(0, RING_R.length * RING_H - 0.18 - sink, 0), qY(t * 0.05), v(0.7 * pulse, 0.2, 0.7 * pulse));
        });
        // Lav akintilari: krater kenarindan asagi kayan turuncu seritler
        for (int i = 0; i < 5; i++) {
            final double a = i * Math.PI * 2 / 5 + 0.4;
            final int st = ERUPT + 1 + i;
            block(s, Material.ORANGE_CONCRETE, BRIGHT, st, 40, 1, 2, t -> {
                double k = smooth(phase(t, st, st + 12));
                double r = lerp(0.35, 1.25, k);
                double y = lerp(RING_R.length * RING_H - 0.05, 0.08, k);
                return Pose.of(v(Math.cos(a) * r, y, Math.sin(a) * r), mul(qY(-a), qZ(-0.6 * (1 - k))), v(0.45, 0.06, 0.16 + 0.1 * k));
            });
        }
        // Patlama: kizgin kayalar
        FxKit.debris(s, v(0, 1.0, 0), new Material[]{Material.MAGMA_BLOCK, Material.BLACKSTONE, Material.MAGMA_BLOCK, Material.BASALT},
                10, 0.08, 0.2, 0.45, 0.7, 0.12f, 0.24f, ERUPT, false);
        FxKit.debris(s, v(0, 1.0, 0), new Material[]{Material.MAGMA_BLOCK, Material.SHROOMLIGHT}, 6, 0.05, 0.15, 0.5, 0.8, 0.08f, 0.16f, ERUPT + 6, true);
        FxKit.cracks(s, 8, 1.8, Material.MAGMA_BLOCK, 6, 4, 2, 0.08);
        FxKit.scorch(s, 1.8, ERUPT);
        // Parcaciklar
        s.every(4, ERUPT - 1, 2, t -> s.particle(P.SMOKE, s.loc(0, 1.0, 0), 3, 0.2, 0.03));
        s.at(ERUPT, () -> {
            s.particle(P.EXPLOSION, s.loc(0, 1.2, 0), 2, 0.3, 0);
            s.particle(P.FLASH, s.loc(0, 1.2, 0), 1, 0, 0);
            FxKit.embers(s, s.loc(0, 1.1, 0), 18, 0.3);
        });
        s.every(ERUPT, 36, 1, t -> {
            s.particle(P.LAVA, s.loc(0, 1.0, 0), 2, 0.15, 0);
            s.dir(P.LARGE_SMOKE, s.loc(s.rd(-0.2, 0.2), 1.1, s.rd(-0.2, 0.2)), s.rd(-0.1, 0.1), 1, s.rd(-0.1, 0.1), 0.12);
            if (t % 2 == 0) s.dir(P.FLAME, s.loc(0, 1.0, 0), s.rd(-0.3, 0.3), 1, s.rd(-0.3, 0.3), s.rd(0.15, 0.3));
        });
        // Ses tasarimi
        s.sound("block.basalt.break", 0.9f, 0.6f);
        s.at(4, () -> s.sound("block.basalt.break", 0.8f, 0.7f));
        s.at(6, () -> s.sound("block.basalt.break", 0.8f, 0.8f));
        s.at(9, () -> s.longSound("block.lava.ambient", 0.9f, 0.8f));
        s.at(ERUPT, () -> {
            s.sound("entity.generic.explode", 1.0f, 0.65f);
            s.sound("entity.blaze.shoot", 0.9f, 0.5f);
            s.sound("item.firecharge.use", 0.8f, 0.6f);
        });
        s.every(ERUPT + 2, 34, 3, t -> s.sound("block.lava.pop", 0.7f, s.rp(0.7f, 1.2f)));
        s.at(35, () -> s.sound("block.fire.extinguish", 0.8f, 0.7f));
    }
}
