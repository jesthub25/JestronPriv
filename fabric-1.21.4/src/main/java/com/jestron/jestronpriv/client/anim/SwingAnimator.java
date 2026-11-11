package com.jestron.jestronpriv.client.anim;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

/**
 * Ana eldeki esyanin vurus (sallama) animasyonu.
 * Stil matematigi SwingAnimation (anomalith, CC0-1.0) modundan uyarlanmistir.
 */
public final class SwingAnimator {

    private SwingAnimator() {
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    /** Son karenin ara tick orani (renderArmWithItem'dan). */
    public static float partial;
    /** Yerel oyuncunun gecerli vurus suresi (tick), getCurrentSwingDuration'dan. */
    private static int duration = 6;
    // esya degisimi: sadece gercek degisimde "inis" animasyonu (saldiri bekleme dalisi degil)
    private static int lastSlot = -1;
    private static net.minecraft.world.item.Item lastItem;
    private static long swapNanos;

    /**
     * Vurus ilerlemesi, tiklanan karede baslar. Vanilla tikladiktan sonraki ilk tick'te ilerlemeyi 0 tutar (o arada
     * saldiri bekleme dalisi oynar, "once klasik sonra stil" gorunur); burada vurus aninda ilerler ve sure ayni kalir.
     */
    public static float progress(float vanilla) {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null) return vanilla;
        if (!p.swinging) return 0f;
        if (p.swingingArm != net.minecraft.world.InteractionHand.MAIN_HAND) return vanilla;
        int d = Math.max(1, duration);
        float pt = Float.isFinite(partial) ? Math.max(0f, Math.min(1f, partial)) : 0f;
        float v = (p.swingTime + 1 + pt) / d;
        return Float.isFinite(v) ? Math.max(0f, Math.min(1f, v)) : vanilla;
    }

    /** Ana eldeki esya ya da secili yuva degisti mi (inis animasyonu icin). */
    private static boolean recentlySwapped() {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null) return true;
        int slot = p.getInventory().selected;
        net.minecraft.world.item.Item it = p.getMainHandItem().getItem();
        long now = System.nanoTime();
        if (slot != lastSlot || it != lastItem) {
            lastSlot = slot;
            lastItem = it;
            swapNanos = now;
        }
        return (now - swapNanos) / 1e9 < 0.6;
    }

    /** Ozel animasyon bu kol icin uygulanacak mi (sadece yerel oyuncunun ana kolu). */
    public static boolean shouldApply(HumanoidArm arm, float swingProgress) {
        ModConfig c = cfg();
        if (c == null || !c.swingEnabled) return false;
        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null || arm != p.getMainArm()) return false;
        return !(c.swingOnlyWhileSwinging && swingProgress <= 0f);
    }

    /** Vanilla swingArm yerine: konum + ozel stil donusumu. */
    public static void apply(PoseStack pose, HumanoidArm arm, float swingProgress, float equipProgress) {
        ModConfig c = cfg();
        if (c == null) return;
        // esya degisince inis (istege bagli); saldiri bekleme dalisi istenirse (klasik his)
        if (equipProgress > 0f) {
            boolean swap = recentlySwapped();
            if ((swap && c.swingEquipDip) || (!swap && c.swingCooldownDip)) pose.translate(0.0F, equipProgress * -0.6F, 0.0F);
        } else {
            recentlySwapped();
        }
        transform(pose, arm, swingProgress, c.swingStyle(), c.swingStrength);
    }

    public static void transform(PoseStack m, HumanoidArm arm, float p, SwingStyle style, float strength) {
        int i = arm == HumanoidArm.RIGHT ? 1 : -1;
        float sin1 = Mth.sin(p * p * (float) Math.PI);
        float sin2 = Mth.sin(Mth.sqrt(p) * (float) Math.PI);
        float sinSmooth = (float) (Math.sin(p * Math.PI) * 0.5);
        // Ilk karede sert sicramayan yumusak can egrisi (sqrt yerine p^0.7)
        float sinSoft = Mth.sin((float) Math.pow(p, 0.7) * (float) Math.PI);
        float s = strength;
        switch (style) {
            case TWIST -> {
                m.translate(i * 0.56F, -0.36F, -0.72F);
                m.mulPose(Axis.YP.rotationDegrees(80 * i));
                m.mulPose(Axis.XP.rotationDegrees(sin2 * -90.0F * s));
                m.mulPose(Axis.ZP.rotationDegrees((sin1 - sin2) * 60.0F * i * s));
                m.mulPose(Axis.XP.rotationDegrees(-30.0F));
                m.translate(0.0F, -0.1F, 0.05F);
            }
            case SWIPE -> {
                m.translate(0.56F * i, -0.32F, -0.72F);
                m.mulPose(Axis.YP.rotationDegrees(70 * i));
                m.mulPose(Axis.ZP.rotationDegrees(-20 * i));
                m.mulPose(Axis.YP.rotationDegrees(sin2 * sin1 * -5.0F * s));
                m.mulPose(Axis.XP.rotationDegrees(sin2 * sin1 * -120.0F * s));
                m.mulPose(Axis.XP.rotationDegrees(-70.0F));
            }
            case DEFAULT -> {
                m.translate(i * 0.56F, -0.52F - sinSoft * 0.5F * s, -0.72F);
            }
            case DOWN -> {
                m.translate(i * 0.56F, -0.32F, -0.72F);
                m.mulPose(Axis.YP.rotationDegrees(76 * i));
                m.mulPose(Axis.YP.rotationDegrees(sin2 * -5.0F * s));
                m.mulPose(Axis.XN.rotationDegrees(sin2 * -100.0F * s));
                m.mulPose(Axis.XP.rotationDegrees(sin2 * -155.0F * s));
                m.mulPose(Axis.XP.rotationDegrees(-100.0F));
            }
            case SMOOTH -> {
                m.translate(i * 0.56F, -0.42F, -0.72F);
                m.mulPose(Axis.YP.rotationDegrees(i * (45.0F + sin1 * -20.0F * s)));
                m.mulPose(Axis.ZP.rotationDegrees(i * sin2 * -20.0F * s));
                m.mulPose(Axis.XP.rotationDegrees(sin2 * -80.0F * s));
                m.mulPose(Axis.YP.rotationDegrees(i * -45.0F));
                m.translate(0.0F, -0.1F, 0.0F);
            }
            case SMOOTH_2 -> {
                m.translate(i * 0.56F, -0.42F, -0.72F);
                m.mulPose(Axis.XP.rotationDegrees(sin2 * -80.0F * s));
                m.translate(0.0F, -0.1F, 0.0F);
            }
            case POWER -> {
                m.translate(i * 0.56F, -0.32F, -0.72F);
                m.translate(-sinSmooth * sinSmooth * sin1 * i * s, 0.0F, 0.0F);
                m.mulPose(Axis.YP.rotationDegrees(61 * i));
                m.mulPose(Axis.ZP.rotationDegrees(sin2 * s));
                m.mulPose(Axis.YP.rotationDegrees(sin2 * sin1 * -5.0F * s));
                m.mulPose(Axis.XP.rotationDegrees(sin2 * sin1 * -30.0F * s));
                m.mulPose(Axis.XP.rotationDegrees(-60.0F));
                m.mulPose(Axis.XP.rotationDegrees(sinSmooth * -60.0F * s));
            }
            case SLASH -> {
                m.translate(i * 0.56F, -0.40F, -0.72F);
                m.mulPose(Axis.YP.rotationDegrees(i * 45.0F));
                m.mulPose(Axis.ZP.rotationDegrees(i * sin2 * 70.0F * s));
                m.mulPose(Axis.XP.rotationDegrees(sin2 * -60.0F * s));
                m.mulPose(Axis.YP.rotationDegrees(i * (-45.0F - sin1 * 30.0F * s)));
            }
            case BACKHAND -> {
                m.translate(i * 0.56F - i * sinSoft * 0.35F * s, -0.40F, -0.72F);
                m.mulPose(Axis.YP.rotationDegrees(i * (45.0F - sinSoft * 110.0F * s)));
                m.mulPose(Axis.XP.rotationDegrees(sin1 * -30.0F * s));
                m.mulPose(Axis.YP.rotationDegrees(i * -45.0F));
            }
            case OVERHEAD -> {
                float lift = Mth.sin(Math.min(1.0F, p * 2.2F) * (float) Math.PI);
                m.translate(i * 0.56F, -0.45F + lift * 0.14F * s, -0.72F + sin2 * 0.1F * s);
                m.mulPose(Axis.XP.rotationDegrees((lift * 35.0F - sin1 * 110.0F) * s));
                m.mulPose(Axis.ZP.rotationDegrees(i * sin2 * -12.0F * s));
            }
            case STAB -> {
                float thrust = Mth.sin(p * (float) Math.PI);
                m.translate(i * (0.56F - thrust * 0.12F * s), -0.42F + thrust * 0.06F, -0.72F - thrust * 0.42F * s);
                m.mulPose(Axis.YP.rotationDegrees(i * thrust * 12.0F * s));
                m.mulPose(Axis.XP.rotationDegrees(thrust * -38.0F * s));
            }
            case SPIN -> {
                float turn = (float) (0.5 - 0.5 * Math.cos(p * Math.PI)) * 360.0F;
                m.translate(i * 0.56F, -0.42F - sin2 * 0.08F * s, -0.72F);
                m.mulPose(Axis.YP.rotationDegrees(i * 45.0F));
                m.mulPose(Axis.XP.rotationDegrees(sin2 * -30.0F * s));
                m.translate(0.0F, 0.2F, 0.0F);
                m.mulPose(Axis.ZP.rotationDegrees(-i * turn));
                m.translate(0.0F, -0.2F, 0.0F);
                m.mulPose(Axis.YP.rotationDegrees(i * -45.0F));
            }
            case BLOCKHIT -> {
                float f = Mth.sin(p * p * (float) Math.PI);
                m.translate(i * 0.56F, -0.52F, -0.72F);
                m.mulPose(Axis.YP.rotationDegrees(i * (45.0F + f * -20.0F * s)));
                m.mulPose(Axis.ZP.rotationDegrees(i * sin2 * -20.0F * s));
                m.mulPose(Axis.XP.rotationDegrees(sin2 * -80.0F * s));
                m.mulPose(Axis.YP.rotationDegrees(i * -45.0F));
                m.translate(i * -0.14142136F, 0.08F, 0.14142136F);
                m.mulPose(Axis.XP.rotationDegrees(-102.25F));
                m.mulPose(Axis.YP.rotationDegrees(i * 13.365F));
                m.mulPose(Axis.ZP.rotationDegrees(i * 78.05F));
            }
            case KNIGHT, KATANA, XSLASH, LUNGE, SMASH, FLIP, TWIRL, REVERSE, KARAMBIT, DRAGON, CRESCENT, UPPERCUT, CROSS, DOUBLESTAB,
                 EXECUTION, RONIN, PHANTOM, WHIRLWIND, TORNADO, MOONLIGHT, ZIGZAG, HAMMER, RIPOSTE, SICKLE, AXE, PUNCH, TRIPLESTAB,
                 SCYTHE, BOOMERANG, WINDMILL, VORTEX, CLEAVE, SWEEP, RISINGDRAGON, FLOURISH, GUARD -> keyed(m, i, p, style, s);
            case FEAST -> {
                m.translate(i * 0.56F, -0.32F, -0.72F);
                m.mulPose(Axis.YP.rotationDegrees(30 * i));
                m.mulPose(Axis.YP.rotationDegrees(sin2 * 75.0F * i * s));
                m.mulPose(Axis.XP.rotationDegrees(sin2 * -45.0F * s));
                m.mulPose(Axis.YP.rotationDegrees(30 * i));
                m.mulPose(Axis.XP.rotationDegrees(-80.0F));
                m.mulPose(Axis.YP.rotationDegrees(35 * i));
            }
        }
    }

    // ------------------------------------------------------------------ anahtar kare stilleri
    //
    // Her satir: {zaman, x, y, z, egim(X), yon(Y), yatis(Z), donus}. Konum blok, acilar derece; sag el icin
    // yazilir, sol elde x/Y/Z aynalanir. Ilk ve son kare dinlenme pozudur (vanilla ile ayni), boylece
    // stil degisince ya da vurus bitince el hic sicramaz. Kareler arasi monoton kubik Hermite: ucta ve
    // tepe noktalarda hiz sifira iner (dogal yavaslama), aradaki gecisler kirilmasiz (C1) akar.
    // "donus" kanali esyanin elde attigi tam turlardir; guc ayarindan etkilenmez (360 hep 360 kalir).

    private static final int CH = 7;

    /** Dinlenme pozu: vanilla esya konumu. */
    private static final float REST_X = 0.56F, REST_Y = -0.52F, REST_Z = -0.72F;

    /** Esyanin elde tutuldugu nokta (kabza) ve kilic govdesinin yonu; handheld modelinden hesaplandi. */
    private static final float GRIP_X = 0.07F, GRIP_Y = -0.03F, GRIP_Z = -0.03F;
    private static final float BLADE_Y = 0.94F, BLADE_Z = 0.34F;

    private static final float[][] KNIGHT_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.20F, -0.02F, 0.20F, 0.00F, 8, 0, -14, 0},
            {0.40F, -0.26F, -0.06F, -0.10F, -40, 8, 70, 0},
            {0.55F, -0.32F, -0.14F, -0.08F, -48, 10, 92, 0},
            {1.00F, 0, 0, 0, 0, 0, 0, 0}};
    private static final float[][] KATANA_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.16F, 0.12F, 0.12F, 0.04F, -78, -55, 84, 0},
            {0.38F, -0.30F, 0.26F, -0.12F, -88, 70, 90, 0},
            {0.54F, -0.33F, 0.24F, -0.08F, -86, 78, 92, 0},
            {0.68F, -0.22F, 0.04F, -0.05F, -55, 55, 58, 0},
            {1.00F, 0, 0, 0, 0, 0, 0, 0}};
    private static final float[][] XSLASH_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.10F, 0.00F, 0.16F, 0.00F, 6, 0, -14, 0},
            {0.30F, -0.26F, -0.06F, -0.10F, -40, 8, 80, 0},
            {0.44F, -0.24F, 0.16F, -0.04F, 6, -6, 34, 0},
            {0.64F, 0.00F, 0.06F, -0.12F, -40, -8, -70, 0},
            {1.00F, 0, 0, 0, 0, 0, 0, 0}};
    private static final float[][] LUNGE_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.22F, 0.05F, 0.02F, 0.12F, -72, 4, 6, 0},
            {0.42F, -0.10F, 0.06F, -0.46F, -86, 6, 24, 0},
            {0.56F, -0.09F, 0.05F, -0.40F, -84, 5, 30, 0},
            {1.00F, 0, 0, 0, 0, 0, 0, 0}};
    private static final float[][] SMASH_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.26F, -0.06F, 0.32F, -0.04F, 2, 0, -6, 0},
            {0.42F, -0.08F, -0.10F, -0.18F, -100, 0, 8, 0},
            {0.48F, -0.07F, -0.05F, -0.17F, -92, 0, -5, 0},
            {0.54F, -0.08F, -0.10F, -0.18F, -98, 0, 4, 0},
            {0.60F, -0.07F, -0.06F, -0.17F, -94, 0, -2.5F, 0},
            {0.66F, -0.08F, -0.08F, -0.17F, -96, 0, 1, 0},
            {1.00F, 0, 0, 0, 0, 0, 0, 0}};
    private static final float[][] FLIP_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.16F, -0.12F, 0.22F, -0.04F, 4, 0, 6, 0},
            {0.62F, -0.16F, 0.26F, -0.10F, -8, 0, 8, -360},
            {0.76F, -0.12F, -0.02F, -0.16F, -45, 4, 12, -360},
            {1.00F, 0, 0, 0, 0, 0, 0, -360}};
    private static final float[][] TWIRL_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.14F, -0.30F, 0.34F, -0.12F, -10, 8, 0, 0},
            {0.62F, -0.32F, 0.36F, -0.14F, -12, 8, 0, 720},
            {0.78F, -0.18F, -0.02F, -0.16F, -55, 6, 40, 720},
            {1.00F, 0, 0, 0, 0, 0, 0, 720}};
    private static final float[][] REVERSE_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.22F, -0.08F, 0.40F, -0.04F, 6, 0, 8, 180},
            {0.42F, -0.14F, 0.16F, -0.26F, -20, 0, 14, 180},
            {0.58F, -0.13F, 0.20F, -0.22F, -16, 0, 12, 180},
            {0.86F, -0.02F, 0.12F, -0.04F, 0, 0, 2, 360},
            {1.00F, 0, 0, 0, 0, 0, 0, 360}};
    private static final float[][] KARAMBIT_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.14F, -0.16F, 0.14F, -0.02F, -6, 28, 16, 0},
            {0.80F, -0.17F, 0.15F, -0.03F, -8, 32, 18, 720},
            {1.00F, 0, 0, 0, 0, 0, 0, 720}};
    private static final float[][] DRAGON_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.18F, -0.02F, 0.18F, 0.02F, 10, -10, -20, 0},
            {0.46F, -0.22F, 0.24F, -0.08F, -6, 6, 20, 360},
            {0.70F, -0.14F, -0.10F, -0.18F, -60, 10, 40, 360},
            {1.00F, 0, 0, 0, 0, 0, 0, 360}};

    private static final float[][] CRESCENT_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.18F, 0.06F, 0.24F, 0.02F, 10, -12, -30, 0},
            {0.36F, -0.12F, 0.10F, -0.10F, -30, 4, 40, 0},
            {0.50F, -0.34F, -0.12F, -0.10F, -55, 14, 95, 0},
            {0.62F, -0.36F, -0.16F, -0.08F, -58, 14, 100, 0},
            {1.00F, 0, 0, 0, 0, 0, 0, 0}};
    private static final float[][] UPPERCUT_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.20F, -0.14F, -0.05F, -0.02F, 20, 6, 58, 0},
            {0.42F, 0.02F, 0.20F, -0.10F, -30, -6, -20, 0},
            {0.55F, 0.06F, 0.28F, -0.08F, -40, -8, -30, 0},
            {1.00F, 0, 0, 0, 0, 0, 0, 0}};
    private static final float[][] CROSS_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.12F, -0.06F, 0.22F, -0.02F, 6, 0, -6, 0},
            {0.30F, -0.08F, -0.10F, -0.16F, -80, 0, 6, 0},
            {0.42F, 0.06F, 0.02F, -0.10F, -30, 30, -40, 0},
            {0.62F, -0.30F, 0.00F, -0.10F, -30, -24, 80, 0},
            {1.00F, 0, 0, 0, 0, 0, 0, 0}};
    private static final float[][] DOUBLESTAB_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.12F, 0.04F, 0.02F, 0.08F, -70, 4, 6, 0},
            {0.27F, -0.08F, 0.05F, -0.34F, -86, 6, 20, 0},
            {0.40F, 0.02F, 0.03F, 0.02F, -74, 4, 10, 0},
            {0.55F, -0.10F, 0.06F, -0.38F, -88, 6, 26, 0},
            {0.66F, -0.08F, 0.05F, -0.34F, -86, 6, 24, 0},
            {1.00F, 0, 0, 0, 0, 0, 0, 0}};
    private static final float[][] EXECUTION_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.20F, -0.04F, 0.31F, 0.02F, 10, 0, -8, 0},
            {0.31F, -0.05F, 0.33F, 0.02F, 12, 0, -8, 0},
            {0.48F, -0.08F, -0.10F, -0.20F, -105, 0, 6, 0},
            {0.54F, -0.08F, -0.05F, -0.19F, -96, 0, -4, 0},
            {0.60F, -0.08F, -0.09F, -0.20F, -101, 0, 3, 0},
            {0.66F, -0.08F, -0.07F, -0.19F, -99, 0, 0, 0},
            {1.00F, 0, 0, 0, 0, 0, 0, 0}};
    private static final float[][] RONIN_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.16F, -0.10F, -0.20F, 0.04F, 36, -18, 56, 0},
            {0.36F, 0.10F, 0.22F, -0.10F, -70, -30, -40, 0},
            {0.50F, 0.12F, 0.26F, -0.08F, -75, -32, -45, 0},
            {0.66F, 0.02F, 0.06F, -0.04F, -30, -10, -10, 0},
            {1.00F, 0, 0, 0, 0, 0, 0, 0}};
    private static final float[][] PHANTOM_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.20F, 0.08F, 0.12F, 0.04F, -6, -20, -16, 0},
            {0.38F, -0.34F, 0.00F, -0.12F, -50, 30, 85, 0},
            {0.46F, -0.38F, -0.02F, -0.12F, -52, 34, 92, 0},
            {0.60F, -0.26F, 0.00F, -0.08F, -40, 20, 70, 0},
            {1.00F, 0, 0, 0, 0, 0, 0, 0}};
    private static final float[][] WHIRLWIND_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.14F, 0.04F, 0.16F, 0.02F, 0, -12, -12, 0},
            {0.56F, -0.30F, 0.08F, -0.12F, -30, 30, 60, 360},
            {0.70F, -0.28F, 0.00F, -0.10F, -30, 28, 55, 360},
            {1.00F, 0, 0, 0, 0, 0, 0, 360}};
    private static final float[][] TORNADO_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.10F, 0.02F, 0.04F, 0.08F, -60, 4, 6, 0},
            {0.86F, -0.10F, 0.06F, -0.36F, -84, 6, 20, 1080},
            {0.93F, -0.05F, 0.03F, -0.18F, -50, 3, 10, 1080},
            {1.00F, 0, 0, 0, 0, 0, 0, 1080}};
    private static final float[][] MOONLIGHT_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.16F, -0.10F, 0.24F, -0.02F, 10, 0, 0, 0},
            {0.58F, -0.12F, 0.28F, -0.08F, 0, 0, 6, 360},
            {0.74F, -0.20F, -0.06F, -0.16F, -60, 8, 40, 360},
            {1.00F, 0, 0, 0, 0, 0, 0, 360}};

    private static final float[][] ZIGZAG_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.12F, 0.02F, 0.16F, 0, 6, -6, -20, 0},
            {0.28F, -0.28F, -0.04F, -0.10F, -36, 10, 78, 0},
            {0.40F, -0.22F, 0.14F, -0.04F, 4, -4, 30, 0},
            {0.54F, 0.02F, -0.04F, -0.10F, -36, -10, -60, 0},
            {0.68F, -0.26F, 0, -0.10F, -34, 10, 70, 0},
            {1.00F, 0, 0, 0, 0, 0, 0, 0}};
    private static final float[][] HAMMER_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.28F, 0.02F, 0.36F, 0.04F, 18, -14, -16, 0},
            {0.38F, 0.02F, 0.38F, 0.04F, 20, -14, -16, 0},
            {0.55F, -0.08F, -0.14F, -0.20F, -110, 4, 8, 0},
            {0.61F, -0.08F, -0.06F, -0.19F, -98, 4, -3, 0},
            {0.67F, -0.08F, -0.11F, -0.20F, -104, 4, 3, 0},
            {0.73F, -0.08F, -0.09F, -0.20F, -101, 4, 0, 0},
            {1.00F, 0, 0, 0, 0, 0, 0, 0}};
    private static final float[][] RIPOSTE_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.16F, -0.10F, 0.08F, 0.02F, -20, -30, 40, 0},
            {0.28F, -0.12F, 0.10F, 0.04F, -22, -32, 44, 0},
            {0.46F, -0.08F, 0.05F, -0.44F, -86, 6, 22, 0},
            {0.58F, -0.07F, 0.05F, -0.38F, -84, 5, 26, 0},
            {1.00F, 0, 0, 0, 0, 0, 0, 0}};
    private static final float[][] SICKLE_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.18F, 0.10F, 0.10F, 0.02F, -10, -40, -10, 0},
            {0.42F, -0.30F, 0.02F, -0.16F, -50, 60, 40, 0},
            {0.56F, -0.20F, 0, -0.04F, -40, 40, 30, 0},
            {1.00F, 0, 0, 0, 0, 0, 0, 0}};
    private static final float[][] AXE_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.22F, 0.04F, 0.30F, 0.06F, 20, -10, -30, 0},
            {0.44F, -0.30F, -0.16F, -0.14F, -90, 10, 60, 0},
            {0.56F, -0.32F, -0.20F, -0.12F, -95, 12, 64, 0},
            {1.00F, 0, 0, 0, 0, 0, 0, 0}};
    private static final float[][] PUNCH_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.18F, 0.06F, -0.04F, 0.12F, 10, 0, 0, 0},
            {0.34F, -0.12F, 0.04F, -0.42F, -12, 4, 0, 0},
            {0.44F, -0.11F, 0.04F, -0.38F, -10, 4, 0, 0},
            {1.00F, 0, 0, 0, 0, 0, 0, 0}};
    private static final float[][] TRIPLESTAB_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.10F, 0.04F, 0.02F, 0.06F, -70, 4, 6, 0},
            {0.22F, -0.08F, 0.05F, -0.32F, -86, 6, 20, 0},
            {0.34F, 0.02F, 0.03F, 0.00F, -74, 4, 10, 0},
            {0.46F, -0.10F, 0.07F, -0.34F, -88, 6, 24, 0},
            {0.58F, 0.02F, 0.03F, 0.00F, -74, 4, 10, 0},
            {0.71F, -0.12F, 0.06F, -0.36F, -90, 6, 26, 0},
            {0.80F, -0.10F, 0.05F, -0.33F, -88, 6, 24, 0},
            {1.00F, 0, 0, 0, 0, 0, 0, 0}};
    private static final float[][] SCYTHE_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.14F, 0.06F, 0.18F, 0.02F, -4, -14, -14, 0},
            {0.60F, -0.34F, -0.02F, -0.18F, -60, 70, 30, -360},
            {0.72F, -0.30F, -0.02F, -0.14F, -56, 64, 28, -360},
            {1.00F, 0, 0, 0, 0, 0, 0, -360}};
    private static final float[][] BOOMERANG_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.12F, -0.04F, 0.10F, 0.04F, -20, 0, 0, 0},
            {0.40F, -0.10F, 0.12F, -0.60F, -60, 0, 10, 360},
            {0.70F, -0.06F, 0.08F, -0.20F, -40, 0, 6, 720},
            {1.00F, 0, 0, 0, 0, 0, 0, 720}};
    private static final float[][] WINDMILL_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.14F, -0.10F, 0.20F, -0.04F, 4, 0, 8, 0},
            {0.70F, -0.14F, 0.10F, -0.14F, -20, 0, 10, -720},
            {0.84F, -0.08F, 0.02F, -0.08F, -10, 0, 4, -720},
            {1.00F, 0, 0, 0, 0, 0, 0, -720}};
    private static final float[][] VORTEX_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.14F, 0.06F, 0.10F, 0.02F, -20, -20, -20, 0},
            {0.50F, -0.30F, 0.04F, -0.12F, -50, 30, 70, 360},
            {0.64F, -0.28F, 0.02F, -0.10F, -48, 28, 66, 360},
            {1.00F, 0, 0, 0, 0, 0, 0, 360}};
    private static final float[][] CLEAVE_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.12F, -0.04F, 0.26F, 0, 6, 0, -4, 0},
            {0.24F, -0.06F, -0.16F, -0.22F, -115, 0, 4, 0},
            {0.40F, -0.06F, -0.14F, -0.20F, -110, 0, 2, 0},
            {1.00F, 0, 0, 0, 0, 0, 0, 0}};
    private static final float[][] SWEEP_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.16F, 0.10F, -0.10F, 0.02F, -60, -50, 90, 0},
            {0.42F, -0.36F, -0.12F, -0.12F, -62, 60, 92, 0},
            {0.54F, -0.38F, -0.12F, -0.10F, -62, 64, 92, 0},
            {1.00F, 0, 0, 0, 0, 0, 0, 0}};
    private static final float[][] RISINGDRAGON_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.16F, -0.14F, -0.05F, -0.02F, 20, 6, 58, 0},
            {0.36F, 0.04F, 0.26F, -0.10F, -36, -6, -26, 0},
            {0.62F, 0.02F, 0.30F, -0.12F, -30, -6, -20, 360},
            {0.78F, -0.06F, 0.04F, -0.08F, -50, 4, 20, 360},
            {1.00F, 0, 0, 0, 0, 0, 0, 360}};
    private static final float[][] FLOURISH_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.12F, -0.12F, 0.12F, -0.02F, -6, 20, 14, 0},
            {0.56F, -0.14F, 0.14F, -0.04F, -8, 24, 16, 360},
            {0.70F, -0.06F, 0.06F, -0.30F, -80, 4, 10, 360},
            {0.80F, -0.06F, 0.06F, -0.28F, -78, 4, 10, 360},
            {1.00F, 0, 0, 0, 0, 0, 0, 360}};
    private static final float[][] GUARD_K = {
            {0.00F, 0, 0, 0, 0, 0, 0, 0},
            {0.20F, -0.20F, 0.10F, 0, -10, -10, 85, 0},
            {0.34F, -0.22F, 0.12F, 0, -12, -12, 88, 0},
            {0.52F, 0.08F, 0.02F, -0.12F, -40, -20, -40, 0},
            {0.62F, 0.10F, 0, -0.10F, -42, -22, -44, 0},
            {1.00F, 0, 0, 0, 0, 0, 0, 0}};

    private static float[][] keys(SwingStyle st) {
        return switch (st) {
            case KNIGHT -> KNIGHT_K;
            case KATANA -> KATANA_K;
            case XSLASH -> XSLASH_K;
            case LUNGE -> LUNGE_K;
            case SMASH -> SMASH_K;
            case FLIP -> FLIP_K;
            case TWIRL -> TWIRL_K;
            case REVERSE -> REVERSE_K;
            case KARAMBIT -> KARAMBIT_K;
            case DRAGON -> DRAGON_K;
            case CRESCENT -> CRESCENT_K;
            case UPPERCUT -> UPPERCUT_K;
            case CROSS -> CROSS_K;
            case DOUBLESTAB -> DOUBLESTAB_K;
            case EXECUTION -> EXECUTION_K;
            case RONIN -> RONIN_K;
            case PHANTOM -> PHANTOM_K;
            case WHIRLWIND -> WHIRLWIND_K;
            case TORNADO -> TORNADO_K;
            case MOONLIGHT -> MOONLIGHT_K;
            case ZIGZAG -> ZIGZAG_K;
            case HAMMER -> HAMMER_K;
            case RIPOSTE -> RIPOSTE_K;
            case SICKLE -> SICKLE_K;
            case AXE -> AXE_K;
            case PUNCH -> PUNCH_K;
            case TRIPLESTAB -> TRIPLESTAB_K;
            case SCYTHE -> SCYTHE_K;
            case BOOMERANG -> BOOMERANG_K;
            case WINDMILL -> WINDMILL_K;
            case VORTEX -> VORTEX_K;
            case CLEAVE -> CLEAVE_K;
            case SWEEP -> SWEEP_K;
            case RISINGDRAGON -> RISINGDRAGON_K;
            case FLOURISH -> FLOURISH_K;
            case GUARD -> GUARD_K;
            default -> null;
        };
    }

    private static final ThreadLocal<float[]> TMP = ThreadLocal.withInitial(() -> new float[CH]);

    private static void keyed(PoseStack m, int i, float p, SwingStyle style, float strength) {
        float[][] k = keys(style);
        float[] o = TMP.get();
        sample(k, p, o);
        // guc: konum genligi dogrudan, acilar daha yumusak olceklenir (asiri acida esya ters donmesin)
        float ts = Mth.clamp(strength, 0.5F, 2.0F);
        float rs = Mth.clamp(1.0F + (strength - 1.0F) * 0.3F, 0.85F, 1.6F);
        m.translate(i * (REST_X + o[0] * ts), REST_Y + o[1] * ts, REST_Z + o[2] * ts);
        if (o[4] != 0) m.mulPose(Axis.YP.rotationDegrees(i * o[4] * rs));
        if (o[5] != 0) m.mulPose(Axis.ZP.rotationDegrees(i * o[5] * rs));
        if (o[3] != 0) m.mulPose(Axis.XP.rotationDegrees(o[3] * rs));
        float spin = o[6];
        if (spin == 0) return;
        // tam turlar kabzanin etrafinda (esya elden kopmuyormus gibi)
        m.translate(i * GRIP_X, GRIP_Y, GRIP_Z);
        switch (style) {
            case TWIRL, DRAGON, WHIRLWIND, SCYTHE, BOOMERANG, RISINGDRAGON -> m.mulPose(Axis.ZP.rotationDegrees(i * spin));
            case KARAMBIT, TORNADO, VORTEX, FLOURISH -> m.mulPose(new org.joml.Quaternionf().rotationAxis((float) Math.toRadians(i * spin), 0F, BLADE_Y, BLADE_Z));
            default -> m.mulPose(Axis.XP.rotationDegrees(spin));
        }
        m.translate(-i * GRIP_X, -GRIP_Y, -GRIP_Z);
    }

    /** Monoton kubik Hermite ornekleme (Fritsch-Carlson egimleri); uclarda hiz sifir. */
    static void sample(float[][] k, float p, float[] out) {
        int n = k.length;
        if (!(p > k[0][0])) {
            System.arraycopy(k[0], 1, out, 0, CH);
            return;
        }
        if (p >= k[n - 1][0]) {
            System.arraycopy(k[n - 1], 1, out, 0, CH);
            return;
        }
        int j = 0;
        while (j < n - 2 && p >= k[j + 1][0]) j++;
        float t0 = k[j][0], t1 = k[j + 1][0], h = t1 - t0;
        float u = (p - t0) / h, u2 = u * u, u3 = u2 * u;
        float h00 = 2 * u3 - 3 * u2 + 1, h10 = u3 - 2 * u2 + u, h01 = -2 * u3 + 3 * u2, h11 = u3 - u2;
        for (int c = 1; c <= CH; c++) {
            out[c - 1] = h00 * k[j][c] + h10 * h * slope(k, j, c) + h01 * k[j + 1][c] + h11 * h * slope(k, j + 1, c);
        }
    }

    private static float slope(float[][] k, int j, int c) {
        if (j == 0 || j == k.length - 1) return 0F;
        float d0 = (k[j][c] - k[j - 1][c]) / (k[j][0] - k[j - 1][0]);
        float d1 = (k[j + 1][c] - k[j][c]) / (k[j + 1][0] - k[j][0]);
        if (d0 * d1 <= 0F) return 0F; // tepe / dip: poz noktasinda dur
        return 2F * d0 * d1 / (d0 + d1);
    }

    /** Yerel oyuncunun vurus suresini ayarlanan carpanla olcekler (Haste/Mining Fatigue korunur). */
    public static int scaleDuration(int vanilla) {
        ModConfig c = cfg();
        int r = c == null || !c.swingEnabled ? vanilla : Math.max(1, Math.round(vanilla * c.swingDuration));
        duration = r;
        return r;
    }
}
