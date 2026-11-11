package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.pet.PetKind;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/** 50 yoldas izgarasi: renk rozeti + ad, ustune gelince aciklama. */
public final class PetPickerScreen extends GridPickerScreen {

    public PetPickerScreen(Screen parent) {
        super(Component.literal("Yoldaş Seç"), parent);
    }

    @Override
    protected String subtitle() {
        return "§7Seç ve F5 ile bak · yer, boyut, renk ayarları 'Ayarlar'da";
    }

    @Override
    protected void fill(List<Entry> out) {
        out.add(new Entry(ModConfig.NONE, "Kapalı", "Yoldaş yok"));
        for (PetKind k : PetKind.values()) out.add(new Entry(k.name(), k.label, k.description));
    }

    @Override
    protected String selected() {
        return mod.config().pet;
    }

    @Override
    protected void select(String id) {
        mod.config().pet = id;
    }

    @Override
    protected int preferredCellW() {
        return 112;
    }

    @Override
    protected int preferredCellH() {
        return 22;
    }

    @Override
    protected void drawCell(GuiGraphics g, Entry e, int x, int y, int w, int h, boolean hover, boolean sel) {
        PetKind k = PetKind.parse(e.id());
        int cy = y + h / 2;
        if (k != null) {
            g.fill(x + 4, cy - 6, x + 16, cy + 6, 0xFF000000 | k.primary);
            g.fill(x + 10, cy - 1, x + 16, cy + 6, 0xFF000000 | k.secondary);
            g.renderOutline(x + 3, cy - 7, 14, 14, 0x80000000);
        } else {
            g.drawString(font, "§c✖", x + 6, cy - 4, 0xFFFFFF);
        }
        g.drawString(font, (sel ? "§d" : "§f") + font.plainSubstrByWidth(e.name(), w - 26), x + 21, cy - 4, 0xFFFFFF);
    }

    @Override
    protected void addButtons(int y, int left, int width) {
        addRenderableWidget(Button.builder(Component.literal("⚙ Yoldaş Ayarları"), b -> minecraft.setScreen(new PetScreen(this)))
                .bounds(left, y, Math.min(200, width), 20).build());
    }
}
