package com.jestron.jestronpriv.client.pvp;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Ozel hitbox gorunumu: F3+B'nin oyunculara ozel, ayarlanabilir hali. Carpisma kutusu, goz hizasi ve bakis yonu.
 * Nisan aldigin oyuncu ve hasar alan oyuncu farkli renkte. Kenarlar kameraya donuk seritlerle cizilir: kalinlik
 * ayarlanabilir ve uzaklikla sabit gorunur. Derinlik testi aciktir: duvar arkasindakiler gorunmez; gorunmez
 * (iksirli) ve izleyici oyuncularin kutusu asla cizilmez. Oyuna hicbir sey eklemez, sadece kutuyu gosterir.
 */
public final class Hitboxes {

    private static final int MAX_BOXES = 96;

    private Hitboxes() {
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    public static void render(WorldRenderContext ctx) {
        ModConfig c = cfg();
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer me = mc.player;
        if (c == null || !c.hbEnabled || me == null || mc.level == null || mc.options.hideGui) return;
        PoseStack ps = ctx.matrixStack();
        MultiBufferSource buffers = ctx.consumers();
        Camera cam = ctx.camera();
        if (ps == null || buffers == null || cam == null) return;
        float pt = ctx.tickCounter().getGameTimeDeltaPartialTick(true);
        Vec3 cp = cam.getPosition();
        double range = Math.max(4, Math.min(128, c.hbRange));
        double range2 = range * range;
        VertexConsumer vc = buffers.getBuffer(RenderType.debugQuads());
        Matrix4f m = ps.last().pose();
        Entity target = mc.crosshairPickEntity;
        boolean firstPerson = mc.options.getCameraType().isFirstPerson() && mc.getCameraEntity() == me;
        int drawn = 0;
        if (c.hbPlayers || c.hbSelf) {
            for (Player p : mc.level.players()) {
                if (drawn >= MAX_BOXES) break;
                if (p == me) {
                    if (!c.hbSelf || firstPerson) continue;
                } else if (!c.hbPlayers) continue;
                if (draw(vc, m, c, p, target, cp, pt, range2)) drawn++;
            }
        }
        if (c.hbMobs) {
            for (Entity e : mc.level.entitiesForRendering()) {
                if (drawn >= MAX_BOXES) break;
                if (e instanceof Player || !(e instanceof LivingEntity)) continue;
                if (draw(vc, m, c, e, target, cp, pt, range2)) drawn++;
            }
        }
    }

    private static boolean draw(VertexConsumer vc, Matrix4f m, ModConfig c, Entity e, Entity target, Vec3 cp, float pt, double range2) {
        // gorunmeyeni gostermek hile olur: gorunmez ve izleyici oyuncular hic cizilmez
        if (e.isInvisible() || e.isSpectator() || !e.isAlive()) return false;
        double ix = e.xo + (e.getX() - e.xo) * pt, iy = e.yo + (e.getY() - e.yo) * pt, iz = e.zo + (e.getZ() - e.zo) * pt;
        double dx = ix - cp.x, dy = iy - cp.y, dz = iz - cp.z;
        double d2 = dx * dx + dy * dy + dz * dz;
        if (d2 > range2) return false;
        AABB bb = e.getBoundingBox();
        // kutuyu ara-kare konumuna kaydir, kamera merkezli
        float ox = (float) (ix - e.getX() - cp.x), oy = (float) (iy - e.getY() - cp.y), oz = (float) (iz - e.getZ() - cp.z);
        float x0 = (float) bb.minX + ox, y0 = (float) bb.minY + oy, z0 = (float) bb.minZ + oz;
        float x1 = (float) bb.maxX + ox, y1 = (float) bb.maxY + oy, z1 = (float) bb.maxZ + oz;
        int rgb = c.hbColor;
        if (c.hbTarget && e == target) rgb = c.hbTargetColor;
        else if (c.hbHurt && e instanceof LivingEntity le && le.hurtTime > 0) rgb = c.hbHurtColor;
        float alpha = Math.max(0.1f, Math.min(1f, c.hbAlpha));
        int col = ((int) (alpha * 255) << 24) | (rgb & 0xFFFFFF);
        float dist = (float) Math.sqrt(d2);
        // ekranda yaklasik sabit piksel kalinligi
        float w = Math.max(0.006f, Math.max(0.5f, Math.min(4f, c.hbWidth)) * 0.0021f * Math.max(1.5f, dist));
        if (c.hbFill) {
            int fc = ((int) (Math.max(0.02f, Math.min(0.6f, c.hbFillAlpha)) * 255) << 24) | (rgb & 0xFFFFFF);
            fillBox(vc, m, x0, y0, z0, x1, y1, z1, fc);
        }
        // 12 kenar
        edge(vc, m, x0, y0, z0, x1, y0, z0, w, col);
        edge(vc, m, x0, y0, z1, x1, y0, z1, w, col);
        edge(vc, m, x0, y1, z0, x1, y1, z0, w, col);
        edge(vc, m, x0, y1, z1, x1, y1, z1, w, col);
        edge(vc, m, x0, y0, z0, x0, y1, z0, w, col);
        edge(vc, m, x1, y0, z0, x1, y1, z0, w, col);
        edge(vc, m, x0, y0, z1, x0, y1, z1, w, col);
        edge(vc, m, x1, y0, z1, x1, y1, z1, w, col);
        edge(vc, m, x0, y0, z0, x0, y0, z1, w, col);
        edge(vc, m, x1, y0, z0, x1, y0, z1, w, col);
        edge(vc, m, x0, y1, z0, x0, y1, z1, w, col);
        edge(vc, m, x1, y1, z0, x1, y1, z1, w, col);
        float ey = (float) (iy - cp.y) + e.getEyeHeight();
        if (c.hbEye) {
            int ec = ((int) (alpha * 255) << 24) | (c.hbEyeColor & 0xFFFFFF);
            float ew = w * 0.8f;
            edge(vc, m, x0, ey, z0, x1, ey, z0, ew, ec);
            edge(vc, m, x0, ey, z1, x1, ey, z1, ew, ec);
            edge(vc, m, x0, ey, z0, x0, ey, z1, ew, ec);
            edge(vc, m, x1, ey, z0, x1, ey, z1, ew, ec);
        }
        if (c.hbLook) {
            Vec3 look = e.getViewVector(pt);
            float lx = (float) (ix - cp.x), lz = (float) (iz - cp.z);
            int lc = ((int) (alpha * 255) << 24) | (c.hbLookColor & 0xFFFFFF);
            edge(vc, m, lx, ey, lz, lx + (float) look.x * 2f, ey + (float) look.y * 2f, lz + (float) look.z * 2f, w * 0.8f, lc);
        }
        return true;
    }

    /** A-B kenari: kameraya donuk serit (kamera orijinde). */
    private static void edge(VertexConsumer vc, Matrix4f m, float ax, float ay, float az, float bx, float by, float bz, float w, int col) {
        float dx = bx - ax, dy = by - ay, dz = bz - az;
        float mx = (ax + bx) * 0.5f, my = (ay + by) * 0.5f, mz = (az + bz) * 0.5f;
        // yan vektor = kenar x bakis
        float sx = dy * mz - dz * my, sy = dz * mx - dx * mz, sz = dx * my - dy * mx;
        float l = (float) Math.sqrt(sx * sx + sy * sy + sz * sz);
        if (l < 1e-6f) return;
        float k = w * 0.5f / l;
        sx *= k;
        sy *= k;
        sz *= k;
        vc.addVertex(m, ax - sx, ay - sy, az - sz).setColor(col);
        vc.addVertex(m, bx - sx, by - sy, bz - sz).setColor(col);
        vc.addVertex(m, bx + sx, by + sy, bz + sz).setColor(col);
        vc.addVertex(m, ax + sx, ay + sy, az + sz).setColor(col);
    }

    private static void fillBox(VertexConsumer vc, Matrix4f m, float x0, float y0, float z0, float x1, float y1, float z1, int col) {
        quad(vc, m, col, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
        quad(vc, m, col, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
        quad(vc, m, col, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0);
        quad(vc, m, col, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
        quad(vc, m, col, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
        quad(vc, m, col, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1);
    }

    private static void quad(VertexConsumer vc, Matrix4f m, int col, float ax, float ay, float az, float bx, float by, float bz, float cx, float cy,
                             float cz, float dx, float dy, float dz) {
        vc.addVertex(m, ax, ay, az).setColor(col);
        vc.addVertex(m, bx, by, bz).setColor(col);
        vc.addVertex(m, cx, cy, cz).setColor(col);
        vc.addVertex(m, dx, dy, dz).setColor(col);
    }
}
