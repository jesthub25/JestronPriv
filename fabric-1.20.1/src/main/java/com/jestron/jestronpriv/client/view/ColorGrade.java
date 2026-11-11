package com.jestron.jestronpriv.client.view;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.mixin.PostChainAccessor;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Locale;

/**
 * Ekran renk duzenleme: doygunluk, parlaklik, kontrast, sicaklik ve hazir filtreler.
 * Vanilla'nin kendi renk matrisi programini (post/color_convolve) kullanan iki gecisli bir
 * post efekti; GLSL eklenmez. Arayuz etkilenmez, el ve dunya etkilenir.
 * Program diger efektlerle (orn. creeper gorunumu) paylasildigi icin degerler her karede
 * kurulur ve cizimden hemen sonra vanilla varsayilanlarina geri alinir.
 */
public final class ColorGrade {

    private static final ResourceLocation ID = new ResourceLocation("jestronpriv", "color_grade");

    /** Yukleme basarisiz olduysa her karede denenmez; 30 sn sonra (orn. kaynak paketi degisince) tekrar denenir. */
    private static boolean failed;
    private static long failedAt;

    public enum Preset {
        YOK("Yok", new float[]{1, 0, 0, 0, 1, 0, 0, 0, 1}, 1F, 1F, 0F),
        CANLI("Canlı", new float[]{1, 0, 0, 0, 1, 0, 0, 0, 1}, 1.35F, 1.06F, 0F),
        SINEMATIK("Sinematik", new float[]{1.06F, 0, -0.02F, 0, 1.0F, 0, -0.02F, 0.04F, 0.98F}, 0.9F, 1.12F, -0.01F),
        SICAK("Sıcak", new float[]{1.08F, 0, 0, 0, 1.0F, 0, 0, 0, 0.86F}, 1.05F, 1F, 0F),
        SOGUK("Soğuk", new float[]{0.9F, 0, 0, 0, 1.0F, 0, 0, 0, 1.1F}, 1F, 1F, 0F),
        NOSTALJI("Nostalji", new float[]{1, 0, 0, 0, 1, 0, 0, 0, 1}, 0.7F, 0.88F, 0.04F),
        SEPYA("Sepya", new float[]{0.393F, 0.769F, 0.189F, 0.349F, 0.686F, 0.168F, 0.272F, 0.534F, 0.131F}, 1F, 1F, 0F),
        SIYAH_BEYAZ("Siyah-Beyaz", new float[]{0.3F, 0.59F, 0.11F, 0.3F, 0.59F, 0.11F, 0.3F, 0.59F, 0.11F}, 1F, 1.06F, 0F),
        GECE_GORUSU("Gece Görüşü", new float[]{0.1F, 0.3F, 0.1F, 0.25F, 0.95F, 0.2F, 0.1F, 0.3F, 0.1F}, 1F, 1.1F, 0.02F),
        NEON("Neon", new float[]{1, 0, 0, 0, 1, 0, 0, 0, 1}, 1.7F, 1.15F, 0F),
        ALTIN_SAAT("Altın Saat", new float[]{1.12F, 0F, 0F, 0F, 1.02F, 0F, 0F, 0F, 0.82F}, 1.12F, 1.04F, 0F),
        MAVI_SAAT("Mavi Saat", new float[]{0.86F, 0F, 0F, 0F, 0.96F, 0F, 0F, 0F, 1.14F}, 0.95F, 1.03F, 0F),
        GUN_BATIMI("Gün Batımı", new float[]{1.175F, 0.122F, -0.148F, -0.017F, 0.929F, 0.038F, 0.095F, -0.079F, 0.834F}, 1.18F, 1.05F, 0F),
        SAFAK("Şafak", new float[]{1.06F, 0F, 0F, 0F, 0.98F, 0F, 0F, 0F, 1.02F}, 0.92F, 0.95F, 0.03F),
        OKYANUS("Okyanus", new float[]{0.82F, 0F, 0F, 0F, 1.02F, 0F, 0F, 0F, 1.12F}, 1.1F, 1.02F, 0F),
        ORMAN("Orman", new float[]{0.92F, 0F, 0F, 0F, 1.08F, 0F, 0F, 0F, 0.88F}, 1.15F, 1.04F, 0F),
        COL("Çöl", new float[]{1.1F, 0F, 0F, 0F, 1F, 0F, 0F, 0F, 0.8F}, 0.8F, 1.04F, 0.02F),
        KIS("Kış", new float[]{0.94F, 0F, 0F, 0F, 1F, 0F, 0F, 0F, 1.1F}, 0.7F, 1.02F, 0.03F),
        SONBAHAR("Sonbahar", new float[]{1.171F, 0.187F, -0.218F, -0.025F, 0.945F, 0.059F, 0.135F, -0.106F, 0.772F}, 1.1F, 1.04F, 0F),
        ILKBAHAR("İlkbahar", new float[]{1.02F, 0F, 0F, 0F, 1.08F, 0F, 0F, 0F, 0.98F}, 1.25F, 1F, 0.01F),
        PASTEL("Pastel", new float[]{1F, 0F, 0F, 0F, 1F, 0F, 0F, 0F, 1F}, 0.7F, 0.82F, 0.08F),
        SOLUK_FILM("Soluk Film", new float[]{1.02F, 0F, 0F, 0F, 1F, 0F, 0F, 0F, 0.96F}, 0.72F, 0.86F, 0.06F),
        POLAROID("Polaroid", new float[]{1.438F, -0.062F, -0.062F, -0.122F, 1.378F, -0.122F, -0.016F, -0.016F, 1.483F}, 0.85F, 0.9F, -0.02F),
        KODAK("Film (Sıcak)", new float[]{1.08F, 0F, 0F, 0F, 1.02F, 0F, 0F, 0F, 0.9F}, 1.08F, 1.12F, 0F),
        TEKNIKOLOR("Teknikolor", new float[]{1.91F, -0.85F, -0.09F, -0.31F, 1.77F, -0.46F, -0.23F, -0.75F, 1.98F}, 0.75F, 1F, -0.03F),
        CAPRAZ_ISLEM("Çapraz İşlem", new float[]{1.1F, 0.05F, -0.1F, 0F, 1.12F, -0.05F, -0.05F, 0.1F, 0.82F}, 1.2F, 1.12F, 0.01F),
        TEAL_TURUNCU("Teal & Turuncu", new float[]{1.1F, 0F, -0.05F, -0.02F, 1F, 0.05F, -0.1F, 0.05F, 1.12F}, 1.15F, 1.1F, 0F),
        MATRIX("Matrix", new float[]{0.6F, 0.2F, 0F, 0.1F, 1.1F, 0.1F, 0.1F, 0.2F, 0.5F}, 0.85F, 1.1F, 0F),
        CYBERPUNK("Cyberpunk", new float[]{1.15F, 0F, 0.1F, 0F, 0.85F, 0.1F, 0.1F, 0.1F, 1.2F}, 1.35F, 1.1F, 0F),
        VAPORWAVE("Vaporwave", new float[]{0.985F, -0.205F, 0.319F, 0.049F, 0.926F, -0.076F, -0.268F, 0.294F, 1.123F}, 1.3F, 0.95F, 0.03F),
        SENTEZ("Synthwave", new float[]{0.962F, -0.27F, 0.459F, 0.064F, 0.826F, -0.09F, -0.375F, 0.443F, 1.132F}, 1.4F, 1.08F, 0F),
        MARS("Kızıl Gezegen", new float[]{1.1F, 0.15F, 0F, 0.15F, 0.75F, 0F, 0.05F, 0.1F, 0.55F}, 0.95F, 1.06F, 0F),
        BUZUL("Buzul", new float[]{0.671F, 0.15F, 0.029F, 0.091F, 0.894F, 0.035F, 0.106F, 0.208F, 0.866F}, 1F, 1.05F, 0.03F),
        ZEHIRLI("Zehirli", new float[]{0.8F, 0.25F, 0F, 0.1F, 1.15F, 0F, 0F, 0.2F, 0.6F}, 1.3F, 1.08F, 0F),
        KAN_AYI("Kan Ayı", new float[]{1.15F, 0.1F, 0.05F, 0.1F, 0.6F, 0.05F, 0.1F, 0.05F, 0.55F}, 1F, 1.15F, -0.02F),
        LAV("Lav", new float[]{1.281F, 0.169F, -0.2F, -0.018F, 0.826F, 0.043F, 0.077F, -0.062F, 0.535F}, 1.3F, 1.1F, 0F),
        NETHER("Nether", new float[]{1.2F, 0.1F, 0F, 0.1F, 0.8F, 0F, 0F, 0.05F, 0.75F}, 1.1F, 1.08F, 0F),
        END("End", new float[]{0.788F, -0.262F, 0.474F, 0.082F, 0.846F, -0.108F, -0.431F, 0.535F, 1.076F}, 1.05F, 1.06F, 0.01F),
        DERIN_DENIZ("Derin Deniz", new float[]{0.55F, 0.1F, 0.05F, 0.05F, 0.85F, 0.15F, 0.05F, 0.15F, 1.1F}, 0.95F, 1.06F, 0F),
        TERS("Ters Renk", new float[]{-1F, 0F, 0F, 0F, -1F, 0F, 0F, 0F, -1F}, 1F, 1F, 1F),
        RONTGEN("Röntgen", new float[]{-0.299F, -0.587F, -0.114F, -0.299F, -0.587F, -0.114F, -0.299F, -0.587F, -0.114F}, 1F, 1.15F, 1F),
        TERMAL("Termal", new float[]{1.6F, 0.4F, -0.2F, 0.2F, 0.5F, 0F, 0.5F, -0.6F, -0.1F}, 1.3F, 1.1F, 0F),
        KOMIK("Çizgi Roman", new float[]{1F, 0F, 0F, 0F, 1F, 0F, 0F, 0F, 1F}, 1.45F, 1.3F, 0F),
        HDR("HDR", new float[]{1F, 0F, 0F, 0F, 1F, 0F, 0F, 0F, 1F}, 1.25F, 1.15F, -0.01F),
        MAT("Mat", new float[]{0.98F, 0F, 0F, 0F, 1F, 0F, 0F, 0F, 1.02F}, 0.95F, 0.85F, 0.04F),
        DRAMATIK("Dramatik", new float[]{1.02F, 0F, 0F, 0F, 1F, 0F, 0F, 0F, 0.98F}, 0.85F, 1.32F, -0.02F),
        NOIR("Noir", new float[]{0.299F, 0.587F, 0.114F, 0.299F, 0.587F, 0.114F, 0.299F, 0.587F, 0.114F}, 0F, 1.4F, -0.02F),
        GECE("Gece", new float[]{0.566F, 0.154F, 0.03F, 0.089F, 0.727F, 0.034F, 0.12F, 0.236F, 0.793F}, 0.8F, 1.05F, -0.03F),
        RUYA("Rüya", new float[]{1.06F, 0F, 0F, 0F, 0.98F, 0F, 0F, 0F, 1.06F}, 1.2F, 0.88F, 0.05F),
        GAMEBOY("Retro Konsol", new float[]{0.185F, 0.364F, 0.071F, 0.254F, 0.499F, 0.097F, 0.09F, 0.176F, 0.034F}, 1F, 1.1F, 0.04F);

        public final String label;
        final float[] m;
        final float sat, contrast, lift;

        Preset(String label, float[] m, float sat, float contrast, float lift) {
            this.label = label;
            this.m = m;
            this.sat = sat;
            this.contrast = contrast;
            this.lift = lift;
        }

        public Preset next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public Preset previous() {
            return values()[(ordinal() + values().length - 1) % values().length];
        }

        public static Preset parse(String s) {
            if (s != null) {
                String n = s.trim().toUpperCase(Locale.ROOT);
                for (Preset p : values()) if (p.name().equals(n)) return p;
            }
            return YOK;
        }
    }

    private ColorGrade() {
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    /** Ayarlar etkisiz mi (hic islem yapilmaz, maliyet sifir). */
    public static boolean identity(ModConfig c) {
        return c == null || !c.cgEnabled || Preset.parse(c.cgPreset) == Preset.YOK && Math.abs(c.cgSaturation - 1F) < 0.005F
                && Math.abs(c.cgBrightness - 1F) < 0.005F && Math.abs(c.cgContrast - 1F) < 0.005F && Math.abs(c.cgTemperature) < 0.005F;
    }

    /** Dunya (ve el) cizildikten sonra, arayuzden once. */
    @SuppressWarnings("deprecation")
    public static void apply(Minecraft mc, GraphicsResourceAllocator pool) {
        ModConfig c = cfg();
        if (identity(c) || pool == null) return;
        long now = System.currentTimeMillis();
        if (failed && now - failedAt < 30_000L) return;
        failed = false;
        PostChain chain;
        try {
            chain = mc.getShaderManager().getPostChain(ID, LevelTargetBundle.MAIN_TARGETS);
        } catch (Throwable t) {
            chain = null;
        }
        if (chain == null) {
            failed = true;
            failedAt = now;
            JestronPrivClient.LOG.warn("Renk duzenleme efekti yuklenemedi; ayar devre disi kaldi.");
            return;
        }
        List<PostPass> passes = ((PostChainAccessor) chain).jestronpriv$passes();
        if (passes == null || passes.isEmpty()) return;
        CompiledShaderProgram sh = passes.get(0).getShader();
        Preset p = Preset.parse(c.cgPreset);
        float con = c.cgContrast * p.contrast;
        float b = c.cgBrightness;
        float t = c.cgTemperature;
        float[] m = p.m;
        try {
            set3(sh, "RedMatrix", m[0], m[1], m[2]);
            set3(sh, "GreenMatrix", m[3], m[4], m[5]);
            set3(sh, "BlueMatrix", m[6], m[7], m[8]);
            float s = con * b;
            set3(sh, "ColorScale", s * (1F + 0.09F * t), s * (1F + 0.01F * t), s * (1F - 0.09F * t));
            float off = 0.5F * (1F - con) + p.lift;
            set3(sh, "Offset", off, off, off);
            sh.safeGetUniform("Saturation").set(Math.max(0F, c.cgSaturation * p.sat));
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.resetTextureMatrix();
            chain.process(mc.getMainRenderTarget(), pool);
        } catch (Throwable e) {
            failed = true;
            failedAt = now;
            JestronPrivClient.LOG.warn("Renk duzenleme devre disi: {}", e.toString());
        } finally {
            // paylasilan programi vanilla varsayilanlarina dondur (post/color_convolve.json)
            set3(sh, "RedMatrix", 1, 0, 0);
            set3(sh, "GreenMatrix", 0, 1, 0);
            set3(sh, "BlueMatrix", 0, 0, 1);
            set3(sh, "ColorScale", 1, 1, 1);
            set3(sh, "Offset", 0, 0, 0);
            sh.safeGetUniform("Saturation").set(1.8F);
            mc.getMainRenderTarget().bindWrite(true);
        }
    }

    private static void set3(CompiledShaderProgram sh, String name, float x, float y, float z) {
        AbstractUniform u = sh.safeGetUniform(name);
        u.set(x, y, z);
    }
}
