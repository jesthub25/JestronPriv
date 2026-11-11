package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.tag.EmojiAtlas;
import com.jestron.jestronpriv.tag.EmojiIndex;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Emoji secici: telefondaki gibi kategoriler (yuzler, insanlar, hayvanlar, yiyecek, seyahat, etkinlik, nesneler,
 * semboller, bayraklar), son kullanilanlar, Turkce / Ingilizce arama ve ten rengi (sag tik).
 */
public final class EmojiPickerScreen extends Screen implements JpThemed {

    private static final int CELL = 22, ICON = 18;

    private final Screen parent;
    private final Consumer<String> onPick;
    private final Runnable onRemoveLast;
    private final Supplier<String> current;
    private final int max;
    private EditBox search;
    private int tab = 1;   // 0 = son kullanilanlar, 1.. = gruplar
    private List<Integer> items = new ArrayList<>();
    private int gx, gy, cols, rows, scroll, maxScroll;
    private int toneFor = -1, toneX, toneY;
    private String flash = "";
    private long flashUntil;

    /**
     * @param current     su anki emoji dizisi (baslikta gosterilir)
     * @param max         en cok emoji sayisi
     */
    public EmojiPickerScreen(Screen parent, String title, Supplier<String> current, int max, Consumer<String> onPick, Runnable onRemoveLast) {
        super(Component.literal(title));
        this.parent = parent;
        this.current = current;
        this.max = max;
        this.onPick = onPick;
        this.onRemoveLast = onRemoveLast;
    }

    private ModConfig cfg() {
        return JestronPrivClient.get().config();
    }

    @Override
    protected void init() {
        int tabsW = (EmojiIndex.GROUPS.length + 1) * 24;
        int left = Math.max(8, (width - Math.max(tabsW, 300)) / 2);
        search = new EditBox(font, left, 40, Math.min(300, width - 16), 18, Component.literal("Ara"));
        search.setHint(Component.literal("§8Ara (Türkçe ya da İngilizce): kedi, kalp, ateş, bayrak..."));
        search.setMaxLength(40);
        search.setResponder(v -> refresh());
        addRenderableWidget(search);
        int by = height - 26;
        addRenderableWidget(Button.builder(Component.literal("⌫ Son emojiyi sil"), b -> {
            onRemoveLast.run();
            flash("Silindi");
        }).bounds(width / 2 - 154, by, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Bitti"), b -> onClose()).bounds(width / 2 + 54, by, 100, 20).build());
        gx = 10;
        gy = 64;
        cols = Math.max(4, (width - 20) / CELL);
        gx = (width - cols * CELL) / 2;
        rows = Math.max(1, (by - 6 - gy) / CELL);
        refresh();
    }

    private void refresh() {
        EmojiIndex idx = EmojiAtlas.index();
        String q = search == null ? "" : search.getValue();
        if (!q.isBlank()) {
            items = idx.list(-1, q, 2000);
        } else if (tab == 0) {
            items = new ArrayList<>();
            for (EmojiIndex.Token t : idx.tokenize(cfg().tagRecentEmoji)) if (t.emoji() >= 0 && !items.contains(t.emoji())) items.add(t.emoji());
        } else {
            items = idx.list(EmojiIndex.GROUPS[tab - 1], "", 4000);
        }
        int totalRows = (items.size() + cols - 1) / cols;
        maxScroll = Math.max(0, totalRows - rows);
        scroll = Math.max(0, Math.min(scroll, maxScroll));
        toneFor = -1;
    }

    private void flash(String s) {
        flash = s;
        flashUntil = System.currentTimeMillis() + 1500;
    }

    private void pick(int i) {
        EmojiIndex idx = EmojiAtlas.index();
        String e = idx.emoji(i);
        if (e.isEmpty()) return;
        if (idx.countEmoji(current.get()) >= max) {
            flash("§cEn çok " + max + " emoji");
            return;
        }
        onPick.accept(e);
        ModConfig c = cfg();
        String rec = e + c.tagRecentEmoji.replace(e, "");
        // en cok 40 son emoji
        List<EmojiIndex.Token> tk = idx.tokenize(rec);
        StringBuilder b = new StringBuilder();
        int n = 0;
        for (EmojiIndex.Token t : tk) {
            if (t.emoji() < 0) continue;
            if (n++ >= 40) break;
            b.append(t.text());
        }
        c.tagRecentEmoji = b.toString();
        flash("Eklendi: " + idx.label(i));
    }

    // ------------------------------------------------------------------ cizim

    private void emoji(GuiGraphics g, int i, int x, int y, int size) {
        g.blit(RenderType::guiTextured, EmojiAtlas.TEXTURE, x, y, (float) EmojiIndex.cellX(i), (float) EmojiIndex.cellY(i), size, size, EmojiIndex.CELL,
                EmojiIndex.CELL, EmojiIndex.ATLAS_SIZE, EmojiIndex.ATLAS_SIZE);
    }

    private int tabX(int k) {
        int total = (EmojiIndex.GROUPS.length + 1) * 24;
        return (width - total) / 2 + k * 24;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        EmojiIndex idx = EmojiAtlas.index();
        g.drawCenteredString(font, title, width / 2, 6, 0xFFFFFF);
        String cur = current.get();
        int count = idx.countEmoji(cur);
        // secili emojiler (gercek gorunum): "Secili (n/max):" + emojiler, ortali
        String label = "§7Seçili §8(" + count + "/" + max + ")§7: " + (count == 0 ? "§8yok" : "");
        int lw = font.width(label), total = lw + count * 11;
        int sx = width / 2 - total / 2;
        g.drawString(font, label, sx, 18, 0xFFFFFF);
        int k = 0;
        for (EmojiIndex.Token t : idx.tokenize(cur)) {
            if (t.emoji() >= 0) emoji(g, t.emoji(), sx + lw + k++ * 11, 17, 10);
        }
        // sekmeler
        for (int i = 0; i <= EmojiIndex.GROUPS.length; i++) {
            int x = tabX(i), y = 28;
            boolean on = i == tab && search.getValue().isBlank();
            g.fill(x, y - 1, x + 22, y + 11, on ? 0x90B070FF : 0x50000000);
            int ei = idx.find(i == 0 ? "🕘" : EmojiIndex.GROUP_ICONS[i - 1]);
            if (ei >= 0) emoji(g, ei, x + 6, y, 10);
        }
        // izgara
        g.fill(gx - 2, gy - 2, gx + cols * CELL + 2, gy + rows * CELL + 2, 0x60000000);
        int hover = -1;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int n = (r + scroll) * cols + c;
                if (n >= items.size()) break;
                int i = items.get(n);
                int x = gx + c * CELL, y = gy + r * CELL;
                boolean h = mouseX >= x && mouseX < x + CELL && mouseY >= y && mouseY < y + CELL && toneFor < 0;
                if (h) {
                    g.fill(x, y, x + CELL, y + CELL, 0x60FFFFFF);
                    hover = i;
                }
                emoji(g, i, x + (CELL - ICON) / 2, y + (CELL - ICON) / 2, ICON);
                if (!idx.skins(i).isEmpty()) g.fill(x + CELL - 4, y + CELL - 4, x + CELL - 2, y + CELL - 2, 0xC0FFD070);
            }
        }
        if (items.isEmpty()) g.drawCenteredString(font, tab == 0 && search.getValue().isBlank() ? "§7Henüz emoji kullanmadın" : "§7Sonuç yok",
                width / 2, gy + 20, 0xFFFFFF);
        if (maxScroll > 0) {
            int barH = Math.max(12, rows * CELL * rows / (rows + maxScroll));
            int barY = gy + (rows * CELL - barH) * scroll / maxScroll;
            g.fill(gx + cols * CELL + 3, barY, gx + cols * CELL + 5, barY + barH, 0xA0B070FF);
        }
        // ten rengi secimi
        if (toneFor >= 0) {
            List<Integer> sk = idx.skins(toneFor);
            int w = (sk.size() + 1) * CELL;
            int px = Math.max(2, Math.min(width - w - 2, toneX)), py = Math.max(2, toneY - CELL - 4);
            g.fill(px - 2, py - 2, px + w + 2, py + CELL + 2, 0xF0202028);
            g.renderOutline(px - 2, py - 2, w + 4, CELL + 4, 0xFFB070FF);
            emoji(g, toneFor, px + 2, py + 2, ICON);
            for (int j = 0; j < sk.size(); j++) {
                int x = px + (j + 1) * CELL;
                if (mouseX >= x && mouseX < x + CELL && mouseY >= py && mouseY < py + CELL) g.fill(x, py, x + CELL, py + CELL, 0x60FFFFFF);
                emoji(g, sk.get(j), x + 2, py + 2, ICON);
            }
        }
        if (hover >= 0) {
            List<Component> tip = new ArrayList<>();
            tip.add(Component.literal(idx.label(hover)));
            if (!idx.skins(hover).isEmpty()) tip.add(Component.literal("§8Sağ tık: ten rengi"));
            g.renderComponentTooltip(font, tip, mouseX, mouseY);
        }
        if (System.currentTimeMillis() < flashUntil) g.drawCenteredString(font, flash, width / 2, height - 38, 0xFFFFFF);
        g.drawString(font, "§8Emoji: Twemoji (CC-BY 4.0)", 4, height - 10, 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        EmojiIndex idx = EmojiAtlas.index();
        if (toneFor >= 0) {
            List<Integer> sk = idx.skins(toneFor);
            int w = (sk.size() + 1) * CELL;
            int px = Math.max(2, Math.min(width - w - 2, toneX)), py = Math.max(2, toneY - CELL - 4);
            if (my >= py && my < py + CELL && mx >= px && mx < px + w) {
                int j = (int) ((mx - px) / CELL);
                pick(j == 0 ? toneFor : sk.get(Math.min(sk.size() - 1, j - 1)));
            }
            toneFor = -1;
            return true;
        }
        // sekmeler
        if (my >= 27 && my < 40) {
            for (int i = 0; i <= EmojiIndex.GROUPS.length; i++) {
                int x = tabX(i);
                if (mx >= x && mx < x + 22) {
                    tab = i;
                    search.setValue("");
                    scroll = 0;
                    refresh();
                    return true;
                }
            }
        }
        if (mx >= gx && mx < gx + cols * CELL && my >= gy && my < gy + rows * CELL) {
            int c = (int) ((mx - gx) / CELL), r = (int) ((my - gy) / CELL);
            int n = (r + scroll) * cols + c;
            if (n >= 0 && n < items.size()) {
                int i = items.get(n);
                if (button == 1 && !idx.skins(i).isEmpty()) {
                    toneFor = i;
                    toneX = gx + c * CELL;
                    toneY = gy + r * CELL;
                } else if (button == 0) {
                    pick(i);
                }
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (maxScroll > 0) {
            scroll = (int) Math.max(0, Math.min(maxScroll, scroll - Math.signum(sy) * 2));
            return true;
        }
        return super.mouseScrolled(mx, my, sx, sy);
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
