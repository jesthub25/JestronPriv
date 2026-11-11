package com.jestron.jestronpriv.mixin;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Esya gorunumu: eklenen katmanlari ozel katmanla degistirmek icin. */
@Mixin(ItemStackRenderState.class)
public interface ItemStackRenderStateAccessor {

    @Accessor("activeLayerCount")
    int jestronpriv$count();

    @Accessor("activeLayerCount")
    void jestronpriv$setCount(int count);

    @Accessor("layers")
    ItemStackRenderState.LayerRenderState[] jestronpriv$layers();
}
