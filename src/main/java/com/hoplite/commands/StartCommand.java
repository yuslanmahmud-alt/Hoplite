package com.hoplite.commands;

import com.hoplite.HoplitePlugin;
import com.hoplite.managers.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.command.*;

public class StartCommand implements CommandExecutor {

    private final HoplitePlugin plugin;
    public StartCommand(HoplitePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        GameManager.State state = plugin.getGameManager().getState();

        if (state == GameManager.State.COUNTDOWN) {
            sender.sendMessage(ChatColor.RED + "Kiraan sudah berjalan!");
            return true;
        }
        if (state == GameManager.State.IN_GAME) {
            sender.sendMessage(ChatColor.RED + "Permainan sudah bermula! Guna /restart untuk reset.");
            return true;
        }
        if (state == GameManager.State.ENDED) {
            sender.sendMessage(ChatColor.RED + "Permainan sudah tamat. Guna /restart untuk reset.");
            return true;
        }

        if (plugin.getTeamManager().getTeamCount() < 2) {
            sender.sendMessage(ChatColor.RED + "Perlukan sekurang-kurangnya 2 team untuk bermain!");
            return true;
        }

        if (!plugin.getCageManager().hasCages()) {
            sender.sendMessage(ChatColor.RED + "Pemain belum di-teleport ke sangkar! Guna /tparena dahulu.");
            return true;
        }

        boolean started = plugin.getGameManager().startCountdown();
        if (started) {
            sender.sendMessage(ChatColor.GREEN + "✔ Kiraan undur dimulakan!");
        } else {
            sender.sendMessage(ChatColor.RED + "Tidak dapat memulakan countdown sekarang.");
        }
        return true;
    }
}
