package com.jestron.jestronpriv.jc.core;

import com.jestron.jestronpriv.jc.modules.AppearanceModule;

/** Vurgu rengi ve HUD renkleri (menü renkleri MenuStyle sınıfında). */
public final class Theme {
    private Theme() {
    }

    public static final String[] ACCENT_NAMES = {"Mürekkep", "Turuncu", "Mavi", "Yeşil", "Mor", "Pembe"};
    private static final int[] ACCENTS = {0xFF18181B, 0xFFEA580C, 0xFF2563EB, 0xFF16A34A, 0xFF7C3AED, 0xFFDB2777};

    private static AppearanceModule app() {
        return Modules.APPEARANCE;
    }

    public static boolean inkAccent() {
        return app().accent.index() == 0;
    }

    public static int accentPreview(int index) {
        return ACCENTS[Math.max(0, Math.min(ACCENTS.length - 1, index))];
    }

    // HUD (her zaman koyu yarı saydam zemin üstünde)
    public static boolean hudBackground() {
        return app().hudBg.on();
    }

    public static boolean hudShadow() {
        return app().hudShadow.on();
    }

    public static int hudValue() {
        return 0xFFFFFFFF;
    }

    public static int hudLabel() {
        if (app().hudColor.index() == 1 && !inkAccent()) return ACCENTS[app().accent.index()] | 0xFF000000;
        return 0xFFB4B4BC;
    }

    public static int hudAccent() {
        return inkAccent() ? 0xFFFFFFFF : ACCENTS[app().accent.index()];
    }
}
