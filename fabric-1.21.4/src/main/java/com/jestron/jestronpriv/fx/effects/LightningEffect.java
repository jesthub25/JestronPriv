package com.jestron.jestronpriv.fx.effects;

import com.jestron.jestronpriv.fx.FxSession;
import org.joml.Quaternionf;
import com.jestron.jestronpriv.fx.api.Transformation;
import com.jestron.jestronpriv.fx.api.ItemDisplay;
import com.jestron.jestronpriv.fx.FxKit;
import com.jestron.jestronpriv.fx.KillEffect;
import com.jestron.jestronpriv.fx.P;
import com.jestron.jestronpriv.fx.api.Color;
import com.jestron.jestronpriv.fx.api.Location;
import com.jestron.jestronpriv.fx.api.Material;
import com.jestron.jestronpriv.fx.api.BlockDisplay;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static com.jestron.jestronpriv.fx.FxMath.*;

/** 3 boyutlu, zikzakli yildirimlar ust uste duser; yerde kivilcimlar ve yanik izi kalir. */
public final class LightningEffect extends KillEffect {

    public LightningEffect() {
        super("yildirim", "&e&lYıldırım Çarpması", Material.LIGHTNING_ROD, true,
                "3D zikzak yıldırımlar üst üste",
                "çarpar, yerde yanık izi kalır.");
    }

    @Override
    public void play(FxSession s) {
        playBase(s);
        extras(s);
    }

    private void playBase(FxSession s) {
        int[] strikes = {2, 8, 13};
        for (int i = 0; i < strikes.length; i++) {
            final int st = strikes[i];
            final boolean first = i == 0;
            final long seed = s.rnd.nextLong();
            s.at(st, () -> bolt(s, seed, st, first));
        }

        // Yanik izi (yildiz seklinde iki plaka)
        final double gy = 0.012;
        BlockDisplay sc1 = s.block(Material.COAL_BLOCK, cube(v(0, gy, 0), qY(0.3), v(0.001, 0.001, 0.001)));
        BlockDisplay sc2 = s.block(Material.COAL_BLOCK, cube(v(0, gy + 0.01, 0), qY(0.3 + Math.PI / 4), v(0.001, 0.001, 0.001)));
        s.tfAt(3, sc1, cube(v(0, gy, 0), qY(0.3), v(1.5, 0.02, 1.5)), 2);
        s.tfAt(3, sc2, cube(v(0, gy + 0.01, 0), qY(0.3 + Math.PI / 4), v(1.3, 0.02, 1.3)), 2);
        s.tfAt(42, sc1, cube(v(0, gy, 0), qY(0.3), v(0.001, 0.001, 0.001)), 6);
        s.tfAt(42, sc2, cube(v(0, gy + 0.01, 0), qY(0.3 + Math.PI / 4), v(0.001, 0.001, 0.001)), 6);

        // Kalan elektrik arklari
        s.every(17, 38, 3, t -> {
            List<BlockDisplay> arcs = new ArrayList<>();
            for (int a = 0; a < 2; a++) {
                Vector3f p = v(s.rd(-0.9, 0.9), s.rd(0.05, 0.4), s.rd(-0.9, 0.9));
                for (int seg = 0; seg < 3; seg++) {
                    Vector3f q = new Vector3f(p).add(v(s.rd(-0.35, 0.35), s.rd(-0.1, 0.3), s.rd(-0.35, 0.35)));
                    arcs.add(s.bright(s.block(Material.LIGHT_BLUE_CONCRETE, beam(p, q, 0.04f))));
                    p = q;
                }
            }
            s.at(t + 2, () -> arcs.forEach(s::remove));
            s.particle(P.ELECTRIC_SPARK, s.loc(0, 0.2, 0), 6, 0.8, 0.1, 0.8, 0.2);
        });
        s.every(16, 42, 3, t -> s.particle(P.SMOKE, s.loc(0, 0.1, 0), 4, 0.5, 0.05, 0.5, 0.02));
    }

    private void bolt(FxSession s, long seed, int st, boolean first) {
        java.util.Random r = new java.util.Random(seed);
        int n = 10;
        Vector3f top = v(r.nextDouble() * 3 - 1.5, 16, r.nextDouble() * 3 - 1.5);
        Vector3f bottom = v(r.nextDouble() * 0.6 - 0.3, 0.02, r.nextDouble() * 0.6 - 0.3);
        List<Vector3f> pts = new ArrayList<>();
        for (int j = 0; j <= n; j++) {
            double u = (double) j / n;
            Vector3f p = lerp(top, bottom, u);
            if (j != 0 && j != n) {
                double amp = 0.75 * (1 - u * 0.5);
                p.add((float) ((r.nextDouble() * 2 - 1) * amp), 0, (float) ((r.nextDouble() * 2 - 1) * amp));
            }
            pts.add(p);
        }
        List<BlockDisplay> parts = new ArrayList<>();
        for (int j = 0; j < n; j++) {
            parts.add(s.bright(s.block(Material.WHITE_CONCRETE, beam(pts.get(j), pts.get(j + 1), 0.09f))));
            parts.add(s.bright(s.block(Material.LIGHT_BLUE_STAINED_GLASS, beam(pts.get(j), pts.get(j + 1), 0.3f))));
        }
        // Dallar
        for (int b : new int[]{3, 6}) {
            Vector3f p = new Vector3f(pts.get(b));
            double a = r.nextDouble() * Math.PI * 2;
            for (int k = 0; k < 3; k++) {
                Vector3f q = new Vector3f(p).add((float) (Math.cos(a) * 0.8 + r.nextDouble() * 0.4 - 0.2), (float) (-0.9 - r.nextDouble() * 0.5),
                        (float) (Math.sin(a) * 0.8 + r.nextDouble() * 0.4 - 0.2));
                parts.add(s.bright(s.block(Material.WHITE_CONCRETE, beam(p, q, 0.06f))));
                p = q;
            }
        }
        s.at(st + 3, () -> parts.forEach(s::remove));

        Location hit = s.loc(bottom.x, 0.1, bottom.z);
        s.at(st + 1, () -> {
            if (first) s.longSound("entity.lightning_bolt.thunder", 0.9f, 1.0f);
            s.sound(hit, "entity.lightning_bolt.impact", 1.2f, s.rp(0.8f, 1.1f));
            s.flash(s.loc(0, 1, 0), Color.WHITE);
            s.particle(P.ELECTRIC_SPARK, hit, 30, 0.4, 0.6);
            s.particle(P.EXPLOSION, hit, 1, 0, 0);
            s.groundCrack(hit, 20, 0.5);
            s.particle(P.LARGE_SMOKE, hit, 4, 0.3, 0.02);
        });
    }

    /** Gercekcilik katmani: ek 3D detaylar, enkaz, sok dalgalari ve parlamalar. */
    private void extras(FxSession s) {

        // Fulgurit: yildirimin camlastirdigi topraktan firlayan isik camlari
        for (int i = 0; i < 9; i++) {
            final double a = i * Math.PI * 2 / 9 + s.rd(-0.2, 0.2);
            final double r = s.rd(0.2, 0.9);
            final double x = Math.cos(a) * r, z = Math.sin(a) * r;
            final double gy = s.groundDy(x, z);
            final float h = (float) s.rd(0.35, 0.8);
            final Quaternionf q = mul(qAxis(s.rd(0.2, 0.6), v(-Math.sin(a), 0, Math.cos(a))), qY(s.rnd.nextDouble() * 3));
            final Vector3f base = v(x, gy - 0.05, z);
            BlockDisplay d = s.bright(s.block(i % 2 == 0 ? Material.LIGHT_BLUE_STAINED_GLASS : Material.WHITE_STAINED_GLASS, cube(base, q, 0.001f)));
            s.tfAt(3, d, cube(new Vector3f(base).add(rot(q, v(0, h / 2, 0))), q, v(0.09, h, 0.09)), 2);
            s.tfAt(FxKit.FADE_AT, d, cube(base, q, 0.001f), 5);
        }
        FxKit.glow(s, Material.LIGHT_BLUE_STAINED_GLASS, v(0, 0.5, 0), 1.1f, 3, 12, 0.25);
        FxKit.impact(s, 1.0, 3, Color.fromRGB(170, 220, 255), false);
        FxKit.cracks(s, 6, 1.6, Material.LIGHT_BLUE_CONCRETE, 9, 3, 2, 0.06);
        FxKit.shockRing(s, 0.05, 0.3, 2.8, 13, 7, Material.LIGHT_BLUE_STAINED_GLASS, 18, 0.18f, true);
    }
}
