package com.thanos.participant3;

/** Minimal fighter contract until participant 2's shared type exists. */
public interface Combatant {
    String name();
    int health();
    int maxHealth();
    int defense();
    void takeDamage(int amount);
    void heal(int amount);

    default boolean isAlive() { return health() > 0; }
}
