package com.jestron.jestronpriv.jc.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** Yuvarlak köşeli kutu, anahtar ve renk yardımcıları. */
public final class Draw {
    private Draw() {
    }

    public static void roundRect(GuiGraphics g, int x1, int y1, int x2, int y2, int r, int color) {
        int w = x2 - x1, h = y2 - y1;
        if (w <= 0 || h <= 0) return;
        r = Math.min(r, Math.min(w, h) / 2);
        if (r <= 0) {
            g.fill(x1, y1, x2, y2, color);
            return;
        }
        g.fill(x1, y1 + r, x2, y2 - r, color);
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int inset = (int) Math.round(r - Math.sqrt(Math.max(0, r * r - dy * dy)));
            g.fill(x1 + inset, y1 + i, x2 - inset, y1 + i + 1, color);
            g.fill(x1 + inset, y2 - i - 1, x2 - inset, y2 - i, color);
        }
    }

    /** Kenarlıklı yuvarlak kutu (1 px kenar). */
    public static void card(GuiGraphics g, int x1, int y1, int x2, int y2, int r, int fill, int border) {
        roundRect(g, x1, y1, x2, y2, r, border);
        roundRect(g, x1 + 1, y1 + 1, x2 - 1, y2 - 1, Math.max(0, r - 1), fill);
    }

    /** t: 0 = kapalı, 1 = açık (animasyon için ara değerler). */
    public static void toggle(GuiGraphics g, int x, int y, int w, int h, float t, int on, int off) {
        int c = lerpColor(off, on, t);
        roundRect(g, x, y, x + w, y + h, h / 2, c);
        int k = h - 4;
        int kx = x + 2 + Math.round((w - 4 - k) * t);
        roundRect(g, kx, y + 2, kx + k, y + 2 + k, k / 2, 0xFFFFFFFF);
    }

    public static int lerpColor(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int aa = a >>> 24, ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255;
        int ba = b >>> 24, br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        return (Math.round(aa + (ba - aa) * t) << 24) | (Math.round(ar + (br - ar) * t) << 16)
                | (Math.round(ag + (bg - ag) * t) << 8) | Math.round(ab + (bb - ab) * t);
    }

    public static int withAlpha(int color, float alpha) {
        int a = Math.round(((color >>> 24) & 255) * Math.max(0f, Math.min(1f, alpha)));
        return (a << 24) | (color & 0x00FFFFFF);
    }

    public static String ellipsize(Font font, String s, int maxW) {
        if (font.width(s) <= maxW) return s;
        String dots = "…";
        int w = Math.max(0, maxW - font.width(dots));
        return font.plainSubstrByWidth(s, w) + dots;
    }

    public static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && my >= y && mx < x + w && my < y + h;
    }
}
