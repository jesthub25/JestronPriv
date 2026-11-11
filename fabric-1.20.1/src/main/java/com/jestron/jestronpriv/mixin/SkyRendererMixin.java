package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.look.Sky;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SkyRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ozel gokyuzu: gunes / ay boyutu (sadece yatay olcek: uzaklik ayni kalir), ay evresi, yildiz parlakligi, gun batimi
 * rengi, ardindan kutup isigi ve kayan yildizlar. Ayar kapaliyken her sey vanilla.
 */
@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {

    @Unique
    private static boolean jestronpriv$sunPushed, jestronpriv$moonPushed;

    @Inject(method = "renderSun", at = @At("HEAD"))
    private void jestronpriv$sunHead(float alpha, MultiBufferSource buffers, PoseStack ps, CallbackInfo ci) {
        jestronpriv$sunPushed = false;
        try {
            float s = Sky.sunScale();
            if (s != 1f && Float.isFinite(s)) {
                ps.pushPose();
                ps.scale(s, 1f, s);
                jestronpriv$sunPushed = true;
            }
        } catch (Throwable ignored) {
        }
    }

    @Inject(method = "renderSun", at = @At("TAIL"))
    private void jestronpriv$sunTail(float alpha, MultiBufferSource buffers, PoseStack ps, CallbackInfo ci) {
        if (jestronpriv$sunPushed) {
            jestronpriv$sunPushed = false;
            ps.popPose();
        }
    }

    @Inject(method = "renderMoon", at = @At("HEAD"))
    private void jestronpriv$moonHead(int phase, float alpha, MultiBufferSource buffers, PoseStack ps, CallbackInfo ci) {
        jestronpriv$moonPushed = false;
        try {
            float s = Sky.moonScale();
            if (s != 1f && Float.isFinite(s)) {
                ps.pushPose();
                ps.scale(s, 1f, s);
                jestronpriv$moonPushed = true;
            }
        } catch (Throwable ignored) {
        }
    }

    @Inject(method = "renderMoon", at = @At("TAIL"))
    private void jestronpriv$moonTail(int phase, float alpha, MultiBufferSource buffers, PoseStack ps, CallbackInfo ci) {
        if (jestronpriv$moonPushed) {
            jestronpriv$moonPushed = false;
            ps.popPose();
        }
    }

    @ModifyVariable(method = "renderSunriseAndSunset", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int jestronpriv$sunset(int color) {
        try {
            return Sky.sunsetColor(color);
        } catch (Throwable t) {
            return color;
        }
    }

    @ModifyVariable(method = "renderSunMoonAndStars", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int jestronpriv$moonPhase(int phase) {
        try {
            return Sky.moonPhase(phase);
        } catch (Throwable t) {
            return phase;
        }
    }

    @ModifyVariable(method = "renderSunMoonAndStars", at = @At("HEAD"), argsOnly = true, ordinal = 2)
    private float jestronpriv$stars(float starBrightness) {
        try {
            return Sky.starBrightness(starBrightness);
        } catch (Throwable t) {
            return starBrightness;
        }
    }

    @Inject(method = "renderSunMoonAndStars", at = @At("TAIL"))
    private void jestronpriv$extras(PoseStack ps, MultiBufferSource.BufferSource buffers, float timeOfDay, int phase, float rainBrightness,
                                    float starBrightness, FogParameters fog, CallbackInfo ci) {
        try {
            Sky.renderExtras(ps, buffers, timeOfDay, rainBrightness, fog);
        } catch (Throwable ignored) {
            // gok efekti asla oyunu bozmasin
        }
    }
}
