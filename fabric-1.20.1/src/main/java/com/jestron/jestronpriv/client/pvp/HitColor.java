package com.jestron.jestronpriv.client.pvp;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.mixin.OverlayTextureAccessor;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;

/**
 * Vurulma rengi. Vanilla kirmizi yanip sonme, 16x16 "overlay" dokusunun 0-7. satirlarindan okunur ve yalnizca 3.
 * satir kullanilir. Ozel renk 4-7. satirlara yazilir; rengi degistirilecek varliklar 6. satiri kullanir. Boylece
 * vanilla kirmizisi (digerleri) hic bozulmaz ve renk sadece istenen varliklarda gorunur.
 */
public final class HitColor {

    public static final int CUSTOM_V = 6;
    private static final int MAXT = 24;
    private static final int[] IDS = new int[MAXT];
    private static final long[] UNTIL = new long[MAXT];
    private static int n;
    private static long ticks;
    private static int uploaded = -1;
    private static boolean failed;

    private HitColor() {
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    /** Senin vurdugun hedef: kisa bir sure (yanip sonme boyunca) renklenir. */
    public static void onHit(int id) {
        for (int i = 0; i < n; i++) {
            if (IDS[i] == id) {
                UNTIL[i] = ticks + 14;
                return;
            }
        }
        if (n < MAXT) {
            IDS[n] = id;
            UNTIL[n] = ticks + 14;
            n++;
        }
    }

    public static void tick() {
        ticks++;
        for (int i = 0; i < n; ) {
            if (UNTIL[i] < ticks) {
                n--;
                IDS[i] = IDS[n];
                UNTIL[i] = UNTIL[n];
            } else {
                i++;
            }
        }
    }

    /** Render durumu cikarilirken: bu varlik ozel renkle mi cizilecek. */
    public static boolean applies(LivingEntity e) {
        ModConfig c = cfg();
        if (c == null || !c.hitColor || failed || e == null || !(e.hurtTime > 0 || e.deathTime > 0)) return false;
        if (c.hitColorAll) return true;
        int id = e.getId();
        for (int i = 0; i < n; i++) if (IDS[i] == id) return true;
        return false;
    }

    /** Paketlenmis overlay koordinatinda kirmizi satiri ozel satirla degistirir (beyaz parlama u korunur). */
    public static int remap(int packed) {
        return (packed & 0xFFFF) | (CUSTOM_V << 16);
    }

    /**
     * Doku pikseli. Golgelendiricide {@code mix(overlay.rgb, renk, overlay.a)}: alfa ASIL rengin agirligidir, yani
     * yogunluk 0.7 icin alfa 0.3 olmali (vanilla kirmizi = yogunluk 0.30).
     */
    static int texel(int rgb, float strength) {
        float k = Math.max(0.2f, Math.min(0.9f, strength));
        int a = Math.round((1f - k) * 255f);
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    /** Her karenin basinda (render thread): renk degistiyse / gokkusagiysa ozel satirlari yeniden yukler. */
    public static void frame() {
        ModConfig c = cfg();
        if (c == null || !c.hitColor || failed) return;
        int rgb = c.hitColorRainbow ? Palette.rainbow(System.nanoTime() / 1e9, 0) : c.hitColorRgb & 0xFFFFFF;
        int argb = texel(rgb, c.hitColorAlpha);
        if (argb == uploaded) return;
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gameRenderer == null) return;
            OverlayTexture ot = mc.gameRenderer.overlayTexture();
            DynamicTexture tex = ((OverlayTextureAccessor) ot).jestronpriv$texture();
            NativeImage img = tex.getPixels();
            if (img == null) return;
            for (int y = 4; y < 8; y++) for (int x = 0; x < 16; x++) img.setPixel(x, y, argb);
            // vanilla OverlayTexture kurucusuyla ayni yukleme (doku birimi 1)
            RenderSystem.activeTexture(33985);
            tex.bind();
            img.upload(0, 0, 0, 0, 0, img.getWidth(), img.getHeight(), false);
            RenderSystem.activeTexture(33984);
            uploaded = argb;
        } catch (Throwable t) {
            failed = true;
            JestronPrivClient.LOG.warn("Vurulma rengi devre disi: {}", t.toString());
        }
    }
}
