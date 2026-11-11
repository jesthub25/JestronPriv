package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.camera.FreeCam;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Function;

/** Serbest 3. sahis kamerada nisangah: ekranin ortasi = kameranin nisan aldigi nokta. */
@Mixin(Gui.class)
public abstract class CrosshairMixin {

    @Redirect(method = "renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z"))
    private boolean jestronpriv$crosshair(CameraType type) {
        if (type.isFirstPerson()) return true;
        try {
            return FreeCam.showCrosshair();
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Ozel nisangah: vanilla nisangah dokusu yerine ciz. Ayni imzali diger cagrilar (saldiri gostergesi) aynen
     * gecer; sadece "hud/crosshair" dokusu degistirilir.
     */
    @Redirect(method = "renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Ljava/util/function/Function;Lnet/minecraft/resources/ResourceLocation;IIII)V"))
    private void jestronpriv$crosshairSprite(GuiGraphics g, Function<ResourceLocation, RenderType> type, ResourceLocation sprite,
                                             int x, int y, int w, int h) {
        try {
            if (com.jestron.jestronpriv.client.pvp.Crosshair.active() && sprite != null && "hud/crosshair".equals(sprite.getPath())) {
                com.jestron.jestronpriv.client.pvp.Crosshair.drawInGame(g);
                return;
            }
        } catch (Throwable ignored) {
            // hata olursa vanilla nisangah cizilir
        }
        g.blitSprite(type, sprite, x, y, w, h);
    }
}
