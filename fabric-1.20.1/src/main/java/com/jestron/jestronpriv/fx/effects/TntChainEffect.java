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
/** Etrafta bir halka TNT yukselir, fitiller yanar ve zincirleme patlarlar. */
public final class TntChainEffect extends KillEffect {

    public TntChainEffect() {
        super("tnt_zinciri", "&c&lTNT Zinciri", Material.TNT, true,
                "Etrafta bir halka TNT yükselir,",
                "fitiller yanar, zincirleme patlarlar.");
    }

    private static final int N = 8;
    private static final double R = 1.8;
    private static final int FUSE = 8, FIRST = 20, GAP = 2;

    @Override
    public void play(FxSession s) {
        for (int i = 0; i < N; i++) {
            final double a = i * Math.PI * 2 / N;
            final int boom = FIRST + i * GAP;
            final int idx = i;
            final Vector3f c = v(Math.cos(a) * R, 0.3, Math.sin(a) * R);
            final Quaternionf q = qY(-a + 0.2);
            block(s, Material.TNT, LIT, 2 + i / 2, boom, 1, 1, t -> {
                double rise = easeOutBack(phase(t, 2 + idx / 2, 7 + idx / 2));
                double swell = t > boom - 3 ? 1 + (t - (boom - 3)) * 0.08 : 1;
                return Pose.of(v(c.x, -0.3 + 0.6 * rise, c.z), q, (float) (0.6 * swell));
            });
            // Fitil yanip sonme: beyaz parlak kabuk
            block(s, Material.WHITE_CONCRETE, BRIGHT, FUSE, boom, 1, 1, t -> {
                boolean on = ((t - FUSE) / 2) % 2 == 0;
                double swell = t > boom - 3 ? 1 + (t - (boom - 3)) * 0.08 : 1;
                return Pose.of(v(c.x, 0.3, c.z), q, on ? (float) (0.62 * swell) : 0.001f);
            });
            s.at(boom, () -> {
                Location l = s.loc(c.x, 0.4, c.z);
                s.particle(P.EXPLOSION, l, 2, 0.3, 0);
                s.particle(P.FLASH, l, 1, 0, 0);
                FxKit.embers(s, l, 6, 0.3);
                s.particle(P.LARGE_SMOKE, l, 6, 0.3, 0.05);
                s.sound(l, "entity.generic.explode", 0.75f, s.rp(0.85f, 1.15f));
            });
            FxKit.debris(s, v(c.x, 0.3, c.z), new Material[]{Material.TNT, Material.RED_CONCRETE, Material.COBBLESTONE}, 3,
                    0.08, 0.2, 0.25, 0.45, 0.08f, 0.16f, boom, false);
        }
        FxKit.scorch(s, 2.6, FIRST + 4);
        FxKit.shockRing(s, 0.05, 0.6, 3.6, FIRST + N * GAP - 2, 8, Material.ORANGE_STAINED_GLASS, 18, 0.3f, true);
        // Fitil kivilcimlari
        s.every(FUSE, FIRST, 1, t -> {
            for (int i = 0; i < N; i++) {
                double a = i * Math.PI * 2 / N;
                if (t < FIRST + i * GAP) s.particle(P.SMOKE, s.loc(Math.cos(a) * R, 0.65, Math.sin(a) * R), 1, 0.02, 0.01);
            }
        });
        s.at(FIRST + N * GAP, () -> s.particle(P.EXPLOSION_EMITTER, s.loc(0, 0.6, 0), 1, 0, 0));
        // Ses tasarimi
        s.sound("block.stone.break", 0.6f, 0.8f);
        s.at(FUSE, () -> s.sound("entity.tnt.primed", 1.0f, 1.0f));
        s.at(FUSE + 4, () -> s.sound("entity.tnt.primed", 0.8f, 1.2f));
        s.at(FIRST + N * GAP, () -> {
            s.sound("entity.generic.explode", 1.0f, 0.6f);
            s.sound("entity.dragon_fireball.explode", 0.6f, 0.8f);
        });
    }
}
