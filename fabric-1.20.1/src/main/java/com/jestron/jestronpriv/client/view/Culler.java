package com.jestron.jestronpriv.client.view;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Varlik ayiklama: kameradan varligin kutusuna isinlar gonderilir; hepsi tam opak bloklara (tas, toprak...)
 * carpiyorsa varlik o karede cizilmez. Isinlar karede butceyle sinirlidir; gorunur olanlar birkac karede bir,
 * gizlenenler her karede yeniden denenir (yeni gorunen hemen cizilir, gec "belirme" olmaz). Parlayan, binilen,
 * oyuncunun kendisi, cok buyuk varliklar ve ekran disinda da cizilmesi gereken blok varliklari (fener isini)
 * asla ayiklanmaz. Kamera bir blogun icindeyse o kare kapali.
 */
public final class Culler {

    /** Izgara: true = gorusu tamamen kapatan blok. */
    public interface Grid {
        boolean solid(int x, int y, int z);
    }

    private static final int CAP = 1 << 13;
    private static final LongCache ENT = new LongCache(CAP), BLK = new LongCache(CAP);
    private static final BlockPos.MutableBlockPos MUT = new BlockPos.MutableBlockPos();
    private static long frame;
    private static int rays, budget, refresh, points;
    private static boolean active, cameraBlocked;
    private static double cx, cy, cz;
    private static ClientLevel level;
    private static final Grid WORLD = (x, y, z) -> {
        ClientLevel l = level;
        return l != null && l.getBlockState(MUT.set(x, y, z)).isSolidRender();
    };
    // istatistik (son kare)
    public static int lastEntities, lastBlocks, lastRays;
    private static int curEntities, curBlocks;
    private static boolean failed;

    private Culler() {
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    /** Dunya degisti / cikildi: eski dunyaya baglanti birakilir, onbellek bosaltilir. */
    public static void onLevelChange() {
        level = null;
        active = false;
        ENT.clear();
        BLK.clear();
    }

    /** Her karenin basinda (WorldRenderEvents.START). */
    public static void beginFrame() {
        lastEntities = curEntities;
        lastBlocks = curBlocks;
        lastRays = rays;
        curEntities = curBlocks = 0;
        rays = 0;
        frame++;
        active = false;
        ModConfig c = cfg();
        Minecraft mc = Minecraft.getInstance();
        if (failed || c == null || !c.cullEnabled || mc.level == null) {
            level = null;
            return;
        }
        if (mc.level != level) {
            ENT.clear();
            BLK.clear();
        }
        level = mc.level;
        if ((frame & 1023) == 0) {
            ENT.clear();
            BLK.clear();
        }
        switch (c.cullQuality) {
            case "HIZLI" -> {
                budget = 1500;
                refresh = 6;
                points = 5;
            }
            case "KALITE" -> {
                budget = 6000;
                refresh = 1;
                points = 9;
            }
            default -> {
                budget = 3000;
                refresh = 3;
                points = 9;
            }
        }
        Camera cam = mc.gameRenderer.getMainCamera();
        if (cam == null) return;
        Vec3 p = cam.getPosition();
        cx = p.x;
        cy = p.y;
        cz = p.z;
        cameraBlocked = WORLD.solid((int) Math.floor(cx), (int) Math.floor(cy), (int) Math.floor(cz));
        active = !cameraBlocked;
    }

    /** Blok varligi ayiklama bu karede acik mi (kapaliysa mixin renderer sorgusunu hic yapmaz). */
    public static boolean blocksActive() {
        if (!active) return false;
        ModConfig c = cfg();
        return c != null && c.cullBlockEntities;
    }

    /** true: bu varlik bu karede cizilmesin. */
    public static boolean cullEntity(Entity e) {
        if (!active) return false;
        ModConfig c = cfg();
        if (c == null || !c.cullEntities) return false;
        try {
            Minecraft mc = Minecraft.getInstance();
            if (e == mc.player || e == mc.getCameraEntity() || e.isCurrentlyGlowing()) return false;
            if (e instanceof Player && !c.cullPlayers) return false;
            if (c.cullKeepNamed && e.hasCustomName() && e.isCustomNameVisible()) return false;
            if (mc.player != null && (e.hasIndirectPassenger(mc.player) || mc.player.getVehicle() == e)) return false;
            AABB b = e.getBoundingBox();
            if (b.getXsize() > 6 || b.getYsize() > 6 || b.getZsize() > 6) return false;
            boolean hide = !visible(ENT, e.getId(), b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ);
            if (hide) curEntities++;
            return hide;
        } catch (Throwable t) {
            failed = true;
            return false;
        }
    }

    /** true: bu blok varligi bu karede cizilmesin. offScreen: renderer ekran disinda da cizilmeli diyor. */
    public static boolean cullBlock(BlockEntity be, boolean offScreen) {
        if (!active || offScreen) return false;
        ModConfig c = cfg();
        if (c == null || !c.cullBlockEntities) return false;
        try {
            BlockPos p = be.getBlockPos();
            boolean hide = !visible(BLK, p.asLong(), p.getX() - 0.05, p.getY() - 0.05, p.getZ() - 0.05, p.getX() + 1.05, p.getY() + 1.05,
                    p.getZ() + 1.05);
            if (hide) curBlocks++;
            return hide;
        } catch (Throwable t) {
            failed = true;
            return false;
        }
    }

    private static boolean visible(LongCache cache, long key, double x0, double y0, double z0, double x1, double y1, double z1) {
        double mx = (x0 + x1) / 2, my = (y0 + y1) / 2, mz = (z0 + z1) / 2;
        double dx = mx - cx, dy = my - cy, dz = mz - cz;
        double d2 = dx * dx + dy * dy + dz * dz;
        if (d2 < 3.0 * 3.0 || d2 > 160.0 * 160.0) return true;
        long v = cache.get(key);
        if (v != LongCache.MISSING) {
            boolean vis = (v & 1) == 1;
            long age = frame - (v >>> 1);
            // gorunur: birkac karede bir; gizli: her kare (butce elverdikce)
            if (vis && age < refresh) return true;
            if (!vis && rays + points > budget) return false;
        } else if (rays + points > budget) {
            return true;
        }
        boolean vis = test(WORLD, cx, cy, cz, x0, y0, z0, x1, y1, z1, points);
        rays += points;
        cache.put(key, (frame << 1) | (vis ? 1 : 0));
        return vis;
    }

    /**
     * Saf test: kameradan kutunun merkezine ve (points >= 9 ise) 8 kosesine, 5 ise merkez + 4 ust/alt kosegen
     * noktasina isin. Biri bile acik ise gorunur.
     */
    public static boolean test(Grid g, double sx, double sy, double sz, double x0, double y0, double z0, double x1, double y1, double z1, int points) {
        // kutuyu biraz kucult: koseler komsu bloklarin icine dusmesin
        double ix = (x1 - x0) * 0.08, iy = (y1 - y0) * 0.08, iz = (z1 - z0) * 0.08;
        double ax = x0 + ix, ay = y0 + iy, az = z0 + iz, bx = x1 - ix, by = y1 - iy, bz = z1 - iz;
        if (clear(g, sx, sy, sz, (ax + bx) / 2, (ay + by) / 2, (az + bz) / 2)) return true;
        if (points >= 9) {
            for (int i = 0; i < 8; i++) {
                if (clear(g, sx, sy, sz, (i & 1) == 0 ? ax : bx, (i & 2) == 0 ? ay : by, (i & 4) == 0 ? az : bz)) return true;
            }
        } else {
            if (clear(g, sx, sy, sz, ax, by, az)) return true;
            if (clear(g, sx, sy, sz, bx, by, bz)) return true;
            if (clear(g, sx, sy, sz, ax, ay, bz)) return true;
            if (clear(g, sx, sy, sz, bx, ay, az)) return true;
        }
        return false;
    }

    /** 3B DDA: baslangic hucresi ve hedef hucre engel sayilmaz. */
    public static boolean clear(Grid g, double sx, double sy, double sz, double ex, double ey, double ez) {
        int x = (int) Math.floor(sx), y = (int) Math.floor(sy), z = (int) Math.floor(sz);
        int tx = (int) Math.floor(ex), ty = (int) Math.floor(ey), tz = (int) Math.floor(ez);
        double dx = ex - sx, dy = ey - sy, dz = ez - sz;
        int stepX = dx > 0 ? 1 : dx < 0 ? -1 : 0, stepY = dy > 0 ? 1 : dy < 0 ? -1 : 0, stepZ = dz > 0 ? 1 : dz < 0 ? -1 : 0;
        double tdx = stepX == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dx);
        double tdy = stepY == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dy);
        double tdz = stepZ == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dz);
        double tmx = stepX == 0 ? Double.POSITIVE_INFINITY : (stepX > 0 ? (x + 1 - sx) : (sx - x)) * tdx;
        double tmy = stepY == 0 ? Double.POSITIVE_INFINITY : (stepY > 0 ? (y + 1 - sy) : (sy - y)) * tdy;
        double tmz = stepZ == 0 ? Double.POSITIVE_INFINITY : (stepZ > 0 ? (z + 1 - sz) : (sz - z)) * tdz;
        int guard = Math.abs(tx - x) + Math.abs(ty - y) + Math.abs(tz - z) + 1;
        while (guard-- > 0) {
            if (x == tx && y == ty && z == tz) return true;
            if (tmx <= tmy && tmx <= tmz) {
                if (tmx > 1.0) return true;
                x += stepX;
                tmx += tdx;
            } else if (tmy <= tmz) {
                if (tmy > 1.0) return true;
                y += stepY;
                tmy += tdy;
            } else {
                if (tmz > 1.0) return true;
                z += stepZ;
                tmz += tdz;
            }
            if (x == tx && y == ty && z == tz) return true;
            if (g.solid(x, y, z)) return false;
        }
        return true;
    }

    /** Kutulamasiz long -> long acik adresli tablo. */
    static final class LongCache {
        static final long MISSING = Long.MIN_VALUE;
        private final long[] keys, vals;
        private final boolean[] used;
        private final int mask;
        private int size;

        LongCache(int cap) {
            keys = new long[cap];
            vals = new long[cap];
            used = new boolean[cap];
            mask = cap - 1;
        }

        private static int hash(long k) {
            k ^= k >>> 33;
            k *= 0xff51afd7ed558ccdL;
            k ^= k >>> 33;
            return (int) k;
        }

        long get(long k) {
            int i = hash(k) & mask;
            for (int n = 0; n <= mask; n++) {
                if (!used[i]) return MISSING;
                if (keys[i] == k) return vals[i];
                i = (i + 1) & mask;
            }
            return MISSING;
        }

        void put(long k, long v) {
            if (size > (mask + 1) * 3 / 4) clear();
            int i = hash(k) & mask;
            while (used[i] && keys[i] != k) i = (i + 1) & mask;
            if (!used[i]) {
                used[i] = true;
                keys[i] = k;
                size++;
            }
            vals[i] = v;
        }

        void clear() {
            java.util.Arrays.fill(used, false);
            size = 0;
        }

        int size() {
            return size;
        }
    }
}
