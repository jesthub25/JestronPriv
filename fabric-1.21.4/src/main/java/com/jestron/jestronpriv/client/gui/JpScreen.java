package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

/**
 * Tum ayar ekranlarinin ortak altyapisi: iki sutunlu secenekler, kaydirma, alt butonlar.
 * Arka plan bulaniklastirilmaz; oyun ve el arkada canli gorunur (ayarlar aninda onizlenir).
 * Degisiklikler ekran kapanirken tek seferde diske yazilir.
 */
public abstract class JpScreen extends Screen implements JpThemed {

    protected static final int COL_W = 150;
    protected static final int GAP = 10;
    protected static final int ROW_H = 24;
    protected static final int TOP = 40;

    protected final Screen parent;
    protected final JestronPrivClient mod;
    private final List<Entry> entries = new ArrayList<>();
    private int scroll, maxScroll, areaTop, areaBottom, left;
    private boolean dirty;

    private record Entry(AbstractWidget widget, int baseY) {
    }

    protected JpScreen(Component title, Screen parent) {
        super(title);
        this.parent = parent;
        this.mod = JestronPrivClient.get();
    }

    protected ModConfig cfg() {
        return mod.config();
    }

    /** Alt sinif seceneklerini burada ekler (toggle/slider/cycle/action). */
    protected abstract void buildOptions();

    /** "Varsayilana sifirla" icin; null ise buton gosterilmez. */
    protected Runnable resetAction() {
        return null;
    }

    /** Bir ayar degistiginde (ornegin onizleme icin). */
    protected void onChanged() {
    }

    protected String subtitle() {
        return null;
    }

    /** Seceneklerin basladigi y (ust bilgi / onizleme icin artirilabilir). */
    protected int topOffset() {
        return TOP;
    }

    protected void markDirty() {
        dirty = true;
        mod.applyConfig();
        onChanged();
    }

    // ------------------------------------------------------------------ yerlesim

    @Override
    protected void init() {
        entries.clear();
        left = width / 2 - COL_W - GAP / 2;
        areaTop = topOffset();
        areaBottom = height - 34;
        buildOptions();
        int rows = (entries.size() + 1) / 2;
        int contentH = rows * ROW_H;
        maxScroll = Math.max(0, contentH - (areaBottom - areaTop));
        scroll = Math.max(0, Math.min(scroll, maxScroll));
        layout();

        Runnable reset = resetAction();
        int by = height - 27;
        if (reset != null) {
            addRenderableWidget(Button.builder(Component.literal("Varsayılana Sıfırla"), b -> {
                reset.run();
                markDirty();
                rebuildWidgets();
            }).bounds(width / 2 - COL_W - GAP / 2, by, COL_W, 20)
                    .tooltip(Tooltip.create(Component.literal("Bu sayfadaki tüm ayarları varsayılan değerlerine döndürür."))).build());
            addRenderableWidget(Button.builder(Component.literal("Geri"), b -> onClose())
                    .bounds(width / 2 + GAP / 2, by, COL_W, 20).build());
        } else {
            addRenderableWidget(Button.builder(Component.literal("Geri"), b -> onClose())
                    .bounds(width / 2 - 100, by, 200, 20).build());
        }
    }

    private void addEntry(AbstractWidget w, boolean full) {
        int idx = entries.size();
        if (full && idx % 2 == 1) {
            // Tam genislikli satir yeni satira baslasin: bos hucre atla
            entries.add(new Entry(null, 0));
            idx++;
        }
        int row = idx / 2, col = idx % 2;
        int x = full ? left : left + col * (COL_W + GAP);
        w.setX(x);
        w.setWidth(full ? COL_W * 2 + GAP : COL_W);
        entries.add(new Entry(w, areaTop + row * ROW_H));
        if (full) entries.add(new Entry(null, 0));
        addRenderableWidget(w);
    }

    private void layout() {
        for (Entry e : entries) {
            if (e.widget == null) continue;
            int y = e.baseY - scroll;
            e.widget.setY(y);
            e.widget.visible = y >= areaTop - 2 && y + 20 <= areaBottom + 2;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (maxScroll > 0 && mouseY >= areaTop && mouseY <= areaBottom) {
            scroll = (int) Math.max(0, Math.min(maxScroll, scroll - scrollY * ROW_H));
            layout();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    // ------------------------------------------------------------------ secenek turleri

    protected static Component onOff(boolean b) {
        return Component.literal(b ? "§aAçık" : "§cKapalı");
    }

    protected Button toggle(String label, String tip, BooleanSupplier get, Consumer<Boolean> set) {
        Button b = Button.builder(Component.empty(), btn -> {
            set.accept(!get.getAsBoolean());
            btn.setMessage(Component.literal(label + ": ").append(onOff(get.getAsBoolean())));
            markDirty();
        }).bounds(0, 0, COL_W, 20).build();
        b.setMessage(Component.literal(label + ": ").append(onOff(get.getAsBoolean())));
        if (tip != null) b.setTooltip(Tooltip.create(Component.literal(tip)));
        addEntry(b, false);
        return b;
    }

    protected Button cycle(String label, String tip, Supplier<String> value, Runnable next) {
        Button b = Button.builder(Component.empty(), btn -> {
            next.run();
            btn.setMessage(Component.literal(label + ": " + value.get()));
            markDirty();
        }).bounds(0, 0, COL_W, 20).build();
        b.setMessage(Component.literal(label + ": " + value.get()));
        if (tip != null) b.setTooltip(Tooltip.create(Component.literal(tip)));
        addEntry(b, false);
        return b;
    }

    protected Slider slider(String label, String tip, double min, double max, double step,
                            DoubleSupplier get, DoubleConsumer set, DoubleFunction<String> fmt) {
        Slider s = new Slider(label, min, max, step, get, set, fmt);
        if (tip != null) s.setTooltip(Tooltip.create(Component.literal(tip)));
        addEntry(s, false);
        return s;
    }

    protected net.minecraft.client.gui.components.EditBox editBox(String hint, String tip, boolean full, int maxLen,
                                                                  Supplier<String> get, Consumer<String> set) {
        net.minecraft.client.gui.components.EditBox e = new net.minecraft.client.gui.components.EditBox(font, 0, 0, COL_W, 20, Component.literal(hint));
        e.setMaxLength(maxLen);
        e.setValue(get.get() == null ? "" : get.get());
        e.setHint(Component.literal("§8" + hint));
        e.setResponder(v -> {
            set.accept(v);
            markDirty();
        });
        if (tip != null) e.setTooltip(Tooltip.create(Component.literal(tip)));
        addEntry(e, full);
        return e;
    }

    /** Ayar olmayan metin kutusu (yazarken ayarlari uygulamaz / kaydetmez). */
    protected net.minecraft.client.gui.components.EditBox textField(String hint, String tip, boolean full, int maxLen,
                                                                    Supplier<String> get, Consumer<String> set) {
        net.minecraft.client.gui.components.EditBox e = new net.minecraft.client.gui.components.EditBox(font, 0, 0, COL_W, 20, Component.literal(hint));
        e.setMaxLength(maxLen);
        e.setValue(get.get() == null ? "" : get.get());
        e.setHint(Component.literal("§8" + hint));
        e.setResponder(set);
        if (tip != null) e.setTooltip(Tooltip.create(Component.literal(tip)));
        addEntry(e, full);
        return e;
    }

    protected Button action(String label, String tip, boolean full, Runnable run) {
        Button b = Button.builder(Component.literal(label), btn -> run.run()).bounds(0, 0, COL_W, 20).build();
        if (tip != null) b.setTooltip(Tooltip.create(Component.literal(tip)));
        addEntry(b, full);
        return b;
    }

    /**
     * Renk secici: tiklayinca hazir renkler arasinda gecer (sag tik / shift ile geri), mesajda gercek renkte kare.
     * Yaninda #RRGGBB kutusu ile ozel renk girilir.
     */
    protected Button color(String label, String tip, java.util.function.IntSupplier get, java.util.function.IntConsumer set) {
        int[] cols = com.jestron.jestronpriv.client.pvp.Palette.COLORS;
        Button b = Button.builder(Component.empty(), btn -> {
            int i = com.jestron.jestronpriv.client.pvp.Palette.index(get.getAsInt());
            int step = hasShiftDown() ? -1 : 1;
            int ni = i < 0 ? 0 : Math.floorMod(i + step, cols.length);
            set.accept(cols[ni]);
            btn.setMessage(colorMessage(label, get.getAsInt()));
            markDirty();
        }).bounds(0, 0, COL_W, 20).build();
        b.setMessage(colorMessage(label, get.getAsInt()));
        b.setTooltip(Tooltip.create(Component.literal((tip == null ? "" : tip + "\n") + "§8Tıkla: sonraki renk · Shift+tık: önceki")));
        addEntry(b, false);
        return b;
    }

    protected static Component colorMessage(String label, int rgb) {
        return Component.literal(label + ": ").append(Component.literal("■ ").withColor(rgb & 0xFFFFFF))
                .append(Component.literal(com.jestron.jestronpriv.client.pvp.Palette.name(rgb)));
    }

    /** #RRGGBB kutusu; gecerli kod yazilinca renk aninda uygulanir. */
    protected net.minecraft.client.gui.components.EditBox hex(String hint, java.util.function.IntConsumer set) {
        return editBox(hint, "Özel renk kodu, ör. #FF00AA", false, 7, () -> "", v -> {
            Integer h = parseHexColor(v);
            if (h != null) set.accept(h);
        });
    }

    protected static Integer parseHexColor(String s) {
        if (s == null) return null;
        String h = s.trim().replace("#", "");
        if (!h.matches("[0-9a-fA-F]{6}")) return null;
        return Integer.parseInt(h, 16);
    }

    /** Tam genislikte bolum basligi (tiklanmaz). */
    protected void header(String text) {
        Button b = Button.builder(Component.literal(text).withStyle(net.minecraft.ChatFormatting.BOLD).withColor(Theme.headerColor()), btn -> {
        }).bounds(0, 0, COL_W, 20).build();
        b.active = false;
        addEntry(b, true);
    }

    protected static String pct(double v) {
        return Math.round(v * 100) + "%";
    }

    /** Degeri adima yuvarlanan kaydirici. */
    protected final class Slider extends AbstractSliderButton {
        private final String label;
        private final double min, max, step;
        private final DoubleConsumer set;
        private final DoubleFunction<String> fmt;

        Slider(String label, double min, double max, double step, DoubleSupplier get, DoubleConsumer set, DoubleFunction<String> fmt) {
            super(0, 0, COL_W, 20, Component.empty(), norm(get.getAsDouble(), min, max));
            this.label = label;
            this.min = min;
            this.max = max;
            this.step = step;
            this.set = set;
            this.fmt = fmt;
            updateMessage();
        }

        private static double norm(double v, double min, double max) {
            return max <= min ? 0 : Math.max(0, Math.min(1, (v - min) / (max - min)));
        }

        private double real() {
            double v = min + value * (max - min);
            if (step > 0) v = Math.round(v / step) * step;
            return Math.max(min, Math.min(max, v));
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(label + ": " + fmt.apply(real())));
        }

        @Override
        protected void applyValue() {
            set.accept(real());
            markDirty();
        }
    }

    // ------------------------------------------------------------------ cizim / kapanis

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Bulaniklastirma yok: oyun arkada gorunur, ayarlar canli onizlenir
        Theme.overlay(g, width, height);
        int x0 = left - 8, x1 = width / 2 + COL_W + GAP / 2 + 8;
        Theme.panel(g, x0, areaTop - 6, x1, areaBottom + 4);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(font, Theme.title(title), width / 2, 12, 0xFFFFFF);
        String sub = subtitle();
        if (sub != null) g.drawCenteredString(font, sub, width / 2, 25, 0xA0A0A0);
        if (maxScroll > 0) {
            int trackH = areaBottom - areaTop;
            int barH = Math.max(12, trackH * trackH / (trackH + maxScroll));
            int barY = areaTop + (int) ((trackH - barH) * (scroll / (double) maxScroll));
            int x = width / 2 + COL_W + GAP / 2 + 3;
            Theme.scrollbar(g, x, areaTop, areaBottom, barY, barH);
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
