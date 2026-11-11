package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.pvp.Reach;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/** Reach gostergesi ayarlari. */
public final class ReachScreen extends JpScreen {

    public ReachScreen(Screen parent) {
        super(Component.literal("Reach Göstergesi"), parent);
    }

    @Override
    protected String subtitle() {
        if (Reach.count() == 0) return "§7Vuruşunun mesafesi · sunucunun onayladığı vuruşlar sayılır";
        return String.format(Locale.ROOT, "§7Son §f%.2f §7· ort §f%.2f §7· en uzak §f%.2f §7· §f%d §7vuruş", Reach.last(), Reach.average(), Reach.best(),
                Reach.count());
    }

    @Override
    protected Runnable resetAction() {
        return () -> {
            ModConfig c = cfg();
            ModConfig d = new ModConfig();
            c.reachHud = d.reachHud;
            c.reachBg = d.reachBg;
            c.reachDecimals = d.reachDecimals;
            c.reachHistory = d.reachHistory;
            c.reachStats = d.reachStats;
            c.reachColor = d.reachColor;
            c.reachMobs = d.reachMobs;
            c.reachFade = d.reachFade;
            c.reachX = d.reachX;
            c.reachY = d.reachY;
            c.reachScale = d.reachScale;
        };
    }

    @Override
    protected void buildOptions() {
        ModConfig c = cfg();
        header("Gösterge");
        toggle("Reach göstergesi", "Vuruşunun gözünden hedefin çarpışma kutusuna uzaklığı (sunucunun ölçtüğü gibi).\n"
                + "Sadece sunucunun onayladığı vuruşlar sayılır.", () -> c.reachHud, v -> c.reachHud = v);
        slider("Ondalık", "Kaç basamak gösterilsin.", 1, 3, 1, () -> c.reachDecimals, v -> c.reachDecimals = (int) Math.round(v),
                v -> Math.round(v) == 1 ? "2.9" : Math.round(v) == 2 ? "2.87" : "2.871");
        toggle("Renkli", "§b<2.0 §akısa-normal §e2.7-3.0 uzun §6>3.0 gecikme payı", () -> c.reachColor, v -> c.reachColor = v);
        toggle("Son 10 vuruş", "Mesafelerin küçük çubuk grafiği.", () -> c.reachHistory, v -> c.reachHistory = v);
        toggle("İstatistik", "Ortalama · en uzak · vuruş sayısı (bu oturum).", () -> c.reachStats, v -> c.reachStats = v);
        toggle("Moblarda da", "Kapalı: sadece oyunculara vuruşlar.", () -> c.reachMobs, v -> c.reachMobs = v);
        slider("Kaybolma", "Son vuruştan bu kadar sonra solar (0 = hep görünür).", 0, 30, 1, () -> c.reachFade, v -> c.reachFade = (float) v,
                v -> v < 0.5 ? "hep görünür" : Math.round(v) + " sn");
        slider("Arka plan", null, 0, 0.9, 0.05, () -> c.reachBg, v -> c.reachBg = (float) v, v -> Math.round(v * 100) + "%");
        header("Konum ve deneme");
        action("§e✥ Konum ve boyut", "HUD düzenleyicide sürükle, tekerlekle boyutlandır.", false, () -> minecraft.setScreen(new HudEditScreen(this)));
        action("§e▶ Dene", "Örnek vuruşlar ekler.", false, Reach::demo);
        action("§c⟲ İstatistiği sıfırla", null, true, Reach::reset);
    }
}
