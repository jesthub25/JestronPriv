package com.jestron.jestronpriv.jc.core;

public enum Category {
    HUD("HUD"),
    PVP("PvP"),
    GAMEPLAY("Oynanış"),
    VISUAL("Görsel"),
    CHAT("Sohbet");

    public final String label;

    Category(String label) {
        this.label = label;
    }
}
