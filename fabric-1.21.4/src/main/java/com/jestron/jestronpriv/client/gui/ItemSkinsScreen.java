package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.itemskin.ItemSkins;
import com.jestron.jestronpriv.client.itemskin.SkinImport;
import com.jestron.jestronpriv.itemskin.SkinAnim;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Esya gorunumleri: oyundaki tum esyalar arama kutulu bir izgarada. Birini sec, sagdan "Ciz / Duzenle" ile kendi
 * dokusunu ciz, PNG / kaynak paketi birak ya da vanilla'ya dondur. Ozellestirilen esyalarin kosesinde isaret var.
 */
public final class ItemSkinsScreen extends Screen implements JpThemed {

    private static final int CELL = 20;

    record Entry(Item item, ItemStack stack, String id, String search) {
    }

    private final Screen parent;
    private final JestronPrivClient mod = JestronPrivClient.get();
    private final List<Entry> all = new ArrayList<>();
    private final List<Entry> shown = new ArrayList<>();
    private EditBox search;
    private static boolean onlyCustom;
    private static String lastSelected = "minecraft:diamond_sword";
    private Entry selected;
    private int gridX, gridY, gridW, gridH, cols, scroll, maxScroll, side;
    private String message = "";
    private long messageAt;

    public ItemSkinsScreen(Screen parent) {
        super(Component.literal("Eşya Görünümleri"));
        this.parent = parent;
    }

    private ModConfig cfg() {
        return mod.config();
    }

    static String norm(String s) {
        return s.toLowerCase(Locale.ROOT).replace('ı', 'i').replace('İ', 'i').replace("i̇", "i").replace('ş', 's').replace('ğ', 'g').replace('ü', 'u')
                .replace('ö', 'o').replace('ç', 'c');
    }

    @Override
    protected void init() {
        if (all.isEmpty()) {
            for (Item it : BuiltInRegistries.ITEM) {
                if (it == Items.AIR) continue;
                String id = ItemSkins.idOf(it);
                ItemStack st = new ItemStack(it);
                String name;
                try {
                    name = st.getHoverName().getString();
                } catch (Throwable t) {
                    name = id;
                }
                all.add(new Entry(it, st, id, norm(id + " " + name)));
            }
        }
        side = width >= 420 ? Math.min(200, width / 3) : 0;
        gridX = 12;
        gridY = 56;
        gridW = width - side - 30;
        gridH = height - gridY - 34;
        cols = Math.max(1, gridW / CELL);
        gridW = cols * CELL;
        String prev = search == null ? "" : search.getValue();
        search = new EditBox(font, gridX, 34, Math.max(60, gridW - 128), 18, Component.literal("Ara"));
        search.setHint(Component.literal("Ara: kılıç, diamond, totem..."));
        search.setMaxLength(48);
        search.setValue(prev);
        search.setResponder(s -> refilter());
        addRenderableWidget(search);
        addRenderableWidget(Button.builder(filterText(), b -> {
            onlyCustom = !onlyCustom;
            b.setMessage(filterText());
            refilter();
        }).bounds(gridX + gridW - 122, 33, 122, 20).tooltip(Tooltip.create(Component.literal("Sadece görünümü değiştirilmiş eşyaları göster."))).build());
        refilter();
        if (selected == null) for (Entry e : all) if (e.id.equals(lastSelected)) selected = e;
        if (side > 0) {
            int px = width - side - 6, bw = side - 12, st = height < 330 ? 22 : 24, y = height - 34 - 5 * st;
            sideButtonsY = y;
            addRenderableWidget(Button.builder(Component.literal("§a✎ Çiz / Düzenle"), b -> openEditor()).bounds(px + 6, y, bw, 20)
                    .tooltip(Tooltip.create(Component.literal("Piksel piksel kendin çiz, PNG ya da kaynak paketinden yükle."))).build());
            addRenderableWidget(Button.builder(Component.literal("⬇ Şu anki dokudan başla"), b -> startFromCurrent()).bounds(px + 6, y + st, bw, 20)
                    .tooltip(Tooltip.create(Component.literal("Vanilla / aktif kaynak paketindeki dokuyu açar, üstüne çizersin."))).build());
            addRenderableWidget(Button.builder(Component.literal("§c↺ Vanilla'ya döndür"), b -> reset()).bounds(px + 6, y + st * 2, bw, 20).build());
            addRenderableWidget(Button.builder(enabledText(), b -> {
                cfg().itemSkinsEnabled = !cfg().itemSkinsEnabled;
                cfg().save();
                b.setMessage(enabledText());
            }).bounds(px + 6, y + st * 3, bw, 20).tooltip(Tooltip.create(Component.literal("Tüm özel eşya görünümlerini aç / kapat (silinmez)."))).build());
            addRenderableWidget(Button.builder(Component.literal("§d❖ Doku Kütüphanesi"), b -> {
                if (selected == null) flash("§cÖnce bir eşya seç");
                else minecraft.setScreen(new TextureLibraryScreen(this, selected.item, null));
            }).bounds(px + 6, y + st * 4, bw, 20).tooltip(Tooltip.create(Component.literal("Herhangi bir kaynak paketinden ya da mod klasöründen (config/jestronpriv/items)\n"
                    + "istediğin dokuyu seç, bu eşyaya uygula. Klasöre attığın PNG / GIF'ler hazır doku olur."))).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Geri"), b -> onClose()).bounds(width / 2 - 75, height - 26, 150, 20).build());
    }

    private Component filterText() {
        return Component.literal("Sadece özel: " + (onlyCustom ? "§aAçık" : "§7Kapalı"));
    }

    private Component enabledText() {
        return Component.literal("Görünümler: " + (cfg().itemSkinsEnabled ? "§aAçık" : "§cKapalı"));
    }

    private void refilter() {
        shown.clear();
        String q = search == null ? "" : norm(search.getValue().trim());
        for (Entry e : all) {
            if (onlyCustom && !ItemSkins.has(e.item)) continue;
            if (!q.isEmpty() && !e.search.contains(q)) continue;
            shown.add(e);
        }
        int rows = (shown.size() + cols - 1) / cols;
        maxScroll = Math.max(0, rows * CELL - gridH);
        scroll = Math.min(scroll, maxScroll);
    }

    private void flash(String m) {
        message = m;
        messageAt = System.currentTimeMillis();
    }

    private void openEditor() {
        if (selected == null) {
            flash("§cÖnce bir eşya seç");
            return;
        }
        minecraft.setScreen(new ItemPaintScreen(this, selected.item, null));
    }

    private void startFromCurrent() {
        if (selected == null) return;
        SkinImport.Result r = SkinImport.current(selected.id);
        if (r.anim() == null) {
            flash("§c" + r.error());
            return;
        }
        minecraft.setScreen(new ItemPaintScreen(this, selected.item, r.anim()));
    }

    private void reset() {
        if (selected == null) return;
        if (cfg().itemSkins.remove(selected.id) != null) {
            cfg().save();
            ItemSkins.invalidate();
            flash("§aVanilla görünüme döndü: §f" + selected.stack.getHoverName().getString());
            if (onlyCustom) refilter();
        } else flash("§7Bu eşya zaten vanilla");
    }

    @Override
    public void onFilesDrop(List<Path> paths) {
        if (selected == null || paths.isEmpty()) {
            flash("§cÖnce bir eşya seç, sonra dosyayı bırak");
            return;
        }
        SkinImport.Result r = SkinImport.fromPath(paths.get(0), selected.id);
        if (r.anim() == null) {
            flash("§c" + r.error());
            return;
        }
        minecraft.setScreen(new ItemPaintScreen(this, selected.item, r.anim()));
    }

    // ------------------------------------------------------------------ cizim / fare

    private int hovered(double mx, double my) {
        if (mx < gridX || mx >= gridX + cols * CELL || my < gridY || my >= gridY + gridH) return -1;
        int c = (int) ((mx - gridX) / CELL), r = (int) ((my - gridY + scroll) / CELL);
        int i = r * cols + c;
        return i >= 0 && i < shown.size() ? i : -1;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        Theme.overlay(g, width, height);
        Theme.panel(g, gridX - 4, gridY - 4, gridX + gridW + 6, gridY + gridH + 4);
        if (side > 0) Theme.panel(g, width - side - 6, 30, width - 6, height - 32);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(font, Theme.title(title), width / 2, 8, 0xFFFFFF);
        int custom = cfg().itemSkins.size();
        g.drawCenteredString(font, "§7" + all.size() + " eşya · §f" + custom + " §7özel · PNG / GIF / kaynak paketi (.zip) sürükle-bırak · çift tık: düzenle", width / 2,
                19, 0xFFFFFF);
        int hov = hovered(mouseX, mouseY);
        g.enableScissor(gridX, gridY, gridX + cols * CELL, gridY + gridH);
        int first = scroll / CELL * cols;
        for (int i = Math.max(0, first); i < shown.size(); i++) {
            Entry e = shown.get(i);
            int x = gridX + (i % cols) * CELL, y = gridY + (i / cols) * CELL - scroll;
            if (y > gridY + gridH) break;
            boolean sel = e == selected, h = i == hov;
            if (sel || h) Theme.cell(g, x, y, CELL - 1, CELL - 1, sel, h, false);
            g.renderItem(e.stack, x + 2, y + 2);
            if (ItemSkins.has(e.item)) {
                g.pose().pushPose();
                g.pose().translate(0, 0, 250);
                g.fill(x + CELL - 6, y + 1, x + CELL - 2, y + 5, 0xFF000000 | Theme.accent());
                g.pose().popPose();
            }
        }
        g.disableScissor();
        if (maxScroll > 0) {
            int barH = Math.max(12, gridH * gridH / (gridH + maxScroll));
            int barY = gridY + (int) ((gridH - barH) * (scroll / (double) maxScroll));
            Theme.scrollbar(g, gridX + cols * CELL + 2, gridY, gridY + gridH, barY, barH);
        }
        if (side > 0 && selected != null) renderSide(g);
        if (!message.isEmpty() && System.currentTimeMillis() - messageAt < 4000) g.drawCenteredString(font, message, width / 2, height - 38, 0xFFFFFF);
        if (hov >= 0) {
            Entry e = shown.get(hov);
            List<Component> tip = new ArrayList<>();
            tip.add(Component.literal("§f" + e.stack.getHoverName().getString()));
            tip.add(Component.literal("§8" + e.id));
            if (ItemSkins.has(e.item)) tip.add(Component.literal("§d✎ Özel görünüm"));
            g.renderComponentTooltip(font, tip, mouseX, mouseY);
        }
    }

    private int sideButtonsY = Integer.MAX_VALUE;

    private void renderSide(GuiGraphics g) {
        int x0 = width - side - 6, cx = x0 + side / 2;
        // kucuk ekranda onizleme kuculur, dugmelerin altina girmez
        int sc = Math.max(1, Math.min(4, (sideButtonsY - 40 - 38) / 16)), top = 38;
        g.pose().pushPose();
        g.pose().translate(cx - 8 * sc, top, 0);
        g.pose().scale(sc, sc, 1f);
        g.renderItem(selected.stack, 0, 0);
        g.pose().popPose();
        int ty = top + 16 * sc + 5;
        g.drawCenteredString(font, "§f" + font.plainSubstrByWidth(selected.stack.getHoverName().getString(), side - 12), cx, ty, 0xFFFFFF);
        if (ty + 24 <= sideButtonsY) g.drawCenteredString(font, "§8" + font.plainSubstrByWidth(selected.id, side - 12), cx, ty + 11, 0xFFFFFF);
        String code = cfg().itemSkins.get(selected.id);
        if (ty + 35 <= sideButtonsY) g.drawCenteredString(font, status(code), cx, ty + 22, 0xFFFFFF);
    }

    // durum yazisi koda gore onbellekte: her karede cozmesin
    private String statusCode, statusText;

    private String status(String code) {
        if (code == null) return "§7Vanilla görünüm";
        if (!code.equals(statusCode)) {
            SkinAnim a = SkinAnim.decode(code);
            statusText = a == null ? "§cBozuk görünüm"
                    : "§d✎ Özel çizim §7" + a.w + "×" + a.h + (a.animated() ? " · " + a.count() + " kare" : "");
            statusCode = code;
        }
        return statusText;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int i = hovered(mx, my);
        if (i >= 0) {
            Entry e = shown.get(i);
            boolean again = e == selected && System.currentTimeMillis() - lastClick < 350;
            selected = e;
            lastSelected = e.id;
            lastClick = System.currentTimeMillis();
            if (again) openEditor();
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    private long lastClick;

    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        if (maxScroll > 0 && mx < gridX + cols * CELL + 6) {
            scroll = (int) Math.max(0, Math.min(maxScroll, scroll - dy * CELL * 2));
            return true;
        }
        return super.mouseScrolled(mx, my, dx, dy);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
