package com.jestron.jestronpriv.client.hud;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.pvp.Reach;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Locale;

/** Reach gostergesi: son vurusun mesafesi (renkli), son 10 vurusun cubuklari, istege bagli ortalama / en uzak. */
final class ReachHud extends BoundElement {

    private static final String[] FMT = {"%.1f", "%.2f", "%.3f"};
    private float cachedVal = Float.NaN;
    private int cachedDec = -1;
    private String cachedText = "";

    ReachHud() {
        super("reach", "Reach Göstergesi", c -> c.reachHud, (c, v) -> c.reachHud = v, c -> c.reachX, (c, v) -> c.reachX = v, c -> c.reachY,
                (c, v) -> c.reachY = v, c -> c.reachScale, (c, v) -> c.reachScale = v);
    }

    private static float alpha(ModConfig c) {
        if (c.reachFade <= 0f) return 1f;
        float since = (Util.getMillis() - Reach.lastAt()) / 1000f;
        if (since <= c.reachFade) return 1f;
        return Math.max(0f, 1f - (since - c.reachFade) / 0.6f);
    }

    @Override
    public boolean hasContent(ModConfig c) {
        return Reach.last() >= 0f && alpha(c) > 0.02f;
    }

    private String value(float v, ModConfig c) {
        int dec = Math.max(1, Math.min(3, c.reachDecimals));
        if (v != cachedVal || dec != cachedDec) {
            cachedVal = v;
            cachedDec = dec;
            cachedText = String.format(Locale.ROOT, FMT[dec - 1], v);
        }
        return cachedText;
    }

    @Override
    public void measure(ModConfig c, boolean sample) {
        Font f = Minecraft.getInstance().font;
        float v = sample && Reach.last() < 0 ? 2.87f : Reach.last();
        int tw = f.width("⟷ " + value(v, c) + " blok");
        int bw = c.reachHistory ? Reach.HIST * 4 : 0;
        int sw = c.reachStats ? f.width(stats(c, sample)) : 0;
        w = Math.max(Math.max(tw, bw), sw) + 8;
        h = 12 + (c.reachHistory ? 8 : 0) + (c.reachStats ? 10 : 0);
    }

    private int statsCount = -1, statsDec = -1;
    private String statsText = "";

    private String stats(ModConfig c, boolean sample) {
        if (sample && Reach.count() == 0) return "ort 2.71 · en 3.02 · 18 vuruş";
        int dec = Math.max(1, Math.min(3, c.reachDecimals));
        if (Reach.count() == statsCount && dec == statsDec) return statsText;
        statsCount = Reach.count();
        statsDec = dec;
        return statsText = "ort " + String.format(Locale.ROOT, FMT[dec - 1], Reach.average()) + " · en " + String.format(Locale.ROOT, FMT[dec - 1], Reach.best()) + " · "
                + Reach.count() + " vuruş";
    }

    @Override
    public void draw(GuiGraphics g, ModConfig c, boolean sample, boolean right) {
        Font f = Minecraft.getInstance().font;
        float a = sample ? 1f : alpha(c);
        if (a * 255 < 5) return;
        int ax = (int) (a * 255) << 24;
        Hud.background(g, 0, 0, w, h, c.reachBg * a);
        float v = sample && Reach.last() < 0 ? 2.87f : Reach.last();
        int col = c.reachColor ? Reach.color(v) : 0xFFFFFF;
        String t = "⟷ " + value(v, c);
        int tw = f.width(t + " blok");
        int x = (w - tw) / 2;
        Hud.text(g, f, t, x, 2, ax | col, c);
        Hud.text(g, f, " blok", x + f.width(t), 2, ax | 0xA0A0A0, c);
        int y = 12;
        if (c.reachHistory) {
            int n = Reach.historySize();
            int bx = (w - Reach.HIST * 4) / 2;
            for (int i = 0; i < Reach.HIST; i++) {
                float hv;
                if (sample && n == 0) hv = 2.2f + (i % 4) * 0.25f;
                else if (i < Reach.HIST - n) hv = -1f;
                else hv = Reach.history(i - (Reach.HIST - n));
                int x0 = bx + i * 4;
                g.fill(x0, y, x0 + 3, y + 6, ((int) (a * 70) << 24));
                if (hv >= 0f) {
                    int hh = Math.max(1, Math.min(6, Math.round(hv / 3.3f * 6f)));
                    g.fill(x0, y + 6 - hh, x0 + 3, y + 6, ax | (c.reachColor ? Reach.color(hv) : 0xFFFFFF));
                }
            }
            y += 8;
        }
        if (c.reachStats) {
            String s = stats(c, sample);
            Hud.text(g, f, s, (w - f.width(s)) / 2, y + 1, ax | 0xB0B0B0, c);
        }
    }
}
