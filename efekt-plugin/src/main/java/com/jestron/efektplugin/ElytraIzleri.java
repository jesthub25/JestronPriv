package com.jestron.efektplugin;

import java.util.List;

/**
 * 50 elytra izi: Efekt Menusu modundaki (Jestron) 50 elytra gorunumunun renkleri ve parcaciklariyla, suzulurken kanat
 * uclarindan cikan iz. Sunucuda olusur, herkes gorur (elytranin dokusu modsuz degistirilemez; iz desenin renginde).
 */
public final class ElytraIzleri {

    /** Iz: kimlik, ad, aciklama, uc renk (koyu, ana, vurgu), desenin kendi parcacigi (yoksa null), parcacik orani. */
    public record Iz(String id, String name, String desc, int c1, int c2, int c3, String particle, float rate) {
    }

    public static final List<Iz> ALL = List.of(
            new Iz("anka", "Anka Kuşu", "Alevden tüyler, uçlarda kor", 0x3A0800, 0xFF5A00, 0xFFE27A, "flame", 1.4f),
            new Iz("melek", "Melek", "Beyaz tüyler, altın kenar, hale ışığı", 0xF4F1EA, 0xD8D2C4, 0xFFD54F, "end_rod", 0.4f),
            new Iz("ejder_zari", "Ejder Zarı", "Kemik çubuklar arasında kızıl zar", 0x2A0505, 0x9E1B1B, 0xE8D3A8, null, 0f),
            new Iz("yarasa", "Gece Yarasası", "Mor-siyah zar, tırtıklı kenar", 0x0D0814, 0x3B2152, 0x9C7BC2, null, 0f),
            new Iz("monark", "Monark Kelebeği", "Turuncu kanat, siyah damarlar, beyaz benek", 0xFF8A00, 0x141010, 0xFFFFFF, null, 0f),
            new Iz("tavus", "Tavus Kuşu", "Parlayan göz benekleri, yeşil-mavi tüy", 0x0A3B2E, 0x14A38B, 0x2E5BFF, null, 0f),
            new Iz("buz_kanat", "Buz Kanat", "Kristal parçalar, soğuk ışıltı", 0x0C3150, 0x7FD8FF, 0xFFFFFF, "snowflake", 1.0f),
            new Iz("nebula", "Nebula", "Dönen bulutsu, göz kırpan yıldızlar", 0x0A0420, 0x8E3BD8, 0x3FD0FF, "portal", 1.0f),
            new Iz("aurora_kanat", "Kutup Perdesi", "Kanat boyunca akan yeşil-mor ışık", 0x030A16, 0x22FF9C, 0xC04BFF, null, 0f),
            new Iz("siber", "Siber Neon", "Karanlık zırh, akan neon hatlar", 0x0A0A12, 0xFF2BD6, 0x2BE7FF, "electric_spark", 0.5f),
            new Iz("magma", "Magma", "Kara kaya, nabız gibi atan çatlaklar", 0x140C0A, 0xFF4500, 0xFFD15C, "lava", 0.3f),
            new Iz("yildirim", "Yıldırım", "Kanat boyunca çakan şimşekler", 0x10141F, 0x5B6B9A, 0xD9F2FF, "electric_spark", 0.9f),
            new Iz("dalga", "Okyanus Dalgası", "Derin mavi, köpüklü dalgalar", 0x022A48, 0x1287C4, 0xCFF6FF, "dripping_water", 0.6f),
            new Iz("selale", "Şelale", "Yukarıdan akan berrak su", 0x05324F, 0x3CB4E6, 0xFFFFFF, "falling_water", 0.8f),
            new Iz("prizma", "Prizma", "Kırılan ışık, kayan gökkuşağı", 0xEDEDF7, 0x8AC7FF, 0xFFFFFF, "dust:FF8AE6", 0.5f),
            new Iz("vitray", "Vitray", "Kurşun çizgili renkli cam", 0x1A1A1A, 0x2C6BD6, 0xFFD54F, null, 0f),
            new Iz("altin_zirh", "Altın Zırh", "Üst üste altın plakalar, kayan parlama", 0x7A5A00, 0xFFCC33, 0xFFF6C8, null, 0f),
            new Iz("sovalye", "Şövalye", "Perçinli çelik plakalar", 0x3C454D, 0xAEB9C2, 0xFFFFFF, null, 0f),
            new Iz("obsidyen_kanat", "Obsidyen", "Cam gibi kara taş, mor çizgiler", 0x06040B, 0x2B1748, 0xBA80FF, "reverse_portal", 0.6f),
            new Iz("bosluk_yirtigi", "Boşluk Yırtığı", "Kanattan açılan mor yarıklar", 0x000000, 0x3A0A66, 0xE070FF, "portal", 1.6f),
            new Iz("ruh_kanat", "Ruh Alevi", "Turkuaz ruh ateşi tüyler", 0x00161A, 0x00B8C8, 0xC2FFFF, "soul_fire_flame", 1.3f),
            new Iz("sarmasik", "Zehirli Sarmaşık", "Koyu yeşil, parlayan damarlı sarmaşık", 0x08180A, 0x2E8B3A, 0xB6FF3B, "dust:7CFF3B", 0.6f),
            new Iz("kiraz_kanat", "Kiraz Bahçesi", "Pembe çiçekler, süzülen yapraklar", 0xFFE6EE, 0xFF8FB4, 0x7A4A3A, "cherry_leaves", 0.9f),
            new Iz("yaprak", "Sonbahar Yaprağı", "Damarlı yaprak, tırtıklı kenar", 0x5A2408, 0xE8650E, 0xFFC23B, null, 0f),
            new Iz("yusufcuk", "Yusufçuk", "İnce damarlı yanardöner zar", 0x10202A, 0x4FD1C5, 0xD6FFF6, null, 0f),
            new Iz("petek", "Bal Peteği", "Altıgen petek, damlayan bal", 0x5A3500, 0xFFB000, 0xFFE9A0, "dripping_honey", 0.4f),
            new Iz("kara_melek", "Kara Melek", "Siyah tüyler, kızıl ışıltı", 0x0A0A0C, 0x26262C, 0xFF2A2A, "dust:FF2020", 0.5f),
            new Iz("seytan", "Cehennem", "Kızıl zar, kara kemik, kor", 0x220000, 0xB00A0A, 0x1A0A0A, "small_flame", 0.8f),
            new Iz("kar_firtinasi", "Kar Fırtınası", "Savrulan kar, buzlu mavi", 0x29507A, 0x9FC9F0, 0xFFFFFF, "snowflake", 1.2f),
            new Iz("gunes_patlamasi", "Güneş Patlaması", "Omuzdan fışkıran altın ışınlar", 0xB34700, 0xFFB300, 0xFFFDE0, "end_rod", 0.5f),
            new Iz("tutulma", "Tutulma", "Karanlık disk, parlayan taç", 0x020208, 0x1A1A3A, 0xFFE9B0, null, 0f),
            new Iz("takimyildiz", "Takımyıldız", "Çizgilerle bağlanan yıldızlar", 0x050B22, 0x1B2D66, 0xFFFFFF, null, 0f),
            new Iz("hologram", "Hologram", "Tarama çizgili titrek mavi ışık", 0x001018, 0x00C8FF, 0xCFFBFF, null, 0f),
            new Iz("matris", "Matris", "Yağan yeşil karakterler", 0x000A02, 0x00D24A, 0xD8FFD8, null, 0f),
            new Iz("retro", "Retro Gün Batımı", "Dilimli güneş, neon ızgara", 0x12002A, 0xFF3FA4, 0xFFD23F, null, 0f),
            new Iz("kan", "Kan Damlası", "Koyu kızıl, aşağı süzülen damlalar", 0x2A0003, 0x9A0010, 0xFF4A55, "dust:B00010", 0.4f),
            new Iz("mantar", "Mantar", "Kırmızı şapka, beyaz benekler", 0xB3121B, 0xE53935, 0xFFF8E1, null, 0f),
            new Iz("geode", "Ametist Jeodu", "Dışı taş, içi parlayan kristal", 0x3A2D3F, 0x8E44FF, 0xF0D6FF, null, 0f),
            new Iz("zumrut_run", "Zümrüt Rün", "Yeşil taş, nabız gibi atan rünler", 0x06261A, 0x0FA060, 0x8AFFC8, "happy_villager", 0.3f),
            new Iz("antik_tas", "Antik Taş", "Yosunlu taş, sönük oymalar", 0x3D3F3A, 0x6B6E64, 0x9BD18B, null, 0f),
            new Iz("kum_firtinasi", "Kum Fırtınası", "Akan kum tepeleri", 0x8A5A20, 0xE8C27A, 0xFFF0C8, null, 0f),
            new Iz("seker", "Şeker Kamışı", "Kıvrılan kırmızı-beyaz şeritler", 0xFFFFFF, 0xE0182D, 0x7CD992, null, 0f),
            new Iz("atesbocegi", "Ateşböceği", "Gece ormanı, uçuşan ışıklar", 0x06140A, 0x173A1E, 0xFFF27A, null, 0f),
            new Iz("plazma", "Plazma", "Kıvrılan pembe-mavi enerji", 0x14002A, 0xFF2BAA, 0x2BC4FF, "dust:FF55CC", 0.6f),
            new Iz("mermer", "Altın Mermer", "Beyaz mermer, altın damarlar", 0xEFEDE8, 0xC9C5BC, 0xD9A82B, null, 0f),
            new Iz("dijital_kamuflaj", "Dijital Kamuflaj", "Piksel lekeli orman deseni", 0x2E3B22, 0x5C6B3A, 0x9A9A6A, null, 0f),
            new Iz("kartal", "Kartal", "Kahverengi tüyler, beyaz uçlar", 0x3A220F, 0x7A4A22, 0xF2E6D0, null, 0f),
            new Iz("baykus", "Kar Baykuşu", "Beyaz tüy, siyah benekler", 0xF5F5F2, 0xD8D8D2, 0x1E1E1E, null, 0f),
            new Iz("ejder_atesi", "Ejder Ateşi", "Kara pullar arasından kızgın ışık", 0x0E0A0A, 0x2A1A18, 0xFF6A00, "small_flame", 0.7f),
            new Iz("kozmik_kraliyet", "Kozmik Kraliyet", "Kadife mor, altın yıldızlı kenar", 0x2A0845, 0x5E1B8C, 0xFFC94A, "end_rod", 0.3f));

    private ElytraIzleri() {
    }

    public static Iz get(String id) {
        if (id == null) return null;
        for (Iz z : ALL) if (z.id().equals(id)) return z;
        return null;
    }
}
