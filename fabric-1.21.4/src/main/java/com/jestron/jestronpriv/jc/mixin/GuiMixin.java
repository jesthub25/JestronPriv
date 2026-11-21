package com.jestron.jestronpriv.jc.mixin;

import com.jestron.jestronpriv.jc.core.Hud;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** HUD öğeleri tüm arayüzün üstünde çizilir (F1 ile gizlenir, HUD düzenleyici açıkken çizilmez). */
@Mixin(Gui.class)
public abstract class GuiMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void jestron$hud(GuiGraphics g, DeltaTracker delta, CallbackInfo ci) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.options.hideGui || mc.player == null) return;
            if (mc.getDebugOverlay().showDebugScreen()) return; // F3 açıkken üst üste binmesin
            if (mc.screen instanceof com.jestron.jestronpriv.jc.gui.HudEditorScreen) return;
            g.pose().pushPose();
            g.pose().translate(0f, 0f, 850f);
            try {
                Hud.renderAll(g);
            } finally {
                g.pose().popPose();
            }
        } catch (Throwable ignored) {
        }
    }

    /** Ekran ortasındaki büyük yazı (minigame kazananı vb.) otomatik GG'ye iletilir. */
    @Inject(method = "setTitle", at = @At("HEAD"))
    private void jestron$title(net.minecraft.network.chat.Component title, CallbackInfo ci) {
        try {
            com.jestron.jestronpriv.jc.core.Modules.AUTO_GG.onTitle(title);
        } catch (Throwable ignored) {
        }
    }
}
