package com.thanos.participant2.enemies;

public enum BossType {

    IN_BETWEENER(
            "In-Betweener", 600, 45, 2.0f, 30, "Soul Blast"
    ),

    CHAMPION(
            "Champion", 800, 60, 1.5f, 40, "Power Punch"
    ),

    GARDENER(
            "Gardener", 550, 35, 2.5f, 20, "Time Control"
    ),

    RUNNER(
            "Runner", 500, 50, 4.0f, 25, "Teleport Strike"
    ),

    COLLECTOR(
            "Collector", 650, 40, 1.8f, 35, "Reality Trap"
    ),

    GRANDMASTER(
            "Grandmaster", 700, 45, 2.0f, 30, "Mind Control"
    );

    private final String displayName;
    private final int health;
    private final int damage;
    private final float speed;
    private final int defense;
    private final String abilityName;

    BossType(
            String displayName,
            int health,
            int damage,
            float speed,
            int defense,
            String abilityName
    ) {
        this.displayName = displayName;
        this.health = health;
        this.damage = damage;
        this.speed = speed;
        this.defense = defense;
        this.abilityName = abilityName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getHealth() {
        return health;
    }

    public int getDamage() {
        return damage;
    }

    public float getSpeed() {
        return speed;
    }

    public int getDefense() {
        return defense;
    }

    public String getAbilityName() {
        return abilityName;
    }
}