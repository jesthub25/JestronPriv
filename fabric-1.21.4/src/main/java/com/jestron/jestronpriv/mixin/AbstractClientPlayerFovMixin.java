package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.view.FovControl;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Ozel FOV etkileri: kosma, hiz iksiri, ucma, yay ayri ayri olceklenir. */
@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerFovMixin {

    @Inject(method = "getFieldOfViewModifier", at = @At("HEAD"), cancellable = true)
    private void jestronpriv$fov(boolean firstPerson, float effectScale, CallbackInfoReturnable<Float> cir) {
        try {
            float v = FovControl.modifier((AbstractClientPlayer) (Object) this, firstPerson);
            if (!Float.isNaN(v)) cir.setReturnValue(v);
        } catch (Throwable ignored) {
        }
    }
}
