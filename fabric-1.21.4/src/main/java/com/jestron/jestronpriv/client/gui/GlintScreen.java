package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.look.Glint;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Locale;

/** Buyu parlamasi: esya ve zirh icin ayri renk modu, hiz, parlaklik, desen boyutu, egim. Ustte canli onizleme. */
public final class GlintScreen extends JpScreen {

    private static final int PREVIEW_H = 44;
    private final ItemStack[] preview;

    public GlintScreen(Screen parent) {
        super(Component.literal("Büyü Parlaması"), parent);
        ItemStack[] s = {new ItemStack(Items.NETHERITE_SWORD), new ItemStack(Items.DIAMOND_PICKAXE), new ItemStack(Items.BOW),
                new ItemStack(Items.TRIDENT), new ItemStack(Items.DIAMOND_CHESTPLATE), new ItemStack(Items.BOOK)};
        for (ItemStack st : s) {
            try {
                st.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            } catch (Throwable ignored) {
            }
        }
        preview = s;
    }

    @Override
    protected String subtitle() {
        ModConfig c = cfg();
        return c.glintEnabled ? "§aÖzel parlama açık §8· §7Eşya: §f" + Glint.modeName(c.glintItemMode) : "§7Kapalı: vanilla mor parlama";
    }

    @Override
    protected int topOffset() {
        return TOP + PREVIEW_H;
    }

    @Override
    protected Runnable resetAction() {
        return () -> cfg().resetGlint();
    }

    private static String mul(double v) {
        return String.format(Locale.ROOT, "%.2fx", v);
    }

    @Override
    protected void buildOptions() {
        ModConfig c = cfg();
        header("Büyü Parlaması");
        toggle("Özel parlama", "Kapalıyken oyunun kendi mor parlaması aynen kalır.", () -> c.glintEnabled, v -> c.glintEnabled = v);
        cycle("Eşya modu", "Elde, envanterde ve yerdeki eşyalarda.\nTek renk · İki renk · Akan gökkuşağı · Renk döngüsü · Vanilla mor",
                () -> "§e" + Glint.modeName(c.glintItemMode), () -> c.glintItemMode = Glint.next(c.glintItemMode, hasShiftDown()));
        color("Eşya rengi 1", null, () -> c.glintItemColor, v -> c.glintItemColor = v);
        color("Eşya rengi 2", "'İki renk' modunda ikinci renk.", () -> c.glintItemColor2, v -> c.glintItemColor2 = v);
        hex("#RRGGBB renk 1", v -> c.glintItemColor = v);
        hex("#RRGGBB renk 2", v -> c.glintItemColor2 = v);

        header("Zırh & Varlık");
        toggle("Eşyayla aynı", "Giyilen zırh, kalkan, trident parlaması eşya ayarını kullansın.", () -> c.glintArmorSame, v -> c.glintArmorSame = v);
        cycle("Zırh modu", "'Eşyayla aynı' kapalıyken.", () -> "§e" + Glint.modeName(c.glintArmorMode),
                () -> c.glintArmorMode = Glint.next(c.glintArmorMode, hasShiftDown()));
        color("Zırh rengi 1", null, () -> c.glintArmorColor, v -> c.glintArmorColor = v);
        color("Zırh rengi 2", null, () -> c.glintArmorColor2, v -> c.glintArmorColor2 = v);

        header("Hareket & Parlaklık");
        slider("Hız", "Parlamanın kayma hızı (oyunun kendi hız ayarıyla çarpılır).\n0 = durgun.", 0, 4, 0.05, () -> c.glintSpeed,
                v -> c.glintSpeed = (float) v, v -> v < 0.01 ? "durgun" : mul(v));
        slider("Parlaklık", "Oyunun kendi parlaklık ayarıyla çarpılır.", 0.1, 2, 0.05, () -> c.glintStrength, v -> c.glintStrength = (float) v,
                JpScreen::pct);
        slider("Desen boyutu", "Büyük = geniş, yumuşak çizgiler · Küçük = sık parıltı.", 0.4, 2.5, 0.05, () -> c.glintScale,
                v -> c.glintScale = (float) v, GlintScreen::mul);
        slider("Eğim", "Parlama çizgilerinin açısı. Vanilla 10°.", -45, 45, 1, () -> c.glintAngle, v -> c.glintAngle = (float) v,
                v -> String.format(Locale.ROOT, "%+.0f°", v));
        slider("Döngü hızı", "'Renk döngüsü' modunda renk değişim hızı.", 0.2, 4, 0.1, () -> c.glintCycleSpeed, v -> c.glintCycleSpeed = (float) v,
                GlintScreen::mul);
        toggle("Nabız", "Parlaklık yavaşça nefes alır.", () -> c.glintPulse, v -> c.glintPulse = v);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        int n = preview.length;
        int size = 32, gap = 10;
        int total = n * size + (n - 1) * gap;
        int x = width / 2 - total / 2, y = TOP - 2;
        g.fill(x - 8, y - 4, x + total + 8, y + size + 4, 0x50000000);
        for (int i = 0; i < n; i++) {
            g.pose().pushPose();
            g.pose().translate(x + i * (size + gap), y, 0);
            g.pose().scale(2f, 2f, 1f);
            g.renderItem(preview[i], 0, 0);
            g.pose().popPose();
        }
    }
}
