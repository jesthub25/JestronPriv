package com.jestron.jestronpriv.client.tag;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.tag.EmojiIndex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;

/** Emoji atlasi (3944 renkli emoji, Twemoji) ve dizini. Ilk kullanimda yuklenir. */
public final class EmojiAtlas {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("jestronpriv", "textures/emoji/atlas.png");
    private static volatile EmojiIndex index;
    private static boolean failed, filtered;

    private EmojiAtlas() {
    }

    /** Dizin (yuklenemezse bos dizin; asla null degil). */
    public static EmojiIndex index() {
        EmojiIndex x = index;
        if (x != null) return x;
        synchronized (EmojiAtlas.class) {
            if (index != null) return index;
            EmojiIndex loaded = null;
            if (!failed) {
                try (InputStream in = EmojiAtlas.class.getResourceAsStream("/assets/jestronpriv/emoji/index.tsv")) {
                    if (in != null) loaded = EmojiIndex.parse(in);
                } catch (Throwable t) {
                    JestronPrivClient.LOG.warn("JestronPriv emoji dizini okunamadi", t);
                }
                if (loaded == null) failed = true;
            }
            if (loaded == null) {
                try {
                    loaded = EmojiIndex.parse(new java.io.ByteArrayInputStream(new byte[0]));
                } catch (Throwable t) {
                    return null;
                }
            }
            index = loaded;
            return loaded;
        }
    }

    /** Atlas dokusunu yumusak (cift dogrusal) suzgece alir: kucuk boyutta emojiler pul pul olmaz. */
    public static void prepare() {
        if (filtered) return;
        try {
            AbstractTexture t = Minecraft.getInstance().getTextureManager().getTexture(TEXTURE);
            if (t != null) {
                t.setFilter(true, false);
                filtered = true;
            }
        } catch (Throwable ignored) {
            filtered = true;
        }
    }

    public static float u0(int i) {
        return EmojiIndex.cellX(i) / (float) EmojiIndex.ATLAS_SIZE;
    }

    public static float v0(int i) {
        return EmojiIndex.cellY(i) / (float) EmojiIndex.ATLAS_SIZE;
    }

    public static float u1(int i) {
        return (EmojiIndex.cellX(i) + EmojiIndex.CELL) / (float) EmojiIndex.ATLAS_SIZE;
    }

    public static float v1(int i) {
        return (EmojiIndex.cellY(i) + EmojiIndex.CELL) / (float) EmojiIndex.ATLAS_SIZE;
    }
}
