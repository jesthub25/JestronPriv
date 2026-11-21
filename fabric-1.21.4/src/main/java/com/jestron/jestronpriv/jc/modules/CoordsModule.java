package com.jestron.jestronpriv.jc.modules;

import com.jestron.jestronpriv.jc.core.Category;
import com.jestron.jestronpriv.jc.core.Setting;
import com.jestron.jestronpriv.jc.core.TextHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CoordsModule extends TextHud {
    private final Setting.Num decimals = add(new Setting.Num("decimals", "Ondalık basamak", 1, 0, 2, 1, ""));
    private final Setting.Bool direction = add(new Setting.Bool("direction", "Yönü göster", true));
    private final Setting.Bool biome = add(new Setting.Bool("biome", "Biyomu göster", false));
    private static final String[] DIRS = {"Güney (+Z)", "Güneybatı", "Batı (-X)", "Kuzeybatı", "Kuzey (-Z)", "Kuzeydoğu", "Doğu (+X)", "Güneydoğu"};

    public CoordsModule() {
        super("coords", "Koordinatlar", "X Y Z, bakış yönü ve biyom.", Category.HUD, true, 0.005, 0.145);
    }

    @Override
    protected String[] lines(boolean preview) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        List<String> out = new ArrayList<>(3);
        if (p == null) {
            if (!preview) return new String[0];
            out.add(kv("XYZ", "128.5 64 -42.3"));
            if (direction.on()) out.add(kv("Yön", DIRS[4]));
            if (biome.on()) out.add(kv("Biyom", "Ova"));
            return out.toArray(new String[0]);
        }
        String f = "%." + decimals.i() + "f";
        out.add(kv("XYZ", String.format(Locale.ROOT, f + " " + f + " " + f, p.getX(), p.getY(), p.getZ())));
        if (direction.on()) {
            float yaw = Mth.wrapDegrees(p.getYRot());
            int idx = Math.floorMod(Math.round((yaw < 0 ? yaw + 360 : yaw) / 45f), 8);
            out.add(kv("Yön", DIRS[idx]));
        }
        if (biome.on() && mc.level != null) {
            String name = mc.level.getBiome(p.blockPosition()).unwrapKey()
                    .map(k -> Component.translatable(k.location().toLanguageKey("biome")).getString())
                    .orElse("?");
            out.add(kv("Biyom", name));
        }
        return out.toArray(new String[0]);
    }
}
