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
/** Etrafinda End portali cercevesi yukselir, gozler tek tek yerlesir, portal kurbani yutar. */
public final class EndPortalEffect extends KillEffect {

    public EndPortalEffect() {
        super("end_portali", "&5&lEnd Portalı", Material.ENDER_EYE, true,
                "End portalı çerçevesi yükselir, gözler",
                "yerleşir ve portal kurbanı yutar.");
    }

    private static final int N = 12;
    private static final double R = 1.35;
    private static final int OPEN = 19;

    @Override
    public void play(FxSession s) {
        // Cerceve bloklari halka halinde yukselir
        for (int i = 0; i < N; i++) {
            final double a = i * Math.PI * 2 / N;
            final int st = 2 + i / 3;
            final Quaternionf q = qY(-a);
            block(s, Material.END_STONE, LIT, st, 41, 1, 1, t -> {
                double k = easeOutBack(phase(t, st, st + 5));
                return Pose.of(v(Math.cos(a) * R, -0.3 + 0.55 * k, Math.sin(a) * R), q, v(0.5, 0.42, 0.5));
            });
            block(s, Material.CYAN_CONCRETE, LIT, st, 41, 1, 1, t -> {
                double k = easeOutBack(phase(t, st, st + 5));
                return Pose.of(v(Math.cos(a) * R, -0.3 + 0.55 * k + 0.22, Math.sin(a) * R), q, v(0.42, 0.04, 0.42));
            });
            // Ender gozu yerlesir
            final int eyeAt = 7 + i;
            item(s, Material.ENDER_EYE, BRIGHT, eyeAt, 41, 1, 2, t -> {
                double drop = 1 - smooth(phase(t, eyeAt, eyeAt + 3));
                double y = 0.52 + drop * 0.6 + (t > 33 ? (t - 33) * 0.25 : 0);
                return Pose.of(v(Math.cos(a) * R, y, Math.sin(a) * R), mul(qY(-a + t * 0.1), qX(-Math.PI / 2)), 0.32f);
            });
            s.at(eyeAt + 3, () -> s.sound("block.end_portal_frame.fill", 0.8f, 0.9f + eyeAt * 0.02f));
        }
        // Portal yuzeyi: karanlik disk + donen yildiz katmanlari
        block(s, Material.BLACK_CONCRETE, DARK, OPEN, 40, 1, 3, t -> Pose.of(v(0, 0.2, 0), qY(t * 0.02), v(2.2, 0.02, 2.2)));
        for (int l = 0; l < 3; l++) {
            final int layer = l;
            final Material m = l == 0 ? Material.PURPLE_STAINED_GLASS : l == 1 ? Material.CYAN_STAINED_GLASS : Material.MAGENTA_STAINED_GLASS;
            block(s, m, BRIGHT, OPEN + l, 40, 1, 3, t -> {
                double pulse = 1 + 0.05 * Math.sin(t * 0.8 + layer);
                return Pose.of(v(0, 0.22 + layer * 0.012, 0), qY(t * (0.12 + layer * 0.07) * (layer == 1 ? -1 : 1)),
                        v(1.9 * pulse - layer * 0.35, 0.01, 1.9 * pulse - layer * 0.35));
            });
        }
        // Kurbanin kafasi portala cekilir
        item(s, s.victimHead, LIT, OPEN + 1, 34, 1, 3, t -> {
            double k = smooth(phase(t, OPEN + 3, 34));
            return Pose.of(v(0, 1.4 - k * 1.3, 0), qY(t * 0.35), (float) (0.75 * (1 - k * 0.8)));
        });
        // Parcaciklar
        s.every(OPEN, 40, 1, t -> {
            s.particle(P.PORTAL, s.loc(s.rd(-1, 1), 0.3, s.rd(-1, 1)), 4, 0.1, 0.4);
            if (t % 3 == 0) s.particle(P.REVERSE_PORTAL, s.loc(0, 0.25, 0), 6, 0.6, 0.02);
        });
        s.at(OPEN, () -> s.particle(P.FLASH, s.loc(0, 0.6, 0), 1, 0, 0));
        s.at(34, () -> {
            s.particle(P.END_ROD, s.loc(0, 0.6, 0), 30, 0.3, 0.25);
            s.particle(P.PORTAL, s.loc(0, 0.5, 0), 60, 0.5, 1.2);
        });
        // Ses tasarimi
        s.sound("block.stone.break", 0.6f, 0.6f);
        s.at(4, () -> s.sound("block.stone.break", 0.6f, 0.7f));
        s.at(OPEN, () -> {
            s.sound("block.end_portal.spawn", 0.9f, 1.0f);
            s.sound("block.respawn_anchor.charge", 0.7f, 0.6f);
        });
        s.at(OPEN + 2, () -> s.longSound("block.portal.ambient", 0.5f, 0.9f));
        s.at(28, () -> s.sound("entity.enderman.teleport", 0.9f, 0.6f));
        s.at(34, () -> {
            s.sound("entity.ender_eye.death", 1.0f, 0.8f);
            s.sound("entity.enderman.teleport", 0.8f, 1.2f);
        });
    }
}
