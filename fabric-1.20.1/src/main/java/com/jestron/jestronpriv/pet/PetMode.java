package com.jestron.jestronpriv.pet;

import java.util.Locale;

/** Yoldasin oyuncuya gore yeri. */
public enum PetMode {
    OMUZ("Omuzda"), YAN("Yanında"), ARKA("Arkada, yukarıda"), YORUNGE("Etrafında döner");

    public final String label;

    PetMode(String label) {
        this.label = label;
    }

    public PetMode next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static PetMode parse(String s) {
        if (s != null) {
            String n = s.trim().toUpperCase(Locale.ROOT);
            for (PetMode m : values()) if (m.name().equals(n)) return m;
        }
        return YAN;
    }
}
