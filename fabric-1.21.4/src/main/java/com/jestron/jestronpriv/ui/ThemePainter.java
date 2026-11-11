package com.jestron.jestronpriv.ui;

/**
 * Tema cizimi (Minecraft'a bagimsiz): ayni kod hem oyunda (GuiGraphics) hem ekran goruntusu aracinda kullanilir,
 * boylece goruntuler oyundakiyle birebir ayni olur. Tum koordinatlar GUI pikseli.
 */
public final class ThemePainter {

    /** Cizim yuzeyi. */
    public interface Canvas {
        void fill(int x0, int y0, int x1, int y1, int argb);

        /** Dikey gradyan (ust -> alt). */
        void gradient(int x0, int y0, int x1, int y1, int top, int bottom);
    }

    private ThemePainter() {
    }

    /** Renk + saydamlik (opaklik carpaniyla). */
    public static int a(int rgb, int alpha, float op) {
        int al = Math.max(0, Math.min(255, Math.round(alpha * op)));
        return (al << 24) | (rgb & 0xFFFFFF);
    }

    // ------------------------------------------------------------------ bicim

    /** Kose bicimine gore dolu dikdortgen (dikey gradyan). */
    public static void shapeFill(Canvas c, GuiTheme.Shape s, int x, int y, int w, int h, int top, int bot) {
        if (w <= 0 || h <= 0) return;
        switch (s) {
            case ROUND -> {
                if (w < 3 || h < 3) {
                    c.gradient(x, y, x + w, y + h, top, bot);
                    return;
                }
                c.fill(x + 1, y, x + w - 1, y + 1, top);
                c.gradient(x, y + 1, x + w, y + h - 1, top, bot);
                c.fill(x + 1, y + h - 1, x + w - 1, y + h, bot);
            }
            case PILL -> {
                if (w < 5 || h < 5) {
                    shapeFill(c, GuiTheme.Shape.ROUND, x, y, w, h, top, bot);
                    return;
                }
                c.fill(x + 2, y, x + w - 2, y + 1, top);
                c.fill(x + 1, y + 1, x + w - 1, y + 2, top);
                c.gradient(x, y + 2, x + w, y + h - 2, top, bot);
                c.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, bot);
                c.fill(x + 2, y + h - 1, x + w - 2, y + h, bot);
            }
            default -> c.gradient(x, y, x + w, y + h, top, bot);
        }
    }

    /** Kose bicimine gore cerceve. */
    public static void shapeBorder(Canvas c, GuiTheme.Shape s, int x, int y, int w, int h, int argb) {
        if (w <= 1 || h <= 1 || (argb >>> 24) == 0) return;
        switch (s) {
            case ROUND -> {
                c.fill(x + 1, y, x + w - 1, y + 1, argb);
                c.fill(x + 1, y + h - 1, x + w - 1, y + h, argb);
                c.fill(x, y + 1, x + 1, y + h - 1, argb);
                c.fill(x + w - 1, y + 1, x + w, y + h - 1, argb);
            }
            case PILL -> {
                c.fill(x + 2, y, x + w - 2, y + 1, argb);
                c.fill(x + 2, y + h - 1, x + w - 2, y + h, argb);
                c.fill(x, y + 2, x + 1, y + h - 2, argb);
                c.fill(x + w - 1, y + 2, x + w, y + h - 2, argb);
                c.fill(x + 1, y + 1, x + 2, y + 2, argb);
                c.fill(x + w - 2, y + 1, x + w - 1, y + 2, argb);
                c.fill(x + 1, y + h - 2, x + 2, y + h - 1, argb);
                c.fill(x + w - 2, y + h - 2, x + w - 1, y + h - 1, argb);
            }
            case BRACKET -> {
                int lx = Math.min(5, w / 3), ly = Math.min(5, h / 2);
                c.fill(x, y, x + lx, y + 1, argb);
                c.fill(x, y + 1, x + 1, y + ly, argb);
                c.fill(x + w - lx, y, x + w, y + 1, argb);
                c.fill(x + w - 1, y + 1, x + w, y + ly, argb);
                c.fill(x, y + h - 1, x + lx, y + h, argb);
                c.fill(x, y + h - ly, x + 1, y + h - 1, argb);
                c.fill(x + w - lx, y + h - 1, x + w, y + h, argb);
                c.fill(x + w - 1, y + h - ly, x + w, y + h - 1, argb);
            }
            case LINE -> c.fill(x, y + h - 1, x + w, y + h, argb);
            default -> {
                c.fill(x, y, x + w, y + 1, argb);
                c.fill(x, y + h - 1, x + w, y + h, argb);
                c.fill(x, y + 1, x + 1, y + h - 1, argb);
                c.fill(x + w - 1, y + 1, x + w, y + h - 1, argb);
            }
        }
    }

    private static void glow(Canvas c, GuiTheme t, int x, int y, int w, int h, float op) {
        GuiTheme.Shape s = t.shape() == GuiTheme.Shape.BRACKET || t.shape() == GuiTheme.Shape.LINE ? GuiTheme.Shape.ROUND : t.shape();
        shapeBorder(c, s == GuiTheme.Shape.SQUARE ? GuiTheme.Shape.SQUARE : GuiTheme.Shape.ROUND, x - 1, y - 1, w + 2, h + 2, a(t.accent(), 0x58, op));
        shapeBorder(c, GuiTheme.Shape.ROUND, x - 2, y - 2, w + 4, h + 4, a(t.accent(), 0x22, op));
    }

    private static void scan(Canvas c, int x, int y, int w, int h, float op) {
        for (int yy = y + 1; yy < y + h - 1; yy += 2) c.fill(x + 1, yy, x + w - 1, yy + 1, a(0xFFFFFF, 0x0C, op));
    }

    // ------------------------------------------------------------------ parcalar

    /** Dugme arka plani. {@code active=false}: bolum basligi / pasif dugme. */
    public static void button(Canvas c, GuiTheme t, int x, int y, int w, int h, boolean active, boolean hover, float op) {
        if (w <= 0 || h <= 0) return;
        if (w < 6 || h < 6) {
            // cok kucuk dugme: sade dolgu
            c.fill(x, y, x + w, y + h, a(hover ? t.accent() : t.tint(), hover ? 0x60 : Math.max(0x30, t.btnTop()), op));
            return;
        }
        GuiTheme.Shape s = t.shape();
        if (!active) {
            // bolum basligi: hafif vurgu zemini, ortada parlak alt cizgi
            shapeFill(c, s == GuiTheme.Shape.LINE ? GuiTheme.Shape.SQUARE : s, x, y, w, h, a(t.accent(), 0x1C, op), a(t.accent(), 0x0A, op));
            shapeBorder(c, s, x, y, w, h, a(t.accent(), Math.max(0x30, t.border() / 2), op));
            int m = w / 5;
            c.fill(x + m, y + h - 2, x + w - m, y + h - 1, a(t.accent(), 0xB0, op));
            return;
        }
        int top = hover ? a(t.accent2(), 0x50, op) : a(t.tint(), t.btnTop(), op);
        int bot = hover ? a(t.accent(), 0x30, op) : a(t.tint(), t.btnBot(), op);
        shapeFill(c, s == GuiTheme.Shape.LINE || s == GuiTheme.Shape.BRACKET ? GuiTheme.Shape.SQUARE : s, x, y, w, h, top, bot);
        if (t.scan()) scan(c, x, y, w, h, op);
        int border = hover ? a(t.accent(), 0xFF, op) : a(t.accent(), t.border(), op);
        if (s == GuiTheme.Shape.LINE) border = a(t.accent(), hover ? 0xFF : 0x70, op);
        shapeBorder(c, s, x, y, w, h, border);
        if (s == GuiTheme.Shape.BRACKET) shapeBorder(c, GuiTheme.Shape.SQUARE, x, y, w, h, a(t.accent2(), hover ? 0x50 : 0x22, op));
        if ("ALTIN".equals(t.id()) && w > 6 && h > 6) shapeBorder(c, GuiTheme.Shape.SQUARE, x + 2, y + 2, w - 4, h - 4, a(t.accent(), hover ? 0x90 : 0x40, op));
        if (t.bar()) c.fill(x + 1, y + 2, x + 3, y + h - 2, a(t.accent(), hover ? 0xFF : 0x90, op));
        if (t.underline() && s != GuiTheme.Shape.LINE) c.fill(x + 2, y + h - 2, x + w - 2, y + h - 1, a(t.accent(), hover ? 0xFF : 0x70, op));
        if (hover && t.glow()) glow(c, t, x, y, w, h, op);
    }

    /** Kaydirici yolu (degere kadar dolu). */
    public static void sliderTrack(Canvas c, GuiTheme t, int x, int y, int w, int h, double value, boolean hover, float op) {
        GuiTheme.Shape s = t.shape() == GuiTheme.Shape.LINE || t.shape() == GuiTheme.Shape.BRACKET ? GuiTheme.Shape.SQUARE : t.shape();
        shapeFill(c, s, x, y, w, h, a(t.tint(), Math.max(0x18, t.btnBot()), op), a(t.tint(), Math.max(0x18, t.btnBot()), op));
        int fw = (int) Math.round((w - 2) * Math.max(0, Math.min(1, value)));
        if (fw > 0) c.gradient(x + 1, y + 1, x + 1 + fw, y + h - 1, a(t.accent2(), 0x38, op), a(t.accent(), 0x24, op));
        if (t.scan()) scan(c, x, y, w, h, op);
        int border = a(t.accent(), hover ? 0xD0 : Math.max(0x40, t.border()), op);
        if (t.shape() == GuiTheme.Shape.LINE) border = a(t.accent(), hover ? 0xFF : 0x70, op);
        shapeBorder(c, t.shape(), x, y, w, h, border);
        if (hover && t.glow()) glow(c, t, x, y, w, h, op);
    }

    /** Kaydirici tutamaci (8 piksel genis). */
    public static void sliderHandle(Canvas c, GuiTheme t, int x, int y, int w, int h, boolean hover, float op) {
        int cx = x + w / 2;
        c.fill(cx - 2, y + 2, cx + 2, y + h - 2, a(t.accent(), hover ? 0xFF : 0xD0, op));
        c.fill(cx - 1, y + 3, cx + 1, y + h - 3, a(0xFFFFFF, hover ? 0xB0 : 0x70, op));
        if (hover) c.fill(cx - 3, y + 1, cx + 3, y + 2, a(t.accent(), 0x80, op));
    }

    /** Yazi kutusu. */
    public static void editBox(Canvas c, GuiTheme t, int x, int y, int w, int h, boolean focused, float op) {
        GuiTheme.Shape s = t.shape() == GuiTheme.Shape.LINE || t.shape() == GuiTheme.Shape.BRACKET ? GuiTheme.Shape.SQUARE : t.shape();
        shapeFill(c, s, x, y, w, h, a(0x000000, 0x90, op), a(0x000000, 0x70, op));
        shapeBorder(c, t.shape(), x, y, w, h, a(focused ? t.accent() : 0xFFFFFF, focused ? 0xFF : 0x50, op));
        if (focused && t.glow()) glow(c, t, x, y, w, h, op);
    }

    /** Ekranin arkasini karartma (oyun gorunur kalir). */
    public static void overlay(Canvas c, GuiTheme t, int w, int h, float op) {
        if (t.vanilla()) {
            c.gradient(0, 0, w, h, 0x70000000, 0x90000000);
            return;
        }
        c.gradient(0, 0, w, h, a(0x000000, 0x50, op), a(0x000000, 0x88, op));
    }

    /** Ayar paneli. */
    public static void panel(Canvas c, GuiTheme t, int x0, int y0, int x1, int y1, float op) {
        if (t.vanilla()) {
            c.fill(x0, y0, x1, y1, 0x66000000);
            shapeBorder(c, GuiTheme.Shape.SQUARE, x0, y0, x1 - x0, y1 - y0, 0x40B070FF);
            return;
        }
        GuiTheme.Shape s = t.shape() == GuiTheme.Shape.LINE ? GuiTheme.Shape.SQUARE : t.shape();
        shapeFill(c, s == GuiTheme.Shape.BRACKET ? GuiTheme.Shape.SQUARE : s, x0, y0, x1 - x0, y1 - y0, a(t.panel(), t.panelA(), op),
                a(t.panel(), Math.min(255, t.panelA() + 0x18), op));
        if (t.border() > 0) shapeBorder(c, s, x0, y0, x1 - x0, y1 - y0, a(t.accent(), 0x50, op));
        if (t.shape() == GuiTheme.Shape.BRACKET) shapeBorder(c, GuiTheme.Shape.BRACKET, x0 - 1, y0 - 1, x1 - x0 + 2, y1 - y0 + 2, a(t.accent(), 0xE0, op));
        if (t.shape() == GuiTheme.Shape.LINE) c.fill(x0, y0, x1, y0 + 1, a(t.accent(), 0x40, op));
    }

    /** Izgara hucresi / menu karti. */
    public static void cell(Canvas c, GuiTheme t, int x, int y, int w, int h, boolean selected, boolean hover, boolean leftBar, float op) {
        if (w < 6 || h < 6) {
            if (w > 0 && h > 0) c.fill(x, y, x + w, y + h, selected || hover ? a(t.accent(), 0x60, op) : a(t.tint(), Math.max(0x30, t.btnTop()), op));
            return;
        }
        if (t.vanilla()) {
            c.fill(x, y, x + w, y + h, selected ? 0xC0402060 : (hover ? 0xA0303040 : 0x90101018));
            if (selected || leftBar) shapeBorder(c, GuiTheme.Shape.SQUARE, x, y, w, h, selected || hover ? 0xFFD070FF : 0x60B070FF);
            if (leftBar) c.fill(x + 1, y + 1, x + 3, y + h - 1, hover ? 0xFFD070FF : 0x80B070FF);
            return;
        }
        GuiTheme.Shape s = t.shape() == GuiTheme.Shape.LINE || t.shape() == GuiTheme.Shape.BRACKET ? GuiTheme.Shape.SQUARE : t.shape();
        int top, bot;
        if (selected) {
            top = a(t.accent2(), 0x48, op);
            bot = a(t.accent(), 0x30, op);
        } else if (hover) {
            top = a(t.accent2(), 0x34, op);
            bot = a(t.accent(), 0x1C, op);
        } else {
            top = a(t.tint(), t.btnTop(), op);
            bot = a(t.tint(), t.btnBot(), op);
        }
        shapeFill(c, s, x, y, w, h, top, bot);
        if (t.scan()) scan(c, x, y, w, h, op);
        int border = selected ? a(t.accent(), 0xFF, op) : (hover ? a(t.accent(), 0xC0, op) : a(t.accent(), t.border(), op));
        if (t.shape() == GuiTheme.Shape.LINE && !selected && !hover) border = a(t.accent(), 0x40, op);
        shapeBorder(c, t.shape(), x, y, w, h, border);
        if (leftBar || t.bar()) c.fill(x + 1, y + 2, x + 3, y + h - 2, a(t.accent(), hover || selected ? 0xFF : 0x90, op));
        if ((selected || hover) && t.glow()) glow(c, t, x, y, w, h, op);
    }

    /** Kaydirma cubugu. */
    public static void scrollbar(Canvas c, GuiTheme t, int x, int y0, int y1, int barY, int barH, float op) {
        c.fill(x, y0, x + 2, y1, t.vanilla() ? 0x40FFFFFF : a(0xFFFFFF, 0x30, op));
        c.fill(x, barY, x + 2, barY + barH, t.vanilla() ? 0xC0B070FF : a(t.accent(), 0xD0, op));
    }

    /** Baslik ve bolum yazisi rengi (RGB). */
    public static int titleColor(GuiTheme t) {
        return t.vanilla() ? 0xAA00AA : t.accent();
    }

    public static int headerColor(GuiTheme t) {
        return t.vanilla() ? 0xFF55FF : mixWhite(t.accent(), 0.25f);
    }

    static int mixWhite(int c, float k) {
        int r = (c >> 16) & 255, g = (c >> 8) & 255, b = c & 255;
        r = Math.round(r + (255 - r) * k);
        g = Math.round(g + (255 - g) * k);
        b = Math.round(b + (255 - b) * k);
        return (r << 16) | (g << 8) | b;
    }
}
