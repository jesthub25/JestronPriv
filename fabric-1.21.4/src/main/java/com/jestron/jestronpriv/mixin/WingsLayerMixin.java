package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.cosmetic.Wings;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.client.model.ElytraModel;
import com.jestron.jestronpriv.client.cosmetic.Elytras;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Animasyonlu kanatlar: yerel oyuncuda kanat seciliyse vanilla elytra yerine JestronPriv kanatlari cizilir
 * (elytra giyili olsun olmasin).
 * Secili degilse vanilla davranis aynen surer.
 */
@Mixin(WingsLayer.class)
public abstract class WingsLayerMixin {

    @Shadow
    @Final
    private ElytraModel elytraModel;
    @Shadow
    @Final
    private ElytraModel elytraBabyModel;

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
            at = @At("HEAD"), cancellable = true)
    private void jestronpriv$wings(PoseStack poseStack, MultiBufferSource buffers, int light, HumanoidRenderState state,
                                   float yRot, float xRot, CallbackInfo ci) {
        try {
            if (Wings.styleFor(state) != null) {
                Object model = ((RenderLayer<?, ?>) (Object) this).getParentModel();
                if (Wings.render(poseStack, buffers, light, state, model)) {
                    ci.cancel();
                    return;
                }
            }
            // elytra gorunumu: vanilla model + animasyon, JestronPriv dokusu
            if (Elytras.render(poseStack, buffers, light, state, this.elytraModel, this.elytraBabyModel)) ci.cancel();
        } catch (Throwable t) {
            // Kanat cizimi asla oyunu bozmasin: hata olursa vanilla elytra cizilir
        }
    }
}
