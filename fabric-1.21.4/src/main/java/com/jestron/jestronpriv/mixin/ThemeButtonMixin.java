package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.gui.Theme;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Function;

/** JestronPriv ekranlarinda dugme arka plani menu temasiyla (seffaf) cizilir. Diger menulere dokunulmaz. */
@Mixin(AbstractButton.class)
public abstract class ThemeButtonMixin {

    @WrapOperation(method = "renderWidget", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Ljava/util/function/Function;Lnet/minecraft/resources/ResourceLocation;IIIII)V"))
    private void jestronpriv$themeButton(GuiGraphics g, Function<ResourceLocation, RenderType> fn, ResourceLocation sprite, int x, int y, int w, int h,
                                         int color, Operation<Void> original) {
        boolean done = false;
        try {
            AbstractWidget self = (AbstractWidget) (Object) this;
            done = Theme.button(g, x, y, w, h, self.active, self.isHoveredOrFocused());
        } catch (Throwable ignored) {
        }
        if (!done) original.call(g, fn, sprite, x, y, w, h, color);
    }
}
