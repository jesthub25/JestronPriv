package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.cosmetic.Wings;
import com.jestron.jestronpriv.wings.WingAnimator;
import com.jestron.jestronpriv.wings.WingStyle;
import com.jestron.jestronpriv.wings.WingStyles;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 51 animasyonlu kanat. Sagda oyuncunun kendisi kanatlariyla canli doner; onizleme modu ile
 * dururken / yururken / kosarken / ucarken animasyonu denenebilir.
 */
public final class WingScreen extends GridPickerScreen {

    private static final WingAnimator.Mode[] MODES = {null, WingAnimator.Mode.IDLE, WingAnimator.Mode.WALK,
            WingAnimator.Mode.SPRINT, WingAnimator.Mode.FLY};
    private static final String[] MODE_NAMES = {"Canlı", "Dururken", "Yürürken", "Koşarken", "Uçarken"};

    private final Map<String, ItemStack> icons = new HashMap<>();
    private int modeIdx;

    public WingScreen(Screen parent) {
        super(Component.literal("Kanatlar"), parent);
        for (WingStyle s : WingStyles.ALL) icons.put(s.id(), new ItemStack(icon(s.id())));
    }

    private static Item icon(String id) {
        return switch (id) {
            case "melek" -> Items.FEATHER;
            case "kutsal" -> Items.TOTEM_OF_UNDYING;
            case "su" -> Items.WATER_BUCKET;
            case "ates" -> Items.BLAZE_POWDER;
            case "buz" -> Items.BLUE_ICE;
            case "seytan" -> Items.FIRE_CHARGE;
            case "ejderha" -> Items.DRAGON_BREATH;
            case "anka" -> Items.BLAZE_ROD;
            case "galaksi" -> Items.ENDER_EYE;
            case "aurora" -> Items.SEA_LANTERN;
            case "gokkusagi" -> Items.PRISMARINE_SHARD;
            case "altin" -> Items.GOLD_INGOT;
            case "elmas" -> Items.DIAMOND;
            case "zumrut" -> Items.EMERALD;
            case "lav" -> Items.MAGMA_CREAM;
            case "bosluk" -> Items.OBSIDIAN;
            case "kelebek_mavi" -> Items.BLUE_DYE;
            case "kelebek_monark" -> Items.ORANGE_DYE;
            case "sakura" -> Items.PINK_PETALS;
            case "neon" -> Items.GLOW_INK_SAC;
            case "matrix" -> Items.LIME_DYE;
            case "kamuflaj" -> Items.MOSS_BLOCK;
            case "zebra" -> Items.BLACK_WOOL;
            case "leopar" -> Items.RABBIT_HIDE;
            case "bal" -> Items.HONEYCOMB;
            case "vitray" -> Items.MAGENTA_STAINED_GLASS;
            case "sonbahar" -> Items.OAK_LEAVES;
            case "ender" -> Items.ENDER_PEARL;
            case "nether" -> Items.CRIMSON_FUNGUS;
            case "slime" -> Items.SLIME_BALL;
            case "ametist" -> Items.AMETHYST_SHARD;
            case "gul" -> Items.ROSE_BUSH;
            case "gece" -> Items.SPYGLASS;
            case "gunbatimi" -> Items.SUNFLOWER;
            case "okyanus" -> Items.HEART_OF_THE_SEA;
            case "toksik" -> Items.POISONOUS_POTATO;
            case "simsek" -> Items.LIGHTNING_ROD;
            case "kar" -> Items.SNOWBALL;
            case "kraliyet" -> Items.GOLDEN_HELMET;
            case "kristal_kalp" -> Items.POPPY;
            case "kutup" -> Items.WHITE_WOOL;
            case "seraf" -> Items.NETHER_STAR;
            case "kara_melek" -> Items.WITHER_ROSE;
            case "tavus" -> Items.PRISMARINE_CRYSTALS;
            case "buz_anka" -> Items.PACKED_ICE;
            case "ruh" -> Items.SOUL_LANTERN;
            case "plazma" -> Items.END_CRYSTAL;
            case "mekanik" -> Items.IRON_INGOT;
            case "kan_ejderi" -> Items.REDSTONE;
            case "kozmik_ejder" -> Items.DRAGON_EGG;
            case "luna" -> Items.LILY_PAD;
            default -> Items.ELYTRA;
        };
    }

    @Override
    protected String subtitle() {
        return "§7Dururken, yürürken, koşarken ve uçarken farklı animasyon · F5 ile kendine bak";
    }

    @Override
    protected void fill(List<Entry> out) {
        out.add(new Entry(ModConfig.NONE, "Kapalı", "Vanilla elytra / pelerin"));
        for (WingStyle s : WingStyles.ALL) out.add(new Entry(s.id(), s.name(), s.desc()));
    }

    @Override
    protected String selected() {
        return mod.config().wing;
    }

    @Override
    protected void select(String id) {
        mod.config().wing = id;
    }

    @Override
    protected int preferredCellW() {
        return 112;
    }

    @Override
    protected int preferredCellH() {
        return 24;
    }

    @Override
    protected int sidePanel(int screenWidth) {
        // genis ekranda canli onizleme; dar ekranda izgara tum alani kullanir
        return screenWidth >= 420 ? Math.min(200, screenWidth / 3) : 0;
    }

    @Override
    protected void drawCell(GuiGraphics g, Entry e, int x, int y, int w, int h, boolean hover, boolean sel) {
        ItemStack icon = icons.get(e.id());
        if (icon != null) g.renderItem(icon, x + 3, y + (h - 16) / 2);
        else g.renderItem(new ItemStack(Items.ELYTRA), x + 3, y + (h - 16) / 2);
        String name = font.plainSubstrByWidth(e.name(), w - 26);
        g.drawString(font, (sel ? "§d" : "§f") + name, x + 22, y + (h - 8) / 2, 0xFFFFFF);
    }

    @Override
    protected int buttonRows(int width) {
        return oneRow(width) ? 1 : 2;
    }

    private static boolean oneRow(int width) {
        return (width - 74 - 8) / 3 >= 100;
    }

    @Override
    protected void addButtons(int y, int left, int width) {
        ModConfig c = mod.config();
        int w, x1, x2, x3, y3, w3;
        if (oneRow(width)) {
            w = (width - 74 - 8) / 3;
            x1 = left;
            x2 = left + w + 4;
            x3 = left + (w + 4) * 2;
            y3 = y;
            w3 = w;
        } else {
            w = (width - 4) / 2;
            x1 = left;
            x2 = left + w + 4;
            x3 = left;
            y3 = y + 24;
            w3 = width - 74;
        }
        addRenderableWidget(Button.builder(modeText(), b -> {
            modeIdx = (modeIdx + 1) % MODES.length;
            Wings.preview = MODES[modeIdx];
            b.setMessage(modeText());
        }).bounds(x1, y, w, 20).tooltip(Tooltip.create(Component.literal(
                "Sağdaki önizlemede ve F5 görünümünde kanat animasyonunu dene. Ekrandan çıkınca gerçek hareketine döner."))).build());
        Slider size = new Slider("Boyut", 0.6, 1.4, 0.05, () -> c.wingScale, v -> c.wingScale = (float) v).at(x2, y, w);
        size.setTooltip(Tooltip.create(Component.literal("Kanat boyutu. 1x varsayılan.")));
        addRenderableWidget(size);
        addRenderableWidget(Button.builder(partText(c), b -> {
            c.wingParticles = !c.wingParticles;
            b.setMessage(partText(c));
            markDirty();
        }).bounds(x3, y3, w3, 20).tooltip(Tooltip.create(Component.literal(
                "Kanat uçlarından stile uygun parçacık (tüy ışıltısı, alev, kar, yaprak...). Uçarken artar."))).build());
    }

    private Component modeText() {
        return Component.literal("Önizleme: " + MODE_NAMES[modeIdx]);
    }

    private static Component partText(ModConfig c) {
        return Component.literal("Parçacık: " + (c.wingParticles ? "§aAçık" : "§cKapalı"));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        if (side > 0) renderPreview(g);
    }

    private void renderPreview(GuiGraphics g) {
        LocalPlayer pl = minecraft == null ? null : minecraft.player;
        int x0 = width - side + 4, x1 = width - 8, y0 = gridY, y1 = viewBottom;
        if (x1 - x0 < 60 || y1 - y0 < 60) return;
        g.fill(x0, y0, x1, y1, 0x70000000);
        g.renderOutline(x0, y0, x1 - x0, y1 - y0, 0x60B070FF);
        WingStyle st = Wings.selected();
        String label = st == null ? "§7Kanat seçilmedi" : "§d" + st.name();
        g.drawCenteredString(font, label, (x0 + x1) / 2, y0 + 4, 0xFFFFFF);
        if (pl == null) return;
        // arkadan capraz, yavasca salinan aci (kanatlar sirtta gorunsun)
        double t = (System.currentTimeMillis() % 16000L) / 16000.0 * Math.PI * 2;
        float rot = (float) (Math.sin(t) * 55.0);
        float fb = pl.yBodyRot, fy = pl.getYRot(), fx = pl.getXRot(), fh = pl.yHeadRot, fho = pl.yHeadRotO;
        try {
            pl.yBodyRot = rot;
            pl.setYRot(rot);
            pl.setXRot(0f);
            pl.yHeadRot = rot;
            pl.yHeadRotO = rot;
            float scale = Math.min((x1 - x0) / 3.4f, (y1 - y0 - 14) / 2.5f);
            Vector3f off = new Vector3f(0f, pl.getBbHeight() / 2f + 0.05f, 0f);
            Quaternionf q = new Quaternionf().rotateZ((float) Math.PI);
            g.enableScissor(x0 + 1, y0 + 14, x1 - 1, y1 - 1);
            try {
                InventoryScreen.renderEntityInInventory(g, (x0 + x1) / 2f, (y0 + 14 + y1) / 2f + 6, scale, off, q, null, pl);
            } finally {
                g.disableScissor();
            }
        } catch (Throwable ignored) {
            // onizleme hicbir zaman ekrani bozmasin
        } finally {
            pl.yBodyRot = fb;
            pl.setYRot(fy);
            pl.setXRot(fx);
            pl.yHeadRot = fh;
            pl.yHeadRotO = fho;
        }
    }

    @Override
    public void removed() {
        Wings.preview = null;
        super.removed();
    }
}
