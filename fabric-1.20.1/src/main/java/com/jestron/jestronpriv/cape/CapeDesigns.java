package com.jestron.jestronpriv.cape;

import static com.jestron.jestronpriv.cape.CapeArt.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 50 ozgun pelerin deseni. Hepsi prosedurel: dokular calisma aninda 40x64 piksel (vanilla pelerinin 4 kati
 * cozunurluk) uretilir, dosya yok. Animasyonlu desenler saniyede 20 kare yenilenir; durgun olanlar bir kez.
 * Hicbiri resmi Minecraft pelerinlerinin kopyasi degildir.
 */
public final class CapeDesigns {

    public static final List<CapeDesign> ALL;
    private static final Map<String, CapeDesign> BY_ID = new HashMap<>();

    private CapeDesigns() {
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
        d(l, "gece_yarisi", "Gece Yarısı", "Lacivert gece, göz kırpan yıldızlar", 0x0B1030, 0x26346E, 0xE8E8FF, true, "end_rod", 0.4f,
                CapeDesigns::nightSky);
        d(l, "galaksi", "Galaksi", "Dönen mor bulutsu ve yıldızlar", 0x120626, 0x7B2FBE, 0x3FB8FF, true, "portal", 1.2f, CapeDesigns::galaxy);
        d(l, "alev", "Alev", "Aşağıdan yükselen canlı ateş", 0x1A0500, 0xFF5A00, 0xFFE07A, true, "flame", 1.6f, CapeDesigns::fire);
        d(l, "buz", "Buz Kristali", "Kristal yüzeyler, parlayan çizgiler", 0x0E3A5C, 0x7FD4FF, 0xFFFFFF, true, "snowflake", 1.2f, CapeDesigns::ice);
        d(l, "lav", "Lav Akıntısı", "Kara kaya, akan kızgın çatlaklar", 0x1A1110, 0xFF4800, 0xFFD15C, true, "lava", 0.35f, CapeDesigns::lava);
        d(l, "okyanus", "Okyanus", "Dalgalanan derin mavi, köpük", 0x022B4A, 0x0E7FB8, 0xBFF4FF, true, "dripping_water", 0.8f, CapeDesigns::ocean);
        d(l, "orman", "Orman", "Yaprak dokusu, sarmaşık ve çiçekler", 0x0F2E14, 0x2E7D32, 0x9CCC65, false, null, 0f, CapeDesigns::forest);
        d(l, "gun_batimi", "Gün Batımı", "Mor-pembe-turuncu, dilimli güneş", 0xFF8A3D, 0xE0457B, 0x3A1C71, true, null, 0f, CapeDesigns::sunset);
        d(l, "kutup_isigi", "Kutup Işığı", "Gece göğünde akan yeşil-mor perdeler", 0x050A1A, 0x2BFF9A, 0xB24BFF, true, null, 0f, CapeDesigns::aurora);
        d(l, "simsek", "Şimşek", "Fırtına bulutları, çakan yıldırım", 0x14161F, 0x3A4058, 0xBFE8FF, true, "electric_spark", 0.8f, CapeDesigns::storm);
        d(l, "ejderha", "Ejderha Pulu", "Kızıl pullar, altın kenar, parlama", 0x5A0B0B, 0xC62828, 0xFFC857, true, null, 0f, CapeDesigns::scales);
        d(l, "zumrut_pul", "Zümrüt Pul", "Yeşil pullar, beyaz parlama", 0x053D2A, 0x1DB97A, 0xC9FFE6, true, null, 0f, CapeDesigns::scales);
        d(l, "kraliyet", "Kraliyet", "Kadife mor, altın taç ve kafes", 0x3B0A57, 0x6A1B9A, 0xFFC107, false, null, 0f, CapeDesigns::royal);
        d(l, "savasci", "Savaşçı", "Kızıl zemin, siyah şeritler, gümüş", 0x7F0000, 0x1A1A1A, 0xC0C0C0, false, null, 0f, CapeDesigns::warrior);
        d(l, "satranc", "Satranç", "Kabartmalı siyah-beyaz kareler", 0x151515, 0xEEEEEE, 0xB0B0B0, false, null, 0f, CapeDesigns::checker);
        d(l, "zebra", "Zebra", "Dalgalı siyah-beyaz çizgiler", 0xF5F5F5, 0x111111, 0x9E9E9E, false, null, 0f, CapeDesigns::zebra);
        d(l, "kaplan", "Kaplan", "Turuncu post, kenarda kalın çizgiler", 0xF57C00, 0x1B1B1B, 0xFFE0B2, false, null, 0f, CapeDesigns::tiger);
        d(l, "leopar", "Leopar", "Halka benekli altın post", 0xE0B05A, 0x2B1A0A, 0x8D5A1E, false, null, 0f, CapeDesigns::leopard);
        d(l, "neon_izgara", "Neon Izgara", "Retro güneş, akan neon zemin", 0x0A0018, 0xFF2BD6, 0x2BE7FF, true, null, 0f, CapeDesigns::synth);
        d(l, "devre", "Devre Kartı", "Yollarda akan ışık sinyalleri", 0x0B3D20, 0x2ECC71, 0xE8FF8A, true, null, 0f, CapeDesigns::circuit);
        d(l, "kod_yagmuru", "Kod Yağmuru", "Yağan yeşil karakter sütunları", 0x000800, 0x00C040, 0xCCFFCC, true, null, 0f, CapeDesigns::codeRain);
        d(l, "gokkusagi", "Gökkuşağı", "Aşağı akan renk şeritleri", 0xFF4D4D, 0x4DA6FF, 0xFFFFFF, true, "dust:FF66CC", 0.6f, CapeDesigns::rainbow);
        d(l, "pastel_bulut", "Pastel Bulut", "Yumuşak renkler, süzülen bulutlar", 0xFFD1DC, 0xC9E4FF, 0xFFFFFF, true, null, 0f, CapeDesigns::pastel);
        d(l, "kalpler", "Kalpler", "Atan beyaz kalpler", 0xFF5C8A, 0xFFB3C7, 0xFFFFFF, true, "heart", 0.35f, CapeDesigns::hearts);
        d(l, "yildiz_tozu", "Yıldız Tozu", "Altın tozu, ışıltılı parıltılar", 0x1D1B3A, 0x8C6CFF, 0xFFF3B0, true, "end_rod", 0.6f, CapeDesigns::stardust);
        d(l, "ay_isigi", "Ay Işığı", "Hilal, hale ve yıldızlar", 0x081226, 0x1E3A6E, 0xFFF6D6, true, null, 0f, CapeDesigns::moon);
        d(l, "gunes", "Güneş", "Dönen altın ışınlar", 0xFF8F00, 0xFFD54F, 0xFFFDE7, true, null, 0f, CapeDesigns::sun);
        d(l, "kiraz", "Kiraz Çiçeği", "Pembe çiçekler, süzülen yapraklar", 0xFFE4EC, 0xFF8FB1, 0x6D4C41, true, "cherry_leaves", 1.0f, CapeDesigns::cherry);
        d(l, "sonbahar", "Sonbahar", "Kızıl, turuncu, sarı yapraklar", 0x5D2E0C, 0xE65100, 0xFFB300, false, null, 0f, CapeDesigns::autumn);
        d(l, "kar_tanesi", "Kar Tanesi", "Yavaşça düşen kar taneleri", 0x0D2A4A, 0x4F86C6, 0xFFFFFF, true, "snowflake", 0.9f, CapeDesigns::snow);
        d(l, "zehir", "Zehir", "Kabarcıklı, damlayan zehir", 0x0B1F0B, 0x39FF14, 0xC6FF00, true, "dust:39FF14", 0.9f, CapeDesigns::toxic);
        d(l, "kizil_ay", "Kızıl Ay", "Krateli kızıl ay, geçen bulutlar", 0x1A0000, 0x8B0000, 0xFF3D3D, true, null, 0f, CapeDesigns::bloodMoon);
        d(l, "altin", "Altın", "Fırçalanmış altın, kayan parlama", 0x8D6E00, 0xFFD54F, 0xFFFDE7, true, null, 0f, CapeDesigns::metal);
        d(l, "gumus", "Gümüş", "Fırçalanmış gümüş, kayan parlama", 0x5F6A72, 0xD5DEE6, 0xFFFFFF, true, null, 0f, CapeDesigns::metal);
        d(l, "elmas", "Elmas", "Kesme yüzeyler, ışıltı", 0x00838F, 0x6FFFE9, 0xFFFFFF, true, null, 0f, CapeDesigns::gem);
        d(l, "ametist", "Ametist", "Mor kesme yüzeyler, ışıltı", 0x4A148C, 0xB388FF, 0xF3E5F5, true, null, 0f, CapeDesigns::gem);
        d(l, "obsidyen", "Obsidyen", "Cam gibi kara taş, mor pırıltı", 0x07050C, 0x2A1748, 0xB57CFF, true, "reverse_portal", 0.7f, CapeDesigns::obsidian);
        d(l, "bosluk", "Boşluk", "Merkeze akan girdap", 0x000000, 0x2E0854, 0xD35CFF, true, "portal", 2.0f, CapeDesigns::voidSwirl);
        d(l, "kor", "Kor", "Yükselen kıvılcımlar", 0x120400, 0xB33000, 0xFFB347, true, "small_flame", 1.2f, CapeDesigns::embers);
        d(l, "ruh_atesi", "Ruh Ateşi", "Turkuaz ruh alevi", 0x001418, 0x00B7C7, 0xB9FFFF, true, "soul_fire_flame", 1.6f, CapeDesigns::fire);
        d(l, "kamuflaj", "Kamuflaj", "Dört tonlu orman kamuflajı", 0x4B5320, 0x6B8E23, 0x3E2723, false, null, 0f, CapeDesigns::camo);
        d(l, "piksel", "Piksel Sanatı", "8-bit bloklar, parlak köşeler", 0x283593, 0x00ACC1, 0xFFCA28, false, null, 0f, CapeDesigns::pixel);
        d(l, "mozaik", "Mozaik", "Renkli taşlar, derz çizgileri", 0x263238, 0x26A69A, 0xFFB74D, false, null, 0f, CapeDesigns::mosaic);
        d(l, "kilim", "Kilim", "Anadolu kilimi: göz, zikzak, elibelinde", 0xB71C1C, 0x1A237E, 0xF9A825, false, null, 0f, CapeDesigns::kilim);
        d(l, "cini", "Çini", "Kobalt-turkuaz rozet çini", 0xF7F3E8, 0x0D47A1, 0x00ACC1, false, null, 0f, CapeDesigns::tile);
        d(l, "dalga_ritmi", "Dalga Ritmi", "Müziğe dans eden çubuklar", 0x0A0A14, 0x7C4DFF, 0x18FFFF, true, null, 0f, CapeDesigns::equalizer);
        d(l, "hologram", "Hologram", "Tarama çizgileri, titreyen ışık", 0x001A26, 0x00E5FF, 0xB2FFFF, true, null, 0f, CapeDesigns::hologram);
        d(l, "prizma", "Prizma", "Beyaz zeminde kırılan ışık", 0xEDEDF5, 0xB0BEC5, 0xFFFFFF, true, null, 0f, CapeDesigns::prism);
        d(l, "mermer", "Mermer", "Beyaz mermer, altın damarlar", 0xF2F2F2, 0x9E9E9E, 0xC9A227, false, null, 0f, CapeDesigns::marble);
        d(l, "bambu", "Bambu", "Boğumlu bambu sapları, yapraklar", 0xDCEDC8, 0x689F38, 0x33691E, false, null, 0f, CapeDesigns::bamboo);
        ALL = Collections.unmodifiableList(l);
        for (CapeDesign c : ALL) BY_ID.put(c.id(), c);
    }

    // ================================================================== desenler

    private static void nightSky(Px p) {
        int base = mix(p.c1, p.c2, p.v * 0.95f);
        base = screen(base, 0x3A2A7A, smooth(0.45f, 0.8f, fbm(p.u * 3f, p.v * 5f, 3)) * 0.45f);
        float s = star(p.x, p.y, 11, 0.055f, p.t, 0.75f);
        float sp = Math.max(sparkle(p.x, p.y, 9, 14, 3.2f), Math.max(sparkle(p.x, p.y, 30, 33, 2.6f), sparkle(p.x, p.y, 15, 52, 2.2f)));
        sp *= 0.6f + 0.4f * sin(p.t * 2.3f + p.x);
        float k = Math.max(s, sp);
        p.rgb = mix(base, p.c3, k);
        p.glow = k * 0.95f;
    }

    private static void galaxy(Px p) {
        float n = fbm(p.u * 2.6f, p.v * 3.6f, p.t * 0.12f, 4);
        float d = Math.abs((p.v - 0.5f) - (p.u - 0.5f) * 0.9f + (n - 0.5f) * 0.55f);
        float neb = smooth(0.42f, 0.02f, d) * (0.4f + n);
        int c = mix(p.c1, p.c2, clamp01(neb * 1.25f));
        c = screen(c, p.c3, smooth(0.5f, 0.85f, n) * neb * 1.5f);
        c = screen(c, 0xFFE6FF, smooth(0.08f, 0f, d) * 0.5f);
        float s = star(p.x, p.y, 23, 0.05f, p.t, 0.6f);
        p.rgb = mix(c, 0xFFFFFF, s);
        p.glow = clamp01(neb * 0.55f + s);
    }

    private static void fire(Px p) {
        float n = fbm(p.u * 4.2f, p.v * 3.2f + p.t * 1.7f, 4);
        float n2 = noise(p.u * 9f, p.v * 7f + p.t * 3.1f);
        float heat = clamp01(p.v * 1.3f - 0.2f + (n - 0.5f) * 1.0f + (n2 - 0.5f) * 0.25f);
        int dark = mix(p.c1, p.c2, 0.25f);
        int c;
        if (heat < 0.35f) c = mix(p.c1, dark, heat / 0.35f);
        else if (heat < 0.72f) c = mix(dark, p.c2, (heat - 0.35f) / 0.37f);
        else c = mix(p.c2, p.c3, (heat - 0.72f) / 0.28f);
        p.rgb = c;
        p.glow = smooth(0.38f, 1f, heat);
    }

    private static void ice(Px p) {
        Cell c = cell(p.u * 5f, p.v * 8f, p.cell);
        float facet = 0.5f + 0.5f * rand(c.id, 3);
        float edge = smooth(0.07f, 0f, c.d2 - c.d1);
        int base = mix(p.c1, p.c2, facet * (0.35f + 0.65f * (1f - p.v)));
        base = mix(base, p.c3, edge * 0.75f);
        float s = star(p.x, p.y, 31, 0.025f, p.t, 1f);
        p.rgb = mix(base, p.c3, s);
        p.glow = clamp01(edge * 0.35f + s * 0.9f);
    }

    private static void lava(Px p) {
        Cell c = cell(p.u * 4f, p.v * 6f + p.t * 0.12f, p.cell);
        float crack = smooth(0.13f, 0f, c.d2 - c.d1);
        float rock = 0.6f + 0.4f * fbm(p.u * 8f, p.v * 12f, 3);
        int stone = mul(p.c1, 0.7f + rock * 0.6f);
        float pulse = 0.72f + 0.28f * sin(p.t * 2.2f + p.v * 6f + rand(c.id, 5) * 4f);
        int hot = mix(p.c2, p.c3, crack * pulse);
        float k = crack * pulse;
        p.rgb = mix(stone, hot, clamp01(k * 1.15f));
        p.glow = k;
    }

    private static void ocean(Px p) {
        float w = sin(p.v * 22f - p.t * 2.2f + sin(p.u * 7f + p.t) * 1.5f);
        int c = mix(p.c1, p.c2, clamp01((1f - p.v) * 0.55f + 0.2f + 0.15f * w));
        float foam = smooth(0.82f, 1f, w) * fbm(p.u * 8f, p.v * 8f + p.t * 0.6f, 2);
        c = mix(c, p.c3, clamp01(foam * 1.6f));
        c = screen(c, p.c3, smooth(0.3f, 0f, p.v) * 0.25f * (0.5f + 0.5f * sin(p.u * 20f + p.t * 3f)));
        p.rgb = c;
        p.glow = foam * 0.3f;
    }

    private static void forest(Px p) {
        Cell c = cell(p.u * 6f, p.v * 9f, p.cell);
        float shade = rand(c.id, 1) * 0.45f + (1f - clamp01(c.d1 * 1.4f)) * 0.55f;
        int col = mix(p.c1, p.c2, shade);
        if (rand(c.id, 9) < 0.12f && c.d1 < 0.16f) col = mix(p.c3, 0xFFF59D, rand(c.id, 4));
        float vx = 0.5f + 0.25f * sin(p.v * 9f + 1f) + 0.06f * sin(p.v * 31f);
        float dv = Math.abs(p.u - vx);
        if (dv < 0.035f) col = mix(0x4E342E, 0x6D4C41, dv / 0.035f);
        else if (dv < 0.05f) col = mul(col, 0.7f);
        p.rgb = col;
    }

    private static void sunset(Px p) {
        int c = ramp(p.c3, p.c2, p.c1, p.v * 1.05f);
        float dx = (p.x + 0.5f - 20f) / 11f, dy = (p.y + 0.5f - 37f) / 11f;
        float r = (float) Math.sqrt(dx * dx + dy * dy);
        boolean horizon = p.y >= 46;
        if (r < 1f && !horizon) {
            boolean cut = dy > 0.05f && fract((p.y - p.t * 2f) / 4f) < 0.35f + dy * 0.3f;
            if (!cut) {
                c = mix(0xFFE07A, 0xFF6F61, clamp01(dy * 0.6f + 0.5f));
                p.glow = 0.6f;
            }
        }
        c = screen(c, 0xFFD180, smooth(1.9f, 0.9f, r) * 0.35f);
        if (horizon) {
            int water = mix(0x2A1450, 0x140A2E, (p.y - 46) / 18f);
            float ref = Math.abs(p.x + 0.5f - 20f) < 9f - (p.y - 46) * 0.35f && fract((p.y + p.t * 1.5f) / 3f) < 0.45f ? 1f : 0f;
            c = mix(water, 0xFFB07A, ref * 0.7f);
            p.glow = ref * 0.35f;
        }
        p.rgb = c;
    }

    private static void aurora(Px p) {
        int base = mix(p.c1, 0x0E1838, p.v);
        float n = fbm(p.u * 4.5f + p.t * 0.22f, p.v * 1.1f - p.t * 0.08f, 3);
        float band = smooth(0.26f, 0.62f, n) * smooth(1.05f, 0.2f, p.v);
        float streak = 0.55f + 0.45f * noise(p.u * 28f, p.t * 0.9f + p.v * 0.6f);
        int ac = mix(p.c2, p.c3, clamp01(p.v * 1.3f + (n - 0.5f)));
        float k = band * streak;
        int c = screen(base, ac, k * 1.7f);
        float s = star(p.x, p.y, 41, 0.04f, p.t, 0.6f) * (1f - k);
        p.rgb = mix(c, 0xFFFFFF, s);
        p.glow = clamp01(k * 0.95f + s * 0.7f);
    }

    private static void storm(Px p) {
        float cl = fbm(p.u * 3f, p.v * 4f + p.t * 0.05f, 4);
        int c = mix(p.c1, p.c2, smooth(0.3f, 0.8f, cl));
        c = mix(c, 0x24204A, smooth(0.6f, 1f, p.v) * 0.5f);
        float rain = fract((p.x * 0.5f + p.y - p.t * 38f) / 7f);
        if (rain < 0.09f && rand(p.x, (int) Math.floor((p.y - p.t * 38f) / 7f), 5) < 0.45f) c = mix(c, 0x9FB4D8, 0.35f);
        float period = 2.6f;
        int cycle = (int) Math.floor(p.t / period);
        float f = p.t / period - cycle;
        float flash = f < 0.2f ? (1f - f / 0.2f) * (0.7f + 0.3f * sin(f * 90f)) : 0f;
        if (flash > 0f) {
            float seg = p.v * 9f;
            int si = (int) seg;
            float a = 0.5f + (rand(si, cycle) - 0.5f) * 0.42f, b = 0.5f + (rand(si + 1, cycle) - 0.5f) * 0.42f;
            if (si == 0) a = 0.5f + (rand(0, cycle, 3) - 0.5f) * 0.2f;
            float bx = a + (b - a) * (seg - si);
            float dist = Math.abs(p.u - bx) * p.w;
            float core = dist < 0.9f ? 1f : 0f;
            float halo = smooth(5f, 0f, dist) * 0.55f;
            float k = Math.max(core, halo) * flash * (p.v < 0.92f ? 1f : 0f);
            c = screen(c, p.c3, k + flash * 0.25f * cl);
            p.glow = k;
        }
        p.rgb = c;
    }

    private static void scales(Px p) {
        // catı kiremidi gibi ust uste binen pullar: her sira bir altindakinin ustunde, pulun yuvarlak alt kenari gorunur
        final float cw = 10f, chh = 4.5f, rad = 5.6f;
        float px = p.x + 0.5f, py = p.y + 0.5f;
        int rowMax = (int) Math.floor(py / chh);
        int rowMin = (int) Math.floor((py - rad) / chh);
        float bestD = -1f;
        int bestRow = 0;
        float bestDx = 0f, bestDy = 0f;
        for (int r = rowMin; r <= rowMax; r++) {
            float off = (r & 1) == 1 ? 0.5f : 0f;
            float cy = r * chh;
            if (py < cy) continue;
            float cx = ((float) Math.floor(px / cw - off) + 0.5f + off) * cw;
            float dx = px - cx, dy = py - cy;
            float d = (float) Math.sqrt(dx * dx + dy * dy);
            if (d < rad) {
                bestD = d / rad;
                bestRow = r;
                bestDx = dx / rad;
                bestDy = dy / rad;
                break;
            }
        }
        int c;
        if (bestD < 0f) c = mul(p.c1, 0.6f);
        else {
            // ust kisim golgede (ustteki pulun altinda), alt kenara dogru parlak; ince altin kenar, koyu dis cizgi
            float shade = 0.2f + 0.8f * bestDy;
            c = mix(p.c1, p.c2, clamp01(shade));
            if (bestD > 0.84f) c = bestD > 0.95f ? mul(p.c1, 0.5f) : mix(p.c2, p.c3, 0.75f);
            else if (bestDy > 0.45f && Math.abs(bestDx + 0.25f) < 0.12f && bestD < 0.7f) c = mix(c, 0xFFFFFF, 0.35f);
        }
        float sheen = smooth(0.11f, 0f, Math.abs(fract(p.u * 0.6f + p.v * 0.45f - p.t * 0.16f) - 0.5f));
        c = screen(c, p.c3, sheen * 0.5f);
        p.rgb = c;
        p.glow = sheen * 0.3f + (bestD > 0.78f && bestD <= 0.96f ? 0.1f : 0f);
    }

    private static final String[] CROWN = {
            "..X...X...X..",
            "..X..XXX..X..",
            ".XXX.XXX.XXX.",
            ".XXXXXXXXXXX.",
            "..XXoXXXoXX..",
            "..XXXXoXXXX..",
            "..XXXXXXXXX..",
            ".XXXXXXXXXXX."};

    private static char bitmap(String[] bm, int x, int y, int ox, int oy) {
        int lx = x - ox, ly = y - oy;
        if (ly < 0 || ly >= bm.length || lx < 0 || lx >= bm[ly].length()) return '.';
        return bm[ly].charAt(lx);
    }

    private static void royal(Px p) {
        float fab = fbm(p.u * 14f, p.v * 20f, 2);
        int c = mix(p.c1, p.c2, 0.35f + fab * 0.45f + (1f - p.v) * 0.15f);
        boolean lattice = Math.floorMod(p.x + p.y, 8) == 0 || Math.floorMod(p.x - p.y, 8) == 0;
        if (lattice) c = mix(c, p.c3, 0.22f);
        if (Math.floorMod(p.x + p.y, 8) == 0 && Math.floorMod(p.x - p.y, 8) == 0) c = mix(c, p.c3, 0.7f);
        int b = Math.min(Math.min(p.x, p.w - 1 - p.x), Math.min(p.y, p.h - 1 - p.y));
        if (b == 2 || b == 3) c = mix(p.c3, 0xFFF3C4, b == 2 ? 0.35f : 0f);
        char ch = bitmap(CROWN, p.x, p.y, 13, 14);
        if (ch == 'X') {
            c = mix(p.c3, 0xFFF59D, (p.y - 14) / 10f);
            p.glow = 0.15f;
        } else if (ch == 'o') {
            c = 0xE53935;
            p.glow = 0.5f;
        }
        p.rgb = c;
    }

    private static void warrior(Px p) {
        float ch = fract(p.v * 5f - Math.abs(p.u - 0.5f) * 1.25f);
        int c = mul(p.c1, 0.85f + 0.3f * fbm(p.u * 10f, p.v * 16f, 2));
        if (ch < 0.24f) c = p.c2;
        else if (ch < 0.29f) c = p.c3;
        if (Math.abs(p.x + 0.5f - 20f) < 1.1f && p.y > 6 && p.y < 58) c = mix(c, p.c3, 0.5f);
        p.rgb = c;
    }

    private static void checker(Px p) {
        int cx = (int) Math.floor(p.u * 5f), cy = (int) Math.floor(p.v * 8f);
        boolean white = ((cx + cy) & 1) == 0;
        float lu = fract(p.u * 5f), lv = fract(p.v * 8f);
        float bevel = lu < 0.12f || lv < 0.12f ? 1.15f : (lu > 0.88f || lv > 0.88f ? 0.8f : 1f);
        int c = white ? p.c2 : p.c1;
        p.rgb = white ? mul(c, 0.82f * bevel + 0.08f) : mix(c, 0x404040, bevel > 1f ? 0.5f : 0f);
    }

    private static void zebra(Px p) {
        float s = sin(p.v * 28f + sin(p.u * 6f) * 2.2f + noise(p.u * 3f, p.v * 3f) * 3f);
        p.rgb = s > 0.25f ? p.c2 : mix(p.c1, 0xE0E0E0, fbm(p.u * 10f, p.v * 10f, 2) * 0.5f);
    }

    private static void tiger(Px p) {
        float edge = Math.abs(p.u - 0.5f) * 2f;
        int c = mix(p.c3, p.c1, clamp01(0.35f + edge * 0.9f));
        float s = Math.abs(sin(p.v * 17f + (noise(p.u * 2.5f, p.v * 3f) - 0.5f) * 5f + edge * 1.5f));
        float w = 0.08f + 0.22f * edge;
        if (s < w && edge > 0.12f) c = p.c2;
        p.rgb = c;
    }

    private static void leopard(Px p) {
        Cell c = cell(p.u * 6f, p.v * 9f, p.cell);
        float jitter = rand(c.id, 2) * 0.08f;
        int col = mix(p.c1, 0xF3D08A, fbm(p.u * 8f, p.v * 8f, 2) * 0.5f);
        if (c.d1 < 0.21f + jitter) col = p.c3;
        float ring = Math.abs(c.d1 - (0.3f + jitter));
        if (ring < 0.075f && noise(p.u * 40f, p.v * 40f) > 0.25f) col = p.c2;
        p.rgb = col;
    }

    private static void synth(Px p) {
        int c;
        if (p.v < 0.52f) {
            c = mix(p.c1, 0x3B0A5E, p.v / 0.52f);
            float dx = (p.x + 0.5f - 20f) / 13f, dy = (p.y + 0.5f - 31f) / 13f;
            float r = (float) Math.sqrt(dx * dx + dy * dy);
            if (r < 1f && dy < 0.15f) {
                boolean cut = dy > -0.45f && fract((p.y + p.t * 1.5f) / 3f) < 0.3f + (dy + 0.45f) * 0.5f;
                if (!cut) {
                    c = mix(0xFFE066, p.c2, clamp01(dy * 0.8f + 0.6f));
                    p.glow = 0.65f;
                }
            }
            c = screen(c, p.c2, smooth(1.6f, 1f, r) * 0.35f);
            p.glow = Math.max(p.glow, star(p.x, p.y, 51, 0.03f, p.t, 0.5f) * (r > 1.1f ? 1f : 0f));
            c = mix(c, 0xFFFFFF, p.glow > 0.5f && r > 1f ? p.glow : 0f);
        } else {
            float vz = p.v - 0.5f;
            float z = 0.32f / (vz + 0.015f);
            float gx = (p.u - 0.5f) * z * 2.2f, gz = z + p.t * 1.6f;
            float lw = 0.05f * z;
            float lx = Math.abs(fract(gx) - 0.5f), lz = Math.abs(fract(gz * 0.5f) - 0.5f);
            float line = Math.max(lx > 0.5f - lw ? 1f : 0f, lz > 0.5f - lw * 0.7f ? 1f : 0f);
            c = mix(0x12002A, 0x050010, clamp01(vz * 2f));
            float fade = smooth(0f, 0.12f, vz);
            c = mix(c, p.c3, line * fade);
            if (p.y == (int) (0.52f * p.h)) c = p.c2;
            p.glow = line * fade;
        }
        p.rgb = c;
    }

    private static void circuit(Px p) {
        int cx = p.x >> 2, cy = p.y >> 2, lx = p.x & 3, ly = p.y & 3;
        float r = rand(cx, cy, 7);
        boolean hor = r < 0.5f;
        boolean trace = hor ? ly == 1 : lx == 1;
        boolean pad = rand(cx, cy, 9) < 0.1f && lx >= 1 && lx <= 2 && ly >= 1 && ly <= 2;
        int c = mix(p.c1, 0x07261A, fbm(p.u * 6f, p.v * 9f, 2));
        if (trace || pad) {
            c = mix(p.c2, 0x1E8449, 0.45f);
            float ph = fract((p.x + p.y) * 0.045f - p.t * 0.7f + rand(cx, cy, 3));
            float pulse = smooth(0.07f, 0f, ph);
            if (pad) {
                c = 0xC9A227;
                pulse = Math.max(pulse, 0.25f + 0.25f * sin(p.t * 3f + r * 9f));
            }
            c = mix(c, p.c3, pulse);
            p.glow = pulse * 0.95f;
        }
        p.rgb = c;
    }

    private static void codeRain(Px p) {
        int col = p.x >> 1;
        float speed = 7f + rand(col, 1) * 12f;
        float head = fract(p.t * speed / (p.h + 24f) + rand(col, 2)) * (p.h + 24f) - 8f;
        float trail = 14f + rand(col, 3) * 14f;
        float dy = head - p.y;
        int c = p.c1;
        if (dy >= 0f && dy < trail) {
            float k = 1f - dy / trail;
            boolean glyph = rand(p.x, p.y >> 1, (int) Math.floor(p.t * 6f + rand(col, 4) * 10f)) > 0.38f;
            if (glyph) {
                c = mix(p.c1, p.c2, k);
                if (dy < 1.5f) c = p.c3;
                p.glow = dy < 1.5f ? 1f : k * 0.8f;
            }
        }
        p.rgb = c;
    }

    private static void rainbow(Px p) {
        float h = p.v * 0.9f - p.t * 0.12f + sin(p.u * 4f + p.t) * 0.03f;
        int c = hsv(h, 0.72f, 1f);
        float hi = smooth(0.12f, 0f, Math.abs(fract(p.v * 3f - p.t * 0.25f) - 0.5f) - 0.3f);
        p.rgb = screen(c, 0xFFFFFF, hi * 0.25f);
        p.glow = 0.22f;
    }

    private static void pastel(Px p) {
        int c = mix(p.c1, p.c2, clamp01(p.u * 0.5f + p.v * 0.6f));
        float cl = fbm(p.u * 3f + p.t * 0.05f, p.v * 4f, 4);
        float sh = fbm(p.u * 3f + p.t * 0.05f, p.v * 4f - 0.06f, 4);
        if (sh > 0.6f) c = mix(c, mul(c, 0.86f), smooth(0.6f, 0.64f, sh));
        c = mix(c, p.c3, smooth(0.56f, 0.62f, cl));
        p.rgb = c;
    }

    private static void hearts(Px p) {
        int c = mix(p.c1, p.c2, p.v * 0.6f + 0.2f);
        int row = (int) Math.floor(p.v * 5f);
        float cu = fract(p.u * 3f + ((row & 1) == 1 ? 0.5f : 0f)) - 0.5f, cv = fract(p.v * 5f) - 0.5f;
        float pulse = 1f + 0.13f * sin(p.t * 4.5f + row * 1.3f);
        float hx = cu / (0.34f * pulse), hy = cv * (3f / 5f) * (64f / 40f) / (0.34f * pulse);
        if (heart(hx * 1.1f, hy * 1.1f)) {
            c = p.c3;
            p.glow = (pulse - 0.87f) * 1.5f;
        } else if (heart(hx * 0.92f, hy * 0.92f)) c = mix(p.c1, 0xC2185B, 0.4f);
        p.rgb = c;
    }

    private static void stardust(Px p) {
        int c = mix(p.c1, p.c2, clamp01(p.u * 0.4f + p.v * 0.7f) * 0.8f);
        float dust = fbm(p.u * 5f + p.t * 0.03f, p.v * 7f, 3);
        c = screen(c, p.c3, smooth(0.55f, 0.85f, dust) * 0.4f);
        float s = star(p.x, p.y, 61, 0.11f, p.t, 0.9f);
        float sp = Math.max(sparkle(p.x, p.y, 8, 10, 3f), Math.max(sparkle(p.x, p.y, 31, 24, 3.5f), sparkle(p.x, p.y, 18, 47, 2.8f)));
        sp *= 0.5f + 0.5f * sin(p.t * 3.1f + p.y * 0.2f);
        float k = Math.max(s, sp);
        p.rgb = mix(c, p.c3, k);
        p.glow = k;
    }

    private static void moon(Px p) {
        int c = mix(p.c1, p.c2, p.v);
        float dx = (p.x + 0.5f - 21f) / 8.5f, dy = (p.y + 0.5f - 17f) / 8.5f;
        float r = (float) Math.sqrt(dx * dx + dy * dy);
        float r2 = (float) Math.hypot(dx - 0.45f, dy + 0.12f);
        c = screen(c, p.c3, smooth(2.2f, 0.9f, r) * 0.28f);
        if (r < 1f && r2 > 0.92f) {
            c = mix(p.c3, 0xE8DDB5, fbm(dx * 3f, dy * 3f, 2) * 0.6f);
            p.glow = 0.85f;
        } else {
            float s = star(p.x, p.y, 71, 0.05f, p.t, 0.7f);
            c = mix(c, 0xFFFFFF, s);
            p.glow = s * 0.8f;
        }
        p.rgb = c;
    }

    private static void sun(Px p) {
        float dx = (p.x + 0.5f - 20f) / 20f, dy = (p.y + 0.5f - 22f) / 20f;
        float r = (float) Math.sqrt(dx * dx + dy * dy);
        float a = (float) Math.atan2(dy, dx);
        float ray = 0.5f + 0.5f * cos(a * 12f + p.t * 0.9f);
        int c = mix(p.c1, p.c2, clamp01(ray * smooth(1.6f, 0.2f, r) + 0.15f));
        float core = smooth(0.42f, 0.36f, r);
        c = mix(c, p.c3, core);
        c = screen(c, p.c3, smooth(0.7f, 0.4f, r) * 0.4f);
        p.rgb = c;
        p.glow = clamp01(core + ray * smooth(1.1f, 0.3f, r) * 0.3f);
    }

    private static void cherry(Px p) {
        int c = mix(p.c1, 0xFFF7FA, p.v * 0.6f);
        float bx = 0.08f + p.v * 0.75f + 0.05f * sin(p.v * 14f);
        float db = Math.abs(p.u - bx) * p.w;
        if (db < 1.4f - p.v) c = p.c3;
        Cell cc = cell(p.u * 5f, p.v * 8f, p.cell);
        if (rand(cc.id, 3) < 0.6f) {
            float lx = (p.u * 5f - cc.cx), ly = (p.v * 8f - cc.cy) * (64f / 40f) * (5f / 8f);
            float rr = (float) Math.sqrt(lx * lx + ly * ly);
            float ang = (float) Math.atan2(ly, lx) + rand(cc.id, 4) * TAU;
            float petal = 0.26f * (0.72f + 0.28f * cos(ang * 5f));
            if (rr < petal) {
                c = rr < 0.07f ? 0xFFD54F : mix(p.c2, 0xFFFFFF, clamp01(rr / petal) * 0.35f);
            }
        }
        for (int k = 0; k < 7; k++) {
            float px = fract(rand(k, 1) + sin(p.t * 0.8f + k) * 0.04f) * p.w;
            float py = fract(rand(k, 2) + p.t * 0.07f * (0.7f + rand(k, 3) * 0.6f)) * p.h;
            if (Math.abs(p.x + 0.5f - px) < 1f && Math.abs(p.y + 0.5f - py) < 0.8f) c = 0xFFB6CF;
        }
        p.rgb = c;
    }

    private static void autumn(Px p) {
        int c = mix(p.c1, 0x8D4A1C, fbm(p.u * 4f, p.v * 6f, 3));
        Cell cc = cell(p.u * 5f, p.v * 8f, p.cell);
        float a = rand(cc.id, 2) * TAU;
        float lx = (p.u * 5f - cc.cx), ly = (p.v * 8f - cc.cy) * (64f / 40f) * (5f / 8f);
        float rx = lx * cos(a) - ly * sin(a), ry = lx * sin(a) + ly * cos(a);
        float e = (rx * rx) / (0.3f * 0.3f) + (ry * ry) / (0.15f * 0.15f);
        if (e < 1f) {
            float pick = rand(cc.id, 6);
            int leaf = pick < 0.33f ? p.c2 : (pick < 0.66f ? p.c3 : 0xB71C1C);
            c = Math.abs(ry) < 0.02f ? mul(leaf, 0.7f) : mix(leaf, mul(leaf, 0.75f), e);
        } else if (Math.abs(ry) < 0.018f && rx > 0.28f && rx < 0.4f) c = 0x4E342E;
        p.rgb = c;
    }

    private static void snow(Px p) {
        int c = mix(p.c1, p.c2, p.v * 0.8f);
        float fv = p.v * 7f - p.t * 0.25f;
        int row = (int) Math.floor(fv);
        float cu = fract(p.u * 3f + (row & 1) * 0.5f) - 0.5f, cv = fract(fv) - 0.5f;
        int id = hash((int) Math.floor(p.u * 3f + (row & 1) * 0.5f), row);
        float sc = 2.6f + rand(id, 1) * 1.2f;
        float sx = cu * sc, sy = cv * sc * (64f / 40f) * (3f / 7f);
        float rot = rand(id, 2) * 1.0f + p.t * 0.2f;
        float k = snowflake(sx * cos(rot) - sy * sin(rot), sx * sin(rot) + sy * cos(rot));
        float dot = star(p.x, (int) Math.floor(p.y - p.t * 9f), 81, 0.02f, 0f, 0f);
        float kk = Math.max(k, dot * 0.9f);
        p.rgb = mix(c, p.c3, kk);
        p.glow = kk * 0.35f;
    }

    private static void toxic(Px p) {
        int c = mix(p.c1, 0x1B3A10, fbm(p.u * 5f, p.v * 6f + p.t * 0.1f, 3));
        Cell cc = cell(p.u * 5f, p.v * 7f + p.t * 0.6f, p.cell);
        float br = 0.18f + rand(cc.id, 2) * 0.12f;
        float ring = smooth(0.05f, 0f, Math.abs(cc.d1 - br));
        float fill = cc.d1 < br ? 0.25f : 0f;
        float k = Math.max(ring, fill);
        c = mix(c, p.c2, k);
        int col = p.x >> 2;
        float drip = 0.1f + 0.14f * rand(col, 11) + 0.03f * sin(p.t * 2f + col);
        if (p.v < drip) {
            c = mix(p.c2, p.c3, 1f - p.v / drip);
            k = 0.8f;
        }
        p.rgb = c;
        p.glow = k * 0.85f;
    }

    private static void bloodMoon(Px p) {
        int c = mix(p.c1, p.c2, p.v * 0.7f + 0.15f);
        float dx = (p.x + 0.5f - 20f) / 11f, dy = (p.y + 0.5f - 20f) / 11f;
        float r = (float) Math.sqrt(dx * dx + dy * dy);
        c = screen(c, p.c3, smooth(2f, 0.95f, r) * 0.4f);
        if (r < 1f) {
            float cr = fbm(dx * 2.5f + 3f, dy * 2.5f, 3);
            c = mix(mix(p.c3, 0xFF8A65, 0.35f), 0xA01818, smooth(0.5f, 0.75f, cr) * 0.65f);
            c = mul(c, 0.85f + 0.3f * (1f - r));
            p.glow = 0.75f;
        }
        float cloud = smooth(0.55f, 0.72f, fbm(p.u * 3f - p.t * 0.06f, p.v * 9f, 4));
        c = mix(c, 0x1A0A0A, cloud * 0.85f);
        p.glow *= 1f - cloud;
        p.rgb = c;
    }

    private static void metal(Px p) {
        float brush = noise(p.u * 2f, p.v * 60f);
        int c = mix(p.c1, p.c2, clamp01(0.45f + 0.35f * sin(p.u * 7f + p.v * 3f + fbm(p.u * 2f, p.v * 3f, 2) * 3f) + (brush - 0.5f) * 0.35f));
        float sheen = smooth(0.09f, 0f, Math.abs(fract(p.u * 0.5f + p.v * 0.8f - p.t * 0.2f) - 0.5f));
        c = screen(c, p.c3, sheen * 0.9f);
        p.rgb = c;
        p.glow = sheen * 0.4f;
    }

    private static void gem(Px p) {
        float fu = p.u * 6f, fv = p.v * 8f;
        int ix = (int) Math.floor(fu), iy = (int) Math.floor(fv);
        int tri = fract(fu) > fract(fv) ? 1 : 0;
        float shade = rand(ix * 2 + tri, iy, 5);
        int c = mix(p.c1, p.c2, 0.25f + shade * 0.7f);
        float edge = Math.min(Math.min(fract(fu), 1f - fract(fu)), Math.min(Math.min(fract(fv), 1f - fract(fv)), Math.abs(fract(fu) - fract(fv))));
        c = mix(c, p.c3, smooth(0.06f, 0f, edge) * 0.55f);
        float sweep = smooth(0.08f, 0f, Math.abs(fract(p.u * 0.4f - p.v * 0.6f + p.t * 0.12f) - 0.5f));
        c = screen(c, p.c3, sweep * shade * 0.6f);
        float s = star(p.x, p.y, 91, 0.035f, p.t, 1f);
        float sp = Math.max(sparkle(p.x, p.y, 12, 20, 3f), sparkle(p.x, p.y, 28, 44, 3f)) * (0.5f + 0.5f * sin(p.t * 2.7f));
        float k = Math.max(s, sp);
        p.rgb = mix(c, p.c3, k);
        p.glow = clamp01(k + sweep * 0.25f);
    }

    private static void obsidian(Px p) {
        Cell cc = cell(p.u * 4f, p.v * 6f, p.cell);
        float f = rand(cc.id, 1);
        int c = mix(p.c1, p.c2, f * 0.5f + smooth(0.1f, 0f, cc.d2 - cc.d1) * 0.4f);
        float g2 = star(p.x, p.y, 101, 0.03f, p.t, 1f);
        float sweep = smooth(0.06f, 0f, Math.abs(fract(p.u * 0.5f + p.v * 0.5f - p.t * 0.1f) - 0.5f)) * f;
        c = screen(c, p.c3, sweep * 0.5f);
        p.rgb = mix(c, p.c3, g2);
        p.glow = clamp01(g2 + sweep * 0.4f);
    }

    private static void voidSwirl(Px p) {
        float dx = (p.u - 0.5f) * 1f, dy = (p.v - 0.5f) * 1.6f;
        float r = (float) Math.sqrt(dx * dx + dy * dy);
        float a = (float) Math.atan2(dy, dx) + r * 5f - p.t * 0.7f;
        float sx = cos(a) * r * 4f, sy = sin(a) * r * 4f;
        float n = fbm(sx + 10f, sy + 10f, 3);
        float arm = smooth(0.45f, 0.75f, n) * smooth(1.1f, 0.15f, r);
        int c = mix(p.c1, p.c2, arm);
        c = screen(c, p.c3, arm * arm * 0.9f);
        c = screen(c, p.c3, smooth(0.16f, 0f, r) * 0.7f);
        float s = star(p.x, p.y, 111, 0.04f, p.t, 0.8f) * smooth(0.1f, 0.4f, r);
        p.rgb = mix(c, 0xF3E5F5, s);
        p.glow = clamp01(arm * 0.8f + s + smooth(0.16f, 0f, r) * 0.6f);
    }

    private static void embers(Px p) {
        int c = mix(p.c1, p.c2, smooth(0.45f, 1f, p.v) * 0.75f);
        c = screen(c, p.c2, fbm(p.u * 4f, p.v * 3f + p.t * 0.6f, 3) * smooth(0.5f, 1f, p.v) * 0.6f);
        Cell cc = cell(p.u * 8f, p.v * 10f + p.t * 1.2f, p.cell);
        float sz = 0.08f + rand(cc.id, 2) * 0.1f;
        float fl = 0.6f + 0.4f * sin(p.t * 9f + rand(cc.id, 3) * 9f);
        float k = 0f;
        if (rand(cc.id, 4) < 0.55f && cc.d1 < sz) {
            k = fl * (1f - cc.d1 / sz);
            c = mix(c, p.c3, k + 0.2f);
        }
        p.rgb = c;
        p.glow = clamp01(k + smooth(0.6f, 1f, p.v) * 0.3f);
    }

    private static void camo(Px p) {
        float n = fbm(p.u * 3.2f, p.v * 4.5f, 4);
        float n2 = fbm(p.u * 3.2f + 40f, p.v * 4.5f, 3);
        int c;
        if (n < 0.42f) c = p.c1;
        else if (n < 0.55f) c = p.c2;
        else c = 0xBDB76B;
        if (n2 > 0.6f) c = p.c3;
        // kumas dokusu: duz renkler canli dursun
        p.rgb = mul(c, 0.9f + 0.18f * noise(p.u * 40f, p.v * 64f) + (((p.x + p.y) & 1) == 0 ? 0.03f : -0.03f));
    }

    private static void pixel(Px p) {
        int bx = p.x >> 2, by = p.y >> 2, lx = p.x & 3, ly = p.y & 3;
        float r = rand(bx, by, 17);
        int[] pal = {p.c1, p.c2, p.c3, 0xE91E63, 0xFFFFFF, mul(p.c1, 0.6f)};
        int c = pal[(int) (r * pal.length) % pal.length];
        if (lx == 0 && ly == 0) c = mix(c, 0xFFFFFF, 0.5f);
        else if (lx == 3 || ly == 3) c = mul(c, 0.72f);
        p.rgb = c;
    }

    private static void mosaic(Px p) {
        int tx = p.x / 5, ty = p.y / 5, lx = p.x % 5, ly = p.y % 5;
        if (lx == 4 || ly == 4) {
            p.rgb = mix(p.c1, 0x455A64, 0.3f);
            return;
        }
        float r = rand(tx, ty, 23);
        int base = r < 0.4f ? p.c2 : (r < 0.7f ? p.c3 : (r < 0.85f ? 0xEF5350 : 0xECEFF1));
        float sh = 0.85f + 0.3f * rand(tx, ty, 29);
        int c = mul(base, sh);
        if (lx == 0 || ly == 0) c = mix(c, 0xFFFFFF, 0.15f);
        p.rgb = c;
    }

    private static void kilim(Px p) {
        int cream = 0xF3E5C0;
        int band = p.y / 16;
        int ly = p.y % 16;
        int c;
        if (ly < 2 || ly > 13) {
            c = (Math.floorMod(p.x + (ly < 2 ? ly : 15 - ly) * 2, 6) < 3) ? p.c3 : p.c2;
        } else {
            int cx = (p.x / 10) * 10 + 5;
            float dx = Math.abs(p.x + 0.5f - cx), dy = Math.abs(ly + 0.5f - 8f);
            float dia = dx / 5f + dy / 6f;
            int bg = (band & 1) == 0 ? p.c1 : p.c2;
            if (dia < 0.38f) c = p.c3;
            else if (dia < 0.6f) c = cream;
            else if (dia < 0.82f) c = (band & 1) == 0 ? p.c2 : p.c1;
            else if (dia < 0.95f) c = cream;
            else c = bg;
            if (dia >= 0.95f && dx > 3.5f && dy < 1.2f) c = p.c3;
        }
        p.rgb = mul(c, 0.92f + 0.12f * rand(p.x, p.y, 37));
    }

    private static void tile(Px p) {
        int lx = p.x % 20, ly = p.y % 20;
        float dx = lx + 0.5f - 10f, dy = ly + 0.5f - 10f;
        float r = (float) Math.sqrt(dx * dx + dy * dy);
        float a = (float) Math.atan2(dy, dx);
        int c = mix(p.c1, 0xEDE6D3, fbm(p.u * 10f, p.v * 10f, 2) * 0.5f);
        float petal = 6.5f * (0.62f + 0.38f * Math.abs(cos(a * 4f)));
        if (r < petal) c = r < 2.2f ? 0xC62828 : (r < petal - 1.3f ? p.c2 : p.c3);
        float cr = Math.min(Math.min((float) Math.hypot(lx, ly), (float) Math.hypot(20 - lx, ly)), Math.min((float) Math.hypot(lx, 20 - ly), (float) Math.hypot(20 - lx, 20 - ly)));
        if (cr < 4f) c = cr < 2f ? p.c3 : p.c2;
        if (lx == 0 || ly == 0) c = mix(c, 0x8D8D8D, 0.5f);
        p.rgb = c;
    }

    private static void equalizer(Px p) {
        int bar = p.x / 3;
        boolean gap = p.x % 3 == 2;
        float h = 0.18f + 0.78f * Math.abs(sin(p.t * (2.2f + rand(bar, 1) * 2.5f) + bar * 0.7f)) * (0.55f + 0.45f * noise(bar * 0.6f, p.t * 1.3f));
        float dy = Math.abs(p.v - 0.5f) * 2f;
        int c = mix(p.c1, 0x14142A, dy);
        if (!gap && dy < h) {
            boolean seg = (p.y & 1) == 0;
            c = mix(p.c2, p.c3, dy / Math.max(0.01f, h));
            if (!seg) c = mul(c, 0.7f);
            p.glow = 0.75f;
        }
        p.rgb = c;
    }

    private static void hologram(Px p) {
        float scan = 0.55f + 0.45f * (fract((p.y - p.t * 12f) / 3f) < 0.5f ? 1f : 0f);
        float flick = 0.85f + 0.15f * noise(p.t * 8f, 0.5f);
        int c = mix(p.c1, 0x003344, p.v);
        boolean grid = p.x % 8 == 0 || p.y % 8 == 0;
        if (grid) c = mix(c, p.c2, 0.25f);
        float dx = Math.abs(p.x + 0.5f - 20f) / 11f, dy = Math.abs(p.y + 0.5f - 28f) / 16f;
        float dia = dx + dy;
        float k = 0f;
        if (Math.abs(dia - 0.9f) < 0.08f || Math.abs(dia - 0.55f) < 0.06f) k = 1f;
        else if (dia < 0.3f) k = 0.6f + 0.4f * sin(p.t * 3f);
        float band = smooth(0.06f, 0f, Math.abs(fract(p.v - p.t * 0.35f) - 0.5f));
        c = mix(c, p.c3, clamp01(k * scan + band * 0.5f));
        c = mul(c, flick);
        p.rgb = c;
        p.glow = clamp01((k * scan + band * 0.6f + (grid ? 0.15f : 0f)) * flick);
    }

    private static void prism(Px p) {
        int c = mix(p.c1, p.c2, fbm(p.u * 3f, p.v * 4f, 2) * 0.4f);
        float d = fract((p.u + p.v * 0.9f) * 1.1f - p.t * 0.18f);
        float band = smooth(0.35f, 0f, Math.abs(d - 0.5f));
        int rb = hsv(d * 1.6f + p.v * 0.2f, 0.65f, 1f);
        c = mix(c, rb, band * 0.75f);
        c = screen(c, 0xFFFFFF, smooth(0.04f, 0f, Math.abs(d - 0.5f)) * 0.7f);
        p.rgb = c;
        p.glow = band * 0.3f;
    }

    private static void marble(Px p) {
        float n = fbm(p.u * 3f, p.v * 4f, 4);
        float vein = Math.abs(sin(p.u * 6f + p.v * 10f + n * 7f));
        int c = mix(p.c1, 0xE0E0E0, fbm(p.u * 6f, p.v * 6f, 2) * 0.6f);
        if (vein < 0.14f) c = mix(mul(p.c2, 0.85f), c, smooth(0f, 0.14f, vein));
        float gold = Math.abs(sin(p.u * 3f - p.v * 6f + n * 5f + 1.3f));
        if (gold < 0.035f) {
            c = p.c3;
            p.glow = 0.12f;
        }
        p.rgb = c;
    }

    private static void bamboo(Px p) {
        int c = mix(p.c1, 0xF1F8E9, p.v * 0.5f);
        int[] xs = {7, 20, 33};
        int[] off = {0, 6, 3};
        for (int i = 0; i < 3; i++) {
            float dx = p.x + 0.5f - xs[i];
            if (Math.abs(dx) < 3.5f) {
                float sh = 1f - Math.abs(dx) / 3.5f;
                c = mix(p.c3, p.c2, 0.35f + sh * 0.65f);
                if (dx < -1.5f) c = mul(c, 0.85f);
                int ny = Math.floorMod(p.y + off[i], 14);
                if (ny == 0) c = mul(p.c3, 0.8f);
                else if (ny == 1) c = mix(c, 0xC5E1A5, 0.5f);
            }
        }
        float lx = fract(p.u * 3f + 0.17f), lv = fract(p.v * 4f + 0.3f);
        float leaf = Math.abs((lv - 0.5f) - (lx - 0.5f) * 0.6f);
        if (lx > 0.25f && lx < 0.6f && leaf < 0.05f * (1f - Math.abs(lx - 0.42f) * 5f)) c = 0x7CB342;
        p.rgb = c;
    }
}
