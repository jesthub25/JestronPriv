package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.pvp.PingTracker;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/** Ping paneli ayarlari. */
public final class PingScreen extends JpScreen {

    private final int[] st = new int[4];

    public PingScreen(Screen parent) {
        super(Component.literal("Ping"), parent);
    }

    @Override
    protected String subtitle() {
        int ms = PingTracker.current();
        if (ms < 0) return "§7Bir sunucuya girince ölçülür";
        String src = PingTracker.live() ? "§aanlık ölçüm" : "§7sekme listesi";
        String s = PingTracker.stats(st) ? " §8· §7en düşük §f" + st[0] + " §7ort §f" + st[1] + " §7en yüksek §f" + st[2] : "";
        return "§7Ping §f" + ms + " ms §8(" + src + "§8)" + s;
    }

    @Override
    protected Runnable resetAction() {
        return () -> {
            ModConfig c = cfg();
            ModConfig d = new ModConfig();
            c.pingHud = d.pingHud;
            c.pingBg = d.pingBg;
            c.pingGraph = d.pingGraph;
            c.pingStats = d.pingStats;
            c.pingBars = d.pingBars;
            c.pingTarget = d.pingTarget;
            c.pingSpike = d.pingSpike;
            c.pingInterval = d.pingInterval;
            c.pingX = d.pingX;
            c.pingY = d.pingY;
            c.pingScale = d.pingScale;
        };
    }

    @Override
    protected void buildOptions() {
        ModConfig c = cfg();
        header("Panel");
        toggle("Ping paneli", "Anlık ping: vanilla'nın F3 ağ grafiğinde kullandığı ping isteğiyle ölçülür.\n"
                + "Sekme listesi yarım dakikada bir güncellenir; bu her saniye.", () -> c.pingHud, v -> c.pingHud = v);
        toggle("Sinyal çubukları", null, () -> c.pingBars, v -> c.pingBars = v);
        toggle("Grafik", "Son 60 ölçümün renkli grafiği.", () -> c.pingGraph, v -> c.pingGraph = v);
        toggle("İstatistik", "▼ en düşük · ≈ ortalama · ▲ en yüksek · ± titreme (jitter).", () -> c.pingStats, v -> c.pingStats = v);
        toggle("Rakibin pingi", "Son vurduğun / nişan aldığın oyuncunun sekme listesi pingi.", () -> c.pingTarget, v -> c.pingTarget = v);
        toggle("Sıçrama uyarısı", "Ping aniden fırlayınca panel kenarı kırmızı yanıp söner.", () -> c.pingSpike, v -> c.pingSpike = v);
        slider("Ölçüm aralığı", "Kaç saniyede bir ölçülsün.", 0.5, 5, 0.5, () -> c.pingInterval, v -> c.pingInterval = (float) v,
                v -> String.format(Locale.ROOT, "%.1f sn", v));
        slider("Arka plan", null, 0, 0.9, 0.05, () -> c.pingBg, v -> c.pingBg = (float) v, v -> Math.round(v * 100) + "%");
        header("Konum");
        action("§e✥ Konum ve boyut", "HUD düzenleyicide sürükle, tekerlekle boyutlandır.", true, () -> minecraft.setScreen(new HudEditScreen(this)));
    }
}
