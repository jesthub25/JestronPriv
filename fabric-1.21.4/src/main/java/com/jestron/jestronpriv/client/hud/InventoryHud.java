package com.jestron.jestronpriv.client.hud;

import com.jestron.jestronpriv.client.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Envanter HUD: ana envanter (3x9), istenirse hizli cubuk satiri ve zirh + sol el sutunu. Adet ve dayaniklilik
 * cubugu vanilla gibi. Envanter / sandik ekrani acikken gizlenebilir.
 */
final class InventoryHud extends BoundElement {

    private static final int CELL = 18, PAD = 3;

    InventoryHud() {
        super("inventory", "Envanter", c -> c.invHud, (c, v) -> c.invHud = v, c -> c.invX, (c, v) -> c.invX = v, c -> c.invY,
                (c, v) -> c.invY = v, c -> c.invScale, (c, v) -> c.invScale = v);
    }

    @Override
    public boolean hasContent(ModConfig c) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null) return false;
        if (c.invHideInScreens && mc.screen != null && !(mc.screen instanceof net.minecraft.client.gui.screens.ChatScreen)) return false;
        if (!c.invHideEmpty) return true;
        Inventory inv = p.getInventory();
        for (int i = 9; i < 36; i++) if (!inv.getItem(i).isEmpty()) return true;
        if (c.invHotbar) for (int i = 0; i < 9; i++) if (!inv.getItem(i).isEmpty()) return true;
        return false;
    }

    private static int rows(ModConfig c) {
        return c.invHotbar ? 4 : 3;
    }

    @Override
    public void measure(ModConfig c, boolean sample) {
        int armorW = c.invArmor ? CELL + 4 : 0;
        w = PAD * 2 + 9 * CELL + armorW;
        h = PAD * 2 + rows(c) * CELL + (c.invHotbar ? 3 : 0);
        if (c.invArmor) h = Math.max(h, PAD * 2 + 5 * CELL);
    }

    @Override
    public void draw(GuiGraphics g, ModConfig c, boolean sample, boolean right) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        Hud.background(g, 0, 0, w, h, c.invBg);
        if (p == null) return;
        Font f = mc.font;
        Inventory inv = p.getInventory();
        int gx = PAD + (c.invArmor && right ? CELL + 4 : 0);
        // ana envanter
        for (int r = 0; r < 3; r++) for (int col = 0; col < 9; col++) slot(g, f, inv.getItem(9 + r * 9 + col), gx + col * CELL, PAD + r * CELL, false);
        if (c.invHotbar) {
            int y = PAD + 3 * CELL + 3;
            for (int col = 0; col < 9; col++) slot(g, f, inv.getItem(col), gx + col * CELL, y, col == inv.selected);
        }
        if (c.invArmor) {
            int ax = right ? PAD : PAD + 9 * CELL + 4;
            // kask ustte: 39 kask, 38 gogus, 37 pantolon, 36 bot; 40 sol el
            for (int k = 0; k < 4; k++) slot(g, f, inv.getItem(39 - k), ax, PAD + k * CELL, false);
            slot(g, f, inv.getItem(40), ax, PAD + 4 * CELL, false);
        }
    }

    private static void slot(GuiGraphics g, Font f, ItemStack s, int x, int y, boolean selected) {
        g.fill(x + 1, y + 1, x + CELL - 1, y + CELL - 1, selected ? 0x60FFFFFF : 0x30000000);
        if (s == null || s.isEmpty()) return;
        g.renderItem(s, x + 1, y + 1);
        g.renderItemDecorations(f, s, x + 1, y + 1);
    }
}
