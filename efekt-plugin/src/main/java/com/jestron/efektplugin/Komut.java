package com.jestron.efektplugin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Her komut ayri (alt komut yok):
 * /efekt (menu) · /aura [ad|kapat] · /elytra [ad|kapat] · /efektgizle · /efektayar · /efektkapat · /efektliste [aura|elytra]
 * · /efektver &lt;oyuncu&gt; &lt;aura|elytra&gt; &lt;ad|kapat&gt; · /efektyenile
 */
final class Komut implements CommandExecutor, TabCompleter {

    /** plugin.yml'deki komut adlari. */
    static final String[] KOMUTLAR = {"efekt", "aura", "elytra", "efektgizle", "efektayar", "efektkapat", "efektliste", "efektver", "efektyenile"};

    private static final String ONEK = EfektPlugin.ONEK;
    private static final String ADMIN = "efektplugin.admin";
    private static final String USE = "efektplugin.use";
    private final EfektPlugin plugin;

    Komut(EfektPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String kmt = command.getName().toLowerCase(Locale.ROOT);
        switch (kmt) {
            case "efektyenile" -> {
                if (!sender.hasPermission(ADMIN)) return izinYok(sender);
                plugin.ayarlariOku();
                sender.sendMessage(ONEK + "Ayarlar yeniden okundu.");
                return true;
            }
            case "efektver" -> {
                if (!sender.hasPermission(ADMIN)) return izinYok(sender);
                ver(sender, label, args);
                return true;
            }
            case "efektliste" -> {
                liste(sender, args.length > 0 && args[0].toLowerCase(Locale.ROOT).startsWith("e"));
                return true;
            }
            default -> {
            }
        }
        if (!(sender instanceof Player p)) {
            sender.sendMessage(ONEK + "Bu komut oyun içinden kullanılır. Konsoldan: §f/efektver§7, §f/efektliste§7, §f/efektyenile");
            return true;
        }
        if (!p.hasPermission(USE)) return izinYok(p);
        switch (kmt) {
            case "aura" -> {
                if (args.length == 0) plugin.menuler().auralar(p, 0);
                else plugin.secAura(p, kapat(args[0]) ? null : args[0].toLowerCase(Locale.ROOT), true);
            }
            case "elytra" -> {
                if (args.length == 0) plugin.menuler().izler(p, 0);
                else plugin.secIz(p, kapat(args[0]) ? null : args[0].toLowerCase(Locale.ROOT), true);
            }
            case "efektgizle" -> {
                Veri.Secim s = plugin.veri().al(p.getUniqueId());
                s.gizle = !s.gizle;
                plugin.veri().degisti(p.getUniqueId());
                p.sendMessage(ONEK + "Başkalarının efektleri: " + (s.gizle ? "§cgizli" : "§agörünür"));
            }
            case "efektayar" -> plugin.menuler().ayarlar(p);
            case "efektkapat" -> {
                plugin.secAura(p, null, false);
                plugin.secIz(p, null, false);
                p.sendMessage(ONEK + "Aura ve elytra izi kapatıldı.");
            }
            default -> {
                if (args.length > 0 && (args[0].equalsIgnoreCase("yardim") || args[0].equalsIgnoreCase("help"))) yardim(p);
                else plugin.menuler().ana(p);
            }
        }
        return true;
    }

    private void ver(CommandSender sender, String label, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ONEK + "§eKullanım: §f/" + label + " <oyuncu> <aura|elytra> <ad|kapat>");
            return;
        }
        Player hedef = Bukkit.getPlayerExact(args[0]);
        if (hedef == null) {
            sender.sendMessage(ONEK + "§cOyuncu çevrimiçi değil: §f" + args[0]);
            return;
        }
        String id = kapat(args[2]) ? null : args[2].toLowerCase(Locale.ROOT);
        boolean ok;
        if (args[1].equalsIgnoreCase("aura")) ok = (id == null || Auralar.get(id) != null) && plugin.secAura(hedef, id, false);
        else if (args[1].equalsIgnoreCase("elytra")) ok = (id == null || ElytraIzleri.get(id) != null) && plugin.secIz(hedef, id, false);
        else {
            sender.sendMessage(ONEK + "§cTür §faura §cya da §felytra §colmalı.");
            return;
        }
        sender.sendMessage(ok ? ONEK + "§a" + hedef.getName() + " için ayarlandı." : ONEK + "§cBöyle bir öğe yok ya da oyuncunun izni yok: §f" + args[2]);
    }

    private static void liste(CommandSender sender, boolean ely) {
        StringBuilder b = new StringBuilder(ONEK).append(ely ? "§bElytra izleri §8(" + ElytraIzleri.ALL.size() + ")§7: " : "§6Auralar §8(" + Auralar.ALL.size() + ")§7: ");
        boolean ilk = true;
        if (ely) for (ElytraIzleri.Iz z : ElytraIzleri.ALL) {
            b.append(ilk ? "" : "§8, ").append("§f").append(z.id());
            ilk = false;
        }
        else for (Auralar.Aura a : Auralar.ALL) {
            b.append(ilk ? "" : "§8, ").append("§f").append(a.id());
            ilk = false;
        }
        sender.sendMessage(b.toString());
    }

    private static boolean kapat(String s) {
        String k = s.toLowerCase(Locale.ROOT);
        return k.equals("kapat") || k.equals("off") || k.equals("yok") || k.equals("none");
    }

    private static boolean izinYok(CommandSender s) {
        s.sendMessage(ONEK + "§cBunun için iznin yok.");
        return true;
    }

    private static void yardim(CommandSender s) {
        s.sendMessage(ONEK + "§fKomutlar:");
        s.sendMessage("§e/efekt §7— menüyü açar");
        s.sendMessage("§e/aura <ad|kapat> §7— aura seçer (adsız: liste)");
        s.sendMessage("§e/elytra <ad|kapat> §7— elytra izi seçer (adsız: liste)");
        s.sendMessage("§e/efektgizle §7— başkalarının efektlerini gizler / gösterir");
        s.sendMessage("§e/efektayar §7— görünürlük ve hız ayarları");
        s.sendMessage("§e/efektkapat §7— hepsini kapatır");
        s.sendMessage("§e/efektliste [aura|elytra] §7— kimlikleri listeler");
        if (s.hasPermission(ADMIN)) {
            s.sendMessage("§c/efektver <oyuncu> <aura|elytra> <ad|kapat> §7— başkasına ayarlar");
            s.sendMessage("§c/efektyenile §7— config.yml'i yeniden okur");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 0) return out;
        String kmt = command.getName().toLowerCase(Locale.ROOT);
        boolean admin = sender.hasPermission(ADMIN);
        int n = args.length;
        switch (kmt) {
            case "aura" -> {
                if (n == 1) idler(out, false);
            }
            case "elytra" -> {
                if (n == 1) idler(out, true);
            }
            case "efektliste" -> {
                if (n == 1) {
                    out.add("aura");
                    out.add("elytra");
                }
            }
            case "efekt" -> {
                if (n == 1) out.add("yardim");
            }
            case "efektver" -> {
                if (!admin) return out;
                if (n == 1) for (Player p : Bukkit.getOnlinePlayers()) out.add(p.getName());
                else if (n == 2) {
                    out.add("aura");
                    out.add("elytra");
                } else if (n == 3) idler(out, args[1].equalsIgnoreCase("elytra"));
            }
            default -> {
            }
        }
        String son = args[n - 1].toLowerCase(Locale.ROOT);
        out.removeIf(s -> !s.toLowerCase(Locale.ROOT).startsWith(son));
        return out;
    }

    private static void idler(List<String> out, boolean ely) {
        out.add("kapat");
        if (ely) for (ElytraIzleri.Iz z : ElytraIzleri.ALL) out.add(z.id());
        else for (Auralar.Aura a : Auralar.ALL) out.add(a.id());
    }
}
