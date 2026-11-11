package com.jestron.jestronpriv.fx.api;

/**
 * Istemci tarafinda cizilen tek bir 3D nesne (blok, esya veya yazi).
 * Vanilla display entity'lerinin davranisini birebir taklit eder: donusum + interpolasyon.
 */
public abstract class Display {

    public enum Billboard { FIXED, VERTICAL, HORIZONTAL, CENTER }

    /** Paketlenmis isik degeri (LightTexture.pack). */
    public static final int FULL_BRIGHT = (15 << 4) | (15 << 20);

    private Transformation from;
    private Transformation to;
    private long startTick;
    private int duration;
    private boolean valid = true;
    private int light = -1;
    private Billboard billboard = Billboard.FIXED;
    private double ax, ay, az;

    protected Display(Transformation initial, long now) {
        this.from = initial;
        this.to = initial;
        this.startTick = now;
        this.duration = 0;
    }

    public boolean isValid() { return valid; }

    /** Donusumlerin goreli oldugu dunya noktasi. */
    public void setAnchor(double x, double y, double z) {
        this.ax = x;
        this.ay = y;
        this.az = z;
    }

    public double anchorX() { return ax; }
    public double anchorY() { return ay; }
    public double anchorZ() { return az; }
    public void remove() { valid = false; }

    public Billboard getBillboard() { return billboard; }
    public void setBillboard(Billboard b) { this.billboard = b; }

    /** -1 = dunya isigi, aksi halde paketlenmis isik. */
    public int getLight() { return light; }
    public void setLight(int packed) { this.light = packed; }

    /** Yeni hedef donusum. Su anki (interpolasyonlu) durumdan baslar. */
    public void setTransformation(Transformation t, int dur, long now) {
        this.from = sample(now, 0f);
        this.to = t;
        this.startTick = now;
        this.duration = Math.max(0, dur);
    }

    /** Hedef donusum zaten gorunmez mi (kuculme yolunda). */
    public boolean targetHidden() {
        if (to == null) return false;
        org.joml.Vector3f s = to.scaleView();
        return Math.max(s.x, Math.max(s.y, s.z)) <= 0.002f;
    }

    /** now + partial anindaki donusum. */
    public Transformation sample(long now, float partial) {
        if (duration <= 0) return to;
        float p = ((now - startTick) + partial) / (float) duration;
        if (p >= 1f) return to;
        if (p <= 0f) return from;
        return Transformation.lerp(from, to, p);
    }
}
