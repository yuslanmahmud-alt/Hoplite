package com.hoplite;

import com.hoplite.commands.*;
import com.hoplite.listeners.*;
import com.hoplite.managers.*;
import org.bukkit.plugin.java.JavaPlugin;

public class HoplitePlugin extends JavaPlugin {

    private static HoplitePlugin instance;

    private TeamManager teamManager;
    private CageManager cageManager;
    private BorderManager borderManager;
    private ClassManager classManager;
    private GameManager gameManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Init managers in correct dependency order
        teamManager  = new TeamManager(this);
        cageManager  = new CageManager(this);
        borderManager = new BorderManager(this);
        classManager = new ClassManager(this);
        gameManager  = new GameManager(this);

        // Register commands
        getCommand("team").setExecutor(new TeamCommand(this));
        getCommand("setcenter").setExecutor(new SetCenterCommand(this));
        getCommand("tparena").setExecutor(new TpArenaCommand(this));
        getCommand("start").setExecutor(new StartCommand(this));
        getCommand("border").setExecutor(new BorderCommand(this));
        getCommand("restart").setExecutor(new RestartCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new CageListener(this), this);
        getServer().getPluginManager().registerEvents(new TeamListener(this), this);

        getLogger().info("HoplitePlugin v2.0 enabled!");
    }

    @Override
    public void onDisable() {
        if (borderManager != null) borderManager.deactivate();
        if (cageManager != null) cageManager.removeAllCages();
        if (teamManager != null) teamManager.stopGlowTask();
        getLogger().info("HoplitePlugin disabled.");
    }

    public static HoplitePlugin getInstance() { return instance; }
    public TeamManager getTeamManager()  { return teamManager; }
    public CageManager getCageManager()  { return cageManager; }
    public BorderManager getBorderManager() { return borderManager; }
    public ClassManager getClassManager() { return classManager; }
    public GameManager getGameManager()  { return gameManager; }
}
