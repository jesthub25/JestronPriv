package com.jestron.jestronpriv.jc.modules;

import com.jestron.jestronpriv.jc.core.Category;
import com.jestron.jestronpriv.jc.core.TextHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.util.Locale;

public final class SpeedModule extends TextHud {
    private double lastX, lastZ, speed;
    private boolean has;

    public SpeedModule() {
        super("speed", "Hız", "Yatay hareket hızı (blok/sn).", Category.HUD, false, 0.72, 0.19);
    }

    @Override
    public void tick() {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null) {
            has = false;
            speed = 0;
            return;
        }
        if (has) {
            double dx = p.getX() - lastX, dz = p.getZ() - lastZ;
            double inst = Math.sqrt(dx * dx + dz * dz) * 20.0;
            // ışınlanma sıçramalarını yumuşat
            if (inst > 200) inst = 0;
            speed = speed * 0.6 + inst * 0.4;
        }
        lastX = p.getX();
        lastZ = p.getZ();
        has = true;
    }

    @Override
    protected void onDisable() {
        has = false;
        speed = 0;
    }

    @Override
    protected String[] lines(boolean preview) {
        if (Minecraft.getInstance().player == null && !preview) return new String[0];
        double v = Minecraft.getInstance().player == null ? 4.32 : speed;
        return new String[]{kv("Hız", String.format(Locale.ROOT, "%.2f b/sn", v))};
    }
}
