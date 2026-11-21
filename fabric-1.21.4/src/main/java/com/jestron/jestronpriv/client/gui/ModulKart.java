package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.jc.JestronClient;
import com.jestron.jestronpriv.jc.core.Module;
import com.jestron.jestronpriv.jc.core.Modules;
import com.jestron.jestronpriv.jc.gui.JestronMenuScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Ana menudeki "Moduller" karti: modul menusunu (Sag Shift) acar. */
final class ModulKart {
    private ModulKart() {
    }

    static String durum() {
        if (!JestronClient.active()) return JestronClient.standalonePresent() ? "§7Jestron Client üzerinden" : "§cKapalı";
        int on = 0;
        for (Module m : Modules.ALL) if (m.isEnabled()) on++;
        return "§a" + on + " §7/ " + Modules.ALL.size() + " modül açık";
    }

    static void ac(Screen from) {
        Minecraft mc = Minecraft.getInstance();
        if (JestronClient.active()) {
            mc.setScreen(new JestronMenuScreen(from));
            return;
        }
        // Ayri Jestron Client kuruluysa onun menusu acilir (derleme bagimliligi yok)
        try {
            Object s = Class.forName("dev.jestron.client.gui.JestronMenuScreen").getConstructor(Screen.class).newInstance(from);
            if (s instanceof Screen screen) mc.setScreen(screen);
        } catch (Throwable t) {
            JestronClient.LOG.warn("Modül menüsü açılamadı", t);
        }
    }
}
