package com.jestron.jestronpriv.jc.modules;

import com.jestron.jestronpriv.jc.core.Category;
import com.jestron.jestronpriv.jc.core.Hud;
import com.jestron.jestronpriv.jc.core.HudModule;
import com.jestron.jestronpriv.jc.core.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/** Giyilen zırh ve eldeki eşya, dayanıklılık renkli. */
public final class ArmorModule extends HudModule {
    private final Setting.Bool mainHand = add(new Setting.Bool("mainhand", "Ana eldeki eşya", true));
    private final Setting.Bool offHand = add(new Setting.Bool("offhand", "İkinci eldeki eşya", false));
    private final Setting.Choice display = add(new Setting.Choice("display", "Dayanıklılık", 0, "Kalan", "Yüzde", "Gizle"));
    private final Setting.Choice layout = add(new Setting.Choice("layout", "Düzen", 0, "Dikey", "Yatay"));

    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private long cachedFrame = -1;
    private boolean cachedPreview;
    private final List<ItemStack> items = new ArrayList<>();
    private final List<String> texts = new ArrayList<>();
    private final List<Integer> colors = new ArrayList<>();

    public ArmorModule() {
        super("armor", "Zırh durumu", "Zırh ve eşya dayanıklılığı.", Category.HUD, true, 0.72, 0.74);
    }

    private void collect(boolean preview) {
        if (cachedFrame == Hud.frame && cachedPreview == preview) return;
        cachedFrame = Hud.frame;
        cachedPreview = preview;
        items.clear();
        texts.clear();
        colors.clear();
        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null) {
            if (!preview) return;
            addItem(sample(Items.DIAMOND_HELMET, 0.9));
            addItem(sample(Items.DIAMOND_CHESTPLATE, 0.55));
            addItem(sample(Items.DIAMOND_LEGGINGS, 0.3));
            addItem(sample(Items.DIAMOND_BOOTS, 0.1));
            if (mainHand.on()) addItem(sample(Items.DIAMOND_SWORD, 0.75));
            return;
        }
        for (EquipmentSlot s : ARMOR) addItem(p.getItemBySlot(s));
        if (mainHand.on()) addItem(p.getItemBySlot(EquipmentSlot.MAINHAND));
        if (offHand.on()) addItem(p.getItemBySlot(EquipmentSlot.OFFHAND));
    }

    private static ItemStack sample(net.minecraft.world.item.Item item, double left) {
        ItemStack st = new ItemStack(item);
        if (st.isDamageableItem()) st.setDamageValue((int) Math.round(st.getMaxDamage() * (1 - left)));
        return st;
    }

    private void addItem(ItemStack st) {
        if (st == null || st.isEmpty()) return;
        items.add(st);
        String t = "";
        int c = 0xFFFFFFFF;
        if (st.isDamageableItem() && st.getMaxDamage() > 0) {
            int max = st.getMaxDamage(), left = max - st.getDamageValue();
            double r = left / (double) max;
            c = durabilityColor(r);
            if (display.index() == 0) t = String.valueOf(left);
            else if (display.index() == 1) t = "%" + Math.round(r * 100);
        } else if (st.getCount() > 1) {
            t = "×" + st.getCount();
        }
        texts.add(t);
        colors.add(c);
    }

    public static int durabilityColor(double r) {
        if (r > 0.6) return 0xFF55FF55;
        if (r > 0.3) return 0xFFFFFF55;
        if (r > 0.15) return 0xFFFFAA00;
        return 0xFFFF5555;
    }

    private int maxText() {
        int w = 0;
        for (String t : texts) w = Math.max(w, Hud.width(t));
        return w;
    }

    @Override
    public int width(boolean preview) {
        collect(preview);
        if (items.isEmpty()) return 0;
        if (layout.index() == 0) return Hud.PAD * 2 + 16 + (maxText() > 0 ? 4 + maxText() : 0);
        int w = 0;
        for (String t : texts) w += Math.max(16, Hud.width(t)) + 4;
        return Hud.PAD * 2 + w - 4;
    }

    @Override
    public int height(boolean preview) {
        collect(preview);
        if (items.isEmpty()) return 0;
        if (layout.index() == 0) return Hud.PAD * 2 + items.size() * 18 - 2;
        return Hud.PAD * 2 + 16 + (maxText() > 0 ? 10 : 0);
    }

    @Override
    public void render(GuiGraphics g, boolean preview) {
        collect(preview);
        if (items.isEmpty()) return;
        Hud.box(g, width(preview), height(preview));
        int x = Hud.PAD, y = Hud.PAD;
        for (int i = 0; i < items.size(); i++) {
            ItemStack st = items.get(i);
            String t = texts.get(i);
            if (layout.index() == 0) {
                g.renderItem(st, x, y);
                if (!t.isEmpty()) Hud.text(g, t, x + 20, y + 4, colors.get(i));
                y += 18;
            } else {
                int cw = Math.max(16, Hud.width(t));
                g.renderItem(st, x + (cw - 16) / 2, y);
                if (!t.isEmpty()) Hud.text(g, t, x + (cw - Hud.width(t)) / 2, y + 17, colors.get(i));
                x += cw + 4;
            }
        }
    }
}
