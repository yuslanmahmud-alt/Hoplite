package com.hoplite.managers;

import com.hoplite.HoplitePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;

public class BorderManager {

    private final HoplitePlugin plugin;
    private double borderSize;
    private Location center;
    private BukkitTask damageTask;
    private BukkitTask particleTask;
    private BukkitTask shrinkTask;
    private boolean active = false;

    public BorderManager(HoplitePlugin plugin) {
        this.plugin = plugin;
        this.borderSize = plugin.getConfig().getDouble("border.initial-size", 100);
    }

    public void setCenter(Location center) {
        this.center = center;
    }

    public void setBorderSize(double size) {
        this.borderSize = size;
        // Use world border for visual
        if (center != null) {
            center.getWorld().getWorldBorder().setCenter(center);
            center.getWorld().getWorldBorder().setSize(size * 2);
        }
    }

    public void activate() {
        if (center == null) {
            // Default to world spawn
            center = plugin.getServer().getWorlds().get(0).getSpawnLocation();
        }
        active = true;

        // Setup world border (for visual)
        WorldBorder wb = center.getWorld().getWorldBorder();
        wb.setCenter(center);
        wb.setSize(borderSize * 2);
        wb.setDamageAmount(0); // We handle damage ourselves
        wb.setDamageBuffer(0);

        // Start damage task
        double damagePerTick = plugin.getConfig().getDouble("border.damage-per-tick", 0.5);
        damageTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!active) { cancel(); return; }
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!isInsideBorder(p.getLocation())) {
                        p.damage(damagePerTick);
                        p.sendActionBar(Component.text("⚠ Anda diluar safe zone! Anda menerima damage!", NamedTextColor.RED));
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);

        // Start border particle visualization
        particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!active) { cancel(); return; }
                drawBorderParticles();
            }
        }.runTaskTimer(plugin, 0L, 10L);

        plugin.getLogger().info("Border activated at size: " + borderSize);
    }

    public void shrinkBorder(double targetSize, int seconds) {
        if (shrinkTask != null) shrinkTask.cancel();

        double startSize = borderSize;
        double diff = startSize - targetSize;
        double perTick = diff / (seconds * 20.0);

        // Show notification on all players' screens
        showBorderNotification(targetSize, seconds);

        shrinkTask = new BukkitRunnable() {
            int ticks = 0;
            final int totalTicks = seconds * 20;

            @Override
            public void run() {
                if (!active || ticks >= totalTicks) {
                    setBorderSize(targetSize);
                    cancel();
                    return;
                }
                borderSize -= perTick;
                if (borderSize < targetSize) borderSize = targetSize;

                // Update world border
                if (center != null) {
                    center.getWorld().getWorldBorder().setSize(borderSize * 2);
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void showBorderNotification(double targetSize, int seconds) {
        Title title = Title.title(
                Component.text("⚠ BORDER MENGECIL ⚠", NamedTextColor.YELLOW),
                Component.text("Saiz baru: " + (int)targetSize + " | Masa: " + seconds + "s", NamedTextColor.RED),
                Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(500))
        );
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showTitle(title);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
        }
    }

    private void drawBorderParticles() {
        if (center == null) return;
        World world = center.getWorld();
        double cx = center.getX();
        double cz = center.getZ();
        double y = center.getY();

        // Get min Y of nearby players for better visualization
        double minY = y;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getWorld().equals(world)) {
                minY = Math.min(minY, p.getLocation().getY());
            }
        }

        double half = borderSize;

        // Draw 4 sides as particle lines at player eye level
        for (double offset = -half; offset <= half; offset += 2.0) {
            // North wall
            spawnBorderParticle(world, cx + offset, minY + 1, cz - half);
            // South wall
            spawnBorderParticle(world, cx + offset, minY + 1, cz + half);
            // West wall
            spawnBorderParticle(world, cx - half, minY + 1, cz + offset);
            // East wall
            spawnBorderParticle(world, cx + half, minY + 1, cz + offset);
        }
    }

    private void spawnBorderParticle(World world, double x, double y, double z) {
        // Only spawn if any player is close enough to see it
        Location loc = new Location(world, x, y, z);
        for (Player p : world.getPlayers()) {
            if (p.getLocation().distanceSquared(loc) < 2500) { // 50 blocks radius
                p.spawnParticle(Particle.DUST,
                        loc, 1,
                        new Particle.DustOptions(Color.fromRGB(255, 80, 0), 1.5f));
                break;
            }
        }
    }

    public boolean isInsideBorder(Location loc) {
        if (center == null) return true;
        if (!loc.getWorld().equals(center.getWorld())) return false;
        double dx = Math.abs(loc.getX() - center.getX());
        double dz = Math.abs(loc.getZ() - center.getZ());
        return dx <= borderSize && dz <= borderSize;
    }

    public void stopShrinking() {
        if (shrinkTask != null) { shrinkTask.cancel(); shrinkTask = null; }
    }

    public void deactivate() {
        active = false;
        if (damageTask != null) { damageTask.cancel(); damageTask = null; }
        if (particleTask != null) { particleTask.cancel(); particleTask = null; }
        stopShrinking();
        if (center != null) {
            center.getWorld().getWorldBorder().reset();
        }
    }

    public double getBorderSize() { return borderSize; }
    public boolean isActive() { return active; }
    public Location getCenter() { return center; }
}
