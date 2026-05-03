package com.hoplite.listeners;

import com.hoplite.HoplitePlugin;
import com.hoplite.managers.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class TeamListener implements Listener {

    private final HoplitePlugin plugin;

    public TeamListener(HoplitePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().title().toString();
        if (!title.contains("Pilih Team Anda")) return;

        event.setCancelled(true);

        var item = event.getCurrentItem();
        if (item == null) return;

        if (plugin.getGameManager().getState() == GameManager.GameState.IN_GAME) {
            player.sendMessage(ChatColor.RED + "Tidak boleh tukar team semasa permainan sedang berjalan!");
            player.closeInventory();
            return;
        }

        if (item.getType() == Material.RED_WOOL) {
            plugin.getTeamManager().setTeam(player, "red");
            player.closeInventory();
            player.sendMessage(ChatColor.RED + "Anda telah menyertai " + ChatColor.BOLD + "Team Merah" + ChatColor.RED + "!");
            player.sendMessage(ChatColor.GREEN + "Class dan skill telah diberikan secara rawak!");
        } else if (item.getType() == Material.BLUE_WOOL) {
            plugin.getTeamManager().setTeam(player, "blue");
            player.closeInventory();
            player.sendMessage(ChatColor.BLUE + "Anda telah menyertai " + ChatColor.BOLD + "Team Biru" + ChatColor.BLUE + "!");
            player.sendMessage(ChatColor.GREEN + "Class dan skill telah diberikan secara rawak!");
        }
    }
}
