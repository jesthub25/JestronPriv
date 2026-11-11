package com.jestron.jestronpriv.client.gui;

import com.jestron.jestronpriv.client.ConfigProfiles;
import com.jestron.jestronpriv.client.ConfigProfiles.Category;
import com.jestron.jestronpriv.client.ConfigProfiles.Profile;
import com.jestron.jestronpriv.client.ModConfig;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Ayar profilleri: kaydet, yukle, paylas. Bolum secimi (kozmetik, vurus, oldurme, gorunum, HUD, performans) hem
 * kaydetmede hem paylasimda hem yuklemede gecerlidir. Her yuklemeden once otomatik yedek alinir (Geri Al).
 * Dosya birakma: .json profil ya da paylasim kodu iceren .txt ekrana surukle-birak ile ice aktarilir.
 */
public final class ProfilesScreen extends JpScreen {

    private static final Set<Category> CATS = EnumSet.allOf(Category.class);
    private static String pendingName = "";
    private static String selectedFile;

    private List<Profile> profiles = List.of();
    private String flash;
    private long flashUntil;
    /** Silme onayi bekleyen profil (ikinci tik siler; baska bir yere tiklayinca iptal). */
    private boolean deleteArmed;

    public ProfilesScreen(Screen parent) {
        super(Component.literal("Ayar Profilleri"), parent);
    }

    private void flash(String s) {
        flash = s;
        flashUntil = System.currentTimeMillis() + 4000;
    }

    @Override
    protected String subtitle() {
        if (flash != null && System.currentTimeMillis() < flashUntil) return flash;
        return "§7Ayarlarını kaydet, yükle, arkadaşınla paylaş §8· §7dosyayı buraya sürükleyip bırakabilirsin";
    }

    private Profile selected() {
        if (selectedFile == null) return null;
        for (Profile p : profiles) if (p.file() != null && p.file().getFileName().toString().equals(selectedFile)) return p;
        return null;
    }

    private void refresh() {
        profiles = ConfigProfiles.list();
        rebuildWidgets();
    }

    private Set<Category> cats() {
        return CATS.isEmpty() ? EnumSet.allOf(Category.class) : EnumSet.copyOf(CATS);
    }

    @Override
    protected void buildOptions() {
        ModConfig c = cfg();
        profiles = ConfigProfiles.list();

        header("Kaydet");
        textField("Profil adı (ör. PvP, Gösteriş)", "Kaydedilecek profilin adı. Aynı ad varsa üzerine yazılır.", false, 32, () -> pendingName,
                v -> pendingName = v);
        action("§a✔ Şu anki ayarları kaydet", "Seçili bölümlerdeki ayarlarını bu adla kaydeder.", false, () -> {
            String name = pendingName.isBlank() ? "Profil " + (profiles.size() + 1) : pendingName;
            try {
                Path f = ConfigProfiles.save(name, c, cats());
                selectedFile = f.getFileName().toString();
                pendingName = "";
                flash("§aKaydedildi: §f" + ConfigProfiles.cleanName(name) + " §8(" + ConfigProfiles.catsLabel(cats()) + ")");
            } catch (Exception e) {
                flash("§cKaydedilemedi: " + e.getClass().getSimpleName());
            }
            refresh();
        });

        header("Paylaş");
        action("§b⬆ Paylaşım kodunu kopyala", "Seçili bölümlerdeki ayarların kısa bir koda dönüşür ve panoya kopyalanır.\n"
                + "Discord'a vb. yapıştırıp gönder; arkadaşın 'Panodaki kodu yükle' ile alır.\n"
                + "§7Her şey dahil: çizdiğin eşya görünümleri, özel totem sesi, isim etiketi görselleri, menü teması.\n"
                + "§8Ses / görsel varsa kod uzun olur: o zaman profil dosyasını gönder (klasörü aç).", false, () -> {
            try {
                String code = ConfigProfiles.toCode(pendingName.isBlank() ? "Paylaşılan ayarlar" : pendingName, c, cats());
                minecraft.keyboardHandler.setClipboard(code);
                flash((code.length() > 60_000 ? "§eKod kopyalandı ama uzun §8(" : "§aKod panoya kopyalandı §8(") + sizeText(code.length()) + " · "
                        + ConfigProfiles.catsLabel(cats()) + ")" + (code.length() > 60_000 ? " §7· sohbete sığmazsa profil dosyasını gönder" : ""));
            } catch (Exception e) {
                flash("§cKopyalanamadı");
            }
        });
        action("§b⬇ Panodaki kodu içe aktar", "Panodaki paylaşım kodunu (ya da JSON'u) profil olarak ekler.\n"
                + "Ayarların hemen değişmez: listede seçip 'Yükle'ye bas.", false, () -> {
            String clip;
            try {
                clip = minecraft.keyboardHandler.getClipboard();
            } catch (Exception e) {
                clip = null;
            }
            Profile p = ConfigProfiles.fromCode(clip);
            if (p == null) {
                flash("§cPanoda geçerli bir JestronPriv kodu yok");
                return;
            }
            importProfile(p);
        });
        action("☰ Profil klasörünü aç", "Profil dosyaları: config/jestronpriv/profiles\nDosyaları buradan arkadaşına gönderebilirsin.", false, () -> {
            try {
                Path d = ConfigProfiles.dir();
                Files.createDirectories(d);
                Util.getPlatform().openPath(d);
            } catch (Exception e) {
                flash("§cKlasör açılamadı");
            }
        });
        Profile backup = ConfigProfiles.lastBackup();
        Button undo = action("↶ Son yüklemeyi geri al", "Son profil yüklemesinden önceki ayarlarına döner.\n§8(Tekrar basarsan geri alma da geri alınır.)",
                false, () -> {
                    Profile b = ConfigProfiles.lastBackup();
                    if (b == null) return;
                    load(b, EnumSet.allOf(Category.class), "§aÖnceki ayarlara dönüldü");
                });
        undo.active = backup != null;

        header("Bölümler §7(kaydet · paylaş · yükle)");
        for (Category cat : Category.values()) {
            toggle(cat.label, cat.desc, () -> CATS.contains(cat), v -> {
                if (v) CATS.add(cat);
                else if (CATS.size() > 1) CATS.remove(cat);
                else flash("§eEn az bir bölüm seçili kalmalı");
            });
        }

        Profile sel = selected();
        if (sel != null) {
            header("Seçili: §f" + sel.name());
            action("§a▶ Yükle", "Bu profili uygular (sadece seçili bölümler: " + ConfigProfiles.catsLabel(intersect(sel)) + ").\n"
                    + "§8Önce otomatik yedek alınır; 'Son yüklemeyi geri al' ile dönebilirsin.", false, () -> load(sel, intersect(sel), null));
            action("§e✎ Üzerine kaydet", "Bu profile şu anki ayarlarını (seçili bölümler) yazar.", false, () -> {
                try {
                    ConfigProfiles.overwrite(sel, c, cats());
                    flash("§aGüncellendi: §f" + sel.name());
                } catch (Exception e) {
                    flash("§cYazılamadı");
                }
                refresh();
            });
            action("§b⬆ Kodunu kopyala", "Bu profilin paylaşım kodunu panoya kopyalar.", false, () -> {
                String code = ConfigProfiles.codeOf(sel);
                minecraft.keyboardHandler.setClipboard(code);
                flash("§aKod panoya kopyalandı: §f" + sel.name() + " §8(" + sizeText(code.length()) + ")");
            });
            boolean armed = deleteArmed;
            action(armed ? "§c✖ Emin misin? Tekrar tıkla" : "§c✖ Sil", "Profili kalıcı olarak siler (iki kez tıkla).", false, () -> {
                if (deleteArmed) {
                    try {
                        ConfigProfiles.delete(sel);
                        flash("§eSilindi: §f" + sel.name());
                    } catch (Exception e) {
                        flash("§cSilinemedi");
                    }
                    selectedFile = null;
                    deleteArmed = false;
                } else {
                    deleteArmed = true;
                }
                refresh();
            });
        }

        header(profiles.isEmpty() ? "Henüz kayıtlı profil yok" : "Kayıtlı profiller (" + profiles.size() + ")");
        for (Profile p : profiles) {
            boolean on = p.file() != null && p.file().getFileName().toString().equals(selectedFile);
            String label = (on ? "§d▶ " : "§f") + p.name();
            String tip = (ConfigProfiles.isBackup(p) ? "§7Otomatik yedek\n" : "") + "§7Bölümler: §f" + ConfigProfiles.catsLabel(p.cats())
                    + (p.created() > 0 ? "\n§7Tarih: §f" + ConfigProfiles.date(p.created()) : "") + "\n§8Tıkla: seç";
            action(label, tip, false, () -> {
                selectedFile = p.file() == null ? null : p.file().getFileName().toString();
                deleteArmed = false;
                refresh();
            });
        }
    }

    private Set<Category> intersect(Profile p) {
        Set<Category> s = EnumSet.copyOf(cats());
        s.retainAll(p.cats());
        return s;
    }

    private void load(Profile p, Set<Category> which, String msg) {
        ModConfig c = cfg();
        if (which.isEmpty()) {
            flash("§eBu profilde seçili bölümlere ait ayar yok");
            return;
        }
        try {
            ConfigProfiles.backup(c);
            ConfigProfiles.Applied r = ConfigProfiles.apply(c, p.cfg(), which);
            c.save();
            com.jestron.jestronpriv.client.itemskin.ItemSkins.invalidate();
            com.jestron.jestronpriv.client.cosmetic.Capes.invalidate();
            markDirty();
            flash(msg != null ? msg : "§aYüklendi: §f" + p.name() + " §8(" + r.count() + " ayar · " + ConfigProfiles.catsLabel(r.cats()) + ")");
        } catch (Throwable t) {
            // beklenmeyen durum: eski ayarlara don, oyun asla durmasin
            Profile b = ConfigProfiles.lastBackup();
            if (b != null) {
                try {
                    ConfigProfiles.apply(c, b.cfg(), java.util.EnumSet.allOf(Category.class));
                    c.save();
                } catch (Throwable ignored) {
                }
            }
            flash("§cProfil yüklenemedi, ayarların korundu");
        }
        refresh();
    }

    private void importProfile(Profile p) {
        try {
            Path f = ConfigProfiles.store(p);
            selectedFile = f.getFileName().toString();
            flash("§aİçe aktarıldı: §f" + p.name() + " §8(" + ConfigProfiles.catsLabel(p.cats()) + ") §7· uygulamak için 'Yükle'");
        } catch (Exception e) {
            flash("§cİçe aktarılamadı");
        }
        refresh();
    }

    @Override
    public void onFilesDrop(List<Path> files) {
        int ok = 0, bad = 0;
        Profile last = null;
        for (Path f : files) {
            Profile p = ConfigProfiles.read(f);
            if (p == null) {
                bad++;
                continue;
            }
            try {
                Path saved = ConfigProfiles.store(p);
                selectedFile = saved.getFileName().toString();
                last = p;
                ok++;
            } catch (Exception e) {
                bad++;
            }
        }
        if (ok == 1 && bad == 0) flash("§aİçe aktarıldı: §f" + last.name() + " §7· uygulamak için 'Yükle'");
        else if (ok > 0) flash("§a" + ok + " profil içe aktarıldı" + (bad > 0 ? " §c· " + bad + " dosya geçersiz" : ""));
        else flash("§cGeçerli profil dosyası bulunamadı (.json ya da kod içeren .txt)");
        refresh();
    }

    private static String sizeText(int chars) {
        return chars < 10_000 ? chars + " karakter" : (chars < 1_000_000 ? (chars / 1000) + " KB" : String.format(java.util.Locale.ROOT, "%.1f MB", chars / 1e6));
    }
}
