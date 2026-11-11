package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.camera.FreeCam;
import com.jestron.jestronpriv.client.camera.FreeLook;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Serbest 3. sahis kamera: fare hareketi oyuncu yerine kamerayi dondurur. */
@Mixin(Entity.class)
public abstract class EntityTurnMixin {

    /** Yakinlastirmada olceklenmis cagri icin tekrar giris korumasi (sadece render/istemci thread'i). */
    @org.spongepowered.asm.mixin.Unique
    private static boolean jestronpriv$scaled;

    @Inject(method = "turn(DD)V", at = @At("HEAD"), cancellable = true)
    private void jestronpriv$turn(double yRot, double xRot, CallbackInfo ci) {
        try {
            if ((Object) this != Minecraft.getInstance().player) return;
            // yakinlastirma: fare hassasiyeti yakinlik oraninda duser (nisan titremez)
            double k = com.jestron.jestronpriv.client.view.Zoom.mouseScale();
            if (k < 1.0 && !jestronpriv$scaled) {
                jestronpriv$scaled = true;
                try {
                    ((Entity) (Object) this).turn(yRot * k, xRot * k);
                } finally {
                    jestronpriv$scaled = false;
                }
                ci.cancel();
                return;
            }
            // once bas-bak (Alt), sonra serbest kamera
            if (FreeLook.onTurn(yRot, xRot) || FreeCam.onTurn(yRot, xRot)) ci.cancel();
        } catch (Throwable ignored) {
            // kamera hatasi asla fareyi kilitlemesin
        }
    }
}
