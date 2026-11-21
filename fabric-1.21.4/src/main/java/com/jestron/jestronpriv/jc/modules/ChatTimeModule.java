package com.jestron.jestronpriv.jc.modules;

import com.jestron.jestronpriv.jc.core.Category;
import com.jestron.jestronpriv.jc.core.Module;
import com.jestron.jestronpriv.jc.core.Setting;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/** Sohbet mesajlarının başına saat ekler. */
public final class ChatTimeModule extends Module {
    private final Setting.Bool seconds = add(new Setting.Bool("seconds", "Saniyeyi göster", false));
    private final Setting.Choice color = add(new Setting.Choice("color", "Renk", 0, "Gri", "Koyu gri", "Altın"));
    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter HMS = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ChatTimeModule() {
        super("chattime", "Sohbet saati", "Her mesajın başında [12:34] saati.", Category.CHAT, false);
    }

    public Component decorate(Component msg) {
        if (!isEnabled() || msg == null) return msg;
        ChatFormatting c = switch (color.index()) {
            case 1 -> ChatFormatting.DARK_GRAY;
            case 2 -> ChatFormatting.GOLD;
            default -> ChatFormatting.GRAY;
        };
        String t = LocalTime.now().format(seconds.on() ? HMS : HM);
        return Component.literal("[" + t + "] ").withStyle(c).append(msg);
    }
}
