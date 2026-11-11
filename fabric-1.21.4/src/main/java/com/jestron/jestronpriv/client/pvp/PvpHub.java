package com.jestron.jestronpriv.client.pvp;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.cosmetic.HitFx;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** PvP olaylarinin tek dagitim noktasi: hasar olayi, kritik, oldurme, tick. Her alt sistem hatadan yalitilir. */
public final class PvpHub {

    private PvpHub() {
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    /** Sunucunun hasar olayi (istemci ana thread'i). */
    public static void onDamageEvent(int entityId, int causeId, int directId) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null) return;
        if (entityId == p.getId()) {
            safe(Combo::onHurt);
            return;
        }
        safe(() -> HitFx.onDamageEvent(entityId, causeId, directId));
        // vuran sen (yakin dovus ya da senin okun / mizragin)
        if (causeId != p.getId()) return;
        Entity e = mc.level.getEntity(entityId);
        if (!(e instanceof LivingEntity le)) return;
        safe(() -> Reach.onConfirmedHit(entityId));
        safe(() -> PingTracker.setTarget(e));
        ModConfig c = cfg();
        if (c == null) return;
        safe(() -> HitColor.onHit(entityId));
        if (!(e instanceof Player) && !c.pvpMobs) return;
        safe(() -> DamageNumbers.onHit(le));
        safe(Combo::onHit);
        safe(() -> Crosshair.onHit(Crosshair.HIT));
        safe(() -> com.jestron.jestronpriv.client.cosmetic.Pets.react(false));
    }

    /** Kritik animasyonu senin vurdugun hedefe geldi. */
    public static void onCrit(int entityId) {
        safe(() -> DamageNumbers.markCrit(entityId));
        safe(() -> Crosshair.onHit(Crosshair.HIT_CRIT));
    }

    /** Dogrulanmis oldurme. */
    public static void onKill(String victim) {
        safe(() -> KillStreak.onKill(victim));
        safe(() -> KillFeed.onMyKill(victim));
        safe(() -> Crosshair.onHit(Crosshair.HIT_KILL));
        safe(() -> com.jestron.jestronpriv.client.cosmetic.Pets.react(true));
    }

    public static void tick(Minecraft mc) {
        safe(() -> Sfx.tick(mc));
        safe(() -> DamageNumbers.tick(mc));
        safe(() -> Combo.tick(mc));
        safe(() -> KillStreak.tick(mc));
        safe(() -> PingTracker.tick(mc));
        safe(HitColor::tick);
    }

    /** Dunya degisti (sunucu / boyut). */
    public static void onLevelChange() {
        safe(WorldText::clear);
        safe(TotemFx::clear);
        safe(TotemFeed::clear);
        safe(PingTracker::reset);
        safe(Combo::reset);
        safe(com.jestron.jestronpriv.client.view.WorldTime::reset);
    }

    private static void safe(Runnable r) {
        try {
            r.run();
        } catch (Throwable t) {
            // tek bir alt sistemin hatasi digerlerini ve oyunu asla bozmasin
            JestronPrivClient.LOG.debug("PvP olayi hatasi", t);
        }
    }
}
