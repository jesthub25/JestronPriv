package com.jestron.jestronpriv.client.cosmetic;

import com.jestron.jestronpriv.cape.CapeCloth;
import com.jestron.jestronpriv.cape.CapeDesign;
import com.jestron.jestronpriv.cape.CapeDesigns;
import com.jestron.jestronpriv.cape.CapeTexture;
import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
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
 * Pelerinler: sadece kendi oyuncunda (F5, envanter, menu onizlemesi). Vanilla pelerini yerine cizilir.
 * <p>
 * Kumas her karede {@link CapeCloth} ile cozulur (8x14 izgara, ~270 dortgen); doku {@link CapeTexture} ile uretilir
 * ve sadece desen / ayar degisince ya da animasyonlu desende saniyede en cok 20 kez yenilenir. Elytra giyiliyken
 * (vanilla gibi) ve istege bagli olarak JestronPriv kanatlari gorunurken gizlenir.
 */
public final class Capes {

    public static final ResourceLocation TEX = new ResourceLocation("jestronpriv", "dynamic/cape");
    public static final ResourceLocation GLOW_TEX = new ResourceLocation("jestronpriv", "dynamic/cape_glow");

    private static final CapeCloth CLOTH = new CapeCloth();
    private static final CapeCloth.Input IN = new CapeCloth.Input();
    private static final CapeTexture GEN = new CapeTexture();
    private static final CapeTexture.Options OPTS = new CapeTexture.Options();
    private static final int[] BASE = new int[CapeTexture.TEX_W * CapeTexture.TEX_H];
    private static final int[] GLOW = new int[CapeTexture.TEX_W * CapeTexture.TEX_H];
    private static final Vector3f TP = new Vector3f(), TN = new Vector3f(), TN2 = new Vector3f();
    private static final float[] OUT = new float[(CapeCloth.ROWS + 1) * (CapeCloth.COLS + 1) * 3];
    private static final float[] INN = new float[(CapeCloth.ROWS + 1) * (CapeCloth.COLS + 1) * 3];

    private static DynamicTexture baseTex, glowTex;
    private static boolean texBroken, glowAny;
    /** Doku hatasi: kalici kapatmak yerine biraz sonra yeniden dene (kaynak yeniden yukleme gibi gecici durumlar). */
    private static long texBrokenAt;
    private static int texFails;
    private static String texDesign;
    private static CapeTexture.Options texOpts;
    private static long texAt, clockAt, lastDrawn;
    private static float patternClock, clothClock;

    // parcacik cikis noktalari (dunya): alt kenar
    private static final double[] EMIT = new double[(CapeCloth.COLS + 1) * 3];
    private static int emitN;
    private static long emitTick, ticks;
    private static float emitAcc;
    private static String emitDesign;
    private static final Map<String, ParticleOptions> PARTICLES = new HashMap<>();

    private Capes() {
    }

    /** Oyuncu modeline eklenen katman. */
    public static final class Layer extends RenderLayer<PlayerRenderState, PlayerModel> {
        public Layer(RenderLayerParent<PlayerRenderState, PlayerModel> parent) {
            super(parent);
        }

        @Override
        public void render(PoseStack ps, MultiBufferSource buffers, int light, PlayerRenderState state, float yRot, float xRot) {
            try {
                Capes.render(ps, buffers, light, state, getParentModel());
            } catch (Throwable ignored) {
                // pelerin cizimi asla oyunu bozmasin
            }
        }
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    public static CapeDesign selected() {
        ModConfig c = cfg();
        return c == null ? null : CapeDesigns.get(c.cape);
    }

    /** Bu oyuncu icin cizilecek desen (yerel oyuncu, gorunur, secili desen varsa). */
    public static CapeDesign designFor(PlayerRenderState st) {
        if (st == null || st.isInvisible || st.isSpectator) return null;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || st.id != mc.player.getId()) return null;
        return selected();
    }

    /** Vanilla pelerin gizlensin mi (bizimki cizilecek). */
    public static boolean replacesVanilla(PlayerRenderState st) {
        return designFor(st) != null;
    }

    private static boolean glider(ItemStack chest) {
        return chest != null && !chest.isEmpty() && chest.has(DataComponents.GLIDER);
    }

    private static void advanceClocks(ModConfig c) {
        long now = Util.getMillis();
        if (clockAt == 0L) clockAt = now;
        float dt = Math.min(0.25f, Math.max(0f, (now - clockAt) / 1000f));
        clockAt = now;
        patternClock = (patternClock + dt * c.capeSpeed) % 3600f;
        clothClock = (clothClock + dt * Math.max(0.15f, c.capeSpeed)) % 3600f;
    }

    static void render(PoseStack ps, MultiBufferSource buffers, int light, PlayerRenderState st, PlayerModel model) {
        CapeDesign d = designFor(st);
        ModConfig c = cfg();
        if (d == null || c == null || model == null) return;
        if (glider(st.chestEquipment) || Elytras.previewActive()) return;
        if (c.capeHideWings && Wings.styleFor(st) != null) return;
        advanceClocks(c);
        if (!ensureTexture(d, c)) return;
        lastDrawn = Util.getMillis();
        ps.pushPose();
        try {
            // vanilla gibi: gogus zirhi varken pelerin biraz yukari ve geriye
            if (st.chestEquipment != null && !st.chestEquipment.isEmpty()) ps.translate(0.0F, -0.053125F, 0.06875F);
            model.body.translateAndRotate(ps);
            IN.lean = st.capeLean;
            IN.flap = st.capeFlap;
            IN.lean2 = st.capeLean2;
            IN.time = clothClock;
            IN.wind = c.capeWind;
            IN.physics = c.capePhysics;
            IN.length = c.capeLength;
            IN.width = c.capeWidth;
            CLOTH.solve(IN);
            offsets();
            PoseStack.Pose pose = ps.last();
            emitBase(buffers.getBuffer(RenderType.entityCutoutNoCull(TEX)), pose, light);
            if (glowAny && c.capeGlow) emitGlow(buffers.getBuffer(RenderType.eyes(GLOW_TEX)), pose);
            if (c.capeParticles && d.particle() != null && d.rate() > 0f) capture(d.id(), pose.pose());
        } finally {
            ps.popPose();
        }
    }

    // ------------------------------------------------------------------ doku

    private static void fillOptions(ModConfig c, CapeTexture.Options o) {
        o.hue = c.capeHue;
        o.rainbow = c.capeRainbow;
        o.rainbowSpeed = c.capeRainbowSpeed;
        o.trim = c.capeTrim;
        o.innerAuto = c.capeInnerAuto;
        o.innerColor = c.capeInnerColor;
        o.glow = c.capeGlowStrength;
        o.glowOn = c.capeGlow;
    }

    private static boolean ensureTexture(CapeDesign d, ModConfig c) {
        long now = Util.getMillis();
        if (texBroken) {
            if (texFails >= 3 || now - texBrokenAt < 5000L) return false;
            texBroken = false;
            dropTextures();
        }
        fillOptions(c, OPTS);
        boolean anim = (d.animated() && c.capeSpeed > 0f) || OPTS.rainbow;
        boolean need = baseTex == null || !d.id().equals(texDesign) || !OPTS.same(texOpts) || (anim && now - texAt >= 50L);
        if (!need) return true;
        try {
            if (baseTex == null) {
                baseTex = new DynamicTexture(CapeTexture.TEX_W, CapeTexture.TEX_H, false);
                glowTex = new DynamicTexture(CapeTexture.TEX_W, CapeTexture.TEX_H, false);
                Minecraft.getInstance().getTextureManager().register(TEX, baseTex);
                Minecraft.getInstance().getTextureManager().register(GLOW_TEX, glowTex);
            }
            float t = OPTS.rainbow && !(d.animated() && c.capeSpeed > 0f) ? patternClock + (now % 3_600_000L) / 1000f : patternClock;
            GEN.paint(d, OPTS, t, BASE, GLOW);
            NativeImage bi = baseTex.getPixels(), gi = glowTex.getPixels();
            if (bi == null || gi == null) {
                broke(now);
                return false;
            }
            boolean any = false;
            int w = CapeTexture.TEX_W;
            for (int y = 0; y < CapeTexture.TEX_H; y++) {
                for (int x = 0; x < w; x++) {
                    int i = y * w + x;
                    bi.setPixel(x, y, BASE[i] == 0 ? 0xFF000000 : BASE[i]);
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
    }

    // ------------------------------------------------------------------ geometri

    private static void offsets() {
        float h = CapeCloth.HALF_T;
        float[] p = CLOTH.pos, n = CLOTH.nrm;
        for (int k = 0; k < p.length; k++) {
            OUT[k] = p[k] + n[k] * h;
            INN[k] = p[k] - n[k] * h;
        }
    }

    private static float uOuter(int j) {
        return (CapeTexture.W * (CapeCloth.COLS - j) / (float) CapeCloth.COLS) / CapeTexture.TEX_W;
    }

    private static float uInner(int j) {
        return (CapeTexture.INNER_X + CapeTexture.W * j / (float) CapeCloth.COLS) / CapeTexture.TEX_W;
    }

    private static float vRow(int i) {
        return (float) i / CapeCloth.ROWS;
    }

    private static void vert(VertexConsumer vc, PoseStack.Pose pose, float[] src, int k, float u, float v, float nx, float ny, float nz, int light,
                             int color) {
        Matrix4f m = pose.pose();
        Vector3f p = m.transformPosition(src[k], src[k + 1], src[k + 2], TP);
        Vector3f n = pose.transformNormal(TN.set(nx, ny, nz), TN2);
        vc.addVertex(p.x(), p.y(), p.z(), color, u, v, OverlayTexture.NO_OVERLAY, light, n.x(), n.y(), n.z());
    }

    private static void emitBase(VertexConsumer vc, PoseStack.Pose pose, int light) {
        int R = CapeCloth.ROWS, C = CapeCloth.COLS;
        float[] n = CLOTH.nrm;
        int white = 0xFFFFFFFF;
        for (int i = 0; i < R; i++) {
            for (int j = 0; j < C; j++) {
                int a = CapeCloth.idx(i, j), b = CapeCloth.idx(i, j + 1), cc = CapeCloth.idx(i + 1, j + 1), d = CapeCloth.idx(i + 1, j);
                // dis yuz (normal disari)
                vert(vc, pose, OUT, a, uOuter(j), vRow(i), n[a], n[a + 1], n[a + 2], light, white);
                vert(vc, pose, OUT, b, uOuter(j + 1), vRow(i), n[b], n[b + 1], n[b + 2], light, white);
                vert(vc, pose, OUT, cc, uOuter(j + 1), vRow(i + 1), n[cc], n[cc + 1], n[cc + 2], light, white);
                vert(vc, pose, OUT, d, uOuter(j), vRow(i + 1), n[d], n[d + 1], n[d + 2], light, white);
                // ic yuz (normal sirta)
                vert(vc, pose, INN, d, uInner(j), vRow(i + 1), -n[d], -n[d + 1], -n[d + 2], light, white);
                vert(vc, pose, INN, cc, uInner(j + 1), vRow(i + 1), -n[cc], -n[cc + 1], -n[cc + 2], light, white);
                vert(vc, pose, INN, b, uInner(j + 1), vRow(i), -n[b], -n[b + 1], -n[b + 2], light, white);
                vert(vc, pose, INN, a, uInner(j), vRow(i), -n[a], -n[a + 1], -n[a + 2], light, white);
            }
        }
        float e0 = (float) CapeTexture.EDGE_X / CapeTexture.TEX_W, e1 = (float) (CapeTexture.EDGE_X + CapeTexture.EDGE_W) / CapeTexture.TEX_W;
        // yan kenarlar
        for (int side = 0; side < 2; side++) {
            int j = side == 0 ? 0 : C, jn = side == 0 ? 1 : C - 1;
            for (int i = 0; i < R; i++) {
                int a = CapeCloth.idx(i, j), d = CapeCloth.idx(i + 1, j);
                float[] sn = sideNormal(CLOTH.pos, a, CapeCloth.idx(i, jn));
                vert(vc, pose, OUT, a, e0, vRow(i), sn[0], sn[1], sn[2], light, white);
                vert(vc, pose, OUT, d, e0, vRow(i + 1), sn[0], sn[1], sn[2], light, white);
                vert(vc, pose, INN, d, e1, vRow(i + 1), sn[0], sn[1], sn[2], light, white);
                vert(vc, pose, INN, a, e1, vRow(i), sn[0], sn[1], sn[2], light, white);
            }
        }
        // alt ve ust kenar
        for (int edge = 0; edge < 2; edge++) {
            int i = edge == 0 ? R : 0, in = edge == 0 ? R - 1 : 1;
            for (int j = 0; j < C; j++) {
                int a = CapeCloth.idx(i, j), b = CapeCloth.idx(i, j + 1);
                float[] sn = sideNormal(CLOTH.pos, a, CapeCloth.idx(in, j));
                float va = (float) j / C, vb = (float) (j + 1) / C;
                vert(vc, pose, OUT, a, e0, va, sn[0], sn[1], sn[2], light, white);
                vert(vc, pose, OUT, b, e0, vb, sn[0], sn[1], sn[2], light, white);
                vert(vc, pose, INN, b, e1, vb, sn[0], sn[1], sn[2], light, white);
                vert(vc, pose, INN, a, e1, va, sn[0], sn[1], sn[2], light, white);
            }
        }
    }

    private static final float[] SN = new float[3];

    private static float[] sideNormal(float[] p, int at, int inner) {
        float x = p[at] - p[inner], y = p[at + 1] - p[inner + 1], z = p[at + 2] - p[inner + 2];
        float l = (float) Math.sqrt(x * x + y * y + z * z);
        if (l < 1e-8f || !Float.isFinite(l)) {
            SN[0] = 0f;
            SN[1] = 1f;
            SN[2] = 0f;
        } else {
            SN[0] = x / l;
            SN[1] = y / l;
            SN[2] = z / l;
        }
        return SN;
    }

    private static void emitGlow(VertexConsumer vc, PoseStack.Pose pose) {
        int R = CapeCloth.ROWS, C = CapeCloth.COLS;
        float[] n = CLOTH.nrm;
        int light = LightTexture.FULL_BRIGHT, white = 0xFFFFFFFF;
        for (int i = 0; i < R; i++) {
            for (int j = 0; j < C; j++) {
                int a = CapeCloth.idx(i, j), b = CapeCloth.idx(i, j + 1), cc = CapeCloth.idx(i + 1, j + 1), d = CapeCloth.idx(i + 1, j);
                // isilti katmani arka yuzu atar: iki sarimla yazilir, on taraftan bakinca ic yuz derinlikle onu gizler
                vert(vc, pose, OUT, a, uOuter(j), vRow(i), n[a], n[a + 1], n[a + 2], light, white);
                vert(vc, pose, OUT, b, uOuter(j + 1), vRow(i), n[b], n[b + 1], n[b + 2], light, white);
                vert(vc, pose, OUT, cc, uOuter(j + 1), vRow(i + 1), n[cc], n[cc + 1], n[cc + 2], light, white);
                vert(vc, pose, OUT, d, uOuter(j), vRow(i + 1), n[d], n[d + 1], n[d + 2], light, white);
                vert(vc, pose, OUT, d, uOuter(j), vRow(i + 1), n[d], n[d + 1], n[d + 2], light, white);
                vert(vc, pose, OUT, cc, uOuter(j + 1), vRow(i + 1), n[cc], n[cc + 1], n[cc + 2], light, white);
                vert(vc, pose, OUT, b, uOuter(j + 1), vRow(i), n[b], n[b + 1], n[b + 2], light, white);
                vert(vc, pose, OUT, a, uOuter(j), vRow(i), n[a], n[a + 1], n[a + 2], light, white);
            }
        }
    }

    // ------------------------------------------------------------------ parcacik

    private static void capture(String designId, Matrix4f m) {
        // GUI'de (envanter) model matrisi cok buyuk olceklidir: orada parcacik yok
        float sx = (float) Math.sqrt(m.m00() * m.m00() + m.m01() * m.m01() + m.m02() * m.m02());
        if (sx < 0.2f || sx > 4f) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameRenderer == null) return;
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        if (cam == null) return;
        int n = 0;
        for (int j = 0; j <= CapeCloth.COLS; j++) {
            int k = CapeCloth.idx(CapeCloth.ROWS, j);
            Vector3f p = m.transformPosition(OUT[k], OUT[k + 1], OUT[k + 2], TP);
            EMIT[n * 3] = cam.x + p.x();
            EMIT[n * 3 + 1] = cam.y + p.y();
            EMIT[n * 3 + 2] = cam.z + p.z();
            n++;
        }
        emitN = n;
        emitTick = ticks;
        emitDesign = designId;
    }

    /** Istemci tick'i: yakin zamanda cizilen pelerinin alt kenarindan parcacik. */
    public static void tick(Minecraft mc) {
        ticks++;
        if (emitN == 0 || mc.level == null || ticks - emitTick > 3) return;
        ModConfig c = cfg();
        CapeDesign d = CapeDesigns.get(emitDesign);
        if (c == null || d == null || !c.capeParticles || d.particle() == null) return;
        ParticleStatus status = mc.options.particles().get();
        if (status == ParticleStatus.MINIMAL) return;
        float rate = d.rate() * c.capeParticleRate * (status == ParticleStatus.DECREASED ? 0.5f : 1f);
        emitAcc += rate / 20f;
        RandomSource r = mc.level.random;
        int guard = 0;
        while (emitAcc >= 1f && guard++ < 4) {
            emitAcc -= 1f;
            ParticleOptions o = particle(d.particle(), r);
            if (o == null) {
                emitAcc = 0f;
                break;
            }
            int i = r.nextInt(emitN);
            mc.level.addParticle(o, EMIT[i * 3] + (r.nextDouble() - 0.5) * 0.06, EMIT[i * 3 + 1] + (r.nextDouble() - 0.5) * 0.06,
                    EMIT[i * 3 + 2] + (r.nextDouble() - 0.5) * 0.06, (r.nextDouble() - 0.5) * 0.01, -0.005, (r.nextDouble() - 0.5) * 0.01);
        }
        if (emitAcc > 3f) emitAcc = 3f;
    }

    public static void onLevelChange() {
        emitN = 0;
        emitAcc = 0f;
    }

    private static ParticleOptions particle(String id, RandomSource r) {
        ParticleOptions cached = PARTICLES.get(id);
        if (cached != null) return cached;
        ParticleOptions o = null;
        if (id.startsWith("dust:")) {
            try {
                o = new DustParticleOptions(Integer.parseInt(id.substring(5), 16) & 0xFFFFFF, 0.8f);
            } catch (NumberFormatException ignored) {
            }
        } else {
            ParticleType<?> t = BuiltInRegistries.PARTICLE_TYPE.get(new ResourceLocation(id));
            if (t instanceof SimpleParticleType s) o = s;
        }
        if (o != null) PARTICLES.put(id, o);
        return o;
    }

    /** Pelerin yakin zamanda cizildi mi (menu onizlemesi icin bilgi). */
    public static boolean recentlyDrawn() {
        return Util.getMillis() - lastDrawn < 1000L;
    }
}
