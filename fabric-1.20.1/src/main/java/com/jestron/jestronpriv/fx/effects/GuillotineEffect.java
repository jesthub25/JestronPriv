package com.jestron.jestronpriv.fx.effects;

import com.jestron.jestronpriv.fx.FxSession;
import com.jestron.jestronpriv.fx.FxKit;
import com.jestron.jestronpriv.fx.KillEffect;
import com.jestron.jestronpriv.fx.P;
import com.jestron.jestronpriv.fx.api.Color;
import com.jestron.jestronpriv.fx.api.Location;
import com.jestron.jestronpriv.fx.api.Material;
import com.jestron.jestronpriv.fx.api.BlockDisplay;
import com.jestron.jestronpriv.fx.api.ItemDisplay;
import com.jestron.jestronpriv.fx.api.ItemStack;
import com.jestron.jestronpriv.fx.api.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static com.jestron.jestronpriv.fx.FxMath.*;

/** Yerden bir giyotin yukselir, bicak duser ve kurbanin kafasi yuvarlanir. */
public final class GuillotineEffect extends KillEffect {

    private static final double SINK = -3.7;

    public GuillotineEffect() {
        super("giyotin", "&4&lGiyotin", Material.IRON_AXE, true,
                "Yerden bir giyotin yükselir,",
                "bıçak düşer ve kafa yuvarlanır.");
    }

    private record Part(Material m, Vector3f c, Vector3f size, double rollZ) {
    }

    @Override
    public void play(FxSession s) {
        playBase(s);
        extras(s);
    }

    private void playBase(FxSession s) {
        final Quaternionf face = s.face();
        Part[] frame = {
                new Part(Material.DARK_OAK_PLANKS, v(0, 0.075, 0), v(1.7, 0.15, 0.9), 0),
                new Part(Material.DARK_OAK_LOG, v(0.62, 1.70, 0), v(0.22, 3.1, 0.22), 0),
                new Part(Material.DARK_OAK_LOG, v(-0.62, 1.70, 0), v(0.22, 3.1, 0.22), 0),
                new Part(Material.DARK_OAK_PLANKS, v(0, 3.38, 0), v(1.6, 0.26, 0.32), 0),
                new Part(Material.STRIPPED_DARK_OAK_LOG, v(0, 0.56, 0), v(1.02, 0.2, 0.26), 0),
                new Part(Material.STRIPPED_DARK_OAK_LOG, v(0, 1.02, 0), v(1.02, 0.16, 0.26), 0),
        };
        for (Part p : frame) {
            final Part part = p;
            BlockDisplay d = s.lit(s.block(part.m, tf(face, part, SINK)));
            s.anim(d, 2, 12, 2, t -> tf(face, part, SINK * (1 - easeOut((t - 2) / 10.0))));
            s.tfAt(40, d, tf(face, part, SINK), 8);
            s.tfAt(48, d, cube(rot(face, new Vector3f(part.c).add(0, (float) SINK, 0)), face, 0.001f), 1);
        }

        // Bicak (egimli kenar)
        final double bladeTop = 2.85, bladeBottom = 0.98;
        final Part bladePart = new Part(Material.IRON_BLOCK, v(0, bladeTop, 0.16), v(1.02, 0.42, 0.05), Math.toRadians(-18));
        BlockDisplay blade = s.lit(s.block(Material.IRON_BLOCK, tf(face, bladePart, SINK)));
        s.anim(blade, 2, 12, 2, t -> tf(face, bladePart, SINK * (1 - easeOut((t - 2) / 10.0))));
        for (int t = 17; t <= 21; t++) {
            double u = (t - 16) / 5.0;
            double y = lerp(bladeTop, bladeBottom, u * u);
            s.tfAt(t, blade, tf(face, new Part(Material.IRON_BLOCK, v(0, y, 0.16), bladePart.size, bladePart.rollZ), 0), 1);
        }
        Part bladeDown = new Part(Material.IRON_BLOCK, v(0, bladeBottom, 0.16), bladePart.size, bladePart.rollZ);
        s.tfAt(40, blade, tf(face, bladeDown, SINK), 8);
        s.tfAt(48, blade, cube(rot(face, v(0, bladeBottom + SINK, 0.16)), face, 0.001f), 1);

        // Kafa
        ItemStack headItem = s.victimHead != null ? s.victimHead.clone() : new ItemStack(Material.SKELETON_SKULL);
        final float hs = 1.1f;
        final Vector3f headStart = v(0, 0.80, 0.32);
        ItemDisplay head = s.lit(s.item(headItem, item(rot(face, headStart), face, 0.001f)));
        s.tfAt(12, head, item(rot(face, headStart), face, hs), 2);

        s.sound("block.piston.extend", 0.7f, 0.5f);
        s.every(2, 12, 3, t -> s.groundCrack(s.loc(0, 0.1, 0), 15, 0.7));
        s.at(17, () -> s.sound(s.loc(0, 3, 0), "block.tripwire.click_on", 0.9f, 0.7f));
        s.at(18, () -> s.sound(s.loc(0, 2, 0), "item.trident.throw", 0.6f, 1.4f));
        s.at(22, () -> {
            Location neck = s.lloc(0, 0.8, 0.3);
            s.sound(neck, "block.anvil.land", 0.7f, 1.3f);
            s.sound(neck, "entity.player.attack.sweep", 1.0f, 0.6f);
            s.sound(neck, "entity.player.attack.crit", 1.0f, 0.8f);
            s.sound(neck, "item.axe.strip", 1.0f, 0.7f);
            s.particle(P.SWEEP_ATTACK, neck, 1, 0, 0);
            s.particle(P.CRIT, neck, 15, 0.3, 0.3);
            s.crack(neck, Material.REDSTONE_BLOCK, 25, 0.2);
            s.dust(neck, Color.fromRGB(120, 0, 0), 1.0f, 10, 0.2);
        });

        // Kafa yuvarlanir (+Z = olduren oyuncuya dogru)
        final double radius = 0.28;
        for (int t = 22; t <= 38; t += 2) {
            double u = (t - 22) / 16.0;
            double dist = 1.3 * easeOut(u);
            double y = t <= 26 ? lerp(0.80, radius, easeIn((t - 22) / 4.0)) : radius;
            double z = 0.32 + dist;
            double rollAngle = dist / radius;
            Quaternionf q = mul(face, qX(rollAngle));
            s.tfAt(t, head, item(rot(face, v(0, y, z)), q, hs), 2);
            if (t >= 26 && t <= 36) {
                final Location bl = s.lloc(0, 0.05, z);
                s.at(t, () -> s.dust(bl, Color.fromRGB(110, 0, 0), 0.8f, 2, 0.06));
            }
        }
        s.at(26, () -> s.sound(s.lloc(0, 0.3, 0.8), "entity.player.hurt", 0.6f, 0.6f));
        double endDist = 1.3;
        s.tfAt(42, head, item(rot(face, v(0, radius, 0.32 + endDist)), mul(face, qX(endDist / radius)), 0.001f), 6);
        s.at(40, () -> s.groundCrack(s.loc(0, 0.1, 0), 20, 0.7));
    }

    private static Transformation tf(Quaternionf face, Part p, double dy) {
        Vector3f local = new Vector3f(p.c).add(0, (float) dy, 0);
        Quaternionf q = p.rollZ == 0 ? new Quaternionf(face) : mul(face, qZ(p.rollZ));
        return cube(rot(face, local), q, p.size);
    }

    /** Gercekcilik katmani: ek 3D detaylar, enkaz, sok dalgalari ve parlamalar. */
    private void extras(FxSession s) {

        Material g = s.groundMaterial(Material.DIRT);
        FxKit.debris(s, v(0, 0.2, 0), new Material[]{g, Material.DARK_OAK_PLANKS, g}, 8, 0.06, 0.18, 0.3, 0.5, 0.1f, 0.22f, 3, false);
        s.at(3, () -> FxKit.dustRing(s, s.loc(0, 0.15, 0), 18, 0.8, 0.12));
        // Bicak parlamasi
        s.every(12, 16, 2, t -> s.particle(P.WAX_OFF, s.lloc(s.rd(-0.4, 0.4), 2.85, 0.2), 2, 0.05, 0));
        s.at(16, () -> s.particle(P.ELECTRIC_SPARK, s.lloc(0, 2.85, 0.2), 8, 0.4, 0.05, 0.05, 0.05));
        // Kesilme: kan puskurmesi ve platformda leke
        s.at(22, () -> {
            for (int i = 0; i < 18; i++) {
                Vector3f d = s.local(s.rd(-0.6, 0.6), s.rd(0.1, 0.8), s.rd(0.2, 1.0));
                s.dir(P.DAMAGE_INDICATOR, s.lloc(0, 0.8, 0.3), d, 0.3);
            }
            s.dust(s.lloc(0, 0.8, 0.35), Color.fromRGB(140, 0, 0), 1.3f, 22, 0.25);
        });
        final Quaternionf face = s.face();
        BlockDisplay stain = s.lit(s.block(Material.REDSTONE_BLOCK, cube(rot(face, v(0, 0.157, 0.32)), face, 0.001f)));
        s.tfAt(23, stain, cube(rot(face, v(0, 0.157, 0.32)), mul(face, qY(0.4)), v(0.55, 0.012, 0.4)), 4);
        s.tfAt(40, stain, cube(rot(face, v(0, 0.157 - 3.7, 0.32)), face, 0.001f), 4);
    }
}
