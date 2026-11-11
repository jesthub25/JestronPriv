package com.jestron.jestronpriv.cape;

/**
 * Bir pelerin deseni.
 *
 * @param c1       ana renk (ic yuz otomatik rengi bundan turetilir)
 * @param c2       ikinci renk
 * @param c3       vurgu (kenar seridi)
 * @param animated desen zamanla degisiyor mu (degismiyorsa doku sadece bir kez uretilir)
 * @param particle parcacik kimligi (vanilla id, "dust:RRGGBB" ya da null)
 * @param rate     saniyede parcacik
 */
public record CapeDesign(String id, String name, String desc, int c1, int c2, int c3, boolean animated, String particle, float rate,
                         Painter painter) {

    /** Dis yuzun bir pikselini boyar: {@code p.rgb} ve {@code p.glow} yazilir. */
    @FunctionalInterface
    public interface Painter {
        void paint(Px p);
    }
}
