package com.jestron.jestronpriv.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Istemci tarafinda "bir oyuncuyu GERCEKTEN ben oldurdum" tespiti.
 *
 * <p><b>Kesinlestirme mantigi.</b> Sinyaller iki sinifa ayrilir:</p>
 * <ul>
 *   <li><b>Guclu</b> (tek basina yeterli):
 *     <ul>
 *       <li>Sunucunun olum olayi (entity event 3) — sadece son <b>5 saniyede</b> (sunucunun oldurme kredisi suresi)
 *           vurdugun biri icin ve senden sonra yakindan baska bir oyuncu ona vurmadiysa.</li>
 *       <li>Vurdugun varligin olum pozuna gecmesi veya canin 2 tick boyunca 0 kalmasi (ayni kural).</li>
 *       <li>Vanilla olum mesajinda oldurucu olarak senin adin.</li>
 *       <li>Seni oldurucu olarak acikca gosteren sunucu mesaji ("Jestron killed X", "You killed X",
 *           "X was slain by Jestron", "X adli oyuncuyu oldurdun").</li>
 *     </ul></li>
 *   <li><b>Zayif</b> (tek basina yetmez, sik dogrulamada ikinci bir farkli zayif sinyal gerekir):
 *     <ul>
 *       <li>Vurdugun oyuncunun vurustan hemen sonra dunyadan silinmesi (isinlanma / cikis da olabilir).</li>
 *       <li>Kimin oldurdugunu soylemeyen mesaj ("+1 Kill | X", "[Kill] X").</li>
 *     </ul></li>
 * </ul>
 * Ayni kurban icin 3 saniye icinde ikinci kez tetiklenmez. Sik dogrulama kapatilirsa zayif sinyal de tek basina yeter.
 */
public final class KillDetector {

    public interface Listener {
        void onKill(Entity victimOrNull, String victimName, Vec3 pos);
    }

    /** Sunucunun oyuncu oldurme kredisi: son vurustan sonra 100 tick (5 sn). */
    private static final long HIT_WINDOW = 100;
    /** Aninda silinme: vurustan sonra 0.75 sn. */
    private static final long REMOVE_WINDOW = 15;
    /** Ozel mesajlar: vurustan sonra 8 sn. */
    private static final long TEXT_WINDOW = 160;
    /** Iki zayif sinyalin birbirini dogrulamasi icin azami aralik (1.5 sn). */
    private static final long WEAK_PAIR_WINDOW = 30;
    private static final long DEDUP = 60;
    /** Baska oyuncunun vurusu sayilmasi icin kurbana azami uzaklik. */
    private static final double OTHER_REACH = 4.8;

    private static final String[] KILL_WORDS = {
            "kill", "slain", "slew", "murder", "eliminat", "destroy", "defeat", "finish", "execut", "rekt", "wrecked",
            "öldür", "oldur", "kesti", "kestin", "katlet", "yendi", "yendin", "indirdin", "☠", "⚔", "🗡"};
    /** Oldurme olayi degil, istatistik / durum mesajlari. */
    private static final String[] NOT_EVENT = {
            "killstreak", "kill streak", "kills:", "kills ", "k/d", "kdr", "bounty", "ödül", "seri ",
            "streak", "leaderboard", "sıralama", "combat", "savaş", "logged", "çıkış yaptı"};
    private static final String[] SELF_DEATH = {
            "killed you", "slain you", "slew you", "murdered you", "eliminated you", "defeated you", "destroyed you",
            "you died", "you were", "you have died", "seni öldürdü", "seni kesti", "seni yendi", "öldün", "öldürüldün", "kesildin"};
    /** Ikinci sahis, aktif: oldurenin sen oldugunu soyler. */
    private static final String[] YOU_KILLED = {
            "you killed", "you have killed", "you slew", "you eliminated", "you defeated", "you destroyed", "you finished",
            "öldürdün", "kestin", "yendin", "indirdin", "öldürdünüz"};

    private record Weak(String kind, long tick) {
    }

    private final Listener listener;
    /** Vurulan varlik id -> son vurusum */
    private final Map<Integer, Long> hits = new HashMap<>();
    /** Vurulan varlik id -> baska bir oyuncunun son vurusu */
    private final Map<Integer, Long> otherHits = new HashMap<>();
    /** Vurulan varlik id -> cani 0 gorulen ardisik tick sayisi */
    private final Map<Integer, Integer> zeroHp = new HashMap<>();
    /** Vurulan oyuncu adi -> son vurusum */
    private final Map<String, Long> hitNames = new HashMap<>();
    /** Kurban adi -> bekleyen zayif sinyal */
    private final Map<String, Weak> weak = new HashMap<>();
    private final Map<String, Long> recent = new HashMap<>();
    private final Map<String, Vec3> lastPos = new HashMap<>();
    private final Map<String, Long> lastSeen = new HashMap<>();
    private long now;
    private boolean mobKills;
    private boolean debug;
    private boolean strict = true;

    public KillDetector(Listener listener) {
        this.listener = listener;
    }

    public void setMobKills(boolean b) {
        this.mobKills = b;
    }

    public void setDebug(boolean b) {
        this.debug = b;
    }

    public void setStrict(boolean b) {
        this.strict = b;
    }

    public void reset() {
        hits.clear();
        otherHits.clear();
        zeroHp.clear();
        hitNames.clear();
        weak.clear();
        recent.clear();
        lastPos.clear();
        lastSeen.clear();
    }

    private boolean tracks(Entity e) {
        Minecraft mc = Minecraft.getInstance();
        return e instanceof LivingEntity && e != mc.player && (e instanceof Player || mobKills);
    }

    /** Son 5 sn icinde ben vurdum ve benden sonra baskasi vurmadi mi. */
    private boolean myCredit(Entity e) {
        Long mine = hits.get(e.getId());
        if (mine == null || now - mine > HIT_WINDOW) return false;
        Long other = otherHits.get(e.getId());
        return other == null || other <= mine;
    }

    // ------------------------------------------------------------------ sinyaller

    /** Yerel oyuncu bir varliga vurdu. */
    public void onAttack(Entity target) {
        if (!tracks(target)) return;
        hits.put(target.getId(), now);
        if (target instanceof Player p) {
            String n = p.getGameProfile().getName();
            hitNames.put(n, now);
            lastPos.put(n, p.position());
            lastSeen.put(n, now);
        }
    }

    /** Sunucudan olum olayi (entity event 3) geldi. Guclu sinyal. */
    public void onDeathEvent(LivingEntity e) {
        if (!tracks(e)) return;
        if (myCredit(e)) {
            fire(e, nameOf(e), e.position(), "ölüm olayı");
        } else if (debug && hits.containsKey(e.getId())) {
            note("§7" + nameOf(e) + " öldü ama son vuran sen değilsin / 5 sn geçti §8(efekt yok)");
        }
    }

    /** Bir varlik istemci dunyasindan silinmek uzere. */
    public void onRemoved(Entity e) {
        if (e == null || !tracks(e)) return;
        Long t = hits.get(e.getId());
        if (t == null) return;
        LivingEntity le = (LivingEntity) e;
        boolean dead = le.getPose() == Pose.DYING || le.deathTime > 0;
        if (dead && myCredit(e)) {
            fire(e, nameOf(e), e.position(), "ölüm pozu");
        } else if (e instanceof Player && now - t <= REMOVE_WINDOW && myCredit(e)) {
            weakSignal(e, nameOf(e), e.position(), "anında silinme");
        }
    }

    public void tick() {
        now++;
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) return;

        // Oyuncu konumlarini hatirla (mesaj geldiginde varlik kaybolmus olabilir)
        if (now % 2 == 0) {
            for (Player p : level.players()) {
                if (p == mc.player) continue;
                String n = p.getGameProfile().getName();
                lastPos.put(n, p.position());
                lastSeen.put(n, now);
            }
            lastSeen.entrySet().removeIf(e -> {
                if (now - e.getValue() <= 400) return false;
                lastPos.remove(e.getKey());
                return true;
            });
        }

        Iterator<Map.Entry<Integer, Long>> it = hits.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Long> e = it.next();
            if (now - e.getValue() > HIT_WINDOW) {
                it.remove();
                otherHits.remove(e.getKey());
                zeroHp.remove(e.getKey());
                continue;
            }
            Entity ent = level.getEntity(e.getKey());
            if (!(ent instanceof LivingEntity le)) continue; // silinme onRemoved'da islenir

            // Kredi hirsizligi korumasi: benden sonra yakindan vuran baska oyuncu var mi
            for (Player o : level.players()) {
                if (o == mc.player || o == ent) continue;
                if (o.swinging && o.swingTime <= 0 && o.position().distanceTo(ent.position()) <= OTHER_REACH) {
                    Vec3 look = o.getLookAngle();
                    Vec3 to = ent.position().add(0, 1.0, 0).subtract(o.getEyePosition()).normalize();
                    if (look.dot(to) > 0.55) otherHits.put(e.getKey(), now);
                }
            }

            boolean dying = le.getPose() == Pose.DYING || le.deathTime > 0;
            int z = le.getHealth() <= 0f ? zeroHp.merge(e.getKey(), 1, Integer::sum) : 0;
            if (z == 0) zeroHp.remove(e.getKey());
            if ((dying || z >= 2) && myCredit(ent)) {
                it.remove();
                fire(ent, nameOf(ent), ent.position(), dying ? "ölüm pozu" : "can 0");
            }
        }
        hitNames.entrySet().removeIf(e -> now - e.getValue() > TEXT_WINDOW);
        weak.entrySet().removeIf(e -> now - e.getValue().tick > WEAK_PAIR_WINDOW);
        recent.entrySet().removeIf(e -> now - e.getValue() > DEDUP);
    }

    /** Sohbet / sistem mesaji / aksiyon cubugu / baslik. */
    public void onMessage(Component message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || message == null) return;
        if (message.getContents() instanceof TranslatableContents tc && tc.getKey().startsWith("death.")) {
            onVanillaDeath(tc);
            return;
        }
        onCustomText(message.getString());
    }

    private void onVanillaDeath(TranslatableContents tc) {
        Minecraft mc = Minecraft.getInstance();
        Object[] args = tc.getArgs();
        if (args.length < 2) return;
        Component victimC = asComponent(args[0]);
        Component killerC = asComponent(args[1]);
        if (victimC == null || killerC == null) return;
        if (!isMe(killerC) || isMe(victimC)) return;

        UUID vid = hoverUuid(victimC);
        Entity victim = null;
        if (vid != null) victim = mc.level.getPlayerByUUID(vid);
        String name = insertionOrString(victimC);
        if (victim == null) victim = playerByName(name);
        if (victim != null) {
            fire(victim, nameOf(victim), victim.position(), "ölüm mesajı");
        } else {
            Vec3 pos = lastPos.get(name);
            if (pos != null) fire(null, name, pos, "ölüm mesajı");
        }
    }

    /** Eklentilerin ozel oldurme mesajlari. Sadece son 8 sn'de vurdugun oyuncular icin. */
    private void onCustomText(String raw) {
        if (raw == null || raw.isEmpty() || hitNames.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        String text = raw.replaceAll("§.", "");
        if (text.length() > 160) return; // uzun duyurular / sohbet bloklari oldurme olayi degildir
        String lower = text.toLowerCase(Locale.ROOT);
        String lowerTr = text.toLowerCase(Locale.forLanguageTag("tr"));
        if (!containsAny(lower, lowerTr, KILL_WORDS)) return;
        if (containsAny(lower, lowerTr, SELF_DEATH) || containsAny(lower, lowerTr, NOT_EVENT)) return;
        if (mc.player.isDeadOrDying()) return;
        String me = mc.player.getGameProfile().getName();
        String meLower = me.toLowerCase(Locale.ROOT);
        String meLowerTr = me.toLowerCase(Locale.forLanguageTag("tr"));

        for (Map.Entry<String, Long> e : hitNames.entrySet()) {
            String victim = e.getKey();
            if (victim.equalsIgnoreCase(me) || now - e.getValue() > TEXT_WINDOW) continue;
            int vi = indexOfName(text, victim);
            if (vi < 0) continue;

            boolean byMe = lower.contains("by " + meLower) || lowerTr.contains(meLowerTr + " tarafından");
            // "X was slain by Y" / "X, Y tarafindan" -> olduren baskasi (veya kurban)
            if (!byMe && (lower.contains(" by ") || lowerTr.contains("tarafından"))) continue;
            int mi = indexOfName(text, me);
            // "X killed Jestron": kurban once, ben sonra -> olduren kurban
            if (!byMe && mi >= 0 && vi < mi) continue;
            // "Y killed X": baska bir oyuncu adi geciyor
            if (!byMe && mi < 0 && mentionsOtherPlayer(text, victim, me, mc)) continue;

            boolean strong = byMe || (mi >= 0 && mi < vi) || containsAny(lower, lowerTr, YOU_KILLED);
            Entity ent = playerByName(victim);
            Vec3 pos = ent != null ? ent.position() : lastPos.get(victim);
            if (pos == null) continue;
            if (strong) fire(ent, victim, pos, "sunucu mesajı");
            else weakSignal(ent, victim, pos, "sunucu mesajı");
            return;
        }
    }

    /** Zayif sinyal: sik dogrulamada farkli ikinci bir zayif sinyal 1.5 sn icinde gelirse onaylanir. */
    private void weakSignal(Entity victim, String name, Vec3 pos, String kind) {
        if (name == null) return;
        if (!strict) {
            fire(victim, name, pos, kind);
            return;
        }
        Weak w = weak.get(name);
        if (w != null && !w.kind.equals(kind) && now - w.tick <= WEAK_PAIR_WINDOW) {
            weak.remove(name);
            fire(victim, name, pos, w.kind + " + " + kind);
        } else {
            weak.put(name, new Weak(kind, now));
            if (debug) note("§7" + name + ": tek zayıf sinyal (" + kind + "), doğrulama bekleniyor");
        }
    }

    private Player playerByName(String name) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || name == null) return null;
        for (Player p : mc.level.players()) {
            if (p.getGameProfile().getName().equals(name)) return p;
        }
        return null;
    }

    /** Mesajda kurban ve ben disinda, yakindaki baska bir oyuncu adi geciyor mu. */
    private boolean mentionsOtherPlayer(String text, String victim, String me, Minecraft mc) {
        if (mc.level == null) return false;
        for (Player p : mc.level.players()) {
            String n = p.getGameProfile().getName();
            if (n.equals(victim) || n.equals(me)) continue;
            if (indexOfName(text, n) >= 0) return true;
        }
        return false;
    }

    private static boolean containsAny(String a, String b, String[] words) {
        for (String w : words) if (a.contains(w) || b.contains(w)) return true;
        return false;
    }

    /** Isim, harf/rakam/alt cizgi ile bitisik olmayan bir kelime olarak geciyor mu. */
    private static int indexOfName(String text, String name) {
        if (name == null || name.isEmpty()) return -1;
        int from = 0;
        while (true) {
            int i = text.indexOf(name, from);
            if (i < 0) return -1;
            boolean okL = i == 0 || !isNameChar(text.charAt(i - 1));
            int end = i + name.length();
            boolean okR = end >= text.length() || !isNameChar(text.charAt(end));
            if (okL && okR) return i;
            from = i + 1;
        }
    }

    private static boolean isNameChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_';
    }

    private void note(String s) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) mc.player.displayClientMessage(Component.literal("§8[§5JestronPriv§8] " + s), false);
    }

    private void fire(Entity victim, String name, Vec3 pos, String source) {
        if (pos == null) return;
        String key = victim != null ? victim.getUUID().toString() : name;
        if (recent.containsKey(key) || (name != null && recent.containsKey("n:" + name))) return;
        recent.put(key, now);
        if (name != null) {
            recent.put("n:" + name, now);
            hitNames.remove(name);
            weak.remove(name);
        }
        if (victim != null) {
            hits.remove(victim.getId());
            otherHits.remove(victim.getId());
            zeroHp.remove(victim.getId());
        }
        if (debug) note("§7Öldürme algılandı: §f" + name + " §8(" + source + ")");
        listener.onKill(victim, name, pos);
    }

    // ------------------------------------------------------------------ yardimcilar

    private static String nameOf(Entity e) {
        if (e instanceof Player p) return p.getGameProfile().getName();
        return e.getName().getString();
    }

    private static Component asComponent(Object o) {
        if (o instanceof Component c) return c;
        if (o == null) return null;
        return Component.literal(String.valueOf(o));
    }

    private boolean isMe(Component c) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        UUID me = mc.player.getUUID();
        String myName = mc.player.getGameProfile().getName();
        UUID u = hoverUuid(c);
        if (u != null) return u.equals(me);
        String ins = insertion(c);
        if (ins != null) return ins.equals(myName);
        String s = c.getString();
        if (s.equals(myName)) return true;
        // Takim on eki/son eki olan isimler: "[VIP] Isim"
        for (String part : s.split("[^A-Za-z0-9_]+")) if (part.equals(myName)) return true;
        return false;
    }

    private static UUID hoverUuid(Component c) {
        Style st = c.getStyle();
        HoverEvent he = st.getHoverEvent();
        if (he != null) {
            HoverEvent.EntityTooltipInfo info = he.getValue(HoverEvent.Action.SHOW_ENTITY);
            if (info != null) return info.id;
        }
        for (Component sib : c.getSiblings()) {
            UUID u = hoverUuid(sib);
            if (u != null) return u;
        }
        return null;
    }

    private static String insertion(Component c) {
        String ins = c.getStyle().getInsertion();
        if (ins != null && !ins.isEmpty()) return ins;
        for (Component sib : c.getSiblings()) {
            String s = insertion(sib);
            if (s != null) return s;
        }
        return null;
    }

    private static String insertionOrString(Component c) {
        String ins = insertion(c);
        return ins != null ? ins : c.getString();
    }
}
