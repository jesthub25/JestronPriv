package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.anim.SwingAnimator;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    /** Sadece yerel oyuncunun el sallama suresi ayarlanan carpanla olceklenir. */
    @Inject(method = "getCurrentSwingDuration", at = @At("RETURN"), cancellable = true)
    private void jestronpriv$swingDuration(CallbackInfoReturnable<Integer> cir) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.player != null && (Object) this == mc.player) {
                int v = cir.getReturnValue();
                int scaled = SwingAnimator.scaleDuration(v);
                if (scaled != v) cir.setReturnValue(scaled);
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * Sunucunun gonderdigi olum olayi (id 3). Can gizlense veya olum mesaji kapatilsa bile gelir;
     * oldurme tespitinin ana sinyalidir.
     */
    @Inject(method = "handleEntityEvent", at = @At("HEAD"))
    private void jestronpriv$deathEvent(byte id, CallbackInfo ci) {
        if (id != 3) return;
        try {
            LivingEntity self = (LivingEntity) (Object) this;
            if (!self.level().isClientSide()) return;
            JestronPrivClient mod = JestronPrivClient.get();
            if (mod != null && mod.detector() != null) mod.detector().onDeathEvent(self);
            com.jestron.jestronpriv.client.pvp.TotemFx.onDeath(self.getId());
        } catch (Throwable ignored) {
        }
    }
}
