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

/** Uc parlak katana kesigi kurbani capraz biçer; bir an durur, kesikler kizarir ve sakura yapraklari patlar. */
public final class SamuraiSlashEffect extends KillEffect {

    public SamuraiSlashEffect() {
        super("samuray", "&d&lSamuray Kesiği", Material.NETHERITE_SWORD, true,
                "Üç ışık hızında katana kesiği,",
                "bir an sessizlik ve sakura fırtınası.");
    }

    private static final int[] AT = {4, 7, 10};
    private static final double[] ANG = {0.62, -0.55, 1.35};
    private static final int BLOOM = 20;
    private static final Color PINK = Color.fromRGB(255, 160, 200), DEEP = Color.fromRGB(230, 60, 120);

    @Override
    public void play(FxSession s) {
        final Quaternionf face = s.face();
        for (int i = 0; i < 3; i++) {
            final int a = AT[i];
            final Quaternionf q = mul(face, qZ(ANG[i]));
            final Vector3f c = v(0, 1.05 + (i - 1) * 0.12, 0);
            // beyaz kesik: bir anda uzar, incelerek kaybolur
            block(s, Material.WHITE_STAINED_GLASS, BRIGHT, a, a + 9, 1, 1, t -> {
                double k = smooth(phase(t, a, a + 2));
                double thin = 1 - smooth(phase(t, a + 2, a + 9));
                return Pose.of(c, q, v(3.4 * k + 0.01, 0.09 * thin + 0.004, 0.02));
            });
            // kizil iz: sessizlikten sonra parlar ve patlamada dagilir
            block(s, Material.RED_STAINED_GLASS, BRIGHT, a + 6, BLOOM + 2, 1, 2, t -> {
                double glow = 0.5 + 0.5 * smooth(phase(t, a + 6, BLOOM));
                double out = t > BLOOM ? 1 - phase(t, BLOOM, BLOOM + 2) : 1;
                return Pose.of(c, q, v(2.2 * out + 0.01, 0.035 * glow, 0.025));
            });
            s.at(a, () -> {
                s.sound("entity.player.attack.sweep", 1.0f, 1.5f + s.rp(0f, 0.3f));
                s.sound("item.trident.return", 0.5f, 1.8f);
                s.particle(P.SWEEP_ATTACK, s.loc(0, 1.05, 0), 1, 0, 0);
                s.particle(P.CRIT, s.loc(0, 1.05, 0), 10, 0.6, 0.4);
            });
        }
        // kinindan cikip donen katana (tepede yatay)
        item(s, Material.NETHERITE_SWORD, LIT, 2, 18, 1, 2, t -> {
            double k = smooth(phase(t, 2, 12));
            Vector3f p = v(0, 2.6 - k * 0.2, 0);
            return Pose.of(p, mul(s.face(), qZ(-Math.PI / 4 + t * 0.9 * (1 - k))), 1.3f);
        });
        // sessizlik: tek beyaz parilti
        s.at(14, () -> {
            s.particle(P.END_ROD, s.loc(0, 1.05, 0), 6, 0.3, 0.01);
            s.sound("block.amethyst_block.chime", 0.8f, 1.9f);
        });
        // sakura patlamasi
        s.at(BLOOM, () -> {
            Location c = s.loc(0, 1.05, 0);
            s.particle(P.FLASH, c, 1, 0, 0);
            s.particle("cherry_leaves", c, 60, 1.0, 0.1);
            s.dust(c, PINK, 1.6f, 50, 0.9);
            s.dust(c, DEEP, 1.2f, 30, 0.6);
            for (int i = 0; i < 24; i++) s.dir(P.END_ROD, c, randomUnit(s.rnd), s.rd(0.15, 0.3));
            s.sound("block.cherry_leaves.break", 1.2f, 0.8f);
            s.sound("block.cherry_leaves.break", 1.0f, 1.2f);
            s.sound("entity.player.attack.crit", 1.0f, 0.8f);
            s.sound("block.bamboo.break", 0.9f, 0.7f);
        });
        FxKit.glow(s, Material.PINK_STAINED_GLASS, v(0, 1.05, 0), 1.3f, BLOOM, BLOOM + 6, 0.2);
        FxKit.shockRing(s, s.groundDy(0, 0) + 0.02, 0.3, 3.0, BLOOM, 9, Material.PINK_STAINED_GLASS, 16, 0.14f, true);
        // dusen yapraklar
        s.every(BLOOM + 1, 46, 2, t -> {
            s.particle("cherry_leaves", s.loc(s.rd(-1.6, 1.6), s.rd(2.2, 3.2), s.rd(-1.6, 1.6)), 2, 0.3, 0);
            if (t % 4 == 0) s.dust(s.loc(s.rd(-1.2, 1.2), s.rd(0.3, 1.8), s.rd(-1.2, 1.2)), PINK, 0.9f, 1, 0.05);
        });
        s.every(BLOOM + 4, 40, 8, t -> s.sound("block.cherry_leaves.place", 0.5f, s.rp(0.9f, 1.3f)));
    }
}
