package com.hoplite.managers;

import com.hoplite.HoplitePlugin;
import com.hoplite.models.HopliteTeam;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;

public class CageManager {

    private final HoplitePlugin plugin;

    // All glass block locations placed by this manager
    private final Set<Location> cageBlocks = new HashSet<>();

    // playerUUID -> their cage center (floor level)
    private final Map<UUID, Location> playerCageCenter = new HashMap<>();

    // Cage interior is 2x2 blocks wide, 5 blocks tall
    // We build a circular wall around a 2x2 interior using radius 2 from center
    // Interior: center block ± 1 in X and Z (4 floor blocks total)
    // Wall ring: all blocks at radius 2 from center column

    private static final int CAGE_HEIGHT = 5;
    // Wall radius: 2 means the wall is 2 blocks away from the center column
    // giving a 2x2 interior (center ±1) plus 1 block wall thickness
    private static final int WALL_RADIUS = 2;

    public CageManager(HoplitePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Teleport all players in all teams to their cage positions,
     * arranged in a circle of radius cageRadius around the arena center.
     */
    public void placeAllCages(Location arenaCenter) {
        // Collect all online players across all teams
        List<Player> allPlayers = new ArrayList<>();
        for (HopliteTeam team : plugin.getTeamManager().getAllTeams().values()) {
            allPlayers.addAll(plugin.getTeamManager().getOnlineMembers(team));
        }

        int count = allPlayers.size();
        if (count == 0) return;

        int cageRadius = plugin.getConfig().getInt("arena.cage-radius", 50);

        for (int i = 0; i < count; i++) {
            Player player = allPlayers.get(i);

            // Distribute cages evenly around the circle
            double angle = (2 * Math.PI / count) * i;
            double cx = arenaCenter.getX() + cageRadius * Math.cos(angle);
            double cz = arenaCenter.getZ() + cageRadius * Math.sin(angle);

            // Find the surface Y at this position
            World world = arenaCenter.getWorld();
            int surfaceY = world.getHighestBlockYAt((int) cx, (int) cz);

            Location cageCenter = new Location(world, Math.floor(cx) + 0.5, surfaceY + 1, Math.floor(cz) + 0.5);

            // Build the cage
            buildCage(cageCenter);

            // Teleport player to the center of the cage interior (2 blocks above floor)
            Location spawnPoint = cageCenter.clone().add(0, 0, 0);
            spawnPoint.setY(cageCenter.getY()); // stand on the floor
            player.teleport(spawnPoint);

            // Store cage center for this player
            playerCageCenter.put(player.getUniqueId(), cageCenter);

            // Clear inventory — start fresh
            player.getInventory().clear();
            player.getInventory().setHelmet(null);
            player.getInventory().setChestplate(null);
            player.getInventory().setLeggings(null);
            player.getInventory().setBoots(null);

            player.sendMessage(ChatColor.GREEN + "Anda telah dimasukkan ke dalam sangkar. Tunggu permainan bermula!");
        }
    }

    /**
     * Builds a circular glass cage at the given floor-level center location.
     *
     * Layout (top view, # = glass wall, . = air interior):
     *  # # # # #
     *  # . . . #
     *  # . P . #     P = player spawn
     *  # . . . #
     *  # # # # #
     *
     * Interior is 3x3 air (player is at exact center).
     * Wall is 1 block thick glass ring at radius 2.
     * Height: 5 blocks (floor = glass, walls = 5 tall, ceiling = glass).
     */
    private void buildCage(Location center) {
        World world = center.getWorld();
        int bx = center.getBlockX();
        int by = center.getBlockY() - 1; // floor level is one below teleport Y
        int bz = center.getBlockZ();

        for (int dx = -WALL_RADIUS; dx <= WALL_RADIUS; dx++) {
            for (int dz = -WALL_RADIUS; dz <= WALL_RADIUS; dz++) {
                boolean isWallPosition = isWallBlock(dx, dz);

                for (int dy = 0; dy <= CAGE_HEIGHT; dy++) {
                    boolean isFloor = (dy == 0);
                    boolean isCeiling = (dy == CAGE_HEIGHT);

                    if (isFloor || isCeiling) {
                        // Full floor and ceiling
                        if (Math.abs(dx) <= WALL_RADIUS && Math.abs(dz) <= WALL_RADIUS) {
                            placeGlass(world, bx + dx, by + dy, bz + dz);
                        }
                    } else {
                        // Only walls (ring), interior stays air
                        if (isWallPosition) {
                            placeGlass(world, bx + dx, by + dy, bz + dz);
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns true if this (dx, dz) offset should be a wall block.
     * Wall = outermost ring of the 5x5 footprint (radius 2).
     * Interior = the 3x3 center (dx and dz both in -1..1).
     */
    private boolean isWallBlock(int dx, int dz) {
        // If on the edge of the 5x5 square → wall
        return Math.abs(dx) == WALL_RADIUS || Math.abs(dz) == WALL_RADIUS;
    }

    private void placeGlass(World world, int x, int y, int z) {
        Block block = world.getBlockAt(x, y, z);
        // Only place if air (don't overwrite existing terrain)
        if (block.getType() == Material.AIR || block.getType() == Material.CAVE_AIR) {
            block.setType(Material.GLASS, false);
            cageBlocks.add(block.getLocation().toBlockLocation());
        }
    }

    /**
     * Removes all cage glass blocks instantly.
     * Called when countdown hits GO!
     */
    public void removeAllCages() {
        for (Location loc : cageBlocks) {
            Block block = loc.getBlock();
            if (block.getType() == Material.GLASS) {
                block.setType(Material.AIR, false);
            }
        }
        cageBlocks.clear();
        playerCageCenter.clear();
    }

    /**
     * Returns true if the given location is a cage glass block.
     * Used by CageListener to cancel block break events.
     */
    public boolean isCageBlock(Location loc) {
        return cageBlocks.contains(loc.toBlockLocation());
    }

    public boolean hasCages() {
        return !cageBlocks.isEmpty();
    }
}
