package com.jestron.jestronpriv.client.view;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.FogType;

/**
 * FOV etkileri ayri ayri: kosma, hiz (iksir / yavaslik / ruh hizi), ucma, yay germe, su / lav. Vanilla tek bir
 * "FOV etkileri" ayari ile hepsini birlikte olcekler; burada her biri 0 - %150 arasi ayarlanir. Durbun aynen kalir.
 */
public final class FovControl {

    /** Vanilla kosma degistiricisi: hareket hizi toplamda x1.3. */
    static final float SPRINT_MUL = 1.3f;

    private FovControl() {
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    /** NaN = vanilla hesaplasin. */
    public static float modifier(AbstractClientPlayer p, boolean firstPerson) {
        ModConfig c = cfg();
        if (c == null || !c.fovCustom || p == null) return Float.NaN;
        boolean flying = p.getAbilities() != null && p.getAbilities().flying;
        float walk = p.getAbilities() == null ? 0f : p.getAbilities().getWalkingSpeed();
        float ratio = walk != 0f ? (float) p.getAttributeValue(Attributes.MOVEMENT_SPEED) / walk : 1f;
        float bow = -1f;
        if (p.isUsingItem()) {
            if (p.getUseItem().is(Items.BOW)) bow = Math.min(p.getTicksUsingItem() / 20f, 1f);
            else if (firstPerson && p.isScoping()) return 0.1f;
        }
        return compute(c, flying, walk != 0f, ratio, p.isSprinting(), bow);
    }

    /** Saf hesap (test edilebilir). bow: yay germe orani 0-1, -1 = yay yok. */
    static float compute(ModConfig c, boolean flying, boolean hasWalk, float ratio, boolean sprinting, float bow) {
        float f = 1f;
        if (flying) f *= scaled(c.fovFly, 1.1f);
        if (hasWalk && Float.isFinite(ratio)) {
            float noSprint = sprinting ? ratio / SPRINT_MUL : ratio;
            float all = (ratio + 1f) / 2f, base = (noSprint + 1f) / 2f;
            float sprintPart = base != 0f ? all / base : 1f;
            f *= scaled(c.fovSprint, sprintPart) * scaled(c.fovSpeed, base);
        }
        if (bow >= 0f) f *= scaled(c.fovBow, 1f - bow * bow * 0.15f);
        if (!Float.isFinite(f)) return 1f;
        return Math.max(0.1f, Math.min(1.5f, f));
    }

    /** 1 + (v - 1) * s : s = 0 etkisiz, 1 = vanilla, 1.5 = abartili. */
    static float scaled(float s, float v) {
        if (!Float.isFinite(s)) s = 1f;
        return 1f + (v - 1f) * s;
    }

    /** Su / lav altinda daralma (getFov sonucu uzerine). */
    public static float water(float fov, Camera cam) {
        ModConfig c = cfg();
        if (c == null || !c.fovCustom || cam == null) return fov;
        FogType ft = cam.getFluidInCamera();
        if (ft != FogType.WATER && ft != FogType.LAVA) return fov;
        Minecraft mc = Minecraft.getInstance();
        float vs = mc.options.fovEffectScale().get().floatValue();
        float vanilla = 1f + (0.85714287f - 1f) * vs, mine = 1f + (0.85714287f - 1f) * Math.max(0f, Math.min(1f, c.fovWater));
        if (vanilla <= 0.01f) return fov;
        return fov / vanilla * mine;
    }

    /** Gecis yumusakligi: vanilla tick basina farkin yarisini kapatir. */
    public static float smooth(float old, float cur) {
        ModConfig c = cfg();
        if (c == null || !c.fovCustom) return cur;
        float k = switch (c.fovSmooth) {
            case "ANINDA" -> 1f;
            case "YUMUSAK" -> 0.25f;
            case "COK_YUMUSAK" -> 0.12f;
            default -> 0.5f;
        };
        if (k == 0.5f) return cur;
        float target = old + (cur - old) * 2f;
        float v = old + (target - old) * k;
        return Float.isFinite(v) ? Math.max(0.1f, Math.min(1.5f, v)) : cur;
    }
}
