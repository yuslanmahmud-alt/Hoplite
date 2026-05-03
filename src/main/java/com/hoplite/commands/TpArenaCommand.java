package com.hoplite.commands;

import com.hoplite.HoplitePlugin;
import com.hoplite.managers.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class TpArenaCommand implements CommandExecutor {

    private final HoplitePlugin plugin;
    public TpArenaCommand(HoplitePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (plugin.getGameManager().getState() == GameManager.State.IN_GAME) {
            sender.sendMessage(ChatColor.RED + "Permainan sedang berjalan! Guna /restart dahulu.");
            return true;
        }

        if (plugin.getTeamManager().getTeamCount() == 0) {
            sender.sendMessage(ChatColor.RED + "Tiada team dibuat! Guna /team create dan /team join dahulu.");
            return true;
        }

        Location center = plugin.getBorderManager().getCenter();
        if (center == null) {
            sender.sendMessage(ChatColor.RED + "Pusat arena belum ditetapkan! Guna /setcenter dahulu.");
            return true;
        }

        int totalPlayers = 0;
        for (var team : plugin.getTeamManager().getAllTeams().values()) {
            totalPlayers += plugin.getTeamManager().getOnlineMembers(team).size();
        }

        if (totalPlayers == 0) {
            sender.sendMessage(ChatColor.RED + "Tiada pemain dalam mana-mana team!");
            return true;
        }

        plugin.getCageManager().placeAllCages(center);

        sender.sendMessage(ChatColor.GREEN + "✔ " + totalPlayers + " pemain telah di-teleport ke sangkar arena!");
        sender.sendMessage(ChatColor.AQUA + "Guna /start untuk memulakan kiraan 3,2,1.");
        return true;
    }
}
