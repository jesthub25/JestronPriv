package com.jestron.jestronpriv.fx;

import com.jestron.jestronpriv.fx.api.Color;
import com.jestron.jestronpriv.fx.api.Display;

/** Efekt cekirdeginin Minecraft istemcisine ihtiyac duydugu her sey. */
public interface FxBackend {

    /** Parcacik verisi: renkli toz. */
    record Dust(Color color, float size) {
    }

    /** Parcacik verisi: renk gecisli toz. */
    record DustFade(Color from, Color to, float size) {
    }

    /** Oyun duraklatilinca durmayan, her istemci tick'inde artan sayac. */
    long now();

    void addDisplay(Display d);

    void removeDisplay(Display d);

    /**
     * count > 0: (ox,oy,oz) gauss dagilim yaricapi, speed rastgele hiz.
     * count == 0: tek parcacik, hiz = (ox,oy,oz) * speed.
     * data: Dust, DustFade, Material (blok parcacigi), getGroundState() sonucu veya null.
     */
    void particle(String id, double x, double y, double z, int count, double ox, double oy, double oz, double speed, Object data);

    void sound(String key, double x, double y, double z, float volume, float pitch);

    void stopSound(String key);

    boolean isLoaded(int blockX, int blockZ);

    int minY();

    /** Blok carpisma kutusu bos mu (icinden gecilebilir). */
    boolean isPassable(int x, int y, int z);

    /** Blok carpisma kutusunun mutlak ust yuksekligi (gecilebilirse y). */
    double topY(int x, int y, int z);

    /** Blok parcacigi icin opak blok durumu (gecilebilir veya sivi ise null). */
    Object groundState(int x, int y, int z);

    /** Blogun kimligi, ornek "grass_block". */
    String blockId(int x, int y, int z);

    /** Tam opak, kup seklinde blok mu. */
    boolean isFullSolid(int x, int y, int z);

    /** Paketlenmis isik (blok, gok) noktada. */
    int lightAt(int x, int y, int z);
}
