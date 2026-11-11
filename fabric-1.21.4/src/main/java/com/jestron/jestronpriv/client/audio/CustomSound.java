package com.jestron.jestronpriv.client.audio;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Totem icin ozel ses: cihazdaki ses / video dosyasi (MP4, M4A, MOV, 3GP, MP3, WAV, OGG) config/jestronpriv/sounds
 * klasorune konur (ya da ayar ekranina surukle-birak), arka planda cozulur ve totem patlayinca oyunun ses
 * motorundan calinir (oyuncu ses ayarina uyar). Baslangic, sure, ses seviyesi, giris / cikis, hiz ayarlanir.
 */
public final class CustomSound {

    /** sounds.json'daki akis sesi (bos .ogg'yi gosterir; gercek veri {@link PcmStream}'den gelir). */
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("jestronpriv", "totem_custom");
    /** Cozulen en uzun ses (sn); baslangic bu sinir icinde secilir. */
    public static final double MAX_DECODE = 180;
    public static final double MAX_LENGTH = 60;

    private static final ExecutorService IO = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "JestronPriv-Ses");
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY);
        return t;
    });

    private static volatile AudioDecoder.Pcm pcm;
    private static volatile String loadedKey = "", status = "Dosya seçilmedi";
    private static volatile boolean loading;
    private static SoundInstance playing;
    // vanilla totem sesini susturma: paket isleminin basinda kurulur, sonunda kalkar
    private static boolean muteArmed;

    private CustomSound() {
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    public static Path folder() {
        return FabricLoader.getInstance().getConfigDir().resolve("jestronpriv").resolve("sounds");
    }

    public static Path ensureFolder() {
        Path d = folder();
        try {
            Files.createDirectories(d);
        } catch (IOException ignored) {
        }
        return d;
    }

    /** Klasordeki desteklenen dosyalar (ada gore). */
    public static List<String> files() {
        List<String> out = new ArrayList<>();
        Path d = folder();
        if (!Files.isDirectory(d)) return out;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(d)) {
            for (Path p : ds) if (Files.isRegularFile(p) && AudioDecoder.supported(p)) out.add(p.getFileName().toString());
        } catch (IOException ignored) {
        }
        out.sort(String.CASE_INSENSITIVE_ORDER);
        return out;
    }

    public static String status() {
        return status;
    }

    public static boolean ready() {
        return pcm != null && !loading;
    }

    public static double loadedSeconds() {
        AudioDecoder.Pcm p = pcm;
        return p == null ? 0 : p.seconds();
    }

    /** Ozel ses bu totemde calacak mi (ayar acik, dosya hazir, kisi uygun). */
    public static boolean activeFor(Entity e) {
        ModConfig c = cfg();
        if (c == null || !c.totemCustom || pcm == null || e == null) return false;
        Minecraft mc = Minecraft.getInstance();
        boolean me = e == mc.player;
        return switch (c.totemSoundWho) {
            case "BEN" -> me;
            case "DIGER" -> !me;
            default -> true;
        };
    }

    /** Ayara gore secili dosyayi yukler (degistiyse). Ana is parcacigindan cagrilir. */
    public static void sync() {
        ModConfig c = cfg();
        if (c == null) return;
        String name = c.totemSoundFile == null ? "" : c.totemSoundFile.trim();
        if (name.isEmpty()) {
            pcm = null;
            loadedKey = "";
            status = "Dosya seçilmedi";
            return;
        }
        Path p = folder().resolve(name).normalize();
        if (!p.startsWith(folder().normalize())) {
            status = "Geçersiz dosya adı";
            return;
        }
        String key;
        try {
            key = name + "|" + Files.size(p) + "|" + Files.getLastModifiedTime(p).toMillis();
        } catch (IOException e) {
            pcm = null;
            loadedKey = "";
            status = "Dosya bulunamadı: " + name;
            return;
        }
        if (key.equals(loadedKey) || loading) return;
        load(p, key);
    }

    private static void load(Path p, String key) {
        loading = true;
        loadedKey = key;
        status = "Yükleniyor: " + p.getFileName();
        IO.execute(() -> {
            try {
                AudioDecoder.Pcm r = AudioDecoder.decode(p, MAX_DECODE);
                pcm = r;
                status = String.format(Locale.ROOT, "Hazır: %s · %.1f sn · %s", p.getFileName(), r.seconds(), r.channels == 1 ? "mono" : "stereo");
            } catch (Throwable t) {
                pcm = null;
                String msg = t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage();
                status = "Hata: " + msg;
                JestronPrivClient.LOG.warn("JestronPriv ozel ses yuklenemedi: {}", p, t);
            } finally {
                loading = false;
                // yukleme surerken baska dosya secildiyse hemen onu yukle (5 sn beklemeden)
                try {
                    Minecraft.getInstance().execute(CustomSound::sync);
                } catch (Throwable ignored) {
                }
            }
        });
    }

    /** Dosyayi yeniden yukle (ayni dosya degistirildiyse). */
    public static void reload() {
        loadedKey = "";
        sync();
    }

    /** Surukle-birak: desteklenen dosyalari klasore kopyalar, sonuncuyu secer. */
    public static void importFiles(List<Path> paths) {
        ModConfig c = cfg();
        if (c == null || paths == null || paths.isEmpty()) return;
        List<Path> ok = new ArrayList<>();
        for (Path p : paths) if (p != null && Files.isRegularFile(p) && AudioDecoder.supported(p)) ok.add(p);
        if (ok.isEmpty()) {
            status = "Desteklenmeyen dosya (mp4, m4a, mov, 3gp, mp3, wav, ogg)";
            return;
        }
        status = "Kopyalanıyor...";
        IO.execute(() -> {
            Path dir = ensureFolder();
            String last = null;
            for (Path p : ok) {
                try {
                    Path dst = dir.resolve(p.getFileName().toString());
                    if (!p.toAbsolutePath().normalize().equals(dst.toAbsolutePath().normalize()))
                        Files.copy(p, dst, StandardCopyOption.REPLACE_EXISTING);
                    last = dst.getFileName().toString();
                } catch (Throwable t) {
                    status = "Kopyalanamadı: " + p.getFileName();
                }
            }
            String sel = last;
            if (sel != null) {
                Minecraft.getInstance().execute(() -> {
                    c.totemSoundFile = sel;
                    c.totemCustom = true;
                    c.save();
                    loadedKey = "";
                    sync();
                });
            }
        });
    }

    private static int syncTicks = 99;

    /** Istemci tick'i: 5 sn'de bir dosya degisti mi bakar; ozellik kapaliysa bellegi bosaltir. */
    public static void tick(Minecraft mc) {
        ModConfig c = cfg();
        if (c == null || ++syncTicks < 100) return;
        syncTicks = 0;
        if (c.totemCustom || mc.screen instanceof com.jestron.jestronpriv.client.gui.TotemTimeScreen) sync();
        else release();
    }

    /** Cozulmus sesi bellekten atar (ozellik kapaliyken). */
    public static void release() {
        if (pcm == null || loading) return;
        pcm = null;
        loadedKey = "";
        status = "Kapalı";
    }

    // ------------------------------------------------------------------ calma

    /** Totem patladi: ozel sesi cal. */
    public static boolean playFor(Entity e) {
        if (!activeFor(e)) return false;
        Minecraft mc = Minecraft.getInstance();
        boolean me = e == mc.player;
        return play(me ? null : e);
    }

    /** Ayar ekranindan dene (konumsuz). */
    public static boolean preview() {
        ModConfig c = cfg();
        if (c == null) return false;
        if (pcm == null) {
            sync();
            return false;
        }
        return play(null);
    }

    /** e == null: konumsuz (kendi totemin / deneme); degilse onun konumunda tek kanal. */
    private static boolean play(Entity at) {
        ModConfig c = cfg();
        AudioDecoder.Pcm p = pcm;
        if (c == null || p == null) return false;
        Minecraft mc = Minecraft.getInstance();
        try {
            if (!c.totemSoundOverlap) stop();
            PcmStream stream = new PcmStream(p, c.totemSoundStart, c.totemSoundLength, c.totemSoundVolume, c.totemSoundFadeIn, c.totemSoundFadeOut,
                    at != null);
            if (stream.seconds() <= 0.01) {
                status = "Seçilen aralık boş (başlangıç sesin sonunda)";
                return false;
            }
            Instance inst = new Instance(stream, at, c.totemSoundSpeed);
            mc.getSoundManager().play(inst);
            playing = inst;
            return true;
        } catch (Throwable t) {
            status = "Çalınamadı: " + t.getClass().getSimpleName();
            return false;
        }
    }

    public static void stop() {
        SoundInstance s = playing;
        playing = null;
        if (s != null) {
            try {
                Minecraft.getInstance().getSoundManager().stop(s);
            } catch (Throwable ignored) {
            }
        }
    }

    // ------------------------------------------------------------------ vanilla sesini susturma

    public static void armMute(Entity e) {
        ModConfig c = cfg();
        muteArmed = c != null && c.totemSoundMuteVanilla && activeFor(e);
    }

    public static void disarmMute() {
        muteArmed = false;
    }

    /** ClientLevel.playLocalSound basinda: totem paketi islenirken vanilla totem sesi mi. */
    public static boolean shouldMute(SoundEvent ev) {
        if (!muteArmed || ev == null) return false;
        ResourceLocation l = ev.location();
        return l != null && "minecraft".equals(l.getNamespace()) && "item.totem.use".equals(l.getPath());
    }

    /** Oyunun ses motoruna akis olarak verilen ses. */
    static final class Instance extends AbstractSoundInstance {
        private final PcmStream stream;

        Instance(PcmStream stream, Entity at, float speed) {
            super(ID, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
            this.stream = stream;
            this.pitch = Float.isFinite(speed) ? Math.max(0.5f, Math.min(2f, speed)) : 1f;
            this.looping = false;
            this.delay = 0;
            if (at == null) {
                this.relative = true;
                this.attenuation = SoundInstance.Attenuation.NONE;
                this.volume = 1f;
                this.x = this.y = this.z = 0;
            } else {
                this.relative = false;
                this.attenuation = SoundInstance.Attenuation.LINEAR;
                this.volume = 2f;   // duyulma mesafesi 32 blok (ses gucu yine %100 ile sinirli; guc PCM'de)
                this.x = at.getX();
                this.y = at.getY() + at.getBbHeight() * 0.5;
                this.z = at.getZ();
            }
        }

        @Override
        public CompletableFuture<AudioStream> getAudioStream(SoundBufferLibrary loader, ResourceLocation id, boolean repeatInstantly) {
            return CompletableFuture.completedFuture(stream);
        }
    }
}
