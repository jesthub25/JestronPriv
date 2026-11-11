package com.jestron.jestronpriv.client.look;

import com.jestron.jestronpriv.cape.CapeArt;
import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.joml.Matrix4f;

import java.io.InputStream;
import java.util.Optional;

/**
 * Buyu parlamasi (enchant glint): renk, hiz, parlaklik, desen boyutu ve egim. Esya ve zirh ayri ayarlanabilir.
 * <p>
 * Nasil: vanilla parlama dokusu okunur, parlaklik haritasina cevrilir ve secilen renklerle yeniden boyanir (kendi
 * dokumuz, vanilla dokusu degismez). Parlama cizilirken doku matrisi kurulan yerde (vanilla setupGlintTexturing)
 * bizim dokumuz baglanir; renk donme modunda renk, golgelendiricinin renk carpaniyla verilir ve cizim biter bitmez
 * sifirlanir. Kapaliyken vanilla aynen calisir. Doku sadece ayar degisince uretilir (cizim sirasinda dosya / GPU
 * yuklemesi yok).
 */
public final class Glint {

    private static final ResourceLocation ITEM_TEX = new ResourceLocation("jestronpriv", "dynamic/glint_item");
    private static final ResourceLocation ENTITY_TEX = new ResourceLocation("jestronpriv", "dynamic/glint_entity");

    /** Kaynak parlaklik haritasi (0..1) ve boyutu. */
    private static final class Source {
        float[] lum;
        int w, h;
        boolean failed;
    }

    private static final Source SRC_ITEM = new Source(), SRC_ENTITY = new Source();
    private static DynamicTexture itemTex, entityTex;
    private static String itemKey, entityKey;
    private static int itemId = -1, entityId = -1;
    private static boolean colorSet;
    private static float prevAlpha = 1f;

    private Glint() {
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    public static final String[] MODES = {"TEK", "IKI", "AKAN", "DONEN", "VANILLA"};

    public static String modeName(String m) {
        return switch (m) {
            case "IKI" -> "İki renk";
            case "AKAN" -> "Akan gökkuşağı";
            case "DONEN" -> "Renk döngüsü";
            case "VANILLA" -> "Vanilla mor";
            default -> "Tek renk";
        };
    }

    public static String next(String m, boolean back) {
        int i = 0;
        for (int k = 0; k < MODES.length; k++) if (MODES[k].equals(m)) i = k;
        return MODES[Math.floorMod(i + (back ? -1 : 1), MODES.length)];
    }

    // ------------------------------------------------------------------ cizim kancalari (render thread)

    /**
     * Vanilla {@code setupGlintTexturing(scale)} yerine. true: vanilla atlanir.
     *
     * @param scale 8 = esya (elde / GUI), 0.16 = varlik (zirh, trident, kalkan)
     */
    public static boolean setup(float scale) {
        ModConfig c = cfg();
        if (c == null || !c.glintEnabled) return false;
        Minecraft mc = Minecraft.getInstance();
        boolean entity = scale < 1f;
        boolean useArmor = entity && !c.glintArmorSame;
        String mode = useArmor ? c.glintArmorMode : c.glintItemMode;
        double vs = mc.options == null ? 0.5 : mc.options.glintSpeed().get();
        long i = (long) ((double) Util.getMillis() * vs * 8.0 * c.glintSpeed);
        float f = (float) (i % 110000L) / 110000.0F;
        float f1 = (float) (i % 30000L) / 30000.0F;
        float sc = scale / Math.max(0.4f, Math.min(2.5f, c.glintScale));
        Matrix4f m = new Matrix4f().translation(-f, f1, 0.0F);
        m.rotateZ((float) Math.toRadians(c.glintAngle)).scale(sc);
        RenderSystem.setTextureMatrix(m);

        float r = 1f, g = 1f, b = 1f;
        if (!"VANILLA".equals(mode)) {
            int id = entity ? entityId : itemId;
            if (id > 0) {
                RenderSystem.setShaderTexture(0, id);
                if ("DONEN".equals(mode)) {
                    float h = (Util.getMillis() % 1_000_000L) / 1000f * 0.08f * c.glintCycleSpeed;
                    int rgb = CapeArt.hsv(h, 0.8f, 1f);
                    r = ((rgb >> 16) & 255) / 255f;
                    g = ((rgb >> 8) & 255) / 255f;
                    b = (rgb & 255) / 255f;
                }
            } else {
                // dokumuz henuz hazir degil: vanilla dokusu renk carpaniyla
                int col = useArmor ? c.glintArmorColor : c.glintItemColor;
                r = 0.4f + 0.6f * ((col >> 16) & 255) / 255f;
                g = 0.4f + 0.6f * ((col >> 8) & 255) / 255f;
                b = 0.4f + 0.6f * (col & 255) / 255f;
            }
        }
        float vanillaStrength = mc.options == null ? 0.75f : mc.options.glintStrength().get().floatValue();
        float alpha = vanillaStrength * c.glintStrength;
        if (c.glintPulse) alpha *= 0.62f + 0.38f * (float) Math.sin((Util.getMillis() % 1_000_000L) / 1000f * 2.4f);
        prevAlpha = RenderSystem.getShaderGlintAlpha();
        RenderSystem.setShaderGlintAlpha((double) Math.max(0f, Math.min(2f, alpha)));
        RenderSystem.setShaderColor(r, g, b, 1f);
        colorSet = true;
        return true;
    }

    /** Doku matrisi sifirlanirken (parlama cizimi bitti): renk ve parlaklik eski haline. */
    public static void reset() {
        if (!colorSet) return;
        colorSet = false;
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderGlintAlpha((double) prevAlpha);
    }

    // ------------------------------------------------------------------ doku (istemci tick'i)

    /** Ayar degistiyse dokulari yeniden uret (ucuz kontrol, her tick). */
    public static void tick(Minecraft mc) {
        ModConfig c = cfg();
        if (c == null || !c.glintEnabled) return;
        String ik = key(c.glintItemMode, c.glintItemColor, c.glintItemColor2);
        if (!ik.equals(itemKey)) {
            itemKey = ik;
            itemId = build(mc, SRC_ITEM, ItemRenderer.ENCHANTED_GLINT_ITEM, ITEM_TEX, false, c.glintItemMode, c.glintItemColor, c.glintItemColor2);
        }
        boolean same = c.glintArmorSame;
        String mode = same ? c.glintItemMode : c.glintArmorMode;
        int c1 = same ? c.glintItemColor : c.glintArmorColor, c2 = same ? c.glintItemColor2 : c.glintArmorColor2;
        String ek = key(mode, c1, c2);
        if (!ek.equals(entityKey)) {
            entityKey = ek;
            entityId = build(mc, SRC_ENTITY, ItemRenderer.ENCHANTED_GLINT_ENTITY, ENTITY_TEX, true, mode, c1, c2);
        }
    }

    private static String key(String mode, int c1, int c2) {
        return switch (mode) {
            case "TEK" -> "TEK" + c1;
            case "IKI" -> "IKI" + c1 + ":" + c2;
            case "VANILLA" -> "VANILLA";
            default -> mode;
        };
    }

    /** Kaynak paketleri yeniden yuklendi: vanilla dokusu degismis olabilir. */
    public static void onResourceReload() {
        SRC_ITEM.lum = null;
        SRC_ITEM.failed = false;
        SRC_ENTITY.lum = null;
        SRC_ENTITY.failed = false;
        itemKey = null;
        entityKey = null;
    }

    private static boolean load(Minecraft mc, Source s, ResourceLocation loc) {
        if (s.lum != null) return true;
        if (s.failed) return false;
        try {
            Optional<Resource> res = mc.getResourceManager().getResource(loc);
            if (res.isEmpty()) {
                s.failed = true;
                return false;
            }
            try (InputStream in = res.get().open(); NativeImage img = NativeImage.read(in)) {
                int w = img.getWidth(), h = img.getHeight();
                if (w <= 0 || h <= 0 || w > 1024 || h > 1024) {
                    s.failed = true;
                    return false;
                }
                float[] lum = new float[w * h];
                float max = 0.001f;
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        int p = img.getPixel(x, y);
                        float a = ((p >>> 24) & 255) / 255f;
                        float l = Math.max(((p >> 16) & 255), Math.max(((p >> 8) & 255), (p & 255))) / 255f * a;
                        lum[y * w + x] = l;
                        if (l > max) max = l;
                    }
                }
                for (int k = 0; k < lum.length; k++) lum[k] = Math.min(1f, lum[k] / max);
                s.lum = lum;
                s.w = w;
                s.h = h;
                return true;
            }
        } catch (Throwable t) {
            s.failed = true;
            return false;
        }
    }

    /** Dokuyu boyar; GL kimligi ya da -1 (vanilla dokusuna dusulur). */
    private static int build(Minecraft mc, Source s, ResourceLocation src, ResourceLocation dst, boolean entity, String mode, int c1, int c2) {
        if ("VANILLA".equals(mode) || !load(mc, s, src)) return -1;
        try {
            DynamicTexture tex = entity ? entityTex : itemTex;
            if (tex == null || tex.getPixels() == null || tex.getPixels().getWidth() != s.w || tex.getPixels().getHeight() != s.h) {
                if (tex != null) mc.getTextureManager().release(dst);
                tex = new DynamicTexture(s.w, s.h, false);
                mc.getTextureManager().register(dst, tex);
                if (entity) entityTex = tex;
                else itemTex = tex;
            }
            NativeImage img = tex.getPixels();
            if (img == null) return -1;
            int w = s.w, h = s.h;
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    float l = s.lum[y * w + x];
                    int col = color(mode, c1, c2, x / (float) w, y / (float) h);
                    // biraz parlat: koyu renkler de gorunsun (toplamali karisim)
                    float k = Math.min(1f, l * 1.15f);
                    int rr = (int) (((col >> 16) & 255) * k), gg = (int) (((col >> 8) & 255) * k), bb = (int) ((col & 255) * k);
                    img.setPixel(x, y, 0xFF000000 | (rr << 16) | (gg << 8) | bb);
                }
            }
            tex.upload();
            // vanilla parlama dokusu gibi: yumusak (blur), tekrar eden (clamp yok)
            tex.setFilter(true, false);
            tex.setClamp(false);
            return tex.getId();
        } catch (Throwable t) {
            return -1;
        }
    }

    /** Desen rengi (dikissiz: doku tekrar ettiginde kenarlar bulusur). */
    static int color(String mode, int c1, int c2, float u, float v) {
        return switch (mode) {
            case "IKI" -> CapeArt.mix(c1, c2, CapeArt.tri(u + 0.25f * CapeArt.tri(v)));
            case "AKAN" -> CapeArt.hsv(u + 0.25f * CapeArt.tri(v), 0.85f, 1f);
            case "DONEN" -> 0xFFFFFF;
            default -> c1;
        };
    }
}
