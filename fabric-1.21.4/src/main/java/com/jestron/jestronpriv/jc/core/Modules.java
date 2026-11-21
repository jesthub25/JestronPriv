package com.jestron.jestronpriv.jc.core;

import com.jestron.jestronpriv.jc.modules.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Tüm modüller (menüdeki sırayla). */
public final class Modules {
    private Modules() {
    }

    public static final AppearanceModule APPEARANCE = new AppearanceModule();
    public static final FpsModule FPS = new FpsModule();
    public static final CpsModule CPS = new CpsModule();
    public static final PingModule PING = new PingModule();
    public static final CoordsModule COORDS = new CoordsModule();
    public static final KeystrokesModule KEYSTROKES = new KeystrokesModule();
    public static final ArmorModule ARMOR = new ArmorModule();
    public static final ClockModule CLOCK = new ClockModule();
    public static final SpeedModule SPEED = new SpeedModule();
    public static final MemoryModule MEMORY = new MemoryModule();
    public static final ServerModule SERVER = new ServerModule();
    public static final SessionModule SESSION = new SessionModule();
    public static final TargetHealthModule TARGET_HEALTH = new TargetHealthModule();
    public static final NametagHealthModule NAMETAG_HEALTH = new NametagHealthModule();
    public static final DurabilityModule DURABILITY = new DurabilityModule();
    public static final ToggleSprintModule TOGGLE_SPRINT = new ToggleSprintModule();
    public static final FullbrightModule FULLBRIGHT = new FullbrightModule();
    public static final ChatTimeModule CHAT_TIME = new ChatTimeModule();
    public static final AutoGgModule AUTO_GG = new AutoGgModule();
    public static final JoinMessageModule JOIN_MESSAGE = new JoinMessageModule();
    public static final MacrosModule MACROS = new MacrosModule();

    public static final List<Module> ALL;
    public static final List<HudModule> HUD;

    static {
        List<Module> all = new ArrayList<>(List.of(
                FPS, CPS, PING, COORDS, KEYSTROKES, ARMOR, CLOCK, SPEED, MEMORY, SERVER, SESSION,
                TARGET_HEALTH, NAMETAG_HEALTH, DURABILITY,
                TOGGLE_SPRINT,
                FULLBRIGHT, APPEARANCE,
                CHAT_TIME, AUTO_GG, JOIN_MESSAGE, MACROS));
        List<HudModule> hud = new ArrayList<>();
        for (Module m : all) if (m instanceof HudModule h) hud.add(h);
        ALL = Collections.unmodifiableList(all);
        HUD = Collections.unmodifiableList(hud);
    }

    public static Module byId(String id) {
        for (Module m : ALL) if (m.id.equals(id)) return m;
        return null;
    }
}
