package com.hoplite.commands;

import com.hoplite.HoplitePlugin;
import com.hoplite.managers.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class StartCommand implements CommandExecutor {

    private final HoplitePlugin plugin;

    public StartCommand(HoplitePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        GameManager.GameState state = plugin.getGameManager().getState();

        if (state == GameManager.GameState.IN_GAME) {
            sender.sendMessage(ChatColor.RED + "Permainan sudah bermula!");
            return true;
        }
        if (state == GameManager.GameState.STARTING) {
            sender.sendMessage(ChatColor.RED + "Countdown sudah berjalan!");
            return true;
        }

        int redCount = plugin.getTeamManager().getTeamPlayers("red").size();
        int blueCount = plugin.getTeamManager().getTeamPlayers("blue").size();

        if (redCount == 0 || blueCount == 0) {
            sender.sendMessage(ChatColor.RED + "Kedua-dua team mesti ada pemain! Red: " + redCount + ", Blue: " + blueCount);
            return true;
        }

        sender.sendMessage(ChatColor.GREEN + "Memulakan kiraan undur...");
        plugin.getGameManager().startCountdown();
        return true;
    }
}
