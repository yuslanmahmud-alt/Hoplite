package com.hoplite.listeners;

import com.hoplite.HoplitePlugin;
import com.hoplite.managers.GameManager;
import com.hoplite.models.PlayerClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

public class PlayerListener implements Listener {

    private final HoplitePlugin plugin;

    public PlayerListener(HoplitePlugin plugin) { this.plugin = plugin; }

    // ─── Join / Quit ──────────────────────────────────────────────────────────

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(ChatColor.GOLD + "════ HoplitePlugin v2.0 ════");
        player.sendMessage(ChatColor.YELLOW + "Guna " + ChatColor.WHITE + "/team create"
            + ChatColor.YELLOW + " untuk buat team.");
        player.sendMessage(ChatColor.YELLOW + "Guna " + ChatColor.WHITE + "/team join <1-5>"
            + ChatColor.YELLOW + " untuk sertai team.");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getTeamManager().leaveTeam(event.getPlayer());
    }

    // ─── Friendly fire prevention ─────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        Player attacker = null;

        if (event.getDamager() instanceof Player p) {
            attacker = p;
        } else if (event.getDamager() instanceof Arrow arrow
                && arrow.getShooter() instanceof Player p) {
            attacker = p;
        }

        if (attacker == null) return;

        // No PvP in lobby/countdown
        if (plugin.getGameManager().getState() != GameManager.State.IN_GAME) {
            event.setCancelled(true);
            return;
        }

        // No friendly fire
        if (plugin.getTeamManager().areSameTeam(attacker, victim)) {
            event.setCancelled(true);
            attacker.sendActionBar(Component.text(
                "Anda tidak boleh menyerang rakan sepasukan!", NamedTextColor.RED));
            return;
        }

        // Assassin: break stealth when they attack
        if (plugin.getClassManager().getPlayerClass(attacker) == PlayerClass.ASSASSIN) {
            plugin.getClassManager().breakStealth(attacker);
        }
    }

    // ─── Death with lightning ─────────────────────────────────────────────────

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setDeathMessage(null); // We handle our own message
        plugin.getGameManager().onPlayerDeath(event.getEntity());
    }

    // ─── Assassin stealth on sneak ────────────────────────────────────────────

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;
        if (plugin.getGameManager().getState() != GameManager.State.IN_GAME) return;

        Player player = event.getPlayer();
        if (plugin.getClassManager().getPlayerClass(player) == PlayerClass.ASSASSIN) {
            plugin.getClassManager().tryActivateStealth(player);
        }
    }

    // ─── Border proximity warning ─────────────────────────────────────────────

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (plugin.getGameManager().getState() != GameManager.State.IN_GAME) return;
        if (!plugin.getBorderManager().isActive()) return;

        // Only check when moving to a new block
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
            && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();
        var center = plugin.getBorderManager().getCenter();
        if (center == null) return;

        double size = plugin.getBorderManager().getCurrentSize();
        double dx = Math.abs(event.getTo().getX() - center.getX());
        double dz = Math.abs(event.getTo().getZ() - center.getZ());
        double warning = size - 10; // warn at 10 blocks from edge

        if ((dx >= warning || dz >= warning) && dx <= size && dz <= size) {
            player.sendActionBar(Component.text(
                "⚠ Anda hampir keluar safe zone!", NamedTextColor.YELLOW));
        }
    }

    // ─── Prevent respawn exploit ──────────────────────────────────────────────

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // If game is in progress and player is in a team, keep them spectating
        if (plugin.getGameManager().getState() == GameManager.State.IN_GAME) {
            event.setRespawnLocation(plugin.getBorderManager().getCenter() != null
                ? plugin.getBorderManager().getCenter()
                : event.getRespawnLocation());
        }
    }
}
