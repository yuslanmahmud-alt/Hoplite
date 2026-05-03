package com.hoplite.commands;

import com.hoplite.HoplitePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.*;

public class BorderCommand implements CommandExecutor {

    private final HoplitePlugin plugin;
    public BorderCommand(HoplitePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) { sendHelp(sender); return true; }

        switch (args[0].toLowerCase()) {

            case "set" -> {
                if (args.length < 2) { sender.sendMessage(ChatColor.RED + "Guna: /border set <saiz>"); return true; }
                try {
                    double size = Double.parseDouble(args[1]);
                    if (size < 10) { sender.sendMessage(ChatColor.RED + "Saiz minimum ialah 10."); return true; }
                    plugin.getBorderManager().setSize(size);
                    sender.sendMessage(ChatColor.GREEN + "✔ Border ditetapkan ke saiz: " + (int)size + " blok");
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Saiz mesti nombor!");
                }
            }

            case "shrink" -> {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Guna: /border shrink <saiz_baru> <masa_saat>");
                    return true;
                }
                try {
                    double target = Double.parseDouble(args[1]);
                    int seconds = Integer.parseInt(args[2]);
                    if (target < 10) { sender.sendMessage(ChatColor.RED + "Saiz minimum ialah 10."); return true; }
                    if (seconds < 1) { sender.sendMessage(ChatColor.RED + "Masa minimum ialah 1 saat."); return true; }
                    if (!plugin.getBorderManager().isActive()) {
                        sender.sendMessage(ChatColor.RED + "Border tidak aktif! Mulakan permainan dahulu.");
                        return true;
                    }
                    plugin.getBorderManager().shrink(target, seconds);
                    sender.sendMessage(ChatColor.GREEN + "✔ Border akan mengecil ke " + (int)target
                        + " blok dalam masa " + seconds + " saat (smooth).");
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Saiz dan masa mesti nombor!");
                }
            }

            case "stop" -> {
                plugin.getBorderManager().stopShrink();
                sender.sendMessage(ChatColor.YELLOW + "Border pengecilan dihentikan.");
            }

            case "info" -> {
                double size = plugin.getBorderManager().getCurrentSize();
                boolean active = plugin.getBorderManager().isActive();
                sender.sendMessage(ChatColor.GOLD + "══ Border Info ══");
                sender.sendMessage(ChatColor.GRAY + "  Status: " + (active ? ChatColor.GREEN + "Aktif" : ChatColor.RED + "Tidak aktif"));
                sender.sendMessage(ChatColor.GRAY + "  Saiz semasa: " + ChatColor.WHITE + (int)size + " blok (radius)");
                sender.sendMessage(ChatColor.GRAY + "  Damage: " + ChatColor.WHITE
                    + plugin.getConfig().getDouble("border.damage-per-second") + " HP/saat");
            }

            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "══ Border Commands ══");
        sender.sendMessage(ChatColor.YELLOW + "/border set <saiz> " + ChatColor.GRAY + "- Tetapkan saiz border");
        sender.sendMessage(ChatColor.YELLOW + "/border shrink <saiz> <saat> " + ChatColor.GRAY + "- Kecilkan border secara smooth");
        sender.sendMessage(ChatColor.YELLOW + "/border stop " + ChatColor.GRAY + "- Hentikan pengecilan");
        sender.sendMessage(ChatColor.YELLOW + "/border info " + ChatColor.GRAY + "- Lihat status border");
    }
}
