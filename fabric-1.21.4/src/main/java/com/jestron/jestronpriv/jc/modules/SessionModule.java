package com.jestron.jestronpriv.jc.modules;

import com.jestron.jestronpriv.jc.core.Category;
import com.jestron.jestronpriv.jc.core.TextHud;
import com.jestron.jestronpriv.jc.util.TextUtil;
import net.minecraft.client.Minecraft;

public final class SessionModule extends TextHud {
    /** Dünyaya / sunucuya girildiği an (JestronClient ayarlar). */
    public static long joinedAt;

    public SessionModule() {
        super("session", "Oturum süresi", "Bu dünyada / sunucuda geçen süre.", Category.HUD, false, 0.72, 0.325);
    }

    @Override
    protected String[] lines(boolean preview) {
        if (Minecraft.getInstance().player == null || joinedAt == 0) return preview ? new String[]{kv("Oturum", "12:34")} : new String[0];
        return new String[]{kv("Oturum", TextUtil.duration(System.currentTimeMillis() - joinedAt))};
    }
}
