package com.hoplite.models;

public enum PlayerClass {
    TANK("Tank", 40.0, 0),
    FIGHTER("Fighter", 30.0, 1),
    ARCHER("Archer", 20.0, 2),
    MAGE("Mage", 20.0, 1);

    private final String displayName;
    private final double maxHealth;
    private final int speedLevel;

    PlayerClass(String displayName, double maxHealth, int speedLevel) {
        this.displayName = displayName;
        this.maxHealth = maxHealth;
        this.speedLevel = speedLevel;
    }

    public String getDisplayName() { return displayName; }
    public double getMaxHealth() { return maxHealth; }
    public int getSpeedLevel() { return speedLevel; }
}
