package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.ModConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/** Blok secim kutusu ve FOV etkileri. */
public final class OutlineFovScreen extends JpScreen {

    public OutlineFovScreen(Screen parent) {
        super(Component.literal("Seçim Kutusu & FOV"), parent);
    }

    @Override
    protected String subtitle() {
        return "§7Bir bloğa bak: değişiklikler canlı";
    }

    @Override
    protected void buildOptions() {
        ModConfig c = cfg();
        header("Blok Seçim Kutusu");
        toggle("Özel seçim kutusu", "Kapalı: vanilla siyah çizgi.", () -> c.boEnabled, v -> c.boEnabled = v);
        color("Çizgi rengi", null, () -> c.boColor, v -> c.boColor = v);
        hex("#RRGGBB (çizgi)", v -> c.boColor = v);
        toggle("Gökkuşağı", "Renkler kenarlar boyunca akar.", () -> c.boRainbow, v -> c.boRainbow = v);
        toggle("Nabız", "Hafifçe atar (parlaklık ve kalınlık).", () -> c.boPulse, v -> c.boPulse = v);
        slider("Kalınlık", "Piksel (1080p'de); yüksek çözünürlükte orantılı büyür.", 1, 10, 0.5, () -> c.boWidth, v -> c.boWidth = (float) v,
                v -> String.format(Locale.ROOT, "%.1f px", v));
        slider("Opaklık", null, 0.1, 1, 0.05, () -> c.boAlpha, v -> c.boAlpha = (float) v, JpScreen::pct);
        cycle("Dolgu", "§fYok · §fBakılan yüz§7: sadece nişangahın olduğu yüz · §fTüm blok", () -> switch (c.boFill) {
            case "YOK" -> "§dYok";
            case "TUM" -> "§dTüm blok";
            default -> "§dBakılan yüz";
        }, () -> c.boFill = switch (c.boFill) {
            case "YUZ" -> "TUM";
            case "TUM" -> "YOK";
            default -> "YUZ";
        });
        toggle("Dolgu çizgiyle aynı renk", null, () -> c.boFillSame, v -> c.boFillSame = v);
        color("Dolgu rengi", null, () -> c.boFillColor, v -> {
            c.boFillColor = v;
            c.boFillSame = false;
        });
        slider("Dolgu opaklığı", null, 0.03, 0.6, 0.01, () -> c.boFillAlpha, v -> c.boFillAlpha = (float) v, JpScreen::pct);
        toggle("Kırma ilerlemesi", "Kazarken dolgu ortadan büyür, renk kırmızıdan yeşile döner.", () -> c.boMining, v -> c.boMining = v);

        header("FOV Etkileri");
        toggle("Özel FOV etkileri", "Açıkken aşağıdakiler vanilla \"FOV Etkileri\" ayarının yerine geçer.\n100% = vanilla, 0% = hiç değişmez.",
                () -> c.fovCustom, v -> c.fovCustom = v);
        slider("Koşma", "Koşarken genişleme.", 0, 1.5, 0.05, () -> c.fovSprint, v -> c.fovSprint = (float) v, JpScreen::pct);
        slider("Hız / yavaşlık", "Hız iksiri, yavaşlık, ruh hızı vb.", 0, 1.5, 0.05, () -> c.fovSpeed, v -> c.fovSpeed = (float) v, JpScreen::pct);
        slider("Uçma", "Yaratıcı / izleyici uçuşu.", 0, 1.5, 0.05, () -> c.fovFly, v -> c.fovFly = (float) v, JpScreen::pct);
        slider("Yay germe", "Yay çekerken yakınlaşma.", 0, 1.5, 0.05, () -> c.fovBow, v -> c.fovBow = (float) v, JpScreen::pct);
        slider("Su / lav altı", "Sıvı içinde daralma.", 0, 1, 0.05, () -> c.fovWater, v -> c.fovWater = (float) v, JpScreen::pct);
        cycle("Geçiş", "FOV değişiminin hızı.", () -> switch (c.fovSmooth) {
            case "ANINDA" -> "§dAnında";
            case "YUMUSAK" -> "§dYumuşak";
            case "COK_YUMUSAK" -> "§dÇok yumuşak";
            default -> "§dNormal";
        }, () -> c.fovSmooth = switch (c.fovSmooth) {
            case "NORMAL" -> "YUMUSAK";
            case "YUMUSAK" -> "COK_YUMUSAK";
            case "COK_YUMUSAK" -> "ANINDA";
            default -> "NORMAL";
        });
        action("§ePvP ön ayarı", "Koşma 0%, hız 0%, uçma 0%, yay 100%, su 0%, geçiş anında.", false, () -> {
            c.fovCustom = true;
            c.fovSprint = c.fovSpeed = c.fovFly = c.fovWater = 0f;
            c.fovBow = 1f;
            c.fovSmooth = "ANINDA";
            rebuildWidgets();
        });
        action("§eSabit FOV", "Hiçbir etki FOV'u değiştirmez.", false, () -> {
            c.fovCustom = true;
            c.fovSprint = c.fovSpeed = c.fovFly = c.fovBow = c.fovWater = 0f;
            rebuildWidgets();
        });
    }

    @Override
    protected Runnable resetAction() {
        return () -> cfg().resetOutlineFov();
    }
}
