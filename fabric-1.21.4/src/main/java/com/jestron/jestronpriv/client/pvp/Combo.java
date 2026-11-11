package com.jestron.jestronpriv.client.pvp;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Kombo sayaci: art arda (sen hasar almadan) sunucu onayli isabetler. Her isabette sayi buyuyerek atar, perdesi
 * yukselen bir nota calar; 5/10/15... kombolarda ozel ses. Hasar alinca ya da sure dolunca biter ve kisa bir sure
 * sonuc gosterilir. Altta kalan sureyi gosteren ince bir cubuk vardir.
 */
public final class Combo {

    private static int count, best, ended;
    private static long lastHit = -1000, endTick = -1000, ticks;
    private static boolean endedByHurt;
    private static long popNanos;
    // ayar ekranindan deneme: otomatik isabet dizisi (en iyi kombo kaydini bozmaz)
    private static int demoLeft;
    private static long demoNext;
    private static boolean demo, endedDemo;

    private Combo() {
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    public static int count() {
        return count;
    }

    public static int best() {
        return best;
    }

    private static int timeoutTicks(ModConfig c) {
        return Math.round(Math.max(1f, Math.min(6f, c.comboTimeout)) * 20f);
    }

    /** Sunucu onayli isabet. */
    public static void onHit() {
        ModConfig c = cfg();
        if (c == null || !c.combo) return;
        if (demoLeft > 0 || demo) {
            // gercek isabet denemeyi keser
            demoLeft = 0;
            demo = false;
            count = 0;
        }
        hit(c);
    }

    private static void hit(ModConfig c) {
        if (ticks - lastHit > timeoutTicks(c)) count = 0;
        count++;
        lastHit = ticks;
        if (!demo && count > best) best = count;
        popNanos = System.nanoTime();
        if (count >= 2 && c.comboSound) Sfx.self(sound(count), c.comboVolume);
    }

    /** Yerel oyuncu hasar aldi: kombo kirilir. */
    public static void onHurt() {
        if (count >= 2) finish(true);
        count = 0;
        demoLeft = 0;
        demo = false;
    }

    private static void finish(boolean hurt) {
        endedDemo = demo;
        ended = count;
        endTick = ticks;
        endedByHurt = hurt;
        count = 0;
    }

    public static void reset() {
        demoLeft = 0;
        demo = false;
        count = 0;
        ended = 0;
        endTick = -1000;
    }

    public static void tick(Minecraft mc) {
        ticks++;
        ModConfig c = cfg();
        if (c == null || mc.player == null) return;
        if (demoLeft > 0 && ticks >= demoNext) {
            demoLeft--;
            demoNext = ticks + 5;
            demo = true;
            hit(c);
        }
        if (count > 0 && ticks - lastHit > timeoutTicks(c)) {
            if (count >= 2) finish(false);
            count = 0;
            demo = false;
        }
    }

    /** Kombo sesi: yarim ton yukselen nota, kilometre taslarinda katmanli odul sesi. */
    public static Sfx.Layer[] sound(int n) {
        float p = (float) Math.min(2.0, 0.6 * Math.pow(2.0, (n - 2) / 12.0));
        if (n % 10 == 0) return new Sfx.Layer[]{Sfx.Layer.of("block.note_block.pling", 0.55f, p),
                Sfx.Layer.of("entity.player.levelup", 0.35f, 1.5f), Sfx.Layer.of("block.amethyst_block.chime", 0.8f, 1.8f, 2)};
        if (n % 5 == 0) return new Sfx.Layer[]{Sfx.Layer.of("block.note_block.pling", 0.55f, p),
                Sfx.Layer.of("entity.experience_orb.pickup", 0.5f, 1.3f), Sfx.Layer.of("block.note_block.bell", 0.4f, 1.6f, 1)};
        return new Sfx.Layer[]{Sfx.Layer.of("block.note_block.pling", 0.45f, p)};
    }

    /** Kombo seviyesine gore renk (gokkusagi 20+). */
    public static int tierColor(int n, double t) {
        if (n >= 20) return Palette.rainbow(t, 0);
        if (n >= 15) return 0xFF3B3B;
        if (n >= 10) return 0xFF9A2E;
        if (n >= 5) return 0xFFE047;
        return 0xFFFFFF;
    }

    // ------------------------------------------------------------------ HUD

    /** HUD'da gosterilecek bir sey var mi. */
    public static boolean active() {
        ModConfig c = cfg();
        if (c == null || (!c.combo && !demo && !endedDemo)) return false;
        return count >= 2 || (ended >= 2 && ticks - endTick < 30);
    }

    /**
     * (cx, cy) merkezli cizer; boyut HUD ogesinin olceginden gelir. sample: duzenleyici ornegi.
     */
    public static void renderAt(GuiGraphics g, float partial, int cx, int cy, boolean sample) {
        ModConfig c = cfg();
        Minecraft mc = Minecraft.getInstance();
        if (c == null) return;
        double t = System.nanoTime() / 1e9;
        Font font = mc.font;
        int n = sample && count < 2 ? 7 : count;
        if (n >= 2) {
            float since = sample ? 1f : (System.nanoTime() - popNanos) / 1e9f;
            float pop = since < 0.18f ? 1f + 0.55f * (float) Math.sin(Math.PI * since / 0.18f) : 1f;
            int col = tierColor(n, t);
            float shake = n >= 10 && since < 0.25f ? (float) Math.sin(since * 90f) * 1.2f * (1f - since / 0.25f) : 0f;
            String num = "×" + n;
            g.pose().pushPose();
            g.pose().translate(cx + shake, cy, 0);
            g.pose().scale(2.0f * pop, 2.0f * pop, 1f);
            g.drawString(font, num, -font.width(num) / 2, -4, 0xFF000000 | col, true);
            g.pose().popPose();
            String lab = "KOMBO";
            g.pose().pushPose();
            g.pose().translate(cx, cy + 12, 0);
            g.pose().scale(0.85f, 0.85f, 1f);
            g.drawString(font, lab, -font.width(lab) / 2, -4, 0xE0FFFFFF, true);
            g.pose().popPose();
            // kalan sure cubugu
            float left = sample && count < 2 ? 0.6f : 1f - Math.min(1f, (ticks - lastHit + partial) / (float) timeoutTicks(c));
            int bw = 30, by = cy + 18;
            g.fill(cx - bw, by, cx + bw, by + 1, 0x60000000);
            g.fill(cx - bw, by, cx - bw + Math.round(2 * bw * left), by + 1, 0xFF000000 | col);
        } else if (ended >= 2 && ticks - endTick < 30) {
            float k = (ticks - endTick + partial) / 30f;
            float a = k < 0.7f ? 1f : 1f - (k - 0.7f) / 0.3f;
            if (a > 0.05f) {
                String s = (endedByHurt ? "Kombo kırıldı: " : "Kombo: ") + ended + (!endedDemo && ended >= best && best > 2 ? "  ★ En iyi" : "");
                int col = endedByHurt ? 0xFF7070 : tierColor(ended, t);
                g.drawString(font, s, cx - font.width(s) / 2, cy - 4, Palette.argb(col, a), true);
            }
        }
    }

    /** Ayar ekranindan deneme. */
    public static void demo() {
        ModConfig c = cfg();
        if (c == null) return;
        count = 0;
        demo = true;
        demoLeft = 10;
        demoNext = ticks;
    }
}
