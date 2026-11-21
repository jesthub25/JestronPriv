package com.jestron.jestronpriv.jc.gui;

import com.jestron.jestronpriv.jc.JestronClient;
import com.jestron.jestronpriv.jc.integration.EfektMenu;
import com.jestron.jestronpriv.jc.modules.SessionModule;
import com.jestron.jestronpriv.jc.util.TextUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Jestron ESC menüsü. Vanilla duraklatma menüsünün yerine açılır (MinecraftMixin).
 * Ayarlar / İlerlemeler / İstatistikler / LAN / Çıkış gibi işlemler vanilla menünün kendi düğmeleriyle yapılır;
 * böylece kaydetme ve bağlantı kesme davranışı birebir vanilla ile aynıdır.
 */
public final class JestronPauseScreen extends Screen {
    public record Item(String id, String label, String shortLabel, String icon, Runnable action, boolean danger) {
    }

    private record Hit(Item item, int x, int y, int w, int h) {
    }

    private enum Mode { ROW, CENTER, TILE, ICON }

    private final List<Item> items = new ArrayList<>();
    private final List<Hit> hits = new ArrayList<>();
    private PauseStyle st = PauseStyle.current();
    private long openedAt;

    public JestronPauseScreen() {
        super(Component.translatable("menu.game"));
    }

    public List<Item> items() {
        return items;
    }

    // ------------------------------------------------------------------ öğeler

    @Override
    protected void init() {
        st = PauseStyle.current();
        if (openedAt == 0) openedAt = System.currentTimeMillis();
        items.clear();
        items.add(new Item("resume", "Oyuna dön", "Devam", "▶", this::onClose, false));
        items.add(new Item("jestron", "Modül Menüsü", "Modüller", "★", () -> minecraft.setScreen(new JestronMenuScreen(this)), false));
        items.add(new Item("hud", "HUD Düzenle", "HUD", "✎", () -> minecraft.setScreen(new HudEditorScreen(this)), false));
        if (EfektMenu.isLoaded()) items.add(new Item("efekt", "Efekt Menüsü", "Efekt", "✦", () -> EfektMenu.open(this), false));
        Map<String, Button> v = vanillaButtons();
        vanilla(v, "menu.options", "options", "Ayarlar", "Ayarlar", "⚙");
        vanilla(v, "gui.advancements", "advancements", "İlerlemeler", "İlerleme", "⚑");
        vanilla(v, "gui.stats", "stats", "İstatistikler", "İstatistik", "▤");
        vanilla(v, "menu.shareToLan", "lan", "LAN'a aç", "LAN", "⇄");
        Button d = v.containsKey("menu.returnToMenu") ? v.get("menu.returnToMenu") : v.get("menu.disconnect");
        boolean local = minecraft.isLocalServer();
        if (d != null) {
            items.add(new Item("disconnect", local ? "Kaydet ve çık" : "Bağlantıyı kes", "Çık", "✖", d::onPress, true));
        } else {
            // beklenmedik durum: vanilla menüyü aç, çıkış oradan yapılır
            items.add(new Item("disconnect", "Çıkış (klasik menü)", "Çık", "✖", () -> {
                bypass = true;
                minecraft.setScreen(new PauseScreen(true));
            }, true));
        }
    }

    /** true iken bir sonraki vanilla duraklatma menüsü değiştirilmez. */
    public static boolean bypass;

    private void vanilla(Map<String, Button> map, String key, String id, String label, String shortLabel, String icon) {
        Button b = map.get(key);
        if (b != null && b.active && b.visible) items.add(new Item(id, label, shortLabel, icon, b::onPress, false));
    }

    /** Görünmeyen bir vanilla duraklatma menüsü kurup düğmelerini çeviri anahtarına göre toplar. */
    private Map<String, Button> vanillaButtons() {
        Map<String, Button> map = new HashMap<>();
        try {
            PauseScreen ps = new PauseScreen(true);
            ps.init(minecraft, width, height);
            for (GuiEventListener l : ps.children()) {
                if (l instanceof Button b && b.getMessage().getContents() instanceof TranslatableContents tc) {
                    map.putIfAbsent(tc.getKey(), b);
                }
            }
        } catch (Throwable t) {
            JestronClient.LOG.warn("[Jestron] Vanilla duraklatma menüsü okunamadı", t);
        }
        return map;
    }

    // ------------------------------------------------------------------ bilgiler

    private String serverName() {
        if (minecraft.isLocalServer()) return "Tek oyuncu";
        if (minecraft.getCurrentServer() != null) return minecraft.getCurrentServer().name;
        return "Sunucu";
    }

    private String ping() {
        if (minecraft.isLocalServer() || minecraft.getConnection() == null || minecraft.player == null) return "—";
        PlayerInfo info = minecraft.getConnection().getPlayerInfo(minecraft.player.getUUID());
        return info == null ? "—" : info.getLatency() + " ms";
    }

    private String session() {
        return SessionModule.joinedAt == 0 ? "—" : TextUtil.duration(System.currentTimeMillis() - SessionModule.joinedAt);
    }

    private String coords() {
        if (minecraft.player == null) return "—";
        BlockPos p = minecraft.player.blockPosition();
        return p.getX() + " " + p.getY() + " " + p.getZ();
    }

    private String[][] info() {
        return new String[][]{
                {"Sunucu", serverName()}, {"Ping", ping()}, {"FPS", String.valueOf(minecraft.getFps())},
                {"Oturum", session()}, {"Konum", coords()}
        };
    }

    private String infoLine() {
        String p = ping();
        return serverName() + ("—".equals(p) ? "" : " · " + p) + " · " + minecraft.getFps() + " FPS";
    }

    // ------------------------------------------------------------------ çizim yardımcıları

    private float openProgress() {
        float t = Math.min(1f, (System.currentTimeMillis() - openedAt) / 140f);
        return 1f - (1f - t) * (1f - t);
    }

    private void text(GuiGraphics g, String s, int x, int y, int c) {
        g.drawString(font, s, x, y, c, st.bevel());
    }

    private void bold(GuiGraphics g, String s, int x, int y, int c) {
        g.drawString(font, Component.literal(s).withStyle(v -> v.withBold(true)), x, y, c, st.bevel());
    }

    private int boldW(String s) {
        return font.width(Component.literal(s).withStyle(v -> v.withBold(true)));
    }

    private void big(GuiGraphics g, String s, int cx, int y, int c, float scale) {
        g.pose().pushPose();
        try {
            g.pose().translate(cx, y, 0);
            g.pose().scale(scale, scale, 1f);
            Component t = Component.literal(s).withStyle(v -> v.withBold(true));
            g.drawString(font, t, -font.width(t) / 2, 0, c, st.bevel());
        } finally {
            g.pose().popPose();
        }
    }

    private void box(GuiGraphics g, int x1, int y1, int x2, int y2, int fill, int border, int radius, boolean raised) {
        if (x2 <= x1 || y2 <= y1) return;
        if (st.bevel()) {
            g.fill(x1, y1, x2, y2, fill);
            boolean button = fill == st.btn() || fill == st.btnHover() || fill == st.accent();
            int light = button ? (raised ? 0xFFAAAAAA : 0xFF2E2E2E) : (raised ? 0xFFFFFFFF : 0xFF373737);
            int dark = button ? (raised ? 0xFF2E2E2E : 0xFFAAAAAA) : (raised ? 0xFF555555 : 0xFFFFFFFF);
            g.fill(x1, y1, x2 - 1, y1 + 1, light);
            g.fill(x1, y1, x1 + 1, y2 - 1, light);
            g.fill(x1 + 1, y2 - 1, x2, y2, dark);
            g.fill(x2 - 1, y1 + 1, x2, y2, dark);
            return;
        }
        if (st.glow() && (border >>> 24) != 0) {
            // neon: dışa doğru sönen iki halka
            g.fill(x1 - 2, y1 - 2, x2 + 2, y2 + 2, Draw.withAlpha(border, 0.10f));
            g.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, Draw.withAlpha(border, 0.22f));
        }
        if ((fill >>> 24) == 0 && (border >>> 24) == 0) return;
        if ((border >>> 24) == 0) {
            Draw.roundRect(g, x1, y1, x2, y2, radius, fill);
        } else if ((fill >>> 24) == 0xFF) {
            Draw.card(g, x1, y1, x2, y2, radius, fill, border);
        } else {
            int r = Math.min(radius, Math.min(x2 - x1, y2 - y1) / 2);
            if ((fill >>> 24) != 0) Draw.roundRect(g, x1 + 1, y1 + 1, x2 - 1, y2 - 1, Math.max(0, r - 1), fill);
            int c = r >= 3 ? r - 1 : r;
            g.fill(x1 + c, y1, x2 - c, y1 + 1, border);
            g.fill(x1 + c, y2 - 1, x2 - c, y2, border);
            g.fill(x1, y1 + c, x1 + 1, y2 - c, border);
            g.fill(x2 - 1, y1 + c, x2, y2 - c, border);
        }
    }

    /** Tek düğme. İlk öğe (Oyuna dön) vurgu rengiyle çizilir. */
    private void button(GuiGraphics g, Item it, int x, int y, int w, int h, Mode mode, int mx, int my) {
        boolean hover = Draw.inside(mx, my, x, y, w, h);
        boolean primary = it.id().equals("resume");
        int r = Math.min(st.radius(), h / 2);
        int fill = primary ? (hover ? Draw.lerpColor(st.accent(), 0xFF000000, 0.12f) : st.accent()) : (hover ? st.btnHover() : st.btn());
        int border = st.glow() ? (primary || hover ? st.accent() : Draw.withAlpha(st.accent(), 0.45f)) : 0;
        if (!primary && it.danger() && hover && !st.bevel()) fill = Draw.withAlpha(0xFFEF4444, st.dark() ? 0.28f : 0.14f);
        if (!primary && (fill >>> 24) == 0 && !st.dark() && mode != Mode.ICON && st.layout() != PauseStyle.Layout.DOCK && st.layout() != PauseStyle.Layout.TOPBAR) {
            fill = st.btn();
        }
        box(g, x, y, x + w, y + h, fill, border, r, true);
        int tc = primary ? st.onAccent() : it.danger() ? (st.bevel() ? 0xFFFF8080 : 0xFFEF4444) : st.btnText();
        if (st.glow() && primary) tc = 0xFF000000;
        int ic = primary ? tc : it.danger() ? tc : (st.dark() ? Draw.withAlpha(st.btnText(), 0.7f) : st.muted());
        if (st.bevel() && !primary) ic = 0xFFE0E0E0;
        switch (mode) {
            case ROW -> {
                int ty = y + (h - 8) / 2;
                g.drawString(font, it.icon(), x + 7, ty, ic, st.bevel());
                g.drawString(font, Draw.ellipsize(font, it.label(), w - 26), x + 20, ty, tc, st.bevel());
            }
            case CENTER -> {
                String l = Draw.ellipsize(font, it.label(), w - 22);
                int tw = font.width(it.icon()) + 5 + font.width(l);
                int tx = x + (w - tw) / 2, ty = y + (h - 8) / 2;
                g.drawString(font, it.icon(), tx, ty, ic, st.bevel());
                g.drawString(font, l, tx + font.width(it.icon()) + 5, ty, tc, st.bevel());
            }
            case TILE -> {
                g.pose().pushPose();
                try {
                    g.pose().translate(x + w / 2f, y + Math.max(4, h / 2f - 13), 0);
                    g.pose().scale(1.6f, 1.6f, 1f);
                    g.drawString(font, it.icon(), -font.width(it.icon()) / 2, 0, primary ? tc : (it.danger() ? tc : st.accent()), false);
                } finally {
                    g.pose().popPose();
                }
                String l = Draw.ellipsize(font, h < 40 ? it.shortLabel() : it.label(), w - 6);
                g.drawString(font, l, x + (w - font.width(l)) / 2, y + h - 13, tc, st.bevel());
            }
            case ICON -> {
                g.pose().pushPose();
                try {
                    g.pose().translate(x + w / 2f, y + 5, 0);
                    g.pose().scale(1.3f, 1.3f, 1f);
                    g.drawString(font, it.icon(), -font.width(it.icon()) / 2, 0, primary ? tc : (it.danger() ? tc : st.text()), false);
                } finally {
                    g.pose().popPose();
                }
                String l = Draw.ellipsize(font, it.shortLabel(), w - 2);
                g.drawString(font, l, x + (w - font.width(l)) / 2, y + h - 11, primary ? tc : it.danger() ? tc : st.muted(), false);
            }
        }
        hits.add(new Hit(it, x, y, w, h));
    }

    private void logo(GuiGraphics g, int x, int y, int c) {
        bold(g, "jestron", x, y, c);
        int dx = x + boldW("jestron") + 1;
        g.fill(dx, y + 5, dx + 2, y + 7, st.accent());
    }

    /** İlk öğe tam genişlik, son öğe (çıkış) tam genişlik, aradakiler cols sütun. @return yükseklik */
    private int stack(GuiGraphics g, int x, int y, int w, int cols, int h, int gap, Mode mode, int mx, int my, boolean draw) {
        int n = items.size();
        int cy = y;
        if (draw) button(g, items.get(0), x, cy, w, h, mode, mx, my);
        cy += h + gap;
        int mid = n - 2;
        int cw = (w - (cols - 1) * gap) / cols;
        for (int i = 0; i < mid; i++) {
            int c = i % cols;
            if (c == 0 && i > 0) cy += h + gap;
            boolean lastAlone = i == mid - 1 && c == 0;
            if (draw) button(g, items.get(i + 1), x + c * (cw + gap), cy, lastAlone ? w : cw, h, mode, mx, my);
        }
        if (mid > 0) cy += h + gap;
        cy += 4;
        if (draw) button(g, items.get(n - 1), x, cy, w, h, mode, mx, my);
        cy += h;
        return cy - y;
    }

    // ------------------------------------------------------------------ çizim

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, Draw.withAlpha(st.overlay(), openProgress()));
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        renderBackground(g, mx, my, partialTick);
        hits.clear();
        int slide = Math.round((1f - openProgress()) * 6f);
        g.pose().pushPose();
        g.pose().translate(0, slide, 0);
        try {
            int m = my - slide;
            switch (st.layout()) {
                case CARD -> drawCard(g, mx, m);
                case LEFT -> drawLeft(g, mx, m);
                case RIGHT -> drawRight(g, mx, m);
                case DOCK -> drawDock(g, mx, m);
                case GRID -> drawGrid(g, mx, m);
                case COMPACT -> drawCompact(g, mx, m);
                case FULL -> drawFull(g, mx, m);
                case INFO -> drawInfo(g, mx, m);
                case TOPBAR -> drawTopBar(g, mx, m);
            }
        } finally {
            g.pose().popPose();
        }
    }

    private void drawCard(GuiGraphics g, int mx, int my) {
        int w = Math.min(width - 20, 224), bh = 18, gap = 3;
        int body = stack(g, 0, 0, w - 20, 2, bh, gap, Mode.CENTER, mx, my, false);
        int h = 40 + body + 12;
        int x = (width - w) / 2, y = Math.max(6, (height - h) / 2);
        box(g, x, y, x + w, y + h, st.panel(), st.border(), st.radius(), true);
        String title = "Oyun duraklatıldı";
        bold(g, title, x + (w - boldW(title)) / 2, y + 11, st.text());
        String info = Draw.ellipsize(font, infoLine(), w - 20);
        text(g, info, x + (w - font.width(info)) / 2, y + 24, st.muted());
        stack(g, x + 10, y + 40, w - 20, 2, bh, gap, Mode.CENTER, mx, my, true);
    }

    private void drawLeft(GuiGraphics g, int mx, int my) {
        int w = Math.min(150, width / 3);
        g.fill(0, 0, w, height, st.panel());
        g.fill(w, 0, w + 1, height, st.border());
        logo(g, 12, 12, st.text());
        text(g, "Oyun duraklatıldı", 12, 26, st.muted());
        int y = 46, bh = 17;
        int n = items.size();
        for (int i = 0; i < n - 1; i++) {
            button(g, items.get(i), 6, y, w - 12, bh, Mode.ROW, mx, my);
            y += bh + 2;
        }
        int by = height - 12 - bh;
        String[][] inf = info();
        int iy = by - 10 - 3 * 10;
        if (iy > y + 4) {
            for (int i = 0; i < 3; i++) {
                text(g, inf[i][0], 12, iy + i * 10, st.muted());
                String val = Draw.ellipsize(font, inf[i][1], w - 70);
                text(g, val, w - 12 - font.width(val), iy + i * 10, st.text());
            }
        }
        button(g, items.get(n - 1), 6, by, w - 12, bh, Mode.ROW, mx, my);
    }

    private void drawRight(GuiGraphics g, int mx, int my) {
        int w = Math.min(160, width / 2 - 10), x = width - w - 10, y = 10, h = height - 20;
        box(g, x, y, x + w, y + h, st.panel(), st.border(), st.radius(), true);
        // oyuncu kartı
        int py = y + 10;
        if (minecraft.player != null) {
            try {
                PlayerFaceRenderer.draw(g, minecraft.player.getSkin(), x + 10, py, 24);
            } catch (Throwable ignored) {
            }
            bold(g, Draw.ellipsize(font, minecraft.player.getName().getString(), w - 50), x + 40, py + 3, st.text());
            float hp = minecraft.player.getHealth(), max = Math.max(1f, minecraft.player.getMaxHealth());
            text(g, String.format(java.util.Locale.ROOT, "❤ %.0f/%.0f", hp, max), x + 40, py + 14, 0xFFEF4444);
        }
        py += 32;
        int bx = x + 10, bw = w - 20;
        g.fill(bx, py, bx + bw, py + 1, st.border());
        py += 6;
        String[][] inf = info();
        for (int i = 0; i < inf.length; i++) {
            text(g, inf[i][0], bx, py, st.muted());
            String v = Draw.ellipsize(font, inf[i][1], bw - 44);
            text(g, v, bx + bw - font.width(v), py, st.text());
            py += 10;
        }
        py += 4;
        int n = items.size(), bh = 16;
        int avail = y + h - 10 - py;
        int need = n * (bh + 2);
        if (need > avail) bh = Math.max(12, avail / n - 2);
        for (int i = 0; i < n; i++) {
            int yy = i == n - 1 ? y + h - 10 - bh : py;
            button(g, items.get(i), bx, yy, bw, bh, Mode.ROW, mx, my);
            py += bh + 2;
        }
    }

    private void drawDock(GuiGraphics g, int mx, int my) {
        int n = items.size();
        int tw = Math.min(48, (width - 40) / n), th = 34, gap = 2;
        int total = n * tw + (n - 1) * gap;
        int x = (width - total) / 2, y = height - th - 16;
        box(g, x - 6, y - 6, x + total + 6, y + th + 6, st.panel(), st.border(), st.radius() + 4, true);
        for (int i = 0; i < n; i++) button(g, items.get(i), x + i * (tw + gap), y, tw, th, Mode.ICON, mx, my);
        // üstte başlık hapı
        String title = "Oyun duraklatıldı";
        String info = infoLine();
        int pw = Math.max(boldW(title), font.width(info)) + 24, ph = 30;
        int px = (width - pw) / 2, py = Math.max(10, height / 4 - ph);
        box(g, px, py, px + pw, py + ph, st.panel(), st.border(), Math.min(st.radius() + 4, ph / 2), true);
        bold(g, title, px + (pw - boldW(title)) / 2, py + 6, st.text());
        text(g, info, px + (pw - font.width(info)) / 2, py + 17, st.muted());
    }

    private void drawGrid(GuiGraphics g, int mx, int my) {
        int n = items.size(), cols = 3, gap = 5;
        int tw = Math.min(78, (width - 40 - (cols - 1) * gap) / cols), th = 46;
        int rows = (n + cols - 1) / cols;
        int gw = cols * tw + (cols - 1) * gap, gh = rows * th + (rows - 1) * gap;
        int top = Math.max(34, (height - gh) / 2 + 8);
        String title = "Oyun duraklatıldı";
        bold(g, title, (width - boldW(title)) / 2, top - 26, st.text());
        String info = infoLine();
        text(g, info, (width - font.width(info)) / 2, top - 14, st.muted());
        int x0 = (width - gw) / 2;
        for (int i = 0; i < n; i++) {
            int c = i % cols, r = i / cols;
            int rowCount = Math.min(cols, n - r * cols);
            int rx = x0 + (gw - (rowCount * tw + (rowCount - 1) * gap)) / 2; // son satır ortalanır
            button(g, items.get(i), rx + c * (tw + gap), top + r * (th + gap), tw, th, Mode.TILE, mx, my);
        }
    }

    private void drawCompact(GuiGraphics g, int mx, int my) {
        int x = 10, y = 10, w = 118, bh = 14;
        logo(g, x, y, 0xFFFFFFFF);
        g.drawString(font, "duraklatıldı", x + boldW("jestron") + 6, y, 0xFFA1A1AA, true);
        y += 14;
        for (Item it : items) {
            if (it.danger()) y += 4;
            button(g, it, x, y, w, bh, Mode.ROW, mx, my);
            y += bh + 1;
        }
        String info = infoLine();
        g.drawString(font, info, 10, height - 18, 0xFFD4D4D8, true);
    }

    private void drawFull(GuiGraphics g, int mx, int my) {
        int top = Math.max(16, height / 2 - 82);
        big(g, "Duraklatıldı", width / 2, top, st.text(), 2f);
        String info = infoLine();
        text(g, info, (width - font.width(info)) / 2, top + 22, st.muted());
        int n = items.size();
        int cols = 3, gap = 5, bw = Math.min(110, (width - 40 - (cols - 1) * gap) / cols), bh = 20;
        int gw = cols * bw + (cols - 1) * gap;
        int x0 = (width - gw) / 2, y = top + 40;
        int mid = n - 1;
        for (int i = 0; i < mid; i++) {
            int c = i % cols, r = i / cols;
            int rowCount = Math.min(cols, mid - r * cols);
            int rx = x0 + (gw - (rowCount * bw + (rowCount - 1) * gap)) / 2;
            button(g, items.get(i), rx + c * (bw + gap), y + r * (bh + gap), bw, bh, Mode.CENTER, mx, my);
        }
        int rows = (mid + cols - 1) / cols;
        int dy = y + rows * (bh + gap) + 8;
        int dw = Math.min(140, gw);
        button(g, items.get(n - 1), (width - dw) / 2, dy, dw, bh, Mode.CENTER, mx, my);
        logo(g, 12, height - 18, Draw.withAlpha(st.text(), 0.6f));
    }

    private void drawInfo(GuiGraphics g, int mx, int my) {
        int iw = 86, pw = Math.min(170, width - iw - 40), gap = 8;
        int bh = 17;
        int body = stack(g, 0, 0, pw - 20, 2, bh, 3, Mode.CENTER, mx, my, false);
        int ph = 30 + body + 12;
        int total = iw + gap + pw;
        int x = (width - total) / 2, y = Math.max(6, (height - ph) / 2);
        // bilgi kartları
        String[][] inf = info();
        int ch = Math.min(28, (ph - (inf.length - 1) * 4) / inf.length);
        for (int i = 0; i < inf.length; i++) {
            int cy = y + i * (ch + 4);
            box(g, x, cy, x + iw, cy + ch, st.panel(), st.border(), st.radius(), true);
            g.fill(x + 1, cy + 4, x + 3, cy + ch - 4, st.accent());
            text(g, inf[i][0], x + 8, cy + (ch >= 26 ? 5 : 2), st.muted());
            bold(g, Draw.ellipsize(font, inf[i][1], iw - 14), x + 8, cy + ch - 12, st.text());
        }
        int px = x + iw + gap;
        box(g, px, y, px + pw, y + ph, st.panel(), st.border(), st.radius(), true);
        logo(g, px + 10, y + 10, st.text());
        String t = "duraklatıldı";
        text(g, t, px + pw - 10 - font.width(t), y + 10, st.muted());
        stack(g, px + 10, y + 30, pw - 20, 2, bh, 3, Mode.CENTER, mx, my, true);
    }

    private void drawTopBar(GuiGraphics g, int mx, int my) {
        int bh = 36;
        g.fill(0, 0, width, bh, st.panel());
        g.fill(0, bh, width, bh + 1, st.border());
        int n = items.size();
        int logoW = boldW("jestron") + 16;
        int iw = Math.min(54, (width - 12) / n - 2);
        int total = n * (iw + 2) - 2;
        boolean showLogo = total + 2 * logoW + 10 < width;
        if (showLogo) logo(g, 10, 14, st.text());
        int x = (width - total) / 2;
        for (int i = 0; i < n; i++) {
            button(g, items.get(i), x, 3, iw, bh - 6, Mode.ICON, mx, my);
            x += iw + 2;
        }
        // ortada bilgi kartı
        String title = "Oyun duraklatıldı";
        String[][] inf = info();
        int cw = 150, chh = 20 + inf.length * 11 + 6;
        int cx = (width - cw) / 2, cy = Math.max(bh + 16, (height - chh) / 2);
        box(g, cx, cy, cx + cw, cy + chh, st.panel(), st.border(), st.radius(), true);
        bold(g, title, cx + (cw - boldW(title)) / 2, cy + 7, st.text());
        int ly = cy + 22;
        for (String[] r : inf) {
            text(g, r[0], cx + 10, ly, st.muted());
            String v = Draw.ellipsize(font, r[1], cw - 64);
            text(g, v, cx + cw - 10 - font.width(v), ly, st.text());
            ly += 11;
        }
    }

    // ------------------------------------------------------------------ giriş

    @Override
    public boolean mouseClicked(double mx, double myRaw, int button) {
        double my = myRaw - Math.round((1f - openProgress()) * 6f);
        if (button == 0) {
            for (Hit h : hits) {
                if (Draw.inside(mx, my, h.x(), h.y(), h.w(), h.h())) {
                    playClick();
                    try {
                        h.item().action().run();
                    } catch (Throwable t) {
                        JestronClient.LOG.warn("[Jestron] ESC menüsü işlemi başarısız: {}", h.item().id(), t);
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, myRaw, button);
    }

    private void playClick() {
        try {
            minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
        } catch (Throwable ignored) {
        }
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}
