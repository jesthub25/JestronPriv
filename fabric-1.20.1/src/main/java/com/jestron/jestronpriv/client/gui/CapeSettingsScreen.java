package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.cape.CapeDesign;
import com.jestron.jestronpriv.cape.CapeDesigns;
import com.jestron.jestronpriv.client.ModConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/** Pelerin ayarlari: desen, boy, genislik, ruzgar, fizik, hiz, renk tonu, gokkusagi, isilti, ic yuz, parcacik. */
public final class CapeSettingsScreen extends JpScreen {

    public CapeSettingsScreen(Screen parent) {
        super(Component.literal("Pelerin Ayarları"), parent);
    }

    @Override
    protected String subtitle() {
        CapeDesign d = CapeDesigns.get(cfg().cape);
        return d == null ? "§7Pelerin seçilmedi · aşağıdan seç" : "§d" + d.name() + " §8· §7" + d.desc();
    }

    @Override
    protected Runnable resetAction() {
        return () -> cfg().resetCape();
    }

    private static String mul(double v) {
        return String.format(Locale.ROOT, "%.2fx", v);
    }

    @Override
    protected void buildOptions() {
        ModConfig c = cfg();
        header("Pelerin");
        cycle("Desen", "Tıkla: sonraki desen · Shift+tık: önceki.\nKüçük resimli ızgara için 'Pelerinler' menüsü.", () -> {
            CapeDesign d = CapeDesigns.get(c.cape);
            return d == null ? "§7Kapalı" : "§d" + d.name();
        }, () -> {
            int n = CapeDesigns.ALL.size();
            int i = CapeDesigns.indexOf(c.cape);
            int ni;
            if (hasShiftDown()) ni = i < 0 ? n - 1 : i - 1;
            else ni = i + 1;
            c.cape = ni < 0 || ni >= n ? ModConfig.NONE : CapeDesigns.ALL.get(ni).id();
        });
        action("§b▦ Izgaradan seç", "50 deseni küçük resimleriyle gör.", false, () -> minecraft.setScreen(new CapeScreen(this)));
        slider("Uzunluk", "Pelerin boyu. 1x = vanilla.", 0.7, 1.4, 0.05, () -> c.capeLength, v -> c.capeLength = (float) v, CapeSettingsScreen::mul);
        slider("Genişlik", "Pelerin eni. 1x = vanilla (omuz genişliği).", 0.8, 1.2, 0.05, () -> c.capeWidth, v -> c.capeWidth = (float) v,
                CapeSettingsScreen::mul);

        header("Fizik");
        slider("Rüzgar", "Aşağı doğru ilerleyen dalga ve enine dalgalanma.\n0 = durgun kumaş.", 0, 2, 0.05, () -> c.capeWind, v -> c.capeWind = (float) v,
                v -> v < 0.01 ? "yok" : mul(v));
        slider("Fizik gücü", "Koşarken açılma, zıplarken savrulma.\n1x = vanilla tepkisi, 0 = hep aşağı sarkar.", 0, 2, 0.05, () -> c.capePhysics,
                v -> c.capePhysics = (float) v, CapeSettingsScreen::mul);
        slider("Animasyon hızı", "Desen animasyonu ve dalga hızı.\n0 = desen durur.", 0, 3, 0.05, () -> c.capeSpeed, v -> c.capeSpeed = (float) v,
                v -> v < 0.01 ? "durgun" : mul(v));

        header("Renk & Işıltı");
        toggle("Işıltı", "Desenin parlayan kısımları (yıldız, alev, neon...) karanlıkta ışıldar.", () -> c.capeGlow, v -> c.capeGlow = v);
        slider("Işıltı gücü", null, 0.2, 1.5, 0.05, () -> c.capeGlowStrength, v -> c.capeGlowStrength = (float) v, JpScreen::pct);
        slider("Renk tonu", "Desenin tüm renklerini kaydırır (parlaklık korunur).", -180, 180, 5, () -> c.capeHue, v -> c.capeHue = (float) v,
                v -> Math.abs(v) < 0.5 ? "orijinal" : String.format(Locale.ROOT, "%+.0f°", v));
        toggle("Gökkuşağı", "Renkler aşağı doğru akan gökkuşağına döner.", () -> c.capeRainbow, v -> c.capeRainbow = v);
        slider("Gökkuşağı hızı", null, 0.2, 4, 0.1, () -> c.capeRainbowSpeed, v -> c.capeRainbowSpeed = (float) v, CapeSettingsScreen::mul);
        toggle("Kenar şeridi", "Dış yüzde ince vurgu kenarı.", () -> c.capeTrim, v -> c.capeTrim = v);
        toggle("İç yüz otomatik", "Açık: sırta bakan yüz desenin koyu tonu.\nKapalı: aşağıdaki renk.", () -> c.capeInnerAuto, v -> c.capeInnerAuto = v);
        color("İç yüz rengi", "'İç yüz otomatik' kapalıyken.", () -> c.capeInnerColor, v -> c.capeInnerColor = v);
        hex("#RRGGBB", v -> {
            c.capeInnerColor = v;
            c.capeInnerAuto = false;
        });

        header("Diğer");
        toggle("Parçacık", "Bazı desenlerin alt kenarından stile uygun parçacık (alev, kar, yaprak...).", () -> c.capeParticles, v -> c.capeParticles = v);
        slider("Parçacık miktarı", "Vanilla 'Parçacıklar' ayarına da uyar.", 0, 3, 0.1, () -> c.capeParticleRate, v -> c.capeParticleRate = (float) v,
                v -> v < 0.01 ? "yok" : mul(v));
        toggle("Kanatla gizle", "JestronPriv kanatları görünürken pelerini gizle (iç içe geçmesin).\nElytra giyince her zaman gizlenir (vanilla gibi).",
                () -> c.capeHideWings, v -> c.capeHideWings = v);
    }
}
