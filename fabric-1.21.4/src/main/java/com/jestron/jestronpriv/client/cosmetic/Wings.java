package com.jestron.jestronpriv.client.cosmetic;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.wings.WingAnimator;
import com.jestron.jestronpriv.wings.WingMesh;
import com.jestron.jestronpriv.wings.WingPose;
import com.jestron.jestronpriv.wings.WingStyle;
import com.jestron.jestronpriv.wings.WingStyles;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
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
import java.util.Map;

/**
 * Animasyonlu kanatlar (sadece yerel oyuncu, sadece istemci). Vanilla elytra / pelerin katmaninin yerine cizilir.
 * Geometri ve animasyon MC'den bagimsiz {@link WingMesh} / {@link WingAnimator} ile uretilir; burada sadece
 * hareket bilgisi toplanir, dortgenler katman katman (once taban, sonra isilti) tampona yazilir ve uclardan
 * parcacik cikar.
 */
public final class Wings {

    /** Ayarlar ekranindaki onizleme modu (null = oyuncunun gercek hareketi). */
    public static volatile WingAnimator.Mode preview;

    private static final WingAnimator ANIM = new WingAnimator();
    private static final WingAnimator.Motion MOTION = new WingAnimator.Motion();
    private static final WingMesh MESH = new WingMesh();
    private static final WingPose POSE = new WingPose();
    private static final Map<String, ResourceLocation> TEX = new HashMap<>();
    private static final Map<String, ParticleOptions> PARTICLES = new HashMap<>();

    // katman tamponu: dortgen basina 28 float (katman, 12 konum, 8 uv, 4 renk (int bitleri), 3 normal)
    private static final int STRIDE = 28;
    private static float[] buf = new float[STRIDE * 640];
    private static int count;

    private static long lastNanos;

    // parcacik uclari (dunya koordinati) ve tazelik
    private static final double[] TIP_WORLD = new double[WingMesh.MAX_TIPS * 3];
    private static int tipWorldCount;
    private static long tipStampTick = -100;
    private static long clientTicks;
    private static float particleAcc;
    private static float lastEnergy;

    private static final Vector3f TMP = new Vector3f(), TMPN = new Vector3f(), TMPN2 = new Vector3f();

    private Wings() {
    }

    // ------------------------------------------------------------------ secim

    public static WingStyle selected() {
        JestronPrivClient mod = JestronPrivClient.get();
        if (mod == null) return null;
        return WingStyles.get(mod.config().wing);
    }

    /** Bu render durumu yerel oyuncuya mi ait ve kanat secili mi. */
    public static WingStyle styleFor(HumanoidRenderState state) {
        if (!(state instanceof PlayerRenderState prs) || state.isInvisible) return null;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || prs.id != mc.player.getId()) return null;
        return selected();
    }

    private static ResourceLocation texture(WingStyle st) {
        return TEX.computeIfAbsent(st.id(), id -> ResourceLocation.fromNamespaceAndPath("jestronpriv", st.texturePath()));
    }

    // ------------------------------------------------------------------ animasyon

    private static void updateAnimation(HumanoidRenderState state) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        long now = System.nanoTime();
        float dt = lastNanos == 0 ? 0.05f : (now - lastNanos) / 1e9f;
        // ayni karede ikinci cizim (envanter / onizleme penceresi): animasyon bir kez ilerler
        if (lastNanos != 0 && dt < 0.002f) return;
        lastNanos = now;
        if (dt > 0.25f) dt = 0.05f;               // uzun takilma / pencere arka planda
        if (mc.isPaused()) dt = 0f;               // oyun duraklatilinca kanat da durur
        WingAnimator.Mode pv = preview;
        if (pv != null) {
            switch (pv) {
                case WALK -> MOTION.set(0.1, 0, true, false, false, false, false, false, false);
                case SPRINT -> MOTION.set(0.28, 0, true, false, false, true, false, false, false);
                case GLIDE, FLY -> MOTION.set(0.0, 0, false, false, true, false, false, false, false);
                case AIR -> MOTION.set(0.0, -0.3, false, false, false, false, false, false, false);
                default -> MOTION.set(0, 0, true, false, false, false, false, false, false);
            }
        } else if (p != null) {
            double vx = p.getX() - p.xo, vz = p.getZ() - p.zo;
            Vec3 dm = p.getDeltaMovement();
            MOTION.set(Math.sqrt(vx * vx + vz * vz), dm == null ? 0 : dm.y, p.onGround(), p.isFallFlying(),
                    p.getAbilities() != null && p.getAbilities().flying, p.isSprinting(), state.isCrouching,
                    p.isVisuallySwimming(), p.isSleeping() || p.isPassenger());
        } else {
            MOTION.set(0, 0, true, false, false, false, false, false, false);
        }
        POSE.set(ANIM.update(MOTION, dt));
    }

    // ------------------------------------------------------------------ cizim

    /**
     * WingsLayer yerine cizer. true donerse vanilla elytra cizilmez.
     *
     * @param model oyuncu modeli (govde egilmesine uymak icin; null olabilir)
     */
    public static boolean render(PoseStack ps, MultiBufferSource buffers, int light, HumanoidRenderState state, Object model) {
        WingStyle st = styleFor(state);
        if (st == null) return false;
        JestronPrivClient mod = JestronPrivClient.get();
        float scale = mod == null ? 1f : mod.config().wingScale;
        updateAnimation(state);
        ps.pushPose();
        try {
            if (model instanceof HumanoidModel<?> hm) hm.body.translateAndRotate(ps);
            count = 0;
            MESH.build(st, POSE, scale, Wings::collect);
            if (st.kind() == com.jestron.jestronpriv.wings.WingKind.HOLY) {
                renderHoly(ps, buffers, st.glow() > 0 ? LightTexture.FULL_BRIGHT : light);
                captureTips(ps.last().pose());
                return true;
            }
            PoseStack.Pose pose = ps.last();
            ResourceLocation tex = texture(st);
            RenderType baseType = st.translucent() ? RenderType.entityTranslucent(tex) : RenderType.entityCutoutNoCull(tex);
            // tampon kaynagi ayni anda tek turu acik tutar: once tum taban, sonra tum isilti
            // taban: arka yuz atilmaz (NO_CULL); isilti (eyes): arka yuz atilir -> cift yuzlu yazilir
            flush(buffers.getBuffer(baseType), pose, WingMesh.BASE, light, false);
            if (hasLayer(WingMesh.GLOW)) flush(buffers.getBuffer(RenderType.eyes(tex)), pose, WingMesh.GLOW, LightTexture.FULL_BRIGHT, true);
            captureTips(pose.pose());
        } finally {
            ps.popPose();
        }
        return true;
    }

    private static final net.minecraft.world.item.ItemStack FEATHER = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.FEATHER);
    private static final org.joml.Quaternionf HQ = new org.joml.Quaternionf();

    /**
     * Kutsal melek: oldurme efektindeki ile ayni, oyunun kendi 3D tuy esyalari (vanilla ItemDisplay ile ayni cizim:
     * konum -> donus -> olcek -> Y 180 -> FIXED baglam). Mesh "kutsal uzayda" (y yukari) uretir; govdeye X 180 ile gecilir.
     */
    private static void renderHoly(PoseStack ps, MultiBufferSource buffers, int light) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        ps.pushPose();
        try {
            ps.mulPose(com.mojang.math.Axis.XP.rotation((float) Math.PI));
            float[] it = MESH.items;
            for (int i = 0; i < MESH.itemCount; i++) {
                int o = i * 8;
                float sc = it[o + 7];
                if (!(sc > 0.001f)) continue;
                ps.pushPose();
                ps.translate(it[o], it[o + 1], it[o + 2]);
                ps.mulPose(HQ.set(it[o + 3], it[o + 4], it[o + 5], it[o + 6]));
                ps.scale(sc, sc, sc);
                ps.mulPose(com.mojang.math.Axis.YP.rotation((float) Math.PI));
                mc.getItemRenderer().renderStatic(FEATHER, net.minecraft.world.item.ItemDisplayContext.FIXED, light,
                        OverlayTexture.NO_OVERLAY, ps, buffers, mc.level, 0);
                ps.popPose();
            }
        } finally {
            ps.popPose();
        }
    }

    private static void collect(int layer, float[] xyz, float[] uv, int[] argb, float nx, float ny, float nz) {
        if ((count + 1) * STRIDE > buf.length) {
            float[] nb = new float[buf.length * 2];
            System.arraycopy(buf, 0, nb, 0, buf.length);
            buf = nb;
        }
        int o = count * STRIDE;
        buf[o] = layer;
        System.arraycopy(xyz, 0, buf, o + 1, 12);
        System.arraycopy(uv, 0, buf, o + 13, 8);
        buf[o + 21] = Float.intBitsToFloat(argb[0]);
        buf[o + 22] = Float.intBitsToFloat(argb[1]);
        buf[o + 23] = Float.intBitsToFloat(argb[2]);
        buf[o + 24] = Float.intBitsToFloat(argb[3]);
        buf[o + 25] = nx;
        buf[o + 26] = ny;
        buf[o + 27] = nz;
        count++;
    }

    private static boolean hasLayer(int layer) {
        for (int i = 0; i < count; i++) if (buf[i * STRIDE] == layer) return true;
        return false;
    }

    /**
     * @param twoSided arka yuzu atan cizim turleri (isilti / eyes) icin her dortgeni ters yonde de yazar:
     *                 kanadin iki tarafi da her acidan ayni parlaklikta gorunur.
     */
    private static void flush(VertexConsumer vc, PoseStack.Pose pose, int layer, int light, boolean twoSided) {
        Matrix4f m = pose.pose();
        for (int i = 0; i < count; i++) {
            int o = i * STRIDE;
            if (buf[o] != layer) continue;
            TMPN.set(buf[o + 25], buf[o + 26], buf[o + 27]);
            Vector3f n = pose.transformNormal(TMPN, TMPN2);
            float nx = n.x(), ny = n.y(), nz = n.z();
            for (int v = 0; v < 4; v++) vertex(vc, m, o, v, light, nx, ny, nz);
            if (twoSided) for (int v = 3; v >= 0; v--) vertex(vc, m, o, v, light, -nx, -ny, -nz);
        }
    }

    private static void vertex(VertexConsumer vc, Matrix4f m, int o, int v, int light, float nx, float ny, float nz) {
        Vector3f p = m.transformPosition(buf[o + 1 + v * 3], buf[o + 2 + v * 3], buf[o + 3 + v * 3], TMP);
        int c = Float.floatToRawIntBits(buf[o + 21 + v]);
        vc.addVertex(p.x(), p.y(), p.z(), c, buf[o + 13 + v * 2], buf[o + 14 + v * 2],
                OverlayTexture.NO_OVERLAY, light, nx, ny, nz);
    }

    /** Dunya cizimindeyse (GUI degil) uc noktalarini dunya koordinatina cevirip saklar. */
    private static void captureTips(Matrix4f m) {
        // GUI'de (envanter / onizleme) model matrisi yuzlerce kat buyutulmustur; orada parcacik olmaz
        float sx = (float) Math.sqrt(m.m00() * m.m00() + m.m01() * m.m01() + m.m02() * m.m02());
        if (sx < 0.2f || sx > 3f) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameRenderer == null || mc.player == null) return;
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        if (cam == null) return;
        int n = 0;
        for (int i = 0; i < MESH.tipCount; i++) {
            Vector3f p = m.transformPosition(MESH.tips[i * 3], MESH.tips[i * 3 + 1], MESH.tips[i * 3 + 2], TMP);
            double wx = cam.x + p.x(), wy = cam.y + p.y(), wz = cam.z + p.z();
            // guvenlik: uc noktasi oyuncuya yakin olmali
            double dx = wx - mc.player.getX(), dy = wy - (mc.player.getY() + 1), dz = wz - mc.player.getZ();
            if (dx * dx + dy * dy + dz * dz > 16) continue;
            TIP_WORLD[n * 3] = wx;
            TIP_WORLD[n * 3 + 1] = wy;
            TIP_WORLD[n * 3 + 2] = wz;
            n++;
        }
        tipWorldCount = n;
        tipStampTick = clientTicks;
        lastEnergy = POSE.energy;
    }

    // ------------------------------------------------------------------ parcacik

    /** Istemci tick'i (duraklatilmamisken). */
    public static void tick(Minecraft mc) {
        clientTicks++;
        JestronPrivClient mod = JestronPrivClient.get();
        if (mod == null || mc.level == null || mc.player == null) return;
        ModConfig c = mod.config();
        WingStyle st = selected();
        if (st == null || !c.wingParticles || st.particle() == null || st.particleRate() <= 0) return;
        if (clientTicks - tipStampTick > 3 || tipWorldCount == 0) return;   // kanat su an cizilmiyor (1. sahis vb.)
        ParticleStatus ps = mc.options.particles().get();
        if (ps == ParticleStatus.MINIMAL) return;
        float rate = st.particleRate() * (0.5f + 2f * lastEnergy) * (ps == ParticleStatus.DECREASED ? 0.5f : 1f);
        particleAcc += rate / 20f;
        RandomSource r = mc.level.random;
        int guard = 0;
        while (particleAcc >= 1f && guard++ < 6) {
            particleAcc -= 1f;
            ParticleOptions o = particle(st, r);
            if (o == null) {
                particleAcc = 0;
                return;
            }
            int i = r.nextInt(tipWorldCount);
            double x = TIP_WORLD[i * 3] + (r.nextDouble() - 0.5) * 0.15;
            double y = TIP_WORLD[i * 3 + 1] + (r.nextDouble() - 0.5) * 0.15;
            double z = TIP_WORLD[i * 3 + 2] + (r.nextDouble() - 0.5) * 0.15;
            mc.level.addParticle(o, x, y, z, (r.nextDouble() - 0.5) * 0.02, -0.01, (r.nextDouble() - 0.5) * 0.02);
        }
        if (particleAcc > 4f) particleAcc = 4f;
    }

    private static ParticleOptions particle(WingStyle st, RandomSource r) {
        String id = st.particle();
        if (id.equals("rainbow")) {
            float h = (clientTicks % 120) / 120f + r.nextFloat() * 0.1f;
            return new DustParticleOptions(hsv(h), 0.9f);
        }
        ParticleOptions cached = PARTICLES.get(id);
        if (cached != null) return cached;
        ParticleOptions o = null;
        if (id.startsWith("dust:")) {
            try {
                o = new DustParticleOptions(Integer.parseInt(id.substring(5), 16) & 0xFFFFFF, 1.0f);
            } catch (NumberFormatException ignored) {
            }
        } else {
            ParticleType<?> t = BuiltInRegistries.PARTICLE_TYPE.getValue(ResourceLocation.withDefaultNamespace(id));
            if (t instanceof SimpleParticleType s) o = s;
        }
        if (o != null) PARTICLES.put(id, o);
        return o;
    }

    private static int hsv(float h) {
        h -= (float) Math.floor(h);
        float r = Math.max(0, Math.min(1, Math.abs(h * 6 - 3) - 1)), g = Math.max(0, Math.min(1, 2 - Math.abs(h * 6 - 2))),
                b = Math.max(0, Math.min(1, 2 - Math.abs(h * 6 - 4)));
        return ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }
}
