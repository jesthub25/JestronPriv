package com.jestron.jestronpriv.fx.effects;

import com.jestron.jestronpriv.fx.FxSession;
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
import org.joml.Vector3f;

import static com.jestron.jestronpriv.fx.FxMath.*;

/** 8 kilic kurbanin etrafinda doner, sonra hepsi ayni anda merkeze saplanir. */
public final class SwordRingEffect extends KillEffect {

    private static final double TARGET_Y = 1.0;

    public SwordRingEffect() {
        super("kilic_cemberi", "&9&lKılıç Çemberi", Material.NETHERITE_SWORD, true,
                "8 kılıç kurbanın etrafında döner",
                "ve aynı anda merkeze saplanır.");
    }

    /** t anindaki {aci, yaricap, yukseklik, olcek}. */
    private static double[] state(int t, int i) {
        double a0 = i * Math.PI / 4;
        double a, r, y, sc = 1.3;
        if (t <= 24) {
            a = a0 + 0.13 * t;
            r = lerp(2.6, 2.2, t / 24.0);
            y = 1.4 + 0.15 * Math.sin(t * 0.4 + i);
            if (t <= 8) sc = 1.3 * easeOutBack((t - 2) / 6.0);
        } else {
            a = a0 + 0.13 * 24;
            y = 1.4;
            if (t <= 27) r = lerp(2.2, 2.55, (t - 24) / 3.0);
            else if (t <= 30) r = lerp(2.55, 0.55, (t - 27) / 3.0);
            else if (t <= 34) r = 0.55;
            else {
                double k = (t - 34) / 12.0;
                r = lerp(0.55, 3.0, easeIn(k));
                y = lerp(1.4, 5.5, easeIn(k));
                a += 0.25 * (t - 34);
                if (t >= 41) sc = 1.3 * (1 - (t - 41) / 4.0);
            }
            if (t > 27 && t <= 34) y = lerp(1.4, TARGET_Y + 0.25, Math.min(1, (t - 27) / 3.0));
        }
        return new double[]{a, r, y, Math.max(0.001, sc)};
    }

    @Override
    public void play(FxSession s) {
        playBase(s);
        extras(s);
    }

    private void playBase(FxSession s) {
        for (int i = 0; i < 8; i++) {
            final int k = i;
            Material m = i % 2 == 0 ? Material.NETHERITE_SWORD : Material.DIAMOND_SWORD;
            double[] s0 = state(2, k);
            double x0 = Math.cos(s0[0]) * s0[1], z0 = Math.sin(s0[0]) * s0[1];
            double p0 = Math.atan2(s0[2] - TARGET_Y, Math.max(0.3, s0[1]));
            ItemDisplay d = s.lit(s.item(m, item(v(x0, s0[2], z0), spriteHorizontal(yawForX(-x0, -z0), p0), 0.001f)));
            for (int t = 2; t <= 46; t += (t >= 24 && t <= 34 ? 1 : 2)) {
                final int tt = t;
                final int step = (t >= 24 && t <= 34) ? 1 : 2;
                double[] st = state(t, k);
                double x = Math.cos(st[0]) * st[1], z = Math.sin(st[0]) * st[1];
                Vector3f pos = v(x, st[2], z);
                double dx = -x, dz = -z;
                double pitch = Math.atan2(st[2] - TARGET_Y, Math.max(0.3, st[1]));
                if (t > 34) pitch = 0.6;
                s.tfAt(tt, d, item(pos, spriteHorizontal(yawForX(dx, dz), pitch), (float) st[3]), step);
            }
        }

        s.sound("item.armor.equip_netherite", 1.0f, 1.0f);
        s.at(4, () -> s.sound("entity.player.attack.sweep", 0.5f, 1.3f));
        s.at(12, () -> s.sound("entity.player.attack.sweep", 0.5f, 1.5f));
        s.at(20, () -> s.sound("entity.player.attack.sweep", 0.5f, 1.6f));
        s.at(24, () -> s.sound("item.trident.return", 1.0f, 0.8f));
        s.every(4, 24, 2, t -> {
            for (int k = 0; k < 8; k++) {
                double[] st = state(t, k);
                s.dust(s.loc(Math.cos(st[0] - 0.15) * st[1], st[2], Math.sin(st[0] - 0.15) * st[1]), Color.fromRGB(180, 180, 255), 0.7f, 1, 0);
            }
        });
        s.at(30, () -> {
            Location c = s.loc(0, TARGET_Y + 0.2, 0);
            s.sound(c, "entity.player.attack.crit", 1.0f, 0.8f);
            s.sound(c, "item.trident.hit", 1.0f, 0.7f);
            s.sound(c, "entity.player.attack.strong", 1.0f, 0.6f);
            s.sound(c, "entity.generic.explode", 0.4f, 1.6f);
            s.particle(P.CRIT, c, 30, 0.3, 0.5);
            s.particle(P.SWEEP_ATTACK, c, 3, 0.4, 0);
            s.particle(P.ENCHANTED_HIT, c, 20, 0.3, 0.4);
            s.flash(c, Color.WHITE);
        });
        s.at(34, () -> s.sound(s.loc(0, 2, 0), "entity.player.attack.sweep", 0.8f, 1.4f));
    }

    /** Gercekcilik katmani: ek 3D detaylar, enkaz, sok dalgalari ve parlamalar. */
    private void extras(FxSession s) {

        // Donen kiliclarin arkasinda hayalet izler
        for (int i = 0; i < 8; i++) {
            Material m = i % 2 == 0 ? Material.NETHERITE_SWORD : Material.DIAMOND_SWORD;
            for (int t = 6; t <= 22; t += 4) {
                double[] st = state(t - 1, i);
                double x = Math.cos(st[0]) * st[1], z = Math.sin(st[0]) * st[1];
                double pitch = Math.atan2(st[2] - TARGET_Y, Math.max(0.3, st[1]));
                FxKit.ghostItem(s, m, item(v(x, st[2], z), spriteHorizontal(yawForX(-x, -z), pitch), (float) st[3] * 0.9f), t, 4, true);
            }
        }
        FxKit.glow(s, Material.WHITE_STAINED_GLASS, v(0, TARGET_Y + 0.2, 0), 0.9f, 29, 36, 0.2);
        FxKit.shockRing(s, TARGET_Y + 0.1, 0.4, 3.2, 30, 8, Material.LIGHT_BLUE_STAINED_GLASS, 20, 0.16f, true);
        FxKit.shockRing(s, 0.04, 0.4, 2.6, 30, 9, Material.LIGHT_GRAY_STAINED_GLASS, 18, 0.2f, false);
        s.at(30, () -> FxKit.sparks(s, s.loc(0, TARGET_Y + 0.2, 0), 26, 0.55));
    }
}
