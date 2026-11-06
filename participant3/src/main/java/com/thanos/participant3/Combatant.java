package com.thanos.participant3;

/** Minimal fighter contract; replace with participant 2's shared type once it exists. */
public interface Combatant {
    String name();
    int health();
    int maxHealth();
    int defense();
    void takeDamage(int amount);
    void heal(int amount);

    default boolean isAlive() { return health() > 0; }
}
