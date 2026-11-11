package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.hats.HatStyle;
import com.jestron.jestronpriv.hats.HatStyles;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/** Sapka ayarlari: secim, boyut, yukseklik, egim, hiz, renk, parilti, kask, parcacik. */
public final class HatSettingsScreen extends JpScreen {

    public HatSettingsScreen(Screen parent) {
        super(Component.literal("Şapka Ayarları"), parent);
    }

    @Override
    protected String subtitle() {
        HatStyle st = HatStyles.get(cfg().hat);
        return st == null ? "§7Şapka seçilmedi · aşağıdan seç" : "§d" + st.name() + " §8· §7" + st.desc();
    }

    @Override
    protected Runnable resetAction() {
        return () -> cfg().resetHat();
    }

    private static String deg(double v) {
        return String.format(Locale.ROOT, "%+.0f°", v);
    }

    @Override
    protected void buildOptions() {
        ModConfig c = cfg();
        header("Şapka");
        cycle("Şapka", "Tıkla: sonraki şapka · Shift+tık: önceki.\nIzgara görünümü için 'Şapkalar' menüsünü kullan.", () -> {
            HatStyle st = HatStyles.get(c.hat);
            return st == null ? "§7Kapalı" : "§d" + st.name();
        }, () -> {
            int n = HatStyles.ALL.size();
            int i = -1;
            for (int k = 0; k < n; k++) if (HatStyles.ALL.get(k).id().equals(c.hat)) i = k;
            int ni = hasShiftDown() ? (i <= 0 ? -1 : i - 1) : i + 1;
            c.hat = ni < 0 || ni >= n ? ModConfig.NONE : HatStyles.ALL.get(ni).id();
            if (hasShiftDown() && i < 0) c.hat = HatStyles.ALL.get(n - 1).id();
        });
        action("§b▦ Izgaradan seç", "Tüm şapkaları canlı önizlemeyle gör.", false, () -> minecraft.setScreen(new HatScreen(this)));
        slider("Boyut", "1x varsayılan.", 0.5, 2.0, 0.05, () -> c.hatScale, v -> c.hatScale = (float) v, v -> String.format(Locale.ROOT, "%.2fx", v));
        slider("Yükseklik", "Şapkayı yukarı / aşağı kaydır (blok).", -0.3, 0.6, 0.01, () -> c.hatHeight, v -> c.hatHeight = (float) v,
                v -> String.format(Locale.ROOT, "%+.2f", v));
        slider("Eğim", "Öne / arkaya yatır.", -35, 35, 1, () -> c.hatTilt, v -> c.hatTilt = (float) v, HatSettingsScreen::deg);
        slider("Animasyon hızı", "0 = sabit (hareketsiz), 1x varsayılan.", 0, 3, 0.05, () -> c.hatSpeed, v -> c.hatSpeed = (float) v,
                v -> v < 0.01 ? "sabit" : String.format(Locale.ROOT, "%.2fx", v));
        header("Görünüm");
        toggle("Parıltı", "Taşlar, haleler, alevler karanlıkta ışıldar.", () -> c.hatGlow, v -> c.hatGlow = v);
        toggle("Gökkuşağı", "Renkler akan gökkuşağına döner.", () -> c.hatRainbow, v -> c.hatRainbow = v);
        toggle("Özel renk", "Şapkanın ana rengini seçtiğin renk yapar.", () -> c.hatCustomColor, v -> c.hatCustomColor = v);
        color("Renk", "Özel renk açıkken kullanılır.", () -> c.hatColor, v -> c.hatColor = v);
        hex("#RRGGBB", v -> {
            c.hatColor = v;
            c.hatCustomColor = true;
        });
        toggle("Parçacık", "Bazı şapkalardan stile uygun parçacık (alev, kar, yıldız...).", () -> c.hatParticles, v -> c.hatParticles = v);
        toggle("Kaskla gizle", "Açık: kask takınca şapka gizlenir.\nKapalı: şapka kaskın üstüne çıkar.", () -> c.hatHideHelmet, v -> c.hatHideHelmet = v);
    }
}
