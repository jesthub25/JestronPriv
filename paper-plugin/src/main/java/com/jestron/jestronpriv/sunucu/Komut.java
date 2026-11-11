package com.jestron.jestronpriv.sunucu;

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
 * /jestron (kisaca /jp):
 * menu | aura &lt;id|kapat&gt; | elytra &lt;id|kapat&gt; | kapat | gizle | liste [aura|elytra] | yenile | ver &lt;oyuncu&gt; &lt;aura|elytra&gt; &lt;id|kapat&gt;
 */
final class Komut implements CommandExecutor, TabCompleter {

    private static final String ONEK = JestronSunucu.ONEK;
    private final JestronSunucu plugin;

    Komut(JestronSunucu plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String alt = args.length == 0 ? "menu" : args[0].toLowerCase(Locale.ROOT);
        switch (alt) {
            case "yenile", "reload" -> {
                if (!sender.hasPermission("jestronpriv.admin")) return izinYok(sender);
                plugin.ayarlariOku();
                sender.sendMessage(ONEK + "Ayarlar yeniden okundu.");
                return true;
            }
            case "ver", "set" -> {
                if (!sender.hasPermission("jestronpriv.admin")) return izinYok(sender);
                if (args.length < 4) {
                    sender.sendMessage(ONEK + "§eKullanım: §f/" + label + " ver <oyuncu> <aura|elytra> <id|kapat>");
                    return true;
                }
                Player hedef = Bukkit.getPlayerExact(args[1]);
                if (hedef == null) {
                    sender.sendMessage(ONEK + "§cOyuncu çevrimiçi değil: §f" + args[1]);
                    return true;
                }
                String id = kapat(args[3]) ? null : args[3].toLowerCase(Locale.ROOT);
                boolean ok;
                if (args[2].equalsIgnoreCase("aura")) ok = admin(hedef, () -> plugin.secAura(hedef, id, false), id == null || Auralar.get(id) != null);
                else if (args[2].equalsIgnoreCase("elytra")) ok = admin(hedef, () -> plugin.secIz(hedef, id, false), id == null || ElytraIzleri.get(id) != null);
                else {
                    sender.sendMessage(ONEK + "§cTür §faura §cya da §felytra §colmalı.");
                    return true;
                }
                sender.sendMessage(ok ? ONEK + "§a" + hedef.getName() + " için ayarlandı." : ONEK + "§cBöyle bir öğe yok: §f" + args[3]);
                return true;
            }
            case "liste", "list" -> {
                boolean ely = args.length > 1 && args[1].toLowerCase(Locale.ROOT).startsWith("e");
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
                return true;
            }
            default -> {
            }
        }
        if (!(sender instanceof Player p)) {
            sender.sendMessage(ONEK + "Bu komut oyun içinden kullanılır. Konsol: §f/" + label + " ver | liste | yenile");
            return true;
        }
        if (!p.hasPermission("jestronpriv.use")) return izinYok(p);
        switch (alt) {
            case "menu", "menü", "m" -> plugin.menuler().ana(p);
            case "aura" -> {
                if (args.length < 2) plugin.menuler().auralar(p, 0);
                else plugin.secAura(p, kapat(args[1]) ? null : args[1].toLowerCase(Locale.ROOT), true);
            }
            case "elytra", "iz" -> {
                if (args.length < 2) plugin.menuler().izler(p, 0);
                else plugin.secIz(p, kapat(args[1]) ? null : args[1].toLowerCase(Locale.ROOT), true);
            }
            case "kapat", "off" -> {
                plugin.secAura(p, null, false);
                plugin.secIz(p, null, false);
                p.sendMessage(ONEK + "Aura ve elytra izi kapatıldı.");
            }
            case "gizle", "hide" -> {
                Veri.Secim s = plugin.veri().al(p.getUniqueId());
                s.gizle = !s.gizle;
                plugin.veri().degisti(p.getUniqueId());
                p.sendMessage(ONEK + "Başkalarının efektleri: " + (s.gizle ? "§cgizli" : "§agörünür"));
            }
            case "ayar", "ayarlar" -> plugin.menuler().ayarlar(p);
            default -> yardim(p, label);
        }
        return true;
    }

    private static boolean admin(Player hedef, java.util.function.BooleanSupplier is, boolean gecerli) {
        return gecerli && is.getAsBoolean();
    }

    private static boolean kapat(String s) {
        String k = s.toLowerCase(Locale.ROOT);
        return k.equals("kapat") || k.equals("off") || k.equals("yok") || k.equals("none");
    }

    private static boolean izinYok(CommandSender s) {
        s.sendMessage(ONEK + "§cBunun için iznin yok.");
        return true;
    }

    private void yardim(CommandSender s, String l) {
        s.sendMessage(ONEK + "§fKomutlar:");
        s.sendMessage("§e/" + l + " §7— menüyü açar");
        s.sendMessage("§e/" + l + " aura <ad|kapat> §7— aura seçer");
        s.sendMessage("§e/" + l + " elytra <ad|kapat> §7— elytra izi seçer");
        s.sendMessage("§e/" + l + " gizle §7— başkalarının efektlerini gizler / gösterir");
        s.sendMessage("§e/" + l + " ayar §7— görünürlük ve hız ayarları");
        s.sendMessage("§e/" + l + " kapat §7— hepsini kapatır");
        s.sendMessage("§e/" + l + " liste [aura|elytra] §7— kimlikleri listeler");
        if (s.hasPermission("jestronpriv.admin")) {
            s.sendMessage("§c/" + l + " ver <oyuncu> <aura|elytra> <ad|kapat> §7— başkasına ayarlar");
            s.sendMessage("§c/" + l + " yenile §7— config.yml'i yeniden okur");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        boolean admin = sender.hasPermission("jestronpriv.admin");
        if (args.length == 1) {
            for (String s : new String[]{"menu", "aura", "elytra", "gizle", "ayar", "kapat", "liste"}) out.add(s);
            if (admin) {
                out.add("ver");
                out.add("yenile");
            }
        } else if (args.length == 2) {
            String a = args[0].toLowerCase(Locale.ROOT);
            if (a.equals("aura")) idler(out, false);
            else if (a.equals("elytra") || a.equals("iz")) idler(out, true);
            else if (a.equals("liste")) {
                out.add("aura");
                out.add("elytra");
            } else if (a.equals("ver") && admin) for (Player p : Bukkit.getOnlinePlayers()) out.add(p.getName());
        } else if (args.length == 3 && admin && args[0].equalsIgnoreCase("ver")) {
            out.add("aura");
            out.add("elytra");
        } else if (args.length == 4 && admin && args[0].equalsIgnoreCase("ver")) {
            idler(out, args[2].equalsIgnoreCase("elytra"));
        } else return out;
        String son = args[args.length - 1].toLowerCase(Locale.ROOT);
        out.removeIf(s -> !s.toLowerCase(Locale.ROOT).startsWith(son));
        return out;
    }

    private static void idler(List<String> out, boolean ely) {
        out.add("kapat");
        if (ely) for (ElytraIzleri.Iz z : ElytraIzleri.ALL) out.add(z.id());
        else for (Auralar.Aura a : Auralar.ALL) out.add(a.id());
    }
}
