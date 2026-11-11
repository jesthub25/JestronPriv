package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.view.ScreenTweaks;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Dusuk ates: birinci sahis ates katmanini asagi kaydirir ve saydamlastirir. */
@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {

    @ModifyArg(method = "renderFire", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"), index = 1)
    private static float jestronpriv$fireY(float y) {
        return ScreenTweaks.fireY(y);
    }

    @ModifyArg(method = "renderFire", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;setColor(FFFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;"), index = 3)
    private static float jestronpriv$fireAlpha(float alpha) {
        return ScreenTweaks.fireAlpha(alpha);
    }
}
