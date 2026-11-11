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

/** Zehirli yesil gaz bulutlari kaynar, asit gölü yayilir, kurbanin kafasi icinde eriyerek batar. */
public final class ToxicEffect extends KillEffect {

    public ToxicEffect() {
        super("toksik", "&a&lToksik Patlama", Material.SLIME_BALL, true,
                "Zehirli gaz bulutları kaynar,",
                "asit gölünde her şey erir.");
    }

    private static final int POP = 30;
    private static final Color ACID = Color.fromRGB(120, 255, 40), DARK = Color.fromRGB(40, 120, 20);

    @Override
    public void play(FxSession s) {
        final double gy = s.groundDy(0, 0);
        // asit golu
        block(s, Material.LIME_STAINED_GLASS, BRIGHT, 4, 40, 1, 3, t -> {
            double k = smooth(phase(t, 4, 18));
            double r = 0.3 + k * 2.2;
            return Pose.of(v(0, gy + 0.03, 0), qY(t * 0.03), v(r, 0.04, r));
        });
        block(s, Material.LIME_CONCRETE, LIT, 6, 40, 1, 3, t -> {
            double k = smooth(phase(t, 6, 20));
            double r = 0.2 + k * 1.5;
            return Pose.of(v(0, gy + 0.02, 0), qY(-t * 0.02 + 0.4), v(r, 0.03, r));
        });
        // eriyen kafa
        item(s, Material.ZOMBIE_HEAD, LIT, 2, 26, 1, 3, t -> {
            double k = smooth(phase(t, 6, 26));
            return Pose.of(v(0, 1.5 - k * 1.45 + gy * k, 0), mul(s.face(), qZ(Math.sin(t * 0.5) * 0.15 * k)), (float) (1.0 - k * 0.5));
        });
        // kaynayan gaz bulutlari
        for (int i = 0; i < 11; i++) {
            final int st = 3 + i * 2;
            final double a = i * 2.39, r = 0.4 + (i % 4) * 0.35;
            final double x = Math.cos(a) * r, z = Math.sin(a) * r;
            final Material m = i % 3 == 0 ? Material.YELLOW_STAINED_GLASS : Material.LIME_STAINED_GLASS;
            block(s, m, BRIGHT, st, Math.min(st + 16, 42), 1, 3, t -> {
                double k = phase(t, st, st + 16);
                double size = 0.3 + Math.sin(k * Math.PI) * 1.1;
                return Pose.of(v(x, gy + 0.4 + k * 1.6, z), mul(qY(t * 0.2 + a), qX(t * 0.15)), (float) size);
            });
        }
        s.every(4, 40, 2, t -> {
            s.particle("bubble_pop", s.loc(s.rd(-1.5, 1.5), gy + 0.1, s.rd(-1.5, 1.5)), 3, 0.2, 0.02);
            s.particle("item_slime", s.loc(s.rd(-1.0, 1.0), gy + 0.2, s.rd(-1.0, 1.0)), 2, 0.2, 0.05);
            s.dust(s.loc(s.rd(-1.2, 1.2), s.rd(0.5, 2.5), s.rd(-1.2, 1.2)), t % 4 == 0 ? ACID : DARK, 1.3f, 2, 0.3);
        });
        s.every(6, 36, 6, t -> s.sound("block.bubble_column.upwards_inside", 0.8f, s.rp(0.6f, 1.0f)));
        s.at(3, () -> {
            s.sound("entity.puffer_fish.blow_up", 1.0f, 0.6f);
            s.sound("block.brewing_stand.brew", 1.0f, 0.7f);
            s.longSound("block.lava.ambient", 0.6f, 1.6f);
        });
        s.every(8, 26, 6, t -> s.sound("block.slime_block.break", 0.7f, s.rp(0.6f, 0.9f)));
        // zehir patlamasi
        s.at(POP, () -> {
            Location c = s.loc(0, gy + 0.8, 0);
            s.stopSound("block.lava.ambient");
            s.particle(P.EXPLOSION, c, 2, 0.6, 0);
            s.dust(c, ACID, 1.8f, 50, 1.4);
            s.particle(P.WITCH, c, 30, 1.0, 0.1);
            s.particle("item_slime", c, 30, 0.8, 0.2);
            s.sound("entity.puffer_fish.blow_up", 1.2f, 0.5f);
            s.sound("block.lava.extinguish", 1.0f, 0.6f);
            s.sound("entity.generic.explode", 0.5f, 1.4f);
        });
        FxKit.shockRing(s, gy + 0.03, 0.4, 3.4, POP, 8, Material.LIME_STAINED_GLASS, 16, 0.2f, true);
        FxKit.glow(s, Material.LIME_STAINED_GLASS, v(0, gy + 0.8, 0), 1.6f, POP, POP + 7, 0.25);
        s.every(POP + 2, 47, 2, t -> s.particle(P.WITCH, s.loc(s.rd(-1.8, 1.8), gy + s.rd(0.2, 1.8), s.rd(-1.8, 1.8)), 1, 0.1, 0.01));
    }
}
