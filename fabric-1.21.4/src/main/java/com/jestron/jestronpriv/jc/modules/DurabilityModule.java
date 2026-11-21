package com.jestron.jestronpriv.jc.modules;

import com.jestron.jestronpriv.jc.core.Category;
import com.jestron.jestronpriv.jc.core.Hud;
import com.jestron.jestronpriv.jc.core.HudModule;
import com.jestron.jestronpriv.jc.core.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Zırh / eldeki eşya dayanıklılığı eşiğin altına inince uyarır (bir kez ses çalar). */
public final class DurabilityModule extends HudModule {
    private final Setting.Num threshold = add(new Setting.Num("threshold", "Uyarı eşiği", 10, 5, 50, 5, "%"));
    private final Setting.Bool sound = add(new Setting.Bool("sound", "Uyarı sesi", true));
    private final Setting.Bool hands = add(new Setting.Bool("hands", "Eldeki eşyaları da kontrol et", true));

    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND};
    private static final String[] NAMES = {"Kask", "Göğüslük", "Pantolon", "Bot", "Ana el", "İkinci el"};

    private final List<String> warnings = new ArrayList<>();
    private final Set<String> warned = new HashSet<>();

    public DurabilityModule() {
        super("durability", "Dayanıklılık uyarısı", "Zırh ya da silah kırılmak üzereyken uyarır.", Category.PVP, true, 0.40, 0.25);
    }

    @Override
    public void tick() {
        warnings.clear();
        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null) {
            warned.clear();
            return;
        }
        Set<String> now = new HashSet<>();
        for (int i = 0; i < SLOTS.length; i++) {
            if (i >= 4 && !hands.on()) continue;
            ItemStack st = p.getItemBySlot(SLOTS[i]);
            if (st.isEmpty() || !st.isDamageableItem() || st.getMaxDamage() <= 0) continue;
            int max = st.getMaxDamage(), left = max - st.getDamageValue();
            double pct = left * 100.0 / max;
            if (pct <= threshold.d()) {
                warnings.add(NAMES[i] + " %" + Math.round(pct) + " (" + left + ")");
                String key = i + ":" + st.getItem() + ":" + max;
                now.add(key);
                if (!warned.contains(key) && sound.on()) {
                    try {
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.6f));
                    } catch (Throwable ignored) {
                    }
                }
            }
        }
        warned.clear();
        warned.addAll(now);
    }

    @Override
    protected void onDisable() {
        warnings.clear();
        warned.clear();
    }

    private List<String> shown(boolean preview) {
        if (!warnings.isEmpty() || !preview) return warnings;
        return List.of("Göğüslük %8 (19)");
    }

    @Override
    public int width(boolean preview) {
        List<String> l = shown(preview);
        if (l.isEmpty()) return 0;
        int w = 0;
        for (String s : l) w = Math.max(w, Hud.width("⚠ " + s));
        return w + Hud.PAD * 2;
    }

    @Override
    public int height(boolean preview) {
        List<String> l = shown(preview);
        return l.isEmpty() ? 0 : l.size() * 10 - 1 + Hud.PAD * 2;
    }

    @Override
    public void render(GuiGraphics g, boolean preview) {
        List<String> l = shown(preview);
        if (l.isEmpty()) return;
        Hud.box(g, width(preview), height(preview));
        // yumuşak nabız
        float t = (float) (0.65 + 0.35 * Math.sin(System.currentTimeMillis() / 180.0));
        int a = Math.max(0x60, Math.min(0xFF, (int) (t * 255)));
        int color = (a << 24) | 0xFF5555;
        int y = Hud.PAD;
        for (String s : l) {
            Hud.text(g, "⚠ " + s, Hud.PAD, y, color);
            y += 10;
        }
    }
}
