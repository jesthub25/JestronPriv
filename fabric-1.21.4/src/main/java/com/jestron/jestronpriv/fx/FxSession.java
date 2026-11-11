package com.jestron.jestronpriv.fx;

import com.jestron.jestronpriv.fx.api.BlockDisplay;
import com.jestron.jestronpriv.fx.api.Color;
import com.jestron.jestronpriv.fx.api.Display;
import com.jestron.jestronpriv.fx.api.ItemDisplay;
import com.jestron.jestronpriv.fx.api.ItemStack;
import com.jestron.jestronpriv.fx.api.Location;
import com.jestron.jestronpriv.fx.api.Material;
import com.jestron.jestronpriv.fx.api.TextDisplay;
import com.jestron.jestronpriv.fx.api.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;

/**
 * Tek bir efekt calistirmasi. Tum zamanlama tick tabanlidir (0..49).
 * 50. tick'te (2.5 sn) tum nesneler silinir, uzun sesler durdurulur.
 */
public final class FxSession {

    public static final int END_TICK = 50;

    private final FxEngine engine;
    private final FxBackend backend;
    public final KillEffect effect;
    public final Location origin;
    /** Yatay birim vektor: oyuncudan efekt noktasina dogru (x, z). */
    public final double forwardX, forwardZ;
    /** +Z eksenini oyuncuya dogru ceviren Y acisi (radyan). */
    public final float faceYaw;
    public final String victimName;
    public final ItemStack victimHead;
    public final boolean screenMode;
    public final Random rnd = new Random();
    /** Zemin blogunun opak durumu (blok parcaciklari icin), yoksa null. */
    public final Object groundData;
    private final String groundId;
    private final boolean groundFull;

    private final Map<Integer, List<Runnable>> tasks = new HashMap<>();
    private final List<Display> displays = new ArrayList<>();
    private final Map<Display, Integer> spawnTicks = new IdentityHashMap<>();
    private final Set<String> longSounds = new HashSet<>();
    private final Map<Long, Double> groundCache = new HashMap<>();

    private int tick = 0;
    private boolean running = false;
    private boolean finished = false;
    private Throwable firstError;

    FxSession(FxEngine engine, FxBackend backend, KillEffect effect, String victimName, ItemStack victimHead,
              Location origin, double fwdX, double fwdZ, boolean screenMode) {
        this.engine = engine;
        this.backend = backend;
        this.effect = effect;
        this.victimName = victimName;
        this.victimHead = victimHead;
        this.origin = origin.clone();
        double len = Math.sqrt(fwdX * fwdX + fwdZ * fwdZ);
        if (len < 1e-6) {
            fwdX = 0;
            fwdZ = 1;
            len = 1;
        }
        this.forwardX = fwdX / len;
        this.forwardZ = fwdZ / len;
        this.faceYaw = (float) Math.atan2(-this.forwardX, -this.forwardZ);
        this.screenMode = screenMode;
        int bx = floor(origin.getX()), bz = floor(origin.getZ());
        int by = floor(origin.getY() - 0.05);
        Object gd = null;
        String gid = null;
        boolean full = false;
        for (int i = 0; i < 3; i++) {
            Object st = backend.groundState(bx, by - i, bz);
            if (st != null) {
                gd = st;
                gid = backend.blockId(bx, by - i, bz);
                full = backend.isFullSolid(bx, by - i, bz);
                break;
            }
        }
        this.groundData = gd;
        this.groundId = gid;
        this.groundFull = full;
    }

    private static int floor(double d) {
        return (int) Math.floor(d);
    }

    // ------------------------------------------------------------------ yasam dongusu

    void start() {
        try {
            effect.play(this);
        } catch (Throwable t) {
            recordError(t);
        }
        running = true;
    }

    /** Motor tarafindan her istemci tick'inde bir kez cagrilir. */
    void step() {
        if (finished) return;
        try {
            if (!backend.isLoaded(floor(origin.getX()), floor(origin.getZ()))) {
                finish();
                return;
            }
            List<Runnable> list = tasks.remove(tick);
            if (list != null) {
                for (int i = 0; i < list.size(); i++) runSafe(list.get(i));
            }
            if (tick == END_TICK - 5) fadeOutStragglers();
        } catch (Throwable t) {
            recordError(t);
        }
        tick++;
        if (tick >= END_TICK) finish();
    }

    /**
     * Guvenlik agi: 45. tick'te hala gorunen her parca, merkezini ve donusunu koruyarak 3 tick'te kuculur.
     * Boylece hicbir efekt parcasi 50. tick'teki temizlikte "pat" diye kaybolmaz.
     */
    private void fadeOutStragglers() {
        long now = backend.now();
        for (Display d : new ArrayList<>(displays)) {
            if (!d.isValid() || d.targetHidden()) continue;
            Transformation cur = d.sample(now, 0f);
            Vector3f sc = cur.getScale();
            if (Math.max(sc.x, Math.max(sc.y, sc.z)) <= 0.002f) continue;
            Quaternionf left = cur.getLeftRotation(), right = cur.getRightRotation();
            Vector3f tiny = new Vector3f(0.001f);
            Vector3f t = cur.getTranslation();
            if (d instanceof BlockDisplay) {
                // Blok modeli 0..1 kupu: merkez = T + L * (S . (R * 0.5))
                Vector3f half = right.transform(new Vector3f(0.5f));
                Vector3f center = new Vector3f(t).add(left.transform(new Vector3f(half).mul(sc)));
                t = new Vector3f(center).sub(left.transform(new Vector3f(half).mul(tiny)));
            }
            d.setTransformation(new Transformation(t, left, tiny, right), 3, now);
        }
    }

    public void finish() {
        if (finished) return;
        finished = true;
        for (Display d : displays) {
            d.remove();
            backend.removeDisplay(d);
        }
        displays.clear();
        for (String s : longSounds) {
            try {
                backend.stopSound(s);
            } catch (Throwable ignored) {
            }
        }
        tasks.clear();
        engine.ended(this);
    }

    public boolean isFinished() {
        return finished;
    }

    public int tick() {
        return tick;
    }

    public Throwable firstError() {
        return firstError;
    }

    private void runSafe(Runnable r) {
        try {
            r.run();
        } catch (Throwable t) {
            recordError(t);
        }
    }

    private void recordError(Throwable t) {
        if (firstError == null) {
            firstError = t;
            engine.reportError(effect.id(), t);
        }
    }

    // ------------------------------------------------------------------ zamanlama

    public void at(int t, Runnable r) {
        if (finished) return;
        if (running && t <= tick) {
            runSafe(r);
            return;
        }
        if (t >= END_TICK) return;
        tasks.computeIfAbsent(t, k -> new ArrayList<>()).add(r);
    }

    private void atFront(int t, Runnable r) {
        if (t >= END_TICK) return;
        tasks.computeIfAbsent(t, k -> new ArrayList<>()).add(0, r);
    }

    public void every(int from, int to, int step, IntConsumer c) {
        int st = Math.max(1, step);
        for (int t = from; t <= to && t < END_TICK; t += st) {
            final int tt = t;
            at(tt, () -> c.accept(tt));
        }
    }

    // ------------------------------------------------------------------ nesneler

    private <T extends Display> T register(T d) {
        if (finished) return d;
        d.setAnchor(origin.getX(), origin.getY() + 0.01, origin.getZ());
        displays.add(d);
        spawnTicks.put(d, running ? tick : 0);
        backend.addDisplay(d);
        return d;
    }

    public BlockDisplay block(Material m, Transformation t) {
        return register(new BlockDisplay(m, t, backend.now()));
    }

    public ItemDisplay item(ItemStack stack, Transformation t) {
        return register(new ItemDisplay(stack, t, backend.now()));
    }

    public ItemDisplay item(Material m, Transformation t) {
        return item(new ItemStack(m), t);
    }

    public TextDisplay text(String text, com.jestron.jestronpriv.fx.api.TextStyle style, Transformation t) {
        return register(new TextDisplay(text, style, t, backend.now()));
    }

    public TextDisplay text(String text, Transformation t) {
        return register(new TextDisplay(text, t, backend.now()));
    }

    /** Bu oturumun nesneleri icin dunya capasi (origin). */
    public Location anchor() {
        return origin;
    }

    public <T extends Display> T bright(T d) {
        if (d != null) d.setLight(Display.FULL_BRIGHT);
        return d;
    }

    public <T extends Display> T lit(T d) {
        if (d == null) return null;
        int packed;
        try {
            packed = backend.lightAt(floor(origin.getX()), floor(origin.getY() + 0.01), floor(origin.getZ()));
        } catch (Throwable t) {
            packed = (12 << 4) | (15 << 20);
        }
        int block = Math.max((packed >> 4) & 0xF, 10);
        int sky = (packed >> 20) & 0xF;
        d.setLight((block << 4) | (sky << 20));
        return d;
    }

    public <T extends Display> T dark(T d) {
        if (d != null) d.setLight(0);
        return d;
    }

    public void tf(Display d, Transformation t, int duration) {
        if (d == null || finished) return;
        Integer st = spawnTicks.get(d);
        int min = st == null ? 0 : st + 2;
        int now = running ? tick : 0;
        if (now < min) {
            atFront(min, () -> applyTf(d, t, duration));
            return;
        }
        applyTf(d, t, duration);
    }

    private void applyTf(Display d, Transformation t, int duration) {
        if (!d.isValid()) return;
        d.setTransformation(t, duration, backend.now());
    }

    public void tfAt(int t, Display d, Transformation tr, int duration) {
        at(t, () -> tf(d, tr, duration));
    }

    public void anim(Display d, int from, int to, int step, IntFunction<Transformation> f) {
        if (d == null) return;
        int st = Math.max(1, step);
        every(from, to, st, t -> tf(d, f.apply(t), st));
    }

    public void remove(Display d) {
        if (d == null) return;
        d.remove();
        backend.removeDisplay(d);
    }

    public void removeAt(int t, Display d) {
        at(t, () -> remove(d));
    }

    // ------------------------------------------------------------------ konum yardimcilari

    public Quaternionf face() {
        return FxMath.qY(faceYaw);
    }

    public Vector3f local(double x, double y, double z) {
        return FxMath.rot(face(), FxMath.v(x, y, z));
    }

    public Location loc(double x, double y, double z) {
        return origin.clone().add(x, y, z);
    }

    public Location loc(Vector3f v) {
        return origin.clone().add(v.x, v.y, v.z);
    }

    public Location lloc(double x, double y, double z) {
        return loc(local(x, y, z));
    }

    public double groundDy(double dx, double dz) {
        double wx = origin.getX() + dx, wz = origin.getZ() + dz;
        int bx = floor(wx), bz = floor(wz);
        long key = (((long) bx) << 32) ^ (bz & 0xffffffffL);
        Double cached = groundCache.get(key);
        if (cached != null) return cached;
        double res = 0;
        try {
            if (backend.isLoaded(bx, bz)) {
                int start = floor(origin.getY()) + 1;
                for (int y = start; y >= start - 6 && y >= backend.minY(); y--) {
                    if (backend.isPassable(bx, y, bz)) continue;
                    double top = backend.topY(bx, y, bz);
                    if (top <= origin.getY() + 1.6) {
                        res = top - origin.getY();
                        break;
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        groundCache.put(key, res);
        return res;
    }

    public Material groundMaterial(Material def) {
        if (groundId == null || !groundFull) return def;
        try {
            Material m = Material.valueOf(groundId.toUpperCase(Locale.ROOT));
            return m.isBlock() ? m : def;
        } catch (IllegalArgumentException ex) {
            // Efektlerde tanimli olmayan zemin: bilinen guvenli bir dokuya donus
            return def;
        }
    }

    // ------------------------------------------------------------------ parcaciklar

    public void particle(String p, Location l, int count, double ox, double oy, double oz, double speed, Object data) {
        if (p == null || finished || l == null) return;
        try {
            backend.particle(p, l.getX(), l.getY(), l.getZ(), count, ox, oy, oz, speed, data);
        } catch (Throwable ignored) {
        }
    }

    public void particle(String p, Location l, int count, double spread, double speed) {
        particle(p, l, count, spread, spread, spread, speed, null);
    }

    public void particle(String p, Location l, int count, double ox, double oy, double oz, double speed) {
        particle(p, l, count, ox, oy, oz, speed, null);
    }

    public void dir(String p, Location l, double dx, double dy, double dz, double speed) {
        particle(p, l, 0, dx, dy, dz, speed, null);
    }

    public void dir(String p, Location l, Vector3f d, double speed) {
        particle(p, l, 0, d.x, d.y, d.z, speed, null);
    }

    public void dust(Location l, Color c, float size, int count, double spread) {
        particle(P.DUST, l, count, spread, spread, spread, 0, new FxBackend.Dust(c, size));
    }

    public void dustT(Location l, Color a, Color b, float size, int count, double spread) {
        particle(P.DUST_TRANSITION, l, count, spread, spread, spread, 0, new FxBackend.DustFade(a, b, size));
    }

    public void crack(Location l, Material m, int count, double spread) {
        if (m == null || !m.isBlock()) return;
        particle(P.BLOCK, l, count, spread, spread * 0.5, spread, 0.15, m);
    }

    public void groundCrack(Location l, int count, double spread) {
        if (groundData == null) return;
        particle(P.BLOCK, l, count, spread, 0.05, spread, 0.15, groundData);
    }

    public void flash(Location l, Color c) {
        particle(P.FLASH, l, 1, 0, 0, 0, 0, null);
    }

    // ------------------------------------------------------------------ sesler

    private static String key(String k) {
        return k.indexOf(':') >= 0 ? k : "minecraft:" + k;
    }

    public void sound(Location l, String k, float vol, float pitch) {
        if (finished) return;
        float v = vol * engine.volume();
        if (v <= 0f) return;
        float pt = Math.max(0.5f, Math.min(2.0f, pitch));
        try {
            backend.sound(key(k), l.getX(), l.getY(), l.getZ(), v, pt);
        } catch (Throwable ignored) {
        }
    }

    public void sound(String k, float vol, float pitch) {
        sound(origin, k, vol, pitch);
    }

    public void longSound(Location l, String k, float vol, float pitch) {
        longSounds.add(key(k));
        sound(l, k, vol, pitch);
    }

    public void longSound(String k, float vol, float pitch) {
        longSound(origin, k, vol, pitch);
    }

    public void stopSound(String k) {
        try {
            backend.stopSound(key(k));
        } catch (Throwable ignored) {
        }
    }

    public float rp(float min, float max) {
        return min + rnd.nextFloat() * (max - min);
    }

    public double rd(double min, double max) {
        return min + rnd.nextDouble() * (max - min);
    }

    // ------------------------------------------------------------------ fizik

    public List<double[]> ballistic(double x, double y, double z, double vx, double vy, double vz,
                                    double gravity, double halfHeight, int ticks) {
        List<double[]> out = new ArrayList<>(ticks + 1);
        boolean landed = false;
        for (int i = 0; i <= ticks; i++) {
            out.add(new double[]{x, y, z, landed ? 1 : 0});
            if (landed) continue;
            x += vx;
            y += vy;
            z += vz;
            vy -= gravity;
            vx *= 0.98;
            vz *= 0.98;
            double g = groundDy(x, z) + halfHeight;
            if (y <= g && vy < 0) {
                y = g;
                landed = true;
            }
        }
        return out;
    }
}
