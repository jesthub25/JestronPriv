package com.jestron.jestronpriv.wings;

/**
 * Oyuncunun hareketinden kanat durusu uretir. Tum degerler yumusak gecislerle hedefe yaklasir;
 * cirpma fazi entegre edildigi icin hiz degisse de kanat asla ziplamaz.
 *
 * Durumlar: dururken (yururkenki acikliginda yavas salinim + arada bir gerinip silkelenme), yururken (yavas cirpma),
 * kosarken (geriye supurulmus, daha hizli), havada (zipla: guclu cirp, dus: parasut gibi acik + titresim),
 * elytra ile suzulurken (acik suzulme, yukselirken / roketle guclu cirpma, arada kendiliginden kanat vurusu),
 * yaratici ucus (havada asili kalma cirpmasi), egilme / yuzme / uyuma (katli).
 */
public final class WingAnimator {

    public enum Mode { IDLE, WALK, SPRINT, AIR, GLIDE, FLY, CROUCH, SWIM, REST }

    /** Hareket bilgisi (her kare doldurulur). Hizlar blok/tick. */
    public static final class Motion {
        public double speed, vy;
        public boolean onGround = true, fallFlying, flying, sprinting, crouching, swimming, resting;

        public Motion set(double speed, double vy, boolean onGround, boolean fallFlying, boolean flying,
                          boolean sprinting, boolean crouching, boolean swimming, boolean resting) {
            this.speed = speed;
            this.vy = vy;
            this.onGround = onGround;
            this.fallFlying = fallFlying;
            this.flying = flying;
            this.sprinting = sprinting;
            this.crouching = crouching;
            this.swimming = swimming;
            this.resting = resting;
            return this;
        }
    }

    private static final float TAU = (float) (Math.PI * 2);

    public final WingPose pose = new WingPose();
    public Mode mode = Mode.IDLE;
    private float fold = 0.9f, yaw = 24f, amp = 2f, freq = 0.22f, raise, energy = 0.2f;
    // yay hizlari (kritik sonumlu: durum degisince hareket yumusakca baslar ve durur)
    private float vFold, vYaw, vAmp, vFreq, vRaise, vEnergy;
    private float phase, time, idleTime, boost;
    private double prevSpeed;
    private boolean started;

    /** dt: saniye. */
    public WingPose update(Motion m, float dt) {
        if (!(dt >= 0)) dt = 0;          // NaN korumasi
        dt = Math.min(dt, 0.1f);
        time += dt;
        if (time > 3600f) time -= 3600f; // uzun oturumlarda hassasiyet

        Mode md = classify(m);
        if (md != Mode.IDLE) idleTime = 0;
        else idleTime += dt;
        mode = md;

        // roket / ani hizlanma tespiti
        if (started && dt > 0) {
            double acc = (m.speed - prevSpeed) / dt;
            if (m.fallFlying && acc > 0.5) boost = 1f;
        }
        prevSpeed = m.speed;
        boost = Math.max(0f, boost - dt * 1.2f);

        float tf, ty, ta, tq, tr, te;
        switch (md) {
            case GLIDE -> {
                float p = clamp(Math.max(boost, (float) (m.vy + 0.03) * 8f), 0f, 1f);
                float cyc = (time % 3.4f) / 3.4f;
                float burst = p < 0.4f && cyc < 0.3f ? (float) Math.sin(Math.PI * cyc / 0.3f) : 0f;
                tf = 0.02f;
                ty = 12f + 6f * p;
                ta = 5f + 30f * p + 20f * burst;
                tq = 1.0f + 0.9f * p + 0.5f * burst;
                tr = 8f;
                te = 1f;
            }
            case FLY -> {
                boolean mv = m.speed > 0.08;
                tf = 0.05f;
                ty = mv ? 22f : 14f;
                ta = mv ? 36f : 30f;
                tq = mv ? 2.0f : 1.6f;
                tr = 6f;
                te = 1f;
            }
            case AIR -> {
                if (m.vy > 0.02) {
                    tf = 0.25f;
                    ty = 20f;
                    ta = 26f;
                    tq = 2.2f;
                    tr = 4f;
                } else {
                    float fall = clamp((float) -m.vy * 2.5f, 0f, 1f);
                    tf = 0.3f - 0.2f * fall;
                    ty = 14f;
                    ta = 12f + 12f * fall;
                    tq = 2.4f + 0.8f * fall;
                    tr = 10f + 14f * fall;
                }
                te = 0.7f;
            }
            case SPRINT -> {
                tf = 0.40f;
                ty = 52f;
                ta = 14f;
                tq = 1.65f;
                tr = 0f;
                te = 0.55f;
            }
            case WALK -> {
                // yari acik, geriye supurulmus V (gercek kanat durusu); yurudukce one-arkaya salinir
                float k = clamp((float) m.speed / 0.2f, 0f, 1f);
                tf = 0.48f - 0.08f * k;
                ty = 30f + 6f * k;
                ta = 9f + 6f * k;
                tq = 0.75f + 0.55f * k;
                tr = 6f;
                te = 0.35f;
            }
            case CROUCH -> {
                tf = 1f;
                ty = 8f;
                ta = 1f;
                tq = 0.25f;
                tr = -6f;
                te = 0.15f;
            }
            case SWIM -> {
                tf = 1f;
                ty = 40f;
                ta = 3f;
                tq = 0.5f;
                tr = 0f;
                te = 0.15f;
            }
            case REST -> {
                tf = 1f;
                ty = 20f;
                ta = 1f;
                tq = 0.2f;
                tr = 0f;
                te = 0.1f;
            }
            default -> {
                // Dururken: yururkenki acikliginda kalir (katlanip kuculmez), yavasca salinir;
                // 9 sn sonra (ve her 12 sn'de) kanatlarini iyice acip silkeler
                tf = 0.48f;
                ty = 30f;
                ta = 8f;
                tq = 0.42f;
                tr = 6f;
                te = 0.3f;
                float cyc = idleTime % 12f;
                if (idleTime > 9f && cyc > 9.2f) {
                    float e = (float) Math.sin(Math.PI * (cyc - 9.2f) / 2.8f);
                    tf -= 0.4f * e;
                    ta += 10f * e;
                    tq += 1.1f * e;
                    tr += 12f * e;
                    te += 0.35f * e;
                }
            }
        }

        if (!started) {
            fold = tf;
            yaw = ty;
            amp = ta;
            freq = tq;
            raise = tr;
            energy = te;
            started = true;
        } else {
            fold = damp(fold, tf, 0, 0.30f, dt);
            yaw = damp(yaw, ty, 1, 0.34f, dt);
            amp = damp(amp, ta, 2, 0.28f, dt);
            freq = damp(freq, tq, 3, 0.45f, dt);
            raise = damp(raise, tr, 4, 0.34f, dt);
            energy = damp(energy, te, 5, 0.40f, dt);
        }
        phase += TAU * freq * dt;
        if (phase > TAU * 64) phase -= TAU * 64;

        pose.fold = fold;
        pose.yaw = yaw;
        pose.amp = amp;
        pose.raise = raise;
        pose.phase = phase;
        pose.energy = energy;
        pose.time = time;
        return pose;
    }

    public static Mode classify(Motion m) {
        if (m.resting) return Mode.REST;
        if (m.fallFlying) return Mode.GLIDE;
        if (m.flying) return Mode.FLY;
        if (m.swimming) return Mode.SWIM;
        if (!m.onGround) return Mode.AIR;
        if (m.crouching) return Mode.CROUCH;
        if (m.speed > 0.17 || (m.sprinting && m.speed > 0.08)) return Mode.SPRINT;
        if (m.speed > 0.025) return Mode.WALK;
        return Mode.IDLE;
    }

    /** Kritik sonumlu yay (her dt icin kararli). slot: hiz degiskeni. */
    private float damp(float cur, float target, int slot, float smoothTime, float dt) {
        if (dt <= 0) return cur;
        float vel = switch (slot) {
            case 0 -> vFold;
            case 1 -> vYaw;
            case 2 -> vAmp;
            case 3 -> vFreq;
            case 4 -> vRaise;
            default -> vEnergy;
        };
        float omega = 2f / smoothTime;
        float x = omega * dt;
        float e = 1f / (1f + x + 0.48f * x * x + 0.235f * x * x * x);
        float change = cur - target;
        float temp = (vel + omega * change) * dt;
        vel = (vel - omega * temp) * e;
        float out = target + (change + temp) * e;
        switch (slot) {
            case 0 -> vFold = vel;
            case 1 -> vYaw = vel;
            case 2 -> vAmp = vel;
            case 3 -> vFreq = vel;
            case 4 -> vRaise = vel;
            default -> vEnergy = vel;
        }
        return out;
    }

    private static float clamp(float v, float a, float b) {
        return v < a ? a : (v > b ? b : v);
    }
}
