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

/** Kurbanin etrafinda alti run dikilitasi yukselir, isinlarla altigen yildiz muhru cizer ve muhur kapanir. */
public final class RuneSealEffect extends KillEffect {

    public RuneSealEffect() {
        super("run_muhru", "&5&lRün Mührü", Material.ENDER_EYE, true,
                "Altı rün dikilitaşı yükselir, ışınlarla",
                "mühür çizer ve kurbanı mühürler.");
    }

    private static final int N = 6, LINK = 12, CLOSE = 27, SINK = 33;
    private static final double R = 2.1;
    private static final Color RUNE = Color.fromRGB(200, 90, 255);

    private static Vector3f top(FxSession s, int i) {
        double a = i * Math.PI * 2 / N;
        double x = Math.cos(a) * R, z = Math.sin(a) * R;
        return v(x, s.groundDy(x, z) + 1.75, z);
    }

    @Override
    public void play(FxSession s) {
        for (int i = 0; i < N; i++) {
            final int ii = i;
            final int st = 2 + i;
            final double a = i * Math.PI * 2 / N;
            final double x = Math.cos(a) * R, z = Math.sin(a) * R, gy = s.groundDy(x, z);
            // dikilitas: yerden yukselir, mühür kapaninca geri batar
            block(s, Material.PURPUR_BLOCK, LIT, st, 42, 1, 2, t -> {
                double up = smooth(phase(t, st, st + 6)) * (1 - smooth(phase(t, SINK, 42)));
                return Pose.of(v(x, gy - 0.8 + up * 1.6, z), qY(-a), v(0.36, 1.6, 0.36));
            });
            block(s, Material.MAGENTA_STAINED_GLASS, BRIGHT, st + 4, 42, 1, 2, t -> {
                double up = smooth(phase(t, st, st + 6)) * (1 - smooth(phase(t, SINK, 42)));
                return Pose.of(v(x, gy - 0.8 + up * 1.6 + 0.95, z), mul(qY(t * 0.2), qX(0.62)), 0.32f);
            });
            s.at(st + 3, () -> {
                s.crack(s.loc(x, gy + 0.1, z), Material.PURPUR_BLOCK, 10, 0.3);
                s.sound(s.loc(x, gy, z), "block.end_portal_frame.fill", 1.0f, 0.8f + ii * 0.1f);
            });
            // altigen yildiz isinlari: i -> i+2
            final int from = LINK + i * 2;
            block(s, Material.PURPLE_STAINED_GLASS, BRIGHT, from, CLOSE + 3, 1, 2, t -> {
                Vector3f p0 = top(s, ii), p1 = top(s, (ii + 2) % N);
                double grow = smooth(phase(t, from, from + 3));
                double shrink = smooth(phase(t, CLOSE, CLOSE + 3));
                Vector3f mid = new Vector3f(p0).add(p1).mul(0.5f);
                Vector3f a0 = lerp(p0, mid, shrink), a1 = lerp(p0, p1, grow);
                a1 = lerp(a1, mid, shrink);
                return beamPose(a0, a1, 0.07f);
            });
            s.at(from, () -> s.sound("block.beacon.power_select", 0.6f, 1.2f + ii * 0.1f));
        }
        // yerde donen muhur halkasi
        for (int k = 0; k < 12; k++) {
            final double a = k * Math.PI * 2 / 12;
            block(s, k % 2 == 0 ? Material.MAGENTA_STAINED_GLASS : Material.PURPLE_STAINED_GLASS, BRIGHT, 6, SINK, 1, 3, t -> {
                double rr = 1.3 + 0.05 * Math.sin(t * 0.4);
                double aa = a + t * 0.07;
                double gy = s.groundDy(0, 0);
                return Pose.of(v(Math.cos(aa) * rr, gy + 0.03, Math.sin(aa) * rr), qY(-aa), v(0.6, 0.03, 0.12));
            });
        }
        FxKit.glow(s, Material.MAGENTA_STAINED_GLASS, v(0, 1.1, 0), 1.2f, LINK + 4, CLOSE + 2, 0.15);
        s.every(LINK, CLOSE, 2, t -> {
            s.particle(P.PORTAL, s.loc(0, 1.1, 0), 10, 1.5, 0.5);
            s.dust(s.loc(s.rd(-R, R), s.rd(0.2, 2.0), s.rd(-R, R)), RUNE, 0.9f, 2, 0.1);
        });
        s.sound("block.enchantment_table.use", 1.0f, 0.6f);
        s.at(LINK, () -> s.longSound("block.beacon.ambient", 0.8f, 1.2f));
        s.at(20, () -> s.sound("entity.evoker.prepare_summon", 0.8f, 1.0f));
        // muhur kapanir
        s.at(CLOSE + 3, () -> {
            Location c = s.loc(0, 1.1, 0);
            s.stopSound("block.beacon.ambient");
            s.particle(P.FLASH, c, 1, 0, 0);
            s.particle(P.REVERSE_PORTAL, c, 40, 0.6, 0.2);
            s.particle(P.END_ROD, c, 30, 0.3, 0.25);
            s.sound("block.end_portal.spawn", 0.8f, 1.3f);
            s.sound("block.respawn_anchor.deplete", 1.0f, 0.8f);
            s.sound("block.beacon.deactivate", 1.0f, 0.7f);
        });
        FxKit.pillar(s, Material.MAGENTA_STAINED_GLASS, 0, 0, 0, 7, 0.8f, CLOSE + 3, CLOSE + 11);
        FxKit.shockRing(s, s.groundDy(0, 0) + 0.02, 0.3, 3.0, CLOSE + 3, 8, Material.PURPLE_STAINED_GLASS, 16, 0.16f, true);
        s.every(CLOSE + 5, 46, 3, t -> s.particle(P.WITCH, s.loc(s.rd(-1.5, 1.5), s.rd(0.3, 2.5), s.rd(-1.5, 1.5)), 2, 0.1, 0.01));
    }
}
