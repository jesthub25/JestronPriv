package com.jestron.jestronpriv.client;

import com.jestron.jestronpriv.fx.api.BlockDisplay;
import com.jestron.jestronpriv.fx.api.Display;
import com.jestron.jestronpriv.fx.api.ItemDisplay;
import com.jestron.jestronpriv.fx.api.TextDisplay;
import com.jestron.jestronpriv.fx.api.Transformation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.culling.Frustum;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import org.joml.Vector3f;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Efekt nesnelerini vanilla display entity renderer'i ile ayni sekilde cizer:
 * capa -> donusum (T * L * S * R) -> model. Interpolasyon her karede yapilir (akici).
 */
public final class FxRenderer {

    private final ClientBackend backend;
    private final Supplier<ModConfig> config;
    private final Map<com.jestron.jestronpriv.fx.api.ItemStack, ItemStack> itemCache = new IdentityHashMap<>();
    /** Kare basina isik onbellegi: ayni bloktaki yuzlerce parca icin tek sorgu. */
    private final Long2IntOpenHashMap lightCache = new Long2IntOpenHashMap();
    /** Kare basina yeniden kullanilan liste kopyasi (her karede yeni dizi ayirmamak icin). */
    private Display[] snapshot = new Display[64];

    FxRenderer(ClientBackend backend, Supplier<ModConfig> config) {
        this.backend = backend;
        this.config = config;
    }

    void clearCache() {
        itemCache.clear();
    }

    private ItemStack toVanilla(com.jestron.jestronpriv.fx.api.ItemStack s) {
        return itemCache.computeIfAbsent(s, k -> {
            ItemStack it = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(k.getType().id())));
            if (k.profile() instanceof com.mojang.authlib.GameProfile gp)
                it.getOrCreateTag().put("SkullOwner", net.minecraft.nbt.NbtUtils.writeGameProfile(new net.minecraft.nbt.CompoundTag(), gp));
            return it;
        });
    }

    void render(WorldRenderContext ctx) {
        List<Display> list = backend.displays();
        if (list.isEmpty()) {
            if (!itemCache.isEmpty()) itemCache.clear();
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        PoseStack ps = ctx.matrixStack();
        MultiBufferSource buffers = ctx.consumers();
        if (level == null || ps == null || buffers == null) return;
        Camera cam = ctx.camera();
        Vec3 cp = cam.getPosition();
        float partial = ctx.tickCounter().getGameTimeDeltaPartialTick(true);
        long now = backend.now();
        ModConfig cfg = config.get();
        Frustum frustum = cfg != null && cfg.frustumCulling ? ctx.frustum() : null;
        double maxDist = cfg == null ? 64 : cfg.effectRenderDistance;
        double maxDistSq = maxDist * maxDist;
        lightCache.clear();

        int n = list.size();
        if (snapshot.length < n) snapshot = new Display[Math.max(n, snapshot.length * 2)];
        Display[] arr = list.toArray(snapshot);
        for (int idx = 0; idx < n; idx++) {
            Display d = arr[idx];
            arr[idx] = null;
            if (d == null || !d.isValid()) continue;
            Transformation t = d.sample(now, partial);
            Vector3f sc = t.scaleView();
            if (sc.x < 0.002f && sc.y < 0.002f && sc.z < 0.002f) continue;
            Vector3f tr = t.translationView();
            double cx = d.anchorX() + tr.x, cy = d.anchorY() + tr.y, cz = d.anchorZ() + tr.z;
            double dx = cx - cp.x, dy = cy - cp.y, dz = cz - cp.z;
            if (dx * dx + dy * dy + dz * dz > maxDistSq) continue;
            if (frustum != null) {
                // Donusumden bagimsiz guvenli kure: en buyuk olcek * kosegen
                double r = Math.max(sc.x, Math.max(sc.y, sc.z)) * 1.8 + 0.6;
                if (d instanceof TextDisplay) r += 1.5;
                if (!frustum.isVisible(new AABB(cx - r, cy - r, cz - r, cx + r, cy + r, cz + r))) continue;
            }
            ps.pushPose();
            try {
                int light = d.getLight();
                if (light < 0) {
                    long key = BlockPos.asLong((int) Math.floor(d.anchorX()), (int) Math.floor(d.anchorY()), (int) Math.floor(d.anchorZ()));
                    light = lightCache.getOrDefault(key, Integer.MIN_VALUE);
                    if (light == Integer.MIN_VALUE) {
                        light = LevelRenderer.getLightColor(level, BlockPos.containing(d.anchorX(), d.anchorY(), d.anchorZ()));
                        lightCache.put(key, light);
                    }
                }
                ps.translate(d.anchorX() - cp.x, d.anchorY() - cp.y, d.anchorZ() - cp.z);
                if (d instanceof TextDisplay td) {
                    ps.translate(tr.x, tr.y, tr.z);
                    if (td.style() != null) StyledText.render(ps, buffers, cam, td.text(), td.style(), sc.y, light, mc.font, now, partial);
                    else renderText(ps, buffers, cam, td.text(), sc.y, light, mc.font);
                } else {
                    ps.translate(tr.x, tr.y, tr.z);
                    ps.mulPose(t.leftView());
                    ps.scale(Math.max(sc.x, 0.0005f), Math.max(sc.y, 0.0005f), Math.max(sc.z, 0.0005f));
                    ps.mulPose(t.rightView());
                    if (d instanceof BlockDisplay bd) {
                        mc.getBlockRenderer().renderSingleBlock(backend.blockState(bd.block()), ps, buffers, light, OverlayTexture.NO_OVERLAY);
                    } else if (d instanceof ItemDisplay id) {
                        // Vanilla ItemDisplayRenderer ile ayni: Y ekseninde 180 derece + FIXED baglam
                        ps.mulPose(Axis.YP.rotation((float) Math.PI));
                        mc.getItemRenderer().renderStatic(toVanilla(id.stack()), ItemDisplayContext.FIXED, light,
                                OverlayTexture.NO_OVERLAY, ps, buffers, level, 0);
                    }
                }
            } catch (Throwable ex) {
                // Tek bir nesnenin cizim hatasi oyunu asla bozmasin
                d.remove();
            } finally {
                ps.popPose();
            }
        }
    }

    /** Isim etiketi gibi her zaman kameraya bakan, ortalanmis cok satirli yazi. */
    private static void renderText(PoseStack ps, MultiBufferSource buffers, Camera cam, String text, float scale, int light, Font font) {
        ps.mulPose(cam.rotation());
        float s = 0.025f * Math.max(scale, 0.0005f);
        ps.scale(s, -s, s);
        String[] lines = text.split("\n");
        int lineH = font.lineHeight + 1;
        float y = -lines.length * lineH;
        for (String line : lines) {
            float x = -font.width(line) / 2f;
            font.drawInBatch(line, x, y, 0xFFFFFFFF, true, ps.last().pose(), buffers, Font.DisplayMode.NORMAL, 0, light);
            y += lineH;
        }
    }
}
