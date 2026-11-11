package com.jestron.jestronpriv.tag;

/** Isim etiketi ayarlari (ayarlardan uretilir). */
public final class TagSettings {

    /** Etiket bayraklari. */
    public static final int F_BOLD = 1, F_ITALIC = 2, F_UNDERLINE = 4, F_STRIKE = 8, F_SHADOW = 16, F_OUTLINE = 32, F_GLOW = 64;

    /** En cok yazi uzunlugu (kod noktasi) ve bas / son emoji dizisi uzunlugu. */
    public static final int MAX_TEXT_CP = 32, MAX_EMOJI = 96;

    public String text = "", font = "mc", prefix = "", suffix = "";
    public int colorMode, c1 = 0xFFFFFF, c2 = 0x55FFFF, c3 = 0xFF55FF, outlineColor = 0x000000, bgColor = 0x000000, anim, emojiAnim,
            flags = F_SHADOW;
    public float animSpeed = 1f, bgAlpha = 0.25f, scale = 1f, height = 0f, emojiScale = 1f;

    /** Gorunur metin: kontrol karakterleri ve renk kodu (§) atilir, kod noktasi sinirli. */
    public static String cleanText(String s, int maxCp) {
        if (s == null) return "";
        StringBuilder b = new StringBuilder();
        int cps = 0;
        for (int i = 0; i < s.length() && cps < maxCp; ) {
            int cp = s.codePointAt(i);
            i += Character.charCount(cp);
            if (cp < 0x20 || cp == 0x7F || cp == 0xA7 || (cp >= 0x80 && cp < 0xA0) || cp == 0x2028 || cp == 0x2029 || cp == 0xFEFF
                    || (cp >= 0x202A && cp <= 0x202E) || (cp >= 0x2066 && cp <= 0x2069)) continue;
            b.appendCodePoint(cp);
            cps++;
        }
        return b.toString();
    }
}
