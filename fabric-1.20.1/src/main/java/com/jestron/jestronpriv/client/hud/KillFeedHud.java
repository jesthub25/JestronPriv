package com.jestron.jestronpriv.client.hud;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.pvp.KillFeed;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Oldurme akisi paneli: "Oldurenin adi [silah] Olen". Senin oldurmelerin yesil seritle, senin olumun kirmizi
 * seritle vurgulanir; oldurenin serisi "×3" olarak yazilir. Yeni satir yandan kayarak gelir, suresi bitince soner.
 */
final class KillFeedHud extends BoundElement {

    private static final int ROW = 13;
    private final List<KillFeed.Entry> view = new ArrayList<>();
    private static final List<KillFeed.Entry> SAMPLE = new ArrayList<>();

    KillFeedHud() {
        super("killfeed", "Öldürme Akışı", c -> c.kfHud, (c, v) -> c.kfHud = v, c -> c.kfX, (c, v) -> c.kfX = v, c -> c.kfY, (c, v) -> c.kfY = v,
                c -> c.kfScale, (c, v) -> c.kfScale = v);
    }

    private static boolean pass(KillFeed.Entry e, ModConfig c) {
        return switch (c.kfFilter) {
            case "OYUNCU" -> e.pvp || e.meKiller || e.meVictim;
            case "BENIM" -> e.meKiller || e.meVictim;
            default -> true;
        };
    }

    private static float life(ModConfig c) {
        return Math.max(2f, Math.min(30f, c.kfDuration)) * 1000f;
    }

    private void collect(ModConfig c, boolean sample) {
        view.clear();
        int max = Math.max(1, Math.min(10, c.kfMax));
        long now = Util.getMillis();
        float life = life(c);
        for (KillFeed.Entry e : KillFeed.entries()) {
            if (view.size() >= max) break;
            if (now - e.time > life + 500) break; // daha eskiler de dolmustur
            if (pass(e, c)) view.add(e);
        }
        if (sample && view.isEmpty()) {
            if (SAMPLE.isEmpty()) SAMPLE.addAll(KillFeed.sample());
            for (KillFeed.Entry e : SAMPLE) if (view.size() < max) view.add(e);
        }
    }

    @Override
    public boolean hasContent(ModConfig c) {
        long now = Util.getMillis();
        float life = life(c);
        for (KillFeed.Entry e : KillFeed.entries()) {
            if (now - e.time > life + 500) return false;
            if (pass(e, c)) return true;
        }
        return false;
    }

    private String killerText(KillFeed.Entry e, ModConfig c) {
        return e.meKiller && c.kfSelfName ? "Sen" : e.killer;
    }

    private String victimText(KillFeed.Entry e, ModConfig c) {
        return e.meVictim && c.kfSelfName ? "Sen" : e.victim;
    }

    private int rowWidth(Font f, KillFeed.Entry e, ModConfig c) {
        // genislikler bir kez olculur ("Sen" ayari degisince yeniden)
        int key = c.kfSelfName ? 1 : 0;
        if (e.wKiller < 0 || e.wStreak != (key | (c.kfStreak ? 2 : 0))) {
            String k = killerText(e, c);
            e.wKiller = k.isEmpty() ? 0 : f.width(k) + (c.kfStreak && e.streak >= 2 ? f.width(" ×" + e.streak) : 0);
            e.wVictim = f.width(victimText(e, c));
            e.wStreak = key | (c.kfStreak ? 2 : 0);
        }
        int icon = c.kfIcons ? 14 : f.width(" ⚔ ");
        return 6 + e.wKiller + (e.wKiller > 0 ? 3 : 0) + icon + 3 + e.wVictim + 4;
    }

    @Override
    public void measure(ModConfig c, boolean sample) {
        collect(c, sample);
        Font f = Minecraft.getInstance().font;
        int mw = 120;
        for (KillFeed.Entry e : view) mw = Math.max(mw, rowWidth(f, e, c));
        w = mw;
        h = Math.max(1, Math.min(10, c.kfMax)) * ROW;
    }

    @Override
    public void draw(GuiGraphics g, ModConfig c, boolean sample, boolean right) {
        Font f = Minecraft.getInstance().font;
        long now = Util.getMillis();
        float life = life(c);
        int n = view.size(), slots = Math.max(1, Math.min(10, c.kfMax));
        for (int i = 0; i < n; i++) {
            KillFeed.Entry e = view.get(i);
            float age = sample ? 1000f : now - e.time;
            float a = age > life ? Math.max(0f, 1f - (age - life) / 500f) : 1f;
            if (a * 255 < 5) continue;
            int slot = c.kfNewestTop ? i : slots - 1 - i;
            int y = slot * ROW;
            int rw = rowWidth(f, e, c);
            float slide = age < 180f ? 1f - age / 180f : 0f;
            int off = Math.round(slide * slide * 40f) * (right ? 1 : -1);
            int x0 = right ? w - rw : 0;
            int ax = (int) (a * 255) << 24;
            g.pose().pushPose();
            g.pose().translate(off, 0, 0);
            Hud.background(g, x0, y, x0 + rw, y + ROW - 1, c.kfBg * a);
            if (e.meKiller || e.meVictim) {
                int sc = e.meKiller ? 0x55FF7A : 0xFF4A4A;
                g.fill(x0, y, x0 + 2, y + ROW - 1, ax | sc);
                g.fill(x0, y, x0 + rw, y + ROW - 1, ((int) (a * 40) << 24) | sc);
            }
            int x = x0 + 6;
            int ty = y + 2;
            String k = killerText(e, c);
            if (!k.isEmpty()) {
                Hud.text(g, f, k, x, ty, ax | (e.meKiller ? 0x7CFF95 : 0xFFFFFF), c);
                int kx = x + f.width(k);
                if (c.kfStreak && e.streak >= 2) {
                    int stc = e.streak >= 5 ? 0xFF4A4A : e.streak >= 3 ? 0xFF9A3C : 0xFFE066;
                    Hud.text(g, f, " ×" + e.streak, kx, ty, ax | stc, c);
                }
                x += e.wKiller + 3;
            }
            if (c.kfIcons) {
                ItemStack icon = c.kfWeapon && e.weapon != null ? e.weapon : e.cause;
                if (icon == null || icon.isEmpty()) icon = KillFeedIcons.SWORD.get();
                g.pose().pushPose();
                g.pose().translate(x, y + 0.5f, 0);
                g.pose().scale(0.75f, 0.75f, 1f);
                g.renderItem(icon, 0, 0);
                g.pose().popPose();
                x += 14;
            } else {
                Hud.text(g, f, " ⚔ ", x - 3, ty, ax | 0xC0C0C0, c);
                x += f.width(" ⚔ ") - 3;
            }
            g.pose().pushPose();
            g.pose().translate(0, 0, 200);
            Hud.text(g, f, victimText(e, c), x + 3, ty, ax | (e.meVictim ? 0xFF6B6B : 0xE0E0E0), c);
            g.pose().popPose();
            g.pose().popPose();
        }
    }

    /** Yedek simge (gec yuklenir: esya kaydi hazir olmadan olusturulmaz). */
    enum KillFeedIcons {
        SWORD;
        private ItemStack stack;

        ItemStack get() {
            if (stack == null) stack = new ItemStack(Items.IRON_SWORD);
            return stack;
        }
    }
}
