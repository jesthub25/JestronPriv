package com.jestron.jestronpriv.fx.effects;

import com.jestron.jestronpriv.fx.FxSession;
import com.jestron.jestronpriv.fx.api.Transformation;
import com.jestron.jestronpriv.fx.api.BlockDisplay;
import com.jestron.jestronpriv.fx.api.Location;
import com.jestron.jestronpriv.fx.FxKit;
import com.jestron.jestronpriv.fx.KillEffect;
import com.jestron.jestronpriv.fx.P;
import com.jestron.jestronpriv.fx.api.Color;
import com.jestron.jestronpriv.fx.api.Material;
import com.jestron.jestronpriv.fx.api.ItemDisplay;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static com.jestron.jestronpriv.fx.FxMath.*;

/** Gokten dev bir elmas kilic duser ve oldugu noktaya saplanir. */
public final class DiamondSwordEffect extends KillEffect {

    public DiamondSwordEffect() {
        super("elmas_kilic", "&b&lDev Elmas Kılıç", Material.DIAMOND_SWORD, true,
                "Gökyüzünden dev bir elmas kılıç",
                "düşer ve yere saplanır.");
    }

    @Override
    public void play(FxSession s) {
        playBase(s);
        extras(s);
    }

    private void playBase(FxSession s) {
        final float sc = 3.2f;
        final float tip = SPRITE_TIP_DIST * sc;
        final float yEnd = tip - 0.55f;
        final float drop = 14f;
        final Quaternionf rot = mul(s.face(), qZ(-3.0 * Math.PI / 4.0));

        ItemDisplay sw = s.lit(s.item(Material.DIAMOND_SWORD, item(v(0, yEnd + drop, 0), rot, sc)));

        // Dusus (hizlanarak)
        s.sound(s.loc(0, drop, 0), "item.trident.throw", 1.0f, 0.5f);
        s.at(7, () -> s.sound(s.loc(0, 6, 0), "entity.arrow.shoot", 0.9f, 0.5f));
        s.at(10, () -> s.sound("item.trident.riptide_1", 0.6f, 1.3f));
        for (int t = 3; t <= 14; t++) {
            final double u = (t - 2) / 12.0;
            final float y = (float) (yEnd + drop * (1 - u * u));
            s.tfAt(t, sw, item(v(0, y, 0), rot, sc), 1);
            final int tt = t;
            s.at(t, () -> {
                s.dust(s.loc(0, y - tip * 0.6, 0), Color.fromRGB(90, 235, 235), 1.0f, 3, 0.12);
                if (tt % 2 == 0) s.particle(P.ENCHANTED_HIT, s.loc(0, y, 0), 3, 0.15, 0.4, 0.15, 0.01);
            });
        }

        // Carpma
        s.at(15, () -> {
            s.sound("item.mace.smash_ground_heavy", 1.0f, 0.8f);
            s.sound("item.trident.hit_ground", 1.2f, 0.6f);
            s.sound("block.anvil.land", 0.5f, 0.6f);
            s.sound("entity.generic.explode", 0.45f, 1.5f);
            s.groundCrack(s.loc(0, 0.1, 0), 70, 0.9);
            s.particle(P.EXPLOSION, s.loc(0, 0.4, 0), 1, 0, 0);
            s.particle(P.SWEEP_ATTACK, s.loc(0, 0.5, 0), 1, 0, 0);
            s.particle(P.CRIT, s.loc(0, 1.0, 0), 30, 0.5, 0.4);
            for (int i = 0; i < 28; i++) {
                double a = i * Math.PI * 2 / 28;
                s.dir(P.CLOUD, s.loc(Math.cos(a) * 0.3, 0.15, Math.sin(a) * 0.3), Math.cos(a), 0.02, Math.sin(a), 0.32);
            }
        });
        // Sok dalgasi halkasi
        s.every(15, 21, 1, t -> {
            double r = 0.5 + (t - 15) * 0.45;
            for (int i = 0; i < 32; i++) {
                double a = i * Math.PI * 2 / 32;
                s.dust(s.loc(Math.cos(a) * r, 0.12, Math.sin(a) * r), Color.fromRGB(200, 245, 255), 1.0f, 1, 0);
            }
        });

        // Saplandiktan sonra sallanma (ucu sabit)
        final Vector3f tipPos = v(0, yEnd - tip, 0);
        final Vector3f axis = s.local(0, 0, 1);
        for (int t = 15; t <= 28; t++) {
            final int k = t - 15;
            double ang = 0.11 * Math.exp(-k / 5.0) * Math.sin(k * 0.95);
            Quaternionf w = qAxis(ang, axis);
            Vector3f c = new Vector3f(tipPos).add(rot(w, v(0, tip, 0)));
            s.tfAt(t, sw, item(c, mul(w, rot), sc), 1);
        }

        // Parilti
        s.every(18, 38, 4, t -> {
            double y = s.rd(0.3, yEnd + tip * 0.6);
            s.particle(P.ELECTRIC_SPARK, s.loc(s.rd(-0.2, 0.2), y, s.rd(-0.2, 0.2)), 3, 0.1, 0.05);
            s.dust(s.loc(0, y, 0), Color.fromRGB(150, 255, 255), 0.8f, 2, 0.25);
        });

        // Yere gomulerek kaybolma
        s.at(40, () -> s.sound("item.trident.return", 0.5f, 1.4f));
        s.tfAt(40, sw, item(v(0, yEnd - 1.2f, 0), rot, 0.01f), 8);
        s.at(46, () -> {
            s.groundCrack(s.loc(0, 0.1, 0), 15, 0.3);
            s.particle(P.CLOUD, s.loc(0, 0.2, 0), 6, 0.2, 0.02);
        });
    }

    /** Gercekcilik katmani: ek 3D detaylar, enkaz, sok dalgalari ve parlamalar. */
    private void extras(FxSession s) {

        final float sc = 3.2f;
        final float tip = SPRITE_TIP_DIST * sc;
        final float yEnd = tip - 0.55f;
        final float drop = 14f;
        final Quaternionf rot = mul(s.face(), qZ(-3.0 * Math.PI / 4.0));
        // Hedef isini: kilicin inecegi noktayi gosteren ince elmas isik sutunu
        FxKit.pillar(s, Material.LIGHT_BLUE_STAINED_GLASS, 0, 0, 0, 16, 0.16f, 2, 15);
        // Dusus izi: arkasinda kalan hayalet kilic kopyalari (hareket bulanikligi)
        for (int t = 6; t <= 14; t += 2) {
            double u = (t - 3) / 12.0;
            float y = (float) (yEnd + drop * (1 - u * u));
            FxKit.ghostItem(s, Material.DIAMOND_SWORD, item(v(0, y, 0), rot, sc * 0.92f), t, 4, true);
        }
        // Agir carpma: flas, toz halkasi, 3D sok halkasi, catlaklar, yanik izi ve firlayan zemin parcalari
        FxKit.impact(s, 1.25, 15, Color.fromRGB(150, 240, 255), true);
        FxKit.shockRing(s, 0.05, 0.4, 3.4, 15, 9, Material.LIGHT_BLUE_STAINED_GLASS, 22, 0.22f, true);
        // Kilicin etrafinda yukselen elmas kivilcimlari
        s.every(17, 38, 2, t -> {
            for (int k = 0; k < 3; k++) {
                double a = t * 0.45 + k * Math.PI * 2 / 3;
                double y = 0.4 + ((t - 17) * 0.09 + k * 0.5) % 2.2;
                s.dust(s.loc(Math.cos(a) * 0.55, y, Math.sin(a) * 0.55), Color.fromRGB(120, 255, 255), 0.7f, 1, 0);
            }
        });
    }
}
