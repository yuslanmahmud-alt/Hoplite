package com.hoplite.managers;

import com.hoplite.HoplitePlugin;
import com.hoplite.models.HopliteTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.List;

public class GameManager {

    public enum State { LOBBY, COUNTDOWN, IN_GAME, ENDED }

    private final HoplitePlugin plugin;
    private State state = State.LOBBY;
    private BukkitTask countdownTask;

    // Spawn location to return players to on restart
    private Location defaultSpawn;

    public GameManager(HoplitePlugin plugin) {
        this.plugin = plugin;
    }

    // ─── Start countdown ──────────────────────────────────────────────────────

    public boolean startCountdown() {
        if (state != State.LOBBY) return false;
        if (plugin.getTeamManager().getTeamCount() == 0) return false;
        state = State.COUNTDOWN;

        countdownTask = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
            int tick = 3;

            @Override
            public void run() {
                if (tick > 0) {
                    showCountdownTitle(tick);
                    tick--;
                } else {
                    // GO!
                    showGoTitle();
                    beginGame();
                    countdownTask.cancel();
                }
            }
        }, 0L, 20L);

        return true;
    }

    private void showCountdownTitle(int number) {
        NamedTextColor color = switch (number) {
            case 3 -> NamedTextColor.RED;
            case 2 -> NamedTextColor.YELLOW;
            case 1 -> NamedTextColor.GREEN;
            default -> NamedTextColor.WHITE;
        };

        Title title = Title.title(
            Component.text(String.valueOf(number), color, TextDecoration.BOLD),
            Component.text("Bersiap sedia...", NamedTextColor.GRAY),
            Title.Times.times(
                Duration.ofMillis(0),
                Duration.ofMillis(950),
                Duration.ofMillis(50))
        );

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showTitle(title);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING,
                SoundCategory.MASTER, 1.0f, number == 1 ? 2.0f : 1.0f);
        }
    }

    private void showGoTitle() {
        Title title = Title.title(
            Component.text("MULA!", NamedTextColor.GREEN, TextDecoration.BOLD),
            Component.text("Selamat berjuang!", NamedTextColor.YELLOW),
            Title.Times.times(
                Duration.ofMillis(100),
                Duration.ofMillis(2000),
                Duration.ofMillis(500))
        );
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showTitle(title);
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL,
                SoundCategory.MASTER, 0.6f, 1.5f);
        }
    }

    private void beginGame() {
        state = State.IN_GAME;

        // Remove all cages instantly
        plugin.getCageManager().removeAllCages();

        // Assign class to every player in a team
        for (HopliteTeam team : plugin.getTeamManager().getAllTeams().values()) {
            for (Player p : plugin.getTeamManager().getOnlineMembers(team)) {
                plugin.getClassManager().assignClass(p);
            }
        }

        // Start glow radius tracking
        plugin.getTeamManager().startGlowTask();

        // Activate border
        plugin.getBorderManager().activate();
    }

    // ─── Death handling ───────────────────────────────────────────────────────

    public void onPlayerDeath(Player player) {
        Location loc = player.getLocation();

        // Visual lightning only (no damage to bystanders)
        loc.getWorld().strikeLightningEffect(loc);

        // Extra flair particles
        loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 3, 0.5, 0.5, 0.5, 0.05);
        loc.getWorld().spawnParticle(Particle.FLASH, loc, 2, 0.2, 0.2, 0.2, 0);

        // Team-coloured death message
        HopliteTeam team = plugin.getTeamManager().getPlayerTeam(player);
        String teamStr = team != null ? team.getColoredName() : ChatColor.GRAY + "?";
        String msg = ChatColor.GRAY + "[" + teamStr + ChatColor.GRAY + "] "
            + ChatColor.WHITE + player.getName()
            + ChatColor.RED + " telah tewas!";

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.sendMessage(msg);
        }

        // Remove from team count and check win
        if (state == State.IN_GAME) {
            plugin.getServer().getScheduler().runTaskLater(plugin, this::checkWin, 5L);
        }
    }

    private void checkWin() {
        if (state != State.IN_GAME) return;

        // Count teams that still have alive players
        List<HopliteTeam> aliveTeams = plugin.getTeamManager().getAllTeams().values().stream()
            .filter(t -> plugin.getTeamManager().getOnlineMembers(t).stream()
                .anyMatch(p -> !p.isDead()))
            .toList();

        if (aliveTeams.size() == 1) {
            endGame(aliveTeams.get(0));
        } else if (aliveTeams.isEmpty()) {
            endGameDraw();
        }
    }

    private void endGame(HopliteTeam winner) {
        state = State.ENDED;
        plugin.getBorderManager().deactivate();
        plugin.getTeamManager().stopGlowTask();

        Title title = Title.title(
            Component.text(winner.getName() + " MENANG!", NamedTextColor.GOLD, TextDecoration.BOLD),
            Component.text("Tahniah kepada pasukan " + winner.getName() + "!", NamedTextColor.YELLOW),
            Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(5000), Duration.ofMillis(1000))
        );

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showTitle(title);
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE,
                SoundCategory.MASTER, 1.0f, 1.0f);
        }
    }

    private void endGameDraw() {
        state = State.ENDED;
        plugin.getBorderManager().deactivate();
        plugin.getTeamManager().stopGlowTask();

        Title title = Title.title(
            Component.text("SERI!", NamedTextColor.WHITE, TextDecoration.BOLD),
            Component.text("Tiada pemenang!", NamedTextColor.GRAY),
            Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(4000), Duration.ofMillis(1000))
        );
        for (Player p : Bukkit.getOnlinePlayers()) p.showTitle(title);
    }

    // ─── Full reset ───────────────────────────────────────────────────────────

    public void reset() {
        if (countdownTask != null) { countdownTask.cancel(); countdownTask = null; }
        state = State.LOBBY;

        // Reset all subsystems
        plugin.getCageManager().removeAllCages();
        plugin.getBorderManager().deactivate();
        plugin.getBorderManager().setSize(plugin.getConfig().getDouble("border.initial-size", 200));
        plugin.getTeamManager().stopGlowTask();
        plugin.getTeamManager().resetAll();
        plugin.getClassManager().resetAll();

        // Teleport all players to world spawn
        Location spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.teleport(spawn);
            p.getInventory().clear();
            p.setMaxHealth(20.0);
            p.setHealth(20.0);
            p.setFoodLevel(20);
            p.sendMessage(ChatColor.YELLOW + "Permainan telah di-reset. Guna /team create dan /team join untuk bermula semula.");
        }
    }

    public State getState() { return state; }
    public boolean isInGame() { return state == State.IN_GAME; }
}
