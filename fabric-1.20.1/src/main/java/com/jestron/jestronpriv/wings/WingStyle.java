package com.jestron.jestronpriv.wings;

/**
 * Bir kanat stilinin tum gorunum parametreleri. Doku: assets/jestronpriv/textures/wings/&lt;id&gt;.png (256x256 atlas).
 *
 * @param tint       kanat boyunca renk carpani: [ic, orta, dis] (RGB)
 * @param rainbow    tint yerine zamanla akan gokkusagi
 * @param glowColor  isilti katmani rengi (RGB)
 * @param glow       isilti gucu (0 = yok)
 * @param pulse      isilti nabiz hizi (Hz, 0 = sabit)
 * @param flicker    titreme / cakma miktari (0..1)
 * @param scroll     kayan desen hizi (doku yuksekligi / sn, 0 = yok)
 * @param translucent taban katman yari saydam mi
 * @param alpha      taban katman opakligi
 * @param size       boyut carpani
 * @param particle   parcacik ("flame", "dust:RRGGBB", "rainbow" veya null)
 * @param particleRate saniyedeki ortalama parcacik (ucarken 2.5 kat)
 * @param pairs      kanat cifti sayisi (1-3; tuy turlerinde)
 */
public record WingStyle(String id, String name, String desc, WingKind kind,
                        int[] tint, boolean rainbow,
                        int glowColor, float glow, float pulse, float flicker, float scroll,
                        boolean translucent, float alpha, float size,
                        String particle, float particleRate, int pairs) {

    public String texturePath() {
        return "textures/wings/" + id + ".png";
    }

    /** Akici tanim icin kurucu. */
    public static final class B {
        private final String id, name, desc;
        private final WingKind kind;
        private int[] tint = {0xFFFFFF, 0xFFFFFF, 0xFFFFFF};
        private boolean rainbow, translucent;
        private int glowColor = 0xFFFFFF;
        private float glow, pulse, flicker, scroll, alpha = 1f, size = 1f, rate;
        private int pairs = 1;
        private String particle;

        public B(String id, String name, String desc, WingKind kind) {
            this.id = id;
            this.name = name;
            this.desc = desc;
            this.kind = kind;
        }

        public B tint(int inner, int mid, int outer) {
            tint = new int[]{inner, mid, outer};
            return this;
        }

        public B rainbow() {
            rainbow = true;
            return this;
        }

        public B glow(int color, float strength) {
            glowColor = color;
            glow = strength;
            return this;
        }

        public B pulse(float hz) {
            pulse = hz;
            return this;
        }

        public B flicker(float f) {
            flicker = f;
            return this;
        }

        public B scroll(float s) {
            scroll = s;
            return this;
        }

        public B translucent(float a) {
            translucent = true;
            alpha = a;
            return this;
        }

        public B size(float s) {
            size = s;
            return this;
        }

        public B particle(String p, float perSecond) {
            particle = p;
            rate = perSecond;
            return this;
        }

        public B pairs(int n) {
            pairs = n;
            return this;
        }

        public WingStyle build() {
            return new WingStyle(id, name, desc, kind, tint, rainbow, glowColor, glow, pulse, flicker, scroll,
                    translucent, alpha, size, particle, rate, pairs);
        }
    }
}
