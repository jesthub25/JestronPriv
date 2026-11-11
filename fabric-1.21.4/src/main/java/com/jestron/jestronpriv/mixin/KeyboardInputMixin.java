package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.camera.FreeCam;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Serbest 3. sahis kamera: hareket tuslari kameraya gore calisir. */
@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin {

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void jestronpriv$cameraRelative(CallbackInfo ci) {
        try {
            FreeCam.onInput((ClientInput) (Object) this);
        } catch (Throwable ignored) {
        }
    }
}
