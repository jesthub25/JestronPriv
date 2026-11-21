package com.jestron.jestronpriv.jc.mixin;

import com.jestron.jestronpriv.jc.JestronClient;
import com.jestron.jestronpriv.jc.gui.JestronPauseScreen;
import com.jestron.jestronpriv.jc.gui.PauseStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Vanilla ESC menüsü açılırken yerine Jestron ESC menüsü açılır (ayar: Görünüm › ESC menüsü). */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen jestron$pauseScreen(Screen screen) {
        if (!(screen instanceof PauseScreen ps)) return screen;
        if (JestronPauseScreen.bypass) {
            JestronPauseScreen.bypass = false;
            return screen;
        }
        try {
            if (PauseStyle.enabled() && ((PauseScreenAccessor) ps).jestron$showPauseMenu()) return new JestronPauseScreen();
        } catch (Throwable t) {
            JestronClient.LOG.warn("[Jestron] ESC menüsü açılamadı, vanilla menü kullanılıyor", t);
        }
        return screen;
    }
}
