package com.jestron.jestronpriv.client.view;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

/**
 * Yakinlastirma (varsayilan C, basili tut). Yumusak gecis, basiliyken fare tekerlegiyle seviye
 * ayari ve yakinlastikca fare hassasiyetinin otomatik dusmesi (nisan titremez).
 */
public final class Zoom {

    private static KeyMapping key;
    private static float current = 1F;
    /** Tekerlekle degistirilen anlik seviye (tus birakilinca ayardaki degere doner). */
    private static float wheel = -1F;
    private static long lastNanos;

    private Zoom() {
    }

    public static void setKey(KeyMapping k) {
        key = k;
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    /** Tus su an basili ve yakinlastirma acik mi. */
    public static boolean held() {
        ModConfig c = cfg();
        if (c == null || !c.zoomEnabled || key == null) return false;
        Minecraft mc = Minecraft.getInstance();
        return mc.screen == null && mc.player != null && key.isDown();
    }

    private static float target() {
        ModConfig c = cfg();
        if (!held()) {
            wheel = -1F;
            return 1F;
        }
        return wheel > 0F ? wheel : c.zoomFactor;
    }

    /** FOV boleni; her karede bir kez (oyun FOV'u hesaplanirken) cagrilir. */
    public static float factor() {
        long now = System.nanoTime();
        float dt = lastNanos == 0L ? 0F : Math.min(0.1F, Math.max(0F, (now - lastNanos) / 1e9F));
        lastNanos = now;
        float t = target();
        ModConfig c = cfg();
        if (c == null || !c.zoomSmooth) {
            current = t;
        } else {
            // ussel yaklasim (kare hizindan bagimsiz), ~0.12 sn'de %90
            float k = 1F - (float) Math.exp(-dt * 19.0);
            current += (t - current) * k;
            if (Math.abs(current - t) < 0.002F) current = t;
        }
        return Math.max(1F, current);
    }

    /** Fare hassasiyeti carpani (yakinlastikca azalir). */
    public static double mouseScale() {
        return current > 1.01F ? 1.0 / current : 1.0;
    }

    /** Basiliyken tekerlek: true = olay tuketildi (hotbar kaymaz). */
    public static boolean onScroll(double amount) {
        ModConfig c = cfg();
        if (!held() || !c.zoomScroll || amount == 0) return false;
        float base = wheel > 0F ? wheel : c.zoomFactor;
        float next = (float) (base * Math.pow(1.18, Math.signum(amount)));
        wheel = Math.max(1.5F, Math.min(50F, next));
        return true;
    }
}
