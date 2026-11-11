package com.jestron.jestronpriv.fx.effects;

import com.jestron.jestronpriv.fx.FxSession;
import org.joml.Vector3f;
import com.jestron.jestronpriv.fx.api.Transformation;
import com.jestron.jestronpriv.fx.api.Location;
import com.jestron.jestronpriv.fx.FxKit;
import com.jestron.jestronpriv.fx.KillEffect;
import com.jestron.jestronpriv.fx.P;
import com.jestron.jestronpriv.fx.api.Color;
import com.jestron.jestronpriv.fx.api.Material;
import com.jestron.jestronpriv.fx.api.BlockDisplay;
import com.jestron.jestronpriv.fx.api.ItemDisplay;
import org.joml.Quaternionf;

import static com.jestron.jestronpriv.fx.FxMath.*;

/** Gokten bir ors duser, ezer ve ustunde cizgi film yildizlari doner. */
public final class AnvilEffect extends KillEffect {

    public AnvilEffect() {
        super("ors", "&8&lÖrs Düşüşü", Material.ANVIL, true,
                "Gökten koca bir örs düşer,",
                "başında yıldızlar döner.");
    }

    @Override
    public void play(FxSession s) {
        playBase(s);
        extras(s);
    }

    private void playBase(FxSession s) {
        final Quaternionf q = s.face();
        final float sz = 1.2f;
        final double yEnd = sz / 2.0;
        final double drop = 11;
        BlockDisplay anvil = s.lit(s.block(Material.ANVIL, cube(v(0, yEnd + drop, 0), q, sz)));
        s.sound(s.loc(0, 8, 0), "entity.arrow.shoot", 0.7f, 0.5f);
        for (int t = 3; t <= 13; t++) {
            double u = (t - 2) / 11.0;
            s.tfAt(t, anvil, cube(v(0, yEnd + drop * (1 - u * u), 0), q, sz), 1);
        }
        // Ezilme - esneme
        s.tfAt(14, anvil, cube(v(0, 0.425, 0), q, v(1.4, 0.85, 1.4)), 2);
        s.tfAt(16, anvil, cube(v(0, 0.65, 0), q, v(1.1, 1.3, 1.1)), 2);
        s.tfAt(18, anvil, cube(v(0, yEnd, 0), q, sz), 2);
        s.tfAt(40, anvil, cube(v(0, -0.7, 0), q, sz), 8);
        s.tfAt(48, anvil, cube(v(0, -0.7, 0), q, 0.001f), 1);

        s.at(14, () -> {
            s.sound("block.anvil.land", 1.3f, 0.8f);
            s.sound("block.anvil.destroy", 0.5f, 1.2f);
            s.sound("entity.generic.explode", 0.4f, 1.4f);
            s.crack(s.loc(0, 0.3, 0), Material.ANVIL, 30, 0.6);
            s.groundCrack(s.loc(0, 0.1, 0), 50, 1.0);
            s.particle(P.CRIT, s.loc(0, 0.8, 0), 20, 0.5, 0.3);
            for (int i = 0; i < 20; i++) {
                double a = i * Math.PI * 2 / 20;
                s.dir(P.CLOUD, s.loc(Math.cos(a) * 0.6, 0.1, Math.sin(a) * 0.6), Math.cos(a), 0.02, Math.sin(a), 0.28);
            }
        });

        // Donen yildizlar (3D)
        final ItemDisplay[] stars = new ItemDisplay[3];
        for (int i = 0; i < 3; i++) {
            stars[i] = s.bright(s.item(Material.NETHER_STAR, item(v(0, 1.6, 0), q(), 0.001f)));
            final int k = i;
            s.anim(stars[i], 18, 40, 2, t -> {
                double a = t * 0.35 + k * Math.PI * 2 / 3;
                double sc = t < 22 ? 0.4 * easeOutBack((t - 18) / 4.0) : (t >= 38 ? 0.4 * (1 - (t - 38) / 2.0) : 0.4);
                return item(v(Math.cos(a) * 0.6, 1.55 + 0.08 * Math.sin(t * 0.5 + k), Math.sin(a) * 0.6), qY(-a), (float) Math.max(0.001, sc));
            });
        }
        s.every(18, 38, 2, t -> {
            for (int k = 0; k < 3; k++) {
                double a = t * 0.35 + k * Math.PI * 2 / 3 + 0.5;
                s.dust(s.loc(Math.cos(a) * 0.6, 1.55, Math.sin(a) * 0.6), Color.fromRGB(255, 240, 60), 0.8f, 1, 0);
            }
        });
        s.at(18, () -> s.sound(s.loc(0, 1.6, 0), "block.note_block.chime", 0.8f, 1.2f));
        s.at(21, () -> s.sound(s.loc(0, 1.6, 0), "block.note_block.chime", 0.8f, 1.5f));
        s.at(24, () -> s.sound(s.loc(0, 1.6, 0), "block.note_block.chime", 0.8f, 1.8f));
        s.at(40, () -> s.groundCrack(s.loc(0, 0.1, 0), 20, 0.6));
    }

    /** Gercekcilik katmani: ek 3D detaylar, enkaz, sok dalgalari ve parlamalar. */
    private void extras(FxSession s) {

        // Ors yaklastikca buyuyen golge
        final double gy = s.groundDy(0, 0);
        BlockDisplay shadow = s.dark(s.block(Material.BLACK_CONCRETE, cube(v(0, gy + 0.01, 0), qY(s.faceYaw), v(0.001, 0.001, 0.001))));
        s.anim(shadow, 2, 13, 1, t -> {
            double k = (t - 2) / 11.0;
            float sz = (float) (0.3 + 0.95 * k * k);
            return cube(v(0, gy + 0.01, 0), qY(s.faceYaw), v(sz, 0.01f, sz));
        });
        s.tfAt(14, shadow, cube(v(0, gy + 0.01, 0), qY(s.faceYaw), 0.001f), 1);
        FxKit.impact(s, 1.1, 14, Color.fromRGB(120, 120, 120), false);
        s.at(14, () -> FxKit.sparks(s, s.loc(0, 0.25, 0), 24, 0.5));
    }
}
