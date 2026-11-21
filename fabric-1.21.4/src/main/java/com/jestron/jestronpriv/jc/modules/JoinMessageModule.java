package com.jestron.jestronpriv.jc.modules;

import com.jestron.jestronpriv.jc.core.Category;
import com.jestron.jestronpriv.jc.core.Module;
import com.jestron.jestronpriv.jc.core.Setting;
import com.jestron.jestronpriv.jc.util.ChatSender;
import net.minecraft.client.Minecraft;

/** Sunucuya girince bir kez mesaj ya da komut gönderir. */
public final class JoinMessageModule extends Module {
    private final Setting.Text message = add(new Setting.Text("message", "Mesaj ya da /komut", "", 256));
    private final Setting.Num delay = add(new Setting.Num("delay", "Gecikme", 3, 1, 15, 1, " sn"));
    private final Setting.Bool singleplayer = add(new Setting.Bool("singleplayer", "Tek oyunculuda da", false));
    private int pendingTicks = -1;

    public JoinMessageModule() {
        super("joinmessage", "Giriş mesajı", "Sunucuya girince otomatik selam ya da komut.", Category.CHAT, false);
    }

    public void onJoin() {
        if (!isEnabled() || message.get().isBlank()) return;
        if (Minecraft.getInstance().isLocalServer() && !singleplayer.on()) return;
        pendingTicks = (int) Math.round(delay.d() * 20);
    }

    public void onLeave() {
        pendingTicks = -1;
    }

    @Override
    public void tick() {
        if (pendingTicks < 0) return;
        if (pendingTicks-- == 0) {
            ChatSender.send(message.get(), false);
            pendingTicks = -1;
        }
    }

    @Override
    protected void onDisable() {
        pendingTicks = -1;
    }
}
