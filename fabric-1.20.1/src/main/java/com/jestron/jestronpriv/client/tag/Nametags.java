package com.jestron.jestronpriv.client.tag;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.camera.FreeCam;
import com.jestron.jestronpriv.client.cosmetic.Hats;
import com.jestron.jestronpriv.client.cosmetic.LocalLook;
import com.jestron.jestronpriv.hats.HatSettings;
import com.jestron.jestronpriv.tag.TagSettings;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** Isim etiketi: F5 / serbest kamerada kendi etiketin (dunya ciziminde; envanterdeki modeli etkilemez). */
public final class Nametags {

    private Nametags() {
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    /** Kendi etiketin: arka gorunumde / serbest kamerada, oyuncunun ustunde. */
    public static void renderSelf(WorldRenderContext ctx) {
        ModConfig c = cfg();
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (c == null || p == null || mc.level == null || !c.tagEnabled || mc.options.hideGui) return;
        TagSettings tag = LocalLook.tag();
        if (tag == null) return;
        boolean thirdPerson = !mc.options.getCameraType().isFirstPerson() || mc.getCameraEntity() != p;
        if (!thirdPerson) return;
        boolean free = FreeCam.active();
        if (free ? !c.tagShowFreecam : !c.tagShowF5) return;
        if (p.isInvisible() || p.isSpectator() || !p.isAlive()) return;
        PoseStack ps = ctx.matrixStack();
        MultiBufferSource buffers = ctx.consumers();
        Camera cam = ctx.camera();
        if (ps == null || buffers == null || cam == null) return;
        float pt = ctx.tickCounter().getGameTimeDeltaPartialTick(true);
        double x = p.xo + (p.getX() - p.xo) * pt, y = p.yo + (p.getY() - p.yo) * pt, z = p.zo + (p.getZ() - p.zo) * pt;
        boolean helmet = !p.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).isEmpty();
        float lift = hatLift(LocalLook.hat(), helmet);
        double ty = y + p.getBbHeight() + 0.5 + lift + tag.height;
        Vec3 cp = cam.getPosition();
        ps.pushPose();
        try {
            ps.translate(x - cp.x, ty - cp.y, z - cp.z);
            ps.mulPose(cam.rotation());
            float s = 0.025f * tag.scale;
            ps.scale(s, -s, s);
            int light = LevelRenderer.getLightColor(mc.level, BlockPos.containing(x, ty, z));
            String name = p.getGameProfile().getName();
            TagRenderer.draw(ps, buffers, light, tag, name, CustomIcons.texture(c.tagPrefixImage), CustomIcons.texture(c.tagSuffixImage), false,
                    c.tagShowRealName, System.currentTimeMillis());
        } finally {
            ps.popPose();
        }
    }

    /** Sapka isim etiketiyle cakismasin: sapka tepesi ust payi asarsa etiket yukari kalkar (blok). */
    public static float hatLift(HatSettings h, boolean helmet) {
        float top = Hats.topFor(h, helmet);
        return Math.max(0f, top - 0.22f);
    }
}
