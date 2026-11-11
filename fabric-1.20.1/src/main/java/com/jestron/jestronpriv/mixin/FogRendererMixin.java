package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.look.Fog;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Sis: sadece acik hava sisi (su / lav / toz kar / korluk / karanlik sisine dokunulmaz). */
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @Inject(method = "setupFog", at = @At("RETURN"), cancellable = true)
    private static void jestronpriv$fog(Camera camera, FogRenderer.FogMode mode, Vector4f color, float renderDistance, boolean thick, float partialTick,
                                        CallbackInfoReturnable<FogParameters> cir) {
        try {
            FogParameters in = cir.getReturnValue();
            FogParameters out = Fog.adjust(camera, mode, renderDistance, thick, in);
            if (out != in) cir.setReturnValue(out);
        } catch (Throwable ignored) {
        }
    }

    @Inject(method = "computeFogColor", at = @At("RETURN"))
    private static void jestronpriv$fogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darken,
                                             CallbackInfoReturnable<Vector4f> cir) {
        try {
            Fog.color(camera, level, cir.getReturnValue());
        } catch (Throwable ignored) {
        }
    }
}
