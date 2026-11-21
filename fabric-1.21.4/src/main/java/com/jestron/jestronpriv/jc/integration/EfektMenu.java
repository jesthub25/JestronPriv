package com.jestron.jestronpriv.jc.integration;

import com.jestron.jestronpriv.client.gui.MainMenuScreen;
import com.jestron.jestronpriv.jc.JestronClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Modul menusu ile Efekt Menusu artik ayni mod: ana menu dogrudan acilir. */
public final class EfektMenu {
    private EfektMenu() {
    }

    public static boolean isLoaded() {
        return true;
    }

    public static void open(Screen from) {
        try {
            Minecraft.getInstance().setScreen(new MainMenuScreen());
        } catch (Throwable t) {
            JestronClient.LOG.warn("Efekt Menüsü açılamadı", t);
        }
    }
}
