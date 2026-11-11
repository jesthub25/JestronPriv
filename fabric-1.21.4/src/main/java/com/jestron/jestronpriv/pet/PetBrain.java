package com.jestron.jestronpriv.pet;

/**
 * Yoldasin hareketi (MC'den bagimsiz): oyuncuya gore hedef nokta, kritik sonumlu yay ile takip, suzulme, yon,
 * egilme, kanat cirpma hizi, goz kirpma, kalp atisi ve sevinc (zipla + don) tepkileri.
 */
public final class PetBrain {

    // dunya konumu ve hizi
    public double x, y, z;
    private double vx, vy, vz;
    // hedefin hizi (on besleme: oyuncu kosarken / ucarken geride kalmaz)
    private double ltx, lty, ltz, tvx, tvy, tvz;
    private boolean placed;
    /** Yoldasin baktigi yon (MC yaw, derece) ve egilmesi (radyan). */
    public float yaw, tilt, bank;
    public final PetMesh.Anim anim = new PetMesh.Anim();

    private float flapPhase, blinkT, nextBlink = 2.5f, beatT, hopT = -1f, spinT = -1f, spinTurns;
    private float time;
    private long seed = 0x5DEECE66DL;

    /** Bir sonraki rastgele sayi [0,1) (deterministik, nesne uretmez). */
    private float rnd() {
        seed = (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
        return (int) (seed >>> 24) / (float) (1 << 24);
    }

    public void reset() {
        placed = false;
        vx = vy = vz = 0;
        tvx = tvy = tvz = 0;
        hopT = spinT = -1f;
    }

    /** Vurusta kucuk zipla, oldurmede iki tur donerek zipla. */
    public void react(boolean kill) {
        hopT = 0f;
        if (kill) {
            spinT = 0f;
            spinTurns = 2f;
        } else if (spinT < 0f && rnd() < 0.35f) {
            spinT = 0f;
            spinTurns = 1f;
        }
    }

    /** Oyuncunun yaw'ina gore yerel ofset (x: sol(+)/sag(-), y yukari, z ileri). */
    public static void offset(PetMode mode, boolean left, boolean firstPerson, boolean crouch, float t, double[] out) {
        double side = left ? 1 : -1;
        double ox, oy, oz;
        switch (mode) {
            case OMUZ -> {
                ox = 0.42 * side;
                oy = 1.62;
                oz = -0.04;
            }
            case ARKA -> {
                ox = 0.32 * side;
                oy = 2.15;
                oz = -0.85;
            }
            case YORUNGE -> {
                double a = t * 0.9;
                ox = Math.cos(a) * 1.05;
                oy = 1.75 + Math.sin(t * 1.7) * 0.12;
                oz = Math.sin(a) * 1.05;
            }
            default -> {
                ox = 0.85 * side;
                oy = 1.7;
                oz = -0.3;
            }
        }
        if (firstPerson && mode != PetMode.YORUNGE) {
            // 1. sahista goz kenarinda, onde ve biraz asagida (kameraya girmez, gorus alanini kapatmaz)
            ox = 0.8 * side;
            oy = 1.38;
            oz = 0.95;
        }
        if (crouch) oy -= 0.3;
        out[0] = ox;
        out[1] = oy;
        out[2] = oz;
    }

    private final double[] off = new double[3];

    /**
     * @param px,py,pz  oyuncu ayak konumu (karede araya girilmis)
     * @param frameYaw  ofset icin kullanilan yaw (govde ya da kamera, derece)
     * @param lookYaw   oyuncunun bakis yaw'i, lookPitch bakis pitch'i (derece)
     * @param follow    takip hizi 0.3 - 3, bob suzulme 0 - 2
     */
    public void update(float dt, double px, double py, double pz, float frameYaw, float lookYaw, float lookPitch,
                       PetMode mode, boolean left, boolean firstPerson, boolean crouch, float follow, float bob, PetKind kind) {
        if (!(dt >= 0f) || dt > 0.25f) dt = 0.05f;
        time += dt;
        offset(mode, left, firstPerson, crouch, time, off);
        double r = Math.toRadians(frameYaw);
        double sin = Math.sin(r), cos = Math.cos(r);
        // MC: ileri = (-sin, 0, cos), sol = (cos, 0, sin)
        double tx = px + off[0] * cos - off[2] * sin;
        double ty = py + off[1] + Math.sin(time * 2.1) * 0.045 * bob;
        double tz = pz + off[0] * sin + off[2] * cos;
        if (!placed || !Double.isFinite(x + y + z) || dist2(tx, ty, tz) > 14 * 14) {
            x = tx;
            y = ty;
            z = tz;
            vx = vy = vz = 0;
            yaw = lookYaw;
            placed = true;
            ltx = tx;
            lty = ty;
            ltz = tz;
            tvx = tvy = tvz = 0;
        }
        if (dt > 1e-4f) {
            double ax = (tx - ltx) / dt, ay = (ty - lty) / dt, az = (tz - ltz) / dt;
            // asiri sicramalari (isinlanma, ilk kare) sinirla, yumusat
            double sp = Math.sqrt(ax * ax + ay * ay + az * az);
            if (sp > 60) {
                ax = ay = az = 0;
            }
            double kk = 1.0 - Math.exp(-dt * 12.0);
            tvx += (ax - tvx) * kk;
            tvy += (ay - tvy) * kk;
            tvz += (az - tvz) * kk;
        }
        ltx = tx;
        lty = ty;
        ltz = tz;
        // kritik sonumlu yay, kucuk adimlarla (kararli)
        double k = 38.0 * follow * follow, c = 2.0 * Math.sqrt(k);
        float left2 = dt;
        while (left2 > 1e-5f) {
            float h = Math.min(0.01f, left2);
            left2 -= h;
            vx += (k * (tx - x) - c * (vx - tvx)) * h;
            vy += (k * (ty - y) - c * (vy - tvy)) * h;
            vz += (k * (tz - z) - c * (vz - tvz)) * h;
            x += vx * h;
            y += vy * h;
            z += vz * h;
        }
        // zipla
        float hop = 0f;
        if (hopT >= 0f) {
            hopT += dt;
            float u = hopT / 0.45f;
            if (u >= 1f) hopT = -1f;
            else hop = (float) Math.sin(Math.PI * u) * 0.22f;
        }
        anim.time = time;
        // yon: hizli giderken gidis yonune, yoksa oyuncunun baktigi yere
        double hs = Math.sqrt(vx * vx + vz * vz);
        float want = lookYaw;
        if (hs > 1.2 && mode != PetMode.YORUNGE) want = (float) Math.toDegrees(Math.atan2(-vx, vz));
        float dy = wrap(want - yaw);
        yaw += dy * (1f - (float) Math.exp(-dt * 6f));
        // egilme: hiz yonunde one, donuste yana
        double fx = -Math.sin(Math.toRadians(yaw)), fz = Math.cos(Math.toRadians(yaw));
        float fwd = (float) (vx * fx + vz * fz);
        float tiltT = clamp(fwd * 0.09f, -0.35f, 0.5f);
        tilt += (tiltT - tilt) * (1f - (float) Math.exp(-dt * 8f));
        float bankT = clamp(-dy * 0.008f, -0.4f, 0.4f);
        bank += (bankT - bank) * (1f - (float) Math.exp(-dt * 6f));
        // kanat cirpma: hizla hizlanir
        float flapSpeed = kind == PetKind.PERI ? 16f : kind == PetKind.YARASA ? 11f + (float) Math.min(8.0, hs * 3.0) : 7f + (float) Math.min(10.0, hs * 3.0);
        flapPhase += dt * flapSpeed;
        if (flapPhase > 1000f) flapPhase -= (float) (Math.PI * 2 * 100);
        anim.flap = flapPhase;
        anim.speed = (float) hs;
        // goz kirpma
        blinkT += dt;
        if (blinkT > nextBlink) {
            blinkT = 0f;
            nextBlink = 2.2f + rnd() * 4f;
        }
        anim.blink = blinkT < 0.16f ? (float) Math.sin(Math.PI * blinkT / 0.16f) : 0f;
        // kalp: iki vuruslu nabiz (lub-dub), ~70 bpm
        beatT = (beatT + dt) % 0.85f;
        anim.beat = pulse(beatT, 0.1f) + 0.6f * pulse(beatT, 0.32f);
        // sevinc donusu
        anim.spin = 0f;
        if (spinT >= 0f) {
            spinT += dt;
            float dur = 0.45f * spinTurns;
            float u = spinT / dur;
            if (u >= 1f) spinT = -1f;
            else anim.spin = (float) (spinTurns * 2 * Math.PI * (u * u * (3 - 2 * u)));
        }
        hopOffset = hop;
        // goz: oyuncunun baktigi yone gore (yoldasin yonune goreli)
        anim.lookYaw = clamp((float) Math.toRadians(wrap(lookYaw - yaw)) * -1f, -0.9f, 0.9f);
        anim.lookPitch = clamp((float) Math.toRadians(lookPitch), -0.7f, 0.7f);
    }

    /** Ziplama icin gorsel dikey ofset (blok). */
    public float hopOffset;

    private static float pulse(float t, float at) {
        float d = (t - at) / 0.07f;
        return d < -1f || d > 3f ? 0f : d < 0f ? 1f + d : (float) Math.exp(-d * 1.4f) * (1f - d / 3f);
    }

    private double dist2(double tx, double ty, double tz) {
        double dx = x - tx, dy = y - ty, dz = z - tz;
        return dx * dx + dy * dy + dz * dz;
    }

    static float wrap(float d) {
        d %= 360f;
        if (d >= 180f) d -= 360f;
        if (d < -180f) d += 360f;
        return d;
    }

    private static float clamp(float v, float lo, float hi) {
        return Float.isNaN(v) ? 0f : Math.max(lo, Math.min(hi, v));
    }
}
