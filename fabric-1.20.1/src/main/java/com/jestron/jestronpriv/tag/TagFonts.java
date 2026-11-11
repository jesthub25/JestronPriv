package com.jestron.jestronpriv.tag;

import java.util.List;

/** Isim etiketi fontlari: MC'nin yerlesik fontlari + mod icindeki 42 TTF font (hepsi Turkce karakterli). */
public final class TagFonts {

    /** id: config degeri; res: MC font kaynak kimligi ("ad:yol"). */
    public record Font(String id, String name, String res) {
    }

    public static final List<Font> ALL = List.of(
            new Font("mc", "Minecraft", "minecraft:default"),
            new Font("mc_uniform", "Unicode (Unifont)", "minecraft:uniform"),
            new Font("mc_galaktik", "Büyü Dili (Galaktik)", "minecraft:alt"),
            new Font("mc_illager", "İllager Dili", "minecraft:illageralt"),
            new Font("piksel", "Piksel", "jestronpriv:piksel"),
            new Font("retro_oyun", "Retro Oyun", "jestronpriv:retro_oyun"),
            new Font("terminal", "Terminal", "jestronpriv:terminal"),
            new Font("mini_piksel", "Mini Piksel", "jestronpriv:mini_piksel"),
            new Font("forma", "Forma", "jestronpriv:forma"),
            new Font("futuristik", "Fütüristik", "jestronpriv:futuristik"),
            new Font("siber", "Siber", "jestronpriv:siber"),
            new Font("teknoloji", "Teknoloji", "jestronpriv:teknoloji"),
            new Font("spor", "Sportif", "jestronpriv:spor"),
            new Font("tabela", "Tabela", "jestronpriv:tabela"),
            new Font("golgeli_3d", "3D Gölgeli", "jestronpriv:golgeli_3d"),
            new Font("retro", "Retro", "jestronpriv:retro"),
            new Font("baslik", "Uzun Başlık", "jestronpriv:baslik"),
            new Font("ince_uzun", "İnce Uzun", "jestronpriv:ince_uzun"),
            new Font("askeri", "Askeri", "jestronpriv:askeri"),
            new Font("sablon", "Şablon", "jestronpriv:sablon"),
            new Font("cizgi_roman", "Çizgi Roman", "jestronpriv:cizgi_roman"),
            new Font("eglenceli", "Eğlenceli", "jestronpriv:eglenceli"),
            new Font("sevimli", "Sevimli", "jestronpriv:sevimli"),
            new Font("balon", "Balon", "jestronpriv:balon"),
            new Font("glitch", "Glitch", "jestronpriv:glitch"),
            new Font("akan_boya", "Akan Boya", "jestronpriv:akan_boya"),
            new Font("ay_tasi", "Ay Taşı", "jestronpriv:ay_tasi"),
            new Font("neon_cizgi", "Neon Çizgi", "jestronpriv:neon_cizgi"),
            new Font("hiz", "Hız", "jestronpriv:hiz"),
            new Font("korku", "Korku", "jestronpriv:korku"),
            new Font("el_yazisi", "El Yazısı", "jestronpriv:el_yazisi"),
            new Font("plaj", "Plaj", "jestronpriv:plaj"),
            new Font("dans", "Dans", "jestronpriv:dans"),
            new Font("zarif", "Zarif", "jestronpriv:zarif"),
            new Font("not_defteri", "Not Defteri", "jestronpriv:not_defteri"),
            new Font("kalem", "Kalem", "jestronpriv:kalem"),
            new Font("kraliyet", "Kraliyet", "jestronpriv:kraliyet"),
            new Font("korsan", "Korsan", "jestronpriv:korsan"),
            new Font("gotik", "Gotik", "jestronpriv:gotik"),
            new Font("daktilo", "Daktilo", "jestronpriv:daktilo"),
            new Font("yumusak", "Yumuşak", "jestronpriv:yumusak"),
            new Font("oyuncu", "Oyuncu", "jestronpriv:oyuncu"),
            new Font("modern", "Modern", "jestronpriv:modern"),
            new Font("led", "LED Dijital", "jestronpriv:led"),
            new Font("nokta_matris", "Nokta Matris", "jestronpriv:nokta_matris"),
            new Font("minik", "Minik", "jestronpriv:minik"),
            new Font("canavar", "Canavar", "jestronpriv:canavar"),
            new Font("golcuk", "Su Birikintisi", "jestronpriv:golcuk"),
            new Font("plak", "Plak", "jestronpriv:plak"),
            new Font("labirent", "Labirent", "jestronpriv:labirent")
    );

    private TagFonts() {
    }

    public static Font get(String id) {
        if (id != null) for (Font f : ALL) if (f.id().equals(id)) return f;
        return ALL.get(0);
    }

    public static int indexOf(String id) {
        for (int i = 0; i < ALL.size(); i++) if (ALL.get(i).id().equals(id)) return i;
        return 0;
    }
}
