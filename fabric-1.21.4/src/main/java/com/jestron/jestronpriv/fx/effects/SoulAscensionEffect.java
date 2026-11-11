package com.jestron.jestronpriv.fx.effects;

import com.jestron.jestronpriv.fx.FxSession;
import org.joml.Vector3f;
import org.joml.Quaternionf;
import com.jestron.jestronpriv.fx.api.Transformation;
import com.jestron.jestronpriv.fx.api.BlockDisplay;
import com.jestron.jestronpriv.fx.api.Location;
import com.jestron.jestronpriv.fx.FxKit;
import com.jestron.jestronpriv.fx.KillEffect;
import com.jestron.jestronpriv.fx.P;
import com.jestron.jestronpriv.fx.api.Color;
import com.jestron.jestronpriv.fx.api.Material;
import com.jestron.jestronpriv.fx.api.ItemDisplay;
import com.jestron.jestronpriv.fx.api.ItemStack;

import static com.jestron.jestronpriv.fx.FxMath.*;

/** Kurbanin kafasi (kendi skini) ruh alevleri icinde gokyuzune yukselir. */
public final class SoulAscensionEffect extends KillEffect {

    private static final Color HALO = Color.fromRGB(235, 245, 255);

    public SoulAscensionEffect() {
        super("ruh_yukselisi", "&3&lRuh Yükselişi", Material.SOUL_LANTERN, true,
                "Kurbanın kafası ruh alevleri",
                "içinde göğe yükselir.");
    }

    private static double[] pos(int t) {
        double k = easeInOut((t - 4) / 38.0);
        double y = lerp(1.2, 5.0, k);
        double sway = 0.25 * Math.sin(t * 0.25);
        double sway2 = 0.25 * Math.cos(t * 0.25);
        return new double[]{sway * k, y, sway2 * k};
    }

    @Override
    public void play(FxSession s) {
        playBase(s);
        extras(s);
    }

    private void playBase(FxSession s) {
        ItemStack head = s.victimHead != null ? s.victimHead.clone() : new ItemStack(Material.SKELETON_SKULL);
        ItemDisplay d = s.lit(s.item(head, item(v(0, 1.2, 0), s.face(), 0.001f)));
        s.anim(d, 2, 46, 2, t -> {
            double[] p = pos(Math.max(4, t));
            double sc;
            if (t <= 6) sc = 1.6 * easeOutBack((t - 2) / 4.0);
            else if (t <= 36) sc = 1.6;
            else sc = 1.6 * (1 - easeIn((t - 36) / 8.0));
            return item(v(p[0], p[1], p[2]), qY(s.faceYaw + 0.12 * t), (float) Math.max(0.001, sc));
        });

        s.every(2, 12, 2, t -> s.particle(P.SCULK_SOUL, s.loc(0, 0.4, 0), 3, 0.3, 0.2, 0.3, 0.02));
        s.every(4, 42, 2, t -> {
            double[] p = pos(t);
            s.particle(P.SOUL, s.loc(p[0], p[1] - 0.35, p[2]), 2, 0.15, 0.01);
            for (int k = 0; k < 2; k++) {
                double a = t * 0.5 + k * Math.PI;
                s.particle(P.SOUL_FIRE_FLAME, s.loc(p[0] + Math.cos(a) * 0.6, p[1] - 0.2, p[2] + Math.sin(a) * 0.6), 1, 0, 0);
            }
            if (t <= 38) {
                for (int i = 0; i < 8; i++) {
                    double a = i * Math.PI / 4 + t * 0.2;
                    s.dust(s.loc(p[0] + Math.cos(a) * 0.35, p[1] + 0.62, p[2] + Math.sin(a) * 0.35), HALO, 0.6f, 1, 0);
                }
            }
        });

        s.sound("particle.soul_escape", 1.0f, 0.6f);
        s.at(4, () -> s.sound("entity.ghast.ambient", 0.35f, 1.6f));
        s.at(10, () -> s.sound("particle.soul_escape", 1.0f, 0.9f));
        s.at(18, () -> s.sound("particle.soul_escape", 1.0f, 1.2f));
        s.at(20, () -> s.sound(s.loc(0, 3, 0), "block.beacon.activate", 0.5f, 1.6f));
        s.at(44, () -> {
            double[] p = pos(42);
            s.sound(s.loc(p[0], p[1], p[2]), "block.amethyst_block.chime", 1.0f, 1.8f);
            s.particle(P.SOUL, s.loc(p[0], p[1], p[2]), 15, 0.3, 0.05);
            s.dust(s.loc(p[0], p[1], p[2]), HALO, 1.0f, 15, 0.4);
        });
    }

    /** Gercekcilik katmani: ek 3D detaylar, enkaz, sok dalgalari ve parlamalar. */
    private void extras(FxSession s) {

        // Kafanin altinda yari saydam hayalet beden
        Object[][] parts = {
                {v(0, -0.83, 0), v(0.5, 0.72, 0.26), 0.0},
                {v(-0.38, -0.8, 0), v(0.22, 0.7, 0.22), 0.5},
                {v(0.38, -0.8, 0), v(0.22, 0.7, 0.22), -0.5},
                {v(-0.13, -1.55, 0), v(0.23, 0.72, 0.23), 0.12},
                {v(0.13, -1.55, 0), v(0.23, 0.72, 0.23), -0.12}};
        for (Object[] pp : parts) {
            final Vector3f off = (Vector3f) pp[0];
            final Vector3f size = (Vector3f) pp[1];
            final double roll = (double) pp[2];
            BlockDisplay d = s.bright(s.block(Material.WHITE_STAINED_GLASS, cube(v(0, 1.2, 0), mul(qY(s.faceYaw + 0.48), qZ(roll)), 0.001f)));
            s.anim(d, 4, 44, 2, t -> {
                double[] p = pos(Math.max(4, t));
                double k = t <= 8 ? Math.max(0.001, (t - 4) / 4.0) : (t >= 34 ? Math.max(0.001, 1 - (t - 34) / 10.0) : 1);
                Quaternionf yaw = qY(s.faceYaw + 0.12 * t);
                double trail = 0.25 * Math.sin(t * 0.3);
                Quaternionf q = mul(yaw, qZ(roll * (1 + 0.3 * Math.sin(t * 0.4))), qX(trail));
                Vector3f c = v(p[0], p[1], p[2]).add(rot(yaw, new Vector3f(off).mul((float) Math.max(0.3, k))));
                return cube(c, q, new Vector3f(size).mul((float) k));
            });
            s.tfAt(45, d, cube(v(0, 4, 0), mul(qY(s.faceYaw + 0.12 * 44), qZ(roll)), 0.001f), 1);
        }
        // Basin uzerinde donen 3D altin hale
        for (int i = 0; i < 10; i++) {
            final double a = i * Math.PI * 2 / 10;
            BlockDisplay h = s.bright(s.block(Material.GOLD_BLOCK, cube(v(0, 2, 0), qY(-(a + 6 * 0.15)), 0.001f)));
            s.anim(h, 6, 40, 2, t -> {
                double[] p = pos(Math.max(4, t));
                double k = t <= 10 ? Math.max(0.001, (t - 6) / 4.0) : (t >= 36 ? Math.max(0.001, (40 - t) / 4.0) : 1);
                double aa = a + t * 0.15;
                Vector3f c = v(p[0] + Math.cos(aa) * 0.36, p[1] + 0.62, p[2] + Math.sin(aa) * 0.36);
                return cube(c, qY(-aa), v(0.2 * k + 0.001, 0.05 * k + 0.001, 0.07 * k + 0.001));
            });
            s.tfAt(41, h, cube(v(0, 4, 0), qY(-(a + 40 * 0.15)), 0.001f), 1);
        }
        FxKit.shockRing(s, 0.04, 0.3, 2.2, 2, 10, Material.LIGHT_BLUE_STAINED_GLASS, 16, 0.25f, true);
    }
}
