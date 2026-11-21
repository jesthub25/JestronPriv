package com.jestron.jestronpriv.jc.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiGraphics;

/** Ekranda sürükle-bırak konumlanan bir HUD öğesi. Konum ekranın oranı olarak saklanır. */
public abstract class HudModule extends Module {
    private final double defX, defY;
    public double x, y;
    public double scale = 1.0;

    protected HudModule(String id, String name, String description, Category category, boolean defaultEnabled, double defX, double defY) {
        super(id, name, description, category, defaultEnabled);
        this.defX = defX;
        this.defY = defY;
        this.x = defX;
        this.y = defY;
    }

    /** Ölçeklenmemiş genişlik. preview: HUD düzenleyicide örnek veriyle. 0 = çizilecek bir şey yok. */
    public abstract int width(boolean preview);

    public abstract int height(boolean preview);

    /** (0,0) öğenin sol üst köşesidir; poz ve ölçek çağıran tarafından ayarlanır. */
    public abstract void render(GuiGraphics g, boolean preview);

    public void resetPosition() {
        x = defX;
        y = defY;
        scale = 1.0;
    }

    @Override
    public JsonObject save() {
        JsonObject o = super.save();
        o.addProperty("x", x);
        o.addProperty("y", y);
        o.addProperty("scale", scale);
        return o;
    }

    @Override
    public void load(JsonObject o) {
        super.load(o);
        if (o == null) return;
        x = clamp(num(o.get("x"), defX), 0, 1);
        y = clamp(num(o.get("y"), defY), 0, 1);
        scale = clamp(num(o.get("scale"), 1.0), 0.5, 2.0);
    }

    @Override
    public void resetToDefaults() {
        super.resetToDefaults();
        resetPosition();
    }

    private static double num(JsonElement e, double def) {
        try {
            if (e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber()) {
                double v = e.getAsDouble();
                if (!Double.isNaN(v) && !Double.isInfinite(v)) return v;
            }
        } catch (RuntimeException ignored) {
        }
        return def;
    }

    public static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
