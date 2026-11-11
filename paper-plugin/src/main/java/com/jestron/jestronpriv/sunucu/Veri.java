package com.jestron.jestronpriv.sunucu;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Oyuncu secimleri (oyuncular.yml). Degisince "kirli" isaretlenir; kayit metni ana is parcaciginda uretilir, diske
 * yazma tek bir arka plan is parcaciginda yapilir (sunucu takilmaz). Dosya once .tmp'ye yazilip yerine tasinir: yazma
 * yarida kesilse bile eski dosya bozulmaz.
 */
public final class Veri {

    /** Bir oyuncunun secimleri. */
    public static final class Secim {
        public String aura;
        public String ely;
        /** Baskalarinin aura / izlerini gorme. */
        public boolean gizle;
        /** Kendi auranda yuz hizasindaki parcaciklari gizle (birinci sahis gorusu kapanmasin). */
        public boolean yuzFiltre = true;
        /** Kendi auranı kendin gorme (baskalari gorur). */
        public boolean kendiGizle;
        /** Animasyon hizi 0.5 - 2. */
        public double hiz = 1.0;

        boolean bos() {
            return aura == null && ely == null && !gizle && yuzFiltre && !kendiGizle && hiz == 1.0;
        }
    }

    private final JavaPlugin plugin;
    private final File file;
    private final Map<UUID, Secim> map = new HashMap<>();
    private final ExecutorService yazici = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "JestronPriv-Kayit");
        t.setDaemon(true);
        return t;
    });
    private boolean kirli;

    public Veri(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "oyuncular.yml");
    }

    public void yukle() {
        map.clear();
        kirli = false;
        if (!file.isFile()) return;
        YamlConfiguration y = new YamlConfiguration();
        try {
            y.loadFromString(Files.readString(file.toPath(), StandardCharsets.UTF_8));
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.WARNING, "oyuncular.yml okunamadi, yedeklenip bos baslaniyor", e);
            try {
                Files.copy(file.toPath(), new File(file.getParentFile(), "oyuncular.yml.bozuk").toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {
                // yedek de alinamazsa bos devam
            }
            return;
        }
        ConfigurationSection oy = y.getConfigurationSection("oyuncular");
        if (oy == null) return;
        for (String k : oy.getKeys(false)) {
            UUID id;
            try {
                id = UUID.fromString(k);
            } catch (IllegalArgumentException e) {
                continue;
            }
            ConfigurationSection c = oy.getConfigurationSection(k);
            if (c == null) continue;
            Secim s = new Secim();
            s.aura = Auralar.get(c.getString("aura")) != null ? c.getString("aura") : null;
            s.ely = ElytraIzleri.get(c.getString("elytra")) != null ? c.getString("elytra") : null;
            s.gizle = c.getBoolean("gizle", false);
            s.yuzFiltre = c.getBoolean("yuz-filtresi", true);
            s.kendiGizle = c.getBoolean("kendi-gizle", false);
            s.hiz = hizSinirla(c.getDouble("hiz", 1.0));
            if (!s.bos()) map.put(id, s);
        }
    }

    static double hizSinirla(double h) {
        if (!(h >= 0.5)) return h != h ? 1.0 : 0.5; // NaN → 1
        return Math.min(2.0, h);
    }

    public Secim bul(UUID id) {
        return map.get(id);
    }

    public Secim al(UUID id) {
        return map.computeIfAbsent(id, k -> new Secim());
    }

    public void degisti(UUID id) {
        Secim s = map.get(id);
        if (s != null && s.bos()) map.remove(id);
        kirli = true;
    }

    /** Kirliyse arka planda yaz. Ana is parcaciginda cagrilir. */
    public void kaydetArkada() {
        if (!kirli) return;
        kirli = false;
        String metin = metin();
        try {
            yazici.execute(() -> yaz(metin));
        } catch (java.util.concurrent.RejectedExecutionException e) {
            yaz(metin);
        }
    }

    /** Kapanista: bekleyen yazmayi bitir, son hali hemen yaz. */
    public void kapat() {
        String metin = kirli ? metin() : null;
        kirli = false;
        yazici.shutdown();
        try {
            yazici.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (metin != null) yaz(metin);
    }

    private String metin() {
        YamlConfiguration y = new YamlConfiguration();
        for (Map.Entry<UUID, Secim> e : map.entrySet()) {
            Secim s = e.getValue();
            String k = "oyuncular." + e.getKey();
            if (s.aura != null) y.set(k + ".aura", s.aura);
            if (s.ely != null) y.set(k + ".elytra", s.ely);
            if (s.gizle) y.set(k + ".gizle", true);
            if (!s.yuzFiltre) y.set(k + ".yuz-filtresi", false);
            if (s.kendiGizle) y.set(k + ".kendi-gizle", true);
            if (s.hiz != 1.0) y.set(k + ".hiz", s.hiz);
        }
        return y.saveToString();
    }

    private synchronized void yaz(String metin) {
        try {
            File dir = file.getParentFile();
            if (!dir.isDirectory() && !dir.mkdirs()) throw new IOException("klasor olusturulamadi: " + dir);
            File tmp = new File(dir, "oyuncular.yml.tmp");
            Files.writeString(tmp.toPath(), metin, StandardCharsets.UTF_8);
            try {
                Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "oyuncular.yml yazilamadi", e);
        }
    }

    int boyut() {
        return map.size();
    }
}
