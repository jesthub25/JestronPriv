package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.pvp.Combo;
import com.jestron.jestronpriv.client.pvp.DamageNumbers;
import com.jestron.jestronpriv.client.pvp.KillStreak;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/** PvP hissi: hasar sayilari, kombo sayaci, seri oldurme duyurusu, vurulma rengi. Her bolumde canli deneme. */
public final class PvpScreen extends JpScreen {

    public PvpScreen(Screen parent) {
        super(Component.literal("PvP Hissi"), parent);
    }

    @Override
    protected String subtitle() {
        return "§7Deneme butonlarıyla hemen gör · Kombo ve duyuru bu ekranın üstünde çizilir";
    }

    @Override
    protected void buildOptions() {
        ModConfig c = cfg();
        header("Genel");
        toggle("Moblarda da", "Açık: hasar sayısı, kombo ve isabet işareti moblara vurunca da çalışır.\nKapalı: sadece oyuncular.",
                () -> c.pvpMobs, v -> c.pvpMobs = v);
        action("§e⚔ Hepsini dene", "Hasar sayıları, kombo dizisi ve seri duyurusu birlikte.", false, () -> {
            DamageNumbers.preview();
            Combo.demo();
            KillStreak.demo();
        });

        header("Hasar Sayıları");
        toggle("Hasar sayıları", "Vurduğun hedeften uçan sayılar (sunucunun onayladığı gerçek hasar).\nKritikte altın ✦, öldürmede kızıl ☠.",
                () -> c.dmgNumbers, v -> c.dmgNumbers = v);
        cycle("Biçim", "§fKalp§7: -3.5 ❤  ·  §fCan§7: -7", () -> "KALP".equals(c.dmgFormat) ? "§dKalp ❤" : "§dCan",
                () -> c.dmgFormat = "KALP".equals(c.dmgFormat) ? "CAN" : "KALP");
        slider("Boyut", "Sayıların büyüklüğü.", 0.5, 2, 0.05, () -> c.dmgScale, v -> c.dmgScale = (float) v, JpScreen::pct);
        toggle("Hasara göre renk", "Az hasar beyaz → sarı → turuncu → büyük hasar kırmızı.\nKapalıyken aşağıdaki renk kullanılır.",
                () -> c.dmgColorByAmount, v -> c.dmgColorByAmount = v);
        color("Renk", "Normal vuruş rengi (hasara göre renk kapalıyken).", () -> c.dmgColor, v -> c.dmgColor = v);
        color("Kritik rengi", "Kritik vuruş sayısının rengi.", () -> c.dmgCritColor, v -> c.dmgCritColor = v);
        hex("#RRGGBB (renk)", v -> c.dmgColor = v);
        hex("#RRGGBB (kritik)", v -> c.dmgCritColor = v);
        toggle("Gizli can işareti", "Sunucu canı gizliyorsa (sayı okunamazsa) yine de ✕ / ✦ işareti çıkar.",
                () -> c.dmgUnknownMarker, v -> c.dmgUnknownMarker = v);
        action("§e▶ Sayıları dene", "Önünde örnek sayılar çıkar.", false, DamageNumbers::preview);

        header("Kombo Sayacı");
        toggle("Kombo", "Hasar almadan art arda isabetler. Her isabette sayı atar ve nota yükselir;\n5 / 10 / 15 / 20'de renk ve ödül sesi değişir. Hasar alınca kırılır.",
                () -> c.combo, v -> c.combo = v);
        action("§e✥ Kombo ve duyuru konumu", "HUD düzenleyicide sürükleyerek yerleştir ve boyutlandır.", false,
                () -> minecraft.setScreen(new HudEditScreen(this)));
        slider("Kombo boyutu", null, 0.5, 2, 0.05, () -> c.comboScale, v -> c.comboScale = (float) v, JpScreen::pct);
        slider("Süre", "Bu kadar saniye isabet olmazsa kombo biter.", 1, 6, 0.25, () -> c.comboTimeout,
                v -> c.comboTimeout = (float) v, v -> String.format(Locale.ROOT, "%.2f sn", v));
        toggle("Kombo sesi", "Her isabette yükselen nota.", () -> c.comboSound, v -> c.comboSound = v);
        slider("Kombo ses seviyesi", null, 0, 2, 0.05, () -> c.comboVolume, v -> c.comboVolume = (float) v, JpScreen::pct);
        action("§e▶ Komboyu dene", "Otomatik 10'lu kombo dizisi (en iyi rekorunu etkilemez).", true, Combo::demo);

        header("Seri Öldürme");
        toggle("Seri duyurusu", "Çifte / Üçlü / Dörtlü / Beşli öldürme, seri unvanları (Durdurulamaz, Katliam,\nEfsanevi, Tanrısal…) ve İlk Kan. Ölünce seri sıfırlanır.",
                () -> c.killStreak, v -> c.killStreak = v);
        cycle("Dil", null, () -> "EN".equals(c.streakLang) ? "§dEnglish" : "§dTürkçe",
                () -> c.streakLang = "EN".equals(c.streakLang) ? "TR" : "EN");
        toggle("Duyuru sesi", "Seviyeye göre katmanlı ses (boru, çan, havai fişek…).", () -> c.streakSound, v -> c.streakSound = v);
        slider("Duyuru ses seviyesi", null, 0, 2, 0.05, () -> c.streakVolume, v -> c.streakVolume = (float) v, JpScreen::pct);
        toggle("Kenar parlaması", "Ekran kenarlarında seviye renginde kısa parlama.", () -> c.streakFlash, v -> c.streakFlash = v);
        slider("Duyuru boyutu", null, 0.5, 2, 0.05, () -> c.streakScale, v -> c.streakScale = (float) v, JpScreen::pct);
        slider("Çoklu öldürme süresi", "Bu süre içinde gelen öldürmeler Çifte / Üçlü… sayılır.", 2, 10, 0.5,
                () -> c.streakWindow, v -> c.streakWindow = (float) v, v -> String.format(Locale.ROOT, "%.1f sn", v));
        action("§e▶ Duyuruyu dene", "Her tıkta sıradaki seviye: İlk Kan → Çifte … → Tanrısal.", true, KillStreak::demo);

        header("Vurulma Rengi");
        toggle("Özel renk", "Vurduğun hedef vanilla kırmızı yerine bu renkte yanıp söner.\nDiğer hiçbir şey değişmez.",
                () -> c.hitColor, v -> c.hitColor = v);
        color("Renk", null, () -> c.hitColorRgb, v -> c.hitColorRgb = v);
        toggle("Gökkuşağı", "Renk sürekli akar.", () -> c.hitColorRainbow, v -> c.hitColorRainbow = v);
        hex("#RRGGBB (vurulma)", v -> c.hitColorRgb = v);
        toggle("Herkese uygula", "Açık: hasar alan tüm varlıklar (başkalarının vurdukları da).\nKapalı: sadece senin vurdukların.",
                () -> c.hitColorAll, v -> c.hitColorAll = v);
        slider("Yoğunluk", "Rengin kuvveti. Vanilla kırmızı ≈ 30%.", 0.2, 0.9, 0.01, () -> c.hitColorAlpha,
                v -> c.hitColorAlpha = (float) v, JpScreen::pct);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        // denemeler karartmanin ustunde net gorunsun (oyun arayuzu bu ekran acikken bunlari cizmez)
        com.jestron.jestronpriv.client.hud.Hud.renderPvpOverlay(g, partialTick);
    }

    @Override
    protected Runnable resetAction() {
        return () -> cfg().resetPvp();
    }
}
