package com.jestron.efektplugin;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * EfektPlugin (Jestron): aura ve elytra izi efektlerini sunucu tarafinda calistirir. Parcaciklari sunucu olusturup
 * yakindaki oyunculara gonderir; modsuz (vanilla) oyuncular da gorur. Paper / Spigot / Purpur 1.20.1 - 1.21.x. Yapimci: Jestron.
 */
public final class EfektPlugin extends JavaPlugin implements Listener {

    static final String ONEK = "§d✦ §5Efekt §8» §7";

    private final Partikuller partikuller = new Partikuller();
    private final Map<UUID, Durum> durum = new HashMap<>();
    private final Ctx ctx = new Ctx();
    private final Gonderici gonderici = new Gonderici();
    private Veri veri;
    private Menuler menuler;
    private BukkitTask gorev, kayitGorev;
    private int hataSayisi;

    // ayarlar (config.yml)
    double gorusMesafesi = 48;
    int limit = 600;
    float yogunluk = 1f;
    double yayilim = 1;
    boolean auraAcik = true, elytraAcik = true, herOgeIzin;
    final Set<String> kapaliDunyalar = new HashSet<>();

    /** Oyuncu basina calisma durumu (kaydedilmez). */
    static final class Durum {
        double lx, ly, lz;
        boolean konumVar;
        double faz;
        int gonderilen;
        long tick;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ayarlariOku();
        veri = new Veri(this);
        veri.yukle();
        menuler = new Menuler(this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(menuler, this);
        Komut k = new Komut(this);
        for (String ad : Komut.KOMUTLAR) {
            PluginCommand c = getCommand(ad);
            if (c == null) {
                getLogger().warning("plugin.yml'de komut yok: " + ad);
                continue;
            }
            c.setExecutor(k);
            c.setTabCompleter(k);
        }
        gorev = getServer().getScheduler().runTaskTimer(this, this::tick, 1L, 1L);
        kayitGorev = getServer().getScheduler().runTaskTimer(this, () -> veri.kaydetArkada(), 200L, 200L);
        getLogger().info(Auralar.ALL.size() + " aura, " + ElytraIzleri.ALL.size() + " elytra izi yuklendi; " + veri.boyut() + " oyuncu secimi.");
    }

    @Override
    public void onDisable() {
        if (gorev != null) gorev.cancel();
        if (kayitGorev != null) kayitGorev.cancel();
        if (veri != null) veri.kapat();
        durum.clear();
    }

    void ayarlariOku() {
        reloadConfig();
        FileConfiguration c = getConfig();
        gorusMesafesi = Math.max(8, Math.min(128, c.getDouble("gorus-mesafesi", 48)));
        limit = Math.max(20, Math.min(5000, c.getInt("parcacik-limiti", 600)));
        yogunluk = (float) Math.max(0.1, Math.min(2.0, c.getDouble("yogunluk", 1.0)));
        yayilim = Math.max(0.5, Math.min(2.5, c.getDouble("yayilim", 1.0)));
        auraAcik = c.getBoolean("auralar", true);
        elytraAcik = c.getBoolean("elytra-izleri", true);
        herOgeIzin = c.getBoolean("her-oge-icin-izin", false);
        kapaliDunyalar.clear();
        for (String w : c.getStringList("kapali-dunyalar")) kapaliDunyalar.add(w.toLowerCase(Locale.ROOT));
    }

    Veri veri() {
        return veri;
    }

    Menuler menuler() {
        return menuler;
    }

    boolean izinli(Player p, String tur, String id) {
        if (!herOgeIzin) return true;
        return p.hasPermission("efektplugin." + tur + ".*") || p.hasPermission("efektplugin." + tur + "." + id);
    }

    /** id null → kapat. Mesaj gonderilir; basarili ise true. */
    boolean secAura(Player p, String id, boolean mesaj) {
        if (id != null) {
            Auralar.Aura a = Auralar.get(id);
            if (a == null) {
                if (mesaj) p.sendMessage(ONEK + "§cBöyle bir aura yok: §f" + id);
                return false;
            }
            if (!izinli(p, "aura", id)) {
                if (mesaj) p.sendMessage(ONEK + "§cBu aura için iznin yok.");
                return false;
            }
            veri.al(p.getUniqueId()).aura = id;
            veri.degisti(p.getUniqueId());
            if (mesaj) p.sendMessage(ONEK + "Aura: §e" + a.name());
            return true;
        }
        Veri.Secim s = veri.bul(p.getUniqueId());
        if (s != null && s.aura != null) {
            s.aura = null;
            veri.degisti(p.getUniqueId());
            if (mesaj) p.sendMessage(ONEK + "Aura kapatıldı.");
        }
        return true;
    }

    boolean secIz(Player p, String id, boolean mesaj) {
        if (id != null) {
            ElytraIzleri.Iz z = ElytraIzleri.get(id);
            if (z == null) {
                if (mesaj) p.sendMessage(ONEK + "§cBöyle bir elytra izi yok: §f" + id);
                return false;
            }
            if (!izinli(p, "elytra", id)) {
                if (mesaj) p.sendMessage(ONEK + "§cBu iz için iznin yok.");
                return false;
            }
            veri.al(p.getUniqueId()).ely = id;
            veri.degisti(p.getUniqueId());
            if (mesaj) p.sendMessage(ONEK + "Elytra izi: §b" + z.name() + " §8(elytra ile süzülürken görünür)");
            return true;
        }
        Veri.Secim s = veri.bul(p.getUniqueId());
        if (s != null && s.ely != null) {
            s.ely = null;
            veri.degisti(p.getUniqueId());
            if (mesaj) p.sendMessage(ONEK + "Elytra izi kapatıldı.");
        }
        return true;
    }

    @EventHandler
    public void cikis(PlayerQuitEvent e) {
        durum.remove(e.getPlayer().getUniqueId());
    }

    // ---------------------------------------------------------------- tick

    private Durum durumu(Player p) {
        return durum.computeIfAbsent(p.getUniqueId(), k -> new Durum());
    }

    private static boolean gizlenmis(Player p) {
        if (p.isDead() || p.getGameMode() == GameMode.SPECTATOR || p.isInvisible() || p.isSleeping()) return true;
        // Vanish eklentileri (SuperVanish, PremiumVanish, EssentialsX ...) "vanished" metadatasi koyar
        for (MetadataValue v : p.getMetadata("vanished")) if (v.asBoolean()) return true;
        return false;
    }

    private void tick() {
        List<? extends Player> online = new ArrayList<>(getServer().getOnlinePlayers());
        for (Player p : online) durumu(p).gonderilen = 0;
        for (Player owner : online) {
            try {
                birOyuncu(owner, online);
            } catch (Throwable t) {
                // Bir desen ya da eklenti catismasi sunucuyu asla bozmasin; ilk birkac hata loglanir
                if (hataSayisi++ < 5) getLogger().log(Level.WARNING, "Efekt hatasi (" + owner.getName() + ")", t);
            }
        }
    }

    private void birOyuncu(Player owner, List<? extends Player> online) {
        Durum d = durumu(owner);
        Location loc = owner.getLocation();
        double vx = loc.getX() - d.lx, vy = loc.getY() - d.ly, vz = loc.getZ() - d.lz;
        boolean onceki = d.konumVar;
        d.lx = loc.getX();
        d.ly = loc.getY();
        d.lz = loc.getZ();
        d.konumVar = true;
        d.tick++;

        Veri.Secim s = veri.bul(owner.getUniqueId());
        if (s == null) return;
        Auralar.Aura aura = auraAcik && s.aura != null ? Auralar.get(s.aura) : null;
        ElytraIzleri.Iz iz = elytraAcik && s.ely != null && owner.isGliding() ? ElytraIzleri.get(s.ely) : null;
        if (aura == null && iz == null) return;
        if (gizlenmis(owner)) return;
        World w = loc.getWorld();
        if (w == null || kapaliDunyalar.contains(w.getName().toLowerCase(Locale.ROOT))) return;
        if (aura != null && !izinli(owner, "aura", aura.id())) aura = null;
        if (iz != null && !izinli(owner, "elytra", iz.id())) iz = null;
        if (aura == null && iz == null) return;

        // izleyiciler: ayni dunyada, mesafe icinde, sahibini gorebilen, efektleri gizlememis oyuncular
        Gonderici g = gonderici;
        g.sifirla(owner, s, loc);
        double r2 = gorusMesafesi * gorusMesafesi;
        for (Player v : online) {
            if (v != owner) {
                if (v.getWorld() != w || !v.canSee(owner)) continue;
                Veri.Secim vs = veri.bul(v.getUniqueId());
                if (vs != null && vs.gizle) continue;
                Location vl = v.getLocation();
                double dx = vl.getX() - loc.getX(), dy = vl.getY() - loc.getY(), dz = vl.getZ() - loc.getZ();
                if (dx * dx + dy * dy + dz * dz > r2) continue;
            } else if (s.kendiGizle) continue;
            g.ekle(v, durumu(v));
        }
        if (g.n == 0) return;

        double hiz = s.hiz;
        Ctx c = ctx;
        c.sink = g;
        c.x = loc.getX();
        c.y = loc.getY();
        c.z = loc.getZ();
        double yaw = Math.toRadians(loc.getYaw());
        c.fx = -Math.sin(yaw);
        c.fz = Math.cos(yaw);
        c.speed = onceki ? Math.sqrt(vx * vx + vz * vz) : 0;
        // Isinlanma (buyuk sicrama) hareket sayilmaz
        if (c.speed > 4) c.speed = 0;
        c.moving = c.speed > 0.03;
        c.density = yogunluk;
        c.spread = yayilim;
        c.speedMul = hiz;
        d.faz += hiz;
        c.time(d.faz, hiz);

        if (aura != null) aura.tick().accept(c);
        if (iz != null) elytraIzi(c, iz, d.tick, onceki ? vy : 0);
        c.sink = null;
    }

    /** Suzulurken iki kanat ucundan renkli iz + desenin kendi parcacigi. */
    private void elytraIzi(Ctx c, ElytraIzleri.Iz iz, long tick, double vy) {
        // Suzulurken govde yataya yakin: kanat uclari omuz hizasinda iki yanda, biraz geride
        for (int k = -1; k <= 1; k += 2) {
            double[] o = c.local(k * 0.95, 0.35, -0.35);
            c.dust(iz.c2(), 1.1f, o[0], o[1], o[2]);
            if ((tick & 1) == 0) c.fade(iz.c3(), iz.c1(), 0.8f, o[0] * 0.8, o[1] + 0.05, o[2] * 0.8);
            if (iz.particle() != null && c.r.nextFloat() < iz.rate() * 0.45f) {
                double px = o[0] + c.rd(-0.1, 0.1), pz = o[2] + c.rd(-0.1, 0.1);
                int renk = tozRengi(iz.particle());
                if (renk >= 0) c.dust(renk, 0.9f, px, o[1], pz);
                else c.p(iz.particle(), px, o[1], pz);
            }
        }
        // Govde arkasinda ince orta cizgi (yeterince hizliyken)
        if (c.speed > 0.4 && (tick % 3) == 0) {
            double[] o = c.local(0, 0.3, -0.9);
            c.dust(iz.c3(), 0.6f, o[0], o[1], o[2]);
        }
    }

    /** "dust:RRGGBB" → renk, degilse -1. */
    static int tozRengi(String id) {
        if (id.length() != 11 || !id.startsWith("dust:")) return -1;
        try {
            return Integer.parseInt(id.substring(5), 16);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Parcaciklari izleyicilere gonderir; her izleyicinin tick basina limiti var, sahibine yuz filtresi uygulanir. */
    private final class Gonderici implements Ctx.Sink {
        Player[] v = new Player[16];
        Durum[] vd = new Durum[16];
        int n;
        Player owner;
        boolean yuzFiltre;
        double ox, oy, oz, ofx, ofz;

        void sifirla(Player owner, Veri.Secim s, Location loc) {
            for (int i = 0; i < n; i++) {
                v[i] = null;
                vd[i] = null;
            }
            n = 0;
            this.owner = owner;
            this.yuzFiltre = s.yuzFiltre;
            ox = loc.getX();
            oy = loc.getY();
            oz = loc.getZ();
            double yaw = Math.toRadians(loc.getYaw());
            ofx = -Math.sin(yaw);
            ofz = Math.cos(yaw);
        }

        void ekle(Player p, Durum d) {
            if (n == v.length) {
                v = java.util.Arrays.copyOf(v, n * 2);
                vd = java.util.Arrays.copyOf(vd, n * 2);
            }
            v[n] = p;
            vd[n++] = d;
        }

        /** Moddaki birinci sahis filtresiyle ayni: kafa ve yuz hizasi. */
        private boolean yuzde(double x, double y, double z) {
            double dx = x - ox, dy = y - oy, dz = z - oz;
            double h = Math.sqrt(dx * dx + dz * dz);
            if (dy > 1.1 && h < 1.15) return true;
            double f = dx * ofx + dz * ofz;
            return f > -0.2 && f < 1.6 && dy > 0.75 && Math.abs(dx * -ofz + dz * ofx) < 0.9;
        }

        private boolean gecer(int i, double x, double y, double z) {
            if (vd[i].gonderilen >= limit) return false;
            if (v[i] == owner && yuzFiltre && yuzde(x, y, z)) return false;
            vd[i].gonderilen++;
            return true;
        }

        @Override
        public void particle(String id, double x, double y, double z, double vx, double vy, double vz, int velMode) {
            if (partikuller.get(id) == null) return;
            for (int i = 0; i < n; i++) if (gecer(i, x, y, z)) partikuller.send(v[i], id, x, y, z, vx, vy, vz);
        }

        @Override
        public void dust(int rgb, float size, double x, double y, double z) {
            for (int i = 0; i < n; i++) if (gecer(i, x, y, z)) partikuller.sendDust(v[i], rgb, size, x, y, z);
        }

        @Override
        public void fade(int from, int to, float size, double x, double y, double z) {
            for (int i = 0; i < n; i++) if (gecer(i, x, y, z)) partikuller.sendFade(v[i], from, to, size, x, y, z);
        }
    }
}
