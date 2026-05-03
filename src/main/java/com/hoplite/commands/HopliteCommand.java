package com.hoplite.commands;

import com.hoplite.HoplitePlugin;
import com.hoplite.models.PlayerClass;
import com.hoplite.models.Skill;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class HopliteCommand implements CommandExecutor {

    private final HoplitePlugin plugin;

    public HopliteCommand(HoplitePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            printAdminHelp(sender);
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("info")) {
            showPlayerInfo(player);
            return true;
        }

        player.sendMessage(ChatColor.GOLD + "====== HoplitePlugin ======");
        player.sendMessage(ChatColor.YELLOW + "/team " + ChatColor.GRAY + "- Pilih team anda");
        player.sendMessage(ChatColor.YELLOW + "/hoplite info " + ChatColor.GRAY + "- Lihat class dan skill anda");

        String team = plugin.getTeamManager().getTeam(player);
        PlayerClass pc = plugin.getTeamManager().getClass(player);
        if (team != null) {
            ChatColor tc = team.equals("red") ? ChatColor.RED : ChatColor.BLUE;
            player.sendMessage(ChatColor.GRAY + "Team: " + tc + team.toUpperCase());
        }
        if (pc != null) {
            player.sendMessage(ChatColor.GRAY + "Class: " + ChatColor.WHITE + pc.getDisplayName());
        }

        return true;
    }

    private void showPlayerInfo(Player player) {
        String team = plugin.getTeamManager().getTeam(player);
        PlayerClass pc = plugin.getTeamManager().getClass(player);
        List<Skill> skills = plugin.getTeamManager().getSkills(player);

        player.sendMessage(ChatColor.GOLD + "=== Maklumat Anda ===");
        if (team != null) {
            ChatColor tc = team.equals("red") ? ChatColor.RED : ChatColor.BLUE;
            player.sendMessage(ChatColor.YELLOW + "Team: " + tc + team.toUpperCase());
        } else {
            player.sendMessage(ChatColor.YELLOW + "Team: " + ChatColor.GRAY + "Belum dipilih");
        }
        if (pc != null) {
            player.sendMessage(ChatColor.YELLOW + "Class: " + ChatColor.WHITE + pc.getDisplayName());
            player.sendMessage(ChatColor.YELLOW + "Max HP: " + ChatColor.WHITE + (int)pc.getMaxHealth());
        }
        if (!skills.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Skills:");
            for (Skill s : skills) {
                player.sendMessage(ChatColor.AQUA + "  • " + s.getDisplayName() + ": " + ChatColor.GRAY + s.getDescription());
            }
        }
    }

    private void printAdminHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "====== Hoplite Admin ======");
        sender.sendMessage(ChatColor.YELLOW + "/teleport-arena" + ChatColor.GRAY + " - Teleport semua ke arena");
        sender.sendMessage(ChatColor.YELLOW + "/start" + ChatColor.GRAY + " - Mulakan countdown 3,2,1");
        sender.sendMessage(ChatColor.YELLOW + "/border <size|shrink|stop|activate>" + ChatColor.GRAY + " - Urus border");
    }
}
