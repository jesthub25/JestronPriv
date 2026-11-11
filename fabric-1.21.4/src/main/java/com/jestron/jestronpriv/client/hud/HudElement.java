package com.jestron.jestronpriv.client.hud;

import com.jestron.jestronpriv.client.ModConfig;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Tasinabilir HUD ogesi. Konum ekranin bos alanina gore 0-1 arasi oran olarak saklanir (sol-ust = 0,0, sag-alt =
 * 1,1): her cozunurlukte ve arayuz olceginde oge ekranin icinde kalir. Olcu (w, h) olceksiz arayuz pikselidir.
 */
public abstract class HudElement {

    public final String id, label;
    /** Son olculen boyut (olceksiz). */
    protected int w, h;

    protected HudElement(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public abstract boolean enabled(ModConfig c);

    public abstract void setEnabled(ModConfig c, boolean v);

    public abstract float x(ModConfig c);

    public abstract float y(ModConfig c);

    public abstract float scale(ModConfig c);

    public abstract void setPos(ModConfig c, float x, float y);

    public abstract void setScale(ModConfig c, float s);

    /** Gosterecek bir sey var mi (yoksa oyunda cizilmez; duzenleyicide ornek gosterilir). */
    public abstract boolean hasContent(ModConfig c);

    /** w ve h'yi hesaplar. sample: duzenleyici ornegi. */
    public abstract void measure(ModConfig c, boolean sample);

    /**
     * (0,0)'a, olceklenmis pozda cizer.
     *
     * @param right oge ekranin sag yarisinda: icerik saga yaslanir
     */
    public abstract void draw(GuiGraphics g, ModConfig c, boolean sample, boolean right);

    public int width() {
        return w;
    }

    public int height() {
        return h;
    }

    /** Ekran koordinatinda sol-ust nokta. */
    public int screenX(ModConfig c, int screenW) {
        float s = scale(c);
        float free = screenW - w * s;
        return free <= 0 ? 0 : Math.round(clamp01(x(c)) * free);
    }

    public int screenY(ModConfig c, int screenH) {
        float s = scale(c);
        float free = screenH - h * s;
        return free <= 0 ? 0 : Math.round(clamp01(y(c)) * free);
    }

    static float clamp01(float v) {
        return Float.isNaN(v) ? 0f : Math.max(0f, Math.min(1f, v));
    }
}
