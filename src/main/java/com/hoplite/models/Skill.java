package com.hoplite.models;

public enum Skill {
    // Tank skills
    SHIELD_WALL("Shield Wall", "Reduces incoming damage by 50% for 5 seconds", PlayerClass.TANK),
    IRON_SKIN("Iron Skin", "Gain 10 bonus armor for 8 seconds", PlayerClass.TANK),
    TAUNT("Taunt", "Force nearby enemies to target you", PlayerClass.TANK),

    // Fighter skills
    BATTLE_CRY("Battle Cry", "Boost attack speed for 6 seconds", PlayerClass.FIGHTER),
    WHIRLWIND("Whirlwind", "Spin and deal damage to all nearby enemies", PlayerClass.FIGHTER),
    BERSERK("Berserk", "Double damage when below 30% HP", PlayerClass.FIGHTER),

    // Archer skills
    RAPID_FIRE("Rapid Fire", "Fire 3 arrows rapidly", PlayerClass.ARCHER),
    EXPLOSIVE_ARROW("Explosive Arrow", "Next arrow explodes on impact", PlayerClass.ARCHER),
    EAGLE_EYE("Eagle Eye", "Increase arrow range and damage", PlayerClass.ARCHER),

    // Mage skills
    FIREBALL("Fireball", "Launch a powerful fireball", PlayerClass.MAGE),
    FROST_BOLT("Frost Bolt", "Slow and damage an enemy", PlayerClass.MAGE),
    ARCANE_SHIELD("Arcane Shield", "Absorb the next hit completely", PlayerClass.MAGE);

    private final String displayName;
    private final String description;
    private final PlayerClass ownerClass;

    Skill(String displayName, String description, PlayerClass ownerClass) {
        this.displayName = displayName;
        this.description = description;
        this.ownerClass = ownerClass;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public PlayerClass getOwnerClass() { return ownerClass; }

    public static Skill[] getSkillsForClass(PlayerClass playerClass) {
        java.util.List<Skill> skills = new java.util.ArrayList<>();
        for (Skill skill : values()) {
            if (skill.ownerClass == playerClass) skills.add(skill);
        }
        return skills.toArray(new Skill[0]);
    }
}
