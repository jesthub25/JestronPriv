package com.jestron.jestronpriv.client.cosmetic;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Yerdeki esyalar: ustunde ikon + ad + toplam adet (nadirlik renginde), istenirse hitbox kutusu. Yakindaki ayni
 * esyalar tek etikette birlestirilir. Sadece gorulebilen yerde cizilir (derinlik testli; duvar arkasini
 * gostermez). Tarama tick'te bir kez, menzil ve etiket sayisi sinirli.
 */
public final class GroundItems {

    private static final class Label {
        ItemEntity entity;
        ItemStack stack;
        int count, shownCount = -1, nameColor, textW;
        String text;
        boolean member;   // baska etikette birlesti
    }

    private static final List<Label> LABELS = new ArrayList<>();
    private static final List<Label> POOL = new ArrayList<>();
    private static final List<ItemEntity> FOUND = new ArrayList<>();
    private static int n;

    private GroundItems() {
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    private static Label label(int i) {
        while (POOL.size() <= i) POOL.add(new Label());
        return POOL.get(i);
    }

    public static void tick(Minecraft mc) {
        ModConfig c = cfg();
        n = 0;
        LABELS.clear();
        if (c == null || !c.giEnabled || mc.level == null || mc.player == null) {
            release();
            return;
        }
        try {
            double r = Math.max(4f, Math.min(48f, c.giRange));
            Vec3 p = mc.player.position();
            FOUND.clear();
            FOUND.addAll(mc.level.getEntitiesOfClass(ItemEntity.class, new AABB(p.x - r, p.y - r, p.z - r, p.x + r, p.y + r, p.z + r)));
            double r2 = r * r;
            FOUND.removeIf(e -> e.isRemoved() || e.getItem().isEmpty() || e.distanceToSqr(p) > r2);
            FOUND.sort((a, b) -> Double.compare(a.distanceToSqr(p), b.distanceToSqr(p)));
            int max = Math.max(5, Math.min(80, c.giMax));
            for (int i = 0; i < FOUND.size() && n < max; i++) {
                ItemEntity e = FOUND.get(i);
                ItemStack s = e.getItem();
                if (c.giMerge) {
                    Label host = null;
                    for (int k = 0; k < n; k++) {
                        Label l = POOL.get(k);
                        if (!l.member && l.entity.distanceToSqr(e) < 1.5 * 1.5 && ItemStack.isSameItemSameComponents(l.stack, s)) {
                            host = l;
                            break;
                        }
                    }
                    if (host != null) {
                        host.count += s.getCount();
                        continue;
                    }
                }
                Label l = label(n++);
                if (l.entity != e || l.stack == null || !ItemStack.isSameItemSameComponents(l.stack, s)) {
                    l.shownCount = -1;
                    Integer rc = s.getRarity().color().getColor();
                    l.nameColor = rc == null ? 0xFFFFFF : rc;
                }
                l.entity = e;
                l.stack = s;
                l.count = s.getCount();
                l.member = false;
                LABELS.add(l);
            }
            FOUND.clear();
            // kullanilmayan havuz elemanlari eski varliklari tutmasin
            for (int i = n; i < POOL.size(); i++) {
                Label l = POOL.get(i);
                if (l.entity == null) break;
                l.entity = null;
                l.stack = null;
            }
            for (Label l : LABELS) {
                if (l.shownCount != l.count || l.text == null) {
                    String name = c.giName ? l.stack.getHoverName().getString() : "";
                    String cnt = c.giCount && l.count > 1 ? (name.isEmpty() ? "×" : " ×") + l.count : "";
                    l.text = name + cnt;
                    l.textW = l.text.isEmpty() ? 0 : mc.font.width(l.text);
                    l.shownCount = l.count;
                }
            }
        } catch (Throwable t) {
            LABELS.clear();
            n = 0;
        }
    }

    /** Havuzdaki eski varlik / esya baglantilarini birakir (cikista eski dunya bellekte kalmasin). */
    private static void release() {
        if (POOL.isEmpty() || POOL.get(0).entity == null && POOL.get(POOL.size() - 1).entity == null) return;
        for (Label l : POOL) {
            l.entity = null;
            l.stack = null;
            l.shownCount = -1;
        }
        FOUND.clear();
    }

    /** Ayar degisince yazilar yeniden uretilsin. */
    public static void invalidate() {
        for (Label l : POOL) l.shownCount = -1;
    }

    public static void render(WorldRenderContext ctx) {
        ModConfig c = cfg();
        Minecraft mc = Minecraft.getInstance();
        if (c == null || !c.giEnabled || LABELS.isEmpty() || mc.level == null) return;
        PoseStack ps = ctx.matrixStack();
        MultiBufferSource buffers = ctx.consumers();
        Camera cam = ctx.camera();
        if (ps == null || buffers == null || cam == null) return;
        try {
            float pt = ctx.tickCounter().getGameTimeDeltaPartialTick(true);
            Vec3 cp = cam.getPosition();
            Vector3f look = cam.getLookVector();
            Font font = mc.font;
            float scale = Math.max(0.5f, Math.min(2f, c.giScale));
            int bg = Math.round(Math.max(0f, Math.min(0.8f, c.giBg)) * 255f) << 24;
            // ekran disindakiler (arkan, yanlar) hic islenmez: ikon modeli ve yazi kurulmaz
            Frustum fr = ctx.frustum();
            // once kutular (tek tampon turu), sonra etiketler
            if (c.giHitbox) {
                VertexConsumer lines = buffers.getBuffer(RenderType.lines());
                PoseStack.Pose pose = ps.last();
                for (Label l : LABELS) {
                    ItemEntity e = l.entity;
                    if (e.isRemoved()) continue;
                    double x = lerp(pt, e.xo, e.getX()) - cp.x, y = lerp(pt, e.yo, e.getY()) - cp.y, z = lerp(pt, e.zo, e.getZ()) - cp.z;
                    if (!looking(c, look, x, y, z)) continue;
                    float hw = e.getBbWidth() / 2f, hh = e.getBbHeight();
                    if (fr != null && !fr.isVisible(new AABB(cp.x + x - hw, cp.y + y, cp.z + z - hw, cp.x + x + hw, cp.y + y + hh, cp.z + z + hw)))
                        continue;
                    int col = (0xD0 << 24) | (c.giRarity ? l.nameColor : 0xFFFFFF);
                    box(lines, pose, (float) (x - hw), (float) y, (float) (z - hw), (float) (x + hw), (float) (y + hh), (float) (z + hw), col);
                }
            }
            for (Label l : LABELS) {
                ItemEntity e = l.entity;
                if (e.isRemoved() || l.text == null) continue;
                double x = lerp(pt, e.xo, e.getX()) - cp.x, y = lerp(pt, e.yo, e.getY()) - cp.y + e.getBbHeight() + 0.45, z = lerp(pt, e.zo, e.getZ()) - cp.z;
                if (!looking(c, look, x, y, z)) continue;
                double dist = Math.sqrt(x * x + y * y + z * z);
                float k = 0.022f * scale * (1f + (float) dist / 28f);
                boolean icon = c.giIcon;
                int tw = l.textW;
                float total = (icon ? 18 : 0) + tw;
                if (fr != null) {
                    double half = total * 0.5 * k + 0.25;
                    if (!fr.isVisible(new AABB(cp.x + x - half, cp.y + y - half, cp.z + z - half, cp.x + x + half, cp.y + y + half, cp.z + z + half)))
                        continue;
                }
                ps.pushPose();
                ps.translate(x, y, z);
                ps.mulPose(cam.rotation());
                ps.scale(k, -k, k);
                float left = -total / 2f;
                Matrix4f m = ps.last().pose();
                if (tw > 0) {
                    int col = 0xFF000000 | (c.giRarity ? l.nameColor : 0xFFFFFF);
                    font.drawInBatch(l.text, left + (icon ? 18 : 0), -4f, col, false, m, buffers, Font.DisplayMode.NORMAL, bg, LightTexture.FULL_BRIGHT);
                }
                if (icon) {
                    ps.pushPose();
                    ps.translate(left + 8f, 0f, 1.5f);
                    ps.scale(16f, -16f, 1f);
                    mc.getItemRenderer().renderStatic(l.stack, ItemDisplayContext.GUI, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ps, buffers,
                            mc.level, 0);
                    ps.popPose();
                }
                ps.popPose();
            }
        } catch (Throwable ignored) {
        }
    }

    /** Sadece-baktigin modu: nisangaha ~8 derece yakin ya da 2.5 bloktan yakin. */
    private static boolean looking(ModConfig c, Vector3f look, double x, double y, double z) {
        if (!c.giOnlyLooking) return true;
        double d = Math.sqrt(x * x + y * y + z * z);
        if (d < 2.5) return true;
        double dot = (x * look.x() + y * look.y() + z * look.z()) / d;
        return dot > 0.990;
    }

    private static double lerp(float t, double a, double b) {
        return a + (b - a) * t;
    }

    private static void box(VertexConsumer vc, PoseStack.Pose p, float x0, float y0, float z0, float x1, float y1, float z1, int col) {
        line(vc, p, x0, y0, z0, x1, y0, z0, col);
        line(vc, p, x0, y0, z1, x1, y0, z1, col);
        line(vc, p, x0, y1, z0, x1, y1, z0, col);
        line(vc, p, x0, y1, z1, x1, y1, z1, col);
        line(vc, p, x0, y0, z0, x0, y1, z0, col);
        line(vc, p, x1, y0, z0, x1, y1, z0, col);
        line(vc, p, x0, y0, z1, x0, y1, z1, col);
        line(vc, p, x1, y0, z1, x1, y1, z1, col);
        line(vc, p, x0, y0, z0, x0, y0, z1, col);
        line(vc, p, x1, y0, z0, x1, y0, z1, col);
        line(vc, p, x0, y1, z0, x0, y1, z1, col);
        line(vc, p, x1, y1, z0, x1, y1, z1, col);
    }

    private static void line(VertexConsumer vc, PoseStack.Pose p, float ax, float ay, float az, float bx, float by, float bz, int col) {
        float nx = bx - ax, ny = by - ay, nz = bz - az;
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len < 1e-6f) return;
        nx /= len;
        ny /= len;
        nz /= len;
        vc.addVertex(p, ax, ay, az).setColor(col).setNormal(p, nx, ny, nz);
        vc.addVertex(p, bx, by, bz).setColor(col).setNormal(p, nx, ny, nz);
    }
}
