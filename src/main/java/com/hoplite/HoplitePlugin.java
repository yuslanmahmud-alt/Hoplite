package com.hoplite;

import com.hoplite.commands.*;
import com.hoplite.listeners.*;
import com.hoplite.managers.*;
import org.bukkit.plugin.java.JavaPlugin;

public class HoplitePlugin extends JavaPlugin {

    private static HoplitePlugin instance;
    private TeamManager teamManager;
    private ClassManager classManager;
    private BorderManager borderManager;
    private GameManager gameManager;
    private ArenaManager arenaManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Init managers
        teamManager = new TeamManager(this);
        classManager = new ClassManager(this);
        borderManager = new BorderManager(this);
        gameManager = new GameManager(this);
        arenaManager = new ArenaManager(this);

        // Register commands
        getCommand("team").setExecutor(new TeamCommand(this));
        getCommand("teleport-arena").setExecutor(new TeleportArenaCommand(this));
        getCommand("start").setExecutor(new StartCommand(this));
        getCommand("border").setExecutor(new BorderCommand(this));
        getCommand("hoplite").setExecutor(new HopliteCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new BorderListener(this), this);
        getServer().getPluginManager().registerEvents(new TeamListener(this), this);

        getLogger().info("HoplitePlugin enabled!");
    }

    @Override
    public void onDisable() {
        if (borderManager != null) borderManager.stopShrinking();
        getLogger().info("HoplitePlugin disabled!");
    }

    public static HoplitePlugin getInstance() { return instance; }
    public TeamManager getTeamManager() { return teamManager; }
    public ClassManager getClassManager() { return classManager; }
    public BorderManager getBorderManager() { return borderManager; }
    public GameManager getGameManager() { return gameManager; }
    public ArenaManager getArenaManager() { return arenaManager; }
}
