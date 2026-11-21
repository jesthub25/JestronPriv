package com.jestron.jestronpriv.jc.modules;

import com.jestron.jestronpriv.jc.core.Category;
import com.jestron.jestronpriv.jc.core.Hud;
import com.jestron.jestronpriv.jc.core.HudModule;
import com.jestron.jestronpriv.jc.core.Setting;
import com.jestron.jestronpriv.jc.core.Theme;
import com.jestron.jestronpriv.jc.util.Clicks;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;

/** WASD + fare + boşluk tuşları; basılan tuş yumuşak geçişle vurgulanır. */
public final class KeystrokesModule extends HudModule {
    private final Setting.Bool mouse = add(new Setting.Bool("mouse", "Fare tuşları", true));
    private final Setting.Bool space = add(new Setting.Bool("space", "Boşluk tuşu", true));
    private final Setting.Bool cps = add(new Setting.Bool("cps", "Fare tuşunda CPS", true));

    private static final int K = 20, G = 2, W = K * 3 + G * 2;
    private final float[] anim = new float[7];
    private long lastNanos;

    public KeystrokesModule() {
        super("keystrokes", "Tuşlar", "WASD, fare ve boşluk tuşlarını gösterir.", Category.HUD, false, 0.005, 0.245);
    }

    @Override
    public int width(boolean preview) {
        return W;
    }

    @Override
    public int height(boolean preview) {
        int h = K * 2 + G;
        if (mouse.on()) h += G + K;
        if (space.on()) h += G + 12;
        return h;
    }

    @Override
    public void render(GuiGraphics g, boolean preview) {
        Minecraft mc = Minecraft.getInstance();
        Options o = mc.options;
        long now = System.nanoTime();
        float dt = lastNanos == 0 ? 0f : Math.min(0.1f, (now - lastNanos) / 1_000_000_000f);
        lastNanos = now;

        boolean[] down = {
                isDown(o.keyUp), isDown(o.keyLeft), isDown(o.keyDown), isDown(o.keyRight),
                isDown(o.keyAttack), isDown(o.keyUse), isDown(o.keyJump)
        };
        for (int i = 0; i < anim.length; i++) {
            float target = down[i] ? 1f : 0f;
            float step = dt * 12f;
            anim[i] = anim[i] < target ? Math.min(target, anim[i] + step) : Math.max(target, anim[i] - step);
        }

        key(g, K + G, 0, K, K, label(o.keyUp, "W"), anim[0]);
        key(g, 0, K + G, K, K, label(o.keyLeft, "A"), anim[1]);
        key(g, K + G, K + G, K, K, label(o.keyDown, "S"), anim[2]);
        key(g, (K + G) * 2, K + G, K, K, label(o.keyRight, "D"), anim[3]);
        int y = (K + G) * 2;
        if (mouse.on()) {
            int half = (W - G) / 2;
            mouseKey(g, 0, y, half, "SOL", Clicks.left(), anim[4]);
            mouseKey(g, half + G, y, W - half - G, "SAĞ", Clicks.right(), anim[5]);
            y += K + G;
        }
        if (space.on()) {
            bg(g, 0, y, W, 12, anim[6]);
            int c = fg(anim[6]);
            g.fill(W / 2 - 10, y + 5, W / 2 + 10, y + 7, c);
        }
    }

    private static boolean isDown(KeyMapping k) {
        try {
            return k.isDown();
        } catch (Throwable t) {
            return false;
        }
    }

    private static String label(KeyMapping k, String fallback) {
        try {
            String s = k.getTranslatedKeyMessage().getString();
            if (s.isEmpty()) return fallback;
            return s.length() > 3 ? s.substring(0, 3) : s;
        } catch (Throwable t) {
            return fallback;
        }
    }

    private static int mix(int a, int b, float t) {
        int aa = a >>> 24, ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255;
        int ba = b >>> 24, br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        return ((int) (aa + (ba - aa) * t) << 24) | ((int) (ar + (br - ar) * t) << 16)
                | ((int) (ag + (bg - ag) * t) << 8) | (int) (ab + (bb - ab) * t);
    }

    private static void bg(GuiGraphics g, int x, int y, int w, int h, float t) {
        int idle = Theme.hudBackground() ? 0x70000000 : 0x40000000;
        int on = (Theme.hudAccent() & 0x00FFFFFF) | 0xDD000000;
        g.fill(x, y, x + w, y + h, mix(idle, on, t));
    }

    private static int fg(float t) {
        int onText = Theme.inkAccent() ? 0xFF18181B : 0xFFFFFFFF;
        return mix(0xFFFFFFFF, onText, t);
    }

    private static void key(GuiGraphics g, int x, int y, int w, int h, String s, float t) {
        bg(g, x, y, w, h, t);
        int tw = Hud.width(s);
        Hud.text(g, s, x + (w - tw) / 2, y + (h - 8) / 2, fg(t));
    }

    private void mouseKey(GuiGraphics g, int x, int y, int w, String s, int clicks, float t) {
        bg(g, x, y, w, K, t);
        int c = fg(t);
        if (!cps.on()) {
            Hud.text(g, s, x + (w - Hud.width(s)) / 2, y + 6, c);
            return;
        }
        Hud.text(g, s, x + (w - Hud.width(s)) / 2, y + 2, c);
        String n = clicks + " CPS";
        g.pose().pushPose();
        try {
            g.pose().translate(x + w / 2f, y + 12, 0);
            g.pose().scale(0.75f, 0.75f, 1f);
            Hud.text(g, n, -Hud.width(n) / 2, 0, c);
        } finally {
            g.pose().popPose();
        }
    }
}
