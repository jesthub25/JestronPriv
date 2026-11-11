package com.jestron.jestronpriv.client.hud;

import com.jestron.jestronpriv.client.ModConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Alinan / atilan esyalar: "+12 Elmas" (yesil), "−3 Kum" (kirmizi), ikonla. Ayni esya sure dolmadan tekrar gelirse
 * satir birlesir ve sayi atar. Yeni satir yandan kayarak gelir, suresi bitince soner. Alinanlar sunucunun "esyayi
 * aldi" paketinden (tam adet), atilanlar Q / envanterden atma / disari birakmadan gelir.
 */
public final class PickupHud extends BoundElement implements Hud.Ticking {

    private static final int ROW = 18;

    private static final class Entry {
        ItemStack icon;
        String name;
        int nameColor;
        int amount;
        boolean drop;
        long born, updated, popNanos;
        String text;
        int textAmount = Integer.MIN_VALUE;
    }

    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static final List<Entry> SAMPLE = new ArrayList<>();
    private final List<Entry> view = new ArrayList<>();

    PickupHud() {
        super("pickups", "Alınan / Atılan", c -> c.pkHud, (c, v) -> c.pkHud = v, c -> c.pkX, (c, v) -> c.pkX = v, c -> c.pkY,
                (c, v) -> c.pkY = v, c -> c.pkScale, (c, v) -> c.pkScale = v);
    }

    /** Sunucu onayli alma (sadece yerel oyuncu). */
    public static void onPickup(ItemStack stack, int amount) {
        add(stack, amount, false);
    }

    /** Yerel oyuncu esya atti. */
    public static void onDrop(ItemStack stack, int amount) {
        add(stack, amount, true);
    }

    private static void add(ItemStack stack, int amount, boolean drop) {
        ModConfig c = Hud.cfg();
        if (c == null || !c.pkHud || stack == null || stack.isEmpty() || amount <= 0) return;
        if (drop ? !c.pkDrops : !c.pkPickups) return;
        long now = System.nanoTime();
        if (c.pkMerge) {
            for (Entry e : ENTRIES) {
                if (e.drop == drop && ItemStack.isSameItemSameComponents(e.icon, stack)
                        && (now - e.updated) / 1e9 < c.pkDuration) {
                    e.amount += amount;
                    e.updated = now;
                    e.popNanos = now;
                    return;
                }
            }
        }
        Entry e = new Entry();
        e.icon = stack.copyWithCount(1);
        e.name = stack.getHoverName().getString();
        e.nameColor = rarityColor(stack.getRarity().color());
        e.amount = amount;
        e.drop = drop;
        e.born = e.updated = e.popNanos = now;
        ENTRIES.add(e);
        while (ENTRIES.size() > 24) ENTRIES.remove(0);
    }

    public static void clear() {
        ENTRIES.clear();
    }

    @Override
    public void tick(Minecraft mc, ModConfig c) {
        if (ENTRIES.isEmpty()) return;
        long now = System.nanoTime();
        double life = c.pkDuration + 0.5;
        ENTRIES.removeIf(e -> (now - e.updated) / 1e9 > life);
    }

    private void collect(ModConfig c, boolean sample) {
        view.clear();
        List<Entry> src = ENTRIES;
        if (src.isEmpty() && sample) {
            if (SAMPLE.isEmpty()) {
                SAMPLE.add(sampleEntry(new ItemStack(Items.DIAMOND), "Elmas", 12, false));
                SAMPLE.add(sampleEntry(new ItemStack(Items.GOLDEN_APPLE), "Altın Elma", 3, false));
                SAMPLE.add(sampleEntry(new ItemStack(Items.COBBLESTONE), "Kırık Taş", 32, true));
            }
            src = SAMPLE;
        }
        int max = Math.max(1, Math.min(12, c.pkMax));
        int from = Math.max(0, src.size() - max);
        for (int i = from; i < src.size(); i++) view.add(src.get(i));
    }

    private static Entry sampleEntry(ItemStack s, String name, int n, boolean drop) {
        Entry e = new Entry();
        e.icon = s;
        e.name = name;
        e.nameColor = 0xFFFFFF;
        e.amount = n;
        e.drop = drop;
        e.born = e.updated = e.popNanos = 0L;
        return e;
    }

    @Override
    public boolean hasContent(ModConfig c) {
        return !ENTRIES.isEmpty();
    }

    private static String text(Entry e) {
        if (e.text == null || e.textAmount != e.amount) {
            e.text = (e.drop ? "−" : "+") + e.amount + " ";
            e.textAmount = e.amount;
        }
        return e.text;
    }

    @Override
    public void measure(ModConfig c, boolean sample) {
        collect(c, sample);
        if (view.isEmpty()) {
            w = h = 0;
            return;
        }
        Font f = Minecraft.getInstance().font;
        int tw = 0;
        for (Entry e : view) tw = Math.max(tw, f.width(text(e)) + f.width(e.name));
        w = 18 + 4 + tw + 6;
        // yukseklik sabit (en fazla satir): satir eklenince oge zipmlamaz
        h = Math.max(1, Math.min(12, c.pkMax)) * ROW + 2;
    }

    @Override
    public void draw(GuiGraphics g, ModConfig c, boolean sample, boolean right) {
        Minecraft mc = Minecraft.getInstance();
        Font f = mc.font;
        long now = System.nanoTime();
        int n = view.size();
        int slots = Math.max(1, Math.min(12, c.pkMax));
        for (int k = 0; k < n; k++) {
            Entry e = view.get(k);
            // en yeni altta (yukari dogru dolar) ya da en yeni ustte
            int slot = c.pkNewestBottom ? slots - n + k : n - 1 - k;
            int y = 1 + slot * ROW;
            float age = e.born == 0 ? 10f : (now - e.born) / 1e9f;
            float since = e.updated == 0 ? 0f : (now - e.updated) / 1e9f;
            float life = c.pkDuration;
            float a = since > life ? Math.max(0f, 1f - (since - life) / 0.5f) : 1f;
            if (a * 255 < 5) continue;
            float slide = age < 0.18f ? 1f - age / 0.18f : 0f;
            int off = Math.round(slide * slide * 30f) * (right ? 1 : -1);
            float popAge = e.popNanos == 0 ? 10f : (now - e.popNanos) / 1e9f;
            float pop = popAge < 0.15f ? 1f + 0.25f * (float) Math.sin(Math.PI * popAge / 0.15f) : 1f;
            int rowW = 18 + 4 + f.width(text(e)) + f.width(e.name) + 6;
            int x0 = right ? w - rowW : 0;
            g.pose().pushPose();
            g.pose().translate(off, 0, 0);
            Hud.background(g, x0, y, x0 + rowW, y + ROW - 1, c.pkBg * a);
            g.renderItem(e.icon, x0 + 1, y);
            g.pose().pushPose();
            g.pose().translate(0, 0, 200);
            int ax = (int) (a * 255) << 24;
            String t = text(e);
            int tx = x0 + 21, ty = y + 5;
            g.pose().pushPose();
            g.pose().translate(tx + f.width(t) / 2f, ty + 4, 0);
            g.pose().scale(pop, pop, 1f);
            Hud.text(g, f, t, -f.width(t) / 2, -4, ax | (e.drop ? 0xFF6060 : 0x6BFF5A), c);
            g.pose().popPose();
            int nc = c.pkRarity ? e.nameColor : 0xFFFFFF;
            Hud.text(g, f, e.name, tx + f.width(t), ty, ax | nc, c);
            g.pose().popPose();
            g.pose().popPose();
        }
    }

    /** Test/okuma icin. */
    static int size() {
        return ENTRIES.size();
    }

    static int amountOf(int i) {
        return ENTRIES.get(i).amount;
    }

    /** Nadirlik rengi (yaygin beyaz, nadir sari, destansi mor...). */
    static int rarityColor(ChatFormatting f) {
        Integer v = f == null ? null : f.getColor();
        return v == null ? 0xFFFFFF : v;
    }
}
