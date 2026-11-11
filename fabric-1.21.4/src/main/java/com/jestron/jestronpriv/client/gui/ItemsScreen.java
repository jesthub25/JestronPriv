package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.cosmetic.GroundItems;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Envanter HUD, alinan / atilan bildirimi ve yerdeki esya etiketleri. */
public final class ItemsScreen extends JpScreen {

    public ItemsScreen(Screen parent) {
        super(Component.literal("Envanter & Eşyalar"), parent);
    }

    @Override
    protected String subtitle() {
        return "§7Konum ve boyut: §f\"Konumları düzenle\"§7 · Etiketler duvar arkasını göstermez";
    }

    @Override
    protected void buildOptions() {
        ModConfig c = cfg();
        action("§e✥ Konumları düzenle (sürükle-bırak)", "Envanter ve bildirim kutusunu fareyle yerleştir, tekerlekle boyutlandır.", true,
                () -> minecraft.setScreen(new HudEditScreen(this)));

        header("Envanter HUD");
        toggle("Envanter HUD", "Ana envanter (3×9) ekranda; adet ve dayanıklılık görünür.", () -> c.invHud, v -> c.invHud = v);
        toggle("Hızlı çubuk satırı", "Alt satırda hızlı çubuk (seçili yuva vurgulu).", () -> c.invHotbar, v -> c.invHotbar = v);
        toggle("Zırh + sol el", "Yanda zırh ve sol el sütunu.", () -> c.invArmor, v -> c.invArmor = v);
        toggle("Boşken gizle", "Envanter boşken gösterme.", () -> c.invHideEmpty, v -> c.invHideEmpty = v);
        toggle("Ekran açıkken gizle", "Envanter / sandık / menü açıkken gizlenir (sohbet hariç).", () -> c.invHideInScreens,
                v -> c.invHideInScreens = v);
        slider("Arka plan", null, 0, 0.9, 0.05, () -> c.invBg, v -> c.invBg = (float) v, JpScreen::pct);
        slider("Boyut", null, 0.5, 2, 0.05, () -> c.invScale, v -> c.invScale = (float) v, JpScreen::pct);

        header("Alınan / Atılan Eşyalar");
        toggle("Bildirim", "\"+12 Elmas\" (yeşil) ve \"−3 Kum\" (kırmızı), ikonla.", () -> c.pkHud, v -> c.pkHud = v);
        toggle("Alınanlar", "Yerden aldıkların (sunucunun bildirdiği tam adet).", () -> c.pkPickups, v -> c.pkPickups = v);
        toggle("Atılanlar", "Q, Ctrl+Q, envanterden atma ve pencere dışına bırakma.", () -> c.pkDrops, v -> c.pkDrops = v);
        toggle("Birleştir", "Aynı eşya süre dolmadan gelirse aynı satırda toplanır.", () -> c.pkMerge, v -> c.pkMerge = v);
        toggle("Yeni olan altta", "Kapalı: yeni olan üstte.", () -> c.pkNewestBottom, v -> c.pkNewestBottom = v);
        toggle("Nadirlik rengi", "Ad, eşyanın nadirlik renginde (nadir sarı, destansı mor...).", () -> c.pkRarity, v -> c.pkRarity = v);
        slider("Süre", "Satırın ekranda kalma süresi.", 1, 10, 0.5, () -> c.pkDuration, v -> c.pkDuration = (float) v,
                v -> String.format(java.util.Locale.ROOT, "%.1f sn", v));
        slider("En fazla satır", null, 1, 12, 1, () -> c.pkMax, v -> c.pkMax = (int) Math.round(v), v -> Math.round(v) + "");
        slider("Arka plan", null, 0, 0.9, 0.05, () -> c.pkBg, v -> c.pkBg = (float) v, JpScreen::pct);
        slider("Boyut", null, 0.5, 2, 0.05, () -> c.pkScale, v -> c.pkScale = (float) v, JpScreen::pct);

        header("Yerdeki Eşyalar");
        toggle("Etiketler", "Yerdeki eşyaların üstünde ikon, ad ve adet.", () -> c.giEnabled, v -> c.giEnabled = v);
        toggle("İkon", null, () -> c.giIcon, v -> c.giIcon = v);
        toggle("Ad", null, () -> c.giName, v -> {
            c.giName = v;
            GroundItems.invalidate();
        });
        toggle("Adet", null, () -> c.giCount, v -> {
            c.giCount = v;
            GroundItems.invalidate();
        });
        toggle("Nadirlik rengi", null, () -> c.giRarity, v -> c.giRarity = v);
        toggle("Birleştir", "Yan yana duran aynı eşyalar tek etikette toplam adetle.", () -> c.giMerge, v -> c.giMerge = v);
        toggle("Hitbox kutusu", "Eşyanın çarpışma kutusu (görünür olduğunda).", () -> c.giHitbox, v -> c.giHitbox = v);
        toggle("Sadece baktığım", "Sadece nişangahın üstündeki ya da çok yakındaki eşyalar.", () -> c.giOnlyLooking, v -> c.giOnlyLooking = v);
        slider("Menzil", null, 4, 48, 1, () -> c.giRange, v -> c.giRange = (float) v, v -> Math.round(v) + " blok");
        slider("En fazla etiket", "Çok eşyalı yerlerde FPS'i korur (en yakınlar gösterilir).", 5, 80, 1, () -> c.giMax,
                v -> c.giMax = (int) Math.round(v), v -> Math.round(v) + "");
        slider("Etiket boyutu", null, 0.5, 2, 0.05, () -> c.giScale, v -> c.giScale = (float) v, JpScreen::pct);
        slider("Etiket arka planı", null, 0, 0.8, 0.05, () -> c.giBg, v -> c.giBg = (float) v, JpScreen::pct);
    }

    @Override
    protected Runnable resetAction() {
        return () -> {
            cfg().resetInventoryItems();
            GroundItems.invalidate();
        };
    }
}
