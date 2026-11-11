package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.JestronPrivClient;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {

    /** Varlik silinmeden hemen once: aninda respawn eden sunucularda olen oyuncu burada kaybolur. */
    @Inject(method = "removeEntity", at = @At("HEAD"))
    private void jestronpriv$beforeRemove(int id, Entity.RemovalReason reason, CallbackInfo ci) {
        try {
            JestronPrivClient mod = JestronPrivClient.get();
            if (mod == null || mod.detector() == null) return;
            Entity e = ((ClientLevel) (Object) this).getEntity(id);
            if (e != null) mod.detector().onRemoved(e);
        } catch (Throwable ignored) {
        }
    }

    /** Istemcinin kendi saat ilerletmesi gercek saati okusun/yazsin (gorsel saat ayari onu bozmasin). */
    @Inject(method = "tickTime", at = @At("HEAD"))
    private void jestronpriv$tickTimeStart(CallbackInfo ci) {
        com.jestron.jestronpriv.client.view.WorldTime.setRaw(true);
    }

    @Inject(method = "tickTime", at = @At("RETURN"))
    private void jestronpriv$tickTimeEnd(CallbackInfo ci) {
        com.jestron.jestronpriv.client.view.WorldTime.setRaw(false);
    }
}
