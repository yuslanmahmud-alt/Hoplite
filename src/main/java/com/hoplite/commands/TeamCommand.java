package com.hoplite.commands;

import com.hoplite.HoplitePlugin;
import com.hoplite.models.HopliteTeam;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class TeamCommand implements CommandExecutor {

    private final HoplitePlugin plugin;

    public TeamCommand(HoplitePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Hanya pemain boleh guna command ini.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "create" -> {
                if (plugin.getGameManager().isInGame()) {
                    player.sendMessage(ChatColor.RED + "Tidak boleh buat team semasa permainan sedang berjalan!");
                    return true;
                }
                HopliteTeam team = plugin.getTeamManager().createTeam();
                if (team == null) {
                    player.sendMessage(ChatColor.RED + "Maksimum 5 team sahaja! Tidak boleh buat lagi.");
                } else {
                    for (org.bukkit.entity.Player online : org.bukkit.Bukkit.getOnlinePlayers()) {
                        online.sendMessage(ChatColor.GREEN + team.getColoredName()
                            + ChatColor.GREEN + " telah dibuat oleh " + player.getName() + "!");
                    }
                }
            }

            case "join" -> {
                if (plugin.getGameManager().isInGame()) {
                    player.sendMessage(ChatColor.RED + "Tidak boleh tukar team semasa permainan sedang berjalan!");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Guna: /team join <1-5>");
                    return true;
                }
                int num;
                try {
                    num = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Nombor team mesti antara 1-5.");
                    return true;
                }
                if (plugin.getTeamManager().getTeam(num) == null) {
                    player.sendMessage(ChatColor.RED + "Team " + num + " tidak wujud. Guna /team create dahulu.");
                    return true;
                }
                boolean joined = plugin.getTeamManager().joinTeam(player, num);
                if (joined) {
                    HopliteTeam team = plugin.getTeamManager().getTeam(num);
                    player.sendMessage(ChatColor.GREEN + "Anda telah menyertai " + team.getColoredName() + ChatColor.GREEN + "!");
                    player.sendMessage(ChatColor.GRAY + "Glow hijau akan kelihatan kepada rakan sepasukan dalam 50 blok.");
                } else {
                    player.sendMessage(ChatColor.RED + "Gagal menyertai team.");
                }
            }

            case "leave" -> {
                if (plugin.getGameManager().isInGame()) {
                    player.sendMessage(ChatColor.RED + "Tidak boleh keluar team semasa permainan sedang berjalan!");
                    return true;
                }
                HopliteTeam current = plugin.getTeamManager().getPlayerTeam(player);
                if (current == null) {
                    player.sendMessage(ChatColor.RED + "Anda tidak dalam mana-mana team.");
                } else {
                    plugin.getTeamManager().leaveTeam(player);
                    player.sendMessage(ChatColor.YELLOW + "Anda telah keluar dari " + current.getColoredName() + ChatColor.YELLOW + ".");
                }
            }

            case "list" -> {
                if (plugin.getTeamManager().getTeamCount() == 0) {
                    player.sendMessage(ChatColor.GRAY + "Tiada team dibuat lagi. Guna /team create.");
                    return true;
                }
                player.sendMessage(ChatColor.GOLD + "══ Senarai Team ══");
                for (HopliteTeam team : plugin.getTeamManager().getAllTeams().values()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(team.getColoredName()).append(ChatColor.GRAY).append(" (").append(team.size()).append(" ahli): ");
                    for (org.bukkit.entity.Player p : plugin.getTeamManager().getOnlineMembers(team)) {
                        sb.append(ChatColor.WHITE).append(p.getName()).append(ChatColor.GRAY).append(", ");
                    }
                    String line = sb.toString();
                    if (line.endsWith(", ")) line = line.substring(0, line.length() - 2);
                    player.sendMessage(line);
                }
            }

            default -> sendHelp(player);
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "══ Team Commands ══");
        player.sendMessage(ChatColor.YELLOW + "/team create " + ChatColor.GRAY + "- Buat team baru (max 5)");
        player.sendMessage(ChatColor.YELLOW + "/team join <1-5> " + ChatColor.GRAY + "- Sertai team");
        player.sendMessage(ChatColor.YELLOW + "/team leave " + ChatColor.GRAY + "- Keluar dari team");
        player.sendMessage(ChatColor.YELLOW + "/team list " + ChatColor.GRAY + "- Senarai semua team");
    }
}
