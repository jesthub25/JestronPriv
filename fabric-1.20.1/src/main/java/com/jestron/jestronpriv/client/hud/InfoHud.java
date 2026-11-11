package com.jestron.jestronpriv.client.hud;

import com.jestron.jestronpriv.client.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Bilgi satiri: FPS, ping, koordinat, yon, karsi boyut koordinati (Nether x8), biyom, hiz, gercek saat, oyun gunu.
 * Yazilar tick'te bir kez uretilir; karede sadece cizilir.
 */
public final class InfoHud extends HudElement implements Hud.Ticking {

    public static final String[] FIELDS = {"FPS", "Ping", "Koordinat", "Yön", "Nether / Dünya koordinatı", "Biyom", "Hız", "Saat",
            "Oyun günü / saati"};
    public static final int FIELD_COUNT = FIELDS.length;
    private static final String[] LABELS = {"FPS", "Ping", "XYZ", "Yön", "", "Biyom", "Hız", "Saat", "Gün"};

    static final String[] DIR = {"Güney", "Güneybatı", "Batı", "Kuzeybatı", "Kuzey", "Kuzeydoğu", "Doğu", "Güneydoğu"};
    static final String[] AXIS = {"+Z", "-X +Z", "-X", "-X -Z", "-Z", "+X -Z", "+X", "+X +Z"};

    private final String[] label = new String[FIELD_COUNT];
    private final String[] value = new String[FIELD_COUNT];
    private final int[] color = new int[FIELD_COUNT];
    private final int[] order = new int[FIELD_COUNT];
    private int n;
    private final Map<String, String> biomeNames = new HashMap<>();
    private long lastBiomePos = Long.MIN_VALUE;
    // dunyanin kimligi (nesnenin kendisi tutulmaz: cikista eski dunya bellekte kalmasin)
    private int lastBiomeLevel;
    private String biome = "?";
    private double lastX = Double.NaN, lastZ, speed;
    private int labelW;

    InfoHud() {
        super("info", "Bilgi Satırı");
    }

    @Override
    public boolean enabled(ModConfig c) {
        return c.infoHud;
    }

    @Override
    public void setEnabled(ModConfig c, boolean v) {
        c.infoHud = v;
    }

    @Override
    public float x(ModConfig c) {
        return c.infoX;
    }

    @Override
    public float y(ModConfig c) {
        return c.infoY;
    }

    @Override
    public float scale(ModConfig c) {
        return c.infoScale;
    }

    @Override
    public void setPos(ModConfig c, float x, float y) {
        c.infoX = x;
        c.infoY = y;
    }

    @Override
    public void setScale(ModConfig c, float s) {
        c.infoScale = s;
    }

    /** MC yaw -> 8 yon indeksi (0 = guney). */
    static int direction(float yaw) {
        float y = ((yaw % 360f) + 360f) % 360f;
        if (Float.isNaN(y)) return 0;
        return Math.round(y / 45f) % 8;
    }

    static String coord(double v, int decimals) {
        if (decimals <= 0) return Long.toString((long) Math.floor(v));
        return String.format(Locale.ROOT, decimals == 1 ? "%.1f" : "%.2f", v);
    }

    static int fpsColor(int fps) {
        return fps >= 60 ? 0x6BFF5A : fps >= 30 ? 0xFFE047 : 0xFF5050;
    }

    static int pingColor(int ms) {
        return ms <= 0 ? 0xA0A0A0 : ms < 80 ? 0x6BFF5A : ms < 150 ? 0xFFE047 : 0xFF5050;
    }

    @Override
    public void tick(Minecraft mc, ModConfig c) {
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null || !c.infoHud) {
            lastX = Double.NaN;
            return;
        }
        // hiz (blok/sn, yatay), yumusatilmis
        if (!Double.isNaN(lastX)) {
            double dx = p.getX() - lastX, dz = p.getZ() - lastZ;
            double v = Math.sqrt(dx * dx + dz * dz) * 20.0;
            if (v > 200) v = 0;   // isinlanma
            speed += (v - speed) * 0.35;
        }
        lastX = p.getX();
        lastZ = p.getZ();
        build(mc, c, false);
    }

    private void set(int f, String v, int col) {
        label[f] = LABELS[f];
        value[f] = v;
        color[f] = col;
    }

    private void build(Minecraft mc, ModConfig c, boolean sample) {
        LocalPlayer p = mc.player;
        int vc = c.infoValueColor & 0xFFFFFF;
        n = 0;
        for (int f = 0; f < FIELD_COUNT; f++) {
            if ((c.infoMask & (1 << f)) == 0) continue;
            boolean ok = true;
            switch (f) {
                case 0 -> {
                    int fps = sample ? 144 : mc.getFps();
                    set(f, Integer.toString(fps), c.infoQualityColor ? fpsColor(fps) : vc);
                }
                case 1 -> {
                    int ms = sample ? 32 : ping(mc);
                    set(f, ms <= 0 ? "—" : ms + " ms", c.infoQualityColor ? pingColor(ms) : vc);
                }
                case 2 -> {
                    if (p == null || sample) set(f, "120 64 -340", vc);
                    else set(f, coord(p.getX(), c.infoDecimals) + " " + coord(p.getY(), c.infoDecimals) + " " + coord(p.getZ(), c.infoDecimals), vc);
                }
                case 3 -> {
                    int d = p == null || sample ? 4 : direction(p.getYRot());
                    set(f, DIR[d] + " (" + AXIS[d] + ")", vc);
                }
                case 4 -> {
                    if (p == null || mc.level == null || sample) {
                        label[f] = "Nether";
                        value[f] = "15 -42";
                        color[f] = 0xFF8060;
                    } else if (mc.level.dimension() == Level.NETHER) {
                        label[f] = "Dünya";
                        value[f] = coord(p.getX() * 8, 0) + " " + coord(p.getZ() * 8, 0);
                        color[f] = 0x7FD0FF;
                    } else if (mc.level.dimension() == Level.OVERWORLD) {
                        label[f] = "Nether";
                        value[f] = coord(p.getX() / 8, 0) + " " + coord(p.getZ() / 8, 0);
                        color[f] = 0xFF8060;
                    } else {
                        ok = false;
                    }
                }
                case 5 -> set(f, sample ? "Çayır" : biome(mc), vc);
                case 6 -> set(f, String.format(Locale.ROOT, "%.1f b/s", sample ? 5.6 : speed), vc);
                case 7 -> {
                    LocalTime t = LocalTime.now();
                    set(f, String.format(Locale.ROOT, "%02d:%02d", t.getHour(), t.getMinute()), vc);
                }
                case 8 -> {
                    if (mc.level == null || sample) set(f, "12 · 08:30", vc);
                    else {
                        long dt = mc.level.getDayTime();
                        set(f, (Math.floorDiv(dt, 24000L) + 1) + " · " + com.jestron.jestronpriv.client.view.WorldTime.clock((int) Math.floorMod(dt, 24000L)), vc);
                    }
                }
                default -> ok = false;
            }
            if (ok) order[n++] = f;
        }
    }

    private static int ping(Minecraft mc) {
        // anlik olcum (ping istegi); yoksa sekme listesi
        int live = com.jestron.jestronpriv.client.pvp.PingTracker.current();
        if (live >= 0) return live;
        ClientPacketListener con = mc.getConnection();
        if (con == null || mc.player == null) return 0;
        PlayerInfo pi = con.getPlayerInfo(mc.player.getUUID());
        return pi == null ? 0 : pi.getLatency();
    }

    private String biome(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null) return "?";
        BlockPos bp = p.blockPosition();
        long key = bp.asLong();
        // ayni blok ama farkli dunya / boyut (Nether'e gecis, sunucu degisimi): yeniden sor
        int lid = System.identityHashCode(mc.level);
        if (key == lastBiomePos && lid == lastBiomeLevel) return biome;
        lastBiomePos = key;
        lastBiomeLevel = lid;
        String id = mc.level.getBiome(bp).unwrapKey().map(k -> k.location().getNamespace() + "." + k.location().getPath()).orElse(null);
        if (id == null) return biome = "?";
        biome = biomeNames.computeIfAbsent(id, k -> Component.translatable("biome." + k).getString());
        return biome;
    }

    @Override
    public boolean hasContent(ModConfig c) {
        return n > 0;
    }

    @Override
    public void measure(ModConfig c, boolean sample) {
        Minecraft mc = Minecraft.getInstance();
        // gercek veri tick'te uretilir; ornek sadece duzenleyicide (oyun verisi yokken)
        if (sample) build(mc, c, true);
        Font f = mc.font;
        if (n == 0) {
            w = h = 0;
            return;
        }
        if (c.infoRow) {
            int tw = 0;
            for (int k = 0; k < n; k++) {
                int i = order[k];
                if (k > 0) tw += f.width(" · ");
                tw += (label[i].isEmpty() ? 0 : f.width(label[i]) + 4) + f.width(value[i]);
            }
            w = tw + 8;
            h = 13;
        } else {
            int lw = 0, vw = 0;
            for (int k = 0; k < n; k++) {
                int i = order[k];
                lw = Math.max(lw, label[i].isEmpty() ? 0 : f.width(label[i]));
                vw = Math.max(vw, f.width(value[i]));
            }
            labelW = lw;
            w = lw + 6 + vw + 8;
            h = n * 11 + 3;
        }
    }

    @Override
    public void draw(GuiGraphics g, ModConfig c, boolean sample, boolean right) {
        Minecraft mc = Minecraft.getInstance();
        Font f = mc.font;
        Hud.background(g, 0, 0, w, h, c.infoBg);
        int lc = 0xFF000000 | (c.infoLabelColor & 0xFFFFFF);
        if (c.infoRow) {
            int x = 4;
            for (int k = 0; k < n; k++) {
                int i = order[k];
                if (k > 0) {
                    Hud.text(g, f, " · ", x, 3, 0xFF707070, c);
                    x += f.width(" · ");
                }
                if (!label[i].isEmpty()) {
                    Hud.text(g, f, label[i], x, 3, lc, c);
                    x += f.width(label[i]) + 4;
                }
                Hud.text(g, f, value[i], x, 3, 0xFF000000 | color[i], c);
                x += f.width(value[i]);
            }
            return;
        }
        for (int k = 0; k < n; k++) {
            int i = order[k];
            int y = 3 + k * 11;
            if (right) {
                int vx = w - 4 - f.width(value[i]);
                Hud.text(g, f, value[i], vx, y, 0xFF000000 | color[i], c);
                if (!label[i].isEmpty()) Hud.text(g, f, label[i], vx - 6 - f.width(label[i]), y, lc, c);
            } else {
                if (!label[i].isEmpty()) Hud.text(g, f, label[i], 4, y, lc, c);
                Hud.text(g, f, value[i], 4 + labelW + 6, y, 0xFF000000 | color[i], c);
            }
        }
    }
}
