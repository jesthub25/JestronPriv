package com.jestron.jestronpriv.jc.modules;

import com.jestron.jestronpriv.jc.core.Category;
import com.jestron.jestronpriv.jc.core.Module;
import com.jestron.jestronpriv.jc.core.Setting;
import com.jestron.jestronpriv.jc.util.ChatSender;
import com.jestron.jestronpriv.jc.util.TextUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.List;

/**
 * Otomatik GG. Ne zaman yazılacağı seçeneklerle seçilir:
 * oyun bitince (sohbet / ekran yazısı), birini öldürünce, ölünce. Bekleme süresiyle spam yapmaz.
 */
public final class AutoGgModule extends Module {
    private static final String[] PRESETS = {"Yazdığım mesaj", "gg", "gg wp", "GG!", "gf", "iyi oyundu"};
    /** Oyun sonu: sunucuların sohbette ya da ekran ortasında gösterdiği yazılar. */
    private static final List<String> GAME_END = List.of(
            "kazandin", "kazandi", "kazanan", "oyun bitti", "oyunu kaybettin", "kaybettin", "mac bitti", "macin galibi",
            "victory", "winner", "you won", "you win", "game over", "you lost", "defeat", "won the game", "has won",
            "1st killer", "#1 winner");
    private static final List<String> KILL_WORDS = List.of(
            "killed", "slain", "eliminated", "finished", "shot", "olduruldu", "oldurdu", "tarafindan", "kesildi", "eledi");

    private final Setting.Bool onGameEnd = add(new Setting.Bool("onGameEnd", "Oyun bitince (kazanınca / kaybedince)", true));
    private final Setting.Bool onKill = add(new Setting.Bool("onKill", "Birini öldürünce", false));
    private final Setting.Bool onDeath = add(new Setting.Bool("onDeath", "Ölünce", false));
    private final Setting.Choice preset = add(new Setting.Choice("preset", "Mesaj", 0, PRESETS));
    private final Setting.Text message = add(new Setting.Text("message", "Yazdığım mesaj", "gg", 100));
    private final Setting.Num cooldown = add(new Setting.Num("cooldown", "Bekleme süresi", 15, 3, 120, 1, " sn"));
    private final Setting.Num delay = add(new Setting.Num("delay", "Gönderme gecikmesi", 1, 0, 5, 0.5, " sn"));
    private final Setting.Bool playerChat = add(new Setting.Bool("playerChat", "Oyuncu mesajlarında da oyun sonuna bak", false));
    private final Setting.Text extra = add(new Setting.Text("extraTriggers", "Ek oyun sonu kelimeleri (isteğe bağlı, virgülle)", "", 256));

    private long lastFired;
    private int pendingTicks = -1;
    private boolean wasDead;

    public AutoGgModule() {
        super("autogg", "Otomatik GG", "Oyun bitince, birini öldürünce ya da ölünce otomatik gg yazar.", Category.CHAT, false);
    }

    public String text() {
        int i = preset.index();
        return i <= 0 ? message.get() : PRESETS[i];
    }

    private String me() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player == null ? "" : mc.player.getGameProfile().getName();
    }

    private static String str(Object o) {
        return o instanceof Component c ? c.getString() : String.valueOf(o);
    }

    /** Sunucu / oyun mesajı. */
    public void onGameMessage(Component msg) {
        if (!isEnabled() || msg == null) return;
        String me = me();
        int kind = classifyDeath(msg, me);
        if (kind == 1) {
            if (onKill.on()) fire();
            return;
        }
        if (kind == 2) return; // kendi ölümün can takibiyle yakalanır
        String plain = msg.getString();
        if (onGameEnd.on() && gameEnd(plain)) {
            fire();
            return;
        }
        if (onKill.on() && killedBy(plain, me)) fire();
    }

    /**
     * Sunucuya özel öldürme mesajı mı ve öldüren sen misin:
     * "X was slain by Sen" / "X, Sen tarafından öldürüldü" / "Sen killed X" → evet; ölen sensen hayır.
     */
    public static boolean killedBy(String plain, String me) {
        if (plain == null || me == null || me.isEmpty()) return false;
        String n = TextUtil.norm(plain), m = TextUtil.norm(me);
        int at = n.indexOf(m);
        if (at < 0 || KILL_WORDS.stream().noneMatch(n::contains)) return false;
        int by = n.indexOf(" by "), tr = n.indexOf("tarafindan");
        if (by >= 0) return at > by;
        if (tr >= 0) return at > 0 && at < tr;
        return at == 0;
    }

    public void onPlayerMessage(String plain) {
        if (isEnabled() && onGameEnd.on() && playerChat.on() && gameEnd(plain)) fire();
    }

    /** Vanilla ölüm mesajı (death.*: 0 ölen, 1 öldüren). @return 1: sen öldürdün, 2: sen öldün, 0: değil */
    public static int classifyDeath(Component msg, String me) {
        if (me == null || me.isEmpty() || !(msg.getContents() instanceof TranslatableContents tc) || !tc.getKey().startsWith("death.")) return 0;
        Object[] a = tc.getArgs();
        if (a.length >= 1 && me.equalsIgnoreCase(str(a[0]))) return 2;
        if (a.length >= 2 && me.equalsIgnoreCase(str(a[1]))) return 1;
        return 0;
    }

    /** Ekranın ortasındaki büyük yazı (birçok minigame kazananı böyle gösterir). */
    public void onTitle(Component title) {
        if (isEnabled() && onGameEnd.on() && title != null && gameEnd(title.getString())) fire();
    }

    private boolean gameEnd(String plain) {
        if (plain == null || plain.isBlank()) return false;
        String n = TextUtil.norm(plain);
        for (String t : GAME_END) if (n.contains(t)) return true;
        for (String t : TextUtil.splitList(extra.get())) if (!t.isEmpty() && n.contains(t)) return true;
        return false;
    }

    /** Test ve geriye uyumluluk: düz metinle oyun sonu kontrolü. */
    public boolean check(String plain) {
        if (!isEnabled() || !onGameEnd.on() || !gameEnd(plain)) return false;
        return fire();
    }

    private boolean fire() {
        if (text().isBlank()) return false;
        long now = System.currentTimeMillis();
        if (now - lastFired < cooldown.d() * 1000 || pendingTicks >= 0) return false;
        lastFired = now;
        pendingTicks = (int) Math.round(delay.d() * 20);
        return true;
    }

    @Override
    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        boolean dead = mc.player != null && mc.player.isDeadOrDying();
        if (dead && !wasDead && onDeath.on()) fire();
        wasDead = dead;
        if (pendingTicks < 0) return;
        if (pendingTicks-- == 0) {
            ChatSender.send(text(), false);
            pendingTicks = -1;
        }
    }

    @Override
    protected void onDisable() {
        pendingTicks = -1;
    }
}
