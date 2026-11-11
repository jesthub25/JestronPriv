package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.cosmetic.LocalLook;
import com.jestron.jestronpriv.client.tag.CustomIcons;
import com.jestron.jestronpriv.client.tag.EmojiAtlas;
import com.jestron.jestronpriv.client.tag.TagRenderer;
import com.jestron.jestronpriv.tag.TagFonts;
import com.jestron.jestronpriv.tag.TagSettings;
import com.jestron.jestronpriv.tag.TagStyle;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Isim etiketi: F5 / serbest kamerada ustunde gorunen isim. Metin, 50 font, 16 renk modu, hareket, stil, emoji ve
 * kendi gorsellerin (PNG / JPG / GIF). Ustte canli onizleme.
 */
public final class NametagScreen extends JpScreen {

    private static final int PREVIEW_H = 50;
    public static final int MAX_EMOJI = 6;

    public NametagScreen(Screen parent) {
        super(Component.literal("İsim Etiketi"), parent);
    }

    @Override
    protected int topOffset() {
        return super.topOffset() + PREVIEW_H;
    }

    @Override
    protected String subtitle() {
        ModConfig c = cfg();
        String a = c.tagPrefixImage.isEmpty() ? "" : "§7Ön görsel: " + statusColor(CustomIcons.status(c.tagPrefixImage));
        String b = c.tagSuffixImage.isEmpty() ? "" : "§7 · Son görsel: " + statusColor(CustomIcons.status(c.tagSuffixImage));
        if (!a.isEmpty() || !b.isEmpty()) return a + b;
        return "§7F5 ve serbest kamerada üstünde görünür · görsel dosyasını pencereye sürükle-bırak";
    }

    private static String statusColor(String s) {
        if (s.startsWith("Hazır")) return "§a" + s;
        if (s.startsWith("Yükleniyor")) return "§e" + s;
        return "§c" + s;
    }

    @Override
    protected Runnable resetAction() {
        return () -> cfg().resetTag();
    }

    @Override
    protected void onChanged() {
        LocalLook.refresh();
    }

    @Override
    public void onFilesDrop(List<Path> paths) {
        try {
            List<String> got = CustomIcons.importFiles(paths);
            if (got.isEmpty()) return;
            ModConfig c = cfg();
            if (c.tagPrefixImage.isEmpty() || !c.tagSuffixImage.isEmpty()) c.tagPrefixImage = got.get(got.size() - 1);
            else c.tagSuffixImage = got.get(got.size() - 1);
            markDirty();
            rebuildWidgets();
        } catch (Throwable ignored) {
        }
    }

    private static String font(ModConfig c) {
        return "§d" + TagFonts.get(c.tagFont).name();
    }

    private static String nextImage(String cur, boolean back) {
        List<String> fs = CustomIcons.files();
        if (fs.isEmpty()) return "";
        int i = fs.indexOf(cur);
        int n = fs.size() + 1;   // +1: "yok"
        int pos = i < 0 ? 0 : i + 1;
        pos = Math.floorMod(pos + (back ? -1 : 1), n);
        return pos == 0 ? "" : fs.get(pos - 1);
    }

    private static String shortName(String n) {
        if (n == null || n.isEmpty()) return "§7yok";
        return n.length() > 16 ? n.substring(0, 14) + "…" : n;
    }

    @Override
    protected void buildOptions() {
        ModConfig c = cfg();
        header("İsim");
        toggle("Etiket", "F5 / serbest kamerada üstünde ismin görünsün.", () -> c.tagEnabled, v -> c.tagEnabled = v);
        cycle("Font", "Tıkla: sonraki · Shift+tık: önceki.\n50 font, hepsi Türkçe karakterli.", () -> font(c), () -> {
            int i = TagFonts.indexOf(c.tagFont), n = TagFonts.ALL.size();
            c.tagFont = TagFonts.ALL.get(Math.floorMod(i + (hasShiftDown() ? -1 : 1), n)).id();
        });
        editBox("Özel isim (boş = gerçek adın)", "Üstünde yazacak isim. Emoji de yazabilirsin.\nBoş bırakırsan oyuncu adın yazar.", true,
                TagSettings.MAX_TEXT_CP * 2, () -> c.tagText, v -> c.tagText = v);
        header("Renk & Hareket");
        cycle("Renk modu", "Tek renk, gradyan, gökkuşağı, ateş, buz, neon, altın...", () -> "§d" + TagStyle.COLOR_MODES[c.tagColorMode],
                () -> c.tagColorMode = Math.floorMod(c.tagColorMode + (hasShiftDown() ? -1 : 1), TagStyle.modeCount()));
        cycle("Hareket", "Harflerin hareketi.", () -> "§d" + TagStyle.ANIMS[c.tagAnim],
                () -> c.tagAnim = Math.floorMod(c.tagAnim + (hasShiftDown() ? -1 : 1), TagStyle.ANIMS.length));
        color("Renk 1", "Tek renk / gradyan başlangıcı / nabız.", () -> c.tagColor1, v -> c.tagColor1 = v);
        hex("#RRGGBB (1)", v -> c.tagColor1 = v);
        color("Renk 2", "Gradyan ortası / sonu, nabız ikinci renk.", () -> c.tagColor2, v -> c.tagColor2 = v);
        hex("#RRGGBB (2)", v -> c.tagColor2 = v);
        color("Renk 3", "3 renkli gradyanın sonu.", () -> c.tagColor3, v -> c.tagColor3 = v);
        hex("#RRGGBB (3)", v -> c.tagColor3 = v);
        slider("Hız", "Akan renklerin ve hareketin hızı.", 0.2, 3, 0.05, () -> c.tagAnimSpeed, v -> c.tagAnimSpeed = (float) v,
                v -> String.format(Locale.ROOT, "%.2fx", v));
        slider("Boyut", "Etiket boyutu.", 0.5, 2.5, 0.05, () -> c.tagScale, v -> c.tagScale = (float) v, v -> String.format(Locale.ROOT, "%.2fx", v));
        header("Stil");
        toggle("Kalın", null, () -> c.tagBold, v -> c.tagBold = v);
        toggle("İtalik", null, () -> c.tagItalic, v -> c.tagItalic = v);
        toggle("Altı çizili", null, () -> c.tagUnderline, v -> c.tagUnderline = v);
        toggle("Üstü çizili", null, () -> c.tagStrike, v -> c.tagStrike = v);
        toggle("Gölge", "Harflerin arkasında gölge.", () -> c.tagShadow, v -> c.tagShadow = v);
        toggle("Kontur", "Harflerin çevresinde renkli çerçeve (çok okunaklı).", () -> c.tagOutline, v -> c.tagOutline = v);
        color("Kontur rengi", null, () -> c.tagOutlineColor, v -> c.tagOutlineColor = v);
        toggle("Parlak", "Karanlıkta da tam parlak görünür.", () -> c.tagGlow, v -> c.tagGlow = v);
        slider("Arka plan", "Arka plan kutusunun koyuluğu. 0 = yok.", 0, 0.9, 0.05, () -> c.tagBgAlpha, v -> c.tagBgAlpha = (float) v, JpScreen::pct);
        color("Arka plan rengi", null, () -> c.tagBgColor, v -> c.tagBgColor = v);
        slider("Yükseklik", "Etiketi yukarı / aşağı kaydır (blok).", -0.5, 1.5, 0.05, () -> c.tagHeight, v -> c.tagHeight = (float) v,
                v -> String.format(Locale.ROOT, "%+.2f", v));
        header("Emoji & Görsel");
        action("§e＋ Başa emoji ekle", "Telefondaki gibi tüm emojiler: yüzler, insanlar, hayvanlar, yiyecek, seyahat,\netkinlik, nesneler, semboller, bayraklar. En çok "
                + MAX_EMOJI + " emoji.", false, () -> minecraft.setScreen(new EmojiPickerScreen(this, "Başa Emoji", () -> c.tagPrefix, MAX_EMOJI, e -> {
                    c.tagPrefix = c.tagPrefix + e;
                    markDirty();
                }, () -> {
                    c.tagPrefix = EmojiAtlas.index().removeLast(c.tagPrefix);
                    markDirty();
                })));
        action("§e＋ Sona emoji ekle", "İsmin sonuna emoji. En çok " + MAX_EMOJI + " emoji.", false,
                () -> minecraft.setScreen(new EmojiPickerScreen(this, "Sona Emoji", () -> c.tagSuffix, MAX_EMOJI, e -> {
                    c.tagSuffix = c.tagSuffix + e;
                    markDirty();
                }, () -> {
                    c.tagSuffix = EmojiAtlas.index().removeLast(c.tagSuffix);
                    markDirty();
                })));
        action("⌫ Baştaki son emojiyi sil", null, false, () -> {
            c.tagPrefix = EmojiAtlas.index().removeLast(c.tagPrefix);
            markDirty();
        });
        action("⌫ Sondaki son emojiyi sil", null, false, () -> {
            c.tagSuffix = EmojiAtlas.index().removeLast(c.tagSuffix);
            markDirty();
        });
        cycle("Ön görsel", "config/jestronpriv/icons klasöründeki PNG / JPG / GIF (hareketli) görseller.\nTıkla: sonraki · Shift+tık: önceki.",
                () -> shortName(c.tagPrefixImage), () -> c.tagPrefixImage = nextImage(c.tagPrefixImage, hasShiftDown()));
        cycle("Son görsel", "İsmin sonuna görsel.", () -> shortName(c.tagSuffixImage), () -> c.tagSuffixImage = nextImage(c.tagSuffixImage, hasShiftDown()));
        action("§b❐ Görsel klasörünü aç", "Görsellerini bu klasöre at (ya da bu pencereye sürükle-bırak).", false, () -> {
            try {
                net.minecraft.Util.getPlatform().openPath(CustomIcons.ensureFolder());
            } catch (Throwable ignored) {
            }
        });
        action("⟳ Görselleri yenile", "Klasörü yeniden tarar ve görselleri baştan yükler.", false, () -> {
            CustomIcons.clearAll();
            rebuildWidgets();
        });
        cycle("Emoji hareketi", "Emoji ve görsellerin hareketi.", () -> "§d" + TagStyle.EMOJI_ANIMS[c.tagEmojiAnim],
                () -> c.tagEmojiAnim = Math.floorMod(c.tagEmojiAnim + (hasShiftDown() ? -1 : 1), TagStyle.EMOJI_ANIMS.length));
        slider("Emoji boyutu", "Emoji ve görsel boyutu.", 0.6, 2, 0.05, () -> c.tagEmojiScale, v -> c.tagEmojiScale = (float) v,
                v -> String.format(Locale.ROOT, "%.2fx", v));
        header("Ne zaman görünsün");
        toggle("F5 (arka görünüm)", "Normal 3. şahıs görünümde.", () -> c.tagShowF5, v -> c.tagShowF5 = v);
        toggle("Serbest kamerada", "Serbest 3. şahıs kamerada.", () -> c.tagShowFreecam, v -> c.tagShowFreecam = v);
        toggle("Gerçek adım altta", "Özel isim yazınca gerçek adın altta küçük görünsün.", () -> c.tagShowRealName, v -> c.tagShowRealName = v);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        try {
            renderPreview(g);
        } catch (Throwable ignored) {
            // onizleme asla ekrani bozmasin
        }
    }

    private void renderPreview(GuiGraphics g) {
        int top = super.topOffset() - 4, bottom = top + PREVIEW_H - 2;
        int x0 = width / 2 - 170, x1 = width / 2 + 170;
        g.fill(x0, top, x1, bottom, 0x70000000);
        g.renderOutline(x0, top, x1 - x0, bottom - top, 0x60B070FF);
        ModConfig c = cfg();
        TagSettings tag = LocalLook.tag();
        if (tag == null) {
            g.drawCenteredString(font, "§7Etiket kapalı", width / 2, (top + bottom) / 2 - 4, 0xFFFFFF);
            return;
        }
        String name = minecraft != null && minecraft.player != null ? minecraft.player.getGameProfile().getName() : "Oyuncu";
        var pre = CustomIcons.texture(c.tagPrefixImage);
        var suf = CustomIcons.texture(c.tagSuffixImage);
        float w = TagRenderer.width(tag, tag.text.isEmpty() ? name : tag.text, pre, suf);
        float k = Math.max(0.6f, Math.min(2.4f, Math.min((x1 - x0 - 16) / Math.max(1f, w + 4), (PREVIEW_H - 14) / 14f)));
        float cy = (top + bottom) / 2f - 4f * k;
        g.drawSpecial(buffers -> {
            PoseStack ps = g.pose();
            ps.pushPose();
            try {
                ps.translate(width / 2f, cy, 100f);
                ps.scale(k, k, k);
                TagRenderer.draw(ps, buffers, LightTexture.FULL_BRIGHT, tag, name, pre, suf, false, c.tagShowRealName, System.currentTimeMillis());
            } finally {
                ps.popPose();
            }
        });
    }
}
