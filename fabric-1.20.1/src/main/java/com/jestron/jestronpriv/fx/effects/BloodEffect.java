package com.jestron.jestronpriv.fx.effects;

import com.jestron.jestronpriv.fx.FxKit;
import com.jestron.jestronpriv.fx.FxSession;
import com.jestron.jestronpriv.fx.KillEffect;
import com.jestron.jestronpriv.fx.P;
import com.jestron.jestronpriv.fx.api.Color;
import com.jestron.jestronpriv.fx.api.Location;
import com.jestron.jestronpriv.fx.api.Material;
import com.jestron.jestronpriv.fx.api.BlockDisplay;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

import static com.jestron.jestronpriv.fx.FxMath.*;

/** 3D kan damlalari etrafa sicrar, yere duserek yayilan lekeler olusturur. */
public final class BloodEffect extends KillEffect {

    public BloodEffect() {
        super("kan", "&4&lKan Sıçraması", Material.REDSTONE, true,
                "3D kan damlaları sıçrar ve",
                "yere düşüp leke olarak yayılır.");
    }

    @Override
    public void play(FxSession s) {
        final Vector3f B = v(0, 1.1, 0);
        final Location bl = s.loc(B);
        Material[] mats = {Material.REDSTONE_BLOCK, Material.RED_CONCRETE, Material.NETHER_WART_BLOCK};

        // Merkez gol
        for (int i = 0; i < 2; i++) {
            final Quaternionf q = qY(i * Math.PI / 4 + 0.2);
            final double y = 0.006 + i * 0.008;
            BlockDisplay pool = s.lit(s.block(Material.REDSTONE_BLOCK, cube(v(0, y, 0), q, v(0.001, 0.001, 0.001))));
            s.anim(pool, 6, 20, 2, t -> {
                float sz = (float) (1.15 * easeOut((t - 6) / 14.0));
                return cube(v(0, y, 0), q, v(Math.max(0.001f, sz), 0.006f, Math.max(0.001f, sz)));
            });
            s.tfAt(42, pool, cube(v(0, y, 0), q, v(0.001, 0.001, 0.001)), 6);
        }

        int landSounds = 0;
        for (int i = 0; i < 26; i++) {
            final float size = (float) s.rd(0.1, 0.22);
            Vector3f dir = randomUnit(s.rnd);
            dir.y = Math.abs(dir.y) * 0.6f + 0.2f;
            double hs = s.rd(0.12, 0.28);
            double vy = s.rd(0.15, 0.35);
            double len = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
            if (len < 1e-3) len = 1;
            double vx = dir.x / len * hs, vz = dir.z / len * hs;
            List<double[]> path = s.ballistic(B.x, B.y, B.z, vx, vy, vz, 0.055, size / 2, 30);
            final double yaw0 = s.rnd.nextDouble() * Math.PI * 2;
            final double spinDir = s.rnd.nextBoolean() ? 1 : -1;
            Quaternionf r0 = qY(yaw0);
            BlockDisplay d = s.lit(s.block(mats[i % 3], cube(B, r0, 0.001f)));
            s.tfAt(2, d, cube(B, r0, size), 0);
            int landTick = -1;
            Vector3f landPos = null;
            Quaternionf rot = r0;
            for (int k = 1; k < path.size() && 2 + k <= 40; k++) {
                double[] p = path.get(k);
                if (p[3] >= 1) {
                    landTick = 2 + k;
                    landPos = v(p[0], p[1] - size / 2, p[2]);
                    break;
                }
                rot = qY(yaw0 + spinDir * k * 0.45);
                s.tfAt(2 + k, d, cube(v(p[0], p[1], p[2]), rot, size), 1);
            }
            if (landTick > 0) {
                // Darbe yonunde uzayan, gercekci sicrama lekesi
                final float splat = size * 2.3f;
                double moveYaw = -Math.atan2(vz, vx);
                double cur = yaw0 + spinDir * (landTick - 3) * 0.45;
                double diff = Math.IEEEremainder(moveYaw - cur, Math.PI);
                final Quaternionf flat = qY(cur + diff);
                final float stretch = (float) (1.0 + Math.min(1.4, Math.sqrt(vx * vx + vz * vz) * 5));
                final Vector3f sp = new Vector3f(landPos).add(0, 0.008f + (float) (s.rnd.nextDouble() * 0.006), 0);
                s.tfAt(landTick, d, cube(sp, flat, v(splat * stretch, 0.012f, splat / Math.max(1f, stretch * 0.7f))), 2);
                s.tfAt(42, d, cube(sp, flat, v(0.001f, 0.001f, 0.001f)), 6);
                final Location ll = s.loc(sp);
                final boolean snd = landSounds++ < 5;
                s.at(landTick, () -> {
                    s.crack(ll, Material.REDSTONE_BLOCK, 4, 0.06);
                    s.dust(ll, Color.fromRGB(110, 0, 0), 0.7f, 2, 0.05);
                    if (snd) s.sound(ll, "entity.slime.squish_small", 0.4f, s.rp(0.8f, 1.2f));
                });
            } else {
                s.tfAt(42, d, cube(B, rot, 0.001f), 6);
            }
        }

        s.at(2, () -> {
            s.sound(bl, "entity.player.attack.crit", 1.0f, 0.7f);
            s.sound(bl, "entity.slime.squish", 1.0f, 0.6f);
            s.sound(bl, "block.honey_block.break", 0.8f, 0.6f);
            s.crack(bl, Material.REDSTONE_BLOCK, 40, 0.3);
            s.dust(bl, Color.fromRGB(150, 0, 0), 1.3f, 25, 0.4);
            s.particle(P.DAMAGE_INDICATOR, bl, 6, 0.3, 0.1);
        });
        s.every(6, 30, 6, t -> s.dust(s.loc(s.rd(-0.4, 0.4), 0.05, s.rd(-0.4, 0.4)), Color.fromRGB(100, 0, 0), 0.8f, 2, 0.1));
        // Ilk darbede kirmizi sis ve yonlu puskurme
        s.at(2, () -> {
            for (int i = 0; i < 30; i++) s.dir(P.DAMAGE_INDICATOR, bl, randomUnit(s.rnd), 0.25);
            s.dust(bl, Color.fromRGB(90, 0, 0), 1.5f, 20, 0.7);
            s.flash(bl, Color.fromRGB(200, 0, 0));
        });
        // Merkezden yere damlayan iri damlalar
        for (int i = 0; i < 6; i++) {
            final double dx = s.rd(-0.18, 0.18), dz = s.rd(-0.18, 0.18);
            final float size = (float) s.rd(0.06, 0.1);
            final int st = 6 + i * 3;
            BlockDisplay drop = s.lit(s.block(Material.REDSTONE_BLOCK, cube(v(dx, 0.95, dz), q(), 0.001f)));
            s.tfAt(st, drop, cube(v(dx, 0.95, dz), q(), v(size, size * 1.6f, size)), 0);
            s.tfAt(st + 1, drop, cube(v(dx, 0.04, dz), q(), v(size, size * 1.9f, size)), 5);
            s.tfAt(st + 6, drop, cube(v(dx, 0.012, dz), qY(i * 0.25), v(size * 2.6f, 0.01f, size * 2.6f)), 2);
            s.tfAt(42, drop, cube(v(dx, 0.012, dz), qY(i * 0.25), 0.001f), 6);
        }
        FxKit.shockRing(s, 0.04, 0.3, 1.8, 2, 8, Material.RED_STAINED_GLASS, 16, 0.14f, false);
    }
}
