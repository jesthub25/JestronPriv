package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.JestronPrivClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Kaydirilabilir secim izgarasi (kanat ve aura ekranlari). Arka plan bulaniklasmaz: secimin
 * etkisi oyunda canli gorunur. Ayarlar ekran kapanirken tek seferde kaydedilir.
 */
public abstract class GridPickerScreen extends Screen implements JpThemed {

    protected record Entry(String id, String name, String desc) {
    }

    protected final Screen parent;
    protected final JestronPrivClient mod;
    protected final List<Entry> entries = new ArrayList<>();
    protected int cols, cellW, cellH, gridX, gridY, viewBottom, scroll, maxScroll, side;
    private boolean dirty;

    protected GridPickerScreen(Component title, Screen parent) {
        super(title);
        this.parent = parent;
        this.mod = JestronPrivClient.get();
    }

    protected abstract void fill(List<Entry> out);

    protected abstract String selected();

    protected abstract void select(String id);

    protected abstract int preferredCellW();

    protected abstract int preferredCellH();

    protected abstract void drawCell(GuiGraphics g, Entry e, int x, int y, int w, int h, boolean hover, boolean sel);

    /** Alt satir butonlari; y = buton satiri. Kapat/Geri butonu otomatik eklenir. */
    protected void addButtons(int y, int left, int width) {
    }

    /** Sagda onizleme paneli genisligi (0 = yok). Izgara kalan alana yerlesir. */
    protected int sidePanel(int screenWidth) {
        return 0;
    }

    /** Alt buton satiri sayisi (1 veya 2); "Geri" her zaman en alt satirin sagindadir. */
    protected int buttonRows(int width) {
        return 1;
    }

    protected String subtitle() {
        return null;
    }

    protected void markDirty() {
        dirty = true;
        mod.applyConfig();
    }

    @Override
    protected void init() {
        entries.clear();
        fill(entries);
        side = Math.max(0, sidePanel(width));
        int gridW = width - side;
        cols = Math.max(2, Math.min(10, (gridW - 24) / preferredCellW()));
        cellW = preferredCellW();
        cellH = preferredCellH();
        gridX = (gridW - cols * cellW) / 2;
        gridY = 32;
        int total = Math.min(width - 20, 460);
        int left = (width - total) / 2;
        int rowsB = Math.max(1, Math.min(2, buttonRows(total)));
        int bottomY = height - 26;
        int by = bottomY - (rowsB - 1) * 24;
        viewBottom = by - 6;
        int rows = (entries.size() + cols - 1) / cols;
        maxScroll = Math.max(0, rows * cellH - (viewBottom - gridY));
        scroll = Math.max(0, Math.min(scroll, maxScroll));
        addButtons(by, left, total);
        addRenderableWidget(Button.builder(Component.literal("Geri"), b -> onClose()).bounds(left + total - 70, bottomY, 70, 20).build());
    }

    private int hovered(double mx, double my) {
        if (my < gridY || my >= viewBottom || mx >= width - side) return -1;
        for (int i = 0; i < entries.size(); i++) {
            int x = gridX + (i % cols) * cellW, y = gridY + (i / cols) * cellH - scroll;
            if (mx >= x && mx < x + cellW - 3 && my >= y && my < y + cellH - 3) return i;
        }
        return -1;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        Theme.overlay(g, width, height);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(font, Theme.title(title), width / 2, 8, 0xFFFFFF);
        String sub = subtitle();
        if (sub != null) g.drawCenteredString(font, sub, width / 2, 19, 0xA0A0A0);
        String sel = selected();
        int hov = hovered(mouseX, mouseY);
        g.enableScissor(0, gridY, width - side, viewBottom);
        for (int i = 0; i < entries.size(); i++) {
            Entry e = entries.get(i);
            int x = gridX + (i % cols) * cellW, y = gridY + (i / cols) * cellH - scroll;
            if (y + cellH < gridY || y > viewBottom) continue;
            boolean s = e.id().equals(sel);
            Theme.cell(g, x, y, cellW - 3, cellH - 3, s, i == hov, false);
            drawCell(g, e, x, y, cellW - 3, cellH - 3, i == hov, s);
        }
        g.disableScissor();
        if (maxScroll > 0) {
            int trackH = viewBottom - gridY;
            int barH = Math.max(12, trackH * trackH / (trackH + maxScroll));
            int barY = gridY + (int) ((trackH - barH) * (scroll / (double) maxScroll));
            int sx = gridX + cols * cellW + 1;
            Theme.scrollbar(g, sx, gridY, viewBottom, barY, barH);
        }
        if (hov >= 0) {
            Entry e = entries.get(hov);
            List<Component> tip = new ArrayList<>();
            tip.add(Component.literal("§f§l" + e.name()));
            if (e.desc() != null && !e.desc().isEmpty()) tip.add(Component.literal("§7" + e.desc()));
            tip.add(Component.literal(e.id().equals(sel) ? "§a§l✔ SEÇİLİ" : "§a▶ Tıkla: §fSeç"));
            g.renderComponentTooltip(font, tip, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (maxScroll > 0 && mouseX < width - side) {
            scroll = (int) Math.max(0, Math.min(maxScroll, scroll - scrollY * cellH));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int i = hovered(mouseX, mouseY);
        if (i >= 0 && button == 0) {
            select(entries.get(i).id());
            markDirty();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /** "1.25x" bicimi. */
    protected static String mul(double v) {
        double r = Math.round(v * 100) / 100.0;
        return (r == Math.rint(r) ? String.valueOf((long) r) : String.valueOf(r)) + "x";
    }

    /** Adima yuvarlanan carpan kaydiricisi (surukledikce ayar arkada canli degisir). */
    protected final class Slider extends net.minecraft.client.gui.components.AbstractSliderButton {
        private final String label;
        private final double min, max, step;
        private final java.util.function.DoubleConsumer set;

        public Slider(String label, double min, double max, double step, java.util.function.DoubleSupplier get,
                      java.util.function.DoubleConsumer set) {
            super(0, 0, 100, 20, Component.empty(), Math.max(0, Math.min(1, (get.getAsDouble() - min) / (max - min))));
            this.label = label;
            this.min = min;
            this.max = max;
            this.step = step;
            this.set = set;
            updateMessage();
        }

        private double real() {
            double v = min + value * (max - min);
            v = Math.round(v / step) * step;
            v = Math.round(v * 100) / 100.0;
            return Math.max(min, Math.min(max, v));
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(label + ": " + mul(real())));
        }

        @Override
        protected void applyValue() {
            set.accept(real());
            markDirty();
        }

        /** Konum + genislik (yukseklik 20). */
        public Slider at(int x, int y, int w) {
            setX(x);
            setY(y);
            setWidth(w);
            return this;
        }
    }

    @Override
    public void removed() {
        if (dirty) {
            mod.config().save();
            dirty = false;
        }
        super.removed();
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
