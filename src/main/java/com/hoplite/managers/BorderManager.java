package com.hoplite.managers;

import com.hoplite.HoplitePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;

public class BorderManager {

    private final HoplitePlugin plugin;

    private Location center;
    private double currentSize;
    private boolean active = false;

    // Task that applies damage to players outside border every second
    private BukkitTask damageTask;

    public BorderManager(HoplitePlugin plugin) {
        this.plugin = plugin;
        this.currentSize = plugin.getConfig().getDouble("border.initial-size", 200);
        loadCenter();
    }

    private void loadCenter() {
        String worldName = plugin.getConfig().getString("arena.center.world", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) world = Bukkit.getWorlds().get(0);
        double x = plugin.getConfig().getDouble("arena.center.x", 0.5);
        double y = plugin.getConfig().getDouble("arena.center.y", 64);
        double z = plugin.getConfig().getDouble("arena.center.z", 0.5);
        center = new Location(world, x, y, z);
    }

    public void setCenter(Location loc) {
        this.center = loc.clone();
    }

    // ─── Activate / Deactivate ────────────────────────────────────────────────

    public void activate() {
        if (center == null) return;
        active = true;

        // Apply world border visual
        WorldBorder wb = center.getWorld().getWorldBorder();
        wb.setCenter(center);
        // WorldBorder.setSize uses DIAMETER, so multiply by 2
        wb.setSize(currentSize * 2);
        // Disable built-in damage — we handle it ourselves for custom messages
        wb.setDamageAmount(0.0);
        wb.setDamageBuffer(0.0);
        wb.setWarningDistance(5);
        wb.setWarningTime(15);

        startDamageTask();
    }

    public void deactivate() {
        active = false;
        stopDamageTask();
        if (center != null) {
            center.getWorld().getWorldBorder().reset();
        }
    }

    // ─── Smooth shrink ────────────────────────────────────────────────────────

    /**
     * Smoothly shrinks the border to targetSize over durationSeconds seconds.
     * Uses WorldBorder's built-in lerp animation — completely smooth on client side.
     * Also shows a title notification on all players' screens.
     */
    public void shrink(double targetSize, int durationSeconds) {
        if (center == null || !active) return;

        currentSize = targetSize;

        // WorldBorder.setSize(diameter, seconds) handles smooth animation natively
        // This is the correct Paper/Spigot API for smooth border movement
        WorldBorder wb = center.getWorld().getWorldBorder();
        wb.setSize(targetSize * 2, durationSeconds);

        // Show notification on all players' screens (title, not chat)
        showShrinkNotification(targetSize, durationSeconds);
    }

    public void stopShrink() {
        if (center == null) return;
        // Freeze border at current visual size by setting to current size instantly
        WorldBorder wb = center.getWorld().getWorldBorder();
        double currentDiameter = wb.getSize();
        wb.setSize(currentDiameter);
        currentSize = currentDiameter / 2.0;
    }

    // ─── Damage task ──────────────────────────────────────────────────────────

    private void startDamageTask() {
        stopDamageTask();
        double damagePerSecond = plugin.getConfig().getDouble("border.damage-per-second", 0.5);

        damageTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!active) return;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!isInsideBorder(p.getLocation())) {
                    p.damage(damagePerSecond);
                    p.sendActionBar(
                        Component.text("⚠ Anda diluar safe zone! -" + damagePerSecond + " HP/saat", NamedTextColor.RED));
                }
            }
        }, 20L, 20L);
    }

    private void stopDamageTask() {
        if (damageTask != null) {
            damageTask.cancel();
            damageTask = null;
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Returns true if the location is inside the current border.
     * We read the world border size directly so it stays accurate during smooth shrink.
     */
    public boolean isInsideBorder(Location loc) {
        if (center == null) return true;
        if (!loc.getWorld().equals(center.getWorld())) return false;
        WorldBorder wb = center.getWorld().getWorldBorder();
        double half = wb.getSize() / 2.0;
        double dx = Math.abs(loc.getX() - center.getX());
        double dz = Math.abs(loc.getZ() - center.getZ());
        return dx <= half && dz <= half;
    }

    private void showShrinkNotification(double targetSize, int seconds) {
        Title title = Title.title(
            Component.text("⚠ BORDER MENGECIL ⚠", NamedTextColor.YELLOW, TextDecoration.BOLD),
            Component.text("Saiz baru: " + (int) targetSize + " blok  |  Masa: " + seconds + " saat",
                NamedTextColor.RED),
            Title.Times.times(
                Duration.ofMillis(300),
                Duration.ofMillis(3500),
                Duration.ofMillis(700))
        );
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showTitle(title);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
        }
    }

    // ─── Setters / Getters ────────────────────────────────────────────────────

    public void setSize(double size) {
        currentSize = size;
        if (center != null) {
            center.getWorld().getWorldBorder().setSize(size * 2);
        }
    }

    public double getCurrentSize() { return currentSize; }
    public boolean isActive() { return active; }
    public Location getCenter() { return center; }
}
