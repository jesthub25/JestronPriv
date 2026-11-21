package com.jestron.jestronpriv.jc.core;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Bir ya da birkaç satır yazıdan oluşan HUD öğesi. Bir satır "etiket\u0001değer" biçimindeyse
 * etiket ve değer farklı renkte çizilir.
 */
public abstract class TextHud extends HudModule {
    public static final char SEP = '\u0001';
    private long cachedFrame = -1;
    private boolean cachedPreview;
    private String[] cached = new String[0];

    protected TextHud(String id, String name, String description, Category category, boolean def, double x, double y) {
        super(id, name, description, category, def, x, y);
    }

    /** Boş dizi = bu karede gösterme. */
    protected abstract String[] lines(boolean preview);

    private String[] get(boolean preview) {
        if (cachedFrame != Hud.frame || cachedPreview != preview) {
            String[] l;
            try {
                l = lines(preview);
            } catch (Throwable t) {
                l = null;
            }
            cached = l == null ? new String[0] : l;
            cachedFrame = Hud.frame;
            cachedPreview = preview;
        }
        return cached;
    }

    private static int lineWidth(String s) {
        int i = s.indexOf(SEP);
        if (i < 0) return Hud.width(s);
        return Hud.width(s.substring(0, i)) + Hud.width(" ") + Hud.width(s.substring(i + 1));
    }

    @Override
    public int width(boolean preview) {
        String[] l = get(preview);
        if (l.length == 0) return 0;
        int w = 0;
        for (String s : l) w = Math.max(w, lineWidth(s));
        return w + Hud.PAD * 2;
    }

    @Override
    public int height(boolean preview) {
        String[] l = get(preview);
        if (l.length == 0) return 0;
        return l.length * 10 - 1 + Hud.PAD * 2;
    }

    @Override
    public void render(GuiGraphics g, boolean preview) {
        String[] l = get(preview);
        if (l.length == 0) return;
        Hud.box(g, width(preview), height(preview));
        int y = Hud.PAD;
        for (String s : l) {
            int i = s.indexOf(SEP);
            if (i < 0) {
                Hud.text(g, s, Hud.PAD, y, Theme.hudValue());
            } else {
                int x = Hud.text(g, s.substring(0, i), Hud.PAD, y, Theme.hudLabel());
                x = Hud.PAD + Hud.width(s.substring(0, i)) + Hud.width(" ");
                Hud.text(g, s.substring(i + 1), x, y, Theme.hudValue());
            }
            y += 10;
        }
    }

    protected static String kv(String label, String value) {
        return label + SEP + value;
    }
}
