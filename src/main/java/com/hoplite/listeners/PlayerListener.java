package com.hoplite.listeners;

import com.hoplite.HoplitePlugin;
import com.hoplite.managers.GameManager;
import com.hoplite.models.Skill;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.List;

public class PlayerListener implements Listener {

    private final HoplitePlugin plugin;

    public PlayerListener(HoplitePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(ChatColor.GOLD + "=== Selamat datang ke HoplitePlugin! ===");
        player.sendMessage(ChatColor.YELLOW + "Guna /team untuk pilih team anda.");
        player.sendMessage(ChatColor.GRAY + "Taip /hoplite untuk lebih info.");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Remove player from team on quit
        plugin.getTeamManager().removeFromTeam(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        plugin.getGameManager().onPlayerDeath(player);
        event.setDeathMessage(null); // We handle our own message
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;

        // Prevent friendly fire
        if (plugin.getTeamManager().areSameTeam(attacker, victim)) {
            event.setCancelled(true);
            attacker.sendActionBar(ChatColor.RED + "Anda tidak boleh menyerang rakan sepasukan!");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        if (plugin.getGameManager().getState() != GameManager.GameState.IN_GAME) return;

        // Check if player right-clicks a skill item
        var item = player.getInventory().getItemInMainHand();
        if (item == null || item.getItemMeta() == null) return;

        String displayName = item.getItemMeta().getDisplayName();
        if (displayName == null || !displayName.contains("(Right-click)")) return;

        List<Skill> skills = plugin.getTeamManager().getSkills(player);
        if (skills.isEmpty()) return;

        int slot = player.getInventory().getHeldItemSlot();
        // Skill 1 at slot 1, skill 2 at slot 2/3
        Skill toActivate = null;
        if (slot >= 1 && slot - 1 < skills.size()) {
            toActivate = skills.get(slot - 1);
        } else if (!skills.isEmpty()) {
            toActivate = skills.get(0);
        }

        if (toActivate != null) {
            plugin.getClassManager().activateSkill(player, toActivate);
            event.setCancelled(true);
        }
    }
}
