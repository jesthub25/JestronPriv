package com.jestron.jestronpriv.client.cosmetic;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.hats.HatSettings;
import com.jestron.jestronpriv.tag.TagSettings;

/** Kendi secimlerin: ayarlardan uretilen sapka ve isim etiketi verisi (ayar degisince yenilenir). */
public final class LocalLook {

    private static HatSettings hat;
    private static TagSettings tag;
    private static boolean built;

    private LocalLook() {
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    /** Ayarlar degisti: yerel verileri yeniden uret. */
    public static void refresh() {
        ModConfig c = cfg();
        if (c == null) return;
        hat = buildHat(c);
        tag = buildTag(c);
        built = true;
    }

    /** Secili sapka (yoksa null). */
    public static HatSettings hat() {
        if (!built) refresh();
        return hat;
    }

    /** Etiket (kapaliysa null). */
    public static TagSettings tag() {
        if (!built) refresh();
        return tag;
    }

    private static HatSettings buildHat(ModConfig c) {
        if (c.hat == null || ModConfig.NONE.equals(c.hat)) return null;
        HatSettings h = new HatSettings();
        h.id = c.hat;
        h.scale = c.hatScale;
        h.height = c.hatHeight;
        h.tilt = c.hatTilt;
        h.speed = c.hatSpeed;
        h.glow = c.hatGlow;
        h.customColor = c.hatCustomColor;
        h.color = c.hatColor;
        h.rainbow = c.hatRainbow;
        h.hideHelmet = c.hatHideHelmet;
        h.particles = c.hatParticles;
        return h;
    }

    private static TagSettings buildTag(ModConfig c) {
        if (!c.tagEnabled) return null;
        TagSettings t = new TagSettings();
        t.text = c.tagText;
        t.font = c.tagFont;
        t.prefix = c.tagPrefix;
        t.suffix = c.tagSuffix;
        t.colorMode = c.tagColorMode;
        t.c1 = c.tagColor1;
        t.c2 = c.tagColor2;
        t.c3 = c.tagColor3;
        t.outlineColor = c.tagOutlineColor;
        t.bgColor = c.tagBgColor;
        t.anim = c.tagAnim;
        t.emojiAnim = c.tagEmojiAnim;
        t.flags = (c.tagBold ? TagSettings.F_BOLD : 0) | (c.tagItalic ? TagSettings.F_ITALIC : 0) | (c.tagUnderline ? TagSettings.F_UNDERLINE : 0)
                | (c.tagStrike ? TagSettings.F_STRIKE : 0) | (c.tagShadow ? TagSettings.F_SHADOW : 0) | (c.tagOutline ? TagSettings.F_OUTLINE : 0)
                | (c.tagGlow ? TagSettings.F_GLOW : 0);
        t.animSpeed = c.tagAnimSpeed;
        t.bgAlpha = c.tagBgAlpha;
        t.scale = c.tagScale;
        t.height = c.tagHeight;
        t.emojiScale = c.tagEmojiScale;
        return t;
    }
}
