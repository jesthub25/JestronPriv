package com.jestron.jestronpriv.client.view;

import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;

/**
 * Birinci sahis el gorunumu (viewmodel): kolun konumu, esyanin konumu / boyutu / donusu, el FOV'u.
 * Tum degerler sag el icin girilir; sol elde x ve Y/Z donusleri aynalanir (iki el simetrik durur).
 * Degerler sifir / 1 iken hicbir donusum uygulanmaz: vanilla ile birebir ayni.
 */
public final class ViewModel {

    private ViewModel() {
    }

    private static ModConfig cfg() {
        JestronPrivClient m = JestronPrivClient.get();
        return m == null ? null : m.config();
    }

    private static boolean applies(ModConfig c, InteractionHand hand) {
        return c != null && c.vmEnabled && (hand == InteractionHand.MAIN_HAND || c.vmOffhand);
    }

    private static int side(AbstractClientPlayer player, InteractionHand hand) {
        HumanoidArm main = player.getMainArm();
        HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? main : main.getOpposite();
        return arm == HumanoidArm.RIGHT ? 1 : -1;
    }

    /** Sol eldeki esya gizlensin mi (kullanilirken — kalkan, yemek, totem degil — her zaman gorunur). */
    public static boolean hideOffhand(AbstractClientPlayer player, InteractionHand hand) {
        ModConfig c = cfg();
        if (c == null || !c.vmHideOffhand || hand != InteractionHand.OFF_HAND) return false;
        return !(player.isUsingItem() && player.getUsedItemHand() == InteractionHand.OFF_HAND);
    }

    /** Kol (bos el + esya) konumu ve hafif nefes salinimi. renderArmWithItem'in pushPose'undan hemen sonra. */
    public static void arm(PoseStack pose, AbstractClientPlayer player, InteractionHand hand, float partialTick) {
        ModConfig c = cfg();
        if (!applies(c, hand)) return;
        int i = side(player, hand);
        float x = c.vmHandX, y = c.vmHandY, z = c.vmHandZ;
        if (c.vmIdleSway && !player.isUsingItem()) {
            // dururken cok hafif nefes: iki el farkli fazda, yurumede bob zaten var
            float t = (player.tickCount + partialTick) / 20F;
            float ph = hand == InteractionHand.MAIN_HAND ? 0F : 1.7F;
            y += (float) Math.sin(t * 1.6F + ph) * 0.006F;
            x += (float) Math.sin(t * 0.8F + ph) * 0.004F;
        }
        if (x != 0F || y != 0F || z != 0F) pose.translate(i * x, y, z);
    }

    /** Esya konumu, donusu ve boyutu. Esya cizilmeden hemen once (kabza etrafinda). */
    public static void item(PoseStack pose, AbstractClientPlayer player, InteractionHand hand) {
        ModConfig c = cfg();
        if (!applies(c, hand)) return;
        int i = side(player, hand);
        if (c.vmItemX != 0F || c.vmItemY != 0F || c.vmItemZ != 0F) pose.translate(i * c.vmItemX, c.vmItemY, c.vmItemZ);
        if (c.vmRotY != 0F) pose.mulPose(Axis.YP.rotationDegrees(i * c.vmRotY));
        if (c.vmRotX != 0F) pose.mulPose(Axis.XP.rotationDegrees(c.vmRotX));
        if (c.vmRotZ != 0F) pose.mulPose(Axis.ZP.rotationDegrees(i * c.vmRotZ));
        float s = c.vmItemScale;
        if (s != 1F && s > 0F) pose.scale(s, s, s);
    }

    /** El cizimi FOV'u (vanilla 70). Oyun FOV'unu etkilemez. */
    public static float handFov(float vanilla) {
        ModConfig c = cfg();
        if (c == null || !c.vmEnabled || c.vmHandFov == 70F) return vanilla;
        return vanilla * (c.vmHandFov / 70F);
    }

    // ------------------------------------------------------------------ hazir ayarlar

    public static final String[] PRESETS = {"Varsayılan", "Küçük PvP", "Alçak", "Uzak", "Merkez", "Büyük", "Sinematik"};

    public static void applyPreset(ModConfig c, int idx) {
        c.vmHandX = c.vmHandY = c.vmHandZ = 0F;
        c.vmItemX = c.vmItemY = c.vmItemZ = 0F;
        c.vmRotX = c.vmRotY = c.vmRotZ = 0F;
        c.vmItemScale = 1F;
        c.vmHandFov = 70F;
        switch (idx) {
            case 1 -> {
                c.vmItemScale = 0.7F;
                c.vmHandX = 0.04F;
                c.vmHandY = -0.04F;
            }
            case 2 -> {
                c.vmHandY = -0.14F;
                c.vmHandZ = 0.02F;
            }
            case 3 -> {
                c.vmHandZ = -0.22F;
                c.vmItemScale = 0.9F;
            }
            case 4 -> {
                c.vmHandX = -0.26F;
                c.vmHandY = -0.04F;
                c.vmRotY = -10F;
            }
            case 5 -> c.vmItemScale = 1.35F;
            case 6 -> {
                c.vmHandX = 0.06F;
                c.vmHandY = -0.06F;
                c.vmHandZ = -0.06F;
                c.vmRotZ = 8F;
                c.vmHandFov = 60F;
            }
            default -> {
            }
        }
    }
}
