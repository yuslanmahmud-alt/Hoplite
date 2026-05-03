package com.hoplite.commands;

import com.hoplite.HoplitePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeleportArenaCommand implements CommandExecutor {

    private final HoplitePlugin plugin;

    public TeleportArenaCommand(HoplitePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Set center to sender's location if they are a player
        if (sender instanceof Player player) {
            plugin.getArenaManager().setCenter(player.getLocation());
            sender.sendMessage(ChatColor.GREEN + "Arena center ditetapkan di lokasi anda.");
        }

        int redCount = plugin.getTeamManager().getTeamPlayers("red").size();
        int blueCount = plugin.getTeamManager().getTeamPlayers("blue").size();

        if (redCount == 0 && blueCount == 0) {
            sender.sendMessage(ChatColor.RED + "Tiada pemain dalam mana-mana team! Guna /team dahulu.");
            return true;
        }

        plugin.getArenaManager().teleportPlayersToArena();
        sender.sendMessage(ChatColor.GREEN + "Semua pemain telah di-teleport ke arena!");
        sender.sendMessage(ChatColor.YELLOW + "Red: " + redCount + " | Blue: " + blueCount);
        sender.sendMessage(ChatColor.AQUA + "Guna /start untuk mula kiraan 3,2,1");
        return true;
    }
}
