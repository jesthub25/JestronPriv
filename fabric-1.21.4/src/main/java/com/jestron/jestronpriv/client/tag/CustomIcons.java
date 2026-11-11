package com.jestron.jestronpriv.client.tag;

import com.jestron.jestronpriv.client.JestronPrivClient;
import net.fabricmc.loader.api.FabricLoader;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

/**
 * Isim etiketine konan kendi gorsellerin: config/jestronpriv/icons klasorundeki PNG / JPG / GIF (hareketli GIF
 * dahil) dosyalar. Arka planda cozulur, kucultulur; yerel cizim icin doku uretilir.
 */
public final class CustomIcons {

    public static final String[] EXTENSIONS = {"png", "jpg", "jpeg", "gif", "bmp"};
    /** Cizim kalitesi: en uzun kenar ve en cok kare. */
    public static final int LOCAL_SIDE = 64, LOCAL_FRAMES = 24;
    private static final int MAX_SOURCE_SIDE = 4096;

    private static final ExecutorService IO = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "JestronPriv-Gorsel");
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY);
        return t;
    });

    private static final class Frames {
        int w, h, n;
        int[] delays;
        int[] argb;
    }

    private static final class Entry {
        final String key;
        volatile Frames local;
        volatile String status = "Yükleniyor";
        volatile boolean failed;
        IconTexture tex;

        Entry(String key) {
            this.key = key;
        }
    }

    private static final Map<String, Entry> ENTRIES = new HashMap<>();

    private CustomIcons() {
    }

    public static Path folder() {
        return FabricLoader.getInstance().getConfigDir().resolve("jestronpriv").resolve("icons");
    }

    public static Path ensureFolder() {
        Path d = folder();
        try {
            Files.createDirectories(d);
        } catch (IOException ignored) {
        }
        return d;
    }

    public static boolean supported(Path p) {
        String n = p.getFileName() == null ? "" : p.getFileName().toString().toLowerCase(Locale.ROOT);
        for (String e : EXTENSIONS) if (n.endsWith("." + e)) return true;
        return false;
    }

    /** Klasordeki desteklenen dosyalar (ada gore). */
    public static List<String> files() {
        List<String> out = new ArrayList<>();
        Path d = folder();
        if (!Files.isDirectory(d)) return out;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(d)) {
            for (Path p : ds) if (Files.isRegularFile(p) && supported(p)) out.add(p.getFileName().toString());
        } catch (IOException ignored) {
        }
        out.sort(String.CASE_INSENSITIVE_ORDER);
        return out;
    }

    private static Path resolve(String name) {
        if (name == null || name.isBlank()) return null;
        Path base = folder().normalize();
        Path p = base.resolve(name).normalize();
        return p.startsWith(base) ? p : null;
    }

    private static String keyOf(String name) {
        Path p = resolve(name);
        if (p == null) return null;
        try {
            return name + "|" + Files.size(p) + "|" + Files.getLastModifiedTime(p).toMillis();
        } catch (IOException e) {
            return null;
        }
    }

    // anahtar hesaplamasi dosya sistemine dokunur: ad basina 2 sn'de bir
    private static final Map<String, String> KEY_CACHE = new HashMap<>();
    private static final Map<String, Long> KEY_TIME = new HashMap<>();

    private static Entry entry(String name) {
        if (name == null || name.isBlank()) return null;
        long now = System.currentTimeMillis();
        Long at = KEY_TIME.get(name);
        String key;
        if (at != null && now - at < 2000) {
            key = KEY_CACHE.get(name);
        } else {
            key = keyOf(name);
            KEY_CACHE.put(name, key);
            KEY_TIME.put(name, now);
        }
        if (key == null) return null;
        Entry e = ENTRIES.get(key);
        if (e == null) {
            // ayni adin eski surumlerini birak
            Iterator<Map.Entry<String, Entry>> it = ENTRIES.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Entry> me = it.next();
                if (me.getKey().startsWith(name + "|")) {
                    if (me.getValue().tex != null) me.getValue().tex.release();
                    it.remove();
                }
            }
            if (ENTRIES.size() > 32) clearAll();
            Entry ne = new Entry(key);
            ENTRIES.put(key, ne);
            Path p = resolve(name);
            IO.execute(() -> load(ne, p));
            e = ne;
        }
        return e;
    }

    /** Yerel cizim dokusu (hazir degilse null; yukleme baslar). Ana is parcacigindan. */
    public static IconTexture texture(String name) {
        Entry e = entry(name);
        if (e == null || e.failed) return null;
        if (e.tex == null && e.local != null) {
            Frames f = e.local;
            e.tex = IconTexture.create(f.w, f.h, f.n, f.delays, f.argb);
            if (e.tex == null) {
                e.failed = true;
                e.status = "Doku oluşturulamadı";
            }
        }
        return e.tex;
    }

    public static String status(String name) {
        if (name == null || name.isBlank()) return "Görsel seçilmedi";
        Entry e = entry(name);
        if (e == null) return "Dosya bulunamadı: " + name;
        return e.status;
    }

    public static void clearAll() {
        for (Entry e : ENTRIES.values()) if (e.tex != null) e.tex.release();
        ENTRIES.clear();
        KEY_CACHE.clear();
        KEY_TIME.clear();
    }

    /** Surukle-birak: desteklenen dosyalari klasore kopyalar. Kopyalanan adlar donulur. */
    public static List<String> importFiles(List<Path> paths) {
        List<String> out = new ArrayList<>();
        if (paths == null) return out;
        Path dir = ensureFolder();
        for (Path p : paths) {
            try {
                if (p == null || !Files.isRegularFile(p) || !supported(p)) continue;
                if (Files.size(p) > 32L * 1024 * 1024) continue;
                Path dst = dir.resolve(p.getFileName().toString());
                if (!p.toAbsolutePath().normalize().equals(dst.toAbsolutePath().normalize()))
                    Files.copy(p, dst, StandardCopyOption.REPLACE_EXISTING);
                out.add(dst.getFileName().toString());
            } catch (Throwable ignored) {
            }
        }
        KEY_TIME.clear();
        return out;
    }

    // ------------------------------------------------------------------ cozme (arka plan)

    private static void load(Entry e, Path p) {
        try {
            if (p == null) throw new IOException("Geçersiz dosya adı");
            if (Files.size(p) > 32L * 1024 * 1024) throw new IOException("Dosya çok büyük (en çok 32 MB)");
            List<BufferedImage> frames = new ArrayList<>();
            List<Integer> delays = new ArrayList<>();
            decode(p, frames, delays);
            if (frames.isEmpty()) throw new IOException("Görsel okunamadı");
            e.local = pack(frames, delays, LOCAL_SIDE, LOCAL_FRAMES);
            e.status = String.format(Locale.ROOT, "Hazır · %dx%d%s", e.local.w, e.local.h, e.local.n > 1 ? " · " + e.local.n + " kare" : "");
        } catch (Throwable t) {
            e.failed = true;
            String m = t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage();
            e.status = "Hata: " + m;
            JestronPrivClient.LOG.debug("JestronPriv gorsel yuklenemedi: {}", p, t);
        }
    }

    /** Goruntu / GIF karelerini (birlesik tuvalle) okur; esya gorunumu ice aktarmasi da kullanir. */
    public static void decode(Path p, List<BufferedImage> frames, List<Integer> delays) throws IOException {
        try (ImageInputStream in = ImageIO.createImageInputStream(p.toFile())) {
            if (in == null) throw new IOException("Dosya açılamadı");
            Iterator<ImageReader> it = ImageIO.getImageReaders(in);
            if (!it.hasNext()) throw new IOException("Desteklenmeyen biçim");
            ImageReader r = it.next();
            try {
                r.setInput(in, false, false);
                boolean gif = "gif".equalsIgnoreCase(r.getFormatName());
                int w0 = r.getWidth(0), h0 = r.getHeight(0);
                if (w0 < 1 || h0 < 1 || w0 > MAX_SOURCE_SIDE || h0 > MAX_SOURCE_SIDE) throw new IOException("Görsel boyutu desteklenmiyor (en çok 4096)");
                if (!gif) {
                    frames.add(toArgb(r.read(0)));
                    delays.add(100);
                    return;
                }
                int cw = w0, ch = h0;
                IIOMetadata sm = r.getStreamMetadata();
                if (sm != null) {
                    try {
                        IIOMetadataNode root = (IIOMetadataNode) sm.getAsTree("javax_imageio_gif_stream_1.0");
                        IIOMetadataNode lsd = child(root, "LogicalScreenDescriptor");
                        if (lsd != null) {
                            cw = Math.max(1, intAttr(lsd, "logicalScreenWidth", w0));
                            ch = Math.max(1, intAttr(lsd, "logicalScreenHeight", h0));
                        }
                    } catch (Throwable ignored) {
                    }
                }
                if (cw > MAX_SOURCE_SIDE || ch > MAX_SOURCE_SIDE) throw new IOException("Görsel boyutu desteklenmiyor");
                BufferedImage canvas = new BufferedImage(cw, ch, BufferedImage.TYPE_INT_ARGB);
                // kaynak karelerin hepsi okunur ama en cok 4x hedef kadar saklanir (sonra seyreltilir)
                int maxRead = 300;
                for (int i = 0; i < maxRead; i++) {
                    BufferedImage fr;
                    try {
                        fr = r.read(i);
                    } catch (IndexOutOfBoundsException end) {
                        break;
                    }
                    int left = 0, top = 0, delay = 10;
                    String disposal = "none";
                    try {
                        IIOMetadataNode root = (IIOMetadataNode) r.getImageMetadata(i).getAsTree("javax_imageio_gif_image_1.0");
                        IIOMetadataNode desc = child(root, "ImageDescriptor");
                        if (desc != null) {
                            left = intAttr(desc, "imageLeftPosition", 0);
                            top = intAttr(desc, "imageTopPosition", 0);
                        }
                        IIOMetadataNode gce = child(root, "GraphicControlExtension");
                        if (gce != null) {
                            delay = intAttr(gce, "delayTime", 10);
                            disposal = gce.getAttribute("disposalMethod");
                        }
                    } catch (Throwable ignored) {
                    }
                    BufferedImage before = "restoreToPrevious".equals(disposal) ? copy(canvas) : null;
                    Graphics2D g = canvas.createGraphics();
                    try {
                        g.drawImage(fr, left, top, null);
                    } finally {
                        g.dispose();
                    }
                    frames.add(copy(canvas));
                    delays.add(Math.max(20, delay * 10));
                    if ("restoreToBackgroundColor".equals(disposal)) {
                        Graphics2D g2 = canvas.createGraphics();
                        try {
                            g2.setComposite(AlphaComposite.Clear);
                            g2.fillRect(left, top, fr.getWidth(), fr.getHeight());
                        } finally {
                            g2.dispose();
                        }
                    } else if (before != null) {
                        canvas = before;
                    }
                    if (frames.size() >= LOCAL_FRAMES * 4) break;
                }
            } finally {
                r.dispose();
            }
        }
    }

    private static IIOMetadataNode child(IIOMetadataNode n, String name) {
        if (n == null) return null;
        for (int i = 0; i < n.getLength(); i++) {
            if (n.item(i) instanceof IIOMetadataNode c && name.equals(c.getNodeName())) return c;
        }
        return null;
    }

    private static int intAttr(IIOMetadataNode n, String a, int def) {
        try {
            String v = n.getAttribute(a);
            return v == null || v.isEmpty() ? def : Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static BufferedImage toArgb(BufferedImage src) {
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        try {
            g.drawImage(src, 0, 0, null);
        } finally {
            g.dispose();
        }
        return out;
    }

    private static BufferedImage copy(BufferedImage src) {
        return toArgb(src);
    }

    /** Kareleri seyreltir (sureleri birlestirir), en uzun kenari side olacak sekilde kucultur. */
    private static Frames pack(List<BufferedImage> frames, List<Integer> delays, int side, int maxFrames) {
        int n = frames.size();
        int keep = Math.min(n, maxFrames);
        int sw = frames.get(0).getWidth(), sh = frames.get(0).getHeight();
        float k = Math.min(1f, side / (float) Math.max(sw, sh));
        int tw = Math.max(1, Math.round(sw * k)), th = Math.max(1, Math.round(sh * k));
        Frames f = new Frames();
        f.w = tw;
        f.h = th;
        f.n = keep;
        f.delays = new int[keep];
        f.argb = new int[tw * th * keep];
        for (int i = 0; i < keep; i++) {
            int a = i * n / keep, b = (i + 1) * n / keep;
            int d = 0;
            for (int j = a; j < b; j++) d += delays.get(j);
            f.delays[i] = Math.max(20, Math.min(5000, d));
            BufferedImage scaled = scale(frames.get(a), tw, th);
            scaled.getRGB(0, 0, tw, th, f.argb, i * tw * th, tw);
        }
        return f;
    }

    /** Kaliteli kucultme: yariya yariya, sonra cift dogrusal. */
    private static BufferedImage scale(BufferedImage src, int tw, int th) {
        BufferedImage cur = src;
        int w = src.getWidth(), h = src.getHeight();
        while (w / 2 >= tw && h / 2 >= th) {
            w /= 2;
            h /= 2;
            cur = draw(cur, w, h);
        }
        return draw(cur, tw, th);
    }

    private static BufferedImage draw(BufferedImage src, int w, int h) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(src, 0, 0, w, h, null);
        } finally {
            g.dispose();
        }
        return out;
    }
}
