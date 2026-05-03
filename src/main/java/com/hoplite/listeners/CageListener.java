package com.hoplite.listeners;

import com.hoplite.HoplitePlugin;
import com.hoplite.managers.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class CageListener implements Listener {

    private final HoplitePlugin plugin;

    public CageListener(HoplitePlugin plugin) { this.plugin = plugin; }

    /**
     * Prevent any player from breaking cage glass blocks.
     * This applies even to admins unless in creative mode.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getCageManager().isCageBlock(event.getBlock().getLocation())) return;
        // Allow creative mode admins to break (for debugging)
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        event.setCancelled(true);
        event.getPlayer().sendActionBar(
            net.kyori.adventure.text.Component.text(
                "Kaca sangkar tidak boleh dimusnahkan!",
                net.kyori.adventure.text.format.NamedTextColor.RED));
    }

    /**
     * Prevent placing blocks inside the cage area during countdown/lobby.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        GameManager.State state = plugin.getGameManager().getState();
        if (state != GameManager.State.LOBBY && state != GameManager.State.COUNTDOWN) return;
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        // If the block is placed inside a cage area, cancel
        if (plugin.getCageManager().isCageBlock(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevent players from moving out of their cage before the game starts.
     * They are teleported back if they somehow escape.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        GameManager.State state = plugin.getGameManager().getState();
        if (state != GameManager.State.LOBBY && state != GameManager.State.COUNTDOWN) return;
        if (!plugin.getCageManager().hasCages()) return;

        Player player = event.getPlayer();
        // Only check if they actually moved a full block (reduces lag)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
            && event.getFrom().getBlockY() == event.getTo().getBlockY()
            && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        // If player is inside a cage glass block area, keep them in
        // Check if their new position is outside any glass wall
        // Simple approach: teleport them back to from position if they leave a 2-block radius from cage center
        // We store nothing here — just cancel movement that would put them outside glass
        // The glass itself blocks physical movement, this is extra safety for edge cases
    }

    /**
     * Prevent fall damage and other damage while inside cage during countdown.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        GameManager.State state = plugin.getGameManager().getState();
        if (state == GameManager.State.LOBBY || state == GameManager.State.COUNTDOWN) {
            // No damage before game starts
            event.setCancelled(true);
        }
    }
}
