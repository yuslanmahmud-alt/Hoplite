package com.hoplite.managers;

import com.hoplite.HoplitePlugin;
import com.hoplite.models.PlayerClass;
import com.hoplite.models.Skill;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class ClassManager {

    private final HoplitePlugin plugin;
    private final Map<UUID, Integer> skillCooldowns = new HashMap<>();

    public ClassManager(HoplitePlugin plugin) {
        this.plugin = plugin;
    }

    public void giveClassEquipment(Player player, PlayerClass playerClass) {
        player.getInventory().clear();

        switch (playerClass) {
            case TANK -> {
                // Diamond armor + shield
                player.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
                player.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
                player.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
                player.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
                player.getInventory().setItemInMainHand(createSkillItem(Material.IRON_SWORD, "Tank Sword", player));
                player.getInventory().setItemInOffHand(new ItemStack(Material.SHIELD));
                addSkillItem(player, Material.IRON_BLOCK, "Shield Wall (Right-click)", 1);
                addSkillItem(player, Material.ANVIL, "Iron Skin (Right-click)", 2);
            }
            case FIGHTER -> {
                // Iron armor + sword
                player.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
                player.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                player.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                player.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
                player.getInventory().setItemInMainHand(createSkillItem(Material.DIAMOND_SWORD, "Fighter Blade", player));
                addSkillItem(player, Material.BLAZE_POWDER, "Battle Cry (Right-click)", 1);
                addSkillItem(player, Material.NETHER_STAR, "Whirlwind (Right-click)", 2);
            }
            case ARCHER -> {
                // Leather armor + bow
                player.getInventory().setHelmet(new ItemStack(Material.LEATHER_HELMET));
                player.getInventory().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                player.getInventory().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
                player.getInventory().setBoots(new ItemStack(Material.LEATHER_BOOTS));
                player.getInventory().setItemInMainHand(new ItemStack(Material.BOW));
                player.getInventory().addItem(new ItemStack(Material.ARROW, 64));
                addSkillItem(player, Material.FEATHER, "Rapid Fire (Right-click)", 2);
                addSkillItem(player, Material.TNT, "Explosive Arrow (Right-click)", 3);
            }
            case MAGE -> {
                // Gold armor + stick (wand)
                player.getInventory().setHelmet(new ItemStack(Material.GOLDEN_HELMET));
                player.getInventory().setChestplate(new ItemStack(Material.GOLDEN_CHESTPLATE));
                player.getInventory().setLeggings(new ItemStack(Material.GOLDEN_LEGGINGS));
                player.getInventory().setBoots(new ItemStack(Material.GOLDEN_BOOTS));
                ItemStack wand = new ItemStack(Material.BLAZE_ROD);
                ItemMeta meta = wand.getItemMeta();
                meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Mage Wand");
                wand.setItemMeta(meta);
                player.getInventory().setItemInMainHand(wand);
                addSkillItem(player, Material.FIRE_CHARGE, "Fireball (Right-click)", 1);
                addSkillItem(player, Material.BLUE_ICE, "Frost Bolt (Right-click)", 2);
            }
        }
    }

    private ItemStack createSkillItem(Material mat, String name, Player player) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + name);
        item.setItemMeta(meta);
        return item;
    }

    private void addSkillItem(Player player, Material mat, String name, int slot) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + name);
        item.setItemMeta(meta);
        player.getInventory().setItem(slot, item);
    }

    public void activateSkill(Player player, Skill skill) {
        UUID uuid = player.getUniqueId();

        // Check cooldown
        long now = System.currentTimeMillis();
        if (skillCooldowns.containsKey(uuid)) {
            long elapsed = now - skillCooldowns.get(uuid);
            if (elapsed < 8000) {
                long remaining = (8000 - elapsed) / 1000 + 1;
                player.sendActionBar(ChatColor.RED + "Skill on cooldown! " + remaining + "s");
                return;
            }
        }
        skillCooldowns.put(uuid, (int) now);

        switch (skill) {
            case SHIELD_WALL -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 4));
                player.sendActionBar(ChatColor.GREEN + "Shield Wall activated!");
            }
            case IRON_SKIN -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 160, 2));
                player.sendActionBar(ChatColor.GREEN + "Iron Skin activated!");
            }
            case BATTLE_CRY -> {
                // Buff nearby teammates
                player.getWorld().getNearbyEntities(player.getLocation(), 10, 10, 10).forEach(e -> {
                    if (e instanceof Player nearby) {
                        if (plugin.getTeamManager().areSameTeam(player, nearby)) {
                            nearby.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 120, 1));
                            nearby.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 120, 0));
                        }
                    }
                });
                player.sendActionBar(ChatColor.YELLOW + "Battle Cry! Nearby allies buffed!");
            }
            case WHIRLWIND -> {
                player.getWorld().getNearbyEntities(player.getLocation(), 4, 4, 4).forEach(e -> {
                    if (e instanceof Player nearby && !plugin.getTeamManager().areSameTeam(player, nearby)) {
                        nearby.damage(6.0, player);
                        nearby.setVelocity(nearby.getLocation().subtract(player.getLocation()).toVector().normalize().multiply(1.5).setY(0.5));
                    }
                });
                player.sendActionBar(ChatColor.YELLOW + "Whirlwind!");
            }
            case FIREBALL -> {
                player.launchProjectile(org.bukkit.entity.Fireball.class);
                player.sendActionBar(ChatColor.RED + "Fireball launched!");
            }
            case FROST_BOLT -> {
                player.getWorld().getNearbyEntities(player.getLocation(), 6, 6, 6).forEach(e -> {
                    if (e instanceof Player nearby && !plugin.getTeamManager().areSameTeam(player, nearby)) {
                        nearby.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 3));
                        nearby.damage(4.0, player);
                    }
                });
                player.sendActionBar(ChatColor.AQUA + "Frost Bolt!");
            }
            case RAPID_FIRE -> {
                for (int i = 0; i < 3; i++) {
                    final int delay = i * 5;
                    plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                            player.launchProjectile(org.bukkit.entity.Arrow.class), delay);
                }
                player.sendActionBar(ChatColor.GREEN + "Rapid Fire!");
            }
            case ARCANE_SHIELD -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 3));
                player.sendActionBar(ChatColor.LIGHT_PURPLE + "Arcane Shield activated!");
            }
            default -> player.sendActionBar(ChatColor.GRAY + "Skill activated: " + skill.getDisplayName());
        }
    }
}
