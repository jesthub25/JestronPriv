package com.jestron.jestronpriv.fx.effects;

import com.jestron.jestronpriv.fx.FxSession;
import com.jestron.jestronpriv.fx.api.Transformation;
import com.jestron.jestronpriv.fx.api.ItemDisplay;
import com.jestron.jestronpriv.fx.api.Location;
import com.jestron.jestronpriv.fx.FxKit;
import com.jestron.jestronpriv.fx.KillEffect;
import com.jestron.jestronpriv.fx.P;
import com.jestron.jestronpriv.fx.api.Color;
import com.jestron.jestronpriv.fx.api.Material;
import com.jestron.jestronpriv.fx.api.BlockDisplay;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

import static com.jestron.jestronpriv.fx.FxMath.*;

/** Bir kasirga olusur, yerden parcalari kapip doner ve sonunda etrafa savurur. */
public final class TornadoEffect extends KillEffect {

    private static final double HEIGHT = 4.6;
    private static final Color GREY = Color.fromRGB(205, 205, 215);

    public TornadoEffect() {
        super("kasirga", "&f&lKasırga", Material.WIND_CHARGE, true,
                "Bir kasırga oluşur, yerden parçaları",
                "kapıp döndürür ve savurur.");
    }

    private static double radiusAt(double y) {
        return 0.25 + Math.pow(y / HEIGHT, 1.3) * 1.9;
    }

    private static double form(int t) {
        if (t < 10) return easeOut((t - 2) / 8.0);
        if (t <= 38) return 1.0;
        return Math.max(0, 1 - (t - 38) / 6.0);
    }

    @Override
    public void play(FxSession s) {
        playBase(s);
        extras(s);
    }

    private void playBase(FxSession s) {
        s.every(2, 43, 1, t -> {
            double f = form(t);
            if (f <= 0) return;
            double lift = t > 38 ? (t - 38) * 0.25 : 0;
            for (int k = 0; k < 12; k++) {
                double y = k * 0.4;
                if (y > HEIGHT * f) break;
                double r = radiusAt(y) * (0.6 + 0.4 * f);
                for (int j = 0; j < 3; j++) {
                    double a = t * 0.55 + k * 0.5 + j * Math.PI * 2 / 3;
                    if ((k + j) % 2 == 0) {
                        s.particle(P.WHITE_SMOKE, s.loc(Math.cos(a) * r, y + lift, Math.sin(a) * r), 1, 0.02, 0);
                    } else {
                        s.dust(s.loc(Math.cos(a) * r, y + lift, Math.sin(a) * r), GREY, 1.1f, 1, 0);
                    }
                }
            }
            if (t % 2 == 0 && t <= 40) s.groundCrack(s.loc(0, 0.1, 0), 6, 0.8);
        });

        // Savrulan parcalar
        Material g = s.groundMaterial(Material.DIRT);
        Material[] mats = {g, Material.OAK_LEAVES, Material.DIRT, Material.COBBLESTONE, Material.OAK_PLANKS, g, Material.MOSS_BLOCK};
        for (int i = 0; i < mats.length; i++) {
            final int k = i;
            final float size = (float) s.rd(0.25, 0.4);
            final Quaternionf r0 = mul(qY(s.rnd.nextDouble() * 6), qX(s.rnd.nextDouble() * 6));
            final Vector3f ax = randomUnit(s.rnd);
            final double phase = i * Math.PI * 2 / mats.length;
            BlockDisplay d = s.lit(s.block(mats[i], cube(v(0, 0.2, 0), r0, 0.001f)));
            s.anim(d, 4, 38, 2, t -> {
                double u = (t - 4) / 34.0;
                double y = lerp(0.3, 4.0, u) + 0.15 * Math.sin(t * 0.4 + k);
                double a = phase + t * 0.45;
                double r = radiusAt(y) * 0.85;
                float sc = t < 8 ? (float) (size * easeOut((t - 4) / 4.0)) : size;
                return cube(v(Math.cos(a) * r, y, Math.sin(a) * r), mul(qAxis(t * 0.4, ax), r0), Math.max(0.001f, sc));
            });
            double a38 = phase + 38 * 0.45;
            double y38 = 4.0 + 0.15 * Math.sin(38 * 0.4 + k);
            double r38 = radiusAt(y38) * 0.85;
            double sx = Math.cos(a38) * r38, sz = Math.sin(a38) * r38;
            // Teget yonde savrulma
            double vx = -Math.sin(a38) * 0.3 + Math.cos(a38) * 0.12, vz = Math.cos(a38) * 0.3 + Math.sin(a38) * 0.12;
            List<double[]> path = s.ballistic(sx, y38, sz, vx, 0.1, vz, 0.05, size / 2, 10);
            Vector3f last = v(sx, y38, sz);
            Quaternionf lastR = mul(qAxis(38 * 0.4, ax), r0);
            for (int j = 2; j < path.size() && 38 + j <= 44; j += 2) {
                double[] p = path.get(j);
                last = v(p[0], p[1], p[2]);
                lastR = mul(qAxis((38 + j) * 0.4, ax), r0);
                s.tfAt(38 + j, d, cube(last, lastR, size), 2);
            }
            s.tfAt(45, d, cube(last, lastR, 0.001f), 4);
        }

        s.at(2, () -> s.longSound("item.elytra.flying", 0.7f, 1.3f));
        s.at(14, () -> s.sound(s.loc(0, 2, 0), "entity.ender_dragon.flap", 0.5f, 1.6f));
        s.at(38, () -> s.sound(s.loc(0, 3, 0), "entity.wind_charge.wind_burst", 0.9f, 0.7f));
    }

    /** Gercekcilik katmani: ek 3D detaylar, enkaz, sok dalgalari ve parlamalar. */
    private void extras(FxSession s) {

        // Hacimli huni: donen yari saydam gri katmanlar
        for (int k = 0; k < 5; k++) {
            final int kk = k;
            final double y = 0.45 + k * 0.9;
            BlockDisplay d = s.bright(s.block(k % 2 == 0 ? Material.LIGHT_GRAY_STAINED_GLASS : Material.GRAY_STAINED_GLASS, cube(v(0, y, 0), mul(qY(4 * 0.5 + kk), qX(0.15 * Math.sin(4 * 0.3 + kk))), 0.001f)));
            s.anim(d, 4, 44, 2, t -> {
                double f = form(t);
                double lift = t > 38 ? (t - 38) * 0.25 : 0;
                double r = radiusAt(y) * 1.25 * (0.6 + 0.4 * f) * f;
                return cube(v(0, y + lift, 0), mul(qY(t * 0.5 + kk), qX(0.15 * Math.sin(t * 0.3 + kk))),
                        v(Math.max(0.001, r), 0.85 * f + 0.001, Math.max(0.001, r)));
            });
            s.tfAt(45, d, cube(v(0, y + 1.5, 0), mul(qY(44 * 0.5 + kk), qX(0.15 * Math.sin(44 * 0.3 + kk))), 0.001f), 1);
        }
        // Ters yonde donen ikinci enkaz halkasi
        Material g = s.groundMaterial(Material.DIRT);
        Material[] mats = {Material.GRAVEL, g, Material.OAK_LEAVES, Material.COBBLESTONE, Material.OAK_PLANKS, Material.DIRT};
        for (int i = 0; i < mats.length; i++) {
            final int k = i;
            final float size = (float) s.rd(0.18, 0.3);
            final Quaternionf r0 = mul(qY(s.rnd.nextDouble() * 6), qX(s.rnd.nextDouble() * 6));
            final Vector3f ax = randomUnit(s.rnd);
            final double phase = i * Math.PI * 2 / mats.length + 0.4;
            BlockDisplay d = s.lit(s.block(mats[i], cube(v(0, 0.2, 0), mul(qAxis(6 * 0.5, ax), r0), 0.001f)));
            s.anim(d, 6, 40, 2, t -> {
                double u = (t - 6) / 34.0;
                double y = lerp(0.2, 3.4, u) + 0.2 * Math.sin(t * 0.5 + k);
                double a = phase - t * 0.52;
                double r = radiusAt(y) * 0.7;
                float sc = t < 10 ? (float) (size * (t - 6) / 4.0) : (t > 36 ? (float) (size * Math.max(0, 40 - t) / 4.0) : size);
                return cube(v(Math.cos(a) * r, y, Math.sin(a) * r), mul(qAxis(t * 0.5, ax), r0), Math.max(0.001f, sc));
            });
            s.tfAt(41, d, cube(v(0, 3, 0), mul(qAxis(40 * 0.5, ax), r0), 0.001f), 1);
        }
        // Huninin icinde cakan simsekler
        s.at(20, () -> {
            s.flash(s.loc(0, 2.5, 0), Color.WHITE);
            s.sound("entity.lightning_bolt.impact", 0.35f, 1.4f);
        });
        s.at(31, () -> {
            s.flash(s.loc(0.4, 3.2, 0), Color.WHITE);
            s.sound("entity.lightning_bolt.impact", 0.3f, 1.6f);
        });
        FxKit.shockRing(s, 0.04, 0.6, 2.8, 4, 12, Material.LIGHT_GRAY_STAINED_GLASS, 18, 0.2f, false);
    }
}
