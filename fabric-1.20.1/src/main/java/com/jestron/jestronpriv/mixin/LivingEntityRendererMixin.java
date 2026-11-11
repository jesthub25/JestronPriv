package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.pvp.HitColor;
import com.jestron.jestronpriv.client.pvp.HitColorState;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Vurulma rengi: hangi varliklarin ozel renkle yanip sonecegi. */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
            at = @At("TAIL"))
    private void jestronpriv$hitColorState(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo ci) {
        boolean v = false;
        try {
            v = state.hasRedOverlay && HitColor.applies(entity);
        } catch (Throwable ignored) {
        }
        ((HitColorState) state).jestronpriv$setHitColor(v);
    }

    @Inject(method = "getOverlayCoords", at = @At("RETURN"), cancellable = true)
    private static void jestronpriv$hitColorOverlay(LivingEntityRenderState state, float whiteOverlay, CallbackInfoReturnable<Integer> cir) {
        if (state instanceof HitColorState hs && hs.jestronpriv$hitColor() && state.hasRedOverlay) {
            Integer v = cir.getReturnValue();
            if (v != null) cir.setReturnValue(HitColor.remap(v));
        }
    }
}
