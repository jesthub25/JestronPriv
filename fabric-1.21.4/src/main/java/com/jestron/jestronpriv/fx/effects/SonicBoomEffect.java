package com.jestron.jestronpriv.fx.effects;

import com.jestron.jestronpriv.fx.FxSession;
import org.joml.Vector3f;
import com.jestron.jestronpriv.fx.api.Transformation;
import com.jestron.jestronpriv.fx.api.ItemDisplay;
import com.jestron.jestronpriv.fx.FxKit;
import com.jestron.jestronpriv.fx.KillEffect;
import com.jestron.jestronpriv.fx.P;
import com.jestron.jestronpriv.fx.api.Color;
import com.jestron.jestronpriv.fx.api.Location;
import com.jestron.jestronpriv.fx.api.Material;
import com.jestron.jestronpriv.fx.api.BlockDisplay;
import org.joml.Quaternionf;

import static com.jestron.jestronpriv.fx.FxMath.*;

/** Warden tarzi sonik enerji toplanir ve genisleyen bir sok dalgasiyla patlar. */
public final class SonicBoomEffect extends KillEffect {

    private static final Color TEAL = Color.fromRGB(40, 200, 190);

    public SonicBoomEffect() {
        super("sonik", "&3&lSonik Patlama", Material.SCULK_SHRIEKER, true,
                "Sonik enerji toplanır ve dev bir",
                "şok dalgasıyla patlar.");
    }

    @Override
    public void play(FxSession s) {
        playBase(s);
        extras(s);
    }

    private void playBase(FxSession s) {
        final double cy = 1.2;
        final Location c = s.loc(0, cy, 0);

        // Toplanma spirali
        s.every(2, 16, 1, t -> {
            double k = (t - 2) / 14.0;
            for (int arm = 0; arm < 3; arm++) {
                for (int j = 0; j < 4; j++) {
                    double rr = lerp(2.6, 0.3, k) + j * 0.25;
                    double a = arm * Math.PI * 2 / 3 + t * 0.5 + j * 0.3;
                    s.dust(s.loc(Math.cos(a) * rr, cy + Math.sin(t * 0.3 + j) * 0.3, Math.sin(a) * rr), TEAL, 0.9f, 1, 0);
                }
            }
            if (t % 2 == 0) s.particle(P.SCULK_CHARGE_POP, c, 4, 0.5, 0.02);
        });
        s.sound("entity.warden.sonic_charge", 1.2f, 1.0f);

        // 3D sok halkasi (cam plakalar)
        final int n = 20;
        for (int i = 0; i < n; i++) {
            final double a = i * Math.PI * 2 / n;
            final Quaternionf q = qY(-a + Math.PI / 2);
            BlockDisplay d = s.bright(s.block(Material.CYAN_STAINED_GLASS, cube(v(Math.cos(a) * 0.5, cy, Math.sin(a) * 0.5), q, 0.001f)));
            s.anim(d, 18, 32, 2, t -> {
                double k = (t - 18) / 12.0;
                double r = lerp(0.5, 4.5, easeOut(k));
                double h = t <= 26 ? 0.18 : Math.max(0.001, 0.18 * (1 - (t - 26) / 6.0));
                double w = 2 * Math.PI * r / n * 1.05;
                return cube(v(Math.cos(a) * r, cy, Math.sin(a) * r), q, v(w, h, 0.06));
            });
            s.tfAt(33, d, cube(v(Math.cos(a) * 4.5, cy, Math.sin(a) * 4.5), q, 0.001f), 1);
        }

        s.at(18, () -> {
            s.stopSound("entity.warden.sonic_charge");
            s.sound(c, "entity.warden.sonic_boom", 1.3f, 1.0f);
            s.sound(c, "entity.generic.explode", 0.5f, 0.6f);
            for (int i = 0; i < 5; i++) s.particle(P.SONIC_BOOM, s.loc(0, 0.5 + i * 0.6, 0), 1, 0, 0);
            s.groundCrack(s.loc(0, 0.1, 0), 50, 1.5);
            for (int i = 0; i < 24; i++) {
                double a = i * Math.PI * 2 / 24;
                s.dir(P.CLOUD, s.loc(Math.cos(a) * 0.5, 0.15, Math.sin(a) * 0.5), Math.cos(a), 0.02, Math.sin(a), 0.4);
            }
        });
        s.every(19, 26, 2, t -> {
            double r = 0.6 + (t - 18) * 0.55;
            int pts = (int) Math.max(8, r * 5);
            for (int i = 0; i < pts; i++) {
                double a = i * Math.PI * 2 / pts;
                s.particle(P.SONIC_BOOM, s.loc(Math.cos(a) * r, 0.6, Math.sin(a) * r), 1, 0, 0);
            }
        });
        s.at(22, () -> s.sound(c, "block.sculk_catalyst.bloom", 0.8f, 1.0f));
        s.every(22, 40, 3, t -> {
            s.particle(P.SCULK_SOUL, s.loc(s.rd(-1, 1), 0.3, s.rd(-1, 1)), 1, 0.1, 0.02);
            s.particle(P.SCULK_CHARGE_POP, s.loc(0, 0.2, 0), 3, 1.0, 0.1, 1.0, 0.01);
        });
    }

    /** Gercekcilik katmani: ek 3D detaylar, enkaz, sok dalgalari ve parlamalar. */
    private void extras(FxSession s) {

        // Patlamadan once toplanan enerji kuresi
        FxKit.glow(s, Material.CYAN_STAINED_GLASS, v(0, 1.2, 0), 1.1f, 3, 18, 0.18);
        // Yere yayilan sculk damarlari, enerji sutunu ve firlayan zemin
        FxKit.cracks(s, 10, 2.8, Material.SCULK, 18, 4, 1, 0.16);
        FxKit.pillar(s, Material.CYAN_STAINED_GLASS, 0, 0, 0, 6, 0.9f, 18, 25);
        FxKit.pillar(s, Material.LIGHT_BLUE_STAINED_GLASS, 0, 0, 0, 5, 0.45f, 18, 24);
        Material g = s.groundMaterial(Material.DIRT);
        FxKit.debris(s, v(0, 0.2, 0), new Material[]{g, Material.SCULK, Material.TUFF}, 8, 0.12, 0.26, 0.3, 0.5, 0.12f, 0.24f, 18, false);
    }
}
