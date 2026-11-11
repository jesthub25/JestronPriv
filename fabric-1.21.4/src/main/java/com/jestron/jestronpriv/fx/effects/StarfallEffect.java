package com.jestron.jestronpriv.fx.effects;

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

/** Gokten parlak kuyruklu yildizlar yagar, her biri isilti sacar; sonunda dev bir yildiz kurbana carpar. */
public final class StarfallEffect extends KillEffect {

    public StarfallEffect() {
        super("yildiz_yagmuru", "&e&lYıldız Yağmuru", Material.NETHER_STAR, true,
                "Gökten kuyruklu yıldızlar yağar,",
                "sonunda dev bir yıldız çarpar.");
    }

    private static final int STARS = 7, BIG_FROM = 22, BIG_HIT = 31;
    private static final Color GOLD = Color.fromRGB(255, 230, 120);

    @Override
    public void play(FxSession s) {
        final Vector3f fall = rot(s.face(), v(0.55, -1, 0.25)).normalize();
        for (int i = 0; i < STARS; i++) {
            final int st = 2 + i * 3;
            final int hit = st + 7;
            final double a = i * 2.4, r = 0.6 + (i % 3) * 0.5;
            final double hx = Math.cos(a) * r, hz = Math.sin(a) * r;
            final Vector3f end = v(hx, s.groundDy(hx, hz) + 0.1, hz);
            final Vector3f start = new Vector3f(end).sub(new Vector3f(fall).mul(8f));
            star(s, start, end, st, hit, 0.45f, 1.2f);
            s.at(hit, () -> {
                Location l = s.loc(end);
                s.particle(P.FIREWORK, l, 12, 0.1, 0.12);
                s.particle(P.END_ROD, l, 8, 0.2, 0.08);
                s.dust(l, GOLD, 1.0f, 10, 0.3);
                s.sound(l, "entity.firework_rocket.twinkle", 0.7f, s.rp(1.0f, 1.5f));
                s.sound(l, "block.amethyst_block.chime", 0.7f, s.rp(1.2f, 1.8f));
            });
        }
        // dev yildiz
        final Vector3f bEnd = v(0, s.groundDy(0, 0) + 0.3, 0);
        final Vector3f bStart = new Vector3f(bEnd).sub(new Vector3f(fall).mul(10f));
        star(s, bStart, bEnd, BIG_FROM, BIG_HIT, 1.1f, 2.6f);
        s.at(BIG_FROM, () -> s.sound("entity.firework_rocket.launch", 1.0f, 0.5f));
        FxKit.impact(s, 1.0, BIG_HIT, GOLD, false);
        s.at(BIG_HIT, () -> {
            Location l = s.loc(0, 0.8, 0);
            s.particle(P.FIREWORK, l, 50, 0.3, 0.25);
            for (int i = 0; i < 30; i++) s.dir(P.END_ROD, l, randomUnit(s.rnd), s.rd(0.15, 0.35));
            s.sound("entity.firework_rocket.large_blast", 1.2f, 0.7f);
            s.sound("entity.generic.explode", 0.6f, 1.2f);
            s.sound("block.amethyst_block.break", 1.0f, 0.7f);
        });
        FxKit.glow(s, Material.YELLOW_STAINED_GLASS, v(0, 0.8, 0), 1.4f, BIG_HIT, BIG_HIT + 8, 0.2);
        s.every(BIG_HIT + 2, 47, 2, t -> s.particle(P.END_ROD, s.loc(s.rd(-1.5, 1.5), s.rd(0.3, 2.5), s.rd(-1.5, 1.5)), 1, 0.05, 0.01));
        s.every(BIG_HIT + 3, 45, 5, t -> s.sound("entity.firework_rocket.twinkle", 0.4f, s.rp(1.2f, 1.8f)));
    }

    /** Parlak yildiz basi + kuyruk; st'den hit'e kadar duz cizgide hizlanarak duser. */
    private static void star(FxSession s, Vector3f start, Vector3f end, int st, int hit, float head, float tail) {
        final Vector3f dir = new Vector3f(end).sub(start).normalize();
        item(s, Material.NETHER_STAR, BRIGHT, st, hit, 1, 1, t -> {
            double k = phase(t, st, hit);
            k = k * k;
            Vector3f p = lerp(start, end, k);
            return Pose.of(p, mul(s.face(), qZ(t * 0.6)), head);
        });
        block(s, Material.YELLOW_STAINED_GLASS, BRIGHT, st, hit, 1, 1, t -> {
            double k = phase(t, st, hit);
            k = k * k;
            Vector3f p = lerp(start, end, k);
            Vector3f back = new Vector3f(p).sub(new Vector3f(dir).mul(tail));
            return beamPose(back, p, 0.09f * head / 0.45f);
        });
        block(s, Material.WHITE_STAINED_GLASS, BRIGHT, st, hit, 1, 1, t -> {
            double k = phase(t, st, hit);
            k = k * k;
            Vector3f p = lerp(start, end, k);
            Vector3f back = new Vector3f(p).sub(new Vector3f(dir).mul(tail * 0.55f));
            return beamPose(back, p, 0.04f * head / 0.45f);
        });
        s.every(st, hit, 1, t -> {
            double k = phase(t, st, hit);
            Vector3f p = lerp(start, end, k * k);
            s.particle(P.END_ROD, s.loc(p), 1, 0.05, 0.01);
        });
    }
}
