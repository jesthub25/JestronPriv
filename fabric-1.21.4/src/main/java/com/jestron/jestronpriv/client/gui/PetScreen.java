package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.pet.PetKind;
import com.jestron.jestronpriv.pet.PetMode;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Evcil yoldas: tur, yer, renk, boyut, hareket. Oyun arkada calistigi icin degisiklik aninda gorulur. */
public final class PetScreen extends JpScreen {

    public PetScreen(Screen parent) {
        super(Component.literal("Evcil Yoldaş"), parent);
    }

    @Override
    protected String subtitle() {
        return "§7Sadece sen görürsün · Görmek için §f\"Kamera\"§7 ile 3. şahsa geç";
    }

    private static String kindLabel(ModConfig c) {
        PetKind k = PetKind.parse(c.pet);
        return k == null ? "§cKapalı" : "§d" + k.label;
    }

    @Override
    protected void buildOptions() {
        ModConfig c = cfg();
        cycle("Yoldaş", "50 yoldaş. Tıkla: sonraki · Shift+tık: önceki\nHepsini görmek için 'Izgaradan seç'.", () -> kindLabel(c), () -> {
            PetKind[] all = PetKind.values();
            PetKind k = PetKind.parse(c.pet);
            int i = k == null ? -1 : k.ordinal();
            int ni = hasShiftDown() ? (i < 0 ? all.length - 1 : i - 1) : i + 1;
            c.pet = ni < 0 || ni >= all.length ? ModConfig.NONE : all[ni].name();
        });
        action("§b▦ Izgaradan seç", "50 yoldaşın tamamı tek ekranda.", false, () -> minecraft.setScreen(new PetPickerScreen(this)));
        action("§eKamera: 1. / 3. şahıs", "Yoldaşı görmek için kamerayı değiştir (F5 ile aynı).", false, () -> {
            CameraType t = minecraft.options.getCameraType();
            minecraft.options.setCameraType(t.isFirstPerson() ? CameraType.THIRD_PERSON_BACK : CameraType.FIRST_PERSON);
        });

        header("Yer ve Hareket");
        cycle("Yeri", "Omuzda · Yanında · Arkada, yukarıda · Etrafında döner", () -> "§d" + PetMode.parse(c.petMode).label,
                () -> c.petMode = PetMode.parse(c.petMode).next().name());
        toggle("Sol tarafta", "Kapalı: sağ tarafta.", () -> c.petLeft, v -> c.petLeft = v);
        slider("Boyut", null, 0.5, 2, 0.05, () -> c.petScale, v -> c.petScale = (float) v, JpScreen::pct);
        slider("Takip hızı", "Düşük: süzülerek geç gelir · Yüksek: sıkı takip eder.", 0.3, 3, 0.05, () -> c.petFollow,
                v -> c.petFollow = (float) v, JpScreen::pct);
        slider("Süzülme", "Yukarı-aşağı süzülme miktarı.", 0, 2, 0.05, () -> c.petBob, v -> c.petBob = (float) v, JpScreen::pct);
        toggle("1. şahısta göster", "Görüşü kapatmayacak şekilde sağ alt köşede durur.", () -> c.petFirstPerson, v -> c.petFirstPerson = v);
        toggle("Sevinç tepkisi", "Vuruşta zıplar, öldürmede dönerek sevinir (ses + parçacık).", () -> c.petReact, v -> c.petReact = v);

        header("Görünüm");
        toggle("Özel renk", "Kapalı: yoldaşın kendi renkleri.", () -> c.petCustomColor, v -> c.petCustomColor = v);
        color("Renk", null, () -> c.petColor, v -> {
            c.petColor = v;
            c.petCustomColor = true;
        });
        hex("#RRGGBB (yoldaş)", v -> {
            c.petColor = v;
            c.petCustomColor = true;
        });
        toggle("Gökkuşağı", "Renkler sürekli akar.", () -> c.petRainbow, v -> c.petRainbow = v);
        slider("Parlaklık", "Parlayan kısımların ve halenin gücü.", 0.2, 1.5, 0.05, () -> c.petGlow, v -> c.petGlow = (float) v, JpScreen::pct);
        toggle("Parçacık izi", "Arkasında kendi parçacığını bırakır (oyunun parçacık ayarına uyar).", () -> c.petParticles,
                v -> c.petParticles = v);
    }

    @Override
    protected Runnable resetAction() {
        return () -> cfg().resetPet();
    }
}
