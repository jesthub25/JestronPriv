package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.cosmetic.Wings;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Kanat gosterilirken pelerin kanatlarin icinden gecmesin (vanilla da elytra varken pelerini gizler). JestronPriv
 * pelerini secildiyse vanilla pelerini yerine o cizilir.
 */
@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin {

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/PlayerRenderState;FF)V",
            at = @At("HEAD"), cancellable = true)
    private void jestronpriv$hideCape(PoseStack poseStack, MultiBufferSource buffers, int light, PlayerRenderState state,
                                      float yRot, float xRot, CallbackInfo ci) {
        try {
            if (Wings.styleFor(state) != null || com.jestron.jestronpriv.client.cosmetic.Capes.replacesVanilla(state)) ci.cancel();
        } catch (Throwable ignored) {
        }
    }
}
