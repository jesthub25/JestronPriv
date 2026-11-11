package com.jestron.jestronpriv.util;

import java.util.ArrayList;
import java.util.List;

/** '&' renk kodlarini Minecraft'in '§' kodlarina cevirir. */
public final class Text {
    private static final String CODES = "0123456789abcdefklmnorABCDEFKLMNOR";

    private Text() {
    }

    public static String color(String s) {
        if (s == null) return "";
        char[] c = s.toCharArray();
        for (int i = 0; i < c.length - 1; i++) {
            if (c[i] == '&' && CODES.indexOf(c[i + 1]) >= 0) {
                c[i] = '§';
                c[i + 1] = Character.toLowerCase(c[i + 1]);
            }
        }
        return new String(c);
    }

    public static String strip(String s) {
        String t = color(s);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < t.length(); i++) {
            char ch = t.charAt(i);
            if (ch == '§' && i + 1 < t.length()) {
                i++;
                continue;
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    public static List<String> color(List<String> l) {
        List<String> o = new ArrayList<>();
        for (String s : l) o.add(color(s));
        return o;
    }
}
