package com.jestron.jestronpriv.jc.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Pencere başlığı: "Minecraft 1.21.4 - Tek Oyunculu" yerine "Efekt Menüsü 1.21.4 - Tek Oyunculu". */
@Mixin(Minecraft.class)
public abstract class MinecraftTitleMixin {
    @Inject(method = "createTitle", at = @At("RETURN"), cancellable = true)
    private void jestron$title(CallbackInfoReturnable<String> cir) {
        String s = cir.getReturnValue();
        if (s != null) cir.setReturnValue(s.replaceFirst("^Minecraft\\*?", "Efekt Menüsü"));
    }
}
