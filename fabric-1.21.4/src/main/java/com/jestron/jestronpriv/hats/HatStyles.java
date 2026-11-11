package com.jestron.jestronpriv.hats;

import java.util.List;

import static com.jestron.jestronpriv.hats.HatMesh.BASE;
import static com.jestron.jestronpriv.hats.HatMesh.GLOW;
import static com.jestron.jestronpriv.hats.HatMesh.TRANS;

/**
 * 50 animasyonlu sapka. Her biri {@link HatMesh} ilkel sekilleriyle "sapka uzayinda" (y yukari, kok kafanin ust
 * ortasi, on -z) kurulur; zaman ve harekete gore canlanir.
 */
public final class HatStyles {

    private static final float PI = (float) Math.PI, TAU = PI * 2f;
    // tup / egri icin paylasilan tampon (tek is parcacigindan cizim)
    private static final float[] P = new float[3 * 24];
    private static final float[] P2 = new float[3 * 24];

    private HatStyles() {
    }

    public static final List<HatStyle> ALL = List.of(
            new HatStyle("kral_taci", "Kral Tacı", "Altın taç, nabız gibi parlayan değerli taşlar", "golden_helmet", 0xF5C542, 0xC9922A, 0xE0262B, "dust:FFD54A", 1.2f, HatStyles::kralTaci),
            new HatStyle("kralice_taci", "Kraliçe Tacı", "İnce gümüş tiara, inciler ve pembe elmas", "amethyst_shard", 0xDDE3EA, 0xFFFFFF, 0xFF6FB5, "dust:FFC8E6", 1.0f, HatStyles::kraliceTaci),
            new HatStyle("melek_halesi", "Melek Halesi", "Süzülen ışık halesi, etrafında dönen yıldızcıklar", "glowstone_dust", 0xFFF2B0, 0xFFFFFF, 0xFFE070, "end_rod", 1.5f, HatStyles::melekHalesi),
            new HatStyle("seytan_boynuzu", "Şeytan Boynuzları", "Kıvrık kırmızı boynuzlar, uçları kor gibi", "fire_charge", 0x8E1217, 0x3A0709, 0xFF5A1E, "small_flame", 1.2f, HatStyles::seytanBoynuzu),
            new HatStyle("silindir", "Silindir Şapka", "Kibar siyah silindir, kırmızı saten bant", "black_wool", 0x1B1B20, 0x9E1B22, 0x2A2A30, null, 0f, HatStyles::silindir),
            new HatStyle("sihirbaz", "Sihirbaz Şapkası", "Sivri mavi külah, parıldayan yıldızlar ve ay", "enchanted_book", 0x2B3BA8, 0x1A2470, 0xFFE070, "enchant", 3f, HatStyles::sihirbaz),
            new HatStyle("cadi", "Cadı Şapkası", "Ucu kıvrık kara külah, mor bant, altın toka", "cauldron", 0x19141F, 0x6B2FA0, 0xE8B83A, "witch", 1.5f, HatStyles::cadi),
            new HatStyle("kovboy", "Kovboy Şapkası", "Yanları kalkık deri şapka", "leather", 0x7A4A25, 0x3E2412, 0xC9A26A, null, 0f, HatStyles::kovboy),
            new HatStyle("korsan", "Korsan Şapkası", "Üç köşeli kaptan şapkası, kurukafa armalı", "black_banner", 0x1A1714, 0xD9B44A, 0xF2EEE4, null, 0f, HatStyles::korsan),
            new HatStyle("asci", "Aşçı Şapkası", "Kabarık beyaz aşçı başlığı, hafif nefes alır", "cake", 0xF7F7F2, 0xE4E4DC, 0xD0D0C8, null, 0f, HatStyles::asci),
            new HatStyle("viking", "Viking Miğferi", "Demir miğfer, fildişi boynuzlar", "iron_helmet", 0x8A9097, 0xEDE3C6, 0xC9922A, null, 0f, HatStyles::viking),
            new HatStyle("pervane", "Pervaneli Şapka", "Rengarenk bere, koştukça hızlanan pervane", "feather", 0xE23B3B, 0x2F6FE0, 0xF5D23A, null, 0f, HatStyles::pervane),
            new HatStyle("kedi_kulagi", "Kedi Kulakları", "Ara sıra seğiren sevimli kulaklar", "string", 0x2E2B2B, 0xF5A6C0, 0xFFFFFF, null, 0f, HatStyles::kediKulagi),
            new HatStyle("tavsan_kulagi", "Tavşan Kulakları", "Uzun, salınan pofuduk kulaklar", "rabbit_foot", 0xF4F1EC, 0xF5A6C0, 0xFFFFFF, null, 0f, HatStyles::tavsanKulagi),
            new HatStyle("cicek_taci", "Çiçek Tacı", "Sarmaşık üzerinde renk renk çiçekler", "pink_petals", 0x3F8F3A, 0xFF8FC7, 0xFFE05A, "cherry_leaves", 0.8f, HatStyles::cicekTaci),
            new HatStyle("anten", "Uzaylı Antenleri", "Ucunda yanıp sönen ışıklar", "lime_dye", 0x5BD86A, 0x2F2F35, 0x9BFF6A, "dust:9BFF6A", 1.0f, HatStyles::anten),
            new HatStyle("noel", "Noel Şapkası", "Kırmızı külah, beyaz kürk, sallanan ponpon", "snowball", 0xC8202A, 0xF7F7F7, 0xFFFFFF, "snowflake", 0.8f, HatStyles::noel),
            new HatStyle("beyzbol", "Beyzbol Şapkası", "Siperlikli spor kep", "light_blue_dye", 0x1F3A7A, 0xF2F2F2, 0xE23B3B, null, 0f, HatStyles::beyzbol),
            new HatStyle("bere", "Kışlık Bere", "Örgü bere, ponponlu", "white_wool", 0x2E7D6F, 0xF2F2F2, 0xE8E8E8, null, 0f, HatStyles::bere),
            new HatStyle("mezuniyet", "Mezuniyet Kepi", "Kare kep, sallanan altın püskül", "paper", 0x1B1B22, 0xE8B83A, 0x2A2A32, null, 0f, HatStyles::mezuniyet),
            new HatStyle("fes", "Fes", "Kırmızı fes, siyah püskül", "red_dye", 0xB3131B, 0x111111, 0x7A0C11, null, 0f, HatStyles::fes),
            new HatStyle("samuray", "Samuray Kabutosu", "Koyu miğfer, altın hilal arma", "netherite_helmet", 0x2C2F36, 0xE8B83A, 0x8E1217, null, 0f, HatStyles::samuray),
            new HatStyle("firavun", "Firavun Başlığı", "Altın-lacivert çizgili başlık, kobra arma", "gold_block", 0xE8B83A, 0x1F3A8A, 0x3FA07A, "dust:FFD54A", 0.6f, HatStyles::firavun),
            new HatStyle("neon_hale", "Neon Hale", "Gökkuşağı renklerinde akan çift neon halka", "glow_ink_sac", 0xFF3AD5, 0x3AE8FF, 0xFFFFFF, "dust:FF6AE8", 1.0f, HatStyles::neonHale),
            new HatStyle("kristal_tac", "Kristal Taç", "Işık geçiren buz mavisi kristaller", "diamond", 0x7FE8FF, 0xBFF6FF, 0xFFFFFF, "dust:BFF6FF", 1.0f, HatStyles::kristalTac),
            new HatStyle("ates_taci", "Ateş Tacı", "Titreşen alev dilleri", "blaze_powder", 0xFF6A1A, 0xFFD23A, 0x5A1206, "flame", 3f, HatStyles::atesTaci),
            new HatStyle("buz_taci", "Buz Tacı", "Sivri buz kıymıkları, parıltılar", "blue_ice", 0xCFF4FF, 0x8FD8FF, 0xFFFFFF, "snowflake", 1.5f, HatStyles::buzTaci),
            new HatStyle("yildiz_yorunge", "Yıldız Yörüngesi", "Başın etrafında dönen yıldızlar", "nether_star", 0xFFE65A, 0xFFF6C0, 0xFFFFFF, "dust:FFF3A0", 1.5f, HatStyles::yildizYorunge),
            new HatStyle("saturn", "Satürn", "Halkalı minik gezegen ve uydusu", "ender_eye", 0xE8A86A, 0xF4DBA8, 0xBBBBCC, null, 0f, HatStyles::saturn),
            new HatStyle("bulut", "Yağmur Bulutu", "Kafanın üstünde süzülen bulut", "white_dye", 0xF4F7FA, 0xD6DEE6, 0x7FB8FF, "falling_water", 3f, HatStyles::bulut),
            new HatStyle("gokkusagi_bulut", "Gökkuşağı Bulutu", "İki bulut arasında parlayan gökkuşağı", "prismarine_shard", 0xF4F7FA, 0xD6DEE6, 0xFFFFFF, "rainbow", 1.2f, HatStyles::gokkusagiBulut),
            new HatStyle("kurbaga", "Kurbağa", "Göz kırpan yeşil kurbağa şapka", "lily_pad", 0x5BAA3A, 0xF4F4EE, 0x1A1A1A, null, 0f, HatStyles::kurbaga),
            new HatStyle("mantar", "Mantar", "Benekli kırmızı mantar şapkası", "red_mushroom", 0xD32222, 0xF4F0E6, 0xE8D8C0, "spore_blossom_air", 0.8f, HatStyles::mantar),
            new HatStyle("ordek", "Lastik Ördek", "Başında sallanan sarı ördek", "egg", 0xFFD23A, 0xFF8A1A, 0x1A1A1A, null, 0f, HatStyles::ordek),
            new HatStyle("panda", "Panda Kulakları", "Yuvarlak siyah kulaklar", "bamboo", 0x1E1E1E, 0x3A3A3A, 0xFFFFFF, null, 0f, HatStyles::panda),
            new HatStyle("tilki", "Tilki Kulakları", "Turuncu, ucu siyah kulaklar", "sweet_berries", 0xE8782A, 0xFFF2E6, 0x1E1E1E, null, 0f, HatStyles::tilki),
            new HatStyle("ejder_boynuz", "Ejderha Boynuzları", "Geriye uzanan burgulu boynuzlar ve sırt dikenleri", "dragon_breath", 0x3A1F5C, 0x9A5AE8, 0xD9A6FF, "dragon_breath", 1.0f, HatStyles::ejderBoynuz),
            new HatStyle("geyik", "Geyik Boynuzu", "Çatallı orman boynuzları", "stick", 0x7A5434, 0x5A3C24, 0xE8D8C0, null, 0f, HatStyles::geyik),
            new HatStyle("unicorn", "Unicorn Boynuzu", "Burgulu gökkuşağı boynuzu, sihirli parıltı", "end_rod", 0xFFFFFF, 0xFFC8F0, 0xFFE070, "rainbow", 2f, HatStyles::unicorn),
            new HatStyle("sovalye_tuyu", "Şövalye Sorgucu", "Rüzgârda dalgalanan tüy sorguç", "iron_sword", 0xC8202A, 0xF4F4F4, 0xA0A0A8, null, 0f, HatStyles::sovalyeTuyu),
            new HatStyle("polis", "Polis Şapkası", "Siperlikli lacivert şapka, parlayan rozet", "shield", 0x1A2A55, 0x111111, 0xE8C33A, null, 0f, HatStyles::polis),
            new HatStyle("denizci", "Denizci Şapkası", "Beyaz denizci şapkası, uçuşan kurdeleler", "nautilus_shell", 0xF7F7F7, 0x1A2A6A, 0xE8C33A, null, 0f, HatStyles::denizci),
            new HatStyle("fotr", "Fötr Şapka", "Ortası çökük klasik fötr", "gray_wool", 0x55504A, 0x1C1A18, 0x6A645C, null, 0f, HatStyles::fotr),
            new HatStyle("melon", "Melon Şapka", "Yuvarlak tepeli İngiliz şapkası", "coal", 0x1C1C1E, 0x2E2E30, 0x3A3A3C, null, 0f, HatStyles::melon),
            new HatStyle("hasir", "Hasır Şapka", "Geniş kenarlı yazlık hasır, kurdele fiyonklu", "wheat", 0xE3C27A, 0xC23A3A, 0xC9A55A, null, 0f, HatStyles::hasir),
            new HatStyle("kulaklik", "DJ Kulaklığı", "Ritimle nabız atan ışıklı kulaklık", "note_block", 0x1A1A1E, 0x3AE8FF, 0xFF3AD5, "note", 0.6f, HatStyles::kulaklik),
            new HatStyle("glitch_hale", "Glitch Halesi", "Bozulan piksel küpler halkası", "sculk_sensor", 0x3AE8FF, 0xFF3AD5, 0xFFFFFF, "dust:3AE8FF", 1.2f, HatStyles::glitchHale),
            new HatStyle("kara_delik", "Kara Delik", "Dönen yığılma diski, ışık halkası", "obsidian", 0x050505, 0xFF8A2A, 0x8A3AFF, "reverse_portal", 3f, HatStyles::karaDelik),
            new HatStyle("kalp_hale", "Kalp Halesi", "Atan kalplerden hale", "red_dye", 0xFF3A6A, 0xFF8FB0, 0xFFFFFF, "heart", 0.3f, HatStyles::kalpHale),
            new HatStyle("ruh_alevi", "Ruh Alevi", "Başının üstünde titreşen mavi ruh ateşi", "soul_lantern", 0x3AD8FF, 0xBFF6FF, 0x1A6AFF, "soul_fire_flame", 2.5f, HatStyles::ruhAlevi)
    );

    public static HatStyle get(String id) {
        if (id == null) return null;
        for (HatStyle s : ALL) if (s.id().equals(id)) return s;
        return null;
    }

    // ================================================================== yardimcilar

    private static float wave(float t, float speed, float phase) {
        return (float) Math.sin(t * speed + phase);
    }

    /** Ceyrek elips profilli kubbe: y tabaninda r yaricap, h yukseklik. */
    static void dome(HatMesh m, float y, float r, float h, int lat, int lon) {
        for (int i = 0; i < lat; i++) {
            float a0 = i / (float) lat * PI / 2, a1 = (i + 1) / (float) lat * PI / 2;
            float r0 = r * (float) Math.cos(a0), r1 = i == lat - 1 ? 0f : r * (float) Math.cos(a1);
            m.frustum(y + h * (float) Math.sin(a0), r0, y + h * (float) Math.sin(a1), r1, lon, false, false);
        }
    }

    /** Kubbe, dilimleri sirayla renkli (pervaneli bere). */
    static void dome4(HatMesh m, float y, float r, float h, int lat, int lon, int[] cols) {
        for (int j = 0; j < lon; j++) {
            float b0 = j * TAU / lon, b1 = (j + 1) * TAU / lon;
            m.rgb(cols[j % cols.length]);
            for (int i = 0; i < lat; i++) {
                float a0 = i / (float) lat * PI / 2, a1 = (i + 1) / (float) lat * PI / 2;
                float r0 = r * (float) Math.cos(a0), r1 = i == lat - 1 ? 0f : r * (float) Math.cos(a1);
                float y0 = y + h * (float) Math.sin(a0), y1 = y + h * (float) Math.sin(a1);
                float c0 = (float) Math.cos(b0), s0 = (float) Math.sin(b0), c1 = (float) Math.cos(b1), s1 = (float) Math.sin(b1);
                m.quad(c0 * r0, y0, s0 * r0, c1 * r0, y0, s1 * r0, c1 * r1, y1, s1 * r1, c0 * r1, y1, s0 * r1, 0, 0, 1, 1);
            }
        }
    }

    /**
     * Kenar (siper): mode 0 duz, 1 yanlar kalkik (kovboy), 2 uc kose (korsan), 3 dalgali (hasir), 4 onu egik (fotr),
     * 5 hafif kivrik (melon).
     */
    static void brimShaped(HatMesh m, float y, float rIn, float rOut, int seg, int mode, float amount) {
        float thick = 0.018f;
        for (int i = 0; i < seg; i++) {
            float a0 = i * TAU / seg, a1 = (i + 1) * TAU / seg;
            float c0 = (float) Math.cos(a0), s0 = (float) Math.sin(a0), c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);
            float o0 = brimLift(a0, mode, amount), o1 = brimLift(a1, mode, amount);
            // ic kenarda kaldirma yok, dis kenarda tam
            m.quad(c0 * rIn, y, s0 * rIn, c0 * rOut, y + o0, s0 * rOut, c1 * rOut, y + o1, s1 * rOut, c1 * rIn, y, s1 * rIn, i / (float) seg * 4f, 0, (i + 1) / (float) seg * 4f, 1);
            m.quad(c0 * rIn, y + thick, s0 * rIn, c1 * rIn, y + thick, s1 * rIn, c1 * rOut, y + o1 + thick, s1 * rOut, c0 * rOut, y + o0 + thick, s0 * rOut, i / (float) seg * 4f, 0, (i + 1) / (float) seg * 4f, 1);
            m.quad(c0 * rOut, y + o0, s0 * rOut, c1 * rOut, y + o1, s1 * rOut, c1 * rOut, y + o1 + thick, s1 * rOut, c0 * rOut, y + o0 + thick, s0 * rOut, 0, 0, 1, 0.2f);
        }
    }

    private static float brimLift(float a, int mode, float k) {
        float c = (float) Math.cos(a), s = (float) Math.sin(a);
        return switch (mode) {
            case 1 -> k * c * c;                                          // yanlar (x) kalkik
            case 2 -> k * Math.max(0f, (float) Math.cos(3 * (a + PI / 2)));   // uc kose: on ve iki arka
            case 3 -> k * (float) Math.sin(a * 7);                         // dalgali
            case 4 -> -k * Math.max(0f, -s);                                // on (-z) asagi
            case 5 -> k * c * c * 0.6f + k * 0.2f;                            // kivrik
            default -> 0f;
        };
    }

    /** Ucgen piramit kulak: tabani w x d, yuksekligi h; ic renk on yuzde. */
    static void ear(HatMesh m, float x, float y, float z, float w, float h, float d, float tiltZ, float tiltX, int outer, int inner) {
        m.push();
        m.translate(x, y, z);
        m.rotZ(tiltZ);
        m.rotX(tiltX);
        float hw = w / 2, hd = d / 2;
        m.rgb(outer);
        m.quad(-hw, 0, -hd, hw, 0, -hd, 0, h, 0, 0, h, 0, 0, 0, 1, 1);     // on
        m.quad(hw, 0, hd, -hw, 0, hd, 0, h, 0, 0, h, 0, 0, 0, 1, 1);       // arka
        m.quad(-hw, 0, hd, -hw, 0, -hd, 0, h, 0, 0, h, 0, 0, 0, 1, 1);     // sol
        m.quad(hw, 0, -hd, hw, 0, hd, 0, h, 0, 0, h, 0, 0, 0, 1, 1);       // sag
        m.quad(-hw, 0, hd, hw, 0, hd, hw, 0, -hd, -hw, 0, -hd, 0, 0, 1, 1); // taban
        // ic yuz (onde, biraz kucuk ve one cikik)
        m.rgb(inner);
        float iw = hw * 0.62f, ih = h * 0.78f, f = -hd - 0.004f;
        m.quad(-iw, 0.02f, f + hd * 0.25f, iw, 0.02f, f + hd * 0.25f, 0, ih, f + hd * 0.65f, 0, ih, f + hd * 0.65f, 0, 0, 1, 1);
        m.pop();
    }

    /** Dort koseli parilti yildizi (iki kesisen ince elmas), cevresi sabit kalinlikli. */
    static void sparkle(HatMesh m, float x, float y, float z, float s) {
        m.push();
        m.translate(x, y, z);
        float k = s * 0.22f;
        m.quad(-s, 0, 0, 0, -k, 0, s, 0, 0, 0, k, 0, 0, 0, 1, 1);
        m.quad(0, -s, 0, k, 0, 0, 0, s, 0, -k, 0, 0, 0, 0, 1, 1);
        m.quad(0, 0, -s, 0, -k, 0, 0, 0, s, 0, k, 0, 0, 0, 1, 1);
        m.pop();
    }

    // ================================================================== 1-10

    private static void kralTaci(HatMesh m, HatCtx x) {
        int gold = x.a(), dark = x.b();
        m.layer(BASE).region(HatMesh.R_METAL).rgb(gold);
        m.frustum(0f, 0.285f, 0.12f, 0.3f, 24, false, false);
        m.frustum(0f, 0.27f, 0.12f, 0.285f, 24, false, false);
        m.rgb(dark);
        m.torus(0.005f, 0.288f, 0.014f, 24, 4);
        m.torus(0.118f, 0.302f, 0.012f, 24, 4);
        // sivri uclar ve taslar
        int[] gems = {0xE0262B, 0x2F6FE0, 0x2FC060, 0xE0262B, 0xB040E0, 0x2F6FE0, 0x2FC060, 0xB040E0};
        for (int i = 0; i < 8; i++) {
            float a = i * TAU / 8 - PI / 2;
            float hh = (i % 2 == 0) ? 0.2f : 0.13f;
            m.push();
            m.translate((float) Math.cos(a) * 0.29f, 0.12f, (float) Math.sin(a) * 0.29f);
            m.region(HatMesh.R_METAL).rgb(gold);
            m.frustum(0f, 0.05f, hh, 0.006f, 6, false, false);
            float pulse = 0.75f + 0.25f * wave(x.t, 3.1f, i * 0.8f);
            m.layer(GLOW).region(HatMesh.R_GEM).rgb(HatCtx.shade(gems[i], pulse + 0.25f));
            m.sphere(0f, hh + 0.022f, 0f, 0.028f, 4, 6);
            m.emitter(0f, hh + 0.03f, 0f);
            m.layer(BASE);
            m.pop();
        }
        // on yuz: buyuk yakut
        float pulse = 0.8f + 0.2f * wave(x.t, 2.2f, 0f);
        m.layer(GLOW).region(HatMesh.R_GEM).rgb(HatCtx.shade(x.c(), pulse + 0.2f));
        m.ellipsoid(0f, 0.06f, -0.305f, 0.04f, 0.045f, 0.018f, 4, 6);
        m.layer(BASE);
    }

    private static void kraliceTaci(HatMesh m, HatCtx x) {
        int silver = x.a(), pearl = x.b();
        m.layer(BASE).region(HatMesh.R_METAL).rgb(silver);
        m.torus(0.01f, 0.275f, 0.012f, 28, 4);
        // on kemer (ince filigran)
        for (int i = 0; i < 9; i++) {
            float a = -PI / 2 + (i - 4) * 0.2f;
            float hh = 0.06f + 0.11f * (float) Math.cos((i - 4) / 4f * PI / 2);
            float cx = (float) Math.cos(a) * 0.278f, cz = (float) Math.sin(a) * 0.278f;
            m.push();
            m.translate(cx, 0.01f, cz);
            m.region(HatMesh.R_METAL).rgb(silver);
            m.frustum(0f, 0.012f, hh, 0.006f, 5, false, false);
            m.rgb(pearl);
            m.sphere(0f, hh + 0.012f, 0f, 0.016f, 3, 6);
            m.pop();
        }
        float pulse = 0.85f + 0.15f * wave(x.t, 2.5f, 0f);
        m.layer(GLOW).region(HatMesh.R_GEM).rgb(HatCtx.shade(x.c(), pulse + 0.15f));
        m.push();
        m.translate(0f, 0.19f, -0.282f);
        m.rotZ(PI / 4);
        m.box(0f, 0f, 0f, 0.026f, 0.026f, 0.012f);
        m.pop();
        m.emitter(0f, 0.2f, -0.29f);
        m.layer(BASE);
    }

    private static void melekHalesi(HatMesh m, HatCtx x) {
        float bob = 0.03f * wave(x.t, 1.6f, 0f);
        float y = 0.3f + bob;
        m.push();
        m.translate(0f, y, 0f);
        m.rotX(0.12f + 0.04f * wave(x.t, 1.1f, 1f));
        m.layer(GLOW).region(HatMesh.R_SOLID).rgb(x.a());
        m.torus(0f, 0.26f, 0.022f, 32, 6);
        m.layer(TRANS).region(HatMesh.R_SOFT).color(0x55000000 | x.b());
        m.torus(0f, 0.26f, 0.048f, 32, 6);
        // donen yildizciklar
        m.layer(GLOW).region(HatMesh.R_SOLID).rgb(x.c());
        for (int i = 0; i < 4; i++) {
            float a = x.t * 1.4f + i * TAU / 4;
            float s = 0.022f + 0.01f * wave(x.t, 5f, i);
            sparkle(m, (float) Math.cos(a) * 0.26f, 0.02f, (float) Math.sin(a) * 0.26f, s);
            if (i % 2 == 0) m.emitter((float) Math.cos(a) * 0.26f, 0f, (float) Math.sin(a) * 0.26f);
        }
        m.pop();
        m.layer(BASE);
    }

    private static void seytanBoynuzu(HatMesh m, HatCtx x) {
        for (int side = -1; side <= 1; side += 2) {
            int n = HatMesh.bezier(P, 10, side * 0.17f, -0.02f, -0.06f, side * 0.36f, 0.16f, 0.0f, side * 0.25f, 0.36f, 0.12f);
            m.layer(BASE).region(HatMesh.R_SCALE).rgb(x.a());
            m.tube(P, n, 0.068f, 0.01f, 8);
            float pulse = 0.7f + 0.3f * wave(x.t, 2.7f, side);
            m.layer(GLOW).region(HatMesh.R_SOLID).rgb(HatCtx.shade(x.c(), pulse));
            int k = n - 3;
            int n2 = 3;
            System.arraycopy(P, k * 3, P2, 0, 9);
            m.tube(P2, n2, 0.024f, 0.004f, 6);
            m.emitter(P[(n - 1) * 3], P[(n - 1) * 3 + 1], P[(n - 1) * 3 + 2]);
        }
        m.layer(BASE);
    }

    private static void silindir(HatMesh m, HatCtx x) {
        m.push();
        m.rotZ(0.03f * wave(x.t, 1.3f, 0f));
        m.layer(BASE).region(HatMesh.R_FABRIC).rgb(x.a());
        m.brim(0f, 0.25f, 0.42f, 0.022f, 28);
        m.frustum(0.02f, 0.27f, 0.44f, 0.295f, 24, false, true);
        m.region(HatMesh.R_STRIPE).rgb(x.b());
        m.frustum(0.03f, 0.274f, 0.1f, 0.279f, 24, false, false);
        m.pop();
    }

    private static void sihirbaz(HatMesh m, HatCtx x) {
        float sway = 0.04f * wave(x.t, 1.4f, 0f) + 0.05f * x.energy;
        m.layer(BASE).region(HatMesh.R_FABRIC).rgb(x.a());
        m.brim(0f, 0.24f, 0.46f, 0.02f, 28);
        int n = HatMesh.bezier(P, 12, 0f, 0.0f, 0f, 0f, 0.42f, 0.02f, sway, 0.72f, 0.14f + sway);
        m.tube(P, n, 0.29f, 0.012f, 16);
        m.region(HatMesh.R_STRIPE).rgb(x.b());
        m.frustum(0.02f, 0.284f, 0.08f, 0.268f, 24, false, false);
        // yildizlar ve ay (kulah yuzeyinde)
        m.layer(GLOW).region(HatMesh.R_SOLID);
        float[][] at = {{0.25f, 0.6f}, {0.5f, 2.2f}, {0.68f, -0.6f}, {0.35f, 3.6f}, {0.82f, 1.4f}};
        for (int i = 0; i < at.length; i++) {
            float tt = at[i][0];
            int k = Math.min(n - 1, Math.round(tt * (n - 1)));
            float rad = 0.29f + (0.012f - 0.29f) * k / (float) (n - 1) + 0.008f;
            float a = at[i][1];
            float tw = 0.75f + 0.25f * wave(x.t, 4.2f, i * 1.7f);
            m.rgb(HatCtx.shade(x.c(), tw + 0.2f));
            m.push();
            m.translate(P[k * 3] + (float) Math.cos(a) * rad, P[k * 3 + 1], P[k * 3 + 2] + (float) Math.sin(a) * rad);
            m.rotY(-a + PI / 2);
            m.star(0f, 0f, 0f, 0.032f * tw, 0.014f * tw, 0.008f, 5);
            m.pop();
        }
        // on yuzde hilal
        m.push();
        m.translate(0f, 0.2f, -0.235f);
        m.rgb(0xFFF2B0);
        m.star(0f, 0f, 0f, 0.05f, 0.05f, 0.01f, 8);
        m.layer(BASE).rgb(x.a());
        m.star(0.022f, 0.01f, -0.004f, 0.045f, 0.045f, 0.012f, 8);
        m.pop();
        m.emitter(P[(n - 1) * 3], P[(n - 1) * 3 + 1], P[(n - 1) * 3 + 2]);
        m.layer(BASE);
    }

    private static void cadi(HatMesh m, HatCtx x) {
        float sway = 0.03f * wave(x.t, 1.1f, 0f);
        m.layer(BASE).region(HatMesh.R_FABRIC).rgb(x.a());
        m.brim(0f, 0.24f, 0.48f, 0.02f, 28);
        int n = HatMesh.bezier(P, 12, 0f, 0f, 0f, 0f, 0.55f, 0.04f, 0.04f + sway, 0.56f, 0.34f);
        m.tube(P, n, 0.28f, 0.014f, 16);
        m.region(HatMesh.R_STRIPE).rgb(x.b());
        m.frustum(0.02f, 0.275f, 0.09f, 0.262f, 24, false, false);
        m.region(HatMesh.R_METAL).rgb(x.c());
        m.box(0f, 0.055f, -0.272f, 0.045f, 0.035f, 0.008f);
        m.rgb(x.b());
        m.box(0f, 0.055f, -0.277f, 0.024f, 0.017f, 0.006f);
        m.emitter(P[(n - 1) * 3], P[(n - 1) * 3 + 1], P[(n - 1) * 3 + 2]);
    }

    private static void kovboy(HatMesh m, HatCtx x) {
        m.layer(BASE).region(HatMesh.R_FABRIC).rgb(x.a());
        brimShaped(m, 0f, 0.24f, 0.47f, 32, 1, 0.12f);
        m.push();
        m.scale(1f, 1f, 0.9f);
        m.frustum(0.01f, 0.28f, 0.2f, 0.25f, 24, false, false);
        // tepe cukuru: iki egimli yuzey
        m.frustum(0.2f, 0.25f, 0.24f, 0.1f, 24, false, true);
        m.region(HatMesh.R_STRIPE).rgb(x.b());
        m.frustum(0.02f, 0.279f, 0.065f, 0.27f, 24, false, false);
        m.pop();
        m.region(HatMesh.R_METAL).rgb(x.c());
        m.box(0f, 0.044f, -0.245f, 0.022f, 0.016f, 0.006f);
    }

    private static void korsan(HatMesh m, HatCtx x) {
        m.layer(BASE).region(HatMesh.R_FABRIC).rgb(x.a());
        dome(m, 0f, 0.29f, 0.17f, 4, 24);
        brimShaped(m, 0.0f, 0.27f, 0.44f, 36, 2, 0.22f);
        m.region(HatMesh.R_METAL).rgb(x.b());
        m.torus(0.0f, 0.44f, 0.008f, 36, 3);
        // kurukafa
        m.region(HatMesh.R_SOLID).rgb(x.c());
        m.sphere(0f, 0.12f, -0.29f, 0.045f, 4, 8);
        m.box(0f, 0.075f, -0.3f, 0.026f, 0.014f, 0.012f);
        m.rgb(0x111111);
        m.box(-0.017f, 0.125f, -0.333f, 0.009f, 0.009f, 0.003f);
        m.box(0.017f, 0.125f, -0.333f, 0.009f, 0.009f, 0.003f);
        m.rgb(x.c());
        m.push();
        m.translate(0f, 0.11f, -0.31f);
        m.rotZ(PI / 4);
        m.box(0f, 0f, 0f, 0.09f, 0.008f, 0.006f);
        m.rotZ(-PI / 2);
        m.box(0f, 0f, 0f, 0.09f, 0.008f, 0.006f);
        m.pop();
    }

    private static void asci(HatMesh m, HatCtx x) {
        float br = 1f + 0.03f * wave(x.t, 1.8f, 0f);
        m.layer(BASE).region(HatMesh.R_FABRIC).rgb(x.a());
        m.frustum(0f, 0.27f, 0.13f, 0.285f, 24, false, false);
        m.rgb(x.b());
        m.torus(0.005f, 0.272f, 0.012f, 24, 4);
        m.rgb(x.a());
        m.push();
        m.translate(0f, 0.18f, 0f);
        m.scale(br, br, br);
        for (int i = 0; i < 6; i++) {
            float a = i * TAU / 6;
            m.sphere((float) Math.cos(a) * 0.17f, 0.06f, (float) Math.sin(a) * 0.17f, 0.14f, 5, 10);
        }
        m.sphere(0f, 0.12f, 0f, 0.17f, 6, 12);
        m.pop();
    }

    private static void viking(HatMesh m, HatCtx x) {
        m.layer(BASE).region(HatMesh.R_METAL).rgb(x.a());
        dome(m, -0.04f, 0.305f, 0.24f, 5, 24);
        m.rgb(x.c());
        m.frustum(-0.04f, 0.31f, 0.02f, 0.305f, 24, false, false);
        // perçinler ve burun siperi
        for (int i = 0; i < 12; i++) {
            float a = i * TAU / 12;
            m.sphere((float) Math.cos(a) * 0.312f, -0.01f, (float) Math.sin(a) * 0.312f, 0.014f, 2, 5);
        }
        m.box(0f, -0.07f, -0.31f, 0.025f, 0.075f, 0.012f);
        for (int side = -1; side <= 1; side += 2) {
            int n = HatMesh.bezier(P, 10, side * 0.26f, 0.06f, 0f, side * 0.48f, 0.12f, -0.02f, side * 0.5f, 0.4f, -0.06f);
            m.region(HatMesh.R_SOLID).rgb(x.b());
            m.tube(P, n, 0.06f, 0.012f, 8);
        }
    }

    // ================================================================== 11-20

    private static void pervane(HatMesh m, HatCtx x) {
        int[] cols = {x.a(), x.c(), x.b(), 0x3AC060};
        m.layer(BASE).region(HatMesh.R_FABRIC);
        dome4(m, -0.02f, 0.3f, 0.2f, 4, 8, cols);
        m.rgb(0x2A2A2A);
        m.frustum(0.18f, 0.02f, 0.26f, 0.016f, 6, false, true);
        m.push();
        m.translate(0f, 0.26f, 0f);
        // sabit acisal hiz (enerjiyle carpilirsa hizlanirken faz sicrar)
        float spin = x.t * 14f;
        m.rotY(spin);
        m.region(HatMesh.R_SOLID).rgb(x.c());
        m.push();
        m.rotX(0.25f);
        m.box(0.13f, 0f, 0f, 0.12f, 0.006f, 0.03f);
        m.pop();
        m.push();
        m.rotX(-0.25f);
        m.box(-0.13f, 0f, 0f, 0.12f, 0.006f, 0.03f);
        m.pop();
        m.rgb(x.a());
        m.sphere(0f, 0.012f, 0f, 0.025f, 3, 6);
        m.pop();
    }

    private static void kediKulagi(HatMesh m, HatCtx x) {
        // her ~3.5 sn'de bir kulak hizla seger
        float cyc = x.t % 3.5f;
        float twL = cyc < 0.25f ? (float) Math.sin(cyc / 0.25f * PI) * 0.35f : 0f;
        float cyc2 = (x.t + 1.7f) % 4.1f;
        float twR = cyc2 < 0.25f ? (float) Math.sin(cyc2 / 0.25f * PI) * 0.35f : 0f;
        m.layer(BASE).region(HatMesh.R_FUR);
        ear(m, -0.15f, -0.03f, 0.0f, 0.2f, 0.25f, 0.1f, 0.28f + twL, -0.1f, x.a(), x.b());
        ear(m, 0.15f, -0.03f, 0.0f, 0.2f, 0.25f, 0.1f, -0.28f - twR, -0.1f, x.a(), x.b());
    }

    private static void tavsanKulagi(HatMesh m, HatCtx x) {
        float flopL = 0.12f * wave(x.t, 1.7f, 0f) + 0.25f * x.energy;
        float flopR = 0.12f * wave(x.t, 1.5f, 1.3f) + 0.25f * x.energy;
        m.layer(BASE).region(HatMesh.R_FUR);
        for (int side = -1; side <= 1; side += 2) {
            m.push();
            m.translate(side * 0.11f, -0.02f, 0.02f);
            m.rotZ(-side * (0.18f + (side < 0 ? flopL : flopR)));
            m.rotX(0.12f);
            m.rgb(x.a());
            m.ellipsoid(0f, 0.27f, 0f, 0.065f, 0.27f, 0.035f, 6, 10);
            m.region(HatMesh.R_SOLID).rgb(x.b());
            m.ellipsoid(0f, 0.27f, -0.026f, 0.036f, 0.22f, 0.014f, 5, 8);
            m.region(HatMesh.R_FUR);
            m.pop();
        }
    }

    private static void cicekTaci(HatMesh m, HatCtx x) {
        m.layer(BASE).region(HatMesh.R_SOLID).rgb(x.a());
        m.torus(0.02f, 0.29f, 0.022f, 28, 5);
        int[] petals = {x.b(), 0xFFFFFF, x.c(), 0xC07AFF, 0xFF7A7A};
        for (int i = 0; i < 10; i++) {
            float a = i * TAU / 10 + 0.15f;
            float cx = (float) Math.cos(a) * 0.29f, cz = (float) Math.sin(a) * 0.29f;
            m.push();
            m.translate(cx, 0.045f, cz);
            m.rotY(-a);
            m.rotZ(-0.35f + 0.08f * wave(x.t, 2f, i));
            m.rgb(petals[i % petals.length]);
            for (int k = 0; k < 5; k++) {
                float b = k * TAU / 5;
                m.ellipsoid((float) Math.cos(b) * 0.03f, 0.012f, (float) Math.sin(b) * 0.03f, 0.028f, 0.008f, 0.018f, 2, 6);
            }
            m.rgb(0xFFD23A);
            m.sphere(0f, 0.02f, 0f, 0.016f, 3, 6);
            m.pop();
            if (i % 3 == 0) m.emitter(cx, 0.07f, cz);
        }
        // yapraklar
        m.rgb(HatCtx.shade(x.a(), 1.25f));
        for (int i = 0; i < 10; i++) {
            float a = i * TAU / 10 + 0.47f;
            m.push();
            m.translate((float) Math.cos(a) * 0.3f, 0.02f, (float) Math.sin(a) * 0.3f);
            m.rotY(-a);
            m.ellipsoid(0f, 0f, 0f, 0.012f, 0.008f, 0.04f, 2, 6);
            m.pop();
        }
    }

    private static void anten(HatMesh m, HatCtx x) {
        m.layer(BASE).region(HatMesh.R_SOLID).rgb(x.b());
        m.torus(0.0f, 0.275f, 0.014f, 24, 4);
        for (int side = -1; side <= 1; side += 2) {
            float bob = 0.03f * wave(x.t, 3f, side) + 0.04f * x.energy * wave(x.t, 9f, side);
            int n = HatMesh.bezier(P, 8, side * 0.12f, 0f, 0f, side * 0.13f, 0.2f, 0.01f, side * 0.2f + bob, 0.36f, 0.03f);
            m.rgb(x.b());
            m.tube(P, n, 0.012f, 0.008f, 6);
            float on = 0.6f + 0.4f * wave(x.t, 5f, side * 1.5f);
            m.layer(GLOW).rgb(HatCtx.shade(x.c(), on + 0.15f));
            float ex = P[(n - 1) * 3], ey = P[(n - 1) * 3 + 1], ez = P[(n - 1) * 3 + 2];
            m.sphere(ex, ey + 0.02f, ez, 0.038f, 5, 8);
            m.layer(TRANS).region(HatMesh.R_SOFT).color(0x50000000 | x.c());
            m.sphere(ex, ey + 0.02f, ez, 0.06f, 4, 8);
            m.layer(BASE).region(HatMesh.R_SOLID);
            m.emitter(ex, ey + 0.03f, ez);
        }
    }

    private static void noel(HatMesh m, HatCtx x) {
        float swing = 0.15f * wave(x.t, 2f, 0f) + 0.2f * x.energy;
        m.layer(BASE).region(HatMesh.R_FABRIC).rgb(x.a());
        int n = HatMesh.bezier(P, 12, 0f, 0f, 0f, 0f, 0.36f, 0.06f, 0.22f + swing * 0.3f, 0.32f, 0.26f);
        m.tube(P, n, 0.28f, 0.03f, 16);
        m.region(HatMesh.R_FUR).rgb(x.b());
        m.torus(0.03f, 0.29f, 0.055f, 28, 6);
        m.sphere(P[(n - 1) * 3], P[(n - 1) * 3 + 1] - 0.03f, P[(n - 1) * 3 + 2], 0.065f, 5, 8);
        m.emitter(P[(n - 1) * 3], P[(n - 1) * 3 + 1], P[(n - 1) * 3 + 2]);
    }

    private static void beyzbol(HatMesh m, HatCtx x) {
        m.layer(BASE).region(HatMesh.R_FABRIC).rgb(x.a());
        dome(m, -0.02f, 0.295f, 0.19f, 5, 24);
        m.rgb(x.b());
        m.sphere(0f, 0.168f, 0f, 0.022f, 3, 6);
        // siperlik (onde yarim halka)
        m.rgb(x.a());
        int seg = 12;
        for (int i = 0; i < seg; i++) {
            float a0 = -PI / 2 - 1.15f + i * 2.3f / seg, a1 = -PI / 2 - 1.15f + (i + 1) * 2.3f / seg;
            float c0 = (float) Math.cos(a0), s0 = (float) Math.sin(a0), c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);
            float rOut0 = 0.29f + 0.21f * (float) Math.cos((a0 + PI / 2) * 1.2f), rOut1 = 0.29f + 0.21f * (float) Math.cos((a1 + PI / 2) * 1.2f);
            float y = -0.01f;
            m.quad(c0 * 0.285f, y, s0 * 0.285f, c0 * rOut0, y - 0.03f, s0 * rOut0, c1 * rOut1, y - 0.03f, s1 * rOut1, c1 * 0.285f, y, s1 * 0.285f, 0, 0, 1, 1);
            m.quad(c0 * 0.285f, y + 0.012f, s0 * 0.285f, c1 * 0.285f, y + 0.012f, s1 * 0.285f, c1 * rOut1, y - 0.018f, s1 * rOut1, c0 * rOut0, y - 0.018f, s0 * rOut0, 0, 0, 1, 1);
        }
        m.rgb(x.c());
        m.push();
        m.translate(0f, 0.08f, -0.282f);
        m.star(0f, 0f, 0f, 0.045f, 0.02f, 0.01f, 5);
        m.pop();
    }

    private static void bere(HatMesh m, HatCtx x) {
        m.layer(BASE).region(HatMesh.R_KNIT).rgb(x.a());
        dome(m, 0.04f, 0.29f, 0.22f, 5, 24);
        m.rgb(HatCtx.shade(x.a(), 0.85f));
        m.frustum(-0.04f, 0.305f, 0.06f, 0.3f, 24, false, false);
        float bob = 0.02f * wave(x.t, 2.4f, 0f) + 0.02f * x.energy;
        m.region(HatMesh.R_FUR).rgb(x.b());
        m.sphere(0f, 0.3f + bob, 0f, 0.08f, 5, 9);
    }

    private static void mezuniyet(HatMesh m, HatCtx x) {
        m.layer(BASE).region(HatMesh.R_FABRIC).rgb(x.a());
        m.frustum(-0.01f, 0.28f, 0.1f, 0.27f, 24, false, false);
        m.push();
        m.translate(0f, 0.12f, 0f);
        m.rotY(PI / 4);
        m.rotX(-0.06f);
        m.box(0f, 0f, 0f, 0.31f, 0.012f, 0.31f);
        m.rgb(x.b());
        m.sphere(0f, 0.016f, 0f, 0.02f, 3, 6);
        m.pop();
        // puskul: merkezden kenara, sonra sarkar ve sallanir
        float sw = 0.12f * wave(x.t, 2.1f, 0f) + 0.25f * x.energy;
        P[0] = 0f; P[1] = 0.136f; P[2] = 0f;
        P[3] = 0.3f; P[4] = 0.136f; P[5] = 0f;
        P[6] = 0.32f; P[7] = 0.04f; P[8] = sw * 0.12f;
        P[9] = 0.32f + sw * 0.04f; P[10] = -0.05f; P[11] = sw * 0.2f;
        m.region(HatMesh.R_SOLID).rgb(x.b());
        m.tube(P, 4, 0.006f, 0.006f, 4);
        m.push();
        m.translate(P[9], P[10], P[11]);
        m.frustum(-0.07f, 0.024f, 0f, 0.01f, 6, true, false);
        m.pop();
    }

    // ================================================================== 21-30

    private static void fes(HatMesh m, HatCtx x) {
        m.layer(BASE).region(HatMesh.R_FABRIC).rgb(x.a());
        m.frustum(-0.02f, 0.25f, 0.24f, 0.2f, 20, false, true);
        float sw = 0.14f * wave(x.t, 2.2f, 0f) + 0.3f * x.energy;
        P[0] = 0f; P[1] = 0.245f; P[2] = 0f;
        P[3] = 0.05f; P[4] = 0.25f; P[5] = 0.06f;
        P[6] = 0.17f; P[7] = 0.2f; P[8] = 0.12f + sw * 0.05f;
        P[9] = 0.2f + sw * 0.05f; P[10] = 0.08f; P[11] = 0.14f + sw * 0.1f;
        m.region(HatMesh.R_SOLID).rgb(x.b());
        m.tube(P, 4, 0.008f, 0.008f, 4);
        m.push();
        m.translate(P[9], P[10], P[11]);
        m.frustum(-0.08f, 0.03f, 0f, 0.01f, 6, true, false);
        m.pop();
    }

    private static void samuray(HatMesh m, HatCtx x) {
        m.layer(BASE).region(HatMesh.R_METAL).rgb(x.a());
        dome(m, 0f, 0.3f, 0.21f, 5, 24);
        // ense korumasi (arka yarim)
        for (int i = 0; i < 12; i++) {
            float a0 = i * PI / 12, a1 = (i + 1) * PI / 12;
            float c0 = (float) Math.cos(a0), s0 = (float) Math.sin(a0), c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);
            for (int k = 0; k < 3; k++) {
                float y0 = 0.02f - k * 0.07f, y1 = y0 - 0.07f, ra = 0.3f + k * 0.035f, rb = ra + 0.035f;
                m.rgb(k % 2 == 0 ? x.a() : x.c());
                m.quad(c0 * ra, y0, s0 * ra, c1 * ra, y0, s1 * ra, c1 * rb, y1, s1 * rb, c0 * rb, y1, s0 * rb, 0, 0, 1, 1);
            }
        }
        // altin hilal arma (on)
        m.layer(GLOW).rgb(HatCtx.shade(x.b(), 0.95f));
        for (int side = -1; side <= 1; side += 2) {
            int n = HatMesh.bezier(P, 8, 0f, 0.1f, -0.3f, side * 0.14f, 0.16f, -0.36f, side * 0.24f, 0.38f, -0.3f);
            m.tube(P, n, 0.022f, 0.006f, 5);
        }
        m.layer(BASE).rgb(x.b());
        m.sphere(0f, 0.1f, -0.302f, 0.03f, 3, 6);
    }

    private static void firavun(HatMesh m, HatCtx x) {
        int g = x.a(), bl = x.b();
        m.layer(BASE).region(HatMesh.R_SOLID);
        // cizgili kubbe
        int lon = 24, lat = 4;
        for (int j = 0; j < lon; j++) {
            float b0 = j * TAU / lon, b1 = (j + 1) * TAU / lon;
            for (int i = 0; i < lat; i++) {
                m.rgb((i % 2 == 0) == (j % 2 == 0) ? g : bl);
                float a0 = i / (float) lat * PI / 2, a1 = (i + 1) / (float) lat * PI / 2;
                float r0 = 0.3f * (float) Math.cos(a0), r1 = i == lat - 1 ? 0f : 0.3f * (float) Math.cos(a1);
                float y0 = -0.02f + 0.17f * (float) Math.sin(a0), y1 = -0.02f + 0.17f * (float) Math.sin(a1);
                float c0 = (float) Math.cos(b0), s0 = (float) Math.sin(b0), c1 = (float) Math.cos(b1), s1 = (float) Math.sin(b1);
                m.quad(c0 * r0, y0, s0 * r0, c1 * r0, y0, s1 * r0, c1 * r1, y1, s1 * r1, c0 * r1, y1, s0 * r1, 0, 0, 1, 1);
            }
        }
        // yan sarkan kumaslar (cizgili)
        for (int side = -1; side <= 1; side += 2) {
            for (int k = 0; k < 6; k++) {
                m.rgb(k % 2 == 0 ? g : bl);
                float y0 = -0.02f - k * 0.05f;
                m.box(side * 0.29f, y0 - 0.025f, -0.06f, 0.025f, 0.025f, 0.12f);
            }
        }
        m.rgb(g);
        m.frustum(-0.04f, 0.31f, 0.0f, 0.305f, 24, false, false);
        // kobra
        m.layer(GLOW).rgb(HatCtx.shade(x.c(), 1.1f));
        int n = HatMesh.bezier(P, 7, 0f, 0.0f, -0.31f, 0f, 0.1f, -0.36f, 0f, 0.16f, -0.31f);
        m.tube(P, n, 0.02f, 0.012f, 5);
        m.sphere(0f, 0.17f, -0.31f, 0.024f, 3, 6);
        m.emitter(0f, 0.18f, -0.32f);
        m.layer(BASE);
    }

    private static void neonHale(HatMesh m, HatCtx x) {
        float bob = 0.02f * wave(x.t, 1.8f, 0f);
        m.layer(GLOW).region(HatMesh.R_SOLID);
        for (int ring = 0; ring < 2; ring++) {
            m.push();
            m.translate(0f, 0.3f + bob + ring * 0.06f, 0f);
            m.rotY((ring == 0 ? 1 : -1) * x.t * 1.2f);
            m.rotX(ring == 0 ? 0.18f : -0.12f);
            float R = ring == 0 ? 0.27f : 0.22f;
            int seg = 32;
            for (int i = 0; i < seg; i++) {
                float a0 = i * TAU / seg, a1 = (i + 1) * TAU / seg;
                int ca = HatCtx.hsv(x.t * 0.3f + i / (float) seg + ring * 0.5f, 0.85f, 1f) | 0xFF000000;
                int cb = HatCtx.hsv(x.t * 0.3f + (i + 1) / (float) seg + ring * 0.5f, 0.85f, 1f) | 0xFF000000;
                float r = 0.016f;
                for (int k = 0; k < 4; k++) {
                    float b0 = k * TAU / 4, b1 = (k + 1) * TAU / 4;
                    float q0 = R + r * (float) Math.cos(b0), q1 = R + r * (float) Math.cos(b1);
                    float y0 = r * (float) Math.sin(b0), y1 = r * (float) Math.sin(b1);
                    m.quadC((float) Math.cos(a0) * q0, y0, (float) Math.sin(a0) * q0, (float) Math.cos(a1) * q0, y0, (float) Math.sin(a1) * q0,
                            (float) Math.cos(a1) * q1, y1, (float) Math.sin(a1) * q1, (float) Math.cos(a0) * q1, y1, (float) Math.sin(a0) * q1,
                            0, 0, 1, 1, ca, cb, cb, ca);
                }
            }
            if (ring == 0) m.emitter(R, 0f, 0f);
            m.pop();
        }
        m.layer(BASE);
    }

    private static void kristalTac(HatMesh m, HatCtx x) {
        float pulse = 0.7f + 0.3f * wave(x.t, 2f, 0f);
        int alpha = Math.round(140 + 60 * pulse) << 24;
        m.layer(TRANS).region(HatMesh.R_GEM).color(alpha | x.b());
        m.frustum(0f, 0.285f, 0.07f, 0.29f, 24, false, false);
        for (int i = 0; i < 7; i++) {
            float a = -PI / 2 + (i - 3) * 0.48f;
            float hh = 0.12f + 0.12f * (float) Math.cos((i - 3) / 3f * PI / 2);
            m.push();
            m.translate((float) Math.cos(a) * 0.287f, 0.06f, (float) Math.sin(a) * 0.287f);
            m.rotY(-a);
            m.rotZ(0.12f * (i - 3) / 3f);
            m.color(alpha | x.a());
            m.frustum(0f, 0.0f, hh * 0.25f, 0.045f, 4, false, false);
            m.frustum(hh * 0.25f, 0.045f, hh, 0.0f, 4, false, false);
            m.layer(GLOW).region(HatMesh.R_SOLID).rgb(HatCtx.shade(x.c(), pulse));
            m.frustum(hh * 0.2f, 0.012f, hh * 0.8f, 0.004f, 4, false, false);
            m.layer(TRANS).region(HatMesh.R_GEM);
            if (i % 2 == 0) m.emitter(0f, hh, 0f);
            m.pop();
        }
        m.layer(BASE);
    }

    private static void atesTaci(HatMesh m, HatCtx x) {
        m.layer(BASE).region(HatMesh.R_METAL).rgb(x.c());
        m.frustum(0f, 0.285f, 0.07f, 0.29f, 24, false, false);
        for (int i = 0; i < 9; i++) {
            float a = i * TAU / 9;
            float fl = 0.75f + 0.25f * wave(x.t, 7f + i * 0.3f, i * 2.1f) + 0.1f * wave(x.t, 13f, i);
            float hh = (0.14f + 0.08f * ((i * 37) % 5) / 4f) * fl;
            float cx = (float) Math.cos(a) * 0.27f, cz = (float) Math.sin(a) * 0.27f;
            float lean = 0.03f * wave(x.t, 5f, i);
            int n = HatMesh.bezier(P, 6, cx, 0.05f, cz, cx * 1.02f + lean, 0.05f + hh * 0.5f, cz * 1.02f, cx * 0.92f - lean, 0.05f + hh, cz * 0.92f);
            m.layer(GLOW).region(HatMesh.R_SOLID).rgb(x.a());
            m.tube(P, n, 0.05f, 0.004f, 6);
            m.rgb(x.b());
            m.tube(P, n - 1, 0.028f, 0.006f, 5);
            if (i % 2 == 0) m.emitter(P[(n - 1) * 3], P[(n - 1) * 3 + 1], P[(n - 1) * 3 + 2]);
        }
        m.layer(BASE);
    }

    private static void buzTaci(HatMesh m, HatCtx x) {
        m.layer(TRANS).region(HatMesh.R_GEM).color(0xC0000000 | x.a());
        m.frustum(0f, 0.285f, 0.06f, 0.29f, 24, false, false);
        for (int i = 0; i < 11; i++) {
            float a = i * TAU / 11 + 0.2f;
            float hh = 0.08f + 0.16f * (((i * 53) % 7) / 6f);
            m.push();
            m.translate((float) Math.cos(a) * 0.285f, 0.05f, (float) Math.sin(a) * 0.285f);
            m.rotY(-a);
            m.rotZ(0.25f * (((i * 29) % 5) / 4f - 0.5f));
            m.rotX(-0.15f);
            m.color(0xB8000000 | (i % 2 == 0 ? x.b() : x.a()));
            m.frustum(0f, 0.035f, hh, 0.0f, 4, false, false);
            m.pop();
        }
        m.layer(GLOW).region(HatMesh.R_SOLID).rgb(x.c());
        for (int i = 0; i < 5; i++) {
            float vis = wave(x.t, 3f, i * 1.9f);
            if (vis < 0.3f) continue;
            float a = i * TAU / 5 + 0.5f;
            sparkle(m, (float) Math.cos(a) * 0.3f, 0.12f + 0.1f * ((i * 3) % 4) / 3f, (float) Math.sin(a) * 0.3f, 0.02f * vis);
            m.emitter((float) Math.cos(a) * 0.3f, 0.2f, (float) Math.sin(a) * 0.3f);
        }
        m.layer(BASE);
    }

    private static void yildizYorunge(HatMesh m, HatCtx x) {
        m.layer(TRANS).region(HatMesh.R_SOFT).color(0x40000000 | x.b());
        m.push();
        m.translate(0f, 0.22f, 0f);
        m.rotX(0.2f);
        m.torus(0f, 0.38f, 0.006f, 32, 3);
        m.layer(GLOW).region(HatMesh.R_SOLID);
        for (int i = 0; i < 5; i++) {
            float a = x.t * 1.1f + i * TAU / 5;
            float sx = (float) Math.cos(a) * 0.38f, sz = (float) Math.sin(a) * 0.38f;
            m.push();
            m.translate(sx, 0.02f * wave(x.t, 3f, i), sz);
            m.rotY(x.t * 2.5f + i);
            float s = 0.05f + 0.01f * wave(x.t, 4f, i);
            m.rgb(i % 2 == 0 ? x.a() : x.c());
            m.star(0f, 0f, 0f, s, s * 0.45f, 0.016f, 5);
            m.pop();
            m.emitter(sx, 0f, sz);
        }
        m.pop();
        m.layer(BASE);
    }

    private static void saturn(HatMesh m, HatCtx x) {
        float bob = 0.03f * wave(x.t, 1.5f, 0f);
        m.push();
        m.translate(0f, 0.34f + bob, 0f);
        m.rotY(x.t * 0.6f);
        m.layer(BASE).region(HatMesh.R_STRIPE).rgb(x.a());
        m.sphere(0f, 0f, 0f, 0.11f, 7, 12);
        m.push();
        m.rotX(0.45f);
        m.layer(TRANS).region(HatMesh.R_STRIPE).color(0xC0000000 | x.b());
        m.disc(0f, 0.15f, 0.24f, 32);
        m.pop();
        float a = x.t * 2f;
        m.layer(BASE).region(HatMesh.R_SOLID).rgb(x.c());
        m.sphere((float) Math.cos(a) * 0.3f, 0.06f * (float) Math.sin(a * 0.5f), (float) Math.sin(a) * 0.3f, 0.03f, 4, 6);
        m.pop();
    }

    // ================================================================== 31-40

    private static void cloud(HatMesh m, float cx, float cy, float cz, float s, int c, int shadow) {
        m.rgb(c);
        m.sphere(cx, cy, cz, 0.1f * s, 5, 9);
        m.sphere(cx + 0.1f * s, cy - 0.02f * s, cz, 0.08f * s, 5, 9);
        m.sphere(cx - 0.1f * s, cy - 0.025f * s, cz + 0.01f * s, 0.075f * s, 5, 9);
        m.sphere(cx + 0.03f * s, cy - 0.02f * s, cz + 0.07f * s, 0.075f * s, 5, 9);
        m.rgb(shadow);
        m.sphere(cx - 0.04f * s, cy - 0.03f * s, cz - 0.06f * s, 0.07f * s, 5, 9);
    }

    private static void bulut(HatMesh m, HatCtx x) {
        float bob = 0.035f * wave(x.t, 1.3f, 0f);
        m.layer(BASE).region(HatMesh.R_FUR);
        cloud(m, 0f, 0.36f + bob, 0f, 1.6f, x.a(), x.b());
        for (int i = 0; i < 4; i++) m.emitter(-0.12f + i * 0.08f, 0.24f + bob, -0.03f + (i % 2) * 0.06f);
    }

    private static void gokkusagiBulut(HatMesh m, HatCtx x) {
        float bob = 0.02f * wave(x.t, 1.4f, 0f);
        m.layer(BASE).region(HatMesh.R_FUR);
        cloud(m, -0.3f, 0.12f + bob, 0f, 0.9f, x.a(), x.b());
        cloud(m, 0.3f, 0.12f - bob, 0f, 0.9f, x.a(), x.b());
        int[] rb = {0xFF3A3A, 0xFF9A2A, 0xFFE03A, 0x3AD860, 0x3A8AFF, 0x9A4AFF};
        m.layer(GLOW).region(HatMesh.R_SOLID);
        for (int band = 0; band < 6; band++) {
            float R = 0.33f - band * 0.025f;
            int n = 0;
            for (int i = 0; i <= 12; i++) {
                float a = i * PI / 12;
                P[n * 3] = (float) Math.cos(a) * R;
                P[n * 3 + 1] = 0.12f + (float) Math.sin(a) * R;
                P[n * 3 + 2] = 0f;
                n++;
            }
            float glow = 0.85f + 0.15f * wave(x.t, 2f, band * 0.6f);
            m.rgb(HatCtx.shade(rb[band], glow));
            m.tube(P, n, 0.014f, 0.014f, 4);
        }
        m.emitter(0f, 0.45f, 0f);
        m.layer(BASE);
    }

    private static void kurbaga(HatMesh m, HatCtx x) {
        m.layer(BASE).region(HatMesh.R_SCALE).rgb(x.a());
        dome(m, -0.02f, 0.3f, 0.18f, 5, 24);
        float cyc = x.t % 4f;
        float blink = cyc < 0.15f ? 0.15f : 1f;
        for (int side = -1; side <= 1; side += 2) {
            m.region(HatMesh.R_SCALE).rgb(x.a());
            m.sphere(side * 0.12f, 0.17f, -0.14f, 0.07f, 5, 9);
            m.region(HatMesh.R_SOLID).rgb(x.b());
            m.ellipsoid(side * 0.12f, 0.18f, -0.19f, 0.05f, 0.05f * blink, 0.03f, 4, 8);
            m.rgb(x.c());
            m.ellipsoid(side * 0.12f, 0.18f, -0.215f, 0.022f, 0.026f * blink, 0.012f, 3, 6);
        }
        m.rgb(HatCtx.shade(x.a(), 0.6f));
        m.box(0f, 0.08f, -0.285f, 0.12f, 0.006f, 0.006f);
    }

    private static void mantar(HatMesh m, HatCtx x) {
        m.layer(BASE).region(HatMesh.R_SOLID).rgb(x.c());
        m.frustum(-0.02f, 0.26f, 0.06f, 0.24f, 20, false, false);
        m.region(HatMesh.R_FABRIC).rgb(x.a());
        dome(m, 0.05f, 0.4f, 0.22f, 6, 28);
        m.rgb(HatCtx.shade(x.c(), 0.85f));
        m.disc(0.05f, 0.18f, 0.4f, 28);
        m.region(HatMesh.R_SOLID).rgb(x.b());
        float[][] dots = {{0f, 0.25f, 0f, 0.05f}, {0.2f, 0.17f, 0.12f, 0.04f}, {-0.18f, 0.17f, -0.14f, 0.045f}, {0.05f, 0.15f, -0.26f, 0.04f},
                {-0.22f, 0.13f, 0.18f, 0.035f}, {0.28f, 0.1f, -0.1f, 0.035f}, {-0.3f, 0.1f, -0.04f, 0.03f}, {0.1f, 0.12f, 0.28f, 0.035f}};
        for (float[] d : dots) m.ellipsoid(d[0], d[1], d[2], d[3], d[3] * 0.4f, d[3], 3, 7);
        m.emitter(0f, 0.3f, 0f);
    }

    private static void ordek(HatMesh m, HatCtx x) {
        float bob = 0.02f * wave(x.t, 3f, 0f) + 0.03f * x.energy * wave(x.t, 10f, 0f);
        m.push();
        m.translate(0f, 0.02f + bob, 0.02f);
        m.rotY(0.25f * wave(x.t, 0.7f, 0f));
        m.layer(BASE).region(HatMesh.R_SOLID).rgb(x.a());
        m.ellipsoid(0f, 0.09f, 0.03f, 0.16f, 0.1f, 0.2f, 6, 12);
        m.ellipsoid(0f, 0.11f, 0.2f, 0.06f, 0.04f, 0.05f, 4, 8);   // kuyruk
        m.sphere(0f, 0.23f, -0.1f, 0.095f, 6, 12);
        m.rgb(x.b());
        m.ellipsoid(0f, 0.21f, -0.205f, 0.05f, 0.018f, 0.04f, 3, 8);
        m.rgb(x.c());
        m.sphere(-0.05f, 0.26f, -0.17f, 0.016f, 3, 6);
        m.sphere(0.05f, 0.26f, -0.17f, 0.016f, 3, 6);
        m.pop();
    }

    private static void panda(HatMesh m, HatCtx x) {
        m.layer(BASE).region(HatMesh.R_FUR).rgb(x.a());
        m.torus(0.0f, 0.27f, 0.014f, 24, 4);
        for (int side = -1; side <= 1; side += 2) {
            float wig = 0.05f * wave(x.t, 2f, side);
            m.push();
            m.translate(side * 0.19f, 0.02f, 0.02f);
            m.rotZ(-side * (0.3f + wig));
            m.ellipsoid(0f, 0.08f, 0f, 0.085f, 0.085f, 0.04f, 5, 9);
            m.rgb(x.b());
            m.ellipsoid(0f, 0.08f, -0.03f, 0.045f, 0.045f, 0.015f, 3, 8);
            m.rgb(x.a());
            m.pop();
        }
    }

    private static void tilki(HatMesh m, HatCtx x) {
        float cyc = x.t % 3.8f;
        float tw = cyc < 0.25f ? (float) Math.sin(cyc / 0.25f * PI) * 0.3f : 0f;
        m.layer(BASE).region(HatMesh.R_FUR);
        for (int side = -1; side <= 1; side += 2) {
            ear(m, side * 0.14f, -0.03f, 0f, 0.2f, 0.3f, 0.1f, -side * (0.24f + (side > 0 ? tw : 0f)), -0.08f, x.a(), x.b());
            // siyah uc
            m.push();
            m.translate(side * 0.14f, -0.03f, 0f);
            m.rotZ(-side * (0.24f + (side > 0 ? tw : 0f)));
            m.rotX(-0.08f);
            m.rgb(x.c());
            m.frustum(0.21f, 0.04f, 0.305f, 0.0f, 4, false, false);
            m.pop();
        }
    }

    private static void ejderBoynuz(HatMesh m, HatCtx x) {
        for (int side = -1; side <= 1; side += 2) {
            int n = 14;
            for (int i = 0; i < n; i++) {
                float t = i / (float) (n - 1);
                float spiral = (float) Math.sin(t * 3f * PI) * 0.03f * (1 - t);
                P[i * 3] = side * (0.15f + 0.12f * t + spiral);
                P[i * 3 + 1] = 0.02f + 0.24f * (float) Math.sin(t * PI * 0.6f) + 0.06f * t;
                P[i * 3 + 2] = -0.02f + 0.42f * t * t + (float) Math.cos(t * 3f * PI) * 0.02f * (1 - t);
            }
            m.layer(BASE).region(HatMesh.R_SCALE).rgb(x.a());
            m.tube(P, n, 0.06f, 0.008f, 8);
            float pulse = 0.7f + 0.3f * wave(x.t, 2.4f, side);
            m.layer(GLOW).region(HatMesh.R_SOLID).rgb(HatCtx.shade(x.b(), pulse));
            System.arraycopy(P, (n - 4) * 3, P2, 0, 12);
            m.tube(P2, 4, 0.022f, 0.004f, 5);
            m.emitter(P[(n - 1) * 3], P[(n - 1) * 3 + 1], P[(n - 1) * 3 + 2]);
        }
        // sirt dikenleri
        m.layer(BASE).region(HatMesh.R_SCALE).rgb(HatCtx.shade(x.a(), 1.3f));
        for (int i = 0; i < 4; i++) {
            float z = -0.12f + i * 0.1f;
            float y = (float) Math.sqrt(Math.max(0, 0.3f * 0.3f - z * z)) * 0.18f;
            m.push();
            m.translate(0f, -0.01f + y * 0.2f, z);
            m.rotX(0.5f);
            m.frustum(0f, 0.035f, 0.12f - i * 0.015f, 0f, 4, false, false);
            m.pop();
        }
        m.layer(BASE);
    }

    private static void geyik(HatMesh m, HatCtx x) {
        m.layer(BASE).region(HatMesh.R_FABRIC).rgb(x.a());
        for (int side = -1; side <= 1; side += 2) {
            int n = HatMesh.bezier(P, 9, side * 0.12f, -0.01f, 0.02f, side * 0.26f, 0.2f, 0.06f, side * 0.34f, 0.46f, 0.1f);
            m.rgb(x.a());
            m.tube(P, n, 0.03f, 0.012f, 6);
            // catallar
            for (int k = 0; k < 3; k++) {
                int b = 2 + k * 2;
                float bx = P[b * 3], by = P[b * 3 + 1], bz = P[b * 3 + 2];
                int n2 = HatMesh.bezier(P2, 5, bx, by, bz, bx + side * 0.03f, by + 0.07f, bz - 0.06f, bx + side * (0.02f - k * 0.02f), by + 0.15f - k * 0.02f,
                        bz - 0.12f + k * 0.02f);
                m.rgb(x.b());
                m.tube(P2, n2, 0.016f, 0.006f, 5);
            }
        }
    }

    private static void unicorn(HatMesh m, HatCtx x) {
        float tw = x.t * 0.4f;
        int seg = 8, steps = 12;
        float h = 0.5f, r0 = 0.075f;
        m.push();
        m.translate(0f, 0.0f, -0.16f);
        m.rotX(-0.32f);
        m.layer(GLOW).region(HatMesh.R_SOLID);
        for (int i = 0; i < steps; i++) {
            float t0 = i / (float) steps, t1 = (i + 1) / (float) steps;
            float ra = r0 * (1 - t0), rb = r0 * (1 - t1);
            float ya = h * t0, yb = h * t1;
            int ca = HatCtx.hsv(t0 * 0.8f - x.t * 0.25f, 0.55f, 1f) | 0xFF000000, cb = HatCtx.hsv(t1 * 0.8f - x.t * 0.25f, 0.55f, 1f) | 0xFF000000;
            for (int j = 0; j < seg; j++) {
                float a0 = j * TAU / seg + t0 * 6f, a1 = (j + 1) * TAU / seg + t0 * 6f;
                float b0 = j * TAU / seg + t1 * 6f, b1 = (j + 1) * TAU / seg + t1 * 6f;
                // burgu: cift dilimler daha koyu
                int k0 = (j % 2 == 0) ? ca : (HatCtx.shade(ca & 0xFFFFFF, 0.8f) | 0xFF000000);
                int k1 = (j % 2 == 0) ? cb : (HatCtx.shade(cb & 0xFFFFFF, 0.8f) | 0xFF000000);
                m.quadC((float) Math.cos(a0) * ra, ya, (float) Math.sin(a0) * ra, (float) Math.cos(a1) * ra, ya, (float) Math.sin(a1) * ra,
                        (float) Math.cos(b1) * rb, yb, (float) Math.sin(b1) * rb, (float) Math.cos(b0) * rb, yb, (float) Math.sin(b0) * rb, 0, 0, 1, 1, k0, k0, k1, k1);
            }
        }
        m.emitter(0f, h, 0f);
        m.layer(GLOW).rgb(x.c());
        for (int i = 0; i < 3; i++) {
            float a = tw * 3f + i * TAU / 3;
            float vis = 0.5f + 0.5f * wave(x.t, 3.3f, i);
            sparkle(m, (float) Math.cos(a) * 0.1f, 0.12f + i * 0.11f, (float) Math.sin(a) * 0.1f, 0.02f + 0.012f * vis);
        }
        m.pop();
        m.layer(BASE);
    }

    private static void sovalyeTuyu(HatMesh m, HatCtx x) {
        m.layer(BASE).region(HatMesh.R_METAL).rgb(x.c());
        m.frustum(-0.01f, 0.06f, 0.06f, 0.04f, 8, false, true);
        for (int i = 0; i < 5; i++) {
            float side = (i - 2) * 0.03f;
            float sw = 0.06f * wave(x.t, 2.6f, i * 0.7f) + 0.12f * x.energy;
            int n = HatMesh.bezier(P, 10, side, 0.05f, 0f, side * 2f, 0.32f, 0.04f, side * 2.4f, 0.28f + i % 2 * 0.03f, 0.4f + sw);
            m.region(HatMesh.R_FUR).rgb(i % 2 == 0 ? x.a() : x.b());
            m.tube(P, n, 0.04f, 0.012f, 6);
        }
    }

    // ================================================================== 41-50

    private static void polis(HatMesh m, HatCtx x) {
        m.layer(BASE).region(HatMesh.R_FABRIC).rgb(x.a());
        m.frustum(-0.02f, 0.28f, 0.13f, 0.33f, 24, false, false);
        m.frustum(0.13f, 0.33f, 0.15f, 0.31f, 24, false, true);
        m.region(HatMesh.R_SOLID).rgb(x.b());
        m.frustum(-0.02f, 0.285f, 0.04f, 0.292f, 24, false, false);
        int seg = 10;
        for (int i = 0; i < seg; i++) {
            float a0 = -PI / 2 - 0.9f + i * 1.8f / seg, a1 = -PI / 2 - 0.9f + (i + 1) * 1.8f / seg;
            float c0 = (float) Math.cos(a0), s0 = (float) Math.sin(a0), c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);
            m.quad(c0 * 0.28f, -0.015f, s0 * 0.28f, c0 * 0.42f, -0.06f, s0 * 0.42f, c1 * 0.42f, -0.06f, s1 * 0.42f, c1 * 0.28f, -0.015f, s1 * 0.28f, 0, 0, 1, 1);
        }
        float shine = 0.85f + 0.15f * wave(x.t, 2f, 0f);
        m.layer(GLOW).rgb(HatCtx.shade(x.c(), shine));
        m.push();
        m.translate(0f, 0.08f, -0.31f);
        m.rotX(0.15f);
        m.star(0f, 0f, 0f, 0.05f, 0.025f, 0.01f, 6);
        m.pop();
        m.layer(BASE);
    }

    private static void denizci(HatMesh m, HatCtx x) {
        m.layer(BASE).region(HatMesh.R_FABRIC).rgb(x.a());
        m.frustum(0.0f, 0.27f, 0.1f, 0.33f, 24, false, false);
        m.frustum(0.1f, 0.33f, 0.14f, 0.32f, 24, false, true);
        m.region(HatMesh.R_STRIPE).rgb(x.b());
        m.frustum(-0.01f, 0.272f, 0.05f, 0.29f, 24, false, false);
        m.region(HatMesh.R_SOLID).rgb(x.c());
        m.sphere(0f, 0.03f, -0.29f, 0.02f, 3, 6);
        for (int side = -1; side <= 1; side += 2) {
            float sw = 0.1f * wave(x.t, 2.3f, side) + 0.25f * x.energy;
            m.push();
            m.translate(side * 0.05f, 0.02f, 0.27f);
            m.rotX(0.25f + sw);
            m.rotZ(side * 0.15f);
            m.rgb(x.b());
            m.box(0f, -0.1f, 0f, 0.02f, 0.1f, 0.004f);
            m.pop();
        }
    }

    private static void fotr(HatMesh m, HatCtx x) {
        m.layer(BASE).region(HatMesh.R_FABRIC).rgb(x.a());
        brimShaped(m, 0f, 0.25f, 0.44f, 32, 4, 0.05f);
        m.frustum(0.01f, 0.275f, 0.18f, 0.245f, 24, false, false);
        // ortasi cokuk tepe
        int seg = 24;
        for (int i = 0; i < seg; i++) {
            float a0 = i * TAU / seg, a1 = (i + 1) * TAU / seg;
            float c0 = (float) Math.cos(a0), s0 = (float) Math.sin(a0), c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);
            float d0 = 0.2f - 0.035f * c0 * c0, d1 = 0.2f - 0.035f * c1 * c1;
            m.quad(c0 * 0.245f, 0.18f, s0 * 0.245f, c1 * 0.245f, 0.18f, s1 * 0.245f, c1 * 0.12f, d1, s1 * 0.12f, c0 * 0.12f, d0, s0 * 0.12f, 0, 0, 1, 1);
            m.quad(c0 * 0.12f, d0, s0 * 0.12f, c1 * 0.12f, d1, s1 * 0.12f, 0, 0.17f, 0, 0, 0.17f, 0, 0, 0, 1, 1);
        }
        m.region(HatMesh.R_STRIPE).rgb(x.b());
        m.frustum(0.015f, 0.276f, 0.07f, 0.265f, 24, false, false);
    }

    private static void melon(HatMesh m, HatCtx x) {
        m.layer(BASE).region(HatMesh.R_FABRIC).rgb(x.a());
        brimShaped(m, 0f, 0.26f, 0.38f, 28, 5, 0.05f);
        dome(m, 0.01f, 0.285f, 0.22f, 6, 24);
        m.region(HatMesh.R_STRIPE).rgb(x.b());
        m.frustum(0.01f, 0.287f, 0.05f, 0.283f, 24, false, false);
    }

    private static void hasir(HatMesh m, HatCtx x) {
        m.layer(BASE).region(HatMesh.R_STRAW).rgb(x.a());
        float flap = 0.01f + 0.015f * x.energy;
        brimShaped(m, 0f, 0.26f, 0.6f, 40, 3, flap + 0.008f * wave(x.t, 3f, 0f));
        dome(m, 0.0f, 0.285f, 0.14f, 4, 24);
        m.region(HatMesh.R_SOLID).rgb(x.b());
        m.frustum(0.01f, 0.287f, 0.05f, 0.282f, 24, false, false);
        // arkada fiyonk
        m.push();
        m.translate(0f, 0.03f, 0.29f);
        m.rotZ(0.4f);
        m.box(-0.04f, 0f, 0f, 0.04f, 0.02f, 0.008f);
        m.rotZ(-0.8f);
        m.box(0.04f, 0f, 0f, 0.04f, 0.02f, 0.008f);
        m.pop();
        m.box(0f, 0.03f, 0.292f, 0.014f, 0.018f, 0.01f);
    }

    private static void kulaklik(HatMesh m, HatCtx x) {
        float beat = (float) Math.pow(Math.max(0, Math.sin(x.t * 2.1f * PI)), 8);
        // bas bandi (sol-sag yay)
        int n = 0;
        for (int i = 0; i <= 14; i++) {
            float a = i * PI / 14;
            P[n * 3] = (float) Math.cos(a) * 0.31f;
            P[n * 3 + 1] = -0.2f + (float) Math.sin(a) * 0.32f;
            P[n * 3 + 2] = 0f;
            n++;
        }
        m.layer(BASE).region(HatMesh.R_SOLID).rgb(x.a());
        m.tube(P, n, 0.026f, 0.026f, 6);
        for (int side = -1; side <= 1; side += 2) {
            m.push();
            m.translate(side * 0.3f, -0.26f, 0f);
            m.rotZ(side * PI / 2);
            m.rgb(x.a());
            m.frustum(-0.045f, 0.11f, 0.045f, 0.1f, 16, true, true);
            float k = 0.6f + 0.4f * beat;
            m.layer(GLOW).rgb(HatCtx.shade(side < 0 ? x.b() : x.c(), k + 0.2f));
            m.torus(0.048f, 0.075f, 0.012f + 0.006f * beat, 16, 4);
            m.layer(BASE);
            m.pop();
        }
        m.emitter(0.32f, -0.2f, 0f);
        m.emitter(-0.32f, -0.2f, 0f);
    }

    private static void glitchHale(HatMesh m, HatCtx x) {
        int bucket = (int) Math.floor(x.t * 7f);
        m.layer(GLOW).region(HatMesh.R_SOLID);
        for (int i = 0; i < 14; i++) {
            int h = (bucket * 31 + i * 17) * 1103515245 + 12345;
            float jx = ((h >>> 8) & 255) / 255f - 0.5f, jy = ((h >>> 16) & 255) / 255f - 0.5f;
            boolean jump = ((h >>> 4) & 7) == 0;
            float a = i * TAU / 14 + x.t * 0.8f;
            float r = 0.3f + (jump ? jx * 0.08f : 0f);
            float y = 0.3f + (jump ? jy * 0.06f : 0f) + 0.02f * wave(x.t, 2f, i);
            m.rgb(i % 2 == 0 ? x.a() : x.b());
            float s = 0.022f + (jump ? 0.01f : 0f);
            m.box((float) Math.cos(a) * r, y, (float) Math.sin(a) * r, s, s, s);
            if (i % 4 == 0) m.emitter((float) Math.cos(a) * r, y, (float) Math.sin(a) * r);
        }
        m.layer(BASE);
    }

    private static void karaDelik(HatMesh m, HatCtx x) {
        float bob = 0.02f * wave(x.t, 1.2f, 0f);
        m.push();
        m.translate(0f, 0.4f + bob, 0f);
        m.rotX(0.75f);
        m.rotY(x.t * 1.6f);
        m.layer(BASE).region(HatMesh.R_SOLID).rgb(x.a());
        m.sphere(0f, 0f, 0f, 0.1f, 6, 12);
        // yigilma diski: icten disa turuncu -> mor, donen
        m.layer(GLOW);
        int seg = 32;
        for (int ring = 0; ring < 3; ring++) {
            float rIn = 0.12f + ring * 0.06f, rOut = rIn + 0.06f;
            int ci = HatCtx.mix(x.b(), x.c(), ring / 3f) | 0xFF000000;
            int co = HatCtx.mix(x.b(), x.c(), (ring + 1) / 3f) | 0xFF000000;
            for (int i = 0; i < seg; i++) {
                float a0 = i * TAU / seg, a1 = (i + 1) * TAU / seg;
                float br = 0.75f + 0.25f * (float) Math.sin(a0 * 3 + ring);
                int cI = HatCtx.shade(ci & 0xFFFFFF, br) | 0xFF000000, cO = HatCtx.shade(co & 0xFFFFFF, br) | 0xFF000000;
                m.quadC((float) Math.cos(a0) * rIn, 0f, (float) Math.sin(a0) * rIn, (float) Math.cos(a0) * rOut, 0f, (float) Math.sin(a0) * rOut,
                        (float) Math.cos(a1) * rOut, 0f, (float) Math.sin(a1) * rOut, (float) Math.cos(a1) * rIn, 0f, (float) Math.sin(a1) * rIn, 0, 0, 1, 1, cI, cO, cO, cI);
            }
        }
        m.rgb(0xFFF0D0);
        m.torus(0f, 0.11f, 0.009f, 24, 4);
        m.emitter(0.25f, 0f, 0f);
        m.emitter(-0.25f, 0f, 0f);
        m.pop();
        m.layer(BASE);
    }

    private static void kalpHale(HatMesh m, HatCtx x) {
        m.layer(GLOW).region(HatMesh.R_SOLID);
        float beat = 1f + 0.18f * (float) Math.pow(Math.max(0, Math.sin(x.t * 2.4f * PI)), 6);
        for (int i = 0; i < 6; i++) {
            float a = x.t * 0.9f + i * TAU / 6;
            float hx = (float) Math.cos(a) * 0.34f, hz = (float) Math.sin(a) * 0.34f;
            m.push();
            m.translate(hx, 0.28f + 0.02f * wave(x.t, 2f, i), hz);
            m.rotY(-a - PI / 2);
            m.rgb(i % 2 == 0 ? x.a() : x.b());
            m.heart(0f, 0f, 0f, 0.05f * beat, 0.018f);
            m.pop();
            if (i % 3 == 0) m.emitter(hx, 0.3f, hz);
        }
        m.layer(BASE);
    }

    private static void ruhAlevi(HatMesh m, HatCtx x) {
        m.layer(GLOW).region(HatMesh.R_SOLID);
        // cevrede 6 kucuk dil
        for (int i = 0; i < 6; i++) {
            float a = i * TAU / 6 + 0.3f;
            float fl = 0.75f + 0.25f * wave(x.t, 7f + i * 0.4f, i * 1.3f);
            float bx = (float) Math.cos(a) * 0.11f, bz = (float) Math.sin(a) * 0.11f;
            float h = (0.16f + 0.05f * ((i * 3) % 4) / 3f) * fl;
            float lw = 0.025f * wave(x.t, 4.5f, i);
            int n = HatMesh.bezier(P, 6, bx, 0.04f, bz, bx * 1.15f + lw, 0.04f + h * 0.5f, bz * 1.15f, bx * 0.5f - lw, 0.04f + h, bz * 0.5f);
            m.rgb(i % 2 == 0 ? x.a() : x.c());
            m.tube(P, n, 0.045f, 0.004f, 6);
        }
        // merkez: uc katmanli uzun dil, kivrilarak titrer
        for (int k = 0; k < 3; k++) {
            float fl = 0.8f + 0.2f * wave(x.t, 6f + k, k * 1.7f) + 0.06f * wave(x.t, 11f, k);
            float h = (0.44f - k * 0.1f) * fl;
            float lean = 0.04f * wave(x.t, 3.5f, k) - 0.06f * x.energy;
            int n = 9;
            for (int i = 0; i < n; i++) {
                float tt = i / (float) (n - 1);
                float sway = (float) Math.sin(tt * 5f - x.t * 6f + k) * 0.025f * tt;
                P[i * 3] = lean * tt * tt + sway;
                P[i * 3 + 1] = 0.05f + h * tt;
                P[i * 3 + 2] = 0.04f * x.energy * tt + sway * 0.6f;
            }
            int c = k == 0 ? x.c() : k == 1 ? x.a() : x.b();
            m.rgb(c);
            m.tube(P, n, 0.12f - k * 0.033f, 0.004f, 8);
            if (k == 0) m.emitter(P[(n - 1) * 3], P[(n - 1) * 3 + 1], P[(n - 1) * 3 + 2]);
        }
        m.layer(TRANS).region(HatMesh.R_SOFT).color(0x38000000 | x.a());
        m.sphere(0f, 0.17f, 0f, 0.19f, 5, 9);
        m.layer(BASE);
    }
}
