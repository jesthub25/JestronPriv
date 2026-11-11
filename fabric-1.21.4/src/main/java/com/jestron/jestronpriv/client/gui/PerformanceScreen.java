package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.ModConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Performans ayarlari. */
public final class PerformanceScreen extends JpScreen {

    public PerformanceScreen(Screen parent) {
        super(Component.literal("Performans"), parent);
    }

    @Override
    protected String subtitle() {
        com.jestron.jestronpriv.client.ModConfig c = cfg();
        if (c != null && c.cullEnabled) {
            return "§7Son karede ayıklanan: §f" + com.jestron.jestronpriv.client.view.Culler.lastEntities + " varlık§7, §f"
                    + com.jestron.jestronpriv.client.view.Culler.lastBlocks + " blok varlığı";
        }
        return "§7Düşük FPS'de parçacık ve mesafeyi azalt";
    }

    @Override
    protected void buildOptions() {
        ModConfig c = cfg();
        slider("Parçacık yoğunluğu", "Efektlerin oluşturduğu parçacık miktarı.",
                0.1, 1.0, 0.05, () -> c.particleDensity, v -> c.particleDensity = (float) v, JpScreen::pct);
        toggle("Vanilla ayarına uy", "Minecraft'ın Parçacıklar ayarı Azaltılmış/En Az ise efekt parçacıkları da azalır.",
                () -> c.respectVanillaParticles, v -> c.respectVanillaParticles = v);
        slider("Efekt çizim mesafesi", "Bu mesafeden uzaktaki efekt modelleri çizilmez.",
                16, 160, 8, () -> c.effectRenderDistance, v -> c.effectRenderDistance = (int) Math.round(v),
                v -> Math.round(v) + " blok");
        toggle("Görünmeyeni çizme", "Ekran dışında kalan efekt modelleri hiç çizilmez (önerilir).",
                () -> c.frustumCulling, v -> c.frustumCulling = v);
        slider("Aynı anda efekt", "Aynı anda oynayabilecek en fazla efekt sayısı.",
                1, 40, 1, () -> c.maxSessions, v -> c.maxSessions = (int) Math.round(v), v -> String.valueOf(Math.round(v)));
        action("■ Tüm efektleri durdur", "Şu an oynayan bütün efektleri hemen temizler.", false, () -> mod.stopAllEffects());

        header("Varlık Ayıklama");
        toggle("Varlık ayıklama", "Duvarın arkasında kalan varlıklar ve blok varlıkları (sandık, tabela, sancak,\nkafa...) çizilmez. Kalabalık yerlerde FPS'i belirgin artırır. Görüntü değişmez.",
                () -> c.cullEnabled, v -> c.cullEnabled = v);
        toggle("Varlıklar", "Mob, eşya, zırh askısı, çerçeve...", () -> c.cullEntities, v -> c.cullEntities = v);
        toggle("Blok varlıkları", "Sandık, tabela, sancak, kafa, kazan... (fener ışını gibi uzaktan görünenler hariç).",
                () -> c.cullBlockEntities, v -> c.cullBlockEntities = v);
        toggle("Oyuncular", "Kapalıyken oyuncular hep çizilir (isim etiketleri duvar arkasında görünmeye devam eder).",
                () -> c.cullPlayers, v -> c.cullPlayers = v);
        toggle("İsimli varlıkları koru", "İsim etiketi görünen moblar ayıklanmaz.", () -> c.cullKeepNamed, v -> c.cullKeepNamed = v);
        cycle("Hassasiyet", "§fHızlı§7: en az işlem · §fDengeli§7: önerilen · §fKalite§7: her karede tam kontrol",
                () -> switch (c.cullQuality) {
                    case "HIZLI" -> "§dHızlı";
                    case "KALITE" -> "§dKalite";
                    default -> "§dDengeli";
                }, () -> c.cullQuality = switch (c.cullQuality) {
                    case "HIZLI" -> "DENGELI";
                    case "DENGELI" -> "KALITE";
                    default -> "HIZLI";
                });
    }

    @Override
    protected Runnable resetAction() {
        return () -> cfg().resetPerformance();
    }
}
