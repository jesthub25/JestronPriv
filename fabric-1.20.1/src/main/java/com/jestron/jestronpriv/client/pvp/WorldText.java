package com.jestron.jestronpriv.client.pvp;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Dunyada suzulen yazilar (hasar sayilari, totem sayaci). Yazi kameraya doner, kenarlikli (her zeminde okunur),
 * acilista hafif tasarak buyur, sonunda soner. Etikete bagli yazilar varligi takip eder.
 */
public final class WorldText {

    public static final int NORMAL = 0, CRIT = 1, KILL = 2, LABEL = 3;
    private static final int CAP = 72;

    private static final class E {
        double x, y, z;
        float vx, vy, vz, age, life, scale, seed;
        String text;
        FormattedCharSequence seq;
        int width, color, kind, follow = -1;
        float followDy;
    }

    private static final E[] LIST = new E[CAP];
    private static int n;
    private static long lastNanos;

    static {
        for (int i = 0; i < CAP; i++) LIST[i] = new E();
    }

    private WorldText() {
    }

    public static int count() {
        return n;
    }

    public static void clear() {
        n = 0;
    }

    /** Yeni yazi. Kapasite doluysa en eskisi yerine yazilir. */
    public static void add(String text, double x, double y, double z, float vx, float vy, float vz, float life, float scale,
                           int color, int kind, float seed) {
        addE(text, x, y, z, vx, vy, vz, life, scale, color, kind, seed);
    }

    private static E addE(String text, double x, double y, double z, float vx, float vy, float vz, float life, float scale,
                          int color, int kind, float seed) {
        if (text == null || text.isEmpty() || !Double.isFinite(x + y + z)) return null;
        if (!Float.isFinite(vx + vy + vz) || !(scale > 0)) return null;
        E e;
        if (n < CAP) e = LIST[n++];
        else {
            // en yasli (omrunu en cok tuketmis) girisi kullan
            int oldest = 0;
            for (int i = 1; i < CAP; i++) if (LIST[i].age / LIST[i].life > LIST[oldest].age / LIST[oldest].life) oldest = i;
            e = LIST[oldest];
        }
        e.x = x;
        e.y = y;
        e.z = z;
        e.vx = vx;
        e.vy = vy;
        e.vz = vz;
        e.age = 0;
        e.life = Math.max(0.2f, life);
        e.scale = scale;
        e.seed = seed;
        e.text = text;
        e.seq = null;
        e.width = -1;
        e.color = color & 0xFFFFFF;
        e.kind = kind;
        e.follow = -1;
        return e;
    }

    /** Varliga bagli etiket (basinin uzerinde durur, varlik yoksa yerinde soner). Ayni varlikta eskisi yenilenir. */
    public static void label(Entity ent, String text, float life, float scale, int color) {
        if (ent == null) return;
        for (int i = 0; i < n; i++) {
            E e = LIST[i];
            if (e.kind == LABEL && e.follow == ent.getId()) {
                e.text = text;
                e.seq = null;
                e.width = -1;
                e.age = 0;
                e.life = life;
                e.color = color & 0xFFFFFF;
                e.scale = scale;
                return;
            }
        }
        float dy = ent.getBbHeight() + 0.55f;
        E e = addE(text, ent.getX(), ent.getY() + dy, ent.getZ(), 0, 0, 0, life, scale, color, LABEL, 0);
        if (e != null) {
            e.follow = ent.getId();
            e.followDy = dy;
        }
    }

    private static void update(float dt) {
        Minecraft mc = Minecraft.getInstance();
        for (int i = 0; i < n; ) {
            E e = LIST[i];
            e.age += dt;
            if (!(e.age < e.life)) {
                // son elemanla yer degistir (nesne havuzu korunur)
                E last = LIST[n - 1];
                LIST[n - 1] = e;
                LIST[i] = last;
                n--;
                continue;
            }
            if (e.kind == LABEL) {
                Entity ent = e.follow >= 0 && mc.level != null ? mc.level.getEntity(e.follow) : null;
                if (ent != null) {
                    e.x = ent.getX();
                    e.y = ent.getY() + e.followDy;
                    e.z = ent.getZ();
                }
            } else {
                e.vy -= (e.kind == KILL ? 1.2f : 2.4f) * dt;
                float k = (float) Math.exp(-1.6f * dt);
                e.vx *= k;
                e.vz *= k;
                e.x += e.vx * dt;
                e.y += e.vy * dt;
                e.z += e.vz * dt;
            }
            i++;
        }
    }

    public static void render(WorldRenderContext ctx) {
        try {
            long now = System.nanoTime();
            float dt = lastNanos == 0 ? 0 : Math.min(0.1f, (now - lastNanos) / 1e9f);
            lastNanos = now;
            if (n == 0) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                n = 0;
                return;
            }
            if (!mc.isPaused()) update(dt);
            PoseStack ps = ctx.matrixStack();
            MultiBufferSource buf = ctx.consumers();
            Camera cam = ctx.camera();
            if (ps == null || buf == null || cam == null || n == 0) return;
            Vec3 cp = cam.getPosition();
            Font font = mc.font;
            for (int i = 0; i < n; i++) {
                E e = LIST[i];
                float t = e.age / e.life;
                float a = t < 0.55f ? 1f : 1f - (t - 0.55f) / 0.45f;
                if (e.kind == LABEL) a = t < 0.08f ? t / 0.08f : (t < 0.8f ? 1f : 1f - (t - 0.8f) / 0.2f);
                if (a < 0.04f) continue;
                // acilis: hafif tasan buyume; kritik / olum: daha guclu
                float pop = 1f;
                float pt = Math.min(1f, e.age / (e.kind == KILL ? 0.22f : 0.16f));
                if (pt < 1f) {
                    float c1 = e.kind == NORMAL ? 1.6f : 2.4f, u = pt - 1f;
                    pop = 1f + (c1 + 1f) * u * u * u + c1 * u * u;
                }
                float s = e.scale * Math.max(0.05f, pop);
                double dx = e.x - cp.x, dy = e.y - cp.y, dz = e.z - cp.z;
                double d2 = dx * dx + dy * dy + dz * dz;
                if (d2 > 64 * 64) continue;
                // hedefin govdesine gommemek icin kameraya dogru biraz cek
                double d = Math.sqrt(d2);
                if (d > 1.2) {
                    double pull = Math.min(0.5, d - 1.0) / d;
                    dx -= dx * pull;
                    dy -= dy * pull;
                    dz -= dz * pull;
                }
                // kritikte kisa titreme
                if (e.kind != NORMAL && e.kind != LABEL && e.age < 0.3f) dx += Math.sin(e.age * 70f + e.seed * 10f) * 0.02 * (1 - e.age / 0.3f);
                if (e.seq == null) {
                    e.seq = Component.literal(e.text).getVisualOrderText();
                    e.width = font.width(e.text);
                }
                int col = Palette.argb(e.color, a), outline = Palette.argb(darker(e.color), a * 0.9f);
                ps.pushPose();
                ps.translate(dx, dy, dz);
                ps.mulPose(cam.rotation());
                float k = 0.025f * s;
                ps.scale(k, -k, k);
                font.drawInBatch8xOutline(e.seq, -e.width / 2f, -4f, col, outline, ps.last().pose(), buf, LightTexture.FULL_BRIGHT);
                ps.popPose();
            }
        } catch (Throwable t) {
            n = 0;
        }
    }

    private static int darker(int rgb) {
        int r = ((rgb >> 16) & 255) / 6, g = ((rgb >> 8) & 255) / 6, b = (rgb & 255) / 6;
        return (r << 16) | (g << 8) | b;
    }
}
