package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.hud.InfoHud;
import com.jestron.jestronpriv.client.hud.ItemHud;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** HUD ayarlari: iksir paneli, esya sayaci, bilgi satiri + surukle-birak duzenleyici. */
public final class HudScreen extends JpScreen {

    public HudScreen(Screen parent) {
        super(Component.literal("HUD"), parent);
    }

    @Override
    protected String subtitle() {
        return "§7Konum ve boyut için §f\"Konumları düzenle\"§7 · Değişiklikler canlı";
    }

    @Override
    protected void buildOptions() {
        ModConfig c = cfg();
        action("§e✥ Konumları düzenle (sürükle-bırak)", "Tüm HUD öğelerini fareyle taşı, tekerlekle boyutlandır.", true,
                () -> minecraft.setScreen(new HudEditScreen(this)));
        toggle("F3'te gizle", "Hata ayıklama ekranı (F3) açıkken HUD öğeleri gizlenir.", () -> c.hudHideF3, v -> c.hudHideF3 = v);
        toggle("Yazı gölgesi", null, () -> c.hudShadow, v -> c.hudShadow = v);

        header("İksir Paneli");
        toggle("İksir paneli", "Aktif etkiler: ikon, ad + seviye, kalan süre ve süre çubuğu.", () -> c.potHud, v -> c.potHud = v);
        toggle("Vanilla ikonları gizle", "Sağ üstteki küçük iksir ikonlarını gizler (panel açıkken).",
                () -> c.potHideVanilla, v -> c.potHideVanilla = v);
        toggle("Kompakt", "Sadece ikon + süre, yan yana.", () -> c.potCompact, v -> c.potCompact = v);
        toggle("Etki adları", null, () -> c.potNames, v -> c.potNames = v);
        toggle("Süre çubuğu", "Kalan sürenin oranı (tam boy = gördüğün en uzun süre).", () -> c.potBar, v -> c.potBar = v);
        toggle("Fener etkileri", "Fenerden (beacon) gelen etkiler de gösterilsin.", () -> c.potAmbient, v -> c.potAmbient = v);
        cycle("Renk", "§fEtki§7: etkinin kendi rengi · §fTür§7: iyi yeşil, kötü kırmızı · §fBeyaz",
                () -> switch (c.potColor) {
                    case "TUR" -> "§dTür";
                    case "BEYAZ" -> "§dBeyaz";
                    default -> "§dEtki";
                }, () -> c.potColor = switch (c.potColor) {
                    case "ETKI" -> "TUR";
                    case "TUR" -> "BEYAZ";
                    default -> "ETKI";
                });
        cycle("Sıralama", null, () -> switch (c.potSort) {
            case "TUR" -> "§dİyi → kötü";
            case "AD" -> "§dAda göre";
            default -> "§dKalan süre";
        }, () -> c.potSort = switch (c.potSort) {
            case "SURE" -> "TUR";
            case "TUR" -> "AD";
            default -> "SURE";
        });
        slider("Bitiş uyarısı", "Bu kadar saniye kalınca yanıp söner. 0 = kapalı.", 0, 30, 1, () -> c.potWarn,
                v -> c.potWarn = (int) Math.round(v), v -> v < 0.5 ? "Kapalı" : Math.round(v) + " sn");
        slider("Arka plan", null, 0, 0.9, 0.05, () -> c.potBg, v -> c.potBg = (float) v, JpScreen::pct);
        slider("Boyut", null, 0.5, 2, 0.05, () -> c.potScale, v -> c.potScale = (float) v, JpScreen::pct);

        header("Eşya Sayacı");
        toggle("Eşya sayacı", "Envanterdeki (zırh ve sol el dahil) toplam adet; bekleme süresi ikonda.", () -> c.icHud, v -> c.icHud = v);
        toggle("Alt alta", "Kapalı: yan yana.", () -> c.icVertical, v -> c.icVertical = v);
        toggle("0 olanları gizle", null, () -> c.icHideZero, v -> c.icHideZero = v);
        toggle("Son totem uyarısı", "1 ya da 0 totem kalınca sayı turuncu yanıp söner.", () -> c.icWarn, v -> c.icWarn = v);
        toggle("Bekleme süresi", "İnci / rüzgâr küresi bekleme süresi saniye olarak.", () -> c.icCooldown, v -> c.icCooldown = v);
        slider("Arka plan", null, 0, 0.9, 0.05, () -> c.icBg, v -> c.icBg = (float) v, JpScreen::pct);
        slider("Boyut", null, 0.5, 2, 0.05, () -> c.icScale, v -> c.icScale = (float) v, JpScreen::pct);
        for (int i = 0; i < ItemHud.ITEM_COUNT; i++) {
            int bit = 1 << i;
            toggle(ItemHud.NAMES[i], null, () -> (c.icMask & bit) != 0, v -> c.icMask = v ? c.icMask | bit : c.icMask & ~bit);
        }

        header("Bilgi Satırı");
        toggle("Bilgi satırı", null, () -> c.infoHud, v -> c.infoHud = v);
        toggle("Tek satır", "Kapalı: alt alta liste.", () -> c.infoRow, v -> c.infoRow = v);
        toggle("Kalite renkleri", "FPS ve ping iyi / orta / kötü renklensin.", () -> c.infoQualityColor, v -> c.infoQualityColor = v);
        slider("Koordinat ondalığı", null, 0, 2, 1, () -> c.infoDecimals, v -> c.infoDecimals = (int) Math.round(v),
                v -> Math.round(v) + " basamak");
        color("Etiket rengi", null, () -> c.infoLabelColor, v -> c.infoLabelColor = v);
        color("Değer rengi", null, () -> c.infoValueColor, v -> c.infoValueColor = v);
        slider("Arka plan", null, 0, 0.9, 0.05, () -> c.infoBg, v -> c.infoBg = (float) v, JpScreen::pct);
        slider("Boyut", null, 0.5, 2, 0.05, () -> c.infoScale, v -> c.infoScale = (float) v, JpScreen::pct);
        for (int i = 0; i < InfoHud.FIELD_COUNT; i++) {
            int bit = 1 << i;
            toggle(InfoHud.FIELDS[i], null, () -> (c.infoMask & bit) != 0, v -> c.infoMask = v ? c.infoMask | bit : c.infoMask & ~bit);
        }
    }

    @Override
    protected Runnable resetAction() {
        return () -> cfg().resetHud();
    }
}
