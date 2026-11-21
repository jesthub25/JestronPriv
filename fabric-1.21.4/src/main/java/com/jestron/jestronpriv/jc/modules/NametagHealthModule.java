package com.jestron.jestronpriv.jc.modules;

import com.jestron.jestronpriv.jc.core.Category;
import com.jestron.jestronpriv.jc.core.Module;
import com.jestron.jestronpriv.jc.core.Setting;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Locale;

/** İsim etiketinin yanında can (sunucunun gönderdiği can verisiyle). */
public final class NametagHealthModule extends Module {
    private final Setting.Choice format = add(new Setting.Choice("format", "Biçim", 0, "❤ 20", "20/20", "%100"));
    private final Setting.Bool mobs = add(new Setting.Bool("mobs", "İsimli moblarda da", false));

    public NametagHealthModule() {
        super("nametaghealth", "İsim üstü can", "Oyuncuların isim etiketinin yanında canlarını gösterir.", Category.PVP, true);
    }

    public Component decorate(LivingEntity e, Component nameTag) {
        if (!isEnabled() || nameTag == null || e == null) return nameTag;
        if (e == Minecraft.getInstance().player) return nameTag;
        if (!(e instanceof Player) && !mobs.on()) return nameTag;
        float hp = e.getHealth() + Math.max(0, e.getAbsorptionAmount());
        float max = Math.max(1f, e.getMaxHealth());
        if (Float.isNaN(hp)) return nameTag;
        double r = Math.min(1.0, e.getHealth() / max);
        ChatFormatting color = r > 0.6 ? ChatFormatting.GREEN : r > 0.3 ? ChatFormatting.YELLOW : ChatFormatting.RED;
        String txt = switch (format.index()) {
            case 1 -> fmt(hp) + "/" + fmt(max);
            case 2 -> "%" + Math.round(r * 100);
            default -> "❤ " + fmt(hp);
        };
        return nameTag.copy().append(Component.literal(" " + txt).withStyle(color));
    }

    private static String fmt(float v) {
        return Math.abs(v - Math.round(v)) < 0.05 ? String.valueOf(Math.round(v)) : String.format(Locale.ROOT, "%.1f", v);
    }
}
