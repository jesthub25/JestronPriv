package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.ui.GuiTheme;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Menu temasi: 10 seffaf tasarim + vanilla, saydamlik ve ozel vurgu rengi. Secim aninda tum JestronPriv menulerine uygulanir. */
public final class ThemeScreen extends JpScreen {

    private boolean rebuild;

    public ThemeScreen(Screen parent) {
        super(Component.literal("Menü Teması"), parent);
    }

    @Override
    protected String subtitle() {
        GuiTheme t = Theme.current();
        return "§f" + t.name() + " §8· §7" + t.desc();
    }

    @Override
    protected Runnable resetAction() {
        return () -> {
            cfg().resetTheme();
            rebuild = true;
        };
    }

    private void pick(String id) {
        cfg().menuTheme = id;
        rebuild = true;
    }

    @Override
    public void tick() {
        super.tick();
        if (rebuild) {
            rebuild = false;
            // bolum basliklari tema rengini alsin
            rebuildWidgets();
        }
    }

    @Override
    protected void buildOptions() {
        ModConfig c = cfg();
        header("Tema");
        cycle("Tema", "Tıkla: sonraki · Shift+tık: önceki.\nTüm JestronPriv menüleri anında bu temaya geçer.", () -> "§f" + Theme.current().name(), () -> {
            c.menuTheme = GuiTheme.next(c.menuTheme, hasShiftDown()).id();
            rebuild = true;
        });
        slider("Saydamlık", "Düğme ve panel arka planlarının koyuluğu.\n%100 = temanın kendi hali.", 0.4, 1.6, 0.05, () -> c.menuOpacity,
                v -> c.menuOpacity = (float) v, JpScreen::pct);
        toggle("Özel vurgu rengi", "Çerçeve, başlık ve kaydırıcı rengini kendin seç.", () -> c.menuAccentCustom, v -> {
            c.menuAccentCustom = v;
            rebuild = true;
        });
        color("Vurgu rengi", "'Özel vurgu rengi' açıkken.", () -> c.menuAccent, v -> {
            c.menuAccent = v;
            c.menuAccentCustom = true;
            rebuild = true;
        });
        hex("#RRGGBB vurgu", v -> {
            c.menuAccent = v;
            c.menuAccentCustom = true;
            rebuild = true;
        });

        header("Tüm Tasarımlar");
        for (GuiTheme t : GuiTheme.ALL) {
            boolean on = t.id().equals(c.menuTheme);
            action((on ? "§a✔ " : "") + t.name(), t.desc(), false, () -> pick(t.id()));
        }
    }
}
