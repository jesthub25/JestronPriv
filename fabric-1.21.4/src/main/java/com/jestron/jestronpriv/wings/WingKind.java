package com.jestron.jestronpriv.wings;

/** Kanat geometri / malzeme turu. */
public enum WingKind {
    /** Katmanli gercek tuyler (birincil, ikincil, ortu tuyleri). */
    FEATHER,
    /** Tuy dizilimiyle suzulen yari saydam kristal parcalari. */
    CRYSTAL,
    /** Koyu tuy uzerinde akan isik deseni. */
    ENERGY,
    /** Kemik parmaklar arasinda gerili zar (ejderha / yarasa). */
    MEMBRANE,
    /** On + arka kanat ciftleri (kelebek). */
    BUTTERFLY,
    /**
     * Oyunun kendi 3D tuy esyalarindan yelpaze (oldurme efektindeki melek kanadiyla ayni dizilim).
     * Dortgen uretmez; {@link WingMesh#items} doldurulur ve tuyler esya olarak cizilir.
     */
    HOLY
}
