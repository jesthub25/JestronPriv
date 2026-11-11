package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.look.Glint;
import net.minecraft.client.renderer.RenderStateShard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Buyu parlamasi: doku matrisi kurulurken renk / hiz / boyut / egim (kapaliysa vanilla aynen). */
@Mixin(RenderStateShard.class)
public abstract class GlintTexturingMixin {

    @Inject(method = "setupGlintTexturing", at = @At("HEAD"), cancellable = true)
    private static void jestronpriv$glint(float scale, CallbackInfo ci) {
        try {
            if (Glint.setup(scale)) ci.cancel();
        } catch (Throwable ignored) {
            // hata olursa vanilla parlama
        }
    }
}
