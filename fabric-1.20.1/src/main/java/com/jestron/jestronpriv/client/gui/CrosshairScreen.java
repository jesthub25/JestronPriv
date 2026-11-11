package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.pvp.Crosshair;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/** Nisangah duzenleyici: ustte canli onizleme (gercek boyut + 3x buyutulmus, acik ve koyu zeminde). */
public final class CrosshairScreen extends JpScreen {

    private static final int PREVIEW_H = 58;
    private static int testKind;

    public CrosshairScreen(Screen parent) {
        super(Component.literal("Nişangah"), parent);
    }

    @Override
    protected String subtitle() {
        return "§7Önizleme canlıdır · Saldırı göstergesi (vanilla) aynen kalır";
    }

    @Override
    protected int topOffset() {
        return TOP + PREVIEW_H + 6;
    }

    @Override
    protected void buildOptions() {
        ModConfig c = cfg();
        header("Nişangah");
        toggle("Özel nişangah", "Vanilla nişangahın yerine aşağıdaki tasarım çizilir.", () -> c.chEnabled, v -> c.chEnabled = v);
        cycle("Şekil", "50 şekil: artı, çarpı, halka, kare, elmas, üçgen, yıldız, kalp, radar, keskin nişancı, dönen / nabız atan...\n"
                        + "Tıkla: sonraki · Shift+tık: önceki. Önizleme yukarıda.",
                () -> "§d" + Crosshair.Shape.parse(c.chShape).label + " §8(" + (Crosshair.Shape.parse(c.chShape).ordinal() + 1) + "/"
                        + Crosshair.Shape.values().length + ")",
                () -> {
                    Crosshair.Shape sh = Crosshair.Shape.parse(c.chShape);
                    c.chShape = (hasShiftDown() ? sh.previous() : sh.next()).name();
                });
        slider("Uzunluk", "Kolların uzunluğu (daire / köşelide yarıçap).", 1, 16, 1, () -> c.chSize, v -> c.chSize = (float) v,
                v -> String.format(Locale.ROOT, "%.0f px", v));
        slider("Kalınlık", null, 1, 4, 1, () -> c.chThickness, v -> c.chThickness = (float) v, v -> String.format(Locale.ROOT, "%.0f px", v));
        slider("Boşluk", "Merkezle kollar arasındaki boşluk.", 0, 10, 1, () -> c.chGap, v -> c.chGap = (float) v,
                v -> String.format(Locale.ROOT, "%.0f px", v));
        slider("Opaklık", null, 0.1, 1, 0.05, () -> c.chOpacity, v -> c.chOpacity = (float) v, JpScreen::pct);

        header("Renk");
        color("Renk", null, () -> c.chColor, v -> c.chColor = v);
        hex("#RRGGBB (nişangah)", v -> c.chColor = v);
        toggle("Gökkuşağı", "Renk sürekli akar.", () -> c.chRainbow, v -> c.chRainbow = v);
        toggle("Siyah kontur", "Her zeminde (kar, gökyüzü) net görünür.", () -> c.chOutline, v -> c.chOutline = v);

        header("Davranış");
        toggle("Orta nokta", "Şeklin ortasına nokta ekler.", () -> c.chDot, v -> c.chDot = v);
        toggle("Dinamik açılma", "Yürürken, havadayken ve saldırı dolarken kollar hafifçe açılır;\ndurunca yumuşakça kapanır.",
                () -> c.chDynamic, v -> c.chDynamic = v);
        toggle("İsabet işareti", "Sunucu onaylı isabette çapraz işaret:\n§fbeyaz§7 vuruş · §6altın§7 kritik · §ckızıl§7 öldürme.",
                () -> c.chHitMarker, v -> c.chHitMarker = v);
        action("§e▶ İsabeti dene", "Sırayla vuruş / kritik / öldürme işareti.", false, () -> {
            Crosshair.onHit(testKind);
            testKind = (testKind + 1) % 3;
        });
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        ModConfig c = cfg();
        int top = TOP - 2, w = COL_W * 2 + GAP, x0 = width / 2 - w / 2;
        long now = System.nanoTime();
        float extra = c.chDynamic ? (float) (1.5 + 1.5 * Math.sin(now / 1e9 * 2.2)) : 0f;
        // sol yari: acik (gokyuzu), sag yari: koyu (gece / magara)
        int half = w / 2;
        g.fillGradient(x0, top, x0 + half, top + PREVIEW_H, 0xFF8EC5FF, 0xFFD8ECFF);
        g.fillGradient(x0 + half, top, x0 + w, top + PREVIEW_H, 0xFF202634, 0xFF0B0D12);
        g.renderOutline(x0, top, w, PREVIEW_H, 0x60B070FF);
        int cy = top + PREVIEW_H / 2;
        if (!c.chEnabled) {
            g.drawCenteredString(font, "§7(kapalı — açınca oyunda bu görünür)", width / 2, top + PREVIEW_H - 11, 0xFFFFFF);
        }
        try {
            // gercek boyut
            Crosshair.draw(g, x0 + half / 4, cy, c, extra, now);
            Crosshair.draw(g, x0 + half + half * 3 / 4, cy, c, extra, now);
            // 3x buyutulmus
            for (int side = 0; side < 2; side++) {
                int cx = x0 + (side == 0 ? half * 5 / 8 : half + half * 3 / 8);
                g.enableScissor(cx - half / 4 + 2, top + 1, cx + half / 4 - 2, top + PREVIEW_H - 1);
                g.pose().pushPose();
                g.pose().translate(cx, cy, 0);
                g.pose().scale(3f, 3f, 1f);
                Crosshair.draw(g, 0, 0, c, extra, now);
                g.pose().popPose();
                g.disableScissor();
            }
        } catch (Throwable ignored) {
        }
        g.drawString(font, "§81x", x0 + 3, top + 3, 0xFFFFFF, false);
        g.drawString(font, "§83x", x0 + half * 3 / 8 + 3, top + 3, 0xFFFFFF, false);
    }

    @Override
    protected Runnable resetAction() {
        return () -> cfg().resetCrosshair();
    }
}
