package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.cape.CapeArt;
import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.itemskin.ItemSkins;
import com.jestron.jestronpriv.client.itemskin.SkinImport;
import com.jestron.jestronpriv.client.itemskin.TextureLibrary;
import com.jestron.jestronpriv.itemskin.SkinAnim;
import com.jestron.jestronpriv.itemskin.SkinImage;
import com.jestron.jestronpriv.itemskin.SkinPaint;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Piksel duzenleyici: secilen esyanin dokusunu kendin cizersin.
 * <ul>
 * <li>Araclar: kalem, silgi, kova, renk al, cizgi, kutu, renk degistir, golge (sol koyulastir / sag aciklastir);
 * ayna, firca 1-4, yatay / dikey cevir, dondur, dis hat, ok tuslariyla kaydir, izgara, temizle.</li>
 * <li>Kalip: cizim tahtasinin altinda soluk ya da kontur halinde bir esyanin dokusu (bu esya ya da kilic, kazma, yay,
 * totem...) - ustunden cizersin, kaydedilmez.</li>
 * <li>Animasyon: en cok 32 kare, her biri ayri cizilir; kare ekle / cogalt / sil / tasi, kare suresi, sogan kabugu
 * (onceki kare soluk), oynat. Esya oyunda da animasyonlu gorunur.</li>
 * <li>Renk: 32 renkli palet, son renkler, #RRGGBB(AA), ton / doygunluk / parlaklik / saydamlik.</li>
 * <li>Ice aktar: PNG / JPG / GIF (kareleriyle) / kaynak paketi suruklle-birak; PNG olarak disa aktar.</li>
 * </ul>
 * Cizdikce esya canli degisir; Kaydet'e basinca ayara yazilir ve profil koduyla paylasilir.
 */
public final class ItemPaintScreen extends Screen implements JpThemed {

    static final int[] PALETTE = {
            0xFF000000, 0xFF3F3F3F, 0xFF7F7F7F, 0xFFBFBFBF, 0xFFFFFFFF, 0xFF5B3A1E, 0xFF8B5A2B, 0xFFC08A55,
            0xFF7A0E0E, 0xFFD32F2F, 0xFFFF6B6B, 0xFFFF9800, 0xFFFFC107, 0xFFFFEB3B, 0xFFFFF59D, 0xFFF5D6B4,
            0xFF1B5E20, 0xFF43A047, 0xFF8BC34A, 0xFF00695C, 0xFF26C6DA, 0xFF80DEEA, 0xFF0D47A1, 0xFF1E88E5,
            0xFF64B5F6, 0xFF311B92, 0xFF7E57C2, 0xFFB39DDB, 0xFF880E4F, 0xFFEC407A, 0xFFF8BBD0, 0x00000000};

    /** Kalip kaynaklari: ilk eleman bu esya. */
    static final String[] TEMPLATES = {"", "minecraft:diamond_sword", "minecraft:netherite_sword", "minecraft:diamond_pickaxe", "minecraft:diamond_axe",
            "minecraft:diamond_shovel", "minecraft:diamond_hoe", "minecraft:mace", "minecraft:bow", "minecraft:trident", "minecraft:fishing_rod",
            "minecraft:totem_of_undying", "minecraft:golden_apple", "minecraft:ender_pearl", "minecraft:potion", "minecraft:arrow", "minecraft:stick",
            "minecraft:elytra", "minecraft:nether_star", "minecraft:diamond"};
    /** Ust yardim satiri: ekrana sigan en uzun hali. */
    private static final String[] HELP = {"§7Sol: çiz · Sağ: sil · Orta / Alt: renk al · Oklar: kaydır · Boşluk: oynat · , . kare (Shift: taşı) · Ctrl+Z/Y/S",
            "§7Sol: çiz · Sağ: sil · Orta: renk al · Boşluk: oynat · , . kare · Ctrl+Z/Y/S", "§7Sol: çiz · Sağ: sil · Boşluk: oynat · , . kare"};
    static final int[] DURATIONS = {50, 80, 100, 150, 200, 300, 500, 1000};
    static final int[] TEMPLATE_ALPHA = {20, 35, 50, 70};

    private static final int SIDE_L = 116, SIDE_R = 132, COL = 56;

    private final Screen parent;
    private final Item item;
    private final String id;
    private final JestronPrivClient mod = JestronPrivClient.get();
    private final SkinPaint paint;
    private final List<Integer> recent = new ArrayList<>();
    private int color = 0xFFFFFFFF;
    private float hue, sat, val = 1f;
    private boolean changed, exitArmed, syncing;
    private String message = "";
    private long messageAt;
    // kalip
    private static int templateMode = 1, templateAlpha = 1, templateIndex;
    private final Map<String, SkinImage> templates = new HashMap<>();
    private SkinImage templateScaled;
    private String templateKey = "";
    // animasyon
    private boolean playing;
    // yerlesim
    private int cx, cy, cs, px, leftX, rightX, areaX, areaW, thumbY, frameBtnY;
    // fare
    private boolean drawing;
    private int lastPx = -1, lastPy = -1, startPx, startPy, drawButton;
    private EditBox hexBox;
    private final List<ColorSlider> sliders = new ArrayList<>();
    /** Kutuphaneden secilen kalip (null: listedeki kaynak). */
    private SkinImage customTemplate;
    private String customTemplateName = "";
    /** Ekran baska ekrana gecerken onizleme kapandi; geri gelince yeniden ac. */
    private boolean previewCleared;
    private ItemStack stack;

    public ItemPaintScreen(Screen parent, Item item, SkinAnim start) {
        super(Component.literal("Eşya Çiz"));
        this.parent = parent;
        this.item = item;
        this.id = ItemSkins.idOf(item);
        SkinAnim a = start;
        if (a == null) {
            String code = mod.config().itemSkins.get(id);
            a = code == null ? null : SkinAnim.decode(code);
            if (a == null) {
                // bos tahta; kalip olarak esyanin kendi dokusu altta gorunur
                SkinImage t = SkinImport.template(id);
                a = SkinAnim.of(SkinImage.blank(t == null ? 16 : Math.max(16, Math.min(64, t.w))));
            }
        } else changed = true;
        this.paint = new SkinPaint(a);
        setColorFromArgb(0xFFFFFFFF);
        preview(-1);
    }

    private ModConfig cfg() {
        return mod.config();
    }

    // ------------------------------------------------------------------ yerlesim

    /** Kucuk ekran (GUI olcegi 4 / 1080p = 480x270): satirlar sikisir, etiketler kisalir. */
    boolean compactH() {
        return height < 330;
    }

    boolean compactW() {
        return areaW < 272;
    }

    /** Sol sutun satir araligi / dugme yuksekligi. */
    private int row() {
        return compactH() ? 18 : 20;
    }

    int sliderStep() {
        return compactH() ? 16 : 18;
    }

    int swatchStep() {
        return compactH() ? 14 : 16;
    }

    int swatchY() {
        return 36 + 24 + 4 * sliderStep() + (compactH() ? 4 : 8);
    }

    int recentY() {
        return swatchY() + 4 * swatchStep() + (compactH() ? 4 : 14);
    }

    int previewY() {
        return recentY() + swatchStep() + (compactH() ? 4 : 10);
    }

    @Override
    protected void init() {
        sliders.clear();
        int n = paint.size();
        areaW = width - SIDE_L - SIDE_R - 40;
        areaX = SIDE_L + 20;
        // tahtanin altinda: kare seridi (23) + kare dugmeleri (21+16) + mesaj satiri
        int availH = height - 40 - 82;
        px = Math.max(2, Math.min(areaW, availH) / n);
        cs = px * n;
        cx = areaX + (areaW - cs) / 2;
        cy = 40 + Math.max(0, (availH - cs) / 2);
        leftX = areaX - SIDE_L - 10;
        rightX = areaX + areaW + 10;
        thumbY = cy + cs + 5;
        frameBtnY = thumbY + 21;
        if (cs > availH) {
            // cok kucuk pencere + buyuk cozunurluk: kare seridi gizlenir (kareler , . tuslari / dugmelerle), tahta yukari
            cy = 36;
            thumbY = -10_000;
            frameBtnY = cy + cs + 3;
        }
        int row = row(), bh = row - 2;

        // sol: araclar. Normal ekranda 2 sutun yazili, kucuk ekranda 3 sutun simge (ipucu yazili)
        boolean ch = compactH();
        int cols = ch ? 3 : 2, gap = ch ? 2 : 4, cw0 = (SIDE_L - gap * (cols - 1)) / cols;
        List<Object[]> left = new ArrayList<>();
        String[][] tools = {{"✎ Kalem", "✎", "Kalem (B)"}, {"⌫ Silgi", "⌫", "Silgi (E)"}, {"▣ Kova", "▣", "Kova: bitişik alanı doldurur (G)"},
                {"◉ Renk al", "◉", "Damlalık: tıkladığın rengi alır (I) · Orta tık / Alt+tık her zaman"}, {"╱ Çizgi", "╱", "Çizgi (L)"},
                {"▭ Kutu", "▭", "Dikdörtgen çerçeve (R)"}, {"⇄ Değiş", "⇄", "Renk değiştir: tıkladığın renkteki TÜM pikselleri seçili renge çevirir (C)"},
                {"◐ Gölge", "◐", "Gölgelendir: sol tık koyulaştırır, sağ tık açar (D)"}};
        for (int i = 0; i < tools.length; i++) {
            SkinPaint.Tool t = SkinPaint.Tool.values()[i];
            String sel = paint.tool == t ? "§a" : "";
            left.add(new Object[]{sel + tools[i][0], sel + tools[i][1], tools[i][2], (Runnable) () -> {
                paint.tool = t;
                rebuildWidgets();
            }});
        }
        if (!ch && (left.size() % cols) != 0) left.add(null);
        left.add(new Object[]{"↶ Geri", "↶", "Geri al (Ctrl+Z)", (Runnable) this::undo});
        left.add(new Object[]{"İleri ↷", "↷", "İleri al (Ctrl+Y)", (Runnable) this::redo});
        String mir = paint.mirror ? "§a" : "";
        left.add(new Object[]{mir + "◫ Ayna", mir + "◫", "Sol-sağ simetrik çiz (M)", (Runnable) () -> {
            paint.mirror = !paint.mirror;
            rebuildWidgets();
        }});
        left.add(new Object[]{"Fırça " + paint.brush, "●" + paint.brush, "Fırça boyutu 1-4 (kalem / silgi / gölge)", (Runnable) () -> {
            paint.brush = paint.brush % 4 + 1;
            rebuildWidgets();
        }});
        left.add(new Object[]{"⇆ Yatay", "⇆", "Yatay çevir", (Runnable) () -> op(paint::flipH, "§aYatay çevrildi")});
        left.add(new Object[]{"⇅ Dikey", "⇅", "Dikey çevir", (Runnable) () -> op(paint::flipV, "§aDikey çevrildi")});
        left.add(new Object[]{"⟳ Döndür", "⟳", "Saat yönünde 90° döndür", (Runnable) () -> op(paint::rotate, "§aDöndürüldü")});
        left.add(new Object[]{"▢ Dış hat", "▢", "Dış hat: çiziminin çevresine seçili renkte 1 piksel çerçeve", (Runnable) () -> {
            stopPlay();
            paint.push();
            int k = paint.outline(color);
            touched(paint.current());
            flash(k == 0 ? "§7Dış hat için yer yok" : "§aDış hat: " + k + " piksel");
        }});
        String gr = paint.grid ? "§a" : "";
        left.add(new Object[]{gr + "# Izgara", gr + "#", "Izgara çizgileri", (Runnable) () -> {
            paint.grid = !paint.grid;
            rebuildWidgets();
        }});
        left.add(new Object[]{"§c✖ Temizle", "§c✖", "Bu kareyi temizle", (Runnable) () -> op(paint::clear, "§7Kare temizlendi")});
        int y = 36, k = 0;
        for (Object[] e : left) {
            if (e != null) {
                Runnable r = (Runnable) e[3];
                addRenderableWidget(Button.builder(Component.literal((String) e[ch ? 1 : 0]), b -> r.run()).bounds(leftX + (k % cols) * (cw0 + gap), y, cw0, bh)
                        .tooltip(Tooltip.create(Component.literal((String) e[2]))).build());
            }
            k++;
            if (k % cols == 0) y += row;
        }
        if (k % cols != 0) y += row;
        y += ch ? 2 : 4;
        // kalip
        small(0, y, bh, "Kalıp: " + new String[]{"§7Yok", "§fSoluk", "§fKontur"}[templateMode],
                "Kalıp: çizim tahtasının altında rehber (kaydedilmez).\nYok · Soluk: dokunun kendisi · Kontur: sadece dış çizgisi (T)", () -> {
                    templateMode = (templateMode + 1) % 3;
                    rebuildWidgets();
                });
        small(1, y, bh, "%" + TEMPLATE_ALPHA[templateAlpha], "Kalıp görünürlüğü", () -> {
            templateAlpha = (templateAlpha + 1) % TEMPLATE_ALPHA.length;
            rebuildWidgets();
        });
        y += row;
        addRenderableWidget(Button.builder(Component.literal(font.plainSubstrByWidth("◀ " + templateName() + " ▶", SIDE_L - 6)), b -> {
            if (customTemplate != null) {
                customTemplate = null;
                templateKey = "";
            } else templateIndex = Math.floorMod(templateIndex + (hasShiftDown() ? -1 : 1), TEMPLATES.length);
            rebuildWidgets();
        }).bounds(leftX, y, SIDE_L, bh).tooltip(Tooltip.create(Component.literal("Kalıp kaynağı: tıkla sonraki, Shift+tık önceki.\n"
                + "Bu eşya ya da kılıç, kazma, balta, yay, totem... dokusunun üstünden çiz.\n"
                + "Herhangi bir paketten kalıp: ❖ Kütüphane › ▦ Kalıp yap."))).build());

        // sag: renk
        hexBox = new EditBox(font, rightX + 26, 38, 104, 16, Component.literal("#RRGGBB"));
        hexBox.setMaxLength(9);
        syncing = true;
        hexBox.setValue(hex(color));
        syncing = false;
        hexBox.setResponder(this::onHex);
        addRenderableWidget(hexBox);
        int ry = 36 + 24;
        String[] names = {"Ton", "Doygunluk", "Parlaklık", "Saydamlık"};
        for (int i = 0; i < 4; i++) {
            ColorSlider sl = new ColorSlider(rightX, ry, 130, sliderStep() - 2, names[i], i);
            sliders.add(sl);
            addRenderableWidget(sl);
            ry += sliderStep();
        }

        // kare dugmeleri (tahtanin altinda); dar ekranda kisa etiketler, cok darda tasima dugmeleri yok (Shift + , .)
        boolean cw = compactW();
        int fx = areaX, fw = cw ? 17 : 20, fbCount = areaW < 180 ? 3 : 5;
        String[][] fb = {{"+", "Boş kare ekle"}, {"❏", "Bu kareyi çoğalt (animasyonun sonraki adımı için en kolayı)"}, {"§c✖", "Bu kareyi sil"},
                {"◀", "Kareyi sola taşı"}, {"▶", "Kareyi sağa taşı"}};
        Runnable[] fr = {() -> frameOp(paint.addFrame(), "§aKare eklendi"), () -> frameOp(paint.duplicateFrame(), "§aKare çoğaltıldı"),
                () -> frameOp(paint.deleteFrame(), "§7Kare silindi"), () -> frameOp(paint.moveFrame(-1), null), () -> frameOp(paint.moveFrame(1), null)};
        for (int i = 0; i < fbCount; i++) {
            Runnable r = fr[i];
            addRenderableWidget(Button.builder(Component.literal(fb[i][0]), b -> r.run()).bounds(fx, frameBtnY, fw, 16)
                    .tooltip(Tooltip.create(Component.literal(fb[i][1]))).build());
            fx += fw + 1;
        }
        fx += 3;
        int pw = cw ? 22 : 52, mw = cw ? 30 : 50, ow = cw ? 22 : 46;
        addRenderableWidget(Button.builder(Component.literal(playing ? (cw ? "§a■" : "§a■ Dur") : (cw ? "▶" : "▶ Oynat")), b -> togglePlay())
                .bounds(fx, frameBtnY, pw, 16).tooltip(Tooltip.create(Component.literal("Animasyonu oynat / durdur (Boşluk)"))).build());
        fx += pw + 2;
        addRenderableWidget(Button.builder(Component.literal(paint.ms(paint.current()) + (cw ? "" : "ms")), b -> cycleDuration()).bounds(fx, frameBtnY, mw, 16)
                .tooltip(Tooltip.create(Component.literal("Bu karenin süresi (milisaniye). Shift+tık: tüm karelere uygula."))).build());
        fx += mw + 2;
        addRenderableWidget(Button.builder(Component.literal((paint.onion ? "§a" : "") + (cw ? "◎" : "Soğan")), b -> {
            paint.onion = !paint.onion;
            rebuildWidgets();
        }).bounds(fx, frameBtnY, ow, 16).tooltip(Tooltip.create(Component.literal("Soğan kabuğu: önceki kareyi soluk göster (sonraki adımı çizerken kolaylık)."))).build());

        // alt satir (dar ekranda daralir, tasmaz)
        int by = height - 26, w = Math.min(96, (width - 24) / 6 - 4);
        int total = w * 6 + 4 * 5, bx = width / 2 - total / 2;
        addRenderableWidget(Button.builder(Component.literal("Boyut: " + n + "×" + n), b -> resize()).bounds(bx, by, w, 20)
                .tooltip(Tooltip.create(Component.literal("16 → 32 → 64 piksel. Tüm kareler ölçeklenir."))).build());
        addRenderableWidget(Button.builder(Component.literal("⬇ Şu anki doku"), b -> {
            SkinImport.Result r = SkinImport.current(id);
            if (r.anim() == null) flash("§c" + r.error());
            else load(r.anim(), "§aYüklendi: §f" + r.source());
        }).bounds(bx + (w + 4), by, w, 20).tooltip(Tooltip.create(Component.literal("Vanilla / aktif kaynak paketindeki dokuyu aç (animasyonluysa kareleriyle)."))).build());
        addRenderableWidget(Button.builder(Component.literal("§d❖ Kütüphane"), b -> openLibrary()).bounds(bx + (w + 4) * 2, by, w, 20)
                .tooltip(Tooltip.create(Component.literal("Doku Kütüphanesi: herhangi bir kaynak paketinden, oyunun dokularından ya da mod klasöründen\n"
                        + "(config/jestronpriv/items) istediğin dokuyu seç: tahtaya yükle ya da kalıp yap.\n"
                        + "Ya da PNG / JPG / GIF / .zip paketini ekrana sürükle-bırak."))).build());
        if (previewCleared) {
            previewCleared = false;
            preview(-1);
        }
        addRenderableWidget(Button.builder(Component.literal("⇩ PNG kaydet"), b -> exportPng()).bounds(bx + (w + 4) * 3, by, w, 20)
                .tooltip(Tooltip.create(Component.literal("Çizimi PNG olarak dışa aktar (animasyon: şerit + .mcmeta, kaynak paketine hazır)."))).build());
        addRenderableWidget(Button.builder(Component.literal("§a✔ Kaydet"), b -> save()).bounds(bx + (w + 4) * 4, by, w, 20)
                .tooltip(Tooltip.create(Component.literal("Ayarlara yazar (Ctrl+S). Profil koduyla arkadaşına da geçer."))).build());
        addRenderableWidget(Button.builder(Component.literal(exitArmed ? "§cÇık?" : "İptal"), b -> cancel()).bounds(bx + (w + 4) * 5, by, w, 20)
                .tooltip(Tooltip.create(Component.literal("Kaydetmeden çık (değişiklik varsa iki kez bas)."))).build());
    }

    private void small(int col, int y, int h, String label, String tip, Runnable r) {
        addRenderableWidget(Button.builder(Component.literal(label), b -> r.run()).bounds(leftX + col * (COL + 4), y, COL, h)
                .tooltip(Tooltip.create(Component.literal(tip))).build());
    }

    private String templateName() {
        if (customTemplate != null) return "★ " + customTemplateName.substring(customTemplateName.lastIndexOf('/') + 1);
        String t = TEMPLATES[templateIndex];
        if (t.isEmpty()) return "Bu eşya";
        return new ItemStack(ItemSkins.itemOf(t) == null ? item : ItemSkins.itemOf(t)).getHoverName().getString();
    }

    // ------------------------------------------------------------------ islemler

    private void flash(String m) {
        message = m;
        messageAt = System.currentTimeMillis();
    }

    private void preview(int frame) {
        ItemSkins.setPreview(item, paint.live(), frame);
        ItemSkins.setPreviewFrame(playing ? -1 : paint.current());
    }

    private void touched(int frame) {
        changed = true;
        exitArmed = false;
        preview(frame);
    }

    private void op(Runnable r, String msg) {
        stopPlay();
        paint.push();
        r.run();
        touched(paint.current());
        flash(msg);
    }

    private void frameOp(boolean ok, String msg) {
        stopPlay();
        if (!ok) {
            flash(paint.count() >= SkinAnim.MAX_FRAMES ? "§cEn çok " + SkinAnim.MAX_FRAMES + " kare" : "§7Yapılamadı");
            return;
        }
        touched(-1);
        if (msg != null) flash(msg + " §8(" + (paint.current() + 1) + "/" + paint.count() + ")");
        rebuildWidgets();
    }

    private void load(SkinAnim a, String msg) {
        stopPlay();
        paint.replace(a);
        touched(-1);
        flash(msg);
        rebuildWidgets();
    }

    private void undo() {
        stopPlay();
        if (paint.undo()) touched(-1);
        rebuildWidgets();
    }

    private void redo() {
        stopPlay();
        if (paint.redo()) touched(-1);
        rebuildWidgets();
    }

    private void resize() {
        int n = paint.size();
        int next = n < 16 ? 16 : (n < 32 ? 32 : (n < 64 ? 64 : 16));
        stopPlay();
        paint.resize(next);
        touched(-1);
        flash("§aÇözünürlük: §f" + next + "×" + next);
        rebuildWidgets();
    }

    private void togglePlay() {
        if (paint.count() < 2) {
            flash("§7Animasyon için en az 2 kare ekle (❏ çoğalt)");
            return;
        }
        playing = !playing;
        preview(-2);
        rebuildWidgets();
    }

    private void stopPlay() {
        if (playing) {
            playing = false;
            preview(-2);
            rebuildWidgets();
        }
    }

    private void cycleDuration() {
        int cur = paint.ms(paint.current()), next = DURATIONS[0];
        for (int d : DURATIONS)
            if (d > cur) {
                next = d;
                break;
            }
        paint.push(); // sure degisikligi de geri alinabilsin
        if (hasShiftDown()) {
            paint.setAllMs(next);
            flash("§aTüm kareler: §f" + next + " ms");
        } else paint.setMs(paint.current(), next);
        touched(-2);
        rebuildWidgets();
    }

    private void selectFrame(int i) {
        stopPlay();
        paint.select(i);
        preview(-2);
        rebuildWidgets();
    }

    private void openLibrary() {
        stopPlay();
        minecraft.setScreen(new TextureLibraryScreen(this, item, new TextureLibraryScreen.Picker() {
            @Override
            public void board(SkinAnim anim, String name) {
                load(anim, "§aTahtaya yüklendi: §f" + name + " §7(Ctrl+Z geri alır)");
            }

            @Override
            public void template(SkinImage img, String name) {
                setCustomTemplate(img, name);
            }
        }));
    }

    /** Kutuphaneden kalip: listedeki kaynaklarin yerine gecer (kaynak dugmesine basinca listeye doner). */
    void setCustomTemplate(SkinImage img, String name) {
        customTemplate = img;
        customTemplateName = name == null ? "" : name;
        templateKey = "";
        if (templateMode == 0) templateMode = 1;
        flash("§aKalıp: §f" + customTemplateName);
        rebuildWidgets();
    }

    @Override
    public void onFilesDrop(List<Path> paths) {
        if (paths.isEmpty()) return;
        SkinImport.Result r = SkinImport.fromPath(paths.get(0), id);
        if (r.anim() == null) flash("§c" + r.error());
        else load(r.anim(), "§aYüklendi: §f" + r.source());
    }

    private void exportPng() {
        try {
            SkinAnim a = paint.anim();
            BufferedImage bi = new BufferedImage(a.w, a.h * a.count(), BufferedImage.TYPE_INT_ARGB);
            for (int f = 0; f < a.count(); f++) bi.setRGB(0, f * a.h, a.w, a.h, a.frames.get(f).argb, 0, a.w);
            Path dir = FabricLoader.getInstance().getConfigDir().resolve("jestronpriv").resolve("items");
            Files.createDirectories(dir);
            // ad = esyanin adi: Doku Kutuphanesi "isme gore uygula" ile geri yuklenebilir
            int colon = id.indexOf(':');
            String ns = colon < 0 ? "minecraft" : id.substring(0, colon), path = id.substring(colon + 1).replace('/', '_');
            String base = ns.equals("minecraft") ? path : ns + "__" + path;
            Path out = dir.resolve(base + ".png");
            ImageIO.write(bi, "png", out.toFile());
            Path meta = dir.resolve(base + ".png.mcmeta");
            if (a.animated()) {
                StringBuilder sb = new StringBuilder("{\n  \"animation\": {\n    \"frames\": [");
                for (int f = 0; f < a.count(); f++) sb.append(f == 0 ? "" : ", ").append("{\"index\": ").append(f).append(", \"time\": ")
                        .append(Math.max(1, Math.round(a.ms[f] / 50f))).append('}');
                sb.append("]\n  }\n}\n");
                Files.writeString(meta, sb.toString(), StandardCharsets.UTF_8);
            } else Files.deleteIfExists(meta);
            flash("§aKaydedildi: §fconfig/jestronpriv/items/" + base + ".png");
            Util.getPlatform().openPath(dir);
        } catch (Throwable t) {
            flash("§cPNG yazılamadı: " + t.getClass().getSimpleName());
        }
    }

    private void save() {
        ModConfig c = cfg();
        SkinAnim a = paint.anim();
        String err = TextureLibrary.store(c, id, a);
        if (err != null) {
            flash("§c" + err);
            return;
        }
        c.save();
        ItemSkins.invalidate();
        changed = false;
        ItemSkins.setPreview(null, null, -1);
        minecraft.setScreen(parent);
    }

    private void cancel() {
        if (changed && !exitArmed) {
            exitArmed = true;
            flash("§eKaydedilmemiş değişiklik var · tekrar bas: kaydetmeden çık");
            rebuildWidgets();
            return;
        }
        ItemSkins.setPreview(null, null, -1);
        minecraft.setScreen(parent);
    }

    @Override
    public void onClose() {
        cancel();
    }

    @Override
    public void removed() {
        // hangi yoldan kapanirsa kapansin onizleme kalmasin (kutuphaneden donulurse init yeniden acar)
        ItemSkins.setPreview(null, null, -1);
        previewCleared = true;
        super.removed();
    }

    // ------------------------------------------------------------------ renk

    static String hex(int argb) {
        return (argb >>> 24) == 255 ? String.format(Locale.ROOT, "#%06X", argb & 0xFFFFFF) : String.format(Locale.ROOT, "#%08X", argb);
    }

    private void onHex(String s) {
        if (syncing) return;
        String t = s.trim().replace("#", "");
        try {
            if (t.length() == 6) setColor(0xFF000000 | Integer.parseInt(t, 16), false);
            else if (t.length() == 8) setColor((int) Long.parseLong(t, 16), false);
        } catch (NumberFormatException ignored) {
        }
    }

    private void setColorFromArgb(int argb) {
        color = argb;
        float[] hsv = SkinPaint.hsv(argb);
        hue = hsv[0];
        sat = hsv[1];
        val = hsv[2];
    }

    private void setColor(int argb, boolean updateHex) {
        setColorFromArgb(argb);
        for (ColorSlider s : sliders) s.sync();
        if (updateHex && hexBox != null) {
            syncing = true;
            hexBox.setValue(hex(argb));
            syncing = false;
        }
    }

    private void fromHsv() {
        int a = color >>> 24;
        color = (a << 24) | CapeArt.hsv(hue, sat, val);
        if (hexBox != null) {
            syncing = true;
            hexBox.setValue(hex(color));
            syncing = false;
        }
    }

    private void remember(int c) {
        if ((c >>> 24) == 0) return;
        recent.remove((Integer) c);
        recent.add(0, c);
        while (recent.size() > 8) recent.remove(recent.size() - 1);
    }

    /** Ton / doygunluk / parlaklik / saydamlik kaydiricisi. */
    private final class ColorSlider extends AbstractSliderButton {
        final String label;
        final int kind;

        ColorSlider(int x, int y, int w, int h, String label, int kind) {
            super(x, y, w, h, Component.empty(), 0);
            this.label = label;
            this.kind = kind;
            sync();
        }

        double current() {
            return switch (kind) {
                case 0 -> hue;
                case 1 -> sat;
                case 2 -> val;
                default -> (color >>> 24) / 255.0;
            };
        }

        void sync() {
            value = Math.max(0, Math.min(1, current()));
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(label + ": " + Math.round(value * (kind == 0 ? 360 : 100)) + (kind == 0 ? "°" : "%")));
        }

        @Override
        protected void applyValue() {
            switch (kind) {
                case 0 -> hue = (float) value;
                case 1 -> sat = (float) value;
                case 2 -> val = (float) value;
                default -> color = ((int) Math.round(value * 255) << 24) | (color & 0xFFFFFF);
            }
            fromHsv();
        }
    }

    // ------------------------------------------------------------------ kalip

    /** Kalip goruntusu (tahta boyutuna olceklenmis) ya da null. */
    private SkinImage template() {
        if (templateMode == 0) return null;
        int n = paint.size();
        if (customTemplate != null) {
            String key = "★" + System.identityHashCode(customTemplate) + "@" + n;
            if (!key.equals(templateKey)) {
                templateKey = key;
                templateScaled = customTemplate.resize(n);
            }
            return templateScaled;
        }
        String src = TEMPLATES[templateIndex].isEmpty() ? id : TEMPLATES[templateIndex];
        String key = src + "@" + n;
        if (key.equals(templateKey)) return templateScaled;
        templateKey = key;
        SkinImage raw = templates.computeIfAbsent(src, k -> {
            SkinImage t = SkinImport.template(k);
            return t == null ? SkinImage.blank(1) : t;
        });
        templateScaled = raw.w <= 1 ? null : raw.resize(n);
        return templateScaled;
    }

    // ------------------------------------------------------------------ fare / klavye

    private int pixelX(double mx) {
        return (int) Math.floor((mx - cx) / px);
    }

    private int pixelY(double my) {
        return (int) Math.floor((my - cy) / px);
    }

    private boolean onCanvas(double mx, double my) {
        return mx >= cx && my >= cy && mx < cx + cs && my < cy + cs;
    }

    /** Palet / son renkler: tiklanan renk. */
    private int swatchAt(double mx, double my) {
        int sy = swatchY(), st = swatchStep(), b = st - 1;
        for (int i = 0; i < PALETTE.length; i++) {
            int x = rightX + (i % 8) * st, y = sy + (i / 8) * st;
            if (mx >= x && mx < x + b && my >= y && my < y + b) return i;
        }
        int ry = recentY();
        for (int i = 0; i < recent.size(); i++) {
            int x = rightX + i * st;
            if (mx >= x && mx < x + b && my >= ry && my < ry + b) return 100 + i;
        }
        return -1;
    }

    /** Kare kucuk resimleri: gorunen ilk kare ve kac tane sigar. */
    private int thumbFirst() {
        int fit = Math.max(1, areaW / 20);
        int n = paint.count();
        if (n <= fit) return 0;
        return Math.max(0, Math.min(n - fit, paint.current() - fit / 2));
    }

    private int thumbAt(double mx, double my) {
        if (my < thumbY || my >= thumbY + 18) return -1;
        int i = (int) Math.floor((mx - areaX) / 20) + thumbFirst();
        return mx >= areaX && i < paint.count() && (int) Math.floor((mx - areaX) / 20) < Math.max(1, areaW / 20) ? i : -1;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (onCanvas(mx, my) && button <= 2) {
            setFocused(null);
            if (hexBox != null) hexBox.setFocused(false);
            stopPlay();
            int x = pixelX(mx), y = pixelY(my);
            if (button == 2 || (button == 0 && paint.tool == SkinPaint.Tool.PICK) || (button == 0 && hasAltDown())) {
                setColor(paint.image().get(x, y), true);
                return true;
            }
            int col = button == 1 ? 0 : color;
            paint.push();
            paint.beginStroke();
            drawing = true;
            drawButton = button;
            startPx = lastPx = x;
            startPy = lastPy = y;
            switch (paint.tool) {
                case PEN, ERASER -> paint.stroke(x, y, x, y, paint.tool == SkinPaint.Tool.ERASER ? 0 : col);
                case SHADE -> paint.shade(x, y, x, y, button == 1);
                case FILL -> {
                    paint.fill(x, y, col);
                    drawing = false;
                }
                case REPLACE -> {
                    int k = paint.replaceColor(x, y, col);
                    flash(k == 0 ? "§7Zaten bu renk" : "§a" + k + " piksel değişti");
                    drawing = false;
                }
                default -> {
                }
            }
            if (paint.tool != SkinPaint.Tool.SHADE) remember(col);
            touched(paint.current());
            return true;
        }
        int th = thumbAt(mx, my);
        if (th >= 0) {
            selectFrame(th);
            return true;
        }
        int sw = swatchAt(mx, my);
        if (sw >= 0) {
            setColor(sw >= 100 ? recent.get(sw - 100) : PALETTE[sw], true);
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (drawing) {
            int x = pixelX(mx), y = pixelY(my);
            if (x != lastPx || y != lastPy) {
                switch (paint.tool) {
                    case PEN, ERASER -> {
                        paint.stroke(lastPx, lastPy, x, y, paint.tool == SkinPaint.Tool.ERASER || drawButton == 1 ? 0 : color);
                        touched(paint.current());
                    }
                    case SHADE -> {
                        paint.shade(lastPx, lastPy, x, y, drawButton == 1);
                        touched(paint.current());
                    }
                    default -> {
                    }
                }
                lastPx = x;
                lastPy = y;
            }
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (drawing) {
            drawing = false;
            int col = drawButton == 1 ? 0 : color;
            if (paint.tool == SkinPaint.Tool.LINE) paint.stroke(startPx, startPy, lastPx, lastPy, col);
            else if (paint.tool == SkinPaint.Tool.RECT) paint.rect(startPx, startPy, lastPx, lastPy, col);
            touched(paint.current());
            return true;
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (hexBox != null && hexBox.isFocused()) return super.keyPressed(key, scan, mods);
        boolean ctrl = hasControlDown();
        if (ctrl && key == GLFW.GLFW_KEY_Z) {
            undo();
            return true;
        }
        if (ctrl && key == GLFW.GLFW_KEY_Y) {
            redo();
            return true;
        }
        if (ctrl && key == GLFW.GLFW_KEY_S) {
            save();
            return true;
        }
        int sx = key == GLFW.GLFW_KEY_LEFT ? -1 : (key == GLFW.GLFW_KEY_RIGHT ? 1 : 0), sy = key == GLFW.GLFW_KEY_UP ? -1 : (key == GLFW.GLFW_KEY_DOWN ? 1 : 0);
        if (sx != 0 || sy != 0) {
            op(() -> paint.shift(sx, sy), "§7Kaydırıldı");
            return true;
        }
        if (key == GLFW.GLFW_KEY_SPACE) {
            togglePlay();
            return true;
        }
        if ((key == GLFW.GLFW_KEY_COMMA || key == GLFW.GLFW_KEY_PERIOD) && hasShiftDown()) {
            frameOp(paint.moveFrame(key == GLFW.GLFW_KEY_COMMA ? -1 : 1), null);
            return true;
        }
        if (key == GLFW.GLFW_KEY_COMMA || key == GLFW.GLFW_KEY_PERIOD) {
            selectFrame(Math.floorMod(paint.current() + (key == GLFW.GLFW_KEY_COMMA ? -1 : 1), paint.count()));
            return true;
        }
        SkinPaint.Tool t = switch (key) {
            case GLFW.GLFW_KEY_B -> SkinPaint.Tool.PEN;
            case GLFW.GLFW_KEY_E -> SkinPaint.Tool.ERASER;
            case GLFW.GLFW_KEY_G -> SkinPaint.Tool.FILL;
            case GLFW.GLFW_KEY_I -> SkinPaint.Tool.PICK;
            case GLFW.GLFW_KEY_L -> SkinPaint.Tool.LINE;
            case GLFW.GLFW_KEY_R -> SkinPaint.Tool.RECT;
            case GLFW.GLFW_KEY_C -> SkinPaint.Tool.REPLACE;
            case GLFW.GLFW_KEY_D -> SkinPaint.Tool.SHADE;
            default -> null;
        };
        if (t != null) {
            paint.tool = t;
            rebuildWidgets();
            return true;
        }
        if (key == GLFW.GLFW_KEY_M) {
            paint.mirror = !paint.mirror;
            rebuildWidgets();
            return true;
        }
        if (key == GLFW.GLFW_KEY_T) {
            templateMode = (templateMode + 1) % 3;
            rebuildWidgets();
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    // ------------------------------------------------------------------ cizim

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        Theme.overlay(g, width, height);
        Theme.panel(g, leftX - 6, 30, leftX + SIDE_L + 6, height - 32);
        Theme.panel(g, rightX - 6, 30, rightX + SIDE_R + 4, height - 32);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        if (stack == null) stack = new ItemStack(item);
        g.drawCenteredString(font, Theme.title(Component.literal("Çiz: ").append(stack.getHoverName())), width / 2, 8, 0xFFFFFF);
        String help = HELP[HELP.length - 1];
        for (String h : HELP) if (font.width(h) <= width - 16) {
            help = h;
            break;
        }
        g.drawCenteredString(font, help, width / 2, 19, 0xFFFFFF);
        int n = paint.size();
        // oyundaki onizlemeyle ayni saat: tahta ve esya ayni karede
        int shown = playing ? paint.frameAt(Util.getMillis()) : paint.current();
        SkinImage img = paint.frame(shown);
        int[] lay = layer(n);
        SkinImage tpl = playing ? null : template();
        if (tpl == null) {
            // dama tahtasi (saydamlik): tek zemin + acik kareler
            g.fill(cx, cy, cx + cs, cy + cs, 0xFF2A2A2E);
            for (int i = 0; i < lay.length; i++) lay[i] = (((i % n) + (i / n)) & 1) == 0 ? 0 : 0xFF38383E;
        } else {
            // kalip varken dama sadece kalibin doldurdugu piksellerde; disi seffaf: kalibin bicimi net gorunur
            for (int i = 0; i < lay.length; i++)
                lay[i] = (tpl.argb[i] >>> 24) < 26 ? 0 : ((((i % n) + (i / n)) & 1) == 0 ? 0xFF2A2A2E : 0xFF38383E);
        }
        drawLayer(g, lay, n);
        // kalip
        if (tpl != null) {
            int al = TEMPLATE_ALPHA[templateAlpha] * 255 / 100, edge = (Math.min(255, al * 2) << 24) | (Theme.accent() & 0xFFFFFF);
            for (int y = 0; y < n; y++)
                for (int x = 0; x < n; x++) {
                    int c = 0;
                    if (tpl.opaque(x, y)) {
                        if (templateMode == 1) c = (al << 24) | (tpl.get(x, y) & 0xFFFFFF);
                        else if (!tpl.opaque(x - 1, y) || !tpl.opaque(x + 1, y) || !tpl.opaque(x, y - 1) || !tpl.opaque(x, y + 1)) c = edge;
                    }
                    lay[y * n + x] = c;
                }
            drawLayer(g, lay, n);
        }
        // sogan kabugu: onceki kare (su anki karenin bos yerlerinde soluk)
        if (!playing && paint.onion && paint.count() > 1) {
            SkinImage prev = paint.frame(Math.floorMod(paint.current() - 1, paint.count()));
            for (int i = 0; i < lay.length; i++) {
                int p = prev.argb[i];
                lay[i] = (p >>> 24) < 26 || (img.argb[i] >>> 24) >= 26 ? 0 : 0x50000000 | (p & 0xFFFFFF);
            }
            drawLayer(g, lay, n);
        }
        System.arraycopy(img.argb, 0, lay, 0, lay.length);
        drawLayer(g, lay, n);
        // cizgi / kutu onizlemesi
        if (drawing && (paint.tool == SkinPaint.Tool.LINE || paint.tool == SkinPaint.Tool.RECT)) {
            int col = drawButton == 1 ? 0xA0FF4040 : (0xA0000000 | (color & 0xFFFFFF));
            for (int[] q : paint.preview(startPx, startPy, lastPx, lastPy)) {
                if (q[0] < 0 || q[1] < 0 || q[0] >= n || q[1] >= n) continue;
                int x0 = cx + q[0] * px, y0 = cy + q[1] * px;
                g.fill(x0, y0, x0 + px, y0 + px, col);
            }
        }
        if (paint.grid && px >= 4) {
            for (int i = 0; i <= n; i++) {
                int a = (i % 8 == 0) ? 0x60FFFFFF : 0x22FFFFFF;
                g.fill(cx + i * px, cy, cx + i * px + 1, cy + cs, a);
                g.fill(cx, cy + i * px, cx + cs, cy + i * px + 1, a);
            }
        }
        if (paint.mirror) g.fill(cx + cs / 2, cy - 3, cx + cs / 2 + 1, cy + cs + 3, 0xC0000000 | Theme.accent());
        // sadece cerceve: dolgu cizimin renklerini boyamasin
        g.renderOutline(cx - 2, cy - 2, cs + 4, cs + 4, 0xFF000000 | Theme.accent());
        if (onCanvas(mouseX, mouseY) && !playing) {
            int hx = pixelX(mouseX), hy = pixelY(mouseY);
            boolean brushTool = paint.tool == SkinPaint.Tool.PEN || paint.tool == SkinPaint.Tool.ERASER || paint.tool == SkinPaint.Tool.SHADE;
            int bs = brushTool ? paint.brush : 1;
            int o = (bs - 1) / 2;
            g.renderOutline(cx + (hx - o) * px, cy + (hy - o) * px, px * bs, px * bs, 0xFFFFFFFF);
            g.drawString(font, "§7" + hx + ", " + hy, cx + cs + 2 - font.width(hx + ", " + hy), cy - 11, 0xFFFFFF);
        }
        renderFrames(g, mouseX, mouseY, shown);
        renderColors(g);
        renderPreview(g);
        if (!message.isEmpty() && System.currentTimeMillis() - messageAt < 4000) g.drawCenteredString(font, message, width / 2, height - 38, 0xFFFFFF);
        else if (changed) g.drawCenteredString(font, "§e● Kaydedilmedi", width / 2, height - 38, 0xFFFFFF);
    }

    private int[] layerBuf = new int[0];

    private int[] layer(int n) {
        if (layerBuf.length != n * n) layerBuf = new int[n * n];
        return layerBuf;
    }

    /** Katmani satir satir ayni renkli parcalar halinde cizer (64x64'te bile az dortgen). 0 = bos. */
    private void drawLayer(GuiGraphics g, int[] lay, int n) {
        for (int y = 0; y < n; y++) {
            int row = y * n, y0 = cy + y * px;
            for (int x = 0; x < n; ) {
                int c = lay[row + x];
                int e = x + 1;
                while (e < n && lay[row + e] == c) e++;
                if ((c >>> 24) != 0) g.fill(cx + x * px, y0, cx + e * px, y0 + px, c);
                x = e;
            }
        }
    }

    private void renderFrames(GuiGraphics g, int mouseX, int mouseY, int shown) {
        int first = thumbFirst(), fit = Math.max(1, areaW / 20);
        int n = paint.size();
        for (int k = 0; thumbY >= 0 && k < fit && first + k < paint.count(); k++) {
            int f = first + k;
            int x = areaX + k * 20, y = thumbY;
            boolean sel = f == paint.current();
            g.fill(x, y, x + 18, y + 18, sel ? 0xFF000000 | Theme.accent() : (f == shown && playing ? 0xFF55FF55 : 0xFF202024));
            g.fill(x + 1, y + 1, x + 17, y + 17, 0xFF2E2E34);
            SkinImage im = paint.frame(f);
            // 16x16 kucuk resim (buyuk cizimde orneklenir), ayni renkli yan yana pikseller tek dortgen
            for (int yy = 0; yy < 16; yy++) {
                int sy = yy * n / 16;
                for (int xx = 0; xx < 16; ) {
                    int p = thumbPx(im, xx, sy, n);
                    int e = xx + 1;
                    while (e < 16 && thumbPx(im, e, sy, n) == p) e++;
                    if (p != 0) g.fill(x + 1 + xx, y + 1 + yy, x + 1 + e, y + 2 + yy, p);
                    xx = e;
                }
            }
        }
        String info = (playing ? "§a▶ " + (shown + 1) : "§7Kare " + (paint.current() + 1)) + "§7/" + paint.count()
                + (paint.count() > 1 ? " · " + paint.totalMs() + " ms" : "");
        g.drawString(font, info, cx - 2, cy - 11, 0xFFFFFF);
        int th = thumbAt(mouseX, mouseY);
        if (th >= 0) g.renderTooltip(font, Component.literal("Kare " + (th + 1) + " · " + paint.ms(th) + " ms"), mouseX, mouseY);
    }

    private static int thumbPx(SkinImage im, int xx, int sy, int n) {
        int p = im.argb[sy * n + xx * n / 16];
        return (p >>> 24) >= 26 ? 0xFF000000 | p : 0;
    }

    private void renderColors(GuiGraphics g) {
        g.fill(rightX, 38, rightX + 20, 58, 0xFF000000);
        g.fill(rightX + 1, 39, rightX + 19, 57, 0xFF808080);
        g.fill(rightX + 1, 39, rightX + 19, 57, color);
        int sy = swatchY(), st = swatchStep(), b = st - 1;
        for (int i = 0; i < PALETTE.length; i++) {
            int x = rightX + (i % 8) * st, y = sy + (i / 8) * st;
            g.fill(x, y, x + b, y + b, 0xFF000000);
            if ((PALETTE[i] >>> 24) == 0) {
                g.fill(x + 1, y + 1, x + b - 1, y + b - 1, 0xFFDDDDDD);
                for (int k = 1; k < b - 1; k++) g.fill(x + k, y + b - 1 - k, x + k + 1, y + b - k, 0xFFD03030);
            } else g.fill(x + 1, y + 1, x + b - 1, y + b - 1, PALETTE[i]);
            if (PALETTE[i] == color) g.renderOutline(x - 1, y - 1, b + 2, b + 2, 0xFFFFFFFF);
        }
        int ry = recentY();
        if (!compactH()) g.drawString(font, "§7Son renkler", rightX, ry - 10, 0xFFFFFF);
        for (int i = 0; i < recent.size(); i++) {
            int x = rightX + i * st;
            g.fill(x, ry, x + b, ry + b, 0xFF000000);
            g.fill(x + 1, ry + 1, x + b - 1, ry + b - 1, recent.get(i));
        }
    }

    private void renderPreview(GuiGraphics g) {
        int py = previewY();
        if (!compactH()) {
            g.drawString(font, "§7Önizleme" + (playing ? " §a▶" : ""), rightX, py, 0xFFFFFF);
            py += 12;
        }
        if (stack == null) stack = new ItemStack(item);
        ItemStack st = stack;
        int room = height - 32 - py;
        if (room < 16) return;
        g.renderItem(st, rightX, py);
        if (room >= 32) {
            g.pose().pushPose();
            g.pose().translate(rightX + 22, py, 0);
            g.pose().scale(2f, 2f, 1f);
            g.renderItem(st, 0, 0);
            g.pose().popPose();
        }
        if (room >= 64) {
            g.pose().pushPose();
            g.pose().translate(rightX + 60, py, 0);
            g.pose().scale(4f, 4f, 1f);
            g.renderItem(st, 0, 0);
            g.pose().popPose();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
