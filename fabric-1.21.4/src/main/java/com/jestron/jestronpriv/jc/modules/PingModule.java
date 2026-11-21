package com.jestron.jestronpriv.jc.modules;

import com.jestron.jestronpriv.jc.core.Category;
import com.jestron.jestronpriv.jc.core.TextHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;

public final class PingModule extends TextHud {
    public PingModule() {
        super("ping", "Ping", "Sunucuya gecikme (ms).", Category.HUD, true, 0.005, 0.1);
    }

    @Override
    protected String[] lines(boolean preview) {
        Minecraft mc = Minecraft.getInstance();
        ClientPacketListener con = mc.getConnection();
        if (mc.player == null || con == null) return preview ? new String[]{kv("Ping", "24 ms")} : new String[0];
        if (mc.isLocalServer()) return new String[]{kv("Ping", "Tek oyunculu")};
        PlayerInfo pi = con.getPlayerInfo(mc.player.getUUID());
        if (pi == null) return new String[]{kv("Ping", "—")};
        return new String[]{kv("Ping", pi.getLatency() + " ms")};
    }
}
