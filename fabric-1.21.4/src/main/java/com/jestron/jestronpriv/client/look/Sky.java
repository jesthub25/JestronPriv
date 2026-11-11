package com.jestron.jestronpriv.client.look;

import com.jestron.jestronpriv.cape.CapeArt;
import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.material.FogType;
import org.joml.Matrix4f;

/**
 * Ozel gokyuzu (sadece gorunus, istemci): kutup isigi, kayan yildizlar, gun batimi rengi, gunes / ay boyutu, ay
 * evresi, yildiz parlakligi ve gok rengi tonu. Hepsi vanilla gok cizimine eklenir; Nether / End'de (vanilla gok
 * kubbesi olmayan yerlerde) ve kamera su / lav icindeyken efekt cizilmez.
 */
public final class Sky {

    private static final SkyFx FX = new SkyFx();
    private static long lastAt;
    private static float clock;

    private Sky() {
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        ModConfig c = m == null ? null : m.config();
        return c != null && c.skyEnabled ? c : null;
    }

    public static final String[] PALETTES = {"KLASIK", "BUZUL", "ATES", "PEMBE", "GOKKUSAGI", "OZEL"};

    public static String paletteName(String p) {
        return switch (p) {
            case "BUZUL" -> "§bBuzul";
            case "ATES" -> "§cAteş";
            case "PEMBE" -> "§dPembe";
            case "GOKKUSAGI" -> "§eGökkuşağı";
            case "OZEL" -> "§fÖzel";
            default -> "§aKlasik";
        };
    }

    /** Paletin iki rengi (alt, ust). */
    public static int[] palette(ModConfig c) {
        return switch (c.skyAuroraPalette) {
            case "BUZUL" -> new int[]{0x4FE3FF, 0x3D5AFE};
            case "ATES" -> new int[]{0xFF6A3D, 0xFF2D78};
            case "PEMBE" -> new int[]{0xFF5CD6, 0x8C5CFF};
            case "OZEL" -> new int[]{c.skyAuroraColor1, c.skyAuroraColor2};
            default -> new int[]{0x2BFF9A, 0xB24BFF};
        };
    }

    // ------------------------------------------------------------------ kancalar

    public static float sunScale() {
        ModConfig c = cfg();
        return c == null ? 1f : c.skySunSize;
    }

    public static float moonScale() {
        ModConfig c = cfg();
        return c == null ? 1f : c.skyMoonSize;
    }

    public static int moonPhase(int vanilla) {
        ModConfig c = cfg();
        return c == null || c.skyMoonPhase < 0 ? vanilla : c.skyMoonPhase;
    }

    public static float starBrightness(float vanilla) {
        ModConfig c = cfg();
        if (c == null) return vanilla;
        float v = vanilla * c.skyStars;
        if (c.skyStarsDay) {
            Minecraft mc = Minecraft.getInstance();
            float rain = mc.level == null ? 0f : mc.level.getRainLevel(1f);
            v = Math.max(v, 0.35f * c.skyStars * CapeArt.clamp01(1f - rain));
        }
        return CapeArt.clamp(v, 0f, 1f);
    }

    /** Gun dogumu / batimi rengi (ARGB; alfa = gorunurluk). */
    public static int sunsetColor(int vanilla) {
        ModConfig c = cfg();
        if (c == null) return vanilla;
        float a = ((vanilla >>> 24) & 255) / 255f;
        int rgb = vanilla & 0xFFFFFF;
        switch (c.skySunsetMode) {
            case "KAPALI" -> {
                return 0;
            }
            case "OZEL" -> rgb = c.skySunsetColor;
            case "GOKKUSAGI" -> rgb = CapeArt.hsv((Util.getMillis() % 1_000_000L) / 1000f * 0.03f, 0.7f, 1f);
            default -> {
            }
        }
        a = CapeArt.clamp01(a * c.skySunsetStrength);
        return ((int) (a * 255f + 0.5f) << 24) | rgb;
    }

    /** Gok rengi tonu: karanligi korur (gece mor olmaz, sadece renk kayar). */
    public static int skyColor(int vanilla) {
        ModConfig c = cfg();
        if (c == null || !c.skyTint || c.skyTintAmount <= 0f) return vanilla;
        int rgb = vanilla & 0xFFFFFF;
        float l = CapeArt.luma(rgb), lt = Math.max(0.05f, CapeArt.luma(c.skyTintColor));
        int target = CapeArt.mul(c.skyTintColor, Math.min(3f, l / lt));
        return (vanilla & 0xFF000000) | CapeArt.mix(rgb, target, c.skyTintAmount);
    }

    /**
     * Gunes / ay / yildizlardan sonra: kutup isigi ve kayan yildizlar.
     *
     * @param timeOfDay      vanilla gok acisi 0..1
     * @param rainBrightness 1 - yagmur
     */
    public static void renderExtras(PoseStack ps, MultiBufferSource.BufferSource buffers, float timeOfDay, float rainBrightness, FogParameters fog) {
        ModConfig c = cfg();
        if (c == null || (!c.skyAurora && !c.skyShooting)) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameRenderer == null || mc.gameRenderer.getMainCamera().getFluidInCamera() != FogType.NONE) return;
        long now = Util.getMillis();
        float dt = lastAt == 0L ? 0f : Math.min(0.25f, (now - lastAt) / 1000f);
        lastAt = now;
        clock = (clock + dt) % 3600f;
        float cs = (float) Math.cos(timeOfDay * CapeArt.TAU);
        float night = CapeArt.clamp01(1f - (cs * 2f + 0.25f));
        night = night * night * CapeArt.clamp01(rainBrightness);
        float auroraK = c.skyAurora ? Math.max(night, c.skyAuroraDay ? 0.3f * CapeArt.clamp01(rainBrightness) : 0f) * c.skyAuroraStrength : 0f;
        if (c.skyShooting) FX.tickMeteors(dt, 4f * c.skyShootingRate, night, c.skyShootingLength);
        boolean meteors = c.skyShooting && FX.aliveCount() > 0;
        if (auroraK <= 0.003f && !meteors) return;
        Matrix4f m = ps.last().pose();
        VertexConsumer vc = buffers.getBuffer(SkyRenderType.GLOW);
        SkyFx.Sink sink = (xyz, argb) -> {
            for (int i = 0; i < 4; i++) vc.addVertex(m, xyz[i * 3], xyz[i * 3 + 1], xyz[i * 3 + 2]).setColor(argb[i]);
        };
        if (auroraK > 0.003f) {
            int[] pal = palette(c);
            FX.aurora(sink, clock * c.skyAuroraSpeed, auroraK, c.skyAuroraHeight, pal[0], pal[1], "GOKKUSAGI".equals(c.skyAuroraPalette));
        }
        if (meteors) FX.meteors(sink, c.skyShootingColor, Math.max(0.35f, night));
        // yildizlar gibi: gok sisi bu isiklari bogmasin
        RenderSystem.setShaderFog(FogParameters.NO_FOG);
        try {
            buffers.endBatch(SkyRenderType.GLOW);
        } finally {
            RenderSystem.setShaderFog(fog);
        }
    }

    /** Ayar ekranindan "dene": hemen bir kayan yildiz. */
    public static void testMeteor() {
        ModConfig c = cfg();
        FX.forceMeteor(c == null ? 1f : c.skyShootingLength);
    }
}
