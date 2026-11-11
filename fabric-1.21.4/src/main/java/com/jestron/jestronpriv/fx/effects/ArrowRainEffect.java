package com.jestron.jestronpriv.fx.effects;

import com.jestron.jestronpriv.fx.FxKit;
import com.jestron.jestronpriv.fx.FxSession;
import com.jestron.jestronpriv.fx.api.BlockDisplay;
import com.jestron.jestronpriv.fx.KillEffect;
import com.jestron.jestronpriv.fx.P;
import com.jestron.jestronpriv.fx.api.Location;
import com.jestron.jestronpriv.fx.api.Material;
import com.jestron.jestronpriv.fx.api.ItemDisplay;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static com.jestron.jestronpriv.fx.FxMath.*;

/** 4-5 blok yukaridan oklar yagar ve yere saplanir. */
public final class ArrowRainEffect extends KillEffect {

    public ArrowRainEffect() {
        super("ok_yagmuru", "&f&lOk Yağmuru", Material.ARROW, true,
                "4-5 blok yukarıdan oklar yağar",
                "ve titreyerek yere saplanır.");
    }

    @Override
    public void play(FxSession s) {
        final int n = 16;
        final float sc = 1.15f;
        final float tipOff = SPRITE_TIP_DIST * sc;
        final int fall = 6;

        s.at(1, () -> s.sound(s.loc(0, 5, 0), "item.crossbow.shoot", 0.6f, 0.8f));
        // Gokyuzunde oklarin ciktigi karanlik bulut
        s.every(0, 14, 2, t -> {
            s.particle(P.SMOKE, s.loc(0, 5.4, 0), 8, 1.6, 0.2, 1.6, 0.005);
            s.particle(P.SMOKE, s.loc(0, 5.2, 0), 6, 1.8, 0.2, 1.8, 0.01);
        });

        for (int i = 0; i < n; i++) {
            double ang = s.rnd.nextDouble() * Math.PI * 2;
            double rad = i == 0 ? 0.15 : 0.45 + s.rnd.nextDouble() * 2.0;
            double x = Math.cos(ang) * rad, z = Math.sin(ang) * rad;
            double gy = s.groundDy(x, z);
            double tilt = s.rnd.nextDouble() * 0.28;
            double td = s.rnd.nextDouble() * Math.PI * 2;
            Vector3f dir = v(Math.sin(tilt) * Math.cos(td), -Math.cos(tilt), Math.sin(tilt) * Math.sin(td)).normalize();
            double spin = s.rnd.nextDouble() * Math.PI;
            Quaternionf q1 = spriteToward(dir, spin);
            Quaternionf q2 = spriteToward(dir, spin + Math.PI / 2);

            Vector3f tipP = v(x, gy, z).add(new Vector3f(dir).mul(0.28f));
            Vector3f end = new Vector3f(tipP).sub(new Vector3f(dir).mul(tipOff));
            float len = (float) ((4.4 + s.rnd.nextDouble() * 0.7) / Math.cos(tilt));
            Vector3f start = new Vector3f(end).sub(new Vector3f(dir).mul(len));
            final int st = 2 + (i * 13) / n;
            final boolean flaming = i % 4 == 1;

            ItemDisplay a1 = s.lit(s.item(Material.ARROW, item(start, q1, 0.001f)));
            ItemDisplay a2 = s.lit(s.item(Material.ARROW, item(start, q2, 0.001f)));

            s.tfAt(st, a1, item(start, q1, sc), 0);
            s.tfAt(st, a2, item(start, q2, sc), 0);
            if (i % 2 == 0) {
                final Location sl = s.loc(start);
                final float pitch = s.rp(0.8f, 1.3f);
                s.at(st, () -> s.sound(sl, "entity.arrow.shoot", 0.45f, pitch));
            }
            for (int k = 1; k <= fall; k++) {
                double u = (double) k / fall;
                double e = u * u * 0.6 + u * 0.4;
                Vector3f p = lerp(start, end, e);
                s.tfAt(st + k, a1, item(p, q1, sc), 1);
                s.tfAt(st + k, a2, item(p, q2, sc), 1);
                if (flaming) {
                    final Location fl = s.loc(p);
                    s.at(st + k, () -> {
                        s.particle(P.FLAME, fl, 3, 0.05, 0.01);
                        s.particle(P.SMOKE, fl, 1, 0.04, 0.01);
                    });
                }
                if (k == fall / 2 && i % 2 == 0) {
                    FxKit.ghostItem(s, Material.ARROW, item(lerp(start, end, e * 0.75), q1, sc * 0.9f), st + k, 3, flaming);
                }
            }
            final int hit = st + fall + 1;
            final Location hl = s.loc(tipP.x, tipP.y + 0.08, tipP.z);
            final float hp = s.rp(0.9f, 1.25f);
            s.at(hit, () -> {
                s.sound(hl, "entity.arrow.hit", 0.7f, hp);
                s.groundCrack(hl, 6, 0.12);
                s.particle(P.CRIT, hl, 3, 0.1, 0.1);
                FxKit.puff(s, hl, 6);
                if (flaming) FxKit.embers(s, hl, 5, 0.08);
            });
            if (flaming) {
                final Quaternionf fq = qY(spin);
                BlockDisplay fire = s.bright(s.block(Material.FIRE, cube(v(x, gy + 0.0005, z), fq, 0.001f)));
                s.tfAt(hit, fire, cube(v(x, gy + 0.2, z), fq, 0.4f), 2);
                s.tfAt(40, fire, cube(v(x, gy + 0.0005, z), fq, 0.001f), 4);
            }
            // titreme (uc sabit)
            Vector3f axis = new Vector3f(dir).cross(0, 1, 0);
            if (axis.lengthSquared() < 1e-4f) axis = v(Math.cos(td), 0, Math.sin(td));
            for (int k = 0; k <= 6; k++) {
                double ang2 = 0.13 * Math.exp(-k / 2.5) * Math.sin(k * 1.9);
                Quaternionf w = qAxis(ang2, axis);
                Vector3f c = new Vector3f(tipP).add(rot(w, new Vector3f(end).sub(tipP)));
                s.tfAt(hit + k, a1, item(c, mul(w, q1), sc), 1);
                s.tfAt(hit + k, a2, item(c, mul(w, q2), sc), 1);
            }
            s.tfAt(42, a1, item(end, q1, 0.001f), 6);
            s.tfAt(42, a2, item(end, q2, 0.001f), 6);
        }
    }
}
