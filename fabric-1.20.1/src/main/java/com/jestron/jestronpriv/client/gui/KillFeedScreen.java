package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.pvp.KillFeed;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Oldurme akisi ayarlari. */
public final class KillFeedScreen extends JpScreen {

    public KillFeedScreen(Screen parent) {
        super(Component.literal("Öldürme Akışı"), parent);
    }

    @Override
    protected String subtitle() {
        return "§7Kim kimi neyle öldürdü · sunucunun ölüm mesajlarından";
    }

    @Override
    protected Runnable resetAction() {
        return () -> {
            ModConfig c = cfg();
            ModConfig d = new ModConfig();
            c.kfHud = d.kfHud;
            c.kfBg = d.kfBg;
            c.kfFilter = d.kfFilter;
            c.kfMax = d.kfMax;
            c.kfDuration = d.kfDuration;
            c.kfIcons = d.kfIcons;
            c.kfWeapon = d.kfWeapon;
            c.kfStreak = d.kfStreak;
            c.kfSelfName = d.kfSelfName;
            c.kfNewestTop = d.kfNewestTop;
            c.kfHideChat = d.kfHideChat;
            c.kfX = d.kfX;
            c.kfY = d.kfY;
            c.kfScale = d.kfScale;
        };
    }

    private static String filterLabel(String f) {
        return switch (f) {
            case "OYUNCU" -> "§dOyuncu öldürmeleri";
            case "BENIM" -> "§dSadece benimkiler";
            default -> "§dHepsi";
        };
    }

    @Override
    protected void buildOptions() {
        ModConfig c = cfg();
        header("Akış");
        toggle("Öldürme akışı", "Köşede 'Öldüren [silah] Ölen' satırları.\nSenin öldürmelerin yeşil, senin ölümün kırmızı vurgulanır.",
                () -> c.kfHud, v -> c.kfHud = v);
        cycle("Göster", "Hepsi: tüm ölüm mesajları (düşme, lav, mob...)\nOyuncu öldürmeleri: oyuncu oyuncuyu öldürünce\nSadece benimkiler: senin öldürmen / ölümün",
                () -> filterLabel(c.kfFilter), () -> c.kfFilter = switch (c.kfFilter) {
                    case "HEPSI" -> "OYUNCU";
                    case "OYUNCU" -> "BENIM";
                    default -> "HEPSI";
                });
        slider("Satır sayısı", null, 1, 10, 1, () -> c.kfMax, v -> c.kfMax = (int) Math.round(v), v -> String.valueOf(Math.round(v)));
        slider("Süre", "Her satır ekranda kaç saniye kalsın.", 2, 30, 1, () -> c.kfDuration, v -> c.kfDuration = (float) v, v -> Math.round(v) + " sn");
        toggle("En yeni üstte", "Kapalı: en yeni altta (yukarı doğru dolar).", () -> c.kfNewestTop, v -> c.kfNewestTop = v);
        header("Görünüm");
        toggle("Simgeler", "Kapalı: simge yerine ⚔ yazar.", () -> c.kfIcons, v -> c.kfIcons = v);
        toggle("Silahı göster", "Mesajda silah varsa (ör. Keskin Elmas Kılıç) onun simgesi.\nKapalı: ölüm nedeninin simgesi (ok, patlama, düşme...).",
                () -> c.kfWeapon, v -> c.kfWeapon = v);
        toggle("Seri sayısı", "Öldürenin ölmeden kaç kişi öldürdüğü: ×2, ×3...", () -> c.kfStreak, v -> c.kfStreak = v);
        toggle("Kendime 'Sen' de", "Kendi adın yerine 'Sen' yazar.", () -> c.kfSelfName, v -> c.kfSelfName = v);
        slider("Arka plan", null, 0, 0.9, 0.05, () -> c.kfBg, v -> c.kfBg = (float) v, v -> Math.round(v * 100) + "%");
        header("Sohbet");
        toggle("Ölüm mesajlarını sohbetten gizle", "Akış zaten gösterdiği için sohbet kalabalığını azaltır.\n"
                + "Mesajlar yine işlenir (öldürme efekti / seri etkilenmez).", () -> c.kfHideChat, v -> c.kfHideChat = v);
        header("Konum ve deneme");
        action("§e✥ Konum ve boyut", "HUD düzenleyicide sürükle, tekerlekle boyutlandır.", false, () -> minecraft.setScreen(new HudEditScreen(this)));
        action("§e▶ Dene", "Akışa örnek satırlar ekler.", false, KillFeed::demo);
        action("§c⟲ Akışı temizle", null, true, KillFeed::clear);
    }
}
