package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.view.Zoom;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Yakinlastirma basiliyken fare tekerlegi hotbar yerine yakinlik seviyesini degistirir. */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void jestronpriv$scroll(long window, double xOffset, double yOffset, CallbackInfo ci) {
        try {
            if (Zoom.onScroll(yOffset)) ci.cancel();
        } catch (Throwable ignored) {
        }
    }
}
