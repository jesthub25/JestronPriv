package com.jestron.jestronpriv.jc.util;

import com.jestron.jestronpriv.jc.JestronClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

/**
 * Otomatik mesaj gönderici. Spam ve atılmayı önlemek için tüm otomatik mesajlar arasında
 * en az {@link #MIN_GAP_MS} (makrolarda {@link #MANUAL_GAP_MS}) bekler; boş ve 256 karakterden uzun mesajları göndermez.
 */
public final class ChatSender {
    private ChatSender() {
    }

    public static final long MIN_GAP_MS = 2500;
    /** Elle basılan makrolar için daha kısa aralık. */
    public static final long MANUAL_GAP_MS = 1000;
    private static long last;

    /** @param manual oyuncu tuşa bastıysa (makro) true, otomatikse false
     *  @return gönderildiyse true */
    public static boolean send(String raw, boolean manual) {
        if (raw == null) return false;
        String msg = raw.trim();
        if (msg.isEmpty() || msg.length() > 256) return false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        ClientPacketListener con = mc.getConnection();
        if (con == null) return false;
        long now = System.currentTimeMillis();
        if (now - last < (manual ? MANUAL_GAP_MS : MIN_GAP_MS)) return false;
        last = now;
        try {
            if (msg.startsWith("/")) {
                String cmd = msg.substring(1).trim();
                if (cmd.isEmpty()) return false;
                con.sendCommand(cmd);
            } else {
                con.sendChat(msg);
            }
            return true;
        } catch (Throwable t) {
            JestronClient.LOG.warn("Otomatik mesaj gönderilemedi", t);
            return false;
        }
    }
}
