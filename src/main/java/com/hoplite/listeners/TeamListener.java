package com.hoplite.listeners;

import com.hoplite.HoplitePlugin;
import org.bukkit.event.Listener;

/**
 * Reserved for future team-specific events.
 * Team GUI click handling etc. can be added here.
 */
public class TeamListener implements Listener {

    private final HoplitePlugin plugin;

    public TeamListener(HoplitePlugin plugin) {
        this.plugin = plugin;
    }
}
