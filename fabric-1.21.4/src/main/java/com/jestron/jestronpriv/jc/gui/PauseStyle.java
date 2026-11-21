package com.jestron.jestronpriv.jc.gui;

import com.jestron.jestronpriv.jc.core.Modules;
import com.jestron.jestronpriv.jc.core.Theme;

/**
 * ESC (duraklatma) menüsü tasarımları. Ayar değeri 0 = vanilla menü, 1..N = ALL[değer-1].
 */
public record PauseStyle(String name, Layout layout, int radius, boolean bevel, boolean glow,
                         int overlay, int panel, int border, int btn, int btnHover, int text, int muted,
                         int btnText, int accentOverride, boolean dark) {

    public enum Layout { CARD, LEFT, RIGHT, DOCK, GRID, COMPACT, FULL, INFO, TOPBAR }

    public static final PauseStyle[] ALL = {
            new PauseStyle("Minimal Kart", Layout.CARD, 8, false, false,
                    0x66000000, 0xFFFFFFFF, 0xFFE4E4E7, 0xFFF4F4F5, 0xFFE9E9EC, 0xFF18181B, 0xFF71717A, 0xFF18181B, 0, false),
            new PauseStyle("Yan Panel", Layout.LEFT, 0, false, false,
                    0x40000000, 0xEE111113, 0x26FFFFFF, 0x00000000, 0x1FFFFFFF, 0xFFF4F4F5, 0xFFA1A1AA, 0xFFE4E4E7, 0, true),
            new PauseStyle("Alt Dock", Layout.DOCK, 10, false, false,
                    0x55000000, 0xF5FFFFFF, 0xFFE4E4E7, 0x00000000, 0xFFF1F1F3, 0xFF18181B, 0xFF71717A, 0xFF27272A, 0, false),
            new PauseStyle("Izgara", Layout.GRID, 8, false, false,
                    0x99050A18, 0x00000000, 0x33FFFFFF, 0xD8111A33, 0xE81B2747, 0xFFFFFFFF, 0xFF94A3B8, 0xFFE2E8F0, 0xFF3B82F6, true),
            new PauseStyle("Cam", Layout.CARD, 12, false, false,
                    0x50000000, 0xB8101014, 0x40FFFFFF, 0x1AFFFFFF, 0x33FFFFFF, 0xFFFFFFFF, 0xFFB4B4BC, 0xFFF4F4F5, 0, true),
            new PauseStyle("Piksel", Layout.CARD, 0, true, false,
                    0x88000000, 0xFFC6C6C6, 0xFF373737, 0xFF6F6F6F, 0xFF7E7E7E, 0xFF3F3F3F, 0xFF5A5A5A, 0xFFFFFFFF, 0xFF5CB030, false),
            new PauseStyle("Neon", Layout.CARD, 0, false, true,
                    0xAA000000, 0xF2050507, 0xFFFF2FD1, 0xFF0B0B10, 0xFF1A0F1C, 0xFFFFFFFF, 0xFF9CA3AF, 0xFFF4F4F5, 0xFFFF2FD1, true),
            new PauseStyle("Kompakt", Layout.COMPACT, 4, false, false,
                    0x50000000, 0x00000000, 0x00000000, 0xF0101012, 0xF8242428, 0xFFFFFFFF, 0xFFA1A1AA, 0xFFF4F4F5, 0, true),
            new PauseStyle("Oyuncu Kartı", Layout.RIGHT, 10, false, false,
                    0x55000000, 0xFFFFFFFF, 0xFFE4E4E7, 0xFFF4F4F5, 0xFFE9E9EC, 0xFF18181B, 0xFF71717A, 0xFF18181B, 0, false),
            new PauseStyle("Tam Ekran", Layout.FULL, 6, false, false,
                    0xDC0A0A0E, 0x00000000, 0x26FFFFFF, 0x14FFFFFF, 0x2AFFFFFF, 0xFFFFFFFF, 0xFFA1A1AA, 0xFFF4F4F5, 0, true),
            new PauseStyle("Bilgi Paneli", Layout.INFO, 8, false, false,
                    0x80000000, 0xEE141417, 0x22FFFFFF, 0xFF1F1F23, 0xFF2B2B31, 0xFFFFFFFF, 0xFFA1A1AA, 0xFFF4F4F5, 0xFFF97316, true),
            new PauseStyle("Üst Bar", Layout.TOPBAR, 6, false, false,
                    0x50000000, 0xF7FFFFFF, 0xFFE4E4E7, 0x00000000, 0xFFF1F1F3, 0xFF18181B, 0xFF71717A, 0xFF27272A, 0, false)
    };

    /** Ayar seçenekleri: 0 = vanilla. */
    public static String[] names() {
        String[] out = new String[ALL.length + 1];
        out[0] = "Vanilla (kapalı)";
        for (int i = 0; i < ALL.length; i++) out[i + 1] = (i + 1) + ". " + ALL[i].name;
        return out;
    }

    public static boolean enabled() {
        return Modules.APPEARANCE.pause.index() > 0;
    }

    public static PauseStyle current() {
        int i = Modules.APPEARANCE.pause.index() - 1;
        return ALL[Math.max(0, Math.min(ALL.length - 1, i))];
    }

    public int accent() {
        if (Theme.inkAccent()) {
            if (accentOverride != 0) return accentOverride;
            if (dark) return 0xFFF4F4F5;
        }
        return Theme.accentPreview(Modules.APPEARANCE.accent.index());
    }

    public int onAccent() {
        return luminance(accent()) > 0.6 ? 0xFF18181B : 0xFFFFFFFF;
    }

    static double luminance(int c) {
        double r = ((c >> 16) & 0xFF) / 255.0, g = ((c >> 8) & 0xFF) / 255.0, b = (c & 0xFF) / 255.0;
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }
}
