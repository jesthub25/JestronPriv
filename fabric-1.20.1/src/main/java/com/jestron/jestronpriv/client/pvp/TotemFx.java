package com.jestron.jestronpriv.client.pvp;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.cosmetic.HitFx;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

/**
 * Totem animasyonu: herhangi bir varlik totem patlatinca (sunucu olayi 35) altin-yesil isik halkalari, govde
 * etrafinda yukselen sarmal, isiltilar, katmanli kutsal ses ve basinin ustunde "TOTEM ×N" sayaci.
 * Sayac o varlik olunce sifirlanir. Istege bagli: vanilla totem partikulunu / ekrandaki totem animasyonunu gizle,
 * sohbete yaz.
 */
public final class TotemFx {

    private static final Map<Integer, Integer> COUNTS = new HashMap<>();

    private TotemFx() {
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    public static int count(int entityId) {
        return COUNTS.getOrDefault(entityId, 0);
    }

    /** Totem patladi (istemci ana thread'i, vanilla islemden sonra). */
    public static void onPop(Entity e) {
        ModConfig c = cfg();
        if (c == null || e == null) return;
        int n = COUNTS.merge(e.getId(), 1, Integer::sum);
        if (COUNTS.size() > 256) COUNTS.clear();
        try {
            TotemFeed.onPop(e, n);
        } catch (Throwable ignored) {
        }
        // cihazdan secilen ozel ses (efekt kapali olsa bile calar)
        boolean custom = false;
        try {
            custom = com.jestron.jestronpriv.client.audio.CustomSound.playFor(e);
        } catch (Throwable ignored) {
        }
        if (!c.totemFx) return;
        float sc = Math.max(0.5f, Math.min(2f, c.totemScale));
        HitFx.totemBurst(e, sc);
        if (c.totemSound && !custom) Sfx.at(sound(n), 1f, e.getX(), e.getY() + 1, e.getZ());
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer me = mc.player;
        if (c.totemCounter) {
            int col = n >= 5 ? 0xFF3B3B : n >= 3 ? 0xFF9A2E : 0xFFD54F;
            WorldText.label(e, "✦ TOTEM ×" + n + " ✦", 3.2f, 1.25f, col);
        }
        if (c.totemChat && me != null) {
            String who = e == me ? "Sen" : e instanceof Player pl ? pl.getGameProfile().getName() : e.getName().getString();
            me.displayClientMessage(Component.literal("§6✦ Totem §8» §f" + who + " §7totem patlattı §8(§e" + n + ".§8)"), false);
        }
    }

    /** Katmanli kutsal ses; ust uste totemlerde perde yukselir. */
    public static Sfx.Layer[] sound(int n) {
        // perde 2'yi (oyunun ust siniri) asmasin: en tiz katman 1.75 + 0.25
        float up = Math.min(0.25f, (n - 1) * 0.05f);
        return new Sfx.Layer[]{Sfx.Layer.of("block.beacon.power_select", 0.55f, 1.5f + up), Sfx.Layer.of("block.amethyst_block.chime", 0.9f, 1.3f + up),
                Sfx.Layer.of("entity.player.levelup", 0.35f, 1.2f + up, 2), Sfx.Layer.of("block.amethyst_block.chime", 0.7f, 1.75f + up, 4)};
    }

    /** Varlik oldu: sayac sifirlanir. */
    public static void onDeath(int entityId) {
        COUNTS.remove(entityId);
        try {
            TotemFeed.onDeath(entityId);
        } catch (Throwable ignored) {
        }
    }

    public static void clear() {
        COUNTS.clear();
    }

    /** Vanilla totem partikul yayicisi iptal edilsin mi. */
    public static boolean hideVanillaParticles(ParticleOptions o) {
        ModConfig c = cfg();
        return c != null && c.totemFx && c.totemHideVanilla && o == ParticleTypes.TOTEM_OF_UNDYING;
    }

    /** Ekrandaki buyuk totem animasyonu iptal edilsin mi. */
    public static boolean hideScreen(ItemStack s) {
        ModConfig c = cfg();
        return c != null && c.totemHideScreen && s != null && s.is(Items.TOTEM_OF_UNDYING);
    }

    /** Ayar ekranindan deneme. */
    public static void preview() {
        ModConfig c = cfg();
        Minecraft mc = Minecraft.getInstance();
        if (c == null || mc.player == null) return;
        float sc = Math.max(0.5f, Math.min(2f, c.totemScale));
        HitFx.totemPreview(sc);
        boolean custom = false;
        try {
            custom = c.totemCustom && com.jestron.jestronpriv.client.audio.CustomSound.preview();
        } catch (Throwable ignored) {
        }
        if (c.totemSound && !custom) Sfx.self(sound(1), 1f);
    }
}
