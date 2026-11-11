package com.jestron.jestronpriv.client.view;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.pvp.Palette;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;

/**
 * Blok secim kutusu: renk / gokkusagi (kenarlar boyunca akan), nabiz, piksel cinsinden kalinlik, dolgu (bakilan yuz
 * ya da tum blok) ve kirma ilerlemesi (dolgu buyur, renk kirmizidan yesile). Vanilla cizgi golgelendiricisi
 * kullanilir; kalinlik sadece bu cizim icin degisir.
 */
public final class BlockOutline {

    private static float lineWidth = 2.5f;

    /** Vanilla "lines" ile ayni durum + istenen kalinlik. */
    private static final RenderType LINES = new RenderType("jestronpriv_outline", DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES, 1536, false, false, () -> {
        RenderType.lines().setupRenderState();
        RenderSystem.lineWidth(lineWidth);
    }, () -> RenderType.lines().clearRenderState()) {
    };

    /** Saydam dolgu: derinlik yazmaz (arkadaki su / cam bozulmaz), yuzeye yapismaz (polygon offset). */
    private static final RenderType FILL = new RenderType("jestronpriv_outline_fill", DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS, 1536, false, true, () -> {
        RenderType.debugQuads().setupRenderState();
        RenderSystem.depthMask(false);
        RenderSystem.enablePolygonOffset();
        RenderSystem.polygonOffset(-1f, -10f);
    }, () -> {
        RenderSystem.polygonOffset(0f, 0f);
        RenderSystem.disablePolygonOffset();
        RenderSystem.depthMask(true);
        RenderType.debugQuads().clearRenderState();
    }) {
    };

    private BlockOutline() {
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    /** Fabric BLOCK_OUTLINE: true donerse vanilla cizer. */
    public static boolean onOutline(WorldRenderContext ctx, WorldRenderContext.BlockOutlineContext oc) {
        ModConfig c = cfg();
        if (c == null || !c.boEnabled) return true;
        try {
            draw(ctx, oc, c);
            return false;
        } catch (Throwable t) {
            return true;
        }
    }

    private static void draw(WorldRenderContext ctx, WorldRenderContext.BlockOutlineContext oc, ModConfig c) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        BlockPos pos = oc.blockPos();
        BlockState st = oc.blockState();
        VoxelShape shape = st.getShape(mc.level, pos, CollisionContext.of(oc.entity()));
        if (shape.isEmpty()) return;
        MultiBufferSource buffers = ctx.consumers();
        PoseStack ps = ctx.matrixStack();
        if (buffers == null) return;
        if (ps == null) ps = new PoseStack();
        double ox = pos.getX() - oc.cameraX(), oy = pos.getY() - oc.cameraY(), oz = pos.getZ() - oc.cameraZ();
        double t = System.nanoTime() / 1e9;
        float pulse = c.boPulse ? 0.65f + 0.35f * (float) Math.sin(t * 4.0) : 1f;
        float alpha = Math.max(0.1f, Math.min(1f, c.boAlpha)) * pulse;
        int base = c.boColor & 0xFFFFFF;

        // kirma ilerlemesi
        float mine = c.boMining ? MiningProgress.progress(pos) : -1f;

        // dolgu
        String fill = c.boFill;
        if (mine >= 0f || !"YOK".equals(fill)) {
            int fc = mine >= 0f ? Palette.mix(0xFF4040, 0x50FF60, mine) : c.boFillSame ? base : c.boFillColor & 0xFFFFFF;
            float fa = Math.max(0.03f, Math.min(0.6f, c.boFillAlpha)) * (c.boPulse ? pulse : 1f);
            if (mine >= 0f) fa = Math.max(fa, 0.22f);
            VertexConsumer vc = buffers.getBuffer(FILL);
            Matrix4f m = ps.last().pose();
            Direction face = mc.hitResult instanceof BlockHitResult bh && bh.getType() != HitResult.Type.MISS ? bh.getDirection() : null;
            final int col = Palette.argb(fc, fa);
            final boolean onlyFace = "YUZ".equals(fill) && mine < 0f;
            final float grow = mine >= 0f ? Math.max(0.05f, mine) : 1f;
            for (net.minecraft.world.phys.AABB b : shape.toAabbs()) {
                double e = 0.002;
                double x0 = b.minX - e, y0 = b.minY - e, z0 = b.minZ - e, x1 = b.maxX + e, y1 = b.maxY + e, z1 = b.maxZ + e;
                if (grow < 1f) {
                    // ortadan disari buyur
                    double cx = (x0 + x1) / 2, cy = (y0 + y1) / 2, cz = (z0 + z1) / 2;
                    x0 = cx + (x0 - cx) * grow;
                    x1 = cx + (x1 - cx) * grow;
                    y0 = cy + (y0 - cy) * grow;
                    y1 = cy + (y1 - cy) * grow;
                    z0 = cz + (z0 - cz) * grow;
                    z1 = cz + (z1 - cz) * grow;
                }
                box(vc, m, (float) (x0 + ox), (float) (y0 + oy), (float) (z0 + oz), (float) (x1 + ox), (float) (y1 + oy), (float) (z1 + oz),
                        col, onlyFace ? face : null);
            }
        }

        // cizgiler
        float px = Math.max(1f, Math.min(10f, c.boWidth));
        float wscale = Math.max(1f, mc.getWindow().getWidth() / 1920f);
        lineWidth = px * wscale * (c.boPulse ? 0.85f + 0.15f * pulse : 1f);
        VertexConsumer lv = buffers.getBuffer(LINES);
        PoseStack.Pose pose = ps.last();
        final boolean rainbow = c.boRainbow;
        final float al = alpha;
        final double tt = t;
        shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
            float nx = (float) (x2 - x1), ny = (float) (y2 - y1), nz = (float) (z2 - z1);
            float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (len < 1e-6f) return;
            nx /= len;
            ny /= len;
            nz /= len;
            int c1 = rainbow ? Palette.rainbow(tt + (x1 + y1 + z1) * 0.35, 0f) : base;
            int c2 = rainbow ? Palette.rainbow(tt + (x2 + y2 + z2) * 0.35, 0f) : base;
            lv.addVertex(pose, (float) (x1 + ox), (float) (y1 + oy), (float) (z1 + oz)).setColor(Palette.argb(c1, al)).setNormal(pose, nx, ny, nz);
            lv.addVertex(pose, (float) (x2 + ox), (float) (y2 + oy), (float) (z2 + oz)).setColor(Palette.argb(c2, al)).setNormal(pose, nx, ny, nz);
        });
    }

    /** Kutunun yuzleri (face == null: hepsi). */
    private static void box(VertexConsumer vc, Matrix4f m, float x0, float y0, float z0, float x1, float y1, float z1, int col, Direction face) {
        if (face == null || face == Direction.DOWN) quad(vc, m, col, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
        if (face == null || face == Direction.UP) quad(vc, m, col, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
        if (face == null || face == Direction.NORTH) quad(vc, m, col, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0);
        if (face == null || face == Direction.SOUTH) quad(vc, m, col, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
        if (face == null || face == Direction.WEST) quad(vc, m, col, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
        if (face == null || face == Direction.EAST) quad(vc, m, col, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1);
    }

    private static void quad(VertexConsumer vc, Matrix4f m, int col, float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz) {
        vc.addVertex(m, ax, ay, az).setColor(col);
        vc.addVertex(m, bx, by, bz).setColor(col);
        vc.addVertex(m, cx, cy, cz).setColor(col);
        vc.addVertex(m, dx, dy, dz).setColor(col);
    }
}
