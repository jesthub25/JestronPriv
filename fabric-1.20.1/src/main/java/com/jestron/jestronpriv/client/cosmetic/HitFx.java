package com.jestron.jestronpriv.client.cosmetic;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.hitfx.HitParticles;
import com.jestron.jestronpriv.hitfx.HitStyle;
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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Ozel vurus / kritik partikulleri (sadece istemci, sadece SENIN vuruslarin).
 * <ul>
 *   <li>Vurus: sunucunun hasar olayi paketi vuranin kimligini tasir; vuran yerel oyuncuysa ve
 *       hedef bir canliysa secili vurus stili patlar (iskalanan / kalkanla engellenen vurusta cikmaz).</li>
 *   <li>Kritik: sunucunun kritik animasyonu (CRIT izleyici yayici) son vurdugun hedefe gelirse secili
 *       kritik stili patlar; istenirse vanilla kritik / buyu yildizlari gizlenir.</li>
 * </ul>
 * Yeni partikul turu kaydedilmez (her sunucuda guvenle calisir); kendi dokusuyla kendisi cizer.
 */
public final class HitFx {

    private static final ResourceLocation ATLAS = new ResourceLocation("jestronpriv", "textures/hitfx/atlas.png");
    private static final HitParticles P = new HitParticles();

    /** Son vurusun hedefi ve zamani (istemci tick'i). */
    private static int lastTarget = Integer.MIN_VALUE;
    private static long lastAttackTick = -1000, lastDamageTick = -1000;
    private static int lastDamaged = Integer.MIN_VALUE;
    private static long ticks;
    private static long lastNanos;

    private static final Vector3f TMP = new Vector3f(), N = new Vector3f(), RIGHT = new Vector3f(), UP = new Vector3f();
    private static final Quaternionf Q = new Quaternionf();

    private HitFx() {
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    public static int liveCount() {
        return P.count;
    }

    // ------------------------------------------------------------------ olaylar

    /** Yerel oyuncu bir varliga saldirdi (istemci). */
    public static void onAttack(Entity target) {
        if (target == null) return;
        lastTarget = target.getId();
        lastAttackTick = ticks;
    }

    /** Hasar olayi paketi (istemci ana thread'i). causeId / directId: vuran varlik kimlikleri (-1 = yok). */
    public static void onDamageEvent(int entityId, int causeId, int directId) {
        try {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer p = mc.player;
            ModConfig c = cfg();
            if (p == null || mc.level == null || c == null) return;
            // sadece yakin dovus: vuran da dogrudan vuran da sensin
            if (causeId != p.getId() || directId != p.getId() || entityId == p.getId()) return;
            Entity e = mc.level.getEntity(entityId);
            if (!(e instanceof LivingEntity)) return;
            lastDamaged = entityId;
            lastDamageTick = ticks;
            if (!(e instanceof Player) && !c.hitOnMobs) return;
            HitStyle st = HitStyle.parse(c.hitStyle);
            if (st != HitStyle.VANILLA) spawnAt(e, st, false, p);
        } catch (Throwable ignored) {
            // partikul asla oyunu bozmasin
        }
    }

    /** ParticleEngine.createTrackingEmitter basinda: true = vanilla yayiciyi iptal et. */
    public static boolean onTrackingEmitter(Entity e, ParticleOptions o) {
        try {
            if (e == null || (o != ParticleTypes.CRIT && o != ParticleTypes.ENCHANTED_HIT)) return false;
            ModConfig c = cfg();
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer p = mc.player;
            if (c == null || p == null || e == p) return false;
            int id = e.getId();
            // bu animasyon senin vurusuna mi ait: az once bu hedefe hasar verdin ya da saldirdin
            boolean mine = (id == lastDamaged && ticks - lastDamageTick <= 3) || (id == lastTarget && ticks - lastAttackTick <= 30);
            if (!mine) return false;
            if (o == ParticleTypes.CRIT && (e instanceof Player || c.pvpMobs)) com.jestron.jestronpriv.client.pvp.PvpHub.onCrit(id);
            if (!(e instanceof Player) && !c.hitOnMobs) return false;
            if (o == ParticleTypes.CRIT) {
                HitStyle st = HitStyle.parse(c.critStyle);
                if (st == HitStyle.VANILLA) return false;
                spawnAt(e, st, true, p);
                return c.hitHideVanilla;
            }
            // buyu (keskinlik) yildizlari: vurus stili seciliyse onun yerine gecer
            return c.hitHideVanilla && HitStyle.parse(c.hitStyle) != HitStyle.VANILLA;
        } catch (Throwable t) {
            return false;
        }
    }

    private static void spawnAt(Entity e, HitStyle st, boolean crit, LocalPlayer p) {
        double cx = e.getX(), cy = e.getY() + e.getBbHeight() * 0.62, cz = e.getZ();
        float dx = (float) (cx - p.getX()), dz = (float) (cz - p.getZ());
        // vurulan sensen (on gorunum vb.) ya da cok yakin: hedefin onune dogru
        double fx = cx - dx * 0.12, fz = cz - dz * 0.12;
        spawn(st, fx, cy, fz, dx, dz, crit, e.getBbWidth(), e.getY() + 0.02);
    }

    private static void spawn(HitStyle st, double x, double y, double z, float dx, float dz, boolean crit, float width, double floor) {
        ModConfig c = cfg();
        Minecraft mc = Minecraft.getInstance();
        if (c == null || mc.level == null) return;
        float amount = c.hitAmount * Math.max(0.1f, c.particleDensity);
        if (c.respectVanillaParticles) {
            ParticleStatus ps = mc.options.particles().get();
            if (ps == ParticleStatus.MINIMAL) amount *= 0.35f;
            else if (ps == ParticleStatus.DECREASED) amount *= 0.65f;
        }
        int light;
        try {
            light = LevelRenderer.getLightColor(mc.level, BlockPos.containing(x, y, z));
        } catch (Throwable t) {
            light = LightTexture.FULL_BRIGHT;
        }
        float sizeMul = c.hitSize * Math.max(0.7f, Math.min(1.6f, width / 0.6f));
        P.burst(st, x, y, z, dx, dz, crit, amount, sizeMul, light, floor);
        playSound(c, st, crit, x, y, z);
    }

    // ------------------------------------------------------------------ ses

    private record PendingSound(long due, String key, double x, double y, double z, float vol, float pitch) {
    }

    private static final java.util.List<PendingSound> PENDING = new java.util.ArrayList<>();
    private static final java.util.Map<String, net.minecraft.sounds.SoundEvent> SOUNDS = new java.util.HashMap<>();
    private static final net.minecraft.util.RandomSource RND = net.minecraft.util.RandomSource.create();
    private static long soundTick = -1;
    private static int soundsThisTick;

    /** Stilin vurus / kritik tonu: katmanlar ayni anda ya da tick gecikmeli, hafif dogal perde farkiyla. */
    private static void playSound(ModConfig c, HitStyle st, boolean crit, double x, double y, double z) {
        if (!c.hitSound || !(c.hitSoundVolume > 0f)) return;
        com.jestron.jestronpriv.hitfx.HitSounds.Layer[] ls = crit ? com.jestron.jestronpriv.hitfx.HitSounds.crit(st)
                : com.jestron.jestronpriv.hitfx.HitSounds.hit(st);
        if (ls.length == 0) return;
        // ayni karede cok hedefe (supurme) vurulunca ses patlamasi olmasin
        if (soundTick != ticks) {
            soundTick = ticks;
            soundsThisTick = 0;
        }
        if (++soundsThisTick > 3) return;
        float j = 1f + (RND.nextFloat() - 0.5f) * 0.06f;
        float note = com.jestron.jestronpriv.hitfx.HitSounds.PENTA[RND.nextInt(com.jestron.jestronpriv.hitfx.HitSounds.PENTA.length)];
        for (com.jestron.jestronpriv.hitfx.HitSounds.Layer l : ls) {
            float pitch = Math.max(0.5f, Math.min(2.0f, (l.pitch() < 0f ? note : l.pitch()) * j));
            float vol = Math.min(2f, l.vol() * c.hitSoundVolume);
            if (l.delay() <= 0) playNow(c, l.key(), x, y, z, vol, pitch);
            else if (PENDING.size() < 64) PENDING.add(new PendingSound(ticks + l.delay(), l.key(), x, y, z, vol, pitch));
        }
    }

    private static void playNow(ModConfig c, String key, double x, double y, double z, float vol, float pitch) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;
            net.minecraft.sounds.SoundEvent ev = SOUNDS.computeIfAbsent(key, k -> {
                ResourceLocation rl = ResourceLocation.tryParse(k);
                if (rl == null) return null;
                net.minecraft.sounds.SoundEvent e = net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(rl);
                return e != null ? e : net.minecraft.sounds.SoundEvent.createVariableRangeEvent(rl);
            });
            if (ev == null) return;
            mc.getSoundManager().play(new net.minecraft.client.resources.sounds.SimpleSoundInstance(ev, c.soundSource(), vol, pitch, RND, x, y, z));
        } catch (Throwable ignored) {
            // ses asla oyunu bozmasin
        }
    }

    /** Totem patlamasi efekti (TotemFx). */
    public static void totemBurst(Entity e, float scale) {
        Minecraft mc = Minecraft.getInstance();
        if (e == null || mc.level == null) return;
        ModConfig c = cfg();
        float k = scale * (c == null ? 1f : Math.max(0.1f, c.particleDensity));
        if (c != null && c.respectVanillaParticles) {
            ParticleStatus ps = mc.options.particles().get();
            if (ps == ParticleStatus.MINIMAL) k *= 0.35f;
            else if (ps == ParticleStatus.DECREASED) k *= 0.65f;
        }
        P.totem(e.getX(), e.getY(), e.getZ(), e.getBbHeight(), k, LightTexture.FULL_BRIGHT);
    }

    /** Ayar ekranindan: oyuncunun onunde totem onizlemesi. */
    public static void totemPreview(float scale) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null) return;
        Vec3 look = p.getViewVector(1f);
        P.totem(p.getX() + look.x * 3, p.getY(), p.getZ() + look.z * 3, 1.8f, scale, LightTexture.FULL_BRIGHT);
    }

    /** Ayarlar ekranindan: oyuncunun 2.4 blok onunde onizleme patlamasi. */
    public static void preview(HitStyle st, boolean crit) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || st == null || st == HitStyle.VANILLA) return;
        Vec3 look = p.getViewVector(1f), eye = p.getEyePosition();
        double x = eye.x + look.x * 2.4, y = eye.y + look.y * 2.4 - 0.15, z = eye.z + look.z * 2.4;
        spawn(st, x, y, z, (float) look.x, (float) look.z, crit, 0.6f, Math.min(y - 0.9, p.getY() + 0.02));
    }

    // ------------------------------------------------------------------ tick / cizim

    public static void tick(Minecraft mc) {
        ticks++;
        if (mc.level == null) {
            if (P.count > 0) P.clear();
            PENDING.clear();
            return;
        }
        if (PENDING.isEmpty()) return;
        ModConfig c = cfg();
        for (int i = 0; i < PENDING.size(); ) {
            PendingSound ps = PENDING.get(i);
            if (ps.due() <= ticks) {
                PENDING.remove(i);
                if (c != null) playNow(c, ps.key(), ps.x(), ps.y(), ps.z(), ps.vol(), ps.pitch());
            } else {
                i++;
            }
        }
    }

    public static void render(WorldRenderContext ctx) {
        try {
            long now = System.nanoTime();
            float dt = lastNanos == 0 ? 0f : (now - lastNanos) / 1e9f;
            lastNanos = now;
            Minecraft mc = Minecraft.getInstance();
            if (P.count == 0) return;
            if (mc.level == null) {
                P.clear();
                return;
            }
            if (!mc.isPaused()) P.update(dt);
            if (P.count == 0) return;
            PoseStack ps = ctx.matrixStack();
            MultiBufferSource buffers = ctx.consumers();
            Camera cam = ctx.camera();
            if (ps == null || buffers == null || cam == null) return;
            Vec3 cp = cam.getPosition();
            Q.set(cam.rotation());
            RIGHT.set(1, 0, 0).rotate(Q);
            UP.set(0, 1, 0).rotate(Q);
            PoseStack.Pose pose = ps.last();
            Matrix4f m = pose.pose();
            pose.transformNormal(N.set(0, 1, 0), N);
            // isiksiz gorunum: normal hep yukari (vanilla partikul gibi duz renk). Tek taramada iki tur bulunur.
            boolean flat = false, glow = false;
            for (int i = 0; i < P.count && !(flat && glow); i++) {
                if ((P.flags[i] & HitParticles.GLOW) != 0) glow = true;
                else flat = true;
            }
            // tampon kaynagi tek turu acik tutar: once tum duz, sonra tum parlayan
            if (flat) drawPass(buffers.getBuffer(RenderType.entityTranslucent(ATLAS)), m, cp, false);
            if (glow) drawPass(buffers.getBuffer(RenderType.eyes(ATLAS)), m, cp, true);
        } catch (Throwable t) {
            P.clear();
        }
    }

    private static void drawPass(VertexConsumer vc, Matrix4f m, Vec3 cp, boolean glowPass) {
        for (int i = 0; i < P.count; i++) {
            int f = P.flags[i];
            if (((f & HitParticles.GLOW) != 0) != glowPass) continue;
            float s = P.scale(i), a = P.alpha(i);
            if (s < 0.002f || a < 0.01f) continue;
            float px = (float) (P.x[i] - cp.x), py = (float) (P.y[i] - cp.y), pz = (float) (P.z[i] - cp.z);
            if (px * px + py * py + pz * pz > 96 * 96) continue;
            int rgb = P.rgb[i];
            boolean plain = (f & (HitParticles.RING | HitParticles.FLASH | HitParticles.STREAK | HitParticles.BUTTERFLY)) == 0;
            if (glowPass && plain) {
                // sicak cekirdek: parlayan partikul dogdugu an beyaza yakin, hizla kendi rengine soguyor
                float life = P.age[i] / Math.max(1e-3f, P.life[i]);
                if (life < 0.22f) rgb = whiten(rgb, (1f - life / 0.22f) * 0.5f);
                // yumusak isima (bloom): arkada genis, soluk yuvarlak hale
                float ba = a * 0.28f;
                if (ba > 0.02f && s > 0.01f) {
                    float bu0 = HitParticles.u0(HitParticles.SP_ORB), bv0 = HitParticles.v0(HitParticles.SP_ORB);
                    billboard(vc, m, px, py, pz, s * 2.3f, s * 2.3f, 0f, ((int) (ba * 255) << 24) | P.rgb[i], LightTexture.FULL_BRIGHT, bu0, bv0,
                            bu0 + HitParticles.DU, bv0 + HitParticles.DV, true);
                }
            }
            int argb = ((int) (a * 255) << 24) | rgb;
            int light = glowPass ? LightTexture.FULL_BRIGHT : P.light[i];
            int sp = P.sprite[i];
            float u0 = HitParticles.u0(sp), v0 = HitParticles.v0(sp), u1 = u0 + HitParticles.DU, v1 = v0 + HitParticles.DV;
            if ((f & HitParticles.BUTTERFLY) != 0) {
                butterfly(vc, m, i, px, py, pz, s, argb, light, u0, v0, u1, v1);
            } else if ((f & HitParticles.STREAK) != 0) {
                streak(vc, m, i, px, py, pz, s, argb, light, u0, v0, u1, v1, glowPass);
            } else {
                billboard(vc, m, px, py, pz, s * P.flipX(i), s, P.rot[i], argb, light, u0, v0, u1, v1, glowPass);
            }
        }
    }

    /** Rengi beyaza dogru karistirir (0..1). */
    private static int whiten(int rgb, float k) {
        int r = (rgb >> 16) & 255, g = (rgb >> 8) & 255, b = rgb & 255;
        r += Math.round((255 - r) * k);
        g += Math.round((255 - g) * k);
        b += Math.round((255 - b) * k);
        return (r << 16) | (g << 8) | b;
    }

    private static void billboard(VertexConsumer vc, Matrix4f m, float px, float py, float pz, float hw, float hh, float rot,
                                  int argb, int light, float u0, float v0, float u1, float v1, boolean twoSided) {
        float c = (float) Math.cos(rot), sn = (float) Math.sin(rot);
        // yerel eksenler (ekran duzleminde donmus)
        float rx = RIGHT.x * c + UP.x * sn, ry = RIGHT.y * c + UP.y * sn, rz = RIGHT.z * c + UP.z * sn;
        float ux = -RIGHT.x * sn + UP.x * c, uy = -RIGHT.y * sn + UP.y * c, uz = -RIGHT.z * sn + UP.z * c;
        rx *= hw;
        ry *= hw;
        rz *= hw;
        ux *= hh;
        uy *= hh;
        uz *= hh;
        quad(vc, m, px - rx - ux, py - ry - uy, pz - rz - uz, px + rx - ux, py + ry - uy, pz + rz - uz,
                px + rx + ux, py + ry + uy, pz + rz + uz, px - rx + ux, py - ry + uy, pz - rz + uz,
                argb, light, u0, v1, u1, v1, u1, v0, u0, v0, twoSided);
    }

    /** Hiz yonunde uzayan kivilcim. */
    private static void streak(VertexConsumer vc, Matrix4f m, int i, float px, float py, float pz, float s, int argb, int light,
                               float u0, float v0, float u1, float v1, boolean twoSided) {
        float vx = P.vx[i], vy = P.vy[i], vz = P.vz[i];
        float sp = (float) Math.sqrt(vx * vx + vy * vy + vz * vz);
        if (sp < 1e-3f) {
            billboard(vc, m, px, py, pz, s, s, 0, argb, light, u0, v0, u1, v1, twoSided);
            return;
        }
        float dx = vx / sp, dy = vy / sp, dz = vz / sp;
        // kameraya dik yan eksen: d x (kameradan partikule)
        float cl = (float) Math.sqrt(px * px + py * py + pz * pz);
        float tx = px / Math.max(cl, 1e-3f), ty = py / Math.max(cl, 1e-3f), tz = pz / Math.max(cl, 1e-3f);
        float wx = dy * tz - dz * ty, wy = dz * tx - dx * tz, wz = dx * ty - dy * tx;
        float wl = (float) Math.sqrt(wx * wx + wy * wy + wz * wz);
        if (wl < 1e-4f) {
            billboard(vc, m, px, py, pz, s, s, 0, argb, light, u0, v0, u1, v1, twoSided);
            return;
        }
        float len = s * (2.5f + Math.min(6f, sp * 1.4f)), w = s * 0.9f;
        wx = wx / wl * w;
        wy = wy / wl * w;
        wz = wz / wl * w;
        float lx = dx * len, ly = dy * len, lz = dz * len;
        quad(vc, m, px - lx - wx, py - ly - wy, pz - lz - wz, px + lx - wx, py + ly - wy, pz + lz - wz,
                px + lx + wx, py + ly + wy, pz + lz + wz, px - lx + wx, py - ly + wy, pz - lz + wz,
                argb, light, u0, v1, u1, v1, u1, v0, u0, v0, twoSided);
    }

    /** Iki kanatli kelebek: govde hiz yonunde, kanatlar menteseden cirpar. */
    private static void butterfly(VertexConsumer vc, Matrix4f m, int i, float px, float py, float pz, float s, int argb, int light,
                                  float u0, float v0, float u1, float v1) {
        float hx = P.vx[i], hz = P.vz[i];
        float hl = (float) Math.sqrt(hx * hx + hz * hz);
        if (hl < 1e-3f) {
            hx = (float) Math.cos(P.seed[i] * 6.283f);
            hz = (float) Math.sin(P.seed[i] * 6.283f);
        } else {
            hx /= hl;
            hz /= hl;
        }
        // yan eksen (yatay, govdeye dik)
        float sx = -hz, sz = hx;
        float a = P.flap(i), ca = (float) Math.cos(a), sa = (float) Math.sin(a);
        float L = s * 0.9f, W = s * 1.6f;
        float bx = hx * L, bz = hz * L;
        for (int side = -1; side <= 1; side += 2) {
            // kanat yonu: yan eksenden yukari dogru a kadar kalkik
            float wx = sx * side * ca * W, wy = sa * W, wz = sz * side * ca * W;
            float uA = side > 0 ? u0 : u1, uB = side > 0 ? u1 : u0;
            // mentese: govde cizgisi (on - arka); kanat: menteseden disari
            quad(vc, m, px + bx, py, pz + bz, px + bx + wx, py + wy, pz + bz + wz,
                    px - bx + wx, py + wy, pz - bz + wz, px - bx, py, pz - bz,
                    argb, light, uA, v0, uB, v0, uB, v1, uA, v1, false);
        }
    }

    private static void quad(VertexConsumer vc, Matrix4f m,
                             float x0, float y0, float z0, float x1, float y1, float z1,
                             float x2, float y2, float z2, float x3, float y3, float z3,
                             int argb, int light, float ua, float va, float ub, float vb, float uc, float vcc, float ud, float vd,
                             boolean twoSided) {
        vert(vc, m, x0, y0, z0, argb, ua, va, light);
        vert(vc, m, x1, y1, z1, argb, ub, vb, light);
        vert(vc, m, x2, y2, z2, argb, uc, vcc, light);
        vert(vc, m, x3, y3, z3, argb, ud, vd, light);
        if (twoSided) {
            vert(vc, m, x3, y3, z3, argb, ud, vd, light);
            vert(vc, m, x2, y2, z2, argb, uc, vcc, light);
            vert(vc, m, x1, y1, z1, argb, ub, vb, light);
            vert(vc, m, x0, y0, z0, argb, ua, va, light);
        }
    }

    private static void vert(VertexConsumer vc, Matrix4f m, float x, float y, float z, int argb, float u, float v, int light) {
        Vector3f p = m.transformPosition(x, y, z, TMP);
        vc.addVertex(p.x(), p.y(), p.z(), argb, u, v, OverlayTexture.NO_OVERLAY, light, N.x(), N.y(), N.z());
    }
}
