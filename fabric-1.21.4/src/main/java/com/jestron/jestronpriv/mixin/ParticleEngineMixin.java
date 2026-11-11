package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.cosmetic.HitFx;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Kritik / buyu vurusu yayicisi (sunucunun animasyon paketi): senin vurusunsa ozel stil. */
@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {

    @Inject(method = "createTrackingEmitter(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/particles/ParticleOptions;)V",
            at = @At("HEAD"), cancellable = true)
    private void jestronpriv$hitEmitter(Entity entity, ParticleOptions options, CallbackInfo ci) {
        try {
            if (HitFx.onTrackingEmitter(entity, options)) ci.cancel();
        } catch (Throwable ignored) {
        }
    }

    /** Totem yayicisi (30 tick): istenirse vanilla totem partikulu gizlenir (ozel efekt kalir). */
    @Inject(method = "createTrackingEmitter(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/particles/ParticleOptions;I)V",
            at = @At("HEAD"), cancellable = true)
    private void jestronpriv$totemEmitter(Entity entity, ParticleOptions options, int lifetime, CallbackInfo ci) {
        try {
            if (com.jestron.jestronpriv.client.pvp.TotemFx.hideVanillaParticles(options)) ci.cancel();
        } catch (Throwable ignored) {
        }
    }
}
