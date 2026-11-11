package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.JestronPrivClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

    /** Sunucu basliklari: bircok PvP sunucusu oldurmeyi ekranin ortasinda gosterir ("X oldurdun!"). */
    @Inject(method = "setTitle", at = @At("HEAD"))
    private void jestronpriv$title(Component title, CallbackInfo ci) {
        scan(title);
    }

    @Inject(method = "setSubtitle", at = @At("HEAD"))
    private void jestronpriv$subtitle(Component subtitle, CallbackInfo ci) {
        scan(subtitle);
    }

    /** Kombo sayaci ve seri oldurme duyurusu: tum arayuzun ustunde (F1 ile gizlenir). */
    @Inject(method = "render", at = @At("TAIL"))
    private void jestronpriv$hud(GuiGraphics g, DeltaTracker delta, CallbackInfo ci) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.options.hideGui) return;
            float pt = delta.getGameTimeDeltaPartialTick(true);
            // diger arayuz katmanlarinin (baslik, esya) ustunde kalsin
            g.pose().pushPose();
            g.pose().translate(0f, 0f, 900f);
            try {
                com.jestron.jestronpriv.client.hud.Hud.render(g, pt);
            } finally {
                g.pose().popPose();
            }
        } catch (Throwable ignored) {
        }
    }

    /** Iksir paneli acikken vanilla sag-ust iksir ikonlari (istenirse) cizilmez. */
    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    private void jestronpriv$effects(GuiGraphics g, DeltaTracker delta, CallbackInfo ci) {
        try {
            JestronPrivClient m = JestronPrivClient.get();
            if (m != null && com.jestron.jestronpriv.client.hud.PotionHud.hideVanilla(m.config())) ci.cancel();
        } catch (Throwable ignored) {
        }
    }

    private static void scan(Component c) {
        try {
            JestronPrivClient mod = JestronPrivClient.get();
            if (mod != null && mod.detector() != null && c != null) mod.detector().onMessage(c);
        } catch (Throwable ignored) {
        }
    }
}
