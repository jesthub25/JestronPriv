package com.jestron.jestronpriv.mixin;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.BakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Esya gorunumu: katmanin modeli (konum / donus ve isik bilgisi icin). */
@Mixin(ItemStackRenderState.LayerRenderState.class)
public interface LayerRenderStateAccessor {

    @Accessor("model")
    BakedModel jestronpriv$model();
}
