package com.jestron.jestronpriv.jc.mixin;

import com.jestron.jestronpriv.jc.core.Modules;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Sohbet saati: mesaj sohbet geçmişine eklenirken başına saat konur. */
@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
    @ModifyVariable(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            at = @At("HEAD"), argsOnly = true)
    private Component jestron$time(Component message) {
        try {
            return Modules.CHAT_TIME.decorate(message);
        } catch (Throwable t) {
            return message;
        }
    }
}
