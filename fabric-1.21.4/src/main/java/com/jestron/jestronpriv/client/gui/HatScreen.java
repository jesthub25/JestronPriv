package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.hats.HatStyle;
import com.jestron.jestronpriv.hats.HatStyles;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 50 animasyonlu sapka. Sagda oyuncunun kendisi sapkasiyla canli doner. */
public final class HatScreen extends GridPickerScreen {

    private final Map<String, ItemStack> icons = new HashMap<>();

    public HatScreen(Screen parent) {
        super(Component.literal("Şapkalar"), parent);
        for (HatStyle s : HatStyles.ALL) icons.put(s.id(), new ItemStack(item(s.icon())));
    }

    private static Item item(String id) {
        try {
            Item it = BuiltInRegistries.ITEM.getValue(ResourceLocation.withDefaultNamespace(id));
            return it == null || it == Items.AIR ? Items.LEATHER_HELMET : it;
        } catch (Throwable t) {
            return Items.LEATHER_HELMET;
        }
    }

    @Override
    protected String subtitle() {
        return "§7Kafanın üstünde canlanır · F5 ile kendine bak";
    }

    @Override
    protected void fill(List<Entry> out) {
        out.add(new Entry(ModConfig.NONE, "Kapalı", "Şapka yok"));
        for (HatStyle s : HatStyles.ALL) out.add(new Entry(s.id(), s.name(), s.desc()));
    }

    @Override
    protected String selected() {
        return mod.config().hat;
    }

    @Override
    protected void select(String id) {
        mod.config().hat = id;
    }

    @Override
    protected int preferredCellW() {
        return 116;
    }

    @Override
    protected int preferredCellH() {
        return 24;
    }

    @Override
    protected int sidePanel(int screenWidth) {
        return screenWidth >= 420 ? Math.min(200, screenWidth / 3) : 0;
    }

    @Override
    protected void drawCell(GuiGraphics g, Entry e, int x, int y, int w, int h, boolean hover, boolean sel) {
        ItemStack icon = icons.get(e.id());
        g.renderItem(icon != null ? icon : new ItemStack(Items.BARRIER), x + 3, y + (h - 16) / 2);
        String name = font.plainSubstrByWidth(e.name(), w - 26);
        g.drawString(font, (sel ? "§d" : "§f") + name, x + 22, y + (h - 8) / 2, 0xFFFFFF);
    }

    @Override
    protected void addButtons(int y, int left, int width) {
        ModConfig c = mod.config();
        int w = (width - 74 - 8) / 3;
        if (w < 90) {
            w = (width - 74 - 4) / 2;
            addRenderableWidget(Button.builder(Component.literal("⚙ Ayarlar"), b -> minecraft.setScreen(new HatSettingsScreen(this)))
                    .bounds(left, y, w, 20).build());
            addRenderableWidget(sizeSlider(c).at(left + w + 4, y, w));
            return;
        }
        addRenderableWidget(Button.builder(Component.literal("⚙ Şapka Ayarları"), b -> minecraft.setScreen(new HatSettingsScreen(this)))
                .bounds(left, y, w, 20).tooltip(Tooltip.create(Component.literal("Boyut, yükseklik, eğim, hız, renk, parıltı, kask ayarı..."))).build());
        addRenderableWidget(sizeSlider(c).at(left + w + 4, y, w));
        addRenderableWidget(Button.builder(rainbowText(c), b -> {
            c.hatRainbow = !c.hatRainbow;
            b.setMessage(rainbowText(c));
            markDirty();
        }).bounds(left + (w + 4) * 2, y, w, 20).tooltip(Tooltip.create(Component.literal("Şapkanın renkleri akan gökkuşağına döner."))).build());
    }

    private Slider sizeSlider(ModConfig c) {
        Slider s = new Slider("Boyut", 0.5, 2.0, 0.05, () -> c.hatScale, v -> c.hatScale = (float) v);
        s.setTooltip(Tooltip.create(Component.literal("Şapka boyutu. 1x varsayılan.")));
        return s;
    }

    private static Component rainbowText(ModConfig c) {
        return Component.literal("Gökkuşağı: " + (c.hatRainbow ? "§aAçık" : "§cKapalı"));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        if (side > 0) renderPreview(g);
    }

    private void renderPreview(GuiGraphics g) {
        LocalPlayer pl = minecraft == null ? null : minecraft.player;
        int x0 = width - side + 4, x1 = width - 8, y0 = gridY, y1 = viewBottom;
        if (x1 - x0 < 60 || y1 - y0 < 60) return;
        g.fill(x0, y0, x1, y1, 0x70000000);
        g.renderOutline(x0, y0, x1 - x0, y1 - y0, 0x60B070FF);
        HatStyle st = HatStyles.get(mod.config().hat);
        g.drawCenteredString(font, st == null ? "§7Şapka seçilmedi" : "§d" + st.name(), (x0 + x1) / 2, y0 + 4, 0xFFFFFF);
        if (pl == null) return;
        // onden capraz, yavasca salinan aci
        double t = (System.currentTimeMillis() % 14000L) / 14000.0 * Math.PI * 2;
        float rot = (float) (180.0 + Math.sin(t) * 60.0);
        float fb = pl.yBodyRot, fy = pl.getYRot(), fx = pl.getXRot(), fh = pl.yHeadRot, fho = pl.yHeadRotO;
        try {
            pl.yBodyRot = rot;
            pl.setYRot(rot);
            pl.setXRot(-8f);
            pl.yHeadRot = rot;
            pl.yHeadRotO = rot;
            // kafa ve sapka buyuk gorunsun: ust govdeye yakinlas
            float scale = Math.min((x1 - x0) / 1.9f, (y1 - y0 - 14) / 1.7f);
            Vector3f off = new Vector3f(0f, pl.getBbHeight() * 0.86f, 0f);
            Quaternionf q = new Quaternionf().rotateZ((float) Math.PI);
            g.enableScissor(x0 + 1, y0 + 14, x1 - 1, y1 - 1);
            try {
                InventoryScreen.renderEntityInInventory(g, (x0 + x1) / 2f, (y0 + 14 + y1) / 2f + 6, scale, off, q, null, pl);
            } finally {
                g.disableScissor();
            }
        } catch (Throwable ignored) {
        } finally {
            pl.yBodyRot = fb;
            pl.setYRot(fy);
            pl.setXRot(fx);
            pl.yHeadRot = fh;
            pl.yHeadRotO = fho;
        }
    }
}
