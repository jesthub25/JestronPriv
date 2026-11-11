package com.jestron.jestronpriv.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Blok kirma ilerlemesi (secim kutusu dolgusu icin, sadece okunur). */
@Mixin(MultiPlayerGameMode.class)
public interface MultiPlayerGameModeAccessor {

    @Accessor("destroyProgress")
    float jestronpriv$destroyProgress();

    @Accessor("isDestroying")
    boolean jestronpriv$isDestroying();

    @Accessor("destroyBlockPos")
    BlockPos jestronpriv$destroyBlockPos();
}
