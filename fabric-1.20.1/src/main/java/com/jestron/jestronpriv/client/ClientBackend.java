package com.jestron.jestronpriv.client;

import com.jestron.jestronpriv.fx.FxBackend;
import com.jestron.jestronpriv.fx.api.Color;
import com.jestron.jestronpriv.fx.api.Display;
import com.jestron.jestronpriv.fx.api.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** FxBackend'in Minecraft 1.21.4 istemci uygulamasi. */
public final class ClientBackend implements FxBackend {

    private final Minecraft mc = Minecraft.getInstance();
    private final List<Display> displays = new ArrayList<>();
    private final Map<Material, BlockState> blockCache = new EnumMap<>(Material.class);
    private final Map<String, ParticleOptions> simpleCache = new HashMap<>();
    private final RandomSource soundRandom = RandomSource.create();
    private final Supplier<ModConfig> config;
    private static final java.util.Set<String> KEY_PARTICLES =
            java.util.Set.of("flash", "explosion_emitter", "explosion", "sonic_boom", "sweep_attack");
    private long ticks;

    ClientBackend(Supplier<ModConfig> config) {
        this.config = config;
    }

    /** Performans ayarlarina gore parcacik carpani (0.1 - 1.0). */
    private float density() {
        ModConfig c = config.get();
        if (c == null) return 1f;
        float d = c.particleDensity;
        if (c.respectVanillaParticles) {
            ParticleStatus st = mc.options.particles().get();
            if (st == ParticleStatus.DECREASED) d *= 0.6f;
            else if (st == ParticleStatus.MINIMAL) d *= 0.3f;
        }
        return Math.max(0.05f, Math.min(1f, d));
    }

    void tickClock() {
        ticks++;
        flushPending();
    }

    List<Display> displays() {
        return displays;
    }

    void clear() {
        displays.clear();
        pending.clear();
    }

    @Override
    public long now() {
        return ticks;
    }

    @Override
    public void addDisplay(Display d) {
        displays.add(d);
    }

    @Override
    public void removeDisplay(Display d) {
        displays.remove(d);
    }

    BlockState blockState(Material m) {
        return blockCache.computeIfAbsent(m, k -> {
            Block b = BuiltInRegistries.BLOCK.get(new ResourceLocation(k.id()));
            return b.defaultBlockState();
        });
    }

    // ------------------------------------------------------------------ parcaciklar

    /** 1.21.2+ toz parcaciklari 0xRRGGBB renk ister. */
    private static int rgb(Color c) {
        return (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue();
    }

    private ParticleOptions options(String id, Object data) {
        switch (id) {
            case "dust": {
                if (data instanceof Dust d) {
                    Color c = d.color();
                    return new DustParticleOptions(rgb(c), Math.max(0.01f, Math.min(4f, d.size())));
                }
                return null;
            }
            case "dust_color_transition": {
                if (data instanceof DustFade d) {
                    Color a = d.from(), b = d.to();
                    return new DustColorTransitionOptions(rgb(a), rgb(b),
                            Math.max(0.01f, Math.min(4f, d.size())));
                }
                return null;
            }
            case "block": {
                BlockState st = null;
                if (data instanceof BlockState bs) st = bs;
                else if (data instanceof Material m && m.isBlock()) st = blockState(m);
                if (st == null || st.isAir()) return null;
                return new BlockParticleOption(ParticleTypes.BLOCK, st);
            }
            default: {
                ParticleOptions cached = simpleCache.get(id);
                if (cached != null) return cached;
                ParticleType<?> t = BuiltInRegistries.PARTICLE_TYPE.get(new ResourceLocation(id));
                if (t instanceof SimpleParticleType s) {
                    simpleCache.put(id, s);
                    return s;
                }
                return null;
            }
        }
    }

    @Override
    public void particle(String id, double x, double y, double z, int count, double ox, double oy, double oz, double speed, Object data) {
        ClientLevel level = mc.level;
        if (level == null) return;
        RandomSource r = level.random;
        // Efektin ana anlarini tasiyan tekil buyuk parcaciklar hic azaltilmaz
        float density = KEY_PARTICLES.contains(id) ? 1f : density();
        if (count == 0) {
            if (density < 1f && r.nextFloat() > density) return;
        } else if (density < 1f && count > 1) {
            // Beklenen deger korunur: 7 * 0.5 = 3 veya 4
            float scaled = count * density;
            int n = (int) scaled;
            if (r.nextFloat() < scaled - n) n++;
            if (n <= 0) return;
            count = n;
        } else if (density < 1f && r.nextFloat() > density) {
            return;
        }
        ParticleOptions opt = options(id, data);
        if (opt == null) return;
        try {
            if (count == 0) {
                level.addParticle(opt, x, y, z, ox * speed, oy * speed, oz * speed);
            } else {
                for (int i = 0; i < count; i++) {
                    double px = x + r.nextGaussian() * ox;
                    double py = y + r.nextGaussian() * oy;
                    double pz = z + r.nextGaussian() * oz;
                    double vx = r.nextGaussian() * speed;
                    double vy = r.nextGaussian() * speed;
                    double vz = r.nextGaussian() * speed;
                    level.addParticle(opt, px, py, pz, vx, vy, vz);
                }
            }
        } catch (Throwable ignored) {
        }
    }

    // ------------------------------------------------------------------ sesler

    /** Bir sese otomatik eklenen gercekcilik katmani: ek ses, ses carpani, perde carpani, gecikme (tick). */
    private record Layer(String key, float vol, float pitch, int delay) {
    }

    private record Pending(long due, String key, double x, double y, double z, float vol, float pitch) {
    }

    /**
     * Agir carpma ve patlamalara gercek dunyadaki gibi ikincil katmanlar eklenir:
     * dusen enkaz, cinlama, cizirti, uzak yanki. Hepsi vanilla sesleridir.
     */
    private static final Map<String, Layer[]> LAYERS = Map.ofEntries(
            Map.entry("minecraft:entity.generic.explode", new Layer[]{
                    new Layer("minecraft:block.deepslate.break", 0.55f, 0.6f, 1),
                    new Layer("minecraft:block.gravel.break", 0.5f, 0.7f, 5),
                    new Layer("minecraft:block.stone.break", 0.35f, 0.8f, 9)}),
            Map.entry("minecraft:item.mace.smash_ground_heavy", new Layer[]{
                    new Layer("minecraft:block.deepslate.break", 0.6f, 0.55f, 0),
                    new Layer("minecraft:block.gravel.break", 0.45f, 0.8f, 3)}),
            Map.entry("minecraft:block.anvil.land", new Layer[]{
                    new Layer("minecraft:block.stone.break", 0.5f, 0.6f, 0),
                    new Layer("minecraft:block.chain.fall", 0.35f, 0.7f, 2)}),
            Map.entry("minecraft:item.trident.hit_ground", new Layer[]{
                    new Layer("minecraft:block.basalt.break", 0.45f, 0.75f, 0)}),
            Map.entry("minecraft:entity.lightning_bolt.impact", new Layer[]{
                    new Layer("minecraft:block.fire.extinguish", 0.3f, 1.4f, 4),
                    new Layer("minecraft:entity.generic.burn", 0.25f, 0.8f, 8)}),
            Map.entry("minecraft:block.glass.break", new Layer[]{
                    new Layer("minecraft:block.glass.fall", 0.4f, 1.1f, 3)}),
            Map.entry("minecraft:block.amethyst_cluster.break", new Layer[]{
                    new Layer("minecraft:block.amethyst_block.resonate", 0.35f, 1.2f, 4)}),
            Map.entry("minecraft:entity.firework_rocket.large_blast", new Layer[]{
                    new Layer("minecraft:entity.firework_rocket.twinkle_far", 0.45f, 1.0f, 8)}),
            Map.entry("minecraft:entity.warden.sonic_boom", new Layer[]{
                    new Layer("minecraft:entity.generic.explode", 0.25f, 1.5f, 0)}),
            Map.entry("minecraft:entity.dragon_fireball.explode", new Layer[]{
                    new Layer("minecraft:block.gravel.break", 0.4f, 0.7f, 4)}));

    private final List<Pending> pending = new ArrayList<>();
    private final Map<String, Integer> perTick = new HashMap<>();
    private final Map<String, SoundEvent> soundCache = new HashMap<>();
    private long perTickStamp = -1;
    private boolean soundErrorLogged;

    /** Secili ses kanali (varsayilan: Ana Ses; "Oyuncular" kanali %0 olsa bile duyulur). */
    SoundSource soundSource() {
        ModConfig c = config.get();
        return c == null ? SoundSource.MASTER : c.soundSource();
    }

    private boolean realism() {
        ModConfig c = config.get();
        return c == null || c.soundRealism;
    }

    @Override
    public void sound(String key, double x, double y, double z, float volume, float pitch) {
        if (mc.level == null || volume <= 0f) return;
        boolean real = realism();
        int delay = 0;
        if (real) {
            // Hafif dogal perde farki: ayni ses iki kez birebir ayni duyulmaz
            pitch *= 1f + (float) (soundRandom.nextGaussian() * 0.025);
            // Ses hizi: uzak olumlerde ses biraz gec gelir (~17 blokta 1 tick)
            Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
            double d = Math.sqrt(cam.distanceToSqr(x, y, z));
            if (d > 20) delay = (int) Math.min(6, d / 17.0);
        }
        schedule(delay, key, x, y, z, volume, pitch);
        if (real) {
            Layer[] layers = LAYERS.get(key);
            if (layers != null) {
                for (Layer l : layers) {
                    float lp = pitch * l.pitch * (1f + (float) (soundRandom.nextGaussian() * 0.04));
                    schedule(delay + l.delay, l.key, x, y, z, volume * l.vol, lp);
                }
            }
        }
    }

    private void schedule(int delay, String key, double x, double y, double z, float vol, float pitch) {
        if (delay <= 0) playNow(key, x, y, z, vol, pitch);
        else pending.add(new Pending(ticks + delay, key, x, y, z, vol, pitch));
    }

    private void flushPending() {
        if (pending.isEmpty()) return;
        for (int i = 0; i < pending.size(); ) {
            Pending p = pending.get(i);
            if (p.due <= ticks) {
                pending.remove(i);
                playNow(p.key, p.x, p.y, p.z, p.vol, p.pitch);
            } else {
                i++;
            }
        }
    }

    private SoundEvent resolve(String key) {
        SoundEvent ev = soundCache.get(key);
        if (ev != null) return ev;
        ResourceLocation rl = ResourceLocation.tryParse(key);
        if (rl == null) return null;
        ev = BuiltInRegistries.SOUND_EVENT.get(rl);
        if (ev == null) ev = SoundEvent.createVariableRangeEvent(rl);
        soundCache.put(key, ev);
        return ev;
    }

    private void playNow(String key, double x, double y, double z, float volume, float pitch) {
        // Ayni tick'te ayni sesten en fazla 3 tane: ok yagmuru gibi efektlerde ses patlamasini ve kanal tukenmesini onler
        if (perTickStamp != ticks) {
            perTick.clear();
            perTickStamp = ticks;
        }
        int n = perTick.merge(key, 1, Integer::sum);
        if (n > 3) return;
        try {
            SoundEvent ev = resolve(key);
            if (ev == null) return;
            float p = Math.max(0.5f, Math.min(2.0f, pitch));
            mc.getSoundManager().play(new SimpleSoundInstance(ev, soundSource(), Math.min(volume, 3f), p, soundRandom, x, y, z));
        } catch (Throwable t) {
            if (!soundErrorLogged) {
                soundErrorLogged = true;
                JestronPrivClient.LOG.warn("Efekt sesi calinamadi: {}", key, t);
            }
        }
    }

    @Override
    public void stopSound(String key) {
        pending.removeIf(p -> p.key.equals(key));
        ResourceLocation rl = ResourceLocation.tryParse(key);
        if (rl != null) mc.getSoundManager().stop(rl, soundSource());
    }

    /** Ses testi: oyuncunun yaninda kisa bir ses calar, kanal seviyelerini dondurur. */
    String soundTest() {
        if (mc.player == null) return null;
        Vec3 p = mc.player.position();
        playNow("minecraft:block.note_block.bell", p.x, p.y + 1, p.z, 1f, 1f);
        playNow("minecraft:entity.experience_orb.pickup", p.x, p.y + 1, p.z, 0.8f, 1.2f);
        int master = Math.round(mc.options.getSoundSourceVolume(SoundSource.MASTER) * 100);
        SoundSource src = soundSource();
        int chan = src == SoundSource.MASTER ? master : Math.round(mc.options.getSoundSourceVolume(src) * 100);
        ModConfig c = config.get();
        int fx = c == null ? 100 : Math.round(c.volume * 100);
        StringBuilder sb = new StringBuilder("§7Ses testi çalındı. ");
        sb.append(master == 0 ? "§cAna Ses %0" : "§aAna Ses %" + master);
        if (src != SoundSource.MASTER) sb.append(chan == 0 ? " §8| §cKanal %0" : " §8| §aKanal %" + chan);
        sb.append(fx == 0 ? " §8| §cEfekt sesi %0" : " §8| §aEfekt sesi %" + fx);
        if (master == 0 || chan == 0 || fx == 0) sb.append(" §e→ %0 olanı aç!");
        return sb.toString();
    }

    // ------------------------------------------------------------------ dunya sorgulari

    @Override
    public boolean isLoaded(int blockX, int blockZ) {
        ClientLevel level = mc.level;
        return level != null && level.hasChunk(blockX >> 4, blockZ >> 4);
    }

    @Override
    public int minY() {
        ClientLevel level = mc.level;
        return level == null ? -64 : level.getMinY();
    }

    private VoxelShape shape(int x, int y, int z) {
        ClientLevel level = mc.level;
        BlockPos pos = new BlockPos(x, y, z);
        return level.getBlockState(pos).getCollisionShape(level, pos);
    }

    @Override
    public boolean isPassable(int x, int y, int z) {
        if (mc.level == null) return true;
        return shape(x, y, z).isEmpty();
    }

    @Override
    public double topY(int x, int y, int z) {
        if (mc.level == null) return y;
        VoxelShape s = shape(x, y, z);
        return s.isEmpty() ? y : y + s.max(Direction.Axis.Y);
    }

    @Override
    public Object groundState(int x, int y, int z) {
        ClientLevel level = mc.level;
        if (level == null) return null;
        BlockPos pos = new BlockPos(x, y, z);
        BlockState st = level.getBlockState(pos);
        if (st.isAir() || !st.getFluidState().isEmpty()) return null;
        if (st.getCollisionShape(level, pos).isEmpty()) return null;
        return st;
    }

    @Override
    public String blockId(int x, int y, int z) {
        ClientLevel level = mc.level;
        if (level == null) return "air";
        return BuiltInRegistries.BLOCK.getKey(level.getBlockState(new BlockPos(x, y, z)).getBlock()).getPath();
    }

    @Override
    public boolean isFullSolid(int x, int y, int z) {
        ClientLevel level = mc.level;
        if (level == null) return false;
        BlockPos pos = new BlockPos(x, y, z);
        return level.getBlockState(pos).isSolidRender();
    }

    @Override
    public int lightAt(int x, int y, int z) {
        ClientLevel level = mc.level;
        if (level == null) return Display.FULL_BRIGHT;
        return LevelRenderer.getLightColor(level, new BlockPos(x, y, z));
    }
}
