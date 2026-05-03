package com.hoplite.managers;

import com.hoplite.HoplitePlugin;
import com.hoplite.models.PlayerClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class ClassManager {

    private final HoplitePlugin plugin;
    private final Map<UUID, PlayerClass> playerClasses = new HashMap<>();
    private final Map<UUID, Long> stealthCooldown = new HashMap<>();
    private final Map<UUID, BukkitTask> stealthTasks = new HashMap<>();
    private static final Random RANDOM = new Random();

    public ClassManager(HoplitePlugin plugin) {
        this.plugin = plugin;
    }

    public void assignClass(Player player) {
        PlayerClass[] classes = PlayerClass.values();
        PlayerClass assigned = classes[RANDOM.nextInt(classes.length)];
        playerClasses.put(player.getUniqueId(), assigned);
        applyClassStats(player, assigned);
        giveClassEquipment(player, assigned);

        player.sendMessage(ChatColor.GOLD + "═══════════════════════");
        player.sendMessage(ChatColor.YELLOW + "  Class: " + ChatColor.WHITE + ChatColor.BOLD + assigned.getDisplayName());
        player.sendMessage(ChatColor.GRAY + "  Max HP: " + ChatColor.RED + (int)(assigned.getMaxHealth() / 2) + " ❤");
        if (assigned == PlayerClass.ASSASSIN) {
            player.sendMessage(ChatColor.GRAY + "  Skill: " + ChatColor.DARK_PURPLE + "Sneak → Invisible 10 saat");
            player.sendMessage(ChatColor.GRAY + "  Note: " + ChatColor.YELLOW + "Invisible hilang bila anda attack!");
        }
        player.sendMessage(ChatColor.GOLD + "═══════════════════════");
    }

    private void applyClassStats(Player player, PlayerClass pc) {
        player.setMaxHealth(pc.getMaxHealth());
        player.setHealth(pc.getMaxHealth());
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.RESISTANCE);
        if (pc.getSpeedAmplifier() > 0) {
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED, Integer.MAX_VALUE,
                pc.getSpeedAmplifier() - 1, false, false, false));
        }
        if (pc == PlayerClass.TANK) {
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 1, false, false, false));
        }
    }

    private void giveClassEquipment(Player player, PlayerClass pc) {
        player.getInventory().clear();
        switch (pc) {
            case TANK -> {
                player.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
                player.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
                player.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
                player.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
                player.getInventory().setItemInMainHand(named(Material.IRON_SWORD, ChatColor.GRAY + "Tank Sword"));
                player.getInventory().setItemInOffHand(new ItemStack(Material.SHIELD));
            }
            case FIGHTER -> {
                player.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
                player.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                player.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                player.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
                player.getInventory().setItemInMainHand(named(Material.DIAMOND_SWORD, ChatColor.AQUA + "Fighter Blade"));
            }
            case ARCHER -> {
                player.getInventory().setHelmet(new ItemStack(Material.LEATHER_HELMET));
                player.getInventory().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                player.getInventory().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
                player.getInventory().setBoots(new ItemStack(Material.LEATHER_BOOTS));
                player.getInventory().setItemInMainHand(named(Material.BOW, ChatColor.GREEN + "Hunter Bow"));
                player.getInventory().addItem(new ItemStack(Material.ARROW, 32));
            }
            case ASSASSIN -> {
                player.getInventory().setHelmet(new ItemStack(Material.LEATHER_HELMET));
                player.getInventory().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                player.getInventory().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
                player.getInventory().setBoots(new ItemStack(Material.LEATHER_BOOTS));
                player.getInventory().setItemInMainHand(named(Material.STONE_SWORD, ChatColor.DARK_PURPLE + "Shadow Blade"));
            }
        }
    }

    private ItemStack named(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.setDisplayName(name); item.setItemMeta(meta); }
        return item;
    }

    public void tryActivateStealth(Player player) {
        if (getPlayerClass(player) != PlayerClass.ASSASSIN) return;
        int cooldownSec = plugin.getConfig().getInt("assassin.cooldown", 20);
        int durationSec = plugin.getConfig().getInt("assassin.invisible-duration", 10);

        Long lastUsed = stealthCooldown.get(player.getUniqueId());
        if (lastUsed != null) {
            long elapsed = (System.currentTimeMillis() - lastUsed) / 1000L;
            if (elapsed < cooldownSec) {
                long remaining = cooldownSec - elapsed;
                player.sendActionBar(Component.text("⏳ Stealth cooldown: " + remaining + " saat", NamedTextColor.RED));
                return;
            }
        }

        if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) return;

        stealthCooldown.put(player.getUniqueId(), System.currentTimeMillis());
        player.addPotionEffect(new PotionEffect(
            PotionEffectType.INVISIBILITY, durationSec * 20, 0, false, false, true));
        player.sendActionBar(Component.text("⚫ Stealth aktif! (" + durationSec + " saat)", NamedTextColor.DARK_PURPLE));

        BukkitTask old = stealthTasks.remove(player.getUniqueId());
        if (old != null) old.cancel();

        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
                player.sendActionBar(Component.text("Stealth tamat.", NamedTextColor.GRAY));
            }
            stealthTasks.remove(player.getUniqueId());
        }, durationSec * 20L);

        stealthTasks.put(player.getUniqueId(), task);
    }

    public void breakStealth(Player player) {
        if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY)) return;
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.sendActionBar(Component.text("💥 Stealth terputus!", NamedTextColor.YELLOW));
        BukkitTask task = stealthTasks.remove(player.getUniqueId());
        if (task != null) task.cancel();
    }

    public PlayerClass getPlayerClass(Player player) {
        return playerClasses.get(player.getUniqueId());
    }

    public void resetAll() {
        for (BukkitTask t : stealthTasks.values()) t.cancel();
        stealthTasks.clear();
        stealthCooldown.clear();
        for (UUID uuid : new HashSet<>(playerClasses.keySet())) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.removePotionEffect(PotionEffectType.SPEED);
                p.removePotionEffect(PotionEffectType.RESISTANCE);
                p.removePotionEffect(PotionEffectType.INVISIBILITY);
                p.setMaxHealth(20.0);
                p.setHealth(20.0);
                p.getInventory().clear();
            }
        }
        playerClasses.clear();
    }
}
