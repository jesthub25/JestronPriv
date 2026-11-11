package com.jestron.jestronpriv.client.itemskin;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.itemskin.SkinAnim;
import com.jestron.jestronpriv.itemskin.SkinImage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Doku kutuphanesi: hazir dokulari tek yerden secmek.
 * <ul>
 * <li><b>Mod klasoru</b> ({@code config/jestronpriv/items}): oraya attigin her PNG / GIF / JPG (yaninda .mcmeta varsa
 * animasyonlu) hazir doku olur. Adi bir esyanin adiysa ({@code diamond_sword.png}) "isme gore uygula" ile o esyaya
 * kendiliginden gecer.</li>
 * <li><b>Kaynak paketleri</b> (resourcepacks klasoru ya da surukle-birak .zip): paketin TUM esya / blok dokulari,
 * istedigini sec, istedigin esyaya uygula ya da mod klasorune kopyala.</li>
 * <li><b>Oyun dokulari</b>: vanilla + aktif paketlerin dokulari.</li>
 * </ul>
 * Okumalar sinirli (dosya / girdi boyutu, sayi), bozuk dosya atlanir.
 */
public final class TextureLibrary implements Closeable {

    public enum Kind {FOLDER, GAME, PACK}

    /** Bir doku: ad, grup ("Eşya" / "Blok" / "Klasör"), anahtar (dosya yolu, zip girdisi ya da kaynak konumu). */
    public record Entry(String name, String group, String key, String search) {
    }

    public static final int MAX_ENTRIES = 8000;
    static final long MAX_ENTRY = 4L * 1024 * 1024;
    private static final Pattern PACK_TEX = Pattern.compile("assets/([a-z0-9_.-]+)/textures/(item|block)/([a-z0-9_./-]+)\\.png");
    private static final Pattern SAFE = Pattern.compile("[^a-z0-9_.-]");

    public final Kind kind;
    public final Path pack;
    public final String label;
    private ZipFile zip;
    private List<Entry> entries;

    private TextureLibrary(Kind kind, Path pack, String label) {
        this.kind = kind;
        this.pack = pack;
        this.label = label;
    }

    public static TextureLibrary folder() {
        return new TextureLibrary(Kind.FOLDER, null, "Mod klasörü");
    }

    public static TextureLibrary game() {
        return new TextureLibrary(Kind.GAME, null, "Oyun dokuları");
    }

    public static TextureLibrary pack(Path p) {
        String n = p.getFileName().toString().replaceFirst("(?i)\\.zip$", "");
        return new TextureLibrary(Kind.PACK, p, n);
    }

    /** Kutuphane klasoru (yoksa olusturulur). */
    public static Path dir() {
        Path d = FabricLoader.getInstance().getConfigDir().resolve("jestronpriv").resolve("items");
        try {
            Files.createDirectories(d);
        } catch (IOException ignored) {
        }
        return d;
    }

    static String norm(String s) {
        return s.toLowerCase(Locale.ROOT).replace('ı', 'i').replace('ş', 's').replace('ğ', 'g').replace('ü', 'u').replace('ö', 'o').replace('ç', 'c');
    }

    // ------------------------------------------------------------------ listeleme

    /** Dokular (ilk cagrida listelenir, sonra onbellekte). Hata olursa bos liste. */
    public List<Entry> entries() {
        if (entries != null) return entries;
        List<Entry> out = new ArrayList<>();
        try {
            switch (kind) {
                case FOLDER -> listFolder(out);
                case PACK -> listPack(out);
                case GAME -> listGame(out);
            }
        } catch (Throwable ignored) {
        }
        out.sort(Comparator.comparing((Entry e) -> e.group).thenComparing(e -> e.name));
        entries = out;
        return out;
    }

    /** Yeniden listele (klasore dosya eklendi). */
    public void refresh() {
        entries = null;
    }

    private void listFolder(List<Entry> out) throws IOException {
        Path d = dir();
        try (Stream<Path> s = Files.list(d)) {
            s.filter(Files::isRegularFile).filter(SkinImport::isImage).sorted().limit(MAX_ENTRIES).forEach(p -> {
                String n = p.getFileName().toString();
                out.add(new Entry(n, "Klasör", n, norm(n)));
            });
        }
    }

    private void listPack(List<Entry> out) throws IOException {
        if (Files.isDirectory(pack)) {
            Path assets = pack.resolve("assets");
            if (!Files.isDirectory(assets)) return;
            try (Stream<Path> s = Files.walk(assets, 8)) {
                s.filter(Files::isRegularFile).limit(200_000).forEach(p -> {
                    if (out.size() >= MAX_ENTRIES) return;
                    String rel = pack.relativize(p).toString().replace('\\', '/');
                    add(out, rel);
                });
            }
            return;
        }
        if (zip == null) zip = new ZipFile(pack.toFile());
        Enumeration<? extends ZipEntry> en = zip.entries();
        while (en.hasMoreElements() && out.size() < MAX_ENTRIES) {
            ZipEntry e = en.nextElement();
            if (!e.isDirectory() && e.getSize() <= MAX_ENTRY) add(out, e.getName());
        }
    }

    private static void add(List<Entry> out, String rel) {
        if (rel.contains("..") || rel.contains("//")) return;
        Matcher m = PACK_TEX.matcher(rel);
        if (!m.matches()) return;
        String ns = m.group(1), path = m.group(3);
        String name = (ns.equals("minecraft") ? "" : ns + ":") + path;
        out.add(new Entry(name, m.group(2).equals("item") ? "Eşya" : "Blok", rel, norm(name)));
    }

    private void listGame(List<Entry> out) {
        Minecraft mc = Minecraft.getInstance();
        for (String dirName : new String[]{"textures/item", "textures/block"}) {
            Map<ResourceLocation, ?> m = mc.getResourceManager().listResources(dirName, rl -> rl.getPath().endsWith(".png"));
            for (ResourceLocation rl : m.keySet()) {
                if (out.size() >= MAX_ENTRIES) return;
                add(out, "assets/" + rl.getNamespace() + "/" + rl.getPath());
            }
        }
    }

    // ------------------------------------------------------------------ okuma

    /** Dokuyu acar (animasyonluysa kare kare). */
    public SkinImport.Result load(Entry e) {
        try {
            switch (kind) {
                case FOLDER -> {
                    Path p = dir().resolve(e.key).normalize();
                    if (!p.startsWith(dir().normalize())) return SkinImport.Result.fail("Geçersiz dosya");
                    return SkinImport.fromImageFile(p);
                }
                case GAME -> {
                    ResourceLocation rl = toLocation(e.key);
                    SkinAnim a = rl == null ? null : SkinImport.fromResource(Minecraft.getInstance(), rl);
                    return a == null ? SkinImport.Result.fail("Doku okunamadı") : SkinImport.Result.ok(a, e.name);
                }
                default -> {
                    byte[] b = bytes(e.key);
                    if (b == null) return SkinImport.Result.fail("Doku okunamadı");
                    byte[] mm = bytes(e.key + ".mcmeta");
                    return SkinImport.decode(b, label + " › " + e.name,
                            mm == null ? SkinAnim.DEFAULT_MS : SkinImport.frameMs(new String(mm, 0, Math.min(mm.length, 65536), StandardCharsets.UTF_8)));
                }
            }
        } catch (Throwable t) {
            return SkinImport.Result.fail("Okunamadı: " + t.getClass().getSimpleName());
        }
    }

    private static ResourceLocation toLocation(String rel) {
        // assets/<ns>/<yol>
        if (!rel.startsWith("assets/")) return null;
        String r = rel.substring(7);
        int s = r.indexOf('/');
        return s <= 0 ? null : ResourceLocation.tryParse(r.substring(0, s) + ":" + r.substring(s + 1));
    }

    /** Paketteki / oyundaki bir dosyanin baytlari (yoksa ya da cok buyukse null). */
    byte[] bytes(String rel) throws IOException {
        switch (kind) {
            case PACK -> {
                if (Files.isDirectory(pack)) {
                    Path p = pack.resolve(rel).normalize();
                    if (!p.startsWith(pack.normalize()) || !Files.isRegularFile(p) || Files.size(p) > MAX_ENTRY) return null;
                    return Files.readAllBytes(p);
                }
                if (zip == null) zip = new ZipFile(pack.toFile());
                ZipEntry z = zip.getEntry(rel);
                if (z == null || z.isDirectory() || z.getSize() > MAX_ENTRY) return null;
                try (InputStream in = zip.getInputStream(z)) {
                    byte[] b = in.readNBytes((int) MAX_ENTRY + 1);
                    return b.length > MAX_ENTRY ? null : b;
                }
            }
            case GAME -> {
                ResourceLocation rl = toLocation(rel);
                if (rl == null) return null;
                var res = Minecraft.getInstance().getResourceManager().getResource(rl);
                if (res.isEmpty()) return null;
                try (InputStream in = res.get().open()) {
                    byte[] b = in.readNBytes((int) MAX_ENTRY + 1);
                    return b.length > MAX_ENTRY ? null : b;
                }
            }
            default -> {
                Path p = dir().resolve(rel).normalize();
                if (!p.startsWith(dir().normalize()) || !Files.isRegularFile(p) || Files.size(p) > MAX_ENTRY) return null;
                return Files.readAllBytes(p);
            }
        }
    }

    // ------------------------------------------------------------------ mod klasorune kopyala

    /**
     * Dokuyu (ve varsa .mcmeta'sini) mod klasorune kopyalar; ayni adda dosya varsa ezmez, yanina numarali yazar.
     *
     * @return yazilan dosya adi ya da null
     */
    public String copyToFolder(Entry e) {
        if (kind == Kind.FOLDER) return e.key;
        try {
            byte[] b = bytes(e.key);
            if (b == null) return null;
            String base = e.key.substring(e.key.lastIndexOf('/') + 1).replaceFirst("\\.png$", "");
            // baska ad alanindaki doku: "ns__ad" (isme gore uygulamada dogru esyaya gitsin)
            Matcher m = PACK_TEX.matcher(e.key);
            if (m.matches() && !m.group(1).equals("minecraft")) base = m.group(1) + "__" + base;
            base = SAFE.matcher(base.toLowerCase(Locale.ROOT)).replaceAll("_");
            if (base.isEmpty()) base = "doku";
            Path d = dir();
            String name = base + ".png";
            for (int i = 2; Files.exists(d.resolve(name)); i++) {
                // ayni icerik zaten varsa yeniden yazma
                if (java.util.Arrays.equals(Files.readAllBytes(d.resolve(name)), b)) return name;
                name = base + "_" + i + ".png";
                if (i > 999) return null;
            }
            Files.write(d.resolve(name), b);
            byte[] mm = bytes(e.key + ".mcmeta");
            if (mm != null && mm.length < 65536) Files.write(d.resolve(name + ".mcmeta"), mm);
            return name;
        } catch (Throwable t) {
            return null;
        }
    }

    /** Disaridan birakilan goruntuyu mod klasorune kopyalar. */
    public static String importFile(Path src) {
        try {
            if (!Files.isRegularFile(src) || !SkinImport.isImage(src) || Files.size(src) > 16L * 1024 * 1024) return null;
            String fn = src.getFileName().toString();
            int dot = fn.lastIndexOf('.');
            String base = SAFE.matcher((dot > 0 ? fn.substring(0, dot) : fn).toLowerCase(Locale.ROOT)).replaceAll("_");
            String ext = dot > 0 ? fn.substring(dot + 1).toLowerCase(Locale.ROOT) : "png";
            if (base.isEmpty()) base = "doku";
            Path d = dir();
            byte[] b = Files.readAllBytes(src);
            String name = base + "." + ext;
            for (int i = 2; Files.exists(d.resolve(name)); i++) {
                if (java.util.Arrays.equals(Files.readAllBytes(d.resolve(name)), b)) return name;
                name = base + "_" + i + "." + ext;
                if (i > 999) return null;
            }
            Files.write(d.resolve(name), b);
            Path meta = src.resolveSibling(fn + ".mcmeta");
            if (Files.isRegularFile(meta) && Files.size(meta) < 65536) Files.copy(meta, d.resolve(name + ".mcmeta"));
            return name;
        } catch (Throwable t) {
            return null;
        }
    }

    // ------------------------------------------------------------------ isme gore

    /**
     * Dosya adindan esya kimligi: {@code diamond_sword.png} → minecraft:diamond_sword, {@code ns__ad.png} → ns:ad,
     * {@code minecraft_diamond_sword.png} (eski disa aktarma) da olur. Esya degilse null.
     */
    public static String itemIdFor(String fileName, java.util.function.Predicate<String> isItem) {
        String n = fileName.toLowerCase(Locale.ROOT);
        int dot = n.lastIndexOf('.');
        if (dot > 0) n = n.substring(0, dot);
        List<String> c = new ArrayList<>();
        int dd = n.indexOf("__");
        if (dd > 0) c.add(n.substring(0, dd) + ":" + n.substring(dd + 2));
        c.add("minecraft:" + n);
        if (n.startsWith("minecraft_")) c.add("minecraft:" + n.substring(10));
        for (String id : c) if (SkinImage.validId(id) && isItem.test(id)) return id;
        return null;
    }

    /**
     * Ayara bir gorunum yazar (boyut sinirlariyla).
     *
     * @return null = tamam, aksi halde Turkce hata
     */
    public static String store(ModConfig c, String id, SkinAnim a) {
        if (a == null || a.isEmpty()) {
            c.itemSkins.remove(id);
            return null;
        }
        String code = a.encode();
        if (code == null) return "Çok karmaşık: kare sayısını ya da çözünürlüğü azalt";
        long total = code.length();
        for (Map.Entry<String, String> e : c.itemSkins.entrySet()) if (!e.getKey().equals(id) && e.getValue() != null) total += e.getValue().length();
        if (total > ModConfig.MAX_ITEM_SKINS_TOTAL || (!c.itemSkins.containsKey(id) && c.itemSkins.size() >= ModConfig.MAX_ITEM_SKINS))
            return "Çok fazla özel görünüm var: bazılarını sil";
        c.itemSkins.put(id, code);
        return null;
    }

    @Override
    public void close() {
        if (zip != null) {
            try {
                zip.close();
            } catch (IOException ignored) {
            }
            zip = null;
        }
    }
}
