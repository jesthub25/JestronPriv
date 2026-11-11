package com.jestron.jestronpriv.fx.effects;

import com.jestron.jestronpriv.fx.FxKit;
import com.jestron.jestronpriv.fx.FxSession;
import com.jestron.jestronpriv.fx.KillEffect;
import com.jestron.jestronpriv.fx.P;
import com.jestron.jestronpriv.fx.api.BlockDisplay;
import com.jestron.jestronpriv.fx.api.Color;
import com.jestron.jestronpriv.fx.api.Location;
import com.jestron.jestronpriv.fx.api.Material;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static com.jestron.jestronpriv.fx.FxMath.*;

/**
 * Yerden sivri uclu (konik) ametist kristalleri fiskirir; etrafinda kalsit/bazalt geod halkasi,
 * mor parlama kabuklari ve kristal kivilcimlari. Sonunda kristaller kirilip parcalara ayrilir.
 */
public final class AmethystEffect extends KillEffect {

    private static final Color AME = Color.fromRGB(190, 120, 255);
    /** Kristal bolumleri: {genislik orani, yukseklik orani}. Ust uste konik uc olusturur. */
    private static final double[][] SEG = {{1.0, 0.5}, {0.72, 0.28}, {0.42, 0.14}, {0.18, 0.08}};

    public AmethystEffect() {
        super("ametist", "&d&lAmetist Patlaması", Material.AMETHYST_CLUSTER, true,
                "Yerden ametist kristalleri",
                "fışkırır, parlar ve kırılır.");
    }

    @Override
    public void play(FxSession s) {
        final int n = 11;
        for (int i = 0; i < n; i++) {
            final boolean center = i == 0;
            double a = center ? 0 : i * Math.PI * 2 / (n - 1) + s.rd(-0.25, 0.25);
            double r = center ? 0 : s.rd(0.85, 1.75);
            double x = Math.cos(a) * r, z = Math.sin(a) * r;
            final Vector3f base = v(x, s.groundDy(x, z) - 0.1, z);
            final float w = center ? 0.62f : (float) s.rd(0.3, 0.46);
            final double H = center ? 3.0 : s.rd(1.1, 2.4);
            double tilt = center ? 0 : Math.toRadians(s.rd(12, 30));
            Vector3f axis = center ? v(1, 0, 0) : v(-Math.sin(a), 0, Math.cos(a));
            final Quaternionf q = mul(qAxis(-tilt, axis), qY(Math.PI / 4 + s.rnd.nextDouble()));
            final int st = 2 + (center ? 0 : i);
            final int br = 34 + (i % 4);

            // Konik kristal: 4 bolum ust uste, her biri bir oncekinden ince
            for (int sg = 0; sg < SEG.length; sg++) {
                final int seg = sg;
                Material m = sg == 0 ? Material.AMETHYST_BLOCK : (sg == SEG.length - 1 ? Material.PURPLE_STAINED_GLASS : Material.AMETHYST_BLOCK);
                BlockDisplay d = s.bright(s.block(m, cube(base, q, 0.001f)));
                s.anim(d, st, st + 5, 1, t -> crystal(base, q, w, H, seg, growth(t, st)));
                s.tfAt(br, d, cube(new Vector3f(base).add(rot(q, v(0, 0.001, 0))), q, 0.001f), 4);
            }
            // Buyuk kristallerde ic isilti (yari saydam magenta kabuk)
            if (center || H > 1.9) {
                BlockDisplay glow = s.bright(s.block(Material.MAGENTA_STAINED_GLASS, cube(base, q, 0.001f)));
                s.anim(glow, st + 2, br, 2, t -> {
                    double g = growth(t, st);
                    double h = H * 0.62 * g;
                    double pulse = 1 + 0.06 * Math.sin(t * 0.8);
                    return cube(new Vector3f(base).add(rot(q, v(0, h / 2, 0))), q,
                            v(w * 1.35 * pulse, Math.max(0.001, h * 1.05), w * 1.35 * pulse));
                });
                s.tfAt(br, glow, cube(base, q, 0.001f), 3);
            }

            final Location bl = s.loc(base.x, base.y + 0.15, base.z);
            final float pitch = s.rp(0.8f, 1.4f);
            s.at(st, () -> {
                s.sound(bl, "block.amethyst_cluster.break", 0.9f, pitch);
                s.groundCrack(bl, 10, 0.25);
                s.particle(P.WAX_OFF, bl, 4, 0.2, 0);
            });
            final Location mid = s.loc(new Vector3f(base).add(rot(q, v(0, H / 2, 0))));
            s.at(br, () -> {
                s.crack(mid, Material.AMETHYST_BLOCK, 16, 0.35);
                s.sound(mid, "block.amethyst_block.break", 1.0f, s.rp(0.9f, 1.3f));
                FxKit.sparks(s, mid, 6, 0.25);
            });
        }

        // Geod halkasi: once kalsit, disinda puruzsuz bazalt yukselir
        for (int ring = 0; ring < 2; ring++) {
            final int cnt = ring == 0 ? 10 : 12;
            final double rr = ring == 0 ? 2.15 : 2.6;
            final Material m = ring == 0 ? Material.CALCITE : Material.SMOOTH_BASALT;
            for (int i = 0; i < cnt; i++) {
                final double a = i * Math.PI * 2 / cnt + ring * 0.2;
                final double x = Math.cos(a) * rr, z = Math.sin(a) * rr;
                final double gy = s.groundDy(x, z);
                final float size = (float) s.rd(0.32, 0.5);
                final Quaternionf q = mul(qAxis(-0.35, v(-Math.sin(a), 0, Math.cos(a))), qY(a + s.rd(-0.3, 0.3)));
                final Vector3f low = v(x, gy - size, z), high = v(x, gy + size * 0.1, z);
                BlockDisplay d = s.lit(s.block(m, cube(low, q, size)));
                final int st = 3 + i % 4 + ring * 2;
                s.tfAt(st, d, cube(high, q, size), 4);
                s.tfAt(FxKit.FADE_AT - 3 + ring, d, cube(low, q, 0.001f), 5);
            }
        }

        // Kucuk kume kristalleri
        for (int i = 0; i < 8; i++) {
            double a = s.rnd.nextDouble() * Math.PI * 2;
            double r = s.rd(0.5, 2.0);
            double x = Math.cos(a) * r, z = Math.sin(a) * r;
            final Vector3f c = v(x, s.groundDy(x, z) + 0.3, z);
            final Quaternionf q = qY(s.rnd.nextDouble() * 3);
            BlockDisplay d = s.bright(s.block(Material.AMETHYST_CLUSTER, cube(c, q, 0.001f)));
            s.tfAt(4 + i, d, cube(c, q, 0.6f), 3);
            s.tfAt(40, d, cube(c, q, 0.001f), 6);
        }

        FxKit.shockRing(s, 0.05, 0.4, 3.0, 2, 10, Material.PURPLE_STAINED_GLASS, 16, 0.25f, true);
        FxKit.cracks(s, 8, 2.0, Material.AMETHYST_BLOCK, 2, 5, 2, 0.1);
        FxKit.debris(s, v(0, 1.4, 0), new Material[]{Material.AMETHYST_BLOCK, Material.PURPLE_STAINED_GLASS, Material.AMETHYST_BLOCK},
                12, 0.1, 0.24, 0.15, 0.35, 0.1f, 0.22f, 35, true);

        s.at(14, () -> s.sound("block.amethyst_block.chime", 1.2f, 1.0f));
        s.at(20, () -> s.sound("block.amethyst_block.chime", 1.2f, 1.4f));
        s.at(26, () -> s.sound("block.amethyst_block.chime", 1.2f, 1.7f));
        s.every(10, 34, 2, t -> {
            s.dust(s.loc(s.rd(-1.5, 1.5), s.rd(0.3, 2.6), s.rd(-1.5, 1.5)), AME, 0.8f, 4, 0.2);
            s.particle(P.WAX_OFF, s.loc(s.rd(-1.2, 1.2), s.rd(0.4, 2.4), s.rd(-1.2, 1.2)), 2, 0.2, 0);
            s.particle(P.ENCHANTED_HIT, s.loc(0, s.rd(0.5, 2.8), 0), 1, 0.25, 0.02);
        });
    }

    private static double growth(int t, int st) {
        double k = (t - st) / 3.0;
        return k <= 1 ? Math.max(0.001, easeOutBack(k)) : 1.0;
    }

    /** Konik kristalin seg. bolumu (g = buyume 0..1+). */
    private static com.jestron.jestronpriv.fx.api.Transformation crystal(Vector3f base, Quaternionf q, float w, double H, int seg, double g) {
        double y0 = 0;
        for (int i = 0; i < seg; i++) y0 += SEG[i][1] * H * g;
        double h = Math.max(0.001, SEG[seg][1] * H * g);
        double ww = w * SEG[seg][0];
        return cube(new Vector3f(base).add(rot(q, v(0, y0 + h / 2 - (seg > 0 ? 0.01 : 0), 0))), q, v(ww, h, ww));
    }
}
