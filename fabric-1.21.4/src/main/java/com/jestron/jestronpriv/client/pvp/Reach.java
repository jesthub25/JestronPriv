package com.jestron.jestronpriv.client.pvp;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Reach gostergesi: vurusunun mesafesi. Olcum, sunucunun da kullandigi yontemle yapilir: gozunden hedefin carpisma
 * kutusunun en yakin noktasina uzaklik. Sadece sunucunun dogruladigi (hasar olayi gelen) vuruslar sayilir; kalkan /
 * dokunulmazlik yuzunden islemeyen vuruslar istatistigi bozmaz. Hicbir sey degistirmez, sadece olcer.
 */
public final class Reach {

    /** Gecmis uzunlugu. */
    public static final int HIST = 10;
    private static final long CONFIRM_MS = 1500;

    private static int pendingId = -1;
    private static float pendingDist;
    private static long pendingAt;
    private static boolean pendingPlayer;

    private static final float[] hist = new float[HIST];
    private static int histN, histHead;
    private static float last = -1f, best, sum;
    private static int count;
    private static long lastAt;

    private Reach() {
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    /** Gozden kutunun en yakin noktasina uzaklik (kutunun icindeyse 0). */
    public static double distanceToBox(Vec3 eye, AABB bb) {
        return boxDistance(eye.x, eye.y, eye.z, bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
    }

    /** Noktadan eksene hizali kutuya en kisa uzaklik. */
    public static double boxDistance(double ex, double ey, double ez, double x0, double y0, double z0, double x1, double y1, double z1) {
        double dx = Math.max(Math.max(x0 - ex, 0), ex - x1);
        double dy = Math.max(Math.max(y0 - ey, 0), ey - y1);
        double dz = Math.max(Math.max(z0 - ez, 0), ez - z1);
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /** Yakin dovus saldirisi bir varliga gitti (istemci, saldiri aninda). */
    public static void onAttack(Entity target) {
        ModConfig c = cfg();
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (c == null || p == null || !c.reachHud || !(target instanceof LivingEntity)) return;
        double d = distanceToBox(p.getEyePosition(), target.getBoundingBox());
        if (!Double.isFinite(d) || d > 64) return;
        pendingId = target.getId();
        pendingDist = (float) d;
        pendingAt = Util.getMillis();
        pendingPlayer = target instanceof Player;
    }

    /** Sunucu, bekleyen hedefe senin verdigin hasari dogruladi. */
    public static void onConfirmedHit(int entityId) {
        if (entityId != pendingId || Util.getMillis() - pendingAt > CONFIRM_MS) return;
        ModConfig c = cfg();
        pendingId = -1;
        if (c == null || (!pendingPlayer && !c.reachMobs)) return;
        record(pendingDist);
    }

    static void record(float d) {
        last = d;
        lastAt = Util.getMillis();
        hist[histHead] = d;
        histHead = (histHead + 1) % HIST;
        if (histN < HIST) histN++;
        sum += d;
        count++;
        if (d > best) best = d;
    }

    public static float last() {
        return last;
    }

    public static long lastAt() {
        return lastAt;
    }

    public static float best() {
        return best;
    }

    public static int count() {
        return count;
    }

    public static float average() {
        return count == 0 ? 0f : sum / count;
    }

    public static int historySize() {
        return histN;
    }

    /** i = 0 en eski, histN - 1 en yeni. */
    public static float history(int i) {
        int start = (histHead - histN + HIST) % HIST;
        return hist[(start + i) % HIST];
    }

    /** Renk: kisa = mavi, normal = yesil, uzun (3 bloga yakin) = sari, 3 ustu (gecikme payi) = turuncu. */
    public static int color(float d) {
        if (d < 2.0f) return 0x6EC8FF;
        if (d < 2.7f) return 0x6EFF8A;
        if (d <= 3.0f) return 0xFFE066;
        return 0xFF9A3C;
    }

    public static void reset() {
        pendingId = -1;
        histN = histHead = 0;
        last = -1f;
        best = sum = 0f;
        count = 0;
        lastAt = 0;
    }

    /** Ayar ekranindan deneme: ornek vuruslar. */
    public static void demo() {
        float[] s = {2.41f, 2.86f, 3.02f, 2.67f, 1.94f, 2.95f};
        for (float v : s) record(v);
    }
}
