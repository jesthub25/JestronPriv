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

/** Kurbanin ustunde kapkara bir gunes dogar; alev taci donerken parlamalar savurur, coker ve suparnova olur. */
public final class BlackSunEffect extends KillEffect {

    public BlackSunEffect() {
        super("kara_gunes", "&6&lKara Güneş", Material.COAL_BLOCK, true,
                "Alev taçlı kara bir güneş doğar,",
                "çöker ve süpernova gibi patlar.");
    }

    private static final double Y = 2.3;
    private static final int RAYS = 16, COLLAPSE = 30, NOVA = 33;

    @Override
    public void play(FxSession s) {
        final Quaternionf face = s.face();
        final Vector3f c = v(0, Y, 0);
        // kara kure
        block(s, Material.BLACK_CONCRETE, DARK, 3, NOVA, 1, 6, t -> {
            double k = t < COLLAPSE ? smooth(phase(t, 3, 12)) : 1 - smooth(phase(t, COLLAPSE, NOVA));
            return Pose.of(c, mul(face, qY(t * 0.05), qX(0.6)), (float) (0.01 + 1.15 * k));
        });
        // alev taci: kurbana donuk duzlemde, donen ve nabizlanan isinlar
        Material[] mats = {Material.ORANGE_STAINED_GLASS, Material.YELLOW_STAINED_GLASS, Material.RED_STAINED_GLASS};
        for (int i = 0; i < RAYS; i++) {
            final int ii = i;
            final Material m = mats[i % 3];
            block(s, m, BRIGHT, 5 + i % 4, COLLAPSE, 1, 3, t -> {
                double a = ii * Math.PI * 2 / RAYS + t * 0.06;
                double flare = 0.45 + 0.35 * Math.sin(t * 0.7 + ii * 1.9) + (ii % 4 == 0 ? 0.3 * Math.sin(t * 0.3 + ii) : 0);
                double in = smooth(phase(t, 5, 14));
                double r = 0.62 + flare * 0.5;
                Vector3f p = rot(face, v(Math.cos(a) * r, Math.sin(a) * r, 0)).add(c);
                return Pose.of(p, mul(face, qZ(a)), v(0.9 * flare * in + 0.01, 0.07, 0.04));
            });
        }
        FxKit.glow(s, Material.ORANGE_STAINED_GLASS, c, 1.55f, 6, COLLAPSE, 0.1);
        // gunes patlamalari: kavisli kor yaylari
        s.every(8, COLLAPSE - 2, 3, t -> {
            double a = s.rd(0, Math.PI * 2);
            for (int k = 0; k <= 8; k++) {
                double u = k / 8.0, r = 0.8 + Math.sin(u * Math.PI) * 0.7, aa = a + u * 0.9;
                Vector3f p = rot(face, v(Math.cos(aa) * r, Math.sin(aa) * r, 0)).add(c);
                s.dustT(s.loc(p), Color.fromRGB(255, 220, 80), Color.fromRGB(220, 40, 0), 1.0f, 1, 0.02);
            }
            s.particle(P.FLAME, s.loc(c), 4, 0.8, 0.03);
        });
        s.sound("block.respawn_anchor.charge", 0.7f, 0.5f);
        s.at(4, () -> {
            s.longSound("block.beacon.ambient", 0.8f, 0.5f);
            s.longSound("block.fire.ambient", 1.0f, 0.7f);
            s.sound("block.beacon.activate", 1.0f, 0.6f);
        });
        s.every(10, COLLAPSE, 7, t -> s.sound("item.firecharge.use", 0.5f, s.rp(0.6f, 0.9f)));
        // cokus ve supernova
        s.at(COLLAPSE, () -> {
            s.stopSound("block.beacon.ambient");
            s.stopSound("block.fire.ambient");
            s.sound("block.respawn_anchor.deplete", 1.0f, 0.5f);
            s.particle(P.REVERSE_PORTAL, s.loc(c), 30, 1.2, 0.05);
        });
        block(s, Material.WHITE_STAINED_GLASS, BRIGHT, NOVA, 41, 1, 1, t -> {
            double k = smooth(phase(t, NOVA, 41));
            return Pose.of(c, qY(t * 0.3), (float) (0.3 + k * 4.0));
        });
        FxKit.shockRing(s, Y, 0.4, 4.2, NOVA, 8, Material.ORANGE_STAINED_GLASS, 18, 0.14f, true);
        FxKit.shockRing(s, s.groundDy(0, 0) + 0.02, 0.3, 3.4, NOVA + 1, 8, Material.RED_STAINED_GLASS, 16, 0.18f, true);
        s.at(NOVA, () -> {
            Location l = s.loc(c);
            s.particle(P.FLASH, l, 1, 0, 0);
            s.particle(P.EXPLOSION, l, 3, 0.8, 0);
            for (int i = 0; i < 40; i++) s.dir(P.FLAME, l, randomUnit(s.rnd), s.rd(0.2, 0.45));
            s.particle(P.LAVA, l, 12, 0.6, 0);
            s.sound("entity.generic.explode", 1.1f, 0.6f);
            s.sound("block.end_portal.spawn", 0.6f, 1.2f);
            s.sound("entity.firework_rocket.large_blast", 1.0f, 0.5f);
        });
        s.every(NOVA + 2, 47, 2, t -> s.particle(P.LAVA, s.loc(s.rd(-1.5, 1.5), Y + s.rd(-0.5, 1.0), s.rd(-1.5, 1.5)), 1, 0.1, 0));
    }
}
