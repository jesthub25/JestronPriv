package com.jestron.jestronpriv.jc.modules;

import com.jestron.jestronpriv.jc.core.Category;
import com.jestron.jestronpriv.jc.core.Hud;
import com.jestron.jestronpriv.jc.core.HudModule;
import com.jestron.jestronpriv.jc.core.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Locale;

/** Nişan aldığın oyuncunun / mobun canı (kalp ya da çubuk). */
public final class TargetHealthModule extends HudModule {
    private final Setting.Choice style = add(new Setting.Choice("style", "Gösterim", 0, "Çubuk", "Kalpler"));
    private final Setting.Bool mobs = add(new Setting.Bool("mobs", "Mobları da göster", true));
    private final Setting.Bool linger = add(new Setting.Bool("linger", "Bakmayı bırakınca 1.5 sn kalsın", true));
    private final Setting.Bool numbers = add(new Setting.Bool("numbers", "Sayısal değer", true));

    private static final int W = 124;
    private LivingEntity target;
    private long lastSeen;

    public TargetHealthModule() {
        super("targethealth", "Hedef can", "Baktığın oyuncunun ya da mobun canını gösterir.", Category.PVP, true, 0.44, 0.58);
    }

    @Override
    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            target = null;
            return;
        }
        long now = System.currentTimeMillis();
        Entity e = mc.crosshairPickEntity;
        if (e instanceof LivingEntity le && le.isAlive() && le != mc.player && (le instanceof Player || mobs.on())) {
            target = le;
            lastSeen = now;
            return;
        }
        if (target != null) {
            boolean keep = linger.on() && now - lastSeen < 1500 && target.isAlive() && !target.isRemoved()
                    && target.distanceToSqr(mc.player) < 64;
            if (!keep) target = null;
        }
    }

    @Override
    protected void onDisable() {
        target = null;
    }

    private boolean visible(boolean preview) {
        return target != null || preview;
    }

    @Override
    public int width(boolean preview) {
        return visible(preview) ? W : 0;
    }

    @Override
    public int height(boolean preview) {
        if (!visible(preview)) return 0;
        return Hud.PAD * 2 + 10 + 9 + (numbers.on() ? 10 : 0);
    }

    @Override
    public void render(GuiGraphics g, boolean preview) {
        if (!visible(preview)) return;
        String name;
        float hp, max, abs;
        if (target != null) {
            name = target.getDisplayName().getString();
            hp = target.getHealth();
            max = Math.max(1f, target.getMaxHealth());
            abs = target.getAbsorptionAmount();
        } else {
            name = "Oyuncu";
            hp = 15f;
            max = 20f;
            abs = 4f;
        }
        if (Float.isNaN(hp)) hp = 0;
        hp = Math.max(0, Math.min(hp, max));
        Hud.box(g, W, height(preview));
        String shown = Hud.font().plainSubstrByWidth(name, W - Hud.PAD * 2);
        Hud.text(g, shown, Hud.PAD, Hud.PAD, 0xFFFFFFFF);
        int y = Hud.PAD + 11;
        int bw = W - Hud.PAD * 2;
        if (style.index() == 1 && max <= 40) {
            hearts(g, Hud.PAD, y, bw, hp, max, abs);
        } else {
            float total = max + Math.max(0, abs);
            int hpW = Math.round(bw * hp / total);
            int absW = Math.round(bw * Math.max(0, abs) / total);
            g.fill(Hud.PAD, y + 1, Hud.PAD + bw, y + 6, 0x66FFFFFF);
            g.fill(Hud.PAD, y + 1, Hud.PAD + hpW, y + 6, ArmorModule.durabilityColor(hp / max) | 0xFF000000);
            if (absW > 0) g.fill(Hud.PAD + hpW, y + 1, Math.min(Hud.PAD + bw, Hud.PAD + hpW + absW), y + 6, 0xFFFFC107);
        }
        if (numbers.on()) {
            String t = String.format(Locale.ROOT, "%.1f / %.0f ❤", hp, max) + (abs > 0 ? String.format(Locale.ROOT, "  +%.0f", abs) : "");
            Hud.text(g, t, Hud.PAD, y + 10, 0xFFE0E0E0);
        }
    }

    private static void hearts(GuiGraphics g, int x, int y, int bw, float hp, float max, float abs) {
        int total = Math.min(20, Math.max(1, (int) Math.ceil(max / 2f)));
        int full = (int) Math.floor(hp / 2f);
        boolean half = hp - full * 2f >= 1f;
        int step = total <= 10 ? 9 : Math.max(5, (bw - 18) / total);
        for (int i = 0; i < total; i++) {
            int c = i < full ? 0xFFFF5555 : (i == full && half ? 0xFFFF9999 : 0xFF555555);
            Hud.text(g, "❤", x + i * step, y - 1, c);
        }
        if (abs > 0) Hud.text(g, "+" + (int) Math.ceil(abs / 2f), x + total * step + 2, y - 1, 0xFFFFC107);
    }
}
