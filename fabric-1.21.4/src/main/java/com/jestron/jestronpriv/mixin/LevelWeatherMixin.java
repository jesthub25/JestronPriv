package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.view.WorldTime;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Sadece istemci dunyasinda (ClientLevel) gorunen hava; tek oyunculu sunucu dunyasi etkilenmez. Hava olmayan
 * boyutlarda (Nether, End) dokunulmaz.
 */
@Mixin(Level.class)
public abstract class LevelWeatherMixin {

    @Shadow
    private boolean canHaveWeather() {
        throw new AssertionError();
    }

    @Inject(method = "getRainLevel", at = @At("RETURN"), cancellable = true)
    private void jestronpriv$rain(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (!((Object) this instanceof ClientLevel)) return;
        try {
            if (!canHaveWeather()) return;
            Float rv = cir.getReturnValue();
            float real = rv == null ? 0f : rv;
            float v = WorldTime.rain(real);
            if (v != real) cir.setReturnValue(v);
        } catch (Throwable ignored) {
        }
    }

    @Inject(method = "getThunderLevel", at = @At("RETURN"), cancellable = true)
    private void jestronpriv$thunder(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (!((Object) this instanceof ClientLevel)) return;
        try {
            if (!canHaveWeather()) return;
            Float rv = cir.getReturnValue();
            float real = rv == null ? 0f : rv;
            float v = WorldTime.thunder(real);
            if (v != real) cir.setReturnValue(v);
        } catch (Throwable ignored) {
        }
    }
}
