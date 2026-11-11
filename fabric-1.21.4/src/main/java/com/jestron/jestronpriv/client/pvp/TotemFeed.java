package com.jestron.jestronpriv.client.pvp;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.UUID;

/**
 * Totem bildirimleri: yakindaki bir oyuncu totem patlatinca (sunucunun herkese gonderdigi varlik olayi 35) kisa
 * bildirim: kim, kacinci totem, ne zaman. Ayni oyuncunun yeni totemi ayni satiri gunceller ve satiri "nabiz" ile
 * vurgular. Oyuncu olurse satir "oldu" olarak isaretlenir.
 */
public final class TotemFeed {

    static final int KEEP = 10;

    /** Bir bildirim. */
    public static final class Entry {
        public final int entityId;
        public final String name;
        public final UUID uuid;
        public final boolean me;
        public int count;
        public long time, pulseAt;
        public boolean died;

        Entry(int entityId, String name, UUID uuid, boolean me, int count, long time) {
            this.entityId = entityId;
            this.name = name;
            this.uuid = uuid;
            this.me = me;
            this.count = count;
            this.time = time;
            this.pulseAt = time;
        }
    }

    private static final ArrayDeque<Entry> ENTRIES = new ArrayDeque<>();

    private TotemFeed() {
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

    /** Totem patladi (ana thread). n: o varligin kacinci totemi. */
    public static void onPop(Entity e, int n) {
        ModConfig c = cfg();
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer me = mc.player;
        if (c == null || !c.tfHud || e == null || me == null) return;
        boolean self = e == me;
        if (self && !c.tfSelf) return;
        if (!(e instanceof Player) && !c.tfMobs) return;
        if (!self && c.tfRange > 0 && e.distanceToSqr(me) > (double) c.tfRange * c.tfRange) return;
        long now = Util.getMillis();
        String name = e instanceof Player p ? p.getGameProfile().getName() : e.getName().getString();
        UUID id = e.getUUID();
        Entry found = null;
        for (Iterator<Entry> it = ENTRIES.iterator(); it.hasNext(); ) {
            Entry x = it.next();
            if (x.entityId == e.getId() || x.uuid.equals(id)) {
                found = x;
                it.remove();
                break;
            }
        }
        if (found == null) found = new Entry(e.getId(), name, id, self, n, now);
        found.count = n;
        found.time = now;
        found.pulseAt = now;
        found.died = false;
        ENTRIES.addFirst(found);
        while (ENTRIES.size() > KEEP) ENTRIES.removeLast();
        if (c.tfSound) {
            float up = Math.min(0.5f, (n - 1) * 0.1f);
            Sfx.self(new Sfx.Layer[]{Sfx.Layer.of("block.note_block.chime", 0.45f, 1.2f + up), Sfx.Layer.of("block.amethyst_block.hit", 0.6f, 1.5f + up, 2)},
                    1f);
        }
    }

    /** Varlik oldu: bildirimi "oldu" olarak isaretle. */
    public static void onDeath(int entityId) {
        for (Entry x : ENTRIES) {
            if (x.entityId == entityId && !x.died) {
                x.died = true;
                x.time = Util.getMillis();
            }
        }
    }

    /** Ornek satirlar (akisa eklenmez). */
    public static java.util.List<Entry> sample() {
        long now = Util.getMillis();
        Entry a = new Entry(-101, "Kral_Ahmet", new UUID(0, 101), false, 3, now);
        Entry b = new Entry(-102, "Ayse_Pvp", new UUID(0, 102), false, 1, now - 1200);
        b.pulseAt = now - 1200;
        Entry d = new Entry(-103, "Mehmet35", new UUID(0, 103), false, 5, now - 2500);
        d.pulseAt = now - 2500;
        d.died = true;
        return java.util.List.of(a, b, d);
    }

    /** Ayar ekranindan deneme. */
    public static void demo() {
        ENTRIES.removeIf(x -> x.entityId <= -100);
        java.util.List<Entry> s = sample();
        for (int i = s.size() - 1; i >= 0; i--) ENTRIES.addFirst(s.get(i));
        while (ENTRIES.size() > KEEP) ENTRIES.removeLast();
    }

    public static void clear() {
        ENTRIES.clear();
    }
}
