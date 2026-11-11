package com.jestron.jestronpriv.fx.effects;

import com.jestron.jestronpriv.fx.CustomText;
import com.jestron.jestronpriv.fx.FxKit;
import com.jestron.jestronpriv.fx.FxSession;
import com.jestron.jestronpriv.fx.KillEffect;
import com.jestron.jestronpriv.fx.P;
import com.jestron.jestronpriv.fx.api.Color;
import com.jestron.jestronpriv.fx.api.Material;
import com.jestron.jestronpriv.fx.api.TextDisplay;
import com.jestron.jestronpriv.fx.api.TextStyle;

import static com.jestron.jestronpriv.fx.FxMath.*;

/** Kurbanin ustunde, menuden yazdigin yazi secdigin yazi tipi, renk ve animasyonla belirir. */
public final class CustomTextEffect extends KillEffect {

    public CustomTextEffect() {
        super("ozel_yazi", "&e&lÖzel Yazı", Material.OAK_SIGN, true,
                "Kurbanın üstünde kendi yazdığın yazı",
                "seçtiğin yazı tipi, renk ve animasyonla çıkar.",
                "Düzenlemek için: Efektler > Özel Yazı");
    }

    @Override
    public void play(FxSession s) {
        final String full = CustomText.resolve(s.victimName);
        final TextStyle st = CustomText.style();
        final float size = CustomText.size();
        final double h = CustomText.height();
        final CustomText.Anim anim = CustomText.anim();
        final double gy = s.groundDy(0, 0);

        final TextDisplay d = s.text(anim == CustomText.Anim.TYPE ? "" : full, st, item(v(0, gy + h, 0), q(), 0.001f));
        if (st.glow()) s.bright(d);
        else s.lit(d);

        switch (anim) {
            case POP -> {
                s.tfAt(2, d, pose(gy + h, size * 1.25), 3);
                s.tfAt(5, d, pose(gy + h, size), 3);
            }
            case RISE -> {
                s.tfAt(2, d, pose(gy + h - 0.8, size * 0.7), 1);
                s.anim(d, 3, 40, 2, t -> pose(gy + h - 0.8 + easeOut(phase(t, 3, 30)) * 0.9, size * (0.7 + 0.3 * easeOut(phase(t, 3, 12)))));
            }
            case TYPE -> {
                s.tfAt(2, d, pose(gy + h, size), 2);
                final int len = full.length();
                s.every(2, 22, 1, t -> {
                    int n = (int) Math.ceil(len * phase(t, 2, 20));
                    // Bicim kodunu ('§' + harf) bolmeden kes
                    if (n > 0 && n < len && full.charAt(n - 1) == '§') n++;
                    d.setText(full.substring(0, Math.min(len, n)));
                });
            }
            case PULSE -> {
                s.tfAt(2, d, pose(gy + h, size), 3);
                s.anim(d, 5, 40, 2, t -> pose(gy + h, size * (1 + 0.12 * Math.sin(t * 0.7))));
            }
            case SHAKE -> {
                s.tfAt(2, d, pose(gy + h, size * 1.1), 2);
                s.anim(d, 4, 16, 1, t -> {
                    double k = 1 - phase(t, 4, 16);
                    return item(v(Math.sin(t * 2.7) * 0.08 * k, gy + h + Math.cos(t * 3.3) * 0.05 * k, 0), q(), size);
                });
            }
            case ZOOM -> {
                s.tfAt(2, d, pose(gy + h, size * 3.2), 1);
                s.tfAt(3, d, pose(gy + h, size * 0.92), 3);
                s.tfAt(6, d, pose(gy + h, size), 2);
            }
        }
        // Yumusak kaybolus
        s.tfAt(FxKit.FADE_AT - 3, d, pose(gy + h, 0.001), 4);
        s.removeAt(FxKit.FADE_AT + 4, d);

        // Parcacik patlamasi (yazi rengiyle)
        if (CustomText.particles()) {
            final Color c = Color.fromRGB((st.color1() >> 16) & 0xFF, (st.color1() >> 8) & 0xFF, st.color1() & 0xFF);
            final Color c2 = Color.fromRGB((st.color2() >> 16) & 0xFF, (st.color2() >> 8) & 0xFF, st.color2() & 0xFF);
            final int burst = anim == CustomText.Anim.ZOOM ? 4 : anim == CustomText.Anim.TYPE ? 20 : 5;
            s.at(burst, () -> {
                s.dust(s.loc(0, gy + h + 0.1, 0), c, 1.3f, 24, 0.7);
                s.dustT(s.loc(0, gy + h + 0.1, 0), c, c2, 1.0f, 16, 0.9);
                s.particle(P.END_ROD, s.loc(0, gy + h, 0), 14, 0.5, 0.06);
                s.particle(P.FLASH, s.loc(0, gy + h, 0), 1, 0, 0);
            });
            s.every(burst + 2, 38, 4, t -> s.dust(s.loc(s.rd(-0.9, 0.9), gy + h + s.rd(-0.3, 0.4), s.rd(-0.3, 0.3)), c, 0.9f, 2, 0.05));
        }
        // Ses
        final int at = anim == CustomText.Anim.TYPE ? 2 : anim == CustomText.Anim.ZOOM ? 3 : 2;
        switch (CustomText.sfx()) {
            case DING -> s.at(at, () -> s.sound("entity.experience_orb.pickup", 1.0f, 0.9f));
            case LEVELUP -> s.at(at, () -> s.sound("entity.player.levelup", 0.9f, 1.0f));
            case VICTORY -> s.at(at, () -> s.sound("ui.toast.challenge_complete", 0.8f, 1.0f));
            case GONG -> s.at(at, () -> {
                s.sound("block.bell.use", 1.0f, 0.7f);
                s.sound("block.bell.resonate", 0.5f, 0.8f);
            });
            case FIREWORK -> s.at(at, () -> {
                s.sound("entity.firework_rocket.blast", 0.9f, 1.0f);
                s.sound("entity.firework_rocket.twinkle", 0.8f, 1.0f);
            });
            default -> { }
        }
        if (anim == CustomText.Anim.TYPE) s.every(3, 20, 2, t -> s.sound("block.note_block.hat", 0.5f, 1.6f));
        if (anim == CustomText.Anim.ZOOM) s.at(3, () -> s.sound("item.mace.smash_ground_heavy", 0.6f, 1.2f));
    }

    private static com.jestron.jestronpriv.fx.api.Transformation pose(double y, double scale) {
        return item(v(0, y, 0), q(), (float) Math.max(0.001, scale));
    }

    private static double phase(int t, int a, int b) {
        return clamp01((t - a) / (double) Math.max(1, b - a));
    }
}
