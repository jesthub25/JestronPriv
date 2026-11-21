package com.jestron.jestronpriv.jc.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** Tüm HUD öğelerini çizer ve konumlarını hesaplar. */
public final class Hud {
    private Hud() {
    }

    public static final int PAD = 3;
    public static final int BG = 0x70000000;
    /** Her karede artar; metin öğeleri satırlarını kare başına bir kez üretir. */
    public static long frame;

    public static void renderAll(GuiGraphics g) {
        frame++;
        Minecraft mc = Minecraft.getInstance();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        for (HudModule m : Modules.HUD) {
            if (!m.isEnabled()) continue;
            try {
                draw(g, m, false, sw, sh);
            } catch (Throwable t) {
                // tek bir öğe hatası tüm HUD'u bozmasın
            }
        }
    }

    /** {x, y, genişlik, yükseklik} ölçekli ekran koordinatlarında; çizilecek bir şey yoksa null. */
    public static int[] bounds(HudModule m, boolean preview, int sw, int sh) {
        int w = m.width(preview), h = m.height(preview);
        if (w <= 0 || h <= 0) return null;
        int ws = (int) Math.ceil(w * m.scale), hs = (int) Math.ceil(h * m.scale);
        int px = (int) Math.round(m.x * sw), py = (int) Math.round(m.y * sh);
        px = Math.max(0, Math.min(sw - ws, px));
        py = Math.max(0, Math.min(sh - hs, py));
        return new int[]{px, py, ws, hs};
    }

    public static void draw(GuiGraphics g, HudModule m, boolean preview, int sw, int sh) {
        int[] b = bounds(m, preview, sw, sh);
        if (b == null) return;
        g.pose().pushPose();
        try {
            g.pose().translate(b[0], b[1], 0);
            g.pose().scale((float) m.scale, (float) m.scale, 1f);
            m.render(g, preview);
        } finally {
            g.pose().popPose();
        }
    }

    // ------------------------------------------------------------------ yardımcılar

    public static Font font() {
        return Minecraft.getInstance().font;
    }

    public static void box(GuiGraphics g, int w, int h) {
        if (Theme.hudBackground()) g.fill(0, 0, w, h, BG);
    }

    public static int text(GuiGraphics g, String s, int x, int y, int color) {
        return g.drawString(font(), s, x, y, color, Theme.hudShadow());
    }

    public static int width(String s) {
        return font().width(s);
    }
}
