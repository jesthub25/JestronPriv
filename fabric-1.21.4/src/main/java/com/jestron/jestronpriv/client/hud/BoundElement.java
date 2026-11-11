package com.jestron.jestronpriv.client.hud;

import com.jestron.jestronpriv.client.ModConfig;

/** Ayar alanlarina lambdalarla baglanan HUD ogesi (tekrar eden erisimcileri kisaltir). */
abstract class BoundElement extends HudElement {

    interface GetF {
        float get(ModConfig c);
    }

    interface SetF {
        void set(ModConfig c, float v);
    }

    interface GetB {
        boolean get(ModConfig c);
    }

    interface SetB {
        void set(ModConfig c, boolean v);
    }

    private final GetB on;
    private final SetB setOn;
    private final GetF gx, gy, gs;
    private final SetF sx, sy, ss;

    BoundElement(String id, String label, GetB on, SetB setOn, GetF gx, SetF sx, GetF gy, SetF sy, GetF gs, SetF ss) {
        super(id, label);
        this.on = on;
        this.setOn = setOn;
        this.gx = gx;
        this.sx = sx;
        this.gy = gy;
        this.sy = sy;
        this.gs = gs;
        this.ss = ss;
    }

    @Override
    public boolean enabled(ModConfig c) {
        return on.get(c);
    }

    @Override
    public void setEnabled(ModConfig c, boolean v) {
        setOn.set(c, v);
    }

    @Override
    public float x(ModConfig c) {
        return gx.get(c);
    }

    @Override
    public float y(ModConfig c) {
        return gy.get(c);
    }

    @Override
    public float scale(ModConfig c) {
        float s = gs.get(c);
        return Float.isFinite(s) ? Math.max(0.5f, Math.min(2f, s)) : 1f;
    }

    @Override
    public void setPos(ModConfig c, float x, float y) {
        sx.set(c, x);
        sy.set(c, y);
    }

    @Override
    public void setScale(ModConfig c, float s) {
        ss.set(c, s);
    }
}
