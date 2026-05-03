package com.hoplite.listeners;

import com.hoplite.HoplitePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class BorderListener implements Listener {

    private final HoplitePlugin plugin;

    public BorderListener(HoplitePlugin plugin) {
        this.plugin = plugin;
    }

    // Border damage is handled by the scheduled task in BorderManager
    // This listener can be used for additional border-related events if needed

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only check if moved to a different block
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        if (!plugin.getBorderManager().isActive()) return;

        // Visual warning when approaching border (within 5 blocks)
        var player = event.getPlayer();
        var center = plugin.getBorderManager().getCenter();
        if (center == null) return;

        double size = plugin.getBorderManager().getBorderSize();
        double dx = Math.abs(event.getTo().getX() - center.getX());
        double dz = Math.abs(event.getTo().getZ() - center.getZ());
        double nearEdge = size - 5;

        if ((dx >= nearEdge || dz >= nearEdge) && (dx <= size && dz <= size)) {
            player.sendActionBar(net.kyori.adventure.text.Component.text(
                    "⚠ Anda hampir diluar safe zone!",
                    net.kyori.adventure.text.format.NamedTextColor.YELLOW));
        }
    }
}
