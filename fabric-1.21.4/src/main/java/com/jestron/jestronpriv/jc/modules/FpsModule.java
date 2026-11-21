package com.jestron.jestronpriv.jc.modules;

import com.jestron.jestronpriv.jc.core.Category;
import com.jestron.jestronpriv.jc.core.Setting;
import com.jestron.jestronpriv.jc.core.TextHud;
import net.minecraft.client.Minecraft;

public final class FpsModule extends TextHud {
    private final Setting.Bool label = add(new Setting.Bool("label", "Etiketi göster", true));

    public FpsModule() {
        super("fps", "FPS", "Saniyedeki kare sayısı.", Category.HUD, true, 0.005, 0.01);
    }

    @Override
    protected String[] lines(boolean preview) {
        int fps = Minecraft.getInstance().getFps();
        return new String[]{label.on() ? kv("FPS", String.valueOf(fps)) : fps + " FPS"};
    }
}
