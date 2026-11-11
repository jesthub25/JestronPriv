package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.itemskin.ItemSkins;
import com.jestron.jestronpriv.client.itemskin.SkinImport;
import com.jestron.jestronpriv.client.itemskin.TextureLibrary;
import com.jestron.jestronpriv.itemskin.SkinAnim;
import com.jestron.jestronpriv.itemskin.SkinImage;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Doku Kutuphanesi: mod klasoru, kaynak paketleri ve oyunun kendi dokulari tek ekranda. Istedigin paketten istedigin
 * dokuyu sec; istedigin esyaya uygula, editorde ac, kalip yap ya da mod klasorune kopyala. Mod klasorune attigin
 * PNG / GIF'ler hazir doku olarak burada durur; adi bir esyanin adiysa tek tusla o esyaya gecer.
 */
public final class TextureLibraryScreen extends Screen implements JpThemed {

    /** Editorden acildiginda secimi geri verir. */
    public interface Picker {
        void board(SkinAnim anim, String name);

        void template(SkinImage img, String name);
    }

    private static final int CELL = 22, DECODES_PER_FRAME = 10;
    private static final String[] GROUPS = {"Hepsi", "Eşya", "Blok"};
    private static final List<Path> DROPPED = new ArrayList<>();
    private static String lastSource = "";
    private static int group;

    private final Screen parent;
    private final Item target;
    private final Picker picker;
    private final JestronPrivClient mod = JestronPrivClient.get();
    private final List<TextureLibrary> sources = new ArrayList<>();
    private TextureLibrary source;
    private final List<TextureLibrary.Entry> shown = new ArrayList<>();
    /** Kucuk resimler: kaynagin kendi kayit nesnesine bagli (her karede yazi birlestirmeden bulunur). */
    private final Map<TextureLibrary.Entry, SkinImage> thumbs = new java.util.IdentityHashMap<>();
    private TextureLibrary.Entry selected;
    private SkinAnim selectedAnim;
    private String selectedInfo = "";
    private int byName = -1;
    private EditBox search;
    private int gridX, gridY, gridW, gridH, cols, scroll, maxScroll, side, sideX, previewSize, buttonsY;
    private String message = "";
    private long messageAt;

    public TextureLibraryScreen(Screen parent, Item target, Picker picker) {
        super(Component.literal("Doku Kütüphanesi"));
        this.parent = parent;
        this.target = target;
        this.picker = picker;
    }

    private ModConfig cfg() {
        return mod.config();
    }

    private String targetName() {
        return target == null ? "?" : new ItemStack(target).getHoverName().getString();
    }

    // ------------------------------------------------------------------ yerlesim

    @Override
    protected void init() {
        if (sources.isEmpty()) {
            sources.add(TextureLibrary.folder());
            sources.add(TextureLibrary.game());
            for (Path p : DROPPED) sources.add(TextureLibrary.pack(p));
            for (Path p : SkinImport.installedPacks()) if (!DROPPED.contains(p)) sources.add(TextureLibrary.pack(p));
            source = sources.get(0);
            for (TextureLibrary l : sources) if (key(l).equals(lastSource)) source = l;
        }
        side = Math.max(132, Math.min(200, width / 3));
        sideX = width - side - 6;
        gridX = 12;
        gridY = 58;
        gridW = width - side - 30;
        gridH = height - gridY - 46; // altta mesaj satiri
        cols = Math.max(1, gridW / CELL);
        gridW = cols * CELL;
        int srcW = Math.min(140, gridW / 3), grpW = 52;
        addRenderableWidget(Button.builder(Component.literal(font.plainSubstrByWidth("◀ " + source.label + " ▶", srcW - 8)), b -> cycleSource())
                .bounds(gridX, 33, srcW, 20).tooltip(Tooltip.create(Component.literal("Kaynak: mod klasörü, oyun dokuları ya da bir kaynak paketi.\n"
                        + "Tıkla sonraki, Shift+tık önceki. Ekrana .zip paketi sürükle-bırak da olur."))).build());
        String prev = search == null ? "" : search.getValue();
        search = new EditBox(font, gridX + srcW + 4, 34, Math.max(40, gridW - srcW - grpW - 30), 18, Component.literal("Ara"));
        search.setHint(Component.literal("Ara: sword, totem..."));
        search.setMaxLength(48);
        search.setValue(prev);
        search.setResponder(s -> refilter());
        addRenderableWidget(search);
        addRenderableWidget(Button.builder(Component.literal(GROUPS[group]), b -> {
            group = (group + 1) % GROUPS.length;
            b.setMessage(Component.literal(GROUPS[group]));
            refilter();
        }).bounds(gridX + gridW - grpW - 22, 33, grpW, 20).tooltip(Tooltip.create(Component.literal("Eşya / blok dokuları"))).build());
        addRenderableWidget(Button.builder(Component.literal("↻"), b -> {
            source.refresh();
            byName = -1;
            refilter();
            flash("§7Yenilendi: " + source.entries().size() + " doku");
        }).bounds(gridX + gridW - 20, 33, 20, 20).tooltip(Tooltip.create(Component.literal("Yeniden tara (klasöre dosya ekledinse)"))).build());
        refilter();

        // sag panel dugmeleri (asagidan yukari)
        List<Object[]> btn = new ArrayList<>();
        if (picker == null) {
            btn.add(new Object[]{"§a✔ Uygula: " + targetName(), "Seçili dokuyu bu eşyaya uygula (hemen kaydedilir, profil koduyla paylaşılır).", (Runnable) this::apply});
            btn.add(new Object[]{"✎ Editörde aç", "Dokuyu editörde aç, üstünde değişiklik yap.", (Runnable) this::openEditor});
        } else {
            btn.add(new Object[]{"§a✔ Tahtaya yükle", "Seçili dokuyu çizim tahtasına yükle (animasyonluysa kareleriyle). Geri alınabilir.", (Runnable) this::toBoard});
            btn.add(new Object[]{"▦ Kalıp yap", "Seçili dokuyu kalıp (rehber) olarak tahtanın altına koy; üstünden çiz.", (Runnable) this::toTemplate});
        }
        if (source.kind == TextureLibrary.Kind.FOLDER) {
            if (picker == null) btn.add(new Object[]{"★ İsme göre uygula" + (byName > 0 ? " (" + byName + ")" : ""),
                    "Adı bir eşyanın adı olan tüm dosyaları o eşyalara uygula.\nÖrn: diamond_sword.png → Elmas Kılıç, totem_of_undying.gif → Totem.\n"
                            + "Başka mod eşyası: modadi__esya.png", (Runnable) this::applyByName});
        } else btn.add(new Object[]{"⇩ Mod klasörüne kopyala", "Seçili dokuyu (ve animasyon bilgisini) config/jestronpriv/items klasörüne kopyala;\n"
                + "sonra her zaman Mod klasörü kaynağında hazır durur.", (Runnable) this::copy});
        btn.add(new Object[]{"▤ Klasörü aç", "config/jestronpriv/items: buraya PNG / GIF at, burada görünür.", (Runnable) () -> {
            try {
                Util.getPlatform().openPath(TextureLibrary.dir());
            } catch (Throwable t) {
                flash("§cKlasör açılamadı");
            }
        }});
        int bh = height < 300 ? 18 : 20, step = bh + 2;
        buttonsY = height - 34 - btn.size() * step;
        for (int i = 0; i < btn.size(); i++) {
            Object[] e = btn.get(i);
            Runnable r = (Runnable) e[2];
            addRenderableWidget(Button.builder(Component.literal(font.plainSubstrByWidth((String) e[0], side - 20)), b -> r.run())
                    .bounds(sideX + 6, buttonsY + i * step, side - 12, bh).tooltip(Tooltip.create(Component.literal((String) e[1]))).build());
        }
        previewSize = Math.max(16, Math.min(64, buttonsY - 36 - 34));
        addRenderableWidget(Button.builder(Component.literal("Geri"), b -> onClose()).bounds(width / 2 - 75, height - 26, 150, 20).build());
    }

    private static String key(TextureLibrary l) {
        return l.kind + ":" + (l.pack == null ? "" : l.pack.toString());
    }

    private void cycleSource() {
        int i = sources.indexOf(source);
        setSource(sources.get(Math.floorMod(i + (hasShiftDown() ? -1 : 1), sources.size())));
    }

    private void setSource(TextureLibrary l) {
        if (source != null && source != l) source.close();
        source = l;
        lastSource = key(l);
        selected = null;
        selectedAnim = null;
        scroll = 0;
        byName = -1;
        rebuildWidgets();
        int n = source.entries().size();
        flash(n == 0 ? (source.kind == TextureLibrary.Kind.FOLDER ? "§7Klasör boş: PNG / GIF sürükle-bırak ya da bir paketten ⇩ kopyala" : "§7Bu kaynakta doku yok")
                : "§7" + source.label + ": §f" + n + " §7doku");
    }

    private void refilter() {
        shown.clear();
        String q = search == null ? "" : ItemSkinsScreen.norm(search.getValue().trim());
        for (TextureLibrary.Entry e : source.entries()) {
            if (group == 1 && e.group().equals("Blok")) continue;
            if (group == 2 && e.group().equals("Eşya")) continue;
            if (!q.isEmpty() && !e.search().contains(q)) continue;
            shown.add(e);
        }
        if (source.kind == TextureLibrary.Kind.FOLDER && byName < 0) {
            int k = 0;
            for (TextureLibrary.Entry e : source.entries()) if (TextureLibrary.itemIdFor(e.name(), id -> ItemSkins.itemOf(id) != null) != null) k++;
            byName = k;
        }
        int rows = (shown.size() + cols - 1) / cols;
        maxScroll = Math.max(0, rows * CELL - gridH);
        scroll = Math.min(scroll, maxScroll);
    }

    private void flash(String m) {
        message = m;
        messageAt = System.currentTimeMillis();
    }

    // ------------------------------------------------------------------ islemler

    private void select(TextureLibrary.Entry e) {
        selected = e;
        SkinImport.Result r = source.load(e);
        selectedAnim = r.anim();
        selectedInfo = r.anim() == null ? "§c" + r.error()
                : "§7" + r.anim().w + "×" + r.anim().h + (r.anim().animated() ? " · " + r.anim().count() + " kare · " + r.anim().totalMs() + " ms" : "");
    }

    private boolean need() {
        if (selectedAnim == null) {
            flash(selected == null ? "§cÖnce bir doku seç" : "§cBu doku okunamadı");
            return false;
        }
        return true;
    }

    private void apply() {
        if (!need() || target == null) return;
        String err = TextureLibrary.store(cfg(), ItemSkins.idOf(target), selectedAnim);
        if (err != null) {
            flash("§c" + err);
            return;
        }
        cfg().save();
        ItemSkins.invalidate();
        flash("§aUygulandı: §f" + selected.name() + " §a→ §f" + targetName());
    }

    private void openEditor() {
        if (!need() || target == null) return;
        minecraft.setScreen(new ItemPaintScreen(parent, target, selectedAnim));
    }

    private void toBoard() {
        if (!need()) return;
        SkinAnim a = selectedAnim;
        String n = selected.name();
        minecraft.setScreen(parent);
        picker.board(a, n);
    }

    private void toTemplate() {
        if (!need()) return;
        SkinImage img = selectedAnim.frames.get(0);
        String n = selected.name();
        minecraft.setScreen(parent);
        picker.template(img, n);
    }

    private void copy() {
        if (selected == null) {
            flash("§cÖnce bir doku seç");
            return;
        }
        String name = source.copyToFolder(selected);
        if (name == null) flash("§cKopyalanamadı");
        else {
            for (TextureLibrary l : sources) if (l.kind == TextureLibrary.Kind.FOLDER) l.refresh();
            flash("§aMod klasörüne kopyalandı: §f" + name);
        }
    }

    private void applyByName() {
        ModConfig c = cfg();
        int ok = 0, bad = 0;
        String err = null;
        for (TextureLibrary.Entry e : source.entries()) {
            String id = TextureLibrary.itemIdFor(e.name(), x -> ItemSkins.itemOf(x) != null);
            if (id == null) continue;
            SkinImport.Result r = source.load(e);
            String er = r.anim() == null ? r.error() : TextureLibrary.store(c, id, r.anim());
            if (er == null) ok++;
            else {
                bad++;
                err = e.name() + ": " + er;
            }
        }
        if (ok > 0) {
            c.save();
            ItemSkins.invalidate();
        }
        flash(ok == 0 && bad == 0 ? "§7Adı bir eşya olan dosya yok (örn. diamond_sword.png)"
                : "§a" + ok + " eşyaya uygulandı" + (bad > 0 ? " §c· " + bad + " hata (" + err + ")" : ""));
    }

    @Override
    public void onFilesDrop(List<Path> paths) {
        int imported = 0;
        String last = null;
        for (Path p : paths) {
            if (SkinImport.isPack(p)) {
                if (!DROPPED.contains(p)) DROPPED.add(p);
                TextureLibrary l = null;
                for (TextureLibrary s : sources) if (p.equals(s.pack)) l = s;
                if (l == null) {
                    l = TextureLibrary.pack(p);
                    sources.add(2, l);
                }
                setSource(l);
                return;
            }
            String n = TextureLibrary.importFile(p);
            if (n != null) {
                imported++;
                last = n;
            }
        }
        if (imported == 0) {
            flash("§cPNG / JPG / GIF / BMP ya da kaynak paketi (.zip) bırak");
            return;
        }
        TextureLibrary folder = sources.get(0);
        folder.refresh();
        setSource(folder);
        for (TextureLibrary.Entry e : source.entries()) if (e.name().equals(last)) select(e);
        flash("§aMod klasörüne eklendi: §f" + imported + " dosya");
    }

    // ------------------------------------------------------------------ fare

    private int hovered(double mx, double my) {
        if (mx < gridX || mx >= gridX + cols * CELL || my < gridY || my >= gridY + gridH) return -1;
        int c = (int) ((mx - gridX) / CELL), r = (int) ((my - gridY + scroll) / CELL);
        int i = r * cols + c;
        return i >= 0 && i < shown.size() ? i : -1;
    }

    private long lastClick;

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int i = hovered(mx, my);
        if (i >= 0) {
            TextureLibrary.Entry e = shown.get(i);
            boolean again = e == selected && System.currentTimeMillis() - lastClick < 350;
            if (e != selected) select(e);
            lastClick = System.currentTimeMillis();
            if (again) {
                if (picker == null) apply();
                else toBoard();
            }
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        if (maxScroll > 0 && mx < gridX + cols * CELL + 6) {
            scroll = (int) Math.max(0, Math.min(maxScroll, scroll - dy * CELL * 2));
            return true;
        }
        return super.mouseScrolled(mx, my, dx, dy);
    }

    // ------------------------------------------------------------------ cizim

    private SkinImage thumb(TextureLibrary.Entry e, int[] budget) {
        SkinImage t = thumbs.get(e);
        if (t != null || budget[0] <= 0) return t;
        budget[0]--;
        SkinImport.Result r = source.load(e);
        t = r.anim() == null ? SkinImage.blank(1) : r.anim().frames.get(0).resize(16);
        if (thumbs.size() > 6000) thumbs.clear();
        thumbs.put(e, t);
        return t;
    }

    /** Goruntuyu (x, y)'de, her piksel p birim olacak sekilde, satir parcalari halinde cizer. */
    private void image(GuiGraphics g, SkinImage im, int x, int y, int size) {
        int n = im.w;
        for (int yy = 0; yy < n; yy++) {
            int y0 = y + yy * size / n, y1 = y + (yy + 1) * size / n;
            if (y1 <= y0) continue;
            for (int xx = 0; xx < n; ) {
                int c = im.argb[yy * n + xx];
                int e = xx + 1;
                while (e < n && im.argb[yy * n + e] == c) e++;
                if ((c >>> 24) != 0) g.fill(x + xx * size / n, y0, x + e * size / n, y1, c);
                xx = e;
            }
        }
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        Theme.overlay(g, width, height);
        Theme.panel(g, gridX - 4, gridY - 4, gridX + gridW + 6, gridY + gridH + 4);
        Theme.panel(g, sideX, 30, width - 6, height - 32);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(font, Theme.title(title), width / 2, 8, 0xFFFFFF);
        String sub = picker == null ? "§7Hedef: §f" + targetName() + " §7· çift tık: uygula · PNG / GIF / .zip sürükle-bırak"
                : "§7Çizim tahtası için doku seç · çift tık: tahtaya yükle";
        if (font.width(sub) > width - 16) sub = picker == null ? "§7Hedef: §f" + targetName() : "§7Tahta için doku seç";
        g.drawCenteredString(font, sub, width / 2, 19, 0xFFFFFF);
        int hov = hovered(mouseX, mouseY);
        int[] budget = {DECODES_PER_FRAME};
        g.enableScissor(gridX, gridY, gridX + cols * CELL, gridY + gridH);
        int first = scroll / CELL * cols;
        for (int i = Math.max(0, first); i < shown.size(); i++) {
            TextureLibrary.Entry e = shown.get(i);
            int x = gridX + (i % cols) * CELL, y = gridY + (i / cols) * CELL - scroll;
            if (y > gridY + gridH) break;
            boolean sel = e == selected, h = i == hov;
            if (sel || h) Theme.cell(g, x, y, CELL - 1, CELL - 1, sel, h, false);
            SkinImage t = thumb(e, budget);
            if (t == null) g.fill(x + 3, y + 3, x + 19, y + 19, 0x30FFFFFF);
            else if (t.w > 1) image(g, t, x + 3, y + 3, 16);
            else g.drawString(font, "§c?", x + 8, y + 7, 0xFFFFFF);
        }
        g.disableScissor();
        if (maxScroll > 0) {
            int barH = Math.max(12, gridH * gridH / (gridH + maxScroll));
            int barY = gridY + (int) ((gridH - barH) * (scroll / (double) maxScroll));
            Theme.scrollbar(g, gridX + cols * CELL + 2, gridY, gridY + gridH, barY, barH);
        }
        if (shown.isEmpty()) {
            String t = source.entries().isEmpty() ? (source.kind == TextureLibrary.Kind.FOLDER ? "§7Klasör boş · PNG / GIF sürükle-bırak" : "§7Doku yok")
                    : "§7Aramaya uyan doku yok";
            g.drawCenteredString(font, t, gridX + gridW / 2, gridY + gridH / 2 - 4, 0xFFFFFF);
        }
        renderSide(g);
        if (!message.isEmpty() && System.currentTimeMillis() - messageAt < 4500) g.drawCenteredString(font, message, width / 2, height - 38, 0xFFFFFF);
        if (hov >= 0) {
            TextureLibrary.Entry e = shown.get(hov);
            List<Component> tip = new ArrayList<>();
            tip.add(Component.literal("§f" + e.name()));
            tip.add(Component.literal("§8" + e.group() + " · " + source.label));
            g.renderComponentTooltip(font, tip, mouseX, mouseY);
        }
    }

    private void renderSide(GuiGraphics g) {
        int cx = sideX + side / 2, ps = previewSize, px = cx - ps / 2, py = 38;
        g.fill(px - 1, py - 1, px + ps + 1, py + ps + 1, 0xFF000000 | Theme.accent());
        int cell = Math.max(2, ps / 8);
        for (int y = 0; y < ps; y += cell)
            for (int x = 0; x < ps; x += cell)
                g.fill(px + x, py + y, px + Math.min(ps, x + cell), py + Math.min(ps, y + cell), (((x + y) / cell) & 1) == 0 ? 0xFF2A2A2E : 0xFF38383E);
        if (selectedAnim != null) image(g, selectedAnim.frames.get(selectedAnim.frameAt(Util.getMillis())), px, py, ps);
        int ty = py + ps + 5;
        if (selected == null) {
            g.drawCenteredString(font, "§7Bir doku seç", cx, ty, 0xFFFFFF);
            return;
        }
        g.drawCenteredString(font, "§f" + font.plainSubstrByWidth(selected.name(), side - 12), cx, ty, 0xFFFFFF);
        if (ty + 22 <= buttonsY) g.drawCenteredString(font, font.plainSubstrByWidth(selectedInfo, side - 12), cx, ty + 11, 0xFFFFFF);
    }

    @Override
    public void removed() {
        if (source != null) source.close();
        super.removed();
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
