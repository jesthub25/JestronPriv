package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.ModConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/** Serbest 3. sahis kamera ayarlari. */
public final class CameraScreen extends JpScreen {

    public CameraScreen(Screen parent) {
        super(Component.literal("Serbest Kamera (3. Şahıs)"), parent);
    }

    @Override
    protected String subtitle() {
        return "§7F5 arka görünümde kamera oyuncudan bağımsız döner";
    }

    @Override
    protected void buildOptions() {
        ModConfig c = cfg();
        toggle("Bas-bak (Alt)", "Tuş basılıyken (varsayılan Sol Alt) 3. şahısta kamerayla etrafına / arkana bakarsın; karakter dönmeden gittiği yöne devam eder.\nBırakınca kamera yumuşakça geri döner ve önceki görünüme (1. şahıs vb.) geçer. Tuşu Kontroller'den değiştirebilirsin.",
                () -> c.freeLook, v -> c.freeLook = v);
        toggle("Serbest kamera", "Açıkken F5 arka görünümde fare kamerayı döndürür, oyuncuyu değil.\nDururken kamera etrafında serbestçe döner; yürürken hareket kameraya göredir ve oyuncu gittiği yöne döner.",
                () -> c.freeCam, v -> c.freeCam = v);
        toggle("Etkileşimde nişan al", "Vurunca, blok kırınca veya eşya kullanınca oyuncu önce kameranın nişan aldığı yere döner.\nKapalıysa oyuncunun baktığı yöne etkileşir.",
                () -> c.freeCamAim, v -> c.freeCamAim = v);
        toggle("Nişangah", "Serbest kamerada ekranın ortasında nişangah gösterir; vurduğun / kullandığın yer tam orasıdır.",
                () -> c.freeCamCrosshair, v -> c.freeCamCrosshair = v);
        slider("Nişan süresi", "Etkileşimden sonra oyuncunun nişan yönüne bakmaya devam ettiği süre.",
                0.25, 3.0, 0.25, () -> c.freeCamAimTime, v -> c.freeCamAimTime = (float) v,
                v -> String.format(Locale.ROOT, "%.2f sn", v));
        slider("Dönüş hızı", "Yürürken oyuncunun gittiği yöne ne kadar hızlı döneceği.",
                0.1, 1.0, 0.05, () -> c.freeCamTurnSpeed, v -> c.freeCamTurnSpeed = (float) v, JpScreen::pct);
        toggle("Elytrada serbest", "Açıkken elytra ile uçarken de kamera serbesttir; oyuncu yumuşakça kameranın baktığı yöne döner.\nKapalıysa uçuşta vanilla kamera.",
                () -> c.freeCamElytra, v -> c.freeCamElytra = v);
        toggle("Ön görünümü atla", "F5 döngüsü: birinci şahıs → arka görünüm → birinci şahıs (yüzüne bakan görünüm atlanır).",
                () -> c.freeCamSkipFront, v -> c.freeCamSkipFront = v);
    }

    @Override
    protected Runnable resetAction() {
        return () -> cfg().resetCamera();
    }
}
