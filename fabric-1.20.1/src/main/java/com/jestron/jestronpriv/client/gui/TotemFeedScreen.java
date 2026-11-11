package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.pvp.TotemFeed;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Totem bildirimleri ayarlari. */
public final class TotemFeedScreen extends JpScreen {

    public TotemFeedScreen(Screen parent) {
        super(Component.literal("Totem Bildirimleri"), parent);
    }

    @Override
    protected String subtitle() {
        return "§7Yakındaki oyuncu totem patlatınca: kim, kaçıncı, ne zaman";
    }

    @Override
    protected Runnable resetAction() {
        return () -> {
            ModConfig c = cfg();
            ModConfig d = new ModConfig();
            c.tfHud = d.tfHud;
            c.tfBg = d.tfBg;
            c.tfSelf = d.tfSelf;
            c.tfMobs = d.tfMobs;
            c.tfSound = d.tfSound;
            c.tfHead = d.tfHead;
            c.tfMax = d.tfMax;
            c.tfDuration = d.tfDuration;
            c.tfRange = d.tfRange;
            c.tfX = d.tfX;
            c.tfY = d.tfY;
            c.tfScale = d.tfScale;
        };
    }

    @Override
    protected void buildOptions() {
        ModConfig c = cfg();
        header("Bildirim");
        toggle("Totem bildirimleri", "Satır: [yüz] Oyuncu ✦ TOTEM ×3 · 2 sn\nAynı oyuncunun yeni totemi satırı günceller ve nabız atar; ölürse '☠ öldü' yazar.",
                () -> c.tfHud, v -> c.tfHud = v);
        toggle("Kendi totemim", null, () -> c.tfSelf, v -> c.tfSelf = v);
        toggle("Moblar", "Totem tutan moblar (ör. haydut köylüler).", () -> c.tfMobs, v -> c.tfMobs = v);
        slider("Mesafe", "Bu mesafeden uzaktakiler bildirilmez.", 0, 256, 8, () -> c.tfRange, v -> c.tfRange = (int) Math.round(v),
                v -> v < 0.5 ? "sınırsız" : Math.round(v) + " blok");
        toggle("Ses", "Kısa bildirim sesi; üst üste totemde perde yükselir.", () -> c.tfSound, v -> c.tfSound = v);
        header("Görünüm");
        toggle("Oyuncu yüzü", "Kapalı: totem simgesi.", () -> c.tfHead, v -> c.tfHead = v);
        slider("Satır sayısı", null, 1, 8, 1, () -> c.tfMax, v -> c.tfMax = (int) Math.round(v), v -> String.valueOf(Math.round(v)));
        slider("Süre", null, 2, 30, 1, () -> c.tfDuration, v -> c.tfDuration = (float) v, v -> Math.round(v) + " sn");
        slider("Arka plan", null, 0, 0.9, 0.05, () -> c.tfBg, v -> c.tfBg = (float) v, v -> Math.round(v * 100) + "%");
        header("Konum ve deneme");
        action("§e✥ Konum ve boyut", "HUD düzenleyicide sürükle, tekerlekle boyutlandır.", false, () -> minecraft.setScreen(new HudEditScreen(this)));
        action("§e▶ Dene", "Örnek bildirimler ekler.", false, TotemFeed::demo);
    }
}
