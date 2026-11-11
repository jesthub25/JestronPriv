package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.ui.GuiTheme;
import com.jestron.jestronpriv.ui.ThemePainter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Menu temasinin oyun tarafi: {@link ThemePainter}'i GuiGraphics'e baglar. Dugme / kaydirici / yazi kutusu kancalari
 * sadece JestronPriv ekranlarinda ({@link JpThemed}) devreye girer; oyunun diger menulerine dokunulmaz.
 */
public final class Theme {

    private static final class GCanvas implements ThemePainter.Canvas {
        GuiGraphics g;

        @Override
        public void fill(int x0, int y0, int x1, int y1, int argb) {
            if (x1 > x0 && y1 > y0 && (argb >>> 24) != 0) g.fill(x0, y0, x1, y1, argb);
        }

        @Override
        public void gradient(int x0, int y0, int x1, int y1, int top, int bottom) {
            if (x1 > x0 && y1 > y0 && ((top >>> 24) != 0 || (bottom >>> 24) != 0)) g.fillGradient(x0, y0, x1, y1, top, bottom);
        }
    }

    private static final GCanvas CANVAS = new GCanvas();
    private static GuiTheme cached = GuiTheme.ALL.get(0);
    // onbellek anahtari (her karede yuzlerce kez sorulur: yazi birlestirmeden karsilastirilir)
    private static String cachedTheme;
    private static boolean cachedCustom;
    private static int cachedAccent;
    private static boolean cachedValid;

    private Theme() {
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    public static GuiTheme current() {
        ModConfig c = cfg();
        if (c == null) return cached;
        String th = c.menuTheme;
        if (!cachedValid || (th != cachedTheme && !java.util.Objects.equals(th, cachedTheme)) || c.menuAccentCustom != cachedCustom
                || c.menuAccent != cachedAccent) {
            GuiTheme t = GuiTheme.get(th);
            if (c.menuAccentCustom && !t.vanilla()) t = t.withAccent(c.menuAccent);
            cached = t;
            cachedTheme = th;
            cachedValid = true;
            cachedCustom = c.menuAccentCustom;
            cachedAccent = c.menuAccent;
        }
        return cached;
    }

    public static float opacity() {
        ModConfig c = cfg();
        return c == null ? 1f : c.menuOpacity;
    }

    private static ThemePainter.Canvas canvas(GuiGraphics g) {
        CANVAS.g = g;
        return CANVAS;
    }

    /** Su anki ekran temali mi ve tema vanilla degil mi. */
    public static boolean active() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.screen instanceof JpThemed && !current().vanilla();
    }

    // ------------------------------------------------------------------ widget kancalari (true: cizildi, vanilla atlanir)

    public static boolean button(GuiGraphics g, int x, int y, int w, int h, boolean active, boolean hover) {
        if (!active()) return false;
        ThemePainter.button(canvas(g), current(), x, y, w, h, active, hover, opacity());
        return true;
    }

    public static boolean sliderTrack(GuiGraphics g, int x, int y, int w, int h, double value, boolean hover) {
        if (!active()) return false;
        ThemePainter.sliderTrack(canvas(g), current(), x, y, w, h, value, hover, opacity());
        return true;
    }

    public static boolean sliderHandle(GuiGraphics g, int x, int y, int w, int h, boolean hover) {
        if (!active()) return false;
        ThemePainter.sliderHandle(canvas(g), current(), x, y, w, h, hover, opacity());
        return true;
    }

    public static boolean editBox(GuiGraphics g, int x, int y, int w, int h, boolean focused) {
        if (!active()) return false;
        ThemePainter.editBox(canvas(g), current(), x, y, w, h, focused, opacity());
        return true;
    }

    // ------------------------------------------------------------------ ekran parcalari (vanilla temada eski gorunum)

    public static void overlay(GuiGraphics g, int w, int h) {
        ThemePainter.overlay(canvas(g), current(), w, h, opacity());
    }

    public static void panel(GuiGraphics g, int x0, int y0, int x1, int y1) {
        ThemePainter.panel(canvas(g), current(), x0, y0, x1, y1, opacity());
    }

    public static void cell(GuiGraphics g, int x, int y, int w, int h, boolean selected, boolean hover, boolean leftBar) {
        ThemePainter.cell(canvas(g), current(), x, y, w, h, selected, hover, leftBar, opacity());
    }

    public static void scrollbar(GuiGraphics g, int x, int y0, int y1, int barY, int barH) {
        ThemePainter.scrollbar(canvas(g), current(), x, y0, y1, barY, barH, opacity());
    }

    /** "JestronPriv » " basligi tema rengiyle. */
    public static MutableComponent title(Component rest) {
        GuiTheme t = current();
        // kok bos: kalin stil sadece "JestronPriv" yazisinda kalsin (alt parcalar ust stili miras alir)
        MutableComponent c = Component.empty();
        c.append(Component.literal("JestronPriv").withStyle(ChatFormatting.BOLD).withColor(ThemePainter.titleColor(t)));
        if (rest != null) {
            c.append(Component.literal(" » ").withColor(0x555555));
            c.append(rest.copy().withColor(0xFFFFFF));
        }
        return c;
    }

    public static int headerColor() {
        return ThemePainter.headerColor(current());
    }

    public static int accent() {
        GuiTheme t = current();
        return t.vanilla() ? 0xD070FF : t.accent();
    }
}
