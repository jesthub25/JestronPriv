package com.jestron.jestronpriv.client.pvp;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;

import java.util.Locale;

/**
 * Ozel nisangah: 50 sekil (bazilari hareketli), boyut, kalinlik, bosluk, renk / gokkusagi, opaklik, siyah kontur, orta nokta,
 * dinamik acilma (hareket ederken ve saldiri beklerken) ve isabet isareti (vurus beyaz, kritik altin, oldurme kizil).
 * Saldiri gostergesi (vanilla) aynen kalir.
 */
public final class Crosshair {

    public enum Shape {
        ARTI("Artı"), ARTI_NOKTA("Artı + Nokta", true), NOKTA("Nokta", true), DAIRE("Daire"), HALKA_NOKTA("Halka + Nokta", true),
        X("Çarpı"), T("T"), KOSELI("Köşeli"), OK("Ok (V)"),
        ARTI_HALKA("Artı + Halka"), X_NOKTA("Çarpı + Nokta", true), X_HALKA("Çarpı + Halka"), YATAY("Yatay Çizgi"), DIKEY("Dikey Çizgi"),
        TERS_T("Ters T"), KOSE_L("Köşe (L)"), KARE("Kare"), KARE_NOKTA("Kare + Nokta", true), ELMAS("Elmas"),
        ELMAS_NOKTA("Elmas + Nokta", true), UCGEN("Üçgen"), UCGEN_TERS("Ters Üçgen"), ALTIGEN("Altıgen"), SEKIZGEN("Sekizgen"),
        YILDIZ("Yıldız"), PARANTEZ("Parantez ( )"), KOSELI_PARANTEZ("Köşeli Parantez [ ]"), KUM_SAATI("Kum Saati"), HEDEF("Hedef", true),
        NISANCI("Keskin Nişancı", true), UZUN_ARTI("Uzun Artı"), MINI_ARTI("Mini Artı"), CIFT_HALKA("Çift Halka"), YAY("Yay (alt)"),
        GULUMSEME("Gülümseme"), KALP("Kalp"), OK_UST("Ters Ok (üstte)"), CIFT_OK("Çift Ok"), UC_NOKTA("Üç Nokta"),
        DORT_NOKTA("Dört Nokta"), NOKTA_HALKA("Nokta Halkası"), RADAR("Radar", true), PERVANE("Pervane (3 kol)"), ALTI_KOL("Altı Kol"),
        SEKIZ_KOL("Sekiz Kol"), KESIK_HALKA("Kesik Halka"), ELMAS_ARTI("Elmas + Artı"), KARE_ARTI("Kare + Artı"), DONEN("Dönen Çarpı"),
        NABIZ("Nabız Halkası");

        public final String label;
        /** Sekilde zaten orta nokta var (ayrica nokta eklenmez). */
        public final boolean hasDot;

        Shape(String label) {
            this(label, false);
        }

        Shape(String label, boolean hasDot) {
            this.label = label;
            this.hasDot = hasDot;
        }

        public Shape previous() {
            return values()[(ordinal() + values().length - 1) % values().length];
        }

        public Shape next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public static Shape parse(String s) {
            if (s != null) {
                String n = s.trim().toUpperCase(Locale.ROOT);
                for (Shape v : values()) if (v.name().equals(n)) return v;
            }
            return ARTI;
        }
    }

    public static final int HIT = 0, HIT_CRIT = 1, HIT_KILL = 2;
    private static long hitNanos = -1;
    private static int hitKind;
    private static float dyn;
    private static long lastNanos;

    private Crosshair() {
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    public static boolean active() {
        ModConfig c = cfg();
        return c != null && c.chEnabled;
    }

    /** Isabet isareti (kritik / oldurme daha onemli: zayif olan guclu olani ezmez). */
    public static void onHit(int kind) {
        long now = System.nanoTime();
        if (hitNanos > 0 && (now - hitNanos) / 1e9 < 0.12 && kind < hitKind) return;
        hitKind = kind;
        hitNanos = now;
    }

    /** Oyunda: ekran ortasina ciz. */
    public static void drawInGame(GuiGraphics g) {
        ModConfig c = cfg();
        if (c == null) return;
        long now = System.nanoTime();
        float dt = lastNanos == 0 ? 0 : Math.min(0.1f, (now - lastNanos) / 1e9f);
        lastNanos = now;
        // dinamik acilma: yatay hareket + saldiri bekleme
        float target = 0;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (c.chDynamic && p != null) {
            double vx = p.getX() - p.xo, vz = p.getZ() - p.zo;
            float sp = (float) Math.sqrt(vx * vx + vz * vz);
            target += Math.min(3f, sp * 14f);
            if (!p.onGround()) target += 1.5f;
            float atk = p.getAttackStrengthScale(0f);
            if (atk < 1f) target += (1f - atk) * 3f;
        }
        dyn += (target - dyn) * (1f - (float) Math.exp(-dt * 14f));
        // vanilla 15x15 nisangahin orta pikseliyle birebir ayni merkez
        draw(g, (g.guiWidth() - 15) / 2 + 7, (g.guiHeight() - 15) / 2 + 7, c, dyn, now);
    }

    /** Ayar ekrani onizlemesi icin de kullanilir. extra = dinamik ek bosluk (px). */
    public static void draw(GuiGraphics g, int cx, int cy, ModConfig c, float extra, long nowNanos) {
        int col = c.chRainbow ? Palette.rainbow(nowNanos / 1e9, 0) : c.chColor & 0xFFFFFF;
        float op = Math.max(0.1f, Math.min(1f, c.chOpacity));
        int main = Palette.argb(col, op);
        int out = Palette.argb(0x000000, op * 0.85f);
        int L = Math.round(Math.max(1f, Math.min(16f, c.chSize)));
        int T = Math.round(Math.max(1f, Math.min(4f, c.chThickness)));
        int G = Math.round(Math.max(0f, Math.min(10f, c.chGap)) + extra);
        Shape s = Shape.parse(c.chShape);
        boolean ol = c.chOutline;
        // once kontur (1 px tasar), sonra renk
        for (int pass = ol ? 0 : 1; pass < 2; pass++) {
            int k = pass == 0 ? 1 : 0, cc = pass == 0 ? out : main;
            shape(g, s, cx, cy, L, T, G, k, cc, nowNanos);
            if (c.chDot && !s.hasDot) dot(g, cx, cy, T, k, cc);
        }
        // isabet isareti
        if (c.chHitMarker && hitNanos > 0) {
            float t = (nowNanos - hitNanos) / 1e9f;
            float life = hitKind == HIT_KILL ? 0.5f : hitKind == HIT_CRIT ? 0.35f : 0.25f;
            if (t >= 0 && t < life) {
                float a = 1f - t / life;
                int hc = hitKind == HIT_KILL ? 0xFF3030 : hitKind == HIT_CRIT ? 0xFFC830 : 0xFFFFFF;
                int len = hitKind == HIT_KILL ? 7 : hitKind == HIT_CRIT ? 6 : 4;
                int gap = G + 3 + Math.round((1f - a) * 2f);
                for (int pass = 0; pass < 2; pass++) {
                    int k = pass == 0 ? 1 : 0;
                    int cc = pass == 0 ? Palette.argb(0, a * 0.8f) : Palette.argb(hc, a);
                    diag(g, cx, cy, len, hitKind == HIT_KILL ? 2 : 1, gap, k, cc, true);
                }
            }
        }
    }

    private static void rect(GuiGraphics g, int x0, int y0, int x1, int y1, int k, int col) {
        g.fill(x0 - k, y0 - k, x1 + k, y1 + k, col);
    }

    /** Kollar (sol, sag, ust, alt ayri ayri). */
    private static void arms(GuiGraphics g, int cx, int cy, int L, int T, int G, int k, int col, boolean left, boolean right, boolean up, boolean down) {
        int h0 = -(T / 2), h1 = h0 + T;
        if (left) rect(g, cx - G - L, cy + h0, cx - G, cy + h1, k, col);
        if (right) rect(g, cx + G + 1, cy + h0, cx + G + 1 + L, cy + h1, k, col);
        if (up) rect(g, cx + h0, cy - G - L, cx + h1, cy - G, k, col);
        if (down) rect(g, cx + h0, cy + G + 1, cx + h1, cy + G + 1 + L, k, col);
    }

    private static void dot(GuiGraphics g, int cx, int cy, int size, int k, int col) {
        int h0 = -(size / 2);
        rect(g, cx + h0, cy + h0, cx + h0 + size, cy + h0 + size, k, col);
    }

    /** Halka: satir satir dolu araliklar (piksel basina degil, satir basina en cok 2 dikdortgen). */
    private static void ring(GuiGraphics g, int cx, int cy, int r, int T, int k, int col) {
        r = Math.max(2, r);
        float rin = r - T / 2f - k, rout = r + T / 2f + k;
        float rin2 = rin * rin, rout2 = rout * rout;
        int ext = r + T + 1;
        for (int y = -ext; y <= ext; y++) {
            int runStart = Integer.MIN_VALUE;
            for (int x = -ext; x <= ext + 1; x++) {
                int d2 = x * x + y * y;
                boolean in = x <= ext && d2 >= rin2 && d2 <= rout2;
                if (in && runStart == Integer.MIN_VALUE) runStart = x;
                if (!in && runStart != Integer.MIN_VALUE) {
                    g.fill(cx + runStart, cy + y, cx + x, cy + y + 1, col);
                    runStart = Integer.MIN_VALUE;
                }
            }
        }
    }

    /** Capraz kollar (X) ya da isabet isareti. */
    private static void diag(GuiGraphics g, int cx, int cy, int L, int T, int G, int k, int col, boolean all) {
        for (int i = 0; i < L; i++) {
            int d = G + i;
            for (int sx = -1; sx <= 1; sx += 2) {
                for (int sy = -1; sy <= 1; sy += 2) {
                    if (!all && sy < 0) continue;
                    int px = cx + (sx > 0 ? d + 1 : -d - T), py = cy + (sy > 0 ? d + 1 : -d - T);
                    g.fill(px - k, py - k, px + T + k, py + T + k, col);
                }
            }
        }
    }

    /** Kose parantezleri. */
    private static void corners(GuiGraphics g, int cx, int cy, int L, int T, int G, int k, int col) {
        int r = G + Math.max(2, L / 2) + 2, arm = Math.max(2, L / 2 + 1);
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sy = -1; sy <= 1; sy += 2) {
                int x = sx > 0 ? cx + r : cx - r - T + 1, y = sy > 0 ? cy + r : cy - r - T + 1;
                // yatay parca (merkeze dogru) ve dikey parca
                int hx0 = sx > 0 ? x - arm + T : x, hx1 = sx > 0 ? x + T : x + arm;
                rect(g, hx0, y, hx1, y + T, k, col);
                int vy0 = sy > 0 ? y - arm + T : y, vy1 = sy > 0 ? y + T : y + arm;
                rect(g, x, vy0, x + T, vy1, k, col);
            }
        }
    }

    /** Merkezin altinda V (ok ucu); up = merkezin ustunde ters V. */
    private static void chevron(GuiGraphics g, int cx, int cy, int L, int T, int G, int k, int col, boolean up) {
        for (int i = 0; i < L; i++) {
            int y = up ? cy - G - i - T + 1 : cy + G + i;
            g.fill(cx + i - k, y - k, cx + i + T + k, y + T + k, col);
            g.fill(cx - i - T + 1 - k, y - k, cx - i + 1 + k, y + T + k, col);
        }
    }

    /** Tek bir sekli cizer (kontur gecisinde k = 1). */
    private static void shape(GuiGraphics g, Shape s, int cx, int cy, int L, int T, int G, int k, int col, long nowNanos) {
        int r = G + Math.max(2, L / 2) + 1; // kapali sekillerin yaricapi
        switch (s) {
            case ARTI, ARTI_NOKTA -> {
                arms(g, cx, cy, L, T, G, k, col, true, true, true, true);
                if (s == Shape.ARTI_NOKTA) dot(g, cx, cy, T, k, col);
            }
            case T -> arms(g, cx, cy, L, T, G, k, col, true, true, false, true);
            case TERS_T -> arms(g, cx, cy, L, T, G, k, col, true, true, true, false);
            case YATAY -> arms(g, cx, cy, L, T, G, k, col, true, true, false, false);
            case DIKEY -> arms(g, cx, cy, L, T, G, k, col, false, false, true, true);
            case KOSE_L -> arms(g, cx, cy, L, T, G, k, col, false, true, false, true);
            case UZUN_ARTI -> arms(g, cx, cy, Math.min(L * 2, 24), T, G, k, col, true, true, true, true);
            case MINI_ARTI -> arms(g, cx, cy, Math.max(1, (L + 1) / 2), T, G / 2, k, col, true, true, true, true);
            case NOKTA -> dot(g, cx, cy, Math.max(T, Math.round(L / 3f)) | 1, k, col); // tek sayi: tam ortali
            case DAIRE, HALKA_NOKTA -> {
                ring(g, cx, cy, L + G, T, k, col);
                if (s == Shape.HALKA_NOKTA) dot(g, cx, cy, T, k, col);
            }
            case ARTI_HALKA -> {
                arms(g, cx, cy, L, T, G, k, col, true, true, true, true);
                ring(g, cx, cy, G + L + 2, T, k, col);
            }
            case X, X_NOKTA -> {
                diag(g, cx, cy, L, T, G, k, col, true);
                if (s == Shape.X_NOKTA) dot(g, cx, cy, T, k, col);
            }
            case X_HALKA -> {
                diag(g, cx, cy, L, T, G, k, col, true);
                ring(g, cx, cy, Math.round((G + L) * 1.414f) + 2, T, k, col);
            }
            case KOSELI -> corners(g, cx, cy, L, T, G, k, col);
            case OK -> chevron(g, cx, cy, L, T, G, k, col, false);
            case OK_UST -> chevron(g, cx, cy, L, T, G, k, col, true);
            case CIFT_OK -> {
                chevron(g, cx, cy, Math.max(2, L * 2 / 3), T, G, k, col, false);
                chevron(g, cx, cy, Math.max(2, L * 2 / 3), T, G + T + 3, k, col, false);
            }
            case KARE, KARE_NOKTA, KARE_ARTI -> {
                square(g, cx, cy, r, T, k, col);
                if (s == Shape.KARE_NOKTA) dot(g, cx, cy, T, k, col);
                if (s == Shape.KARE_ARTI) arms(g, cx, cy, Math.max(1, r - G - T - 1), T, G, k, col, true, true, true, true);
            }
            case ELMAS, ELMAS_NOKTA -> {
                poly(g, cx, cy, r + 1, 4, -90, 1, T, k, col);
                if (s == Shape.ELMAS_NOKTA) dot(g, cx, cy, T, k, col);
            }
            case ELMAS_ARTI -> {
                poly(g, cx, cy, r + 1, 4, -90, 1, T, k, col);
                arms(g, cx, cy, Math.max(2, L / 2), T, r + 3, k, col, true, true, true, true);
            }
            case UCGEN -> poly(g, cx, cy, r + 1, 3, -90, 1, T, k, col);
            case UCGEN_TERS -> poly(g, cx, cy, r + 1, 3, 90, 1, T, k, col);
            case ALTIGEN -> poly(g, cx, cy, r + 1, 6, 0, 1, T, k, col);
            case SEKIZGEN -> poly(g, cx, cy, r + 1, 8, 22.5f, 1, T, k, col);
            case YILDIZ -> poly(g, cx, cy, r + 2, 5, -90, 2, T, k, col);
            case PARANTEZ -> {
                arc(g, cx, cy, L + G, T, 130, 230, k, col);
                arc(g, cx, cy, L + G, T, -50, 50, k, col);
            }
            case KESIK_HALKA -> {
                for (int q = 0; q < 4; q++) arc(g, cx, cy, L + G, T, q * 90 + 18, q * 90 + 72, k, col);
            }
            case YAY -> arc(g, cx, cy, L + G, T, 20, 160, k, col);
            case KOSELI_PARANTEZ -> brackets(g, cx, cy, L, T, G, k, col);
            case KUM_SAATI -> {
                float h = G + L, w = Math.max(2, L * 0.7f);
                line(g, cx, cy, -w, -h, w, -h, T, k, col);
                line(g, cx, cy, -w, -h, 0, -G, T, k, col);
                line(g, cx, cy, w, -h, 0, -G, T, k, col);
                line(g, cx, cy, -w, h, w, h, T, k, col);
                line(g, cx, cy, -w, h, 0, G, T, k, col);
                line(g, cx, cy, w, h, 0, G, T, k, col);
            }
            case HEDEF -> {
                ring(g, cx, cy, G + L, T, k, col);
                ring(g, cx, cy, Math.max(2, (G + L) / 2), T, k, col);
                dot(g, cx, cy, T, k, col);
            }
            case NISANCI -> {
                int rr = G + L + 2;
                ring(g, cx, cy, rr, 1, k, col);
                arms(g, cx, cy, L + 3, T, G + L / 2 + 2, k, col, true, true, true, true);
                dot(g, cx, cy, T, k, col);
            }
            case RADAR -> {
                int rr = G + Math.max(2, L / 2) + 2;
                ring(g, cx, cy, rr, T, k, col);
                arms(g, cx, cy, Math.max(2, L / 2 + 2), T, rr + T + 1, k, col, true, true, true, true);
                dot(g, cx, cy, T, k, col);
            }
            case CIFT_HALKA -> {
                int rr = G + Math.max(2, L / 2) + 1;
                ring(g, cx, cy, rr, T, k, col);
                ring(g, cx, cy, rr + T + 3, T, k, col);
            }
            case GULUMSEME -> {
                int rr = G + L;
                ring(g, cx, cy, rr, T, k, col);
                int e = Math.max(1, Math.round(rr * 0.35f));
                dot(g, cx - e, cy - e, T, k, col);
                dot(g, cx + e, cy - e, T, k, col);
                arc(g, cx, cy, Math.max(2, Math.round(rr * 0.55f)), T, 25, 155, k, col);
            }
            case KALP -> heart(g, cx, cy, G + L, T, k, col);
            case UC_NOKTA -> spokes(g, cx, cy, 3, -90, 0, 0, G + Math.max(2, L / 2), T, k, col);
            case DORT_NOKTA -> spokes(g, cx, cy, 4, 0, 0, 0, G + Math.max(2, L / 2), T, k, col);
            case NOKTA_HALKA -> spokes(g, cx, cy, 8, 0, 0, 0, G + L, T, k, col);
            case PERVANE -> spokes(g, cx, cy, 3, -90, G + 1, G + L, 0, T, k, col);
            case ALTI_KOL -> spokes(g, cx, cy, 6, -90, G + 1, G + L, 0, T, k, col);
            case SEKIZ_KOL -> {
                arms(g, cx, cy, L, T, G, k, col, true, true, true, true);
                diag(g, cx, cy, Math.max(1, L * 2 / 3), T, G, k, col, true);
            }
            case DONEN -> {
                float a = (float) ((nowNanos / 1e9 * 90.0) % 90.0) + 45f;
                spokes(g, cx, cy, 4, a, G + 1, G + L, 0, T, k, col);
            }
            case NABIZ -> {
                double ph = nowNanos / 1e9 * Math.PI * 2 * 1.2;
                ring(g, cx, cy, L + G + (int) Math.round(Math.sin(ph) * 1.5), T, k, col);
                dot(g, cx, cy, T, k, col);
            }
        }
    }

    /** Merkeze gore simetrik yuvarlama (yarim degerler sifirdan uzaga; kucuk kayan nokta hatasi temizlenir). */
    private static int sround(float v) {
        double q = Math.rint(v * 1024.0) / 1024.0;
        return (int) (Math.signum(q) * Math.floor(Math.abs(q) + 0.5));
    }

    /** Merkez pikseline gore (dx, dy) noktasina T x T blok (sag-sol / ust-alt ayna simetrik). */
    private static void block(GuiGraphics g, int cx, int cy, float dx, float dy, int T, int k, int col) {
        int x = cx + sround(dx) - (T - 1) / 2, y = cy + sround(dy) - (T - 1) / 2;
        g.fill(x - k, y - k, x + T + k, y + T + k, col);
    }

    /** Merkeze gore (x0, y0) -> (x1, y1) kalin cizgi. */
    private static void line(GuiGraphics g, int cx, int cy, float x0, float y0, float x1, float y1, int T, int k, int col) {
        int n = Math.max(1, Math.round(Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0))));
        for (int i = 0; i <= n; i++) {
            float t = i / (float) n;
            block(g, cx, cy, x0 + (x1 - x0) * t, y0 + (y1 - y0) * t, T, k, col);
        }
    }

    /** Duzgun cokgen (step 2 = yildiz). */
    private static void poly(GuiGraphics g, int cx, int cy, float r, int n, float rotDeg, int step, int T, int k, int col) {
        for (int i = 0; i < n; i++) {
            double a0 = Math.toRadians(rotDeg + 360.0 * i / n), a1 = Math.toRadians(rotDeg + 360.0 * ((i + step) % n) / n);
            line(g, cx, cy, (float) (Math.cos(a0) * r), (float) (Math.sin(a0) * r), (float) (Math.cos(a1) * r), (float) (Math.sin(a1) * r), T, k, col);
        }
    }

    /** n esit aralikli kol (r0..r1) ya da nokta (r0 = r1 = 0, rDot yaricapinda). */
    private static void spokes(GuiGraphics g, int cx, int cy, int n, float rotDeg, float r0, float r1, float rDot, int T, int k, int col) {
        for (int i = 0; i < n; i++) {
            double a = Math.toRadians(rotDeg + 360.0 * i / n);
            float ca = (float) Math.cos(a), sa = (float) Math.sin(a);
            if (r1 > 0) line(g, cx, cy, ca * r0, sa * r0, ca * r1, sa * r1, T, k, col);
            else block(g, cx, cy, ca * rDot, sa * rDot, Math.max(T, 2) | 1, k, col);
        }
    }

    /** Kare cerceve (yari kenar r). */
    private static void square(GuiGraphics g, int cx, int cy, int r, int T, int k, int col) {
        rect(g, cx - r, cy - r, cx + r + 1, cy - r + T, k, col);
        rect(g, cx - r, cy + r + 1 - T, cx + r + 1, cy + r + 1, k, col);
        rect(g, cx - r, cy - r, cx - r + T, cy + r + 1, k, col);
        rect(g, cx + r + 1 - T, cy - r, cx + r + 1, cy + r + 1, k, col);
    }

    /** Yay (derece; ekran y asagi, 0 = sag, 90 = alt). */
    private static void arc(GuiGraphics g, int cx, int cy, int r, int T, float a0, float a1, int k, int col) {
        r = Math.max(2, r);
        float rin = r - T / 2f - k, rout = r + T / 2f + k;
        int ext = r + T + 1;
        for (int y = -ext; y <= ext; y++) {
            int runStart = Integer.MIN_VALUE;
            for (int x = -ext; x <= ext + 1; x++) {
                boolean in = false;
                if (x <= ext) {
                    float d = (float) Math.sqrt(x * x + y * y);
                    if (d >= rin && d <= rout) {
                        float ang = (float) Math.toDegrees(Math.atan2(y, x));
                        in = angleIn(ang, a0, a1);
                    }
                }
                if (in && runStart == Integer.MIN_VALUE) runStart = x;
                if (!in && runStart != Integer.MIN_VALUE) {
                    g.fill(cx + runStart, cy + y, cx + x, cy + y + 1, col);
                    runStart = Integer.MIN_VALUE;
                }
            }
        }
    }

    private static boolean angleIn(float a, float a0, float a1) {
        float span = a1 - a0;
        float d = ((a - a0) % 360f + 360f) % 360f;
        return d <= span;
    }

    /** Koseli parantez [ ]. */
    private static void brackets(GuiGraphics g, int cx, int cy, int L, int T, int G, int k, int col) {
        int x = G + Math.max(2, L / 2) + 1, h = Math.max(3, L / 2 + 2), stub = Math.max(2, L / 3 + 1);
        for (int sx = -1; sx <= 1; sx += 2) {
            int xa = sx < 0 ? cx - x - T + 1 : cx + x, xb = xa + T;
            rect(g, xa, cy - h, xb, cy + h + 1, k, col);
            int s0 = sx < 0 ? xb : xa - stub, s1 = sx < 0 ? xb + stub : xa;
            rect(g, s0, cy - h, s1, cy - h + T, k, col);
            rect(g, s0, cy + h + 1 - T, s1, cy + h + 1, k, col);
        }
    }

    /** Kalp cercevesi (parametrik). */
    private static void heart(GuiGraphics g, int cx, int cy, int size, int T, int k, int col) {
        float sc = Math.max(3, size) / 16f;
        int n = Math.max(24, size * 6);
        for (int i = 0; i < n; i++) {
            double t = Math.PI * 2 * i / n;
            double sx = Math.sin(t);
            float x = (float) (16 * sx * sx * sx) * sc;
            float y = (float) -(13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t)) * sc + sc * 2;
            block(g, cx, cy, x, y, T, k, col);
        }
    }
}
