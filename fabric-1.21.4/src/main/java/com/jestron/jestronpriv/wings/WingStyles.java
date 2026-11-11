package com.jestron.jestronpriv.wings;

import java.util.List;

import static com.jestron.jestronpriv.wings.WingKind.BUTTERFLY;
import static com.jestron.jestronpriv.wings.WingKind.CRYSTAL;
import static com.jestron.jestronpriv.wings.WingKind.ENERGY;
import static com.jestron.jestronpriv.wings.WingKind.FEATHER;
import static com.jestron.jestronpriv.wings.WingKind.HOLY;
import static com.jestron.jestronpriv.wings.WingKind.MEMBRANE;

/** 51 animasyonlu kanat. Kimlikler eski elytra gorunumleriyle aynidir (ayar dosyasi otomatik tasinir). */
public final class WingStyles {

    private static WingStyle.B b(String id, String name, String desc, WingKind kind) {
        return new WingStyle.B(id, name, desc, kind);
    }

    public static final List<WingStyle> ALL = List.of(
            b("melek", "Melek Kanadı", "Kat kat bembeyaz tüyler, altın uçlar, yumuşak ışıltı", FEATHER)
                    .glow(0xFFF6D8, 0.35f).pulse(0.5f).particle("end_rod", 0.6f).build(),
            b("kutsal", "Kutsal Melek", "Öldürme efektindeki melek kanadı: gerçek 3D tüyler, kutsal ışık", HOLY)
                    .glow(0xFFF0B0, 1f).particle("wax_off", 2.2f).build(),
            b("su", "Su Kanadı", "Yarı saydam su tüyleri, içinde akan ışık kırılmaları", FEATHER)
                    .translucent(0.85f).glow(0x8FE4FF, 0.45f).scroll(0.22f).particle("dripping_water", 1.2f).build(),
            b("ates", "Ateş Kanadı", "Kökten uca kor, alev ve sarı ateş; titreyen alev ışığı", FEATHER)
                    .glow(0xFF7A18, 0.9f).flicker(0.35f).particle("flame", 2.2f).build(),
            b("buz", "Buz Kanadı", "Süzülen keskin buz kristalleri, soğuk ışıltı", CRYSTAL)
                    .translucent(1f).glow(0x9FE0FF, 0.55f).pulse(0.4f).particle("snowflake", 1.2f).build(),
            b("seytan", "Şeytan Kanadı", "Kızıl zar, kara kemikler, kor ışıltılı kenar", MEMBRANE)
                    .glow(0xFF5A10, 0.75f).flicker(0.2f).particle("small_flame", 1.2f).build(),
            b("ejderha", "Ejderha Kanadı", "Mor pullu ejderha zarı, pençeli kemikler", MEMBRANE)
                    .glow(0xC070FF, 0.45f).pulse(0.35f).size(1.08f).particle("dragon_breath", 0.8f).build(),
            b("anka", "Anka Kuşu", "Kor kırmızısından altına yanan tüyler", FEATHER)
                    .glow(0xFF8A20, 1.0f).pulse(0.7f).flicker(0.25f).size(1.05f).particle("flame", 2.6f).build(),
            b("galaksi", "Galaksi", "Kara tüylerin içinde akan bulutsu ve yıldızlar", ENERGY)
                    .glow(0xB070FF, 0.35f).scroll(0.10f).particle("end_rod", 0.8f).build(),
            b("aurora", "Kuzey Işıkları", "Tüylerde dalgalanan yeşil-mor aurora perdeleri", ENERGY)
                    .glow(0x40FFB0, 0.3f).scroll(0.16f).particle("glow", 0.8f).build(),
            b("gokkusagi", "Gökkuşağı", "Kanat boyunca akan canlı gökkuşağı renkleri", FEATHER)
                    .rainbow().glow(0xFFFFFF, 0.22f).particle("rainbow", 1.4f).build(),
            b("altin", "Altın Kanat", "Parıldayan altın tüyler", FEATHER)
                    .glow(0xFFD25A, 0.38f).pulse(0.6f).particle("wax_on", 1.0f).build(),
            b("elmas", "Elmas", "Pırıl pırıl elmas kristalleri", CRYSTAL)
                    .translucent(1f).glow(0x7FFFF2, 0.6f).pulse(0.5f).particle("wax_off", 1.0f).build(),
            b("zumrut", "Zümrüt", "Derin yeşil zümrüt kristalleri", CRYSTAL)
                    .translucent(1f).glow(0x4AFF8A, 0.55f).pulse(0.45f).particle("happy_villager", 0.9f).build(),
            b("lav", "Lav", "Kara kaya zarında nabız gibi atan lav çatlakları", MEMBRANE)
                    .glow(0xFF7A1A, 1.0f).pulse(0.8f).particle("falling_lava", 1.0f).build(),
            b("bosluk", "Boşluk", "Sonsuz karanlık zar, mor parıltılı damarlar", MEMBRANE)
                    .glow(0xA050FF, 0.85f).pulse(0.45f).particle("reverse_portal", 1.8f).build(),
            b("kelebek_mavi", "Mavi Kelebek", "Morpho kelebeğinin parlayan mavisi", BUTTERFLY)
                    .glow(0x3A8AFF, 0.22f).pulse(1.0f).particle("dust:5AB8FF", 0.8f).build(),
            b("kelebek_monark", "Monark Kelebek", "Turuncu kanat, kara damarlar, beyaz noktalar", BUTTERFLY)
                    .build(),
            b("sakura", "Sakura", "Kiraz çiçeği kanatlar, uçuşan yapraklar", BUTTERFLY)
                    .particle("cherry_leaves", 1.4f).build(),
            b("neon", "Neon", "Karanlıkta parlayan neon tüy hatları", ENERGY)
                    .glow(0x30F0FF, 1.0f).scroll(0.3f).particle("electric_spark", 0.8f).build(),
            b("matrix", "Matrix", "Tüylerden aşağı akan yeşil kod", ENERGY)
                    .glow(0x30FF6A, 0.3f).scroll(-0.35f).particle("dust:2DFF6A", 1.0f).build(),
            b("kamuflaj", "Kamuflaj", "Orman kamuflajı desenli tüyler", FEATHER).build(),
            b("zebra", "Zebra", "Siyah beyaz dalgalı çizgili tüyler", FEATHER).build(),
            b("leopar", "Leopar", "Altın tüy üstünde rozet benekler", FEATHER).build(),
            b("bal", "Bal Peteği", "Altın bal peteği kanatlar", BUTTERFLY)
                    .particle("falling_honey", 0.7f).build(),
            b("vitray", "Vitray", "Kurşun çerçeveli, ışık geçiren renkli camlar", BUTTERFLY)
                    .translucent(0.92f).glow(0xFFFFFF, 0.3f).build(),
            b("sonbahar", "Sonbahar", "Kızıl, turuncu ve sarı yaprak tüyler", FEATHER)
                    .tint(0xFFD8B0, 0xFFFFFF, 0xFFFFFF).particle("dust:E0701A", 0.9f).build(),
            b("ender", "Ender", "Koyu turkuaz zar, parlayan ender gözleri", MEMBRANE)
                    .glow(0x40FFC8, 0.8f).pulse(0.55f).particle("portal", 1.6f).build(),
            b("nether", "Nether", "Kızıl nether zarı, çarpık damarlar", MEMBRANE)
                    .glow(0xFF8A30, 0.6f).pulse(0.3f).particle("crimson_spore", 1.6f).build(),
            b("slime", "Balçık", "Yarı saydam yeşil balçık zar", MEMBRANE)
                    .translucent(1f).glow(0x9AFF7A, 0.2f).particle("item_slime", 1.0f).build(),
            b("ametist", "Ametist", "Mor ametist kristalleri", CRYSTAL)
                    .translucent(1f).glow(0xC58AFF, 0.6f).pulse(0.5f).particle("witch", 0.8f).build(),
            b("gul", "Kızıl Gül", "Kadife kırmızı gül yaprağı tüyler", FEATHER)
                    .glow(0xFF3050, 0.15f).particle("dust:C0102A", 0.9f).build(),
            b("gece", "Yıldızlı Gece", "Lacivert tüylerde göz kırpan yıldızlar", FEATHER)
                    .glow(0xFFFFFF, 1.0f).pulse(1.4f).particle("end_rod", 0.5f).build(),
            b("gunbatimi", "Gün Batımı", "Turuncudan pembeye, mora gün batımı", FEATHER)
                    .tint(0xFFA040, 0xF06A9A, 0x8A4AD0).glow(0xFFB070, 0.25f).build(),
            b("okyanus", "Okyanus", "Derin mavi tüyler, köpüklü uçlar", FEATHER)
                    .glow(0x8FE8FF, 0.3f).particle("falling_water", 0.9f).build(),
            b("toksik", "Toksik", "Kara zarda parlayan zehirli yeşil damarlar", MEMBRANE)
                    .glow(0x7AFF2A, 0.9f).pulse(1.1f).particle("dust:7AFF2A", 1.2f).build(),
            b("simsek", "Şimşek", "Fırtına tüyleri, çakan şimşek hatları", FEATHER)
                    .glow(0xFFF26A, 1.0f).flicker(0.9f).particle("electric_spark", 1.8f).build(),
            b("kar", "Kar Tanesi", "Buz mavisi tüylerde kar tanesi desenleri", FEATHER)
                    .glow(0xCFEAFF, 0.3f).particle("snowflake", 1.2f).build(),
            b("kraliyet", "Kraliyet", "Mor kadife tüyler, altın işleme", FEATHER)
                    .glow(0xFFD257, 0.5f).pulse(0.5f).size(1.05f).particle("wax_on", 0.8f).build(),
            b("kristal_kalp", "Kalpler", "Pembe tüylerde kırmızı kalpler", FEATHER)
                    .glow(0xFF6A90, 0.3f).particle("heart", 0.3f).build(),
            b("kutup", "Kutup Tilkisi", "Yumuşak beyaz kürk tüyler, gri uçlar", FEATHER)
                    .glow(0xFFFFFF, 0.15f).particle("snowflake", 0.6f).build(),
            b("seraf", "Serafim", "Altı kanatlı efsanevi melek: üç çift altın uçlu kanat", FEATHER)
                    .pairs(3).glow(0xFFF0B0, 0.6f).pulse(0.45f).size(0.95f).particle("end_rod", 1.2f).build(),
            b("kara_melek", "Düşmüş Melek", "Kapkara tüyler, kızıl parlayan kenarlar, savrulan kül", FEATHER)
                    .glow(0xFF1A3A, 0.6f).pulse(0.35f).size(1.05f).particle("ash", 3.0f).build(),
            b("tavus", "Tavus Kuşu", "Göz desenli zümrüt-turkuaz tavus tüyleri", FEATHER)
                    .glow(0xFFD060, 0.3f).pulse(0.6f).size(1.05f).particle("dust:1AA2C8", 0.7f).build(),
            b("buz_anka", "Buz Ankası", "Mavi kökten bembeyaz uca buz tüyleri, soğuk ışıltı", FEATHER)
                    .glow(0x8FE0FF, 0.7f).pulse(0.5f).size(1.05f).particle("snowflake", 2.0f).build(),
            b("ruh", "Ruh Kanadı", "Gece mavisi tüylerde yükselen ruh alevleri", ENERGY)
                    .glow(0x40D0FF, 0.35f).scroll(0.25f).particle("soul_fire_flame", 1.4f).build(),
            b("plazma", "Plazma", "Tüylerde çakan mor-mavi plazma arkları", ENERGY)
                    .glow(0xE060FF, 0.35f).scroll(0.45f).flicker(0.3f).particle("electric_spark", 1.2f).build(),
            b("mekanik", "Mekanik", "Süzülen çelik kanat plakaları, parlayan turuncu çekirdek", CRYSTAL)
                    .glow(0xFF9A30, 0.7f).pulse(1.2f).particle("smoke", 0.8f).build(),
            b("kan_ejderi", "Kan Ejderi", "Kara zarda kalp gibi atan kızıl damarlar", MEMBRANE)
                    .glow(0xFF2030, 0.9f).pulse(0.9f).size(1.1f).particle("dust:8A0010", 1.2f).build(),
            b("kozmik_ejder", "Kozmik Ejder", "Bulutsu ve yıldızlarla dolu uzay zarı", MEMBRANE)
                    .glow(0xB070FF, 0.8f).pulse(0.4f).size(1.12f).particle("end_rod", 1.0f).build(),
            b("luna", "Ay Kelebeği", "Göz benekli soluk yeşil gece kelebeği", BUTTERFLY)
                    .glow(0xC8FFB8, 0.18f).pulse(0.6f).particle("dust:B8F0B0", 0.6f).build());

    private WingStyles() {
    }

    public static WingStyle get(String id) {
        if (id == null) return null;
        for (WingStyle s : ALL) if (s.id().equals(id)) return s;
        return null;
    }
}
