package com.jestron.jestronpriv.client.cosmetic;

import com.jestron.jestronpriv.cape.CapeDesign;
import com.jestron.jestronpriv.cape.CapeTexture;
import com.jestron.jestronpriv.cape.ElytraDesigns;
import com.jestron.jestronpriv.cape.ElytraTexture;
import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

/**
 * Elytra gorunumleri: sadece kendi oyuncunda, elytra giyiliyken (F5, envanter, menu onizlemesi). Vanilla elytra modeli
 * ve animasyonu aynen kullanilir (acilma, suzulme, egilme); sadece doku 50 animasyonlu desenden biriyle degisir.
 * Doku 256x128 (vanilla x4) ve sadece desen / ayar degisince ya da animasyonlu desende saniyede en cok 20 kez
 * uretilir. Karanlikta parlayan kisimlar ayri isilti dokusuyla ({@code eyes}) cizilir. Bozulursa vanilla elytra gorunur.
 */
public final class Elytras {

    public static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath("jestronpriv", "dynamic/elytra");
    public static final ResourceLocation GLOW_TEX = ResourceLocation.fromNamespaceAndPath("jestronpriv", "dynamic/elytra_glow");
    private static final ElytraTexture GEN = new ElytraTexture();
    private static final CapeTexture.Options OPTS = new CapeTexture.Options();
    private static final int[] BASE = new int[ElytraTexture.TEX_W * ElytraTexture.TEX_H];
    private static final int[] GLOW = new int[ElytraTexture.TEX_W * ElytraTexture.TEX_H];
    private static final Map<String, ParticleOptions> PARTICLES = new HashMap<>();
    private static final Vector3f TP = new Vector3f();
    private static final float[] EDGE_X = {-9f, -5f, -1f};
    private static final double[] EMIT = new double[EDGE_X.length * 2 * 3];

    private static DynamicTexture baseTex, glowTex;
    private static String texDesign;
    private static CapeTexture.Options texOpts;
    private static long texAt, clockAt, texBrokenAt;
    private static float clock;
    private static boolean texBroken, glowAny;
    private static int texFails, emitN, ticks, emitTick;
    private static String emitDesign;
    private static float emitAcc;
    /** Menu onizlemesi: elytra giyili olmasa da cizilir, kanatlar yavasca acilip kapanir. */
    private static boolean preview;
    private static String previewDesign;

    private Elytras() {
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    public static CapeDesign selected() {
        ModConfig c = cfg();
        return c == null ? null : ElytraDesigns.get(c.ely);
    }

    private static boolean glider(ItemStack chest) {
        return chest != null && !chest.isEmpty() && chest.has(DataComponents.GLIDER);
    }

    /** Onizleme cizimi baslat / bitir (menu). designId null: secili desen. */
    public static void beginPreview(String designId) {
        preview = true;
        previewDesign = designId;
    }

    public static void endPreview() {
        preview = false;
        previewDesign = null;
    }

    public static boolean previewActive() {
        return preview;
    }

    /** Bu render durumu icin cizilecek desen (yerel oyuncu, gorunur, elytra giyili ya da onizleme). */
    public static CapeDesign designFor(HumanoidRenderState st) {
        if (!(st instanceof PlayerRenderState prs) || st.isInvisible || prs.isSpectator) return null;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || prs.id != mc.player.getId()) return null;
        if (preview) return previewDesign != null ? ElytraDesigns.get(previewDesign) : selected();
        return glider(st.chestEquipment) ? selected() : null;
    }

    /**
     * WingsLayer yerine cizer.
     *
     * @return true: cizildi, vanilla elytra cizilmesin
     */
    public static boolean render(PoseStack ps, MultiBufferSource buffers, int light, HumanoidRenderState st, ElytraModel adult, ElytraModel baby) {
        CapeDesign d = designFor(st);
        ModConfig c = cfg();
        if (d == null || c == null || adult == null) return false;
        advanceClock(c);
        if (!ensureTexture(d, c)) return false;
        ElytraModel model = st.isBaby && baby != null ? baby : adult;
        ps.pushPose();
        try {
            ps.translate(0.0F, 0.0F, 0.125F);
            model.setupAnim(st);
            if (preview) previewPose(model);
            ItemStack chest = st.chestEquipment;
            boolean foil = chest != null && !chest.isEmpty() && chest.hasFoil();
            VertexConsumer vc = ItemRenderer.getArmorFoilBuffer(buffers, RenderType.armorCutoutNoCull(TEX), foil);
            model.renderToBuffer(ps, vc, light, OverlayTexture.NO_OVERLAY, -1);
            if (glowAny && c.elyGlow) model.renderToBuffer(ps, buffers.getBuffer(RenderType.eyes(GLOW_TEX)), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, -1);
            if (!preview && c.elyParticles && d.particle() != null && d.rate() > 0f) capture(d.id(), model, ps);
        } finally {
            ps.popPose();
        }
        return true;
    }

    /** Onizlemede kanatlar yavasca acilir / kapanir: desen iyi gorunsun. */
    private static void previewPose(ElytraModel model) {
        try {
            ModelPart root = model.root();
            ModelPart l = root.getChild("left_wing"), r = root.getChild("right_wing");
            float k = 0.5f + 0.5f * (float) Math.sin((Util.getMillis() % 6000L) / 6000.0 * Math.PI * 2);
            l.xRot = 0.2618F + k * 0.12f;
            l.zRot = -0.2618F - k * 0.75f;
            l.yRot = 0f;
            r.xRot = l.xRot;
            r.zRot = -l.zRot;
            r.yRot = 0f;
        } catch (Throwable ignored) {
        }
    }

    private static void advanceClock(ModConfig c) {
        long now = Util.getMillis();
        if (clockAt == 0L) clockAt = now;
        float dt = Math.min(0.25f, Math.max(0f, (now - clockAt) / 1000f));
        clockAt = now;
        clock = (clock + dt * c.elySpeed) % 3600f;
    }

    // ------------------------------------------------------------------ doku

    static void fillOptions(ModConfig c, CapeTexture.Options o) {
        o.hue = c.elyHue;
        o.rainbow = c.elyRainbow;
        o.rainbowSpeed = c.elyRainbowSpeed;
        o.trim = c.elyTrim;
        o.innerAuto = c.elyInnerSame;
        o.innerColor = 0x101014;
        o.glow = c.elyGlowStrength;
        o.glowOn = c.elyGlow;
    }

    private static boolean ensureTexture(CapeDesign d, ModConfig c) {
        long now = Util.getMillis();
        if (texBroken) {
            // gecici hata: biraz sonra yeniden dene, beklerken vanilla elytra
            if (texFails >= 3 || now - texBrokenAt < 5000L) return false;
            texBroken = false;
            dropTextures();
        }
        fillOptions(c, OPTS);
        boolean anim = (d.animated() && c.elySpeed > 0f) || OPTS.rainbow;
        boolean need = baseTex == null || !d.id().equals(texDesign) || !OPTS.same(texOpts) || (anim && now - texAt >= 50L);
        if (!need) return true;
        try {
            if (baseTex == null) {
                baseTex = new DynamicTexture(ElytraTexture.TEX_W, ElytraTexture.TEX_H, false);
                glowTex = new DynamicTexture(ElytraTexture.TEX_W, ElytraTexture.TEX_H, false);
                Minecraft.getInstance().getTextureManager().register(TEX, baseTex);
                Minecraft.getInstance().getTextureManager().register(GLOW_TEX, glowTex);
            }
            float t = OPTS.rainbow && !(d.animated() && c.elySpeed > 0f) ? clock + (now % 3_600_000L) / 1000f : clock;
            GEN.paint(d, OPTS, t, BASE, GLOW);
            NativeImage bi = baseTex.getPixels(), gi = glowTex.getPixels();
            if (bi == null || gi == null) {
                broke(now);
                return false;
            }
            boolean any = false;
            int w = ElytraTexture.TEX_W;
            for (int y = 0; y < ElytraTexture.TEX_H; y++) {
                for (int x = 0; x < w; x++) {
                    int i = y * w + x;
                    bi.setPixel(x, y, BASE[i]);
                    int g = GLOW[i];
                    gi.setPixel(x, y, g == 0 ? 0xFF000000 : g);
                    if ((g & 0xFFFFFF) != 0) any = true;
                }
            }
            baseTex.upload();
            glowTex.upload();
            glowAny = any;
            texDesign = d.id();
            texOpts = OPTS.copy();
            texAt = now;
            return true;
        } catch (Throwable t) {
            broke(now);
            return false;
        }
    }

    private static void broke(long now) {
        texBroken = true;
        texBrokenAt = now;
        texFails++;
    }

    private static void dropTextures() {
        try {
            if (baseTex != null) Minecraft.getInstance().getTextureManager().release(TEX);
            if (glowTex != null) Minecraft.getInstance().getTextureManager().release(GLOW_TEX);
        } catch (Throwable ignored) {
        }
        baseTex = null;
        glowTex = null;
        texDesign = null;
    }

    /** Doku yeniden uretilsin (ayar ekranindan). */
    public static void invalidate() {
        texDesign = null;
        texFails = 0;
    }

    // ------------------------------------------------------------------ parcacik

    private static void capture(String designId, ElytraModel model, PoseStack ps) {
        Matrix4f m0 = ps.last().pose();
        // GUI'de (envanter) model matrisi cok buyuk olceklidir: orada parcacik yok
        float sx = (float) Math.sqrt(m0.m00() * m0.m00() + m0.m01() * m0.m01() + m0.m02() * m0.m02());
        if (sx < 0.2f || sx > 4f) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameRenderer == null) return;
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        if (cam == null) return;
        int n = 0;
        try {
            ModelPart root = model.root();
            for (int w = 0; w < 2; w++) {
                ps.pushPose();
                try {
                    root.translateAndRotate(ps);
                    root.getChild(w == 0 ? "left_wing" : "right_wing").translateAndRotate(ps);
                    Matrix4f m = ps.last().pose();
                    for (float ex : EDGE_X) {
                        // alt kenar: y = 20, kalinligin ortasi z = 1 (birim 1/16 blok)
                        Vector3f p = m.transformPosition((w == 0 ? ex : -ex) / 16f, 20f / 16f, 1f / 16f, TP);
                        EMIT[n * 3] = cam.x + p.x();
                        EMIT[n * 3 + 1] = cam.y + p.y();
                        EMIT[n * 3 + 2] = cam.z + p.z();
                        n++;
                    }
                } finally {
                    ps.popPose();
                }
            }
        } catch (Throwable t) {
            n = 0;
        }
        emitN = n;
        emitTick = ticks;
        emitDesign = designId;
    }

    /** Istemci tick'i: yakin zamanda cizilen elytranin alt uclarindan parcacik (suzulurken daha cok). */
    public static void tick(Minecraft mc) {
        ticks++;
        if (emitN == 0 || mc.level == null || mc.player == null || ticks - emitTick > 3) return;
        ModConfig c = cfg();
        CapeDesign d = ElytraDesigns.get(emitDesign);
        if (c == null || d == null || !c.elyParticles || d.particle() == null) return;
        ParticleStatus status = mc.options.particles().get();
        if (status == ParticleStatus.MINIMAL) return;
        float rate = d.rate() * c.elyParticleRate * (status == ParticleStatus.DECREASED ? 0.5f : 1f) * (mc.player.isFallFlying() ? 2.5f : 1f);
        emitAcc += rate / 20f;
        RandomSource r = mc.level.random;
        int guard = 0;
        while (emitAcc >= 1f && guard++ < 5) {
            emitAcc -= 1f;
            ParticleOptions o = particle(d.particle());
            if (o == null) {
                emitAcc = 0f;
                break;
            }
            int i = r.nextInt(emitN);
            mc.level.addParticle(o, EMIT[i * 3] + (r.nextDouble() - 0.5) * 0.08, EMIT[i * 3 + 1] + (r.nextDouble() - 0.5) * 0.08,
                    EMIT[i * 3 + 2] + (r.nextDouble() - 0.5) * 0.08, (r.nextDouble() - 0.5) * 0.01, -0.01, (r.nextDouble() - 0.5) * 0.01);
        }
        if (emitAcc > 3f) emitAcc = 3f;
    }

    public static void onLevelChange() {
        emitN = 0;
        emitAcc = 0f;
    }

    private static ParticleOptions particle(String id) {
        ParticleOptions cached = PARTICLES.get(id);
        if (cached != null) return cached;
        ParticleOptions o = null;
        if (id.startsWith("dust:")) {
            try {
                o = new DustParticleOptions(Integer.parseInt(id.substring(5), 16) & 0xFFFFFF, 0.8f);
            } catch (NumberFormatException ignored) {
            }
        } else {
            ParticleType<?> t = BuiltInRegistries.PARTICLE_TYPE.getValue(ResourceLocation.withDefaultNamespace(id));
            if (t instanceof SimpleParticleType s) o = s;
        }
        if (o != null) PARTICLES.put(id, o);
        return o;
    }
}
