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
/** Dev bir kum saati belirir, kum akar, zaman dolunca doner ve paramparca olur. */
public final class HourglassEffect extends KillEffect {

    public HourglassEffect() {
        super("kum_saati", "&e&lKum Saati", Material.CLOCK, true,
                "Dev bir kum saati belirir, kum akar,",
                "zaman dolunca döner ve parçalanır.");
    }

    private static final double Y = 1.1;
    private static final int FLIP = 30, BREAK = 37;

    /** Kum saatinin donusu: FLIP'te 180 derece takla. */
    private static Quaternionf turn(FxSession s, int t) {
        double k = smooth(phase(t, FLIP, FLIP + 6));
        return mul(s.face(), qZ(k * Math.PI));
    }

    @Override
    public void play(FxSession s) {
        final Vector3f c = v(0, Y, 0);
        // Cerceve: ust ve alt tabla + dort direk
        Object[][] frame = {
                {Material.DARK_OAK_PLANKS, v(0, 0.86, 0), v(0.95, 0.1, 0.95)},
                {Material.DARK_OAK_PLANKS, v(0, -0.86, 0), v(0.95, 0.1, 0.95)},
                {Material.GOLD_BLOCK, v(0, 0.93, 0), v(0.7, 0.04, 0.7)},
                {Material.GOLD_BLOCK, v(0, -0.93, 0), v(0.7, 0.04, 0.7)},
                {Material.STRIPPED_DARK_OAK_LOG, v(0.4, 0, 0.4), v(0.07, 1.65, 0.07)},
                {Material.STRIPPED_DARK_OAK_LOG, v(-0.4, 0, 0.4), v(0.07, 1.65, 0.07)},
                {Material.STRIPPED_DARK_OAK_LOG, v(0.4, 0, -0.4), v(0.07, 1.65, 0.07)},
                {Material.STRIPPED_DARK_OAK_LOG, v(-0.4, 0, -0.4), v(0.07, 1.65, 0.07)}};
        for (Object[] p : frame) {
            final Material m = (Material) p[0];
            final Vector3f off = (Vector3f) p[1], size = (Vector3f) p[2];
            block(s, m, LIT, 3, BREAK, 1, 3, t -> attach(c, turn(s, t), off, q(), size));
        }
        // Cam haznelar (ust ve alt, daralan katlar)
        double[][] glass = {{0.62, 0.62}, {0.46, 0.36}, {0.22, 0.12}, {0.22, -0.12}, {0.46, -0.36}, {0.62, -0.62}};
        for (double[] g : glass) {
            final float w = (float) g[0];
            final Vector3f off = v(0, g[1], 0);
            block(s, Material.WHITE_STAINED_GLASS, BRIGHT, 4, BREAK, 1, 3, t -> attach(c, turn(s, t), off, q(), v(w, 0.25, w)));
        }
        // Kum: ustte azalir, altta artar (FLIP'e kadar), sonra donmus saatle birlikte doner
        block(s, Material.YELLOW_CONCRETE, LIT, 5, BREAK, 1, 2, t -> {
            double k = smooth(phase(t, 7, FLIP - 2));
            double h = 0.45 * (1 - k) + 0.01;
            return attach(c, turn(s, t), v(0, 0.2 + h / 2, 0), q(), v(0.42 * (1 - k * 0.5), h, 0.42 * (1 - k * 0.5)));
        });
        block(s, Material.YELLOW_CONCRETE, LIT, 7, BREAK, 1, 2, t -> {
            double k = smooth(phase(t, 7, FLIP - 2));
            double h = 0.45 * k + 0.01;
            return attach(c, turn(s, t), v(0, -0.78 + h / 2, 0), q(), v(0.5, h, 0.5));
        });
        // Akan kum ipi
        block(s, Material.YELLOW_CONCRETE, BRIGHT, 7, FLIP - 2, 1, 2, t -> attach(c, turn(s, t), v(0, -0.25, 0), q(), v(0.035, 0.85, 0.035)));
        // Kirilma: cam parcalari ve kum
        FxKit.debris(s, v(0, Y, 0), new Material[]{Material.WHITE_STAINED_GLASS, Material.YELLOW_CONCRETE, Material.DARK_OAK_PLANKS, Material.GOLD_BLOCK},
                12, 0.08, 0.22, 0.2, 0.45, 0.08f, 0.18f, BREAK, false);
        FxKit.shockRing(s, Y, 0.3, 2.4, BREAK, 6, Material.YELLOW_STAINED_GLASS, 14, 0.1f, true);
        // Parcaciklar
        s.every(8, FLIP - 2, 2, t -> s.particle(P.BLOCK, s.loc(0, Y - 0.6, 0), 2, 0.05, 0.02, 0.05, 0.02, Material.YELLOW_CONCRETE));
        s.every(4, BREAK, 3, t -> s.particle(P.WAX_ON, s.loc(s.rd(-0.6, 0.6), Y + s.rd(-0.9, 0.9), s.rd(-0.6, 0.6)), 1, 0.05, 0.01));
        s.at(BREAK, () -> {
            s.particle(P.FLASH, s.loc(0, Y, 0), 1, 0, 0);
            s.particle(P.BLOCK, s.loc(0, Y, 0), 40, 0.4, 0.4, 0.4, 0.15, Material.YELLOW_CONCRETE);
            s.particle(P.END_ROD, s.loc(0, Y, 0), 16, 0.2, 0.2);
        });
        // Ses tasarimi: saat tik-taklari hizlanir
        s.sound("block.amethyst_block.chime", 0.6f, 0.5f);
        int[] ticks = {6, 10, 14, 17, 20, 23, 25, 27, 29};
        for (int i = 0; i < ticks.length; i++) {
            final float pitch = 0.9f + i * 0.08f;
            s.at(ticks[i], () -> s.sound("block.note_block.hat", 0.9f, pitch));
        }
        s.at(8, () -> s.longSound("block.sand.fall", 0.6f, 0.8f));
        s.at(FLIP, () -> {
            s.sound("block.bell.use", 1.0f, 0.7f);
            s.sound("block.bell.resonate", 0.5f, 0.7f);
        });
        s.at(BREAK, () -> {
            s.sound("block.glass.break", 1.0f, 0.7f);
            s.sound("block.glass.break", 0.8f, 1.1f);
            s.sound("block.sand.break", 0.9f, 0.6f);
        });
    }
}
