package com.jestron.jestronpriv.client.cosmetic;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.pvp.Palette;
import com.jestron.jestronpriv.client.pvp.Sfx;
import com.jestron.jestronpriv.pet.PetBrain;
import com.jestron.jestronpriv.pet.PetKind;
import com.jestron.jestronpriv.pet.PetMesh;
import com.jestron.jestronpriv.pet.PetMode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.ParticleStatus;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.Map;

/**
 * Evcil yoldas (sadece yerel oyuncu, sadece istemci). Geometri {@link PetMesh}, hareket {@link PetBrain}; burada
 * dunyaya cizilir: isikli katman (bulundugu yerin isigi), parlak katman ve kameraya donen isik haleleri.
 */
public final class Pets {

    private static final ResourceLocation TEX = new ResourceLocation("jestronpriv", "textures/pet/pet.png");
    private static final float WU = 0.25f, WV = 0.5f;          // beyaz bolgenin ortasi
    private static final PetBrain BRAIN = new PetBrain();
    private static final PetMesh MESH = new PetMesh();
    private static final Map<PetKind, ParticleOptions> PARTICLES = new EnumMap<>(PetKind.class);

    // kare tamponu: dortgen basina 12 konum + renk + 3 normal + katman/iki yuz
    private static final int STRIDE = 18;
    private static float[] buf = new float[STRIDE * 512];
    private static int count;
    private static final float[] HALO = new float[5 * 32];
    private static int haloCount;

    private static long lastNanos, renderTick = -100, ticks;
    private static PetKind lastKind;
    private static float particleAcc;
    private static final Vector3f TMP = new Vector3f(), TN = new Vector3f(), TN2 = new Vector3f();
    private static final float[] HX = new float[4], HY = new float[4], HZ = new float[4];
    private static final float[] HU = {0.5f, 1f, 1f, 0.5f}, HV = {1f, 1f, 0f, 0f};

    private static final PetMesh.Sink SINK = new PetMesh.Sink() {
        @Override
        public void quad(int layer, float[] xyz, int argb, float nx, float ny, float nz, boolean twoSided) {
            if ((count + 1) * STRIDE > buf.length) {
                if (buf.length >= STRIDE * 8192) return;
                float[] nb = new float[buf.length * 2];
                System.arraycopy(buf, 0, nb, 0, buf.length);
                buf = nb;
            }
            int o = count * STRIDE;
            System.arraycopy(xyz, 0, buf, o, 12);
            buf[o + 12] = Float.intBitsToFloat(argb);
            buf[o + 13] = nx;
            buf[o + 14] = ny;
            buf[o + 15] = nz;
            buf[o + 16] = layer;
            buf[o + 17] = twoSided ? 1 : 0;
            count++;
        }

        @Override
        public void halo(float x, float y, float z, float size, int argb) {
            if (haloCount >= 32) return;
            int o = haloCount * 5;
            HALO[o] = x;
            HALO[o + 1] = y;
            HALO[o + 2] = z;
            HALO[o + 3] = size;
            HALO[o + 4] = Float.intBitsToFloat(argb);
            haloCount++;
        }
    };

    private Pets() {
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    public static PetKind selected() {
        ModConfig c = cfg();
        return c == null ? null : PetKind.parse(c.pet);
    }

    /** Vurus / oldurme tepkisi. */
    public static void react(boolean kill) {
        ModConfig c = cfg();
        if (c == null || !c.petReact || selected() == null || ticks - renderTick > 5) return;
        BRAIN.react(kill);
        if (kill) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;
            Sfx.at(new Sfx.Layer[]{Sfx.Layer.of("entity.allay.item_given", 0.55f, 1.35f), Sfx.Layer.of("block.amethyst_block.chime", 0.6f, 1.6f, 3)},
                    1f, BRAIN.x, BRAIN.y, BRAIN.z);
            if (c.petParticles && mc.options.particles().get() != ParticleStatus.MINIMAL) {
                RandomSource r = mc.level.random;
                ParticleOptions o = net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER;
                for (int i = 0; i < 8; i++) {
                    mc.level.addParticle(o, BRAIN.x + (r.nextDouble() - 0.5) * 0.5, BRAIN.y + (r.nextDouble() - 0.3) * 0.4,
                            BRAIN.z + (r.nextDouble() - 0.5) * 0.5, 0, 0.02, 0);
                }
            }
        }
    }

    // ------------------------------------------------------------------ cizim

    public static void render(WorldRenderContext ctx) {
        try {
            render0(ctx);
        } catch (Throwable t) {
            count = haloCount = 0;
        }
    }

    private static void render0(WorldRenderContext ctx) {
        long now = System.nanoTime();
        float dt = lastNanos == 0 ? 0.05f : (now - lastNanos) / 1e9f;
        lastNanos = now;
        ModConfig c = cfg();
        PetKind kind = selected();
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (c == null || kind == null || p == null || mc.level == null) return;
        if (kind != lastKind) {
            BRAIN.reset();
            lastKind = kind;
        }
        if (mc.isPaused()) dt = 0f;
        boolean hidden = p.isInvisible() || p.isSpectator() || !p.isAlive();
        float pt = ctx.tickCounter().getGameTimeDeltaPartialTick(true);
        Vec3 pos = p.getPosition(pt);
        boolean fp = mc.options.getCameraType().isFirstPerson() && mc.getCameraEntity() == p;
        float bodyYaw = rotLerp(pt, p.yBodyRotO, p.yBodyRot);
        float viewYaw = p.getViewYRot(pt), viewPitch = p.getViewXRot(pt);
        PetMode mode = PetMode.parse(c.petMode);
        BRAIN.update(dt, pos.x, pos.y, pos.z, fp ? viewYaw : bodyYaw, viewYaw, viewPitch, mode, c.petLeft, fp,
                p.isCrouching(), c.petFollow, c.petBob, kind);
        if (hidden || (fp && !c.petFirstPerson)) return;

        PoseStack ps = ctx.matrixStack();
        MultiBufferSource buffers = ctx.consumers();
        Camera cam = ctx.camera();
        if (ps == null || buffers == null || cam == null) return;
        Vec3 cp = cam.getPosition();
        double dx = BRAIN.x - cp.x, dy = BRAIN.y + BRAIN.hopOffset - cp.y, dz = BRAIN.z - cp.z;
        if (dx * dx + dy * dy + dz * dz < 0.3 * 0.3) return;   // kameranin icine girdi
        renderTick = ticks;

        // renkler
        int pc, sc;
        double t = now / 1e9;
        if (c.petRainbow) {
            pc = Palette.rainbow(t, 0f);
            sc = Palette.rainbow(t, 0.35f);
        } else if (c.petCustomColor) {
            pc = c.petColor & 0xFFFFFF;
            sc = PetMesh.mix(pc, 0xFFFFFF, 0.55f);
        } else {
            pc = kind.primary;
            sc = kind.secondary;
        }
        count = haloCount = 0;
        MESH.build(kind, BRAIN.anim, pc, sc, c.petGlow, SINK);

        int light = LevelRenderer.getLightColor(mc.level, BlockPos.containing(BRAIN.x, BRAIN.y, BRAIN.z));
        ps.pushPose();
        try {
            ps.translate(dx, dy, dz);
            ps.mulPose(com.mojang.math.Axis.YP.rotation((float) Math.toRadians(-BRAIN.yaw)));
            ps.mulPose(com.mojang.math.Axis.XP.rotation(BRAIN.tilt));
            ps.mulPose(com.mojang.math.Axis.ZP.rotation(BRAIN.bank));
            float s = Math.max(0.5f, Math.min(2f, c.petScale)) * 1.15f;
            ps.scale(s, s, s);
            PoseStack.Pose pose = ps.last();
            // tampon kaynagi ayni anda tek turu acik tutar: once isikli, sonra parlak (+ haleler)
            flush(buffers.getBuffer(RenderType.entityTranslucent(TEX)), pose, PetMesh.LIT, light, false);
            VertexConsumer glow = buffers.getBuffer(RenderType.eyes(TEX));
            flush(glow, pose, PetMesh.GLOW, LightTexture.FULL_BRIGHT, true);
            halos(glow, pose.pose(), cam);
        } finally {
            ps.popPose();
        }
    }

    private static void flush(VertexConsumer vc, PoseStack.Pose pose, int layer, int light, boolean cullsBack) {
        Matrix4f m = pose.pose();
        for (int i = 0; i < count; i++) {
            int o = i * STRIDE;
            if (buf[o + 16] != layer) continue;
            Vector3f n = pose.transformNormal(TN.set(buf[o + 13], buf[o + 14], buf[o + 15]), TN2);
            int col = Float.floatToRawIntBits(buf[o + 12]);
            for (int v = 0; v < 4; v++) vertex(vc, m, o, v, col, light, n.x(), n.y(), n.z());
            // ince yuzey ve arka yuz atan cizim: ters yonde de yaz
            if (cullsBack && buf[o + 17] > 0) for (int v = 3; v >= 0; v--) vertex(vc, m, o, v, col, light, -n.x(), -n.y(), -n.z());
        }
    }

    private static void vertex(VertexConsumer vc, Matrix4f m, int o, int v, int col, int light, float nx, float ny, float nz) {
        Vector3f p = m.transformPosition(buf[o + v * 3], buf[o + v * 3 + 1], buf[o + v * 3 + 2], TMP);
        vc.addVertex(p.x(), p.y(), p.z(), col, WU, WV, OverlayTexture.NO_OVERLAY, light, nx, ny, nz);
    }

    /** Haleler: kameraya donen kareler (iki yonlu yazilir; arka yuz atildigi icin biri gorunur). */
    private static void halos(VertexConsumer vc, Matrix4f m, Camera cam) {
        if (haloCount == 0) return;
        Vector3f up = cam.getUpVector(), left = cam.getLeftVector();
        float sm = (float) Math.sqrt(m.m00() * m.m00() + m.m01() * m.m01() + m.m02() * m.m02());
        for (int i = 0; i < haloCount; i++) {
            int o = i * 5;
            Vector3f c = m.transformPosition(HALO[o], HALO[o + 1], HALO[o + 2], TMP);
            float s = HALO[o + 3] * sm;
            int col = Float.floatToRawIntBits(HALO[o + 4]);
            float lx = left.x() * s, ly = left.y() * s, lz = left.z() * s, ux = up.x() * s, uy = up.y() * s, uz = up.z() * s;
            float cx = c.x, cy = c.y, cz = c.z;
            HX[0] = cx + lx - ux; HX[1] = cx - lx - ux; HX[2] = cx - lx + ux; HX[3] = cx + lx + ux;
            HY[0] = cy + ly - uy; HY[1] = cy - ly - uy; HY[2] = cy - ly + uy; HY[3] = cy + ly + uy;
            HZ[0] = cz + lz - uz; HZ[1] = cz - lz - uz; HZ[2] = cz - lz + uz; HZ[3] = cz + lz + uz;
            for (int k = 0; k < 4; k++) vc.addVertex(HX[k], HY[k], HZ[k], col, HU[k], HV[k], OverlayTexture.NO_OVERLAY, LightTexture.FULL_BRIGHT, 0, 1, 0);
            for (int k = 3; k >= 0; k--) vc.addVertex(HX[k], HY[k], HZ[k], col, HU[k], HV[k], OverlayTexture.NO_OVERLAY, LightTexture.FULL_BRIGHT, 0, 1, 0);
        }
    }

    private static float rotLerp(float t, float a, float b) {
        float d = b - a;
        while (d < -180f) d += 360f;
        while (d >= 180f) d -= 360f;
        return a + t * d;
    }

    // ------------------------------------------------------------------ parcacik izi

    public static void tick(Minecraft mc) {
        ticks++;
        ModConfig c = cfg();
        PetKind kind = selected();
        if (c == null || kind == null || mc.level == null || mc.player == null || mc.isPaused()) return;
        if (!c.petParticles || ticks - renderTick > 3) return;
        ParticleStatus st = mc.options.particles().get();
        if (st == ParticleStatus.MINIMAL) return;
        float rate = (kind == PetKind.KALP ? 1.2f : kind == PetKind.PERI || kind == PetKind.RUH ? 6f : 3.5f) * (st == ParticleStatus.DECREASED ? 0.5f : 1f);
        particleAcc += rate / 20f;
        if (particleAcc < 1f) return;
        particleAcc -= 1f;
        if (particleAcc > 2f) particleAcc = 0f;
        ParticleOptions o = particle(kind);
        if (o == null) return;
        RandomSource r = mc.level.random;
        float sc = Math.max(0.5f, Math.min(2f, c.petScale));
        mc.level.addParticle(o, BRAIN.x + (r.nextDouble() - 0.5) * 0.15 * sc, BRAIN.y + BRAIN.hopOffset + (r.nextDouble() - 0.5) * 0.12 * sc,
                BRAIN.z + (r.nextDouble() - 0.5) * 0.15 * sc, (r.nextDouble() - 0.5) * 0.01, kind == PetKind.KALP ? 0.02 : -0.005,
                (r.nextDouble() - 0.5) * 0.01);
    }

    private static final java.util.Set<PetKind> NO_PARTICLE = java.util.EnumSet.noneOf(PetKind.class);

    private static ParticleOptions particle(PetKind k) {
        ParticleOptions o = PARTICLES.get(k);
        if (o != null || NO_PARTICLE.contains(k)) return o;
        ParticleType<?> t = BuiltInRegistries.PARTICLE_TYPE.get(new ResourceLocation(k.particle));
        if (t instanceof SimpleParticleType s) {
            PARTICLES.put(k, s);
            return s;
        }
        NO_PARTICLE.add(k); // bulunamadi: her tick yeniden aranmaz
        return null;
    }

    /** Dunya degisti: yoldas oyuncunun yanina isinlanir. */
    public static void onLevelChange() {
        BRAIN.reset();
    }
}
