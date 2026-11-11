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

/** Kurban voksellere ayrisir, titreyerek bozulur ve isik sutunu icinde yukari "yuklenir". */
public final class QuantumEffect extends KillEffect {

    public QuantumEffect() {
        super("kuantum", "&b&lKuantum Parçalanma", Material.ECHO_SHARD, true,
                "Kurban voksellere ayrışır, titreşir",
                "ve ışık sütununda yukarı yüklenir.");
    }

    private static final int GLITCH_END = 14, LIFT = 16, GONE = 36;
    private static final Color CYAN = Color.fromRGB(60, 240, 255), MAG = Color.fromRGB(255, 70, 220);

    /** Oyuncu biciminde voksel noktalari (yerel x, y, z). */
    private static final float[][] VOX;

    static {
        java.util.List<float[]> l = new java.util.ArrayList<>();
        for (int x = 0; x < 2; x++) for (int y = 0; y < 2; y++) for (int z = 0; z < 2; z++) l.add(new float[]{-0.12f + x * 0.24f, 1.62f + y * 0.24f, -0.12f + z * 0.24f});
        for (int x = 0; x < 2; x++) for (int y = 0; y < 3; y++) l.add(new float[]{-0.12f + x * 0.24f, 0.86f + y * 0.24f, 0f});
        for (int y = 0; y < 3; y++) {
            l.add(new float[]{-0.36f, 0.86f + y * 0.24f, 0f});
            l.add(new float[]{0.36f, 0.86f + y * 0.24f, 0f});
        }
        for (int y = 0; y < 3; y++) {
            l.add(new float[]{-0.12f, 0.12f + y * 0.24f, 0f});
            l.add(new float[]{0.12f, 0.12f + y * 0.24f, 0f});
        }
        VOX = l.toArray(new float[0][]);
    }

    @Override
    public void play(FxSession s) {
        final Quaternionf face = s.face();
        Material[] mats = {Material.CYAN_STAINED_GLASS, Material.MAGENTA_STAINED_GLASS, Material.WHITE_STAINED_GLASS, Material.LIGHT_BLUE_STAINED_GLASS};
        for (int i = 0; i < VOX.length; i++) {
            final float[] p = VOX[i];
            final Material m = mats[i % mats.length];
            final long seed = 7919L * i;
            final Vector3f out = randomUnit(new java.util.Random(seed));
            final int lift = LIFT + (int) ((2.0 - p[1]) * 4); // ust kisimlar once yuklenir
            block(s, m, BRIGHT, 2, GONE, 1, 2, t -> {
                Vector3f base = rot(face, v(p[0], p[1], p[2]));
                if (t < GLITCH_END) {
                    // titreyen bozulma: her 2 tick'te kucuk sicrama (sabit tohum, akici)
                    java.util.Random r = new java.util.Random(seed + (t / 2) * 31L);
                    float j = t > 6 ? 0.09f : 0.03f;
                    base.add((float) ((r.nextDouble() - 0.5) * j), (float) ((r.nextDouble() - 0.5) * j), (float) ((r.nextDouble() - 0.5) * j));
                    return Pose.of(base, face, 0.22f);
                }
                double k = smooth(phase(t, Math.max(GLITCH_END, lift - 4), lift + 8));
                // once hafifce disari, sonra merkez sutununa kivrilip yukari
                Vector3f p2 = new Vector3f(base).add(new Vector3f(out).mul((float) (0.5 * Math.sin(k * Math.PI))));
                p2.x *= (float) (1 - k);
                p2.z *= (float) (1 - k);
                p2.y += (float) (k * 3.5);
                return Pose.of(p2, mul(face, qY(k * 6), qX(k * 4)), (float) (0.22 * (1 - k * 0.85)));
            });
        }
        FxKit.pillar(s, Material.CYAN_STAINED_GLASS, 0, 0, 0, 6, 0.7f, LIFT, GONE + 2);
        FxKit.pillar(s, Material.WHITE_STAINED_GLASS, 0, 0, 0, 6, 0.3f, LIFT, GONE);
        // tarama halkasi: yukaridan asagi geçen isik
        block(s, Material.MAGENTA_STAINED_GLASS, BRIGHT, 2, GLITCH_END, 1, 1, t -> {
            double y = 2.1 - phase(t, 2, GLITCH_END) * 2.1;
            return Pose.of(v(0, y, 0), qY(t * 0.4), v(1.0, 0.03, 1.0));
        });
        s.every(2, GLITCH_END, 2, t -> {
            s.particle(P.ELECTRIC_SPARK, s.loc(0, 1.0, 0), 6, 0.4, 0.05);
            s.dust(s.loc(0, s.rd(0.2, 2.0), 0), t % 4 == 0 ? CYAN : MAG, 0.8f, 3, 0.35);
        });
        s.sound("block.beacon.deactivate", 1.0f, 1.8f);
        s.every(4, GLITCH_END, 3, t -> s.sound("entity.enderman.teleport", 0.5f, s.rp(1.4f, 2.0f)));
        s.at(GLITCH_END, () -> {
            s.sound("entity.illusioner.mirror_move", 1.0f, 0.8f);
            s.sound("block.respawn_anchor.deplete", 0.8f, 1.6f);
            s.particle(P.FLASH, s.loc(0, 1.0, 0), 1, 0, 0);
        });
        s.every(LIFT, GONE, 2, t -> {
            s.particle(P.END_ROD, s.loc(0, s.rd(0.5, 5.5), 0), 3, 0.25, 0.05);
            s.particle(P.PORTAL, s.loc(0, 1.0, 0), 6, 0.5, 0.5);
        });
        s.at(LIFT, () -> s.longSound("block.beacon.ambient", 0.8f, 1.6f));
        s.at(GONE, () -> {
            s.stopSound("block.beacon.ambient");
            s.sound("block.beacon.power_select", 1.0f, 1.4f);
            s.particle(P.FLASH, s.loc(0, 4.0, 0), 1, 0, 0);
            s.dust(s.loc(0, 4.0, 0), CYAN, 1.5f, 30, 0.8);
        });
    }
}
