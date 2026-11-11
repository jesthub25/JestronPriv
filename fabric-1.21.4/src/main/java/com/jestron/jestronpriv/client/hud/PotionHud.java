package com.jestron.jestronpriv.client.hud;

import com.jestron.jestronpriv.client.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Iksir paneli: ikon, ad + seviye, kalan sure ve kalan sureyi gosteren cubuk (cubugun tam boyu, etkinin sende
 * gorulen en uzun suresidir). Bitmek uzereyken yanip soner. Kompakt modda sadece ikon + sure.
 */
public final class PotionHud extends HudElement implements Hud.Ticking {

    private static final int ROW = 22, ICON = 18, CELL_W = 24, CELL_H = 30;

    /** Gosterilecek satir: gercek ya da ornek. */
    private static final class Row {
        Holder<MobEffect> effect;
        int duration, max, amp;
        boolean infinite, ambient;
        String name, time;
        // karede yeni yazi uretmemek icin: degismedikce eski yazilar kullanilir
        MobEffect nameFor;
        int nameAmp = -1, timeSec = -1;
        boolean timeInf;
    }

    private static final Comparator<Row> BY_TIME = Comparator.comparingInt(r -> r.infinite ? Integer.MAX_VALUE : r.duration);
    private static final Comparator<Row> BY_TYPE = Comparator.comparingInt(PotionHud::category).thenComparing(BY_TIME);
    private static final Comparator<Row> BY_NAME = Comparator.comparing(r -> r.name);

    private final List<Row> rows = new ArrayList<>();
    private final List<Row> pool = new ArrayList<>();
    private final Map<MobEffect, Integer> maxSeen = new IdentityHashMap<>();
    private final Map<MobEffect, String> names = new IdentityHashMap<>();
    private int textW;
    private long ticks;

    PotionHud() {
        super("potions", "İksir Paneli");
    }

    @Override
    public boolean enabled(ModConfig c) {
        return c.potHud;
    }

    @Override
    public void setEnabled(ModConfig c, boolean v) {
        c.potHud = v;
    }

    @Override
    public float x(ModConfig c) {
        return c.potX;
    }

    @Override
    public float y(ModConfig c) {
        return c.potY;
    }

    @Override
    public float scale(ModConfig c) {
        return c.potScale;
    }

    @Override
    public void setPos(ModConfig c, float x, float y) {
        c.potX = x;
        c.potY = y;
    }

    @Override
    public void setScale(ModConfig c, float s) {
        c.potScale = s;
    }

    /** Vanilla sag-ust iksir ikonlari gizlensin mi. */
    public static boolean hideVanilla(ModConfig c) {
        return c != null && c.potHud && c.potHideVanilla;
    }

    @Override
    public void tick(Minecraft mc, ModConfig c) {
        ticks++;
        LocalPlayer p = mc.player;
        if (p == null) {
            maxSeen.clear();
            return;
        }
        // her etkinin gorulen en uzun suresi (cubugun tam boyu); biten etkiler unutulur
        if (maxSeen.isEmpty() && p.getActiveEffects().isEmpty()) return;
        for (MobEffectInstance in : p.getActiveEffects()) {
            MobEffect e = in.getEffect().value();
            Integer old = maxSeen.get(e);
            int d = in.getDuration();
            if (old == null || d > old) maxSeen.put(e, d);
        }
        if (ticks % 20 == 0) {
            maxSeen.keySet().removeIf(e -> {
                for (MobEffectInstance in : p.getActiveEffects()) if (in.getEffect().value() == e) return false;
                return true;
            });
        }
    }

    @Override
    public boolean hasContent(ModConfig c) {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null) return false;
        for (MobEffectInstance in : p.getActiveEffects()) if (visible(c, in)) return true;
        return false;
    }

    private static boolean visible(ModConfig c, MobEffectInstance in) {
        return in.showIcon() && (c.potAmbient || !in.isAmbient());
    }

    private Row row(int i) {
        while (pool.size() <= i) pool.add(new Row());
        return pool.get(i);
    }

    private String name(MobEffect e) {
        return names.computeIfAbsent(e, k -> k.getDisplayName().getString());
    }

    private void collect(ModConfig c, boolean sample) {
        rows.clear();
        LocalPlayer p = Minecraft.getInstance().player;
        int n = 0;
        if (p != null) {
            for (MobEffectInstance in : p.getActiveEffects()) {
                if (!visible(c, in)) continue;
                Row r = row(n++);
                r.effect = in.getEffect();
                r.duration = in.getDuration();
                r.infinite = in.isInfiniteDuration();
                r.ambient = in.isAmbient();
                r.amp = in.getAmplifier();
                Integer m = maxSeen.get(r.effect.value());
                r.max = Math.max(r.duration, m == null ? r.duration : m);
                rows.add(r);
            }
        }
        if (rows.isEmpty() && sample) {
            sample(row(n++), MobEffects.MOVEMENT_SPEED, 1, 1660, 3600);
            sample(row(n++), MobEffects.DAMAGE_BOOST, 0, 150, 1800);
            sample(row(n++), MobEffects.FIRE_RESISTANCE, 0, -1, 0);
            sample(row(n), MobEffects.POISON, 2, 90, 300);
        }
        for (Row r : rows) {
            MobEffect e = r.effect.value();
            if (r.nameFor != e || r.nameAmp != r.amp || r.name == null) {
                r.name = name(e) + roman(r.amp);
                r.nameFor = e;
                r.nameAmp = r.amp;
            }
            int sec = r.duration / 20;
            if (r.time == null || r.timeInf != r.infinite || (!r.infinite && sec != r.timeSec)) {
                r.time = r.infinite ? "∞" : duration(r.duration);
                r.timeSec = sec;
                r.timeInf = r.infinite;
            }
        }
        rows.sort(switch (c.potSort) {
            case "TUR" -> BY_TYPE;
            case "AD" -> BY_NAME;
            default -> BY_TIME;
        });
    }

    private void sample(Row r, Holder<MobEffect> e, int amp, int dur, int max) {
        if (e == null) return;
        r.effect = e;
        r.amp = amp;
        r.infinite = dur < 0;
        r.duration = Math.max(0, dur);
        r.max = Math.max(1, max);
        r.ambient = false;
        rows.add(r);
    }

    private static int category(Row r) {
        MobEffectCategory k = r.effect.value().getCategory();
        return k == MobEffectCategory.BENEFICIAL ? 0 : k == MobEffectCategory.NEUTRAL ? 1 : 2;
    }

    /** I..X; 0 = seviye yazilmaz. */
    static String roman(int amp) {
        int lv = amp + 1;
        if (lv <= 1) return "";
        String[] r = {"", "", " II", " III", " IV", " V", " VI", " VII", " VIII", " IX", " X"};
        return lv < r.length ? r[lv] : " " + lv;
    }

    /** Tick -> "m:ss" ya da "s:dd:ss". */
    static String duration(int ticks) {
        int s = Math.max(0, ticks) / 20;
        int h = s / 3600, m = (s / 60) % 60, sec = s % 60;
        return h > 0 ? String.format(Locale.ROOT, "%d:%02d:%02d", h, m, sec) : String.format(Locale.ROOT, "%d:%02d", m, sec);
    }

    @Override
    public void measure(ModConfig c, boolean sample) {
        collect(c, sample);
        Font f = Minecraft.getInstance().font;
        if (rows.isEmpty()) {
            w = h = 0;
            return;
        }
        if (c.potCompact) {
            w = rows.size() * CELL_W + 2;
            h = CELL_H;
            return;
        }
        int tw = 0;
        for (Row r : rows) tw = Math.max(tw, Math.max(c.potNames ? f.width(r.name) : 0, f.width(r.time)));
        textW = tw;
        w = ICON + 8 + tw + 4;
        h = rows.size() * ROW + 2;
    }

    private int color(ModConfig c, Row r) {
        return switch (c.potColor) {
            case "TUR" -> {
                MobEffectCategory k = r.effect.value().getCategory();
                yield k == MobEffectCategory.BENEFICIAL ? 0x6BFF5A : k == MobEffectCategory.HARMFUL ? 0xFF5A5A : 0x7FB7FF;
            }
            case "BEYAZ" -> 0xFFFFFF;
            default -> brighten(r.effect.value().getColor() & 0xFFFFFF);
        };
    }

    /** Koyu etki renklerini (kor = gri, zayiflik = koyu) okunur yap. */
    static int brighten(int rgb) {
        int r = (rgb >> 16) & 255, g = (rgb >> 8) & 255, b = rgb & 255;
        int mx = Math.max(r, Math.max(g, b));
        if (mx < 150) {
            float k = mx == 0 ? 0f : 150f / mx;
            if (mx == 0) return 0x9A9A9A;
            r = Math.min(255, Math.round(r * k));
            g = Math.min(255, Math.round(g * k));
            b = Math.min(255, Math.round(b * k));
        }
        return (r << 16) | (g << 8) | b;
    }

    /** Bitmek uzereyken yanip sonme alfasi (1 = normal). */
    private float blink(ModConfig c, Row r) {
        if (r.infinite || c.potWarn <= 0 || r.duration > c.potWarn * 20) return 1f;
        double t = System.nanoTime() / 1e9;
        // kalan sure azaldikca hizlanir
        double speed = 2.0 + 6.0 * (1.0 - r.duration / (c.potWarn * 20.0));
        return 0.35f + 0.65f * (float) (0.5 + 0.5 * Math.cos(t * speed * Math.PI));
    }

    @Override
    public void draw(GuiGraphics g, ModConfig c, boolean sample, boolean right) {
        Minecraft mc = Minecraft.getInstance();
        Font f = mc.font;
        Hud.background(g, 0, 0, w, h, c.potBg);
        if (c.potCompact) {
            for (int i = 0; i < rows.size(); i++) {
                Row r = rows.get(i);
                int x = 1 + (right ? rows.size() - 1 - i : i) * CELL_W;
                float a = blink(c, r);
                icon(g, mc, r, x + (CELL_W - ICON) / 2, 2, a);
                int tw = f.width(r.time);
                g.pose().pushPose();
                g.pose().translate(x + CELL_W / 2f, 21, 0);
                g.pose().scale(0.75f, 0.75f, 1f);
                Hud.text(g, f, r.time, -tw / 2, 0, alpha(a) | (r.duration <= c.potWarn * 20 && !r.infinite ? 0xFF6060 : 0xE0E0E0), c);
                g.pose().popPose();
            }
            return;
        }
        for (int i = 0; i < rows.size(); i++) {
            Row r = rows.get(i);
            int y = 1 + i * ROW;
            float a = blink(c, r);
            int col = color(c, r);
            int ix = right ? w - ICON - 3 : 3;
            icon(g, mc, r, ix, y + 2, a);
            int tx = right ? 3 : ICON + 8;
            int tw = textW;
            if (c.potNames) {
                Hud.text(g, f, r.name, right ? tx + tw - f.width(r.name) : tx, y + 2, alpha(a) | col, c);
                int tcol = r.infinite ? 0xA0A0A0 : r.duration <= c.potWarn * 20 ? 0xFF7070 : 0xC8C8C8;
                Hud.text(g, f, r.time, right ? tx + tw - f.width(r.time) : tx, y + 12, alpha(a) | tcol, c);
            } else {
                Hud.text(g, f, r.time, right ? tx + tw - f.width(r.time) : tx, y + 7, alpha(a) | col, c);
            }
            if (c.potBar && !r.infinite) {
                float k = Math.max(0f, Math.min(1f, r.duration / (float) Math.max(1, r.max)));
                int bx0 = tx, bx1 = tx + tw, by = y + ROW - 2;
                g.fill(bx0, by, bx1, by + 1, 0x50000000);
                int fw = Math.round((bx1 - bx0) * k);
                if (fw > 0) {
                    if (right) g.fill(bx1 - fw, by, bx1, by + 1, alpha(a * 0.9f) | col);
                    else g.fill(bx0, by, bx0 + fw, by + 1, alpha(a * 0.9f) | col);
                }
            }
        }
    }

    private static int alpha(float a) {
        return Math.round(Math.max(0.02f, Math.min(1f, a)) * 255f) << 24;
    }

    private static void icon(GuiGraphics g, Minecraft mc, Row r, int x, int y, float a) {
        TextureAtlasSprite s = mc.getMobEffectTextures().get(r.effect);
        if (s == null) return;
        int col = (Math.round(Math.max(0f, Math.min(1f, a)) * 255f) << 24) | 0xFFFFFF;
        g.blitSprite(RenderType::guiTextured, s, x, y, ICON, ICON, col);
    }
}
