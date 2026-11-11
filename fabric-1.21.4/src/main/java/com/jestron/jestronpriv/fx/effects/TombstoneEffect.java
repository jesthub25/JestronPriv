package com.jestron.jestronpriv.fx.effects;

import com.jestron.jestronpriv.fx.FxSession;
import com.jestron.jestronpriv.fx.api.Transformation;
import com.jestron.jestronpriv.fx.api.ItemDisplay;
import com.jestron.jestronpriv.fx.api.Location;
import com.jestron.jestronpriv.fx.api.Color;
import com.jestron.jestronpriv.fx.FxKit;
import com.jestron.jestronpriv.fx.KillEffect;
import com.jestron.jestronpriv.fx.P;
import com.jestron.jestronpriv.fx.api.Material;
import com.jestron.jestronpriv.fx.api.BlockDisplay;
import com.jestron.jestronpriv.fx.api.TextDisplay;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static com.jestron.jestronpriv.fx.FxMath.*;

/** Yerden, uzerinde kurbanin adi yazan bir mezar tasi yukselir. */
public final class TombstoneEffect extends KillEffect {

    public TombstoneEffect() {
        super("mezar_tasi", "&7&lMezar Taşı", Material.MOSSY_STONE_BRICK_WALL, true,
                "Yerden kurbanın adının yazdığı",
                "bir mezar taşı yükselir.");
    }

    private record Part(Material m, Vector3f center, Vector3f size) {
    }

    @Override
    public void play(FxSession s) {
        playBase(s);
        extras(s);
    }

    private void playBase(FxSession s) {
        final Quaternionf face = s.face();
        final double depth = -1.9;
        List<Part> parts = new ArrayList<>();
        parts.add(new Part(Material.COARSE_DIRT, v(0, 0.08, 0.6), v(0.9, 0.16, 1.3)));
        parts.add(new Part(Material.COBBLESTONE, v(0, 0.11, -0.25), v(1.1, 0.22, 0.45)));
        parts.add(new Part(Material.SMOOTH_STONE, v(0, 0.795, -0.25), v(0.85, 1.15, 0.2)));
        parts.add(new Part(Material.SMOOTH_STONE, v(0, 1.44, -0.25), v(0.6, 0.14, 0.2)));
        parts.add(new Part(Material.SMOOTH_STONE, v(0, 1.55, -0.25), v(0.3, 0.08, 0.2)));
        parts.add(new Part(Material.MOSS_BLOCK, v(0.33, 0.27, -0.25), v(0.24, 0.1, 0.23)));
        parts.add(new Part(Material.MOSS_BLOCK, v(-0.36, 0.25, -0.2), v(0.16, 0.06, 0.12)));

        for (Part p : parts) {
            final Part part = p;
            BlockDisplay d = s.lit(s.block(part.m, tfPart(face, part, depth, 0)));
            s.anim(d, 2, 14, 2, t -> {
                double k = easeOut((t - 2) / 12.0);
                double jitter = t < 14 ? (t % 4 == 0 ? 0.03 : -0.03) : 0;
                return tfPart(face, part, depth * (1 - k), jitter);
            });
            s.tfAt(40, d, tfPart(face, part, depth, 0), 8);
            s.tfAt(48, d, cube(rot(face, new Vector3f(part.center).add(0, (float) depth, 0)), face, 0.001f), 1);
        }

        // Yazi
        String name = s.victimName == null ? "?" : s.victimName;
        String txt = "§8✝ §7R.I.P §8✝\n§c§l" + name;
        // Tasin ustunde suzulen hologram yazi (her acidan okunur)
        Vector3f tpos = rot(face, v(0, 1.68, -0.25));
        TextDisplay text = s.bright(s.text(txt, item(tpos, q(), 0.001f)));
        if (text != null) text.setBillboard(com.jestron.jestronpriv.fx.api.Display.Billboard.VERTICAL);
        s.tfAt(14, text, item(tpos, q(), 0.6f), 3);
        s.tfAt(40, text, item(tpos, q(), 0.001f), 4);

        // Toprak ve sesler
        s.every(2, 14, 3, t -> {
            s.groundCrack(s.loc(0, 0.1, 0), 12, 0.6);
            s.sound(t % 2 == 0 ? "block.rooted_dirt.break" : "block.gravel.break", 0.8f, s.rp(0.6f, 0.8f));
        });
        s.at(14, () -> s.sound("block.bell.use", 0.9f, 0.5f));
        s.at(16, () -> s.longSound("block.bell.resonate", 0.5f, 0.6f));
        s.at(24, () -> s.sound("entity.phantom.ambient", 0.4f, 0.5f));
        s.every(14, 38, 3, t -> {
            s.particle(P.SCULK_SOUL, s.lloc(s.rd(-0.4, 0.4), 0.3, 0.4), 1, 0.1, 0.02);
            s.particle(P.SOUL, s.lloc(s.rd(-0.5, 0.5), 0.2, 0.6), 1, 0.15, 0.01);
        });
        s.at(40, () -> s.groundCrack(s.loc(0, 0.1, 0), 20, 0.6));
    }

    private static com.jestron.jestronpriv.fx.api.Transformation tfPart(Quaternionf face, Part p, double dy, double jitterX) {
        Vector3f local = new Vector3f(p.center).add((float) jitterX, (float) dy, 0);
        return cube(rot(face, local), face, p.size);
    }

    /** Gercekcilik katmani: ek 3D detaylar, enkaz, sok dalgalari ve parlamalar. */
    private void extras(FxSession s) {

        final Quaternionf face = s.face();
        // Yerden firlayan toprak parcalari
        Material g = s.groundMaterial(Material.DIRT);
        FxKit.debris(s, v(0, 0.2, 0), new Material[]{g, Material.COARSE_DIRT, Material.DIRT}, 7, 0.05, 0.16, 0.3, 0.5, 0.1f, 0.22f, 2, false);
        FxKit.debris(s, v(0, 0.2, 0), new Material[]{Material.COARSE_DIRT, g}, 5, 0.05, 0.14, 0.25, 0.45, 0.1f, 0.18f, 8, false);
        // Mezar susleri: cicekler, orumcek agi, fener
        Object[][] deco = {
                {Material.WITHER_ROSE, v(-0.25, 0.16, 0.55), 0.5f},
                {Material.POPPY, v(0.22, 0.16, 0.75), 0.45f},
                {Material.DEAD_BUSH, v(0.05, 0.16, 1.05), 0.45f},
                {Material.COBWEB, v(-0.3, 1.1, -0.13), 0.42f},
                {Material.LANTERN, v(0.78, 0.0, 0.3), 0.55f}};
        for (Object[] dd : deco) {
            final Material m = (Material) dd[0];
            final Vector3f lc = (Vector3f) dd[1];
            final float sz = (float) dd[2];
            Vector3f base = rot(face, new Vector3f(lc).add(0, sz / 2, 0));
            Vector3f hidden = rot(face, new Vector3f(lc).add(0, -1.9f, 0));
            BlockDisplay d = s.block(m, cube(hidden, face, 0.001f));
            if (m == Material.LANTERN) s.bright(d);
            else s.lit(d);
            s.tfAt(15, d, cube(base, face, 0.001f), 0);
            s.tfAt(17, d, cube(base, face, sz), 3);
            s.tfAt(40, d, cube(base, face, 0.001f), 4);
        }
        // Mezarin uzerinde suzulen sis ve fenerde ruh alevi
        s.every(16, 40, 2, t -> s.particle(P.WHITE_SMOKE, s.lloc(s.rd(-0.6, 0.6), 0.15, s.rd(-0.2, 1.1)), 1, 0.05, 0.005));
        s.every(18, 38, 4, t -> s.particle(P.SOUL_FIRE_FLAME, s.lloc(0.78, 0.45, 0.3), 1, 0.03, 0.005));
    }
}
