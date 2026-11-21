package com.jestron.jestronpriv.jc.modules;

import com.jestron.jestronpriv.jc.core.Category;
import com.jestron.jestronpriv.jc.core.Setting;
import com.jestron.jestronpriv.jc.core.TextHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;

/**
 * Aç/kapa koşma ve eğilme. Oyunun kendi "aç/kapa" seçeneklerini kullanır (sunucularda güvenli),
 * kapatınca eski ayarları geri yükler. İsteğe bağlı durum yazısı gösterir.
 */
public final class ToggleSprintModule extends TextHud {
    private final Setting.Bool sprint = add(new Setting.Bool("sprint", "Koşma aç/kapa", true));
    private final Setting.Bool sneak = add(new Setting.Bool("sneak", "Eğilme aç/kapa", false));
    private final Setting.Bool showText = add(new Setting.Bool("text", "Durum yazısı", true));
    private final Setting.Bool captured = add(new Setting.Bool("captured", "", false));
    private final Setting.Bool prevSprint = add(new Setting.Bool("prevSprint", "", false));
    private final Setting.Bool prevSneak = add(new Setting.Bool("prevSneak", "", false));

    public ToggleSprintModule() {
        super("togglesprint", "Aç/Kapa koşma", "Koşmak ve eğilmek için tuşu basılı tutma.", Category.GAMEPLAY, false, 0.005, 0.46);
        captured.visible = false;
        prevSprint.visible = false;
        prevSneak.visible = false;
    }

    @Override
    protected void onEnable() {
        Options o = Minecraft.getInstance().options;
        if (o == null) return;
        if (!captured.on()) {
            prevSprint.set(o.toggleSprint().get());
            prevSneak.set(o.toggleCrouch().get());
            captured.set(true);
        }
        apply();
    }

    private void apply() {
        Options o = Minecraft.getInstance().options;
        if (o == null) return;
        o.toggleSprint().set(sprint.on());
        o.toggleCrouch().set(sneak.on());
        o.save();
    }

    @Override
    protected void onDisable() {
        Options o = Minecraft.getInstance().options;
        if (o == null || !captured.on()) return;
        o.toggleSprint().set(prevSprint.on());
        o.toggleCrouch().set(prevSneak.on());
        captured.set(false);
        o.save();
    }

    @Override
    public void onSettingChanged(Setting<?> s) {
        if (isEnabled() && (s == sprint || s == sneak)) apply();
    }

    @Override
    protected String[] lines(boolean preview) {
        if (!showText.on()) return preview ? new String[]{"[Durum yazısı kapalı]"} : new String[0];
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null) return preview ? new String[]{"[Koşuyor (Aç/Kapa)]"} : new String[0];
        Options o = mc.options;
        if (sneak.on() && o.keyShift.isDown()) return new String[]{"[Eğiliyor (Aç/Kapa)]"};
        if (sprint.on() && o.keySprint.isDown()) return new String[]{"[Koşuyor (Aç/Kapa)]"};
        if (p.isSprinting()) return new String[]{"[Koşuyor]"};
        return preview ? new String[]{"[Koşuyor (Aç/Kapa)]"} : new String[0];
    }
}
