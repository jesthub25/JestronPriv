package com.jestron.jestronpriv.jc.gui;

import com.jestron.jestronpriv.jc.JestronClient;
import com.jestron.jestronpriv.jc.core.Category;
import com.jestron.jestronpriv.jc.core.Config;
import com.jestron.jestronpriv.jc.core.HudModule;
import com.jestron.jestronpriv.jc.core.Module;
import com.jestron.jestronpriv.jc.core.Modules;
import com.jestron.jestronpriv.jc.core.Setting;
import com.jestron.jestronpriv.jc.core.Theme;
import com.jestron.jestronpriv.jc.integration.EfektMenu;
import com.jestron.jestronpriv.jc.util.TextUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Sağ Shift menüsü. 10 tasarım (MenuStyle): kategoriler solda / üstte / altta; modüller kart, kare ya da
 * liste; "Liste + Ayar" tasarımında ayarlar sağda sürekli açık. Ölçüler orta boy.
 */
public final class JestronMenuScreen extends Screen {
    private static final int PAD = 10, HEADER_H = 18, GAP = 6;
    private static final int CARD_MIN_W = 96, CARD_H = 38, TILE_MIN_W = 70, TILE_H = 52, ROW_H = 20;
    private static final int SET_ROW_H = 20, SET_TEXT_H = 32;
    private static int lastCategory = 0;

    private final Screen parent;
    private MenuStyle st = MenuStyle.current();
    private int styleIdx = -1;
    private int px, py, pw, ph;
    private int cx, cy, cw, ch;           // içerik alanı
    private int listW, setX, setW;         // bölünmüş görünüm
    private int navY;                      // üst/alt sekme satırı
    private EditBox search;
    private String query = "";
    private int category = lastCategory;
    private double scroll, maxScroll, setScroll, setMaxScroll;

    private Module settingsOf;
    private final Map<Setting.Text, EditBox> textBoxes = new LinkedHashMap<>();
    private Setting.Num dragging;
    private Setting.Bind capturing; // "tuşa bas" bekleyen tuş ataması
    private int dragX, dragW;

    private final Map<String, Float> anim = new HashMap<>();
    private long lastNanos, openedAt;
    private boolean dirty;

    private record Hit(String id, int x, int y, int w, int h) {
        boolean in(double mx, double my) {
            return Draw.inside(mx, my, x, y, w, h);
        }
    }

    private record ItemHit(Module m, Hit box, Hit sw, Hit more) {
    }

    private record RowHit(Setting<?> s, Hit box, int ctrlX, int ctrlW) {
    }

    private final List<Hit> buttons = new ArrayList<>();
    private final List<ItemHit> itemHits = new ArrayList<>();
    private final List<RowHit> rowHits = new ArrayList<>();

    public JestronMenuScreen(Screen parent) {
        super(Component.literal("Efekt Menüsü · Modüller"));
        this.parent = parent;
    }

    // ------------------------------------------------------------------ yerleşim

    @Override
    protected void init() {
        if (openedAt == 0) openedAt = System.currentTimeMillis();
        layout();
        search = new EditBox(font, 0, 0, 100, 10, Component.literal("Ara"));
        search.setBordered(false);
        search.setMaxLength(40);
        search.setValue(query);
        search.setResponder(v -> {
            query = v;
            scroll = 0;
        });
        addWidget(search);
        textBoxes.clear();
        if (settingsOf != null) {
            search.setVisible(split());
            buildTextBoxes();
        }
    }

    private boolean split() {
        return st.split();
    }

    private void layout() {
        st = MenuStyle.current();
        styleIdx = Modules.APPEARANCE.style.index();
        if (st.full()) {
            px = 12;
            py = 10;
            pw = width - 24;
            ph = height - 20;
        } else {
            pw = Math.min(width - 16, Math.max(300, Math.min(470, (int) (width * 0.74))));
            ph = Math.min(height - 16, Math.max(200, Math.min(290, (int) (height * 0.80))));
            px = (width - pw) / 2;
            py = (height - ph) / 2;
        }
        int bottom = py + ph - PAD;
        switch (st.nav()) {
            case LEFT -> {
                cx = px + navW() + PAD;
                cy = py + PAD + HEADER_H + 8;
            }
            case TOP -> {
                cx = px + PAD;
                navY = py + PAD + HEADER_H + 7;
                cy = navY + 16 + 8;
            }
            case BOTTOM -> {
                cx = px + PAD;
                navY = py + ph - PAD - 17;
                cy = py + PAD + HEADER_H + 8;
                bottom = navY - 7;
            }
        }
        cw = px + pw - PAD - cx;
        ch = Math.max(40, bottom - cy);
        listW = st.split() ? Math.max(110, (int) (cw * 0.42)) : cw;
        setX = cx + listW + 8;
        setW = cx + cw - setX;
    }

    private int navW() {
        return pw < 380 ? 86 : 94;
    }

    private void buildTextBoxes() {
        for (EditBox b : textBoxes.values()) removeWidget(b);
        textBoxes.clear();
        if (settingsOf == null) return;
        for (Setting<?> s : settingsOf.settings()) {
            if (!s.visible || !(s instanceof Setting.Text t)) continue;
            EditBox b = new EditBox(font, 0, 0, 100, 10, Component.literal(t.label));
            b.setBordered(false);
            b.setMaxLength(t.maxLength);
            b.setValue(t.get());
            b.setResponder(v -> {
                t.set(v);
                if (settingsOf != null) settingsOf.onSettingChanged(t);
                dirty = true;
            });
            addWidget(b);
            textBoxes.put(t, b);
        }
    }

    private void openSettingsInternal(Module m) {
        settingsOf = m;
        capturing = null;
        setScroll = 0;
        dragging = null;
        if (search != null && !split()) search.setVisible(false);
        setFocused(null);
        buildTextBoxes();
    }

    /** Oyun içi test ve kısayollar için. */
    public void openSettings(Module m) {
        openSettingsInternal(m);
    }

    private void closeSettings() {
        capturing = null;
        for (EditBox b : textBoxes.values()) removeWidget(b);
        textBoxes.clear();
        settingsOf = null;
        dragging = null;
        if (search != null) search.setVisible(true);
        setFocused(null);
    }

    private List<Module> visibleModules() {
        List<Module> out = new ArrayList<>();
        String q = TextUtil.norm(query.trim());
        for (Module m : Modules.ALL) {
            if (category > 0 && m.category != Category.values()[category - 1]) continue;
            if (!q.isEmpty() && !TextUtil.norm(m.name + " " + m.description + " " + m.category.label).contains(q)) continue;
            out.add(m);
        }
        return out;
    }

    // ------------------------------------------------------------------ çizim yardımcıları

    /** Stile göre kutu: yuvarlak kenarlı ya da vanilla tarzı kabartma. */
    private void box(GuiGraphics g, int x1, int y1, int x2, int y2, int fill, int border, boolean raised) {
        if (x2 <= x1 || y2 <= y1) return;
        if (st.bevel()) {
            g.fill(x1, y1, x2, y2, fill);
            int light, dark;
            if (fill == st.card() || fill == st.cardHover() || fill == st.navActive()) {
                light = raised ? 0xFFAAAAAA : 0xFF2E2E2E;
                dark = raised ? 0xFF2E2E2E : 0xFFAAAAAA;
            } else {
                light = raised ? 0xFFFFFFFF : 0xFF373737;
                dark = raised ? 0xFF555555 : 0xFFFFFFFF;
            }
            g.fill(x1, y1, x2 - 1, y1 + 1, light);
            g.fill(x1, y1, x1 + 1, y2 - 1, light);
            g.fill(x1 + 1, y2 - 1, x2, y2, dark);
            g.fill(x2 - 1, y1 + 1, x2, y2, dark);
            return;
        }
        if ((border >>> 24) == 0) {
            Draw.roundRect(g, x1, y1, x2, y2, st.radius(), fill);
        } else if ((fill >>> 24) == 0xFF) {
            Draw.card(g, x1, y1, x2, y2, st.radius(), fill, border);
        } else {
            // yarı saydam dolgu: kenar çizgisi üst üste binmesin
            int r = Math.min(st.radius(), Math.min(x2 - x1, y2 - y1) / 2);
            Draw.roundRect(g, x1 + 1, y1 + 1, x2 - 1, y2 - 1, Math.max(0, r - 1), fill);
            int c = r >= 3 ? r - 1 : r;
            g.fill(x1 + c, y1, x2 - c, y1 + 1, border);
            g.fill(x1 + c, y2 - 1, x2 - c, y2, border);
            g.fill(x1, y1 + c, x1 + 1, y2 - c, border);
            g.fill(x2 - 1, y1 + c, x2, y2 - c, border);
            if (r >= 3) {
                g.fill(x1 + 1, y1 + 1, x1 + 2, y1 + 2, border);
                g.fill(x2 - 2, y1 + 1, x2 - 1, y1 + 2, border);
                g.fill(x1 + 1, y2 - 2, x1 + 2, y2 - 1, border);
                g.fill(x2 - 2, y2 - 2, x2 - 1, y2 - 1, border);
            }
        }
    }

    private void toggle(GuiGraphics g, int x, int y, int w, int h, float t) {
        int on = st.accent(), off = st.track();
        if (st.bevel() || st.radius() == 0) {
            g.fill(x, y, x + w, y + h, Draw.lerpColor(off, on, t));
            int k = h - 4, kx = x + 2 + Math.round((w - 4 - k) * t);
            g.fill(kx, y + 2, kx + k, y + 2 + k, 0xFFFFFFFF);
            if (st.bevel()) {
                g.fill(x, y, x + w, y + 1, 0xFF373737);
                g.fill(x, y, x + 1, y + h, 0xFF373737);
            }
            return;
        }
        Draw.toggle(g, x, y, w, h, t, on, off);
    }

    private float animate(String key, boolean on, float dt) {
        float cur = anim.getOrDefault(key, on ? 1f : 0f);
        float target = on ? 1f : 0f, step = dt * 10f;
        cur = cur < target ? Math.min(target, cur + step) : Math.max(target, cur - step);
        anim.put(key, cur);
        return cur;
    }

    private float openProgress() {
        float t = Math.min(1f, (System.currentTimeMillis() - openedAt) / 150f);
        return 1f - (1f - t) * (1f - t);
    }

    private void text(GuiGraphics g, String s, int x, int y, int color) {
        g.drawString(font, s, x, y, color, false);
    }

    private void bold(GuiGraphics g, String s, int x, int y, int color, boolean shadow) {
        g.drawString(font, Component.literal(s).withStyle(v -> v.withBold(true)), x, y, color, shadow);
    }

    private int boldW(String s) {
        return font.width(Component.literal(s).withStyle(v -> v.withBold(true)));
    }

    private String boldEllipsize(String s, int maxW) {
        if (boldW(s) <= maxW) return s;
        String out = s;
        while (out.length() > 1 && boldW(out + "…") > maxW) out = out.substring(0, out.length() - 1);
        return out + "…";
    }

    // ------------------------------------------------------------------ çizim

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // bulanıklaştırma yok: oyun arkada görünür, HUD değişiklikleri canlı izlenir
        g.fill(0, 0, width, height, Draw.withAlpha(st.overlay(), openProgress()));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        if (styleIdx != Modules.APPEARANCE.style.index()) {
            layout();
            if (settingsOf != null && search != null) search.setVisible(split());
        }
        long now = System.nanoTime();
        float dt = lastNanos == 0 ? 0f : Math.min(0.1f, (now - lastNanos) / 1_000_000_000f);
        lastNanos = now;

        renderBackground(g, mouseX, mouseY, partialTick);
        int slide = Math.round((1f - openProgress()) * 8f);
        g.pose().pushPose();
        g.pose().translate(0, slide, 0);
        try {
            int my = mouseY - slide;
            buttons.clear();
            if (!st.full()) box(g, px, py, px + pw, py + ph, st.panel(), st.border(), true);
            drawNav(g, mouseX, my);
            drawHeader(g, mouseX, my);
            if (split()) {
                drawList(g, mouseX, my, dt, cx, listW);
                g.fill(setX - 4, cy, setX - 3, cy + ch, st.border());
                if (settingsOf != null) {
                    drawSettings(g, mouseX, my, dt, setX, setW, false);
                } else {
                    rowHits.clear();
                    String t = Draw.ellipsize(font, "Ayarlar için bir modül seç", setW);
                    text(g, t, setX + (setW - font.width(t)) / 2, cy + ch / 2 - 4, st.muted());
                }
            } else if (settingsOf == null) {
                drawList(g, mouseX, my, dt, cx, cw);
            } else {
                drawSettings(g, mouseX, my, dt, cx, cw, true);
            }
        } finally {
            g.pose().popPose();
        }
    }

    private void logo(GuiGraphics g, int x, int y) {
        bold(g, "jestron", x, y, st.text(), st.bevel());
        int dx = x + boldW("jestron") + 1;
        g.fill(dx, y + 5, dx + 2, y + 7, st.accent());
    }

    private void drawNav(GuiGraphics g, int mx, int my) {
        int n = Category.values().length + 1;
        if (st.nav() == MenuStyle.Nav.LEFT) {
            int nw = navW();
            if (st.bevel()) {
                box(g, px + 4, py + 4, px + nw - 2, py + ph - 4, st.navBg(), 0, false);
            } else {
                Draw.roundRect(g, px + 1, py + 1, px + nw, py + ph - 1, Math.max(0, st.radius() - 1), st.navBg());
                g.fill(px + nw - Math.max(1, st.radius()), py + 1, px + nw, py + ph - 1, st.navBg());
                g.fill(px + nw, py + 1, px + nw + 1, py + ph - 1, st.border());
            }
            int x = px + 9, y = py + PAD + 1;
            logo(g, x, y);
            y += 22;
            for (int i = 0; i < n; i++) {
                String label = i == 0 ? "Tümü" : Category.values()[i - 1].label;
                boolean active = isActiveCat(i);
                boolean hover = Draw.inside(mx, my, px + 5, y - 3, nw - 10, 14);
                if (active || hover) {
                    int c = active ? st.navActive() : Draw.withAlpha(st.navActive(), 0.45f);
                    if (st.bevel()) box(g, px + 7, y - 3, px + nw - 5, y + 11, active ? st.navActive() : st.cardHover(), 0, true);
                    else Draw.roundRect(g, px + 5, y - 3, px + nw - 5, y + 11, Math.min(4, st.radius()), c);
                }
                if (active && !st.bevel()) g.fill(px + 9, y + 2, px + 11, y + 4, st.accent());
                int tc = active ? st.navActiveText() : (st.bevel() && hover ? st.cardText() : st.muted());
                g.drawString(font, label, x + 5, y, tc, st.bevel() && (active || hover));
                buttons.add(new Hit("cat" + i, px + 5, y - 3, nw - 10, 14));
                y += 15;
            }
            int by = py + ph - PAD - 14;
            if (EfektMenu.isLoaded()) {
                navButton(g, mx, my, "efekt", "Efekt Menüsü", px + 6, by, nw - 12, false);
                by -= 18;
            }
            navButton(g, mx, my, "hud", "HUD Düzenle", px + 6, by, nw - 12, true);
            return;
        }
        // üst ya da alt sekme satırı
        int[] widths = new int[n];
        int total = 0;
        for (int i = 0; i < n; i++) {
            widths[i] = font.width(i == 0 ? "Tümü" : Category.values()[i - 1].label) + 14;
            total += widths[i] + (i > 0 ? 3 : 0);
        }
        boolean pills = st.pillTabs() || st.nav() == MenuStyle.Nav.BOTTOM;
        int x = st.nav() == MenuStyle.Nav.BOTTOM ? px + (pw - total) / 2 : cx + (pills ? 3 : 0);
        if (pills) {
            box(g, x - 3, navY - 2, x + total + 3, navY + 18, st.navBg(),
                    st.nav() == MenuStyle.Nav.BOTTOM ? st.border() : 0, true);
        } else {
            g.fill(cx, navY + 15, cx + cw, navY + 16, st.border());
        }
        int start = x;
        for (int i = 0; i < n; i++) {
            String label = i == 0 ? "Tümü" : Category.values()[i - 1].label;
            boolean active = isActiveCat(i);
            boolean hover = Draw.inside(mx, my, x, navY, widths[i], 16);
            if (pills) {
                int r = Math.min(7, Math.max(2, st.radius()));
                if (active) Draw.roundRect(g, x, navY, x + widths[i], navY + 16, r, st.navActive());
                else if (hover) Draw.roundRect(g, x, navY, x + widths[i], navY + 16, r, Draw.withAlpha(st.cardHover(), 0.9f));
                text(g, label, x + 7, navY + 4, active ? st.navActiveText() : (hover ? st.text() : st.muted()));
            } else {
                if (active) g.fill(x + 3, navY + 13, x + widths[i] - 3, navY + 15, st.accent());
                text(g, label, x + 7, navY + 3, active || hover ? st.text() : st.muted());
            }
            buttons.add(new Hit("cat" + i, x, navY, widths[i], 16));
            x += widths[i] + 3;
        }
        // açık modül sayısı (yer varsa)
        if (st.nav() == MenuStyle.Nav.TOP) {
            String count = countText();
            int tx = cx + cw - font.width(count);
            if (tx > start + total + 10) text(g, count, tx, navY + 4, st.muted());
        }
    }

    private boolean isActiveCat(int i) {
        if (settingsOf != null && !split()) return i > 0 && settingsOf.category == Category.values()[i - 1];
        return category == i;
    }

    private void navButton(GuiGraphics g, int mx, int my, String id, String label, int x, int y, int w, boolean primary) {
        boolean hover = Draw.inside(mx, my, x, y, w, 14);
        String l = Draw.ellipsize(font, label, w - 6);
        if (primary) {
            int c = hover ? Draw.lerpColor(st.accent(), 0xFF000000, 0.12f) : st.accent();
            if (st.bevel()) box(g, x, y, x + w, y + 14, c, 0, true);
            else Draw.roundRect(g, x, y, x + w, y + 14, Math.min(7, Math.max(0, st.radius() + 1)), c);
            g.drawString(font, l, x + (w - font.width(l)) / 2, y + 3, st.onAccent(), st.bevel());
        } else {
            box(g, x, y, x + w, y + 14, hover ? st.cardHover() : st.card(), st.border(), true);
            g.drawString(font, l, x + (w - font.width(l)) / 2, y + 3, st.cardText(), st.bevel());
        }
        buttons.add(new Hit(id, x, y, w, 14));
    }

    private String countText() {
        List<Module> list = visibleModules();
        long on = list.stream().filter(m -> m.toggleable() && m.isEnabled()).count();
        return on + "/" + list.stream().filter(Module::toggleable).count() + " açık";
    }

    private void drawHeader(GuiGraphics g, int mx, int my) {
        int hy = py + PAD;
        int x1 = cx, x2;
        // Ayarlar tam sayfa açıkken başlık satırını "‹ Geri", modül adı ve Sıfırla kullanır: logo/kısayollar çizilmez
        if (settingsOf != null && !split()) return;
        if (st.nav() != MenuStyle.Nav.LEFT) {
            // logo + sağda kısayollar
            logo(g, px + PAD, hy + 5);
            x1 = px + PAD + boldW("jestron") + 16;
            int bx = cx + cw;
            if (EfektMenu.isLoaded()) {
                bx -= 44;
                navButton(g, mx, my, "efekt", "Efekt", bx, hy + 2, 44, false);
                bx -= 4;
            }
            bx -= 32;
            navButton(g, mx, my, "hud", "HUD", bx, hy + 2, 32, true);
            x2 = bx - 6;
        } else {
            String count = countText();
            x2 = cx + cw - font.width(count) - 8;
            text(g, count, cx + cw - font.width(count), hy + 5, st.muted());
        }
        if (settingsOf != null && !split()) return;
        boolean focused = search.isFocused();
        int fw = Math.max(20, x2 - x1 - 14);
        box(g, x1, hy, x2, hy + HEADER_H, st.bevel() ? 0xFF000000 : st.card(), focused ? st.accent() : st.border(), false);
        search.setX(x1 + 7);
        search.setY(hy + 5);
        search.setWidth(fw);
        drawField(g, search, x1 + 7, hy + 5, fw, "Mod ara…", st.bevel() ? 0xFFFFFFFF : st.cardText());
        buttons.add(new Hit("search", x1, hy, x2 - x1, HEADER_H));
    }

    private void drawList(GuiGraphics g, int mx, int my, float dt, int x0, int w) {
        List<Module> list = visibleModules();
        itemHits.clear();
        int cols, iw, ih, gap;
        switch (st.item()) {
            case TILE -> {
                gap = GAP;
                cols = Math.max(1, (w + gap) / (TILE_MIN_W + gap));
                iw = (w - (cols - 1) * gap) / cols;
                ih = TILE_H;
            }
            case ROW -> {
                gap = 2;
                cols = 1;
                iw = w;
                ih = ROW_H;
            }
            default -> {
                gap = GAP;
                cols = Math.max(1, (w + gap) / (CARD_MIN_W + gap));
                iw = (w - (cols - 1) * gap) / cols;
                ih = CARD_H;
            }
        }
        int rows = (list.size() + cols - 1) / cols;
        int total = Math.max(0, rows * (ih + gap) - gap);
        maxScroll = Math.max(0, total - ch);
        scroll = Math.max(0, Math.min(maxScroll, scroll));
        g.enableScissor(x0 - 2, cy, x0 + w + 2, cy + ch);
        try {
            if (list.isEmpty()) {
                String t = "Sonuç yok";
                text(g, t, x0 + (w - font.width(t)) / 2, cy + 24, st.muted());
            }
            for (int i = 0; i < list.size(); i++) {
                int x = x0 + (i % cols) * (iw + gap);
                int y = cy + (i / cols) * (ih + gap) - (int) Math.round(scroll);
                if (y + ih < cy || y > cy + ch) continue;
                Module m = list.get(i);
                switch (st.item()) {
                    case TILE -> drawTile(g, m, x, y, iw, ih, mx, my, dt);
                    case ROW -> drawRowItem(g, m, x, y, iw, ih, mx, my, dt);
                    default -> drawCard(g, m, x, y, iw, ih, mx, my, dt);
                }
            }
        } finally {
            g.disableScissor();
        }
        scrollbar(g, x0 + w + (split() ? 1 : 4), cy, ch, scroll, maxScroll);
    }

    private boolean inList(double my) {
        return my >= cy && my < cy + ch;
    }

    private void drawCard(GuiGraphics g, Module m, int x, int y, int w, int h, int mx, int my, float dt) {
        boolean hover = inList(my) && Draw.inside(mx, my, x, y, w, h);
        boolean on = m.toggleable() && m.isEnabled();
        float t = animate("item:" + m.id, on, dt);
        int border = Draw.lerpColor(st.border(), st.accent(), t);
        box(g, x, y, x + w, y + h, hover ? st.cardHover() : st.card(), border, true);
        boolean shadow = st.bevel();
        int swW = 18, swH = 10;
        Hit sw = null, more = null;
        if (m.toggleable()) {
            sw = new Hit("sw", x + w - 6 - swW, y + 6, swW, swH);
            toggle(g, sw.x, sw.y, swW, swH, t);
        }
        int titleW = w - 12 - (sw != null ? swW + 4 : 0);
        bold(g, boldEllipsize(m.name, titleW), x + 6, y + 7, st.cardText(), shadow);
        int descW = w - 12 - (m.hasVisibleSettings() ? 12 : 0);
        g.drawString(font, Draw.ellipsize(font, m.description, descW), x + 6, y + 23, st.cardMuted(), shadow);
        if (m.hasVisibleSettings()) {
            more = new Hit("more", x + w - 14, y + 19, 12, 16);
            boolean mh = inList(my) && more.in(mx, my);
            g.drawString(font, "›", more.x + 4, y + 23, mh ? st.accent() : st.cardMuted(), shadow);
        }
        itemHits.add(new ItemHit(m, new Hit("item", x, y, w, h), sw, more));
    }

    private void drawTile(GuiGraphics g, Module m, int x, int y, int w, int h, int mx, int my, float dt) {
        boolean hover = inList(my) && Draw.inside(mx, my, x, y, w, h);
        boolean on = m.toggleable() && m.isEnabled();
        float t = animate("item:" + m.id, on, dt);
        box(g, x, y, x + w, y + h, hover ? st.cardHover() : st.card(), st.border(), true);
        // büyük baş harf
        String letter = m.name.substring(0, 1).toUpperCase(Locale.ROOT);
        g.pose().pushPose();
        try {
            g.pose().translate(x + w / 2f, y + 8, 0);
            g.pose().scale(2f, 2f, 1f);
            int lc = m.toggleable() ? Draw.lerpColor(st.cardMuted(), st.accent(), t) : st.cardMuted();
            g.drawString(font, letter, -font.width(letter) / 2, 0, lc, false);
        } finally {
            g.pose().popPose();
        }
        String name = Draw.ellipsize(font, m.name, w - 8);
        g.drawString(font, name, x + (w - font.width(name)) / 2, y + 30, st.cardText(), false);
        // alt durum çubuğu
        int bar = m.toggleable() ? Draw.lerpColor(st.track(), st.accent(), t) : Draw.withAlpha(st.track(), 0.5f);
        if (st.radius() > 2) Draw.roundRect(g, x + 10, y + h - 8, x + w - 10, y + h - 5, 1, bar);
        else g.fill(x + 8, y + h - 7, x + w - 8, y + h - 5, bar);
        Hit more = null;
        if (m.hasVisibleSettings()) {
            more = new Hit("more", x + w - 14, y + 1, 13, 13);
            boolean mh = inList(my) && more.in(mx, my);
            g.drawString(font, "›", more.x + 5, y + 4, mh ? st.accent() : st.cardMuted(), false);
        }
        itemHits.add(new ItemHit(m, new Hit("item", x, y, w, h), null, more));
    }

    private void drawRowItem(GuiGraphics g, Module m, int x, int y, int w, int h, int mx, int my, float dt) {
        boolean hover = inList(my) && Draw.inside(mx, my, x, y, w, h);
        boolean on = m.toggleable() && m.isEnabled();
        boolean selected = split() && settingsOf == m;
        float t = animate("item:" + m.id, on, dt);
        if (selected) box(g, x, y, x + w, y + h, st.navActive(), 0, true);
        else if (hover) box(g, x, y, x + w, y + h, st.cardHover(), 0, true);
        else g.fill(x + 4, y + h, x + w - 4, y + h + 1, Draw.withAlpha(st.border(), 0.7f));
        if (selected) g.fill(x, y + 4, x + 2, y + h - 4, st.accent());
        int swW = 18, swH = 10;
        Hit sw = null, more = null;
        int right = x + w - 5;
        if (m.toggleable()) {
            sw = new Hit("sw", right - swW, y + (h - swH) / 2, swW, swH);
            toggle(g, sw.x, sw.y, swW, swH, t);
            right -= swW + 6;
        }
        if (m.hasVisibleSettings() && !split()) {
            more = new Hit("more", right - 10, y + 2, 12, h - 4);
            boolean mh = inList(my) && more.in(mx, my);
            text(g, "›", more.x + 4, y + 6, mh ? st.accent() : st.cardMuted());
            right -= 14;
        }
        int nameC = selected ? st.navActiveText() : st.cardText();
        String name = Draw.ellipsize(font, m.name, Math.max(10, right - x - 10));
        text(g, name, x + 7, y + 6, nameC);
        int dx = x + 7 + font.width(name) + 8;
        if (right - dx > 30) {
            text(g, Draw.ellipsize(font, m.description, right - dx - 4), dx, y + 6,
                    selected ? Draw.withAlpha(nameC, 0.65f) : st.cardMuted());
        }
        itemHits.add(new ItemHit(m, new Hit("item", x, y, w, h), sw, more));
    }

    private void scrollbar(GuiGraphics g, int x, int top, int areaH, double sc, double max) {
        if (max <= 0) return;
        int barH = Math.max(14, (int) (areaH * (double) areaH / (areaH + max)));
        int barY = top + (int) ((areaH - barH) * (sc / max));
        g.fill(x, barY, x + 2, barY + barH, st.track());
    }

    private void drawSettings(GuiGraphics g, int mx, int my, float dt, int x0, int w, boolean withBack) {
        Module m = settingsOf;
        int hy = withBack ? py + PAD : cy;
        int titleX = x0;
        if (withBack) {
            boolean bh = Draw.inside(mx, my, x0, hy, 42, HEADER_H);
            box(g, x0, hy, x0 + 42, hy + HEADER_H, bh ? st.cardHover() : st.card(), st.border(), true);
            g.drawString(font, "‹ Geri", x0 + 7, hy + 5, st.cardText(), st.bevel());
            buttons.add(new Hit("back", x0, hy, 42, HEADER_H));
            titleX = x0 + 50;
        }
        String reset = "Sıfırla";
        int rw = font.width(reset) + 12, rx = x0 + w - rw;
        boolean rh = Draw.inside(mx, my, rx, hy, rw, HEADER_H);
        box(g, rx, hy, rx + rw, hy + HEADER_H, rh ? st.cardHover() : st.card(), st.border(), true);
        g.drawString(font, reset, rx + 6, hy + 5, st.cardMuted(), st.bevel());
        buttons.add(new Hit("reset", rx, hy, rw, HEADER_H));
        int titleRight = rx - 6;
        if (m.toggleable()) {
            float t = animate("item:" + m.id, m.isEnabled(), dt);
            int tx = rx - 8 - 22;
            toggle(g, tx, hy + 4, 22, 11, t);
            buttons.add(new Hit("toggle", tx, hy + 4, 22, 11));
            titleRight = tx - 6;
        }
        bold(g, boldEllipsize(m.name, Math.max(10, titleRight - titleX)), titleX, hy + 5, st.text(), st.bevel());

        int top = withBack ? cy : cy + HEADER_H + 6;
        int areaH = cy + ch - top;
        List<Setting<?>> vis = new ArrayList<>();
        for (Setting<?> s : m.settings()) if (s.visible) vis.add(s);
        boolean hud = m instanceof HudModule;
        int total = 0;
        for (Setting<?> s : vis) total += s instanceof Setting.Text ? SET_TEXT_H : SET_ROW_H;
        total += (hud ? SET_ROW_H : 0) + (m == Modules.MACROS ? 20 : 0);
        setMaxScroll = Math.max(0, total - areaH);
        setScroll = Math.max(0, Math.min(setMaxScroll, setScroll));

        rowHits.clear();
        for (EditBox b : textBoxes.values()) b.visible = false;
        g.enableScissor(x0 - 2, top, x0 + w + 2, top + areaH);
        try {
            int y = top - (int) Math.round(setScroll);
            if (vis.isEmpty() && !hud) text(g, "Bu modülün ayarı yok.", x0 + 2, y + 6, st.muted());
            for (Setting<?> s : vis) {
                int h = s instanceof Setting.Text ? SET_TEXT_H : SET_ROW_H;
                if (y + h >= top && y <= top + areaH) drawSettingRow(g, s, x0, w, y, h, top, areaH, mx, my, dt);
                y += h;
            }
            if (hud) {
                String l = "HUD düzenleyicide taşı ›";
                int bw = Math.min(w, font.width(l) + 14);
                boolean hv = my >= top && my < top + areaH && Draw.inside(mx, my, x0, y + 3, bw, 14);
                box(g, x0, y + 3, x0 + bw, y + 17, hv ? st.cardHover() : st.card(), st.border(), true);
                g.drawString(font, Draw.ellipsize(font, l, bw - 10), x0 + 7, y + 6, st.cardText(), st.bevel());
                rowHits.add(new RowHit(null, new Hit("hud", x0, y + 3, bw, 14), 0, 0));
                y += SET_ROW_H;
            }
            if (m == Modules.MACROS) {
                text(g, Draw.ellipsize(font, "Tuşa tıkla, sonra istediğin tuşa bas · Esc: tuşu kaldır", w), x0 + 2, y + 6, st.muted());
            }
        } finally {
            g.disableScissor();
        }
        scrollbar(g, x0 + w + 3, top, areaH, setScroll, setMaxScroll);
    }

    private void drawSettingRow(GuiGraphics g, Setting<?> s, int x0, int w, int y, int h, int top, int areaH, int mx, int my, float dt) {
        boolean inArea = my >= top && my < top + areaH;
        g.fill(x0, y + h - 1, x0 + w, y + h, Draw.withAlpha(st.border(), 0.8f));
        int ty = y + (h - 8) / 2;
        if (s instanceof Setting.Text t) {
            text(g, Draw.ellipsize(font, t.label, w), x0 + 1, y + 3, st.text());
            EditBox b = textBoxes.get(t);
            int by = y + 14;
            box(g, x0, by, x0 + w, by + 14, st.bevel() ? 0xFF000000 : st.card(), b != null && b.isFocused() ? st.accent() : st.border(), false);
            if (b != null) {
                b.setX(x0 + 6);
                b.setY(by + 3);
                b.setWidth(Math.max(10, w - 12));
                b.visible = by >= top && by + 14 <= top + areaH;
                if (b.visible) drawField(g, b, x0 + 6, by + 3, Math.max(10, w - 12), "", st.bevel() ? 0xFFFFFFFF : st.cardText());
            }
            rowHits.add(new RowHit(s, new Hit("row", x0, y, w, h), x0, w));
            return;
        }
        int ctrlW, ctrlX;
        if (s instanceof Setting.Bool b) {
            ctrlW = 22;
            ctrlX = x0 + w - ctrlW;
            toggle(g, ctrlX, y + (h - 11) / 2, 22, 11, animate("set:" + settingsOf.id + ":" + s.key, b.on(), dt));
        } else if (s instanceof Setting.Num n) {
            ctrlW = Math.max(40, Math.min(110, w / 2 - 40));
            ctrlX = x0 + w - ctrlW;
            int cyy = y + h / 2;
            int fillW = (int) Math.round(ctrlW * n.fraction());
            g.fill(ctrlX, cyy - 1, ctrlX + ctrlW, cyy + 1, st.track());
            g.fill(ctrlX, cyy - 1, ctrlX + fillW, cyy + 1, st.accent());
            int kx = ctrlX + fillW;
            if (st.radius() == 0 || st.bevel()) {
                g.fill(kx - 3, cyy - 4, kx + 3, cyy + 4, st.accent());
            } else {
                Draw.roundRect(g, kx - 4, cyy - 4, kx + 4, cyy + 4, 4, st.accent());
                Draw.roundRect(g, kx - 2, cyy - 2, kx + 2, cyy + 2, 2, 0xFFFFFFFF);
            }
            String v = n.display();
            text(g, v, ctrlX - 7 - font.width(v), ty, st.muted());
        } else if (s == Modules.APPEARANCE.accent) {
            Setting.Choice c = (Setting.Choice) s;
            int sw = 11, gap = 4;
            ctrlW = c.options.length * (sw + gap) - gap;
            ctrlX = x0 + w - ctrlW;
            for (int i = 0; i < c.options.length; i++) {
                int sx = ctrlX + i * (sw + gap), sy = y + (h - sw) / 2;
                if (i == c.index()) g.fill(sx - 2, sy - 2, sx + sw + 2, sy + sw + 2, st.text());
                int col = Theme.accentPreview(i) == 0xFF18181B && st.dark() ? 0xFFF4F4F5 : Theme.accentPreview(i);
                if (st.radius() == 0 || st.bevel()) g.fill(sx, sy, sx + sw, sy + sw, col);
                else Draw.roundRect(g, sx, sy, sx + sw, sy + sw, 5, col);
            }
        } else if (s instanceof Setting.Bind b) {
            boolean cap = capturing == b;
            String conflict = cap ? null : b.conflict();
            String v = cap ? "Bir tuşa bas…" : Draw.ellipsize(font, b.display(), Math.max(20, w / 2 - 40));
            ctrlW = Math.max(64, font.width(v) + 16);
            ctrlX = x0 + w - ctrlW;
            boolean hv = inArea && Draw.inside(mx, my, ctrlX, y + 3, ctrlW, h - 6);
            int fill = cap ? st.accent() : hv ? st.cardHover() : st.card();
            box(g, ctrlX, y + 3, ctrlX + ctrlW, y + h - 3, fill, cap ? st.accent() : conflict != null ? 0xFFEF4444 : st.border(), true);
            int vc = cap ? (MenuStyle.luminance(st.accent()) > 0.6 ? 0xFF18181B : 0xFFFFFFFF) : !b.bound() ? st.cardMuted() : conflict != null ? 0xFFEF4444 : st.cardText();
            g.drawString(font, v, ctrlX + (ctrlW - font.width(v)) / 2, ty, vc, st.bevel());
            if (conflict != null && hv) {
                String t = "Çakışıyor: " + conflict;
                int tw = font.width(t) + 8, tx = Math.max(x0, ctrlX + ctrlW - tw);
                g.fill(tx, y - 11, tx + tw, y + 1, 0xEE18181B);
                g.drawString(font, t, tx + 4, y - 9, 0xFFFCA5A5, false);
            }
        } else if (s instanceof Setting.Choice c) {
            String v = Draw.ellipsize(font, c.display(), Math.max(20, w / 2 - 30));
            ctrlW = Math.max(60, font.width(v) + 26);
            ctrlX = x0 + w - ctrlW;
            boolean hv = inArea && Draw.inside(mx, my, ctrlX, y + 3, ctrlW, h - 6);
            box(g, ctrlX, y + 3, ctrlX + ctrlW, y + h - 3, hv ? st.cardHover() : st.card(), st.border(), true);
            g.drawString(font, "‹", ctrlX + 5, ty, st.cardMuted(), st.bevel());
            g.drawString(font, "›", ctrlX + ctrlW - 9, ty, st.cardMuted(), st.bevel());
            g.drawString(font, v, ctrlX + (ctrlW - font.width(v)) / 2, ty, st.cardText(), st.bevel());
        } else {
            ctrlW = 0;
            ctrlX = x0 + w;
        }
        int labelMax = Math.max(10, ctrlX - x0 - 8 - (s instanceof Setting.Num n2 ? font.width(n2.display()) + 8 : 0));
        text(g, Draw.ellipsize(font, s.label, labelMax), x0 + 1, ty, st.text());
        rowHits.add(new RowHit(s, new Hit("row", x0, y, w, h), ctrlX, ctrlW));
    }

    /**
     * Yazı kutusunu kendimiz çizeriz (EditBox yazıyı gölgeli çizer, açık temada bulanık görünür).
     * EditBox sadece klavye girişi için kullanılır.
     */
    private void drawField(GuiGraphics g, EditBox b, int x, int y, int w, String hint, int color) {
        String v = b.getValue();
        int cursor = Math.max(0, Math.min(v.length(), b.getCursorPosition()));
        if (v.isEmpty() && !b.isFocused()) {
            if (!hint.isEmpty()) g.drawString(font, Draw.ellipsize(font, hint, w), x, y, st.bevel() ? 0xFFA0A0A0 : st.cardMuted(), false);
            return;
        }
        int start = 0;
        if (font.width(v) > w) {
            start = cursor;
            while (start > 0 && font.width(v.substring(start - 1, cursor)) < w - 6) start--;
        }
        String vis = font.plainSubstrByWidth(v.substring(start), w);
        g.drawString(font, vis, x, y, color, false);
        if (b.isFocused() && (System.currentTimeMillis() / 530) % 2 == 0) {
            int end = Math.min(cursor, start + vis.length());
            int ccx = x + font.width(v.substring(start, Math.max(start, end)));
            g.fill(ccx, y - 1, ccx + 1, y + 9, st.accent());
        }
    }

    // ------------------------------------------------------------------ giriş

    private void changed(Setting<?> s) {
        if (settingsOf != null) settingsOf.onSettingChanged(s);
        dirty = true;
    }

    private double slideY(double my) {
        return my - Math.round((1f - openProgress()) * 8f);
    }

    @Override
    public boolean mouseClicked(double mx, double myRaw, int button) {
        if (capturing != null) {
            Setting.Bind b = capturing;
            capturing = null;
            if (button >= 2) { // orta tuş ve yan tuşlar atanabilir
                b.bindKey(com.mojang.blaze3d.platform.InputConstants.Type.MOUSE.getOrCreate(button));
                dirty = true;
                return true;
            }
        }
        double my = slideY(myRaw);
        for (EditBox b : textBoxes.values()) {
            if (b.visible && Draw.inside(mx, my, b.getX() - 6, b.getY() - 3, b.getWidth() + 12, 14)) {
                setFocused(b);
                b.moveCursorToEnd(false);
                return true;
            }
        }
        for (Hit h : buttons) {
            if (!h.in(mx, my)) continue;
            if (h.id.equals("search")) {
                if (search.isVisible()) {
                    setFocused(search);
                    search.moveCursorToEnd(false);
                }
                return true;
            }
            setFocused(null);
            click(h.id);
            return true;
        }
        setFocused(null);
        if (button != 0 && button != 1) return false;

        boolean listVisible = split() || settingsOf == null;
        if (listVisible && my >= cy && my < cy + ch && mx >= cx - 2 && mx < cx + listW + 2) {
            for (ItemHit it : itemHits) {
                if (!it.box.in(mx, my)) continue;
                Module m = it.m;
                if (it.sw != null && it.sw.in(mx, my)) {
                    m.toggle();
                    dirty = true;
                } else if (split()) {
                    openSettingsInternal(m);
                } else if (it.more != null && (it.more.in(mx, my) || button == 1 || !m.toggleable())) {
                    openSettingsInternal(m);
                } else if (m.toggleable()) {
                    m.toggle();
                    dirty = true;
                } else if (m.hasVisibleSettings()) {
                    openSettingsInternal(m);
                }
                return true;
            }
            return false;
        }
        if (settingsOf == null) return false;
        for (RowHit r : rowHits) {
            if (!r.box.in(mx, my)) continue;
            if (r.s == null) {
                minecraft.setScreen(new HudEditorScreen(this));
                return true;
            }
            if (r.s instanceof Setting.Bool b) {
                b.toggle();
                changed(b);
            } else if (r.s instanceof Setting.Num n) {
                if (mx >= r.ctrlX - 6 && mx <= r.ctrlX + r.ctrlW + 6) {
                    dragging = n;
                    dragX = r.ctrlX;
                    dragW = r.ctrlW;
                    n.setFraction((mx - dragX) / (double) dragW);
                    changed(n);
                }
            } else if (r.s == Modules.APPEARANCE.accent) {
                Setting.Choice c = (Setting.Choice) r.s;
                int sw = 11, gap = 4;
                for (int i = 0; i < c.options.length; i++) {
                    int sx = r.ctrlX + i * (sw + gap);
                    if (mx >= sx - 2 && mx <= sx + sw + 2) {
                        c.set(i);
                        changed(c);
                        break;
                    }
                }
            } else if (r.s instanceof Setting.Bind b) {
                if (mx >= r.ctrlX) {
                    capturing = capturing == b ? null : b;
                    setFocused(null);
                }
            } else if (r.s instanceof Setting.Choice c) {
                if (mx >= r.ctrlX) {
                    c.next(mx < r.ctrlX + r.ctrlW / 2.0 || button == 1 ? -1 : 1);
                    changed(c);
                }
            }
            return true;
        }
        return false;
    }

    private void click(String id) {
        switch (id) {
            case "hud" -> minecraft.setScreen(new HudEditorScreen(this));
            case "efekt" -> EfektMenu.open(this);
            case "back" -> closeSettings();
            case "reset" -> {
                if (settingsOf != null) {
                    settingsOf.resetToDefaults();
                    buildTextBoxes();
                    dirty = true;
                }
            }
            case "toggle" -> {
                if (settingsOf != null && settingsOf.toggleable()) {
                    settingsOf.toggle();
                    dirty = true;
                }
            }
            default -> {
                if (id.startsWith("cat")) {
                    try {
                        category = Integer.parseInt(id.substring(3));
                    } catch (NumberFormatException e) {
                        category = 0;
                    }
                    lastCategory = category;
                    scroll = 0;
                    if (settingsOf != null && !split()) closeSettings();
                }
            }
        }
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging != null && dragW > 0) {
            dragging.setFraction((mx - dragX) / (double) dragW);
            changed(dragging);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragging = null;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        boolean overSettings = settingsOf != null && (!split() || mx >= setX - 4);
        if (overSettings) setScroll = Math.max(0, Math.min(setMaxScroll, setScroll - sy * 20));
        else scroll = Math.max(0, Math.min(maxScroll, scroll - sy * 20));
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (capturing != null) {
            Setting.Bind b = capturing;
            capturing = null;
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                if (b.allowUnbind) b.unbind(); // Esc: tuşu kaldır (menü tuşu kaldırılamaz)
            } else {
                b.bindKey(com.mojang.blaze3d.platform.InputConstants.getKey(key, scan));
            }
            dirty = true;
            return true;
        }
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            if (getFocused() instanceof EditBox) {
                setFocused(null);
                return true;
            }
            if (settingsOf != null) {
                closeSettings();
                return true;
            }
            onClose();
            return true;
        }
        if (getFocused() instanceof EditBox eb && eb.isFocused()) return super.keyPressed(key, scan, mods);
        if (JestronClient.menuKey != null && JestronClient.menuKey.matches(key, scan)) {
            onClose();
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public void removed() {
        if (dirty) {
            Config.save();
            dirty = false;
        }
    }
}
