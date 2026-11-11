package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.ModConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/** Ozel hitbox ayarlari. */
public final class HitboxScreen extends JpScreen {

    public HitboxScreen(Screen parent) {
        super(Component.literal("Hitbox"), parent);
    }

    @Override
    protected String subtitle() {
        return "§7F3+B'nin ayarlanabilir hali · duvar arkası ve görünmezler asla gösterilmez";
    }

    @Override
    protected Runnable resetAction() {
        return () -> {
            ModConfig c = cfg();
            ModConfig d = new ModConfig();
            c.hbEnabled = d.hbEnabled;
            c.hbPlayers = d.hbPlayers;
            c.hbMobs = d.hbMobs;
            c.hbSelf = d.hbSelf;
            c.hbEye = d.hbEye;
            c.hbLook = d.hbLook;
            c.hbFill = d.hbFill;
            c.hbTarget = d.hbTarget;
            c.hbHurt = d.hbHurt;
            c.hbColor = d.hbColor;
            c.hbTargetColor = d.hbTargetColor;
            c.hbHurtColor = d.hbHurtColor;
            c.hbEyeColor = d.hbEyeColor;
            c.hbLookColor = d.hbLookColor;
            c.hbWidth = d.hbWidth;
            c.hbAlpha = d.hbAlpha;
            c.hbFillAlpha = d.hbFillAlpha;
            c.hbRange = d.hbRange;
        };
    }

    @Override
    protected void buildOptions() {
        ModConfig c = cfg();
        header("Kimler");
        toggle("Hitbox", "Oyuncuların çarpışma kutusu (vuruşun sayıldığı alan).", () -> c.hbEnabled, v -> c.hbEnabled = v);
        toggle("Oyuncular", null, () -> c.hbPlayers, v -> c.hbPlayers = v);
        toggle("Moblar", null, () -> c.hbMobs, v -> c.hbMobs = v);
        toggle("Kendim (F5)", "3. şahısta kendi kutun.", () -> c.hbSelf, v -> c.hbSelf = v);
        slider("Mesafe", null, 4, 128, 4, () -> c.hbRange, v -> c.hbRange = (int) Math.round(v), v -> Math.round(v) + " blok");
        header("Çizgiler");
        color("Kutu rengi", null, () -> c.hbColor, v -> c.hbColor = v);
        slider("Kalınlık", null, 0.5, 4, 0.25, () -> c.hbWidth, v -> c.hbWidth = (float) v, v -> String.format(Locale.ROOT, "%.2f", v));
        slider("Opaklık", null, 0.1, 1, 0.05, () -> c.hbAlpha, v -> c.hbAlpha = (float) v, v -> Math.round(v * 100) + "%");
        toggle("Göz hizası", "Kutunun göz yüksekliğindeki çizgisi.", () -> c.hbEye, v -> c.hbEye = v);
        color("Göz hizası rengi", null, () -> c.hbEyeColor, v -> c.hbEyeColor = v);
        toggle("Bakış yönü", "Oyuncunun baktığı yöne 2 bloklık çizgi.", () -> c.hbLook, v -> c.hbLook = v);
        color("Bakış rengi", null, () -> c.hbLookColor, v -> c.hbLookColor = v);
        header("Vurgular");
        toggle("Nişan aldığım", "Nişangahın üstündeki oyuncunun kutusu farklı renkte.", () -> c.hbTarget, v -> c.hbTarget = v);
        color("Nişan rengi", null, () -> c.hbTargetColor, v -> c.hbTargetColor = v);
        toggle("Hasar alan", "Hasar alan oyuncunun kutusu kısa süre farklı renkte.", () -> c.hbHurt, v -> c.hbHurt = v);
        color("Hasar rengi", null, () -> c.hbHurtColor, v -> c.hbHurtColor = v);
        toggle("Dolgu", "Kutunun içi hafif saydam boyanır.", () -> c.hbFill, v -> c.hbFill = v);
        slider("Dolgu opaklığı", null, 0.02, 0.6, 0.02, () -> c.hbFillAlpha, v -> c.hbFillAlpha = (float) v, v -> Math.round(v * 100) + "%");
    }
}
