package com.hoplite.commands;

import com.hoplite.HoplitePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BorderCommand implements CommandExecutor {

    private final HoplitePlugin plugin;

    public BorderCommand(HoplitePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "size" -> {
                if (args.length < 2) { sender.sendMessage(ChatColor.RED + "Guna: /border size <saiz>"); return true; }
                try {
                    double size = Double.parseDouble(args[1]);
                    plugin.getBorderManager().setBorderSize(size);
                    sender.sendMessage(ChatColor.GREEN + "Border saiz ditetapkan kepada: " + size);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Saiz mesti nombor!");
                }
            }
            case "shrink" -> {
                if (args.length < 3) { sender.sendMessage(ChatColor.RED + "Guna: /border shrink <saiz> <masa_saat>"); return true; }
                try {
                    double target = Double.parseDouble(args[1]);
                    int seconds = Integer.parseInt(args[2]);
                    plugin.getBorderManager().shrinkBorder(target, seconds);
                    sender.sendMessage(ChatColor.GREEN + "Border akan mengecil kepada " + target + " dalam " + seconds + " saat!");
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Saiz dan masa mesti nombor!");
                }
            }
            case "stop" -> {
                plugin.getBorderManager().stopShrinking();
                sender.sendMessage(ChatColor.YELLOW + "Border shrinking dihentikan.");
            }
            case "activate" -> {
                plugin.getBorderManager().activate();
                sender.sendMessage(ChatColor.GREEN + "Border diaktifkan!");
            }
            case "deactivate" -> {
                plugin.getBorderManager().deactivate();
                sender.sendMessage(ChatColor.YELLOW + "Border dinyahaktifkan.");
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Border Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/border size <saiz>" + ChatColor.GRAY + " - Tetapkan saiz border");
        sender.sendMessage(ChatColor.YELLOW + "/border shrink <saiz> <saat>" + ChatColor.GRAY + " - Kecilkan border secara beransur");
        sender.sendMessage(ChatColor.YELLOW + "/border stop" + ChatColor.GRAY + " - Hentikan pengecilan");
        sender.sendMessage(ChatColor.YELLOW + "/border activate" + ChatColor.GRAY + " - Aktifkan border damage");
        sender.sendMessage(ChatColor.YELLOW + "/border deactivate" + ChatColor.GRAY + " - Nyahaktifkan border");
        sender.sendMessage(ChatColor.GRAY + "Saiz semasa: " + ChatColor.WHITE + plugin.getBorderManager().getBorderSize());
    }
}
