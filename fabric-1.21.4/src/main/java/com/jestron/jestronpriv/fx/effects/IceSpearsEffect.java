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

/** Yerden sekiz buz mizragi kurbana dogru firlar, onu dondurur ve paramparca olur. */
public final class IceSpearsEffect extends KillEffect {

    public IceSpearsEffect() {
        super("buz_mizrak", "&b&lBuz Mızrakları", Material.PACKED_ICE, true,
                "Yerden fırlayan buz mızrakları",
                "kurbanı dondurur ve parçalanır.");
    }

    private static final int N = 8, HIT = 12, SHATTER = 26;
    private static final Color FROST = Color.fromRGB(190, 235, 255);

    @Override
    public void play(FxSession s) {
        final Vector3f core = v(0, 1.0, 0);
        for (int i = 0; i < N; i++) {
            final double a = i * Math.PI * 2 / N + 0.2;
            final double gy = s.groundDy(Math.cos(a) * 2.1, Math.sin(a) * 2.1);
            final Vector3f base = v(Math.cos(a) * 2.1, gy - 0.2, Math.sin(a) * 2.1);
            final Vector3f dir = new Vector3f(core).sub(base).normalize();
            final Material m = i % 2 == 0 ? Material.PACKED_ICE : Material.BLUE_ICE;
            final int st = 3 + (i % 4);
            block(s, m, LIT, st, SHATTER, 1, 2, t -> {
                double k = easeOutBack(phase(t, st, HIT));
                float len = (float) (0.4 + 1.85 * Math.min(1.05, k));
                Vector3f tip = new Vector3f(base).add(new Vector3f(dir).mul(len));
                return beamPose(base, tip, 0.16f);
            });
            // ucta parlak buz kristali
            block(s, Material.LIGHT_BLUE_STAINED_GLASS, BRIGHT, st + 2, SHATTER, 1, 2, t -> {
                double k = easeOutBack(phase(t, st, HIT));
                float len = (float) (0.4 + 1.85 * Math.min(1.05, k));
                Vector3f tip = new Vector3f(base).add(new Vector3f(dir).mul(len));
                return Pose.of(tip, mul(fromTo(v(0, 1, 0), dir), qY(t * 0.2)), v(0.1, 0.32, 0.1));
            });
            s.at(st, () -> s.crack(s.loc(base.x, gy + 0.1, base.z), Material.PACKED_ICE, 8, 0.2));
        }
        s.every(3, 7, 1, t -> s.sound("block.glass.place", 0.7f, s.rp(1.4f, 1.9f)));
        // donma
        s.at(HIT, () -> {
            Location c = s.loc(0, 1.0, 0);
            s.particle(P.SNOWFLAKE, c, 40, 0.7, 0.05);
            s.dust(c, FROST, 1.4f, 30, 0.6);
            s.sound("entity.player.hurt_freeze", 1.2f, 0.8f);
            s.sound("block.amethyst_block.resonate", 1.0f, 1.6f);
            s.sound("block.powder_snow.break", 1.0f, 0.7f);
        });
        FxKit.glow(s, Material.LIGHT_BLUE_STAINED_GLASS, core, 1.25f, HIT, SHATTER, 0.08);
        s.every(HIT + 1, SHATTER - 1, 3, t -> {
            s.particle(P.SNOWFLAKE, s.loc(0, 1.0, 0), 4, 0.9, 0.01);
            s.sound("block.amethyst_block.chime", 0.4f, s.rp(1.5f, 2.0f));
        });
        // parcalanma
        s.at(SHATTER, () -> {
            Location c = s.loc(0, 1.0, 0);
            s.particle(P.FLASH, c, 1, 0, 0);
            s.crack(c, Material.PACKED_ICE, 50, 1.0);
            s.crack(c, Material.BLUE_ICE, 30, 0.8);
            s.particle(P.ITEM_SNOWBALL, c, 30, 0.8, 0.2);
            s.particle(P.SNOWFLAKE, c, 40, 1.5, 0.1);
            s.sound("block.glass.break", 1.3f, 0.7f);
            s.sound("block.glass.break", 1.0f, 1.2f);
            s.sound("block.amethyst_block.break", 1.0f, 0.8f);
        });
        FxKit.debris(s, v(0, 1.0, 0), new Material[]{Material.PACKED_ICE, Material.BLUE_ICE, Material.ICE}, 12,
                0.12, 0.3, 0.15, 0.4, 0.1f, 0.24f, SHATTER, false);
        FxKit.shockRing(s, s.groundDy(0, 0) + 0.02, 0.3, 3.2, SHATTER, 8, Material.WHITE_STAINED_GLASS, 16, 0.18f, true);
        s.every(SHATTER + 2, 46, 3, t -> s.particle(P.SNOWFLAKE, s.loc(s.rd(-1.8, 1.8), s.rd(1.5, 2.8), s.rd(-1.8, 1.8)), 2, 0.2, 0));
    }
}
