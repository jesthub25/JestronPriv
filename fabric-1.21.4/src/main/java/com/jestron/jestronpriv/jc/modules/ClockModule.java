package com.jestron.jestronpriv.jc.modules;

import com.jestron.jestronpriv.jc.core.Category;
import com.jestron.jestronpriv.jc.core.Setting;
import com.jestron.jestronpriv.jc.core.TextHud;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class ClockModule extends TextHud {
    private final Setting.Bool seconds = add(new Setting.Bool("seconds", "Saniyeyi göster", false));
    private final Setting.Choice format = add(new Setting.Choice("format", "Biçim", 0, "24 saat", "12 saat"));
    private static final DateTimeFormatter H24 = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter H24S = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter H12 = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter H12S = DateTimeFormatter.ofPattern("hh:mm:ss a");

    public ClockModule() {
        super("clock", "Saat", "Bilgisayarının saati.", Category.HUD, false, 0.47, 0.01);
    }

    @Override
    protected String[] lines(boolean preview) {
        DateTimeFormatter f = format.index() == 0 ? (seconds.on() ? H24S : H24) : (seconds.on() ? H12S : H12);
        return new String[]{LocalTime.now().format(f)};
    }
}
