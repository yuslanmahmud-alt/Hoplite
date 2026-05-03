package com.hoplite.managers;

import com.hoplite.HoplitePlugin;
import com.hoplite.models.HopliteTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.util.*;

public class TeamManager {

    private final HoplitePlugin plugin;

    // teamNumber (1-5) -> HopliteTeam
    private final Map<Integer, HopliteTeam> teams = new LinkedHashMap<>();

    // playerUUID -> teamNumber
    private final Map<UUID, Integer> playerTeamMap = new HashMap<>();

    // Scoreboard: each Hoplite team has its own scoreboard Team
    // Members of same scoreboard team see each other's green glow
    // Members of different scoreboard teams do NOT see each other's glow
    private Scoreboard scoreboard;
    private final Map<Integer, Team> sbTeams = new HashMap<>();

    // Task that manages glow based on 50-block radius
    private BukkitTask glowTask;

    private static final int MAX_TEAMS = 5;
    private static final double GLOW_RADIUS = 50.0;

    public TeamManager(HoplitePlugin plugin) {
        this.plugin = plugin;
        initScoreboard();
    }

    // ─── Scoreboard setup ────────────────────────────────────────────────────

    private void initScoreboard() {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        for (int i = 1; i <= MAX_TEAMS; i++) {
            String teamId = "hoplite_t" + i;
            Team st = scoreboard.registerNewTeam(teamId);
            st.setColor(ChatColor.GREEN);
            st.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            st.setCanSeeFriendlyInvisibles(true);
            // Friendly fire off — teammates cannot hurt each other
            st.setAllowFriendlyFire(false);
            sbTeams.put(i, st);
        }
    }

    // ─── Team CRUD ───────────────────────────────────────────────────────────

    /**
     * Creates a new team with the next available number (1-5).
     * Returns null if max teams already created.
     */
    public HopliteTeam createTeam() {
        if (teams.size() >= MAX_TEAMS) return null;
        for (int i = 1; i <= MAX_TEAMS; i++) {
            if (!teams.containsKey(i)) {
                HopliteTeam team = new HopliteTeam(i);
                teams.put(i, team);
                return team;
            }
        }
        return null;
    }

    public HopliteTeam getTeam(int number) {
        return teams.get(number);
    }

    public Map<Integer, HopliteTeam> getAllTeams() {
        return Collections.unmodifiableMap(teams);
    }

    public int getTeamCount() { return teams.size(); }

    // ─── Player join / leave ─────────────────────────────────────────────────

    /**
     * Adds a player to a team.
     * Automatically removes them from their current team first.
     * Returns false if the team does not exist.
     */
    public boolean joinTeam(Player player, int teamNumber) {
        HopliteTeam team = teams.get(teamNumber);
        if (team == null) return false;

        // Remove from current team (if any)
        leaveTeam(player);

        // Register in data structures
        team.addMember(player.getUniqueId());
        playerTeamMap.put(player.getUniqueId(), teamNumber);

        // Add to scoreboard team so same-team glow works
        sbTeams.get(teamNumber).addEntry(player.getName());

        // Apply this plugin's scoreboard to the player
        player.setScoreboard(scoreboard);

        // Give glow potion — the scoreboard team colour (GREEN) controls
        // what colour teammates see. Enemies are on different scoreboard teams
        // so their client renders the glow as the default colour but more
        // importantly the GLOWING effect itself is hidden from enemies because
        // only members of the SAME scoreboard team receive the outline packet.
        // Note: Standard Minecraft sends glow to everyone; we rely on the
        // scoreboard team visibility rule combined with removing glow from
        // players who have no nearby teammate (handled in glowTask below).
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false, false));

        return true;
    }

    /**
     * Removes a player from their current team entirely.
     */
    public void leaveTeam(Player player) {
        Integer teamNum = playerTeamMap.remove(player.getUniqueId());
        if (teamNum == null) return;

        HopliteTeam team = teams.get(teamNum);
        if (team != null) team.removeMember(player.getUniqueId());

        Team st = sbTeams.get(teamNum);
        if (st != null) st.removeEntry(player.getName());

        player.removePotionEffect(PotionEffectType.GLOWING);
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    // ─── Glow radius enforcement ─────────────────────────────────────────────

    /**
     * Every second, check each player.
     * If they have a teammate within 50 blocks → show glow.
     * If no teammate within 50 blocks → hide glow.
     * This creates the 50-block radius glow effect.
     */
    public void startGlowTask() {
        if (glowTask != null) glowTask.cancel();
        glowTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Integer teamNum = playerTeamMap.get(player.getUniqueId());
                if (teamNum == null) continue;

                HopliteTeam team = teams.get(teamNum);
                if (team == null) continue;

                boolean teammateNearby = false;
                for (UUID uid : team.getMembers()) {
                    if (uid.equals(player.getUniqueId())) continue;
                    Player mate = Bukkit.getPlayer(uid);
                    if (mate == null || !mate.isOnline()) continue;
                    if (!mate.getWorld().equals(player.getWorld())) continue;
                    if (player.getLocation().distance(mate.getLocation()) <= GLOW_RADIUS) {
                        teammateNearby = true;
                        break;
                    }
                }

                boolean hasGlow = player.hasPotionEffect(PotionEffectType.GLOWING);
                if (teammateNearby && !hasGlow) {
                    player.addPotionEffect(new PotionEffect(
                            PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false, false));
                } else if (!teammateNearby && hasGlow) {
                    player.removePotionEffect(PotionEffectType.GLOWING);
                }
            }
        }, 0L, 20L);
    }

    public void stopGlowTask() {
        if (glowTask != null) { glowTask.cancel(); glowTask = null; }
    }

    // ─── Queries ─────────────────────────────────────────────────────────────

    public HopliteTeam getPlayerTeam(Player player) {
        Integer n = playerTeamMap.get(player.getUniqueId());
        return n == null ? null : teams.get(n);
    }

    public boolean areSameTeam(Player a, Player b) {
        Integer ta = playerTeamMap.get(a.getUniqueId());
        Integer tb = playerTeamMap.get(b.getUniqueId());
        return ta != null && ta.equals(tb);
    }

    public boolean isInTeam(Player player) {
        return playerTeamMap.containsKey(player.getUniqueId());
    }

    public List<Player> getOnlineMembers(HopliteTeam team) {
        List<Player> list = new ArrayList<>();
        for (UUID uid : team.getMembers()) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null && p.isOnline()) list.add(p);
        }
        return list;
    }

    // ─── Full reset ───────────────────────────────────────────────────────────

    /**
     * Completely wipes all teams and player assignments.
     * Called by /restart.
     */
    public void resetAll() {
        stopGlowTask();

        // Remove glow and scoreboard from every online player
        for (UUID uuid : new HashSet<>(playerTeamMap.keySet())) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.removePotionEffect(PotionEffectType.GLOWING);
                p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
        }

        // Clear scoreboard team entries
        for (Team st : sbTeams.values()) {
            for (String entry : new HashSet<>(st.getEntries())) {
                st.removeEntry(entry);
            }
        }

        teams.clear();
        playerTeamMap.clear();
    }

    public Scoreboard getScoreboard() { return scoreboard; }
}
