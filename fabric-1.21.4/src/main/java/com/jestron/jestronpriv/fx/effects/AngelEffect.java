package com.jestron.jestronpriv.fx.effects;

import com.jestron.jestronpriv.fx.FxKit;
import com.jestron.jestronpriv.fx.FxSession;
import com.jestron.jestronpriv.fx.KillEffect;
import com.jestron.jestronpriv.fx.P;
import com.jestron.jestronpriv.fx.api.BlockDisplay;
import com.jestron.jestronpriv.fx.api.Color;
import com.jestron.jestronpriv.fx.api.ItemDisplay;
import com.jestron.jestronpriv.fx.api.Material;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static com.jestron.jestronpriv.fx.FxMath.*;

/**
 * Gokten kutsal bir isik iner; gercek tuylerden olusan 3D melek kanatlari acilip cirpar,
 * basin uzerinde altin hale doner, havada tuyler suzulur ve yere isik halkasi yayilir.
 */
public final class AngelEffect extends KillEffect {

    private static final Color GOLD = Color.fromRGB(255, 220, 80);
    private static final Color WHITE = Color.fromRGB(255, 255, 255);
    /** Kanat tuyleri: {acı (derece, yataydan yukari), uzunluk, olcek}. */
    private static final double[][] FEATHERS = {
            {78, 0.55, 0.55}, {62, 0.8, 0.62}, {46, 1.05, 0.7}, {30, 1.25, 0.78}, {15, 1.4, 0.82},
            {0, 1.45, 0.82}, {-14, 1.35, 0.76}, {-28, 1.15, 0.68}, {-42, 0.9, 0.6}, {-56, 0.65, 0.52}};

    public AngelEffect() {
        super("melek", "&f&lMelek Kanatları", Material.ELYTRA, true,
                "Gökten kutsal ışık iner, tüylerden",
                "kanatlar çırpar, tüyler süzülür.");
    }

    private static double rise(int t) {
        return lerp(1.2, 2.3, clamp01((t - 6) / 34.0));
    }

    private static double open(int t) {
        if (t < 6) return 0.001;
        if (t <= 12) return easeOutBack((t - 6) / 6.0);
        if (t <= 38) return 1;
        return Math.max(0.001, 1 - (t - 38) / 6.0);
    }

    @Override
    public void play(FxSession s) {
        final Quaternionf face = s.face();

        // Kutsal isik sutunu (dis ve ic katman)
        FxKit.pillar(s, Material.WHITE_STAINED_GLASS, 0, 0, 0, 14, 0.75f, 2, 20);
        FxKit.pillar(s, Material.YELLOW_STAINED_GLASS, 0, 0, 0, 14, 0.35f, 2, 18);

        // 3D tuy kanatlar: her kanatta 10 tuy, kok noktasindan yelpaze gibi acilir ve cirpar
        for (int side = -1; side <= 1; side += 2) {
            final int sd = side;
            for (int i = 0; i < FEATHERS.length; i++) {
                final double ang = Math.toRadians(FEATHERS[i][0]);
                final double len = FEATHERS[i][1];
                final float sc = (float) FEATHERS[i][2];
                final double lag = i * 0.06;
                final java.util.function.IntFunction<com.jestron.jestronpriv.fx.api.Transformation> fn = t -> {
                    double o = open(t);
                    double flap = 0.35 + 0.4 * Math.sin((t - 12) * 0.45 - lag);
                    if (t < 12) flap = 0.7;
                    // kanat duzlemi: kok etrafinda geriye dogru katlanir (flap)
                    Quaternionf wing = mul(face, qY(sd * flap));
                    Vector3f dir = v(sd * Math.cos(ang), Math.sin(ang), 0);
                    Vector3f root = v(sd * 0.18, rise(t) + 0.15, -0.28);
                    Vector3f localC = new Vector3f(dir).mul((float) (len * 0.5 * o));
                    Vector3f c = rot(face, root).add(rot(wing, localC));
                    // tuy sprite'inin ucu (+X,+Y) kanadin disina baksin
                    double spriteAng = sd > 0 ? ang - Math.PI / 4 : Math.PI - ang - Math.PI / 4;
                    Quaternionf q = mul(wing, qZ(spriteAng));
                    return item(c, q, (float) Math.max(0.001, sc * o * 1.25));
                };
                com.jestron.jestronpriv.fx.api.Transformation t6 = fn.apply(6);
                ItemDisplay f = s.bright(s.item(Material.FEATHER, item(t6.getTranslation(), t6.getLeftRotation(), 0.001f)));
                s.anim(f, 8, 44, 2, fn);
                com.jestron.jestronpriv.fx.api.Transformation t44 = fn.apply(44);
                s.tfAt(45, f, item(t44.getTranslation(), t44.getLeftRotation(), 0.001f), 1);
            }
        }

        // 3D altin hale
        for (int i = 0; i < 12; i++) {
            final double a = i * Math.PI * 2 / 12;
            BlockDisplay h = s.bright(s.block(Material.GOLD_BLOCK, cube(v(0, 2.3, 0), qY(-(a + 6 * 0.12)), 0.001f)));
            s.anim(h, 6, 42, 2, t -> {
                double k = t <= 10 ? Math.max(0.001, (t - 6) / 4.0) : (t >= 38 ? Math.max(0.001, (42 - t) / 4.0) : 1);
                double aa = a + t * 0.12;
                Vector3f c = v(Math.cos(aa) * 0.34, rise(t) + 1.0, Math.sin(aa) * 0.34);
                return cube(c, qY(-aa), v(0.19 * k + 0.001, 0.05 * k + 0.001, 0.07 * k + 0.001));
            });
            s.tfAt(43, h, cube(v(0, 3, 0), qY(-(a + 42 * 0.12)), 0.001f), 1);
        }
        FxKit.glow(s, Material.YELLOW_STAINED_GLASS, v(0, 2.9, 0), 0.5f, 8, 40, 0.15);

        // Suzulen tuyler
        for (int i = 0; i < 12; i++) {
            final double x0 = s.rd(-1.6, 1.6), z0 = s.rd(-1.6, 1.6), y0 = s.rd(2.5, 4.2);
            final double ph = s.rnd.nextDouble() * 6;
            final double gy = s.groundDy(x0, z0) + 0.03;
            final double fall = 0.075;
            ItemDisplay f = s.bright(s.item(Material.FEATHER, item(v(x0, y0, z0), mul(qY(ph + 4 * 0.05), qZ(0.5 * Math.sin(4 * 0.3 + ph))), 0.001f)));
            s.anim(f, 4, 42, 2, t -> {
                double y = Math.max(gy, y0 - (t - 4) * fall);
                boolean landed = y <= gy + 1e-3;
                double x = x0 + 0.3 * Math.sin(t * 0.2 + ph);
                double z = z0 + 0.3 * Math.cos(t * 0.17 + ph);
                Quaternionf r = landed ? mul(qY(ph + t * 0.05), qX(Math.PI / 2)) : mul(qY(ph + t * 0.05), qZ(0.5 * Math.sin(t * 0.3 + ph)));
                float sc = t < 8 ? (float) (0.5 * (t - 4) / 4.0) : (t >= 38 ? (float) (0.5 * (1 - (t - 38) / 4.0)) : 0.5f);
                return item(v(x, y, z), r, Math.max(0.001f, sc));
            });
        }

        // Isik halkalari ve parlak parcaciklar
        FxKit.shockRing(s, 0.04, 0.3, 3.0, 3, 12, Material.YELLOW_STAINED_GLASS, 20, 0.22f, true);
        FxKit.shockRing(s, 0.04, 0.3, 2.4, 20, 10, Material.WHITE_STAINED_GLASS, 18, 0.18f, true);
        s.every(6, 40, 2, t -> {
            double y = rise(t);
            for (int i = 0; i < 14; i++) {
                double a = i * Math.PI * 2 / 14;
                s.dust(s.loc(Math.cos(a) * 0.34, y + 1.0, Math.sin(a) * 0.34), GOLD, 0.6f, 1, 0);
            }
            s.particle(P.WAX_OFF, s.loc(s.rd(-1.4, 1.4), y + s.rd(-0.5, 1.2), s.rd(-0.6, 0.2)), 2, 0.2, 0);
            s.dust(s.loc(0, y + 0.4, 0), WHITE, 0.9f, 3, 0.5);
        });

        s.at(2, () -> s.sound("block.beacon.activate", 1.0f, 1.4f));
        s.at(4, () -> s.sound("block.amethyst_block.chime", 1.0f, 1.8f));
        s.at(8, () -> s.sound(s.loc(0, 2, 0), "block.note_block.bell", 0.6f, 1.0f));
        s.at(10, () -> s.sound(s.loc(0, 2, 0), "entity.ender_dragon.flap", 0.5f, 1.8f));
        s.at(14, () -> s.sound(s.loc(0, 2, 0), "block.note_block.bell", 0.6f, 1.26f));
        s.at(20, () -> s.sound(s.loc(0, 2, 0), "block.note_block.bell", 0.6f, 1.5f));
        s.at(30, () -> s.sound(s.loc(0, 2, 0), "entity.allay.ambient_without_item", 0.5f, 1.2f));
        s.at(40, () -> s.sound(s.loc(0, 2, 0), "block.beacon.deactivate", 0.6f, 1.6f));
    }
}
