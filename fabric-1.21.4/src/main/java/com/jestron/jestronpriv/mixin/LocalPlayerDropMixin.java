package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.hud.PickupHud;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Q ile atilan esya (alinan / atilan bildirimi). */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerDropMixin {

    @Inject(method = "drop", at = @At("HEAD"))
    private void jestronpriv$drop(boolean fullStack, CallbackInfoReturnable<Boolean> cir) {
        try {
            ItemStack s = ((LocalPlayer) (Object) this).getInventory().getSelected();
            if (s != null && !s.isEmpty()) PickupHud.onDrop(s, fullStack ? s.getCount() : 1);
        } catch (Throwable ignored) {
        }
    }
}
