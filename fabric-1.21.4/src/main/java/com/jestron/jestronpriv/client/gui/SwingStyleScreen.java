package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.anim.SwingStyle;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * Tum vurus stilleri tek ekranda: tek tikla sec. Sag taraf bos birakilir; oyundaki elin orada
 * secili stille surekli sallanarak canli onizleme yapar.
 */
public final class SwingStyleScreen extends GridPickerScreen {

    private static boolean autoPreview = true;

    private final ItemStack normalIcon = new ItemStack(Items.IRON_SWORD);
    private final ItemStack cinematicIcon = new ItemStack(Items.NETHER_STAR);
    private final ItemStack classicIcon = new ItemStack(Items.WOODEN_SWORD);
    private int idle;

    public SwingStyleScreen(Screen parent) {
        super(Component.literal("Vuruş Stili Seç"), parent);
    }

    @Override
    protected String subtitle() {
        return "§7Tek tıkla seç · §e★ §7= sinematik (eşya elde döner) · Elin sağda canlı oynar";
    }

    @Override
    protected void fill(List<Entry> out) {
        for (SwingStyle s : SwingStyle.values()) out.add(new Entry(s.name(), s.label, s.description));
    }

    @Override
    protected String selected() {
        return mod.config().swingStyle().name();
    }

    @Override
    protected void select(String id) {
        ModConfig c = mod.config();
        c.swingStyle = SwingStyle.parse(id).name();
        c.swingEnabled = true;
        swing();
        idle = 0;
    }

    @Override
    protected int preferredCellW() {
        return 132;
    }

    @Override
    protected int preferredCellH() {
        return 24;
    }

    @Override
    protected int sidePanel(int screenWidth) {
        // el ekranin sag altinda cizilir: o bolge izgarayla ortulmesin
        // izgara en az 2 sutun sigmali (2 x 132 + kenar)
        int side = Math.min(screenWidth * 2 / 5, screenWidth - (2 * 132 + 24));
        return side >= 110 ? side : 0;
    }

    @Override
    protected void drawCell(GuiGraphics g, Entry e, int x, int y, int w, int h, boolean hover, boolean sel) {
        SwingStyle st = SwingStyle.parse(e.id());
        ItemStack icon = st.cinematic() ? cinematicIcon : st.ordinal() >= SwingStyle.KNIGHT.ordinal() ? normalIcon : classicIcon;
        g.renderItem(icon, x + 3, y + (h - 16) / 2);
        String name = (st.cinematic() ? "§e★ " : "") + (sel ? "§d" : "§f") + st.label.replace(" (Sinematik)", "");
        g.drawString(font, font.plainSubstrByWidth(name, w - 24), x + 22, y + (h - 8) / 2, 0xFFFFFF);
    }

    @Override
    protected int buttonRows(int width) {
        return 2;
    }

    @Override
    protected void addButtons(int y, int left, int width) {
        ModConfig c = mod.config();
        int w = (width - 8) / 3;
        addRenderableWidget(Button.builder(autoText(), b -> {
            autoPreview = !autoPreview;
            b.setMessage(autoText());
        }).bounds(left, y, w, 20).tooltip(Tooltip.create(Component.literal("Açıkken elin seçili stille sürekli sallanır."))).build());
        Slider str = new Slider("Güç", 0.5, 3.0, 0.05, () -> c.swingStrength, v -> c.swingStrength = (float) v).at(left + w + 4, y, w);
        str.setTooltip(Tooltip.create(Component.literal("Hareketin genişliği. 1x = normal.")));
        addRenderableWidget(str);
        Slider dur = new Slider("Süre", 0.5, 4.0, 0.05, () -> c.swingDuration, v -> c.swingDuration = (float) v).at(left + (w + 4) * 2, y, w);
        dur.setTooltip(Tooltip.create(Component.literal("Vuruş hareketinin süresi (sadece görüntü). Büyük = yavaş.")));
        addRenderableWidget(dur);
        addRenderableWidget(Button.builder(Component.literal("▶ Salla"), b -> swing())
                .bounds(left, y + 24, w, 20).build());
    }

    private static Component autoText() {
        return Component.literal("Oto. önizleme: " + (autoPreview ? "§aAçık" : "§cKapalı"));
    }

    private void swing() {
        LocalPlayer p = minecraft == null ? null : minecraft.player;
        if (p != null) p.swing(InteractionHand.MAIN_HAND, false);
    }

    @Override
    public void tick() {
        super.tick();
        LocalPlayer p = minecraft == null ? null : minecraft.player;
        if (!autoPreview || p == null) return;
        if (p.swinging) {
            idle = 0;
        } else if (++idle >= 7) {
            idle = 0;
            swing();
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        if (side <= 0) return;
        SwingStyle st = mod.config().swingStyle();
        int x0 = width - side + 6, x1 = width - 8;
        int cx = (x0 + x1) / 2;
        g.drawCenteredString(font, (st.cinematic() ? "§e★ " : "") + "§d§l" + st.label, cx, gridY + 2, 0xFFFFFF);
        List<net.minecraft.util.FormattedCharSequence> lines = font.split(Component.literal("§7" + st.description), Math.max(60, x1 - x0));
        int ly = gridY + 14;
        for (net.minecraft.util.FormattedCharSequence l : lines) {
            g.drawCenteredString(font, l, cx, ly, 0xFFFFFF);
            ly += 10;
        }
        if (!mod.config().swingEnabled) g.drawCenteredString(font, "§cVuruş animasyonu kapalı", cx, ly + 4, 0xFFFFFF);
    }
}
