package com.jestron.jestronpriv.client.pvp;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PvP ozelliklerinin ortak ses calicisi: oyunun kendi sesleri, tick gecikmeli katmanlar, ayni tick'te
 * ses patlamasi sinirlamasi. Ses kanali Genel Ayarlar'daki efekt kanalidir.
 */
public final class Sfx {

    /** Katman: anahtar ("minecraft:" eklenir), ses, perde, gecikme (tick). */
    public record Layer(String key, float vol, float pitch, int delay) {
        public static Layer of(String key, float vol, float pitch) {
            return new Layer("minecraft:" + key, vol, pitch, 0);
        }

        public static Layer of(String key, float vol, float pitch, int delay) {
            return new Layer("minecraft:" + key, vol, pitch, delay);
        }
    }

    private record Pending(long due, String key, boolean self, double x, double y, double z, float vol, float pitch) {
    }

    private static final List<Pending> PENDING = new ArrayList<>();
    private static final Map<String, SoundEvent> CACHE = new HashMap<>();
    private static final RandomSource RND = RandomSource.create();
    private static long ticks, stamp = -1;
    private static int played;

    private Sfx() {
    }

    private static SoundSource source() {
        JestronPrivClient m = JestronPrivClient.get();
        ModConfig c = m == null ? null : m.config();
        return c == null ? SoundSource.MASTER : c.soundSource();
    }

    /** Katmanlari oyuncunun kulaginda calar (konumsuz his, tam ses). */
    public static void self(Layer[] layers, float volume) {
        play(layers, volume, true, 0, 0, 0);
    }

    /** Katmanlari dunyada bir noktada calar. */
    public static void at(Layer[] layers, float volume, double x, double y, double z) {
        play(layers, volume, false, x, y, z);
    }

    private static void play(Layer[] layers, float volume, boolean self, double x, double y, double z) {
        if (layers == null || !(volume > 0f)) return;
        float j = 1f + (RND.nextFloat() - 0.5f) * 0.04f;
        for (Layer l : layers) {
            float v = Math.min(2f, l.vol() * volume), p = Math.max(0.5f, Math.min(2f, l.pitch() * j));
            if (l.delay() <= 0) now(l.key(), self, x, y, z, v, p);
            else if (PENDING.size() < 96) PENDING.add(new Pending(ticks + l.delay(), l.key(), self, x, y, z, v, p));
        }
    }

    public static void tick(Minecraft mc) {
        ticks++;
        if (mc.level == null) {
            PENDING.clear();
            return;
        }
        for (int i = 0; i < PENDING.size(); ) {
            Pending p = PENDING.get(i);
            if (p.due() <= ticks) {
                PENDING.remove(i);
                now(p.key(), p.self(), p.x(), p.y(), p.z(), p.vol(), p.pitch());
            } else {
                i++;
            }
        }
    }

    private static void now(String key, boolean self, double x, double y, double z, float vol, float pitch) {
        try {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer p = mc.player;
            if (mc.level == null || p == null) return;
            if (stamp != ticks) {
                stamp = ticks;
                played = 0;
            }
            if (++played > 10) return;   // ayni tick'te en fazla 10 katman
            SoundEvent ev = CACHE.computeIfAbsent(key, k -> {
                ResourceLocation rl = ResourceLocation.tryParse(k);
                if (rl == null) return null;
                SoundEvent e = BuiltInRegistries.SOUND_EVENT.getValue(rl);
                return e != null ? e : SoundEvent.createVariableRangeEvent(rl);
            });
            if (ev == null) return;
            double sx = self ? p.getX() : x, sy = self ? p.getEyeY() : y, sz = self ? p.getZ() : z;
            mc.getSoundManager().play(new SimpleSoundInstance(ev, source(), vol, pitch, RND, sx, sy, sz));
        } catch (Throwable ignored) {
            // ses asla oyunu bozmasin
        }
    }
}
