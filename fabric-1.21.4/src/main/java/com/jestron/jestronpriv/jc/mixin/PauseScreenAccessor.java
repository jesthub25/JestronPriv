package com.jestron.jestronpriv.jc.mixin;

import net.minecraft.client.gui.screens.PauseScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PauseScreen.class)
public interface PauseScreenAccessor {
    /** false: F3+Esc ile açılan menüsüz duraklatma; o durumda değiştirilmez. */
    @Accessor("showPauseMenu")
    boolean jestron$showPauseMenu();
}
