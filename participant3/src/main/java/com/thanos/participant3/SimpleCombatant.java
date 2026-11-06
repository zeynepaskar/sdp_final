package com.thanos.participant3;

/** Plain fighter with health and defense; stands in for participant 2's characters until they exist. */
public final class SimpleCombatant implements Combatant {
    private final String name;
    private final int maxHealth;
    private final int defense;
    private int health;

    public SimpleCombatant(String name, int maxHealth, int defense) {
        if (name == null || name.isBlank() || maxHealth <= 0 || defense < 0) {
            throw new IllegalArgumentException("Fighter needs a name, positive health and nonnegative defense");
        }
        this.name = name;
        this.maxHealth = maxHealth;
        this.defense = defense;
        health = maxHealth;
    }

    public String name() { return name; }
    public int health() { return health; }
    public int maxHealth() { return maxHealth; }
    public int defense() { return defense; }

    public void takeDamage(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Damage must be nonnegative");
        }
        health = Math.max(0, health - amount);
    }

    public void heal(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Healing must be nonnegative");
        }
        health = Math.min(maxHealth, health + amount);
    }
}
