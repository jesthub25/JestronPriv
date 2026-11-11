package com.jestron.jestronpriv.client.cosmetic;

import com.jestron.jestronpriv.hats.HatCtx;
import com.jestron.jestronpriv.hats.HatMesh;
import com.jestron.jestronpriv.hats.HatSettings;
import com.jestron.jestronpriv.hats.HatStyle;
import com.jestron.jestronpriv.hats.HatStyles;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Sapkalar: oyuncu modelinin kafasina bagli katman (sadece kendi oyuncunda, ayarlardan). Envanter ekranindaki
 * modelde de gorunur.
 */
public final class Hats {

    public static final ResourceLocation TEX = new ResourceLocation("jestronpriv", "textures/hat/hat.png");

    private static final HatMesh MESH = new HatMesh();
    private static final HatCtx CTX = new HatCtx();
    // dortgen tamponu: katman, 12 konum, 8 uv, 4 renk, 3 normal
    private static final int STRIDE = 28;
    private static float[] buf = new float[STRIDE * 1024];
    private static int count;
    private static final Vector3f TMP = new Vector3f(), TN = new Vector3f(), TN2 = new Vector3f();
    private static final Map<String, Float> TOP = new HashMap<>();
    private static final Map<String, ParticleOptions> PARTICLES = new HashMap<>();

    /** Parcacik cikis noktalari (dunya), varlik basina. */
    private static final class Emit {
        final double[] pos = new double[HatMesh.MAX_EMIT * 3];
        int n;
        long tick;
        String style;
        float acc;
    }

    private static final Map<Integer, Emit> EMITS = new HashMap<>();
    private static long ticks;

    private static final HatMesh.Sink SINK = (layer, xyz, uv, argb, nx, ny, nz) -> {
        if ((count + 1) * STRIDE > buf.length) {
            if (buf.length >= STRIDE * HatMesh.MAX_QUADS) return;
            float[] nb = new float[Math.min(buf.length * 2, STRIDE * HatMesh.MAX_QUADS)];
            System.arraycopy(buf, 0, nb, 0, buf.length);
            buf = nb;
        }
        int o = count * STRIDE;
        buf[o] = layer;
        System.arraycopy(xyz, 0, buf, o + 1, 12);
        System.arraycopy(uv, 0, buf, o + 13, 8);
        for (int i = 0; i < 4; i++) buf[o + 21 + i] = Float.intBitsToFloat(argb[i]);
        buf[o + 25] = nx;
        buf[o + 26] = ny;
        buf[o + 27] = nz;
        count++;
    };

    private Hats() {
    }

    /** Oyuncu modeline eklenen katman. */
    public static final class Layer extends RenderLayer<PlayerRenderState, PlayerModel> {
        public Layer(RenderLayerParent<PlayerRenderState, PlayerModel> parent) {
            super(parent);
        }

        @Override
        public void render(PoseStack ps, MultiBufferSource buffers, int light, PlayerRenderState state, float yRot, float xRot) {
            try {
                Hats.render(ps, buffers, light, state, getParentModel());
            } catch (Throwable ignored) {
                // sapka cizimi asla oyunu bozmasin
            }
        }
    }

    /** Bu oyuncunun gorunen sapkasi (yoksa null). */
    public static HatSettings settingsFor(PlayerRenderState st) {
        if (st == null || st.isInvisible || st.isSpectator) return null;
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && st.id == mc.player.getId() ? LocalLook.hat() : null;
    }

    /** Sapkanin kafa ustunden yuksekligi (blok, olcek dahil); isim etiketini kaldirmak icin. */
    public static float topFor(HatSettings h, boolean helmet) {
        if (h == null) return 0f;
        HatStyle style = HatStyles.get(h.id);
        if (style == null || (helmet && h.hideHelmet)) return 0f;
        Float top = TOP.get(style.id());
        if (top == null) {
            int saved = count;
            float mx = 0f;
            try {
                CTX.set(0f, 0f, style.c1(), style.c2(), style.c3(), false);
                MESH.begin((layer, xyz, uv, argb, nx, ny, nz) -> {
                }, true);
                style.builder().build(MESH, CTX);
                mx = MESH.hasBounds() ? Math.max(0f, MESH.maxY) : 0f;
            } catch (Throwable ignored) {
            }
            count = saved;
            top = mx;
            TOP.put(style.id(), top);
        }
        return (top + 0.03f) * h.scale + h.height + (helmet ? 0.08f : 0f);
    }

    static void render(PoseStack ps, MultiBufferSource buffers, int light, PlayerRenderState st, PlayerModel model) {
        HatSettings h = settingsFor(st);
        if (h == null || model == null) return;
        HatStyle style = HatStyles.get(h.id);
        if (style == null) return;
        boolean helmet = st.headEquipment != null && !st.headEquipment.isEmpty();
        if (helmet && h.hideHelmet) return;
        float lift = helmet ? 0.08f : 0f;
        ps.pushPose();
        try {
            model.head.translateAndRotate(ps);
            ps.translate(0f, -0.53f - lift - h.height, 0f);
            if (h.tilt != 0f) ps.mulPose(com.mojang.math.Axis.XP.rotationDegrees(h.tilt));
            float s = Math.max(0.5f, Math.min(2f, h.scale));
            ps.scale(s, s, s);
            float t = st.ageInTicks / 20f * Math.max(0f, Math.min(3f, h.speed));
            float energy = Math.max(0f, Math.min(1f, st.walkAnimationSpeed));
            int c1 = h.customColor ? h.color : style.c1();
            int c2 = h.customColor ? HatCtx.mix(h.color, 0xFFFFFF, 0.45f) : style.c2();
            CTX.set(t, energy, c1, c2, style.c3(), h.rainbow);
            count = 0;
            MESH.begin(SINK, h.glow);
            style.builder().build(MESH, CTX);
            PoseStack.Pose pose = ps.last();
            if (has(HatMesh.BASE)) flush(buffers.getBuffer(RenderType.entityCutoutNoCull(TEX)), pose, HatMesh.BASE, light, false);
            if (has(HatMesh.TRANS)) flush(buffers.getBuffer(RenderType.entityTranslucent(TEX)), pose, HatMesh.TRANS, light, false);
            if (has(HatMesh.GLOW)) flush(buffers.getBuffer(RenderType.eyes(TEX)), pose, HatMesh.GLOW, LightTexture.FULL_BRIGHT, true);
            if (h.particles && style.particle() != null && style.rate() > 0) capture(st.id, style.id(), pose.pose());
        } finally {
            ps.popPose();
        }
    }

    private static boolean has(int layer) {
        for (int i = 0; i < count; i++) if (buf[i * STRIDE] == layer) return true;
        return false;
    }

    private static void flush(VertexConsumer vc, PoseStack.Pose pose, int layer, int light, boolean twoSided) {
        Matrix4f m = pose.pose();
        for (int i = 0; i < count; i++) {
            int o = i * STRIDE;
            if (buf[o] != layer) continue;
            Vector3f n = pose.transformNormal(TN.set(buf[o + 25], buf[o + 26], buf[o + 27]), TN2);
            float nx = n.x(), ny = n.y(), nz = n.z();
            for (int v = 0; v < 4; v++) vertex(vc, m, o, v, light, nx, ny, nz);
            if (twoSided) for (int v = 3; v >= 0; v--) vertex(vc, m, o, v, light, -nx, -ny, -nz);
        }
    }

    private static void vertex(VertexConsumer vc, Matrix4f m, int o, int v, int light, float nx, float ny, float nz) {
        Vector3f p = m.transformPosition(buf[o + 1 + v * 3], buf[o + 2 + v * 3], buf[o + 3 + v * 3], TMP);
        vc.addVertex(p.x(), p.y(), p.z(), Float.floatToRawIntBits(buf[o + 21 + v]), buf[o + 13 + v * 2], buf[o + 14 + v * 2],
                OverlayTexture.NO_OVERLAY, light, nx, ny, nz);
    }

    // ------------------------------------------------------------------ parcacik

    private static void capture(int entityId, String styleId, Matrix4f m) {
        if (MESH.emitCount == 0) return;
        // GUI'de (envanter) model matrisi cok buyuk olceklidir: orada parcacik yok
        float sx = (float) Math.sqrt(m.m00() * m.m00() + m.m01() * m.m01() + m.m02() * m.m02());
        if (sx < 0.2f || sx > 4f) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameRenderer == null) return;
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        if (cam == null) return;
        Emit e = EMITS.computeIfAbsent(entityId, k -> new Emit());
        int n = 0;
        for (int i = 0; i < MESH.emitCount; i++) {
            // cikis noktasi sapka uzayinda (y yukari) -> model uzayi (y asagi)
            Vector3f p = m.transformPosition(MESH.emit[i * 3], -MESH.emit[i * 3 + 1], MESH.emit[i * 3 + 2], TMP);
            e.pos[n * 3] = cam.x + p.x();
            e.pos[n * 3 + 1] = cam.y + p.y();
            e.pos[n * 3 + 2] = cam.z + p.z();
            n++;
        }
        e.n = n;
        e.tick = ticks;
        e.style = styleId;
    }

    /** Istemci tick'i (duraklatilmamisken): yakin zamanda cizilen sapkalardan parcacik. */
    public static void tick(Minecraft mc) {
        ticks++;
        if (EMITS.isEmpty() || mc.level == null) return;
        ParticleStatus status = mc.options.particles().get();
        Iterator<Map.Entry<Integer, Emit>> it = EMITS.entrySet().iterator();
        RandomSource r = mc.level.random;
        while (it.hasNext()) {
            Map.Entry<Integer, Emit> me = it.next();
            Emit e = me.getValue();
            if (ticks - e.tick > 3) {
                if (ticks - e.tick > 100) it.remove();
                continue;
            }
            if (status == ParticleStatus.MINIMAL || e.n == 0) continue;
            HatStyle st = HatStyles.get(e.style);
            if (st == null || st.particle() == null) continue;
            float rate = st.rate() * (status == ParticleStatus.DECREASED ? 0.5f : 1f);
            e.acc += rate / 20f;
            int guard = 0;
            while (e.acc >= 1f && guard++ < 4) {
                e.acc -= 1f;
                ParticleOptions o = particle(st.particle(), r);
                if (o == null) {
                    e.acc = 0f;
                    break;
                }
                int i = r.nextInt(e.n);
                mc.level.addParticle(o, e.pos[i * 3] + (r.nextDouble() - 0.5) * 0.08, e.pos[i * 3 + 1] + (r.nextDouble() - 0.5) * 0.08,
                        e.pos[i * 3 + 2] + (r.nextDouble() - 0.5) * 0.08, (r.nextDouble() - 0.5) * 0.01, 0.01, (r.nextDouble() - 0.5) * 0.01);
            }
            if (e.acc > 3f) e.acc = 3f;
        }
    }

    public static void onLevelChange() {
        EMITS.clear();
    }

    private static ParticleOptions particle(String id, RandomSource r) {
        if ("rainbow".equals(id)) return new DustParticleOptions(HatCtx.hsv((ticks % 120) / 120f + r.nextFloat() * 0.1f, 0.85f, 1f), 0.8f);
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
}
