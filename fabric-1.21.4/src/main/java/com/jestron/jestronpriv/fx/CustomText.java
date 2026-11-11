package com.jestron.jestronpriv.fx;

import com.jestron.jestronpriv.fx.api.TextStyle;

/**
 * "Ozel Yazi" efektinin ayarlari. Istemci ayarlari degistikce {@link #set} ile guncellenir.
 * Yer tutucular: {kurban} = oldurulen oyuncu, {ben} = sen. Satir atlamak icin '|'.
 */
public final class CustomText {

    public enum Anim { POP, RISE, TYPE, PULSE, SHAKE, ZOOM }

    public enum Sfx { NONE, DING, LEVELUP, VICTORY, GONG, FIREWORK }

    private static volatile String text = "&l{kurban} &fyok edildi!";
    private static volatile TextStyle style = TextStyle.DEFAULT;
    private static volatile float size = 1.4f;
    private static volatile float height = 2.4f;
    private static volatile Anim anim = Anim.POP;
    private static volatile Sfx sfx = Sfx.LEVELUP;
    private static volatile boolean particles = true;
    private static volatile String selfName = "";

    private CustomText() {
    }

    public static void set(String t, TextStyle st, float sz, float h, Anim a, Sfx s, boolean parts) {
        text = t == null ? "" : t;
        style = st == null ? TextStyle.DEFAULT : st;
        size = Math.max(0.3f, Math.min(4f, sz));
        height = Math.max(0.8f, Math.min(5f, h));
        anim = a == null ? Anim.POP : a;
        sfx = s == null ? Sfx.NONE : s;
        particles = parts;
    }

    public static void setSelfName(String n) {
        selfName = n == null ? "" : n;
    }

    public static String rawText() { return text; }
    public static TextStyle style() { return style; }
    public static float size() { return size; }
    public static float height() { return height; }
    public static Anim anim() { return anim; }
    public static Sfx sfx() { return sfx; }
    public static boolean particles() { return particles; }

    /** Yer tutuculari doldurur, '&' kodlarini '§'e, '|' karakterini satir sonuna cevirir. */
    public static String resolve(String victim) {
        String s = text.replace("{kurban}", victim == null ? "?" : victim).replace("{ben}", selfName);
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '&' && i + 1 < s.length() && "0123456789abcdefklmnorABCDEFKLMNOR".indexOf(s.charAt(i + 1)) >= 0) sb.append('§');
            else if (c == '|') sb.append('\n');
            else sb.append(c);
        }
        String out = sb.toString().trim();
        return out.isEmpty() ? (victim == null ? "?" : victim) : out;
    }
}
