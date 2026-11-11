package com.jestron.jestronpriv.client.hud;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.pvp.Combo;
import net.minecraft.client.gui.GuiGraphics;

/** Kombo sayacinin yeri (HUD duzenleyicide tasinir). */
final class ComboHud extends BoundElement {

    ComboHud() {
        super("combo", "Kombo Sayacı", c -> c.combo, (c, v) -> c.combo = v, c -> c.comboX, (c, v) -> c.comboX = v, c -> c.comboY,
                (c, v) -> c.comboY = v, c -> c.comboScale, (c, v) -> c.comboScale = v);
    }

    @Override
    public boolean hasContent(ModConfig c) {
        return Combo.active();
    }

    @Override
    public void measure(ModConfig c, boolean sample) {
        w = 110;
        h = 46;
    }

    @Override
    public void draw(GuiGraphics g, ModConfig c, boolean sample, boolean right) {
        Combo.renderAt(g, Hud.partial, w / 2, 14, sample);
    }
}
