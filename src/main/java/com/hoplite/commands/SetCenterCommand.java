package com.hoplite.commands;

import com.hoplite.HoplitePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class SetCenterCommand implements CommandExecutor {

    private final HoplitePlugin plugin;
    public SetCenterCommand(HoplitePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Hanya pemain boleh guna command ini.");
            return true;
        }

        plugin.getBorderManager().setCenter(player.getLocation());

        // Save to config
        plugin.getConfig().set("arena.center.world", player.getWorld().getName());
        plugin.getConfig().set("arena.center.x", player.getLocation().getX());
        plugin.getConfig().set("arena.center.y", player.getLocation().getY());
        plugin.getConfig().set("arena.center.z", player.getLocation().getZ());
        plugin.saveConfig();

        player.sendMessage(ChatColor.GREEN + "✔ Pusat arena telah ditetapkan di lokasi anda:");
        player.sendMessage(ChatColor.GRAY + "  World: " + ChatColor.WHITE + player.getWorld().getName());
        player.sendMessage(ChatColor.GRAY + "  X: " + ChatColor.WHITE + String.format("%.1f", player.getLocation().getX())
            + ChatColor.GRAY + "  Y: " + ChatColor.WHITE + String.format("%.1f", player.getLocation().getY())
            + ChatColor.GRAY + "  Z: " + ChatColor.WHITE + String.format("%.1f", player.getLocation().getZ()));
        player.sendMessage(ChatColor.AQUA + "Guna /tparena untuk teleport semua pemain ke sangkar.");
        return true;
    }
}
