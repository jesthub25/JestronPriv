package com.jestron.jestronpriv.client.tag;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Gorsel dokusu: tum kareler alt alta tek dokuda (seritte). Kare suresine gore zamanla kare secilir.
 * Ana is parcacigindan (cizim is parcacigi) olusturulur ve birakilir.
 */
public final class IconTexture {

    private static int counter;

    public final ResourceLocation id;
    public final int w, h, frames;
    private final int[] ends;     // kare bitis zamanlari (ms, kumulatif)
    private final int total;
    private boolean released;

    private IconTexture(ResourceLocation id, int w, int h, int frames, int[] delays) {
        this.id = id;
        this.w = w;
        this.h = h;
        this.frames = frames;
        this.ends = new int[frames];
        int acc = 0;
        for (int i = 0; i < frames; i++) {
            acc += Math.max(20, i < delays.length ? delays[i] : 100);
            ends[i] = acc;
        }
        this.total = Math.max(1, acc);
    }

    /**
     * @param argb w*h*kare piksel (ARGB), satir satir, kare kare
     * @return doku; olusturulamazsa null
     */
    public static IconTexture create(int w, int h, int frames, int[] delays, int[] argb) {
        if (w < 1 || h < 1 || frames < 1 || argb == null || argb.length < w * h * frames) return null;
        if (w > 512 || h * frames > 8192) return null;
        Minecraft mc = Minecraft.getInstance();
        DynamicTexture tex = null;
        try {
            tex = new DynamicTexture(w, h * frames, false);
            NativeImage img = tex.getPixels();
            if (img == null) {
                tex.close();
                return null;
            }
            for (int f = 0; f < frames; f++) {
                int base = f * w * h;
                for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) img.setPixel(x, f * h + y, argb[base + y * w + x]);
            }
            tex.upload();
            tex.setFilter(true, false);
            ResourceLocation id = new ResourceLocation("jestronpriv", "dyn/icon_" + (++counter));
            mc.getTextureManager().register(id, tex);
            return new IconTexture(id, w, h, frames, delays);
        } catch (Throwable t) {
            if (tex != null) {
                try {
                    tex.close();
                } catch (Throwable ignored) {
                }
            }
            return null;
        }
    }

    /** Su anki kare (ms zamanina gore). */
    public int frameAt(long ms) {
        if (frames <= 1) return 0;
        int t = (int) Math.floorMod(ms, (long) total);
        for (int i = 0; i < frames; i++) if (t < ends[i]) return i;
        return frames - 1;
    }

    public float v0(int frame) {
        return frame / (float) frames;
    }

    public float v1(int frame) {
        return (frame + 1) / (float) frames;
    }

    /** En-boy orani (genislik / yukseklik). */
    public float aspect() {
        return w / (float) h;
    }

    public void release() {
        if (released) return;
        released = true;
        try {
            Minecraft.getInstance().getTextureManager().release(id);
        } catch (Throwable ignored) {
        }
    }

    public boolean released() {
        return released;
    }
}
