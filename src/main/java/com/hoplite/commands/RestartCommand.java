package com.hoplite.commands;

import com.hoplite.HoplitePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;

public class RestartCommand implements CommandExecutor {

    private final HoplitePlugin plugin;
    public RestartCommand(HoplitePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        sender.sendMessage(ChatColor.YELLOW + "Meresetkan permainan...");

        // Broadcast to all players
        for (var p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(ChatColor.RED + "════════════════════════");
            p.sendMessage(ChatColor.YELLOW + "  PERMAINAN TELAH DI-RESET");
            p.sendMessage(ChatColor.GRAY + "  Semua team dibubarkan.");
            p.sendMessage(ChatColor.GRAY + "  Guna /team create untuk mula semula.");
            p.sendMessage(ChatColor.RED + "════════════════════════");
        }

        // Perform the full game reset
        plugin.getGameManager().reset();

        sender.sendMessage(ChatColor.GREEN + "✔ Permainan berjaya di-reset sepenuhnya.");
        return true;
    }
}
