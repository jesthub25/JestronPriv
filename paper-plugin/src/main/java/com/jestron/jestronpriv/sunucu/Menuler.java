package com.jestron.jestronpriv.sunucu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Sandik menuleri: ana menu, aura listesi, elytra izi listesi, ayarlar. Menu, kendi {@link InventoryHolder}'i ile
 * taninir; menudeki her tiklama ve surukleme iptal edilir (esya alinamaz / konulamaz).
 * <p>
 * InventoryView hic kullanilmaz: 1.21'de sinif → arayuz oldu, 1.20.1'e gore derlenmis eklenti onu cagirirsa coker.
 */
public final class Menuler implements Listener {

    enum Tur {ANA, AURA, ELYTRA, AYAR}

    /** Menu kimligi: tur + sayfa. */
    static final class Holder implements InventoryHolder {
        final Tur tur;
        final int sayfa;
        Inventory inv;

        Holder(Tur tur, int sayfa) {
            this.tur = tur;
            this.sayfa = sayfa;
        }

        @Override
        public Inventory getInventory() {
            return inv;
        }
    }

    private static final int SAYFA = 45;
    private final JestronSunucu plugin;
    private Enchantment parilti;
    private boolean parlaktiBakildi;

    Menuler(JestronSunucu plugin) {
        this.plugin = plugin;
    }

    // ---------------------------------------------------------------- acma

    public void ana(Player p) {
        Holder h = new Holder(Tur.ANA, 0);
        Inventory inv = Bukkit.createInventory(h, 27, "§5✦ JestronPriv");
        h.inv = inv;
        cerceve(inv);
        Veri.Secim s = plugin.veri().bul(p.getUniqueId());
        Auralar.Aura a = s == null ? null : Auralar.get(s.aura);
        ElytraIzleri.Iz z = s == null ? null : ElytraIzleri.get(s.ely);
        inv.setItem(11, esya(Material.BLAZE_POWDER, "§6§lAuralar", a != null,
                "§7Etrafında dönen parçacık desenleri.", "§7Herkes görür, mod gerekmez.", "",
                "§7Seçili: " + (a == null ? "§8yok" : "§e" + a.name()), "", "§e▸ Tıkla: listeyi aç"));
        inv.setItem(13, esya(Material.ELYTRA, "§b§lElytra İzleri", z != null,
                "§7Süzülürken kanat uçlarından çıkan,", "§7modun 50 elytra görünümünün renginde iz.", "",
                "§7Seçili: " + (z == null ? "§8yok" : "§b" + z.name()), "", "§e▸ Tıkla: listeyi aç"));
        inv.setItem(15, esya(Material.COMPARATOR, "§a§lAyarlar", false,
                "§7Görünürlük ve hız ayarları.", "", "§e▸ Tıkla: aç"));
        inv.setItem(22, esya(Material.BARRIER, "§c§lHepsini Kapat", false,
                "§7Aurayı ve elytra izini kaldırır."));
        p.openInventory(inv);
    }

    public void auralar(Player p, int sayfa) {
        List<Auralar.Aura> list = Auralar.ALL;
        int sayfalar = (list.size() + SAYFA - 1) / SAYFA;
        sayfa = Math.max(0, Math.min(sayfalar - 1, sayfa));
        Holder h = new Holder(Tur.AURA, sayfa);
        Inventory inv = Bukkit.createInventory(h, 54, "§6✦ Auralar §8(" + (sayfa + 1) + "/" + sayfalar + ")");
        h.inv = inv;
        Veri.Secim s = plugin.veri().bul(p.getUniqueId());
        String secili = s == null ? null : s.aura;
        for (int i = 0; i < SAYFA; i++) {
            int k = sayfa * SAYFA + i;
            if (k >= list.size()) break;
            Auralar.Aura a = list.get(k);
            boolean sec = a.id().equals(secili);
            boolean izin = plugin.izinli(p, "aura", a.id());
            Material m = izin ? malzeme(a.icon(), Material.NETHER_STAR) : Material.GRAY_DYE;
            inv.setItem(i, esya(m, (sec ? "§a§l✔ " : izin ? "§e" : "§8") + a.name(), sec,
                    "§7" + a.desc(), "", sec ? "§a● Seçili §8(tekrar tıkla: kapat)" : izin ? "§e▸ Tıkla: seç" : "§c✖ Bu aura için iznin yok"));
        }
        alt(inv, sayfa, sayfalar, secili != null);
        p.openInventory(inv);
    }

    public void izler(Player p, int sayfa) {
        List<ElytraIzleri.Iz> list = ElytraIzleri.ALL;
        int sayfalar = (list.size() + SAYFA - 1) / SAYFA;
        sayfa = Math.max(0, Math.min(sayfalar - 1, sayfa));
        Holder h = new Holder(Tur.ELYTRA, sayfa);
        Inventory inv = Bukkit.createInventory(h, 54, "§b✦ Elytra İzleri §8(" + (sayfa + 1) + "/" + sayfalar + ")");
        h.inv = inv;
        Veri.Secim s = plugin.veri().bul(p.getUniqueId());
        String secili = s == null ? null : s.ely;
        for (int i = 0; i < SAYFA; i++) {
            int k = sayfa * SAYFA + i;
            if (k >= list.size()) break;
            ElytraIzleri.Iz z = list.get(k);
            boolean sec = z.id().equals(secili);
            boolean izin = plugin.izinli(p, "elytra", z.id());
            Material m = izin ? malzeme(boya(z.c2()) + "_DYE", Material.ELYTRA) : Material.GRAY_DYE;
            inv.setItem(i, esya(m, (sec ? "§a§l✔ " : izin ? "§b" : "§8") + z.name(), sec,
                    "§7" + z.desc(), "§8Renk: " + hex(z.c1()) + " " + hex(z.c2()) + " " + hex(z.c3()), "",
                    sec ? "§a● Seçili §8(tekrar tıkla: kapat)" : izin ? "§e▸ Tıkla: seç" : "§c✖ Bu iz için iznin yok"));
        }
        alt(inv, sayfa, sayfalar, secili != null);
        p.openInventory(inv);
    }

    public void ayarlar(Player p) {
        Holder h = new Holder(Tur.AYAR, 0);
        Inventory inv = Bukkit.createInventory(h, 27, "§a✦ Ayarlar");
        h.inv = inv;
        cerceve(inv);
        Veri.Secim s = plugin.veri().bul(p.getUniqueId());
        boolean gizle = s != null && s.gizle, yuz = s == null || s.yuzFiltre, kendi = s != null && s.kendiGizle;
        double hiz = s == null ? 1.0 : s.hiz;
        inv.setItem(10, esya(gizle ? Material.ENDER_EYE : Material.ENDER_PEARL, "§eBaşkalarının efektleri: " + (gizle ? "§cgizli" : "§agörünür"), false,
                "§7Diğer oyuncuların aura ve izlerini", "§7sen görmezsin (onlar görmeye devam eder).", "", "§e▸ Tıkla: değiştir"));
        inv.setItem(12, esya(yuz ? Material.LIME_DYE : Material.GRAY_DYE, "§eYüz filtresi: " + (yuz ? "§aaçık" : "§ckapalı"), false,
                "§7Kendi auranın kafa ve yüz hizasındaki", "§7parçacıkları sana gösterilmez,", "§7birinci şahısta görüş kapanmaz.", "", "§e▸ Tıkla: değiştir"));
        inv.setItem(14, esya(kendi ? Material.GLASS : Material.GLOWSTONE_DUST, "§eKendi efektlerim: " + (kendi ? "§cbana gizli" : "§agörünür"), false,
                "§7Açıkken kendi auranı sen görmezsin,", "§7başkaları görmeye devam eder.", "", "§e▸ Tıkla: değiştir"));
        inv.setItem(16, esya(Material.CLOCK, "§eAnimasyon hızı: §f" + hizYazi(hiz), false,
                "§7Auranın dönme / akma hızı.", "", "§e▸ Sol tık: artır  §8|  §e▸ Sağ tık: azalt"));
        inv.setItem(22, esya(Material.ARROW, "§7« Geri", false));
        p.openInventory(inv);
    }

    // ---------------------------------------------------------------- tiklama

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void tikla(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof Holder h)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;
        int slot = e.getRawSlot();
        if (slot < 0 || slot >= e.getInventory().getSize()) return;
        Veri.Secim s;
        switch (h.tur) {
            case ANA -> {
                if (slot == 11) auralar(p, 0);
                else if (slot == 13) izler(p, 0);
                else if (slot == 15) ayarlar(p);
                else if (slot == 22) {
                    plugin.secAura(p, null, true);
                    plugin.secIz(p, null, true);
                    ana(p);
                }
            }
            case AURA, ELYTRA -> {
                boolean aura = h.tur == Tur.AURA;
                int toplam = aura ? Auralar.ALL.size() : ElytraIzleri.ALL.size();
                if (slot < SAYFA) {
                    int k = h.sayfa * SAYFA + slot;
                    if (k >= toplam) return;
                    s = plugin.veri().bul(p.getUniqueId());
                    if (aura) {
                        String id = Auralar.ALL.get(k).id();
                        plugin.secAura(p, s != null && id.equals(s.aura) ? null : id, true);
                        auralar(p, h.sayfa);
                    } else {
                        String id = ElytraIzleri.ALL.get(k).id();
                        plugin.secIz(p, s != null && id.equals(s.ely) ? null : id, true);
                        izler(p, h.sayfa);
                    }
                } else if (slot == 45) {
                    if (aura) auralar(p, h.sayfa - 1);
                    else izler(p, h.sayfa - 1);
                } else if (slot == 53) {
                    if (aura) auralar(p, h.sayfa + 1);
                    else izler(p, h.sayfa + 1);
                } else if (slot == 49) ana(p);
                else if (slot == 51) {
                    if (aura) {
                        plugin.secAura(p, null, true);
                        auralar(p, h.sayfa);
                    } else {
                        plugin.secIz(p, null, true);
                        izler(p, h.sayfa);
                    }
                }
            }
            case AYAR -> {
                if (slot == 22) {
                    ana(p);
                    return;
                }
                if (slot != 10 && slot != 12 && slot != 14 && slot != 16) return;
                s = plugin.veri().al(p.getUniqueId());
                if (slot == 10) s.gizle = !s.gizle;
                else if (slot == 12) s.yuzFiltre = !s.yuzFiltre;
                else if (slot == 14) s.kendiGizle = !s.kendiGizle;
                else s.hiz = sonrakiHiz(s.hiz, !e.isRightClick());
                plugin.veri().degisti(p.getUniqueId());
                ayarlar(p);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void surukle(InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof Holder) e.setCancelled(true);
    }

    // ---------------------------------------------------------------- yardimci

    static final double[] HIZLAR = {0.5, 0.75, 1.0, 1.25, 1.5, 2.0};

    static double sonrakiHiz(double h, boolean artir) {
        int i = 0;
        for (int k = 0; k < HIZLAR.length; k++) if (Math.abs(HIZLAR[k] - h) < Math.abs(HIZLAR[i] - h)) i = k;
        i = artir ? Math.min(HIZLAR.length - 1, i + 1) : Math.max(0, i - 1);
        return HIZLAR[i];
    }

    static String hizYazi(double h) {
        return (h == Math.rint(h) ? String.valueOf((int) h) : String.valueOf(h)) + "x";
    }

    private void alt(Inventory inv, int sayfa, int sayfalar, boolean secili) {
        ItemStack cam = esya(Material.BLACK_STAINED_GLASS_PANE, " ", false);
        for (int i = 45; i < 54; i++) inv.setItem(i, cam);
        if (sayfa > 0) inv.setItem(45, esya(Material.ARROW, "§e« Önceki sayfa", false));
        if (sayfa < sayfalar - 1) inv.setItem(53, esya(Material.ARROW, "§eSonraki sayfa »", false));
        inv.setItem(49, esya(Material.OAK_DOOR, "§7« Ana menü", false));
        if (secili) inv.setItem(51, esya(Material.BARRIER, "§cKapat", false, "§7Seçimi kaldırır."));
    }

    private void cerceve(Inventory inv) {
        ItemStack cam = esya(Material.BLACK_STAINED_GLASS_PANE, " ", false);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, cam);
    }

    static Material malzeme(String ad, Material yedek) {
        Material m = ad == null ? null : Material.matchMaterial(ad);
        return m != null && m.isItem() && !m.isAir() ? m : yedek;
    }

    private Enchantment parilti() {
        if (!parlaktiBakildi) {
            parlaktiBakildi = true;
            // Sabit adi 1.20.5'te degisti (DURABILITY → UNBREAKING); anahtar ile bulunur
            try {
                parilti = Enchantment.getByKey(NamespacedKey.minecraft("unbreaking"));
            } catch (Throwable t) {
                parilti = null;
            }
        }
        return parilti;
    }

    ItemStack esya(Material m, String ad, boolean parla, String... aciklama) {
        ItemStack it = new ItemStack(m);
        ItemMeta meta = it.getItemMeta();
        if (meta == null) return it;
        meta.setDisplayName(ad);
        if (aciklama.length > 0) meta.setLore(new ArrayList<>(Arrays.asList(aciklama)));
        if (parla && parilti() != null) meta.addEnchant(parilti(), 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        it.setItemMeta(meta);
        return it;
    }

    private static String hex(int rgb) {
        return "§x" + String.format("%06X", rgb & 0xFFFFFF).chars().collect(StringBuilder::new, (b, c) -> b.append('§').append((char) c), StringBuilder::append) + "■";
    }

    /** Rengin en yakin boya adi (menu simgesi). */
    static String boya(int rgb) {
        String[] ad = {"WHITE", "ORANGE", "MAGENTA", "LIGHT_BLUE", "YELLOW", "LIME", "PINK", "GRAY", "LIGHT_GRAY", "CYAN", "PURPLE", "BLUE", "BROWN", "GREEN", "RED", "BLACK"};
        int[] renk = {0xF9FFFE, 0xF9801D, 0xC74EBD, 0x3AB3DA, 0xFED83D, 0x80C71F, 0xF38BAA, 0x474F52, 0x9D9D97, 0x169C9C, 0x8932B8, 0x3C44AA, 0x835432, 0x5E7C16, 0xB02E26, 0x1D1D21};
        int r = rgb >> 16 & 255, g = rgb >> 8 & 255, b = rgb & 255;
        int best = 0;
        long bd = Long.MAX_VALUE;
        for (int i = 0; i < renk.length; i++) {
            int dr = r - (renk[i] >> 16 & 255), dg = g - (renk[i] >> 8 & 255), db = b - (renk[i] & 255);
            long d = 2L * dr * dr + 4L * dg * dg + 3L * db * db;
            if (d < bd) {
                bd = d;
                best = i;
            }
        }
        return ad[best];
    }
}
