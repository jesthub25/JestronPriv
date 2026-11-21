package com.jestron.jestronpriv.jc.mixin;

import com.jestron.jestronpriv.jc.util.Clicks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** CPS: oyunda (menü kapalıyken) basılan fare tuşları sayılır. */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Inject(method = "onPress", at = @At("HEAD"))
    private void jestron$click(long window, int button, int action, int mods, CallbackInfo ci) {
        try {
            if (action == 1 && Minecraft.getInstance().screen == null) Clicks.press(button);
        } catch (Throwable ignored) {
        }
    }
}
