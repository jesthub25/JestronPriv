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
/** Gokten dev bir demir yumruk iner, yeri ezer, sonra geri cekilir. */
public final class IronFistEffect extends KillEffect {

    public IronFistEffect() {
        super("demir_yumruk", "&7&lDemir Yumruk", Material.IRON_BLOCK, true,
                "Gökten dev bir demir yumruk iner",
                "ve yeri ezip geri çekilir.");
    }

    private static final int HIT = 9;

    private static Vector3f fist(int t) {
        double y;
        if (t <= HIT) y = lerp(8.5, 0.62, Math.pow(phase(t, 2, HIT), 2.2));
        else if (t < 31) y = 0.62 - Math.max(0, Math.sin((t - HIT) * 0.9)) * 0.05 * Math.max(0, 1 - (t - HIT) / 6.0);
        else y = 0.62 + Math.pow(phase(t, 31, 41), 1.6) * 7;
        return v(0, y, 0);
    }

    @Override
    public void play(FxSession s) {
        final Quaternionf base = mul(s.face(), qZ(0.08));
        Object[][] parts = {
                // malzeme, ofset, boyut
                {Material.IRON_BLOCK, v(0, 0.05, 0), v(1.05, 0.95, 0.9)},             // avuc / el sirti
                {Material.IRON_BLOCK, v(-0.38, -0.5, 0.18), v(0.25, 0.32, 0.55)},     // parmaklar (kivrik)
                {Material.IRON_BLOCK, v(-0.12, -0.52, 0.2), v(0.25, 0.32, 0.58)},
                {Material.IRON_BLOCK, v(0.14, -0.52, 0.2), v(0.25, 0.32, 0.58)},
                {Material.IRON_BLOCK, v(0.4, -0.5, 0.18), v(0.25, 0.32, 0.55)},
                {Material.IRON_BLOCK, v(0.62, -0.15, 0.3), v(0.26, 0.5, 0.3)},        // basparmak
                {Material.GRAY_CONCRETE, v(-0.38, -0.36, 0.47), v(0.2, 0.06, 0.04)},  // bogum cizgileri
                {Material.GRAY_CONCRETE, v(-0.12, -0.36, 0.5), v(0.2, 0.06, 0.04)},
                {Material.GRAY_CONCRETE, v(0.14, -0.36, 0.5), v(0.2, 0.06, 0.04)},
                {Material.GRAY_CONCRETE, v(0.4, -0.36, 0.47), v(0.2, 0.06, 0.04)},
                {Material.LIGHT_GRAY_CONCRETE, v(0, 0.75, -0.05), v(0.85, 0.5, 0.75)}, // bilek
                {Material.SMOOTH_STONE, v(0, 1.9, -0.05), v(0.78, 1.9, 0.7)},          // kol
                {Material.GOLD_BLOCK, v(0, 1.05, -0.05), v(0.9, 0.12, 0.8)}};          // bileklik
        for (Object[] p : parts) {
            final Material m = (Material) p[0];
            final Vector3f off = (Vector3f) p[1], size = (Vector3f) p[2];
            block(s, m, LIT, 2, 41, 1, 1, t -> attach(fist(t), base, off, q(), size));
        }
        FxKit.impact(s, 1.5, HIT, Color.fromRGB(170, 170, 170), false);
        FxKit.shockRing(s, 0.04, 0.5, 3.8, HIT, 8, Material.LIGHT_GRAY_STAINED_GLASS, 20, 0.45f, false);
        FxKit.shockRing(s, 0.04, 0.3, 2.4, HIT + 3, 7, Material.WHITE_STAINED_GLASS, 14, 0.25f, false);
        FxKit.cracks(s, 9, 2.2, Material.BLACK_CONCRETE, HIT, 3);
        // Ruzgar cizgileri (dusus hizi)
        s.every(3, HIT - 1, 1, t -> {
            Vector3f c = fist(t);
            for (int i = 0; i < 4; i++) s.dir(P.CLOUD, s.loc(c.x + s.rd(-0.6, 0.6), c.y + 1.2, c.z + s.rd(-0.6, 0.6)), 0, 1, 0, 0.3);
        });
        s.at(HIT, () -> s.particle(P.EXPLOSION_EMITTER, s.loc(0, 0.4, 0), 1, 0, 0));
        s.at(31, () -> FxKit.dustRing(s, s.loc(0, 0.1, 0), 16, 0.4, 0.2));
        // Ses tasarimi
        s.sound("item.trident.riptide_1", 0.9f, 0.6f);
        s.at(5, () -> s.sound("entity.iron_golem.attack", 1.0f, 0.6f));
        s.at(HIT, () -> {
            s.sound("item.mace.smash_ground_heavy", 1.0f, 0.55f);
            s.sound("entity.iron_golem.damage", 1.0f, 0.5f);
            s.sound("block.anvil.land", 0.7f, 0.6f);
            s.sound("entity.generic.explode", 0.7f, 0.8f);
        });
        s.at(31, () -> s.sound("entity.iron_golem.attack", 0.8f, 0.8f));
        s.at(33, () -> s.sound("item.trident.riptide_1", 0.7f, 0.9f));
    }
}
