package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.look.Sky;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Gok rengi tonu (ozel gokyuzu acik ve ton secili ise). */
@Mixin(ClientLevel.class)
public abstract class SkyColorMixin {

    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
    private void jestronpriv$skyTint(Vec3 pos, float partialTick, CallbackInfoReturnable<Integer> cir) {
        try {
            int v = cir.getReturnValueI();
            int n = Sky.skyColor(v);
            if (n != v) cir.setReturnValue(n);
        } catch (Throwable ignored) {
        }
    }
}
