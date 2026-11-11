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

/** Kurbanin arkasinda dev bir olum tirpani belirir, tek savurusla biçer; ruhu kafatasiyla goge yukselir. */
public final class ScytheEffect extends KillEffect {

    public ScytheEffect() {
        super("tirpan", "&8&lÖlüm Tırpanı", Material.WITHER_SKELETON_SKULL, true,
                "Dev bir ölüm tırpanı tek savuruşla",
                "biçer; ruh göğe yükselir.");
    }

    private static final int SWING0 = 9, SWING1 = 16, END = 26;
    private static final Vector3f PIVOT = v(0, 1.15, 0);
    private static final Color SOUL = Color.fromRGB(80, 220, 230);

    /** Savurus acisi (yerel Y): arkadan baslar, kurbanin icinden gecip one doner. */
    private static double swing(int t) {
        if (t < SWING0) return -2.2 + Math.sin(t * 0.25) * 0.08;
        double k = phase(t, SWING0, SWING1);
        k = k * k * (3 - 2 * k);
        return -2.2 + k * 4.2;
    }

    private static Quaternionf frame(FxSession s, int t) {
        // tirpan kurbanin etrafinda yatay doner, hafif one egik
        return mul(s.face(), qY(swing(t)), qZ(-0.25));
    }

    @Override
    public void play(FxSession s) {
        // sap: pivottan 1.2 blok uzakta dikey
        block(s, Material.DARK_OAK_LOG, LIT, 3, END, 1, 4, t ->
                attach(PIVOT, frame(s, t), v(0, 0.1, -1.25), q(), v(0.09, 2.7, 0.09)));
        // biçak: sapin tepesinden pivota dogru kivrilan uc parca + keskin kenar
        for (int k = 0; k < 4; k++) {
            final int kk = k;
            block(s, k == 3 ? Material.LIGHT_GRAY_CONCRETE : Material.BLACK_CONCRETE, k == 3 ? BRIGHT : LIT, 4, END, 1, 4, t -> {
                double x = 0.18 + kk * 0.27, y = 1.38 - kk * kk * 0.05;
                return attach(PIVOT, frame(s, t), v(0, y, -1.25 + x), qX(0.18 * kk), v(0.04, 0.16 - kk * 0.025, 0.3));
            });
        }
        block(s, Material.LIGHT_GRAY_STAINED_GLASS, BRIGHT, 5, END, 1, 3, t ->
                attach(PIVOT, frame(s, t), v(0, 1.22, -0.68), qX(0.35), v(0.02, 0.03, 1.15)));
        // savurus izi
        for (int t = SWING0 + 1; t <= SWING1; t++) {
            final int tt = t;
            Pose p = attach(PIVOT, frame(s, tt), v(0, 1.3, -0.7), qX(0.3), v(0.03, 0.18, 1.1));
            FxKit.ghostBlock(s, Material.GRAY_STAINED_GLASS, cube(p.c(), p.q(), p.size()), tt, 5);
        }
        s.particle(P.SMOKE, s.loc(0, 1.2, 0), 1, 0, 0);
        s.every(3, SWING0, 2, t -> s.particle(P.LARGE_SMOKE, s.lloc(0, 1.1, -1.2), 2, 0.3, 0.01));
        s.at(3, () -> {
            s.sound("entity.wither_skeleton.ambient", 1.0f, 0.5f);
            s.sound("particle.soul_escape", 1.0f, 0.6f);
        });
        s.at(SWING0, () -> s.sound("entity.player.attack.sweep", 1.2f, 0.5f));
        // bicme ani
        s.at(13, () -> {
            Location c = s.loc(0, 1.1, 0);
            s.particle(P.FLASH, c, 1, 0, 0);
            s.particle(P.SWEEP_ATTACK, c, 3, 0.4, 0);
            s.particle(P.SOUL_FIRE_FLAME, c, 30, 0.5, 0.08);
            s.dust(c, SOUL, 1.4f, 30, 0.6);
            s.sound("entity.wither.shoot", 0.7f, 0.6f);
            s.sound("entity.player.attack.strong", 1.0f, 0.6f);
            s.sound("particle.soul_escape", 1.2f, 0.8f);
            s.sound("particle.soul_escape", 1.2f, 1.2f);
        });
        // yukselen ruh: kafatasi + ruh alevi sutunu
        item(s, Material.WITHER_SKELETON_SKULL, BRIGHT, 14, 40, 1, 4, t -> {
            double k = smooth(phase(t, 14, 40));
            return Pose.of(v(Math.sin(t * 0.3) * 0.15, 1.5 + k * 2.6, 0), mul(s.face(), qY(t * 0.15)), (float) (0.9 * (1 - k * 0.4)));
        });
        FxKit.glow(s, Material.CYAN_STAINED_GLASS, v(0, 2.3, 0), 0.9f, 16, 36, 0.15);
        s.every(15, 44, 2, t -> {
            double y = 1.4 + phase(t, 14, 40) * 2.6;
            s.particle(P.SOUL, s.loc(s.rd(-0.3, 0.3), y - 0.3, s.rd(-0.3, 0.3)), 1, 0.1, 0.02);
            s.particle(P.SOUL_FIRE_FLAME, s.loc(0, y - 0.5, 0), 3, 0.15, 0.02);
        });
        s.every(18, 38, 6, t -> s.sound("particle.soul_escape", 0.7f, s.rp(0.6f, 1.1f)));
        FxKit.cracks(s, 6, 1.4, Material.BLACK_CONCRETE, 13, 3);
    }
}
