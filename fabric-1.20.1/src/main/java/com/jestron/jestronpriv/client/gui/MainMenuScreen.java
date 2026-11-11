package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.EffectScreen;
import com.jestron.jestronpriv.client.JestronPrivClient;
import com.jestron.jestronpriv.client.ModConfig;
import com.jestron.jestronpriv.fx.KillEffect;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Ana menu (varsayilan K). Ust seviyede kategoriler (Kozmetik, PvP, Oldurme & Totem, HUD & Esyalar, Gorunum &
 * Kamera, Ayarlar); kategoriye girince o bolumun kartlari. Her kart bir ayar ekranina girer; sag tik ile ozellik
 * hizlica acilip kapanir. Esc / Geri bir ust seviyeye doner.
 */
public final class MainMenuScreen extends Screen implements JpThemed {

    private static final int CARD_W = 170, GAP = 6;
    /** Kart yuksekligi ekrana gore ayarlanir; sigmazsa kart alani kaydirilir. */
    private int cardH = 44;

    private final JestronPrivClient mod;
    private final List<Card> cards = new ArrayList<>();
    private int gridX, baseY, scroll, maxScroll, viewTop, viewBottom;

    private record Card(ItemStack icon, String title, Supplier<String> status, Runnable open, Runnable quickToggle, String hint) {
    }

    /** Kategoriler. */
    enum Cat {
        KOZMETIK("Kozmetik", Items.ELYTRA, "kanat · elytra · pelerin · şapka · aura"),
        PVP("PvP", Items.NETHERITE_SWORD, "vuruş · nişangah · reach · ping · hitbox"),
        OLDURME("Öldürme & Totem", Items.TOTEM_OF_UNDYING, "öldürme efekti · özel yazı · totem"),
        HUD("HUD & Eşyalar", Items.CLOCK, "iksir · eşya · bilgi · envanter"),
        GORUNUM("Görünüm & Kamera", Items.SPYGLASS, "kamera · seçim kutusu · FOV · renk"),
        AYARLAR("Ayarlar", Items.COMPARATOR, "profiller · genel · performans · tuşlar");

        final String label, hint;
        final net.minecraft.world.item.Item icon;

        Cat(String label, net.minecraft.world.item.Item icon, String hint) {
            this.label = label;
            this.icon = icon;
            this.hint = hint;
        }
    }

    /** null = ust seviye (kategoriler). */
    private final Cat category;
    private final Screen parent;
    private final java.util.EnumMap<Cat, List<Card>> byCat = new java.util.EnumMap<>(Cat.class);

    public MainMenuScreen() {
        this(null, null);
    }

    private MainMenuScreen(Screen parent, Cat category) {
        super(Component.literal(category == null ? "JestronPriv" : "JestronPriv · " + category.label));
        this.mod = JestronPrivClient.get();
        this.parent = parent;
        this.category = category;
    }

    private void add(Cat cat, Card card) {
        byCat.computeIfAbsent(cat, k -> new ArrayList<>()).add(card);
    }

    @Override
    protected void init() {
        cards.clear();
        byCat.clear();
        buildCards();
        if (category == null) {
            for (Cat cat : Cat.values()) {
                List<Card> list = byCat.getOrDefault(cat, List.of());
                cards.add(new Card(new ItemStack(cat.icon), cat.label, () -> "§7" + list.size() + " bölüm", () -> minecraft.setScreen(new MainMenuScreen(this, cat)),
                        null, cat.hint));
            }
        } else {
            cards.addAll(byCat.getOrDefault(category, List.of()));
        }
        layout();
    }

    private void buildCards() {
        ModConfig c = mod.config();
        add(Cat.OLDURME, new Card(new ItemStack(Items.DIAMOND_SWORD), "Öldürme Efektleri", this::effectStatus,
                () -> minecraft.setScreen(new EffectScreen(mod, this)),
                () -> {
                    c.effect = ModConfig.NONE.equals(c.effect) ? "elmas_kilic" : ModConfig.NONE;
                    c.save();
                }, "50 efekt · seç, önizle"));
        add(Cat.PVP, new Card(new ItemStack(Items.NETHERITE_SWORD), "Vuruş Animasyonu",
                () -> c.swingEnabled ? "§aAçık §7· §d" + c.swingStyle().label : "§cKapalı",
                () -> minecraft.setScreen(new SwingScreen(this)),
                () -> {
                    c.swingEnabled = !c.swingEnabled;
                    c.save();
                }, "50 stil · el görünümü (viewmodel)"));
        add(Cat.OLDURME, new Card(new ItemStack(Items.OAK_SIGN), "Özel Yazı",
                () -> "ozel_yazi".equals(c.effect) ? "§aSeçili efekt" : "§7Yazını tasarla",
                () -> minecraft.setScreen(new TextEffectScreen(this)),
                () -> {
                    c.effect = "ozel_yazi";
                    c.save();
                }, "yazı tipi · renk · animasyon"));
        add(Cat.KOZMETIK, new Card(new ItemStack(Items.ELYTRA), "Kanatlar",
                () -> {
                    com.jestron.jestronpriv.wings.WingStyle w = com.jestron.jestronpriv.wings.WingStyles.get(c.wing);
                    return w == null ? "§cKapalı" : "§d" + w.name();
                },
                () -> minecraft.setScreen(new WingScreen(this)),
                () -> {
                    c.wing = ModConfig.NONE;
                    c.save();
                }, "51 kanat · gerçek kıvrımlı"));
        add(Cat.KOZMETIK, new Card(new ItemStack(Items.GOLDEN_HELMET), "Şapkalar",
                () -> {
                    com.jestron.jestronpriv.hats.HatStyle h = com.jestron.jestronpriv.hats.HatStyles.get(c.hat);
                    return h == null ? "§cKapalı" : "§d" + h.name();
                },
                () -> minecraft.setScreen(new HatScreen(this)),
                () -> {
                    c.hat = ModConfig.NONE;
                    c.save();
                    mod.applyConfig();
                }, "50 animasyonlu şapka"));
        add(Cat.KOZMETIK, new Card(new ItemStack(Items.DIAMOND_SWORD), "Eşya Görünümleri",
                () -> c.itemSkins.isEmpty() ? "§7Hiç özel eşya yok" : (c.itemSkinsEnabled ? "§d" : "§7") + c.itemSkins.size() + " eşya özel"
                        + (c.itemSkinsEnabled ? "" : " §c(kapalı)"),
                () -> minecraft.setScreen(new ItemSkinsScreen(this)),
                () -> {
                    c.itemSkinsEnabled = !c.itemSkinsEnabled;
                    c.save();
                }, "kendin çiz · PNG / paket yükle"));
        add(Cat.KOZMETIK, new Card(new ItemStack(Items.PHANTOM_MEMBRANE), "Elytra",
                () -> {
                    com.jestron.jestronpriv.cape.CapeDesign d = com.jestron.jestronpriv.cape.ElytraDesigns.get(c.ely);
                    return d == null ? "§cKapalı" : "§d" + d.name();
                },
                () -> minecraft.setScreen(new ElytraScreen(this)),
                () -> {
                    c.ely = ModConfig.NONE;
                    c.save();
                }, "50 animasyonlu görünüm"));
        add(Cat.KOZMETIK, new Card(new ItemStack(Items.RED_BANNER), "Pelerin",
                () -> {
                    com.jestron.jestronpriv.cape.CapeDesign d = com.jestron.jestronpriv.cape.CapeDesigns.get(c.cape);
                    return d == null ? "§cKapalı" : "§d" + d.name();
                },
                () -> minecraft.setScreen(new CapeScreen(this)),
                () -> {
                    c.cape = ModConfig.NONE;
                    c.save();
                    mod.applyConfig();
                }, "50 desen · rüzgar fiziği · ışıltı"));
        add(Cat.KOZMETIK, new Card(new ItemStack(Items.NAME_TAG), "İsim Etiketi",
                () -> c.tagEnabled ? "§a" + com.jestron.jestronpriv.tag.TagFonts.get(c.tagFont).name() + " §7· "
                        + com.jestron.jestronpriv.tag.TagStyle.COLOR_MODES[c.tagColorMode] : "§cKapalı",
                () -> minecraft.setScreen(new NametagScreen(this)),
                () -> {
                    c.tagEnabled = !c.tagEnabled;
                    c.save();
                    mod.applyConfig();
                }, "50 font · emoji · görsel"));
        add(Cat.KOZMETIK, new Card(new ItemStack(Items.BLAZE_POWDER), "Aura",
                () -> {
                    com.jestron.jestronpriv.client.cosmetic.Auras.Aura au = com.jestron.jestronpriv.client.cosmetic.Auras.get(c.aura);
                    return au == null ? "§cKapalı" : "§d" + au.name();
                },
                () -> minecraft.setScreen(new AuraScreen(this)),
                () -> {
                    c.aura = ModConfig.NONE;
                    c.save();
                }, "50 parçacık aurası"));
        int profileCount = com.jestron.jestronpriv.client.ConfigProfiles.count();
        String profileStatus = profileCount == 0 ? "§7Kaydet · paylaş" : "§a" + profileCount + " profil";
        add(Cat.AYARLAR, new Card(new ItemStack(Items.WRITABLE_BOOK), "Ayar Profilleri", () -> profileStatus,
                () -> minecraft.setScreen(new ProfilesScreen(this)),
                () -> minecraft.setScreen(new ProfilesScreen(this)), "kaydet · yükle · kodla paylaş"));
        add(Cat.GORUNUM, new Card(new ItemStack(Items.SPYGLASS), "Serbest Kamera",
                () -> c.freeCam ? "§aAçık §7· F5 arka görünüm" : "§cKapalı",
                () -> minecraft.setScreen(new CameraScreen(this)),
                () -> {
                    c.freeCam = !c.freeCam;
                    c.save();
                }, "3. şahısta bağımsız kamera"));
        add(Cat.PVP, new Card(new ItemStack(Items.GLOWSTONE_DUST), "Vuruş Partikülleri",
                () -> {
                    com.jestron.jestronpriv.hitfx.HitStyle h = com.jestron.jestronpriv.hitfx.HitStyle.parse(c.hitStyle);
                    com.jestron.jestronpriv.hitfx.HitStyle k = com.jestron.jestronpriv.hitfx.HitStyle.parse(c.critStyle);
                    if (h == com.jestron.jestronpriv.hitfx.HitStyle.VANILLA && k == com.jestron.jestronpriv.hitfx.HitStyle.VANILLA) return "§cKapalı §7(vanilla)";
                    return "§a" + h.label + " §7· Kritik §e" + k.label;
                },
                () -> minecraft.setScreen(new HitFxScreen(this)),
                () -> {
                    boolean off = com.jestron.jestronpriv.hitfx.HitStyle.parse(c.hitStyle) == com.jestron.jestronpriv.hitfx.HitStyle.VANILLA
                            && com.jestron.jestronpriv.hitfx.HitStyle.parse(c.critStyle) == com.jestron.jestronpriv.hitfx.HitStyle.VANILLA;
                    c.hitStyle = off ? "KOPUK" : "VANILLA";
                    c.critStyle = off ? "KELEBEK" : "VANILLA";
                    c.save();
                }, "50 vuruş + kritik partikülü"));
        add(Cat.PVP, new Card(new ItemStack(Items.MACE), "PvP Hissi",
                () -> {
                    int on = (c.dmgNumbers ? 1 : 0) + (c.combo ? 1 : 0) + (c.killStreak ? 1 : 0) + (c.hitColor ? 1 : 0);
                    return on == 0 ? "§cKapalı" : "§a" + on + "/4 açık §7· Kombo en iyi " + com.jestron.jestronpriv.client.pvp.Combo.best();
                },
                () -> minecraft.setScreen(new PvpScreen(this)),
                () -> {
                    boolean any = c.dmgNumbers || c.combo || c.killStreak;
                    c.dmgNumbers = !any;
                    c.combo = !any;
                    c.killStreak = !any;
                    c.save();
                }, "hasar · kombo · seri · vurulma rengi"));
        add(Cat.PVP, new Card(new ItemStack(Items.ENDER_EYE), "Nişangah",
                () -> c.chEnabled ? "§a" + com.jestron.jestronpriv.client.pvp.Crosshair.Shape.parse(c.chShape).label
                        + (c.chHitMarker ? " §7· isabet işareti" : "") : "§cKapalı §7(vanilla)",
                () -> minecraft.setScreen(new CrosshairScreen(this)),
                () -> {
                    c.chEnabled = !c.chEnabled;
                    c.save();
                }, "50 şekil · renk · isabet işareti"));
        add(Cat.OLDURME, new Card(new ItemStack(Items.TOTEM_OF_UNDYING), "Totem & Zaman",
                () -> (c.totemFx ? "§aTotem açık" : "§cTotem kapalı") + " §7· "
                        + com.jestron.jestronpriv.client.view.WorldTime.TimeMode.parse(c.timeMode).label + " / "
                        + com.jestron.jestronpriv.client.view.WorldTime.Weather.parse(c.weather).label,
                () -> minecraft.setScreen(new TotemTimeScreen(this)),
                () -> {
                    c.totemFx = !c.totemFx;
                    c.save();
                }, "totem efekti · saat · hava"));
        add(Cat.HUD, new Card(new ItemStack(Items.CLOCK), "HUD",
                () -> {
                    int on = (c.potHud ? 1 : 0) + (c.icHud ? 1 : 0) + (c.infoHud ? 1 : 0);
                    return on == 0 ? "§cKapalı" : "§a" + on + "/3 açık §7· sürükle-bırak";
                },
                () -> minecraft.setScreen(new HudScreen(this)),
                () -> minecraft.setScreen(new HudEditScreen(this)), "iksir · eşya sayacı · bilgi · konumlar"));
        add(Cat.HUD, new Card(new ItemStack(Items.CHEST), "Envanter & Eşyalar",
                () -> (c.invHud ? "§aEnvanter" : "§7Envanter") + " §7· " + (c.pkHud ? "§aBildirim" : "§7Bildirim") + " §7· "
                        + (c.giEnabled ? "§aEtiket" : "§7Etiket"),
                () -> minecraft.setScreen(new ItemsScreen(this)),
                () -> {
                    c.giEnabled = !c.giEnabled;
                    c.save();
                }, "envanter · alınan/atılan · yerdekiler"));
        add(Cat.KOZMETIK, new Card(new ItemStack(Items.AMETHYST_SHARD), "Evcil Yoldaş",
                () -> {
                    com.jestron.jestronpriv.pet.PetKind k = com.jestron.jestronpriv.pet.PetKind.parse(c.pet);
                    return k == null ? "§cKapalı" : "§a" + k.label + " §7· " + com.jestron.jestronpriv.pet.PetMode.parse(c.petMode).label;
                },
                () -> minecraft.setScreen(new PetScreen(this)),
                () -> {
                    com.jestron.jestronpriv.pet.PetKind k = com.jestron.jestronpriv.pet.PetKind.parse(c.pet);
                    com.jestron.jestronpriv.pet.PetKind[] all = com.jestron.jestronpriv.pet.PetKind.values();
                    c.pet = k == null ? all[0].name() : k.ordinal() + 1 < all.length ? all[k.ordinal() + 1].name() : ModConfig.NONE;
                    c.save();
                }, "50 yoldaş · renk · yer"));
        add(Cat.GORUNUM, new Card(new ItemStack(Items.GLASS), "Seçim Kutusu & FOV",
                () -> (c.boEnabled ? "§aÖzel kutu" : "§7Vanilla kutu") + " §7· " + (c.fovCustom ? "§aÖzel FOV" : "§7Vanilla FOV"),
                () -> minecraft.setScreen(new OutlineFovScreen(this)),
                () -> {
                    c.boEnabled = !c.boEnabled;
                    c.save();
                }, "renk · kalınlık · dolgu · FOV"));
        add(Cat.GORUNUM, new Card(new ItemStack(Items.PAINTING), "Ekran & Görüntü",
                () -> {
                    String a = com.jestron.jestronpriv.client.view.ScreenTweaks.Aspect.parse(c.aspect).label;
                    String col = c.cgEnabled ? "§a" + com.jestron.jestronpriv.client.view.ColorGrade.Preset.parse(c.cgPreset).label : "§7Renk kapalı";
                    return col + " §7· Oran " + a;
                },
                () -> minecraft.setScreen(new DisplayScreen(this)),
                () -> {
                    c.cgEnabled = !c.cgEnabled;
                    c.save();
                }, "50 renk filtresi · oran · zoom · ateş"));
        add(Cat.GORUNUM, new Card(new ItemStack(Items.ENCHANTED_BOOK), "Büyü Parlaması",
                () -> c.glintEnabled ? "§a" + com.jestron.jestronpriv.client.look.Glint.modeName(c.glintItemMode) : "§7Vanilla",
                () -> minecraft.setScreen(new GlintScreen(this)),
                () -> {
                    c.glintEnabled = !c.glintEnabled;
                    c.save();
                }, "renk · hız · parlaklık · zırh ayrı"));
        add(Cat.GORUNUM, new Card(new ItemStack(Items.NETHER_STAR), "Gökyüzü",
                () -> c.skyEnabled ? "§a" + (c.skyAurora ? "Kutup ışığı" : "Özel") + (c.skyShooting ? " §7· §aKayan yıldız" : "") : "§7Vanilla",
                () -> minecraft.setScreen(new SkyScreen(this)),
                () -> {
                    c.skyEnabled = !c.skyEnabled;
                    c.save();
                }, "kutup ışığı · kayan yıldız · gün batımı · ay"));
        add(Cat.GORUNUM, new Card(new ItemStack(Items.POWDER_SNOW_BUCKET), "Sis",
                () -> c.fogEnabled ? (c.fogOff ? "§aKapalı sis" : "§aMesafe " + Math.round(c.fogDistance * 100) + "%") : "§7Vanilla",
                () -> minecraft.setScreen(new FogScreen(this)),
                () -> {
                    c.fogEnabled = !c.fogEnabled;
                    c.save();
                }, "renk · mesafe · yumuşaklık · Nether"));
        add(Cat.AYARLAR, new Card(new ItemStack(Items.PAINTING), "Menü Teması",
                () -> "§f" + Theme.current().name() + " §7· saydamlık " + Math.round(c.menuOpacity * 100) + "%",
                () -> minecraft.setScreen(new ThemeScreen(this)),
                () -> {
                    c.menuTheme = com.jestron.jestronpriv.ui.GuiTheme.next(c.menuTheme, false).id();
                    c.save();
                }, "10 şeffaf tasarım + klasik"));
        add(Cat.AYARLAR, new Card(new ItemStack(Items.COMPARATOR), "Genel Ayarlar",
                () -> c.viewMode().label + " §7· Ses " + Math.round(c.volume * 100) + "%",
                () -> minecraft.setScreen(new GeneralScreen(this)), null, "gösterim · ses · mob"));
        add(Cat.AYARLAR, new Card(new ItemStack(Items.REDSTONE), "Performans",
                () -> "§7Parçacık " + Math.round(c.particleDensity * 100) + "% · " + c.effectRenderDistance + " blok",
                () -> minecraft.setScreen(new PerformanceScreen(this)), null, "FPS dostu ayarlar"));
        add(Cat.AYARLAR, new Card(new ItemStack(Items.TRIPWIRE_HOOK), "Tuş Ayarları",
                () -> "§7Menü tuşu: §f" + mod.menuKey().getTranslatedKeyMessage().getString(),
                () -> minecraft.setScreen(new KeyBindsScreen(this, minecraft.options)), null, "K tuşunu değiştir"));
        add(Cat.PVP, new Card(new ItemStack(Items.SPECTRAL_ARROW), "Reach",
                () -> c.reachHud ? (com.jestron.jestronpriv.client.pvp.Reach.count() > 0
                        ? String.format(java.util.Locale.ROOT, "§aAçık §7· son §f%.2f", com.jestron.jestronpriv.client.pvp.Reach.last()) : "§aAçık") : "§cKapalı",
                () -> minecraft.setScreen(new ReachScreen(this)),
                () -> {
                    c.reachHud = !c.reachHud;
                    c.save();
                }, "vuruş mesafesi · geçmiş · istatistik"));
        add(Cat.PVP, new Card(new ItemStack(Items.RECOVERY_COMPASS), "Ping",
                () -> {
                    if (!c.pingHud) return "§cKapalı";
                    int ms = com.jestron.jestronpriv.client.pvp.PingTracker.current();
                    return ms < 0 ? "§aAçık" : "§aAçık §7· §f" + ms + " ms";
                },
                () -> minecraft.setScreen(new PingScreen(this)),
                () -> {
                    c.pingHud = !c.pingHud;
                    c.save();
                }, "anlık ping · grafik · rakip pingi"));
        add(Cat.PVP, new Card(new ItemStack(Items.SKELETON_SKULL), "Öldürme Akışı",
                () -> c.kfHud ? "§aAçık §7· " + ("BENIM".equals(c.kfFilter) ? "benimkiler" : "OYUNCU".equals(c.kfFilter) ? "oyuncular" : "hepsi") : "§cKapalı",
                () -> minecraft.setScreen(new KillFeedScreen(this)),
                () -> {
                    c.kfHud = !c.kfHud;
                    c.save();
                }, "kim kimi neyle öldürdü"));
        add(Cat.PVP, new Card(new ItemStack(Items.TOTEM_OF_UNDYING), "Totem Bildirimleri",
                () -> c.tfHud ? "§aAçık §7· " + (c.tfRange == 0 ? "sınırsız" : c.tfRange + " blok") : "§cKapalı",
                () -> minecraft.setScreen(new TotemFeedScreen(this)),
                () -> {
                    c.tfHud = !c.tfHud;
                    c.save();
                }, "rakip totem patlatınca bildirim"));
        add(Cat.PVP, new Card(new ItemStack(Items.TARGET), "Hitbox",
                () -> c.hbEnabled ? "§aAçık" + (c.hbMobs ? " §7· oyuncu + mob" : " §7· oyuncular") : "§cKapalı",
                () -> minecraft.setScreen(new HitboxScreen(this)),
                () -> {
                    c.hbEnabled = !c.hbEnabled;
                    c.save();
                }, "kutu · göz hizası · bakış · renkler"));
        add(Cat.HUD, new Card(new ItemStack(Items.MAP), "HUD Düzenle",
                () -> "§7Sürükle · tekerlekle boyut", () -> minecraft.setScreen(new HudEditScreen(this)), null, "tüm panellerin yeri ve boyutu"));

    }

    private void layout() {
        int cols = cols();
        int rows = Math.max(1, (cards.size() + cols - 1) / cols);
        gridX = (width - (cols * CARD_W + (cols - 1) * GAP)) / 2;
        cardH = Math.max(30, Math.min(44, (height - 34 - 30) / rows - GAP));
        int contentH = rows * (cardH + GAP);
        viewTop = 30;
        viewBottom = height - 30;
        maxScroll = Math.max(0, contentH - (viewBottom - viewTop));
        scroll = Math.max(0, Math.min(scroll, maxScroll));
        baseY = maxScroll > 0 ? viewTop : Math.max(30, (height - contentH - 30) / 2 + 6);

        addRenderableWidget(Button.builder(Component.literal(category == null ? "Kapat" : "‹ Geri"), b -> onClose())
                .bounds(width / 2 - 75, Math.min(height - 26, baseY + contentH + 4), 150, 20).build());
    }

    private int cols() {
        if (width >= CARD_W * 3 + GAP * 2 + 20 && cards.size() >= 6) return 3;
        return width >= CARD_W * 2 + GAP + 20 ? 2 : 1;
    }

    private int cardY(int i) {
        return baseY - scroll + (i / cols()) * (cardH + GAP);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (maxScroll > 0) {
            scroll = (int) Math.max(0, Math.min(maxScroll, scroll - scrollY * (cardH + GAP) / 2));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private String effectStatus() {
        ModConfig c = mod.config();
        if (ModConfig.NONE.equals(c.effect)) return "§cKapalı";
        if (ModConfig.RANDOM.equals(c.effect)) return "§dRastgele";
        KillEffect fx = mod.engine().registry().get(c.effect);
        return fx == null ? "§7?" : "§a" + fx.plainName();
    }

    private int hovered(double mx, double my) {
        int cols = cols();
        for (int i = 0; i < cards.size(); i++) {
            int x = gridX + (i % cols) * (CARD_W + GAP), y = cardY(i);
            if (maxScroll > 0 && (my < viewTop || my >= viewBottom)) return -1;
            if (mx >= x && mx < x + CARD_W && my >= y && my < y + cardH) return i;
        }
        return -1;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(font, category == null ? Theme.title(null) : Theme.title(Component.literal(category.label).withStyle(net.minecraft.ChatFormatting.BOLD)),
                width / 2, Math.max(2, baseY - 24), 0xFFFFFF);
        g.drawCenteredString(font, (category == null ? "§7Bir kategori seç" : "§7Sol tık: §fAç   §7Sağ tık: §fHızlı aç/kapat   §7Esc: §fGeri")
                        + (maxScroll > 0 ? "   §8(kaydır)" : ""),
                width / 2, Math.max(13, baseY - 13), 0xFFFFFF);

        int cols = cols();
        int hov = hovered(mouseX, mouseY);
        if (maxScroll > 0) g.enableScissor(0, viewTop, width, viewBottom);
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            int x = gridX + (i % cols) * (CARD_W + GAP), y = cardY(i);
            if (y + cardH < viewTop - 2 && maxScroll > 0) continue;
            if (y > viewBottom && maxScroll > 0) continue;
            boolean h = i == hov;
            Theme.cell(g, x, y, CARD_W, cardH, false, h, true);
            g.renderItem(card.icon, x + 8, y + (cardH - 16) / 2);
            int ty = cardH >= 40 ? 7 : 4, line = cardH >= 40 ? 12 : 10;
            g.drawString(font, "§l" + card.title, x + 30, y + ty, 0xFFFFFF);
            g.drawString(font, font.plainSubstrByWidth(card.status.get(), CARD_W - 36), x + 30, y + ty + line, 0xFFFFFF);
            if (cardH >= 34) g.drawString(font, "§8" + card.hint, x + 30, y + ty + line * 2, 0xFFFFFF);
        }
        if (maxScroll > 0) {
            g.disableScissor();
            int trackH = viewBottom - viewTop;
            int barH = Math.max(12, trackH * trackH / (trackH + maxScroll));
            int barY = viewTop + (int) ((trackH - barH) * (scroll / (double) maxScroll));
            int sx = gridX + cols * CARD_W + (cols - 1) * GAP + 4;
            Theme.scrollbar(g, sx, viewTop, viewBottom, barY, barH);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int i = hovered(mouseX, mouseY);
        if (i >= 0 && minecraft != null) {
            Card card = cards.get(i);
            if (button == 0) {
                card.open.run();
                return true;
            }
            if (button == 1 && card.quickToggle != null) {
                card.quickToggle.run();
                mod.applyConfig();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
