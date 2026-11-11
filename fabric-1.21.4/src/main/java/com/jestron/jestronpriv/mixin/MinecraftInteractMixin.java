package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.camera.FreeCam;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Serbest 3. sahis kamera: vurmadan / kullanmadan once oyuncu kameranin nisan aldigi yere doner. */
@Mixin(Minecraft.class)
public abstract class MinecraftInteractMixin {

    @Inject(method = "startAttack()Z", at = @At("HEAD"))
    private void jestronpriv$aimAttack(CallbackInfoReturnable<Boolean> cir) {
        aim();
    }

    @Inject(method = "startUseItem()V", at = @At("HEAD"))
    private void jestronpriv$aimUse(CallbackInfo ci) {
        aim();
    }

    @Inject(method = "continueAttack(Z)V", at = @At("HEAD"))
    private void jestronpriv$aimMine(boolean attacking, CallbackInfo ci) {
        if (attacking) aim();
    }

    private static void aim() {
        try {
            FreeCam.beforeInteract();
        } catch (Throwable ignored) {
        }
    }
}
