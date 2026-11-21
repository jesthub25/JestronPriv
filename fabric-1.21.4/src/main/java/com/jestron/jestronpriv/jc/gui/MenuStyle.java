package com.jestron.jestronpriv.jc.gui;

import com.jestron.jestronpriv.jc.core.Modules;
import com.jestron.jestronpriv.jc.core.Theme;

/**
 * Jestron menüsünün 10 görünümü. Yerleşim (kategori yeri, kart türü, bölünmüş görünüm),
 * köşe yuvarlaklığı ve renkler burada tanımlıdır.
 */
public record MenuStyle(
        String name,
        Nav nav,
        Item item,
        boolean full,
        boolean split,
        int radius,
        boolean bevel,
        boolean pillTabs,
        int overlay,
        int panel,
        int navBg,
        int navActive,
        int card,
        int cardHover,
        int border,
        int text,
        int muted,
        int cardText,
        int cardMuted,
        int track,
        int accentOverride) {

    public enum Nav { LEFT, TOP, BOTTOM }

    public enum Item { CARD, TILE, ROW }

    public static final MenuStyle[] ALL = {
            // 1 Minimal Açık
            new MenuStyle("Minimal Açık", Nav.LEFT, Item.CARD, false, false, 6, false, false,
                    0x66000000, 0xF8FAFAFA, 0xFFF1F1F3, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFF4F4F5, 0xFFE4E4E7,
                    0xFF18181B, 0xFF5B5B63, 0xFF18181B, 0xFF6B6B73, 0xFFD4D4D8, 0),
            // 2 Gece Camı
            new MenuStyle("Gece Camı", Nav.LEFT, Item.CARD, false, false, 6, false, false,
                    0x88000000, 0xD8101014, 0x50000000, 0x26FFFFFF, 0x16FFFFFF, 0x26FFFFFF, 0x30FFFFFF,
                    0xFFF4F4F5, 0xFFA1A1AA, 0xFFF4F4F5, 0xFFA1A1AA, 0x50FFFFFF, 0),
            // 3 Sekmeli
            new MenuStyle("Sekmeli", Nav.TOP, Item.CARD, false, false, 8, false, true,
                    0x66000000, 0xF8FFFFFF, 0xFFF1F1F3, 0xFF18181B, 0xFFF7F7F8, 0xFFEFEFF1, 0xFFE4E4E7,
                    0xFF18181B, 0xFF5B5B63, 0xFF18181B, 0xFF6B6B73, 0xFFD4D4D8, 0),
            // 4 Lunar Liste (liste + sağda ayarlar)
            new MenuStyle("Liste + Ayar", Nav.TOP, Item.ROW, false, true, 4, false, false,
                    0x77000000, 0xF4161618, 0xFF1C1C20, 0xFF2C2C33, 0xFF1C1C20, 0xFF26262C, 0xFF2E2E35,
                    0xFFF4F4F5, 0xFF9A9AA6, 0xFFF4F4F5, 0xFF9A9AA6, 0xFF3F3F46, 0xFF38BDF8),
            // 5 Izgara (Feather tarzı kareler)
            new MenuStyle("Izgara", Nav.LEFT, Item.TILE, false, false, 6, false, false,
                    0x88000814, 0xF20B1220, 0xFF0F1A2E, 0xFF1C2D4D, 0xFF13213A, 0xFF1A2C4D, 0xFF233456,
                    0xFFE8EEFC, 0xFF8A96B3, 0xFFE8EEFC, 0xFF8A96B3, 0xFF2A3A5C, 0xFF3B82F6),
            // 6 Piksel (vanilla Minecraft arayüzü)
            new MenuStyle("Piksel", Nav.LEFT, Item.CARD, false, false, 0, true, false,
                    0x88000000, 0xFFC6C6C6, 0xFFB8B8B8, 0xFF8B8B8B, 0xFF6F6F6F, 0xFF7E7E7E, 0xFF373737,
                    0xFF3F3F3F, 0xFF5A5A5A, 0xFFFFFFFF, 0xFFD0D0D0, 0xFF555555, 0xFF5CB030),
            // 7 Kompakt Liste
            new MenuStyle("Kompakt", Nav.TOP, Item.ROW, false, false, 4, false, false,
                    0x55000000, 0xF8FFFFFF, 0xFFFFFFFF, 0xFF18181B, 0xFFFFFFFF, 0xFFF4F4F5, 0xFFEAEAEC,
                    0xFF18181B, 0xFF5B5B63, 0xFF18181B, 0xFF6B6B73, 0xFFD4D4D8, 0),
            // 8 Neon
            new MenuStyle("Neon", Nav.LEFT, Item.CARD, false, false, 0, false, false,
                    0x99000000, 0xF2050508, 0xFF0A0A12, 0xFF1A1030, 0xFF0B0B14, 0xFF140E24, 0xFF33224F,
                    0xFFF2E9FF, 0xFF9C90C4, 0xFFF2E9FF, 0xFF9C90C4, 0xFF2A2440, 0xFFFF2FD1),
            // 9 Alt Dock
            new MenuStyle("Alt Dock", Nav.BOTTOM, Item.CARD, false, false, 8, false, true,
                    0x66000000, 0xEEF6F6F8, 0xFFFFFFFF, 0xFF18181B, 0xFFFFFFFF, 0xFFF1F1F3, 0xFFE4E4E7,
                    0xFF18181B, 0xFF5B5B63, 0xFF18181B, 0xFF6B6B73, 0xFFD4D4D8, 0),
            // 10 Tam Ekran
            new MenuStyle("Tam Ekran", Nav.TOP, Item.TILE, true, false, 6, false, true,
                    0xD00A0A0E, 0x00000000, 0x22FFFFFF, 0xFFF4F4F5, 0x18FFFFFF, 0x28FFFFFF, 0x26FFFFFF,
                    0xFFF4F4F5, 0xFFA1A1AA, 0xFFF4F4F5, 0xFFA1A1AA, 0x50FFFFFF, 0)
    };

    public static String[] names() {
        String[] n = new String[ALL.length];
        for (int i = 0; i < ALL.length; i++) n[i] = (i + 1) + ". " + ALL[i].name;
        return n;
    }

    public static MenuStyle current() {
        int i = Modules.APPEARANCE.style.index();
        return ALL[Math.max(0, Math.min(ALL.length - 1, i))];
    }

    public boolean dark() {
        return luminance(panel == 0 ? overlay : panel) < 0.5 && !(bevel);
    }

    /** Vurgu rengi: stilin kendi rengi ya da Görünüm'deki seçim. */
    public int accent() {
        // stilin imza rengi yalnızca varsayılan (Mürekkep) vurgu seçiliyken; kullanıcı renk seçerse o kullanılır
        if (Theme.inkAccent()) {
            if (accentOverride != 0) return accentOverride;
            if (dark()) return 0xFFF4F4F5;
        }
        return Theme.accentPreview(Modules.APPEARANCE.accent.index());
    }

    public int onAccent() {
        return luminance(accent()) > 0.6 ? 0xFF18181B : 0xFFFFFFFF;
    }

    /** Sekme / seçili kategori yazısı için. */
    public int navActiveText() {
        if ((navActive >>> 24) < 0x80) return text;
        return luminance(navActive) > 0.55 ? 0xFF18181B : 0xFFFFFFFF;
    }

    public static double luminance(int c) {
        int r = (c >> 16) & 255, g = (c >> 8) & 255, b = c & 255;
        return (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255.0;
    }
}
