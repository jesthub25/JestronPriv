package com.jestron.jestronpriv.pet;

import java.util.Locale;

/** Yoldas turleri (50). Renkler varsayilandir; ayarlardan ozel renk ya da gokkusagi secilebilir. */
public enum PetKind {
    RUH("Ruh Işığı", "Süzülen parlak ruh: ışık çekirdeği, kuyruk ve etrafında dönen kıvılcımlar.", 0x5FE6FF, 0xC8FAFF, "soul_fire_flame"),
    KRISTAL("Kristal", "Kendi ekseninde dönen ışıklı kristal ve yörüngesinde küçük parçalar.", 0xB36BFF, 0xFFB0FF, "end_rod"),
    EJDERHA("Mini Ejderha", "Kanat çırpan, kuyruğunu sallayan minik ejderha; gözleri parlar.", 0x7A2BD6, 0xFFB347, "dragon_breath"),
    PERI("Peri", "Dört kelebek kanadıyla uçan parlak peri, arkasında ışıltı bırakır.", 0xFF6FC8, 0xFFF0A0, "wax_on"),
    GOZ("Göz", "Baktığın yere bakan, ara sıra göz kırpan süzülen göz.", 0x3DF07A, 0xFFFFFF, "glow"),
    DRONE("Drone", "Pervaneleri dönen, ışıkları yanıp sönen mini drone.", 0x3D9DFF, 0xFF3B3B, "electric_spark"),
    YILDIZ("Yıldız", "Dönen parlak yıldız, ışık halesi.", 0xFFD54F, 0xFFFFFF, "firework"),
    KALP("Kalp", "Atan kalp: iki vuruşlu nabız, pembe hale.", 0xFF3B5C, 0xFFB0C8, "heart"),
    KEDI("Mini Kedi", "Kulakları oynayan, kuyruğunu sallayan, göz kırpan minik kedi.", 0xF2A65A, 0xFFF1E0, "heart"),
    KOPEK("Mini Köpek", "Sarkık kulaklı, kuyruk sallayan neşeli köpek.", 0xC8894A, 0xF5E6D0, "happy_villager"),
    TAVSAN("Tavşan", "Uzun kulakları sallanan, burnunu oynatan beyaz tavşan.", 0xF4F4F8, 0xFFB6C8, "cherry_leaves"),
    PENGUEN("Penguen", "Paytak penguen: yüzgeçlerini çırpar, turuncu gagası parlar.", 0x22262E, 0xFFFFFF, "snowflake"),
    BAYKUS("Baykuş", "Kocaman parlak gözlü, kanat çırpan baykuş.", 0x8A6440, 0xF2D8A8, "end_rod"),
    ARI("Arı", "Çizgili gövde, vızıldayan şeffaf kanatlar.", 0xFFC81E, 0x1E1A14, "falling_nectar"),
    KELEBEK("Kelebek", "Desenli dört kanatlı kelebek, yavaş ve zarif çırpar.", 0x3DA8FF, 0xFFD54F, "wax_on"),
    BALIK("Japon Balığı", "Havada yüzen, kuyruğunu dalgalandıran japon balığı.", 0xFF7A2E, 0xFFE0C0, "splash"),
    AHTAPOT("Ahtapot", "Sekiz dalgalanan kollu sevimli ahtapot.", 0xC85AD8, 0xFFB0F0, "squid_ink"),
    DENIZANASI("Denizanası", "Saydam kubbe, parlayan dokunaçlar; nabız gibi kasılır.", 0x7AD8FF, 0xE0B0FF, "glow"),
    YARASA("Yarasa", "Zar kanatlarını çırpan, kırmızı gözlü yarasa.", 0x2E2836, 0xFF4040, "smoke"),
    KUS("Mavi Kuş", "Kanat çırpan, başını oynatan küçük kuş.", 0x3D8BFF, 0xFFD23F, "cloud"),
    TILKI("Tilki", "Kabarık kuyruklu, sivri kulaklı turuncu tilki.", 0xF07A2A, 0xFFFFFF, "falling_spore_blossom"),
    PANDA("Panda", "Siyah-beyaz tombul panda, kulaklarını oynatır.", 0xF6F6F6, 0x1C1C1C, "happy_villager"),
    BALCIK("Balçık", "Zıplayarak esneyen saydam balçık, içinde parlak çekirdek.", 0x6AE05A, 0xB8FF9A, "item_slime"),
    HAYALET("Hayalet", "Dalgalı eteğiyle süzülen sevimli hayalet.", 0xF0F4FF, 0x6080FF, "soul"),
    KAFATASI("Uçan Kafatası", "Çenesi takırdayan, gözleri yanan kafatası.", 0xE8E4D8, 0x40FFD0, "soul_fire_flame"),
    BALKABAGI("Balkabağı Feneri", "İçi yanan oyma yüzlü balkabağı.", 0xF08A1E, 0xFFE070, "flame"),
    KARDAN_ADAM("Kardan Adam", "Havuç burunlu, şapkalı minik kardan adam.", 0xFFFFFF, 0xFF8A2A, "snowflake"),
    ROBOT("Mini Robot", "Anteni yanıp sönen, ekran yüzlü robot.", 0x9AA4B4, 0x3DF5FF, "electric_spark"),
    UFO("UFO", "Dönen ışıklı uçan daire ve aşağı bakan ışın.", 0xA8B0C0, 0x7CFF6A, "end_rod"),
    ROKET("Roket", "Kanatçıklı roket, arkasından alev püskürür.", 0xF0F0F4, 0xFF4A2A, "small_flame"),
    GEZEGEN("Mini Gezegen", "Halkalı gezegen ve etrafında dönen uydu.", 0xE8B060, 0xC0A0FF, "end_rod"),
    GUNES("Mini Güneş", "Dönen ışın taçlı parlak güneş.", 0xFFC830, 0xFFF4A0, "flame"),
    AY("Hilal Ay", "Sallanan hilal ve yanında göz kırpan yıldız.", 0xE8ECFF, 0xFFE87A, "end_rod"),
    BULUT("Yağmur Bulutu", "Kabarık bulut, altından yağmur damlar.", 0xF4F6FF, 0x7AB8FF, "falling_water"),
    FIRTINA("Fırtına Bulutu", "Kara bulut, içinde şimşek çakar.", 0x50566A, 0xFFF07A, "electric_spark"),
    ATES_TOPU("Ateş Topu", "Dönen alev dilleriyle yanan ateş topu.", 0xFF6A1E, 0xFFE070, "flame"),
    KAR_TANESI("Kar Tanesi", "Altı kollu buz kristali, yavaşça döner.", 0xBFEFFF, 0xFFFFFF, "snowflake"),
    ELMAS("Elmas", "Kesme elmas, ışığı kırarak döner.", 0x6EF3FF, 0xFFFFFF, "end_rod"),
    PARA("Altın Para", "Kendi ekseninde dönen parlak altın para.", 0xFFC830, 0xFFF0A0, "wax_on"),
    ENERJI_KUPU("Enerji Küpü", "İç içe ters dönen iki küp, parlak çekirdek.", 0x5A3AFF, 0x3DF5FF, "enchant"),
    PRIZMA("Işık Prizması", "Dönen üçgen prizma, gökkuşağı yansıması.", 0xE0F4FF, 0xFF7AE0, "end_rod"),
    KUM_SAATI("Kum Saati", "Ara sıra ters dönen, kumu akan kum saati.", 0xC8A060, 0xFFE0A0, "white_ash"),
    KITAP("Büyü Kitabı", "Sayfaları dalgalanan, rünleri parlayan kitap.", 0x6A2A1E, 0x7AFFE0, "enchant"),
    KILIC("Uçan Kılıç", "Yavaşça dönen parlak ağızlı kılıç.", 0x6EF3FF, 0x8A5A2A, "crit"),
    FENER("Fener", "Sallanan zincirli fener, içinde alev.", 0x3A3A44, 0xFFB040, "small_flame"),
    MANTAR("Mantarcık", "Benekli şapkalı, gözlü sevimli mantar.", 0xE83A3A, 0xFFF4E0, "mycelium"),
    CICEK("Çiçek", "Taç yaprakları dönen, ortası parlayan çiçek.", 0xFF6FB0, 0xFFE040, "cherry_leaves"),
    ORUMCEK("Mini Örümcek", "Sekiz bacağını oynatan, kırmızı gözlü örümcek.", 0x2A2430, 0xFF3030, "ash"),
    EJDER_YUMURTASI("Ejder Yumurtası", "Çatlaklarından mor ışık sızan yumurta.", 0x1E1428, 0xC040FF, "portal"),
    KUZU("Kuzu", "Pamuk yünlü, kulakları sallanan kuzucuk.", 0xF6F4EE, 0x3A3030, "cloud");

    public final String label, description, particle;
    public final int primary, secondary;

    PetKind(String label, String description, int primary, int secondary, String particle) {
        this.label = label;
        this.description = description;
        this.primary = primary;
        this.secondary = secondary;
        this.particle = particle;
    }

    /** "yok" ya da bilinmeyen -> null. */
    public static PetKind parse(String s) {
        if (s == null) return null;
        String n = s.trim().toUpperCase(Locale.ROOT);
        for (PetKind k : values()) if (k.name().equals(n)) return k;
        return null;
    }
}
