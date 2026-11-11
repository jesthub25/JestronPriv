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
/** Kurbanin ustunde donen bir galaksi olusur, merkezine cokup parlak bir patlamayla dagilir. */
public final class GalaxyEffect extends KillEffect {

    public GalaxyEffect() {
        super("galaksi", "&d&lGalaksi", Material.NETHER_STAR, true,
                "Üstünde dönen bir galaksi oluşur,",
                "merkezine çöker ve parlayarak dağılır.");
    }

    private static final double Y = 1.7;
    private static final int ARMS = 3, STARS = 9, COLLAPSE = 28, BURST = 35;

    @Override
    public void play(FxSession s) {
        // Disk oyuncuya donuk (yatay duzlemden 65 derece kaldirilmis): kollar on yuzden gorunur
        final Quaternionf tilt = mul(s.face(), qX(1.15));
        Material[] mats = {Material.PURPLE_STAINED_GLASS, Material.MAGENTA_STAINED_GLASS, Material.LIGHT_BLUE_STAINED_GLASS,
                Material.WHITE_STAINED_GLASS, Material.PINK_STAINED_GLASS};
        for (int arm = 0; arm < ARMS; arm++) {
            for (int k = 0; k < STARS; k++) {
                final double u = (k + 1) / (double) STARS;
                final double a0 = arm * Math.PI * 2 / ARMS + u * 2.6;
                final float size = (float) (0.3 - u * 0.14);
                final Material m = mats[(arm + k) % mats.length];
                block(s, m, BRIGHT, 3 + k, BURST, 1, 2, t -> {
                    double spin = t * 0.14;
                    double r = u * 1.9 * (t > COLLAPSE ? 1 - smooth(phase(t, COLLAPSE, BURST)) : 1);
                    double a = a0 + spin * (1.6 - u);
                    Vector3f p = rot(tilt, v(Math.cos(a) * r, Math.sin(t * 0.3 + u * 6) * 0.04, Math.sin(a) * r));
                    p.y += (float) Y;
                    return Pose.of(p, mul(tilt, qY(-a), qZ(t * 0.2)), v(size, size * 0.6, size));
                });
            }
        }
        // Merkez: nether yildizi + parlama
        item(s, Material.NETHER_STAR, BRIGHT, 2, 42, 1, 3, t -> {
            double s1 = t < COLLAPSE ? 0.7 : t < BURST ? 0.7 + phase(t, COLLAPSE, BURST) * 0.6 : 1.3 * (1 - phase(t, BURST, 42));
            return Pose.of(v(0, Y, 0), mul(s.face(), qZ(t * 0.25)), (float) Math.max(0.01, s1));
        });
        FxKit.glow(s, Material.MAGENTA_STAINED_GLASS, v(0, Y, 0), 0.6f, 4, BURST, 0.15);
        // Patlama kabugu
        block(s, Material.WHITE_STAINED_GLASS, BRIGHT, BURST, 41, 1, 1, t -> {
            double k = smooth(phase(t, BURST, 41));
            return Pose.of(v(0, Y, 0), qY(t * 0.3), (float) (0.3 + k * 3.2));
        });
        FxKit.shockRing(s, Y, 0.3, 3.2, BURST, 7, Material.PURPLE_STAINED_GLASS, 16, 0.12f, true);
        // Parcaciklar
        s.every(4, BURST, 2, t -> {
            s.particle(P.END_ROD, s.loc(s.rd(-1.6, 1.6), Y + s.rd(-0.5, 0.5), s.rd(-1.6, 1.6)), 1, 0.05, 0.01);
            s.particle(P.WITCH, s.loc(0, Y, 0), 2, 0.8, 0.02);
        });
        s.every(COLLAPSE, BURST, 1, t -> s.particle(P.REVERSE_PORTAL, s.loc(0, Y, 0), 8, 1.2, 0.05));
        s.at(BURST, () -> {
            s.particle(P.FLASH, s.loc(0, Y, 0), 1, 0, 0);
            s.particle(P.END_ROD, s.loc(0, Y, 0), 40, 0.2, 0.35);
            s.dust(s.loc(0, Y, 0), Color.fromRGB(220, 120, 255), 1.6f, 30, 1.0);
        });
        // Ses tasarimi
        s.sound("block.respawn_anchor.charge", 0.6f, 0.5f);
        s.at(3, () -> s.longSound("block.beacon.ambient", 0.7f, 0.7f));
        s.every(6, COLLAPSE, 4, t -> s.sound("block.amethyst_block.chime", 0.6f, s.rp(1.2f, 1.9f)));
        s.at(COLLAPSE, () -> {
            s.sound("block.respawn_anchor.deplete", 0.9f, 0.6f);
            s.sound("block.end_portal.spawn", 0.5f, 1.4f);
        });
        s.at(BURST, () -> {
            s.sound("entity.firework_rocket.blast", 1.0f, 0.6f);
            s.sound("block.amethyst_block.chime", 1.0f, 0.6f);
            s.sound("entity.generic.explode", 0.5f, 1.6f);
        });
    }
}
