package com.hoplite.managers;

import com.hoplite.HoplitePlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ArenaManager {

    private final HoplitePlugin plugin;
    private Location arenaCenter;
    private final List<Location> glassBlocks = new ArrayList<>();

    public ArenaManager(HoplitePlugin plugin) {
        this.plugin = plugin;
        loadCenter();
    }

    private void loadCenter() {
        String worldName = plugin.getConfig().getString("arena.center.world", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) world = Bukkit.getWorlds().get(0);

        double x = plugin.getConfig().getDouble("arena.center.x", 0.5);
        double y = plugin.getConfig().getDouble("arena.center.y", 64);
        double z = plugin.getConfig().getDouble("arena.center.z", 0.5);
        arenaCenter = new Location(world, x, y, z);

        plugin.getBorderManager().setCenter(arenaCenter);
    }

    public void setCenter(Location loc) {
        this.arenaCenter = loc;
        plugin.getBorderManager().setCenter(loc);
    }

    /**
     * Teleport players to starting positions around the center, each inside a glass cage.
     * Red team on one side, blue team on the other.
     */
    public void teleportPlayersToArena() {
        List<Player> redPlayers = plugin.getTeamManager().getTeamPlayers("red");
        List<Player> bluePlayers = plugin.getTeamManager().getTeamPlayers("blue");

        int radius = plugin.getConfig().getInt("arena.spawn-radius", 10);

        // Place red players on the north side, blue on south
        placeTeamInCages(redPlayers, arenaCenter, radius, 0);    // north
        placeTeamInCages(bluePlayers, arenaCenter, -radius, 0);  // south
    }

    private void placeTeamInCages(List<Player> players, Location center, int zOffset, int xBase) {
        int spacing = 4;
        int startX = -(players.size() - 1) * spacing / 2;

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            int xOff = startX + (i * spacing);
            Location spawnLoc = center.clone().add(xOff, 0, zOffset);
            spawnLoc.setY(getHighestY(spawnLoc));

            // Build glass cage around spawn
            buildGlassCage(spawnLoc);

            // Teleport player to center of cage
            player.teleport(spawnLoc.clone().add(0.5, 1, 0.5));
            player.sendMessage(ChatColor.GREEN + "Anda telah di-teleport ke arena! Menunggu permainan bermula...");
        }
    }

    private double getHighestY(Location loc) {
        World world = loc.getWorld();
        int highestY = world.getHighestBlockYAt(loc);
        return highestY + 1;
    }

    private void buildGlassCage(Location center) {
        World world = center.getWorld();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        // 3x3x3 cage (hollow)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 0; dy <= 2; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    // Only place walls, not interior
                    if (Math.abs(dx) == 1 || Math.abs(dz) == 1 || dy == 0 || dy == 2) {
                        Block block = world.getBlockAt(cx + dx, cy + dy, cz + dz);
                        if (block.getType() == Material.AIR) {
                            block.setType(Material.GLASS);
                            glassBlocks.add(block.getLocation());
                        }
                    }
                }
            }
        }
    }

    /**
     * Remove all glass cages (called when countdown reaches 0)
     */
    public void removeAllGlassCages() {
        for (Location loc : glassBlocks) {
            Block block = loc.getBlock();
            if (block.getType() == Material.GLASS) {
                block.setType(Material.AIR);
            }
        }
        glassBlocks.clear();
    }

    public Location getArenaCenter() { return arenaCenter; }
}
