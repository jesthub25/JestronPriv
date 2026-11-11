package com.jestron.jestronpriv.ui;

import java.util.List;

/**
 * Menu temasi (Minecraft'a bagimsiz). Arka plani seffaf dugmeler, panel, kaydirici, yazi kutusu, izgara hucreleri ve
 * kartlar ayni temadan cizilir. {@link #VANILLA} oyunun kendi dugme gorsellerini kullanir.
 *
 * @param accent    vurgu (cerceve, baslik, kaydirici dolgusu)
 * @param accent2   ikinci vurgu (gradyan / siber ikinci renk)
 * @param tint      dugme ici renk tonu
 * @param btnTop    dugme ust saydamligi (0-255)
 * @param btnBot    dugme alt saydamligi
 * @param panel     panel rengi
 * @param panelA    panel saydamligi
 * @param border    normal cerceve saydamligi (0 = cerceve yok)
 * @param shape     kose bicimi
 * @param glow      ustune gelince dis parilti
 * @param bar       sol kenarda vurgu cubugu
 * @param underline alt cizgi
 * @param scan      tarama cizgileri (siber)
 */
public record GuiTheme(String id, String name, String desc, int accent, int accent2, int tint, int btnTop, int btnBot, int panel, int panelA,
                       int border, Shape shape, boolean glow, boolean bar, boolean underline, boolean scan) {

    public enum Shape {
        /** Keskin kose. */
        SQUARE,
        /** 1 piksel yuvarlatilmis kose. */
        ROUND,
        /** 2 piksel yuvarlatilmis (hap). */
        PILL,
        /** Sadece kose koseli parantezleri (siber). */
        BRACKET,
        /** Sadece alt cizgi (minimal). */
        LINE
    }

    public static final GuiTheme VANILLA = new GuiTheme("VANILLA", "Klasik (vanilla)", "Oyunun kendi gri düğmeleri", 0xC77DFF, 0xB070FF, 0, 0, 0,
            0x000000, 0x66, 0x40, Shape.SQUARE, false, false, false, false);

    public static final List<GuiTheme> ALL = List.of(
            new GuiTheme("MOR_CAM", "Mor Cam", "Şeffaf mor cam, yumuşak parıltı", 0xC77DFF, 0x7C4DFF, 0x2A1440, 0x78, 0x48, 0x14081F, 0x70, 0x70,
                    Shape.ROUND, true, false, false, false),
            new GuiTheme("BUZLU_CAM", "Buzlu Cam", "Beyaz buzlu cam, ince çizgiler", 0xBFF4FF, 0xE0F7FF, 0xC8DCF0, 0x24, 0x0E, 0xDDEEFF, 0x12, 0x68,
                    Shape.ROUND, false, false, false, false),
            new GuiTheme("NEON", "Neon Gece", "Kapkara cam, pembe neon çerçeve", 0xFF3DF2, 0x00E5FF, 0x000000, 0x70, 0x70, 0x000000, 0x68, 0xC0,
                    Shape.SQUARE, true, false, false, false),
            new GuiTheme("OKYANUS", "Okyanus", "Derin mavi cam, turkuaz alt çizgi", 0x3DD6FF, 0x1E88E5, 0x06304A, 0x80, 0x50, 0x041C2C, 0x70, 0x50,
                    Shape.ROUND, false, false, true, false),
            new GuiTheme("ZUMRUT", "Zümrüt", "Yeşil cam, solda vurgu çubuğu", 0x3DFF9A, 0x00BFA5, 0x06301C, 0x78, 0x48, 0x031A10, 0x70, 0x50,
                    Shape.ROUND, false, true, false, false),
            new GuiTheme("GUN_BATIMI", "Gün Batımı", "Pembe-turuncu gradyan cam", 0xFF9A3D, 0xFF4F8B, 0x401020, 0x70, 0x40, 0x200810, 0x68, 0x80,
                    Shape.PILL, true, false, false, false),
            new GuiTheme("ALTIN", "Altın Lüks", "Siyah cam, çift altın çerçeve", 0xFFD24A, 0xB8860B, 0x000000, 0x80, 0x60, 0x000000, 0x70, 0xA0,
                    Shape.SQUARE, false, false, false, false),
            new GuiTheme("KIZIL", "Kızıl", "Koyu kırmızı cam, keskin hatlar", 0xFF4A4A, 0xB71C1C, 0x2A0606, 0x80, 0x50, 0x140202, 0x70, 0x60,
                    Shape.SQUARE, false, true, true, false),
            new GuiTheme("SIBER", "Siber", "Turkuaz-pembe köşeler, tarama çizgileri", 0x00FFE1, 0xFF2BD6, 0x001418, 0x70, 0x50, 0x000A0C, 0x70, 0x50,
                    Shape.BRACKET, true, false, false, true),
            new GuiTheme("MINIMAL", "Minimal", "Neredeyse görünmez, sadece alt çizgi", 0xFFFFFF, 0xAAAAAA, 0x000000, 0x28, 0x10, 0x000000, 0x38, 0x00,
                    Shape.LINE, false, false, true, false),
            VANILLA);

    /** Varsayilan tema: Buzlu Cam. */
    public static final String DEFAULT_ID = "BUZLU_CAM";

    public static GuiTheme get(String id) {
        GuiTheme def = null;
        for (GuiTheme t : ALL) {
            if (t.id.equals(id)) return t;
            if (t.id.equals(DEFAULT_ID)) def = t;
        }
        return def != null ? def : ALL.get(0);
    }

    public static GuiTheme next(String id, boolean back) {
        int i = ALL.indexOf(get(id));
        return ALL.get(Math.floorMod(i + (back ? -1 : 1), ALL.size()));
    }

    public boolean vanilla() {
        return this == VANILLA || "VANILLA".equals(id);
    }

    /** Ayni tema, baska vurgu rengiyle. */
    public GuiTheme withAccent(int rgb) {
        return new GuiTheme(id, name, desc, rgb & 0xFFFFFF, accent2, tint, btnTop, btnBot, panel, panelA, border, shape, glow, bar, underline, scan);
    }
}
