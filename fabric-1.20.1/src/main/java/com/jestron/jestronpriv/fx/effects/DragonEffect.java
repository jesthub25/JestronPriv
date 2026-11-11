package com.jestron.jestronpriv.fx.effects;

import com.jestron.jestronpriv.fx.FxSession;
import org.joml.Vector3f;
import org.joml.Quaternionf;
import com.jestron.jestronpriv.fx.api.Transformation;
import com.jestron.jestronpriv.fx.FxKit;
import com.jestron.jestronpriv.fx.KillEffect;
import com.jestron.jestronpriv.fx.P;
import com.jestron.jestronpriv.fx.api.Color;
import com.jestron.jestronpriv.fx.api.Location;
import com.jestron.jestronpriv.fx.api.Material;
import com.jestron.jestronpriv.fx.api.BlockDisplay;
import com.jestron.jestronpriv.fx.api.ItemDisplay;

import static com.jestron.jestronpriv.fx.FxMath.*;

/** Bir ejderha yumurtasi catlar, dev ejderha kafasi yukselir ve her yone nefes puskurtur. */
public final class DragonEffect extends KillEffect {

    private static final Color BREATH = Color.fromRGB(200, 60, 255);

    public DragonEffect() {
        super("ejderha", "&5&lEjderha Nefesi", Material.DRAGON_HEAD, true,
                "Ejderha yumurtası çatlar, dev bir",
                "ejderha kafası nefes püskürtür.");
    }

    @Override
    public void play(FxSession s) {
        playBase(s);
        extras(s);
    }

    private void playBase(FxSession s) {
        // Yumurta
        BlockDisplay egg = s.lit(s.block(Material.DRAGON_EGG, cube(v(0, 0.3, 0), qY(0), 0.001f)));
        s.anim(egg, 2, 10, 2, t -> {
            double sc = t <= 6 ? 0.6 * easeOutBack((t - 2) / 4.0) : 0.6 + (t % 4 == 0 ? 0.03 : -0.03);
            return cube(v(0, 0.3 * sc / 0.6, 0), qY(t * 0.2), (float) Math.max(0.001, sc));
        });
        s.tfAt(12, egg, cube(v(0, 0.01, 0), qY(2.4), 0.001f), 3);
        s.at(11, () -> {
            s.crack(s.loc(0, 0.3, 0), Material.DRAGON_EGG, 30, 0.3);
            s.sound("entity.turtle.egg_crack", 1.0f, 0.6f);
            s.sound("entity.turtle.egg_break", 1.0f, 0.6f);
        });

        // Kafa
        ItemDisplay head = s.lit(s.item(Material.DRAGON_HEAD, item(v(0, -0.6, 0), s.face(), 0.001f)));
        s.anim(head, 10, 46, 2, t -> {
            double y, sc;
            if (t <= 18) {
                double k = easeOut((t - 10) / 8.0);
                y = lerp(-0.4, 1.7, k);
                sc = 2.2 * Math.min(1, (t - 10) / 4.0 + 0.2);
            } else if (t <= 38) {
                y = 1.7 + 0.1 * Math.sin(t * 0.35);
                sc = 2.2;
            } else {
                double k = easeIn((t - 38) / 8.0);
                y = lerp(1.7, -0.5, k);
                sc = 2.2 * (1 - k);
            }
            return item(v(0, y, 0), qY(s.faceYaw + 0.1 * (t - 10)), (float) Math.max(0.001, sc));
        });

        s.at(4, () -> s.sound("entity.ender_dragon.flap", 0.8f, 1.0f));
        s.at(12, () -> s.sound("entity.ender_dragon.flap", 0.8f, 0.9f));
        s.at(18, () -> s.longSound("entity.ender_dragon.growl", 1.0f, 1.0f));

        // Nefes spirali
        s.every(20, 36, 1, t -> {
            Location c = s.loc(0, 1.55, 0);
            for (int k = 0; k < 6; k++) {
                double a = t * 0.6 + k * Math.PI / 3;
                s.dir(P.DRAGON_BREATH, c, Math.cos(a), s.rd(-0.05, 0.15), Math.sin(a), 0.25);
            }
            if (t % 2 == 0) s.particle(P.WITCH, c, 3, 0.4, 0);
        });
        // Yerde mor bulut halkasi
        s.every(22, 40, 2, t -> {
            double r = Math.min(2.6, 0.6 + (t - 22) * 0.18);
            for (int i = 0; i < 18; i++) {
                double a = i * Math.PI * 2 / 18 + t * 0.05;
                s.dust(s.loc(Math.cos(a) * r, 0.15, Math.sin(a) * r), BREATH, 1.0f, 1, 0.05);
            }
        });
        s.at(38, () -> s.sound("entity.ender_dragon.shoot", 0.8f, 0.8f));
        s.at(44, () -> {
            s.sound("entity.dragon_fireball.explode", 0.6f, 1.2f);
            s.dust(s.loc(0, 0.4, 0), BREATH, 1.0f, 25, 0.6);
            s.particle(P.DRAGON_BREATH, s.loc(0, 0.4, 0), 20, 0.5, 0.02);
        });
    }

    /** Gercekcilik katmani: ek 3D detaylar, enkaz, sok dalgalari ve parlamalar. */
    private void extras(FxSession s) {

        // Kafanin iki yaninda acilip cirpan ejderha kanatlari (zar + kemik)
        for (int side = -1; side <= 1; side += 2) {
            final int sd = side;
            BlockDisplay mem = s.bright(s.block(Material.BLACK_STAINED_GLASS, wing(s, 14, sd, 0)));
            BlockDisplay mem2 = s.bright(s.block(Material.PURPLE_STAINED_GLASS, wing(s, 14, sd, 1)));
            BlockDisplay bone = s.lit(s.block(Material.GRAY_CONCRETE, wing(s, 14, sd, 2)));
            s.anim(mem, 16, 44, 2, t -> wing(s, t, sd, 0));
            s.anim(mem2, 16, 44, 2, t -> wing(s, t, sd, 1));
            s.anim(bone, 16, 44, 2, t -> wing(s, t, sd, 2));
        }
        s.every(18, 38, 6, t -> s.sound(s.loc(0, 2, 0), "entity.ender_dragon.flap", 0.6f, 1.1f));
        FxKit.glow(s, Material.MAGENTA_STAINED_GLASS, v(0, 0.3, 0), 0.9f, 4, 12, 0.2);
        FxKit.shockRing(s, 0.05, 0.4, 3.0, 20, 10, Material.PURPLE_STAINED_GLASS, 20, 0.22f, true);
    }

    /** part: 0 = koyu zar, 1 = mor zar, 2 = ust kemik. */
    private static Transformation wing(FxSession s, int t, int side, int part) {
        double y;
        if (t <= 18) y = lerp(-0.4, 1.7, easeOut((t - 10) / 8.0));
        else if (t <= 38) y = 1.7 + 0.1 * Math.sin(t * 0.35);
        else y = lerp(1.7, -0.5, easeIn((t - 38) / 8.0));
        double grow = t < 18 ? Math.max(0.001, (t - 14) / 4.0) : (t > 38 ? Math.max(0.001, 1 - (t - 38) / 6.0) : 1);
        double flap = t < 18 ? 0.3 : 0.55 * Math.sin((t - 18) * 0.55);
        Quaternionf yaw = qY(s.faceYaw + 0.1 * (t - 10));
        Quaternionf q = mul(yaw, qZ(side * flap));
        float span = (float) (2.0 * grow), hgt = (float) (1.1 * grow);
        Vector3f root = v(0, y + 0.1, 0).add(rot(yaw, v(side * 0.45 * grow, 0, 0)));
        Vector3f c;
        Vector3f size;
        if (part == 0) {
            size = v(span, hgt, 0.04 * grow);
            c = v(side * span / 2, -hgt * 0.25, 0);
        } else if (part == 1) {
            size = v(span * 0.8, hgt * 0.7, 0.07 * grow);
            c = v(side * span * 0.45, -hgt * 0.3, 0.01);
        } else {
            size = v(span, 0.09 * grow + 0.001, 0.09 * grow + 0.001);
            c = v(side * span / 2, hgt * 0.25, 0);
        }
        Vector3f center = new Vector3f(root).add(rot(q, c));
        return cube(center, q, v(Math.max(0.001, size.x), Math.max(0.001, size.y), Math.max(0.001, size.z)));
    }
}
