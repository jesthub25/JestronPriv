package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.ModConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/** Sis: renk, mesafe, yumusaklik, kaldirma, Nether / boss kalin sisi, boyutlar. */
public final class FogScreen extends JpScreen {

    public FogScreen(Screen parent) {
        super(Component.literal("Sis"), parent);
    }

    @Override
    protected String subtitle() {
        return "§7Sadece açık hava sisi · su, lav, körlük ve karanlık sisi asla değişmez";
    }

    @Override
    protected Runnable resetAction() {
        return () -> cfg().resetFog();
    }

    private static String colorMode(String m) {
        return switch (m) {
            case "OZEL" -> "§fÖzel renk";
            case "GOKKUSAGI" -> "§eGökkuşağı";
            default -> "§aDoğal";
        };
    }

    private static String thick(String m) {
        return switch (m) {
            case "AZ" -> "§eAzalt";
            case "KAPALI" -> "§cKaldır";
            default -> "§aDoğal";
        };
    }

    @Override
    protected void buildOptions() {
        ModConfig c = cfg();
        header("Sis");
        toggle("Özel sis", "Kapalıyken oyunun kendi sisi aynen kalır.", () -> c.fogEnabled, v -> c.fogEnabled = v);
        toggle("Sisi kaldır", "Arazi sisini tamamen kaldırır (görüş mesafesi aynı kalır).", () -> c.fogOff, v -> c.fogOff = v);

        header("Renk");
        cycle("Renk modu", "Doğal · Özel renk · Gökkuşağı (yavaşça renk değiştirir)", () -> colorMode(c.fogColorMode), () -> {
            String[] m = {"DOGAL", "OZEL", "GOKKUSAGI"};
            int i = 0;
            for (int k = 0; k < m.length; k++) if (m[k].equals(c.fogColorMode)) i = k;
            c.fogColorMode = m[Math.floorMod(i + (hasShiftDown() ? -1 : 1), m.length)];
        });
        color("Sis rengi", "'Özel renk' modunda. Gece karanlık kalır, sadece renk kayar.", () -> c.fogColor, v -> c.fogColor = v);
        hex("#RRGGBB", v -> {
            c.fogColor = v;
            c.fogColorMode = "OZEL";
        });
        slider("Renk karışımı", null, 0, 1, 0.05, () -> c.fogColorAmount, v -> c.fogColorAmount = (float) v, JpScreen::pct);
        slider("Gökkuşağı hızı", null, 0.2, 4, 0.1, () -> c.fogRainbowSpeed, v -> c.fogRainbowSpeed = (float) v,
                v -> String.format(Locale.ROOT, "%.1fx", v));

        header("Mesafe");
        slider("Sis mesafesi", "1x = vanilla. Düşük = sis yakında, yüksek = uzakta.", 0.1, 2, 0.05, () -> c.fogDistance, v -> c.fogDistance = (float) v,
                JpScreen::pct);
        slider("Yumuşaklık", "Sisin ne kadar yakından başlayıp yavaşça koyulaştığı.\n0 = doğal (sadece en uçta).", 0, 1, 0.05, () -> c.fogSoftness,
                v -> c.fogSoftness = (float) v, v -> v < 0.01 ? "doğal" : JpScreen.pct(v));
        toggle("Gök sisine uygula", "Mesafe ayarı ufuktaki gök sisini de etkilesin.", () -> c.fogSky, v -> c.fogSky = v);
        cycle("Nether / boss sisi", "Nether'in ve boss savaşlarının kalın sisi.\nDoğal · Azalt · Kaldır", () -> thick(c.fogThick), () -> {
            String[] m = {"DOGAL", "AZ", "KAPALI"};
            int i = 0;
            for (int k = 0; k < m.length; k++) if (m[k].equals(c.fogThick)) i = k;
            c.fogThick = m[Math.floorMod(i + (hasShiftDown() ? -1 : 1), m.length)];
        });

        header("Boyutlar");
        toggle("Overworld", null, () -> c.fogDimOverworld, v -> c.fogDimOverworld = v);
        toggle("Nether", null, () -> c.fogDimNether, v -> c.fogDimNether = v);
        toggle("End", null, () -> c.fogDimEnd, v -> c.fogDimEnd = v);
    }
}
