package com.jestron.jestronpriv.fx.effects;

import com.jestron.jestronpriv.fx.FxSession;
import com.jestron.jestronpriv.fx.api.Transformation;
import com.jestron.jestronpriv.fx.api.ItemDisplay;
import com.jestron.jestronpriv.fx.api.Color;
import com.jestron.jestronpriv.fx.FxKit;
import com.jestron.jestronpriv.fx.KillEffect;
import com.jestron.jestronpriv.fx.P;
import com.jestron.jestronpriv.fx.api.Location;
import com.jestron.jestronpriv.fx.api.Material;
import com.jestron.jestronpriv.fx.api.BlockDisplay;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static com.jestron.jestronpriv.fx.FxMath.*;

/** Buz sarkitlari kurbani hapseder, sonra hapishane tuzla buz olur. */
public final class IcePrisonEffect extends KillEffect {

    public IcePrisonEffect() {
        super("buz_hapsi", "&b&lBuz Hapsi", Material.BLUE_ICE, true,
                "Buz kristalleri kurbanı hapseder",
                "ve hapishane paramparça olur.");
    }

    @Override
    public void play(FxSession s) {
        playBase(s);
        extras(s);
    }

    private void playBase(FxSession s) {
        final int n = 8;
        final double R = 0.85;
        final List<BlockDisplay> all = new ArrayList<>();

        // Donmus zemin
        BlockDisplay plate = s.bright(s.block(Material.PACKED_ICE, cube(v(0, 0.03, 0), qY(Math.PI / 4), v(0.001, 0.001, 0.001))));
        s.anim(plate, 2, 8, 2, t -> {
            float sz = (float) (2.3 * easeOut((t - 2) / 6.0));
            return cube(v(0, 0.03, 0), qY(Math.PI / 4), v(sz, 0.05f, sz));
        });
        all.add(plate);

        // Sarkitlar
        for (int i = 0; i < n; i++) {
            double a = i * Math.PI * 2 / n;
            final double H = 2.2 + s.rnd.nextDouble() * 0.4;
            final Vector3f b = v(Math.cos(a) * R, 0.02, Math.sin(a) * R);
            Vector3f axis = v(-Math.sin(a), 0, Math.cos(a));
            final Quaternionf q = mul(qAxis(Math.toRadians(12), axis), qY(Math.PI / 4 + a));
            Material m = i % 3 == 0 ? Material.BLUE_ICE : (i % 2 == 0 ? Material.ICE : Material.PACKED_ICE);
            BlockDisplay d = s.lit(s.block(m, cube(b, q, 0.001f)));
            final int st = 2 + i;
            s.anim(d, st, st + 6, 1, t -> {
                double k = (t - st) / 5.0;
                double h = H * easeOutBack(Math.min(1, k));
                Vector3f c = new Vector3f(b).add(rot(q, v(0, h / 2, 0)));
                return cube(c, q, v(0.35, Math.max(0.001, h), 0.35));
            });
            final float pitch = 0.8f + i * 0.12f;
            s.at(st, () -> s.sound(s.loc(b), "block.glass.place", 0.7f, pitch));
            all.add(d);
        }

        // Donmus govde
        BlockDisplay body = s.lit(s.block(Material.ICE, cube(v(0, 0.02, 0), qY(0.2), 0.001f)));
        s.anim(body, 4, 12, 2, t -> {
            double h = 1.9 * easeOut((t - 4) / 8.0);
            return cube(v(0, 0.02 + h / 2, 0), qY(0.2), v(0.75, Math.max(0.001, h), 0.75));
        });
        all.add(body);

        s.sound("block.powder_snow.place", 1.0f, 0.8f);
        s.sound("entity.player.hurt_freeze", 0.8f, 1.0f);
        s.at(12, () -> s.sound("block.amethyst_block.chime", 0.8f, 1.6f));
        s.every(2, 28, 2, t -> {
            for (int k = 0; k < 6; k++) {
                double a = t * 0.5 + k * Math.PI / 3;
                double y = (t * 0.08 + k * 0.4) % 2.4;
                s.particle(P.SNOWFLAKE, s.loc(Math.cos(a) * 1.3, y, Math.sin(a) * 1.3), 1, 0, 0);
            }
            s.particle(P.ITEM_SNOWBALL, s.loc(0, 1.2, 0), 3, 0.6, 0.8, 0.6, 0);
        });

        // Parcalanma
        s.at(30, () -> {
            for (BlockDisplay d : all) s.remove(d);
            s.sound("block.glass.break", 1.2f, 0.7f);
            s.sound("block.glass.break", 1.0f, 1.2f);
            s.sound("entity.player.hurt_freeze", 0.8f, 0.6f);
            s.crack(s.loc(0, 1.1, 0), Material.ICE, 80, 0.7);
            s.particle(P.SNOWFLAKE, s.loc(0, 1.1, 0), 40, 0.5, 0.2);
            s.particle(P.ITEM_SNOWBALL, s.loc(0, 1.1, 0), 20, 0.5, 0.2);
        });
        Material[] fm = {Material.ICE, Material.PACKED_ICE, Material.BLUE_ICE};
        for (int i = 0; i < 16; i++) {
            float size = (float) s.rd(0.18, 0.32);
            double a = s.rnd.nextDouble() * Math.PI * 2;
            double r0 = s.rd(0, 0.8);
            double sy = s.rd(0.2, 2.2);
            double sx = Math.cos(a) * r0, sz = Math.sin(a) * r0;
            double sp = s.rd(0.15, 0.3);
            List<double[]> path = s.ballistic(sx, sy, sz, Math.cos(a) * sp, s.rd(0.1, 0.3), Math.sin(a) * sp, 0.05, size / 2, 18);
            Quaternionf rq = mul(qY(s.rnd.nextDouble() * 6), qX(s.rnd.nextDouble() * 6));
            Vector3f ax = randomUnit(s.rnd);
            BlockDisplay d = s.lit(s.block(fm[i % 3], cube(v(sx, sy, sz), rq, 0.001f)));
            s.tfAt(30, d, cube(v(sx, sy, sz), rq, size), 0);
            double angle = 0;
            Quaternionf lastRot = rq;
            Vector3f lastPos = v(sx, sy, sz);
            for (int k = 1; k < path.size() && 30 + k <= 42; k++) {
                double[] p = path.get(k);
                if (p[3] < 1) angle += 0.45;
                lastRot = mul(qAxis(angle, ax), rq);
                lastPos = v(p[0], p[1], p[2]);
                s.tfAt(30 + k, d, cube(lastPos, lastRot, size), 1);
            }
            s.tfAt(43, d, cube(lastPos, lastRot, 0.001f), 5);
        }
    }

    /** Gercekcilik katmani: ek 3D detaylar, enkaz, sok dalgalari ve parlamalar. */
    private void extras(FxSession s) {

        // Yerde parlayan buz damarlari
        FxKit.cracks(s, 9, 2.2, Material.PACKED_ICE, 2, 6, 2, 0.14);
        FxKit.cracks(s, 6, 1.4, Material.SNOW_BLOCK, 4, 6, 1, 0.1);
        // Iceri cekilen soguk sis
        s.every(4, 28, 3, t -> {
            for (int i = 0; i < 6; i++) {
                double a = i * Math.PI / 3 + t * 0.2;
                s.dir(P.CLOUD, s.loc(Math.cos(a) * 1.4, 0.15, Math.sin(a) * 1.4), -Math.cos(a), 0.01, -Math.sin(a), 0.03);
            }
        });
        // Parcalanma aninda buz sok halkasi ve kar bulutu
        FxKit.shockRing(s, 0.05, 0.5, 3.2, 30, 9, Material.LIGHT_BLUE_STAINED_GLASS, 20, 0.35f, true);
        s.at(30, () -> {
            FxKit.dustRing(s, s.loc(0, 0.3, 0), 26, 0.4, 0.35);
            s.particle(P.WHITE_SMOKE, s.loc(0, 1.2, 0), 25, 0.6, 0.8, 0.6, 0.03);
            s.flash(s.loc(0, 1.2, 0), Color.fromRGB(200, 240, 255));
        });
    }
}
