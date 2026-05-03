package com.hoplite.models;

public enum PlayerClass {

    TANK("Tank", 40.0, 0),
    FIGHTER("Fighter", 30.0, 1),
    ARCHER("Archer", 20.0, 2),
    ASSASSIN("Assassin", 18.0, 3);

    private final String displayName;
    private final double maxHealth;
    // Speed amplifier: 0=normal, 1=fast, 2=faster, 3=very fast
    private final int speedAmplifier;

    PlayerClass(String displayName, double maxHealth, int speedAmplifier) {
        this.displayName = displayName;
        this.maxHealth = maxHealth;
        this.speedAmplifier = speedAmplifier;
    }

    public String getDisplayName() { return displayName; }
    public double getMaxHealth() { return maxHealth; }
    public int getSpeedAmplifier() { return speedAmplifier; }
}
