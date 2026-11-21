package com.jestron.jestronpriv.jc.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class TextUtil {
    private TextUtil() {
    }

    /** Büyük/küçük harf ve Türkçe karakter farkını yok sayan karşılaştırma için sadeleştirir. */
    public static String norm(String s) {
        if (s == null) return "";
        String l = s.toLowerCase(Locale.ROOT);
        StringBuilder sb = new StringBuilder(l.length());
        for (int i = 0; i < l.length(); i++) {
            char c = l.charAt(i);
            switch (c) {
                case 'ı', 'İ' -> sb.append('i');
                case 'ş' -> sb.append('s');
                case 'ğ' -> sb.append('g');
                case 'ü' -> sb.append('u');
                case 'ö' -> sb.append('o');
                case 'ç' -> sb.append('c');
                case '̇' -> {
                    // "İ".toLowerCase(ROOT) birleştirici nokta bırakır
                }
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    /** "a, b ,c" -> [a, b, c] (sadeleştirilmiş, boşlar atılır). */
    public static List<String> splitList(String csv) {
        List<String> out = new ArrayList<>();
        if (csv == null) return out;
        for (String p : csv.split(",")) {
            String t = norm(p.trim());
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    public static String duration(long ms) {
        long s = Math.max(0, ms / 1000);
        long h = s / 3600, m = (s % 3600) / 60, sec = s % 60;
        if (h > 0) return String.format(Locale.ROOT, "%d:%02d:%02d", h, m, sec);
        return String.format(Locale.ROOT, "%02d:%02d", m, sec);
    }
}
