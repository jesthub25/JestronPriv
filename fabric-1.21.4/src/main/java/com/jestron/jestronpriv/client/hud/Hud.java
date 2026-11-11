package com.jestron.jestronpriv.client.hud;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

/** HUD ogelerinin kaydi, tick'i ve cizimi. Her oge hatadan yalitilir. */
public final class Hud {

    public static final PotionHud POTIONS = new PotionHud();
    public static final ItemHud ITEMS = new ItemHud();
    public static final InfoHud INFO = new InfoHud();
    public static final HudElement INVENTORY = new InventoryHud();
    public static final PickupHud PICKUPS = new PickupHud();
    public static final HudElement COMBO = new ComboHud();
    public static final HudElement STREAK = new StreakHud();
    public static final HudElement REACH = new ReachHud();
    public static final HudElement PING = new PingHud();
    public static final HudElement KILLFEED = new KillFeedHud();
    public static final HudElement TOTEMS = new TotemHud();
    /** Cizim sirasi (sonraki ustte). */
    public static final List<HudElement> ALL = List.of(INFO, INVENTORY, POTIONS, ITEMS, PICKUPS, PING, KILLFEED, TOTEMS, REACH, COMBO, STREAK);
    /** Son karenin ara tick orani (GuiMixin). */
    static float partial;

    private Hud() {
    }

    static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    private static final HudElement[] PVP_ELEMENTS = {COMBO, STREAK};

    /** PvP ekraninin ustunde denemeleri cizer. */
    public static void renderPvpOverlay(GuiGraphics g, float pt) {
        ModConfig c = cfg();
        if (c == null) return;
        partial = pt;
        try {
            com.jestron.jestronpriv.client.pvp.KillStreak.renderFlash(g);
            for (HudElement e : PVP_ELEMENTS) if (e.hasContent(c)) drawAt(g, c, e, false, g.guiWidth(), g.guiHeight());
        } catch (Throwable ignored) {
        }
    }

    public static void tick(Minecraft mc) {
        ModConfig c = cfg();
        if (c == null) return;
        for (HudElement e : ALL) {
            try {
                if (e instanceof Ticking t) t.tick(mc, c);
            } catch (Throwable ignored) {
            }
        }
    }

    /** Oyun arayuzu (GuiMixin). */
    public static void render(GuiGraphics g, float pt) {
        ModConfig c = cfg();
        Minecraft mc = Minecraft.getInstance();
        partial = pt;
        if (c == null || mc.player == null) return;
        if (mc.screen instanceof com.jestron.jestronpriv.client.gui.HudEditScreen) return;
        boolean pvpScreen = mc.screen instanceof com.jestron.jestronpriv.client.gui.PvpScreen;
        // seri duyurusunun kenar parlamasi tum ekrana (F3'ten bagimsiz)
        if (!pvpScreen) {
            try {
                com.jestron.jestronpriv.client.pvp.KillStreak.renderFlash(g);
            } catch (Throwable ignored) {
            }
        }
        boolean f3 = c.hudHideF3 && mc.gui != null && mc.gui.getDebugOverlay().showDebugScreen();
        int sw = g.guiWidth(), sh = g.guiHeight();
        for (HudElement e : ALL) {
            try {
                boolean pvp = e == COMBO || e == STREAK;
                // PvP ekrani acikken bunlar o ekranin ustunde cizilir; F3 sadece bilgi ogelerini gizler
                if (pvp ? pvpScreen : f3) continue;
                if (!e.enabled(c) || !e.hasContent(c)) continue;
                drawAt(g, c, e, false, sw, sh);
            } catch (Throwable ignored) {
            }
        }
    }

    /** Ogeyi kendi konumunda cizer (duzenleyici de kullanir). */
    public static void drawAt(GuiGraphics g, ModConfig c, HudElement e, boolean sample, int sw, int sh) {
        e.measure(c, sample);
        if (e.w <= 0 || e.h <= 0) return;
        float s = e.scale(c);
        int x = e.screenX(c, sw), y = e.screenY(c, sh);
        boolean right = x + e.w * s / 2f > sw / 2f + 1;
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(s, s, 1f);
        e.draw(g, c, sample, right);
        g.pose().popPose();
    }

    // ------------------------------------------------------------------ ortak cizim

    static void background(GuiGraphics g, int x0, int y0, int x1, int y1, float alpha) {
        if (alpha <= 0.01f) return;
        int a = Math.round(Math.min(0.9f, alpha) * 255f);
        g.fill(x0, y0, x1, y1, a << 24);
        // ince ust kenar vurgusu
        g.fill(x0, y0, x1, y0 + 1, (Math.round(a * 0.6f) << 24) | 0xB070FF);
    }

    static void text(GuiGraphics g, Font f, String s, int x, int y, int argb, ModConfig c) {
        // Font, alfasi cok dusuk rengi tam opak cizer
        if ((argb >>> 24) < 5) return;
        g.drawString(f, s, x, y, argb, c.hudShadow);
    }

    interface Ticking {
        void tick(Minecraft mc, ModConfig c);
    }
}
