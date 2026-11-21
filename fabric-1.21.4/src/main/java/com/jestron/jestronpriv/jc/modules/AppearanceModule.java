package com.jestron.jestronpriv.jc.modules;

import com.jestron.jestronpriv.jc.core.Category;
import com.jestron.jestronpriv.jc.core.Module;
import com.jestron.jestronpriv.jc.core.Setting;
import com.jestron.jestronpriv.jc.core.Theme;

/** Menü teması ve HUD yazı stili. Açılıp kapanmaz, sadece ayardır. */
public final class AppearanceModule extends Module {
    public final Setting.Choice style = add(new Setting.Choice("style", "Menü tasarımı", 8, com.jestron.jestronpriv.jc.gui.MenuStyle.names()));
    public final Setting.Choice pause = add(new Setting.Choice("pauseStyle", "ESC menüsü", 8, com.jestron.jestronpriv.jc.gui.PauseStyle.names()));
    public final Setting.Bind menuKey = add(new Setting.Bind("menuKey", "Menü tuşu", () -> com.jestron.jestronpriv.jc.JestronClient.menuKey, false));
    public final Setting.Choice accent = add(new Setting.Choice("accent", "Vurgu rengi", 0, Theme.ACCENT_NAMES));
    public final Setting.Bool hudBg = add(new Setting.Bool("hudBg", "HUD arka planı", true));
    public final Setting.Bool hudShadow = add(new Setting.Bool("hudShadow", "HUD yazı gölgesi", true));
    public final Setting.Choice hudColor = add(new Setting.Choice("hudColor", "HUD etiket rengi", 0, "Gri", "Vurgu rengi"));

    public AppearanceModule() {
        super("appearance", "Görünüm", "Menü tasarımı (10), ESC menüsü (12), vurgu rengi ve HUD yazı stili.", Category.VISUAL, true);
    }

    @Override
    public boolean toggleable() {
        return false;
    }
}
