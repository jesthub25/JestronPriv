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
/** Altin bir piramit kat kat yukselir, tepesindeki tas parlar ve gokyuzune isin yollar. */
public final class GoldPyramidEffect extends KillEffect {

    public GoldPyramidEffect() {
        super("altin_piramit", "&6&lAltın Piramit", Material.GOLD_BLOCK, true,
                "Altın bir piramit kat kat yükselir,",
                "tepe taşı parlayıp göğe ışın yollar.");
    }

    private static final double[] W = {2.4, 1.85, 1.3, 0.8};
    private static final double H = 0.38;
    private static final int BEAM = 16;

    @Override
    public void play(FxSession s) {
        final Quaternionf q = mul(s.face(), qY(Math.PI / 4));
        for (int i = 0; i < W.length; i++) {
            final int layer = i;
            final int st = 2 + i * 3;
            block(s, Material.GOLD_BLOCK, LIT, st, 40, 1, 1, t -> {
                double rise = smooth(phase(t, st, st + 5));
                double sink = t > 36 ? smooth(phase(t, 36, 42)) : 0;
                double y = (layer + 0.5) * H - (1 - rise) * (layer + 1) * H - sink * (layer + 1) * H;
                return Pose.of(v(0, y, 0), q, v(W[layer], H, W[layer]));
            });
            // Kat kenar susu
            block(s, Material.YELLOW_CONCRETE, LIT, st + 2, 40, 1, 2, t -> {
                double rise = smooth(phase(t, st, st + 5));
                double sink = t > 36 ? smooth(phase(t, 36, 42)) : 0;
                double y = (layer + 1) * H - 0.02 - (1 - rise) * (layer + 1) * H - sink * (layer + 1) * H;
                return Pose.of(v(0, y, 0), q, v(W[layer] + 0.04, 0.04, W[layer] + 0.04));
            });
        }
        // Tepe tasi (parlayan) ve isin
        block(s, Material.YELLOW_STAINED_GLASS, BRIGHT, 14, 40, 1, 3, t -> {
            double bob = Math.sin(t * 0.4) * 0.05;
            return Pose.of(v(0, W.length * H + 0.32 + bob, 0), mul(qY(t * 0.12), qX(Math.PI / 4), qZ(Math.PI / 4)), v(0.42, 0.42, 0.42));
        });
        block(s, Material.GOLD_BLOCK, BRIGHT, 14, 40, 1, 3, t -> {
            double bob = Math.sin(t * 0.4) * 0.05;
            return Pose.of(v(0, W.length * H + 0.32 + bob, 0), mul(qY(t * 0.12), qX(Math.PI / 4), qZ(Math.PI / 4)), v(0.26, 0.26, 0.26));
        });
        FxKit.pillar(s, Material.YELLOW_STAINED_GLASS, 0, 0, W.length * H + 0.5, 9, 0.35f, BEAM, 38);
        FxKit.glow(s, Material.YELLOW_STAINED_GLASS, v(0, W.length * H + 0.32, 0), 0.9f, BEAM, 38, 0.12);
        FxKit.shockRing(s, 0.04, 0.6, 3.0, BEAM, 8, Material.YELLOW_STAINED_GLASS, 16, 0.18f, true);
        // Parcaciklar: kum, altin pirilti
        s.every(2, 14, 2, t -> s.particle(P.BLOCK, s.loc(0, 0.2, 0), 8, 1.0, 0.1, 1.0, 0.1, s.groundData));
        s.every(BEAM, 38, 2, t -> {
            s.particle(P.WAX_ON, s.loc(s.rd(-1.2, 1.2), s.rd(0.2, 2.2), s.rd(-1.2, 1.2)), 2, 0.1, 0.02);
            s.particle(P.END_ROD, s.loc(0, W.length * H + 0.5 + s.rd(0, 3), 0), 1, 0.1, 0.02);
        });
        s.at(BEAM, () -> s.dust(s.loc(0, 1.8, 0), Color.fromRGB(255, 210, 60), 1.5f, 30, 0.8));
        // Ses tasarimi
        for (int i = 0; i < W.length; i++) {
            final int at = 2 + i * 3;
            s.at(at, () -> {
                s.sound("block.sand.break", 0.9f, 0.7f);
                s.sound("block.stone.break", 0.5f, 0.6f + at * 0.03f);
            });
        }
        s.at(14, () -> s.sound("block.amethyst_block.chime", 1.0f, 0.8f));
        s.at(BEAM, () -> {
            s.sound("block.beacon.activate", 1.0f, 1.0f);
            s.sound("block.respawn_anchor.charge", 0.6f, 1.4f);
        });
        s.at(BEAM + 2, () -> s.longSound("block.beacon.ambient", 0.7f, 1.2f));
        s.every(BEAM + 4, 36, 5, t -> s.sound("entity.experience_orb.pickup", 0.4f, s.rp(1.2f, 1.8f)));
        s.at(36, () -> {
            s.sound("block.beacon.deactivate", 0.9f, 1.0f);
            s.sound("block.sand.fall", 0.8f, 0.6f);
        });
    }
}
