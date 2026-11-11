package com.jestron.jestronpriv.client.itemskin;

import com.jestron.jestronpriv.client.tag.CustomIcons;
import com.jestron.jestronpriv.itemskin.SkinAnim;
import com.jestron.jestronpriv.itemskin.SkinImage;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Esya gorunumu ice aktarma: PNG / JPG / BMP, animasyonlu GIF (her kare ayri, sureleriyle), kaynak paketi (zip ya da
 * klasor; esyanin dokusu kendiliginden bulunur, animasyonlu dokular .mcmeta suresiyle kare kare gelir) ve su anki
 * (vanilla / aktif paket) dokusu. Buyuk ya da kare olmayan goruntu uyarlanir.
 */
public final class SkinImport {

    private static final long MAX_FILE = 16L * 1024 * 1024;
    private static final long MAX_ENTRY = 4L * 1024 * 1024;
    private static final Pattern FRAMETIME = Pattern.compile("\"frametime\"\\s*:\\s*(\\d+)");

    private SkinImport() {
    }

    /** Sonuc: animasyon (tek kare de olabilir) ya da Turkce hata. */
    public record Result(SkinAnim anim, String error, String source) {
        static Result ok(SkinAnim a, String src) {
            return new Result(a, null, src);
        }

        static Result fail(String e) {
            return new Result(null, e, null);
        }

        public SkinImage image() {
            return anim == null ? null : anim.frames.get(0);
        }
    }

    public static boolean isImage(Path p) {
        String n = p.getFileName().toString().toLowerCase(Locale.ROOT);
        return n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".gif") || n.endsWith(".bmp");
    }

    public static boolean isPack(Path p) {
        String n = p.getFileName().toString().toLowerCase(Locale.ROOT);
        return n.endsWith(".zip") || (Files.isDirectory(p) && Files.isRegularFile(p.resolve("pack.mcmeta")));
    }

    /** Birakilan dosya: goruntu ya da paket. */
    public static Result fromPath(Path p, String itemId) {
        try {
            if (isPack(p)) return fromPack(p, itemId);
            if (isImage(p)) return fromImageFile(p);
            return Result.fail("Desteklenmeyen dosya: PNG, JPG, GIF, BMP ya da kaynak paketi (.zip) bırak");
        } catch (Throwable t) {
            return Result.fail("Okunamadı: " + t.getClass().getSimpleName());
        }
    }

    /** Dosya: GIF ise tum kareleri, PNG serit ise kare kare. */
    public static Result fromImageFile(Path p) throws IOException {
        if (!Files.isRegularFile(p) || Files.size(p) > MAX_FILE) return Result.fail("Dosya bulunamadı ya da çok büyük (en çok 16 MB)");
        String name = p.getFileName().toString();
        if (name.toLowerCase(Locale.ROOT).endsWith(".gif")) {
            List<BufferedImage> frames = new ArrayList<>();
            List<Integer> delays = new ArrayList<>();
            CustomIcons.decode(p, frames, delays);
            if (frames.isEmpty()) return Result.fail("GIF okunamadı");
            int w = frames.get(0).getWidth(), h = frames.get(0).getHeight();
            List<int[]> px = new ArrayList<>();
            for (BufferedImage b : frames) {
                if (b.getWidth() != w || b.getHeight() != h) continue;
                px.add(b.getRGB(0, 0, w, h, null, 0, w));
            }
            SkinAnim a = SkinAnim.fromFrames(px, w, h, delays);
            return a == null ? Result.fail("GIF uyarlanamadı") : Result.ok(a, name + (a.animated() ? " · " + a.count() + " kare" : ""));
        }
        // yaninda .mcmeta varsa (kaynak paketinden kopyalanmis animasyonlu doku) kare suresi ondan
        Path meta = p.resolveSibling(name + ".mcmeta");
        String mm = null;
        try {
            if (Files.isRegularFile(meta) && Files.size(meta) < 65536) mm = Files.readString(meta, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
        return decode(Files.readAllBytes(p), name, mm == null ? SkinAnim.DEFAULT_MS : frameMs(mm));
    }

    /** Bayt dizisinden goruntu. Boyutu once basliktan okur: dev goruntuyu belege acmaz. Dikey serit = animasyon. */
    public static Result decode(byte[] bytes, String src, int frameMs) throws IOException {
        try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            if (in == null) return Result.fail("Görsel okunamadı");
            Iterator<ImageReader> it = ImageIO.getImageReaders(in);
            if (!it.hasNext()) return Result.fail("Görsel biçimi tanınmadı");
            ImageReader r = it.next();
            try {
                r.setInput(in, true, true);
                int w = r.getWidth(0), h = r.getHeight(0);
                if (w < 1 || h < 1 || w > 4096 || h > 8192 || (long) w * h > 16L * 1024 * 1024) return Result.fail("Görsel çok büyük");
                BufferedImage bi = r.read(0);
                int[] px = bi.getRGB(0, 0, w, h, null, 0, w);
                SkinAnim a = SkinAnim.fromStrip(w, h, px, frameMs);
                return a == null ? Result.fail("Görsel uyarlanamadı") : Result.ok(a, src + (a.animated() ? " · " + a.count() + " kare" : ""));
            } finally {
                r.dispose();
            }
        }
    }

    /** .mcmeta icindeki kare suresi (tick) -> ms; yoksa varsayilan. */
    public static int frameMs(String mcmeta) {
        if (mcmeta == null) return 50;
        Matcher m = FRAMETIME.matcher(mcmeta);
        if (m.find()) {
            try {
                return SkinAnim.clampMs(Integer.parseInt(m.group(1)) * 50);
            } catch (NumberFormatException ignored) {
            }
        }
        return 50;
    }

    // ------------------------------------------------------------------ kaynak paketi

    /** Esyanin paket icindeki olasi doku yollari (once esya, sonra blok dokusu). */
    static List<String> candidates(String itemId) {
        List<String> out = new ArrayList<>();
        int c = itemId.indexOf(':');
        String ns = c < 0 ? "minecraft" : itemId.substring(0, c), path = c < 0 ? itemId : itemId.substring(c + 1);
        out.add("assets/" + ns + "/textures/item/" + path + ".png");
        out.add("assets/" + ns + "/textures/block/" + path + ".png");
        for (String suf : new String[]{"_front", "_side", "_top"}) out.add("assets/" + ns + "/textures/block/" + path + suf + ".png");
        return out;
    }

    private static String shortName(Path pack, String rel) {
        return pack.getFileName() + " › " + rel.substring(rel.lastIndexOf('/') + 1);
    }

    public static Result fromPack(Path pack, String itemId) throws IOException {
        if (Files.isDirectory(pack)) {
            for (String rel : candidates(itemId)) {
                Path f = pack.resolve(rel);
                if (Files.isRegularFile(f) && Files.size(f) <= MAX_ENTRY) {
                    Path meta = pack.resolve(rel + ".mcmeta");
                    String mm = Files.isRegularFile(meta) && Files.size(meta) < 65536 ? Files.readString(meta, StandardCharsets.UTF_8) : null;
                    Result r = decode(Files.readAllBytes(f), shortName(pack, rel), frameMs(mm));
                    if (r.anim() != null) return r;
                }
            }
            return Result.fail("Bu pakette bu eşyanın dokusu yok");
        }
        if (!Files.isRegularFile(pack) || Files.size(pack) > 1024L * 1024 * 1024) return Result.fail("Paket bulunamadı");
        try (ZipFile z = new ZipFile(pack.toFile())) {
            for (String rel : candidates(itemId)) {
                ZipEntry e = z.getEntry(rel);
                if (e == null || e.isDirectory() || e.getSize() > MAX_ENTRY) continue;
                byte[] b;
                try (InputStream in = z.getInputStream(e)) {
                    b = in.readNBytes((int) MAX_ENTRY + 1);
                }
                if (b.length > MAX_ENTRY) continue;
                String mm = null;
                ZipEntry me = z.getEntry(rel + ".mcmeta");
                if (me != null && !me.isDirectory() && me.getSize() < 65536) {
                    try (InputStream in = z.getInputStream(me)) {
                        mm = new String(in.readNBytes(65536), StandardCharsets.UTF_8);
                    }
                }
                Result r = decode(b, shortName(pack, rel), frameMs(mm));
                if (r.anim() != null) return r;
            }
        }
        return Result.fail("Bu pakette bu eşyanın dokusu yok");
    }

    /** Oyunun resourcepacks klasorundeki paketler (zip ve klasor). */
    public static List<Path> installedPacks() {
        List<Path> out = new ArrayList<>();
        try {
            Path d = Minecraft.getInstance().getResourcePackDirectory();
            if (d == null || !Files.isDirectory(d)) return out;
            try (var s = Files.list(d)) {
                s.filter(SkinImport::isPack).sorted().limit(200).forEach(out::add);
            }
        } catch (Throwable ignored) {
        }
        return out;
    }

    // ------------------------------------------------------------------ su anki doku

    /** Esyanin su anki dokusu (vanilla ya da aktif kaynak paketi); animasyonlu dokular kare kare. */
    public static Result current(String itemId) {
        try {
            Minecraft mc = Minecraft.getInstance();
            for (String rel : candidates(itemId)) {
                String r = rel.substring("assets/".length());
                int slash = r.indexOf('/');
                ResourceLocation loc = ResourceLocation.tryParse(r.substring(0, slash) + ":" + r.substring(slash + 1));
                if (loc == null) continue;
                SkinAnim a = fromResource(mc, loc);
                if (a != null) return Result.ok(a, rel.substring(rel.lastIndexOf('/') + 1) + (a.animated() ? " · " + a.count() + " kare" : ""));
            }
        } catch (Throwable ignored) {
        }
        return Result.fail("Bu eşyanın düz dokusu yok (özel modelli eşya)");
    }

    /** Oyunun (vanilla + aktif paketler) bir dokusu; animasyonluysa .mcmeta suresiyle kare kare. Yoksa null. */
    public static SkinAnim fromResource(Minecraft mc, ResourceLocation loc) throws IOException {
        Optional<Resource> res = mc.getResourceManager().getResource(loc);
        if (res.isEmpty()) return null;
        String mm = null;
        ResourceLocation metaLoc = ResourceLocation.tryParse(loc + ".mcmeta");
        if (metaLoc != null) {
            Optional<Resource> meta = mc.getResourceManager().getResource(metaLoc);
            if (meta.isPresent()) {
                try (InputStream in = meta.get().open()) {
                    mm = new String(in.readNBytes(65536), StandardCharsets.UTF_8);
                }
            }
        }
        try (InputStream in = res.get().open(); NativeImage img = NativeImage.read(in)) {
            int w = img.getWidth(), h = img.getHeight();
            if (w < 1 || h < 1 || w > 4096 || h > 8192 || (long) w * h > 16L * 1024 * 1024) return null;
            int[] px = new int[w * h];
            for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) px[y * w + x] = img.getPixel(x, y);
            return SkinAnim.fromStrip(w, h, px, frameMs(mm));
        }
    }

    /** Kalip: herhangi bir esyanin ilk karesi (cizim tahtasinda soluk rehber). */
    public static SkinImage template(String itemId) {
        Result r = current(itemId);
        return r.image();
    }
}
