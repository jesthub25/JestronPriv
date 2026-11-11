package com.jestron.jestronpriv.client.view;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;

import java.util.Locale;

/**
 * Sadece senin ekraninda saat ve hava. Sunucuya hicbir sey gitmez; sunucunun saati arkada dogru islemeye devam eder
 * (istemcinin kendi saat ilerletmesi gercek degeri okur, bu yuzden kapatinca saat aninda dogru yere doner).
 */
public final class WorldTime {

    public enum TimeMode {
        SUNUCU("Sunucu"), SABIT("Sabit"), DONGU("Hızlı döngü");

        public final String label;

        TimeMode(String label) {
            this.label = label;
        }

        public TimeMode next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public static TimeMode parse(String s) {
            if (s != null) for (TimeMode m : values()) if (m.name().equals(s.trim().toUpperCase(Locale.ROOT))) return m;
            return SUNUCU;
        }
    }

    public enum Weather {
        SUNUCU("Sunucu"), ACIK("Açık"), YAGMUR("Yağmur"), FIRTINA("Fırtına");

        public final String label;

        Weather(String label) {
            this.label = label;
        }

        public Weather next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public static Weather parse(String s) {
            if (s != null) for (Weather m : values()) if (m.name().equals(s.trim().toUpperCase(Locale.ROOT))) return m;
            return SUNUCU;
        }
    }

    /** Hazir saatler: {etiket, deger}. */
    public static final String[] PRESET_NAMES = {"Gün doğumu", "Sabah", "Öğle", "Gün batımı", "Gece", "Gece yarısı"};
    public static final int[] PRESET_TIMES = {23200, 1000, 6000, 12300, 14500, 18000};

    /** ClientLevel.tickTime sirasinda gercek saat okunur/yazilir. */
    private static boolean raw;
    private static long cycleStart = System.nanoTime();

    private WorldTime() {
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    public static void setRaw(boolean v) {
        raw = v;
    }

    /** Gosterilecek gun saati; -1 = sunucununki. */
    public static long dayTime(long real) {
        if (raw) return real;
        ModConfig c = cfg();
        if (c == null) return real;
        TimeMode m = TimeMode.parse(c.timeMode);
        if (m == TimeMode.SABIT) {
            // gun sayisi korunur (ay evresi sabit kalmasin diye gercek gunun icine yerlestirilir)
            long day = Math.floorDiv(real, 24000L);
            return day * 24000L + Math.floorMod(c.timeValue, 24000);
        }
        if (m == TimeMode.DONGU) {
            double sec = (System.nanoTime() - cycleStart) / 1e9;
            long t = (long) (c.timeValue + sec * 20.0 * Math.max(1f, Math.min(200f, c.timeSpeed)));
            long day = Math.floorDiv(real, 24000L);
            return day * 24000L + Math.floorMod(t, 24000L);
        }
        return real;
    }

    /** Hedef yagmur; NaN = sunucununki. */
    static float rainTarget() {
        ModConfig c = cfg();
        if (c == null) return Float.NaN;
        return switch (Weather.parse(c.weather)) {
            case ACIK -> 0f;
            case YAGMUR, FIRTINA -> 1f;
            default -> Float.NaN;
        };
    }

    /** Hedef gok gurultusu; NaN = sunucununki. */
    static float thunderTarget() {
        ModConfig c = cfg();
        if (c == null) return Float.NaN;
        return switch (Weather.parse(c.weather)) {
            case ACIK, YAGMUR -> 0f;
            case FIRTINA -> 1f;
            default -> Float.NaN;
        };
    }

    // yumusak gecis (vanilla gibi birkac saniyede): gosterilen deger hedefe dogru akar; NaN = mudahale yok
    private static float shownRain = Float.NaN, shownThunder = Float.NaN;
    private static long rainNanos, thunderNanos;

    /** Gosterilecek yagmur seviyesi (real = sunucunun degeri). */
    public static float rain(float real) {
        float v = ease(shownRain, real, rainTarget(), System.nanoTime() - rainNanos);
        rainNanos = System.nanoTime();
        shownRain = v;
        return Float.isNaN(v) ? real : v;
    }

    /** Gosterilecek gok gurultusu seviyesi. */
    public static float thunder(float real) {
        float v = ease(shownThunder, real, thunderTarget(), System.nanoTime() - thunderNanos);
        thunderNanos = System.nanoTime();
        shownThunder = v;
        return Float.isNaN(v) ? real : v;
    }

    /** Saf gecis adimi: NaN donerse sunucu degeri kullanilir. */
    public static float ease(float shown, float real, float target, long dtNanos) {
        if (Float.isNaN(real) || Float.isInfinite(real)) real = 0f;
        if (Float.isNaN(shown)) {
            if (Float.isNaN(target)) return Float.NaN;
            shown = real;
        } else {
            float goal = Float.isNaN(target) ? real : target;
            float dt = Math.max(0f, Math.min(0.25f, dtNanos / 1e9f));
            shown += (goal - shown) * (1f - (float) Math.exp(-dt * 1.4f));
            if (Float.isNaN(target) && Math.abs(shown - real) < 0.004f) return Float.NaN;
        }
        return Math.max(0f, Math.min(1f, shown));
    }

    /** Dunya degisince gecis sifirlanir (yeni dunyada aninda dogru hava). */
    public static void reset() {
        shownRain = Float.NaN;
        shownThunder = Float.NaN;
    }

    public static void restartCycle() {
        cycleStart = System.nanoTime();
    }

    public static String clock(int t) {
        int tt = Math.floorMod(t + 6000, 24000);
        int h = tt / 1000, m = (tt % 1000) * 60 / 1000;
        return String.format(Locale.ROOT, "%02d:%02d", h, m);
    }
}
