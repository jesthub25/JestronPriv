package com.jestron.jestronpriv.client.camera;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * Bas-bak (varsayilan Sol Alt): tus basili tutuldugu surece 3. sahis kamera serbestce doner
 * (arkana, yanina bakabilirsin), karakter ise hic donmeden gittigi yone gitmeye devam eder.
 * Birakinca kamera yumusakca eski yerine kayar ve onceki gorunume (1. sahis vb.) doner;
 * karakter kameranin baktigi yone CEVRILMEZ.
 */
public final class FreeLook {

    private enum State { OFF, LOOK, RETURN }

    private static final float RETURN_SEC = 0.18f;

    private static State state = State.OFF;
    private static CameraType prev, ours;
    private static float yaw, pitch, fromYaw, fromPitch, retT;
    private static long lastNanos;
    private static KeyMapping key;
    /** Bakis sirasinda kullanici gorunumu (F5) degistirdiyse tus birakilana kadar tekrar baslama. */
    private static boolean suppressed;

    private FreeLook() {
    }

    public static void setKey(KeyMapping k) {
        key = k;
    }

    /** Bas-bak calisiyor (bakis veya donus animasyonu). */
    public static boolean busy() {
        return state != State.OFF;
    }

    public static boolean looking() {
        return state == State.LOOK;
    }

    private static boolean wanted(Minecraft mc) {
        JestronPrivClient mod = JestronPrivClient.get();
        ModConfig c = mod == null ? null : mod.config();
        LocalPlayer p = mc.player;
        return c != null && c.freeLook && key != null && key.isDown() && mc.screen == null && p != null && mc.level != null
                && p.isAlive() && !p.isSleeping() && mc.getCameraEntity() == p;
    }

    /** Tusu izler (tick, fare ve kamera yolundan cagrilir; tekrar cagrilmasi zararsiz). */
    public static void update() {
        Minecraft mc = Minecraft.getInstance();
        boolean want = wanted(mc);
        if (!want) suppressed = false;
        // bakarken F5'e basildiysa kullanicinin secimi gecerli: bakis biter, tus birakilana kadar yeniden baslamaz
        if (state == State.LOOK && mc.options.getCameraType() != CameraType.THIRD_PERSON_BACK) {
            suppressed = true;
            reset(mc, false);
        }
        if (suppressed) want = false;
        if (want && state != State.LOOK) start(mc);
        else if (!want && state == State.LOOK) release(mc);
        if (state != State.OFF && (mc.player == null || mc.level == null)) reset(mc, false);
        // donus animasyonu sadece arka gorunumde ilerler; gorunum degistiyse ya da kare gelmiyorsa hemen bitir
        if (state == State.RETURN && (mc.options.getCameraType() != CameraType.THIRD_PERSON_BACK
                || System.nanoTime() - lastNanos > 500_000_000L)) reset(mc, mc.options.getCameraType() == ours);
    }

    private static void start(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (state == State.OFF) {
            prev = mc.options.getCameraType();
            // serbest kamerada ise onun acisindan, degilse karakterin bakisindan baslar
            if (FreeCam.activeIgnoringLook()) {
                yaw = FreeCam.yaw();
                pitch = FreeCam.pitch();
            } else {
                yaw = p.getYRot();
                pitch = p.getXRot();
            }
        }
        // (RETURN sirasinda tekrar basilirsa kaldigi aciyla devam eder)
        state = State.LOOK;
        if (mc.options.getCameraType() != CameraType.THIRD_PERSON_BACK) {
            boolean wasFirst = mc.options.getCameraType().isFirstPerson();
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            perspectiveChanged(mc, wasFirst);
        }
        ours = CameraType.THIRD_PERSON_BACK;
    }

    private static void release(Minecraft mc) {
        state = State.RETURN;
        fromYaw = yaw;
        fromPitch = pitch;
        retT = 0;
        lastNanos = System.nanoTime();
    }

    /** Fare: bakis sirasinda kamerayi dondurur. true = karakter donmez. */
    public static boolean onTurn(double dYaw, double dPitch) {
        update();
        if (state != State.LOOK) return false;
        yaw += (float) dYaw * 0.15f;
        pitch = Math.max(-90f, Math.min(90f, pitch + (float) dPitch * 0.15f));
        if (yaw > 36000f || yaw < -36000f) yaw = FreeCam.wrap(yaw);
        return true;
    }

    /**
     * Kamera kurulumu (her kare, arka gorunumde). true donerse yaw()/pitch() kullanilir.
     *
     * @param targetYaw   donus hedefi (karakterin ya da serbest kameranin bakisi)
     */
    public static boolean camera(float targetYaw, float targetPitch) {
        update();
        if (state == State.LOOK) return true;
        if (state != State.RETURN) return false;
        long now = System.nanoTime();
        float dt = Math.min(0.1f, Math.max(0f, (now - lastNanos) / 1e9f));
        lastNanos = now;
        retT += dt / RETURN_SEC;
        if (retT >= 1f) {
            finish(Minecraft.getInstance());
            return false;
        }
        float k = retT * retT * (3 - 2 * retT);
        yaw = fromYaw + FreeCam.wrap(targetYaw - fromYaw) * k;
        pitch = fromPitch + (targetPitch - fromPitch) * k;
        return true;
    }

    private static void finish(Minecraft mc) {
        reset(mc, true);
    }

    private static void reset(Minecraft mc, boolean restore) {
        state = State.OFF;
        if (restore && prev != null && mc.options.getCameraType() == ours && prev != ours) {
            // kullanici bakis sirasinda F5'e bastiysa onun secimi korunur
            mc.options.setCameraType(prev);
            perspectiveChanged(mc, false);
        }
        prev = null;
        ours = null;
    }

    private static void perspectiveChanged(Minecraft mc, boolean wasFirst) {
        try {
            boolean first = mc.options.getCameraType().isFirstPerson();
            if (wasFirst != first && mc.gameRenderer != null) mc.gameRenderer.checkEntityPostEffect(first ? mc.getCameraEntity() : null);
            if (mc.levelRenderer != null) mc.levelRenderer.needsUpdate();
        } catch (Throwable ignored) {
        }
    }

    public static float yaw() {
        return yaw;
    }

    public static float pitch() {
        return pitch;
    }
}
