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
/** Yerde kirmizi bir nisangah kilitlenir, yorungeden dev bir lazer iner ve zemini eritir. */
public final class OrbitalLaserEffect extends KillEffect {

    public OrbitalLaserEffect() {
        super("yorunge_lazeri", "&c&lYörünge Lazeri", Material.REDSTONE_BLOCK, true,
                "Yerde kırmızı bir nişangah kilitlenir,",
                "yörüngeden dev bir lazer iner.");
    }

    private static final int LOCK = 13, FIRE = 16, END = 28;

    @Override
    public void play(FxSession s) {
        final double gy = s.groundDy(0, 0) + 0.03;
        // Nisangah: daralip donen dort kose + merkez arti
        for (int i = 0; i < 4; i++) {
            final double a = i * Math.PI / 2;
            block(s, Material.RED_STAINED_GLASS, BRIGHT, 2, FIRE + 2, 1, 2, t -> {
                double k = smooth(phase(t, 2, LOCK));
                double r = lerp(2.4, 0.75, k);
                double spin = a + (1 - k) * 2.5;
                return Pose.of(v(Math.cos(spin) * r, gy, Math.sin(spin) * r), qY(-spin + Math.PI / 4), v(0.5, 0.02, 0.08));
            });
        }
        for (int i = 0; i < 2; i++) {
            final double a = i * Math.PI / 2;
            block(s, Material.RED_CONCRETE, BRIGHT, 4, FIRE + 2, 1, 2, t -> Pose.of(v(0, gy + 0.005, 0), qY(a + t * 0.1), v(0.9, 0.02, 0.05)));
        }
        // Hedefleme isini (ince) ve ana lazer (kalin cekirdek + kirmizi kabuk)
        block(s, Material.RED_STAINED_GLASS, BRIGHT, LOCK, FIRE, 1, 1, t -> Pose.of(v(0, 12, 0), q(), v(0.05, 24, 0.05)));
        block(s, Material.WHITE_STAINED_GLASS, BRIGHT, FIRE, END, 1, 2, t -> {
            double w = 0.45 + 0.06 * Math.sin(t * 2.1);
            return Pose.of(v(0, 12, 0), qY(t * 0.3), v(w, 24, w));
        });
        block(s, Material.RED_STAINED_GLASS, BRIGHT, FIRE, END, 1, 2, t -> {
            double w = 0.95 + 0.12 * Math.sin(t * 1.7);
            return Pose.of(v(0, 12, 0), qY(-t * 0.2), v(w, 24, w));
        });
        // Erimis zemin
        block(s, Material.MAGMA_BLOCK, BRIGHT, FIRE + 1, 40, 1, 3, t -> {
            double k = smooth(phase(t, FIRE + 1, FIRE + 6)) * (t > 34 ? 1 - phase(t, 34, 40) : 1);
            return Pose.of(v(0, gy, 0), qY(0.3), v(1.4 * k + 0.01, 0.03, 1.4 * k + 0.01));
        });
        FxKit.impact(s, 1.1, FIRE, Color.fromRGB(255, 60, 40), true);
        FxKit.cracks(s, 8, 1.7, Material.MAGMA_BLOCK, FIRE, 4, 2, 0.07);
        FxKit.shockRing(s, 0.05, 0.4, 3.0, FIRE, 8, Material.RED_STAINED_GLASS, 16, 0.25f, true);
        FxKit.shockRing(s, 0.05, 0.3, 2.2, END, 7, Material.ORANGE_STAINED_GLASS, 14, 0.18f, true);
        // Parcaciklar
        s.every(FIRE, END, 1, t -> {
            s.dir(P.FLAME, s.loc(s.rd(-0.4, 0.4), 0.2, s.rd(-0.4, 0.4)), s.rd(-0.4, 0.4), 0.6, s.rd(-0.4, 0.4), s.rd(0.15, 0.3));
            s.particle(P.LAVA, s.loc(0, 0.2, 0), 1, 0.3, 0);
            if (t % 2 == 0) s.dust(s.loc(0, s.rd(0.5, 5), 0), Color.fromRGB(255, 50, 40), 1.4f, 3, 0.3);
        });
        s.every(END, 40, 2, t -> s.particle(P.LARGE_SMOKE, s.loc(0, 0.3, 0), 2, 0.4, 0.04));
        // Ses tasarimi: hizlanan bip, sarj, atis
        int[] beeps = {3, 6, 8, 10, 11, 12, 13};
        for (int b : beeps) s.at(b, () -> s.sound("block.note_block.bit", 0.8f, 1.2f + b * 0.04f));
        s.at(LOCK, () -> {
            s.sound("block.beacon.activate", 0.9f, 1.6f);
            s.sound("block.respawn_anchor.charge", 0.8f, 1.2f);
        });
        s.at(FIRE, () -> {
            s.sound("entity.guardian.attack", 1.0f, 0.6f);
            s.sound("entity.generic.explode", 0.9f, 0.7f);
            s.sound("entity.warden.sonic_boom", 0.5f, 1.4f);
        });
        s.at(FIRE + 1, () -> s.longSound("block.beacon.ambient", 0.8f, 2.0f));
        s.at(END, () -> {
            s.sound("block.beacon.deactivate", 1.0f, 1.4f);
            s.sound("block.fire.extinguish", 0.9f, 0.8f);
        });
    }
}
