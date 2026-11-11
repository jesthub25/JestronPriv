package com.jestron.jestronpriv.client.view;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;

import java.util.Locale;

/** Ekran orani (stretched) ve dusuk ates ayarlari. */
public final class ScreenTweaks {

    private ScreenTweaks() {
    }

    public enum Aspect {
        KAPALI("Kapalı", 0F),
        R4_3("4:3", 4F / 3F),
        R5_4("5:4", 5F / 4F),
        R3_2("3:2", 3F / 2F),
        R16_10("16:10", 16F / 10F),
        R1_1("1:1", 1F),
        OZEL("Özel", -1F);

        public final String label;
        final float ratio;

        Aspect(String label, float ratio) {
            this.label = label;
            this.ratio = ratio;
        }

        public Aspect next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public static Aspect parse(String s) {
            if (s != null) {
                String n = s.trim().toUpperCase(Locale.ROOT);
                for (Aspect a : values()) if (a.name().equals(n)) return a;
            }
            return KAPALI;
        }
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    /**
     * Projeksiyonun en-boy orani. Hedef oran pencereden darsa goruntu yatayda gerilir
     * (rekabetci "stretched" cozunurluk hissi); arayuz hic etkilenmez.
     */
    public static float aspect(float window) {
        ModConfig c = cfg();
        if (c == null || !(window > 0F) || Float.isNaN(window)) return window;
        Aspect a = Aspect.parse(c.aspect);
        if (a == Aspect.KAPALI) return window;
        float r = a == Aspect.OZEL ? c.aspectCustom : a.ratio;
        return r > 0.3F && r < 4F ? r : window;
    }

    /** Ates katmaninin asagi kaydirilmasi (0 = vanilla). */
    public static float fireY(float vanilla) {
        ModConfig c = cfg();
        return c == null ? vanilla : vanilla - c.fireHeight;
    }

    public static float fireAlpha(float vanilla) {
        ModConfig c = cfg();
        return c == null ? vanilla : vanilla * c.fireOpacity;
    }
}
