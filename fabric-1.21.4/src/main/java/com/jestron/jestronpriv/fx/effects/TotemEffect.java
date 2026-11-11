package com.jestron.jestronpriv.fx.effects;

import com.jestron.jestronpriv.fx.FxSession;
import org.joml.Vector3f;
import org.joml.Quaternionf;
import com.jestron.jestronpriv.fx.api.Transformation;
import com.jestron.jestronpriv.fx.api.BlockDisplay;
import com.jestron.jestronpriv.fx.FxKit;
import com.jestron.jestronpriv.fx.KillEffect;
import com.jestron.jestronpriv.fx.P;
import com.jestron.jestronpriv.fx.api.Color;
import com.jestron.jestronpriv.fx.api.Location;
import com.jestron.jestronpriv.fx.api.Material;
import com.jestron.jestronpriv.fx.api.ItemDisplay;

import static com.jestron.jestronpriv.fx.FxMath.*;

/** Oldugu noktanin 1.5 blok ustunde donen dev bir olumsuzluk totemi belirir. */
public final class TotemEffect extends KillEffect {

    private static final Color GOLD = Color.fromRGB(255, 215, 60);
    private static final Color GREEN = Color.fromRGB(90, 220, 90);

    public TotemEffect() {
        super("totem", "&e&lÖlümsüzlük Totemi", Material.TOTEM_OF_UNDYING, true,
                "Öldüğü noktanın 1.5 blok üstünde",
                "dönen dev bir totem belirir.");
    }

    @Override
    public void play(FxSession s) {
        playBase(s);
        extras(s);
    }

    private void playBase(FxSession s) {
        final double cy = 1.5;
        final Location cl = s.loc(0, cy, 0);
        ItemDisplay totem = s.bright(s.item(Material.TOTEM_OF_UNDYING, item(v(0, 0.4, 0), s.face(), 0.001f)));

        s.anim(totem, 2, 46, 2, t -> {
            double y, scale;
            double spin = s.faceYaw + 0.3 * t + (t > 38 ? 0.3 * (t - 38) : 0);
            if (t <= 8) {
                double k = (t - 2) / 6.0;
                y = lerp(0.4, cy, easeOut(k));
                scale = 1.6 * easeOutBack(k);
            } else if (t <= 38) {
                y = cy + 0.08 * Math.sin(t * 0.3);
                scale = 1.6;
            } else {
                double k = (t - 38) / 8.0;
                y = cy + 0.8 * easeIn(k);
                scale = 1.6 * (1 - easeIn(k));
            }
            return item(v(0, y, 0), qY(spin), (float) scale);
        });

        s.at(2, () -> {
            s.sound("item.totem.use", 1.0f, 1.0f);
            s.particle(P.TOTEM, cl, 70, 0.4, 0.6, 0.4, 0.5);
        });
        s.at(10, () -> s.particle(P.TOTEM, cl, 18, 0.3, 0.4, 0.3, 0.25));

        // Altin/yesil sarmal
        s.every(4, 38, 2, t -> {
            for (int k = 0; k < 2; k++) {
                double a = t * 0.4 + k * Math.PI;
                double y = 0.2 + ((t * 0.12 + k * 1.2) % 2.6);
                s.dust(s.loc(Math.cos(a) * 0.9, y, Math.sin(a) * 0.9), k == 0 ? GOLD : GREEN, 1.0f, 1, 0);
            }
        });
        s.every(12, 36, 6, t -> s.dust(cl, GOLD, 0.8f, 6, 0.6));

        s.at(20, () -> s.sound("block.amethyst_block.chime", 1.0f, 1.4f));
        s.at(46, () -> {
            s.sound("entity.experience_orb.pickup", 0.8f, 0.7f);
            s.dust(s.loc(0, cy + 0.8, 0), GOLD, 1.0f, 30, 0.5);
            s.particle(P.CRIT, s.loc(0, cy + 0.8, 0), 20, 0.4, 0.3);
        });
    }

    /** Gercekcilik katmani: ek 3D detaylar, enkaz, sok dalgalari ve parlamalar. */
    private void extras(FxSession s) {

        final Vector3f c = v(0, 1.5, 0);
        // Altin parlama kabugu
        FxKit.glow(s, Material.YELLOW_STAINED_GLASS, c, 1.15f, 4, 38, 0.08);
        // Gunes isinlari: merkezden disari donen ince altin/zumrut isinlar
        for (int i = 0; i < 10; i++) {
            final Vector3f dir = randomUnit(s.rnd);
            final double len = s.rd(1.3, 2.0);
            BlockDisplay ray = s.bright(s.block(i % 2 == 0 ? Material.YELLOW_STAINED_GLASS : Material.LIME_STAINED_GLASS,
                    beam(c, new Vector3f(c).add(new Vector3f(dir).mul(0.01f)), 0.001f)));
            s.anim(ray, 3, 34, 2, t -> {
                double grow = Math.min(1, (t - 3) / 5.0);
                double fade = t > 28 ? Math.max(0.001, 1 - (t - 28) / 6.0) : 1;
                Vector3f d = rot(qY(t * 0.06), dir);
                Vector3f end = new Vector3f(c).add(d.mul((float) (len * Math.max(0.01, grow))));
                return beam(c, end, (float) (0.07 * fade));
            });
            s.tfAt(36, ray, beam(c, new Vector3f(c).add(rot(qY(34 * 0.06), dir).mul(0.002f)), 0.001f), 1);
        }
        // Yerde altin halka
        FxKit.shockRing(s, 0.04, 0.3, 2.6, 3, 10, Material.YELLOW_STAINED_GLASS, 20, 0.2f, true);
        s.every(6, 36, 3, t -> s.particle(P.WAX_OFF, s.loc(s.rd(-1, 1), s.rd(0.5, 2.8), s.rd(-1, 1)), 2, 0.2, 0));
    }
}
