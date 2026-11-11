package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.audio.CustomSound;
import com.jestron.jestronpriv.client.pvp.TotemFx;
import com.jestron.jestronpriv.client.view.WorldTime;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/** Totem animasyonu + sayaci ve sadece senin ekraninda saat / hava. */
public final class TotemTimeScreen extends JpScreen {

    private static int preset = -1;

    public TotemTimeScreen(Screen parent) {
        super(Component.literal("Totem & Zaman"), parent);
    }

    private String shownFile;

    @Override
    protected String subtitle() {
        ModConfig c = cfg();
        if (c.totemCustom) return "§7Özel ses §8» " + statusColor(CustomSound.status()) + CustomSound.status() + " §8· §7dosyayı pencereye sürükle-bırak";
        return "§7Saat ve hava sadece senin ekranında değişir · Ses dosyasını pencereye sürükle-bırak";
    }

    private static String statusColor(String s) {
        if (s.startsWith("Hazır")) return "§a";
        if (s.startsWith("Hata") || s.startsWith("Dosya bulunamadı") || s.startsWith("Desteklenmeyen") || s.startsWith("Çalınamadı")
                || s.startsWith("Kopyalanamadı") || s.startsWith("Geçersiz") || s.startsWith("Seçilen")) return "§c";
        return "§e";
    }

    @Override
    protected void init() {
        try {
            CustomSound.sync();
        } catch (Throwable ignored) {
        }
        shownFile = cfg().totemSoundFile;
        super.init();
    }

    @Override
    public void tick() {
        super.tick();
        // surukle-birak / arka plan secimi dosyayi degistirdiyse dugmeleri tazele
        String f = cfg().totemSoundFile;
        if (f != null && !f.equals(shownFile)) {
            shownFile = f;
            rebuildWidgets();
        }
    }

    @Override
    protected void onChanged() {
        try {
            CustomSound.sync();
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void onFilesDrop(List<Path> paths) {
        try {
            CustomSound.importFiles(paths);
        } catch (Throwable ignored) {
        }
    }

    private static String shortName(String n) {
        if (n == null || n.isEmpty()) return "§7yok";
        return n.length() > 18 ? n.substring(0, 16) + "…" : n;
    }

    private static String secs(double v) {
        return String.format(Locale.ROOT, "%.1f sn", v);
    }

    @Override
    protected void buildOptions() {
        ModConfig c = cfg();
        header("Totem Animasyonu");
        toggle("Totem efekti", "Biri totem patlatınca altın-yeşil ışık halkaları, yükselen sarmal,\nışıltılar ve kutsal ses.",
                () -> c.totemFx, v -> c.totemFx = v);
        slider("Efekt boyutu", null, 0.5, 2, 0.05, () -> c.totemScale, v -> c.totemScale = (float) v, JpScreen::pct);
        toggle("Totem sesi", "Katmanlı kutsal ses; üst üste totemlerde perde yükselir.", () -> c.totemSound, v -> c.totemSound = v);
        toggle("Totem sayacı", "Başının üstünde \"✦ TOTEM ×N ✦\" (kaç totem patlattı). Ölünce sıfırlanır.",
                () -> c.totemCounter, v -> c.totemCounter = v);
        toggle("Sohbete yaz", "Kim kaçıncı totemini patlattı, sohbete (sadece sana) yazar.", () -> c.totemChat, v -> c.totemChat = v);
        toggle("Vanilla partikülü gizle", "Oyunun yeşil-sarı totem partikül bulutunu gizler (efekt açıkken).\nFPS'e iyi gelir.",
                () -> c.totemHideVanilla, v -> c.totemHideVanilla = v);
        toggle("Ekran totemini gizle", "Kendi totemin patlayınca ekranın ortasına gelen büyük totem animasyonunu gizler.",
                () -> c.totemHideScreen, v -> c.totemHideScreen = v);
        action("§e▶ Totemi dene", "Önünde totem efekti ve sesi.", false, TotemFx::preview);

        header("Özel Totem Sesi (cihazından)");
        toggle("Özel ses", "Totem patlayınca kendi seçtiğin ses / video dosyasının sesi çalar.\n"
                + "Desteklenen: MP4, M4A, MOV, 3GP (AAC sesli), MP3, WAV, OGG.\nOyunun 'Oyuncular' ses ayarına uyar.",
                () -> c.totemCustom, v -> c.totemCustom = v);
        cycle("Dosya", "Tıkla: klasördeki dosyalar arasında geç.\nDosya eklemek için 'Klasörü aç'a tıkla ve içine at\nya da dosyayı bu pencereye sürükle-bırak.",
                () -> "§d" + shortName(c.totemSoundFile), () -> {
                    List<String> fs = CustomSound.files();
                    if (fs.isEmpty()) {
                        c.totemSoundFile = "";
                        return;
                    }
                    int i = fs.indexOf(c.totemSoundFile);
                    c.totemSoundFile = fs.get((i + 1) % fs.size());
                    shownFile = c.totemSoundFile;
                });
        action("§b❐ Klasörü aç", "config/jestronpriv/sounds klasörünü açar. Dosyanı içine at,\nsonra 'Yenile' ya da 'Dosya' ile seç.", false, () -> {
            try {
                net.minecraft.Util.getPlatform().openPath(CustomSound.ensureFolder());
            } catch (Throwable ignored) {
            }
        });
        action("§7⟳ Yenile", "Klasörü yeniden tarar ve seçili dosyayı baştan yükler\n(aynı adla değiştirdiysen).", false, () -> {
            List<String> fs = CustomSound.files();
            if ((c.totemSoundFile == null || c.totemSoundFile.isEmpty() || !fs.contains(c.totemSoundFile)) && !fs.isEmpty()) {
                c.totemSoundFile = fs.get(0);
                markDirty();
            }
            CustomSound.reload();
            rebuildWidgets();
        });
        action("§a▶ Sesi dene", "Seçili aralığı şimdi çalar (efektsiz).", false, CustomSound::preview);
        action("§c■ Durdur", "Çalan özel sesi keser.", false, CustomSound::stop);
        slider("Ses seviyesi", "%100 = dosyanın kendi sesi; %200'e kadar güçlendirir\n(yumuşak sınırlayıcı cızırtıyı önler).", 0, 2, 0.05,
                () -> c.totemSoundVolume, v -> c.totemSoundVolume = (float) v, JpScreen::pct);
        slider("Başlangıç", "Dosyanın kaçıncı saniyesinden başlasın (en çok 180 sn).", 0, 180, 0.1,
                () -> c.totemSoundStart, v -> c.totemSoundStart = (float) v, TotemTimeScreen::secs);
        slider("Süre", "Kaç saniye çalsın. 0 = dosyanın sonuna kadar (en çok 60 sn).", 0, 60, 0.1,
                () -> c.totemSoundLength, v -> c.totemSoundLength = (float) v, v -> v < 0.05 ? "sonuna kadar" : secs(v));
        slider("Hız / perde", "1x = normal. Düşük: kalın ve yavaş, yüksek: ince ve hızlı.", 0.5, 2, 0.05,
                () -> c.totemSoundSpeed, v -> c.totemSoundSpeed = (float) v, v -> String.format(Locale.ROOT, "%.2fx", v));
        slider("Giriş yumuşatma", "Sesin başında yavaşça açılma süresi (çıt sesini önler).", 0, 2, 0.01,
                () -> c.totemSoundFadeIn, v -> c.totemSoundFadeIn = (float) v, TotemTimeScreen::secs);
        slider("Çıkış yumuşatma", "Sesin sonunda yavaşça kısılma süresi.", 0, 3, 0.05,
                () -> c.totemSoundFadeOut, v -> c.totemSoundFadeOut = (float) v, TotemTimeScreen::secs);
        cycle("Kimde çalsın", "§fHerkes§7: her totemde\n§fSadece ben§7: sadece senin totemin\n§fSadece başkaları§7: başkasınınki (onun yerinden, 32 blok)",
                () -> switch (c.totemSoundWho) {
                    case "BEN" -> "§dSadece ben";
                    case "DIGER" -> "§dBaşkaları";
                    default -> "§dHerkes";
                }, () -> c.totemSoundWho = switch (c.totemSoundWho) {
                    case "HERKES" -> "BEN";
                    case "BEN" -> "DIGER";
                    default -> "HERKES";
                });
        toggle("Vanilla sesini sustur", "Özel ses çalınca oyunun kendi totem sesi çalmaz.", () -> c.totemSoundMuteVanilla, v -> c.totemSoundMuteVanilla = v);
        toggle("Üst üste çal", "Kapalı: yeni totem önceki özel sesi keser ve baştan çalar.\nAçık: sesler üst üste biner.",
                () -> c.totemSoundOverlap, v -> c.totemSoundOverlap = v);

        header("Saat (sadece sende)");
        cycle("Saat modu", "§fSunucu§7: normal\n§fSabit§7: seçtiğin saatte durur\n§fHızlı döngü§7: gün hızla akar (gün batımı / gece izle)",
                () -> "§d" + WorldTime.TimeMode.parse(c.timeMode).label, () -> {
                    c.timeMode = WorldTime.TimeMode.parse(c.timeMode).next().name();
                    WorldTime.restartCycle();
                });
        cycle("Hazır saat", "Tıkla: Gün doğumu → Sabah → Öğle → Gün batımı → Gece → Gece yarısı.\nMod 'Sunucu' ise 'Sabit'e geçer.",
                () -> preset < 0 ? "§7seç" : "§d" + WorldTime.PRESET_NAMES[preset], () -> {
                    preset = (preset + 1) % WorldTime.PRESET_NAMES.length;
                    c.timeValue = WorldTime.PRESET_TIMES[preset];
                    if (WorldTime.TimeMode.parse(c.timeMode) == WorldTime.TimeMode.SUNUCU) c.timeMode = WorldTime.TimeMode.SABIT.name();
                    WorldTime.restartCycle();
                    rebuildWidgets();
                });
        slider("Saat", "Sabit modda gösterilen saat; döngüde başlangıç saati.", 0, 23999, 50, () -> c.timeValue, v -> {
            c.timeValue = (int) Math.round(v);
            WorldTime.restartCycle();
        }, v -> WorldTime.clock((int) Math.round(v)));
        slider("Döngü hızı", "Hızlı döngüde günün akış hızı (1x = normal, 72x ≈ 1 gün / 17 sn).", 1, 200, 1, () -> c.timeSpeed, v -> {
            c.timeSpeed = (float) v;
            WorldTime.restartCycle();
        }, v -> String.format(Locale.ROOT, "%.0fx", v));

        header("Hava (sadece sende)");
        cycle("Hava", "§fSunucu§7: normal · §fAçık§7: hep güneşli · §fYağmur · §fFırtına\nGeçiş birkaç saniyede yumuşakça olur. Nether / End'de etkisizdir.",
                () -> "§d" + WorldTime.Weather.parse(c.weather).label, () -> c.weather = WorldTime.Weather.parse(c.weather).next().name());
    }

    @Override
    protected Runnable resetAction() {
        return () -> {
            CustomSound.stop();
            cfg().resetTotemTime();
            preset = -1;
            WorldTime.restartCycle();
        };
    }
}
