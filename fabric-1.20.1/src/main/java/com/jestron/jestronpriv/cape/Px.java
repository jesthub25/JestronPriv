package com.jestron.jestronpriv.cape;

/** Pelerin deseni icin tek pikselin girdisi ve ciktisi (yeniden kullanilir, tahsis yok). */
public final class Px {
    /** Piksel ve dis yuz boyutu. */
    public int x, y, w, h;
    /** 0..1 (v = 0 ust kenar, omuz). */
    public float u, v;
    /** Saniye (animasyon). */
    public float t;
    public int c1, c2, c3;
    /** Cikis: renk (0xRRGGBB). */
    public int rgb;
    /** Cikis: isilti 0..1 (karanlikta parlayan kisim). */
    public float glow;
    /** Cikis: opaklik 0..1 (sadece elytra: 0.5 altindaki pikseller kesilir, delikli / tirtikli kanat). Pelerin yok sayar. */
    public float alpha = 1f;
    /** Kare basina bir kez hesaplanan degerler icin calisma alani (desen kendi anahtariyla isaretler). */
    public final float[] scratch = new float[64];
    public float scratchT = Float.NaN;
    public int scratchKey;
    /** Hucre gurultusu icin calisma alani (her uretecin kendi Px'i vardir: is parcacigi guvenli). */
    public final CapeArt.Cell cell = new CapeArt.Cell();

    void at(int x, int y) {
        this.x = x;
        this.y = y;
        this.u = (x + 0.5f) / w;
        this.v = (y + 0.5f) / h;
        this.rgb = 0;
        this.glow = 0f;
        this.alpha = 1f;
    }
}
