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
/** Yerden lanetli sarmasiklar sarmal cizerek yukselir, uclarinda soldurucu guller acar, sonra kururlar. */
public final class CursedVinesEffect extends KillEffect {

    public CursedVinesEffect() {
        super("sarmasik", "&2&lLanetli Sarmaşık", Material.WITHER_ROSE, true,
                "Yerden lanetli sarmaşıklar sarmal çizer,",
                "soldurucu güller açar ve kururlar.");
    }

    private static final int VINES = 5, SEGS = 9, BLOOM = 18, SQUEEZE = 22, WITHER = 34;

    private static Vector3f point(int vine, double u, int t) {
        double a = vine * Math.PI * 2 / VINES + u * 3.4 + t * 0.03;
        double squeeze = t > SQUEEZE ? 1 - 0.35 * smooth(phase(t, SQUEEZE, SQUEEZE + 6)) : 1;
        double r = (0.85 - u * 0.35) * squeeze;
        return v(Math.cos(a) * r, 0.05 + u * 2.2, Math.sin(a) * r);
    }

    @Override
    public void play(FxSession s) {
        for (int vi = 0; vi < VINES; vi++) {
            final int vine = vi;
            for (int sg = 0; sg < SEGS; sg++) {
                final double u0 = sg / (double) SEGS, u1 = (sg + 1) / (double) SEGS;
                final int appear = 2 + sg * 2 + vi / 2;
                final Material m = sg % 3 == 2 ? Material.OAK_LEAVES : Material.MOSS_BLOCK;
                block(s, m, LIT, appear, 41, 1, 2, t -> {
                    Vector3f a = point(vine, u0, t), b = point(vine, u1, t);
                    Vector3f mid = lerp(a, b, 0.5);
                    Vector3f dir = new Vector3f(b).sub(a);
                    float len = dir.length() + 0.04f;
                    double wither = t > WITHER ? 1 - smooth(phase(t, WITHER, 41)) * 0.6 : 1;
                    float th = (float) ((0.13 - u0 * 0.05) * wither);
                    return Pose.of(mid, fromTo(v(0, 1, 0), dir.normalize()), v(th, len, th));
                });
                // Dikenler
                if (sg % 2 == 1) {
                    block(s, Material.BLACK_CONCRETE, DARK, appear + 1, 40, 1, 2, t -> {
                        Vector3f p = point(vine, (u0 + u1) / 2, t);
                        Vector3f out = new Vector3f(p.x, 0, p.z).normalize().mul(0.08f);
                        return Pose.of(new Vector3f(p).add(out), fromTo(v(0, 1, 0), new Vector3f(out).normalize()), v(0.03, 0.12, 0.03));
                    });
                }
            }
            // Uctaki soldurucu gul
            block(s, Material.WITHER_ROSE, LIT, BLOOM + vi, 40, 1, 3, t -> {
                Vector3f p = point(vine, 1.0, t);
                double k = easeOutBack(phase(t, BLOOM + vine, BLOOM + vine + 4));
                double wither = t > WITHER ? 1 - smooth(phase(t, WITHER, 40)) : 1;
                return Pose.of(new Vector3f(p).add(0, 0.12f, 0), qY(t * 0.05 + vine), (float) (0.42 * k * wither + 0.001));
            });
        }
        // Zemin: kuru cali ve yosun lekesi
        block(s, Material.MOSS_BLOCK, LIT, 3, 41, 1, 4, t -> Pose.of(v(0, s.groundDy(0, 0) + 0.02, 0), qY(0.5), v(2.0 * smooth(phase(t, 3, 10)) + 0.01, 0.03, 2.0 * smooth(phase(t, 3, 10)) + 0.01)));
        for (int i = 0; i < 4; i++) {
            final double a = i * Math.PI / 2 + 0.4;
            block(s, Material.DEAD_BUSH, LIT, WITHER, 41, 1, 3, t -> Pose.of(v(Math.cos(a) * 1.1, 0.25, Math.sin(a) * 1.1), qY(a), 0.5f));
        }
        // Parcaciklar
        s.every(2, BLOOM, 2, t -> s.particle(P.BLOCK, s.loc(0, 0.1, 0), 4, 0.6, 0.05, 0.6, 0.1, Material.MOSS_BLOCK));
        s.every(BLOOM, WITHER, 2, t -> {
            s.dust(s.loc(s.rd(-0.8, 0.8), s.rd(0.5, 2.3), s.rd(-0.8, 0.8)), Color.fromRGB(40, 70, 20), 1.0f, 2, 0.1);
            s.particle(P.SMOKE, s.loc(0, 2.2, 0), 2, 0.4, 0.01);
        });
        s.at(SQUEEZE, () -> s.particle(P.DAMAGE_INDICATOR, s.loc(0, 1.2, 0), 8, 0.3, 0.1));
        s.at(WITHER, () -> {
            s.particle(P.LARGE_SMOKE, s.loc(0, 1.2, 0), 14, 0.6, 0.03);
            s.particle(P.SQUID_INK, s.loc(0, 1.5, 0), 10, 0.5, 0.04);
        });
        // Ses tasarimi
        s.sound("block.moss.place", 1.0f, 0.7f);
        s.every(4, 16, 4, t -> s.sound("block.cave_vines.place", 0.9f, s.rp(0.7f, 1.0f)));
        s.every(6, 18, 6, t -> s.sound("block.weeping_vines.place", 0.8f, s.rp(0.6f, 0.9f)));
        s.at(BLOOM, () -> s.sound("block.amethyst_block.chime", 0.6f, 0.5f));
        s.at(SQUEEZE, () -> {
            s.sound("entity.wither.ambient", 0.6f, 0.6f);
            s.sound("block.grass.break", 1.0f, 0.6f);
        });
        s.at(SQUEEZE + 3, () -> s.sound("block.grass.break", 0.9f, 0.8f));
        s.at(WITHER, () -> {
            s.sound("block.grass.break", 1.0f, 0.5f);
            s.sound("entity.wither.ambient", 0.4f, 1.2f);
        });
    }
}
