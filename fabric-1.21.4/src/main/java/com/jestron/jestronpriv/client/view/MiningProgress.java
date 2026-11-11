package com.jestron.jestronpriv.client.view;

import com.jestron.jestronpriv.mixin.MultiPlayerGameModeAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

/** Su an kirilan blok ve ilerlemesi (0-1); kirilmiyorsa -1. */
public final class MiningProgress {

    private static boolean failed;

    private MiningProgress() {
    }

    public static float progress(BlockPos pos) {
        if (failed || pos == null) return -1f;
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gameMode == null) return -1f;
            MultiPlayerGameModeAccessor a = (MultiPlayerGameModeAccessor) mc.gameMode;
            if (!a.jestronpriv$isDestroying()) return -1f;
            BlockPos d = a.jestronpriv$destroyBlockPos();
            if (d == null || !d.equals(pos)) return -1f;
            float p = a.jestronpriv$destroyProgress();
            return Float.isFinite(p) ? Math.max(0f, Math.min(1f, p)) : -1f;
        } catch (Throwable t) {
            failed = true;
            return -1f;
        }
    }
}
