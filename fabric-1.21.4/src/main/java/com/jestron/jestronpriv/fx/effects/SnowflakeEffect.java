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
/** Havada dev bir buz kristali kar tanesi buyur, doner, sonra cinlayarak paramparca olur. */
public final class SnowflakeEffect extends KillEffect {

    public SnowflakeEffect() {
        super("kar_tanesi", "&b&lDev Kar Tanesi", Material.SNOW_BLOCK, true,
                "Havada dev bir kar tanesi büyür,",
                "döner ve çınlayarak parçalanır.");
    }

    private static final double Y = 1.6;
    private static final int SHATTER = 33;

    @Override
    public void play(FxSession s) {
        final Vector3f c = v(0, Y, 0);
        // Kar tanesi oyuncuya bakar ve kendi ekseninde yavasca doner
        java.util.function.IntFunction<Quaternionf> orient = t -> mul(s.face(), qZ(t * 0.06));
        for (int arm = 0; arm < 6; arm++) {
            final double a = arm * Math.PI / 3;
            final Material m = arm % 2 == 0 ? Material.PACKED_ICE : Material.BLUE_ICE;
            // Ana kol
            block(s, m, BRIGHT, 3, SHATTER, 1, 1, t -> {
                double k = easeOutBack(phase(t, 3, 12));
                double len = 1.25 * k;
                return attach(c, orient.apply(t), v(Math.cos(a) * len / 2, Math.sin(a) * len / 2, 0), qZ(a), v(len, 0.09, 0.07));
            });
            // Iki cift yan dal
            for (int br = 0; br < 2; br++) {
                final double at = br == 0 ? 0.45 : 0.85;
                final double bl = br == 0 ? 0.36 : 0.24;
                for (int sd = -1; sd <= 1; sd += 2) {
                    final double ba = a + sd * Math.PI / 4;
                    final int st = 7 + br * 2;
                    block(s, Material.ICE, BRIGHT, st, SHATTER, 1, 1, t -> {
                        double k = easeOutBack(phase(t, st, st + 6));
                        double l = bl * k;
                        Vector3f base = v(Math.cos(a) * at, Math.sin(a) * at, 0);
                        Vector3f mid = new Vector3f(base).add((float) (Math.cos(ba) * l / 2), (float) (Math.sin(ba) * l / 2), 0);
                        return attach(c, orient.apply(t), mid, qZ(ba), v(l, 0.06, 0.05));
                    });
                }
            }
            // Kol ucu kristali
            block(s, Material.LIGHT_BLUE_STAINED_GLASS, BRIGHT, 10, SHATTER, 1, 2, t ->
                    attach(c, orient.apply(t), v(Math.cos(a) * 1.3, Math.sin(a) * 1.3, 0), qZ(a + Math.PI / 4), v(0.14, 0.14, 0.08)));
        }
        // Merkez altigeni
        block(s, Material.WHITE_STAINED_GLASS, BRIGHT, 3, SHATTER, 1, 2, t -> attach(c, orient.apply(t), v(0, 0, 0), qZ(Math.PI / 4), v(0.3, 0.3, 0.1)));
        FxKit.glow(s, Material.LIGHT_BLUE_STAINED_GLASS, c, 0.7f, 5, SHATTER, 0.1);
        // Yerde kiragi halkasi
        block(s, Material.SNOW_BLOCK, LIT, 6, 40, 1, 4, t -> {
            double k = smooth(phase(t, 6, 14)) * (t > 36 ? 1 - phase(t, 36, 40) : 1);
            return Pose.of(v(0, s.groundDy(0, 0) + 0.02, 0), qY(0.2), v(2.6 * k + 0.01, 0.03, 2.6 * k + 0.01));
        });
        // Kirilma
        FxKit.debris(s, c, new Material[]{Material.PACKED_ICE, Material.BLUE_ICE, Material.ICE, Material.LIGHT_BLUE_STAINED_GLASS},
                14, 0.06, 0.2, 0.05, 0.25, 0.08f, 0.18f, SHATTER, true);
        FxKit.shockRing(s, Y, 0.3, 2.8, SHATTER, 6, Material.WHITE_STAINED_GLASS, 16, 0.1f, true);
        // Parcaciklar
        s.every(3, SHATTER, 2, t -> {
            s.particle(P.SNOWFLAKE, s.loc(s.rd(-1.4, 1.4), Y + s.rd(-1.2, 1.2), s.rd(-0.6, 0.6)), 2, 0.1, 0.01);
            s.particle(P.END_ROD, s.loc(0, Y, 0), 1, 0.6, 0.01);
        });
        s.at(SHATTER, () -> {
            s.particle(P.FLASH, s.loc(0, Y, 0), 1, 0, 0);
            s.particle(P.SNOWFLAKE, s.loc(0, Y, 0), 40, 0.6, 0.2);
            s.particle(P.ITEM_SNOWBALL, s.loc(0, Y, 0), 24, 0.5, 0.15);
        });
        s.every(SHATTER, 42, 2, t -> s.particle(P.SNOWFLAKE, s.loc(s.rd(-1.5, 1.5), Y + 0.5, s.rd(-1.5, 1.5)), 3, 0.2, 0.01));
        // Ses tasarimi
        s.sound("block.powder_snow.place", 1.0f, 0.7f);
        s.at(4, () -> s.sound("block.amethyst_block.chime", 0.8f, 1.5f));
        s.at(8, () -> s.sound("block.amethyst_block.chime", 0.8f, 1.8f));
        s.at(12, () -> {
            s.sound("block.amethyst_block.chime", 1.0f, 1.2f);
            s.sound("block.note_block.chime", 0.6f, 1.6f);
        });
        s.every(16, SHATTER - 2, 5, t -> s.sound("block.amethyst_block.chime", 0.4f, s.rp(1.6f, 2.0f)));
        s.at(SHATTER, () -> {
            s.sound("block.glass.break", 1.0f, 1.2f);
            s.sound("block.amethyst_cluster.break", 1.0f, 0.9f);
            s.sound("entity.player.hurt_freeze", 0.8f, 0.8f);
        });
        s.at(SHATTER + 3, () -> s.sound("block.powder_snow.place", 0.8f, 1.2f));
    }
}
