package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.hud.PickupHud;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Envanter ekranindan atma: Q / Ctrl+Q ile yuvadan, ya da tasinan esyayi pencere disina birakma. */
@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {

    @Inject(method = "handleInventoryMouseClick", at = @At("HEAD"))
    private void jestronpriv$throw(int containerId, int slotId, int button, ClickType type, Player player, CallbackInfo ci) {
        try {
            AbstractContainerMenu menu = player.containerMenu;
            if (menu == null || containerId != menu.containerId) return;
            if (type == ClickType.THROW && slotId >= 0 && slotId < menu.slots.size()) {
                if (!menu.getCarried().isEmpty()) return;   // tasinirken Q atmaz
                ItemStack s = menu.getSlot(slotId).getItem();
                if (!s.isEmpty()) PickupHud.onDrop(s, button == 1 ? s.getCount() : 1);
            } else if (type == ClickType.PICKUP && slotId == AbstractContainerMenu.SLOT_CLICKED_OUTSIDE) {
                ItemStack s = menu.getCarried();
                if (!s.isEmpty()) PickupHud.onDrop(s, button == 0 ? s.getCount() : 1);
            }
        } catch (Throwable ignored) {
        }
    }
}
