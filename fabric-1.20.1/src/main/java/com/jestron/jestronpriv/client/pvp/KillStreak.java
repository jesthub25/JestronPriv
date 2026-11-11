package com.jestron.jestronpriv.client.pvp;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Seri oldurme duyurusu. Coklu oldurme (pencere icinde art arda): Cifte, Uclu, Dortlu, Besli, Olum Makinesi.
 * Olmeden seri: 3 Seri Basladi, 5 Durdurulamaz, 7 Katliam, 10 Efsanevi, 15 Tanrisal, 20+ Olumsuz. Ilk oldurmede
 * Ilk Kan. Ekranda buyuyerek gelen parlayan baslik, kurban adi ve seri, kenarlarda renkli parlama, katmanli ses.
 * Oldugunde seri sifirlanir.
 */
public final class KillStreak {

    private static int multi, spree, total;
    private static long lastKill = -100000, ticks;
    private static boolean wasDead;

    // gosterilen duyuru
    private static String title, sub;
    private static int color, tier;
    private static long showNanos;
    private static boolean demoShown;

    private KillStreak() {
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    public static int spree() {
        return spree;
    }

    /** Dogrulanmis oldurme (KillDetector). */
    public static void onKill(String victim) {
        ModConfig c = cfg();
        if (c == null) return;
        int window = Math.round(Math.max(2f, Math.min(10f, c.streakWindow)) * 20f);
        multi = ticks - lastKill <= window ? multi + 1 : 1;
        lastKill = ticks;
        spree++;
        total++;
        if (!c.killStreak) return;
        boolean en = "EN".equals(c.streakLang);
        String t = null;
        int tr = 0;
        if (multi >= 2) {
            tr = Math.min(5, multi);
            t = multiTitle(multi, en);
        } else if (isSpreeMilestone(spree)) {
            tr = spreeTier(spree);
            t = spreeTitle(spree, en);
        } else if (total == 1) {
            tr = 1;
            t = en ? "FIRST BLOOD" : "İLK KAN";
        }
        if (t == null) return;
        show(t, (victim == null ? "?" : victim) + "  ·  " + (en ? "Streak " : "Seri ") + spree, tr);
    }

    static int spreeTier(int s) {
        return s >= 15 ? 5 : s >= 10 ? 4 : s >= 7 ? 3 : s >= 5 ? 2 : 1;
    }

    static boolean isSpreeMilestone(int s) {
        return s == 3 || s == 5 || s == 7 || s == 10 || s == 15 || (s >= 20 && s % 5 == 0);
    }

    static String multiTitle(int m, boolean en) {
        return switch (m) {
            case 2 -> en ? "DOUBLE KILL" : "ÇİFTE ÖLDÜRME";
            case 3 -> en ? "TRIPLE KILL" : "ÜÇLÜ ÖLDÜRME";
            case 4 -> en ? "QUADRA KILL" : "DÖRTLÜ ÖLDÜRME";
            case 5 -> en ? "PENTA KILL" : "BEŞLİ ÖLDÜRME";
            default -> (en ? "MONSTER KILL ×" : "ÖLÜM MAKİNESİ ×") + m;
        };
    }

    static String spreeTitle(int s, boolean en) {
        if (s >= 20) return (en ? "IMMORTAL ×" : "ÖLÜMSÜZ ×") + s;
        return switch (s) {
            case 3 -> en ? "KILLING SPREE" : "SERİ BAŞLADI";
            case 5 -> en ? "UNSTOPPABLE" : "DURDURULAMAZ";
            case 7 -> en ? "RAMPAGE" : "KATLİAM";
            case 10 -> en ? "LEGENDARY" : "EFSANEVİ";
            default -> en ? "GODLIKE" : "TANRISAL";
        };
    }

    static int tierColor(int tr) {
        return switch (tr) {
            case 1 -> 0xFFE047;
            case 2 -> 0xFF9A2E;
            case 3 -> 0xFF3B3B;
            case 4 -> 0xC86BFF;
            default -> 0x3DF0E0;
        };
    }

    /** Seviyeye gore katmanli duyuru sesi. */
    public static Sfx.Layer[] sound(int tr) {
        return switch (tr) {
            case 1 -> new Sfx.Layer[]{Sfx.Layer.of("entity.player.levelup", 0.55f, 1.25f), Sfx.Layer.of("block.note_block.bell", 0.6f, 1.5f, 2)};
            case 2 -> new Sfx.Layer[]{Sfx.Layer.of("entity.player.levelup", 0.65f, 1.0f), Sfx.Layer.of("block.bell.use", 0.5f, 1.3f),
                    Sfx.Layer.of("block.note_block.bell", 0.6f, 2.0f, 3)};
            case 3 -> new Sfx.Layer[]{Sfx.Layer.of("item.goat_horn.sound.1", 0.35f, 1.25f), Sfx.Layer.of("entity.firework_rocket.large_blast", 0.6f, 1.0f),
                    Sfx.Layer.of("entity.player.levelup", 0.55f, 1.4f, 3)};
            case 4 -> new Sfx.Layer[]{Sfx.Layer.of("item.goat_horn.sound.0", 0.4f, 1.05f), Sfx.Layer.of("ui.toast.challenge_complete", 0.5f, 1.0f),
                    Sfx.Layer.of("entity.firework_rocket.twinkle", 0.6f, 1.0f, 4)};
            default -> new Sfx.Layer[]{Sfx.Layer.of("item.goat_horn.sound.2", 0.4f, 0.95f), Sfx.Layer.of("entity.ender_dragon.growl", 0.22f, 1.4f),
                    Sfx.Layer.of("ui.toast.challenge_complete", 0.55f, 1.15f, 2), Sfx.Layer.of("entity.firework_rocket.twinkle", 0.6f, 0.9f, 5)};
        };
    }

    private static void show(String t, String s, int tr) {
        ModConfig c = cfg();
        title = t;
        sub = s;
        tier = Math.max(1, Math.min(5, tr));
        color = tierColor(tier);
        showNanos = System.nanoTime();
        demoShown = false;
        if (c != null && c.streakSound) Sfx.self(sound(tier), c.streakVolume);
    }

    public static void tick(Minecraft mc) {
        ticks++;
        var p = mc.player;
        if (p == null) return;
        boolean dead = !p.isAlive() || p.getHealth() <= 0f;
        if (dead && !wasDead) {
            spree = 0;
            multi = 0;
        }
        wasDead = dead;
    }

    // ------------------------------------------------------------------ HUD

    private static final float DUR = 2.6f;

    private static float elapsed() {
        return (System.nanoTime() - showNanos) / 1e9f;
    }

    /** Ekranda duyuru var mi. */
    public static boolean active() {
        ModConfig c = cfg();
        if (c == null || title == null || (!c.killStreak && !demoShown)) return false;
        if (elapsed() > DUR) {
            title = null;
            return false;
        }
        return true;
    }

    private static String shownTitle(boolean sample) {
        return title != null && active() ? title : sample ? "ÇİFTE ÖLDÜRME" : null;
    }

    private static int shownTier(boolean sample) {
        return title != null && active() ? tier : 2;
    }

    /** Baslik olcegi (ogenin kendi olcegi uzerine). */
    static float baseScale(int tr) {
        return 2.6f + 0.25f * tr;
    }

    /** HUD ogesi icin olcu: {genislik, yukseklik}. */
    public static void size(boolean sample, int[] out) {
        String t = shownTitle(sample);
        Font f = Minecraft.getInstance().font;
        if (t == null) {
            out[0] = out[1] = 0;
            return;
        }
        float b = baseScale(shownTier(sample));
        out[0] = Math.max(200, Math.round(f.width(t) * b * 1.12f) + 12);
        out[1] = Math.round(11 * b) + 24;   // baslik + alt yazi
    }

    /** Kenar parlamasi: tum ekran (konumdan bagimsiz). */
    public static void renderFlash(GuiGraphics g) {
        ModConfig c = cfg();
        if (c == null || !c.streakFlash || !active()) return;
        float t = elapsed();
        if (t >= 0.7f) return;
        int w = g.guiWidth(), h = g.guiHeight();
        float fa = 0.38f * (1f - t / 0.7f);
        int edge = Palette.argb(color, fa), clear = Palette.argb(color, 0f);
        int ew = Math.max(18, w / 9), eh = Math.max(14, h / 8);
        g.fillGradient(0, 0, w, eh, edge, clear);
        g.fillGradient(0, h - eh, w, h, clear, edge);
        sideGradient(g, 0, 0, ew, h, edge, clear);
        sideGradient(g, w - ew, 0, w, h, clear, edge);
    }

    /** Baslik + alt yazi, (cx, cy) baslik merkezi. */
    public static void renderAt(GuiGraphics g, int cx, int cy, boolean sample) {
        Minecraft mc = Minecraft.getInstance();
        boolean real = title != null && active();
        if (!real && !sample) return;
        String ttl = real ? title : "ÇİFTE ÖLDÜRME";
        String sb = real ? sub : "Steve  ·  Seri 2";
        int tr = real ? tier : 2;
        int col0 = real ? color : tierColor(2);
        float t = real ? elapsed() : 1f;
        float a = t < 0.12f ? t / 0.12f : (t < DUR - 0.5f ? 1f : (DUR - t) / 0.5f);
        // Font, alfasi ~0.016'dan kucuk rengi TAM OPAK cizer: o kadar soluk yaziyi hic cizme
        if (a * 255f < 5f) return;
        Font font = mc.font;
        // buyuyerek gelen baslik (tasan acilis) + titreme
        float pop = t < 0.28f ? easeOutBack(t / 0.28f) : 1f;
        float s = baseScale(tr) * Math.max(0.05f, pop);
        float shake = t < 0.4f ? (float) Math.sin(t * 80f) * 1.5f * (1f - t / 0.4f) * tr / 3f : 0f;
        int tw = font.width(ttl);
        // parlama: arkada genis, soluk kopyalar
        for (int k = 3; k >= 1 && a * 0.12f * 255f >= 5f; k--) {
            g.pose().pushPose();
            g.pose().translate(cx + shake, cy, 0);
            g.pose().scale(s * (1f + 0.035f * k), s * (1f + 0.06f * k), 1f);
            g.drawString(font, ttl, -tw / 2, -4, Palette.argb(col0, a * 0.12f), false);
            g.pose().popPose();
        }
        // gradyanli harfler: soldan saga ana renkten beyaza dogru akan parlaklik
        g.pose().pushPose();
        g.pose().translate(cx + shake, cy, 0);
        g.pose().scale(s, s, 1f);
        int x = -tw / 2;
        double now = System.nanoTime() / 1e9;
        for (int i = 0; i < ttl.length(); ) {
            int cp = ttl.codePointAt(i);
            String ch = new String(Character.toChars(cp));
            float wave = 0.5f + 0.5f * (float) Math.sin(now * 6.0 - i * 0.45);
            int col = tr >= 5 ? Palette.rainbow(now + i * 0.04, 0) : Palette.mix(col0, 0xFFFFFF, wave * 0.45f);
            g.drawString(font, ch, x, -4, Palette.argb(col, a), true);
            x += font.width(ch);
            i += Character.charCount(cp);
        }
        g.pose().popPose();
        // alt yazi
        float sa = t < 0.25f ? 0f : a;
        if (sa > 0.05f && sb != null) {
            g.pose().pushPose();
            g.pose().translate(cx, cy + 6 * s + 6, 0);
            g.pose().scale(1.15f, 1.15f, 1f);
            g.drawString(font, sb, -font.width(sb) / 2, 0, Palette.argb(0xE8E8E8, sa), true);
            g.pose().popPose();
        }
    }

    private static void sideGradient(GuiGraphics g, int x0, int y0, int x1, int y1, int left, int right) {
        // yatay gradyan: ince dikey seritlerle
        int n = Math.max(1, (x1 - x0) / 2);
        for (int i = 0; i < n; i++) {
            float k = (i + 0.5f) / n;
            int a0 = (left >>> 24), a1 = (right >>> 24);
            int a = Math.round(a0 + (a1 - a0) * k);
            int col = (a << 24) | (left & 0xFFFFFF);
            int xa = x0 + (x1 - x0) * i / n, xb = x0 + (x1 - x0) * (i + 1) / n;
            if (a > 0) g.fill(xa, y0, xb, y1, col);
        }
    }

    private static float easeOutBack(float u) {
        float c1 = 1.9f, x = u - 1f;
        return 1f + (c1 + 1f) * x * x * x + c1 * x * x;
    }

    /** Ayar ekranindan deneme: sirayla seviyeleri gosterir. */
    private static int demoStep;
    private static final int[] DEMO_SPREES = {3, 5, 7, 10, 15};

    public static void demo() {
        ModConfig c = cfg();
        if (c == null) return;
        boolean en = "EN".equals(c.streakLang);
        demoStep = demoStep % 11 + 1;
        String st = en ? "Streak " : "Seri ";
        if (demoStep == 1) {
            show(en ? "FIRST BLOOD" : "İLK KAN", "Steve  ·  " + st + 1, 1);
        } else if (demoStep <= 6) {
            show(multiTitle(demoStep, en), "Steve  ·  " + st + demoStep, Math.min(5, demoStep));
        } else {
            int sp = DEMO_SPREES[demoStep - 7];
            show(spreeTitle(sp, en), "Steve  ·  " + st + sp, spreeTier(sp));
        }
        demoShown = true;
    }
}
