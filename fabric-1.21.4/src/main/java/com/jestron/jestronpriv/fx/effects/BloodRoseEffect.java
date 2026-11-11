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
/** Yerden dev bir kan gulu filizlenir, yapraklarini acar, sonra yapraklar ruzgarla savrulur. */
public final class BloodRoseEffect extends KillEffect {

    public BloodRoseEffect() {
        super("kan_gulu", "&4&lKan Gülü", Material.POPPY, true,
                "Yerden dev bir kan gülü filizlenir,",
                "açar ve yaprakları savrulur.");
    }

    private static final double TOP = 1.7;
    private static final int PETALS = 10;

    @Override
    public void play(FxSession s) {
        // Sap: yerden yukari buyur
        block(s, Material.MOSS_BLOCK, LIT, 2, 40, 1, 1, t -> {
            double k = smooth(phase(t, 2, 10));
            double h = Math.max(0.02, TOP * k);
            double sway = Math.sin(t * 0.25) * 0.03;
            return Pose.of(v(sway, h / 2, 0), qZ(sway), v(0.09, h, 0.09));
        });
        // Iki yaprak
        for (int i = 0; i < 2; i++) {
            final int side = i == 0 ? 1 : -1;
            final double y = i == 0 ? 0.65 : 1.0;
            block(s, Material.OAK_LEAVES, LIT, 6 + i * 2, 40, 2, 3, t -> {
                double k = smooth(phase(t, 6, 12));
                Quaternionf q = mul(s.face(), qY(side * 0.6), qZ(side * (0.5 + 0.1 * Math.sin(t * 0.3))));
                return Pose.of(rot(s.face(), v(side * 0.2 * k, y, 0)), q, v(0.42 * k, 0.03, 0.2 * k));
            });
        }
        // Gul goncasi: ic cekirdek
        block(s, Material.NETHER_WART_BLOCK, LIT, 10, 32, 1, 2, t -> Pose.of(v(0, TOP + 0.1, 0), qY(t * 0.05), v(0.3, 0.32, 0.3)));
        // Tac yapraklari: kapali -> acik -> kopup savrulur
        for (int i = 0; i < PETALS; i++) {
            final double base = i * Math.PI * 2 / PETALS + (i % 2) * 0.3;
            final boolean inner = i % 2 == 1;
            final Vector3f drift = v(Math.cos(base) * s.rd(0.9, 1.6), s.rd(0.6, 1.2), Math.sin(base) * s.rd(0.9, 1.6));
            final double spinRate = s.rd(0.25, 0.5);
            block(s, inner ? Material.RED_TERRACOTTA : Material.RED_CONCRETE, LIT, 10, 42, 1, 2, t -> {
                double open = smooth(phase(t, 12, 22)) * (inner ? 0.75 : 1.0);
                double pitch = lerp(1.35, inner ? 0.55 : 0.25, open);       // dikten yataya acilir
                double ang = base + t * 0.04;
                double r = 0.12 + 0.24 * open;
                Vector3f c = v(Math.cos(ang) * r, TOP + 0.12 + (inner ? 0.05 : 0), Math.sin(ang) * r);
                Quaternionf q = mul(qY(-ang), qZ(pitch));
                if (t > 30) {                                               // ruzgarla kopus
                    double u = (t - 30) / 12.0;
                    c.add(new Vector3f(drift).mul((float) (u * 1.6))).add(0, (float) (-u * u * 0.6), 0);
                    q = mul(q, qX(u * spinRate * 12));
                }
                float len = inner ? 0.42f : 0.56f;
                return Pose.of(c, q, v(len, 0.045, inner ? 0.24 : 0.32));
            });
        }
        // Parcaciklar
        s.every(4, 10, 2, t -> s.particle(P.BLOCK, s.loc(0, 0.1, 0), 6, 0.2, 0.05, 0.2, 0.1, s.groundData));
        s.every(14, 30, 3, t -> s.dust(s.loc(0, TOP + 0.2, 0), Color.fromRGB(190, 10, 30), 1.0f, 3, 0.35));
        s.at(22, () -> {
            s.particle(P.HEART, s.loc(0, TOP + 0.5, 0), 3, 0.4, 0);
            s.dust(s.loc(0, TOP + 0.1, 0), Color.fromRGB(255, 40, 60), 1.4f, 18, 0.5);
        });
        s.every(30, 42, 2, t -> s.dustT(s.loc(s.rd(-1.2, 1.2), TOP + s.rd(-0.3, 0.8), s.rd(-1.2, 1.2)),
                Color.fromRGB(200, 20, 40), Color.fromRGB(80, 0, 10), 1.1f, 2, 0.2));

        // Ses tasarimi
        s.sound("block.grass.place", 0.9f, 0.7f);
        s.at(4, () -> s.sound("block.big_dripleaf.tilt_up", 0.8f, 0.8f));
        s.at(8, () -> s.sound("block.grass.place", 0.8f, 1.1f));
        s.at(12, () -> s.sound("block.amethyst_block.chime", 0.8f, 0.7f));
        s.at(18, () -> s.sound("block.amethyst_block.chime", 0.9f, 1.0f));
        s.at(22, () -> {
            s.sound("block.note_block.chime", 0.8f, 0.85f);
            s.sound("block.amethyst_block.chime", 1.0f, 1.3f);
        });
        s.at(30, () -> s.sound("block.cherry_leaves.break", 1.0f, 0.8f));
        s.at(33, () -> s.sound("block.cherry_leaves.break", 0.8f, 1.1f));
    }
}
