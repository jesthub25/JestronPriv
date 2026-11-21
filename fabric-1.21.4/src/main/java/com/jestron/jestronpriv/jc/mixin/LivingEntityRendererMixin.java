package com.jestron.jestronpriv.jc.mixin;

import com.jestron.jestronpriv.jc.core.Modules;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** İsim üstü can: oyuncunun isim etiketine canı eklenir. */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
            at = @At("TAIL"))
    private void jestron$nametagHealth(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo ci) {
        try {
            if (state.nameTag != null) state.nameTag = Modules.NAMETAG_HEALTH.decorate(entity, state.nameTag);
        } catch (Throwable ignored) {
        }
    }
}
