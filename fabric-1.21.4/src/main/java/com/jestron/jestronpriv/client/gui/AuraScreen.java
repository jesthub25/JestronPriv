package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.cosmetic.Auras;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 50 aura: secince arkada oyuncunun etrafinda canli gorunur. */
public final class AuraScreen extends GridPickerScreen {

    private static final float[] DENSITY = {0.25f, 0.5f, 0.75f, 1f, 1.5f, 2f};
    private final Map<String, ItemStack> icons = new HashMap<>();

    public AuraScreen(Screen parent) {
        super(Component.literal("Aura"), parent);
        for (Auras.Aura a : Auras.ALL) icons.put(a.id(), new ItemStack(a.icon()));
    }

    @Override
    protected String subtitle() {
        return "§7Dururken ve yürürken farklı desenler · Seçince arkada canlı görünür";
    }

    @Override
    protected void fill(List<Entry> out) {
        out.add(new Entry(ModConfig.NONE, "Kapalı", "Aura gösterme"));
        for (Auras.Aura a : Auras.ALL) out.add(new Entry(a.id(), a.name(), a.desc()));
    }

    @Override
    protected String selected() {
        return mod.config().aura;
    }

    @Override
    protected void select(String id) {
        mod.config().aura = id;
    }

    @Override
    protected int preferredCellW() {
        return 112;
    }

    @Override
    protected int preferredCellH() {
        return 24;
    }

    @Override
    protected void drawCell(GuiGraphics g, Entry e, int x, int y, int w, int h, boolean hover, boolean sel) {
        ItemStack icon = icons.get(e.id());
        if (icon != null) g.renderItem(icon, x + 3, y + (h - 16) / 2);
        String name = font.plainSubstrByWidth(e.name(), w - 26);
        g.drawString(font, (sel ? "§d" : "§f") + name, x + 22, y + (h - 8) / 2, 0xFFFFFF);
    }

    @Override
    protected int buttonRows(int width) {
        return oneRow(width) ? 1 : 2;
    }

    /** Dort kontrol + Geri tek satira rahat sigiyor mu (her kontrol en az 86 px). */
    private static boolean oneRow(int width) {
        return (width - 74 - 12) / 4 >= 86;
    }

    @Override
    protected void addButtons(int y, int left, int width) {
        ModConfig c = mod.config();
        int x1, x2, x3, x4, w, w4, y2;
        if (oneRow(width)) {
            w = (width - 74 - 12) / 4;
            w4 = w;
            x1 = left;
            x2 = left + (w + 4);
            x3 = left + (w + 4) * 2;
            x4 = left + (w + 4) * 3;
            y2 = y;
        } else {
            // Dar ekran: ust satirda uc ayar, alt satirda 1. sahis + Geri
            w = (width - 8) / 3;
            w4 = width - 74;
            x1 = left;
            x2 = left + w + 4;
            x3 = left + (w + 4) * 2;
            x4 = left;
            y2 = y + 24;
        }
        addRenderableWidget(Button.builder(densText(c), b -> {
            int i = 0;
            while (i < DENSITY.length && DENSITY[i] <= c.auraDensity + 0.01f) i++;
            c.auraDensity = i >= DENSITY.length ? DENSITY[0] : DENSITY[i];
            b.setMessage(densText(c));
            markDirty();
        }).bounds(x1, y, w, 20).tooltip(Tooltip.create(Component.literal("Aura parçacık miktarı. Düşük FPS'de azalt."))).build());

        Slider spread = new Slider("Dağılma", 0.5, 2.5, 0.05, () -> c.auraSpread, v -> c.auraSpread = (float) v);
        spread.setX(x2);
        spread.setY(y);
        spread.setWidth(w);
        spread.setTooltip(Tooltip.create(Component.literal(
                "Parçacıkların etrafına ne kadar geniş yayılacağı. 1x varsayılan; düşük = vücuduna yakın, yüksek = geniş halka.")));
        addRenderableWidget(spread);

        Slider speed = new Slider("Hız", 0.25, 3.0, 0.05, () -> c.auraSpeed, v -> c.auraSpeed = (float) v);
        speed.setX(x3);
        speed.setY(y);
        speed.setWidth(w);
        speed.setTooltip(Tooltip.create(Component.literal(
                "Desenlerin dönme, akış ve atış hızı. 1x varsayılan; 0.25x ağır çekim, 3x çok hızlı.")));
        addRenderableWidget(speed);

        addRenderableWidget(Button.builder(fpText(c), b -> {
            c.auraFirstPerson = !c.auraFirstPerson;
            b.setMessage(fpText(c));
            markDirty();
        }).bounds(x4, y2, w4, 20).tooltip(Tooltip.create(Component.literal(
                "Birinci şahıs görünümde de aura çıksın mı. Yüz hizasındaki parçacıklar görüşünü kapatmasın diye her zaman gizlenir."))).build());
    }

    private static Component densText(ModConfig c) {
        return Component.literal("Yoğunluk: " + Math.round(c.auraDensity * 100) + "%");
    }

    private static Component fpText(ModConfig c) {
        return Component.literal("1. şahıs: " + (c.auraFirstPerson ? "§aGöster" : "§cGizle"));
    }
}
