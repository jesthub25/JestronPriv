package com.jestron.jestronpriv.fx.effects;

import com.jestron.jestronpriv.fx.FxKit;
import com.jestron.jestronpriv.fx.FxSession;
import com.jestron.jestronpriv.fx.KillEffect;
import com.jestron.jestronpriv.fx.api.BlockDisplay;
import com.jestron.jestronpriv.fx.api.Color;
import com.jestron.jestronpriv.fx.api.Material;
import org.joml.Vector3f;

import static com.jestron.jestronpriv.fx.FxMath.*;

/**
 * Yerden donerek yukselen 3D DNA cift sarmali: parlayan cam boncuklar, beyaz baz cubuklari,
 * etrafinda isik parcaciklari ve sarmal buyudukce yukselen melodi.
 */
public final class DnaHelixEffect extends KillEffect {

    private static final Color A = Color.fromRGB(0, 220, 255);
    private static final Color B = Color.fromRGB(255, 40, 200);
    private static final Color RUNG = Color.fromRGB(240, 240, 240);
    private static final double MAX_H = 4.2;
    private static final double R = 0.75;
    private static final int PAIRS = 12;
    private static final float[] NOTES = {0.6f, 0.7f, 0.8f, 0.9f, 1.06f, 1.19f, 1.34f, 1.6f};

    public DnaHelixEffect() {
        super("dna", "&b&lDNA Sarmalı", Material.TWISTING_VINES, true,
                "Dönerek yükselen 3D bir",
                "DNA çift sarmalı ve melodi.");
    }

    private static double top(int t) {
        return Math.min(MAX_H, (t - 1) * 0.3 + 0.2);
    }

    private static double bottom(int t) {
        return t > 36 ? (t - 36) * 0.5 : 0.1;
    }

    @Override
    public void play(FxSession s) {
        // 3D boncuklar ve baz cubuklari
        for (int i = 0; i < PAIRS; i++) {
            final double y = 0.25 + i * (MAX_H - 0.3) / (PAIRS - 1);
            final boolean even = i % 2 == 0;
            BlockDisplay a = s.bright(s.block(Material.CYAN_STAINED_GLASS, bead(2, y, 0, 0.26f)));
            BlockDisplay b = s.bright(s.block(Material.MAGENTA_STAINED_GLASS, bead(2, y, Math.PI, 0.26f)));
            BlockDisplay ca = s.bright(s.block(Material.LIGHT_BLUE_CONCRETE, bead(2, y, 0, 0.13f)));
            BlockDisplay cb = s.bright(s.block(Material.PINK_CONCRETE, bead(2, y, Math.PI, 0.13f)));
            BlockDisplay rung = s.bright(s.block(even ? Material.WHITE_CONCRETE : Material.YELLOW_CONCRETE, beam(v(Math.cos(0.7 + y * 1.6) * 0.01, y, Math.sin(0.7 + y * 1.6) * 0.01), v(-Math.cos(0.7 + y * 1.6) * 0.01, y, -Math.sin(0.7 + y * 1.6) * 0.01), 0.001f)));
            s.anim(a, 2, 46, 2, t -> bead(t, y, 0, 0.26f));
            s.anim(b, 2, 46, 2, t -> bead(t, y, Math.PI, 0.26f));
            s.anim(ca, 2, 46, 2, t -> bead(t, y, 0, 0.13f));
            s.anim(cb, 2, 46, 2, t -> bead(t, y, Math.PI, 0.13f));
            s.anim(rung, 2, 46, 2, t -> {
                double vis = visible(t, y);
                double ang = t * 0.35 + y * 1.6;
                double rl = R * 0.85 * Math.max(0.01, vis);
                Vector3f pa = v(Math.cos(ang) * rl, y, Math.sin(ang) * rl);
                Vector3f pb = v(-Math.cos(ang) * rl, y, -Math.sin(ang) * rl);
                return beam(pa, pb, (float) Math.max(0.001, 0.06 * vis));
            });
        }

        // Parcacik izi: iki iplik boyunca parlayan toz
        s.every(1, 44, 2, t -> {
            double tp = top(t), bt = bottom(t);
            if (bt >= tp) return;
            double phase = t * 0.35;
            for (double y = bt; y <= tp; y += 0.22) {
                double a = phase + y * 1.6;
                double ax = Math.cos(a) * R, az = Math.sin(a) * R;
                s.dust(s.loc(ax, y, az), A, 0.7f, 1, 0);
                s.dust(s.loc(-ax, y, -az), B, 0.7f, 1, 0);
            }
        });
        s.every(2, 40, 4, t -> {
            for (int i = 0; i < 16; i++) {
                double a = i * Math.PI * 2 / 16 + t * 0.1;
                s.dust(s.loc(Math.cos(a) * 1.15, 0.08, Math.sin(a) * 1.15), RUNG, 0.7f, 1, 0);
            }
        });
        FxKit.shockRing(s, 0.04, 0.3, 2.4, 2, 10, Material.CYAN_STAINED_GLASS, 18, 0.2f, true);
        FxKit.pillar(s, Material.LIGHT_BLUE_STAINED_GLASS, 0, 0, 0, MAX_H + 0.5, 0.25f, 4, 40);
        s.at(37, () -> FxKit.sparks(s, s.loc(0, MAX_H, 0), 16, 0.3));

        for (int i = 0; i < NOTES.length; i++) {
            final float pitch = NOTES[i];
            final double y = Math.min(MAX_H, i * 0.6);
            s.at(2 + i * 2, () -> s.sound(s.loc(0, y, 0), "block.note_block.chime", 0.7f, pitch));
        }
        s.at(18, () -> s.sound(s.loc(0, 3, 0), "block.beacon.power_select", 0.7f, 1.6f));
        s.at(36, () -> s.sound(s.loc(0, 3, 0), "block.amethyst_block.chime", 1.0f, 1.2f));
        s.at(44, () -> s.sound(s.loc(0, 4, 0), "entity.experience_orb.pickup", 0.6f, 1.4f));
    }

    /** Boncugun gorunurluk orani: sarmal yukselirken asagidan yukari belirir, sonda asagidan yok olur. */
    private static double visible(int t, double y) {
        double tp = top(t), bt = bottom(t);
        if (y > tp || y < bt) return 0;
        double inTop = Math.min(1, (tp - y) / 0.4);
        double inBot = Math.min(1, (y - bt) / 0.4);
        return Math.max(0, Math.min(inTop, inBot));
    }

    private static com.jestron.jestronpriv.fx.api.Transformation bead(int t, double y, double offset, float size) {
        double vis = visible(t, y);
        double ang = t * 0.35 + y * 1.6 + offset;
        Vector3f c = v(Math.cos(ang) * R, y, Math.sin(ang) * R);
        return cube(c, mul(qY(-ang), qX(0.6)), (float) Math.max(0.001, size * vis));
    }
}
