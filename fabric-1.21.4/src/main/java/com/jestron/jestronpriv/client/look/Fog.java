package com.jestron.jestronpriv.client.look;

import com.jestron.jestronpriv.cape.CapeArt;
import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector4f;

/**
 * Sis ayari: renk (dogal / ozel / gokkusagi), mesafe, yumusaklik, arazi sisini kapatma, Nether / boss kalin sisi.
 * <p>
 * Sadece acik hava (atmosfer / arazi) sisi degisir. Su, lav, toz kar sisi ile korluk / karanlik etkilerinin sisi
 * ASLA degismez: oyunun dengesi bozulmaz. Boyut bazinda (Overworld / Nether / End) acilip kapatilabilir.
 */
public final class Fog {

    private Fog() {
    }

    private static ModConfig active(Camera cam, ClientLevel level) {
        JestronPrivClient m = JestronPrivClient.get();
        ModConfig c = m == null ? null : m.config();
        if (c == null || !c.fogEnabled || cam == null) return null;
        if (cam.getFluidInCamera() != FogType.NONE) return null;
        Entity e = cam.getEntity();
        if (e instanceof LivingEntity le && (le.hasEffect(MobEffects.BLINDNESS) || le.hasEffect(MobEffects.DARKNESS))) return null;
        if (level == null) return null;
        if (level.dimension() == Level.NETHER) return c.fogDimNether ? c : null;
        if (level.dimension() == Level.END) return c.fogDimEnd ? c : null;
        return c.fogDimOverworld ? c : null;
    }

    /** Vanilla setupFog sonucunu ayarlar. */
    public static FogParameters adjust(Camera cam, FogRenderer.FogMode mode, float renderDistance, boolean thick, FogParameters in) {
        if (in == null || in == FogParameters.NO_FOG || in.start() >= Float.MAX_VALUE * 0.5f) return in;
        ModConfig c = active(cam, Minecraft.getInstance().level);
        if (c == null) return in;
        boolean sky = mode == FogRenderer.FogMode.FOG_SKY;
        if (sky && !c.fogSky) return in;
        float start = in.start(), end = in.end();
        if (thick) {
            switch (c.fogThick) {
                case "AZ" -> {
                    end = Math.min(renderDistance, end * 2.4f);
                    start = sky ? 0f : end * 0.35f;
                }
                case "KAPALI" -> {
                    if (sky) {
                        start = 0f;
                        end = renderDistance;
                    } else {
                        float f = Math.max(4f, Math.min(64f, renderDistance / 10f));
                        start = renderDistance - f;
                        end = renderDistance;
                    }
                }
                default -> {
                }
            }
        }
        if (!sky && c.fogOff && (!thick || !"DOGAL".equals(c.fogThick))) {
            return new FogParameters(Float.MAX_VALUE, Float.MAX_VALUE, in.shape(), in.red(), in.green(), in.blue(), in.alpha());
        }
        float mul = c.fogDistance;
        end *= mul;
        if (sky) start = Math.min(start * mul, end);
        else if (c.fogSoftness > 0f) start = end * (1f - c.fogSoftness);
        else start *= mul;
        if (!Float.isFinite(start) || !Float.isFinite(end) || end <= 0.01f) return in;
        if (start > end) start = end;
        return new FogParameters(start, end, in.shape(), in.red(), in.green(), in.blue(), in.alpha());
    }

    /** Vanilla sis rengini ayarlar (yerinde). Karanlik korunur: gece sis parlamaz, sadece renk kayar. */
    public static void color(Camera cam, ClientLevel level, Vector4f v) {
        if (v == null) return;
        ModConfig c = active(cam, level);
        if (c == null || "DOGAL".equals(c.fogColorMode) || c.fogColorAmount <= 0f) return;
        int target = "GOKKUSAGI".equals(c.fogColorMode) ? CapeArt.hsv((Util.getMillis() % 1_000_000L) / 1000f * 0.04f * c.fogRainbowSpeed, 0.55f, 1f)
                : c.fogColor;
        float r = v.x(), g = v.y(), b = v.z();
        if (!Float.isFinite(r) || !Float.isFinite(g) || !Float.isFinite(b)) return;
        float l = 0.299f * r + 0.587f * g + 0.114f * b;
        float lt = Math.max(0.05f, CapeArt.luma(target));
        float k = Math.min(3f, l / lt);
        float tr = CapeArt.clamp01(((target >> 16) & 255) / 255f * k);
        float tg = CapeArt.clamp01(((target >> 8) & 255) / 255f * k);
        float tb = CapeArt.clamp01((target & 255) / 255f * k);
        float a = CapeArt.clamp01(c.fogColorAmount);
        v.set(r + (tr - r) * a, g + (tg - g) * a, b + (tb - b) * a, v.w());
    }
}
