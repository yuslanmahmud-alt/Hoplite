package com.hoplite.managers;

import com.hoplite.HoplitePlugin;
import com.hoplite.models.PlayerClass;
import com.hoplite.models.Skill;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.*;

import java.util.*;

public class TeamManager {

    private final HoplitePlugin plugin;
    private final Map<UUID, String> playerTeams = new HashMap<>();       // uuid -> "red"/"blue"
    private final Map<UUID, PlayerClass> playerClasses = new HashMap<>();
    private final Map<UUID, List<Skill>> playerSkills = new HashMap<>();
    private Scoreboard scoreboard;
    private Team redTeam, blueTeam;

    public TeamManager(HoplitePlugin plugin) {
        this.plugin = plugin;
        setupScoreboard();
    }

    private void setupScoreboard() {
        ScoreboardManager sm = plugin.getServer().getScoreboardManager();
        scoreboard = sm.getNewScoreboard();

        redTeam = scoreboard.registerNewTeam("red_team");
        redTeam.setColor(ChatColor.RED);
        redTeam.setPrefix(ChatColor.RED + "[Red] ");
        redTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        // Red team sees own outline only
        redTeam.setCanSeeFriendlyInvisibles(true);

        blueTeam = scoreboard.registerNewTeam("blue_team");
        blueTeam.setColor(ChatColor.BLUE);
        blueTeam.setPrefix(ChatColor.BLUE + "[Blue] ");
        blueTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        blueTeam.setCanSeeFriendlyInvisibles(true);
    }

    public boolean setTeam(Player player, String team) {
        String t = team.toLowerCase();
        if (!t.equals("red") && !t.equals("blue")) return false;

        // Remove from old team
        removeFromTeam(player);

        playerTeams.put(player.getUniqueId(), t);

        // Add to scoreboard team
        if (t.equals("red")) {
            redTeam.addEntry(player.getName());
        } else {
            blueTeam.addEntry(player.getName());
        }

        // Apply scoreboard to player
        player.setScoreboard(scoreboard);

        // Apply green glowing effect to player (visible to teammates only via scoreboard trick)
        applyTeamGlow(player);

        // Assign random class & skills
        assignRandomClass(player);

        return true;
    }

    private void applyTeamGlow(Player player) {
        // Give glowing effect - teammates see green outline via scoreboard color
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false));
    }

    public void assignRandomClass(Player player) {
        PlayerClass[] classes = PlayerClass.values();
        PlayerClass assigned = classes[new Random().nextInt(classes.length)];
        playerClasses.put(player.getUniqueId(), assigned);

        // Assign 2 random skills from that class
        Skill[] classSkills = Skill.getSkillsForClass(assigned);
        List<Skill> allSkills = new ArrayList<>(Arrays.asList(classSkills));
        Collections.shuffle(allSkills);
        List<Skill> assigned2 = allSkills.subList(0, Math.min(2, allSkills.size()));
        playerSkills.put(player.getUniqueId(), new ArrayList<>(assigned2));

        // Apply class stats
        player.setMaxHealth(assigned.getMaxHealth());
        player.setHealth(assigned.getMaxHealth());

        // Apply speed
        if (assigned.getSpeedLevel() > 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,
                    Integer.MAX_VALUE, assigned.getSpeedLevel() - 1, false, false));
        }

        // Notify player
        player.sendMessage(ChatColor.GOLD + "=== Class Assigned ===");
        player.sendMessage(ChatColor.YELLOW + "Class: " + ChatColor.WHITE + assigned.getDisplayName());
        player.sendMessage(ChatColor.YELLOW + "Skills:");
        for (Skill s : assigned2) {
            player.sendMessage(ChatColor.AQUA + "  - " + s.getDisplayName() + ": " + ChatColor.GRAY + s.getDescription());
        }
    }

    public void removeFromTeam(Player player) {
        String old = playerTeams.remove(player.getUniqueId());
        if (old != null) {
            if (old.equals("red")) redTeam.removeEntry(player.getName());
            else blueTeam.removeEntry(player.getName());
        }
        player.removePotionEffect(PotionEffectType.GLOWING);
    }

    public String getTeam(Player player) {
        return playerTeams.get(player.getUniqueId());
    }

    public PlayerClass getClass(Player player) {
        return playerClasses.get(player.getUniqueId());
    }

    public List<Skill> getSkills(Player player) {
        return playerSkills.getOrDefault(player.getUniqueId(), new ArrayList<>());
    }

    public List<Player> getTeamPlayers(String team) {
        List<Player> result = new ArrayList<>();
        for (Map.Entry<UUID, String> e : playerTeams.entrySet()) {
            if (e.getValue().equalsIgnoreCase(team)) {
                Player p = plugin.getServer().getPlayer(e.getKey());
                if (p != null && p.isOnline()) result.add(p);
            }
        }
        return result;
    }

    public boolean areSameTeam(Player a, Player b) {
        String ta = playerTeams.get(a.getUniqueId());
        String tb = playerTeams.get(b.getUniqueId());
        return ta != null && ta.equals(tb);
    }

    public Map<UUID, String> getAllTeams() { return playerTeams; }
    public Scoreboard getScoreboard() { return scoreboard; }

    public void clearAll() {
        for (UUID uuid : new HashSet<>(playerTeams.keySet())) {
            Player p = plugin.getServer().getPlayer(uuid);
            if (p != null) removeFromTeam(p);
        }
        playerTeams.clear();
        playerClasses.clear();
        playerSkills.clear();
    }
}
