package com.jestron.jestronpriv.client.tag;


import com.jestron.jestronpriv.tag.EmojiIndex;
import com.jestron.jestronpriv.tag.TagFonts;
import com.jestron.jestronpriv.tag.TagSettings;
import com.jestron.jestronpriv.tag.TagStyle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Isim etiketi cizici: harf harf renk ve hareket, emoji ve gorsel karolari, arka plan, kontur, duvar arkasi
 * gorunurlugu. Dizilim (harf genislikleri, emoji ayristirma) metin degisince bir kez hesaplanir ve saklanir.
 */
public final class TagRenderer {

    private static final int T_CHAR = 0, T_EMOJI = 1, T_ICON_PRE = 2, T_ICON_SUF = 3, T_GAP = 4;

    private static final class Seg {
        int type;
        FormattedCharSequence seq;
        int emoji = -1;
        float w;
    }

    private static final class Layout {
        Seg[] segs;
        float width;
        int textCount, emojiCount;
    }

    private static final Map<String, Layout> CACHE = new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Layout> e) {
            return size() > 96;
        }
    };
    private static final Map<String, ResourceLocation> FONTS = new java.util.HashMap<>();
    private static final float[] EA = new float[4];

    private TagRenderer() {
    }

    /** Font / ayar degisince dizilimler yeniden hesaplansin. */
    public static void invalidate() {
        CACHE.clear();
    }

    private static ResourceLocation font(String id) {
        return FONTS.computeIfAbsent(id == null ? "mc" : id, k -> {
            try {
                return ResourceLocation.parse(TagFonts.get(k).res());
            } catch (Throwable t) {
                return ResourceLocation.withDefaultNamespace("default");
            }
        });
    }

    private static Style style(TagSettings tag) {
        int f = tag.flags;
        return Style.EMPTY.withFont(font(tag.font)).withBold((f & TagSettings.F_BOLD) != 0).withItalic((f & TagSettings.F_ITALIC) != 0)
                .withUnderlined((f & TagSettings.F_UNDERLINE) != 0).withStrikethrough((f & TagSettings.F_STRIKE) != 0);
    }

    private static float emojiSize(TagSettings tag) {
        return 9f * tag.emojiScale;
    }

    private static Layout layout(Font font, TagSettings tag, String text, float preAspect, float sufAspect) {
        String key = text + '\u0000' + tag.font + '\u0000' + tag.flags + '\u0000' + tag.emojiScale + '\u0000' + tag.prefix + '\u0000' + tag.suffix
                + '\u0000' + preAspect + '\u0000' + sufAspect;
        Layout L = CACHE.get(key);
        if (L != null) return L;
        EmojiIndex idx = EmojiAtlas.index();
        Style st = style(tag);
        float es = emojiSize(tag);
        List<Seg> segs = new ArrayList<>();
        int[] counts = new int[2];
        if (preAspect > 0) add(segs, T_ICON_PRE, null, -1, es * clampAspect(preAspect) + 1f);
        boolean hasPre = appendTokens(segs, font, st, idx, tag.prefix, es, counts);
        if (preAspect > 0 || hasPre) add(segs, T_GAP, null, -1, 2f);
        appendTokens(segs, font, st, idx, text, es, counts);
        boolean hasSuf = !tag.suffix.isEmpty();
        if (hasSuf || sufAspect > 0) add(segs, T_GAP, null, -1, 2f);
        appendTokens(segs, font, st, idx, tag.suffix, es, counts);
        if (sufAspect > 0) add(segs, T_ICON_SUF, null, -1, es * clampAspect(sufAspect) + 1f);
        L = new Layout();
        L.segs = segs.toArray(new Seg[0]);
        float w = 0;
        for (Seg s : L.segs) w += s.w;
        L.width = w;
        L.textCount = counts[0];
        L.emojiCount = counts[1];
        CACHE.put(key, L);
        return L;
    }

    private static float clampAspect(float a) {
        return Math.max(0.5f, Math.min(2.5f, a));
    }

    private static void add(List<Seg> segs, int type, FormattedCharSequence seq, int emoji, float w) {
        Seg s = new Seg();
        s.type = type;
        s.seq = seq;
        s.emoji = emoji;
        s.w = w;
        segs.add(s);
    }

    private static boolean appendTokens(List<Seg> segs, Font font, Style st, EmojiIndex idx, String s, float es, int[] counts) {
        if (s == null || s.isEmpty()) return false;
        boolean any = false;
        for (EmojiIndex.Token t : idx.tokenize(s)) {
            if (t.emoji() >= 0) {
                add(segs, T_EMOJI, null, t.emoji(), es + 1f);
                counts[1]++;
                any = true;
            } else {
                String p = t.text();
                for (int i = 0; i < p.length(); ) {
                    int cp = p.codePointAt(i);
                    int n = Character.charCount(cp);
                    FormattedCharSequence seq = FormattedCharSequence.forward(p.substring(i, i + n), st);
                    add(segs, T_CHAR, seq, -1, font.width(seq));
                    counts[0]++;
                    any = true;
                    i += n;
                }
            }
        }
        return any;
    }

    /** Etiket genisligi (etiket pikseli). */
    public static float width(TagSettings tag, String text, IconTexture pre, IconTexture suf) {
        Minecraft mc = Minecraft.getInstance();
        return layout(mc.font, tag, text, pre == null ? 0 : pre.aspect(), suf == null ? 0 : suf.aspect()).width;
    }

    /**
     * Etiketi cizer. ps etiket noktasinda, kameraya donuk ve vanilla gibi (0.025, -0.025) olcekli olmali.
     *
     * @param realName     oyuncunun gercek adi (ozel metin yoksa o yazilir)
     * @param seeThrough   duvar arkasindan soluk gorunsun (vanilla, egilmiyorken)
     * @param showRealName ozel metin varsa gercek adi altta kucuk yaz
     */
    public static void draw(PoseStack ps, MultiBufferSource buffers, int light, TagSettings tag, String realName, IconTexture pre,
                            IconTexture suf, boolean seeThrough, boolean showRealName, long ms) {
        if (tag == null) return;
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        String text = tag.text == null || tag.text.isEmpty() ? (realName == null ? "" : realName) : tag.text;
        if (pre != null && pre.released()) pre = null;
        if (suf != null && suf.released()) suf = null;
        Layout L = layout(font, tag, text, pre == null ? 0 : pre.aspect(), suf == null ? 0 : suf.aspect());
        if (L.segs.length == 0) return;
        EmojiAtlas.prepare();
        float t = ms / 1000f * tag.animSpeed;
        float sc = TagStyle.scale(tag.anim, t);
        ps.pushPose();
        try {
            if (sc != 1f) ps.scale(sc, sc, sc);
            Matrix4f m = ps.last().pose();
            boolean glow = (tag.flags & TagSettings.F_GLOW) != 0;
            int lt = glow ? LightTexture.FULL_BRIGHT : LightTexture.lightCoordsWithEmission(light, 2);
            boolean shadow = (tag.flags & TagSettings.F_SHADOW) != 0, outline = (tag.flags & TagSettings.F_OUTLINE) != 0;
            float x0 = -L.width / 2f;
            if (tag.bgAlpha > 0.01f) {
                int bg = (Math.round(tag.bgAlpha * 255f) << 24) | (tag.bgColor & 0xFFFFFF);
                VertexConsumer vc = buffers.getBuffer(seeThrough ? RenderType.textBackgroundSeeThrough() : RenderType.textBackground());
                float top = Math.min(-1f, 3.5f - emojiSize(tag) / 2f - 0.5f), bot = Math.max(9f, 3.5f + emojiSize(tag) / 2f + 0.5f);
                if (L.emojiCount == 0 && pre == null && suf == null) {
                    top = -1f;
                    bot = 9f;
                }
                rect(vc, m, x0 - 1.5f, top, x0 + L.width + 1.5f, bot, 0.01f, bg, lt);
            }
            int visible = TagStyle.visible(tag.anim, L.textCount, t);
            int ci = 0, ei = 0;
            float x = x0;
            for (Seg s : L.segs) {
                switch (s.type) {
                    case T_CHAR -> {
                        if (ci < visible) {
                            int col = 0xFF000000 | TagStyle.color(tag.colorMode, tag.c1, tag.c2, tag.c3, ci, L.textCount, t);
                            float dy = TagStyle.offsetY(tag.anim, ci, L.textCount, t), dx = TagStyle.offsetX(tag.anim, ci, L.textCount, t);
                            drawChar(font, s.seq, x + dx, dy, col, tag, m, buffers, lt, seeThrough, shadow, outline);
                        }
                        ci++;
                    }
                    case T_EMOJI -> {
                        drawEmoji(buffers, m, x, s.emoji, ei, tag, t, lt, seeThrough);
                        ei++;
                    }
                    case T_ICON_PRE -> drawIcon(buffers, m, x, pre, ei++, tag, t, lt, seeThrough, ms);
                    case T_ICON_SUF -> drawIcon(buffers, m, x, suf, ei++, tag, t, lt, seeThrough, ms);
                    default -> {
                    }
                }
                x += s.w;
            }
            if (showRealName && realName != null && !realName.isEmpty() && !realName.equals(text)) {
                ps.pushPose();
                float below = Math.max(10.5f, 3.5f + emojiSize(tag) / 2f + 2f);
                ps.translate(0f, below, 0f);
                ps.scale(0.7f, 0.7f, 0.7f);
                Matrix4f m2 = ps.last().pose();
                float w = font.width(realName);
                if (seeThrough) font.drawInBatch(realName, -w / 2f, 0f, 0x50C8C8C8, false, m2, buffers, Font.DisplayMode.SEE_THROUGH, 0x40000000, lt);
                font.drawInBatch(realName, -w / 2f, 0f, 0xFFC8C8C8, false, m2, buffers, Font.DisplayMode.NORMAL, seeThrough ? 0 : 0x40000000, lt);
                ps.popPose();
            }
        } finally {
            ps.popPose();
        }
    }

    private static void drawChar(Font font, FormattedCharSequence seq, float x, float y, int col, TagSettings tag, Matrix4f m,
                                 MultiBufferSource buffers, int lt, boolean seeThrough, boolean shadow, boolean outline) {
        if (seeThrough) font.drawInBatch(seq, x, y, (0x58 << 24) | (col & 0xFFFFFF), false, m, buffers, Font.DisplayMode.SEE_THROUGH, 0, lt);
        if (outline) font.drawInBatch8xOutline(seq, x, y, col, 0xFF000000 | tag.outlineColor, m, buffers, lt);
        else font.drawInBatch(seq, x, y, col, shadow, m, buffers, Font.DisplayMode.NORMAL, 0, lt);
    }

    private static void drawEmoji(MultiBufferSource buffers, Matrix4f m, float x, int idx, int k, TagSettings tag, float t, int lt, boolean see) {
        float size = emojiSize(tag);
        TagStyle.emoji(tag.emojiAnim, k, t, EA);
        float cx = x + size / 2f, cy = 3.5f + EA[0];
        float u0 = EmojiAtlas.u0(idx), u1 = EmojiAtlas.u1(idx), v0 = EmojiAtlas.v0(idx), v1 = EmojiAtlas.v1(idx);
        if (see) sprite(buffers.getBuffer(RenderType.textSeeThrough(EmojiAtlas.TEXTURE)), m, cx, cy, size, EA, u0, v0, u1, v1, 0x58FFFFFF, lt);
        sprite(buffers.getBuffer(RenderType.text(EmojiAtlas.TEXTURE)), m, cx, cy, size, EA, u0, v0, u1, v1, 0xFFFFFFFF, lt);
    }

    private static void drawIcon(MultiBufferSource buffers, Matrix4f m, float x, IconTexture ic, int k, TagSettings tag, float t, int lt,
                                 boolean see, long ms) {
        if (ic == null) return;
        float h = emojiSize(tag), w = h * clampAspect(ic.aspect());
        TagStyle.emoji(tag.emojiAnim, k, t, EA);
        int f = ic.frameAt(ms);
        float cx = x + w / 2f, cy = 3.5f + EA[0];
        float sx = EA[1];
        // genis gorselde dondurme yerine yatay olcek kullanilir; sprite kare kabul eder, en-boy burada uygulanir
        EA[1] = sx * (w / h);
        if (see) sprite(buffers.getBuffer(RenderType.textSeeThrough(ic.id)), m, cx, cy, h, EA, 0f, ic.v0(f), 1f, ic.v1(f), 0x58FFFFFF, lt);
        sprite(buffers.getBuffer(RenderType.text(ic.id)), m, cx, cy, h, EA, 0f, ic.v0(f), 1f, ic.v1(f), 0xFFFFFFFF, lt);
    }

    /** Merkezli kare (olcek, yatay olcek ve donme ile). Yatay olcek negatifse ayna (dokuyu cevirerek, yuz yonu korunur). */
    private static void sprite(VertexConsumer vc, Matrix4f m, float cx, float cy, float size, float[] a, float u0, float v0, float u1, float v1, int col, int lt) {
        float s = size * a[2] / 2f;
        float sx = a[1];
        if (sx < 0) {
            float tu = u0;
            u0 = u1;
            u1 = tu;
            sx = -sx;
        }
        float hx = s * Math.max(0.02f, sx), hy = s;
        float c = (float) Math.cos(a[3]), sn = (float) Math.sin(a[3]);
        // koseler: (x0,y0) (x0,y1) (x1,y1) (x1,y0) — vanilla harf sirasi (nesne uretmeden)
        corner(vc, m, cx, cy, -hx, -hy, c, sn, u0, v0, col, lt);
        corner(vc, m, cx, cy, -hx, hy, c, sn, u0, v1, col, lt);
        corner(vc, m, cx, cy, hx, hy, c, sn, u1, v1, col, lt);
        corner(vc, m, cx, cy, hx, -hy, c, sn, u1, v0, col, lt);
    }

    private static void corner(VertexConsumer vc, Matrix4f m, float cx, float cy, float qx, float qy, float c, float sn, float u, float v, int col, int lt) {
        vc.addVertex(m, cx + qx * c - qy * sn, cy + qx * sn + qy * c, 0f).setColor(col).setUv(u, v).setLight(lt);
    }

    private static void rect(VertexConsumer vc, Matrix4f m, float x0, float y0, float x1, float y1, float z, int col, int lt) {
        vc.addVertex(m, x0, y0, z).setColor(col).setLight(lt);
        vc.addVertex(m, x0, y1, z).setColor(col).setLight(lt);
        vc.addVertex(m, x1, y1, z).setColor(col).setLight(lt);
        vc.addVertex(m, x1, y0, z).setColor(col).setLight(lt);
    }
}
