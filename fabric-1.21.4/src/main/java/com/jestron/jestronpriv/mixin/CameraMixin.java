package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.camera.FreeCam;
import com.jestron.jestronpriv.client.camera.FreeLook;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Serbest 3. sahis kamera: arka gorunumde kamera acisi oyuncunun degil, kameranin kendi acisidir.
 * Uzaklik (duvara carpma) hesabindan once ayarlanir; boylece kamera duvarlarin icine girmez.
 */
@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Inject(method = "setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getMaxZoom(F)F"))
    private void jestronpriv$freeRotation(BlockGetter level, Entity entity, boolean detached, boolean mirrored, float partialTick,
                                          CallbackInfo ci) {
        try {
            if (!detached || mirrored) return;
            // bas-bak: bakis ya da eski yere yumusak donus (hedef: serbest kamera veya karakterin bakisi)
            float ty, tp;
            if (FreeCam.activeIgnoringLook()) {
                ty = FreeCam.yaw();
                tp = FreeCam.pitch();
            } else {
                ty = entity.getViewYRot(partialTick);
                tp = entity.getViewXRot(partialTick);
            }
            if (FreeLook.camera(ty, tp)) setRotation(FreeLook.yaw(), FreeLook.pitch());
            else if (FreeCam.sync()) setRotation(FreeCam.yaw(), FreeCam.pitch());
        } catch (Throwable ignored) {
        }
    }
}
