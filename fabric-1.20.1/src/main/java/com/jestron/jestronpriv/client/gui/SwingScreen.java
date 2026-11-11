package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.view.ViewModel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;

import java.util.Locale;

/** Vurus animasyonu + el gorunumu (viewmodel) ayarlari. Arkada elin canli onizlenir. */
public final class SwingScreen extends JpScreen {

    private int presetIdx;

    public SwingScreen(Screen parent) {
        super(Component.literal("Vuruş Animasyonu & El Görünümü"), parent);
    }

    @Override
    protected String subtitle() {
        return "§7Ayar değiştikçe elin otomatik sallanır · Fare tekerleği: kaydır";
    }

    private static String f2(double v) {
        return String.format(Locale.ROOT, "%+.2f", v);
    }

    private static String deg(double v) {
        return String.format(Locale.ROOT, "%+.0f°", v);
    }

    @Override
    protected void buildOptions() {
        ModConfig c = cfg();
        header("Vuruş Animasyonu");
        toggle("Vuruş animasyonu", "Ana eldeki eşyanın vuruş hareketini seçili stile çevirir.",
                () -> c.swingEnabled, v -> c.swingEnabled = v);
        action("Stil: §d" + c.swingStyle().label + " §f▸", "Tüm stilleri tek ekranda gör ve tek tıkla seç.\nSeçim ekranında elin sürekli sallanarak stili gösterir.",
                false, () -> {
                    if (minecraft != null) minecraft.setScreen(new SwingStyleScreen(this));
                });
        slider("Güç", "Hareketin genişliği. 1.00 = normal.", 0.5, 3.0, 0.05,
                () -> c.swingStrength, v -> c.swingStrength = (float) v, v -> String.format(Locale.ROOT, "%.2f", v));
        slider("Vuruş süresi", "Sallanma hareketinin süresi. Küçük = hızlı, büyük = yavaş.\nSadece görüntüdür; saldırı hızını değiştirmez.\nSinematik stilleri daha net görmek için 1.5-2x deneyin.",
                0.5, 4.0, 0.05, () -> c.swingDuration, v -> c.swingDuration = (float) v,
                v -> String.format(Locale.ROOT, "x%.2f", v));
        toggle("Sadece vururken", "Açıkken eşya dururken vanilla pozda durur, sadece vuruşta stil uygulanır.",
                () -> c.swingOnlyWhileSwinging, v -> c.swingOnlyWhileSwinging = v);
        toggle("Eşya değiştirme inişi", "Hızlı çubukta eşya değiştirince elin aşağı inip yeni eşyayla kalkması.",
                () -> c.swingEquipDip, v -> c.swingEquipDip = v);
        toggle("Saldırı bekleme dalışı", "Vanilla 1.9+ hissi: vuruştan sonra eşya bekleme süresi boyunca aşağı iner.\nKapalı (önerilen): seçili stil vuruş anında, temiz oynar.",
                () -> c.swingCooldownDip, v -> c.swingCooldownDip = v);
        action("▶ Önizle", "Elini bir kez salla.", true, this::preview);

        header("El Görünümü (Viewmodel)");
        toggle("El görünümü", "Aşağıdaki el / eşya ayarlarını uygular. Kapalıyken vanilla.",
                () -> c.vmEnabled, v -> c.vmEnabled = v);
        action("Hazır: §d" + ViewModel.PRESETS[presetIdx] + " §f▸", "Tıkla: sıradaki hazır ayar.\n" + String.join(" · ", ViewModel.PRESETS),
                false, () -> {
                    presetIdx = (presetIdx + 1) % ViewModel.PRESETS.length;
                    ViewModel.applyPreset(c, presetIdx);
                    markDirty();
                    rebuildWidgets();
                });
        slider("Kol sağ/sol", "Kolun (boş el ve eşya) yatay konumu. + sağa, - ortaya.", -0.6, 0.6, 0.01,
                () -> c.vmHandX, v -> c.vmHandX = (float) v, SwingScreen::f2);
        slider("Kol yukarı/aşağı", "Kolun dikey konumu. + yukarı, - aşağı.", -0.6, 0.6, 0.01,
                () -> c.vmHandY, v -> c.vmHandY = (float) v, SwingScreen::f2);
        slider("Kol ileri/geri", "Kolu uzat (-) ya da geri çek (+).", -0.6, 0.6, 0.01,
                () -> c.vmHandZ, v -> c.vmHandZ = (float) v, SwingScreen::f2);
        slider("El FOV", "Elin çizim açısı (oyun FOV'unu değiştirmez). 70 = vanilla.\nDüşük = el büyük ve yakın, yüksek = küçük ve uzak.", 30, 110, 1,
                () -> c.vmHandFov, v -> c.vmHandFov = (float) v, v -> String.format(Locale.ROOT, "%.0f", v));
        slider("Eşya sağ/sol", "Sadece eşyanın yatay konumu.", -0.6, 0.6, 0.01,
                () -> c.vmItemX, v -> c.vmItemX = (float) v, SwingScreen::f2);
        slider("Eşya yukarı/aşağı", "Sadece eşyanın dikey konumu.", -0.6, 0.6, 0.01,
                () -> c.vmItemY, v -> c.vmItemY = (float) v, SwingScreen::f2);
        slider("Eşya ileri/geri", "Sadece eşyanın derinliği. - uzak, + yakın.", -0.6, 0.6, 0.01,
                () -> c.vmItemZ, v -> c.vmItemZ = (float) v, SwingScreen::f2);
        slider("Eşya boyutu", "Eldeki eşyanın büyüklüğü. 1.00 = vanilla.", 0.3, 2.0, 0.01,
                () -> c.vmItemScale, v -> c.vmItemScale = (float) v, v -> String.format(Locale.ROOT, "%.2fx", v));
        slider("Döndür X (eğim)", "Eşyayı öne / arkaya eğer.", -180, 180, 1,
                () -> c.vmRotX, v -> c.vmRotX = (float) v, SwingScreen::deg);
        slider("Döndür Y (yön)", "Eşyayı sağa / sola çevirir.", -180, 180, 1,
                () -> c.vmRotY, v -> c.vmRotY = (float) v, SwingScreen::deg);
        slider("Döndür Z (yatış)", "Eşyayı saat yönünde / tersine yatırır.", -180, 180, 1,
                () -> c.vmRotZ, v -> c.vmRotZ = (float) v, SwingScreen::deg);
        toggle("Sol ele uygula", "Ayarlar sol ele aynalanarak da uygulanır (iki el simetrik).",
                () -> c.vmOffhand, v -> c.vmOffhand = v);
        toggle("Sol eli gizle", "Sol eldeki eşyayı (kalkan, totem, meşale...) gizler.\nKullanırken (blok, yemek) yine görünür.",
                () -> c.vmHideOffhand, v -> c.vmHideOffhand = v);
        toggle("Nefes salınımı", "Dururken elde çok hafif, doğal bir salınım.",
                () -> c.vmIdleSway, v -> c.vmIdleSway = v);
        action("El görünümünü sıfırla", "Sadece el / eşya konum, boyut ve dönüş ayarlarını vanilla'ya döndürür.", true, () -> {
            c.resetViewModel();
            presetIdx = 0;
            markDirty();
            rebuildWidgets();
        });
    }

    @Override
    protected Runnable resetAction() {
        return () -> {
            cfg().resetSwing();
            cfg().resetViewModel();
            presetIdx = 0;
        };
    }

    @Override
    protected void onChanged() {
        preview();
    }

    private void preview() {
        LocalPlayer p = minecraft == null ? null : minecraft.player;
        // Paket gondermeden sadece yerel olarak salla
        if (p != null) p.swing(InteractionHand.MAIN_HAND, false);
    }
}
