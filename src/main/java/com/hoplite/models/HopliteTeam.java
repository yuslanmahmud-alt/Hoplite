package com.hoplite.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HopliteTeam {

    // Chat colour codes for team numbers 1-5
    private static final String[] CHAT_COLORS = {
        "\u00a7c",  // 1 = red
        "\u00a79",  // 2 = cyan/blue
        "\u00a7e",  // 3 = yellow
        "\u00a7d",  // 4 = light purple
        "\u00a76"   // 5 = gold/orange
    };

    private final int number;
    private final String name;
    private final List<UUID> members = new ArrayList<>();

    public HopliteTeam(int number) {
        this.number = number;
        this.name = "Team " + number;
    }

    public int getNumber() { return number; }
    public String getName() { return name; }

    public String getChatColor() {
        return CHAT_COLORS[Math.min(number - 1, CHAT_COLORS.length - 1)];
    }

    public String getColoredName() {
        return getChatColor() + name + "\u00a7r";
    }

    public List<UUID> getMembers() { return members; }

    public void addMember(UUID uuid) {
        if (!members.contains(uuid)) members.add(uuid);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public boolean hasMember(UUID uuid) {
        return members.contains(uuid);
    }

    public int size() { return members.size(); }
}
