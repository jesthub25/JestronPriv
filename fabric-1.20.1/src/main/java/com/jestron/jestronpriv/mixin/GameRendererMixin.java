package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.view.ColorGrade;
import com.jestron.jestronpriv.client.view.ScreenTweaks;
import com.jestron.jestronpriv.client.view.ViewModel;
import com.jestron.jestronpriv.client.view.Zoom;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** El FOV'u, yakinlastirma, ekran orani ve renk duzenleme. */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow
    private boolean panoramicMode;

    @Shadow
    @Final
    private CrossFrameResourcePool resourcePool;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private float fovModifier;

    @Shadow
    private float oldFovModifier;

    /** FOV gecis yumusakligi. */
    @Inject(method = "tickFov", at = @At("TAIL"))
    private void jestronpriv$fovSmooth(CallbackInfo ci) {
        try {
            fovModifier = com.jestron.jestronpriv.client.view.FovControl.smooth(oldFovModifier, fovModifier);
        } catch (Throwable ignored) {
        }
    }

    /** useFovSetting=false: el cizimi (sabit 70) -> el FOV'u; true: oyun FOV'u -> yakinlastirma. */
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void jestronpriv$fov(Camera camera, float partialTick, boolean useFovSetting, CallbackInfoReturnable<Float> cir) {
        if (panoramicMode) return;
        try {
            float f = cir.getReturnValue();
            float r = useFovSetting ? com.jestron.jestronpriv.client.view.FovControl.water(f, camera) / Zoom.factor() : ViewModel.handFov(f);
            if (r != f && r > 1F && r < 179F) cir.setReturnValue(r);
        } catch (Throwable ignored) {
        }
    }

    @ModifyArg(method = "getProjectionMatrix", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;perspective(FFFF)Lorg/joml/Matrix4f;", remap = false), index = 1)
    private float jestronpriv$aspect(float aspect) {
        if (panoramicMode) return aspect;
        try {
            return ScreenTweaks.aspect(aspect);
        } catch (Throwable t) {
            return aspect;
        }
    }

    /** Ekrandaki buyuk totem animasyonu (istenirse gizlenir). */
    @Inject(method = "displayItemActivation", at = @At("HEAD"), cancellable = true)
    private void jestronpriv$itemActivation(net.minecraft.world.item.ItemStack stack, CallbackInfo ci) {
        try {
            if (com.jestron.jestronpriv.client.pvp.TotemFx.hideScreen(stack)) ci.cancel();
        } catch (Throwable ignored) {
        }
    }

    /** Dunya + el cizildi, arayuz henuz cizilmedi. */
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;doEntityOutline()V", shift = At.Shift.AFTER))
    private void jestronpriv$grade(DeltaTracker delta, boolean renderLevel, CallbackInfo ci) {
        try {
            ColorGrade.apply(minecraft, resourcePool);
        } catch (Throwable ignored) {
            // renk duzenleme asla kareyi bozmasin
        }
    }
}
