package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.JestronPrivClient;
import net.minecraft.client.CameraType;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** "On gorunumu atla": F5 birinci sahis -> arka -> birinci sahis dongusu. */
@Mixin(Options.class)
public abstract class OptionsCameraMixin {

    @ModifyVariable(method = "setCameraType(Lnet/minecraft/client/CameraType;)V", at = @At("HEAD"), argsOnly = true)
    private CameraType jestronpriv$skipFront(CameraType type) {
        try {
            JestronPrivClient mod = JestronPrivClient.get();
            if (type == CameraType.THIRD_PERSON_FRONT && mod != null && mod.config().freeCam && mod.config().freeCamSkipFront) {
                return CameraType.FIRST_PERSON;
            }
        } catch (Throwable ignored) {
        }
        return type;
    }
}
