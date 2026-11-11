package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.look.Glint;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Parlama cizimi bitince (doku matrisi sifirlanir) renk carpanini geri al. Sadece bizim kurdugumuz renk geri alinir. */
@Mixin(RenderSystem.class)
public abstract class RenderSystemGlintMixin {

    // RenderSystem (com.mojang.blaze3d) oyunda karistirilmamis adlarla gelir: eslesme tablosunda yok, adi oldugu gibi kullanilir
    @Inject(method = "resetTextureMatrix", at = @At("HEAD"), remap = false)
    private static void jestronpriv$glintReset(CallbackInfo ci) {
        try {
            Glint.reset();
        } catch (Throwable ignored) {
        }
    }
}
