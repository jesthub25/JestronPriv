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
/** Yerden firlayan ruh zincirleri kurbanin hayalet kafatasini baglar ve ruh atesine ceker. */
public final class SoulChainsEffect extends KillEffect {

    public SoulChainsEffect() {
        super("ruh_zincirleri", "&3&lRuh Zincirleri", Material.SOUL_LANTERN, true,
                "Yerden fırlayan zincirler hayalet",
                "kafatasını bağlar ve ruh ateşine çeker.");
    }

    private static final int CHAINS = 6, LINKS = 7, BIND = 11, PULL = 30;

    private static Vector3f skull(int t) {
        double shake = t > BIND && t < PULL ? Math.sin(t * 2.3) * 0.04 : 0;
        double y = t < PULL ? 1.6 + Math.sin(t * 0.3) * 0.05 : 1.6 - smooth(phase(t, PULL, 38)) * 1.7;
        return v(shake, y, 0);
    }

    @Override
    public void play(FxSession s) {
        item(s, Material.SKELETON_SKULL, BRIGHT, 2, 38, 1, 4, t -> Pose.of(skull(t), mul(s.face(), qY(Math.sin(t * 0.2) * 0.3)), 0.7f));
        // Zincirler: yerdeki noktadan kafataasina halka halka uzanir
        for (int c = 0; c < CHAINS; c++) {
            final double a = c * Math.PI * 2 / CHAINS + 0.3;
            final Vector3f root = v(Math.cos(a) * 2.0, 0.05, Math.sin(a) * 2.0);
            for (int l = 0; l < LINKS; l++) {
                final int li = l;
                final int appear = 3 + c / 2 + l;
                block(s, li % 2 == 0 ? Material.GRAY_CONCRETE : Material.LIGHT_GRAY_CONCRETE, LIT, appear, 40, 1, 1, t -> {
                    Vector3f tip = skull(t);
                    double tension = t > BIND ? 0.0 : 0.25;                  // gevsek -> gergin
                    double u = (li + 0.5) / LINKS;
                    if (t > PULL) u *= 1 - smooth(phase(t, PULL + 2, 40)) * 0.9;  // zincir yere geri cekilir
                    Vector3f p = lerp(root, tip, u);
                    p.y += (float) (Math.sin(u * Math.PI) * tension);
                    Vector3f dir = new Vector3f(tip).sub(root).normalize();
                    Quaternionf q = mul(fromTo(v(0, 1, 0), dir), qY(li % 2 == 0 ? 0 : Math.PI / 2));
                    return Pose.of(p, q, v(0.16, 0.3, 0.05));
                });
            }
        }
        // Ruh atesi cukuru
        for (int i = 0; i < 5; i++) {
            final double a = i * Math.PI * 2 / 5;
            block(s, Material.SOUL_FIRE, BRIGHT, PULL - 2, 42, 1, 3, t -> {
                double k = smooth(phase(t, PULL - 2, PULL + 4));
                return Pose.of(v(Math.cos(a) * 0.35, 0.3 * k, Math.sin(a) * 0.35), qY(a), v(0.55, 0.6 * k + 0.01, 0.55));
            });
        }
        // Parcaciklar
        s.every(4, BIND, 1, t -> s.particle(P.SOUL_FIRE_FLAME, s.loc(0, 0.1, 0), 3, 1.6, 0.02));
        s.every(BIND, PULL, 2, t -> {
            Vector3f k = skull(t);
            s.particle(P.SOUL, s.loc(k.x, k.y + 0.3, k.z), 1, 0.2, 0.02);
            s.particle(P.SCULK_SOUL, s.loc(k.x, k.y, k.z), 1, 0.25, 0.01);
        });
        s.every(PULL, 42, 2, t -> s.particle(P.SOUL_FIRE_FLAME, s.loc(0, 0.3, 0), 4, 0.3, 0.06));
        s.at(38, () -> {
            s.particle(P.SOUL, s.loc(0, 0.5, 0), 12, 0.4, 0.08);
            s.particle(P.SCULK_SOUL, s.loc(0, 0.4, 0), 10, 0.3, 0.1);
        });
        // Ses tasarimi
        for (int c = 0; c < CHAINS; c++) {
            final int at = 3 + c;
            s.at(at, () -> s.sound("block.chain.place", 0.9f, s.rp(0.6f, 0.9f)));
        }
        s.at(BIND, () -> {
            s.sound("block.chain.break", 1.0f, 0.5f);
            s.sound("entity.wither.hurt", 0.5f, 0.6f);
        });
        s.every(BIND + 4, PULL - 2, 5, t -> s.sound("block.chain.place", 0.5f, 0.5f));
        s.at(PULL, () -> {
            s.sound("particle.soul_escape", 1.0f, 0.6f);
            s.sound("block.soul_sand.break", 0.9f, 0.6f);
            s.longSound("block.fire.ambient", 0.8f, 0.7f);
        });
        s.at(38, () -> s.sound("particle.soul_escape", 1.0f, 1.0f));
    }
}
