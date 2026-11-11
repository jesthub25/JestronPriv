package com.jestron.jestronpriv.client.hud;

import com.jestron.jestronpriv.client.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Locale;

/**
 * Esya sayaci: envanterdeki (zirh ve sol el dahil) toplam totem, inci, ok... Sayim tick'te bir kez yapilir.
 * Bekleme suresi (inci, rüzgar kuresi; sunucu eklediyse altin elma) ikonun ustunde kararti ve saniye olarak gosterilir.
 */
public final class ItemHud extends HudElement implements Hud.Ticking {

    public static final String[] NAMES = {"Totem", "Ender İncisi", "Ok (hepsi)", "Altın Elma", "Büyülü Elma", "Rüzgâr Küresi",
            "Fırlatılabilir İksir", "Örümcek Ağı", "Havai Fişek", "Tecrübe Şişesi", "End Kristali", "Koro Meyvesi", "Obsidyen"};
    public static final int ITEM_COUNT = NAMES.length;

    private static final int[] SAMPLE = {3, 12, 64, 8, 1, 16, 4, 0, 32, 64, 9, 5, 64};

    private final ItemStack[] icons = new ItemStack[ITEM_COUNT];
    private final int[] counts = new int[ITEM_COUNT];
    private final String[] countText = new String[ITEM_COUNT];
    private final int[] lastCount = new int[ITEM_COUNT];
    // bekleme suresi tahmini: tick basina dusen oran -> toplam sure
    private final float[] lastPct = new float[ITEM_COUNT];
    private final float[] cdTotal = new float[ITEM_COUNT];
    private final String[] cdText = new String[ITEM_COUNT];
    private final int[] shown = new int[ITEM_COUNT];
    private int shownCount;
    private boolean init;

    ItemHud() {
        super("items", "Eşya Sayacı");
    }

    private static Item item(int i) {
        return switch (i) {
            case 0 -> Items.TOTEM_OF_UNDYING;
            case 1 -> Items.ENDER_PEARL;
            case 2 -> Items.ARROW;
            case 3 -> Items.GOLDEN_APPLE;
            case 4 -> Items.ENCHANTED_GOLDEN_APPLE;
            case 5 -> Items.WIND_CHARGE;
            case 6 -> Items.SPLASH_POTION;
            case 7 -> Items.COBWEB;
            case 8 -> Items.FIREWORK_ROCKET;
            case 9 -> Items.EXPERIENCE_BOTTLE;
            case 10 -> Items.END_CRYSTAL;
            case 11 -> Items.CHORUS_FRUIT;
            default -> Items.OBSIDIAN;
        };
    }

    /** Bu esya bu sayaca dahil mi (oklar: normal, spektral, uclu). */
    private static int slotOf(ItemStack s) {
        if (s.is(Items.TOTEM_OF_UNDYING)) return 0;
        if (s.is(Items.ENDER_PEARL)) return 1;
        if (s.is(Items.ARROW) || s.is(Items.SPECTRAL_ARROW) || s.is(Items.TIPPED_ARROW)) return 2;
        if (s.is(Items.GOLDEN_APPLE)) return 3;
        if (s.is(Items.ENCHANTED_GOLDEN_APPLE)) return 4;
        if (s.is(Items.WIND_CHARGE)) return 5;
        if (s.is(Items.SPLASH_POTION)) return 6;
        if (s.is(Items.COBWEB)) return 7;
        if (s.is(Items.FIREWORK_ROCKET)) return 8;
        if (s.is(Items.EXPERIENCE_BOTTLE)) return 9;
        if (s.is(Items.END_CRYSTAL)) return 10;
        if (s.is(Items.CHORUS_FRUIT)) return 11;
        if (s.is(Items.OBSIDIAN)) return 12;
        return -1;
    }

    private void ensureIcons() {
        if (init) return;
        for (int i = 0; i < ITEM_COUNT; i++) {
            icons[i] = new ItemStack(item(i));
            lastCount[i] = -1;
        }
        init = true;
    }

    public ItemStack icon(int i) {
        ensureIcons();
        return icons[i];
    }

    @Override
    public boolean enabled(ModConfig c) {
        return c.icHud;
    }

    @Override
    public void setEnabled(ModConfig c, boolean v) {
        c.icHud = v;
    }

    @Override
    public float x(ModConfig c) {
        return c.icX;
    }

    @Override
    public float y(ModConfig c) {
        return c.icY;
    }

    @Override
    public float scale(ModConfig c) {
        return c.icScale;
    }

    @Override
    public void setPos(ModConfig c, float x, float y) {
        c.icX = x;
        c.icY = y;
    }

    @Override
    public void setScale(ModConfig c, float s) {
        c.icScale = s;
    }

    @Override
    public void tick(Minecraft mc, ModConfig c) {
        LocalPlayer p = mc.player;
        if (p == null || !c.icHud) return;
        ensureIcons();
        for (int i = 0; i < ITEM_COUNT; i++) counts[i] = 0;
        Inventory inv = p.getInventory();
        int n = inv.getContainerSize();
        for (int i = 0; i < n; i++) {
            ItemStack s = inv.getItem(i);
            if (s == null || s.isEmpty()) continue;
            int k = slotOf(s);
            if (k >= 0) counts[k] += s.getCount();
        }
        for (int i = 0; i < ITEM_COUNT; i++) {
            if (counts[i] != lastCount[i] || countText[i] == null) {
                countText[i] = Integer.toString(counts[i]);
                lastCount[i] = counts[i];
            }
            // bekleme suresi: oran tick basina d azaliyorsa toplam = 1/d tick
            float pct = p.getCooldowns().getCooldownPercent(icons[i], 0f);
            if (pct > 0f && lastPct[i] > pct) {
                float d = lastPct[i] - pct;
                if (d > 1e-4f) cdTotal[i] = cdTotal[i] <= 0 ? 1f / d : cdTotal[i] * 0.7f + (1f / d) * 0.3f;
            }
            if (pct <= 0f) cdTotal[i] = 0f;
            lastPct[i] = pct;
            cdText[i] = pct > 0f && cdTotal[i] > 0f ? seconds(pct * cdTotal[i] / 20f) : null;
        }
    }

    static String seconds(float s) {
        return s >= 10f ? Math.round(s) + "s" : String.format(Locale.ROOT, "%.1fs", s);
    }

    private void collect(ModConfig c, boolean sample) {
        shownCount = 0;
        for (int i = 0; i < ITEM_COUNT; i++) {
            if ((c.icMask & (1 << i)) == 0) continue;
            int cnt = sample ? SAMPLE[i] : counts[i];
            if (c.icHideZero && cnt == 0 && !sample) continue;
            shown[shownCount++] = i;
        }
    }

    @Override
    public boolean hasContent(ModConfig c) {
        collect(c, false);
        return shownCount > 0;
    }

    @Override
    public void measure(ModConfig c, boolean sample) {
        ensureIcons();
        collect(c, sample);
        if (shownCount == 0) {
            w = h = 0;
            return;
        }
        Font f = Minecraft.getInstance().font;
        if (c.icVertical) {
            int tw = 0;
            for (int k = 0; k < shownCount; k++) {
                int i = shown[k];
                String t = sample ? Integer.toString(SAMPLE[i]) : countText[i];
                int x = f.width(t == null ? "0" : t);
                if (c.icCooldown && !sample && cdText[i] != null) x += 4 + f.width(cdText[i]);
                if (sample && i == 1 && c.icCooldown) x += 4 + f.width("0.6s");
                tw = Math.max(tw, x);
            }
            w = 18 + 4 + tw + 4;
            h = shownCount * 18 + 2;
        } else {
            w = shownCount * 20 + 2;
            h = 20;
        }
    }

    @Override
    public void draw(GuiGraphics g, ModConfig c, boolean sample, boolean right) {
        Minecraft mc = Minecraft.getInstance();
        Font f = mc.font;
        Hud.background(g, 0, 0, w, h, c.icBg);
        double t = System.nanoTime() / 1e9;
        for (int k = 0; k < shownCount; k++) {
            int i = shown[k];
            int cnt = sample ? SAMPLE[i] : counts[i];
            String txt = sample ? Integer.toString(cnt) : countText[i];
            if (txt == null) txt = "0";
            String cd = sample ? (i == 1 && c.icCooldown ? "0.6s" : null) : (c.icCooldown ? cdText[i] : null);
            boolean warn = c.icWarn && i == 0 && cnt <= 1;
            int col = cnt == 0 ? 0xFF5050 : warn ? 0xFFB040 : 0xFFFFFF;
            int a = 255;
            if (warn) a = 140 + Math.round(115 * (float) (0.5 + 0.5 * Math.cos(t * 6.0)));
            int ix, iy;
            if (c.icVertical) {
                ix = right ? w - 18 : 1;
                iy = 1 + k * 18;
            } else {
                ix = 2 + (right ? shownCount - 1 - k : k) * 20;
                iy = 2;
            }
            g.renderItem(icons[i], ix + 1, iy + 1);
            // bekleme karartisi (vanilla ile ayni) + sayi
            g.renderItemDecorations(f, icons[i], ix + 1, iy + 1, "");
            g.pose().pushPose();
            g.pose().translate(0, 0, 200);
            if (cnt == 0) g.fill(ix + 1, iy + 1, ix + 17, iy + 17, 0x70000000);
            if (c.icVertical) {
                int tw = f.width(txt) + (cd != null ? 4 + f.width(cd) : 0);
                int tx = right ? ix - 4 - tw : ix + 22;
                Hud.text(g, f, txt, tx, iy + 5, (a << 24) | col, c);
                if (cd != null) Hud.text(g, f, cd, tx + f.width(txt) + 4, iy + 5, 0xFFA0A0A0, c);
            } else {
                Hud.text(g, f, txt, ix + 18 - f.width(txt), iy + 10, (a << 24) | col, c);
                if (cd != null) {
                    g.pose().pushPose();
                    g.pose().translate(ix + 9, iy + 1, 0);
                    g.pose().scale(0.6f, 0.6f, 1f);
                    Hud.text(g, f, cd, -f.width(cd) / 2, 0, 0xFFE0E0E0, c);
                    g.pose().popPose();
                }
            }
            g.pose().popPose();
        }
    }
}
