package com.jestron.jestronpriv.jc;

import com.mojang.blaze3d.platform.InputConstants;
import com.jestron.jestronpriv.jc.core.Config;
import com.jestron.jestronpriv.jc.core.Module;
import com.jestron.jestronpriv.jc.core.Modules;
import com.jestron.jestronpriv.jc.gui.JestronMenuScreen;
import com.jestron.jestronpriv.jc.modules.SessionModule;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Modul menusu (Sag Shift): Jestron Launcher'in istemci modulleri, Efekt Menusu'nun icinde. Efekt Menusu'nun
 * giris noktasindan {@link #init()} ile baslar. Ayri "Jestron Client" modu da kuruluysa (launcher ile gelen) bu kisim
 * hic calismaz, mixin'leri de uygulanmaz: menu, HUD ve tuslar iki kez olmaz.
 */
public final class JestronClient {
    public static final Logger LOG = LoggerFactory.getLogger("Jestron");
    public static KeyMapping menuKey;
    public static final KeyMapping[] MACRO_KEYS = new KeyMapping[5];
    private static final Set<String> FAILED = new HashSet<>();
    /** Ayri Jestron Client modunun kimligi. */
    public static final String STANDALONE_ID = "jestron";
    private static boolean active;

    private JestronClient() {
    }

    /** Modul menusu bu modda calisiyor mu (ayri Jestron Client kurulu degilse evet). */
    public static boolean active() {
        return active;
    }

    public static boolean standalonePresent() {
        return FabricLoader.getInstance().isModLoaded(STANDALONE_ID);
    }

    public static void init() {
        if (active) return;
        if (standalonePresent()) {
            LOG.info("Ayri Jestron Client kurulu: modul menusu onun uzerinden calisir, Efekt Menusu'ndeki kopya kapali");
            return;
        }
        active = true;
        Config.load();
        if (!Config.loadedExisting) {
            // Efekt Menüsü'nün bilgi satırı FPS, ping ve koordinatı zaten gösteriyor: üst üste binmesin
            Modules.FPS.setEnabled(false);
            Modules.PING.setEnabled(false);
            Modules.COORDS.setEnabled(false);
            LOG.info("Efekt Menüsü bulundu: aynı bilgiyi gösteren FPS / Ping / Koordinat HUD'ları ilk açılışta kapalı");
        }

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.jestron.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.jestron"));
        for (int i = 0; i < MACRO_KEYS.length; i++) {
            MACRO_KEYS[i] = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                    "key.jestron.macro" + (i + 1), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.jestron"));
        }

        // seçenekler (options) bu noktada hazır olduğu için açık modüller burada başlatılır
        ClientLifecycleEvents.CLIENT_STARTED.register(mc -> {
            for (Module m : Modules.ALL) m.applyLoadedState();
            com.jestron.jestronpriv.jc.util.WindowBranding.applyIcon(mc);
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> {
            try {
                Modules.FULLBRIGHT.restoreForShutdown();
            } catch (Throwable ignored) {
            }
            Config.save();
        });
        ClientTickEvents.END_CLIENT_TICK.register(JestronClient::tick);

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            SessionModule.joinedAt = System.currentTimeMillis();
            Modules.JOIN_MESSAGE.onJoin();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            SessionModule.joinedAt = 0;
            Modules.JOIN_MESSAGE.onLeave();
        });
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) Modules.AUTO_GG.onGameMessage(message);
        });
        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, params, ts) ->
                Modules.AUTO_GG.onPlayerMessage(message.getString()));

        LOG.info("Modul menusu yuklendi ({} modul)", Modules.ALL.size());
    }

    private static void tick(Minecraft mc) {
        while (menuKey.consumeClick()) {
            if (mc.screen == null) mc.setScreen(new JestronMenuScreen(null));
        }
        for (int i = 0; i < MACRO_KEYS.length; i++) {
            while (MACRO_KEYS[i].consumeClick()) {
                if (mc.screen == null) Modules.MACROS.fire(i);
            }
        }
        for (Module m : Modules.ALL) {
            if (!m.isEnabled()) continue;
            try {
                m.tick();
            } catch (Throwable t) {
                if (FAILED.add(m.id)) LOG.warn("Modül {} tick hatası (bir kez bildirilir)", m.id, t);
            }
        }
    }
}
