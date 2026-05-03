package com.hoplite.managers;

import com.hoplite.HoplitePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.List;

public class GameManager {

    public enum GameState {
        LOBBY, STARTING, IN_GAME, ENDED
    }

    private final HoplitePlugin plugin;
    private GameState state = GameState.LOBBY;
    private BukkitTask countdownTask;

    public GameManager(HoplitePlugin plugin) {
        this.plugin = plugin;
    }

    public void startCountdown() {
        if (state == GameState.STARTING || state == GameState.IN_GAME) {
            Bukkit.getOnlinePlayers().forEach(p ->
                    p.sendMessage(ChatColor.RED + "Permainan sudah bermula atau dalam countdown!"));
            return;
        }

        state = GameState.STARTING;

        // Give equipment to all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            var playerClass = plugin.getTeamManager().getClass(player);
            if (playerClass != null) {
                plugin.getClassManager().giveClassEquipment(player, playerClass);
            }
        }

        countdownTask = new BukkitRunnable() {
            int count = 3;

            @Override
            public void run() {
                if (count > 0) {
                    showCountdown(count);
                    count--;
                } else {
                    showGo();
                    plugin.getArenaManager().removeAllGlassCages();
                    plugin.getBorderManager().activate();
                    state = GameState.IN_GAME;
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void showCountdown(int number) {
        NamedTextColor color = switch (number) {
            case 3 -> NamedTextColor.RED;
            case 2 -> NamedTextColor.YELLOW;
            case 1 -> NamedTextColor.GREEN;
            default -> NamedTextColor.WHITE;
        };

        Title title = Title.title(
                Component.text(String.valueOf(number), color, TextDecoration.BOLD),
                Component.text("Bersiap...", NamedTextColor.GRAY),
                Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(900), Duration.ofMillis(100))
        );

        Sound sound = Sound.BLOCK_NOTE_BLOCK_PLING;

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showTitle(title);
            p.playSound(p.getLocation(), sound, 1.0f, number == 1 ? 2.0f : 1.0f);
        }
    }

    private void showGo() {
        Title title = Title.title(
                Component.text("MULA!", NamedTextColor.GREEN, TextDecoration.BOLD),
                Component.text("Bunuh musuh anda!", NamedTextColor.YELLOW),
                Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(1500), Duration.ofMillis(400))
        );

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showTitle(title);
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.5f);
        }
    }

    /**
     * Called when a player dies - strike lightning effect at their location
     */
    public void onPlayerDeath(Player player) {
        Location loc = player.getLocation();
        World world = loc.getWorld();

        // Visual lightning (no damage)
        world.strikeLightningEffect(loc);

        // Extra particles for flair
        world.spawnParticle(Particle.EXPLOSION, loc, 5, 0.5, 0.5, 0.5, 0.1);
        world.spawnParticle(Particle.FLASH, loc, 3, 0.3, 0.3, 0.3, 0);

        // Announce death
        String teamName = plugin.getTeamManager().getTeam(player);
        ChatColor teamColor = teamName != null && teamName.equals("red") ? ChatColor.RED : ChatColor.BLUE;
        String msg = ChatColor.GRAY + "[" + teamColor + (teamName != null ? teamName.toUpperCase() : "?") + ChatColor.GRAY + "] "
                + player.getName() + ChatColor.RED + " telah kalah!";

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.sendMessage(msg);
        }

        // Check win condition
        checkWinCondition();
    }

    private void checkWinCondition() {
        List<Player> redAlive = plugin.getTeamManager().getTeamPlayers("red")
                .stream().filter(p -> p.isOnline() && !p.isDead()).toList();
        List<Player> blueAlive = plugin.getTeamManager().getTeamPlayers("blue")
                .stream().filter(p -> p.isOnline() && !p.isDead()).toList();

        if (redAlive.isEmpty() && !blueAlive.isEmpty()) {
            endGame("blue");
        } else if (blueAlive.isEmpty() && !redAlive.isEmpty()) {
            endGame("red");
        }
    }

    private void endGame(String winnerTeam) {
        state = GameState.ENDED;
        plugin.getBorderManager().deactivate();

        ChatColor color = winnerTeam.equals("red") ? ChatColor.RED : ChatColor.BLUE;
        NamedTextColor nColor = winnerTeam.equals("red") ? NamedTextColor.RED : NamedTextColor.BLUE;

        Title winTitle = Title.title(
                Component.text(winnerTeam.toUpperCase() + " TEAM MENANG!", nColor, TextDecoration.BOLD),
                Component.text("Tahniah kepada pasukan " + winnerTeam + "!", NamedTextColor.YELLOW),
                Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(5000), Duration.ofMillis(1000))
        );

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showTitle(winTitle);
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
    }

    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }
}
