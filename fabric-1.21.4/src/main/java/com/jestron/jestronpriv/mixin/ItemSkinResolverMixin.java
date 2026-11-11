package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.itemskin.ItemSkins;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Esya gorunumu: model cozumlenince (elde, GUI, yerde, cercevede...) kendi cizimin. */
@Mixin(ItemModelResolver.class)
public abstract class ItemSkinResolverMixin {

    @Inject(method = "appendItemLayers", at = @At("HEAD"))
    private void jestronpriv$skinHead(ItemStackRenderState state, ItemStack stack, ItemDisplayContext ctx, Level level, LivingEntity entity, int seed,
                                      CallbackInfo ci) {
        try {
            ItemSkins.beginAppend(state);
        } catch (Throwable ignored) {
        }
    }

    @Inject(method = "appendItemLayers", at = @At("TAIL"))
    private void jestronpriv$skinTail(ItemStackRenderState state, ItemStack stack, ItemDisplayContext ctx, Level level, LivingEntity entity, int seed,
                                      CallbackInfo ci) {
        try {
            ItemSkins.endAppend(state, stack, (ItemModelResolver) (Object) this);
        } catch (Throwable ignored) {
            // gorunum hatasi: vanilla katmanlar kalir
        }
    }
}
