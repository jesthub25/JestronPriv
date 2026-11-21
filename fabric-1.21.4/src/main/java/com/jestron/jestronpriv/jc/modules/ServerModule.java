package com.jestron.jestronpriv.jc.modules;

import com.jestron.jestronpriv.jc.core.Category;
import com.jestron.jestronpriv.jc.core.TextHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

public final class ServerModule extends TextHud {
    public ServerModule() {
        super("server", "Sunucu", "Bağlı olduğun sunucunun adresi.", Category.HUD, false, 0.72, 0.28);
    }

    @Override
    protected String[] lines(boolean preview) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return preview ? new String[]{kv("Sunucu", "sunucu.adresi")} : new String[0];
        if (mc.isLocalServer()) return new String[]{kv("Sunucu", "Tek oyunculu")};
        ServerData sd = mc.getCurrentServer();
        return new String[]{kv("Sunucu", sd == null || sd.ip == null ? "—" : sd.ip)};
    }
}
