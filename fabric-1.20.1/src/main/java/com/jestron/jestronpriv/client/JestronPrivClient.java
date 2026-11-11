package com.jestron.jestronpriv.client;

import com.jestron.jestronpriv.fx.FxEngine;
import com.jestron.jestronpriv.fx.FxSession;
import com.jestron.jestronpriv.fx.KillEffect;
import com.jestron.jestronpriv.fx.api.Location;
import com.jestron.jestronpriv.fx.api.Material;
import com.jestron.jestronpriv.client.gui.MainMenuScreen;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class JestronPrivClient implements ClientModInitializer {

    public static final Logger LOG = LoggerFactory.getLogger("JestronPriv");
    private static JestronPrivClient instance;

    private ModConfig config;
    private ClientBackend backend;
    private FxEngine engine;
    private FxRenderer renderer;
    private KillDetector detector;
    private KeyMapping menuKey;
    private KeyMapping freeCamKey;
    private KeyMapping freeLookKey;
    private KeyMapping zoomKey;
    private boolean openMenuNextTick;
    private ClientLevel lastLevel;

    public static JestronPrivClient get() {
        return instance;
    }

    @Override
    public void onInitializeClient() {
        instance = this;
        config = ModConfig.load();
        ModConfig.enableAsyncSave();
        backend = new ClientBackend(this::config);
        engine = new FxEngine(backend);
        engine.setErrorSink((id, t) -> LOG.warn("Efekt '{}' calisirken beklenmeyen durum (efekt guvenle devam etti)", id, t));
        renderer = new FxRenderer(backend, this::config);
        detector = new KillDetector(this::onKill);
        applyConfig();

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.jestronpriv.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, "category.jestronpriv"));
        // varsayilan atanmamis: Kontroller'den istege gore atanir
        freeCamKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.jestronpriv.freecam", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.jestronpriv"));
        freeLookKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.jestronpriv.freelook", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "category.jestronpriv"));
        com.jestron.jestronpriv.client.camera.FreeLook.setKey(freeLookKey);
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.jestronpriv.zoom", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, "category.jestronpriv"));
        com.jestron.jestronpriv.client.view.Zoom.setKey(zoomKey);

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        WorldRenderEvents.AFTER_ENTITIES.register(ctx -> guard(renderer::render, ctx));
        WorldRenderEvents.AFTER_ENTITIES.register(ctx -> guard(com.jestron.jestronpriv.client.cosmetic.HitFx::render, ctx));
        WorldRenderEvents.AFTER_ENTITIES.register(ctx -> guard(com.jestron.jestronpriv.client.pvp.WorldText::render, ctx));
        WorldRenderEvents.START.register(ctx -> {
            try {
                com.jestron.jestronpriv.client.pvp.HitColor.frame();
            } catch (Throwable ignored) {
            }
            try {
                com.jestron.jestronpriv.client.view.Culler.beginFrame();
            } catch (Throwable ignored) {
            }
        });
        WorldRenderEvents.AFTER_ENTITIES.register(ctx -> guard(com.jestron.jestronpriv.client.cosmetic.Pets::render, ctx));
        WorldRenderEvents.AFTER_ENTITIES.register(ctx -> guard(com.jestron.jestronpriv.client.tag.Nametags::renderSelf, ctx));
        // buyu parlamasi: kaynak paketleri degisince vanilla parlama dokusu yeniden okunur
        try {
            net.fabricmc.fabric.api.resource.ResourceManagerHelper.get(net.minecraft.server.packs.PackType.CLIENT_RESOURCES)
                    .registerReloadListener(new net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener() {
                        @Override
                        public net.minecraft.resources.ResourceLocation getFabricId() {
                            return net.minecraft.resources.new ResourceLocation("jestronpriv", "glint_reload");
                        }

                        @Override
                        public void onResourceManagerReload(net.minecraft.server.packs.resources.ResourceManager manager) {
                            com.jestron.jestronpriv.client.look.Glint.onResourceReload();
                            com.jestron.jestronpriv.client.itemskin.ItemSkins.onResourceReload();
                        }
                    });
        } catch (Throwable t) {
            LOG.warn("JestronPriv kaynak dinleyicisi eklenemedi", t);
        }
        // sapka ve pelerin: oyuncu modeline katman
        net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, entityRenderer, helper, context) -> {
            try {
                if (entityRenderer instanceof net.minecraft.client.renderer.entity.player.PlayerRenderer pr)
                    helper.register(new com.jestron.jestronpriv.client.cosmetic.Hats.Layer(pr));
            } catch (Throwable t) {
                LOG.warn("JestronPriv sapka katmani eklenemedi", t);
            }
            try {
                if (entityRenderer instanceof net.minecraft.client.renderer.entity.player.PlayerRenderer pr)
                    helper.register(new com.jestron.jestronpriv.client.cosmetic.Capes.Layer(pr));
            } catch (Throwable t) {
                LOG.warn("JestronPriv pelerin katmani eklenemedi", t);
            }
        });
        WorldRenderEvents.AFTER_ENTITIES.register(ctx -> guard(com.jestron.jestronpriv.client.cosmetic.GroundItems::render, ctx));
        WorldRenderEvents.AFTER_ENTITIES.register(ctx -> guard(com.jestron.jestronpriv.client.pvp.Hitboxes::render, ctx));
        WorldRenderEvents.BLOCK_OUTLINE.register(com.jestron.jestronpriv.client.view.BlockOutline::onOutline);
        // Sistem mesajlari ve aksiyon cubugu (overlay): eklentilerin ozel oldurme mesajlari da burada gelir
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            try {
                detector.onMessage(message);
            } catch (Throwable t) {
                LOG.debug("Mesaj okunamadi", t);
            }
            if (!overlay) feed(message);
        });
        // olum mesajlarini sohbetten gizle (oldurme akisi acikken, istege bagli). Gizlenen mesaj yine islenir.
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            try {
                return overlay || !com.jestron.jestronpriv.client.pvp.KillFeed.hideFromChat(message);
            } catch (Throwable t) {
                return true;
            }
        });
        ClientReceiveMessageEvents.GAME_CANCELED.register((message, overlay) -> {
            if (overlay || !com.jestron.jestronpriv.client.pvp.KillFeed.isDeathMessage(message)) return;
            try {
                detector.onMessage(message);
            } catch (Throwable t) {
                LOG.debug("Mesaj okunamadi", t);
            }
            feed(message);
        });
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClientSide() && player == Minecraft.getInstance().player) {
                detector.onAttack(entity);
                try {
                    com.jestron.jestronpriv.client.pvp.Reach.onAttack(entity);
                    com.jestron.jestronpriv.client.pvp.PingTracker.setTarget(entity);
                } catch (Throwable ignored) {
                }
                com.jestron.jestronpriv.client.cosmetic.HitFx.onAttack(entity);
            }
            return InteractionResult.PASS;
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> registerCommands(dispatcher));
        LOG.info("JestronPriv yuklendi: {} efekt, vurus animasyonu aktif. Menu tusu: K", engine.registry().all().size());
    }

    public void applyConfig() {
        config.sanitize();
        engine.setVolume(config.volume);
        engine.setMaxSessions(config.maxSessions);
        detector.setMobKills(config.mobKills);
        detector.setDebug(config.killDebug);
        detector.setStrict(config.killStrict);
        com.jestron.jestronpriv.fx.CustomText.set(config.ctText, config.textStyle(), config.ctSize, config.ctHeight,
                config.ctAnim(), config.ctSound(), config.ctParticles);
        com.jestron.jestronpriv.client.cosmetic.LocalLook.refresh();
    }

    public ModConfig config() {
        return config;
    }

    public FxEngine engine() {
        return engine;
    }

    public KillDetector detector() {
        return detector;
    }

    public KeyMapping menuKey() {
        return menuKey;
    }

    /** Ses testi: test sesi calar ve ses seviyelerini sohbete yazar. */
    public void soundTest() {
        String r = backend.soundTest();
        if (r != null) msg(r);
    }

    /** Oynayan tum efektleri hemen temizler. */
    public void stopAllEffects() {
        engine.stopAll();
        backend.clear();
        renderer.clearCache();
    }

    // ------------------------------------------------------------------ tick

    private void onTick(Minecraft mc) {
        if (mc.level != lastLevel) {
            safe(engine::stopAll);
            safe(backend::clear);
            safe(renderer::clearCache);
            safe(detector::reset);
            safe(com.jestron.jestronpriv.client.pvp.PvpHub::onLevelChange);
            safe(com.jestron.jestronpriv.client.cosmetic.Pets::onLevelChange);
            safe(com.jestron.jestronpriv.client.hud.PickupHud::clear);
            safe(com.jestron.jestronpriv.client.audio.CustomSound::stop);
            safe(com.jestron.jestronpriv.client.view.Culler::onLevelChange);
            safe(com.jestron.jestronpriv.client.cosmetic.Hats::onLevelChange);
            safe(com.jestron.jestronpriv.client.cosmetic.Capes::onLevelChange);
            safe(com.jestron.jestronpriv.client.cosmetic.Elytras::onLevelChange);
            lastLevel = mc.level;
        }
        // her modul ayri yalitilir: birinde hata olsa digerleri ve oyun etkilenmez
        safe(com.jestron.jestronpriv.client.camera.FreeLook::update);
        safe(() -> com.jestron.jestronpriv.client.cosmetic.HitFx.tick(mc));
        safe(() -> com.jestron.jestronpriv.client.pvp.PvpHub.tick(mc));
        safe(() -> com.jestron.jestronpriv.client.hud.Hud.tick(mc));
        safe(() -> com.jestron.jestronpriv.client.cosmetic.Pets.tick(mc));
        safe(() -> com.jestron.jestronpriv.client.cosmetic.GroundItems.tick(mc));
        safe(() -> com.jestron.jestronpriv.client.audio.CustomSound.tick(mc));
        if (mc.level == null || mc.player == null) return;
        while (menuKey.consumeClick()) {
            if (mc.screen == null) mc.setScreen(new MainMenuScreen());
        }
        while (freeCamKey.consumeClick()) {
            config.freeCam = !config.freeCam;
            config.save();
            mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "§5JestronPriv §8» §fSerbest kamera: " + (config.freeCam ? "§aAçık" : "§cKapalı")), true);
        }
        if (openMenuNextTick) {
            openMenuNextTick = false;
            mc.setScreen(new MainMenuScreen());
        }
        safe(() -> com.jestron.jestronpriv.client.look.Glint.tick(mc));
        if (mc.isPaused()) return;
        safe(() -> com.jestron.jestronpriv.client.cosmetic.Auras.tick(mc));
        safe(() -> com.jestron.jestronpriv.client.cosmetic.Wings.tick(mc));
        safe(() -> com.jestron.jestronpriv.client.cosmetic.Hats.tick(mc));
        safe(() -> com.jestron.jestronpriv.client.cosmetic.Capes.tick(mc));
        safe(() -> com.jestron.jestronpriv.client.cosmetic.Elytras.tick(mc));
        safe(backend::tickClock);
        safe(engine::step);
        safe(detector::tick);
    }

    private static int tickErrors, renderErrors;

    /** Dunya cizimi geri cagrisi: hata olursa o kare atlanir, oyun cokmez. */
    private static void feed(net.minecraft.network.chat.Component message) {
        try {
            com.jestron.jestronpriv.client.pvp.KillFeed.onMessage(message);
        } catch (Throwable t) {
            LOG.debug("Oldurme akisi mesaji okunamadi", t);
        }
    }

    private static void guard(java.util.function.Consumer<net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext> c,
                              net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext ctx) {
        try {
            c.accept(ctx);
        } catch (Throwable t) {
            if (renderErrors++ < 5) LOG.warn("JestronPriv cizim hatasi (yok sayildi)", t);
        }
    }

    private static void safe(Runnable r) {
        try {
            r.run();
        } catch (Throwable t) {
            if (tickErrors++ < 5) LOG.warn("JestronPriv tick hatasi (yok sayildi)", t);
        }
    }

    // ------------------------------------------------------------------ oldurme

    private void onKill(Entity victim, String victimName, Vec3 pos) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer me = mc.player;
        if (me == null || mc.level == null) return;
        com.jestron.jestronpriv.client.pvp.PvpHub.onKill(victimName);
        com.jestron.jestronpriv.fx.CustomText.setSelfName(me.getGameProfile().getName());
        KillEffect fx = resolve(config.effect);
        if (fx == null) return;
        com.jestron.jestronpriv.fx.api.ItemStack head = headOf(victim);
        ModConfig.ViewMode mode = config.viewMode();
        String name = victimName == null ? "?" : victimName;

        if (mode == ModConfig.ViewMode.KONUM || mode == ModConfig.ViewMode.IKISI) {
            Location origin = groundAt(pos.x, pos.y, pos.z);
            double fx0 = origin.getX() - me.getX(), fz0 = origin.getZ() - me.getZ();
            if (fx0 * fx0 + fz0 * fz0 < 0.01) {
                Vec3 look = me.getLookAngle();
                fx0 = look.x;
                fz0 = look.z;
            }
            engine.start(fx, name, head, origin, fx0, fz0, false);
        }
        if (mode == ModConfig.ViewMode.EKRAN || mode == ModConfig.ViewMode.IKISI) {
            playInFront(fx, name, head);
            if (config.screenTitle) {
                mc.gui.setTimes(2, 36, 10);
                mc.gui.setSubtitle(Component.literal("§7" + fx.plainName() + " §8• §aSen öldürdün!"));
                mc.gui.setTitle(Component.literal("§c§l☠ §f" + name));
            }
        }
    }

    /** Efekti oyuncunun onunde oynatir (ekran modu ve onizleme). */
    public FxSession playInFront(KillEffect fx, String victimName, com.jestron.jestronpriv.fx.api.ItemStack head) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer me = mc.player;
        if (me == null || mc.level == null || fx == null) return null;
        com.jestron.jestronpriv.fx.CustomText.setSelfName(me.getGameProfile().getName());
        Vec3 look = me.getLookAngle();
        double lx = look.x, lz = look.z;
        double len = Math.sqrt(lx * lx + lz * lz);
        if (len < 1e-4) {
            double yaw = Math.toRadians(me.getYRot());
            lx = -Math.sin(yaw);
            lz = Math.cos(yaw);
            len = 1;
        }
        lx /= len;
        lz /= len;
        double dist = config.screenDistance;
        Vec3 dir = new Vec3(lx, 0, lz);
        for (Vec3 start : new Vec3[]{me.position().add(0, 0.6, 0), me.getEyePosition()}) {
            Vec3 end = start.add(dir.scale(dist));
            BlockHitResult hit = mc.level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, me));
            if (hit.getType() != HitResult.Type.MISS) {
                dist = Math.max(1.6, Math.min(dist, hit.getLocation().distanceTo(start) - 0.9));
            }
        }
        double bx = me.getX() + lx * dist, bz = me.getZ() + lz * dist;
        Location origin = groundAt(bx, me.getY() + 0.5, bz);
        return engine.start(fx, victimName, head, origin, lx, lz, true);
    }

    /** Onizleme: efekti oyuncunun onunde oynatir. */
    public void preview(KillEffect fx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        playInFront(fx, mc.player.getGameProfile().getName(), headOf(mc.player));
    }

    private KillEffect resolve(String id) {
        if (id == null || id.equals(ModConfig.NONE)) return null;
        if (id.equals(ModConfig.RANDOM)) {
            List<KillEffect> all = new ArrayList<>(engine.registry().all());
            return all.get(ThreadLocalRandom.current().nextInt(all.size()));
        }
        return engine.registry().get(id);
    }

    private static com.jestron.jestronpriv.fx.api.ItemStack headOf(Entity victim) {
        if (victim instanceof Player p) {
            return new com.jestron.jestronpriv.fx.api.ItemStack(Material.PLAYER_HEAD, p.getGameProfile());
        }
        if (victim != null) {
            EntityType<?> t = victim.getType();
            if (t == EntityType.ZOMBIE || t == EntityType.HUSK || t == EntityType.DROWNED || t == EntityType.ZOMBIE_VILLAGER)
                return new com.jestron.jestronpriv.fx.api.ItemStack(Material.ZOMBIE_HEAD);
            if (t == EntityType.CREEPER) return new com.jestron.jestronpriv.fx.api.ItemStack(Material.CREEPER_HEAD);
            if (t == EntityType.WITHER_SKELETON) return new com.jestron.jestronpriv.fx.api.ItemStack(Material.WITHER_SKELETON_SKULL);
            if (t == EntityType.PIGLIN || t == EntityType.PIGLIN_BRUTE) return new com.jestron.jestronpriv.fx.api.ItemStack(Material.PIGLIN_HEAD);
            if (t == EntityType.ENDER_DRAGON) return new com.jestron.jestronpriv.fx.api.ItemStack(Material.DRAGON_HEAD);
        }
        return new com.jestron.jestronpriv.fx.api.ItemStack(Material.SKELETON_SKULL);
    }

    /** Verilen noktanin altindaki gercek zemin yuzeyi. */
    private Location groundAt(double x, double y, double z) {
        int bx = (int) Math.floor(x), bz = (int) Math.floor(z);
        int startY = (int) Math.floor(y + 0.01);
        if (backend.isLoaded(bx, bz)) {
            for (int dy = 0; dy <= 10; dy++) {
                int yy = startY - dy;
                if (yy < backend.minY()) break;
                if (backend.isPassable(bx, yy, bz)) continue;
                double top = backend.topY(bx, yy, bz);
                if (top > y + 0.6) continue;
                return new Location(x, top, z);
            }
        }
        return new Location(x, y, z);
    }

    // ------------------------------------------------------------------ komutlar

    private void msg(String s) {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p != null) p.displayClientMessage(Component.literal("§8[§5JestronPriv§8] §r" + s), false);
    }

    private void registerCommands(CommandDispatcher<FabricClientCommandSource> d) {
        for (String root : new String[]{"ke", "jestronpriv"}) {
            LiteralArgumentBuilder<FabricClientCommandSource> b = ClientCommandManager.literal(root)
                    .executes(c -> {
                        openMenuNextTick = true;
                        return 1;
                    })
                    .then(ClientCommandManager.literal("sec")
                            .then(ClientCommandManager.argument("efekt", StringArgumentType.word())
                                    .suggests((c, sb) -> SharedSuggestionProvider.suggest(ids(), sb))
                                    .executes(c -> {
                                        String id = StringArgumentType.getString(c, "efekt");
                                        KillEffect e = engine.registry().get(id);
                                        if (e == null) {
                                            msg("§cBöyle bir efekt yok: §f" + id);
                                            return 0;
                                        }
                                        config.effect = e.id();
                                        config.save();
                                        msg("§aEfekt seçildi: " + e.displayName());
                                        return 1;
                                    })))
                    .then(ClientCommandManager.literal("onizle")
                            .then(ClientCommandManager.argument("efekt", StringArgumentType.word())
                                    .suggests((c, sb) -> SharedSuggestionProvider.suggest(ids(), sb))
                                    .executes(c -> {
                                        KillEffect e = engine.registry().get(StringArgumentType.getString(c, "efekt"));
                                        if (e == null) {
                                            msg("§cBöyle bir efekt yok.");
                                            return 0;
                                        }
                                        preview(e);
                                        return 1;
                                    })))
                    .then(ClientCommandManager.literal("kapat").executes(c -> {
                        config.effect = ModConfig.NONE;
                        config.save();
                        msg("§eÖldürme efekti kapatıldı.");
                        return 1;
                    }))
                    .then(ClientCommandManager.literal("rastgele").executes(c -> {
                        config.effect = ModConfig.RANDOM;
                        config.save();
                        msg("§dHer öldürmede rastgele efekt oynayacak.");
                        return 1;
                    }))
                    .then(ClientCommandManager.literal("mod")
                            .then(ClientCommandManager.argument("mod", StringArgumentType.word())
                                    .suggests((c, sb) -> SharedSuggestionProvider.suggest(new String[]{"konum", "ekran", "ikisi"}, sb))
                                    .executes(c -> {
                                        ModConfig.ViewMode m = ModConfig.ViewMode.parse(StringArgumentType.getString(c, "mod"));
                                        config.mode = m.name();
                                        config.save();
                                        msg("§aGösterim modu: " + m.label + " §7(" + m.hint + ")");
                                        return 1;
                                    })))
                    .then(ClientCommandManager.literal("liste").executes(c -> {
                        StringBuilder sb = new StringBuilder("§5Efektler: ");
                        for (KillEffect e : engine.registry().all()) sb.append("§f").append(e.id()).append("§7, ");
                        msg(sb.substring(0, sb.length() - 4));
                        return 1;
                    }));
            d.register(b);
        }
    }

    private List<String> ids() {
        List<String> l = new ArrayList<>();
        for (KillEffect e : engine.registry().all()) l.add(e.id());
        return l;
    }
}
