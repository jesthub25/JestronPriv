package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.view.Culler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Duvar arkasindaki sandik, tabela, sancak, kafa... cizilmez (fener gibi uzaktan gorunenler haric). */
@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityCullMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void jestronpriv$cull(BlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffers, CallbackInfo ci) {
        if (!Culler.blocksActive()) return;
        try {
            BlockEntityRenderer<BlockEntity> r = ((BlockEntityRenderDispatcher) (Object) this).getRenderer(be);
            if (r == null) return;
            if (Culler.cullBlock(be, r.shouldRenderOffScreen(be))) ci.cancel();
        } catch (Throwable ignored) {
        }
    }
}
