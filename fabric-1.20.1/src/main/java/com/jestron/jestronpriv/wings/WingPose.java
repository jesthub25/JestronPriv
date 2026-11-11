package com.jestron.jestronpriv.wings;

/** Bir karedeki kanat durusu (animatorun ciktisi, mesh'in girdisi). */
public final class WingPose {
    /** 0 = tamamen acik, 1 = sirta katlanmis. */
    public float fold = 0.9f;
    /** Taban geriye supurme acisi (derece). */
    public float yaw = 24f;
    /** Kanadin yukari kalkisi (derece, duzlem icinde). */
    public float raise;
    /** Cirpma genligi (derece). */
    public float amp = 2f;
    /** Cirpma fazi (radyan). */
    public float phase;
    /** 0..1 hareket enerjisi (isilti ve parcacik artisi). */
    public float energy = 0.2f;
    /** Animasyon zamani (sn). */
    public float time;

    public void set(WingPose o) {
        fold = o.fold;
        yaw = o.yaw;
        raise = o.raise;
        amp = o.amp;
        phase = o.phase;
        energy = o.energy;
        time = o.time;
    }
}
