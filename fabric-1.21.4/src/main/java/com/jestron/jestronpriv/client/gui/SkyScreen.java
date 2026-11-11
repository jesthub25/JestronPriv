package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.look.Sky;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/** Ozel gokyuzu: kutup isigi, kayan yildiz, gunes / ay, yildizlar, gun batimi, gok rengi. */
public final class SkyScreen extends JpScreen {

    private static final String[] MOON = {"Dolunay", "Küçülen şişkin", "Son dördün", "Küçülen hilal", "Yeni ay", "Büyüyen hilal", "İlk dördün",
            "Büyüyen şişkin"};

    public SkyScreen(Screen parent) {
        super(Component.literal("Gökyüzü"), parent);
    }

    @Override
    protected String subtitle() {
        return cfg().skyEnabled ? "§aÖzel gökyüzü açık §8· §7Geceyi denemek için: Totem & Zaman › Saat" : "§7Kapalı: vanilla gökyüzü";
    }

    @Override
    protected Runnable resetAction() {
        return () -> cfg().resetSky();
    }

    private static String mul(double v) {
        return String.format(Locale.ROOT, "%.2fx", v);
    }

    private static String sunsetName(String m) {
        return switch (m) {
            case "OZEL" -> "§fÖzel renk";
            case "GOKKUSAGI" -> "§eGökkuşağı";
            case "KAPALI" -> "§cKapalı";
            default -> "§aDoğal";
        };
    }

    @Override
    protected void buildOptions() {
        ModConfig c = cfg();
        header("Gökyüzü");
        toggle("Özel gökyüzü", "Sadece görünüş. Nether / End ve su altında efekt çizilmez.", () -> c.skyEnabled, v -> c.skyEnabled = v);
        action("§e✦ Kayan yıldız göster", "Hemen bir kayan yıldız kaydırır (özel gökyüzü açıkken).", false, Sky::testMeteor);

        header("Kutup Işığı");
        toggle("Kutup ışığı", "Gece kuzey göğünde dalgalanan ışık perdeleri.", () -> c.skyAurora, v -> c.skyAurora = v);
        cycle("Renkler", "Klasik · Buzul · Ateş · Pembe · Gökkuşağı · Özel", () -> Sky.paletteName(c.skyAuroraPalette), () -> {
            String[] p = Sky.PALETTES;
            int i = 0;
            for (int k = 0; k < p.length; k++) if (p[k].equals(c.skyAuroraPalette)) i = k;
            c.skyAuroraPalette = p[Math.floorMod(i + (hasShiftDown() ? -1 : 1), p.length)];
        });
        color("Özel renk alt", "'Özel' renklerde perdenin alt rengi.", () -> c.skyAuroraColor1, v -> {
            c.skyAuroraColor1 = v;
            c.skyAuroraPalette = "OZEL";
        });
        color("Özel renk üst", "'Özel' renklerde perdenin üst rengi.", () -> c.skyAuroraColor2, v -> {
            c.skyAuroraColor2 = v;
            c.skyAuroraPalette = "OZEL";
        });
        slider("Parlaklık", null, 0, 2, 0.05, () -> c.skyAuroraStrength, v -> c.skyAuroraStrength = (float) v, JpScreen::pct);
        slider("Akış hızı", null, 0.2, 3, 0.05, () -> c.skyAuroraSpeed, v -> c.skyAuroraSpeed = (float) v, SkyScreen::mul);
        slider("Yükseklik", "Perdelerin gökteki yüksekliği ve boyu.", 0.6, 1.6, 0.05, () -> c.skyAuroraHeight, v -> c.skyAuroraHeight = (float) v,
                SkyScreen::mul);
        toggle("Gündüz de", "Gündüz soluk olarak görünür.", () -> c.skyAuroraDay, v -> c.skyAuroraDay = v);

        header("Kayan Yıldız");
        toggle("Kayan yıldız", "Gece ara sıra gökte kayan, kuyruklu parlak yıldızlar.", () -> c.skyShooting, v -> c.skyShooting = v);
        slider("Sıklık", "Dakikada ortalama kayan yıldız.", 0.2, 6, 0.1, () -> c.skyShootingRate, v -> c.skyShootingRate = (float) v,
                v -> String.format(Locale.ROOT, "~%.0f/dk", Math.max(1, v * 4)));
        slider("Kuyruk boyu", null, 0.5, 2, 0.05, () -> c.skyShootingLength, v -> c.skyShootingLength = (float) v, SkyScreen::mul);
        color("Renk", null, () -> c.skyShootingColor, v -> c.skyShootingColor = v);

        header("Güneş, Ay & Yıldızlar");
        slider("Güneş boyutu", null, 0.3, 3, 0.05, () -> c.skySunSize, v -> c.skySunSize = (float) v, SkyScreen::mul);
        slider("Ay boyutu", null, 0.3, 3, 0.05, () -> c.skyMoonSize, v -> c.skyMoonSize = (float) v, SkyScreen::mul);
        cycle("Ay evresi", "Sadece görünüş (oyunu etkilemez).", () -> c.skyMoonPhase < 0 ? "§aDoğal" : "§f" + MOON[c.skyMoonPhase],
                () -> c.skyMoonPhase = Math.floorMod(c.skyMoonPhase + 1 + (hasShiftDown() ? -1 : 1), 9) - 1);
        slider("Yıldız parlaklığı", null, 0, 3, 0.05, () -> c.skyStars, v -> c.skyStars = (float) v, v -> v < 0.01 ? "yok" : mul(v));
        toggle("Gündüz yıldızları", "Yıldızlar gündüz de soluk görünür.", () -> c.skyStarsDay, v -> c.skyStarsDay = v);

        header("Gün Batımı & Gök Rengi");
        cycle("Gün batımı", "Gün doğumu / batımı ufuk ışığı.", () -> sunsetName(c.skySunsetMode), () -> {
            String[] m = {"DOGAL", "OZEL", "GOKKUSAGI", "KAPALI"};
            int i = 0;
            for (int k = 0; k < m.length; k++) if (m[k].equals(c.skySunsetMode)) i = k;
            c.skySunsetMode = m[Math.floorMod(i + (hasShiftDown() ? -1 : 1), m.length)];
        });
        color("Gün batımı rengi", "'Özel renk' modunda.", () -> c.skySunsetColor, v -> c.skySunsetColor = v);
        slider("Gün batımı gücü", null, 0, 2, 0.05, () -> c.skySunsetStrength, v -> c.skySunsetStrength = (float) v, JpScreen::pct);
        toggle("Gök rengi tonu", "Gökyüzü rengini seçtiğin renge kaydırır (gece karanlık kalır).", () -> c.skyTint, v -> c.skyTint = v);
        color("Gök rengi", null, () -> c.skyTintColor, v -> c.skyTintColor = v);
        slider("Ton miktarı", null, 0, 1, 0.05, () -> c.skyTintAmount, v -> c.skyTintAmount = (float) v, JpScreen::pct);
    }
}
