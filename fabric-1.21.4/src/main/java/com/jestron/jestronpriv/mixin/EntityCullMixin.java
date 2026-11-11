package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.view.Culler;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Ekranda olsa bile duvar arkasinda kalan varliklar cizilmez. */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityCullMixin {

    @Inject(method = "shouldRender", at = @At("RETURN"), cancellable = true)
    private void jestronpriv$cull(Entity entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        Boolean r = cir.getReturnValue();
        if (r != null && r && Culler.cullEntity(entity)) cir.setReturnValue(false);
    }
}
