package com.thanos.participant3;

/** Test Combatant. */
public final class FakeCombatant implements Combatant {
    private final String name;
    private final int maxHealth;
    private final int defense;
    private int health;

    public FakeCombatant(String name, int health, int maxHealth, int defense) {
        this.name = name;
        this.health = health;
        this.maxHealth = maxHealth;
        this.defense = defense;
    }

    public String name() { return name; }
    public int health() { return health; }
    public int maxHealth() { return maxHealth; }
    public int defense() { return defense; }
    public void takeDamage(int amount) { health = Math.max(0, health - amount); }
    public void heal(int amount) { health = Math.min(maxHealth, health + amount); }
}
