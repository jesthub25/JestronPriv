package com.jestron.jestronpriv.cape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jestron.jestronpriv.cape.CapeArt.*;

/**
 * 50 ozgun elytra gorunumu. Hepsi prosedurel: kanadin dis yuzu calisma aninda 40x80 piksel (vanilla elytranin 4 kati
 * cozunurluk) uretilir, dosya yok. Animasyonlu olanlar saniyede 20 kare yenilenir. Bazilarinin alt kenari tuy / zar
 * ucu gibi tirtiklidir (seffaf kesik). Hicbiri resmi ya da baska bir modun elytrasinin kopyasi degildir.
 * <p>
 * Koordinatlar (dis yuz): u = 0 omuz (kanat mentesesi), u = 1 diger kenar; v = 0 ust, v = 1 kanadin alt ucu.
 */
public final class ElytraDesigns {

    public static final List<CapeDesign> ALL;
    private static final Map<String, CapeDesign> BY_ID = new HashMap<>();

    private ElytraDesigns() {
    }

    public static CapeDesign get(String id) {
        return id == null ? null : BY_ID.get(id);
    }

    public static int indexOf(String id) {
        for (int i = 0; i < ALL.size(); i++) if (ALL.get(i).id().equals(id)) return i;
        return -1;
    }

    private static void d(List<CapeDesign> l, String id, String name, String desc, int c1, int c2, int c3, boolean anim, String particle, float rate,
                          CapeDesign.Painter p) {
        l.add(new CapeDesign(id, name, desc, c1, c2, c3, anim, particle, rate, p));
    }

    static {
        List<CapeDesign> l = new ArrayList<>();
        d(l, "anka", "Anka Kuşu", "Alevden tüyler, uçlarda kor", 0x3A0800, 0xFF5A00, 0xFFE27A, true, "flame", 1.4f, ElytraDesigns::phoenix);
        d(l, "melek", "Melek", "Beyaz tüyler, altın kenar, hale ışığı", 0xF4F1EA, 0xD8D2C4, 0xFFD54F, true, "end_rod", 0.4f, ElytraDesigns::angel);
        d(l, "ejder_zari", "Ejder Zarı", "Kemik çubuklar arasında kızıl zar", 0x2A0505, 0x9E1B1B, 0xE8D3A8, true, null, 0f, ElytraDesigns::dragonMembrane);
        d(l, "yarasa", "Gece Yarasası", "Mor-siyah zar, tırtıklı kenar", 0x0D0814, 0x3B2152, 0x9C7BC2, false, null, 0f, ElytraDesigns::bat);
        d(l, "monark", "Monark Kelebeği", "Turuncu kanat, siyah damarlar, beyaz benek", 0xFF8A00, 0x141010, 0xFFFFFF, false, null, 0f, ElytraDesigns::monarch);
        d(l, "tavus", "Tavus Kuşu", "Parlayan göz benekleri, yeşil-mavi tüy", 0x0A3B2E, 0x14A38B, 0x2E5BFF, true, null, 0f, ElytraDesigns::peacock);
        d(l, "buz_kanat", "Buz Kanat", "Kristal parçalar, soğuk ışıltı", 0x0C3150, 0x7FD8FF, 0xFFFFFF, true, "snowflake", 1.0f, ElytraDesigns::iceShard);
        d(l, "nebula", "Nebula", "Dönen bulutsu, göz kırpan yıldızlar", 0x0A0420, 0x8E3BD8, 0x3FD0FF, true, "portal", 1.0f, ElytraDesigns::nebula);
        d(l, "aurora_kanat", "Kutup Perdesi", "Kanat boyunca akan yeşil-mor ışık", 0x030A16, 0x22FF9C, 0xC04BFF, true, null, 0f, ElytraDesigns::aurora);
        d(l, "siber", "Siber Neon", "Karanlık zırh, akan neon hatlar", 0x0A0A12, 0xFF2BD6, 0x2BE7FF, true, "electric_spark", 0.5f, ElytraDesigns::cyber);
        d(l, "magma", "Magma", "Kara kaya, nabız gibi atan çatlaklar", 0x140C0A, 0xFF4500, 0xFFD15C, true, "lava", 0.3f, ElytraDesigns::magma);
        d(l, "yildirim", "Yıldırım", "Kanat boyunca çakan şimşekler", 0x10141F, 0x5B6B9A, 0xD9F2FF, true, "electric_spark", 0.9f, ElytraDesigns::lightning);
        d(l, "dalga", "Okyanus Dalgası", "Derin mavi, köpüklü dalgalar", 0x022A48, 0x1287C4, 0xCFF6FF, true, "dripping_water", 0.6f, ElytraDesigns::wave);
        d(l, "selale", "Şelale", "Yukarıdan akan berrak su", 0x05324F, 0x3CB4E6, 0xFFFFFF, true, "falling_water", 0.8f, ElytraDesigns::waterfall);
        d(l, "prizma", "Prizma", "Kırılan ışık, kayan gökkuşağı", 0xEDEDF7, 0x8AC7FF, 0xFFFFFF, true, "dust:FF8AE6", 0.5f, ElytraDesigns::prism);
        d(l, "vitray", "Vitray", "Kurşun çizgili renkli cam", 0x1A1A1A, 0x2C6BD6, 0xFFD54F, true, null, 0f, ElytraDesigns::stainedGlass);
        d(l, "altin_zirh", "Altın Zırh", "Üst üste altın plakalar, kayan parlama", 0x7A5A00, 0xFFCC33, 0xFFF6C8, true, null, 0f, ElytraDesigns::goldPlates);
        d(l, "sovalye", "Şövalye", "Perçinli çelik plakalar", 0x3C454D, 0xAEB9C2, 0xFFFFFF, true, null, 0f, ElytraDesigns::steelPlates);
        d(l, "obsidyen_kanat", "Obsidyen", "Cam gibi kara taş, mor çizgiler", 0x06040B, 0x2B1748, 0xBA80FF, true, "reverse_portal", 0.6f, ElytraDesigns::obsidian);
        d(l, "bosluk_yirtigi", "Boşluk Yırtığı", "Kanattan açılan mor yarıklar", 0x000000, 0x3A0A66, 0xE070FF, true, "portal", 1.6f, ElytraDesigns::voidRift);
        d(l, "ruh_kanat", "Ruh Alevi", "Turkuaz ruh ateşi tüyler", 0x00161A, 0x00B8C8, 0xC2FFFF, true, "soul_fire_flame", 1.3f, ElytraDesigns::soulFlame);
        d(l, "sarmasik", "Zehirli Sarmaşık", "Koyu yeşil, parlayan damarlı sarmaşık", 0x08180A, 0x2E8B3A, 0xB6FF3B, true, "dust:7CFF3B", 0.6f, ElytraDesigns::vines);
        d(l, "kiraz_kanat", "Kiraz Bahçesi", "Pembe çiçekler, süzülen yapraklar", 0xFFE6EE, 0xFF8FB4, 0x7A4A3A, true, "cherry_leaves", 0.9f, ElytraDesigns::cherry);
        d(l, "yaprak", "Sonbahar Yaprağı", "Damarlı yaprak, tırtıklı kenar", 0x5A2408, 0xE8650E, 0xFFC23B, false, null, 0f, ElytraDesigns::leaf);
        d(l, "yusufcuk", "Yusufçuk", "İnce damarlı yanardöner zar", 0x10202A, 0x4FD1C5, 0xD6FFF6, true, null, 0f, ElytraDesigns::dragonfly);
        d(l, "petek", "Bal Peteği", "Altıgen petek, damlayan bal", 0x5A3500, 0xFFB000, 0xFFE9A0, true, "dripping_honey", 0.4f, ElytraDesigns::honeycomb);
        d(l, "kara_melek", "Kara Melek", "Siyah tüyler, kızıl ışıltı", 0x0A0A0C, 0x26262C, 0xFF2A2A, true, "dust:FF2020", 0.5f, ElytraDesigns::darkAngel);
        d(l, "seytan", "Cehennem", "Kızıl zar, kara kemik, kor", 0x220000, 0xB00A0A, 0x1A0A0A, true, "small_flame", 0.8f, ElytraDesigns::infernal);
        d(l, "kar_firtinasi", "Kar Fırtınası", "Savrulan kar, buzlu mavi", 0x29507A, 0x9FC9F0, 0xFFFFFF, true, "snowflake", 1.2f, ElytraDesigns::blizzard);
        d(l, "gunes_patlamasi", "Güneş Patlaması", "Omuzdan fışkıran altın ışınlar", 0xB34700, 0xFFB300, 0xFFFDE0, true, "end_rod", 0.5f, ElytraDesigns::solarFlare);
        d(l, "tutulma", "Tutulma", "Karanlık disk, parlayan taç", 0x020208, 0x1A1A3A, 0xFFE9B0, true, null, 0f, ElytraDesigns::eclipse);
        d(l, "takimyildiz", "Takımyıldız", "Çizgilerle bağlanan yıldızlar", 0x050B22, 0x1B2D66, 0xFFFFFF, true, null, 0f, ElytraDesigns::constellation);
        d(l, "hologram", "Hologram", "Tarama çizgili titrek mavi ışık", 0x001018, 0x00C8FF, 0xCFFBFF, true, null, 0f, ElytraDesigns::hologram);
        d(l, "matris", "Matris", "Yağan yeşil karakterler", 0x000A02, 0x00D24A, 0xD8FFD8, true, null, 0f, ElytraDesigns::matrix);
        d(l, "retro", "Retro Gün Batımı", "Dilimli güneş, neon ızgara", 0x12002A, 0xFF3FA4, 0xFFD23F, true, null, 0f, ElytraDesigns::synth);
        d(l, "kan", "Kan Damlası", "Koyu kızıl, aşağı süzülen damlalar", 0x2A0003, 0x9A0010, 0xFF4A55, true, "dust:B00010", 0.4f, ElytraDesigns::blood);
        d(l, "mantar", "Mantar", "Kırmızı şapka, beyaz benekler", 0xB3121B, 0xE53935, 0xFFF8E1, false, null, 0f, ElytraDesigns::mushroom);
        d(l, "geode", "Ametist Jeodu", "Dışı taş, içi parlayan kristal", 0x3A2D3F, 0x8E44FF, 0xF0D6FF, true, null, 0f, ElytraDesigns::geode);
        d(l, "zumrut_run", "Zümrüt Rün", "Yeşil taş, nabız gibi atan rünler", 0x06261A, 0x0FA060, 0x8AFFC8, true, "happy_villager", 0.3f, ElytraDesigns::runes);
        d(l, "antik_tas", "Antik Taş", "Yosunlu taş, sönük oymalar", 0x3D3F3A, 0x6B6E64, 0x9BD18B, false, null, 0f, ElytraDesigns::ancientStone);
        d(l, "kum_firtinasi", "Kum Fırtınası", "Akan kum tepeleri", 0x8A5A20, 0xE8C27A, 0xFFF0C8, true, null, 0f, ElytraDesigns::sand);
        d(l, "seker", "Şeker Kamışı", "Kıvrılan kırmızı-beyaz şeritler", 0xFFFFFF, 0xE0182D, 0x7CD992, true, null, 0f, ElytraDesigns::candy);
        d(l, "atesbocegi", "Ateşböceği", "Gece ormanı, uçuşan ışıklar", 0x06140A, 0x173A1E, 0xFFF27A, true, null, 0f, ElytraDesigns::fireflies);
        d(l, "plazma", "Plazma", "Kıvrılan pembe-mavi enerji", 0x14002A, 0xFF2BAA, 0x2BC4FF, true, "dust:FF55CC", 0.6f, ElytraDesigns::plasma);
        d(l, "mermer", "Altın Mermer", "Beyaz mermer, altın damarlar", 0xEFEDE8, 0xC9C5BC, 0xD9A82B, false, null, 0f, ElytraDesigns::marble);
        d(l, "dijital_kamuflaj", "Dijital Kamuflaj", "Piksel lekeli orman deseni", 0x2E3B22, 0x5C6B3A, 0x9A9A6A, false, null, 0f, ElytraDesigns::digiCamo);
        d(l, "kartal", "Kartal", "Kahverengi tüyler, beyaz uçlar", 0x3A220F, 0x7A4A22, 0xF2E6D0, false, null, 0f, ElytraDesigns::eagle);
        d(l, "baykus", "Kar Baykuşu", "Beyaz tüy, siyah benekler", 0xF5F5F2, 0xD8D8D2, 0x1E1E1E, false, null, 0f, ElytraDesigns::snowOwl);
        d(l, "ejder_atesi", "Ejder Ateşi", "Kara pullar arasından kızgın ışık", 0x0E0A0A, 0x2A1A18, 0xFF6A00, true, "small_flame", 0.7f, ElytraDesigns::emberScales);
        d(l, "kozmik_kraliyet", "Kozmik Kraliyet", "Kadife mor, altın yıldızlı kenar", 0x2A0845, 0x5E1B8C, 0xFFC94A, true, "end_rod", 0.3f, ElytraDesigns::cosmicRoyal);
        ALL = Collections.unmodifiableList(l);
        for (CapeDesign x : ALL) BY_ID.put(x.id(), x);
    }

    // ------------------------------------------------------------------ yardimcilar

    /** Alt kenarda tuy / zar ucu tirtigi: true ise piksel kesik. */
    static boolean scallop(Px p, float cols, float depth) {
        float f = fract(p.u * cols) * 2f - 1f;
        return p.v > 1f - depth * (1f - f * f);
    }

    /** Sivri uclu tirtik (yaprak / ejderha zari). */
    static boolean spikes(Px p, float cols, float depth) {
        float f = Math.abs(fract(p.u * cols) * 2f - 1f);
        return p.v > 1f - depth * f;
    }

    /**
     * Ust uste binen tuyler: 0..1 golge (1 = tuyun ortasi, kucuk = alttaki tuyun golgesi). rachis: tuy sapi.
     *
     * @return {golge, sap(0..1), sira, sutun-kimligi, fu(-1..1), fv(0..1)} - tek dizi tahsis etmemek icin Px alanlari
     */
    static float feather(Px p, float cols, float rows) {
        float vv = p.v * rows;
        int row = (int) Math.floor(vv);
        float fv = vv - row;
        float uu = p.u * cols + ((row & 1) == 1 ? 0.5f : 0f);
        float fu = fract(uu) * 2f - 1f;
        // yuvarlak alt uclu tuy; disarisi bir ust siranin altinda kalan golge
        float edge = 1f - 0.55f * fu * fu - fv;
        if (edge < 0f) return 0.42f + 0.1f * fv;
        float body = 0.72f + 0.28f * (1f - Math.abs(fu)) - fv * 0.18f;
        // ince capraz tuy telcikleri
        float barb = 0.94f + 0.06f * sin((fv * 9f + Math.abs(fu) * 7f) * 3.1f);
        return clamp01(body * barb + smooth(0.06f, 0f, edge) * 0.1f);
    }

    static float featherRachis(Px p, float cols, float rows) {
        float vv = p.v * rows;
        int row = (int) Math.floor(vv);
        float fv = vv - row;
        float uu = p.u * cols + ((row & 1) == 1 ? 0.5f : 0f);
        float fu = fract(uu) * 2f - 1f;
        if (1f - 0.55f * fu * fu - fv < 0f) return 0f;
        return smooth(0.14f, 0.02f, Math.abs(fu)) * (1f - fv * 0.7f);
    }

    static int featherId(Px p, float cols, float rows) {
        float vv = p.v * rows;
        int row = (int) Math.floor(vv);
        float uu = p.u * cols + ((row & 1) == 1 ? 0.5f : 0f);
        return hash((int) Math.floor(uu), row);
    }

    /** Tam sayi cizgilerine yakinlik (izgara / damar): 0..1, w genislik (birim). */
    static float gridLine(float v, float w) {
        float f = fract(v);
        return smooth(w, 0f, Math.min(f, 1f - f));
    }

    /** Kanat boyunca omuzdan cikan kemik / damar: u ile v'yi baglayan isinlar. 0..1 yakinlik. */
    static float ribs(Px p, int n, float width) {
        // omuz (0,0) noktasindan alt kenara yayilan isinlar
        float ang = (float) Math.atan2(p.v, Math.max(0.001f, p.u * 0.5f));
        float k = ang / ((float) Math.PI / 2f) * n;
        float d = Math.abs(fract(k) - 0.5f) * 2f;
        float r = (float) Math.sqrt(p.u * p.u * 0.25f + p.v * p.v);
        return smooth(width, 0f, 1f - d) * smooth(0.02f, 0.12f, r);
    }

    // ------------------------------------------------------------------ tuylu

    private static void phoenix(Px p) {
        float sh = feather(p, 3.2f, 7f);
        float heat = clamp01(p.v * 1.1f + 0.15f * fbm(p.u * 4f, p.v * 6f - p.t * 1.5f, 3));
        int base = ramp(p.c1, p.c2, p.c3, clamp01(1f - heat * 0.9f + sh * 0.25f));
        float flick = fbm(p.u * 6f, p.v * 10f + p.t * 2.2f, p.t * 0.7f, 3);
        float tip = smooth(0.55f, 1f, p.v) * smooth(0.35f, 0.75f, flick);
        int c = mul(base, 0.55f + sh * 0.55f);
        c = screen(c, p.c3, tip * 0.8f + featherRachis(p, 3.2f, 7f) * 0.35f);
        p.rgb = c;
        p.glow = clamp01(tip + heat * 0.25f);
        if (scallop(p, 3.2f, 0.06f + 0.02f * sin(p.t * 3f + p.u * 9f))) p.alpha = 0f;
    }

    private static void angel(Px p) {
        float sh = feather(p, 3.5f, 6.5f);
        int c = mul(mix(p.c1, p.c2, (1f - sh) * 0.8f), 0.7f + sh * 0.35f);
        c = mix(c, 0xFFFFFF, featherRachis(p, 3.5f, 6.5f) * 0.5f);
        // altin kenar: omuz tarafi ve ust kenar
        float rim = Math.max(smooth(0.08f, 0f, p.u), smooth(0.05f, 0f, p.v));
        c = mix(c, p.c3, rim * 0.85f);
        float halo = 0.5f + 0.5f * sin(p.t * 1.6f - p.v * 6f);
        float shimmer = smooth(0.1f, 0f, Math.abs(fract(p.v * 1.2f - p.t * 0.25f) - 0.5f)) * sh;
        p.rgb = screen(c, 0xFFF6D0, shimmer * 0.5f);
        p.glow = clamp01(rim * 0.6f * halo + shimmer * 0.5f);
        if (scallop(p, 3.5f, 0.07f)) p.alpha = 0f;
    }

    private static void darkAngel(Px p) {
        float sh = feather(p, 3.5f, 6.5f);
        int c = mul(mix(p.c1, p.c2, sh), 0.6f + sh * 0.6f);
        float vein = featherRachis(p, 3.5f, 6.5f);
        float pulse = 0.55f + 0.45f * sin(p.t * 2.4f - p.v * 5f);
        c = mix(c, p.c3, vein * pulse * 0.8f);
        float ember = star(p.x, p.y, 61, 0.02f, p.t, 1f) * smooth(0.4f, 1f, p.v);
        p.rgb = mix(c, 0xFF6A4A, ember);
        p.glow = clamp01(vein * pulse * 0.9f + ember);
        if (scallop(p, 3.5f, 0.08f)) p.alpha = 0f;
    }

    private static void soulFlame(Px p) {
        float sh = feather(p, 3f, 6f);
        float fl = fbm(p.u * 5f, p.v * 8f + p.t * 1.8f, p.t * 0.5f, 3);
        float hot = smooth(0.45f, 0.85f, fl * (0.6f + p.v * 0.6f));
        int c = mix(mul(p.c1, 0.8f + sh * 0.6f), p.c2, sh * 0.5f + hot * 0.5f);
        c = screen(c, p.c3, hot * 0.7f);
        p.rgb = c;
        p.glow = clamp01(hot + sh * 0.15f);
        if (scallop(p, 3f, 0.07f + 0.02f * fl)) p.alpha = 0f;
    }

    private static void eagle(Px p) {
        float sh = feather(p, 3.2f, 6f);
        int id = featherId(p, 3.2f, 6f);
        int c = mix(p.c1, p.c2, 0.3f + 0.5f * rand(id, 3));
        c = mul(c, 0.6f + sh * 0.55f);
        // alt siralar beyaz uclu
        float tipBand = smooth(0.62f, 0.78f, p.v);
        float vv = fract(p.v * 6f);
        c = mix(c, p.c3, tipBand * smooth(0.55f, 0.85f, vv) * 0.9f);
        // enine koyu cizgiler
        c = mul(c, 0.9f + 0.1f * sin(p.v * 60f + rand(id, 5) * 6f));
        p.rgb = mix(c, 0xFFFFFF, featherRachis(p, 3.2f, 6f) * 0.25f);
        if (scallop(p, 3.2f, 0.07f)) p.alpha = 0f;
    }

    private static void snowOwl(Px p) {
        float sh = feather(p, 4f, 7f);
        int id = featherId(p, 4f, 7f);
        int c = mul(mix(p.c1, p.c2, 1f - sh), 0.8f + sh * 0.25f);
        // her tuyun ucunda kucuk siyah lekeler (asagi dogru artar)
        float vv = fract(p.v * 7f), uu = fract(p.u * 4f + ((((int) (p.v * 7f)) & 1) == 1 ? 0.5f : 0f)) - 0.5f;
        float spot = smooth(0.13f, 0.05f, (float) Math.sqrt(uu * uu * 1.4f + (vv - 0.62f) * (vv - 0.62f)));
        float prob = rand(id, 9) < 0.25f + p.v * 0.6f ? 1f : 0f;
        p.rgb = mix(c, p.c3, spot * prob * 0.9f);
        if (scallop(p, 4f, 0.06f)) p.alpha = 0f;
    }

    private static void peacock(Px p) {
        float sh = feather(p, 2.5f, 4.5f);
        float vv = p.v * 4.5f;
        int row = (int) Math.floor(vv);
        float fv = vv - row;
        float fu = fract(p.u * 2.5f + ((row & 1) == 1 ? 0.5f : 0f)) * 2f - 1f;
        float d = (float) Math.sqrt(fu * fu * 0.9f + (fv - 0.55f) * (fv - 0.55f) * 3.2f);
        int c = mix(p.c1, p.c2, sh);
        float pulse = 0.6f + 0.4f * sin(p.t * 1.5f + row);
        // goz: kahve halka, altin, mavi, koyu orta
        if (d < 0.62f) {
            if (d < 0.18f) c = 0x0A1240;
            else if (d < 0.3f) c = p.c3;
            else if (d < 0.42f) c = 0x1FBF8A;
            else if (d < 0.52f) c = 0xD9A62B;
            else c = 0x5A3A16;
        }
        float irid = 0.5f + 0.5f * sin(p.u * 12f + p.v * 9f + p.t);
        c = screen(c, 0x7FFFE0, irid * 0.15f);
        p.rgb = mul(c, 0.7f + sh * 0.4f);
        p.glow = d < 0.3f ? 0.5f * pulse : 0f;
        if (scallop(p, 2.5f, 0.08f)) p.alpha = 0f;
    }

    // ------------------------------------------------------------------ zarli

    private static void dragonMembrane(Px p) {
        float rib = ribs(p, 4, 0.12f);
        float m = fbm(p.u * 5f, p.v * 7f, 3);
        int mem = mix(p.c1, p.c2, 0.45f + m * 0.45f);
        // isik gecen zar: nabiz
        float pulse = 0.5f + 0.5f * sin(p.t * 1.7f + p.v * 4f);
        mem = screen(mem, 0xFF5030, smooth(0.6f, 0.9f, m) * 0.3f * pulse);
        int c = mix(mem, p.c3, rib);
        c = mix(c, mul(p.c3, 0.75f), smooth(0.07f, 0f, p.v) + smooth(0.06f, 0f, p.u));
        p.rgb = c;
        p.glow = clamp01(smooth(0.65f, 0.95f, m) * 0.35f * pulse);
        // kemik araliklarinda zar asagi kavisli kesilir
        float ang = (float) Math.atan2(p.v, Math.max(0.001f, p.u * 0.5f)) / ((float) Math.PI / 2f) * 4f;
        float f = fract(ang) * 2f - 1f;
        if (p.v > 0.86f + 0.12f * f * f && rib < 0.5f) p.alpha = 0f;
    }

    private static void bat(Px p) {
        float rib = ribs(p, 5, 0.1f);
        float m = fbm(p.u * 6f, p.v * 8f, 2);
        int c = mix(p.c1, p.c2, 0.35f + m * 0.5f);
        c = mix(c, p.c3, rib * 0.8f);
        // ince kilcal damarlar
        float vein = smooth(0.04f, 0f, Math.abs(fbm(p.u * 9f, p.v * 9f, 2) - 0.5f));
        c = mix(c, p.c3, vein * 0.25f);
        p.rgb = c;
        float ang = (float) Math.atan2(p.v, Math.max(0.001f, p.u * 0.5f)) / ((float) Math.PI / 2f) * 5f;
        float f = fract(ang) * 2f - 1f;
        if (p.v > 0.84f + 0.14f * f * f && rib < 0.5f) p.alpha = 0f;
    }

    private static void infernal(Px p) {
        float rib = ribs(p, 4, 0.14f);
        float m = fbm(p.u * 4f, p.v * 6f + p.t * 0.3f, 3);
        int mem = mix(p.c1, p.c2, 0.3f + m * 0.6f);
        float vein = smooth(0.05f, 0f, Math.abs(fbm(p.u * 7f, p.v * 7f, p.t * 0.2f, 2) - 0.5f));
        float pulse = 0.5f + 0.5f * sin(p.t * 3f);
        mem = screen(mem, 0xFF8A2A, vein * (0.4f + 0.6f * pulse));
        int c = mix(mem, p.c3, rib);
        float ember = star(p.x, p.y, 71, 0.03f, p.t, 1f) * (1f - rib);
        p.rgb = mix(c, 0xFFC04A, ember);
        p.glow = clamp01(vein * (0.3f + 0.5f * pulse) + ember);
        if (spikes(p, 4f, 0.1f) && rib < 0.5f) p.alpha = 0f;
    }

    private static void dragonfly(Px p) {
        // ince damar agi + yanardoner zar
        Cell cc = cell(p.u * 5f, p.v * 9f, p.cell);
        float vein = smooth(0.06f, 0f, cc.d2 - cc.d1);
        float longV = gridLine(p.u * 4f, 0.09f);
        float irid = 0.5f + 0.5f * sin(p.u * 7f + p.v * 5f + p.t * 1.2f);
        int mem = hsv(fract(0.45f + irid * 0.35f + p.v * 0.1f), 0.45f, 0.75f);
        int c = mix(mul(p.c1, 1.3f), mem, 0.7f);
        c = mix(c, 0x0A1418, Math.max(vein, longV) * 0.8f);
        float glint = smooth(0.08f, 0f, Math.abs(fract(p.u * 0.6f + p.v * 0.4f - p.t * 0.15f) - 0.5f));
        p.rgb = screen(c, p.c3, glint * 0.6f);
        p.glow = clamp01(glint * 0.35f);
        if (scallop(p, 1.2f, 0.12f)) p.alpha = 0f;
    }

    private static void monarch(Px p) {
        float rib = ribs(p, 6, 0.09f);
        // kalin siyah kenar
        float border = Math.max(smooth(0.13f, 0.09f, 1f - p.u), smooth(0.12f, 0.08f, 1f - p.v));
        int c = mix(p.c1, 0xFFB347, fbm(p.u * 3f, p.v * 4f, 2) * 0.4f);
        c = mix(c, p.c2, Math.max(rib, border));
        // kenarda iki sira beyaz benek
        float sp = 0f;
        if (border > 0.5f) {
            float k = p.v * 9f + p.u * 0.5f;
            float f = fract(k) - 0.5f;
            float g = (p.u > 0.89f ? p.u - 0.94f : (p.v > 0.9f ? p.v - 0.95f : 1f));
            sp = smooth(0.25f, 0.12f, (float) Math.sqrt(f * f + g * g * 60f));
        }
        p.rgb = mix(c, p.c3, sp);
        if (scallop(p, 2f, 0.06f)) p.alpha = 0f;
    }

    private static void leaf(Px p) {
        // orta damar (u ~ 0.45) ve yan damarlar
        float mid = smooth(0.035f, 0f, Math.abs(p.u - 0.45f));
        float side = gridLine((p.v - Math.abs(p.u - 0.45f) * 0.8f) * 7f, 0.1f);
        float m = fbm(p.u * 5f, p.v * 6f, 3);
        int c = ramp(p.c1, p.c2, p.c3, clamp01(m * 0.8f + p.v * 0.3f));
        c = mix(c, mul(c, 0.55f), Math.max(mid, side * 0.8f));
        // kuruyan lekeler
        c = mix(c, 0x3A1A06, smooth(0.62f, 0.72f, fbm(p.u * 8f + 3f, p.v * 8f, 2)) * 0.6f);
        p.rgb = c;
        if (spikes(p, 3f, 0.09f) || (p.u > 0.97f && rand(p.x, p.y) < 0.5f)) p.alpha = 0f;
    }

    // ------------------------------------------------------------------ kozmik / isik

    private static void nebula(Px p) {
        float n1 = fbm(p.u * 3f + p.t * 0.05f, p.v * 3f, p.t * 0.08f, 4);
        float n2 = fbm(p.u * 5f - 4f, p.v * 4f + p.t * 0.06f, 3);
        int c = mix(p.c1, p.c2, smooth(0.35f, 0.75f, n1));
        c = screen(c, p.c3, smooth(0.55f, 0.85f, n2) * 0.6f);
        float s = star(p.x, p.y, 11, 0.05f, p.t, 1f);
        float big = Math.max(sparkle(p.x, p.y, 12, 22, 3.5f), sparkle(p.x, p.y, 27, 58, 3f)) * (0.6f + 0.4f * sin(p.t * 2f));
        float k = Math.max(s, big);
        p.rgb = mix(c, 0xFFFFFF, k);
        p.glow = clamp01(k + smooth(0.6f, 0.9f, n2) * 0.3f);
    }

    private static void aurora(Px p) {
        int c = mix(p.c1, mul(p.c1, 1.8f), p.v);
        float acc = 0f;
        for (int i = 0; i < 3; i++) {
            float center = 0.25f + i * 0.25f + 0.08f * sin(p.v * 6f + p.t * (0.8f + i * 0.3f) + i * 2f);
            float band = smooth(0.1f, 0f, Math.abs(p.u - center)) * (0.6f + 0.4f * fbm(p.u * 3f, p.v * 8f - p.t * 0.8f, 2));
            acc = Math.max(acc, band);
            c = screen(c, i == 1 ? p.c3 : p.c2, band * 0.8f);
        }
        float s = star(p.x, p.y, 13, 0.03f, p.t, 1f) * (1f - acc);
        p.rgb = mix(c, 0xFFFFFF, s);
        p.glow = clamp01(acc * 0.8f + s);
    }

    private static void prism(Px p) {
        Cell cc = cell(p.u * 3f, p.v * 6f, p.cell);
        float facet = rand(cc.id, 2);
        float edge = smooth(0.05f, 0f, cc.d2 - cc.d1);
        float sweep = fract(p.u * 0.7f + p.v * 0.5f - p.t * 0.2f + facet * 0.3f);
        int rainbow = hsv(sweep, 0.55f, 1f);
        int c = mix(p.c1, rainbow, 0.35f + facet * 0.35f);
        c = mix(c, 0xFFFFFF, edge * 0.7f);
        float spark = star(p.x, p.y, 17, 0.03f, p.t, 1f);
        p.rgb = mix(c, 0xFFFFFF, spark);
        p.glow = clamp01(edge * 0.4f + spark + facet * 0.15f);
    }

    private static void stainedGlass(Px p) {
        Cell cc = cell(p.u * 3.5f, p.v * 7f, p.cell);
        float edge = smooth(0.09f, 0.04f, cc.d2 - cc.d1);
        int[] pal = {0xC62828, 0x1565C0, 0xF9A825, 0x2E7D32, 0x6A1B9A, 0x00838F};
        int c = pal[(cc.id & 0x7FFFFFFF) % pal.length];
        float light = 0.75f + 0.25f * sin(p.t * 0.8f + rand(cc.id, 4) * 6f);
        c = mul(c, light * (0.85f + 0.3f * (1f - cc.d1)));
        p.rgb = mix(c, p.c1, edge);
        p.glow = clamp01((1f - edge) * 0.35f * light);
    }

    private static void solarFlare(Px p) {
        float ang = (float) Math.atan2(p.v, p.u + 0.05f);
        float r = (float) Math.sqrt((p.u + 0.05f) * (p.u + 0.05f) + p.v * p.v);
        float rays = 0.5f + 0.5f * sin(ang * 14f - p.t * 2f) * sin(ang * 5f + p.t);
        float core = smooth(0.5f, 0f, r);
        float flare = smooth(0.5f, 1f, rays) * smooth(1.2f, 0.2f, r);
        int c = ramp(p.c1, p.c2, p.c3, clamp01(core + flare * 0.6f));
        float spot = fbm(p.u * 6f, p.v * 6f - p.t * 0.5f, 2);
        p.rgb = screen(c, p.c3, smooth(0.6f, 0.8f, spot) * 0.3f);
        p.glow = clamp01(core * 0.8f + flare * 0.7f);
    }

    private static void eclipse(Px p) {
        float cx = 0.5f, cy = 0.32f;
        float dx = (p.u - cx) * 0.5f, dy = p.v - cy;
        float r = (float) Math.sqrt(dx * dx + dy * dy);
        float ang = (float) Math.atan2(dy, dx);
        float corona = smooth(0.32f, 0.12f, r) * (0.6f + 0.4f * sin(ang * 9f + p.t * 1.3f) * sin(ang * 4f - p.t));
        int c = mix(p.c1, p.c2, p.v * 0.6f);
        c = screen(c, p.c3, corona);
        if (r < 0.12f) c = 0x020204;
        float ring = smooth(0.02f, 0f, Math.abs(r - 0.125f));
        c = mix(c, 0xFFFFFF, ring);
        float s = star(p.x, p.y, 19, 0.03f, p.t, 1f) * smooth(0.25f, 0.4f, r);
        p.rgb = mix(c, 0xFFFFFF, s);
        p.glow = clamp01(corona + ring + s);
    }

    private static final float[][] STARS = {{8, 6}, {20, 14}, {31, 9}, {14, 28}, {27, 34}, {9, 46}, {22, 52}, {33, 62}, {16, 70}, {28, 76}};

    private static void constellation(Px p) {
        int c = mix(p.c1, p.c2, p.v * 0.7f + fbm(p.u * 3f, p.v * 3f, 2) * 0.3f);
        // sabit yildiz noktalari ve aralarindaki cizgiler
        float[][] st = STARS;
        float line = 0f, dot = 0f;
        for (int i = 0; i < st.length; i++) {
            float dx = p.x - st[i][0], dy = p.y - st[i][1];
            float d = (float) Math.sqrt(dx * dx + dy * dy);
            if (d < 2.2f) dot = Math.max(dot, smooth(2.2f, 0f, d) * (0.7f + 0.3f * sin(p.t * 2f + i)));
            if (i + 1 < st.length) {
                float ax = st[i][0], ay = st[i][1], bx = st[i + 1][0], by = st[i + 1][1];
                // cizginin kutusu disindaysa atla
                if (p.x < Math.min(ax, bx) - 1f || p.x > Math.max(ax, bx) + 1f || p.y < Math.min(ay, by) - 1f || p.y > Math.max(ay, by) + 1f) continue;
                float vx = bx - ax, vy = by - ay;
                float tt = clamp01(((p.x - ax) * vx + (p.y - ay) * vy) / (vx * vx + vy * vy));
                float lx = ax + vx * tt - p.x, ly = ay + vy * tt - p.y;
                line = Math.max(line, smooth(0.7f, 0f, (float) Math.sqrt(lx * lx + ly * ly)));
            }
        }
        float flow = 0.5f + 0.5f * sin(p.y * 0.3f - p.t * 3f);
        float bg = star(p.x, p.y, 23, 0.03f, p.t, 1f) * 0.6f;
        c = mix(c, 0x9FC7FF, line * (0.35f + 0.35f * flow));
        p.rgb = mix(c, p.c3, Math.max(dot, bg));
        p.glow = clamp01(dot + line * 0.4f * flow + bg);
    }

    private static void cosmicRoyal(Px p) {
        float velvet = 0.85f + 0.15f * fbm(p.u * 8f, p.v * 12f, 2);
        int c = mul(mix(p.c1, p.c2, p.v * 0.5f), velvet);
        float border = smooth(0.1f, 0.07f, 1f - p.u) + smooth(0.07f, 0.05f, p.v);
        float pattern = gridLine(p.u * 3f + p.v * 3f, 0.07f)
                + gridLine(p.u * 3f - p.v * 3f, 0.07f);
        c = mix(c, mul(p.c3, 0.7f), clamp01(pattern) * 0.45f);
        c = mix(c, p.c3, clamp01(border));
        float s = star(p.x, p.y, 29, 0.04f, p.t, 1f);
        float sweep = smooth(0.06f, 0f, Math.abs(fract(p.v * 0.8f - p.t * 0.15f) - 0.5f)) * clamp01(border);
        p.rgb = mix(screen(c, 0xFFFFFF, sweep * 0.6f), 0xFFF2C0, s);
        p.glow = clamp01(s + sweep * 0.6f + clamp01(border) * 0.2f);
    }

    // ------------------------------------------------------------------ element

    private static void iceShard(Px p) {
        Cell cc = cell(p.u * 3f, p.v * 7f + p.u * 2f, p.cell);
        float facet = rand(cc.id, 3);
        float edge = smooth(0.08f, 0f, cc.d2 - cc.d1);
        int c = mix(p.c1, p.c2, 0.3f + facet * 0.6f);
        c = mix(c, 0xFFFFFF, edge * 0.8f);
        float sweep = smooth(0.07f, 0f, Math.abs(fract(p.u * 0.5f + p.v * 0.7f - p.t * 0.18f) - 0.5f));
        float s = star(p.x, p.y, 31, 0.03f, p.t, 1f);
        p.rgb = mix(screen(c, p.c3, sweep * 0.6f * facet), 0xFFFFFF, s);
        p.glow = clamp01(edge * 0.4f + sweep * 0.5f + s);
        if (spikes(p, 4f, 0.1f + 0.06f * facet)) p.alpha = 0f;
    }

    private static void magma(Px p) {
        Cell cc = cell(p.u * 3f, p.v * 6f, p.cell);
        float crack = smooth(0.12f, 0f, cc.d2 - cc.d1);
        float pulse = 0.55f + 0.45f * sin(p.t * 2.2f - p.v * 5f + rand(cc.id, 1) * 3f);
        int rock = mul(p.c1, 0.8f + 0.5f * fbm(p.u * 10f, p.v * 10f, 2));
        int hot = mix(p.c2, p.c3, pulse * crack);
        p.rgb = mix(rock, hot, crack * (0.6f + 0.4f * pulse));
        p.glow = clamp01(crack * pulse);
    }

    private static void lightning(Px p) {
        float n = fbm(p.u * 3f, p.v * 3f + p.t * 0.4f, 3);
        int c = mix(p.c1, p.c2, smooth(0.35f, 0.8f, n));
        // her 1.4 sn'de yeni yol, kisa cakma
        float cycle = p.t / 1.4f;
        int seed = (int) Math.floor(cycle);
        float ph = cycle - seed;
        float flash = smooth(0.25f, 0f, ph);
        float path = 0.5f + 0.35f * (fbm(p.v * 5f, seed * 3.1f, 3) - 0.5f) * 2f;
        float bolt = smooth(0.035f, 0f, Math.abs(p.u - path));
        float branch = smooth(0.025f, 0f, Math.abs(p.u - path - (p.v - 0.4f) * 0.8f)) * (p.v > 0.4f && p.v < 0.75f ? 1f : 0f);
        float k = clamp01(bolt + branch * 0.8f) * (0.25f + 0.75f * flash);
        c = screen(c, p.c3, flash * 0.15f);
        p.rgb = mix(c, p.c3, k);
        p.glow = clamp01(k + flash * 0.1f);
    }

    private static void wave(Px p) {
        float y = p.v * 9f + sin(p.u * 6f + p.t * 1.6f) * 0.5f + p.t * 0.6f;
        float crest = fract(y);
        int c = mix(p.c1, p.c2, crest * 0.8f + 0.1f * fbm(p.u * 4f, p.v * 6f, 2));
        float foam = smooth(0.82f, 0.97f, crest) * (0.6f + 0.4f * fbm(p.u * 12f, y * 3f, 2));
        p.rgb = mix(c, p.c3, foam);
        p.glow = clamp01(foam * 0.3f);
        if (scallop(p, 3f, 0.05f + 0.03f * sin(p.t * 2f + p.u * 6f))) p.alpha = 0f;
    }

    private static void waterfall(Px p) {
        float flow = fbm(p.u * 6f, p.v * 3f - p.t * 1.6f, 3);
        float streak = 0.5f + 0.5f * sin(p.u * 40f + fbm(p.u * 3f, p.v, 2) * 6f);
        int c = mix(p.c1, p.c2, flow * 0.7f + streak * 0.2f);
        float white = smooth(0.62f, 0.8f, flow) * (0.6f + 0.4f * streak);
        float mist = smooth(0.75f, 1f, p.v) * fbm(p.u * 8f, p.v * 8f - p.t, 2);
        p.rgb = mix(c, p.c3, clamp01(white + mist * 0.6f));
        p.glow = clamp01(white * 0.3f);
        if (scallop(p, 5f, 0.05f)) p.alpha = 0f;
    }

    private static void blizzard(Px p) {
        int c = mix(p.c1, p.c2, p.v * 0.6f + fbm(p.u * 3f, p.v * 3f - p.t * 0.4f, 3) * 0.4f);
        float k = 0f;
        for (int layer = 0; layer < 3; layer++) {
            float sp = 0.6f + layer * 0.5f;
            float xx = p.x + p.t * (8f + layer * 6f) + layer * 13f, yy = p.y + p.t * (14f + layer * 9f) + sin(p.y * 0.2f + layer) * 2f;
            int ix = (int) Math.floor(xx / 6f), iy = (int) Math.floor(yy / 6f);
            if (rand(ix, iy, layer + 40) < 0.35f) {
                float fx = xx - ix * 6f - 3f, fy = yy - iy * 6f - 3f;
                k = Math.max(k, smooth(1.4f / sp + 0.3f, 0f, (float) Math.sqrt(fx * fx + fy * fy)));
            }
        }
        p.rgb = mix(c, p.c3, k);
        p.glow = clamp01(k * 0.5f);
        if (scallop(p, 4f, 0.06f)) p.alpha = 0f;
    }

    private static void sand(Px p) {
        float dune = sin(p.v * 14f + sin(p.u * 4f) * 1.5f + p.t * 0.5f);
        float grain = rand(p.x, p.y + (int) (p.t * 6f)) * 0.12f;
        int c = mix(p.c1, p.c2, 0.5f + 0.4f * dune);
        c = mul(c, 0.94f + grain);
        float sheen = smooth(0.9f, 1f, dune) * 0.5f;
        p.rgb = mix(c, p.c3, sheen);
    }

    private static void plasma(Px p) {
        float a = sin(p.u * 7f + p.t * 1.3f) + sin(p.v * 9f - p.t * 1.1f) + sin((p.u + p.v) * 6f + p.t * 0.7f)
                + sin((float) Math.sqrt(p.u * p.u * 30f + p.v * p.v * 60f) * 3f - p.t * 2f);
        float f = a * 0.125f + 0.5f;
        int c = ramp(p.c1, p.c2, p.c3, clamp01(f));
        float hot = smooth(0.7f, 0.95f, f) + smooth(0.3f, 0.05f, f) * 0.5f;
        p.rgb = screen(c, 0xFFFFFF, smooth(0.85f, 1f, f) * 0.5f);
        p.glow = clamp01(hot * 0.8f);
    }

    private static void voidRift(Px p) {
        float n = fbm(p.u * 3f, p.v * 5f + p.t * 0.2f, 3);
        int c = mix(p.c1, p.c2, n * 0.6f);
        float rift = 0f;
        for (int i = 0; i < 3; i++) {
            float path = 0.2f + i * 0.3f + 0.08f * (fbm(p.v * 6f, i * 7.3f, 2) - 0.5f) * 3f;
            float open = 0.012f + 0.02f * (0.5f + 0.5f * sin(p.t * 1.5f + i * 2f)) * sin(p.v * (float) Math.PI);
            rift = Math.max(rift, smooth(open + 0.03f, open, Math.abs(p.u - path)));
        }
        float s = star(p.x, p.y, 37, 0.04f, p.t, 1f) * (1f - rift);
        c = mix(c, p.c3, rift);
        p.rgb = mix(c, 0xF0C8FF, s * 0.7f);
        p.glow = clamp01(rift + s * 0.5f);
    }

    private static void obsidian(Px p) {
        Cell cc = cell(p.u * 3f, p.v * 6f, p.cell);
        float f = rand(cc.id, 1);
        int c = mix(p.c1, p.c2, f * 0.5f + smooth(0.1f, 0f, cc.d2 - cc.d1) * 0.4f);
        float vein = smooth(0.04f, 0f, Math.abs(fbm(p.u * 5f, p.v * 5f, 2) - 0.5f));
        float sweep = smooth(0.06f, 0f, Math.abs(fract(p.u * 0.5f + p.v * 0.5f - p.t * 0.1f) - 0.5f)) * f;
        c = screen(c, p.c3, sweep * 0.5f + vein * 0.4f);
        p.rgb = c;
        p.glow = clamp01(vein * 0.5f + sweep * 0.4f);
    }

    // ------------------------------------------------------------------ teknoloji

    private static void cyber(Px p) {
        int c = mul(p.c1, 1f + 0.4f * fbm(p.u * 6f, p.v * 6f, 2));
        // panel cizgileri
        float gx = fract(p.u * 4f), gy = fract(p.v * 8f + (((int) (p.u * 4f)) & 1) * 0.5f);
        float panel = Math.max(smooth(0.06f, 0f, Math.min(gx, 1f - gx)), smooth(0.05f, 0f, Math.min(gy, 1f - gy)));
        c = mix(c, mul(p.c1, 2.2f), panel * 0.6f);
        // kenar ve damar boyunca akan neon
        float rim = smooth(0.05f, 0f, 1f - p.u) + smooth(0.04f, 0f, p.v);
        float circuit = gridLine(p.u * 4f, 0.1f) * (fract(p.v * 3f + p.u) < 0.6f ? 1f : 0f);
        float flow = 0.5f + 0.5f * sin(p.v * 20f - p.t * 6f);
        float neon = clamp01(rim + circuit);
        int nc = mix(p.c2, p.c3, 0.5f + 0.5f * sin(p.v * 4f + p.t));
        p.rgb = mix(c, nc, neon * (0.5f + 0.5f * flow));
        p.glow = clamp01(neon * (0.4f + 0.6f * flow));
    }

    private static void hologram(Px p) {
        float scan = 0.65f + 0.35f * sin(p.y * 1.6f - p.t * 9f);
        float flicker = 0.85f + 0.15f * sin(p.t * 23f) * sin(p.t * 7f);
        float grid = Math.max(gridLine(p.u * 5f, 0.1f), gridLine(p.v * 10f, 0.1f));
        float glitch = rand((int) (p.t * 8f), p.y / 4) < 0.08f ? 1f : 0f;
        int c = mix(p.c1, p.c2, 0.4f * scan + grid * 0.5f);
        c = mix(c, p.c3, grid * 0.4f + glitch * 0.3f);
        p.rgb = mul(c, flicker);
        p.glow = clamp01((0.35f * scan + grid * 0.5f) * flicker);
        // seyrek tarama bosluklari
        if ((p.y & 3) == 0 && rand(p.x, p.y + (int) (p.t * 10f)) < 0.15f) p.alpha = 0f;
    }

    private static void matrix(Px p) {
        int col = p.x / 4;
        float sp = 6f + rand(col, 1) * 10f;
        float head = fract((p.t * sp + rand(col, 2) * 80f) / 80f) * 80f;
        float d = head - p.y;
        if (d < 0) d += 80f;
        float trail = d < 30f ? 1f - d / 30f : 0f;
        boolean glyph = (p.x & 3) != 3 && rand(col * 7 + (p.x & 3), p.y / 3 + (int) (p.t * 4f) * (d < 2 ? 1 : 0)) < 0.55f && (p.y % 3) != 2;
        int c = mul(p.c1, 1.2f);
        if (glyph) c = mix(c, d < 2f ? p.c3 : p.c2, trail);
        p.rgb = c;
        p.glow = glyph ? clamp01(trail * (d < 2f ? 1f : 0.7f)) : 0f;
    }

    private static void synth(Px p) {
        int sky = mix(p.c1, 0x4A0E6E, p.v * 1.6f);
        float sunR = (float) Math.sqrt((p.u - 0.5f) * (p.u - 0.5f) * 0.3f + (p.v - 0.35f) * (p.v - 0.35f));
        int c = sky;
        if (sunR < 0.17f && p.v < 0.45f) {
            boolean slit = p.v > 0.3f && fract(p.v * 30f) < 0.35f;
            if (!slit) c = mix(p.c3, p.c2, clamp01((p.v - 0.2f) / 0.25f));
        }
        if (p.v > 0.45f) {
            float z = 1f / Math.max(0.02f, p.v - 0.43f);
            float gl = gridLine(z * 0.6f + p.t * 1.2f, 0.12f);
            float gv = gridLine((p.u - 0.5f) * z * 0.8f, 0.1f);
            c = mix(0x16002E, p.c2, Math.max(gl, gv) * 0.9f);
            p.glow = Math.max(gl, gv) * 0.7f;
        } else p.glow = sunR < 0.17f ? 0.4f : 0f;
        p.rgb = c;
    }

    private static void digiCamo(Px p) {
        int bx = p.x / 3, by = p.y / 3;
        float n = fbm(bx * 0.35f, by * 0.35f, 3) + (rand(bx, by) - 0.5f) * 0.15f;
        int c = n < 0.42f ? 0x1E2716 : (n < 0.52f ? p.c1 : (n < 0.62f ? p.c2 : p.c3));
        p.rgb = mul(c, 0.9f + 0.16f * rand(bx, by, 5));
    }

    // ------------------------------------------------------------------ doga / malzeme

    private static void vines(Px p) {
        int c = mul(p.c1, 0.9f + 0.3f * fbm(p.u * 6f, p.v * 6f, 2));
        float vine = 0f, leafK = 0f;
        for (int i = 0; i < 3; i++) {
            float path = 0.2f + i * 0.3f + 0.1f * sin(p.v * 9f + i * 2f);
            float d = Math.abs(p.u - path);
            vine = Math.max(vine, smooth(0.035f, 0.01f, d));
            float lv = fract(p.v * 6f + i * 0.33f);
            float side = (p.u - path) * (((int) (p.v * 6f + i * 0.33f)) % 2 == 0 ? 1f : -1f);
            float ld = (float) Math.sqrt((side - 0.07f) * (side - 0.07f) * 30f + (lv - 0.5f) * (lv - 0.5f) * 6f);
            leafK = Math.max(leafK, smooth(0.5f, 0.3f, ld));
        }
        float pulse = 0.5f + 0.5f * sin(p.t * 2f - p.v * 8f);
        c = mix(c, p.c2, leafK);
        c = mix(c, p.c3, vine * (0.5f + 0.5f * pulse));
        float drip = star(p.x, p.y, 43, 0.02f, p.t, 1f);
        p.rgb = mix(c, p.c3, drip);
        p.glow = clamp01(vine * pulse * 0.8f + drip);
        if (spikes(p, 5f, 0.06f)) p.alpha = 0f;
    }

    private static void cherry(Px p) {
        int c = mix(p.c1, 0xFFF4F8, fbm(p.u * 3f, p.v * 4f, 2) * 0.5f);
        // dallar
        float branch = smooth(0.03f, 0.01f, Math.abs(p.u - (0.25f + 0.2f * sin(p.v * 5f)))) * (p.v < 0.85f ? 1f : 0f);
        c = mix(c, p.c3, branch);
        // bes yaprakli cicekler
        Cell cc = cell(p.u * 4f, p.v * 8f, p.cell);
        float dx = (p.u * 4f - cc.cx), dy = (p.v * 8f - cc.cy);
        float r = (float) Math.sqrt(dx * dx + dy * dy), a = (float) Math.atan2(dy, dx);
        float petal = r < 0.34f * (0.65f + 0.35f * Math.abs(cos(a * 2.5f))) ? 1f : 0f;
        if (rand(cc.id, 6) < 0.6f) c = mix(c, r < 0.08f ? 0xFFE066 : p.c2, petal);
        // dusen yapraklar
        float fall = star(p.x + (int) (p.t * 3f), p.y - (int) (p.t * 9f), 47, 0.015f, 0f, 0f);
        p.rgb = mix(c, p.c2, fall);
        if (scallop(p, 4f, 0.05f)) p.alpha = 0f;
    }

    private static void honeycomb(Px p) {
        float x = p.u * 5f, y = p.v * 10f / 1.732f;
        float row = (float) Math.floor(y);
        float xx = x + (((int) row) & 1) * 0.5f;
        float fx = fract(xx) - 0.5f, fy = fract(y) - 0.5f;
        float hex = Math.max(Math.abs(fx) * 1.15f + Math.abs(fy) * 0.5f * 1.15f, Math.abs(fy) * 1.15f);
        float wall = smooth(0.4f, 0.47f, hex);
        int cellId = hash((int) Math.floor(xx), (int) row);
        boolean filled = rand(cellId, 2) < 0.55f + p.v * 0.3f;
        float level = filled ? smooth(0.2f, -0.3f, fy - (0.5f + 0.5f * sin(p.t + rand(cellId, 3) * 6f)) * 0.2f) : 0f;
        int c = filled ? mul(mix(p.c2, p.c3, 0.3f + 0.4f * (0.5f - fy)), 0.9f + 0.12f * sin(p.t * 1.6f + rand(cellId, 4) * 6f + p.v * 3f))
                : mul(p.c2, 0.45f);
        c = mix(c, p.c1, wall);
        float drip = smooth(0.85f, 1f, p.v) * smooth(0.1f, 0.04f, Math.abs(fract(p.u * 6f) - 0.5f));
        p.rgb = mix(c, p.c2, drip * 0.8f);
        p.glow = clamp01(level * 0.1f + (filled ? 0.12f : 0f) * (1f - wall));
    }

    private static void mushroom(Px p) {
        int c = mix(p.c1, p.c2, 0.5f + 0.5f * (1f - p.v) * fbm(p.u * 3f, p.v * 3f, 2));
        Cell cc = cell(p.u * 3f, p.v * 6f, p.cell);
        float sz = 0.18f + 0.18f * rand(cc.id, 8);
        if (cc.d1 < sz) c = mix(p.c3, 0xE8DCC0, cc.d1 / sz * 0.5f);
        // alt kenarda lamel cizgileri
        float gill = smooth(0.82f, 0.9f, p.v) * (0.5f + 0.5f * sin(p.u * 60f));
        p.rgb = mix(c, 0xF0E0C8, gill * 0.7f);
        if (scallop(p, 2f, 0.05f)) p.alpha = 0f;
    }

    private static void geode(Px p) {
        // dis kabuk taneli tas, ice dogru halkalar ve parlayan kristal
        float dx = (p.u - 0.45f) * 1.3f, dy = (p.v - 0.5f) * 0.65f;
        float r = (float) Math.sqrt(dx * dx + dy * dy) + (fbm(p.u * 5f, p.v * 5f, 2) - 0.5f) * 0.12f;
        int c;
        float glow = 0f;
        if (r > 0.33f) c = mul(p.c1, 0.85f + 0.3f * rand(p.x / 2, p.y / 2));
        else if (r > 0.29f) c = 0xD8D0DC;
        else if (r > 0.26f) c = 0x6A5A78;
        else {
            Cell cc = cell(p.u * 9f, p.v * 14f, p.cell);
            float f = rand(cc.id, 4);
            c = mix(p.c2, p.c3, f * 0.6f + (1f - r / 0.26f) * 0.3f);
            float sp = star(p.x, p.y, 53, 0.06f, p.t, 1f);
            c = mix(c, 0xFFFFFF, sp);
            glow = clamp01(0.25f + sp + f * 0.2f);
        }
        p.rgb = c;
        p.glow = glow;
    }

    private static void runes(Px p) {
        int c = mul(p.c1, 0.8f + 0.4f * fbm(p.u * 6f, p.v * 8f, 3));
        int gx = (int) Math.floor(p.u * 3f), gy = (int) Math.floor(p.v * 6f);
        float fx = fract(p.u * 3f), fy = fract(p.v * 6f);
        int seed = hash(gx, gy);
        // rasgele cizgilerden olusan run: dikey sap + 2 egik cizgi
        float stem = smooth(0.06f, 0.02f, Math.abs(fx - 0.5f)) * (fy > 0.15f && fy < 0.85f ? 1f : 0f);
        float a1 = (rand(seed, 1) - 0.5f) * 1.6f, a2 = (rand(seed, 2) - 0.5f) * 1.6f;
        float l1 = smooth(0.06f, 0.02f, Math.abs(fy - 0.3f - (fx - 0.5f) * a1)) * (Math.abs(fx - 0.5f) < 0.3f ? 1f : 0f);
        float l2 = smooth(0.06f, 0.02f, Math.abs(fy - 0.65f - (fx - 0.5f) * a2)) * (Math.abs(fx - 0.5f) < 0.3f ? 1f : 0f);
        float rune = clamp01(stem + l1 + l2) * (rand(seed, 3) < 0.7f ? 1f : 0f);
        float pulse = 0.5f + 0.5f * sin(p.t * 2f + rand(seed, 4) * 6f);
        c = mix(c, mix(p.c2, p.c3, pulse), rune);
        p.rgb = c;
        p.glow = rune * (0.4f + 0.6f * pulse);
    }

    private static void ancientStone(Px p) {
        int bx = (int) Math.floor(p.u * 3f + (((int) (p.v * 6f)) & 1) * 0.5f), by = (int) Math.floor(p.v * 6f);
        float fx = fract(p.u * 3f + (by & 1) * 0.5f), fy = fract(p.v * 6f);
        float mortar = Math.max(smooth(0.05f, 0f, Math.min(fx, 1f - fx)), smooth(0.08f, 0f, Math.min(fy, 1f - fy)));
        int c = mix(p.c1, p.c2, rand(bx, by) * 0.6f + fbm(p.u * 12f, p.v * 12f, 2) * 0.3f);
        c = mix(c, mul(p.c1, 0.5f), mortar);
        float moss = smooth(0.55f, 0.7f, fbm(p.u * 4f + 2f, p.v * 4f, 3)) * (0.4f + p.v * 0.6f);
        c = mix(c, 0x4E6B3A, moss * 0.8f);
        // sonuk oyma: daire
        float dx = fx - 0.5f, dy = fy - 0.5f;
        float ring = smooth(0.05f, 0f, Math.abs((float) Math.sqrt(dx * dx + dy * dy * 0.6f) - 0.25f)) * (rand(bx, by, 3) < 0.4f ? 1f : 0f);
        p.rgb = mix(c, p.c3, ring * 0.5f);
        p.glow = ring * 0.15f;
    }

    private static void marble(Px p) {
        float n = fbm(p.u * 3f, p.v * 5f, 4);
        float vein = gridLine(n * 4f + p.u * 1.5f, 0.05f);
        float vein2 = gridLine(n * 9f - p.v, 0.04f);
        int c = mix(p.c1, p.c2, n * 0.6f);
        c = mix(c, 0x8A8478, vein2 * 0.4f);
        p.rgb = mix(c, p.c3, vein);
        p.glow = vein * 0.2f;
    }

    private static void goldPlates(Px p) {
        // ust uste binen zirh plakalari (asagi dogru), perçin ve kayan parlama
        float rows = 6f;
        float vv = p.v * rows;
        int row = (int) Math.floor(vv);
        float fv = vv - row;
        float curve = 0.12f * (float) Math.pow(Math.abs(p.u - 0.5f) * 2f, 2);
        float shade = 0.65f + 0.4f * (1f - fv) + curve;
        float lip = smooth(0.85f, 1f, fv - curve);
        int c = mul(mix(p.c1, p.c2, 0.6f), clamp(shade, 0.4f, 1.15f));
        c = mix(c, mul(p.c1, 0.6f), lip);
        float rivet = 0f;
        for (float ru : new float[]{0.18f, 0.82f}) {
            float dx = (p.u - ru) * 10f, dy = (fv - 0.25f) * rows * 1.2f;
            rivet = Math.max(rivet, smooth(1.0f, 0.4f, (float) Math.sqrt(dx * dx + dy * dy)));
        }
        c = mix(c, p.c3, rivet * 0.8f);
        float sweep = smooth(0.06f, 0f, Math.abs(fract(p.u * 0.6f - p.v * 0.5f + p.t * 0.2f) - 0.5f));
        p.rgb = screen(c, p.c3, sweep * 0.7f);
        p.glow = clamp01(sweep * 0.45f + rivet * 0.2f);
    }

    private static void steelPlates(Px p) {
        float rows = 5f;
        float vv = p.v * rows;
        int row = (int) Math.floor(vv);
        float fv = vv - row;
        float brushed = 0.9f + 0.1f * rand(p.x, p.y / 6 + row * 13);
        int c = mul(mix(p.c1, p.c2, 0.55f + 0.35f * (1f - fv)), brushed);
        float seam = smooth(0.08f, 0f, Math.min(fv, 1f - fv));
        c = mix(c, mul(p.c1, 0.5f), seam);
        float rivet = 0f;
        for (int i = 0; i < 4; i++) {
            float dx = (p.u - (0.12f + i * 0.25f)) * 10f, dy = (fv - 0.5f) * rows * 1.1f;
            rivet = Math.max(rivet, smooth(0.9f, 0.4f, (float) Math.sqrt(dx * dx + dy * dy)));
        }
        c = mix(c, 0xE8EEF2, rivet * 0.7f);
        float sweep = smooth(0.05f, 0f, Math.abs(fract(p.v * 0.7f - p.t * 0.22f) - 0.5f));
        p.rgb = screen(c, 0xFFFFFF, sweep * 0.5f);
        p.glow = clamp01(sweep * 0.3f);
    }

    private static void emberScales(Px p) {
        // kara pullar, aralarindan kizgin isik
        final float cw = 8f, chh = 4f, rad = 5f;
        float px = p.x + 0.5f, py = p.y + 0.5f;
        int rowMax = (int) Math.floor(py / chh), rowMin = (int) Math.floor((py - rad) / chh);
        float best = -1f;
        int bestId = 0;
        for (int r = rowMin; r <= rowMax; r++) {
            float off = (r & 1) == 1 ? 0.5f : 0f;
            float cy = r * chh;
            if (py < cy) continue;
            float cx = ((float) Math.floor(px / cw - off) + 0.5f + off) * cw;
            float dx = px - cx, dy = py - cy;
            float dd = (float) Math.sqrt(dx * dx + dy * dy);
            if (dd < rad) {
                best = dd / rad;
                bestId = hash((int) Math.floor(px / cw - off), r);
                break;
            }
        }
        float pulse = 0.55f + 0.45f * sin(p.t * 2.5f - p.v * 6f);
        int c;
        float g;
        if (best < 0f) {
            c = mix(p.c3, 0xFFD27A, pulse * 0.5f);
            g = pulse;
        } else {
            float rim = smooth(0.75f, 1f, best);
            c = mul(mix(p.c1, p.c2, 0.4f + 0.4f * rand(bestId, 2)), 0.8f + 0.4f * (1f - best));
            c = mix(c, p.c3, rim * pulse * 0.8f);
            g = rim * pulse * 0.7f;
        }
        p.rgb = c;
        p.glow = clamp01(g);
        if (spikes(p, 5f, 0.07f)) p.alpha = 0f;
    }

    // ------------------------------------------------------------------ eglenceli

    private static void candy(Px p) {
        float s = fract(p.u * 2f + p.v * 3f - p.t * 0.3f);
        int c = s < 0.42f ? p.c2 : (s < 0.5f ? p.c3 : p.c1);
        float shine = smooth(0.1f, 0f, Math.abs(p.u - 0.3f)) * 0.35f;
        p.rgb = screen(c, 0xFFFFFF, shine);
        p.glow = shine * 0.3f;
    }

    private static void fireflies(Px p) {
        int c = mix(p.c1, p.c2, fbm(p.u * 4f, p.v * 6f, 3) * 0.8f + p.v * 0.2f);
        // otlar alt kenarda
        float grass = p.v > 0.8f ? smooth(0.16f, 0.05f, Math.abs(fract(p.u * 12f + sin(p.v * 20f) * 0.1f) - 0.5f)) : 0f;
        c = mix(c, 0x2E6B2A, grass * smooth(0.8f, 0.95f, p.v));
        float[] sc = p.scratch;
        if (p.scratchKey != 43 || p.scratchT != p.t) {
            // bocek konumlari karede bir kez
            for (int i = 0; i < 7; i++) {
                sc[i * 3] = 4f + rand(i, 1) * 32f + sin(p.t * (0.4f + rand(i, 2) * 0.6f) + i) * 5f;
                sc[i * 3 + 1] = 6f + rand(i, 3) * 66f + cos(p.t * (0.3f + rand(i, 4) * 0.5f) + i * 2f) * 6f;
                sc[i * 3 + 2] = smooth(0.2f, 0.8f, 0.5f + 0.5f * sin(p.t * (1.5f + rand(i, 5)) + i * 3f));
            }
            p.scratchKey = 43;
            p.scratchT = p.t;
        }
        float k = 0f;
        for (int i = 0; i < 7; i++) {
            float dx = p.x - sc[i * 3], dy = p.y - sc[i * 3 + 1];
            if (dx > 3f || dx < -3f || dy > 3f || dy < -3f) continue;
            k = Math.max(k, smooth(3f, 0f, (float) Math.sqrt(dx * dx + dy * dy)) * sc[i * 3 + 2]);
        }
        p.rgb = screen(c, p.c3, k);
        p.glow = clamp01(k);
    }

    private static void blood(Px p) {
        int c = mix(p.c1, p.c2, 0.3f + 0.5f * fbm(p.u * 4f, p.v * 5f, 3));
        float k = 0f;
        for (int i = 0; i < 6; i++) {
            float x = 3f + i * 6.5f + rand(i, 7) * 2f;
            float len = 20f + rand(i, 8) * 40f;
            float sp = 6f + rand(i, 9) * 6f;
            float head = (p.t * sp + rand(i, 10) * 80f) % (len + 30f);
            float dx = Math.abs(p.x - x);
            if (p.y < head && p.y > head - len) k = Math.max(k, smooth(1.2f, 0.4f, dx));
            float hy = p.y - head;
            k = Math.max(k, smooth(2.2f, 0.8f, (float) Math.sqrt(dx * dx + hy * hy * 0.6f)));
        }
        float wet = smooth(0.4f, 1f, k);
        p.rgb = mix(c, p.c3, wet * 0.8f);
        p.glow = clamp01(wet * 0.25f);
        if (scallop(p, 6f, 0.04f)) p.alpha = 0f;
    }
}
