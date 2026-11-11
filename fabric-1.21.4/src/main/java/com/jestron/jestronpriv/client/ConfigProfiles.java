package com.jestron.jestronpriv.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Ayar profilleri: ayarlarin tamamini ya da secilen bolumlerini adla kaydet, geri yukle, paylasim koduyla (panoya)
 * ya da dosyayla arkadasinla paylas.
 * <p>
 * Profiller {@code config/jestronpriv/profiles/*.json} icinde durur. Paylasim kodu: {@code JPRIV1:} + sikistirilmis
 * JSON (URL-guvenli Base64). Yuklenen her deger ayni dogrulamadan ({@link ModConfig#sanitize()}) gecer; bilinmeyen ya
 * da bozuk alanlar yok sayilir. Her ayar paylasilir: ozel totem sesi ve isim etiketi gorselleri dosyalariyla birlikte
 * profile gomulur ({@value #ASSETS}) ve alan kisinin bilgisayarina yazilir; kendi cizdigin esya gorunumleri zaten ayar
 * olarak durur. Boylece kodu / dosyayi alan kisi seninkinin birebir aynisini gorur.
 */
public final class ConfigProfiles {

    /** Ayar bolumleri. */
    public enum Category {
        KOZMETIK("Kozmetik", "Kanat, aura, şapka, pelerin, yoldaş, isim etiketi"),
        VURUS("Vuruş & PvP", "Vuruş stili, partikül, hasar sayısı, kombo, nişangah, totem, vurulma rengi"),
        OLDURME("Öldürme Efekti", "Efekt seçimi, ses, özel yazı"),
        GORUNUM("Görünüm & Kamera", "El görünümü, renk / oran / zoom, seçim kutusu, FOV, saat / hava, büyü parlaması, gökyüzü, sis, serbest kamera"),
        HUD("HUD", "İksir, eşya, bilgi, envanter, alınan / atılan, yerdeki eşyalar"),
        PERFORMANS("Performans", "Parçacık yoğunluğu, çizim mesafesi, varlık ayıklama"),
        ESYALAR("Eşya Görünümleri", "Kendi çizdiğin / paketten yüklediğin eşya dokuları");

        public final String label, desc;

        Category(String label, String desc) {
            this.label = label;
            this.desc = desc;
        }
    }

    public static final String CODE_PREFIX = "JPRIV1:";
    public static final String BACKUP = "_son_yuklemeden_once";
    /** Profil / kod en buyuk boyutu (gomulu ses ve gorseller dahil). */
    private static final int MAX_JSON = 64 * 1024 * 1024;
    private static final int MAX_FILES = 200;

    /** Paylasilmayan alan yok: her sey profile girer (dosyalar gomulur). */
    private static final Set<String> PRIVATE = Set.of();

    /** Profil icindeki gomulu dosyalar. */
    public static final String ASSETS = "$assets";

    /** Dosya gosteren alan: klasoru, en buyuk boyutu ve izinli uzantilari. */
    private record AssetField(String field, String folder, long max, Set<String> exts) {
    }

    private static final Set<String> IMAGE_EXT = Set.of("png", "jpg", "jpeg", "gif", "bmp");
    private static final Set<String> SOUND_EXT = Set.of("mp4", "m4a", "m4b", "mov", "3gp", "mp3", "wav", "ogg", "oga");
    private static final long MAX_IMAGE = 4L * 1024 * 1024, MAX_SOUND = 24L * 1024 * 1024;
    private static final List<AssetField> ASSET_FIELDS = List.of(new AssetField("totemSoundFile", "sounds", MAX_SOUND, SOUND_EXT),
            new AssetField("tagPrefixImage", "icons", MAX_IMAGE, IMAGE_EXT), new AssetField("tagSuffixImage", "icons", MAX_IMAGE, IMAGE_EXT));

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private ConfigProfiles() {
    }

    // ------------------------------------------------------------------ alan -> bolum

    private static boolean starts(String n, String p) {
        return n.startsWith(p) && (n.length() == p.length() || Character.isUpperCase(n.charAt(p.length())) || Character.isDigit(n.charAt(p.length())));
    }

    private static boolean any(String n, String... ps) {
        for (String p : ps) if (starts(n, p)) return true;
        return false;
    }

    /** Alanin bolumu (her alan tam olarak bir bolume duser). */
    public static Category categoryOf(String n) {
        if (n.equals("comboX") || n.equals("comboY") || n.equals("streakX") || n.equals("streakY")) return Category.HUD;
        if (n.equals("reachX") || n.equals("reachY") || n.equals("pingX") || n.equals("pingY") || n.equals("kfX") || n.equals("kfY") || n.equals("tfX")
                || n.equals("tfY")) return Category.HUD;
        if (any(n, "reach", "ping", "kf", "tf", "hb")) return Category.VURUS;
        if (any(n, "itemSkins")) return Category.ESYALAR;
        if (any(n, "particleDensity", "respectVanillaParticles", "effectRenderDistance", "frustumCulling", "cull")) return Category.PERFORMANS;
        if (any(n, "wing", "aura", "hat", "pet", "tag", "cape", "ely")) return Category.KOZMETIK;
        if (any(n, "swing", "hit", "crit", "pvp", "dmg", "combo", "killStreak", "streak", "ch", "totem")) return Category.VURUS;
        if (any(n, "effect", "mode", "mobKills", "kill", "volume", "screenDistance", "screenTitle", "maxSessions", "sound", "ct"))
            return Category.OLDURME;
        if (any(n, "vm", "cg", "aspect", "fire", "zoom", "time", "weather", "bo", "fov", "freeCam", "freeLook", "elytraSkin", "glint", "sky", "fog", "menu"))
            return Category.GORUNUM;
        if (any(n, "hud", "pot", "ic", "info", "inv", "pk", "gi")) return Category.HUD;
        return Category.PERFORMANS;
    }

    private static List<Field> fields() {
        List<Field> out = new ArrayList<>();
        for (Field f : ModConfig.class.getDeclaredFields()) {
            int m = f.getModifiers();
            if (Modifier.isStatic(m) || Modifier.isTransient(m) || !Modifier.isPublic(m) || Modifier.isFinal(m)) continue;
            out.add(f);
        }
        return out;
    }

    // ------------------------------------------------------------------ disa / ice aktarma

    /** Secili bolumlerin ayarlari (kisisel alanlar haric). */
    public static JsonObject extract(ModConfig c, Set<Category> cats) {
        JsonObject all = GSON.toJsonTree(c).getAsJsonObject();
        JsonObject out = new JsonObject();
        for (Field f : fields()) {
            String n = f.getName();
            if (PRIVATE.contains(n) || !cats.contains(categoryOf(n)) || !all.has(n)) continue;
            out.add(n, all.get(n));
        }
        return out;
    }

    // ------------------------------------------------------------------ gomulu dosyalar

    private static Path baseDir() {
        return FabricLoader.getInstance().getConfigDir().resolve("jestronpriv");
    }

    private static String ext(String name) {
        int i = name.lastIndexOf('.');
        return i < 0 ? "" : name.substring(i + 1).toLowerCase(Locale.ROOT);
    }

    static String sha(byte[] b) {
        try {
            byte[] d = java.security.MessageDigest.getInstance("SHA-256").digest(b);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) sb.append(String.format(Locale.ROOT, "%02x", d[i]));
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            return Integer.toHexString(java.util.Arrays.hashCode(b));
        }
    }

    /** Son buyuk ses donusumu (ayni dosya her kayitta yeniden cozulmesin). */
    private static String wavKey;
    private static byte[] wavData;

    /** Dosya okuma: buyuk ses dosyasi (uzun video vb.) oyunun caldigi kadariyla WAV'a cevrilir. */
    private static byte[] readAsset(AssetField af, Path f, String[] nameOut) throws IOException {
        long size = Files.size(f);
        if (size <= af.max()) return Files.readAllBytes(f);
        if (!"sounds".equals(af.folder())) return null;
        String key = f + "|" + size + "|" + Files.getLastModifiedTime(f).toMillis();
        if (!key.equals(wavKey)) {
            com.jestron.jestronpriv.client.audio.AudioDecoder.Pcm pcm = com.jestron.jestronpriv.client.audio.AudioDecoder.decode(f,
                    com.jestron.jestronpriv.client.audio.CustomSound.MAX_DECODE);
            wavData = wav(pcm.data, pcm.channels, pcm.rate);
            wavKey = key;
        }
        if (wavData == null || wavData.length > af.max()) return null;
        String n = f.getFileName().toString();
        int dot = n.lastIndexOf('.');
        nameOut[0] = (dot > 0 ? n.substring(0, dot) : n) + ".wav";
        return wavData;
    }

    /** 16 bit tek kanal WAV (cok kanalli ses ortalanir). */
    static byte[] wav(short[] data, int channels, int rate) {
        int ch = Math.max(1, channels);
        int frames = data.length / ch;
        ByteArrayOutputStream bo = new ByteArrayOutputStream(44 + frames * 2);
        java.nio.ByteBuffer h = java.nio.ByteBuffer.allocate(44).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        h.put("RIFF".getBytes(StandardCharsets.US_ASCII)).putInt(36 + frames * 2).put("WAVE".getBytes(StandardCharsets.US_ASCII));
        h.put("fmt ".getBytes(StandardCharsets.US_ASCII)).putInt(16).putShort((short) 1).putShort((short) 1).putInt(rate).putInt(rate * 2)
                .putShort((short) 2).putShort((short) 16);
        h.put("data".getBytes(StandardCharsets.US_ASCII)).putInt(frames * 2);
        bo.write(h.array(), 0, 44);
        byte[] out = new byte[frames * 2];
        for (int i = 0; i < frames; i++) {
            int sum = 0;
            for (int c = 0; c < ch; c++) sum += data[i * ch + c];
            int v = sum / ch;
            out[i * 2] = (byte) v;
            out[i * 2 + 1] = (byte) (v >> 8);
        }
        bo.write(out, 0, out.length);
        return bo.toByteArray();
    }

    /** Ayarlar + secili bolumlerin gosterdigi dosyalar (profile gomulu). */
    public static JsonObject extractWithAssets(ModConfig c, Set<Category> cats) {
        JsonObject o = extract(c, cats);
        JsonObject assets = new JsonObject();
        for (AssetField af : ASSET_FIELDS) {
            if (!cats.contains(categoryOf(af.field()))) continue;
            try {
                Object v = ModConfig.class.getField(af.field()).get(c);
                String name = v instanceof String str ? str.trim() : "";
                if (name.isEmpty() || !name.equals(ModConfig.safeFileName(name))) continue;
                Path f = baseDir().resolve(af.folder()).resolve(name);
                if (!Files.isRegularFile(f)) continue;
                String[] nm = {name};
                byte[] data = readAsset(af, f, nm);
                if (data == null) continue;
                JsonObject a = new JsonObject();
                a.addProperty("name", nm[0]);
                a.addProperty("sha", sha(data));
                a.addProperty("data", Base64.getEncoder().encodeToString(data));
                assets.add(af.field(), a);
            } catch (Exception | LinkageError ignored) {
                // dosya okunamadi: sadece adi gider
            }
        }
        if (assets.size() > 0) o.add(ASSETS, assets);
        return o;
    }

    /**
     * Gomulu dosyalari bu bilgisayara yazar ve alanlari yazilan dosya adlarina cevirir. Ayni icerik zaten varsa yeniden
     * yazilmaz; ayni adda farkli dosya varsa ad sonuna kisa ozet eklenir (kendi dosyalarin asla ezilmez).
     */
    private static JsonObject materialize(JsonObject data, Set<Category> cats) {
        if (data == null || !data.has(ASSETS) || !data.get(ASSETS).isJsonObject()) return data;
        JsonObject d = data.deepCopy();
        JsonObject assets = d.getAsJsonObject(ASSETS);
        d.remove(ASSETS);
        for (AssetField af : ASSET_FIELDS) {
            if (!cats.contains(categoryOf(af.field())) || !assets.has(af.field()) || !assets.get(af.field()).isJsonObject()) continue;
            try {
                JsonObject a = assets.getAsJsonObject(af.field());
                String name = ModConfig.safeFileName(a.get("name").getAsString());
                if (name.isEmpty() || !af.exts().contains(ext(name))) continue;
                String b64 = a.get("data").getAsString();
                if (b64.length() > af.max() / 3 * 4 + 8) continue;
                byte[] bytes = Base64.getDecoder().decode(b64);
                if (bytes.length == 0 || bytes.length > af.max()) continue;
                String h = sha(bytes);
                Path dir = baseDir().resolve(af.folder());
                Files.createDirectories(dir);
                Path target = dir.resolve(name);
                if (Files.isRegularFile(target) && !(Files.size(target) == bytes.length && sha(Files.readAllBytes(target)).equals(h))) {
                    int dot = name.lastIndexOf('.');
                    name = name.substring(0, dot) + "_" + h.substring(0, 8) + name.substring(dot);
                    if (name.length() > 200) continue;
                    target = dir.resolve(name);
                }
                if (!Files.isRegularFile(target) || Files.size(target) != bytes.length) {
                    Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
                    Files.write(tmp, bytes);
                    try {
                        Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                    } catch (IOException e) {
                        Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
                d.addProperty(af.field(), name);
            } catch (Exception ignored) {
                // bozuk dosya: alan oldugu gibi kalir
            }
        }
        return d;
    }

    /** Sonuc: kac ayar uygulandi, hangi bolumler. */
    public record Applied(int count, Set<Category> cats) {
    }

    /**
     * Ayarlari mevcut yapilandirmaya uygular: yalnizca {@code cats} icindeki ve veride bulunan alanlar degisir.
     * Her alan ayri cozulur; bozuk bir deger digerlerini etkilemez. Sonda {@link ModConfig#sanitize()}.
     */
    public static Applied apply(ModConfig c, JsonObject data, Set<Category> cats) {
        return applyFields(c, materialize(data, cats), cats);
    }

    /** Sadece alanlar (gomulu dosyalar yazilmaz): onizleme / kod uretimi icin. */
    static Applied applyFields(ModConfig c, JsonObject data, Set<Category> cats) {
        int n = 0;
        Set<Category> touched = EnumSet.noneOf(Category.class);
        for (Field f : fields()) {
            String name = f.getName();
            if (PRIVATE.contains(name)) continue;
            Category cat = categoryOf(name);
            if (!cats.contains(cat)) continue;
            JsonElement e = data.get(name);
            if (e == null || e.isJsonNull()) continue;
            try {
                Object v = GSON.fromJson(e, f.getGenericType());
                if (v == null) continue;
                f.set(c, v);
                n++;
                touched.add(cat);
            } catch (Exception ignored) {
                // bu alan bozuk: atla
            }
        }
        c.sanitize();
        return new Applied(n, touched);
    }

    /** Veride hangi bolumler var. */
    public static Set<Category> categoriesIn(JsonObject data) {
        Set<Category> s = EnumSet.noneOf(Category.class);
        for (Field f : fields()) if (data.has(f.getName()) && !PRIVATE.contains(f.getName())) s.add(categoryOf(f.getName()));
        return s;
    }

    // ------------------------------------------------------------------ profil verisi

    /** Bir profil: ad, olusturulma, bolumler ve ayarlar. */
    public record Profile(String name, long created, Set<Category> cats, JsonObject cfg, Path file) {
    }

    private static JsonObject wrap(String name, Set<Category> cats, JsonObject cfg) {
        JsonObject o = new JsonObject();
        o.addProperty("jestronpriv_profile", 1);
        o.addProperty("name", name);
        o.addProperty("created", System.currentTimeMillis());
        com.google.gson.JsonArray a = new com.google.gson.JsonArray();
        for (Category c : cats) a.add(c.name());
        o.add("categories", a);
        o.add("settings", cfg);
        return o;
    }

    /** JSON metninden profil (sarili profil ya da duz jestronpriv.json). Gecersizse null. */
    public static Profile parse(String json, String fallbackName, Path file) {
        if (json == null || json.length() > MAX_JSON) return null;
        try {
            JsonElement root = JsonParser.parseString(json);
            if (!root.isJsonObject()) return null;
            JsonObject o = root.getAsJsonObject();
            JsonObject cfg;
            String name = fallbackName;
            long created = 0;
            if (o.has("settings") && o.get("settings").isJsonObject()) {
                cfg = o.getAsJsonObject("settings");
                if (o.has("name") && o.get("name").isJsonPrimitive()) name = o.get("name").getAsString();
                if (o.has("created") && o.get("created").isJsonPrimitive()) created = o.get("created").getAsLong();
            } else {
                cfg = o; // duz ayar dosyasi
            }
            Set<Category> cats = categoriesIn(cfg);
            if (o.has("categories") && o.get("categories").isJsonArray()) {
                Set<Category> listed = EnumSet.noneOf(Category.class);
                for (JsonElement e : o.getAsJsonArray("categories")) {
                    if (!e.isJsonPrimitive()) continue;
                    for (Category c : Category.values()) if (c.name().equals(e.getAsString())) listed.add(c);
                }
                if (!listed.isEmpty()) cats = listed;
            }
            if (cats.isEmpty()) return null;
            if (o.has("diff") && o.get("diff").isJsonPrimitive() && o.get("diff").getAsBoolean()) {
                // fark kodu: varsayilanlarin ustune uygula, bolumlerin tam halini cikar (alicida birebir ayni olur)
                ModConfig tmp = new ModConfig();
                applyFields(tmp, cfg, cats);
                JsonObject full = extract(tmp, cats);
                if (cfg.has(ASSETS)) full.add(ASSETS, cfg.get(ASSETS));
                cfg = full;
            }
            return new Profile(cleanName(name), created, cats, cfg, file);
        } catch (Exception e) {
            return null;
        }
    }

    /** Gorunen ad: kontrol karakteri / renk kodu yok, en cok 32 karakter. */
    public static String cleanName(String s) {
        String t = com.jestron.jestronpriv.tag.TagSettings.cleanText(s == null ? "" : s.trim(), 32).trim();
        return t.isEmpty() ? "Profil" : t;
    }

    // ------------------------------------------------------------------ paylasim kodu

    /** Sadece varsayilandan farkli ayarlar (kod kisalsin); eksik alanlar alicida varsayilan sayilir. */
    private static JsonObject diff(ModConfig c, Set<Category> cats) {
        return diff(extractWithAssets(c, cats), cats);
    }

    private static JsonObject diff(JsonObject full, Set<Category> cats) {
        JsonObject def = extract(new ModConfig(), cats), out = new JsonObject();
        for (String k : full.keySet()) {
            JsonElement v = full.get(k);
            if (!v.equals(def.get(k))) out.add(k, v);
        }
        return out;
    }

    /**
     * Panoya kopyalanacak paylasim kodu. Secili bolumlerin tamami aktarilir: varsayilandan farkli olanlar yazilir,
     * digerleri alicida varsayilana doner; sonuc gonderenin ayarlariyla birebir aynidir.
     */
    public static String toCode(String name, ModConfig c, Set<Category> cats) {
        return encode(cleanName(name), cats, diff(c, cats));
    }

    /** Kayitli profilin kodu (gomulu dosyalari profilden alinir; bu bilgisayardaki dosyalara bakilmaz). */
    public static String codeOf(Profile p) {
        ModConfig tmp = new ModConfig();
        applyFields(tmp, p.cfg(), EnumSet.allOf(Category.class));
        JsonObject full = extract(tmp, p.cats());
        if (p.cfg().has(ASSETS)) full.add(ASSETS, p.cfg().get(ASSETS));
        return encode(p.name(), p.cats(), diff(full, p.cats()));
    }

    private static String encode(String name, Set<Category> cats, JsonObject data) {
        JsonObject w = wrap(name, cats, data);
        w.addProperty("diff", true);
        byte[] raw = new Gson().toJson(w).getBytes(StandardCharsets.UTF_8);
        Deflater d = new Deflater(Deflater.BEST_COMPRESSION);
        d.setInput(raw);
        d.finish();
        ByteArrayOutputStream bo = new ByteArrayOutputStream(raw.length / 3 + 64);
        byte[] buf = new byte[4096];
        while (!d.finished()) bo.write(buf, 0, d.deflate(buf));
        d.end();
        return CODE_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(bo.toByteArray());
    }

    /** Paylasim kodunu (ya da yapistirilmis JSON'u) cozer. Gecersizse null. */
    public static Profile fromCode(String text) {
        if (text == null) return null;
        String s = text.trim();
        if (s.startsWith("{")) return parse(s, "Panodan", null);
        int i = s.indexOf(CODE_PREFIX);
        if (i < 0) return null;
        s = s.substring(i + CODE_PREFIX.length());
        StringBuilder b = new StringBuilder(s.length());
        for (int k = 0; k < s.length(); k++) {
            char ch = s.charAt(k);
            if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') || ch == '-' || ch == '_') b.append(ch);
            else if (!Character.isWhitespace(ch)) break; // kodun sonu
        }
        try {
            byte[] z = Base64.getUrlDecoder().decode(b.toString());
            Inflater inf = new Inflater();
            inf.setInput(z);
            ByteArrayOutputStream bo = new ByteArrayOutputStream(z.length * 4);
            byte[] buf = new byte[8192];
            try {
                while (!inf.finished()) {
                    int n = inf.inflate(buf);
                    if (n == 0 && (inf.needsInput() || inf.needsDictionary())) return null;
                    bo.write(buf, 0, n);
                    if (bo.size() > MAX_JSON) return null; // sikistirma bombasi korumasi
                }
            } finally {
                inf.end();
            }
            return parse(bo.toString(StandardCharsets.UTF_8), "Panodan", null);
        } catch (IllegalArgumentException | DataFormatException e) {
            return null;
        }
    }

    // ------------------------------------------------------------------ dosyalar

    public static Path dir() {
        return FabricLoader.getInstance().getConfigDir().resolve("jestronpriv").resolve("profiles");
    }

    /** Dosya adi icin guvenli ad (Turkce harfler korunur). */
    public static String fileName(String name) {
        StringBuilder b = new StringBuilder();
        for (char ch : cleanName(name).toCharArray()) {
            if (Character.isLetterOrDigit(ch) || ch == '-' || ch == '_') b.append(ch);
            else if (ch == ' ' || ch == '.') b.append('_');
        }
        String s = b.toString();
        if (s.isEmpty() || s.equals("_") || s.equals("__")) s = "profil";
        if (s.length() > 40) s = s.substring(0, 40);
        return s + ".json";
    }

    private static void writeAtomic(Path f, String text) throws IOException {
        Files.createDirectories(f.getParent());
        Path tmp = f.resolveSibling(f.getFileName() + ".tmp");
        try (Writer w = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
            w.write(text);
        }
        try {
            Files.move(tmp, f, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            Files.move(tmp, f, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /** Profili kaydeder (ayni addaki profilin uzerine yazar). */
    public static Path save(String name, ModConfig c, Set<Category> cats) throws IOException {
        Path f = dir().resolve(fileName(name));
        writeAtomic(f, GSON.toJson(wrap(cleanName(name), cats, extractWithAssets(c, cats))));
        return f;
    }

    /** Var olan profilin uzerine su anki ayarlari yazar (adi ve dosyasi korunur). */
    public static void overwrite(Profile p, ModConfig c, Set<Category> cats) throws IOException {
        Path f = p.file() != null && p.file().getParent() != null && p.file().getParent().equals(dir()) ? p.file() : dir().resolve(fileName(p.name()));
        writeAtomic(f, GSON.toJson(wrap(p.name(), cats, extractWithAssets(c, cats))));
    }

    /** Hazir profil verisini dosyaya yazar (ice aktarma). Ayni ad varsa "(2)" eklenir. */
    public static Path store(Profile p) throws IOException {
        String base = p.name();
        Path f = dir().resolve(fileName(base));
        for (int i = 2; Files.exists(f) && i < 100; i++) f = dir().resolve(fileName(base + " " + i));
        String name = f.getFileName().toString().replaceFirst("\\.json$", "").replace('_', ' ');
        writeAtomic(f, GSON.toJson(wrap(name, p.cats(), p.cfg())));
        return f;
    }

    /** Okunmus profil dosyalari: dosya degismedikce yeniden okunmaz / cozulmez (ekran donmaz). */
    private record Cached(long mtime, long size, Profile profile) {
    }

    private static final java.util.Map<Path, Cached> CACHE = new java.util.HashMap<>();

    /** Kayitli profiller (yeniden eskiye). Otomatik yedek listede en sonda. */
    public static synchronized List<Profile> list() {
        List<Profile> out = new ArrayList<>();
        Path d = dir();
        if (!Files.isDirectory(d)) {
            CACHE.clear();
            return out;
        }
        java.util.Set<Path> seen = new java.util.HashSet<>();
        try (Stream<Path> s = Files.list(d)) {
            s.filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json")).limit(MAX_FILES).forEach(p -> {
                try {
                    seen.add(p);
                    long size = Files.size(p), mt = Files.getLastModifiedTime(p).toMillis();
                    if (size > MAX_JSON) return;
                    Cached c = CACHE.get(p);
                    if (c == null || c.mtime != mt || c.size != size) {
                        String name = p.getFileName().toString().replaceFirst("(?i)\\.json$", "").replace('_', ' ');
                        Profile pr = parse(Files.readString(p, StandardCharsets.UTF_8), name, p);
                        c = new Cached(mt, size, pr == null ? null : new Profile(pr.name(), pr.created() > 0 ? pr.created() : mt, pr.cats(), pr.cfg(), p));
                        CACHE.put(p, c);
                    }
                    if (c.profile != null) out.add(c.profile);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
        CACHE.keySet().retainAll(seen);
        out.sort((a, b) -> {
            boolean ab = isBackup(a), bb = isBackup(b);
            if (ab != bb) return ab ? 1 : -1;
            return Long.compare(b.created(), a.created());
        });
        return out;
    }

    /** Kayitli profil sayisi (dosyalari okumadan; ana menu icin). */
    public static int count() {
        Path d = dir();
        if (!Files.isDirectory(d)) return 0;
        try (Stream<Path> s = Files.list(d)) {
            return (int) s.map(p -> p.getFileName().toString()).filter(n -> n.toLowerCase(Locale.ROOT).endsWith(".json") && !n.equals(BACKUP + ".json"))
                    .limit(MAX_FILES).count();
        } catch (IOException e) {
            return 0;
        }
    }

    public static boolean isBackup(Profile p) {
        return p.file() != null && p.file().getFileName().toString().equals(BACKUP + ".json");
    }

    public static void delete(Profile p) throws IOException {
        if (p.file() != null && p.file().getParent() != null && p.file().getParent().equals(dir())) Files.deleteIfExists(p.file());
    }

    /** Yuklemeden once tum ayarlarin yedegi (Geri Al icin). */
    public static void backup(ModConfig c) {
        try {
            writeAtomic(dir().resolve(BACKUP + ".json"),
                    GSON.toJson(wrap("Son yüklemeden önce (otomatik yedek)", EnumSet.allOf(Category.class), extract(c, EnumSet.allOf(Category.class)))));
        } catch (IOException ignored) {
        }
    }

    /** Otomatik yedek (yoksa null). */
    public static Profile lastBackup() {
        Path f = dir().resolve(BACKUP + ".json");
        if (!Files.isRegularFile(f)) return null;
        try (Reader r = Files.newBufferedReader(f, StandardCharsets.UTF_8)) {
            StringBuilder b = new StringBuilder();
            char[] buf = new char[8192];
            int n;
            while ((n = r.read(buf)) > 0 && b.length() <= MAX_JSON) b.append(buf, 0, n);
            return parse(b.toString(), "Yedek", f);
        } catch (IOException e) {
            return null;
        }
    }

    /** Disaridan birakilan dosyayi okur (.json ya da paylasim kodu iceren .txt). */
    public static Profile read(Path file) {
        try {
            if (!Files.isRegularFile(file) || Files.size(file) > MAX_JSON) return null;
            String text = Files.readString(file, StandardCharsets.UTF_8);
            String name = file.getFileName().toString().replaceFirst("\\.[^.]*$", "").replace('_', ' ');
            Profile p = text.trim().startsWith("{") ? parse(text, name, null) : fromCode(text);
            return p == null ? null : new Profile(p.name().equals("Panodan") ? cleanName(name) : p.name(), p.created(), p.cats(), p.cfg(), null);
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    public static String date(long t) {
        return t <= 0 ? "" : new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ROOT).format(new Date(t));
    }

    /** Bolumlerin kisa adi: "Kozmetik, HUD" ya da "Tümü". */
    public static String catsLabel(Set<Category> cats) {
        if (cats.size() == Category.values().length) return "Tümü";
        StringBuilder b = new StringBuilder();
        for (Category c : cats) {
            if (b.length() > 0) b.append(", ");
            b.append(c.label);
        }
        return b.toString();
    }
}
