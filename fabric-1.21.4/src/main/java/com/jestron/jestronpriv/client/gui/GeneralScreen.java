package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.fx.KillEffect;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/** Oldurme efektlerinin genel ayarlari. */
public final class GeneralScreen extends JpScreen {

    public GeneralScreen(Screen parent) {
        super(Component.literal("Genel Ayarlar"), parent);
    }

    @Override
    protected String subtitle() {
        return "§7Öldürme efektlerinin nasıl ve nerede oynayacağı";
    }

    @Override
    protected void buildOptions() {
        ModConfig c = cfg();
        cycle("Gösterim", "§bKonum§7: oyuncunun öldüğü noktada\n§dEkran§7: ekranının önünde + başlık\n§6İkisi§7: ikisi birden",
                () -> c.viewMode().label, () -> c.mode = c.viewMode().next().name());
        toggle("Ekran başlığı", "Ekran modunda öldürünce ortada başlık gösterilir.",
                () -> c.screenTitle, v -> c.screenTitle = v);
        slider("Ekran mesafesi", "Ekran modunda efektin kaç blok önünde oynayacağı.",
                2.5, 8.0, 0.5, () -> c.screenDistance, v -> c.screenDistance = v,
                v -> String.format(Locale.ROOT, "%.1f blok", v));
        slider("Efekt sesi", "Efekt seslerinin şiddeti. 0% = sessiz.",
                0, 2, 0.05, () -> c.volume, v -> c.volume = (float) v, JpScreen::pct);
        cycle("Ses kanalı", "Efekt seslerinin hangi ses ayarına bağlı olacağı.\n§fAna Ses§7: her zaman duyulur (önerilen)\n§fOyuncular§7 / §fOrtam§7: o kaydırıcı %0 ise efekt sesi duyulmaz.",
                c::soundChannelLabel, c::nextSoundChannel);
        toggle("Gerçekçi ses", "Patlama ve çarpmalara enkaz/çınlama katmanları, doğal perde farkı ve uzak öldürmelerde ses gecikmesi ekler.",
                () -> c.soundRealism, v -> c.soundRealism = v);
        action("♪ Ses testi", "Bir test sesi çalar ve ses seviyelerini sohbete yazar.", false, () -> mod.soundTest());
        toggle("Sıkı doğrulama", "Açıkken (önerilen) efekt sadece öldürme kesinse çıkar:\n§f• §7Son 5 sn içinde son vuran sen olmalısın\n§f• §7Senden sonra başkası vurduysa efekt çıkmaz\n§f• §7Işınlanma / tek belirsiz mesaj tek başına yetmez",
                () -> c.killStrict, v -> c.killStrict = v);
        toggle("Algılama bildirimi", "Açıkken her öldürme algılandığında sohbete kimi ve hangi yolla algıladığını yazar.\nEfekt çıkmazsa bunu açıp dene.",
                () -> c.killDebug, v -> c.killDebug = v);
        toggle("Mob öldürünce de", "Açıkken mob öldürdüğünde de efekt oynar.",
                () -> c.mobKills, v -> c.mobKills = v);
        action("▶ Seçili efekti önizle", "Seçili efekti önünde oynatır.", false, () -> {
            KillEffect fx = mod.engine().registry().get(c.effect);
            if (fx == null && ModConfig.RANDOM.equals(c.effect) && !mod.engine().registry().all().isEmpty()) {
                fx = mod.engine().registry().all().get(0);
            }
            if (fx != null && minecraft != null) {
                minecraft.setScreen(null);
                mod.preview(fx);
            }
        });
    }

    @Override
    protected Runnable resetAction() {
        return () -> cfg().resetGeneral();
    }
}
