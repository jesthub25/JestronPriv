package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.view.ColorGrade;
import com.jestron.jestronpriv.client.view.ScreenTweaks;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/** Ekran ve goruntu: renk duzenleme, ekran orani (stretched), yakinlastirma, dusuk ates. */
public final class DisplayScreen extends JpScreen {

    public DisplayScreen(Screen parent) {
        super(Component.literal("Ekran & Görüntü"), parent);
    }

    @Override
    protected String subtitle() {
        return "§7Değişiklikler arkada canlı görünür · Arayüz etkilenmez";
    }

    @Override
    protected void buildOptions() {
        ModConfig c = cfg();
        header("Renk");
        toggle("Renk düzenleme", "Dünyanın ve elin renklerini ayarlar (arayüz değişmez).\nKapalıyken hiçbir maliyeti yoktur.",
                () -> c.cgEnabled, v -> c.cgEnabled = v);
        cycle("Filtre", "50 hazır renk filtresi (sinematik, film, mevsimler, neon, Nether, End, ters renk, noir, retro konsol...).\n"
                        + "Tıkla: sonraki · Shift+tık: önceki. Aşağıdaki ayarlar filtrenin üstüne eklenir.",
                () -> "§d" + ColorGrade.Preset.parse(c.cgPreset).label + " §8(" + (ColorGrade.Preset.parse(c.cgPreset).ordinal() + 1) + "/"
                        + ColorGrade.Preset.values().length + ")",
                () -> {
                    ColorGrade.Preset p = ColorGrade.Preset.parse(c.cgPreset);
                    c.cgPreset = (hasShiftDown() ? p.previous() : p.next()).name();
                });
        slider("Doygunluk", "Renk canlılığı. 0% = siyah-beyaz, 100% = normal, 200% = çok canlı.", 0, 2, 0.01,
                () -> c.cgSaturation, v -> c.cgSaturation = (float) v, JpScreen::pct);
        slider("Parlaklık", "Genel parlaklık. 100% = normal.", 0.5, 1.5, 0.01,
                () -> c.cgBrightness, v -> c.cgBrightness = (float) v, JpScreen::pct);
        slider("Kontrast", "Açık-koyu farkı. 100% = normal.", 0.5, 1.5, 0.01,
                () -> c.cgContrast, v -> c.cgContrast = (float) v, JpScreen::pct);
        slider("Sıcaklık", "- soğuk (mavi), + sıcak (turuncu).", -1, 1, 0.01,
                () -> c.cgTemperature, v -> c.cgTemperature = (float) v, v -> String.format(Locale.ROOT, "%+.0f", v * 100));

        header("Ekran Oranı (Stretched)");
        cycle("Oran", "Görüntüyü seçilen orana göre yatayda gerer (rekabetçi 'stretched' çözünürlük hissi).\nKapalı · 4:3 · 5:4 · 3:2 · 16:10 · 1:1 · Özel\nArayüz, nişangah ve vuruş algılama değişmez.",
                () -> "§d" + ScreenTweaks.Aspect.parse(c.aspect).label,
                () -> c.aspect = ScreenTweaks.Aspect.parse(c.aspect).next().name());
        slider("Özel oran", "Oran 'Özel' iken kullanılır (genişlik / yükseklik).\n1.33 = 4:3, 1.78 = 16:9.", 0.8, 2.4, 0.01,
                () -> c.aspectCustom, v -> c.aspectCustom = (float) v, v -> String.format(Locale.ROOT, "%.2f", v));

        header("Yakınlaştırma (C basılı tut)");
        toggle("Yakınlaştırma", "Tuşu basılı tutunca yakınlaştırır (Tuş Ayarları'ndan değiştirilebilir).\nYakınlaştıkça fare hassasiyeti otomatik düşer.",
                () -> c.zoomEnabled, v -> c.zoomEnabled = v);
        slider("Yakınlık", "Başlangıç yakınlık seviyesi.", 1.5, 15, 0.5,
                () -> c.zoomFactor, v -> c.zoomFactor = (float) v, v -> String.format(Locale.ROOT, "%.1fx", v));
        toggle("Yumuşak geçiş", "Yakınlaşma / uzaklaşma animasyonlu olur.",
                () -> c.zoomSmooth, v -> c.zoomSmooth = v);
        toggle("Tekerlekle ayar", "Basılıyken fare tekerleği yakınlığı değiştirir (eşya kaymaz).",
                () -> c.zoomScroll, v -> c.zoomScroll = v);

        header("Ateş Katmanı");
        slider("Ateş yüksekliği", "Yanarken ekrandaki ateşi aşağı indirir (görüş açılır). 0 = vanilla.", 0, 0.45, 0.01,
                () -> c.fireHeight, v -> c.fireHeight = (float) v, v -> Math.round(v / 0.45 * 100) + "%");
        slider("Ateş opaklığı", "Ekrandaki ateşin saydamlığı. 100% = vanilla.", 0.2, 1, 0.01,
                () -> c.fireOpacity, v -> c.fireOpacity = (float) v, JpScreen::pct);
    }

    @Override
    protected Runnable resetAction() {
        return () -> cfg().resetDisplay();
    }
}
