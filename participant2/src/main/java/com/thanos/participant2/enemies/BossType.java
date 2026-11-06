
package com.thanos.participant2.enemies;

public enum BossType {

    IN_BETWEENER(
            "In-Betweener", "Soul Stone", 600, 45, "Soul Blast"
    ),

    CHAMPION(
            "Champion", "Power Stone", 800, 60, "Power Punch"
    ),

    GARDENER(
            "Gardener", "Time Stone", 550, 35, "Time Control"
    ),

    RUNNER(
            "Runner", "Space Stone", 500, 50, "Teleport Strike"
    ),

    COLLECTOR(
            "Collector", "Reality Stone", 650, 40, "Reality Trap"
    ),

    GRANDMASTER(
            "Grandmaster", "Mind Stone", 700, 45, "Mind Control"
    );

    private final String name;
    private final String infinityStone;
    private final float health;
    private final float damage;
    private final String specialAttack;

    BossType(String name, String infinityStone,
             float health, float damage, String specialAttack) {
        this.name = name;
        this.infinityStone = infinityStone;
        this.health = health;
        this.damage = damage;
        this.specialAttack = specialAttack;
    }

    public String getName() {
        return name;
    }

    public String getInfinityStone() {
        return infinityStone;
    }

    public float getHealth() {
        return health;
    }

    public float getDamage() {
        return damage;
    }

    public String getSpecialAttack() {
        return specialAttack;
    }
}
