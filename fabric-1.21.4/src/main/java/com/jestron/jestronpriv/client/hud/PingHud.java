package com.jestron.jestronpriv.client.hud;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.pvp.PingTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Ping paneli: anlik ping (renkli, sinyal cubuklari), son 60 olcumun grafigi, en dusuk / ortalama / en yuksek /
 * titreme ve istege bagli rakibin pingi. Ani sicramada panel kenari kirmizi yanip soner.
 */
final class PingHud extends BoundElement {

    private static final int GRAPH_H = 14;
    private final int[] buf = new int[PingTracker.N];
    private final int[] st = new int[4];
    private int lastPing = Integer.MIN_VALUE;
    private String pingText = "";

    PingHud() {
        super("ping", "Ping", c -> c.pingHud, (c, v) -> c.pingHud = v, c -> c.pingX, (c, v) -> c.pingX = v, c -> c.pingY, (c, v) -> c.pingY = v,
                c -> c.pingScale, (c, v) -> c.pingScale = v);
    }

    @Override
    public boolean hasContent(ModConfig c) {
        return Minecraft.getInstance().getConnection() != null;
    }

    private String text(int ms) {
        if (ms != lastPing) {
            lastPing = ms;
            pingText = ms < 0 ? "— ms" : ms + " ms";
        }
        return pingText;
    }

    private boolean showTarget(ModConfig c, boolean sample) {
        return c.pingTarget && (sample || PingTracker.targetPing() >= 0);
    }

    @Override
    public void measure(ModConfig c, boolean sample) {
        Font f = Minecraft.getInstance().font;
        int ms = sample && PingTracker.current() < 0 ? 42 : PingTracker.current();
        int top = (c.pingBars ? 14 : 0) + f.width("Ping ") + f.width(text(ms)) + f.width(" ●");
        int ww = Math.max(92, top + 8);
        if (c.pingStats) ww = Math.max(ww, f.width("▼000 ≈000 ▲000 ±00") + 8);
        if (showTarget(c, sample)) ww = Math.max(ww, f.width("Rakip: " + (sample ? "Kral_Ahmet" : PingTracker.targetName()) + " 000 ms") + 8);
        w = ww;
        h = 12 + (c.pingGraph ? GRAPH_H + 3 : 0) + (c.pingStats ? 10 : 0) + (showTarget(c, sample) ? 10 : 0);
    }

    @Override
    public void draw(GuiGraphics g, ModConfig c, boolean sample, boolean right) {
        Font f = Minecraft.getInstance().font;
        boolean demo = sample && PingTracker.current() < 0;
        int ms = demo ? 42 : PingTracker.current();
        int col = PingTracker.color(ms);
        Hud.background(g, 0, 0, w, h, c.pingBg);
        if (c.pingSpike && PingTracker.spike() && (System.currentTimeMillis() / 150) % 2 == 0) g.renderOutline(0, 0, w, h, 0xFFFF4040);
        int x = 4;
        if (c.pingBars) {
            int bars = PingTracker.bars(ms);
            for (int i = 0; i < 5; i++) {
                int bh = 2 + i * 2;
                g.fill(x + i * 2, 10 - bh, x + i * 2 + 1, 10, i < bars ? 0xFF000000 | col : 0x60FFFFFF);
            }
            x += 14;
        }
        Hud.text(g, f, "Ping ", x, 2, 0xFFB36BFF, c);
        x += f.width("Ping ");
        String t = text(ms);
        Hud.text(g, f, t, x, 2, 0xFF000000 | col, c);
        x += f.width(t);
        // anlik olcum mu sekme listesi mi
        boolean live = demo || PingTracker.live();
        Hud.text(g, f, " ●", x, 2, live ? 0xFF55FF7A : 0xFF888888, c);
        int y = 12;
        if (c.pingGraph) {
            int n = demo ? 0 : PingTracker.copySamples(buf);
            int gx0 = 4, gx1 = w - 4, gw = gx1 - gx0;
            g.fill(gx0, y, gx1, y + GRAPH_H, 0x40000000);
            int mx = 100;
            for (int i = 0; i < n; i++) mx = Math.max(mx, buf[i]);
            if (demo) {
                for (int i = 0; i < 30; i++) buf[i] = 38 + (int) (Math.sin(i * 0.7) * 8) + (i == 21 ? 60 : 0);
                n = 30;
            }
            float step = gw / (float) PingTracker.N;
            for (int i = 0; i < n; i++) {
                int v = buf[i];
                int bh = Math.max(1, Math.round(v / (float) mx * (GRAPH_H - 1)));
                int bx = gx1 - Math.round((n - i) * step);
                g.fill(bx, y + GRAPH_H - bh, Math.max(bx + 1, Math.round(bx + step)), y + GRAPH_H, 0xC0000000 | PingTracker.color(v));
            }
            y += GRAPH_H + 3;
        }
        if (c.pingStats) {
            boolean has = demo || PingTracker.stats(st);
            if (demo) {
                st[0] = 31;
                st[1] = 42;
                st[2] = 98;
                st[3] = 6;
            }
            String s = has ? "▼" + st[0] + " ≈" + st[1] + " ▲" + st[2] + " ±" + st[3] : "ölçülüyor…";
            Hud.text(g, f, s, 4, y, 0xFFB0B0B0, c);
            y += 10;
        }
        if (showTarget(c, sample)) {
            int tp = demo ? 85 : PingTracker.targetPing();
            String name = demo ? "Kral_Ahmet" : PingTracker.targetName();
            String pre = "Rakip: " + name + " ";
            Hud.text(g, f, pre, 4, y, 0xFFB0B0B0, c);
            Hud.text(g, f, tp < 0 ? "—" : tp + " ms", 4 + f.width(pre), y, 0xFF000000 | PingTracker.color(tp), c);
        }
    }
}
