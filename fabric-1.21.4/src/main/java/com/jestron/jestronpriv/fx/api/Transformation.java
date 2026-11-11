package com.jestron.jestronpriv.fx.api;

import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Display entity donusumu ile ayni anlam: T * L * S * R. Degismez nesne: yapici ve get* kopya verir.
 * Cizim yolu icin kopyasiz *View() erisimcileri vardir (sonuclari degistirilmemelidir).
 */
public final class Transformation {
    private final Vector3f translation;
    private final Quaternionf leftRotation;
    private final Vector3f scale;
    private final Quaternionf rightRotation;

    public Transformation(Vector3f translation, Quaternionf leftRotation, Vector3f scale, Quaternionf rightRotation) {
        this.translation = new Vector3f(translation);
        this.leftRotation = new Quaternionf(leftRotation);
        this.scale = new Vector3f(scale);
        this.rightRotation = new Quaternionf(rightRotation);
    }

    /** Kopyasiz: verilen nesnelerin sahipligini alir (sadece yeni uretilmis nesnelerle cagrilir). */
    private Transformation(Vector3f t, Quaternionf l, Vector3f s, Quaternionf r, boolean owned) {
        this.translation = t;
        this.leftRotation = l;
        this.scale = s;
        this.rightRotation = r;
    }

    /** a'dan b'ye p oraninda ara donusum (konum / olcek dogrusal, donus kuresel). */
    public static Transformation lerp(Transformation a, Transformation b, float p) {
        return new Transformation(
                new Vector3f(a.translation).lerp(b.translation, p),
                new Quaternionf(a.leftRotation).slerp(b.leftRotation, p),
                new Vector3f(a.scale).lerp(b.scale, p),
                new Quaternionf(a.rightRotation).slerp(b.rightRotation, p), true);
    }

    public Vector3f getTranslation() { return new Vector3f(translation); }
    public Quaternionf getLeftRotation() { return new Quaternionf(leftRotation); }
    public Vector3f getScale() { return new Vector3f(scale); }
    public Quaternionf getRightRotation() { return new Quaternionf(rightRotation); }

    // kopyasiz okuma (cizim yolu, her karede): DEGISTIRMEYIN
    public Vector3f translationView() { return translation; }
    public Quaternionf leftView() { return leftRotation; }
    public Vector3f scaleView() { return scale; }
    public Quaternionf rightView() { return rightRotation; }
}
