package com.jestron.jestronpriv.fx.effects;

import com.jestron.jestronpriv.fx.FxKit;
import com.jestron.jestronpriv.fx.FxSession;
import com.jestron.jestronpriv.fx.KillEffect;
import com.jestron.jestronpriv.fx.P;
import com.jestron.jestronpriv.fx.api.BlockDisplay;
import com.jestron.jestronpriv.fx.api.Color;
import com.jestron.jestronpriv.fx.api.Location;
import com.jestron.jestronpriv.fx.api.Material;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static com.jestron.jestronpriv.fx.FxMath.*;

/**
 * Gokten capraz duzen, puruzlu kaya govdeli, alev kabugu ve ates izi olan meteor.
 * Carpmada krater kenari yukselir, kizgin kayalar sacilir, yanik izi ve duman sutunu kalir.
 */
public final class MeteorEffect extends KillEffect {

    private static final Color FIRE = Color.fromRGB(255, 120, 20);
    private static final int IMPACT = 19;

    public MeteorEffect() {
        super("meteor", "&6&lMeteor Düşüşü", Material.FIRE_CHARGE, true,
                "Gökten alevli bir meteor düşer,",
                "patlar ve kızgın kayalar saçar.");
    }

    @Override
    public void play(FxSession s) {
        final Vector3f start = s.local(-5, 20, -7);
        final Vector3f impact = v(0, 0.35, 0);
        final Vector3f spinAxis = randomUnit(s.rnd);

        // --- Puruzlu govde: farkli boyut/rotasyonda 7 kaya parcasi
        Material[] mats = {Material.MAGMA_BLOCK, Material.BLACKSTONE, Material.MAGMA_BLOCK, Material.BASALT,
                Material.BLACKSTONE, Material.MAGMA_BLOCK, Material.NETHERRACK};
        for (int i = 0; i < mats.length; i++) {
            final Vector3f off = i == 0 ? v(0, 0, 0) : randomUnit(s.rnd).mul((float) s.rd(0.25, 0.42));
            final float size = i == 0 ? 1.0f : (float) s.rd(0.45, 0.7);
            final Quaternionf base = mul(qY(s.rnd.nextDouble() * 6), qX(s.rnd.nextDouble() * 6));
            Material m = mats[i];
            BlockDisplay d = s.block(m, cube(new Vector3f(start).add(off), base, size));
            if (m == Material.MAGMA_BLOCK) s.bright(d);
            else s.lit(d);
            for (int t = 3; t <= IMPACT - 1; t++) {
                double u = Math.pow((t - 2) / 16.0, 1.6);
                Quaternionf sp = qAxis(0.35 * t, spinAxis);
                Vector3f p = lerp(start, impact, u).add(rot(sp, off));
                s.tfAt(t, d, cube(p, mul(sp, base), size), 1);
            }
            Quaternionf spEnd = qAxis(0.35 * IMPACT, spinAxis);
            Vector3f sunk = v(0, 0.12, 0).add(rot(spEnd, new Vector3f(off).mul(0.8f)));
            s.tfAt(IMPACT, d, cube(sunk, mul(spEnd, base), size * 0.85f), 2);
            s.tfAt(40, d, cube(v(0, -0.3, 0), mul(spEnd, base), 0.001f), 8);
        }

        // --- Govdeyi saran alev kabugu (iki katmanli yari saydam cam)
        Material[] shellM = {Material.ORANGE_STAINED_GLASS, Material.YELLOW_STAINED_GLASS};
        float[] shellS = {1.75f, 1.35f};
        for (int k = 0; k < 2; k++) {
            final int kk = k;
            BlockDisplay sh = s.bright(s.block(shellM[k], cube(start, q(), 0.001f)));
            for (int t = 3; t <= IMPACT - 1; t++) {
                double u = Math.pow((t - 2) / 16.0, 1.6);
                Vector3f p = lerp(start, impact, u);
                s.tfAt(t, sh, cube(p, mul(qY(t * 0.5 * (kk == 0 ? 1 : -1)), qX(0.6)), shellS[kk] * (float) (0.9 + 0.1 * Math.sin(t))), 1);
            }
            s.tfAt(IMPACT, sh, cube(impact, mul(qY((IMPACT - 1) * 0.5 * (kk == 0 ? 1 : -1)), qX(0.6)), 0.001f), 1);
        }

        // --- Ates izi: arkada kalan kuculen kizgin hayalet kayalar + parcaciklar
        for (int t = 4; t <= IMPACT - 2; t += 2) {
            double u = Math.pow((t - 3) / 16.0, 1.6);
            Vector3f p = lerp(start, impact, u);
            FxKit.ghostBlock(s, t % 4 == 0 ? Material.MAGMA_BLOCK : Material.ORANGE_STAINED_GLASS,
                    cube(p, qAxis(0.35 * t, spinAxis), 0.95f), t, 5);
        }
        for (int t = 3; t <= IMPACT - 1; t++) {
            final double u = Math.pow((t - 2) / 16.0, 1.6);
            final double up = Math.pow(Math.max(0, t - 3) / 16.0, 1.6);
            final Location p = s.loc(lerp(start, impact, u));
            final Location mid = s.loc(lerp(start, impact, (u + up) / 2));
            s.at(t, () -> {
                s.particle(P.FLAME, p, 8, 0.35, 0.03);
                s.particle(P.LARGE_SMOKE, p, 4, 0.35, 0.01);
                s.particle(P.LAVA, p, 1, 0.2, 0);
                s.dust(mid, FIRE, 1.3f, 5, 0.4);
                s.particle(P.FLAME, mid, 4, 0.3, 0.02);
                s.particle(P.SMOKE, mid, 3, 0.4, 0.01);
            });
        }

        s.sound(s.loc(start), "entity.blaze.shoot", 1.0f, 0.5f);
        s.at(2, () -> s.longSound("item.elytra.flying", 0.55f, 1.6f));
        s.at(10, () -> s.sound("entity.ghast.shoot", 0.8f, 0.5f));
        s.at(15, () -> s.sound("item.trident.riptide_3", 0.6f, 0.6f));

        // --- Carpma
        s.at(IMPACT, () -> {
            s.stopSound("item.elytra.flying");
            s.sound("entity.generic.explode", 1.5f, 0.6f);
            s.sound("entity.dragon_fireball.explode", 1.0f, 0.8f);
            s.sound("item.mace.smash_ground_heavy", 0.9f, 0.6f);
            Location c = s.loc(0, 0.5, 0);
            s.particle(P.EXPLOSION_EMITTER, c, 1, 0, 0);
            s.particle(P.EXPLOSION, c, 6, 1.2, 0);
            s.particle(P.LAVA, c, 30, 0.8, 0);
            s.particle(P.LARGE_SMOKE, c, 24, 1.0, 0.06);
            for (int i = 0; i < 46; i++) {
                Vector3f d = randomUnit(s.rnd);
                d.y = Math.abs(d.y);
                s.dir(P.FLAME, c, d, 0.28);
            }
        });
        FxKit.impact(s, 1.5, IMPACT, FIRE, true);
        FxKit.shockRing(s, 0.05, 0.5, 4.2, IMPACT, 9, Material.ORANGE_STAINED_GLASS, 18, 0.4f, true);
        FxKit.cracks(s, 7, 2.4, Material.MAGMA_BLOCK, IMPACT + 1, 4, 2, 0.11);
        FxKit.glow(s, Material.ORANGE_STAINED_GLASS, v(0, 0.4, 0), 2.2f, IMPACT, IMPACT + 12, 0.15);

        // --- Krater kenari: disa yatik yukselen zemin bloklari
        Material gm = s.groundMaterial(Material.COBBLESTONE);
        for (int i = 0; i < 12; i++) {
            final double a = i * Math.PI * 2 / 12 + s.rd(-0.12, 0.12);
            final double r = s.rd(1.35, 1.65);
            final double x = Math.cos(a) * r, z = Math.sin(a) * r;
            final double gy = s.groundDy(x, z);
            final float size = (float) s.rd(0.4, 0.62);
            final Quaternionf q = mul(qAxis(-s.rd(0.35, 0.7), v(-Math.sin(a), 0, Math.cos(a))), qY(a + s.rd(-0.4, 0.4)));
            Material m = i % 3 == 0 ? Material.BLACKSTONE : (i % 3 == 1 ? gm : Material.COBBLESTONE);
            final Vector3f low = v(x, gy - size, z), high = v(x, gy + size * 0.15, z);
            BlockDisplay d = s.lit(s.block(m, cube(low, q, 0.001f)));
            s.tfAt(IMPACT, d, cube(low, q, size), 0);
            s.tfAt(IMPACT + 1, d, cube(high, q, size), 3);
            s.tfAt(FxKit.FADE_AT, d, cube(low, q, 0.001f), 5);
        }

        // --- Kizgin kaya parcalari
        FxKit.debris(s, v(0, 0.6, 0), new Material[]{Material.MAGMA_BLOCK, Material.BLACKSTONE, gm, Material.BASALT},
                10, 0.18, 0.32, 0.35, 0.55, 0.22f, 0.4f, IMPACT, false);

        // --- Duman sutunu ve kor
        s.every(21, 42, 2, t -> {
            s.particle(P.LARGE_SMOKE, s.loc(0, 0.6 + (t - 21) * 0.05, 0), 3, 0.35, 0.04);
            s.particle(P.FLAME, s.loc(0, 0.4, 0), 2, 0.3, 0.01);
            if (t % 6 == 0) s.particle(P.LAVA, s.loc(0, 0.4, 0), 2, 0.25, 0);
            FxKit.embers(s, s.loc(0, 0.3, 0), 2, 0.8);
        });
    }
}
