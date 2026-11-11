package com.jestron.jestronpriv.client.pvp;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Locale;

/**
 * Hasar sayilari. Sunucu hasar olayini (vuran = sen) gonderdiginde hedefin cani (+ emilim) not edilir; sonraki
 * tick'lerde senkronlanan can dusunce fark yazilir. Kritikte altin ve buyuk, olumde kizil kafatasi. Cani gizleyen
 * sunucularda can hic degismez: o zaman (istenirse) sayi yerine isabet isareti cikar.
 */
public final class DamageNumbers {

    private static final int MAX = 16;
    private static final int[] ID = new int[MAX];
    private static final float[] BEFORE = new float[MAX];
    private static final long[] TICK = new long[MAX];
    private static final boolean[] CRIT = new boolean[MAX];
    private static int count;
    private static long ticks;

    private DamageNumbers() {
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    static float hp(LivingEntity e) {
        float h = e.getHealth() + e.getAbsorptionAmount();
        return Float.isFinite(h) ? h : 0f;
    }

    /** Senin isabetin (sunucu onayli). */
    public static void onHit(LivingEntity e) {
        ModConfig c = cfg();
        if (c == null || !c.dmgNumbers || e == null) return;
        int id = e.getId();
        for (int i = 0; i < count; i++) {
            if (ID[i] == id) {
                // ayni hedefe ust uste: once oncekini sonuclandir
                TICK[i] = ticks;
                return;
            }
        }
        if (count >= MAX) return;
        ID[count] = id;
        BEFORE[count] = hp(e);
        TICK[count] = ticks;
        CRIT[count] = false;
        count++;
    }

    /** Sunucunun kritik animasyonu bu hedefe geldi. */
    public static void markCrit(int entityId) {
        for (int i = 0; i < count; i++) if (ID[i] == entityId) CRIT[i] = true;
    }

    public static void tick(Minecraft mc) {
        ticks++;
        if (mc.level == null) {
            count = 0;
            return;
        }
        ModConfig c = cfg();
        for (int i = 0; i < count; ) {
            Entity ent = mc.level.getEntity(ID[i]);
            LivingEntity e = ent instanceof LivingEntity le ? le : null;
            boolean dead = e == null || !e.isAlive() || e.getHealth() <= 0f;
            float now = e == null ? 0f : hp(e);
            float dmg = BEFORE[i] - now;
            boolean resolved = false;
            if (c != null && dead && BEFORE[i] > 0f && ticks - TICK[i] >= 1) {
                show(c, ent, Math.max(dmg, 0.5f), CRIT[i], true);
                resolved = true;
            } else if (dmg > 0.01f && ticks - TICK[i] >= 1) {
                // kritik animasyonu ayni tick'te gelir; 1 tick bekleyip bilgisini de al
                if (c != null) show(c, e, dmg, CRIT[i], false);
                resolved = true;
            } else if (ticks - TICK[i] > 8) {
                // can degismedi: sunucu cani gizliyor ya da hasar emildi
                if (c != null && c.dmgUnknownMarker && e != null) show(c, e, -1f, CRIT[i], false);
                resolved = true;
            }
            if (resolved) {
                int j = --count;
                ID[i] = ID[j];
                BEFORE[i] = BEFORE[j];
                TICK[i] = TICK[j];
                CRIT[i] = CRIT[j];
            } else {
                i++;
            }
        }
    }

    private static void show(ModConfig c, Entity e, float dmg, boolean crit, boolean kill) {
        if (e == null) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null) return;
        float r1 = (float) Math.random() - 0.5f, r2 = (float) Math.random() - 0.5f;
        double x = e.getX() + r1 * e.getBbWidth() * 0.8, y = e.getY() + e.getBbHeight() * 0.9 + 0.15, z = e.getZ() + r2 * e.getBbWidth() * 0.8;
        // vurana gore yana savrulma (kameranin saginda / solunda)
        float side = (float) (Math.random() < 0.5 ? -1 : 1);
        double fx = e.getX() - p.getX(), fz = e.getZ() - p.getZ();
        double fl = Math.sqrt(fx * fx + fz * fz);
        float sx = fl > 1e-3 ? (float) (-fz / fl) * side : side, sz = fl > 1e-3 ? (float) (fx / fl) * side : 0f;
        float spd = 0.55f + (float) Math.random() * 0.35f;
        String text;
        int color;
        int kind;
        float scale = c.dmgScale, life;
        if (dmg < 0f) {
            text = crit ? "✦" : "✕";
            color = crit ? c.dmgCritColor : 0xE0E0E0;
            kind = crit ? WorldText.CRIT : WorldText.NORMAL;
            life = 0.7f;
            scale *= 0.9f;
        } else if (kill) {
            text = "☠ " + format(dmg, "KALP".equals(c.dmgFormat));
            color = 0xFF2A2A;
            kind = WorldText.KILL;
            life = 1.7f;
            scale *= 1.55f;
        } else if (crit) {
            text = "✦ " + format(dmg, "KALP".equals(c.dmgFormat));
            color = c.dmgCritColor;
            kind = WorldText.CRIT;
            life = 1.25f;
            scale *= 1.3f;
        } else {
            text = format(dmg, "KALP".equals(c.dmgFormat));
            color = c.dmgColorByAmount ? colorFor(dmg) : c.dmgColor;
            kind = WorldText.NORMAL;
            life = 1.0f;
        }
        WorldText.add(text, x, y, z, sx * spd, 1.7f + (kill ? 0.6f : 0f), sz * spd, life, scale, color, kind, (float) Math.random());
    }

    /** "-3.5 ❤" (kalp) ya da "-7" (can). */
    public static String format(float hpDamage, boolean hearts) {
        float v = hearts ? hpDamage / 2f : hpDamage;
        v = Math.round(v * 10f) / 10f;
        String num = v == Math.rint(v) ? String.valueOf((long) v) : String.format(Locale.ROOT, "%.1f", v);
        return hearts ? "-" + num + " ❤" : "-" + num;
    }

    /** Hasara gore renk (can puani): kucuk beyaz, orta sari, buyuk turuncu, cok buyuk kirmizi. */
    public static int colorFor(float hpDamage) {
        if (hpDamage < 2f) return 0xFFFFFF;
        if (hpDamage < 4f) return Palette.mix(0xFFFFFF, 0xFFE047, (hpDamage - 2f) / 2f);
        if (hpDamage < 7f) return Palette.mix(0xFFE047, 0xFF9A2E, (hpDamage - 4f) / 3f);
        return Palette.mix(0xFF9A2E, 0xFF3030, Math.min(1f, (hpDamage - 7f) / 5f));
    }

    /** Ayar ekranindan: oyuncunun onunde ornek sayilar. */
    public static void preview() {
        ModConfig c = cfg();
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (c == null || p == null) return;
        net.minecraft.world.phys.Vec3 look = p.getViewVector(1f), eye = p.getEyePosition();
        double x = eye.x + look.x * 2.6, y = eye.y + look.y * 2.6 - 0.2, z = eye.z + look.z * 2.6;
        boolean h = "KALP".equals(c.dmgFormat);
        float[] dm = {1.5f, 4f, 7.5f};
        for (int i = 0; i < 3; i++) {
            float side = (i - 1) * 0.7f;
            WorldText.add(format(dm[i], h), x + look.z * side * 0.6, y, z - look.x * side * 0.6, (float) look.z * side, 1.7f,
                    (float) -look.x * side, 1.1f, c.dmgScale, c.dmgColorByAmount ? colorFor(dm[i]) : c.dmgColor, WorldText.NORMAL, i);
        }
        WorldText.add("✦ " + format(9f, h), x, y + 0.35, z, 0, 2.0f, 0, 1.3f, c.dmgScale * 1.3f, c.dmgCritColor, WorldText.CRIT, 0.5f);
    }
}
