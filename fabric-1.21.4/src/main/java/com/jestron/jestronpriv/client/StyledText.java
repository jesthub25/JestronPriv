package com.jestron.jestronpriv.client;

import com.jestron.jestronpriv.fx.api.TextStyle;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/** Ozel yazi efektinin dunya icinde ve menude ortak bicimlendirmesi. */
public final class StyledText {

    private static final ResourceLocation[] FONTS = {
            ResourceLocation.withDefaultNamespace("default"),
            ResourceLocation.withDefaultNamespace("uniform"),
            ResourceLocation.withDefaultNamespace("alt"),
            ResourceLocation.withDefaultNamespace("illageralt")};
    public static final String[] FONT_NAMES = {"Normal", "Unicode (ince)", "Büyü Dili (Galaktik)", "İllager Rünleri"};

    private StyledText() {
    }

    /** Renksiz temel stil (yazi tipi + kalin/italik/...). */
    public static Style plainStyle(TextStyle st) {
        return base(st);
    }

    private static Style base(TextStyle st) {
        return Style.EMPTY.withFont(FONTS[Math.max(0, Math.min(FONTS.length - 1, st.font()))])
                .withBold(st.bold()).withItalic(st.italic()).withUnderlined(st.underline()).withStrikethrough(st.strike());
    }

    private static int lerpRgb(int a, int b, float t) {
        int r = (int) (((a >> 16) & 0xFF) + (((b >> 16) & 0xFF) - ((a >> 16) & 0xFF)) * t);
        int g = (int) (((a >> 8) & 0xFF) + (((b >> 8) & 0xFF) - ((a >> 8) & 0xFF)) * t);
        int bl = (int) ((a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * t);
        return (r << 16) | (g << 8) | bl;
    }

    private static int hsv(float h) {
        h = h - (float) Math.floor(h);
        float r = Math.abs(h * 6 - 3) - 1, g = 2 - Math.abs(h * 6 - 2), b = 2 - Math.abs(h * 6 - 4);
        r = Math.max(0, Math.min(1, r));
        g = Math.max(0, Math.min(1, g));
        b = Math.max(0, Math.min(1, b));
        return ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }

    /**
     * Bir satiri renk moduna gore bicimler. '§' kodlari korunur (kullanici yazinin bir kismini ayrica boyayabilir).
     * time: animasyonlu gokkusagi icin (tick).
     */
    public static MutableComponent line(String text, TextStyle st, float time) {
        Style base = base(st);
        if (st.colorMode() == 0) {
            return Component.literal(text).withStyle(base.withColor(st.color1()));
        }
        MutableComponent out = Component.empty();
        // Gorunen karakter sayisi (bicim kodlari haric)
        int visible = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '§') i++;
            else visible++;
        }
        int idx = 0;
        StringBuilder codes = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '§' && i + 1 < text.length()) {
                codes.append(c).append(text.charAt(i + 1));
                i++;
                continue;
            }
            float u = visible <= 1 ? 0 : idx / (float) (visible - 1);
            int col = st.colorMode() == 1 ? lerpRgb(st.color1(), st.color2(), u) : hsv(u * 0.8f + time * 0.02f);
            out.append(Component.literal(codes + String.valueOf(c)).withStyle(base.withColor(col)));
            idx++;
        }
        return out;
    }

    public static String strip(String s) {
        return s.replaceAll("§.", "");
    }

    /** Dunya icinde kameraya donuk, ortalanmis, cok satirli stilli yazi. */
    public static void render(PoseStack ps, MultiBufferSource buffers, Camera cam, String text, TextStyle st,
                              float scale, int light, Font font, long now, float partial) {
        ps.mulPose(cam.rotation());
        float s = 0.025f * Math.max(scale, 0.0005f);
        ps.scale(s, -s, s);
        int lt = st.glow() ? LightTexture.FULL_BRIGHT : light;
        String[] lines = text.split("\n");
        int lineH = font.lineHeight + 2;
        float y = -lines.length * lineH;
        float time = now + partial;
        for (String raw : lines) {
            MutableComponent c = line(raw, st, time);
            float x = -font.width(c) / 2f;
            Matrix4f m = ps.last().pose();
            int color = st.colorMode() == 0 ? (0xFF000000 | st.color1()) : 0xFFFFFFFF;
            if (!st.outline()) {
                font.drawInBatch(c, x, y, color, st.shadow(), m, buffers, Font.DisplayMode.NORMAL, st.background(), lt);
            } else {
                if (st.background() != 0) {
                    // Sadece arka plan kutusu: yazi neredeyse tamamen saydam (golgelendirici atar), kutu arkada kalir
                    MutableComponent bg = Component.literal(strip(raw)).withStyle(base(st));
                    font.drawInBatch(bg, x, y, 0x04FFFFFF, false, m, buffers, Font.DisplayMode.NORMAL, st.background(), lt);
                }
                font.drawInBatch8xOutline(c.getVisualOrderText(), x, y, color, 0xFF000000 | st.outlineColor(), m, buffers, lt);
            }
            y += lineH;
        }
    }
}
