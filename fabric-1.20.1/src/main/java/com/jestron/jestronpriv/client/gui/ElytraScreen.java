package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.cape.CapeDesign;
import com.jestron.jestronpriv.cape.ElytraDesigns;
import com.jestron.jestronpriv.cape.ElytraTexture;
import com.jestron.jestronpriv.client.cosmetic.Elytras;
import com.jestron.jestronpriv.cape.CapeTexture;
import com.jestron.jestronpriv.client.ModConfig;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Locale;

/**
 * 50 elytra gorunumu: izgarada kucuk resimler (iki kanat), sagda oyuncu arkadan, elytrasi acilip kapanirken (elytra
 * giyili olmasa da onizlemede gorunur).
 */
public final class ElytraScreen extends GridPickerScreen {

    private static final ResourceLocation THUMBS = new ResourceLocation("jestronpriv", "dynamic/elytra_thumbs");
    private static final int TW = ElytraTexture.THUMB_W, TH = ElytraTexture.THUMB_H, PER_ROW = 10;
    private DynamicTexture thumbs;
    private int atlasW, atlasH;
    private String thumbKey;
    private static final ItemStack OFF = new ItemStack(Items.BARRIER);

    public ElytraScreen(Screen parent) {
        super(Component.literal("Elytra Görünümleri"), parent);
    }

    @Override
    protected String subtitle() {
        if (com.jestron.jestronpriv.wings.WingStyles.get(mod.config().wing) != null)
            return "§eKanat seçili: elytra yerine kanat görünür §7(Kanatlar › Kapalı)";
        return "§7Elytra giyince görünür · süzülürken parçacık · F5 ile arkana bak";
    }

    @Override
    protected void init() {
        super.init();
        buildThumbs();
    }

    private String key(ModConfig c) {
        return String.format(Locale.ROOT, "%.0f|%b", c.elyHue, c.elyTrim);
    }

    /** Tum desenlerin kucuk resimleri tek dokuda (ekran acilinca bir kez, ~50 ms). */
    private void buildThumbs() {
        ModConfig c = mod.config();
        String k = key(c);
        if (thumbs != null && k.equals(thumbKey)) return;
        releaseThumbs();
        try {
            int n = ElytraDesigns.ALL.size();
            atlasW = PER_ROW * TW;
            atlasH = ((n + PER_ROW - 1) / PER_ROW) * TH;
            DynamicTexture tex = new DynamicTexture(atlasW, atlasH, false);
            NativeImage img = tex.getPixels();
            if (img == null) return;
            ElytraTexture gen = new ElytraTexture();
            CapeTexture.Options o = new CapeTexture.Options();
            o.hue = c.elyHue;
            o.trim = c.elyTrim;
            o.glowOn = false;
            int[] px = new int[TW * TH];
            for (int i = 0; i < n; i++) {
                gen.thumbnail(ElytraDesigns.ALL.get(i), o, 1.5f, px);
                int ox = (i % PER_ROW) * TW, oy = (i / PER_ROW) * TH;
                for (int y = 0; y < TH; y++) for (int x = 0; x < TW; x++) img.setPixel(ox + x, oy + y, px[y * TW + x]);
            }
            tex.upload();
            minecraft.getTextureManager().register(THUMBS, tex);
            thumbs = tex;
            thumbKey = k;
        } catch (Throwable t) {
            thumbs = null;
        }
    }

    private void releaseThumbs() {
        if (thumbs != null) {
            try {
                minecraft.getTextureManager().release(THUMBS);
            } catch (Throwable ignored) {
            }
            thumbs = null;
            thumbKey = null;
        }
    }

    @Override
    public void removed() {
        releaseThumbs();
        super.removed();
    }

    @Override
    protected void fill(List<Entry> out) {
        out.add(new Entry(ModConfig.NONE, "Kapalı", "Vanilla elytra dokusu"));
        for (CapeDesign d : ElytraDesigns.ALL)
            out.add(new Entry(d.id(), d.name(), d.desc() + (d.animated() ? " §8· §bcanlı" : "") + (d.particle() != null ? " §8· §eparçacık" : "")));
    }

    @Override
    protected String selected() {
        return mod.config().ely;
    }

    @Override
    protected void select(String id) {
        mod.config().ely = id;
        Elytras.invalidate();
    }

    @Override
    protected int preferredCellW() {
        return 116;
    }

    @Override
    protected int preferredCellH() {
        return 28;
    }

    @Override
    protected int sidePanel(int screenWidth) {
        return screenWidth >= 420 ? Math.min(200, screenWidth / 3) : 0;
    }

    @Override
    protected void drawCell(GuiGraphics g, Entry e, int x, int y, int w, int h, boolean hover, boolean sel) {
        int i = ElytraDesigns.indexOf(e.id());
        int iw = 22, ih = 22;
        int iy = y + (h - ih) / 2;
        if (i < 0 || thumbs == null) {
            g.renderItem(OFF, x + 2, y + (h - 16) / 2);
        } else {
            int u = (i % PER_ROW) * TW, v = (i / PER_ROW) * TH;
            g.blit(RenderType::guiTextured, THUMBS, x + 2, iy, u, v, iw, ih, TW, TH, atlasW, atlasH);
        }
        String name = font.plainSubstrByWidth(e.name(), w - 30);
        g.drawString(font, (sel ? "§d" : "§f") + name, x + 27, y + (h - 8) / 2, 0xFFFFFF);
    }

    @Override
    protected void addButtons(int y, int left, int width) {
        ModConfig c = mod.config();
        int w = (width - 74 - 8) / 3;
        if (w < 90) {
            w = (width - 74 - 4) / 2;
            addRenderableWidget(Button.builder(Component.literal("⚙ Ayarlar"), b -> minecraft.setScreen(new ElytraSettingsScreen(this)))
                    .bounds(left, y, w, 20).build());
            addRenderableWidget(glowButton(c).bounds(left + w + 4, y, w, 20).build());
            return;
        }
        addRenderableWidget(Button.builder(Component.literal("⚙ Elytra Ayarları"), b -> minecraft.setScreen(new ElytraSettingsScreen(this)))
                .bounds(left, y, w, 20).tooltip(Tooltip.create(Component.literal("Hız, renk tonu, gökkuşağı, ışıltı, iç yüz, parçacık...")))
                .build());
        addRenderableWidget(glowButton(c).bounds(left + w + 4, y, w, 20).build());
        addRenderableWidget(Button.builder(particleText(c), b -> {
            c.elyParticles = !c.elyParticles;
            b.setMessage(particleText(c));
            markDirty();
        }).bounds(left + (w + 4) * 2, y, w, 20).tooltip(Tooltip.create(Component.literal("Kanat uçlarından desene uygun parçacık; süzülürken daha çok.")))
                .build());
    }

    private Button.Builder glowButton(ModConfig c) {
        return Button.builder(glowText(c), b -> {
            c.elyGlow = !c.elyGlow;
            b.setMessage(glowText(c));
            markDirty();
        }).tooltip(Tooltip.create(Component.literal("Desenin parlayan kısımları karanlıkta ışıldar.")));
    }

    private static Component glowText(ModConfig c) {
        return Component.literal("Işıltı: " + (c.elyGlow ? "§aAçık" : "§cKapalı"));
    }

    private static Component particleText(ModConfig c) {
        return Component.literal("Parçacık: " + (c.elyParticles ? "§aAçık" : "§cKapalı"));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        if (side > 0) renderPreview(g);
    }

    private void renderPreview(GuiGraphics g) {
        LocalPlayer pl = minecraft == null ? null : minecraft.player;
        int x0 = width - side + 4, x1 = width - 8, y0 = gridY, y1 = viewBottom;
        if (x1 - x0 < 60 || y1 - y0 < 70) return;
        Theme.panel(g, x0, y0, x1, y1);
        CapeDesign d = ElytraDesigns.get(mod.config().ely);
        g.drawCenteredString(font, d == null ? "§7Elytra görünümü seçilmedi" : "§d" + d.name(), (x0 + x1) / 2, y0 + 4, 0xFFFFFF);
        if (pl == null || d == null) return;
        // arkadan, yavasca iki yana salinan aci: elytra tam gorunur, kanatlar acilip kapanir
        double t = (System.currentTimeMillis() % 12000L) / 12000.0 * Math.PI * 2;
        float rot = (float) (Math.sin(t) * 55.0);
        float fb = pl.yBodyRot, fy = pl.getYRot(), fx = pl.getXRot(), fh = pl.yHeadRot, fho = pl.yHeadRotO;
        try {
            pl.yBodyRot = rot;
            pl.setYRot(rot);
            pl.setXRot(5f);
            pl.yHeadRot = rot;
            pl.yHeadRotO = rot;
            float scale = Math.min((x1 - x0) / 1.5f, (y1 - y0 - 18) / 2.3f);
            Vector3f off = new Vector3f(0f, pl.getBbHeight() / 2f + 0.05f, 0f);
            Quaternionf q = new Quaternionf().rotateZ((float) Math.PI);
            g.enableScissor(x0 + 1, y0 + 14, x1 - 1, y1 - 1);
            Elytras.beginPreview(null);
            try {
                InventoryScreen.renderEntityInInventory(g, (x0 + x1) / 2f, (y0 + 14 + y1) / 2f + 4, scale, off, q, null, pl);
            } finally {
                Elytras.endPreview();
                g.disableScissor();
            }
        } catch (Throwable ignored) {
        } finally {
            pl.yBodyRot = fb;
            pl.setYRot(fy);
            pl.setXRot(fx);
            pl.yHeadRot = fh;
            pl.yHeadRotO = fho;
        }
    }
}
