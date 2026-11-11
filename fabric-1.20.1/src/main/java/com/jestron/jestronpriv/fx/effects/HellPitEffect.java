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

import static com.jestron.jestronpriv.fx.FxMath.*;

/** Yer yarilir: kizgin magma halkasi, alev sutunlari ve ortadan ates patlamasi. */
public final class HellPitEffect extends KillEffect {

    public HellPitEffect() {
        super("cehennem", "&c&lCehennem Çukuru", Material.MAGMA_BLOCK, true,
                "Yer yarılır, magma halkası ve",
                "alev sütunları yükselir.");
    }

    @Override
    public void play(FxSession s) {
        playBase(s);
        extras(s);
    }

    private void playBase(FxSession s) {
        // Kizgin taban
        BlockDisplay floor = s.bright(s.block(Material.MAGMA_BLOCK, cube(v(0, 0.012, 0), qY(Math.PI / 4), v(0.001, 0.001, 0.001))));
        s.anim(floor, 2, 8, 2, t -> {
            float sz = (float) (1.9 * easeOut((t - 2) / 6.0));
            return cube(v(0, 0.012, 0), qY(Math.PI / 4), v(sz, 0.02f, sz));
        });
        s.tfAt(40, floor, cube(v(0, 0.012, 0), qY(Math.PI / 4), v(0.001, 0.001, 0.001)), 8);

        // Magma halkasi
        for (int i = 0; i < 10; i++) {
            double a = i * Math.PI * 2 / 10;
            double x = Math.cos(a) * 1.35, z = Math.sin(a) * 1.35;
            double gy = s.groundDy(x, z);
            final Quaternionf q = mul(qY(a + s.rnd.nextDouble()), qX(s.rd(-0.25, 0.25)));
            final Vector3f low = v(x, gy - 0.2, z), high = v(x, gy + 0.05, z);
            BlockDisplay d = s.bright(s.block(Material.MAGMA_BLOCK, cube(low, q, v(0.55, 0.14, 0.55))));
            final int st = 2 + i / 2;
            s.tfAt(st, d, cube(high, q, v(0.55, 0.14, 0.55)), 4);
            s.tfAt(40, d, cube(low, q, v(0.001, 0.001, 0.001)), 8);
        }
        // Dis kirik kaya
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI * 2 / 8 + 0.3;
            double x = Math.cos(a) * 1.95, z = Math.sin(a) * 1.95;
            double gy = s.groundDy(x, z);
            final Quaternionf q = mul(qY(a), qZ(s.rd(-0.5, 0.5)));
            Vector3f c = v(x, gy + 0.12, z);
            BlockDisplay d = s.lit(s.block(Material.BLACKSTONE, cube(c, q, 0.001f)));
            s.tfAt(4, d, cube(c, q, v(0.42, 0.28, 0.42)), 3);
            s.tfAt(40, d, cube(v(x, gy - 0.2, z), q, 0.001f), 8);
        }
        // Ates bloklari
        final Vector3f[] firePos = new Vector3f[9];
        for (int i = 0; i < 9; i++) {
            boolean soul = i >= 6;
            double a = soul ? (i - 6) * Math.PI * 2 / 3 + 0.5 : i * Math.PI * 2 / 6 + Math.PI / 6;
            double r = soul ? 0.55 : 1.35;
            double x = Math.cos(a) * r, z = Math.sin(a) * r;
            double gy = soul ? 0.02 : s.groundDy(x, z) + 0.12;
            firePos[i] = v(x, gy, z);
            final Vector3f base = firePos[i];
            final float max = soul ? 0.8f : 1.15f;
            final Quaternionf q = qY(a);
            BlockDisplay d = s.bright(s.block(soul ? Material.SOUL_FIRE : Material.FIRE, cube(new Vector3f(base).add(0, 0.0005f, 0), q, 0.001f)));
            final int k = i;
            s.anim(d, 4 + (i % 3), 40, 4, t -> {
                double grow = t < 12 ? easeOut((t - 4) / 8.0) : 1.0;
                double flick = 0.9 + 0.35 * Math.abs(Math.sin(t * 0.7 + k));
                float sz = (float) (max * grow * (t >= 36 ? Math.max(0, 1 - (t - 36) / 4.0) : flick));
                return cube(new Vector3f(base).add(0, sz / 2, 0), q, Math.max(0.001f, sz));
            });
            s.tfAt(41, d, cube(new Vector3f(base).add(0, 0.0005f, 0), q, 0.001f), 4);
        }

        // Parcaciklar
        s.every(6, 40, 2, t -> {
            for (int i = 0; i < 6; i++) {
                Vector3f p = firePos[i];
                s.dir(P.FLAME, s.loc(p.x, p.y + 0.4, p.z), s.rd(-0.1, 0.1), 1, s.rd(-0.1, 0.1), 0.12);
            }
            if (t % 4 == 0) s.particle(P.LAVA, s.loc(0, 0.2, 0), 2, 0.5, 0);
            if (t % 6 == 0) s.particle(P.LARGE_SMOKE, s.loc(0, 0.6, 0), 3, 0.8, 0.2, 0.8, 0.02);
        });
        s.at(20, () -> {
            Location c = s.loc(0, 0.3, 0);
            for (int i = 0; i < 25; i++) s.dir(P.FLAME, c, s.rd(-0.15, 0.15), 1, s.rd(-0.15, 0.15), s.rd(0.25, 0.42));
            s.particle(P.LAVA, c, 15, 0.4, 0);
            s.particle(P.EXPLOSION, s.loc(0, 1.5, 0), 2, 0.3, 0);
            s.sound("entity.blaze.shoot", 1.0f, 0.6f);
            s.sound("item.firecharge.use", 1.0f, 0.5f);
            s.sound("entity.generic.explode", 0.4f, 0.8f);
        });

        s.sound("block.lava.extinguish", 0.6f, 0.6f);
        s.at(4, () -> s.sound("block.fire.ambient", 1.0f, 0.8f));
        s.at(12, () -> s.sound("entity.ghast.scream", 0.35f, 0.5f));
        s.at(30, () -> s.sound("block.lava.pop", 0.8f, 0.8f));
        s.at(44, () -> {
            s.sound("block.fire.extinguish", 0.7f, 1.0f);
            s.particle(P.SMOKE, s.loc(0, 0.3, 0), 20, 1.0, 0.1, 1.0, 0.02);
        });
    }

    /** Gercekcilik katmani: ek 3D detaylar, enkaz, sok dalgalari ve parlamalar. */
    private void extras(FxSession s) {

        // Yerde kizgin lav yariklari
        FxKit.cracks(s, 8, 2.6, Material.MAGMA_BLOCK, 2, 5, 2, 0.13);
        FxKit.cracks(s, 5, 1.8, Material.SHROOMLIGHT, 4, 5, 2, 0.06);
        // Patlama aninda ates sutunu, lav cesmesi ve sok halkasi
        FxKit.pillar(s, Material.ORANGE_STAINED_GLASS, 0, 0, 0, 4.6, 0.95f, 20, 28);
        FxKit.pillar(s, Material.YELLOW_STAINED_GLASS, 0, 0, 0, 3.8, 0.5f, 20, 27);
        FxKit.debris(s, v(0, 0.3, 0), new Material[]{Material.MAGMA_BLOCK, Material.NETHERRACK, Material.MAGMA_BLOCK, Material.BLACKSTONE},
                10, 0.04, 0.14, 0.5, 0.75, 0.15f, 0.3f, 20, false);
        FxKit.shockRing(s, 0.05, 0.4, 3.0, 20, 8, Material.ORANGE_STAINED_GLASS, 20, 0.3f, true);
        s.every(8, 40, 2, t -> s.particle(P.WHITE_SMOKE, s.loc(s.rd(-1.4, 1.4), 0.3, s.rd(-1.4, 1.4)), 1, 0.1, 0.02));
    }
}
