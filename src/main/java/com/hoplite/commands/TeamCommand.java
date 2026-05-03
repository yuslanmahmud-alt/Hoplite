package com.hoplite.commands;

import com.hoplite.HoplitePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class TeamCommand implements CommandExecutor {

    private final HoplitePlugin plugin;

    public TeamCommand(HoplitePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Hanya pemain boleh menggunakan command ini.");
            return true;
        }

        openTeamGUI(player);
        return true;
    }

    public void openTeamGUI(Player player) {
        Inventory gui = plugin.getServer().createInventory(null, 9, ChatColor.BOLD + "Pilih Team Anda");

        // Red Team item
        ItemStack redItem = new ItemStack(Material.RED_WOOL);
        ItemMeta redMeta = redItem.getItemMeta();
        redMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Team Merah");
        redMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Sertai Team Merah",
                "",
                ChatColor.YELLOW + "Klik untuk pilih!"
        ));
        redItem.setItemMeta(redMeta);

        // Blue Team item
        ItemStack blueItem = new ItemStack(Material.BLUE_WOOL);
        ItemMeta blueMeta = blueItem.getItemMeta();
        blueMeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + "Team Biru");
        blueMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Sertai Team Biru",
                "",
                ChatColor.YELLOW + "Klik untuk pilih!"
        ));
        blueItem.setItemMeta(blueMeta);

        // Current team info
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        String currentTeam = plugin.getTeamManager().getTeam(player);
        infoMeta.setDisplayName(ChatColor.GOLD + "Team Semasa: " +
                (currentTeam != null ? (currentTeam.equals("red") ? ChatColor.RED : ChatColor.BLUE) + currentTeam.toUpperCase() : ChatColor.GRAY + "Tiada"));
        infoMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Pilih team di sebelah kanan atau kiri",
                ChatColor.GRAY + "Class akan diberikan secara rawak"
        ));
        infoItem.setItemMeta(infoMeta);

        gui.setItem(2, redItem);
        gui.setItem(4, infoItem);
        gui.setItem(6, blueItem);

        player.openInventory(gui);
    }
}
