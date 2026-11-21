package com.jestron.jestronpriv.jc.mixin;

import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Tam parlaklık için parlaklık değerini doğrulama sınırını aşarak yazar. */
@Mixin(OptionInstance.class)
public interface OptionInstanceAccessor {
    @Accessor("value")
    void jestron$setValue(Object value);
}
