package com.jestron.jestronpriv.client;

import com.jestron.jestronpriv.fx.KillEffect;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Oyun ici efekt secim menusu (K tusu veya /ke). Sol tik: sec, sag tik: onizle. */
public final class EffectScreen extends Screen {

    private final JestronPrivClient mod;
    private final Screen parent;
    private boolean dirty;
    private final List<KillEffect> effects;
    private final List<ItemStack> icons = new ArrayList<>();
    private int cols, cellW, cellH, gridX, gridY;
    /** Izgara kaydirma (50 efekt kucuk ekranlara sigmayabilir). */
    private int scroll, maxScroll, viewBottom;
    private Button modeButton, randomButton, offButton, mobButton, volumeButton;

    public EffectScreen(JestronPrivClient mod, Screen parent) {
        super(Component.literal("Öldürme Efektleri"));
        this.mod = mod;
        this.parent = parent;
        this.effects = mod.engine().registry().all();
        for (KillEffect e : effects) {
            icons.add(new ItemStack(BuiltInRegistries.ITEM.getValue(ResourceLocation.withDefaultNamespace(e.icon().id()))));
        }
    }

    @Override
    protected void init() {
        cols = Math.max(2, Math.min(5, (width - 20) / 112));
        int rows = (effects.size() + cols - 1) / cols;
        cellW = Math.min(124, (width - 20) / cols);
        cellH = Math.max(18, Math.min(24, (height - 86) / rows));
        gridX = (width - cols * cellW) / 2;
        gridY = 30;

        int by = Math.min(height - 26, gridY + rows * cellH + 8);
        viewBottom = by - 6;
        maxScroll = Math.max(0, rows * cellH - (viewBottom - gridY));
        scroll = Math.max(0, Math.min(scroll, maxScroll));
        int bw = 74, gap = 4;
        int total = bw * 6 + gap * 5;
        int bx = (width - total) / 2;
        ModConfig c = mod.config();

        modeButton = addRenderableWidget(Button.builder(Component.literal("§e✎ Özel Yazı"), b -> {
            if (minecraft != null) minecraft.setScreen(new com.jestron.jestronpriv.client.gui.TextEffectScreen(this));
        }).bounds(bx, by, bw, 20).build());
        randomButton = addRenderableWidget(Button.builder(randomText(), b -> {
            c.effect = ModConfig.RANDOM;
            dirty = true;
            refresh();
        }).bounds(bx + (bw + gap), by, bw, 20).build());
        offButton = addRenderableWidget(Button.builder(offText(), b -> {
            c.effect = ModConfig.NONE;
            dirty = true;
            refresh();
        }).bounds(bx + (bw + gap) * 2, by, bw, 20).build());
        mobButton = addRenderableWidget(Button.builder(mobText(), b -> {
            c.mobKills = !c.mobKills;
            dirty = true;
            mod.applyConfig();
            b.setMessage(mobText());
        }).bounds(bx + (bw + gap) * 3, by, bw, 20).build());
        volumeButton = addRenderableWidget(Button.builder(volumeText(), b -> {
            float[] steps = {0f, 0.25f, 0.5f, 0.75f, 1f, 1.5f, 2f};
            int i = 0;
            while (i < steps.length && steps[i] <= c.volume + 0.01f) i++;
            c.volume = i >= steps.length ? 0f : steps[i];
            dirty = true;
            mod.applyConfig();
            b.setMessage(volumeText());
        }).bounds(bx + (bw + gap) * 4, by, bw, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Geri"), b -> onClose())
                .bounds(bx + (bw + gap) * 5, by, bw, 20).build());
    }

    private void refresh() {
        if (randomButton != null) randomButton.setMessage(randomText());
        if (offButton != null) offButton.setMessage(offText());
    }


    private Component randomText() {
        return Component.literal(ModConfig.RANDOM.equals(mod.config().effect) ? "§d✔ Rastgele" : "Rastgele");
    }

    private Component offText() {
        return Component.literal(ModConfig.NONE.equals(mod.config().effect) ? "§c✔ Kapalı" : "Efekti Kapat");
    }

    private Component mobText() {
        return Component.literal("Mob: " + (mod.config().mobKills ? "§aAçık" : "§7Kapalı"));
    }

    private Component volumeText() {
        return Component.literal("Ses: " + Math.round(mod.config().volume * 100) + "%");
    }

    private int hovered(double mx, double my) {
        for (int i = 0; i < effects.size(); i++) {
            int x = gridX + (i % cols) * cellW, y = gridY + (i / cols) * cellH - scroll;
            if (my < gridY || my >= viewBottom) return -1;
            if (mx >= x && mx < x + cellW - 2 && my >= y && my < y + cellH - 2) return i;
        }
        return -1;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(font, "§5§lJestronPriv §7- Öldürme Efektleri", width / 2, 8, 0xFFFFFF);
        g.drawCenteredString(font, "§7Sol tık: §fSeç   §7Sağ tık: §fÖnizle   §7Tekerlek: §fKaydır   §7Esc: §fGeri", width / 2, 18, 0xFFFFFF);

        String sel = mod.config().effect;
        int hov = hovered(mouseX, mouseY);
        g.enableScissor(0, gridY, width, viewBottom);
        for (int i = 0; i < effects.size(); i++) {
            KillEffect e = effects.get(i);
            int x = gridX + (i % cols) * cellW, y = gridY + (i / cols) * cellH - scroll;
            if (y + cellH < gridY || y > viewBottom) continue;
            boolean selected = e.id().equals(sel);
            int bg = selected ? 0xC0402060 : (i == hov ? 0xA0303040 : 0x90101018);
            g.fill(x, y, x + cellW - 2, y + cellH - 2, bg);
            if (selected) g.renderOutline(x, y, cellW - 2, cellH - 2, 0xFFD070FF);
            int iy = y + (cellH - 2 - 16) / 2;
            g.renderItem(icons.get(i), x + 3, iy);
            String name = font.plainSubstrByWidth(e.displayName(), cellW - 26);
            g.drawString(font, name, x + 22, y + (cellH - 2 - 8) / 2, 0xFFFFFF);
            if (e.is3D()) g.drawString(font, "§b3D", x + cellW - 16, y + 2, 0xFFFFFF);
        }
        g.disableScissor();
        if (maxScroll > 0) {
            int trackH = viewBottom - gridY;
            int barH = Math.max(12, trackH * trackH / (trackH + maxScroll));
            int barY = gridY + (int) ((trackH - barH) * (scroll / (double) maxScroll));
            int sx = gridX + cols * cellW + 2;
            g.fill(sx, gridY, sx + 2, viewBottom, 0x40FFFFFF);
            g.fill(sx, barY, sx + 2, barY + barH, 0xC0B070FF);
        }

        if (hov >= 0) {
            KillEffect e = effects.get(hov);
            List<Component> tip = new ArrayList<>();
            tip.add(Component.literal(e.displayName()));
            for (String line : e.description()) tip.add(Component.literal("§7" + line));
            tip.add(Component.literal(""));
            tip.add(Component.literal(e.is3D() ? "§bTür: 3D" : "§eTür: 2D Parçacık"));
            tip.add(Component.literal("§aSüre: 2.5 saniye"));
            tip.add(Component.literal(""));
            tip.add(Component.literal(e.id().equals(sel) ? "§a§l✔ ŞU AN SEÇİLİ" : "§a▶ Sol tık: §fSeç"));
            tip.add(Component.literal("§e▶ Sağ tık: §fÖnizle"));
            g.renderComponentTooltip(font, tip, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (maxScroll > 0) {
            scroll = (int) Math.max(0, Math.min(maxScroll, scroll - scrollY * cellH));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int i = hovered(mouseX, mouseY);
        if (i >= 0) {
            KillEffect e = effects.get(i);
            if (button == 1) {
                if (minecraft != null) minecraft.setScreen(null);
                mod.preview(e);
                return true;
            }
            if (button == 0) {
                mod.config().effect = e.id();
                dirty = true;
                refresh();
                if (minecraft != null && minecraft.player != null) {
                    minecraft.player.displayClientMessage(Component.literal("§8[§5JestronPriv§8] §aEfekt seçildi: " + e.displayName()), true);
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void removed() {
        if (dirty) {
            mod.config().save();
            dirty = false;
        }
        super.removed();
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
