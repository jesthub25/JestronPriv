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

/** Gokten simsekler esliginde dev bir savas cekici duser, yere saplanir ve elektrik saçar. */
public final class ThorHammerEffect extends KillEffect {

    public ThorHammerEffect() {
        super("cekic", "&b&lYıldırım Çekici", Material.IRON_BLOCK, true,
                "Gökten şimşeklerle dev bir savaş",
                "çekici düşer, yeri yarar, elektrik saçar.");
    }

    private static final int HIT = 12;

    /** Cekic govdesinin merkezi ve donusu. */
    private static Vector3f center(int t) {
        double k = phase(t, 2, HIT);
        double y = lerp(9.0, 0.55, k * k);
        return v(0, y, 0);
    }

    private static Quaternionf spin(FxSession s, int t) {
        double k = phase(t, 2, HIT);
        double a = (1 - k) * (1 - k) * 4.2;          // dusus boyunca yavaslayan takla
        double wob = t > HIT && t < HIT + 8 ? Math.sin((t - HIT) * 2.2) * 0.12 * (1 - (t - HIT) / 8.0) : 0;
        return mul(s.face(), qX(a), qZ(wob));
    }

    @Override
    public void play(FxSession s) {
        // Govde: demir kafa + koyu yan plakalar + sap + kabza sargisi + topuz
        Object[][] parts = {
                {Material.IRON_BLOCK, v(0, 0, 0), v(1.05, 0.62, 0.62)},
                {Material.GRAY_CONCRETE, v(-0.56, 0, 0), v(0.08, 0.68, 0.68)},
                {Material.GRAY_CONCRETE, v(0.56, 0, 0), v(0.08, 0.68, 0.68)},
                {Material.LIGHT_GRAY_CONCRETE, v(0, 0.33, 0), v(0.5, 0.06, 0.5)},
                {Material.DARK_OAK_LOG, v(0, 0.95, 0), v(0.15, 1.05, 0.15)},
                {Material.BLACK_WOOL, v(0, 1.25, 0), v(0.18, 0.42, 0.18)},
                {Material.GOLD_BLOCK, v(0, 1.52, 0), v(0.22, 0.12, 0.22)}};
        for (Object[] p : parts) {
            final Material m = (Material) p[0];
            final Vector3f off = (Vector3f) p[1], size = (Vector3f) p[2];
            block(s, m, m == Material.GOLD_BLOCK ? BRIGHT : LIT, 2, 40, 1, 1,
                    t -> attach(center(t), spin(s, t), off, q(), size));
        }
        // Simsek yuku: cekicin etrafinda parlayan mavi kabuk
        block(s, Material.LIGHT_BLUE_STAINED_GLASS, BRIGHT, 4, 38, 1, 2, t -> {
            double pulse = 1 + 0.12 * Math.sin(t * 1.7);
            return attach(center(t), spin(s, t), v(0, 0.3, 0), qY(t * 0.4), v(1.25 * pulse, 0.85 * pulse, 0.85 * pulse));
        });
        // Iki simsek huzmesi: gokten cekice
        for (int i = 0; i < 2; i++) {
            final int at = i == 0 ? HIT - 1 : HIT + 7;
            final double ox = i == 0 ? 0.3 : -0.4, oz = i == 0 ? -0.2 : 0.35;
            block(s, Material.WHITE_STAINED_GLASS, BRIGHT, at, at + 3, 1, 1,
                    t -> Pose.of(v(ox * 0.5, 6.5, oz * 0.5), mul(qZ(ox * 0.08), qX(oz * 0.08)), v(0.18, 12, 0.18)));
            block(s, Material.LIGHT_BLUE_STAINED_GLASS, BRIGHT, at, at + 3, 1, 1,
                    t -> Pose.of(v(ox * 0.5, 6.5, oz * 0.5), mul(qZ(ox * 0.08), qX(oz * 0.08)), v(0.42, 12, 0.42)));
        }
        FxKit.impact(s, 1.35, HIT, Color.fromRGB(150, 220, 255), true);
        FxKit.shockRing(s, 0.05, 0.4, 3.4, HIT, 9, Material.LIGHT_BLUE_STAINED_GLASS, 18, 0.35f, true);
        FxKit.cracks(s, 7, 1.9, Material.LIGHT_BLUE_CONCRETE, HIT, 4, 2, 0.07);

        // Parcaciklar
        s.every(3, HIT, 1, t -> {
            Vector3f c = center(t);
            s.particle(P.ELECTRIC_SPARK, s.loc(c.x, c.y + 0.3, c.z), 4, 0.5, 0.05);
            s.particle(P.CLOUD, s.loc(c.x, c.y + 0.6, c.z), 1, 0.15, 0.01);
        });
        s.every(HIT + 1, 38, 2, t -> {
            s.particle(P.ELECTRIC_SPARK, s.loc(s.rd(-0.7, 0.7), s.rd(0.2, 1.8), s.rd(-0.7, 0.7)), 3, 0.2, 0.15);
            if (t % 6 == 0) FxKit.sparks(s, s.loc(0, 0.6, 0), 6, 0.35);
        });
        s.at(HIT, () -> s.particle(P.FLASH, s.loc(0, 1, 0), 1, 0, 0));

        // Ses tasarimi
        s.longSound("entity.lightning_bolt.thunder", 0.55f, 1.15f);
        s.at(3, () -> s.sound("item.trident.thunder", 0.9f, 0.8f));
        s.at(7, () -> s.sound("item.trident.riptide_3", 0.7f, 0.6f));
        s.at(HIT - 1, () -> s.sound("entity.lightning_bolt.impact", 1.0f, 0.8f));
        s.at(HIT, () -> {
            s.sound("item.mace.smash_ground_heavy", 1.0f, 0.65f);
            s.sound("block.anvil.land", 0.8f, 0.5f);
            s.sound("entity.generic.explode", 0.6f, 0.9f);
        });
        s.at(HIT + 7, () -> s.sound("entity.lightning_bolt.impact", 0.7f, 1.2f));
        s.every(HIT + 3, 36, 6, t -> s.sound("block.respawn_anchor.charge", 0.35f, s.rp(1.6f, 2.0f)));
    }
}
