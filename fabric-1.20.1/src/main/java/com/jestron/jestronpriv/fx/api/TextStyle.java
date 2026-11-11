package com.jestron.jestronpriv.fx.api;

/**
 * Ozel yazi stili (Minecraft'tan bagimsiz). Istemci cizici bunu gercek yazi stiline cevirir.
 *
 * @param font         0 normal, 1 unicode (ince), 2 buyu dili (galaktik), 3 illager
 * @param color1       ana renk 0xRRGGBB
 * @param color2       gradyan ikinci rengi 0xRRGGBB
 * @param colorMode    0 tek renk, 1 gradyan, 2 gokkusagi (hareketli)
 * @param background   arka plan 0xAARRGGBB (0 = yok)
 * @param outlineColor kontur rengi 0xRRGGBB
 */
public record TextStyle(int font, int color1, int color2, int colorMode, int background,
                        boolean bold, boolean italic, boolean underline, boolean strike,
                        boolean shadow, boolean outline, int outlineColor, boolean glow) {

    public static final TextStyle DEFAULT = new TextStyle(0, 0xFF4040, 0xFFD700, 0, 0x80000000,
            true, false, false, false, true, false, 0x000000, true);
}
