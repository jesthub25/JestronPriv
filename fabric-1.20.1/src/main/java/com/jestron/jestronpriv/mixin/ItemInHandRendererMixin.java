package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.anim.SwingAnimator;
import com.jestron.jestronpriv.client.view.ViewModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    /** Ana eldeki esyanin vurus animasyonu: vanilla swingArm yerine secili stil. */
    @Inject(method = "swingArm", at = @At("HEAD"), cancellable = true)
    private void jestronpriv$swing(float swingProgress, float equipProgress, PoseStack poseStack, int armDir,
                                   HumanoidArm arm, CallbackInfo ci) {
        try {
            float p = SwingAnimator.progress(swingProgress);
            if (SwingAnimator.shouldApply(arm, p)) {
                SwingAnimator.apply(poseStack, arm, p, equipProgress);
                ci.cancel();
            }
        } catch (Throwable ignored) {
            // hata olursa vanilla vurus
        }
    }

    /** Sol el gizleme (kullanilirken gorunur kalir). */
    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void jestronpriv$hideOffhand(AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand,
                                         float swingProgress, ItemStack stack, float equipProgress, PoseStack poseStack,
                                         MultiBufferSource buffers, int light, CallbackInfo ci) {
        SwingAnimator.partial = partialTick;
        try {
            if (ViewModel.hideOffhand(player, hand)) ci.cancel();
        } catch (Throwable ignored) {
        }
    }

    /** El gorunumu: kol konumu (bos el ve esya birlikte). */
    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift = At.Shift.AFTER))
    private void jestronpriv$arm(AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand,
                                 float swingProgress, ItemStack stack, float equipProgress, PoseStack poseStack,
                                 MultiBufferSource buffers, int light, CallbackInfo ci) {
        try {
            ViewModel.arm(poseStack, player, hand, partialTick);
        } catch (Throwable ignored) {
        }
    }

    /** El gorunumu: esya konumu / donusu / boyutu (cizimden hemen once). */
    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    private void jestronpriv$item(AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand,
                                  float swingProgress, ItemStack stack, float equipProgress, PoseStack poseStack,
                                  MultiBufferSource buffers, int light, CallbackInfo ci) {
        try {
            ViewModel.item(poseStack, player, hand);
        } catch (Throwable ignored) {
        }
    }
}
