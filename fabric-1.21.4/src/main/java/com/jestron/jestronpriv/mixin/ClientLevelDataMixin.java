package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.view.WorldTime;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Sadece istemcide gorunen gun saati (gok, isik, ay). */
@Mixin(ClientLevel.ClientLevelData.class)
public abstract class ClientLevelDataMixin {

    @Inject(method = "getDayTime", at = @At("RETURN"), cancellable = true)
    private void jestronpriv$dayTime(CallbackInfoReturnable<Long> cir) {
        try {
            long real = cir.getReturnValue();
            long v = WorldTime.dayTime(real);
            if (v != real) cir.setReturnValue(v);
        } catch (Throwable ignored) {
        }
    }
}
