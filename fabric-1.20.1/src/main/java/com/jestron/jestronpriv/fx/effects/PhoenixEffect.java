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
/** Kurbanin kulunden alevli bir anka kusu dogar, kanat cirparak yukselir ve alev patlamasiyla kaybolur. */
public final class PhoenixEffect extends KillEffect {

    public PhoenixEffect() {
        super("anka", "&6&lAnka Kuşu", Material.FIRE_CHARGE, true,
                "Küllerden alevli bir anka kuşu doğar,",
                "kanat çırparak yükselir ve alevlenir.");
    }

    private static final int BIRTH = 9, BURST = 36;
    /** Kus olcegi. */
    private static final float SC = 1.7f;

    private static Vector3f body(int t) {
        double k = phase(t, BIRTH, BURST);
        return v(Math.sin(t * 0.15) * 0.15, 0.8 + smooth(k) * 2.5 + Math.sin(t * 0.5) * 0.06, 0);
    }

    @Override
    public void play(FxSession s) {
        final Quaternionf face = s.face();
        // Kul yigini
        block(s, Material.GRAY_CONCRETE, LIT, 2, 30, 1, 3, t -> Pose.of(v(0, 0.06, 0), qY(0.4), v(0.8, 0.12 * (t < BIRTH ? 1 : 1 - phase(t, BIRTH, 30) * 0.9), 0.8)));
        block(s, Material.BLACK_CONCRETE, DARK, 2, 30, 1, 3, t -> Pose.of(v(0.1, 0.1, -0.05), qY(1.1), v(0.5, 0.1, 0.5)));
        // Govde, bas, gaga, kuyruk
        Object[][] parts = {
                {Material.ORANGE_WOOL, v(0, 0, 0), v(0.34, 0.3, 0.6), q()},
                {Material.YELLOW_CONCRETE, v(0, 0.2, 0.35), v(0.24, 0.24, 0.24), q()},
                {Material.ORANGE_CONCRETE, v(0, 0.17, 0.53), v(0.08, 0.06, 0.14), q()},
                {Material.RED_STAINED_GLASS, v(0, 0.05, -0.5), v(0.3, 0.05, 0.5), qX(0.35)},
                {Material.ORANGE_STAINED_GLASS, v(0.12, 0.08, -0.62), v(0.12, 0.04, 0.45), mul(qY(0.3), qX(0.4))},
                {Material.ORANGE_STAINED_GLASS, v(-0.12, 0.08, -0.62), v(0.12, 0.04, 0.45), mul(qY(-0.3), qX(0.4))},
                {Material.YELLOW_STAINED_GLASS, v(0, 0.36, 0.3), v(0.06, 0.16, 0.14), qX(-0.4)}};
        for (Object[] p : parts) {
            final Material m = (Material) p[0];
            final Vector3f off = (Vector3f) p[1], size = (Vector3f) p[2];
            final Quaternionf loc = (Quaternionf) p[3];
            block(s, m, BRIGHT, BIRTH, BURST, 1, 3, t -> attach(body(t), mul(face, qX(-0.35)), new Vector3f(off).mul(SC), loc, new Vector3f(size).mul(SC)));
        }
        // Kanatlar: uc segment, cirpma dalgasi
        Material[] wingM = {Material.ORANGE_STAINED_GLASS, Material.YELLOW_STAINED_GLASS, Material.RED_STAINED_GLASS};
        for (int side = -1; side <= 1; side += 2) {
            final int sd = side;
            for (int seg = 0; seg < 3; seg++) {
                final int sg = seg;
                final Material m = wingM[seg];
                block(s, m, BRIGHT, BIRTH + 1, BURST, 1, 3, t -> {
                    double flap = Math.sin(t * 0.9 - sg * 0.5) * 0.6 + 0.15;
                    Quaternionf bodyQ = mul(face, qX(-0.35));
                    // segment kok noktasi: onceki segmentin ucu
                    Vector3f root = v(sd * 0.15, 0.08, 0);
                    double ang = 0;
                    for (int i = 0; i < sg; i++) {
                        double fi = Math.sin(t * 0.9 - i * 0.5) * 0.6 + 0.15;
                        ang += fi * (i == 0 ? 1 : 0.5);
                        root.add((float) (sd * Math.cos(ang) * 0.42), (float) (Math.sin(ang) * 0.42), 0);
                    }
                    ang += flap * (sg == 0 ? 1 : 0.5);
                    Vector3f mid = new Vector3f(root).add((float) (sd * Math.cos(ang) * 0.21), (float) (Math.sin(ang) * 0.21), 0);
                    return attach(body(t), bodyQ, mid.mul(SC), qZ(sd * ang), v(0.44 * SC, 0.05, (0.38 - sg * 0.08) * SC));
                });
            }
        }
        // Dogus ve patlama
        s.at(BIRTH, () -> {
            s.particle(P.FLASH, s.loc(0, 0.6, 0), 1, 0, 0);
            FxKit.embers(s, s.loc(0, 0.3, 0), 16, 0.4);
        });
        s.at(BURST, () -> {
            Vector3f b = body(BURST);
            Location l = s.loc(b.x, b.y, b.z);
            s.particle(P.FLASH, l, 1, 0, 0);
            s.particle(P.EXPLOSION, l, 1, 0, 0);
            for (int i = 0; i < 30; i++) s.dir(P.FLAME, l, randomUnit(s.rnd), s.rd(0.15, 0.35));
            s.particle(P.LAVA, l, 8, 0.4, 0);
        });
        FxKit.shockRing(s, 3.2, 0.3, 2.6, BURST, 6, Material.ORANGE_STAINED_GLASS, 14, 0.12f, true);
        // Ates izi
        s.every(2, BIRTH, 1, t -> s.particle(P.SMOKE, s.loc(0, 0.2, 0), 2, 0.2, 0.02));
        s.every(BIRTH, BURST, 1, t -> {
            Vector3f b = body(t);
            s.particle(P.FLAME, s.loc(b.x, b.y - 0.1, b.z), 3, 0.15, 0.03);
            if (t % 2 == 0) s.dustT(s.loc(b.x + s.rd(-0.8, 0.8), b.y, b.z), Color.fromRGB(255, 200, 40), Color.fromRGB(200, 40, 0), 1.0f, 2, 0.1);
        });
        // Ses tasarimi
        s.sound("block.fire.extinguish", 0.6f, 0.6f);
        s.at(BIRTH, () -> {
            s.sound("entity.blaze.shoot", 1.0f, 0.7f);
            s.sound("item.firecharge.use", 0.9f, 0.8f);
            s.longSound("block.fire.ambient", 1.0f, 1.0f);
        });
        s.every(BIRTH + 3, BURST - 2, 7, t -> {
            s.sound("entity.ender_dragon.flap", 0.6f, 1.5f);
            s.sound("entity.phantom.flap", 0.5f, 0.8f);
        });
        s.at(BURST, () -> {
            s.sound("entity.blaze.death", 0.8f, 0.9f);
            s.sound("item.firecharge.use", 1.0f, 0.6f);
            s.sound("entity.generic.explode", 0.5f, 1.3f);
        });
    }
}
