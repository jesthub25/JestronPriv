package com.jestron.jestronpriv.client.hud;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.pvp.KillStreak;
import net.minecraft.client.gui.GuiGraphics;

/** Seri oldurme duyurusunun yeri (kenar parlamasi her zaman tum ekran). */
final class StreakHud extends BoundElement {

    private final int[] size = new int[2];

    StreakHud() {
        super("streak", "Seri Duyurusu", c -> c.killStreak, (c, v) -> c.killStreak = v, c -> c.streakX, (c, v) -> c.streakX = v,
                c -> c.streakY, (c, v) -> c.streakY = v, c -> c.streakScale, (c, v) -> c.streakScale = v);
    }

    @Override
    public boolean hasContent(ModConfig c) {
        return KillStreak.active();
    }

    @Override
    public void measure(ModConfig c, boolean sample) {
        KillStreak.size(sample, size);
        w = size[0];
        h = size[1];
    }

    @Override
    public void draw(GuiGraphics g, ModConfig c, boolean sample, boolean right) {
        // h = 11*olcek + 24; baslik merkezi 4.5*olcek + 4
        int cy = Math.round((h - 24) * 4.5f / 11f) + 4;
        KillStreak.renderAt(g, w / 2, cy, sample);
    }
}
