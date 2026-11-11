package com.jestron.jestronpriv.sunucu;

import java.util.List;

/**
 * 50 aura: JestronPriv modundaki auralarin sunucu karsiligi. Desenler moddakiyle ayni koddur; parcaciklar sunucuda
 * olusur ve modsuz oyuncular dahil herkes gorur.
 */
public final class Auralar {

    /** Aura: kimlik, ad, menu simgesi (Material adi), aciklama, her tick'te calisan desen. */
    public record Aura(String id, String name, String icon, String desc, java.util.function.Consumer<Ctx> tick) {
    }

    private static final double TAU = Math.PI * 2;

    private static int hsv(double h) {
        h = h - Math.floor(h);
        double r = Math.abs(h * 6 - 3) - 1, g = 2 - Math.abs(h * 6 - 2), b = 2 - Math.abs(h * 6 - 4);
        r = Math.max(0, Math.min(1, r));
        g = Math.max(0, Math.min(1, g));
        b = Math.max(0, Math.min(1, b));
        return ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }

    private static void trail(Ctx c, String id, double up) {
        double[] b = c.behind(up);
        c.p(id, b[0], b[1], b[2]);
    }

    public static final List<Aura> ALL = List.of(
            new Aura("ates_halkasi", "Ateş Halkası", "BLAZE_POWDER", "Ayaklarında dönen alev halkası, yürürken alev izi", c -> {
                if (c.moving) { trail(c, "small_flame", 0.1); if (c.every(2)) trail(c, "flame", 0.15); }
                else c.ring("flame", 0.75, 0.08, 2, c.t * 0.18);
            }),
            new Aura("ruh_alevi", "Ruh Alevi", "SOUL_TORCH", "Mavi ruh alevleri ve süzülen ruhlar", c -> {
                if (c.moving) { trail(c, "soul_fire_flame", 0.12); if (c.every(6)) trail(c, "soul", 0.3); }
                else { c.ring("soul_fire_flame", 0.7, 0.08, 2, -c.t * 0.15); if (c.every(10)) c.p("soul", c.rd(-0.5, 0.5), c.rd(0.2, 1.2), c.rd(-0.5, 0.5)); }
            }),
            new Aura("kalpler", "Kalp Yağmuru", "POPPY", "Etrafında uçuşan kalpler", c -> {
                if (c.moving) { if (c.every(4)) trail(c, "heart", 0.6); }
                else if (c.every(6)) c.p("heart", c.rd(-0.7, 0.7), c.rd(0.4, 2.1), c.rd(-0.7, 0.7));
            }),
            new Aura("yildiz_tozu", "Yıldız Tozu", "NETHER_STAR", "Gövdeni saran yükselen yıldız sarmalı", c -> {
                double h = (c.t * 0.05) % 2.0;
                double a = c.t * 0.32;
                c.pv("end_rod", Math.cos(a) * 0.65, h, Math.sin(a) * 0.65, 0, 0.005, 0);
                if (c.moving && c.every(2)) trail(c, "end_rod", 0.5);
            }),
            new Aura("galaksi", "Galaksi Sarmalı", "ENDER_EYE", "Mor yıldızların döndüğü küçük bir galaksi", c -> {
                double a = c.t * 0.25;
                for (int k = 0; k < 2; k++) {
                    double b = a + k * Math.PI;
                    c.pv("portal", Math.cos(b) * 0.9, 0.9 + Math.sin(c.t * 0.1) * 0.2, Math.sin(b) * 0.9, -Math.cos(b) * 0.3, 0, -Math.sin(b) * 0.3);
                }
                if (c.every(3)) c.p("witch", c.rd(-0.6, 0.6), c.rd(0.3, 1.6), c.rd(-0.6, 0.6));
            }),
            new Aura("kar_firtinasi", "Kar Fırtınası", "SNOWBALL", "Başının üstünden yağan kar", c -> {
                c.pv("snowflake", c.rd(-1.2, 1.2), c.rd(2.2, 2.8), c.rd(-1.2, 1.2), 0, -0.04, 0);
                if (c.moving && c.every(2)) trail(c, "item_snowball", 0.2);
            }),
            new Aura("melek_halesi", "Melek Halesi", "FEATHER", "Başının üstünde parlayan hale", c -> {
                double a = c.t * 0.2;
                c.p("end_rod", Math.cos(a) * 0.32, 2.25, Math.sin(a) * 0.32);
                c.p("end_rod", Math.cos(a + Math.PI) * 0.32, 2.25, Math.sin(a + Math.PI) * 0.32);
                if (c.moving && c.every(2)) { double[] b = c.behind(1.2); c.dust(0xFFFFFF, 0.8f, b[0], b[1], b[2]); }
            }),
            new Aura("seytan", "Şeytan Boynuzları", "FIRE_CHARGE", "Başında alev boynuzlar, ayaklarında kor", c -> {
                for (int s = -1; s <= 1; s += 2) {
                    double[] h = c.local(s * 0.22, 2.05 + Math.sin(c.t * 0.3) * 0.03, 0.05);
                    c.p("small_flame", h[0], h[1], h[2]);
                }
                if (c.every(4)) c.p("smoke", c.rd(-0.4, 0.4), 0.1, c.rd(-0.4, 0.4));
                if (c.moving && c.every(3)) trail(c, "falling_lava", 0.4);
            }),
            new Aura("gokkusagi", "Gökkuşağı İzi", "PRISMARINE_SHARD", "Arkanda gökkuşağı izi, dururken renkli halka", c -> {
                if (c.moving) {
                    for (int i = 0; i < 2; i++) { double[] b = c.behind(0.6 + i * 0.25); c.dust(hsv(c.t * 0.03 + i * 0.15), 1.1f, b[0], b[1], b[2]); }
                } else {
                    double a = c.t * 0.2;
                    c.dust(hsv(c.t * 0.02), 1.0f, Math.cos(a) * 0.8, 0.1, Math.sin(a) * 0.8);
                    c.dust(hsv(c.t * 0.02 + 0.5), 1.0f, Math.cos(a + Math.PI) * 0.8, 0.1, Math.sin(a + Math.PI) * 0.8);
                }
            }),
            new Aura("elmas", "Elmas Parıltısı", "DIAMOND", "Gövdende parlayan elmas kırıntıları", c -> {
                c.dust(0x4FE3D8, 0.6f, c.rd(-0.5, 0.5), c.rd(0.1, 1.9), c.rd(-0.5, 0.5));
                if (c.every(6)) c.p("end_rod", c.rd(-0.5, 0.5), c.rd(0.3, 1.8), c.rd(-0.5, 0.5));
            }),
            new Aura("zumrut", "Zümrüt Bereketi", "EMERALD", "Etrafında yeşil bereket parıltısı", c -> {
                if (c.every(2)) c.p("happy_villager", c.rd(-0.8, 0.8), c.rd(0.1, 2.0), c.rd(-0.8, 0.8));
                if (c.moving && c.every(2)) trail(c, "happy_villager", 0.2);
            }),
            new Aura("notalar", "Nota Dansı", "NOTE_BLOCK", "Çevrende dönen renkli müzik notaları", c -> {
                if (c.every(4)) {
                    double a = c.t * 0.12;
                    c.pv("note", Math.cos(a) * 0.9, 1.0 + Math.sin(c.t * 0.2) * 0.4, Math.sin(a) * 0.9, Math.floor(c.t % 24) / 24.0, 0, 0);
                }
            }),
            new Aura("runler", "Büyü Rünleri", "ENCHANTED_BOOK", "Büyü rünleri dört bir yandan sana akar", c -> {
                for (int i = 0; i < 2; i++) {
                    double a = c.rd(0, TAU), rr = c.rd(1.0, 1.8);
                    c.pv("enchant", 0, 1.0, 0, Math.cos(a) * rr, c.rd(-0.4, 1.0), Math.sin(a) * rr);
                }
            }),
            new Aura("ender_sisi", "Ender Sisi", "ENDER_PEARL", "Mor ender parçacıkları etrafında kaynar", c -> {
                for (int i = 0; i < 2; i++) c.pv("portal", c.rd(-0.5, 0.5), c.rd(0, 1.8), c.rd(-0.5, 0.5), c.rd(-0.5, 0.5), c.rd(-0.3, 0.3), c.rd(-0.5, 0.5));
            }),
            new Aura("lav_damlasi", "Lav Damlası", "LAVA_BUCKET", "Üstünden damlayan kızgın lav", c -> {
                if (c.every(2)) c.p("dripping_lava", c.rd(-0.8, 0.8), c.rd(2.2, 2.6), c.rd(-0.8, 0.8));
                if (c.moving && c.every(2)) trail(c, "falling_lava", 0.5);
            }),
            new Aura("su_damlasi", "Su Damlaları", "WATER_BUCKET", "Etrafına süzülen su damlaları", c -> {
                c.p(c.every(2) ? "dripping_water" : "falling_water", c.rd(-0.8, 0.8), c.rd(2.0, 2.5), c.rd(-0.8, 0.8));
            }),
            new Aura("sakura", "Sakura", "PINK_PETALS", "Üstüne yağan kiraz çiçeği yaprakları", c -> {
                if (c.every(2)) c.p("cherry_leaves", c.rd(-1.0, 1.0), c.rd(2.3, 2.8), c.rd(-1.0, 1.0));
            }),
            new Aura("duman", "Duman Perdesi", "CAMPFIRE", "Ayaklarından yükselen gizemli duman", c -> {
                if (c.moving) { if (c.every(2)) trail(c, "cloud", 0.1); }
                else { if (c.every(3)) c.pv("smoke", c.rd(-0.5, 0.5), 0.1, c.rd(-0.5, 0.5), 0, 0.03, 0); if (c.every(25)) c.pv("campfire_cosy_smoke", c.rd(-0.3, 0.3), 0.1, c.rd(-0.3, 0.3), 0, 0.02, 0); }
            }),
            new Aura("simsek", "Şimşek Yükü", "LIGHTNING_ROD", "Vücudunda çıtırdayan elektrik", c -> {
                if (c.every(2)) for (int i = 0; i < 3; i++) c.p("electric_spark", c.rd(-0.5, 0.5), c.rd(0.1, 1.9), c.rd(-0.5, 0.5));
                if (c.moving) trail(c, "electric_spark", 0.3);
            }),
            new Aura("kan_izi", "Kan İzi", "REDSTONE", "Damlayan koyu kırmızı kan", c -> {
                if (c.every(2)) c.dustV(0x8A0000, 0.9f, c.rd(-0.4, 0.4), c.rd(0.8, 1.6), c.rd(-0.4, 0.4), 0, -0.05, 0);
                if (c.moving) { double[] b = c.behind(0.05); c.dust(0x5A0000, 1.2f, b[0], b[1], b[2]); }
            }),
            new Aura("kelebekler", "Kelebekler", "ORANGE_DYE", "Etrafında kanat çırpan kelebekler", c -> {
                for (int k = 0; k < 3; k++) {
                    double a = c.t * (0.07 + k * 0.02) + k * 2.1;
                    double rr = 0.9 + 0.15 * Math.sin(c.t * 0.13 + k);
                    double h = 1.0 + 0.5 * Math.sin(c.t * 0.09 + k * 1.7);
                    double flap = Math.abs(Math.sin(c.t * 0.9 + k)) * 0.09;
                    int col = k == 0 ? 0xFF9A1F : k == 1 ? 0x5AB4FF : 0xFF5CC8;
                    c.dust(col, 0.55f, Math.cos(a) * rr + flap, h, Math.sin(a) * rr);
                    c.dust(col, 0.55f, Math.cos(a) * rr - flap, h, Math.sin(a) * rr);
                }
            }),
            new Aura("dna", "DNA Sarmalı", "CYAN_DYE", "Gövdeni saran çift sarmal", c -> {
                double h = (c.t * 0.06) % 2.1;
                double a = c.t * 0.35;
                c.dust(0x2BE8FF, 0.7f, Math.cos(a) * 0.6, h, Math.sin(a) * 0.6);
                c.dust(0xFF3BD0, 0.7f, Math.cos(a + Math.PI) * 0.6, h, Math.sin(a + Math.PI) * 0.6);
            }),
            new Aura("sonik", "Sonik Dalga", "ECHO_SHARD", "Aralıklarla yayılan sonik halkalar", c -> {
                if (c.every(20)) for (int i = 0; i < 16; i++) {
                    double a = i * TAU / 16;
                    c.dustV(0x39D6E0, 1.0f, Math.cos(a) * 0.4, 0.15, Math.sin(a) * 0.4, Math.cos(a) * 0.18, 0, Math.sin(a) * 0.18);
                }
                if (c.every(5)) c.p("sculk_charge_pop", c.rd(-0.5, 0.5), 0.1, c.rd(-0.5, 0.5));
            }),
            new Aura("tac", "Kraliyet Tacı", "GOLDEN_HELMET", "Başının üstünde altın taç", c -> {
                if (c.every(2)) for (int i = 0; i < 5; i++) {
                    double a = i * TAU / 5 + c.t * 0.03;
                    c.dust(0xFFD23A, 0.6f, Math.cos(a) * 0.3, 2.15, Math.sin(a) * 0.3);
                    c.dust(0xFFE98A, 0.5f, Math.cos(a) * 0.3, 2.3, Math.sin(a) * 0.3);
                }
            }),
            new Aura("kara_delik", "Kara Delik", "OBSIDIAN", "Belinde dönen, içine çeken karanlık", c -> {
                double a = c.t * 0.3;
                for (int k = 0; k < 2; k++) {
                    double b = a + k * Math.PI;
                    c.pv("squid_ink", Math.cos(b) * 1.0, 1.0, Math.sin(b) * 1.0, -Math.cos(b) * 0.05, 0, -Math.sin(b) * 0.05);
                }
                if (c.every(3)) c.pv("reverse_portal", c.rd(-0.2, 0.2), 1.0, c.rd(-0.2, 0.2), 0, 0, 0);
            }),
            new Aura("totem", "Totem Işıltısı", "TOTEM_OF_UNDYING", "Yükselen yeşil-altın totem ışıltısı", c -> {
                if (c.every(2)) c.pv("totem_of_undying", c.rd(-0.5, 0.5), c.rd(0, 0.6), c.rd(-0.5, 0.5), 0, c.rd(0.05, 0.15), 0);
            }),
            new Aura("atesbocegi", "Ateş Böcekleri", "GLOWSTONE_DUST", "Etrafında süzülen ateş böcekleri", c -> {
                if (c.every(2)) c.p("glow", c.rd(-1.3, 1.3), c.rd(0.3, 2.2), c.rd(-1.3, 1.3));
                if (c.every(3)) c.dust(0xD8FF5A, 0.4f, c.rd(-1.2, 1.2), c.rd(0.3, 2.0), c.rd(-1.2, 1.2));
            }),
            new Aura("matrix", "Matrix", "LIME_DYE", "Etrafından akan yeşil kod yağmuru", c -> {
                for (int i = 0; i < 2; i++) {
                    double a = c.rd(0, TAU);
                    c.dustV(0x1AFF4A, 0.6f, Math.cos(a) * 1.0, c.rd(1.6, 2.6), Math.sin(a) * 1.0, 0, -0.12, 0);
                }
            }),
            new Aura("kristal", "Kristal Parıltı", "AMETHYST_SHARD", "Etrafında parlayan kristal kıvılcımlar", c -> {
                c.p(c.every(2) ? "wax_on" : "scrape", c.rd(-0.7, 0.7), c.rd(0.2, 2.0), c.rd(-0.7, 0.7));
                if (c.every(4)) c.dust(0xB873FF, 0.6f, c.rd(-0.6, 0.6), c.rd(0.2, 1.8), c.rd(-0.6, 0.6));
            }),
            new Aura("bulut", "Bulut Yürüyüşü", "WHITE_WOOL", "Bulutların üstünde yürü", c -> {
                if (c.moving) { trail(c, "cloud", 0.02); }
                else if (c.every(3)) c.pv("cloud", c.rd(-0.6, 0.6), 0.02, c.rd(-0.6, 0.6), 0, 0.005, 0);
            }),
            new Aura("kalp_atisi", "Kalp Atışı", "RED_DYE", "Göğsünden yayılan kalp atışı halkaları", c -> {
                boolean beat = c.crossed(24, 0);
                if (beat || c.crossed(24, 5)) for (int i = 0; i < 14; i++) {
                    double a = i * TAU / 14;
                    c.dustV(0xFF1F4A, 0.8f, Math.cos(a) * 0.3, 1.2, Math.sin(a) * 0.3, Math.cos(a) * 0.12, 0, Math.sin(a) * 0.12);
                }
                if (beat) c.p("heart", 0, 2.2, 0);
            }),
            new Aura("dalga", "Okyanus Dalgası", "HEART_OF_THE_SEA", "Ayaklarında dalgalanan mavi su halkası", c -> {
                double rr = 0.7 + 0.25 * Math.sin(c.t * 0.15);
                c.dustRing(0x1E7FD6, 0.8f, rr, 0.06 + 0.05 * Math.sin(c.t * 0.3), 3, c.t * 0.1);
                if (c.every(4)) c.p("splash", c.rd(-0.6, 0.6), 0.1, c.rd(-0.6, 0.6));
            }),
            new Aura("sculk", "Sculk Ruhları", "SCULK", "Ayaklarından yükselen sculk ruhları", c -> {
                if (c.every(4)) c.pv("sculk_soul", c.rd(-0.5, 0.5), 0.1, c.rd(-0.5, 0.5), 0, 0.03, 0);
                if (c.every(3)) c.p("sculk_charge_pop", c.rd(-0.6, 0.6), 0.05, c.rd(-0.6, 0.6));
            }),
            new Aura("neon", "Neon Çember", "MAGENTA_DYE", "Belinde dönen neon renk çemberi", c -> {
                double a = c.t * 0.25;
                c.fade(0xFF2BD6, 0x00F0FF, 0.9f, Math.cos(a) * 0.8, 1.0, Math.sin(a) * 0.8);
                c.fade(0x00F0FF, 0xFF2BD6, 0.9f, Math.cos(a + Math.PI) * 0.8, 1.0, Math.sin(a + Math.PI) * 0.8);
            }),
            new Aura("uzay_tozu", "Uzay Tozu", "CHORUS_FRUIT", "Etrafında süzülen mor uzay tozu", c -> {
                c.p("reverse_portal", c.rd(-0.8, 0.8), c.rd(0, 1.9), c.rd(-0.8, 0.8));
            }),
            new Aura("kivilcim", "Kıvılcım İzi", "FLINT_AND_STEEL", "Yürürken arkanda kıvılcım saçar", c -> {
                if (c.moving) { trail(c, "crit", 0.3); trail(c, "crit", 0.6); }
                else if (c.every(8)) for (int i = 0; i < 6; i++) c.pv("crit", 0, 0.5, 0, c.rd(-0.3, 0.3), c.rd(0.1, 0.4), c.rd(-0.3, 0.3));
            }),
            new Aura("pembe_ruya", "Pembe Rüya", "PINK_DYE", "Pembe sarmal ve uçuşan kalpler", c -> {
                double a = c.t * 0.28, h = (c.t * 0.05) % 2.0;
                c.dust(0xFF8AD8, 0.8f, Math.cos(a) * 0.55, h, Math.sin(a) * 0.55);
                if (c.every(16)) c.p("heart", c.rd(-0.5, 0.5), c.rd(1.0, 2.0), c.rd(-0.5, 0.5));
            }),
            new Aura("buz_kristali", "Buz Kristali", "BLUE_ICE", "Etrafında dönen buz kristalleri", c -> {
                c.ring("snowflake", 0.85, 0.5 + Math.sin(c.t * 0.1) * 0.3, 2, c.t * 0.15);
                if (c.every(3)) c.p("item_snowball", c.rd(-0.6, 0.6), c.rd(0.2, 1.8), c.rd(-0.6, 0.6));
            }),
            new Aura("isik_kanatlari", "Işık Kanatları", "ELYTRA", "Sırtında beliren parlak ışık kanatları", c -> {
                if (!c.every(2)) return;
                double flap = 0.25 + 0.15 * Math.sin(c.t * 0.18);
                for (int s = -1; s <= 1; s += 2) {
                    for (int i = 1; i <= 7; i++) {
                        double u = i / 7.0;
                        double side = s * (0.15 + u * 0.95);
                        double up = 1.3 + Math.sin(u * Math.PI) * 0.45 - u * 0.15;
                        double back = -0.3 - u * flap;
                        double[] l = c.local(side, up, back);
                        c.dust(u > 0.8 ? 0xFFF2B0 : 0xFFFFFF, 0.7f, l[0], l[1], l[2]);
                    }
                }
            }),
            new Aura("ejder_nefesi", "Ejder Nefesi", "DRAGON_BREATH", "Mor ejder nefesi etrafında kıvrılır", c -> {
                if (c.moving) trail(c, "dragon_breath", 0.3);
                else { double a = c.t * 0.2; c.pv("dragon_breath", Math.cos(a) * 0.7, 0.2, Math.sin(a) * 0.7, 0, 0.01, 0); }
            }),
            // ------------------------------------------------------------------ 1.7.0: 10 efsane aura
            new Aura("buyu_cemberi", "Büyü Çemberi", "LAPIS_LAZULI", "Ayaklarında dönen iki halkalı, altı köşeli yıldızlı büyü çemberi", c -> {
                double a = c.t * 0.06;
                for (int k = 0; k < 3; k++) {
                    double b = a + k * TAU / 3;
                    c.dust(0xB070FF, 0.7f, Math.cos(b) * 1.0, 0.05, Math.sin(b) * 1.0);
                }
                for (int k = 0; k < 2; k++) {
                    double b = -a * 1.4 + k * Math.PI;
                    c.dust(0xFFD050, 0.6f, Math.cos(b) * 0.62, 0.06, Math.sin(b) * 0.62);
                }
                // altigen yildiz: iki ucgenin kenarlarindan rastgele noktalar
                for (int k = 0; k < 2; k++) {
                    int e = c.r.nextInt(6);
                    double p0 = a * 0.5 + e * TAU / 6, p1 = p0 + TAU / 3;
                    double u = c.r.nextDouble();
                    double x = Math.cos(p0) * 0.96 * (1 - u) + Math.cos(p1) * 0.96 * u, z = Math.sin(p0) * 0.96 * (1 - u) + Math.sin(p1) * 0.96 * u;
                    c.dust(0xE0A0FF, 0.5f, x, 0.07, z);
                }
                if (c.every(6)) c.pv("enchant", 0, 0.2, 0, c.rd(-1.2, 1.2), c.rd(0.6, 1.6), c.rd(-1.2, 1.2));
            }),
            new Aura("kozmik_halka", "Gezegen Halkası", "ENDER_EYE", "Belinde dönen eğik gezegen halkası ve etrafında dolanan ay", c -> {
                double a = c.t * 0.12;
                for (int k = 0; k < 3; k++) {
                    double b = a + k * TAU / 3;
                    double x = Math.cos(b) * 0.95, z = Math.sin(b) * 0.95;
                    c.fade(0xFFD08A, 0x8A6AFF, 0.7f, x, 1.0 + z * 0.35, z);
                }
                if (c.every(3)) {
                    double m = -c.t * 0.09;
                    c.p("end_rod", Math.cos(m) * 1.35, 1.25 + Math.sin(m * 2) * 0.15, Math.sin(m) * 1.35);
                }
            }),
            new Aura("ruh_firtinasi", "Ruh Fırtınası", "SOUL_LANTERN", "Etrafında yükselen mavi ruh alevi hortumu", c -> {
                for (int k = 0; k < 2; k++) {
                    double h = (c.t * 0.08 + k * 1.1) % 2.2;
                    double r = 0.3 + h * 0.28;
                    double a = c.t * 0.42 + k * Math.PI + h * 2.0;
                    c.pv("soul_fire_flame", Math.cos(a) * r, h, Math.sin(a) * r, 0, 0.015, 0);
                }
                if (c.every(5)) c.pv("soul", c.rd(-0.5, 0.5), c.rd(0.1, 0.6), c.rd(-0.5, 0.5), 0, 0.02, 0);
                if (c.moving && c.every(2)) trail(c, "soul_fire_flame", 0.1);
            }),
            new Aura("yildirim_kafesi", "Yıldırım Kafesi", "COPPER_INGOT", "Etrafına çakan dikey yıldırım parmaklıkları", c -> {
                if (c.every(6)) {
                    double a = c.rd(0, TAU), x = Math.cos(a) * 0.95, z = Math.sin(a) * 0.95;
                    for (int i = 0; i < 8; i++) {
                        double j = (i % 2 == 0 ? 1 : -1) * 0.07;
                        c.p("electric_spark", x + j * Math.sin(a), i * 0.28, z - j * Math.cos(a));
                    }
                }
                double b = c.t * 0.3;
                c.p("electric_spark", Math.cos(b) * 0.95, c.rd(0.1, 2.1), Math.sin(b) * 0.95);
            }),
            new Aura("kan_ayi", "Kan Ayı", "REDSTONE", "Başının üstünde kızıl ay, etrafa damlayan kan", c -> {
                double a = c.t * 0.15;
                for (int k = 0; k < 2; k++) {
                    double b = a + k * Math.PI;
                    c.dust(0xD0101C, 0.9f, Math.cos(b) * 0.3, 2.45, Math.sin(b) * 0.3);
                }
                if (c.every(4)) c.dust(0xFF5050, 1.2f, 0, 2.45, 0);
                if (c.every(3)) {
                    double d = c.rd(0, TAU), r = c.rd(0.5, 0.9);
                    c.dustV(0x8A0008, 0.8f, Math.cos(d) * r, 2.3, Math.sin(d) * r, 0, -0.09, 0);
                }
            }),
            new Aura("kar_yildizi", "Kar Yıldızı", "PACKED_ICE", "Ayaklarında dönen dev kar tanesi, üstüne yağan kar", c -> {
                double a = c.t * 0.03;
                for (int k = 0; k < 3; k++) {
                    int arm = c.r.nextInt(6);
                    double ang = a + arm * TAU / 6, u = c.r.nextDouble();
                    double x = Math.cos(ang) * u * 1.05, z = Math.sin(ang) * u * 1.05;
                    if (u > 0.45 && c.r.nextFloat() < 0.5f) {
                        // dal: kolun iki yanina 45 derece
                        double side = c.r.nextFloat() < 0.5f ? 1 : -1, bl = c.rd(0, 0.28), ba = ang + side * Math.PI / 4;
                        x += Math.cos(ba) * bl;
                        z += Math.sin(ba) * bl;
                    }
                    c.dust(k == 0 ? 0xFFFFFF : 0xA8D8FF, 0.55f, x, 0.06, z);
                }
                if (c.every(4)) c.pv("snowflake", c.rd(-1.0, 1.0), c.rd(2.2, 2.6), c.rd(-1.0, 1.0), 0, -0.03, 0);
            }),
            new Aura("volkan", "Volkan", "MAGMA_CREAM", "Ayaklarında kaynayan lav, alev halkası ve kıvılcım püskürmeleri", c -> {
                double a = c.t * 0.2;
                c.p("flame", Math.cos(a) * 0.65, 0.05, Math.sin(a) * 0.65);
                c.p("flame", Math.cos(a + Math.PI) * 0.65, 0.05, Math.sin(a + Math.PI) * 0.65);
                if (c.every(4)) c.p("lava", c.rd(-0.5, 0.5), 0.1, c.rd(-0.5, 0.5));
                if (c.every(16)) for (int i = 0; i < 5; i++) c.pv("flame", c.rd(-0.15, 0.15), 0.1, c.rd(-0.15, 0.15), c.rd(-0.04, 0.04), c.rd(0.1, 0.2), c.rd(-0.04, 0.04));
                if (c.moving && c.every(2)) trail(c, "small_flame", 0.1);
            }),
            new Aura("atom", "Atom", "END_CRYSTAL", "Etrafında üç yörüngede dönen elektronlar ve parlayan çekirdek", c -> {
                double a = c.t * 0.32;
                int[] cols = {0x40E8FF, 0x7A8CFF, 0x40FFB0};
                for (int k = 0; k < 3; k++) {
                    double tilt = k * Math.PI / 3, b = a + k * 2.1;
                    double x = Math.cos(b) * 0.95, y0 = Math.sin(b) * 0.45;
                    // yorunge ekseni etrafinda egik elips
                    double rx = x * Math.cos(tilt), rz = x * Math.sin(tilt);
                    c.dust(cols[k], 0.6f, rx, 1.05 + y0, rz);
                }
                if (c.every(2)) c.dust(0xFF50E0, 0.9f, c.rd(-0.08, 0.08), 1.05 + c.rd(-0.08, 0.08), c.rd(-0.08, 0.08));
            }),
            new Aura("su_kalkani", "Su Kalkanı", "PRISMARINE_CRYSTALS", "Seni saran dönen su küresi ve patlayan kabarcıklar", c -> {
                double a = c.t * 0.05;
                for (int k = 0; k < 3; k++) {
                    double lat = c.rd(-1.2, 1.2), lon = a * (k + 1) + c.rd(0, TAU);
                    double r = 1.1;
                    c.dust(0x5AC8FF, 0.5f, Math.cos(lon) * Math.cos(lat) * r, 1.0 + Math.sin(lat) * r, Math.sin(lon) * Math.cos(lat) * r);
                }
                if (c.every(3)) c.p("bubble_pop", c.rd(-1.0, 1.0), c.rd(0.2, 2.0), c.rd(-1.0, 1.0));
                if (c.every(8)) c.p("dripping_water", c.rd(-0.4, 0.4), 2.2, c.rd(-0.4, 0.4));
            }),
            new Aura("bahar", "Bahar Bahçesi", "SUNFLOWER", "Ayaklarında açan çiçekler, uçuşan yapraklar ve polen", c -> {
                if (c.every(2)) {
                    double a = c.rd(0, TAU), r = c.rd(0.4, 1.0);
                    int[] fl = {0xFF8AC8, 0xFFE040, 0xC8A0FF, 0xFFFFFF};
                    c.dust(fl[c.r.nextInt(fl.length)], 0.7f, Math.cos(a) * r, 0.08, Math.sin(a) * r);
                }
                if (c.every(4)) c.p("happy_villager", c.rd(-0.9, 0.9), c.rd(0.1, 0.5), c.rd(-0.9, 0.9));
                if (c.every(5)) c.p("cherry_leaves", c.rd(-1.0, 1.0), c.rd(2.2, 2.6), c.rd(-1.0, 1.0));
                if (c.every(6)) c.p("spore_blossom_air", c.rd(-0.8, 0.8), c.rd(0.5, 1.8), c.rd(-0.8, 0.8));
                if (c.moving && c.every(2)) { double[] bh = c.behind(0.05); c.dust(0xFFA0D0, 0.8f, bh[0], bh[1], bh[2]); }
            }));

    private Auralar() {
    }

    public static Aura get(String id) {
        if (id == null) return null;
        for (Aura a : ALL) if (a.id().equals(id)) return a;
        return null;
    }
}
