package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.cape.CapeDesign;
import com.jestron.jestronpriv.cape.ElytraDesigns;
import com.jestron.jestronpriv.client.ModConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/** Elytra ayarlari: desen, hiz, renk tonu, gokkusagi, isilti, kenar, ic yuz, parcacik. */
public final class ElytraSettingsScreen extends JpScreen {

    public ElytraSettingsScreen(Screen parent) {
        super(Component.literal("Elytra Ayarları"), parent);
    }

    @Override
    protected String subtitle() {
        CapeDesign d = ElytraDesigns.get(cfg().ely);
        return d == null ? "§7Elytra görünümü seçilmedi · aşağıdan seç" : "§d" + d.name() + " §8· §7" + d.desc();
    }

    @Override
    protected Runnable resetAction() {
        return () -> cfg().resetEly();
    }

    private static String mul(double v) {
        return String.format(Locale.ROOT, "%.2fx", v);
    }

    @Override
    protected void buildOptions() {
        ModConfig c = cfg();
        header("Elytra");
        cycle("Desen", "Tıkla: sonraki desen · Shift+tık: önceki.\nKüçük resimli ızgara için 'Elytra Görünümleri' menüsü.", () -> {
            CapeDesign d = ElytraDesigns.get(c.ely);
            return d == null ? "§7Kapalı" : "§d" + d.name();
        }, () -> {
            int n = ElytraDesigns.ALL.size();
            int i = ElytraDesigns.indexOf(c.ely);
            int ni;
            if (hasShiftDown()) ni = i < 0 ? n - 1 : i - 1;
            else ni = i + 1;
            c.ely = ni < 0 || ni >= n ? ModConfig.NONE : ElytraDesigns.ALL.get(ni).id();
        });
        action("§b▦ Izgaradan seç", "50 deseni küçük resimleriyle gör.", false, () -> minecraft.setScreen(new ElytraScreen(this)));
        slider("Animasyon hızı", "Desen animasyonunun hızı.\n0 = desen durur.", 0, 3, 0.05, () -> c.elySpeed, v -> c.elySpeed = (float) v,
                v -> v < 0.01 ? "durgun" : mul(v));

        header("Renk & Işıltı");
        toggle("Işıltı", "Desenin parlayan kısımları (alev, yıldız, neon, rün...) karanlıkta ışıldar.", () -> c.elyGlow, v -> c.elyGlow = v);
        slider("Işıltı gücü", null, 0.2, 1.5, 0.05, () -> c.elyGlowStrength, v -> c.elyGlowStrength = (float) v, JpScreen::pct);
        slider("Renk tonu", "Desenin tüm renklerini kaydırır (parlaklık korunur).", -180, 180, 5, () -> c.elyHue, v -> c.elyHue = (float) v,
                v -> Math.abs(v) < 0.5 ? "orijinal" : String.format(Locale.ROOT, "%+.0f°", v));
        toggle("Gökkuşağı", "Renkler kanat boyunca akan gökkuşağına döner.", () -> c.elyRainbow, v -> c.elyRainbow = v);
        slider("Gökkuşağı hızı", null, 0.2, 4, 0.1, () -> c.elyRainbowSpeed, v -> c.elyRainbowSpeed = (float) v, ElytraSettingsScreen::mul);
        toggle("Kenar şeridi", "Kanadın dış kenarında ince vurgu.", () -> c.elyTrim, v -> c.elyTrim = v);
        toggle("İç yüz desenli", "Açık: sırta bakan yüz de desenli.\nKapalı: koyu astar.", () -> c.elyInnerSame, v -> c.elyInnerSame = v);

        header("Parçacık");
        toggle("Parçacık", "Bazı desenlerin kanat uçlarından stile uygun parçacık (alev, kar, yaprak...). Süzülürken daha çok.",
                () -> c.elyParticles, v -> c.elyParticles = v);
        slider("Parçacık miktarı", "Vanilla 'Parçacıklar' ayarına da uyar.", 0, 3, 0.1, () -> c.elyParticleRate, v -> c.elyParticleRate = (float) v,
                v -> v < 0.01 ? "yok" : mul(v));
    }
}
