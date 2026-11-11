package com.jestron.jestronpriv.tag;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Emoji dizini (MC'den bagimsiz): atlas sirasi, grup, Turkce ad, arama kelimeleri ve ten rengi varyantlari.
 * Metindeki emojileri en uzun eslesmeyle bulur (aile / bayrak gibi cok kod noktali emojiler dahil).
 */
public final class EmojiIndex {

    /** Gruplar (atlas dizinindeki numara) ve Turkce adlari. */
    public static final int[] GROUPS = {0, 1, 3, 4, 5, 6, 7, 8, 9};
    public static final String[] GROUP_NAMES = {"Yüzler", "İnsanlar", "Hayvan & Doğa", "Yiyecek", "Seyahat", "Etkinlik", "Nesneler", "Semboller", "Bayraklar"};
    public static final String[] GROUP_ICONS = {"😀", "👋", "🐶", "🍔", "✈️", "⚽", "💡", "❤️", "🏁"};

    public static final int ATLAS_COLS = 64, CELL = 32, ATLAS_SIZE = ATLAS_COLS * CELL;

    private final List<String> emoji = new ArrayList<>();
    private final List<String> label = new ArrayList<>();
    private final List<String> search = new ArrayList<>();
    private final List<Integer> group = new ArrayList<>();
    private final List<Integer> parent = new ArrayList<>();
    private final Map<Integer, List<Integer>> skins = new HashMap<>();
    private final Map<String, Integer> lookup = new HashMap<>();
    private int maxLen = 1;

    public static EmojiIndex parse(InputStream in) throws IOException {
        EmojiIndex x = new EmojiIndex();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                // yorum satiri "# " ile baslar (tus kapagi emojisi "#\uFE0F\u20E3" yorum sayilmaz)
                if (line.isEmpty() || line.startsWith("# ")) continue;
                String[] f = line.split("\t", -1);
                if (f.length < 5) continue;
                int idx = x.emoji.size();
                if (idx >= ATLAS_COLS * ATLAS_COLS) break;
                int g, par;
                try {
                    g = Integer.parseInt(f[1].trim());
                    par = Integer.parseInt(f[4].trim());
                } catch (NumberFormatException e) {
                    continue;
                }
                x.emoji.add(f[0]);
                x.group.add(g);
                x.label.add(f[2]);
                x.search.add((f[2] + "," + f[3]).toLowerCase(Locale.ROOT));
                x.parent.add(par);
                if (par >= 0 && par < idx) x.skins.computeIfAbsent(par, k -> new ArrayList<>()).add(idx);
                x.lookup.putIfAbsent(f[0], idx);
                String bare = f[0].replace("️", "");
                if (!bare.isEmpty()) x.lookup.putIfAbsent(bare, idx);
                x.maxLen = Math.max(x.maxLen, f[0].length());
            }
        }
        return x;
    }

    public int size() {
        return emoji.size();
    }

    public String emoji(int i) {
        return i >= 0 && i < emoji.size() ? emoji.get(i) : "";
    }

    public String label(int i) {
        return i >= 0 && i < label.size() ? label.get(i) : "";
    }

    public int group(int i) {
        return i >= 0 && i < group.size() ? group.get(i) : -1;
    }

    /** Ten rengi varyantlari (ana emoji icin; yoksa bos). */
    public List<Integer> skins(int i) {
        List<Integer> s = skins.get(i);
        return s == null ? List.of() : s;
    }

    public boolean isBase(int i) {
        return i >= 0 && i < parent.size() && parent.get(i) < 0;
    }

    /** Atlas hucresi (sol ust) piksel cinsinden. */
    public static int cellX(int i) {
        return (i % ATLAS_COLS) * CELL;
    }

    public static int cellY(int i) {
        return (i / ATLAS_COLS) * CELL;
    }

    public int find(String e) {
        if (e == null) return -1;
        Integer i = lookup.get(e);
        if (i == null) i = lookup.get(e.replace("️", ""));
        return i == null ? -1 : i;
    }

    /** Gruptaki ana emojiler (grup = -1: hepsi), arama (Turkce / Ingilizce) ile suzulur. */
    public List<Integer> list(int grp, String query, int limit) {
        List<Integer> out = new ArrayList<>();
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        for (int i = 0; i < emoji.size() && out.size() < limit; i++) {
            if (parent.get(i) >= 0) continue;
            if (grp >= 0 && group.get(i) != grp) continue;
            if (!q.isEmpty() && !search.get(i).contains(q) && !emoji.get(i).equals(q)) continue;
            out.add(i);
        }
        return out;
    }

    /** Metin parcasi: emoji (index >= 0) ya da duz metin. */
    public record Token(String text, int emoji) {
    }

    /** Metni emoji ve duz metin parcalarina ayirir (en uzun eslesme). */
    public List<Token> tokenize(String s) {
        List<Token> out = new ArrayList<>();
        if (s == null || s.isEmpty()) return out;
        StringBuilder plain = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            int found = -1, foundLen = 0;
            char c = s.charAt(i);
            // hizli eleme: emojiler ASCII disi ya da # * 0-9 (tus kapagi) ile baslar
            if (c >= 0x80 || c == '#' || c == '*' || (c >= '0' && c <= '9')) {
                for (int len = Math.min(maxLen, s.length() - i); len >= 1; len--) {
                    Integer idx = lookup.get(s.substring(i, i + len));
                    if (idx != null) {
                        // tek ASCII rakam / # / * duz metin kalsin (sadece tus kapagi dizisi emoji)
                        if (len == 1 && c < 0x80) break;
                        found = idx;
                        foundLen = len;
                        break;
                    }
                }
            }
            if (found >= 0) {
                if (plain.length() > 0) {
                    out.add(new Token(plain.toString(), -1));
                    plain.setLength(0);
                }
                // ardindan gelen degisken secici (FE0F) emojiye dahil
                int end = i + foundLen;
                if (end < s.length() && s.charAt(end) == '️') end++;
                out.add(new Token(s.substring(i, end), found));
                i = end;
            } else {
                int cp = s.codePointAt(i);
                plain.appendCodePoint(cp);
                i += Character.charCount(cp);
            }
        }
        if (plain.length() > 0) out.add(new Token(plain.toString(), -1));
        return out;
    }

    /** Metindeki emoji sayisi. */
    public int countEmoji(String s) {
        int n = 0;
        for (Token t : tokenize(s)) if (t.emoji() >= 0) n++;
        return n;
    }

    /** Sondaki emojiyi siler (onek / sonek duzenleme). */
    public String removeLast(String s) {
        List<Token> t = tokenize(s);
        if (t.isEmpty()) return "";
        t.remove(t.size() - 1);
        StringBuilder b = new StringBuilder();
        for (Token k : t) b.append(k.text());
        return b.toString();
    }
}
