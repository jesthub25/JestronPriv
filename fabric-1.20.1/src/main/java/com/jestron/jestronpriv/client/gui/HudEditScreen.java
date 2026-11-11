package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.hud.Hud;
import com.jestron.jestronpriv.client.hud.HudElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * HUD duzenleyici: ogeleri surukle-birak ile tasi, tekerlekle boyutlandir, sag tikla ac/kapat, ok tuslariyla
 * piksel piksel kaydir. Ekran ortasina ve kenarlara miknatis gibi hizalanir.
 */
public final class HudEditScreen extends Screen {

    private static final int SNAP = 5;
    private final Screen parent;
    private final JestronPrivClient mod;
    private final int[] rx = new int[Hud.ALL.size()], ry = new int[Hud.ALL.size()], rw = new int[Hud.ALL.size()], rh = new int[Hud.ALL.size()];
    private HudElement drag, selected;
    private double offX, offY;
    private boolean guideV, guideH, dirty;

    public HudEditScreen(Screen parent) {
        super(Component.literal("HUD Düzenle"));
        this.parent = parent;
        this.mod = JestronPrivClient.get();
    }

    private ModConfig cfg() {
        return mod.config();
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Bitti"), b -> onClose()).bounds(width / 2 + 4, height - 26, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Konumları sıfırla"), b -> {
            cfg().resetHudLayout();
            dirty = true;
        }).bounds(width / 2 - 104, height - 26, 100, 20).build());
    }

    private List<HudElement> elements() {
        return Hud.ALL;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0x38000000);
        // dorde bolen ince kilavuz
        for (int i = 1; i < 4; i++) {
            g.fill(width * i / 4, 0, width * i / 4 + 1, height, 0x14FFFFFF);
            g.fill(0, height * i / 4, width, height * i / 4 + 1, 0x14FFFFFF);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        ModConfig c = cfg();
        List<HudElement> els = elements();
        int hov = hovered(mouseX, mouseY);
        for (int i = 0; i < els.size(); i++) {
            HudElement e = els.get(i);
            try {
                boolean sample = !e.hasContent(c);
                Hud.drawAt(g, c, e, sample, width, height);
                float s = e.scale(c);
                rw[i] = Math.max(8, Math.round(e.width() * s));
                rh[i] = Math.max(8, Math.round(e.height() * s));
                rx[i] = e.screenX(c, width);
                ry[i] = e.screenY(c, height);
            } catch (Throwable t) {
                rw[i] = rh[i] = 0;
                continue;
            }
            boolean on = e.enabled(c);
            int col = !on ? 0xA0FF5050 : i == hov || e == drag ? 0xFFD070FF : e == selected ? 0xC0B070FF : 0x80B070FF;
            if (!on) g.fill(rx[i], ry[i], rx[i] + rw[i], ry[i] + rh[i], 0x90200000);
            g.renderOutline(rx[i] - 1, ry[i] - 1, rw[i] + 2, rh[i] + 2, col);
            String lab = e.label + (on ? "" : " §c(kapalı)") + " §7" + Math.round(e.scale(c) * 100) + "%";
            int ly = ry[i] > 11 ? ry[i] - 10 : ry[i] + rh[i] + 2;
            int lx = Math.max(1, Math.min(width - font.width(lab) - 1, rx[i]));
            g.drawString(font, lab, lx, ly, 0xFFFFFFFF, true);
        }
        if (drag != null && guideV) g.fill(width / 2, 0, width / 2 + 1, height, 0xC0FFD070);
        if (drag != null && guideH) g.fill(0, height / 2, width, height / 2 + 1, 0xC0FFD070);
        g.drawCenteredString(font, "§fSürükle: §7taşı   §fTekerlek: §7boyut   §fSağ tık: §7aç/kapat   §fOk tuşları: §71 piksel §8(Shift: 10)",
                width / 2, 6, 0xFFFFFF);
        g.drawCenteredString(font, "§8Kombo ve seri duyurusu da burada; boş kutular örnek gösterir", width / 2, 17, 0xFFFFFF);
    }

    private int hovered(double mx, double my) {
        List<HudElement> els = elements();
        for (int i = els.size() - 1; i >= 0; i--) {
            if (rw[i] <= 0) continue;
            if (mx >= rx[i] && mx < rx[i] + rw[i] && my >= ry[i] && my < ry[i] + rh[i]) return i;
        }
        return -1;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int i = hovered(mx, my);
        if (i >= 0) {
            HudElement e = elements().get(i);
            if (button == 1) {
                e.setEnabled(cfg(), !e.enabled(cfg()));
                dirty = true;
                return true;
            }
            if (button == 0) {
                drag = e;
                selected = e;
                offX = mx - rx[i];
                offY = my - ry[i];
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (drag == null || button != 0) return super.mouseDragged(mx, my, button, dx, dy);
        int i = elements().indexOf(drag);
        if (i < 0) return false;
        moveTo(drag, i, (int) Math.round(mx - offX), (int) Math.round(my - offY), true);
        return true;
    }

    /** Sol-ust koseyi (px, py) yapar; istenirse merkeze / kenarlara hizalar. */
    private void moveTo(HudElement e, int i, int px, int py, boolean snap) {
        ModConfig c = cfg();
        int ew = rw[i], eh = rh[i];
        guideV = guideH = false;
        if (snap) {
            if (Math.abs(px + ew / 2 - width / 2) <= SNAP) {
                px = width / 2 - ew / 2;
                guideV = true;
            }
            if (Math.abs(py + eh / 2 - height / 2) <= SNAP) {
                py = height / 2 - eh / 2;
                guideH = true;
            }
            if (px <= SNAP) px = 0;
            if (py <= SNAP) py = 0;
            if (px + ew >= width - SNAP) px = width - ew;
            if (py + eh >= height - SNAP) py = height - eh;
        }
        int fx = width - ew, fy = height - eh;
        float x = fx <= 0 ? 0f : Math.max(0f, Math.min(1f, px / (float) fx));
        float y = fy <= 0 ? 0f : Math.max(0f, Math.min(1f, py / (float) fy));
        e.setPos(c, x, y);
        rx[i] = Math.round(x * Math.max(0, fx));
        ry[i] = Math.round(y * Math.max(0, fy));
        dirty = true;
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (drag != null && button == 0) {
            drag = null;
            guideV = guideH = false;
            return true;
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        int i = hovered(mx, my);
        if (i >= 0 && sy != 0) {
            HudElement e = elements().get(i);
            ModConfig c = cfg();
            // merkez sabit kalsin
            int cx = rx[i] + rw[i] / 2, cy = ry[i] + rh[i] / 2;
            float s = Math.max(0.5f, Math.min(2f, Math.round((e.scale(c) + (sy > 0 ? 0.05f : -0.05f)) * 20f) / 20f));
            float k = s / Math.max(0.01f, e.scale(c));
            e.setScale(c, s);
            rw[i] = Math.max(8, Math.round(rw[i] * k));
            rh[i] = Math.max(8, Math.round(rh[i] * k));
            moveTo(e, i, cx - rw[i] / 2, cy - rh[i] / 2, false);
            return true;
        }
        return super.mouseScrolled(mx, my, sx, sy);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (selected != null) {
            int i = elements().indexOf(selected);
            int dx = key == 263 ? -1 : key == 262 ? 1 : 0, dy = key == 265 ? -1 : key == 264 ? 1 : 0;
            if (i >= 0 && (dx != 0 || dy != 0)) {
                int step = hasShiftDown() ? 10 : 1;
                moveTo(selected, i, rx[i] + dx * step, ry[i] + dy * step, false);
                return true;
            }
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public void removed() {
        if (dirty) {
            cfg().save();
            dirty = false;
        }
        super.removed();
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
