package com.jestron.jestronpriv.jc.gui;

import com.jestron.jestronpriv.jc.core.Config;
import com.jestron.jestronpriv.jc.core.Hud;
import com.jestron.jestronpriv.jc.core.HudModule;
import com.jestron.jestronpriv.jc.core.Modules;
import com.jestron.jestronpriv.jc.core.Theme;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Sürükle-bırak HUD düzenleyici: sürükle = taşı (kenar / orta mıknatısı), tekerlek = boyut,
 * sağ tık = gizle, Shift = serbest, ok tuşları = 1 px (Shift ile 10 px).
 */
public final class HudEditorScreen extends Screen {
    private static final int SNAP = 4;
    private final Screen parent;
    private HudModule dragging, selected;
    private double offX, offY;
    private int guideX = -1, guideY = -1;
    private boolean dirty;

    public HudEditorScreen(Screen parent) {
        super(Component.literal("HUD Düzenle"));
        this.parent = parent;
    }

    private List<HudModule> mods() {
        return Modules.HUD;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0x55000000);
        // ince orta çizgiler
        g.fill(width / 2, 0, width / 2 + 1, height, 0x22FFFFFF);
        g.fill(0, height / 2, width, height / 2 + 1, 0x22FFFFFF);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        Hud.frame++;
        HudModule hover = topAt(mouseX, mouseY);
        for (HudModule m : mods()) {
            if (!m.isEnabled()) continue;
            int[] b = Hud.bounds(m, true, width, height);
            if (b == null) continue;
            try {
                Hud.draw(g, m, true, width, height);
            } catch (Throwable ignored) {
            }
            boolean hot = m == hover || m == dragging || m == selected;
            int c = hot ? Theme.hudAccent() | 0xFF000000 : 0x80FFFFFF;
            outline(g, b[0] - 1, b[1] - 1, b[2] + 2, b[3] + 2, c);
            if (hot) {
                String label = m.name + "  " + Math.round(m.scale * 100) + "%";
                int ly = b[1] - 11 < 0 ? b[1] + b[3] + 2 : b[1] - 11;
                g.fill(b[0] - 1, ly - 1, b[0] + font.width(label) + 3, ly + 9, 0xCC000000);
                g.drawString(font, label, b[0] + 1, ly, 0xFFFFFFFF, false);
            }
        }
        if (dragging != null) {
            if (guideX >= 0) g.fill(guideX, 0, guideX + 1, height, 0xFF38BDF8);
            if (guideY >= 0) g.fill(0, guideY, width, guideY + 1, 0xFF38BDF8);
        }

        // yardım ve düğmeler
        String help = "Sürükle: taşı · Tekerlek: boyut · Sağ tık: gizle · Shift: serbest · Oklar: ince ayar";
        int hw = font.width(help) + 16;
        Draw.roundRect(g, (width - hw) / 2, 8, (width + hw) / 2, 24, 8, 0xCC000000);
        g.drawCenteredString(font, help, width / 2, 12, 0xFFFFFFFF);

        button(g, mouseX, mouseY, "Bitti", width / 2 - 64, height - 30, 60, true);
        button(g, mouseX, mouseY, "Sıfırla", width / 2 + 4, height - 30, 60, false);
    }

    private void button(GuiGraphics g, int mx, int my, String label, int x, int y, int w, boolean primary) {
        boolean hov = Draw.inside(mx, my, x, y, w, 18);
        int bg = primary ? Theme.hudAccent() | 0xFF000000 : (hov ? 0xEE3F3F46 : 0xEE27272A);
        Draw.roundRect(g, x, y, x + w, y + 18, 9, bg);
        int tc = primary ? (Theme.inkAccent() ? 0xFF18181B : 0xFFFFFFFF) : 0xFFFFFFFF;
        g.drawCenteredString(font, label, x + w / 2, y + 5, tc);
    }

    private static void outline(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }

    private HudModule topAt(double mx, double my) {
        List<HudModule> l = mods();
        for (int i = l.size() - 1; i >= 0; i--) {
            HudModule m = l.get(i);
            if (!m.isEnabled()) continue;
            int[] b = Hud.bounds(m, true, width, height);
            if (b != null && Draw.inside(mx, my, b[0], b[1], b[2], b[3])) return m;
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (Draw.inside(mx, my, width / 2 - 64, height - 30, 60, 18)) {
            onClose();
            return true;
        }
        if (Draw.inside(mx, my, width / 2 + 4, height - 30, 60, 18)) {
            for (HudModule m : mods()) m.resetPosition();
            dirty = true;
            return true;
        }
        HudModule m = topAt(mx, my);
        if (m == null) {
            selected = null;
            return false;
        }
        if (button == 1) {
            m.setEnabled(false);
            dirty = true;
            if (selected == m) selected = null;
            return true;
        }
        if (button == 0) {
            int[] b = Hud.bounds(m, true, width, height);
            if (b == null) return false;
            dragging = m;
            selected = m;
            offX = mx - b[0];
            offY = my - b[1];
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging == null) return false;
        int[] b = Hud.bounds(dragging, true, width, height);
        if (b == null) return false;
        int w = b[2], h = b[3];
        double nx = mx - offX, ny = my - offY;
        guideX = -1;
        guideY = -1;
        if (!hasShiftDown()) {
            // kenarlar ve orta çizgi
            if (Math.abs(nx) <= SNAP) {
                nx = 0;
                guideX = 0;
            } else if (Math.abs(nx + w - width) <= SNAP) {
                nx = width - w;
                guideX = width - 1;
            } else if (Math.abs(nx + w / 2.0 - width / 2.0) <= SNAP) {
                nx = width / 2.0 - w / 2.0;
                guideX = width / 2;
            }
            if (Math.abs(ny) <= SNAP) {
                ny = 0;
                guideY = 0;
            } else if (Math.abs(ny + h - height) <= SNAP) {
                ny = height - h;
                guideY = height - 1;
            } else if (Math.abs(ny + h / 2.0 - height / 2.0) <= SNAP) {
                ny = height / 2.0 - h / 2.0;
                guideY = height / 2;
            }
        }
        nx = Math.max(0, Math.min(width - w, nx));
        ny = Math.max(0, Math.min(height - h, ny));
        dragging.x = width <= 0 ? 0 : nx / width;
        dragging.y = height <= 0 ? 0 : ny / height;
        dirty = true;
        return true;
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragging = null;
        guideX = -1;
        guideY = -1;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        HudModule m = topAt(mx, my);
        if (m == null) return false;
        m.scale = HudModule.clamp(Math.round((m.scale + (sy > 0 ? 0.1 : -0.1)) * 10) / 10.0, 0.5, 2.0);
        dirty = true;
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (selected != null) {
            int step = hasShiftDown() ? 10 : 1;
            int dx = 0, dy = 0;
            if (key == GLFW.GLFW_KEY_LEFT) dx = -step;
            else if (key == GLFW.GLFW_KEY_RIGHT) dx = step;
            else if (key == GLFW.GLFW_KEY_UP) dy = -step;
            else if (key == GLFW.GLFW_KEY_DOWN) dy = step;
            if (dx != 0 || dy != 0) {
                int[] b = Hud.bounds(selected, true, width, height);
                if (b != null) {
                    double nx = Math.max(0, Math.min(width - b[2], b[0] + dx));
                    double ny = Math.max(0, Math.min(height - b[3], b[1] + dy));
                    selected.x = nx / width;
                    selected.y = ny / height;
                    dirty = true;
                }
                return true;
            }
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public void removed() {
        if (dirty) {
            Config.save();
            dirty = false;
        }
    }
}
