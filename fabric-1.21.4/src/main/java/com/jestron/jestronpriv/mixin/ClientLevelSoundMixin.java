package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.audio.CustomSound;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Ozel totem sesi secildiyse vanilla totem sesi (sadece o paket islenirken) calmaz. */
@Mixin(ClientLevel.class)
public abstract class ClientLevelSoundMixin {

    @Inject(method = "playLocalSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V", at = @At("HEAD"), cancellable = true)
    private void jestronpriv$mute(double x, double y, double z, SoundEvent sound, SoundSource source, float volume, float pitch,
                                  boolean distanceDelay, CallbackInfo ci) {
        try {
            if (CustomSound.shouldMute(sound)) ci.cancel();
        } catch (Throwable ignored) {
        }
    }
}
