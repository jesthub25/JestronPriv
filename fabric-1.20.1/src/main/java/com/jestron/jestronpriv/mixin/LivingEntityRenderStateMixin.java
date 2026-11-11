package com.jestron.jestronpriv.mixin;

import com.jestron.jestronpriv.client.pvp.HitColorState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Render durumuna "ozel vurulma rengi" bayragi ekler. */
@Mixin(LivingEntityRenderState.class)
public abstract class LivingEntityRenderStateMixin implements HitColorState {

    @Unique
    private boolean jestronpriv$hit;

    @Override
    public boolean jestronpriv$hitColor() {
        return jestronpriv$hit;
    }

    @Override
    public void jestronpriv$setHitColor(boolean v) {
        jestronpriv$hit = v;
    }
}
