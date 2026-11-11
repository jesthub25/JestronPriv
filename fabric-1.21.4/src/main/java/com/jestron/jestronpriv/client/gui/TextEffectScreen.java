package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.StyledText;
import com.jestron.jestronpriv.fx.CustomText;
import com.jestron.jestronpriv.fx.KillEffect;
import com.jestron.jestronpriv.fx.api.TextStyle;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;

/** Ozel Yazi efektinin duzenleyicisi: canli onizlemeli. */
public final class TextEffectScreen extends JpScreen {

    private static final String[] COLOR_NAMES = {"Beyaz", "Kırmızı", "Koyu Kırmızı", "Turuncu", "Altın", "Sarı", "Yeşil", "Zümrüt",
            "Camgöbeği", "Gökyüzü", "Mavi", "Lacivert", "Mor", "Pembe", "Gri", "Siyah"};
    private static final int[] COLORS = {0xFFFFFF, 0xFF4040, 0xAA0000, 0xFFA500, 0xFFD700, 0xFFFF55, 0x55FF55, 0x00C878,
            0x55FFFF, 0x87CEEB, 0x5599FF, 0x2040C0, 0xAA55FF, 0xFF77CC, 0xAAAAAA, 0x202020};
    private static final String[] BG_NAMES = {"Siyah", "Lacivert", "Bordo", "Mor", "Koyu Yeşil", "Altın", "Beyaz", "Kırmızı"};
    private static final int[] BGS = {0x000000, 0x0A1440, 0x4A0010, 0x3A0A5A, 0x0A3A1A, 0x8A6A00, 0xFFFFFF, 0xB01010};
    private static final String[] MODES = {"Tek Renk", "Gradyan", "Gökkuşağı"};
    private static final String[] ANIMS = {"Patlayarak (Pop)", "Yükselerek", "Daktilo", "Nabız", "Titreyerek", "Çarparak (Zoom)"};
    private static final String[] SOUNDS = {"Sessiz", "Ding", "Seviye Atlama", "Zafer", "Gong", "Havai Fişek"};

    private net.minecraft.client.gui.components.EditBox hex1, hex2;

    public TextEffectScreen(Screen parent) {
        super(Component.literal("Özel Yazı Efekti"), parent);
    }

    @Override
    protected int topOffset() {
        return 86;
    }

    private static int idx(int[] arr, int v) {
        for (int i = 0; i < arr.length; i++) if (arr[i] == v) return i;
        return -1;
    }

    private static String colorName(int v) {
        int i = idx(COLORS, v);
        return i >= 0 ? COLOR_NAMES[i] : String.format(Locale.ROOT, "#%06X", v);
    }

    private static int nextColor(int v) {
        int i = idx(COLORS, v);
        return COLORS[(i + 1) % COLORS.length];
    }

    private static String bgName(int v) {
        int i = idx(BGS, v);
        return i >= 0 ? BG_NAMES[i] : String.format(Locale.ROOT, "#%06X", v);
    }

    private static Integer parseHex(String s) {
        if (s == null) return null;
        String h = s.trim().replace("#", "");
        if (!h.matches("[0-9a-fA-F]{6}")) return null;
        return Integer.parseInt(h, 16);
    }

    @Override
    protected void buildOptions() {
        ModConfig c = cfg();
        editBox("Yazı: {kurban} öldü! — {ben}", "Öldürünce çıkacak yazı.\n§f{kurban}§7 = öldürdüğün oyuncu, §f{ben}§7 = sen\n§f&c, &l§7 gibi renk kodları çalışır\n§f|§7 = alt satıra geç",
                true, 96, () -> c.ctText, v -> c.ctText = v);
        cycle("Yazı tipi", "Minecraft'ın yazı tipleri.", () -> StyledText.FONT_NAMES[c.ctFont], () -> c.ctFont = (c.ctFont + 1) % 4);
        cycle("Renk modu", "§fTek Renk§7: Renk 1\n§fGradyan§7: Renk 1'den Renk 2'ye geçiş\n§fGökkuşağı§7: hareketli gökkuşağı",
                () -> MODES[c.ctColorMode], () -> c.ctColorMode = (c.ctColorMode + 1) % 3);
        cycle("Renk 1", "Tıklayarak hazır renkler arasında geç ya da aşağıya #RRGGBB yaz.", () -> colorName(c.ctColor1), () -> {
            c.ctColor1 = nextColor(c.ctColor1);
            if (hex1 != null) hex1.setValue("");
        });
        cycle("Renk 2", "Gradyan modunda ikinci renk.", () -> colorName(c.ctColor2), () -> {
            c.ctColor2 = nextColor(c.ctColor2);
            if (hex2 != null) hex2.setValue("");
        });
        hex1 = editBox("#RRGGBB (Renk 1)", "Özel renk kodu, ör. #FF00AA", false, 7, () -> "", v -> {
            Integer h = parseHex(v);
            if (h != null) c.ctColor1 = h;
        });
        hex2 = editBox("#RRGGBB (Renk 2)", "Özel renk kodu, ör. #00FFAA", false, 7, () -> "", v -> {
            Integer h = parseHex(v);
            if (h != null) c.ctColor2 = h;
        });
        toggle("Arka plan", "Yazının arkasında renkli kutu.", () -> c.ctBgOn, v -> c.ctBgOn = v);
        cycle("Arka plan rengi", "Kutunun rengi.", () -> bgName(c.ctBgColor), () -> {
            int i = idx(BGS, c.ctBgColor);
            c.ctBgColor = BGS[(i + 1) % BGS.length];
        });
        slider("Arka plan opaklığı", "Kutunun saydamlığı.", 0.05, 1.0, 0.05, () -> c.ctBgAlpha, v -> c.ctBgAlpha = (float) v, JpScreen::pct);
        toggle("Kalın", null, () -> c.ctBold, v -> c.ctBold = v);
        toggle("İtalik", null, () -> c.ctItalic, v -> c.ctItalic = v);
        toggle("Altı çizili", null, () -> c.ctUnderline, v -> c.ctUnderline = v);
        toggle("Üstü çizili", null, () -> c.ctStrike, v -> c.ctStrike = v);
        toggle("Gölge", "Yazının altında gölge (kontur açıkken kullanılmaz).", () -> c.ctShadow, v -> c.ctShadow = v);
        toggle("Kontur", "Parlayan tabela yazısı gibi kalın dış çizgi.", () -> c.ctOutline, v -> c.ctOutline = v);
        cycle("Kontur rengi", null, () -> colorName(c.ctOutlineColor), () -> c.ctOutlineColor = nextColor(c.ctOutlineColor));
        toggle("Parlak (karanlıkta)", "Açıkken yazı karanlıkta da tam parlak görünür.", () -> c.ctGlow, v -> c.ctGlow = v);
        slider("Boyut", "Yazının büyüklüğü.", 0.5, 3.0, 0.05, () -> c.ctSize, v -> c.ctSize = (float) v,
                v -> String.format(Locale.ROOT, "x%.2f", v));
        slider("Yükseklik", "Ölüm noktasının kaç blok üstünde çıkacağı.", 1.0, 4.0, 0.1, () -> c.ctHeight, v -> c.ctHeight = (float) v,
                v -> String.format(Locale.ROOT, "%.1f blok", v));
        cycle("Animasyon", null, () -> ANIMS[c.ctAnim().ordinal()], () -> {
            CustomText.Anim[] a = CustomText.Anim.values();
            c.ctAnim = a[(c.ctAnim().ordinal() + 1) % a.length].name();
        });
        cycle("Ses", null, () -> SOUNDS[c.ctSound().ordinal()], () -> {
            CustomText.Sfx[] a = CustomText.Sfx.values();
            c.ctSound = a[(c.ctSound().ordinal() + 1) % a.length].name();
        });
        toggle("Parçacık patlaması", "Yazı çıkarken renginde parçacıklar saçılır.", () -> c.ctParticles, v -> c.ctParticles = v);
        action("✔ Bu efekti seç", "Öldürme efekti olarak Özel Yazı'yı seçer.", false, () -> {
            c.effect = "ozel_yazi";
            markDirty();
        });
        action("▶ Önünde önizle", "Yazıyı önünde dener.", false, () -> {
            KillEffect fx = mod.engine().registry().get("ozel_yazi");
            if (fx != null && minecraft != null) {
                minecraft.setScreen(null);
                mod.preview(fx);
            }
        });
    }

    @Override
    protected Runnable resetAction() {
        return () -> cfg().resetCustomText();
    }

    @Override
    protected String subtitle() {
        boolean on = "ozel_yazi".equals(cfg().effect);
        return on ? "§aŞu an seçili efekt: Özel Yazı" : "§eSeçili değil — aşağıdan \"Bu efekti seç\"";
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        ModConfig c = cfg();
        TextStyle st = c.textStyle();
        String me = minecraft != null && minecraft.player != null ? minecraft.player.getGameProfile().getName() : "Sen";
        CustomText.setSelfName(me);
        String[] lines = CustomText.resolve("Kurban").split("\n");
        float time = (System.currentTimeMillis() % 1_000_000L) / 50f;
        float scale = 1.6f;
        int lineH = (int) ((font.lineHeight + 2) * scale);
        int y0 = 38 + Math.max(0, (44 - lines.length * lineH) / 2);
        g.fill(width / 2 - 160, 35, width / 2 + 160, 81, 0x55000000);
        g.renderOutline(width / 2 - 160, 35, 320, 46, 0x40B070FF);
        for (int li = 0; li < lines.length && li < 3; li++) {
            MutableComponent comp = StyledText.line(lines[li], st, time);
            int w = font.width(comp);
            g.pose().pushPose();
            g.pose().translate(width / 2f, y0 + li * lineH, 0);
            g.pose().scale(scale, scale, 1f);
            int x = -w / 2;
            if (st.background() != 0) g.fill(x - 2, -2, x + w + 2, font.lineHeight + 1, st.background());
            if (st.outline()) {
                MutableComponent o = Component.literal(StyledText.strip(lines[li])).withStyle(
                        StyledText.plainStyle(st).withColor(st.outlineColor()));
                for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) if (dx != 0 || dy != 0) g.drawString(font, o, x + dx, dy, 0xFFFFFFFF, false);
            }
            int col = st.colorMode() == 0 ? (0xFF000000 | st.color1()) : 0xFFFFFFFF;
            g.drawString(font, comp, x, 0, col, st.shadow() && !st.outline());
            g.pose().popPose();
        }
    }
}
