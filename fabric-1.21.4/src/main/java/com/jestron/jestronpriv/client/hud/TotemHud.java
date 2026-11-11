package com.jestron.jestronpriv.client.hud;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.pvp.TotemFeed;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Totem bildirimleri: "[yuz] Oyuncu ✦ TOTEM ×3 · 2 sn". Totem sayisina gore renk (sari / turuncu / kirmizi), yeni
 * totemde satir nabiz atar, oyuncu olurse "☠ öldü" yazar. Satirlar suresi bitince soner.
 */
final class TotemHud extends BoundElement {

    private static final int ROW = 14;
    private final List<TotemFeed.Entry> view = new ArrayList<>();
    private ItemStack totem;
    private List<TotemFeed.Entry> sampleRows;
    private long sampleAt;

    TotemHud() {
        super("totemfeed", "Totem Bildirimleri", c -> c.tfHud, (c, v) -> c.tfHud = v, c -> c.tfX, (c, v) -> c.tfX = v, c -> c.tfY, (c, v) -> c.tfY = v,
                c -> c.tfScale, (c, v) -> c.tfScale = v);
    }

    private static float life(ModConfig c) {
        return Math.max(2f, Math.min(30f, c.tfDuration)) * 1000f;
    }

    private void collect(ModConfig c, boolean sample) {
        view.clear();
        int max = Math.max(1, Math.min(8, c.tfMax));
        long now = Util.getMillis();
        float life = life(c);
        for (TotemFeed.Entry e : TotemFeed.entries()) {
            if (view.size() >= max) break;
            if (now - e.time <= life + 500) view.add(e);
        }
        if (sample && view.isEmpty()) {
            if (sampleRows == null || now - sampleAt > 3000) {
                sampleRows = TotemFeed.sample();
                sampleAt = now;
            }
            for (TotemFeed.Entry e : sampleRows) if (view.size() < max) view.add(e);
        }
    }

    @Override
    public boolean hasContent(ModConfig c) {
        long now = Util.getMillis();
        float life = life(c);
        for (TotemFeed.Entry e : TotemFeed.entries()) if (now - e.time <= life + 500) return true;
        return false;
    }

    private static String label(TotemFeed.Entry e) {
        return e.died ? " ☠ öldü (" + e.count + " totem)" : " ✦ TOTEM ×" + e.count;
    }

    private static String ago(long ms) {
        long s = ms / 1000;
        return s < 1 ? "şimdi" : s + " sn";
    }

    @Override
    public void measure(ModConfig c, boolean sample) {
        collect(c, sample);
        Font f = Minecraft.getInstance().font;
        int mw = 110;
        for (TotemFeed.Entry e : view) mw = Math.max(mw, 4 + 12 + f.width(e.me ? "Sen" : e.name) + f.width(label(e)) + f.width(" · 00 sn") + 6);
        w = mw;
        h = Math.max(1, Math.min(8, c.tfMax)) * ROW;
    }

    @Override
    public void draw(GuiGraphics g, ModConfig c, boolean sample, boolean right) {
        Font f = Minecraft.getInstance().font;
        long now = Util.getMillis();
        float life = life(c);
        ClientPacketListener con = Minecraft.getInstance().getConnection();
        for (int i = 0; i < view.size(); i++) {
            TotemFeed.Entry e = view.get(i);
            long age = now - e.time;
            float a = age > life ? Math.max(0f, 1f - (age - life) / 500f) : 1f;
            if (a * 255 < 5) continue;
            int ax = (int) (a * 255) << 24;
            String name = e.me ? "Sen" : e.name;
            String lab = label(e);
            String t = " · " + ago(now - e.pulseAt);
            int rw = 4 + 12 + f.width(name) + f.width(lab) + f.width(t) + 6;
            int y = i * ROW;
            int x0 = right ? w - rw : 0;
            float pAge = (now - e.pulseAt) / 1000f;
            float pulse = !e.died && pAge < 0.35f ? (float) Math.sin(Math.PI * pAge / 0.35f) : 0f;
            float slide = age < 180 && !e.died ? 1f - age / 180f : 0f;
            int off = Math.round(slide * slide * 40f) * (right ? 1 : -1);
            int col = e.died ? 0xFF6B6B : e.count >= 5 ? 0xFF4A4A : e.count >= 3 ? 0xFF9A3C : 0xFFD54F;
            g.pose().pushPose();
            g.pose().translate(off, 0, 0);
            Hud.background(g, x0, y, x0 + rw, y + ROW - 1, c.tfBg * a);
            if (pulse > 0f) g.fill(x0, y, x0 + rw, y + ROW - 1, ((int) (pulse * 90 * a) << 24) | (col & 0xFFFFFF));
            g.fill(x0, y, x0 + 2, y + ROW - 1, ax | col);
            int x = x0 + 4;
            PlayerInfo pi = c.tfHead && con != null && e.uuid != null ? con.getPlayerInfo(e.uuid) : null;
            if (pi != null) {
                PlayerFaceRenderer.draw(g, pi.getSkin(), x, y + 2, 9);
            } else {
                if (totem == null) totem = new ItemStack(Items.TOTEM_OF_UNDYING);
                g.pose().pushPose();
                g.pose().translate(x - 1, y + 1, 0);
                g.pose().scale(0.7f, 0.7f, 1f);
                g.renderItem(totem, 0, 0);
                g.pose().popPose();
            }
            x += 12;
            g.pose().pushPose();
            g.pose().translate(0, 0, 200);
            Hud.text(g, f, name, x, y + 3, ax | (e.me ? 0x7CFF95 : 0xFFFFFF), c);
            x += f.width(name);
            Hud.text(g, f, lab, x, y + 3, ax | col, c);
            x += f.width(lab);
            Hud.text(g, f, t, x, y + 3, ax | 0x909090, c);
            g.pose().popPose();
            g.pose().popPose();
        }
    }
}
