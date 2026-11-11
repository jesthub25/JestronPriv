package com.jestron.efektplugin;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Parcacik kimligi (moddaki gibi "happy_villager") → bu sunucunun Bukkit Particle'i. 1.20.5'te Bukkit adlari
 * Minecraft kimliklerine gore degisti (REDSTONE → DUST, VILLAGER_HAPPY → HAPPY_VILLAGER ...): once yeni ad, sonra eski
 * ad denenir; boylece tek jar 1.20.1 - 1.21.x arasi calisir. Bulunamayan parcacik sessizce atlanir.
 * <p>
 * Hicbir Particle sabiti dogrudan kullanilmaz (eski / yeni surumde olmayan sabit, sinif yuklenirken coker).
 */
public final class Partikuller {

    /** Yeni kimlik → 1.20.4 ve oncesi Bukkit adi. */
    static final Map<String, String> OLD = Map.ofEntries(
            Map.entry("poof", "EXPLOSION_NORMAL"), Map.entry("explosion", "EXPLOSION_LARGE"), Map.entry("explosion_emitter", "EXPLOSION_HUGE"),
            Map.entry("firework", "FIREWORKS_SPARK"), Map.entry("bubble", "WATER_BUBBLE"), Map.entry("splash", "WATER_SPLASH"),
            Map.entry("fishing", "WATER_WAKE"), Map.entry("underwater", "SUSPENDED_DEPTH"), Map.entry("enchanted_hit", "CRIT_MAGIC"),
            Map.entry("smoke", "SMOKE_NORMAL"), Map.entry("large_smoke", "SMOKE_LARGE"), Map.entry("effect", "SPELL"),
            Map.entry("instant_effect", "SPELL_INSTANT"), Map.entry("entity_effect", "SPELL_MOB"), Map.entry("witch", "SPELL_WITCH"),
            Map.entry("dripping_water", "DRIP_WATER"), Map.entry("dripping_lava", "DRIP_LAVA"), Map.entry("angry_villager", "VILLAGER_ANGRY"),
            Map.entry("happy_villager", "VILLAGER_HAPPY"), Map.entry("mycelium", "TOWN_AURA"), Map.entry("enchant", "ENCHANTMENT_TABLE"),
            Map.entry("dust", "REDSTONE"), Map.entry("item_snowball", "SNOWBALL"), Map.entry("item_slime", "SLIME"), Map.entry("item", "ITEM_CRACK"),
            Map.entry("block", "BLOCK_CRACK"), Map.entry("rain", "WATER_DROP"), Map.entry("elder_guardian", "MOB_APPEARANCE"),
            Map.entry("totem_of_undying", "TOTEM"));

    private static final Particle NONE_MARK = null;
    private final Map<String, Particle> cache = new HashMap<>();
    private final Map<String, Boolean> missing = new HashMap<>();
    private Particle dust, transition;
    private boolean dustLooked, transitionLooked;

    private static Particle byName(String n) {
        try {
            return Particle.valueOf(n);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Veri istemeyen parcacik (ya da null). */
    public Particle get(String id) {
        Particle p = cache.get(id);
        if (p != null || missing.containsKey(id)) return p;
        p = byName(id.toUpperCase(Locale.ROOT));
        if (p == null && OLD.containsKey(id)) p = byName(OLD.get(id));
        if (p != null && p.getDataType() != Void.class) p = NONE_MARK;
        if (p == null) missing.put(id, Boolean.TRUE);
        else cache.put(id, p);
        return p;
    }

    public Particle dust() {
        if (!dustLooked) {
            dustLooked = true;
            dust = byName("DUST");
            if (dust == null) dust = byName("REDSTONE");
        }
        return dust;
    }

    public Particle transition() {
        if (!transitionLooked) {
            transitionLooked = true;
            transition = byName("DUST_COLOR_TRANSITION");
        }
        return transition;
    }

    /** Hareketli / normal parcacik: count 0 → verilen vektor hiz olarak gider (istemcideki addParticle ile ayni). */
    public boolean send(Player to, String id, double x, double y, double z, double vx, double vy, double vz) {
        Particle p = get(id);
        if (p == null) return false;
        to.spawnParticle(p, x, y, z, 0, vx, vy, vz, 1.0);
        return true;
    }

    public boolean sendDust(Player to, int rgb, float size, double x, double y, double z) {
        Particle p = dust();
        if (p == null) return false;
        to.spawnParticle(p, x, y, z, 1, 0, 0, 0, 0, new Particle.DustOptions(Color.fromRGB(rgb & 0xFFFFFF), size));
        return true;
    }

    public boolean sendFade(Player to, int from, int too, float size, double x, double y, double z) {
        Particle p = transition();
        if (p == null) return sendDust(to, from, size, x, y, z);
        to.spawnParticle(p, x, y, z, 1, 0, 0, 0, 0, new Particle.DustTransition(Color.fromRGB(from & 0xFFFFFF), Color.fromRGB(too & 0xFFFFFF), size));
        return true;
    }
}
