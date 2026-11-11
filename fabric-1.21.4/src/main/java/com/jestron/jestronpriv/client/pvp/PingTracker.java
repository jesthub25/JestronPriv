package com.jestron.jestronpriv.client.pvp;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/**
 * Gercek zamanli ping. Vanilla istemcinin F3 ag grafiginde kullandigi ping istegi (ServerboundPingRequestPacket)
 * saniyede bir gonderilir, sunucunun yaniti (ClientboundPongResponsePacket) gidis-donus suresini verir. Sekme
 * listesindeki ping sunucuda yarim dakikada bir guncellenir; bu olcum anliktir. Yanit gelmezse (eski surum /
 * ozel proxy) sekme listesi degerine duser.
 * <p>
 * Yanit ag thread'inde gelir: tum durum tek bir kilitle korunur, cizim kopya uzerinden okur.
 */
public final class PingTracker {

    /** Saklanan olcum sayisi (grafik ve istatistik). */
    public static final int N = 60;
    private static final Object LOCK = new Object();
    private static final int[] samples = new int[N];
    private static int head, size;
    private static float smooth = -1f;
    private static long lastPong, lastSend, spikeUntil;

    /** Son vurulan / nisan alinan oyuncu (rakip pingi icin). */
    private static UUID target;
    private static String targetName;
    private static long targetAt;

    private PingTracker() {
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    /** Istemci tick'i: gerekiyorsa ping istegi gonderir. */
    public static void tick(Minecraft mc) {
        ModConfig c = cfg();
        ClientPacketListener con = mc.getConnection();
        if (c == null || con == null || mc.player == null) return;
        if (!c.pingHud && !(c.infoHud)) return;
        long now = Util.getMillis();
        long interval = Math.round(Math.max(0.5f, Math.min(5f, c.pingInterval)) * 1000f);
        if (now - lastSend >= interval) {
            lastSend = now;
            con.send(new ServerboundPingRequestPacket(now));
        }
        // nisan alinan oyuncu rakip olarak hatirlanir
        if (mc.crosshairPickEntity instanceof Player pl && pl != mc.player) setTarget(pl);
    }

    /** Sunucu yaniti (ag thread'i olabilir). */
    public static void onPong(long sentTime) {
        long now = Util.getMillis();
        long rtt = now - sentTime;
        if (rtt < 0 || rtt > 30_000) return;
        synchronized (LOCK) {
            float avgBefore = size == 0 ? -1f : average0();
            samples[head] = (int) rtt;
            head = (head + 1) % N;
            if (size < N) size++;
            smooth = smooth < 0 ? rtt : smooth * 0.6f + rtt * 0.4f;
            lastPong = now;
            if (avgBefore > 0 && rtt > Math.max(150, avgBefore * 2.2f)) spikeUntil = now + 1500;
        }
    }

    /** Senin vurdugun oyuncu (rakip pingi). */
    public static void setTarget(Entity e) {
        if (!(e instanceof Player p)) return;
        target = p.getUUID();
        targetName = p.getGameProfile().getName();
        targetAt = Util.getMillis();
    }

    /** Gosterilecek ping (ms) ya da -1. */
    public static int current() {
        synchronized (LOCK) {
            if (smooth >= 0 && Util.getMillis() - lastPong < 6000) return Math.round(smooth);
        }
        return listPing();
    }

    /** Anlik olcum mu (false: sekme listesi degeri). */
    public static boolean live() {
        synchronized (LOCK) {
            return smooth >= 0 && Util.getMillis() - lastPong < 6000;
        }
    }

    public static boolean spike() {
        synchronized (LOCK) {
            return Util.getMillis() < spikeUntil;
        }
    }

    /** Sekme listesindeki kendi pingin. */
    public static int listPing() {
        Minecraft mc = Minecraft.getInstance();
        ClientPacketListener con = mc.getConnection();
        if (con == null || mc.player == null) return -1;
        PlayerInfo pi = con.getPlayerInfo(mc.player.getUUID());
        return pi == null ? -1 : pi.getLatency();
    }

    /** Istatistik: {en dusuk, ortalama, en yuksek, titreme} ya da null. out en az 4 eleman. */
    public static boolean stats(int[] out) {
        synchronized (LOCK) {
            if (size == 0) return false;
            int mn = Integer.MAX_VALUE, mx = 0;
            long s = 0, jit = 0;
            int prev = -1;
            int start = (head - size + N) % N;
            for (int i = 0; i < size; i++) {
                int v = samples[(start + i) % N];
                mn = Math.min(mn, v);
                mx = Math.max(mx, v);
                s += v;
                if (prev >= 0) jit += Math.abs(v - prev);
                prev = v;
            }
            out[0] = mn;
            out[1] = (int) (s / size);
            out[2] = mx;
            out[3] = size > 1 ? (int) (jit / (size - 1)) : 0;
            return true;
        }
    }

    private static float average0() {
        long s = 0;
        int start = (head - size + N) % N;
        for (int i = 0; i < size; i++) s += samples[(start + i) % N];
        return s / (float) size;
    }

    /** Grafik icin olcumleri kopyalar (eskiden yeniye). Doner: kopyalanan sayi. */
    public static int copySamples(int[] out) {
        synchronized (LOCK) {
            int n = Math.min(size, out.length);
            int start = (head - n + N) % N;
            for (int i = 0; i < n; i++) out[i] = samples[(start + i) % N];
            return n;
        }
    }

    /** Rakibin sekme listesindeki pingi (son 15 sn icinde vurdugun / nisan aldigin). -1 = yok. */
    public static int targetPing() {
        if (target == null || Util.getMillis() - targetAt > 15_000) return -1;
        ClientPacketListener con = Minecraft.getInstance().getConnection();
        if (con == null) return -1;
        PlayerInfo pi = con.getPlayerInfo(target);
        return pi == null ? -1 : pi.getLatency();
    }

    public static String targetName() {
        return targetName == null ? "" : targetName;
    }

    /** Ping rengi. */
    public static int color(int ms) {
        if (ms < 0) return 0xAAAAAA;
        if (ms <= 50) return 0x55FF7A;
        if (ms <= 100) return 0xB8FF55;
        if (ms <= 150) return 0xFFE066;
        if (ms <= 250) return 0xFF9A3C;
        return 0xFF4A4A;
    }

    /** 0 - 5 cubuk. */
    public static int bars(int ms) {
        if (ms < 0) return 0;
        if (ms <= 50) return 5;
        if (ms <= 100) return 4;
        if (ms <= 150) return 3;
        if (ms <= 250) return 2;
        return 1;
    }

    public static void reset() {
        synchronized (LOCK) {
            head = size = 0;
            smooth = -1f;
            lastPong = 0;
            spikeUntil = 0;
        }
        lastSend = 0;
        target = null;
        targetName = null;
    }
}
