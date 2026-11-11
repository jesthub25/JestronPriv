package com.jestron.jestronpriv.hats;

/**
 * Bir sapka tasarimi.
 *
 * @param icon     menude gosterilen esya (minecraft esya kimligi, ornegin "golden_helmet")
 * @param c1       varsayilan birincil renk (ozel renk secilince bu degisir)
 * @param particle parcacik ("dust:RRGGBB", "rainbow" ya da vanilla basit parcacik kimligi); null = yok
 * @param rate     saniyedeki parcacik sayisi
 */
public record HatStyle(String id, String name, String desc, String icon, int c1, int c2, int c3, String particle, float rate,
                       Builder builder) {

    @FunctionalInterface
    public interface Builder {
        void build(HatMesh m, HatCtx x);
    }
}
