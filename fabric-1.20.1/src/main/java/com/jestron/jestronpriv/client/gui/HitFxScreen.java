package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.client.cosmetic.HitFx;
import com.jestron.jestronpriv.hitfx.HitStyle;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Vurus ve kritik partikulu secimi. Ust butonla duzenlenen yuva (Vurus / Kritik) degisir; izgaradan tek
 * tikla stil secilir ve hemen onunde onizlenir.
 */
public final class HitFxScreen extends GridPickerScreen {

    private static boolean editCrit;
    private static boolean autoPreview = true;
    private final Map<HitStyle, ItemStack> icons = new EnumMap<>(HitStyle.class);
    private int idle;

    public HitFxScreen(Screen parent) {
        super(Component.literal("Vuruş Partikülleri"), parent);
        for (HitStyle s : HitStyle.values()) icons.put(s, new ItemStack(icon(s)));
    }

    private static Item icon(HitStyle s) {
        return switch (s) {
            case KOPUK -> Items.SLIME_BALL;
            case KELEBEK -> Items.BLUE_DYE;
            case KALP -> Items.POPPY;
            case ISILTI -> Items.GLOWSTONE_DUST;
            case YILDIZ -> Items.NETHER_STAR;
            case ALEV -> Items.BLAZE_POWDER;
            case RUH -> Items.SOUL_LANTERN;
            case KAR -> Items.SNOWBALL;
            case ELMAS -> Items.DIAMOND;
            case KAN -> Items.REDSTONE;
            case SIMSEK -> Items.LIGHTNING_ROD;
            case SAKURA -> Items.PINK_PETALS;
            case YAPRAK -> Items.OAK_LEAVES;
            case NOTA -> Items.NOTE_BLOCK;
            case GOKKUSAGI -> Items.PRISMARINE_SHARD;
            case ENDER -> Items.ENDER_PEARL;
            case PARA -> Items.GOLD_INGOT;
            case KAFATASI -> Items.WITHER_ROSE;
            case SOK -> Items.END_CRYSTAL;
            case NEON -> Items.GLOW_INK_SAC;
            case KONFETI -> Items.MAGENTA_DYE;
            case TUY -> Items.FEATHER;
            case HILAL -> Items.SEA_LANTERN;
            case PAPATYA -> Items.OXEYE_DAISY;
            case BUZ -> Items.BLUE_ICE;
            case GIRDAP -> Items.HEART_OF_THE_SEA;
            case TAC -> Items.GOLDEN_HELMET;
            case RUN -> Items.ENCHANTED_BOOK;
            case YARASA -> Items.BAT_SPAWN_EGG;
            case PATI -> Items.BONE;
            case YONCA -> Items.LIME_DYE;
            case KESIK -> Items.DIAMOND_SWORD;
            case VOLKAN -> Items.MAGMA_CREAM;
            case ZEHIR -> Items.POISONOUS_POTATO;
            case GALAKSI -> Items.ECHO_SHARD;
            case KARADELIK -> Items.OBSIDIAN;
            case BALON -> Items.PINK_DYE;
            case ZUMRUT -> Items.EMERALD;
            case EJDERATESI -> Items.DRAGON_BREATH;
            case BUYU -> Items.AMETHYST_SHARD;
            case PATLAMA -> Items.FIRE_CHARGE;
            case MUREKKEP -> Items.INK_SAC;
            case FISEK -> Items.FIREWORK_ROCKET;
            case PIKSEL -> Items.PAINTING;
            case OKYANUS -> Items.HEART_OF_THE_SEA;
            case KUM -> Items.SAND;
            case PLAZMA -> Items.BEACON;
            case GOLGE -> Items.ECHO_SHARD;
            case SEKER -> Items.CAKE;
            case ATES_BOCEGI -> Items.GLOW_BERRIES;
            default -> Items.IRON_SWORD;
        };
    }

    private ModConfig c() {
        return mod.config();
    }

    @Override
    protected String subtitle() {
        ModConfig c = c();
        return "§7Vuruş: §f" + HitStyle.parse(c.hitStyle).label + "   §7Kritik: §f" + HitStyle.parse(c.critStyle).label
                + "   §8· Düzenlenen: " + (editCrit ? "§eKritik" : "§aVuruş");
    }

    @Override
    protected void fill(List<Entry> out) {
        for (HitStyle s : HitStyle.values()) out.add(new Entry(s.name(), s.label, s.description));
    }

    @Override
    protected String selected() {
        return editCrit ? HitStyle.parse(c().critStyle).name() : HitStyle.parse(c().hitStyle).name();
    }

    @Override
    protected void select(String id) {
        HitStyle st = HitStyle.parse(id);
        if (editCrit) c().critStyle = st.name();
        else c().hitStyle = st.name();
        HitFx.preview(st, editCrit);
        idle = 0;
    }

    @Override
    protected int preferredCellW() {
        return 118;
    }

    @Override
    protected int preferredCellH() {
        return 24;
    }

    @Override
    protected void drawCell(GuiGraphics g, Entry e, int x, int y, int w, int h, boolean hover, boolean sel) {
        HitStyle st = HitStyle.parse(e.id());
        ItemStack icon = icons.get(st);
        if (icon != null) g.renderItem(icon, x + 3, y + (h - 16) / 2);
        // diger yuvada secili olan stil kucuk isaretle gosterilir
        ModConfig c = c();
        boolean other = (editCrit ? HitStyle.parse(c.hitStyle) : HitStyle.parse(c.critStyle)) == st && st != HitStyle.VANILLA;
        String name = (sel ? (editCrit ? "§e" : "§a") : "§f") + st.label;
        g.drawString(font, font.plainSubstrByWidth(name, w - 26 - (other ? 8 : 0)), x + 22, y + (h - 8) / 2, 0xFFFFFF);
        if (other) g.drawString(font, editCrit ? "§aV" : "§eK", x + w - 9, y + (h - 8) / 2, 0xFFFFFF);
    }

    @Override
    protected int buttonRows(int width) {
        return 2;
    }

    @Override
    protected void addButtons(int y, int left, int width) {
        ModConfig c = c();
        int w = (width - 12) / 4;
        addRenderableWidget(Button.builder(slotText(), b -> {
            editCrit = !editCrit;
            rebuildWidgets();
        }).bounds(left, y, w, 20).tooltip(Tooltip.create(Component.literal(
                "Izgaradan seçtiğin stil hangi vuruşa uygulanacak:\nVuruş = her isabet · Kritik = zıplayarak vurulan kritik."))).build());
        Slider amt = new Slider("Miktar", 0.25, 2.5, 0.05, () -> c.hitAmount, v -> c.hitAmount = (float) v).at(left + w + 4, y, w);
        amt.setTooltip(Tooltip.create(Component.literal("Partikül sayısı. Performans ayarındaki yoğunluk ve vanilla partikül ayarı da uygulanır.")));
        addRenderableWidget(amt);
        Slider size = new Slider("Boyut", 0.5, 2.0, 0.05, () -> c.hitSize, v -> c.hitSize = (float) v).at(left + (w + 4) * 2, y, w);
        size.setTooltip(Tooltip.create(Component.literal("Partikül büyüklüğü.")));
        addRenderableWidget(size);
        Slider vol = new Slider("Ses", 0, 2.0, 0.05, () -> c.hitSoundVolume, v -> c.hitSoundVolume = (float) v).at(left + (w + 4) * 3, y, w);
        vol.setTooltip(Tooltip.create(Component.literal("Vuruş / kritik sesinin seviyesi. Ses kanalı: Genel Ayarlar'daki efekt ses kanalı.")));
        addRenderableWidget(vol);
        int y2 = y + 24, w2 = (width - 74 - 16) / 4;
        addRenderableWidget(Button.builder(hideText(c), b -> {
            c.hitHideVanilla = !c.hitHideVanilla;
            b.setMessage(hideText(c));
            markDirty();
        }).bounds(left, y2, w2, 20).tooltip(Tooltip.create(Component.literal(
                "Özel stil seçiliyken oyunun kendi kritik ve büyü (keskinlik) yıldızlarını gizler."))).build());
        addRenderableWidget(Button.builder(mobText(c), b -> {
            c.hitOnMobs = !c.hitOnMobs;
            b.setMessage(mobText(c));
            markDirty();
        }).bounds(left + w2 + 4, y2, w2, 20).tooltip(Tooltip.create(Component.literal(
                "Açık: oyuncu ve moblar · Kapalı: sadece oyunculara vurunca."))).build());
        addRenderableWidget(Button.builder(soundText(c), b -> {
            c.hitSound = !c.hitSound;
            b.setMessage(soundText(c));
            markDirty();
        }).bounds(left + (w2 + 4) * 2, y2, w2, 20).tooltip(Tooltip.create(Component.literal(
                "Her stilin kendine özel vuruş tonu ve daha dolgun, katmanlı kritik tonu."))).build());
        addRenderableWidget(Button.builder(autoText(), b -> {
            autoPreview = !autoPreview;
            b.setMessage(autoText());
        }).bounds(left + (w2 + 4) * 3, y2, w2, 20).tooltip(Tooltip.create(Component.literal(
                "Açıkken seçili stil önünde sürekli patlar (sesiyle birlikte)."))).build());
    }

    private static Component slotText() {
        return Component.literal("Düzenle: " + (editCrit ? "§eKritik" : "§aVuruş"));
    }

    private static Component hideText(ModConfig c) {
        return Component.literal("Vanilla: " + (c.hitHideVanilla ? "§aGizli" : "§7Görünür"));
    }

    private static Component mobText(ModConfig c) {
        return Component.literal("Mob: " + (c.hitOnMobs ? "§aAçık" : "§cKapalı"));
    }

    private static Component soundText(ModConfig c) {
        return Component.literal("Ses: " + (c.hitSound ? "§aAçık" : "§cKapalı"));
    }

    private static Component autoText() {
        return Component.literal("Oto: " + (autoPreview ? "§aAçık" : "§cKapalı"));
    }

    @Override
    public void tick() {
        super.tick();
        if (!autoPreview || minecraft == null || minecraft.player == null) return;
        if (++idle >= 24) {
            idle = 0;
            ModConfig c = c();
            HitStyle st = HitStyle.parse(editCrit ? c.critStyle : c.hitStyle);
            HitFx.preview(st, editCrit);
        }
    }
}
