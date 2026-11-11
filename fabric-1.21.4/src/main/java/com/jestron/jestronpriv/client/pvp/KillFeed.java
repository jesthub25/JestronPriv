package com.jestron.jestronpriv.client.pvp;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Oldurme akisi: sunucunun herkese gonderdigi olum mesajlarindan (vanilla "death.*" ceviri anahtarlari) kim kimi
 * neyle oldurdu. Silah, mesajin icindeki esya bilgisinden (ustune gelince gorunen) gelir; olum nedeni simgeye
 * cevrilir. Ozel eklenti mesajli sunucularda senin dogrulanmis oldurmelerin (KillDetector) de eklenir.
 * Oldurenin seri sayisi (olene kadar) tutulur.
 */
public final class KillFeed {

    /** Bellekte tutulan en fazla satir. */
    static final int KEEP = 12;
    private static final long MERGE_MS = 3000;

    /** Bir satir. */
    public static final class Entry {
        public final String killer, victim;
        /** Mesajdaki silah (yoksa null) ve olum nedeni simgesi. */
        public final ItemStack weapon, cause;
        public final boolean meKiller, meVictim, pvp;
        public int streak;
        public final long time;
        /** Cizim icin hesaplanmis genislikler (ilk cizimde). */
        public int wKiller = -1, wVictim = -1, wStreak;

        Entry(String killer, String victim, ItemStack weapon, ItemStack cause, boolean meKiller, boolean meVictim, boolean pvp, int streak, long time) {
            this.killer = killer;
            this.victim = victim;
            this.weapon = weapon;
            this.cause = cause;
            this.meKiller = meKiller;
            this.meVictim = meVictim;
            this.pvp = pvp;
            this.streak = streak;
            this.time = time;
        }
    }

    private static final ArrayDeque<Entry> ENTRIES = new ArrayDeque<>();
    private static final Map<String, Integer> STREAKS = new HashMap<>();
    private static final Map<Item, ItemStack> ICONS = new HashMap<>();

    private KillFeed() {
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    public static Iterable<Entry> entries() {
        return ENTRIES;
    }

    public static int size() {
        return ENTRIES.size();
    }

    // ------------------------------------------------------------------ mesajlar

    /** Bu mesaj bir vanilla olum mesaji mi. */
    public static boolean isDeathMessage(Component m) {
        return m != null && m.getContents() instanceof TranslatableContents tc && tc.getKey().startsWith("death.");
    }

    /** Sohbetten gizlensin mi (ayar acik ve olum mesajiysa). */
    public static boolean hideFromChat(Component m) {
        ModConfig c = cfg();
        return c != null && c.kfHud && c.kfHideChat && isDeathMessage(m);
    }

    /** Sistem mesaji (ana thread). */
    public static void onMessage(Component m) {
        ModConfig c = cfg();
        Minecraft mc = Minecraft.getInstance();
        if (c == null || !c.kfHud || mc.player == null || !(m.getContents() instanceof TranslatableContents tc)) return;
        String key = tc.getKey();
        if (!key.startsWith("death.")) return;
        Object[] args = tc.getArgs();
        if (args.length < 1) return;
        Component victimC = comp(args[0]);
        if (victimC == null) return;
        Component killerC = args.length >= 2 ? comp(args[1]) : null;
        Component itemC = args.length >= 3 ? comp(args[2]) : null;
        String victim = name(victimC), killer = killerC == null ? "" : name(killerC);
        if (victim.isEmpty()) return;
        boolean meV = isMe(victimC, victim), meK = killerC != null && isMe(killerC, killer);
        boolean victimPlayer = isPlayer(victimC, victim), killerPlayer = killerC != null && isPlayer(killerC, killer);
        ItemStack weapon = itemC == null ? null : hoverItem(itemC);
        if (weapon != null && weapon.isEmpty()) weapon = null;
        add(killer, victim, weapon, causeIcon(key, killerPlayer), meK, meV, victimPlayer && killerPlayer, false);
    }

    /** Dogrulanmis oldurmen (ozel sunucu mesajlari icin; vanilla mesaj geldiyse birlesir). */
    public static void onMyKill(String victim) {
        ModConfig c = cfg();
        Minecraft mc = Minecraft.getInstance();
        if (c == null || !c.kfHud || mc.player == null || victim == null || victim.isEmpty()) return;
        long now = Util.getMillis();
        for (Entry e : ENTRIES) if (e.victim.equals(victim) && now - e.time < MERGE_MS) return; // vanilla mesaj zaten geldi
        ItemStack held = mc.player.getMainHandItem();
        add(mc.player.getGameProfile().getName(), victim, held.isEmpty() ? null : held.copy(), icon(Items.IRON_SWORD), true, false, true, true);
    }

    private static void add(String killer, String victim, ItemStack weapon, ItemStack cause, boolean meK, boolean meV, boolean pvp, boolean synthetic) {
        long now = Util.getMillis();
        // ayni olay iki kez gelmesin; sentetik satir gelirse vanilla satiri onun yerini alir
        for (Iterator<Entry> it = ENTRIES.iterator(); it.hasNext(); ) {
            Entry e = it.next();
            if (!e.victim.equals(victim) || now - e.time >= MERGE_MS) continue;
            if (synthetic) return;
            if (e.killer.equals(killer) || e.meKiller && meK) it.remove();
        }
        // seri sadece oyuncu oldurmelerinde sayilir (mob / cevre olumleri seriyi bozmaz ama artirmaz)
        int streak = !killer.isEmpty() && pvp ? STREAKS.merge(killer, 1, Integer::sum) : 0;
        STREAKS.remove(victim);
        if (STREAKS.size() > 512) STREAKS.clear();
        ENTRIES.addFirst(new Entry(killer, victim, weapon, cause, meK, meV, pvp, streak, now));
        while (ENTRIES.size() > KEEP) ENTRIES.removeLast();
    }

    // ------------------------------------------------------------------ yardimcilar

    private static Component comp(Object o) {
        if (o instanceof Component c) return c;
        return o == null ? null : Component.literal(String.valueOf(o));
    }

    /** Gorunen ad: takim onekleri ve renk kodlari olmadan, en fazla 24 karakter. */
    private static String name(Component c) {
        String ins = c.getStyle().getInsertion();
        String s = ins != null && !ins.isEmpty() ? ins : c.getString();
        s = s.replaceAll("§.", "").trim();
        return s.length() > 24 ? s.substring(0, 24) : s;
    }

    private static UUID hoverUuid(Component c) {
        HoverEvent he = c.getStyle().getHoverEvent();
        if (he == null) return null;
        HoverEvent.EntityTooltipInfo info = he.getValue(HoverEvent.Action.SHOW_ENTITY);
        return info == null ? null : info.id;
    }

    private static boolean isMe(Component c, String name) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        UUID u = hoverUuid(c);
        if (u != null) return u.equals(mc.player.getUUID());
        return name.equals(mc.player.getGameProfile().getName());
    }

    private static boolean isPlayer(Component c, String name) {
        HoverEvent he = c.getStyle().getHoverEvent();
        if (he != null) {
            HoverEvent.EntityTooltipInfo info = he.getValue(HoverEvent.Action.SHOW_ENTITY);
            if (info != null) return info.type == EntityType.PLAYER;
        }
        var con = Minecraft.getInstance().getConnection();
        return con != null && con.getPlayerInfo(name) != null;
    }

    private static ItemStack hoverItem(Component c) {
        try {
            HoverEvent he = c.getStyle().getHoverEvent();
            if (he == null) {
                for (Component sib : c.getSiblings()) {
                    he = sib.getStyle().getHoverEvent();
                    if (he != null) break;
                }
            }
            if (he == null) return null;
            HoverEvent.ItemStackInfo info = he.getValue(HoverEvent.Action.SHOW_ITEM);
            return info == null ? null : info.getItemStack();
        } catch (Throwable t) {
            return null;
        }
    }

    static ItemStack icon(Item it) {
        return ICONS.computeIfAbsent(it, ItemStack::new);
    }

    /** Olum nedeninden simge. */
    static ItemStack causeIcon(String key, boolean killerPlayer) {
        String k = key.toLowerCase(java.util.Locale.ROOT);
        Item it;
        if (k.contains("arrow")) it = Items.BOW;
        else if (k.contains("trident")) it = Items.TRIDENT;
        else if (k.contains("firework")) it = Items.FIREWORK_ROCKET;
        else if (k.contains("explosion") || k.contains("badrespawnpoint")) it = k.contains("badrespawn") ? Items.RED_BED : Items.TNT;
        else if (k.contains("fireball")) it = Items.FIRE_CHARGE;
        else if (k.contains("witherskull")) it = Items.WITHER_SKELETON_SKULL;
        else if (k.contains("wither")) it = Items.WITHER_ROSE;
        else if (k.contains("magic")) it = Items.SPLASH_POTION;
        else if (k.contains("thorns")) it = Items.IRON_CHESTPLATE;
        else if (k.contains("sting")) it = Items.HONEYCOMB;
        else if (k.contains("mace")) it = Items.MACE;
        else if (k.contains("sonic")) it = Items.ECHO_SHARD;
        else if (k.contains("lightning")) it = Items.LIGHTNING_ROD;
        else if (k.contains("fall") || k.contains("fell") || k.contains("flyintowall")) it = Items.FEATHER;
        else if (k.contains("lava")) it = Items.LAVA_BUCKET;
        else if (k.contains("hotfloor")) it = Items.MAGMA_BLOCK;
        else if (k.contains("fire") || k.contains("campfire")) it = Items.BLAZE_POWDER;
        else if (k.contains("drown")) it = Items.WATER_BUCKET;
        else if (k.contains("outofworld") || k.contains("outsideborder")) it = Items.ENDER_EYE;
        else if (k.contains("starve")) it = Items.ROTTEN_FLESH;
        else if (k.contains("cactus")) it = Items.CACTUS;
        else if (k.contains("sweetberry")) it = Items.SWEET_BERRIES;
        else if (k.contains("freeze")) it = Items.POWDER_SNOW_BUCKET;
        else if (k.contains("anvil")) it = Items.ANVIL;
        else if (k.contains("stalag") || k.contains("stalac")) it = Items.POINTED_DRIPSTONE;
        else if (k.contains("dragonbreath")) it = Items.DRAGON_BREATH;
        else if (k.contains("inwall") || k.contains("cramming") || k.contains("fallingblock")) it = Items.GRAVEL;
        else if (k.contains("player")) it = Items.IRON_SWORD;
        else if (k.contains("mob")) it = killerPlayer ? Items.IRON_SWORD : Items.ZOMBIE_HEAD;
        else it = Items.SKELETON_SKULL;
        return icon(it);
    }

    /** Ayar ekranindan deneme. */
    public static void demo() {
        Minecraft mc = Minecraft.getInstance();
        String me = mc.player == null ? "Sen" : mc.player.getGameProfile().getName();
        STREAKS.remove("Kral_Ahmet");
        add("Kral_Ahmet", "Mehmet35", icon(Items.NETHERITE_SWORD), icon(Items.IRON_SWORD), false, false, true, false);
        add("Kral_Ahmet", "Zeynep", icon(Items.NETHERITE_SWORD), icon(Items.IRON_SWORD), false, false, true, false);
        add("Ayse_Pvp", "Can", null, icon(Items.BOW), false, false, true, false);
        add(me, "Kral_Ahmet", icon(Items.MACE), icon(Items.IRON_SWORD), true, false, true, false);
    }

    /** HUD duzenleyici ornegi (akisa eklenmez). */
    public static java.util.List<Entry> sample() {
        long now = Util.getMillis();
        return java.util.List.of(new Entry("Sen", "Kral_Ahmet", icon(Items.DIAMOND_SWORD), icon(Items.IRON_SWORD), true, false, true, 3, now),
                new Entry("Ayse_Pvp", "Can", null, icon(Items.BOW), false, false, true, 0, now),
                new Entry("Mehmet35", "Sen", icon(Items.NETHERITE_AXE), icon(Items.IRON_SWORD), false, true, true, 2, now));
    }

    public static void clear() {
        ENTRIES.clear();
        STREAKS.clear();
    }
}
